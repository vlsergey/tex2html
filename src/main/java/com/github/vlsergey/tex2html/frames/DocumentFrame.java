package com.github.vlsergey.tex2html.frames;

import java.io.PrintWriter;

import lombok.NonNull;

public class DocumentFrame extends BeginEndCommandFrame {

	public DocumentFrame() {
		super("document");
	}

	@Override
	public @NonNull DocumentFrame onEnter(PrintWriter out) {
		out.println("<BODY>");
		return this;
	}

	@Override
	public void onExit(PrintWriter out) {
		out.println("</BODY>");
	}

	@Override
	public void onText(PrintWriter out, String text) {
		out.append(text);
	}
}