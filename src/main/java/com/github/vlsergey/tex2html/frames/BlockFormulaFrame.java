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
	public @NonNull Frame onEnter(@NonNull XmlWriter out) {
		out.beginElement("block-formula");
		return this;
	}

	@Override
	public void onExit(@NonNull XmlWriter out) {
		out.endElement("block-formula");
	}
}