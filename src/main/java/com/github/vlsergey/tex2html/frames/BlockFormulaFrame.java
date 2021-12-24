package com.github.vlsergey.tex2html.frames;

import com.github.vlsergey.tex2html.LatexVisitor;
import com.github.vlsergey.tex2html.MathMode;
import com.github.vlsergey.tex2html.XmlWriter;

import lombok.NonNull;

public class BlockFormulaFrame extends MathMode {

	public BlockFormulaFrame(final @NonNull LatexVisitor latexVisitor) {
		super(latexVisitor);
	}

	@Override
	public @NonNull BlockFormulaFrame onEnter(@NonNull XmlWriter out) {
		out.beginElement("tex-formula-block");
		return this;
	}

	@Override
	public void onExit(@NonNull XmlWriter out) {
		out.endElement("tex-formula-block");
	}
}