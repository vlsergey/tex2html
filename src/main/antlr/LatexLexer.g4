lexer grammar LatexLexer;

COMMAND_START       : '\\' + ALPHANUMERIC;

CURLY_BRACKET_OPEN  : '{';
CURLY_BRACKET_CLOSE : '}';

SLASH_SQUARE_BRACKET_OPEN : '\\[';
SLASH_SQUARE_BRACKET_CLOSE: '\\]';

SQUARE_BRACKET_OPEN : '[';
SQUARE_BRACKET_CLOSE: ']';

DOLLAR_SIGN     : '$';

ESCAPED_DOLLAR_SIGN : '\\$';

ALPHANUMERIC    : [a-zA-Z0-9]+;
LINEEND         : '\r\n' | '\r' | '\n';
SPACES          : [ ]+;
PROCENT         : '%';
ETC             : ~[a-zA-Z0-9\[\]\{\}\\\$\%\r\n ]+;
