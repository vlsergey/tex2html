parser grammar ColumnSpecParser ;

@header {
   package com.github.vlsergey.tex2html.grammar;
}

options {
	tokenVocab	= ColumnSpecLexer ;
}

spec	: SPACES? ( borderSpec? SPACES? columnSpec SPACES? )+ borderSpec? ;

borderSpec	: SINGLE_BORDER | DOUBLE_BORDER ;

columnSpec	: ( columnSpecMultiplied | colAlign ) colWidthSpec? ;
columnSpecMultiplied	: ASTERIX ( ( CURLY_BRACKET_OPEN number CURLY_BRACKET_CLOSE ) | number ) ( ( CURLY_BRACKET_OPEN colAlign CURLY_BRACKET_CLOSE ) | colAlign ) ;
colAlign	: L | C | R ;

colWidthSpec
	:	(CURLY_BRACKET_OPEN ( absolute | textWidthRelative | lineWidthRelative ) CURLY_BRACKET_CLOSE ) | ( absolute | textWidthRelative | lineWidthRelative )
	;

absolute			: number		   ;

textWidthRelative	: number TEXTWIDTH ;
lineWidthRelative	: number LINEWIDTH ;

number
	: NUMERIC | ( NUMERIC DOT NUMERIC ) | ( DOT NUMERIC ) | ( NUMERIC DOT )
	;
