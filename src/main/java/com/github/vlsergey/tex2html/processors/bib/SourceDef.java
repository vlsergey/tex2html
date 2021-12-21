package com.github.vlsergey.tex2html.processors.bib;

import java.util.LinkedHashMap;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
class SourceDef {
	private final String type;
	private final String alphabeticLabel;
	private final LinkedHashMap<String, String[]> attributes;
}