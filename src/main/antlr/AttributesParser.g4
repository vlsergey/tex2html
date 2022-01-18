parser grammar AttributesParser ;

@header {
   package com.github.vlsergey.tex2html.grammar;
}

options {
	tokenVocab	= AttributesLexer ;
}

attributes
	: (attribute (COMMA attribute )* )? ;
attribute	: name EQUALS value ;
name		: ALPHA ;
value		: textWidth | textWidthRelative ;

number
	: NUMERIC | ( NUMERIC DOT NUMERIC ) | ( DOT NUMERIC ) | ( NUMERIC DOT )
	;

textWidth	: TEXTWIDTH ;

textWidthRelative	: number TEXTWIDTH ; 

