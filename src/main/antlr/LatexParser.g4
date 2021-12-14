parser grammar LatexParser;

options { tokenVocab=LatexLexer; }

content         : ( ALPHANUMERIC | ESCAPED_DOLLAR_SIGN | LINEEND | ETC | SPACES | blockFormula | comment | command | curlyToken | inlineFormula )*;

command         : COMMAND_START SPACES? commandOptions? commandArguments?;
commandArguments: curlyToken;
commandOptions  : SQUARE_BRACKET_OPEN ( ALPHANUMERIC | ESCAPED_DOLLAR_SIGN | ETC | SPACES )* SQUARE_BRACKET_CLOSE;

comment         : PROCENT ( ALPHANUMERIC | ESCAPED_DOLLAR_SIGN | ETC | SPACES | blockFormula | command | curlyToken | inlineFormula )* LINEEND;

blockFormula    : SLASH_SQUARE_BRACKET_OPEN content SLASH_SQUARE_BRACKET_CLOSE;

squareToken     : SQUARE_BRACKET_OPEN content SQUARE_BRACKET_CLOSE;

curlyToken      : CURLY_BRACKET_OPEN content CURLY_BRACKET_CLOSE;

inlineFormula   : DOLLAR_SIGN content DOLLAR_SIGN;
