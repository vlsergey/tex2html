lexer grammar LatexLexer;

CURLY_BRACKET_OPEN  : '{';
CURLY_BRACKET_CLOSE : '}';

SLASH_SQUARE_BRACKET_OPEN : '\\[';
SLASH_SQUARE_BRACKET_CLOSE: '\\]';

SQUARE_BRACKET_OPEN : '[';
SQUARE_BRACKET_CLOSE: ']';

AT              : '@';
ASTERIX         : '*';
SHARP           : '#';
DOLLAR_SIGN     : '$';
GT              : '>';
GTGT            : '>>';
LINE_BREAK      : '\r\n' | '\r' | '\n';
LT              : '<';
LTLT            : '<<';
DOUBLE_MINUS    : '--';
TRIPLE_MINUS    : '---';
SLASH           : '\\';
TILDA           : '~';
SPACES          : [ ]+;

SUBSTITUTION    : [#][0-9];

ESCAPED_APOSTROPHE  : '\\\'';
ESCAPED_DOLLAR_SIGN : '\\$';
ESCAPED_MINUS       : '\\-';
ESCAPED_PROCENT     : '\\%';
ESCAPED_SLASH       : '\\\\';
ESCAPED_SPACE       : '\\ ';

ALPHANUMERIC    : [a-zA-Z0-9]+;
ETC             : ~[a-zA-Z0-9\[\]\{\}\\\$\%\~\r\n<> #]+;

PROCENT         : '%' -> pushMode(COMMENT);

mode COMMENT;
COMMENT_TEXT        : ~[\r\n]+;
COMMENT_LINE_BREAK  : ('\r\n' | '\r' | '\n') -> popMode;
