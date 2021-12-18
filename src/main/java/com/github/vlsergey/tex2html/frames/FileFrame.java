package com.github.vlsergey.tex2html.frames;

import java.io.File;

import com.github.vlsergey.tex2html.XmlWriter;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.SneakyThrows;

@AllArgsConstructor
public class FileFrame implements Frame {

	@Getter
	private final File file;

	@Override
	@SneakyThrows
	public @NonNull Frame onEnter(@NonNull XmlWriter out) {
		out.beginElement("file");
		out.setAttribute("path", file.getCanonicalPath().toString());
		out.setAttribute("parent-path", file.getParentFile().getCanonicalPath().toString());
		return this;
	}

	@Override
	public void onExit(@NonNull XmlWriter out) {
		out.endElement("file");
	}
}