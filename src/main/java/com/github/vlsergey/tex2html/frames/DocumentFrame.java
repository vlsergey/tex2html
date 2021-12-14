package com.github.vlsergey.tex2html.frames;

import com.github.vlsergey.tex2html.html.HtmlWriter;

import lombok.NonNull;

public class DocumentFrame extends BeginEndCommandFrame {

	public DocumentFrame() {
		super("document");
	}

	@Override
	public @NonNull DocumentFrame onEnter(final @NonNull HtmlWriter out) {
		out.beginElement("BODY");
		return this;
	}

	@Override
	public void onExit(final @NonNull HtmlWriter out) {
		out.endElement("BODY");
	}

}