package com.github.vlsergey.tex2html;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.w3c.dom.Document;

import com.github.vlsergey.tex2html.grammar.LatexLexer;
import com.github.vlsergey.tex2html.grammar.LatexParser;
import com.github.vlsergey.tex2html.grammar.LatexParser.ContentContext;
import com.github.vlsergey.tex2html.processors.CjrlProcessor;
import com.github.vlsergey.tex2html.processors.IncludeGraphicsProcessor;
import com.github.vlsergey.tex2html.processors.ParagraphProcessor;
import com.github.vlsergey.tex2html.processors.XsltProcessor;
import com.github.vlsergey.tex2html.utils.ThrowingConsumer;
import com.github.vlsergey.tex2html.utils.ThrowingFunction;

import lombok.NonNull;

@SpringBootTest(classes = { Tex2HtmlCommand.class, CjrlProcessor.class, IncludeGraphicsProcessor.class,
		ParagraphProcessor.class, XsltProcessor.class })
class MainTest {

	private static String readAndNormalize(String resourcePath) throws IOException {
		return IOUtils.toString(MainTest.class.getResource(resourcePath), StandardCharsets.UTF_8).replace("\r\n", "\n")
				.trim();
	}

	private static <E extends Throwable> void withTempFile(String prefix, String suffix,
			ThrowingConsumer<File, E> consumer) throws E, IOException {
		final File in = File.createTempFile(prefix, suffix);
		try {
			consumer.accept(in);
		} finally {
			if (!in.delete()) {
				in.deleteOnExit();
			}
		}
	}

	@Autowired
	private Tex2HtmlCommand tex2HtmlCommand;

	@ParameterizedTest
	@CsvSource({ "chapter", "helloWorld", "innerFormula", "tabularInFigure" })
	void testToHtml(String code) throws Exception {
		withTempFile(code, ".tex", in -> {
			withTempFile(code, ".html", out -> {
				final String src = IOUtils.toString(MainTest.class.getResource("test/" + code + ".tex"),
						StandardCharsets.UTF_8);
				FileUtils.writeStringToFile(in, src, StandardCharsets.UTF_8);

				tex2HtmlCommand.setIn(in);
				tex2HtmlCommand.setOut(out);
				tex2HtmlCommand.setIndent(true);
				tex2HtmlCommand.call();

				String actual = FileUtils.readFileToString(out, StandardCharsets.UTF_8).replace("\r\n", "\n").trim();
				String expected = readAndNormalize("test/" + code + ".html");
				assertEquals(expected, actual);
			});
		});
	}

	@ParameterizedTest
	@CsvSource({ "chapter", "helloWorld", "innerFormula", "tabularInFigure" })
	void testToXml(String code) throws Exception {
		final String src = IOUtils.toString(MainTest.class.getResource("test/" + code + ".tex"),
				StandardCharsets.UTF_8);

		final LatexLexer lexer = new LatexLexer(new ANTLRInputStream(src));
		final LatexParser parser = new LatexParser(new CommonTokenStream(lexer));
		final ContentContext contentContext = parser.content();
		assertNotNull(contentContext);

		final XmlWriter xmlWriter = new XmlWriter();
		new StandardVisitor(new LatexContext(xmlWriter)).visit(contentContext);
		String actual = toXml(xmlWriter.getDoc());
		String expected = readAndNormalize("test/" + code + ".xml");
		assertEquals(expected, actual);
	}

	private @NonNull String toXml(final @NonNull Document doc) throws TransformerException {
		Transformer transformer = TransformerFactory.newInstance().newTransformer();
		transformer.setOutputProperty(OutputKeys.ENCODING, StandardCharsets.UTF_8.name());
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		transformer.setOutputProperty(OutputKeys.METHOD, "xml");
		transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
		final StringWriter writer = new StringWriter();
		transformer.transform(new DOMSource(doc), new StreamResult(writer));
		return writer.getBuffer().toString().replace("\r\n", "\n").trim();
	}

}
