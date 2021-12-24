package com.github.vlsergey.tex2html.frames;

import java.io.File;
import java.io.FileNotFoundException;

import com.github.vlsergey.tex2html.LatexVisitor;
import com.github.vlsergey.tex2html.TextMode;
import com.github.vlsergey.tex2html.grammar.LatexLexer;
import com.github.vlsergey.tex2html.grammar.LatexParser;
import com.github.vlsergey.tex2html.grammar.LatexParser.ContentContext;
import com.github.vlsergey.tex2html.utils.AntlrUtils;
import com.github.vlsergey.tex2html.utils.FileUtils;

import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TexFile extends TextMode implements FileFrame {

	@Getter
	private final File file;

	public TexFile(final @NonNull LatexVisitor latexVisitor, final String path) throws FileNotFoundException {
		super(latexVisitor);

		final File currentFolder = latexVisitor.getCurrentFolder().orElse(new File("."));
		this.file = FileUtils.findFile(currentFolder, path, "tex").orElseThrow(
				() -> new FileNotFoundException("Input '" + path + "' not found with base '" + currentFolder + "'"));
	}

	@Override
	public ContentContext parseFile() {
		return AntlrUtils.parse(LatexLexer::new, LatexParser::new, getFile(), log).content();
	}

}
