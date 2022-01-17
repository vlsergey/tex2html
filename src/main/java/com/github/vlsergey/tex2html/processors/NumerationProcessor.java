package com.github.vlsergey.tex2html.processors;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.lang3.mutable.MutableInt;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.github.vlsergey.tex2html.Tex2HtmlOptions;
import com.github.vlsergey.tex2html.utils.DomUtils;
import com.github.vlsergey.tex2html.utils.TexXmlUtils;

import lombok.NonNull;
import lombok.SneakyThrows;

@Component
@Order(0)
public class NumerationProcessor implements TexXmlProcessor {

	private final XPathFactory xPathFactory = XPathFactory.newInstance();

	@NonNull
	@Override
	@SneakyThrows
	public Document process(final @NonNull Tex2HtmlOptions command, final @NonNull Document xmlDoc) {
		final boolean haveChapters = (Boolean) xPathFactory.newXPath()
				.evaluate("count(//command[@name='chapter' or @name='chapter*']) > 0", xmlDoc, XPathConstants.BOOLEAN);
		final MutableInt chapterCounter = new MutableInt(0);
		final MutableInt sectionCounter = new MutableInt(0);
		final MutableInt subsectionCounter = new MutableInt(0);

		final MutableInt figureCounter = new MutableInt(0);
		final MutableInt tableCounter = new MutableInt(0);

		final Element documentNode = (Element) xPathFactory.newXPath().evaluate("//command[@name='document']", xmlDoc,
				XPathConstants.NODE);

		DomUtils.visit(documentNode, node -> {
			if (TexXmlUtils.isCommandElement(node, "document")) {
				final Element document = (Element) node;
				document.setAttribute("level", "-2");
			}

			if (TexXmlUtils.isCommandElement(node, "chapter")) {
				chapterCounter.increment();
				sectionCounter.setValue(0);
				subsectionCounter.setValue(0);

				figureCounter.setValue(0);
				tableCounter.setValue(0);

				final Element chapter = (Element) node;
				chapter.setAttribute("chapter-index", chapterCounter.toString());
				chapter.setAttribute("level", "0");
				chapter.setAttribute("index", chapterCounter.toString());
			}

			if (TexXmlUtils.isCommandElement(node, "chapter*")) {
				final Element chapter = (Element) node;
				chapter.setAttribute("level", "0");
			}

			if (TexXmlUtils.isCommandElement(node, "section")) {
				sectionCounter.increment();
				subsectionCounter.setValue(0);

				final Element section = (Element) node;
				section.setAttribute("level", "1");
				section.setAttribute("section-index", sectionCounter.toString());
				section.setAttribute("index", haveChapters ? chapterCounter.toString() + "." + sectionCounter.toString()
						: sectionCounter.toString());
			}

			if (TexXmlUtils.isCommandElement(node, "subsection")) {
				subsectionCounter.increment();

				final Element subsection = (Element) node;
				subsection.setAttribute("level", "2");
				subsection.setAttribute("section-index", sectionCounter.toString());
				subsection.setAttribute("subsection-index", subsectionCounter.toString());
				subsection.setAttribute("index",
						haveChapters
								? chapterCounter.toString() + "." + sectionCounter.toString() + "."
										+ subsectionCounter.toString()
								: sectionCounter.toString() + "." + subsectionCounter.toString());
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

			if (TexXmlUtils.isCommandElement(node, "table")) {
				String tableIndex = String.valueOf(tableCounter.incrementAndGet());

				final Element table = (Element) node;
				table.setAttribute("table-index", tableIndex);
				if (haveChapters) {
					table.setAttribute("chapter-index", chapterCounter.toString());
					table.setAttribute("index", chapterCounter.toString() + "." + tableIndex);
				} else {
					table.setAttribute("index", tableIndex);
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
