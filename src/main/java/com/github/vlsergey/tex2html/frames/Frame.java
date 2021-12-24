package com.github.vlsergey.tex2html.frames;

import com.github.vlsergey.tex2html.XmlWriter;

import lombok.NonNull;

public interface Frame {

	@NonNull
	default Frame onEnter(final @NonNull XmlWriter out) {
		return this;
	}

	default void onExit(final @NonNull XmlWriter out) {

	}

}