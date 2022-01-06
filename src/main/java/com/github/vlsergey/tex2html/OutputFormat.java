package com.github.vlsergey.tex2html;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum OutputFormat {

	SINGLE_HTML("singlehtml"),

	;

	private final String defaultChildFileName;
}
