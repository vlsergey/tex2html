package com.github.vlsergey.tex2html.frames;

import java.io.File;

import org.antlr.v4.runtime.tree.ParseTree;
import org.slf4j.LoggerFactory;

import com.github.vlsergey.tex2html.XmlWriter;

import lombok.NonNull;
import lombok.SneakyThrows;

public interface FileFrame extends Frame {

	public @NonNull File getFile();

	@Override
	@SneakyThrows
	public default @NonNull Frame onEnter(@NonNull XmlWriter out) {
		LoggerFactory.getLogger(FileFrame.class).info("Begin processing file {}", getFile());
		out.beginElement("file");
		out.setAttribute("path", getFile().getCanonicalPath().toString());
		out.setAttribute("parent-path", getFile().getParentFile().getCanonicalPath().toString());
		return this;
	}

	@Override
	public default void onExit(@NonNull XmlWriter out) {
		LoggerFactory.getLogger(FileFrame.class).info("End processing file {}", getFile());
		out.endElement("file");
	}

	ParseTree parseFile();
}