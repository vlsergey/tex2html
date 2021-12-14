package com.github.vlsergey.tex2html;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.nio.charset.StandardCharsets;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import com.github.vlsergey.tex2html.grammar.LatexLexer;
import com.github.vlsergey.tex2html.grammar.LatexParser;
import com.github.vlsergey.tex2html.grammar.LatexParser.ContentContext;

public class LatexVisitorTest {

	@ParameterizedTest
	@CsvSource({ "helloWorld", "tabularInFigure" })
	void testLatexVisitor(String code) throws Exception {
		final String src = IOUtils.toString(LatexVisitorTest.class.getResource("test/" + code + ".tex"),
				StandardCharsets.UTF_8);

		final LatexLexer lexer = new LatexLexer(new ANTLRInputStream(src));
		final LatexParser parser = new LatexParser(new CommonTokenStream(lexer));
		final ContentContext contentContext = parser.content();
		assertNotNull(contentContext);

		new LatexVisitor(new XmlWriter()).visit(contentContext);
	}

}
