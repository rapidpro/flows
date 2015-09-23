/**
 * Javascript parser for Excellent-style expressions used in RapidPro
 */
(function(excellent, $) {

    var STATE_BODY = 0;               // not in a expression
    var STATE_PREFIX = 1;             // '@' prefix that denotes the start of an expression
    var STATE_IDENTIFIER = 2;         // the identifier part, e.g. 'SUM' in '@SUM(1, 2)' or 'contact.age' in '@contact.age'
    var STATE_BALANCED = 3;           // the balanced parentheses delimited part, e.g. '(1 + 2)' in '@(1 + 2)'
    var STATE_STRING_LITERAL = 4;     // a string literal which could contain )
    var STATE_ESCAPED_PREFIX = 5;     // a '@' prefix preceded by another '@'

    excellent.Parser = function(expressionPrefix, allowedTopLevels) {
        this.expressionPrefix = expressionPrefix;
        this.allowedTopLevels = allowedTopLevels;
    };

    excellent.Parser.prototype.findExpressionContext = function(text) {
        expressions = this.findExpressions(text);

        if (expressions.length == 0) {
            return null;
        }

        var lastExpression = expressions[expressions.length - 1];

        if (lastExpression.end < text.length || lastExpression.complete) {
            return null;
        }

        return lastExpression.text.substring(1);  // return without prefix
    };

    excellent.Parser.prototype.findNameContext = function(expression) {
        // TODO
    };

    excellent.Parser.prototype.findExpressions = function(text) {
        var expressions = [];
        var state = STATE_BODY;
        var currentExpression = null;
        var parenthesesLevel = 0;

        for (var pos = 0; pos < text.length; pos++) {
            var ch = text[pos];

            // in order to determine if the b in a.b terminates an identifier, we have to peek two characters ahead as
            // it could be a.b. (b terminates) or a.b.c (b doesn't terminate)
            var nextCh = (pos < (text.length - 1)) ? text[pos + 1] : 0;
            var nextNextCh = (pos < (text.length - 2)) ? text[pos + 2] : 0;

            if (state == STATE_BODY) {
                if (ch == this.expressionPrefix && (isWordChar(nextCh) || nextCh == '(')) {
                    state = STATE_PREFIX;
                    currentExpression = {start: pos, end: null, text: ch};
                } else if (ch == this.expressionPrefix && nextCh == this.expressionPrefix) {
                    state = STATE_ESCAPED_PREFIX;
                }
            }
            else if (state == STATE_PREFIX) {
                if (isWordChar(ch)) {
                    state = STATE_IDENTIFIER; // we're parsing an expression like @XXX
                } else if (ch == '(') {
                    // we're parsing an expression like @(1 + 2)
                    state = STATE_BALANCED;
                    parenthesesLevel += 1;
                }
                currentExpression.text += ch;
            }
            else if (state == STATE_IDENTIFIER) {
                currentExpression.text += ch;
            }
            else if (state == STATE_BALANCED) {
                if (ch == '(') {
                    parenthesesLevel += 1;
                } else if (ch == ')') {
                    parenthesesLevel -= 1;
                } else if (ch == '"') {
                    state = STATE_STRING_LITERAL;
                }

                currentExpression.text += ch;

                // expression terminates if parentheses balance
                if (parenthesesLevel == 0) {
                    currentExpression.end = pos + 1;
                }
            }
            else if (state == STATE_STRING_LITERAL) {
                if (ch == '"') {
                    state = STATE_BALANCED;
                }
                currentExpression.text += ch;
            }
            else if (state == STATE_ESCAPED_PREFIX) {
                state = STATE_BODY;
            }

            // identifier can terminate expression in 3 ways:
            //  1. next char is null (i.e. end of the input)
            //  2. next char is not a word character or period
            //  3. next char is a period, but it's not followed by a word character
            if (state == STATE_IDENTIFIER) {
                if ((!isWordChar(nextCh) && nextCh !== '.') || (nextCh === '.' && !isWordChar(nextNextCh))) {
                    currentExpression.end = pos + 1;
                }
            }

            if (currentExpression != null && (currentExpression.end != null || nextCh === 0)) {
                var allowIncomplete = (nextCh === 0); // if we're at the end of the input, allow incomplete expressions

                if (isValidExpression(currentExpression.text, this.allowedTopLevels, allowIncomplete)) {
                	currentExpression.complete = (currentExpression.text[1] === '(') && (parenthesesLevel == 0);
                    currentExpression.end = pos + 1;
                    expressions.push(currentExpression);
                }

                currentExpression = null;
                state = STATE_BODY;
            }
        }

        return expressions;
    };

    /**
     * Checks the parsed expression to determine if it's valid
     */
    function isValidExpression(expression, allowedTopLevels, allowIncomplete) {
        var body = expression.substring(1); // strip prefix

        if (body[0] === '(') {
            return true;
        } else {
            // if expression doesn't start with ( then check it's an allowed top level context reference
            var topLevel = body.split('.')[0].toLowerCase();

            if (allowIncomplete) {
                for (var n = 0; n < allowedTopLevels.length; n++) {
                    if (startsWith(allowedTopLevels[n], topLevel)) {
                        return true;
                    }
                }
            } else {
                return allowedTopLevels.indexOf(topLevel) >= 0;
            }
            return false;
        }
    }

    /**
     * Determines whether the given string starts with the given text
     */
    function startsWith(str, start) {
        return str.indexOf(start, 0) === 0;
    }

    /**
     * Determines whether the given character is a word character, i.e. \w in a regex
     */
    function isWordChar(ch) {
        return (ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z') || (ch >= '0' && ch <= '9') || ch == '_'; 
    }

}(window.excellent = window.excellent || {}, jQuery));
