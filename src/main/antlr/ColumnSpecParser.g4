parser grammar ColumnSpecParser ;

@header {
   package com.github.vlsergey.tex2html.grammar;
}

options {
	tokenVocab	= ColumnSpecLexer ;
}

spec	: SPACES? ( borderSpec? SPACES? columnSpec SPACES? )+ borderSpec? ;

borderSpec	: SINGLE_BORDER | DOUBLE_BORDER ;

columnSpec	: L | C | R ;
