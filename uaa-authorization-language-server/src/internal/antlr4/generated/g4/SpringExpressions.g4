grammar SpringExpressions;

expression
    : printExpression EOF
    | logicalExpressionWithParen EOF
    ;

printExpression
    : PRINT LPAREN (STRING_LITERAL | chainMethodCall | IDENTIFIER) (PLUS (STRING_LITERAL | chainMethodCall | IDENTIFIER | property))* RPAREN
    ;

logicalExpressionWithParen
    : ( logicalExpression | (LPAREN logicalExpressionWithParen RPAREN)) ((AND | OR) ((LPAREN logicalExpressionWithParen RPAREN) | logicalExpression))*
    ;

logicalExpression
    : compositeCondition ((AND | OR) compositeCondition)*
    ;

compositeCondition
    : (NOT)* baseCondition
    | (NOT)* LPAREN baseCondition RPAREN
    | (NOT)* LPAREN complexCondition RPAREN
    | complexCondition
    ;

complexCondition
    : basicExpression operator basicExpression
    | chainMethodCall operator basicExpression
    | basicExpression operator chainMethodCall
    ;

baseCondition
    : chainMethodCall
    | basicExpression
    | property DOT chainMethodCall
    ;

basicExpression
    : property
    | literalValue
    | constructorExp
    | STRING_LITERAL ASSIGNMENT basicExpression
    ;

chainMethodCall
    : methodCall (DOT methodCall)*
    ;

methodCall
    : (AT_SIGN)? property LPAREN (parameterList)? RPAREN
    ;

constructorExp
    : NEW property LPAREN (parameterList?) RPAREN
    ;

parameterList
    : (literalValue | property | constructorExp) (COMMA (literalValue | property | constructorExp))*
    ;

property
    : IDENTIFIER (((DOT)? LSQUARE_BRACKET .+? RSQUARE_BRACKET) | (DOT IDENTIFIER))*
    ;

literalValue
    : STRING_LITERAL
    | NUMBER
    | BOOLEAN
    ;

operator
    : EQUALS
    | NOT_EQUALS
    | GT
    | LT
    | GTE
    | LTE
    | INSTANCEOF
    | BETWEEN
    | MATCHES
    ;

// Lexer Rules
AND            : '&&' | [Aa] [Nn] [Dd];
OR             : '||' | [Oo] [Rr];
EQUALS         : '==';
NOT_EQUALS     : '!=';
NOT            : '!';
GT             : '>';
LT             : '<';
GTE            : '>=';
LTE            : '<=';
PRINT          : 'print';
LPAREN         : '(' ;
RPAREN         : ')' ;
LSQUARE_BRACKET: '[' ;
RSQUARE_BRACKET: ']' ;
PLUS           : '+' ;
MINUS          : '-' ;
DOT            : '.' ;
COMMA          : ',' ;
AT_SIGN        : '@' ;
ASSIGNMENT     : '=' ;
INSTANCEOF     : 'instanceof';
BETWEEN        : 'between';
MATCHES        : 'matches';
NEW            : 'new';
IDENTIFIER     : [a-zA-Z][a-zA-Z0-9]* ;
STRING_LITERAL : '\'' (ESC | ~['\\] | ~'\'' | '\'\'')* '\'';
NUMBER         : [0-9]+ ('.' [0-9]+)?;
BOOLEAN        : 'true' | 'false';
INVALID_TOKEN  : '&' | '|'; // To cach invalid operator
WS             : [ \t\r\n]+ -> skip ;

// Escape sequences for string literals
fragment ESC : '\\' . ;