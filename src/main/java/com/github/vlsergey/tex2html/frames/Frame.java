package com.github.vlsergey.tex2html.frames;

import com.github.vlsergey.tex2html.html.HtmlWriter;

import lombok.NonNull;

public interface Frame {
	default void onExit(@NonNull HtmlWriter out) {

	}

	@NonNull
	default Frame onEnter(@NonNull HtmlWriter out) {
		return this;
	}

	default void onText(@NonNull HtmlWriter out, String text) {
		out.appendTextNode(text);
	}
}