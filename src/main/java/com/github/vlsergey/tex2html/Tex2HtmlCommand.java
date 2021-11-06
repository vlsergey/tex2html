package com.github.vlsergey.tex2html;

import java.io.File;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.concurrent.Callable;

import org.antlr.v4.runtime.ANTLRFileStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.springframework.stereotype.Component;

import com.github.vlsergey.tex2html.grammar.LatexLexer;
import com.github.vlsergey.tex2html.grammar.LatexParser;
import com.github.vlsergey.tex2html.grammar.LatexParser.BeginCommandContext;
import com.github.vlsergey.tex2html.grammar.LatexParser.ContentContext;
import com.github.vlsergey.tex2html.grammar.LatexParser.FileContext;

import lombok.SneakyThrows;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Component
@Command(name = "tex2html", mixinStandardHelpOptions = true)
public class Tex2HtmlCommand implements Callable<Integer> {

	@Option(names = "--out", description = "destination directory", required = false)
	private File out;

	@Option(names = "--in", description = "source TeX file", required = true)
	private File in;

	@Override
	@SneakyThrows
	public Integer call() {
		final ANTLRFileStream inStream = new ANTLRFileStream(this.in.getPath(), StandardCharsets.UTF_8.name());
		final LatexLexer latexLexer = new LatexLexer(inStream);
		final LatexParser latexParser = new LatexParser(new CommonTokenStream(latexLexer));

		final FileContext file = latexParser.file();
		final ContentContext content = file.content();

		final Optional<BeginCommandContext> opBeginDoc = content.beginCommand().stream()
				.filter(bcc -> bcc.ALPHANUMERIC().getText().equals("document")).findFirst();
		final BeginCommandContext beginDoc = opBeginDoc
				.orElseThrow(() -> new RuntimeException("Missing \begin{document} command"));

		try (PrintWriter out = this.out != null ? new PrintWriter(this.out, StandardCharsets.UTF_8)
				: new PrintWriter(System.out)) {
			out.println("<HTML>");
			out.println("<BODY>");
			out.println("<H1>Hello, World!</H1>");
			out.println("</BODY>");
			out.println("</HTML>");
		}
		return 0;
	}

}
