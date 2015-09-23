from __future__ import absolute_import, unicode_literals

import datetime
import logging
import pytz

from antlr4 import InputStream, CommonTokenStream, ParseTreeVisitor, Token
from antlr4.error.Errors import ParseCancellationException, NoViableAltException
from antlr4.error.ErrorStrategy import BailErrorStrategy
from decimal import Decimal
from enum import Enum
from . import conversions, EvaluationError
from .dates import DateStyle, DateParser
from .functions import FunctionManager, custom, excel
from .utils import decimal_pow, urlquote

logger = logging.getLogger(__name__)


DEFAULT_FUNCTION_MANAGER = FunctionManager()
DEFAULT_FUNCTION_MANAGER.add_library(excel)
DEFAULT_FUNCTION_MANAGER.add_library(custom)


class EvaluationContext(object):
    """
    Evaluation context, i.e. variables and date options
    """
    def __init__(self, variables=None, timezone=pytz.UTC, date_style=DateStyle.DAY_FIRST):
        self.variables = variables if variables is not None else {}
        self.timezone = timezone
        self.date_style = date_style

    @classmethod
    def from_json(cls, json_obj):
        variables = json_obj['variables']
        timezone = pytz.timezone(json_obj['timezone'])
        date_style = DateStyle.DAY_FIRST if json_obj['date_style'] == 'day_first' else DateStyle.MONTH_FIRST
        return EvaluationContext(variables, timezone, date_style)

    def resolve_variable(self, path):
        return self._resolve_variable_in_container(self.variables, path.lower(), path)

    def put_variable(self, key, value):
        self.variables[key.lower()] = value

    def get_date_format(self, inc_time):
        if self.date_style == DateStyle.DAY_FIRST:
            pattern = "%d-%m-%Y"
        else:
            pattern = "%m-%d-%Y"

        if inc_time:
            pattern += " %H:%M"

        return pattern

    def get_date_parser(self):
        return DateParser(datetime.date.today(), self.timezone, self.date_style)

    def _resolve_variable_in_container(self, container, path, original_path):
        if '.' in path:
            (item, remaining_path) = path.split('.', 1)
        else:
            item = path
            remaining_path = None

        if item not in container:
            raise EvaluationError("Undefined variable: %s" % original_path)

        value = container[item]

        if remaining_path is not None and value is not None:
            if not isinstance(value, dict):
                raise ValueError("Context lookup into non-dict container")

            return self._resolve_variable_in_container(value, remaining_path, original_path)

        elif isinstance(value, dict):
            if '*' in value:
                return value['*']
            elif '__default__' in value:
                return value['__default__']
            else:
                raise ValueError("Context contains map without default key")
        else:
            return value


class EvaluationStrategy(Enum):
    COMPLETE = 1           # evaluate all expressions completely
    RESOLVE_AVAILABLE = 2  # if expression contains missing context references, just substitute what variables we have


class State(Enum):
    """
    Temba templates support 2 forms of embedded expression:
      1. Single variable, e.g. @contact, @contact.name (delimited by character type or end of input)
      2. Contained expression, e.g. @(SUM(1, 2) + 2) (delimited by balanced parentheses)
    """
    BODY = 0            # not in a expression
    PREFIX = 1          # '=' prefix that denotes the start of an expression
    IDENTIFIER = 2      # the identifier part, e.g. 'contact.age' in '@contact.age'
    BALANCED = 3        # the balanced parentheses delimited part, e.g. '(1 + 2)' in '@(1 + 2)'
    STRING_LITERAL = 4  # a string literal
    ESCAPED_PREFIX = 5  # a '@' prefix preceded by another '@'


