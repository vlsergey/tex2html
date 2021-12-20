lexer grammar BibLexer ;

@header {
   package com.github.vlsergey.tex2html.grammar;
}

CURLY_BRACKET_OPEN	: '{' ;
CURLY_BRACKET_CLOSE : '}' ;

AT		: '@' ;
COMMA	: ',' ;
EQUAL	: '=' ;

AND	: ' and ' ;

ALPHANUMERIC	: [a-zA-Z0-9]+				;
ETC				: ~[a-zA-Z0-9={}@, \t\r\n]+ ;
SPACES			: [ \t\r\n]+				;
