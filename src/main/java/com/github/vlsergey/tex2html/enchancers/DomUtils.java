package com.github.vlsergey.tex2html.enchancers;

import java.util.function.Function;
import java.util.stream.Stream;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

public class DomUtils {

	public static Stream<Node> childrenStream(Node node) {
		return stream(node.getChildNodes());
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

	public static void visit(Node node, Function<Node, Boolean> consumer) {
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
