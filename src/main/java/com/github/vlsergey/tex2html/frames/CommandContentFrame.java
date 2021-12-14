package com.github.vlsergey.tex2html.frames;

import com.github.vlsergey.tex2html.XmlWriter;

import lombok.Data;
import lombok.NonNull;

@Data
public class CommandContentFrame implements Frame {

	@Override
	public @NonNull Frame onEnter(@NonNull XmlWriter out) {
		out.beginElement("content");
		return this;
	}

	@Override
	public void onExit(@NonNull XmlWriter out) {
		out.endElement("content");
	}
}