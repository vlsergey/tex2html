package com.github.vlsergey.tex2html.frames;

import com.github.vlsergey.tex2html.XmlWriter;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

@AllArgsConstructor
public class CommandArgumentFrame implements Frame {

	@Getter
	private final boolean required;

	@Override
	public @NonNull CommandArgumentFrame onEnter(@NonNull XmlWriter out) {
		out.beginElement("argument");
		out.setAttribute("required", Boolean.toString(required));
		return this;
	}

	@Override
	public void onExit(@NonNull XmlWriter out) {
		out.endElement("argument");
	}
}