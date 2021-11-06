lexer grammar LatexLexer;

BEGIN           : '\\begin';
END             : '\\end';

COMMANDSTART    : '\\' + ALPHANUMERIC;

BROPEN          : '{';
BRCLOSE         : '}';

SBROPEN         : '[';
SBRCLOSE        : ']';

ALPHANUMERIC    : [a-zA-Z0-9]+;
ETC             : ~[a-zA-Z0-9\[\]\{\}\\]+;
