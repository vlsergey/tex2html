package com.github.vlsergey.tex2html.output;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum OutputFormat {

	INTERMEDIATE("intermediate"),

	SINGLE_HTML("singlehtml"),

	WEBSITE("website"),

	;

	private final String defaultChildFileName;
}
