package com.github.vlsergey.tex2html.utils;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import lombok.NonNull;

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

	public static final void copyChildren(Element src, Element dst) {
		DomUtils.childrenStream(src).forEach(child -> dst.appendChild(child.cloneNode(true)));
	}

	public static final @NonNull List<List<Node>> split(final @NonNull Node parent, final @NonNull String separator) {
		concatenateTextNodes(parent);
		return split(childrenStream(parent), separator);
	}

	public static final @NonNull List<List<Node>> split(final @NonNull Stream<Node> nodes,
			final @NonNull String separator) {
		return StreamUtils.group((nextGroup, flush) -> {
			nodes.forEach(node -> {
				if (node instanceof Text) {
					String value = node.getNodeValue();
					if (value.indexOf(separator) != -1) {
						String[] splitted = StringUtils.splitByWholeSeparatorPreserveAllTokens(value, separator);
						for (String token : splitted) {
							flush.run();
							if (!token.isEmpty()) {
								nextGroup.add(node.getOwnerDocument().createTextNode(token));
							}
						}
						return;
					}
				}
				nextGroup.add(node);
			});
		});
	}

	public static List<List<Node>> splitOnce(Node parent, String separator) {
		concatenateTextNodes(parent);
		final List<Node> children = childrenStream(parent).collect(toList());

		for (int i = 0; i < children.size(); i++) {
			Node node = children.get(i);
			if (node instanceof Text) {
				String value = node.getNodeValue();
				int indexOf = value.indexOf(separator);
				if (indexOf != -1) {
					String before = value.substring(0, indexOf).stripTrailing();
					String after = value.substring(indexOf + separator.length()).stripLeading();

					List<Node> nodesBefore = new ArrayList<>(i > 0 ? children.subList(0, i - 1) : emptyList());
					if (!before.isEmpty()) {
						nodesBefore.add(parent.getOwnerDocument().createTextNode(before));
					}

					List<Node> nodesAfter = new ArrayList<>(
							i < children.size() - 1 ? children.subList(i + 1, children.size()) : emptyList());
					if (!after.isEmpty()) {
						nodesAfter.add(parent.getOwnerDocument().createTextNode(after));
					}

					return Arrays.asList(nodesBefore, nodesAfter);
				}
			}
		}

		return singletonList(children);
	}

	public static Stream<Node> stream(NodeList nodeList) {
		return Stream.iterate(0, i -> i + 1).limit(nodeList.getLength()).map(nodeList::item);
	}

	public static List<Node> trim(List<Node> nodes) {
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

		return nodes;
	}

	public static <E extends Throwable> void visit(Node node, ThrowingFunction<Node, Boolean, E> consumer) throws E {
		if (!consumer.apply(node)) {
			return;
		}
		NodeList list = node.getChildNodes();
		for (int i = 0; i < list.getLength(); i++) {
			Node childNode = list.item(i);
			visit(childNode, consumer);
			if (list.item(i) != childNode) {
				i--;
			}
		}
	}

}
