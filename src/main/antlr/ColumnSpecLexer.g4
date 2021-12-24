lexer grammar ColumnSpecLexer ;

@header {
   package com.github.vlsergey.tex2html.grammar;
}

NUMERIC	: [0-9]+ ;
DOT		: '.'	 ;
SPACES	: [ ]+	 ;

CURLY_BRACKET_OPEN	: '{' ;
CURLY_BRACKET_CLOSE : '}' ;

ASTERIX			: '*'  ;
DOUBLE_BORDER	: '||' ;
SINGLE_BORDER	: '|'  ;
L				: 'l' | 'L' ;
C				: 'c' | 'C' ;
R				: 'r' | 'R' ;

LINEWIDTH	: '\\linewidth' ;
TEXTWIDTH	: '\\textwidth' ;
