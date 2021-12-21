package com.github.vlsergey.tex2html.frames;

import com.github.vlsergey.tex2html.XmlWriter;

import lombok.AllArgsConstructor;
import lombok.NonNull;

@AllArgsConstructor
public class BlockFormulaFrame implements Frame {

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