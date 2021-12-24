package com.github.vlsergey.tex2html.processors.bib;

import static com.github.vlsergey.tex2html.FileTestUtils.withTempFile;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.charset.StandardCharsets;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Element;

import com.github.vlsergey.tex2html.LatexVisitor;
import com.github.vlsergey.tex2html.XmlWriter;

class GostRendererTest {

	private final GostRenderer gostRenderer = new GostRenderer();

	private final XPath xPath = XPathFactory.newInstance().newXPath();

	private String renderBibElement(final XmlWriter xmlWriter, final String name) throws XPathExpressionException {
		return gostRenderer.render(
				((Element) xPath.evaluate("//*[@name='" + name + "']", xmlWriter.getDoc(), XPathConstants.NODE)))
				.getResult().getTextContent();
	}

	@Test
	void test() throws Exception {
		withTempFile("test", ".bib", file -> {
			final String src = IOUtils.toString(GostRendererTest.class.getResource("test.bib"), StandardCharsets.UTF_8);
			FileUtils.writeStringToFile(file, src, StandardCharsets.UTF_8);

			final XmlWriter xmlWriter = new XmlWriter();
			final LatexVisitor visitor = new LatexVisitor(xmlWriter);

			BibFile bibFile = new BibFile(visitor, file.getAbsolutePath());
			visitor.visit(bibFile);

			assertEquals(
					"Biryukov A., Perrin L., Udovenko A. "
							+ "The Secret Structure of the S-Box of Streebog, Kuznechik and Stribob "
							+ "— International Association for Cryptologic Research, 2015. "
							+ "— URL: https://eprint.iacr.org/2015/812. Cryptology ePrint Archive: Report 2015/812.",
					renderBibElement(xmlWriter, "Biryukov:Perrin:Udovenko:2015"));

			assertEquals(
					"Информационная технология. Криптографическая защита информации. Блочные шифры [Текст] : ГОСТ Р 34.12-2015. "
							+ "— Введ. 01.01.2016. " + "— М. : Стандартинформ, 2015. " + "— с. 25. "
							+ "— (Национальный стандарт Российской Федерации) "
							+ "— URL: http://protect.gost.ru/document.aspx?control=7&id=200990.",
					renderBibElement(xmlWriter, "GOST-R:34.12-2015"));
		});
	}

}
