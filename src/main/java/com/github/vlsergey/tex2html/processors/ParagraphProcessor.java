package com.github.vlsergey.tex2html.processors;

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

import com.github.vlsergey.tex2html.Tex2HtmlOptions;
import com.github.vlsergey.tex2html.utils.DomUtils;
import com.github.vlsergey.tex2html.utils.StreamUtils;

import lombok.NonNull;
import lombok.SneakyThrows;

@Component
@Order(Ordered.LOWEST_PRECEDENCE)
public class ParagraphProcessor implements TexXmlProcessor {

	private static final Set<String> NON_BLOCK_ELEMENTS = new HashSet<>(
			Arrays.asList("a", "b", "em", "i", "span", "tex-formula-inline", "tt", "u", "wbr"));

	private static boolean canBePartOfParagraph(Node node) {
		return node instanceof Text || (node instanceof Element && NON_BLOCK_ELEMENTS.contains(node.getNodeName()));
	}

	@NonNull
	@Override
	@SneakyThrows
	public Document process(final @NonNull Tex2HtmlOptions command, final @NonNull Document xhtmlDoc) {
		DomUtils.concatenateTextNodes(xhtmlDoc);

		DomUtils.visit(xhtmlDoc, node -> {
			if (node.getNodeType() == Node.ELEMENT_NODE && node.getNodeName().equals("body")) {

				final List<List<Node>> grouped = StreamUtils.group((nextGroup, flush) -> {
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
				});

				if (!grouped.isEmpty()) {
					grouped.forEach(DomUtils::trim);

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
					grouped.stream().filter(group -> !group.isEmpty()).forEach(group -> {
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
