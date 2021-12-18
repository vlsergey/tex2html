package com.github.vlsergey.tex2html.utils;

import java.util.List;
import java.util.stream.Stream;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

public class DomUtils {

	public static Stream<Node> childrenStream(Node node) {
		return stream(node.getChildNodes());
	}

	public static void trim(List<Node> nodes) {
		boolean hasChanges = true;
		while (hasChanges) {
			hasChanges = false;

			if (!nodes.isEmpty()) {
				final Node first = nodes.get(0);
				if (first instanceof Text) {
					final String src = first.getNodeValue();
					final String stripped = src.stripLeading();
					if (stripped.isEmpty()) {
						nodes.remove(0);
						hasChanges = true;
					} else if (!stripped.equals(src)) {
						first.setNodeValue(stripped);
						hasChanges = true;
					}
				}
			}

			if (!nodes.isEmpty()) {
				final Node last = nodes.get(nodes.size() - 1);
				if (last instanceof Text) {
					final String src = last.getNodeValue();
					final String stripped = src.stripTrailing();
					if (stripped.isEmpty()) {
						nodes.remove(nodes.size() - 1);
						hasChanges = true;
					} else if (!stripped.equals(src)) {
						last.setNodeValue(stripped);
						hasChanges = true;
					}
				}
			}
		}
	}

	public static void concatenateTextNodes(Node root) {
		visit(root, node -> {
			for (int i = node.getChildNodes().getLength() - 1; i >= 1; i--) {
				Node first = node.getChildNodes().item(i - 1);
				Node second = node.getChildNodes().item(i);

				if (first instanceof Text && second instanceof Text) {
					first.setNodeValue(first.getNodeValue() + second.getNodeValue());
					node.removeChild(second);
				}
			}

			return true;
		});
	}

	public static Stream<Node> stream(NodeList nodeList) {
		return Stream.iterate(0, i -> i + 1).limit(nodeList.getLength()).map(nodeList::item);
	}

	public static <E extends Throwable> void visit(Node node, ThrowingFunction<Node, Boolean, E> consumer) throws E {
		if (!consumer.apply(node)) {
			return;
		}
		NodeList list = node.getChildNodes();
		for (int i = 0; i < list.getLength(); i++) {
			Node childNode = list.item(i);
			visit(childNode, consumer);
		}
	}

}
