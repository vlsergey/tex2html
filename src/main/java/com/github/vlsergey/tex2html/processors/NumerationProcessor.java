package com.github.vlsergey.tex2html.processors;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.lang3.mutable.MutableInt;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

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
		final MutableInt chapterCounter = new MutableInt(0);
		final MutableInt figureCounter = new MutableInt(0);

		final Element documentNode = (Element) xPathFactory.newXPath().evaluate("//command[@name='document']", xmlDoc,
				XPathConstants.NODE);

		DomUtils.visit(documentNode, node -> {
			if (TexXmlUtils.isCommandElement(node, "chapter")) {
				chapterCounter.increment();

				final Element chapter = (Element) node;
				chapter.setAttribute("chapter-index", chapterCounter.toString());
				chapter.setAttribute("index", chapterCounter.toString());
			}

			if (TexXmlUtils.isCommandElement(node, "figure")) {
				figureCounter.increment();

				final Element figure = (Element) node;
				figure.setAttribute("figure-index", figureCounter.toString());
				if (haveChapters) {
					figure.setAttribute("chapter-index", chapterCounter.toString());
					figure.setAttribute("index", chapterCounter.toString() + "." + figureCounter.toString());
				} else {
					figure.setAttribute("index", figureCounter.toString());
				}
			}

			return true;
		});

		// additional numeration for subcaptionboxes
		final NodeList figureWithSubcaptionboxes = (NodeList) xPathFactory.newXPath().evaluate(
				"//command[@name='figure'][.//command[@name='subcaptionbox']]", xmlDoc, XPathConstants.NODESET);
		DomUtils.stream(figureWithSubcaptionboxes).forEach(figure -> {
			final String figureIndex = ((Element) figure).getAttribute("index");
			final MutableInt subcaptionboxCounter = new MutableInt(0);

			TexXmlUtils.visitCommandNodes(figure, "subcaptionbox", box -> {
				final String boxIndex = Character.toString(('a' + subcaptionboxCounter.intValue()));
				box.setAttribute("box-index", boxIndex);
				box.setAttribute("index", figureIndex + boxIndex);
				subcaptionboxCounter.increment();
			});
		});

		return xmlDoc;
	}

}
