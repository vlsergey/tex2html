package com.github.vlsergey.tex2html.processors;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.lang3.mutable.MutableInt;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.github.vlsergey.tex2html.utils.DomUtils;
import com.github.vlsergey.tex2html.utils.TexXmlUtils;

import lombok.SneakyThrows;

@Component
@Order(0)
public class NumerationProcessor implements TexXmlProcessor {

	private final XPathFactory xPathFactory = XPathFactory.newInstance();

	@Override
	@SneakyThrows
	public Document process(Document xmlDoc) {
		final boolean haveChapters = (Boolean) xPathFactory.newXPath().evaluate("count(//command[@name='chapter']) > 0",
				xmlDoc, XPathConstants.BOOLEAN);
		final MutableInt chapterIndex = new MutableInt(0);
		final MutableInt figureIndex = new MutableInt(0);

		final Element documentNode = (Element) xPathFactory.newXPath().evaluate("//command[@name='document']", xmlDoc,
				XPathConstants.NODE);

		DomUtils.visit(documentNode, node -> {
			if (TexXmlUtils.isCommandElement(node, "chapter")) {
				chapterIndex.increment();

				final Element chapter = (Element) node;
				chapter.setAttribute("chapter-index", chapterIndex.toString());
				chapter.setAttribute("index", chapterIndex.toString());
			}

			if (TexXmlUtils.isCommandElement(node, "figure")) {
				figureIndex.increment();

				final Element figure = (Element) node;
				figure.setAttribute("figure-index", figureIndex.toString());
				if (haveChapters) {
					figure.setAttribute("chapter-index", chapterIndex.toString());
					figure.setAttribute("index", chapterIndex.toString() + "." + figureIndex.toString());
				} else {
					figure.setAttribute("index", figureIndex.toString());
				}
			}

			return true;
		});
		return xmlDoc;
	}

}
