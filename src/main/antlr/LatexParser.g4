parser grammar LatexParser;

options { tokenVocab=LatexLexer; }

formulaContent      : (
    ALPHANUMERIC |
    ASTERIX |
    ESCAPED_APOSTROPHE |
    ESCAPED_DOLLAR_SIGN |
    ESCAPED_MINUS |
    ESCAPED_SLASH |
    ESCAPED_SPACE |
    ETC |
    LINE_BREAK |
    DOUBLE_MINUS | TRIPLE_MINUS |
    SHARP |
    SPACES |
    SUBSTITUTION |
    TILDA |
    comment |
    command |
    curlyToken
)+;
content             : ( formulaContent | blockFormula | inlineFormula )+;

command             : commandStart SPACES? commandArguments;
commandStart        : SLASH (ALPHANUMERIC | AT)+;
commandArguments    : (requiredArgument | optionalArgument)*;
requiredArgument    : curlyToken;
optionalArgument    : squareToken;

comment         : PROCENT COMMENT_TEXT? (COMMENT_LINE_BREAK | EOF);

blockFormula    : SLASH_SQUARE_BRACKET_OPEN formulaContent? SLASH_SQUARE_BRACKET_CLOSE;

squareToken     : SQUARE_BRACKET_OPEN content? SQUARE_BRACKET_CLOSE;

curlyToken      : CURLY_BRACKET_OPEN content? CURLY_BRACKET_CLOSE;

inlineFormula   : DOLLAR_SIGN formulaContent? DOLLAR_SIGN;
