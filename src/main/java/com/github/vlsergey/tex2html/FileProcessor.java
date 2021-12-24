package com.github.vlsergey.tex2html;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import com.github.vlsergey.tex2html.frames.FileFrame;
import com.github.vlsergey.tex2html.grammar.LatexLexer;
import com.github.vlsergey.tex2html.grammar.LatexParser;
import com.github.vlsergey.tex2html.grammar.LatexParser.ContentContext;
import com.github.vlsergey.tex2html.utils.AntlrUtils;
import com.github.vlsergey.tex2html.utils.FileUtils;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@AllArgsConstructor
@Slf4j
public class FileProcessor {

	private final File base;

	public void processFile(final LatexVisitor latexVisitor, String path) throws IOException {
		final File input = FileUtils.findFile(base, path, "tex").orElseThrow(
				() -> new FileNotFoundException("Input '" + path + "' not found with base '" + base.getPath() + "'"));

		final @NonNull LatexParser parser = AntlrUtils.parse(LatexLexer::new, LatexParser::new, input, log);
		final ContentContext contentContext = parser.content();

		latexVisitor.with(new FileFrame(input), () -> {
			latexVisitor.visit(contentContext);
		});
	}

}
