grammar Excellent;

COMMA      : ',';
LPAREN     : '(';
RPAREN     : ')';

PLUS       : '+';
MINUS      : '-';
TIMES      : '*';
DIVIDE     : '/';
EXPONENT   : '^'; // TODO

EQ         : '=';
NEQ        : '<>';

LTE        : '<=';
LT         : '<';
GTE        : '>=';
GT         : '>';

AMPERSAND  : '&';
NAME       : [a-zA-Z_][a-zA-Z0-9_\.]*;    // variable names, e.g. contact.name or function names, e.g. SUM

DECIMAL    : [0-9]+('.'[0-9]+)?;
STRING     : '"' (~[\r\n"] | '""')* '"';

WS         : [ \t\n\r]+ -> skip;  // ignore whitespace

expression : NAME LPAREN parameters? RPAREN              # functionCall
           | expression (TIMES | DIVIDE) expression      # multiplicationOrDivisionExpression
           | expression (PLUS | MINUS) expression        # additionOrSubtractionExpression
           | expression (LTE | LT | GTE | GT) expression # comparisonExpression
           | expression (EQ | NEQ) expression            # equalityExpression
           | NAME                                        # contextReference
           | STRING                                      # stringLiteral
           | DECIMAL                                     # decimalLiteral
           ;

parameters : expression (COMMA expression)*              # functionParameters
           ;
