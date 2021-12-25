parser grammar LatexParser;

@header {
   package com.github.vlsergey.tex2html.grammar;
}

options { tokenVocab=LatexLexer; }

limitedContent      : (
    ALPHA |
    AMPERSAND |
    ASTERIX |
    ESCAPED_AMPERSAND |
    ESCAPED_APOSTROPHE |
    ESCAPED_COMMA |
    ESCAPED_CURLY_BRACKET_OPEN |
    ESCAPED_CURLY_BRACKET_CLOSE |
    ESCAPED_DOLLAR_SIGN |
    ESCAPED_HASH |
    ESCAPED_MINUS |
    ESCAPED_PIPE |
    ESCAPED_SPACE |
    ESCAPED_UNDERSCORE |
    ETC |
    GT |
    GTGT |
    HASH |
    LINE_BREAK |
    LT |
    LTLT |
    DOUBLE_MINUS | TRIPLE_MINUS |
    PIPE |
    SHARP |
    DOUBLE_SLASH |
    SPACES |
    SUBSTITUTION |
    TILDA |
    UNDERSCORE |
    comment |
    command |
    curlyToken
)+;

formulaContent      : ( limitedContent | SQUARE_BRACKET_OPEN | SQUARE_BRACKET_CLOSE )+;
content             : ( formulaContent | blockFormula | inlineFormula )+;

command             : commandStart SPACES? commandArguments;
commandStart        : SLASH (ALPHA | ASTERIX | AT)+;
commandArguments    : (requiredArgument | optionalArgument)? (LINE_BREAK? ( requiredArgument | optionalArgument))*;
requiredArgument    : curlyToken;
optionalArgument    : SQUARE_BRACKET_OPEN limitedContent? SQUARE_BRACKET_CLOSE;

comment         : PROCENT COMMENT_TEXT? (COMMENT_LINE_BREAK | EOF);

blockFormula    : SLASH_SQUARE_BRACKET_OPEN formulaContent? SLASH_SQUARE_BRACKET_CLOSE;

curlyToken      : CURLY_BRACKET_OPEN content? CURLY_BRACKET_CLOSE;

inlineFormula   : DOLLAR_SIGN formulaContent? DOLLAR_SIGN;
