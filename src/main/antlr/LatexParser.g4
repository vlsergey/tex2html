parser grammar LatexParser;

options { tokenVocab=LatexLexer; }

content         : ( ALPHANUMERIC | ESCAPED_DOLLAR_SIGN | ETC | SPACES | blockFormula | command | curlyToken | inlineFormula )*;

command         : COMMAND_START SPACES? commandArguments?;
commandArguments: curlyToken;

blockFormula    : SLASH_SQUARE_BRACKET_OPEN content SLASH_SQUARE_BRACKET_CLOSE;

squareToken     : SQUARE_BRACKET_OPEN content SQUARE_BRACKET_CLOSE;

curlyToken      : CURLY_BRACKET_OPEN content CURLY_BRACKET_CLOSE;

inlineFormula   : DOLLAR_SIGN content DOLLAR_SIGN;
