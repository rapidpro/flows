from __future__ import absolute_import, unicode_literals

import logging

logger = logging.getLogger(__name__)


class EvaluationError(Exception):
    """
    Exception class for errors during template/expression evaluation
    """
    def __init__(self, message, caused_by=None):
        Exception.__init__(self, message)
        self.caused_by = caused_by


class TemplateEvaluator:
    # Temba templates support 2 forms of embedded expression:
    #  1. Single variable, e.g. @contact, @contact.name (delimited by character type or end of input)
    #  2. Contained expression, e.g. @(SUM(1, 2) + 2) (delimited by balanced parentheses)
    STATE_BODY = 0            # not in a expression
    STATE_PREFIX = 1          # '=' prefix that denotes the start of an expression
    STATE_IDENTIFIER = 2      # the identifier part, e.g. 'contact.age' in '@contact.age'
    STATE_BALANCED = 3        # the balanced parentheses delimited part, e.g. '(1 + 2)' in '@(1 + 2)'
    STATE_STRING_LITERAL = 4  # a string literal

    def __init__(self, expression_prefix='@'):
        self.expression_prefix = expression_prefix

    def evaluate_template(self, template, context, url_encode=False):
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
        state = self.STATE_BODY
        current_expression_chars = []
        current_expression_terminated = False
        parentheses_level = 0

        # determines whether the given character is a word character, i.e. \w in a regex
        is_word_char = lambda c: c and (c.isalnum() or c == '_')

        def resolve_expression(expression):
            """
            Resolves an expression found in the template. If an evaluation error occurs, expression is returned as is.
            """
            try:
                evaluated = evaluate_expression(expression[1:], context)  # remove prefix and evaluate

                # convert result to string
                result = val_to_string(evaluated)

                return urlquote(result) if url_encode else result
            except EvaluationError, e:
                logger.debug("EvaluationError: %s" % e.message)

                # if we can't evaluate expression, include it as is in the output
                errors.append(e.message)
                return expression

        for pos, ch in enumerate(input_chars):
            # in order to determine if the b in a.b terminates an identifier, we have to peek two characters ahead as it
            # could be a.b. (b terminates) or a.b.c (b doesn't terminate)
            next_ch = input_chars[pos + 1] if (pos < (len(input_chars) - 1)) else None
            next_next_ch = input_chars[pos + 2] if (pos < (len(input_chars) - 2)) else None

            if state == self.STATE_BODY:
                if ch == '=' and (is_word_char(next_ch) or next_ch == '('):
                    state = self.STATE_PREFIX
                    current_expression_chars = [ch]
                else:
                    output_chars.append(ch)

            elif state == self.STATE_PREFIX:
                if is_word_char(ch):
                    # we're parsing an expression like @XXX
                    state = self.STATE_IDENTIFIER
                elif ch == '(':
                    # we're parsing an expression like @(1 + 2)
                    state = self.STATE_BALANCED
                    parentheses_level += 1

                current_expression_chars.append(ch)

            elif state == self.STATE_IDENTIFIER:
                if ch == '(':
                    state = self.STATE_BALANCED
                    parentheses_level += 1

                current_expression_chars.append(ch)

            elif state == self.STATE_BALANCED:
                if ch == '(':
                    parentheses_level += 1
                elif ch == ')':
                    parentheses_level -= 1
                elif ch == '"':
                    state = self.STATE_STRING_LITERAL

                current_expression_chars.append(ch)

                # expression terminates if parentheses balance
                if parentheses_level == 0:
                    current_expression_terminated = True

            elif state == self.STATE_STRING_LITERAL:
                if ch == '"':
                    state = self.STATE_BALANCED
                current_expression_chars.append(ch)

            # identifier can terminate expression in 3 ways:
            #  1. next char is null (i.e. end of the input)
            #  2. next char is not a word character or period or left parentheses
            #  3. next char is a period, but it's not followed by a word character
            if state == self.STATE_IDENTIFIER:
                if not next_ch \
                        or (not is_word_char(next_ch) and not next_ch == '.' and not next_ch == '(') \
                        or (next_ch == '.' and not is_word_char(next_next_ch)):
                    current_expression_terminated = True

            if current_expression_terminated:
                output_chars.append(resolve_expression(''.join(current_expression_chars)))
                current_expression_chars = []
                current_expression_terminated = False
                state = self.STATE_BODY

        output = ''.join(output_chars)  # joining is fastest way to build strings in Python
        return output, errors
