parser grammar LatexParser;

options { tokenVocab=LatexLexer; }

content             : (
    ALPHANUMERIC |
    ASTERIX |
    ESCAPED_DOLLAR_SIGN |
    ESCAPED_MINUS |
    ESCAPED_SLASH |
    ESCAPED_SPACE |
    LINEEND |
    ETC |
    SPACES |
    TILDA |
    blockFormula |
    comment |
    command |
    curlyToken |
    inlineFormula
)*;

command             : commandStart SPACES? commandArguments;
commandStart        : SLASH (ALPHANUMERIC | AT)+;
commandArguments    : (requiredArgument | optionalArgument)*;
requiredArgument    : curlyToken;
optionalArgument    : squareToken;

comment         : PROCENT COMMENT_TEXT? (LINEEND | EOF);

blockFormula    : SLASH_SQUARE_BRACKET_OPEN content SLASH_SQUARE_BRACKET_CLOSE;

squareToken     : SQUARE_BRACKET_OPEN content SQUARE_BRACKET_CLOSE;

curlyToken      : CURLY_BRACKET_OPEN content CURLY_BRACKET_CLOSE;

inlineFormula   : DOLLAR_SIGN content DOLLAR_SIGN;
