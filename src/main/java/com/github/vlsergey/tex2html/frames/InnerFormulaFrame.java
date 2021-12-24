package com.github.vlsergey.tex2html.frames;

import com.github.vlsergey.tex2html.LatexVisitor;
import com.github.vlsergey.tex2html.MathMode;
import com.github.vlsergey.tex2html.XmlWriter;

import lombok.NonNull;

public class InnerFormulaFrame extends MathMode {

	public InnerFormulaFrame(@NonNull LatexVisitor latexVisitor) {
		super(latexVisitor);
	}

	@Override
	public @NonNull Frame onEnter(@NonNull XmlWriter out) {
		out.beginElement("tex-formula-inline");
		return this;
	}

	@Override
	public void onExit(@NonNull XmlWriter out) {
		out.endElement("tex-formula-inline");
	}
}