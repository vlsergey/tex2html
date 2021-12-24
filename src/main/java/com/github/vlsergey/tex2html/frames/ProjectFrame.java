package com.github.vlsergey.tex2html.frames;

import com.github.vlsergey.tex2html.LatexVisitor;
import com.github.vlsergey.tex2html.TextMode;

import lombok.NonNull;

public class ProjectFrame extends TextMode {

	public ProjectFrame(final @NonNull LatexVisitor latexVisitor) {
		super(latexVisitor);
	}

}