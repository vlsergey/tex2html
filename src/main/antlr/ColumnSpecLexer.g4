lexer grammar ColumnSpecLexer ;

@header {
   package com.github.vlsergey.tex2html.grammar;
}

NUMERIC	: [0-9]+ ;
SPACES	: [ ]+	 ;

CURLY_BRACKET_OPEN	: '{' ;
CURLY_BRACKET_CLOSE : '}' ;

ASTERIX			: '*'  ;
DOUBLE_BORDER	: '||' ;
SINGLE_BORDER	: '|'  ;

L	: 'l' ;
C	: 'c' ;
R	: 'r' ;
