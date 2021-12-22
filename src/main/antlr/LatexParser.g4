parser grammar LatexParser;

@header {
   package com.github.vlsergey.tex2html.grammar;
}

options { tokenVocab=LatexLexer; }

formulaContent      : (
    ALPHANUMERIC |
    AMPERSAND |
    ASTERIX |
    ESCAPED_AMPERSAND |
    ESCAPED_APOSTROPHE |
    ESCAPED_COMMA |
    ESCAPED_CURLY_BRACKET_OPEN |
    ESCAPED_CURLY_BRACKET_CLOSE |
    ESCAPED_DOLLAR_SIGN |
    ESCAPED_MINUS |
    ESCAPED_SPACE |
    ETC |
    GT |
    GTGT |
    LINE_BREAK |
    LTLT |
    DOUBLE_MINUS | TRIPLE_MINUS |
    SHARP |
    DOUBLE_SLASH |
    SPACES |
    SUBSTITUTION |
    TILDA |
    comment |
    command |
    curlyToken
)+;
content             : ( formulaContent | blockFormula | inlineFormula )+;

command             : commandStart SPACES? commandArguments;
commandStart        : SLASH (ALPHANUMERIC | ASTERIX | AT)+;
commandArguments    : (requiredArgument | optionalArgument)*;
requiredArgument    : curlyToken;
optionalArgument    : squareToken;

comment         : PROCENT COMMENT_TEXT? (COMMENT_LINE_BREAK | EOF);

blockFormula    : SLASH_SQUARE_BRACKET_OPEN formulaContent? SLASH_SQUARE_BRACKET_CLOSE;

squareToken     : SQUARE_BRACKET_OPEN content? SQUARE_BRACKET_CLOSE;

curlyToken      : CURLY_BRACKET_OPEN content? CURLY_BRACKET_CLOSE;

inlineFormula   : DOLLAR_SIGN formulaContent? DOLLAR_SIGN;
