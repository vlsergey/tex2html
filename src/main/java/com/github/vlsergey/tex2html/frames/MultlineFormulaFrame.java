package com.github.vlsergey.tex2html.frames;

import com.github.vlsergey.tex2html.LatexVisitor;
import com.github.vlsergey.tex2html.XmlWriter;

import lombok.NonNull;

public class MultlineFormulaFrame extends BlockFormulaFrame {

	public MultlineFormulaFrame(final @NonNull LatexVisitor latexVisitor) {
		super(latexVisitor);
	}

	@Override
	public @NonNull MultlineFormulaFrame onEnter(@NonNull XmlWriter out) {
		out.beginElement("tex-formula-multline");
		return this;
	}

	@Override
	public void onExit(@NonNull XmlWriter out) {
		out.endElement("tex-formula-multline");
	}
}