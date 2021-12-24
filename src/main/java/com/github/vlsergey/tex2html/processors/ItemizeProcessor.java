package com.github.vlsergey.tex2html.processors;

import java.util.List;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.lang3.StringUtils;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

import com.github.vlsergey.tex2html.Tex2HtmlOptions;
import com.github.vlsergey.tex2html.utils.DomUtils;
import com.github.vlsergey.tex2html.utils.StreamUtils;
import com.github.vlsergey.tex2html.utils.TexXmlUtils;

import lombok.SneakyThrows;

@Component
@Order(0)
public class ItemizeProcessor implements TexXmlProcessor {

	private final XPathFactory xPathFactory = XPathFactory.newInstance();

	@Override
	public Document process(Tex2HtmlOptions command, Document xmlDoc) {
		return TexXmlUtils.visitCommandNodes(xmlDoc, "itemize", this::processImpl);
	}

	@SneakyThrows
	private void processImpl(Element command) {
		Element contentElement = (Element) xPathFactory.newXPath().evaluate("./content", command, XPathConstants.NODE);
		if (contentElement == null) {
			return;
		}

		final List<List<Node>> grouped = StreamUtils.group((nextGroup, flush) -> {
			DomUtils.childrenStream(contentElement).forEach(child -> {
				if (nextGroup.isEmpty() && child instanceof Text
						&& StringUtils.trimToNull(child.getNodeValue()) == null) {
					return;
				}

				if (TexXmlUtils.isCommandElement(child, "item")) {
					flush.run();
					return;
				}

				nextGroup.add(child);
			});
		});

		grouped.forEach(DomUtils::trim);

		final Document doc = command.getOwnerDocument();
		final Element itemize = doc.createElement("itemize");
		grouped.forEach(group -> {
			final Element item = doc.createElement("item");
			group.forEach(item::appendChild);
			itemize.appendChild(item);
		});

		command.getParentNode().replaceChild(itemize, command);
	}

}
