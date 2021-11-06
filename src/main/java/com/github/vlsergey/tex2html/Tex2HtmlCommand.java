package com.github.vlsergey.tex2html;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Callable;

import org.springframework.stereotype.Component;

import lombok.SneakyThrows;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Component
@Command(name = "tex2html", mixinStandardHelpOptions = true)
public class Tex2HtmlCommand implements Callable<Integer> {

	@Option(names = "--out", description = "destination directory", required = false, defaultValue = "./html/")
	private File out;

	@Option(names = "--in", description = "source TeX file", required = true)
	private File in;

	@Override
	@SneakyThrows
	public Integer call() {
		try (PrintWriter out = new PrintWriter(this.out, StandardCharsets.UTF_8)) {
			out.println("<HTML>");
			out.println("<BODY>");
			out.println("<H1>Hello, World!</H1>");
			out.println("</BODY>");
			out.println("</HTML>");
		}
		return 0;
	}

}
