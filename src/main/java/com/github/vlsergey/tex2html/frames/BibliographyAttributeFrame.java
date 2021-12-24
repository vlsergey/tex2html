package com.github.vlsergey.tex2html.frames;

import com.github.vlsergey.tex2html.LatexVisitor;
import com.github.vlsergey.tex2html.TextMode;

import lombok.NonNull;

public class BibliographyAttributeFrame extends TextMode {

	public BibliographyAttributeFrame(@NonNull LatexVisitor latexVisitor) {
		super(latexVisitor);
	}

}