package com.github.vlsergey.tex2html;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.antlr.v4.runtime.ANTLRFileStream;
import org.antlr.v4.runtime.CommonTokenStream;

import com.github.vlsergey.tex2html.frames.FileFrame;
import com.github.vlsergey.tex2html.grammar.LatexLexer;
import com.github.vlsergey.tex2html.grammar.LatexParser;
import com.github.vlsergey.tex2html.grammar.LatexParser.ContentContext;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class FileProcessor {

	private final File base;

	public void processFile(String path, LatexVisitor latexVisitor) throws IOException {
		path = path.replace('/', File.separatorChar);
		File input = findFile(path);
		if (!input.exists()) {
			throw new FileNotFoundException(
					"Input '" + path + "' not found with base '" + base.getCanonicalPath() + "'");
		}

		final ANTLRFileStream inStream = new ANTLRFileStream(input.getPath(), StandardCharsets.UTF_8.name());
		final LatexLexer lexer = new LatexLexer(inStream);
		final LatexParser parser = new LatexParser(new CommonTokenStream(lexer));
		final ContentContext contentContext = parser.content();

		latexVisitor.getLatexContext().withFrame(new FileFrame(input), () -> {
			latexVisitor.visit(contentContext);
		});
	}

	private File findFile(String path) {
		File toLookup = new File(path);

		if (toLookup.isAbsolute()) {
			if (!toLookup.exists()) {
				toLookup = new File(path + ".tex");
			}
			return toLookup;
		}

		File toCheck = new File(base, path);
		if (!toCheck.exists()) {
			toCheck = new File(base, path + ".tex");
		}
		return toCheck;
	}

}
