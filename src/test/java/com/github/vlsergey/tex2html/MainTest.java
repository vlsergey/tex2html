package com.github.vlsergey.tex2html;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.w3c.dom.Document;

import com.github.vlsergey.tex2html.enchancers.CjrlProcessor;
import com.github.vlsergey.tex2html.enchancers.ParagraphProcessor;
import com.github.vlsergey.tex2html.enchancers.TexXmlProcessor;
import com.github.vlsergey.tex2html.enchancers.XsltProcessor;
import com.github.vlsergey.tex2html.grammar.LatexLexer;
import com.github.vlsergey.tex2html.grammar.LatexParser;
import com.github.vlsergey.tex2html.grammar.LatexParser.ContentContext;

import lombok.NonNull;

@SpringBootTest(classes = { CjrlProcessor.class, ParagraphProcessor.class, XsltProcessor.class })
class MainTest {

	private static String readAndNormalize(String resourcePath) throws IOException {
		return IOUtils.toString(MainTest.class.getResource(resourcePath), StandardCharsets.UTF_8).replace("\r\n", "\n")
				.trim();
	}

	@Autowired
	private List<TexXmlProcessor> processors;

	@ParameterizedTest
	@CsvSource({ "chapter", "helloWorld", "innerFormula", "tabularInFigure" })
	void testToHtml(String code) throws Exception {
		final String src = IOUtils.toString(MainTest.class.getResource("test/" + code + ".tex"),
				StandardCharsets.UTF_8);

		final LatexLexer lexer = new LatexLexer(new ANTLRInputStream(src));
		final LatexParser parser = new LatexParser(new CommonTokenStream(lexer));
		final ContentContext contentContext = parser.content();
		assertNotNull(contentContext);

		final XmlWriter xmlWriter = new XmlWriter();
		new StandardVisitor(new LatexContext(xmlWriter)).visit(contentContext);

		Document doc = xmlWriter.getDoc();
		for (TexXmlProcessor texXmlProcessor : processors) {
			doc = texXmlProcessor.process(doc);
		}

		final StringWriter writer = new StringWriter();
		XmlUtils.writeAsHtml(doc, true, new StreamResult(writer));
		String actual = writer.getBuffer().toString().replace("\r\n", "\n").trim();

		String expected = readAndNormalize("test/" + code + ".html");
		assertEquals(expected, actual);
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