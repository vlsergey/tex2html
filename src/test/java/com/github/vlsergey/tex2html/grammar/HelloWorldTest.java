package com.github.vlsergey.tex2html.grammar;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.nio.charset.StandardCharsets;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

import com.github.vlsergey.tex2html.grammar.LatexParser.ContentContext;

public class HelloWorldTest {

	@Test
	void test() throws Exception {
		final String src = IOUtils.toString(HelloWorldTest.class.getResource("../test/simple.tex"),
				StandardCharsets.UTF_8);

		final LatexLexer lexer = new LatexLexer(new ANTLRInputStream(src));
		final LatexParser parser = new LatexParser(new CommonTokenStream(lexer));
		final ContentContext contentContext = parser.content();
		assertNotNull(contentContext);
	}

}
