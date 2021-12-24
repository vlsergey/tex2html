lexer grammar LatexLexer ;

@header {
   package com.github.vlsergey.tex2html.grammar;
}

CURLY_BRACKET_OPEN	: '{' ;
CURLY_BRACKET_CLOSE : '}' ;

SLASH_SQUARE_BRACKET_OPEN	: '\\[' ;
SLASH_SQUARE_BRACKET_CLOSE	: '\\]' ;

SQUARE_BRACKET_OPEN		: '[' ;
SQUARE_BRACKET_CLOSE	: ']' ;

AT				: '@'	 ;
AMPERSAND		: '&'	 ;
ASTERIX			: '*'	 ;
SHARP			: '#'	 ;
DOLLAR_SIGN		: '$'	 ;
GT				: '>'	 ;
GTGT			: '>>'	 ;
HASH			: '#'	 ;
LINE_BREAK		: '\r\n' | '\r' | '\n' ;
LT				: '<'	 ;
LTLT			: '<<'	 ;
DOUBLE_MINUS	: '--'	 ;
TRIPLE_MINUS	: '---'	 ;
PIPE			: '|'	 ;
SLASH			: '\\'	 ;
DOUBLE_SLASH	: '\\\\' ;
TILDA			: '~'	 ;
SPACES			: [ ]+	 ;

SUBSTITUTION	:   [#][0-9] ;

ESCAPED_AMPERSAND			: '\\&'	 ;
ESCAPED_COMMA				: '\\,'	 ;
ESCAPED_CURLY_BRACKET_OPEN	: '\\{'	 ;
ESCAPED_CURLY_BRACKET_CLOSE	: '\\}'	 ;
ESCAPED_APOSTROPHE			: '\\\'' ;
ESCAPED_DOLLAR_SIGN			: '\\$'	 ;
ESCAPED_HASH				: '\\#'	 ;
ESCAPED_MINUS				: '\\-'	 ;
ESCAPED_PIPE				: '\\|'	 ;
ESCAPED_PROCENT				: '\\%'	 ;
ESCAPED_SPACE				: '\\ '	 ;

ALPHA	: [a-zA-Z]+ ;
ETC             : ~[a-zA-Z\[\]\{\}\\#\$%~&\r\n<> #]+;

PROCENT : '%' -> pushMode(COMMENT) ;

mode COMMENT;
COMMENT_TEXT	: ~[\r\n]+ ;
COMMENT_LINE_BREAK
	: ('\r\n' | '\r' | '\n' ) -> popMode ;
