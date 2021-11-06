parser grammar LatexParser;

options { tokenVocab=LatexLexer; }

command     : COMMANDSTART commandArguments?;   

beginCommand : BEGIN BROPEN ALPHANUMERIC BRCLOSE commandArguments?;

file : content EOF;

endCommand : END BROPEN ALPHANUMERIC BRCLOSE; 

commandArguments: BROPEN content BRCLOSE;

content     : (command | beginCommand | endCommand | wrappedToken | ALPHANUMERIC | ETC)*;

wrappedToken : BROPEN content BRCLOSE;
