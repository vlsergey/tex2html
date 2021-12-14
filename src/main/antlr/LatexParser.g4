parser grammar LatexParser;

options { tokenVocab=LatexLexer; }

content             : ( ALPHANUMERIC | ESCAPED_DOLLAR_SIGN | LINEEND | ETC | SPACES | blockFormula | comment | command | curlyToken | inlineFormula )*;

command             : commandStart SPACES? (requiredArgument | optionalArgument)*;
commandStart        : SLASH (ALPHANUMERIC | AT)+;
requiredArgument    : curlyToken;
optionalArgument    : squareToken;

comment         : PROCENT COMMENT_TEXT? (LINEEND | EOF);

blockFormula    : SLASH_SQUARE_BRACKET_OPEN content SLASH_SQUARE_BRACKET_CLOSE;

squareToken     : SQUARE_BRACKET_OPEN content SQUARE_BRACKET_CLOSE;

curlyToken      : CURLY_BRACKET_OPEN content CURLY_BRACKET_CLOSE;

inlineFormula   : DOLLAR_SIGN content DOLLAR_SIGN;
