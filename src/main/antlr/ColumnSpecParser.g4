parser grammar ColumnSpecParser ;

@header {
   package com.github.vlsergey.tex2html.grammar;
}

options {
	tokenVocab	= ColumnSpecLexer ;
}

spec	: SPACES? ( borderSpec? SPACES? columnSpec SPACES? )+ borderSpec? ;

borderSpec	: SINGLE_BORDER | DOUBLE_BORDER ;

columnSpec			: colSpecMultiplier? colAlign ;
colSpecMultiplier	: ASTERIX CURLY_BRACKET_OPEN number CURLY_BRACKET_CLOSE ;
colAlign			: L | C | R ;

number	: NUMERIC ;