class Evaluator(object):

    def __init__(self, expression_prefix='@', allowed_top_levels=(), function_manager=DEFAULT_FUNCTION_MANAGER):
        """
        Creates a new evaluator
        :param expression_prefix: the prefix for expressions, e.g. @
        :param allowed_top_levels: top-level context items allowed outside of parentheses, e.g. contact, flow
        :param function_manager: the function manager to use
        """
        self._expression_prefix = expression_prefix
        self._allowed_top_levels = set(allowed_top_levels)
        self._function_manager = function_manager

    def evaluate_template(self, template, context, url_encode=False, strategy=EvaluationStrategy.COMPLETE):
        """
        Evaluates a template string, e.g. "Hello @contact.name you have @(contact.reports * 2) reports"
        :param template: the template string
        :param context: the evaluation context
        :param url_encode: whether or not values should be URL encoded
        :return: a tuple of the evaluated template and a list of evaluation errors
        """
        input_chars = list(template)
        output_chars = []
        errors = []
        state = State.BODY
        current_expression_chars = []
        current_expression_terminated = False
        parentheses_level = 0

        # determines whether the given character is a word character, i.e. \w in a regex
        is_word_char = lambda c: c and (c.isalnum() or c == '_')

        for pos, ch in enumerate(input_chars):
            # in order to determine if the b in a.b terminates an identifier, we have to peek two characters ahead as it
            # could be a.b. (b terminates) or a.b.c (b doesn't terminate)
            next_ch = input_chars[pos + 1] if (pos < (len(input_chars) - 1)) else None
            next_next_ch = input_chars[pos + 2] if (pos < (len(input_chars) - 2)) else None

            if state == State.BODY:
                if ch == self._expression_prefix and (is_word_char(next_ch) or next_ch == '('):
                    state = State.PREFIX
                    current_expression_chars = [ch]
                elif ch == self._expression_prefix and next_ch == self._expression_prefix:
                    state = State.ESCAPED_PREFIX
                else:
                    output_chars.append(ch)

            elif state == State.PREFIX:
                if is_word_char(ch):
                    # we're parsing an expression like @XXX
                    state = State.IDENTIFIER
                elif ch == '(':
                    # we're parsing an expression like @(1 + 2)
                    state = State.BALANCED
                    parentheses_level += 1

                current_expression_chars.append(ch)

            elif state == State.IDENTIFIER:
                current_expression_chars.append(ch)

            elif state == State.BALANCED:
                if ch == '(':
                    parentheses_level += 1
                elif ch == ')':
                    parentheses_level -= 1
                elif ch == '"':
                    state = State.STRING_LITERAL

                current_expression_chars.append(ch)

                # expression terminates if parentheses balance
                if parentheses_level == 0:
                    current_expression_terminated = True

            elif state == State.STRING_LITERAL:
                if ch == '"':
                    state = State.BALANCED
                current_expression_chars.append(ch)

            elif state == State.ESCAPED_PREFIX:
                state = State.BODY
                output_chars.append(ch)

            # identifier can terminate expression in 3 ways:
            #  1. next char is null (i.e. end of the input)
            #  2. next char is not a word character or period
            #  3. next char is a period, but it's not followed by a word character
            if state == State.IDENTIFIER:
                if not next_ch or (not is_word_char(next_ch) and next_ch != '.') or (next_ch == '.' and not is_word_char(next_next_ch)):
                    current_expression_terminated = True

            if current_expression_terminated:
                expression = ''.join(current_expression_chars)
                output_chars.append(self._resolve_expression_block(expression, context, url_encode, strategy, errors))
                current_expression_chars = []
                current_expression_terminated = False
                state = State.BODY

        # if last expression didn't terminate - add to output as is
        if not current_expression_terminated and current_expression_chars:
            output_chars.append(''.join(current_expression_chars))

        output = ''.join(output_chars)  # joining is fastest way to build strings in Python
        return output, errors

    def _resolve_expression_block(self, expression, context, url_encode, strategy, errors):
        """
        Resolves an expression block found in the template, e.g. @(...). If an evaluation error occurs, expression is
        returned as is.
        """
        try:
            body = expression[1:]  # strip prefix

            # if expression doesn't start with ( then check it's an allowed top level context reference
            if not body.startswith('('):
                top_level = body.split('.')[0].lower()
                if top_level not in self._allowed_top_levels:
                    return expression

            evaluated = self.evaluate_expression(body, context, strategy)

            # convert result to string
            result = conversions.to_string(evaluated, context)

            return urlquote(result) if url_encode else result
        except EvaluationError, e:
            logger.debug("EvaluationError: %s" % e.message)

            # if we can't evaluate expression, include it as is in the output
            errors.append(e.message)
            return expression

    def evaluate_expression(self, expression, context, strategy=EvaluationStrategy.COMPLETE):
        """
        Evaluates a single expression, e.g. "contact.reports * 2"
        :param expression: the expression string
        :param context: the evaluation context
        :param strategy: the evaluation strategy
        :return: the evaluated expression value
        """
        from .gen.ExcellentLexer import ExcellentLexer
        from .gen.ExcellentParser import ExcellentParser

        stream = InputStream(expression)
        lexer = ExcellentLexer(stream)
        tokens = CommonTokenStream(lexer)

        parser = ExcellentParser(tokens)
        parser._errHandler = BailErrorStrategy()

        try:
            tree = parser.parse()

            if logger.isEnabledFor(logging.DEBUG):
                logger.debug("Expression '%s' parsed as %s" % (expression, tree.toStringTree()))
        except ParseCancellationException, ex:
            message = None
            if ex.args and isinstance(ex.args[0], NoViableAltException):
                token = ex.args[0].offendingToken
                if token is not None and token.type != ExcellentParser.EOF:
                    message = "Expression error at: %s" % token.text

            if message is None:
                message = "Expression is invalid"

            raise EvaluationError(message, ex)

        if strategy == EvaluationStrategy.RESOLVE_AVAILABLE:
            resolved = self._resolve_available(tokens, context)
            if resolved is not None:
                return resolved

        visitor = ExcellentVisitor(self._function_manager, context)
        return visitor.visit(tree)

    def _resolve_available(self, tokens, context):
        """
        Checks the token stream for context references and if there are missing references - substitutes available
        references and returns a partially evaluated expression.
        :param tokens: the token stream (all tokens fetched)
        :param context: the evaluation context
        :return: the partially evaluated expression or none if expression can be fully evaluated
        """
        from .gen.ExcellentParser import ExcellentParser

        has_missing = False
        output_components = []

        for t in range(len(tokens.tokens) - 1):  # we can ignore the final EOF token
            token = tokens.get(t)
            next_token = tokens.get(t + 1)

            # if token is a NAME not followed by ( then it's a context reference
            if token.type == ExcellentParser.NAME and next_token.type != ExcellentParser.LPAREN:
                try:
                    output_components.append(context.resolve_variable(token.text))
                except EvaluationError:
                    has_missing = True
                    output_components.append(token)
            else:
                output_components.append(token)

        # if we don't have missing context references, perform evaluation as normal
        if not has_missing:
            return None

        # re-combine the tokens and context values back into an expression
        output = [self._expression_prefix]

        for output_component in output_components:
            if isinstance(output_component, Token):
                comp_val = output_component.text
            else:
                comp_val = conversions.to_repr(output_component, context)
            output.append(comp_val)

        return ''.join(output)


