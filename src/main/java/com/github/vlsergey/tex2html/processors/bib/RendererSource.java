package com.github.vlsergey.tex2html.processors.bib;

import org.w3c.dom.Element;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class RendererSource {

	private final String name;

	private final Element result;

	private final String sortKey;

}
