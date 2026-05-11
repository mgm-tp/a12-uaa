grammar PropertyReferences;

references
    : stringReferences EOF
    ;

stringReferences
    : sentences ((AND | OR) sentences)*
    ;

sentences
    : IDENTIFIER (WS? IDENTIFIER)*
    ;

// Lexer Rules
AND         : '&&';
OR          : '||';
EQUALS      : '==';
NOT_EQUALS  : '!=';
GT          : '>';
LT          : '<';
GTE         : '>=';
LTE         : '<=';
LPAREN      : '(' ;
RPAREN      : ')' ;
PLUS        : '+' ;
MINUS       : '-' ;
DOT         : '.' ;
COMMA       : ',' ;
COLON       : ':' ;
AT_SIGN     : '@' ;
IDENTIFIER    : [a-zA-Z][a-zA-Z0-9]* ;
STRING_LITERAL: '\'' .*? '\'' ;
NUMBER        : [0-9]+('.'[0-9]+)?;
BOOLEAN       : 'true' | 'false';
WS           : [ \t\r\n]+ -> skip ;
INVALID_TOKEN : '&' | '|' ; // To catch invalid operator