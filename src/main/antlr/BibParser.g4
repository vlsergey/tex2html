parser grammar BibParser ;

@header {
   package com.github.vlsergey.tex2html.grammar;
}

options {
	tokenVocab	= BibLexer ;
}

definitions
	: ( SPACES? definition SPACES? )* ;

definition	: AT defType CURLY_BRACKET_OPEN defName COMMA attributes SPACES? CURLY_BRACKET_CLOSE ;

defType : ALPHANUMERIC ;
defName : (ALPHANUMERIC | ETC )+ ;

attributes	: ( SPACES? attribute SPACES? COMMA )* SPACES? attribute SPACES? COMMA? ;
attribute	: attrName SPACES? EQUAL SPACES? attrValue								;
attrName	: (ALPHANUMERIC	  | ETC )+		 ;
attrValue	: attrValuesArray | contentPlain ;

attrValuesArray : CURLY_BRACKET_OPEN ( contentUnwrapped (AND contentUnwrapped )* )? CURLY_BRACKET_CLOSE ;

contentPlain
					: (ALPHANUMERIC | ETC ) ( ALPHANUMERIC | ETC | SPACES )* ;
contentUnwrapped	: (AT | ALPHANUMERIC | COMMA | EQUAL | ETC | SPACES | contentWrapped )+ ;
contentWrapped	: CURLY_BRACKET_OPEN (contentUnwrapped | AND )? CURLY_BRACKET_CLOSE ;
