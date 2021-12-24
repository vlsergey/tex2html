package com.github.vlsergey.tex2html.processors.bib;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.charset.StandardCharsets;
import java.util.List;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Element;

import com.github.vlsergey.tex2html.FileTestUtils;
import com.github.vlsergey.tex2html.LatexContext;
import com.github.vlsergey.tex2html.StandardVisitor;
import com.github.vlsergey.tex2html.XmlWriter;
import com.github.vlsergey.tex2html.grammar.BibLexer;
import com.github.vlsergey.tex2html.grammar.BibParser;
import com.github.vlsergey.tex2html.grammar.BibParser.DefinitionContext;
import com.github.vlsergey.tex2html.utils.AntlrUtils;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
class GostRendererTest {

	@Test
	void test() throws Exception {
		FileTestUtils.withTempFile("test", ".bib", file -> {
			final String src = IOUtils.toString(GostRendererTest.class.getResource("test.bib"), StandardCharsets.UTF_8);
			FileUtils.writeStringToFile(file, src, StandardCharsets.UTF_8);

			final @NonNull BibParser bibParser = AntlrUtils.parse(BibLexer::new, BibParser::new, file, log);
			final StandardVisitor visitor = new StandardVisitor(new LatexContext(new XmlWriter()));

			final List<DefinitionContext> defs = bibParser.definitions().definition();
			defs.forEach(def -> new BibliographyResourceFactory().build(visitor, def));

			assertEquals(
					"Biryukov A., Perrin L., Udovenko A. "
							+ "The Secret Structure of the S-Box of Streebog, Kuznechik and Stribob "
							+ "— International Association for Cryptologic Research, 2015. "
							+ "— URL: https://eprint.iacr.org/2015/812. Cryptology ePrint Archive: Report 2015/812.",
					renderBibElement(visitor, "Biryukov:Perrin:Udovenko:2015"));

			assertEquals(
					"Информационная технология. Криптографическая защита информации. Блочные шифры [Текст] : ГОСТ Р 34.12-2015. "
							+ "— Введ. 01.01.2016. " + "— М. : Стандартинформ, 2015. " + "— с. 25. "
							+ "— (Национальный стандарт Российской Федерации) "
							+ "— URL: http://protect.gost.ru/document.aspx?control=7id=200990.",
					renderBibElement(visitor, "GOST-R:34.12-2015"));
		});
	}

	private final XPath xPath = XPathFactory.newInstance().newXPath();

	private final GostRenderer gostRenderer = new GostRenderer();

	private String renderBibElement(final StandardVisitor visitor, final String name) throws XPathExpressionException {
		return gostRenderer
				.render(((Element) xPath.evaluate("//*[@name='" + name + "']",
						visitor.getLatexContext().getOut().getDoc(), XPathConstants.NODE)))
				.getResult().getTextContent();
	}

}
