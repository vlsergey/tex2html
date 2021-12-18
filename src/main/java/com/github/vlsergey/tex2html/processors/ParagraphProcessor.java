package com.github.vlsergey.tex2html.processors;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

import com.github.vlsergey.tex2html.utils.DomUtils;

import lombok.SneakyThrows;

@Component
@Order(Ordered.LOWEST_PRECEDENCE)
public class ParagraphProcessor implements TexXmlProcessor {

	private static final Set<String> NON_BLOCK_ELEMENTS = new HashSet<>(
			Arrays.asList("a", "em", "i", "span", "t", "u"));

	private static boolean canBePartOfParagraph(Node node) {
		return node instanceof Text || (node instanceof Element && NON_BLOCK_ELEMENTS.contains(node.getNodeName()));
	}

	@Override
	@SneakyThrows
	public Document process(Document xhtmlDoc) {
		DomUtils.concatenateTextNodes(xhtmlDoc);

		DomUtils.visit(xhtmlDoc, node -> {
			if (node.getNodeType() == Node.ELEMENT_NODE && node.getNodeName().equals("body")) {
				List<Node> nextGroup = new ArrayList<>();

				final List<List<Node>> grouped = new ArrayList<>();
				Runnable flush = () -> {
					if (!nextGroup.isEmpty()) {
						grouped.add(new ArrayList<>(nextGroup));
					}
					nextGroup.clear();
				};

				DomUtils.childrenStream(node).forEach(child -> {
					if (!canBePartOfParagraph(child)) {
						flush.run();
						nextGroup.add(child);
						flush.run();
						return;
					}

					if (child instanceof Text && child.getNodeValue().contains("\n\n")) {
						final String[] splitted = StringUtils.splitByWholeSeparator(child.getNodeValue(), "\n\n");
						for (int i = 0; i < splitted.length; i++) {
							if (i != 0) {
								flush.run();
							}
							nextGroup.add(xhtmlDoc.createTextNode(splitted[i]));
						}
					} else {
						nextGroup.add(child);
					}
				});

				if (grouped.size() > 1) {
					grouped.forEach(group -> {
						final Node first = group.get(0);
						if (first instanceof Text) {
							first.setNodeValue(first.getNodeValue().stripLeading());
						}

						final Node last = group.get(group.size() - 1);
						if (last instanceof Text) {
							last.setNodeValue(last.getNodeValue().stripTrailing());
						}
					});

					// remove empty pars
					for (Iterator<List<Node>> iterator = grouped.iterator(); iterator.hasNext();) {
						List<Node> group = iterator.next();
						if (group.size() == 1 && group.get(0) instanceof Text
								&& group.get(0).getNodeValue().isBlank()) {
							iterator.remove();
						}
					}

					for (int c = node.getChildNodes().getLength() - 1; c >= 0; c--) {
						node.removeChild(node.getChildNodes().item(c));
					}
					grouped.forEach(group -> {
						if (canBePartOfParagraph(group.get(0))) {
							final Element par = xhtmlDoc.createElement("p");
							group.forEach(par::appendChild);
							node.appendChild(par);
						} else {
							group.forEach(node::appendChild);
						}
					});
				}
				return false;
			}

			return true;
		});

		return xhtmlDoc;
	}

}
