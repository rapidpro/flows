grammar Excellent;

COMMA      : ',';
LPAREN     : '(';
RPAREN     : ')';

PLUS       : '+';
MINUS      : '-';
TIMES      : '*';
DIVIDE     : '/';
EXPONENT   : '^';

EQ         : '=';
NEQ        : '<>';

LTE        : '<=';
LT         : '<';
GTE        : '>=';
GT         : '>';

AMPERSAND  : '&';

DECIMAL    : [0-9]+('.'[0-9]+)?;
STRING     : '"' (~[\r\n"] | '""')* '"';

TRUE       : [Tt][Rr][Uu][Ee];
FALSE      : [Ff][Aa][Ll][Ss][Ee];

NAME       : [a-zA-Z_][a-zA-Z0-9_\.]*;    // variable names, e.g. contact.name or function names, e.g. SUM

WS         : [ \t\n\r]+ -> skip;  // ignore whitespace

input      : expression EOF;

expression : NAME LPAREN parameters? RPAREN              # functionCall
           | MINUS expression                            # negation
           | expression EXPONENT expression              # exponentExpression
           | expression (TIMES | DIVIDE) expression      # multiplicationOrDivisionExpression
           | expression (PLUS | MINUS) expression        # additionOrSubtractionExpression
           | expression (LTE | LT | GTE | GT) expression # comparisonExpression
           | expression (EQ | NEQ) expression            # equalityExpression
           | expression AMPERSAND expression             # concatenation
           | STRING                                      # stringLiteral
           | DECIMAL                                     # decimalLiteral
           | TRUE                                        # true
           | FALSE                                       # false
           | NAME                                        # contextReference
           | LPAREN expression RPAREN                    # parentheses
           ;

parameters : expression (COMMA expression)*              # functionParameters
           ;
