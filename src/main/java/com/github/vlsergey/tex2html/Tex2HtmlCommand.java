package com.github.vlsergey.tex2html;

import java.io.File;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Callable;

import org.antlr.v4.runtime.ANTLRFileStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.AbstractParseTreeVisitor;
import org.springframework.stereotype.Component;

import com.github.vlsergey.tex2html.grammar.LatexLexer;
import com.github.vlsergey.tex2html.grammar.LatexParser;
import com.github.vlsergey.tex2html.grammar.LatexParser.ContentContext;

import lombok.SneakyThrows;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Component
@Command(name = "tex2html", mixinStandardHelpOptions = true)
public class Tex2HtmlCommand implements Callable<Integer> {

	@Option(names = "--in", description = "source TeX file", required = true)
	private File in;

	@Option(names = "--out", description = "destination directory", required = false)
	private File out;

	@Override
	@SneakyThrows
	public Integer call() {
		final ANTLRFileStream inStream = new ANTLRFileStream(this.in.getPath(), StandardCharsets.UTF_8.name());
		final LatexLexer lexer = new LatexLexer(inStream);
		final LatexParser parser = new LatexParser(new CommonTokenStream(lexer));
		final ContentContext contentContext = parser.content();

		try (PrintWriter out = this.out != null ? new PrintWriter(this.out, StandardCharsets.UTF_8)
				: new PrintWriter(System.out)) {

			final AbstractParseTreeVisitor<Void> visitor = new LatexVisitor(out);
			visitor.visit(contentContext);

		}
		return 0;
	}

}