class ExcellentVisitor(ParseTreeVisitor):

    def __init__(self, functions, eval_context):
        self._functions = functions
        self._eval_context = eval_context

    def visitParse(self, ctx):
        return self.visit(ctx.expression())

    def visitFunctionCall(self, ctx):
        """
        expression : fnname LPAREN parameters? RPAREN
        """
        func_name = ctx.fnname().getText()

        if ctx.parameters() is not None:
            parameters = self.visit(ctx.parameters())
        else:
            parameters = []

        return self._functions.invoke_function(self._eval_context, func_name, parameters)

    def visitFunctionParameters(self, ctx):
        """
        parameters : expression (COMMA expression)*
        """
        return [self.visit(expression) for expression in ctx.expression()]

    def visitNegation(self, ctx):
        """
        expression: MINUS expression
        """
        return -conversions.to_decimal(self.visit(ctx.expression()), self._eval_context)

    def visitExponentExpression(self, ctx):
        """
        expression: expression EXPONENT expression
        """
        arg1 = conversions.to_decimal(self.visit(ctx.expression(0)), self._eval_context)
        arg2 = conversions.to_decimal(self.visit(ctx.expression(1)), self._eval_context)
        return conversions.to_decimal(decimal_pow(arg1, arg2), ctx)

    def visitMultiplicationOrDivisionExpression(self, ctx):
        """
        expression: expression (TIMES | DIVIDE) expression
        """
        is_mul = ctx.TIMES() is not None

        arg1 = conversions.to_decimal(self.visit(ctx.expression(0)), self._eval_context)
        arg2 = conversions.to_decimal(self.visit(ctx.expression(1)), self._eval_context)

        if not is_mul and arg2 == Decimal(0):
            raise EvaluationError("Division by zero")

        return arg1 * arg2 if is_mul else arg1 / arg2

    def visitAdditionOrSubtractionExpression(self, ctx):
        """
        expression: expression (PLUS | MINUS) expression
        """
        is_add = ctx.PLUS() is not None
        arg1 = self.visit(ctx.expression(0))
        arg2 = self.visit(ctx.expression(1))

        # first try as decimals
        try:
            _arg1 = conversions.to_decimal(arg1, self._eval_context)
            _arg2 = conversions.to_decimal(arg2, self._eval_context)
            return _arg1 + _arg2 if is_add else _arg1 - _arg2
        except EvaluationError:
            pass

        # then as date + something
        try:
            _arg1 = conversions.to_date_or_datetime(arg1, self._eval_context)

            if isinstance(arg2, datetime.time):
                # upgrade our date to datetime
                _arg1 = conversions.to_datetime(_arg1, self._eval_context)

                # convert time value to a duration
                _arg2 = datetime.timedelta(hours=arg2.hour, minutes=arg2.minute, seconds=arg2.second, microseconds=arg2.microsecond)
            else:
                _arg2 = datetime.timedelta(days=conversions.to_integer(arg2, self._eval_context))

            return _arg1 + _arg2 if is_add else _arg1 - _arg2

        except EvaluationError, ex:
            raise EvaluationError("Expression could not be evaluated as decimal or date arithmetic", ex)

    def visitComparisonExpression(self, ctx):
        """
        expression: expression (LTE | LT | GTE | GT) expression
        """
        arg1, arg2 = conversions.to_same(self.visit(ctx.expression(0)), self.visit(ctx.expression(1)), self._eval_context)

        if isinstance(arg1, basestring):
            # string comparison is case-insensitive
            compared = cmp(arg1.lower(), arg2.lower())
        else:
            compared = cmp(arg1, arg2)

        if ctx.LTE() is not None:
            return compared <= 0
        elif ctx.LT() is not None:
            return compared < 0
        elif ctx.GTE() is not None:
            return compared >= 0
        else:  # GT
            return compared > 0

    def visitEqualityExpression(self, ctx):
        """
        expression: expression (EQ | NEQ) expression
        """
        arg1, arg2 = conversions.to_same(self.visit(ctx.expression(0)), self.visit(ctx.expression(1)), self._eval_context)

        if isinstance(arg1, basestring):
            # string equality is case-insensitive
            equal = arg1.lower() == arg2.lower()
        else:
            equal = arg1 == arg2

        return equal if ctx.EQ() is not None else not equal

    def visitConcatenation(self, ctx):
        """
        expression: expression AMPERSAND expression
        """
        arg1 = conversions.to_string(self.visit(ctx.expression(0)), self._eval_context)
        arg2 = conversions.to_string(self.visit(ctx.expression(1)), self._eval_context)
        return arg1 + arg2

    def visitStringLiteral(self, ctx):
        """
        expression: STRING
        """
        value = ctx.getText()[1:-1]  # strip surrounding quotes
        return value.replace('""', '"')  # unescape embedded quotes

    def visitDecimalLiteral(self, ctx):
        """
        expression: DECIMAL
        """
        return Decimal(ctx.getText())

    def visitTrue(self, ctx):
        """
        expression: TRUE
        """
        return True

    def visitFalse(self, ctx):
        """
        expression: FALSE
        """
        return False

    def visitContextReference(self, ctx):
        """
        expression: NAME
        """
        identifier = ctx.NAME().getText()
        value = self._eval_context.resolve_variable(identifier)
        return value if value is not None else ""  # return empty string rather than none

    def visitParentheses(self, ctx):
        """
        expression: LPAREN expression RPAREN
        """
        return self.visit(ctx.expression())
