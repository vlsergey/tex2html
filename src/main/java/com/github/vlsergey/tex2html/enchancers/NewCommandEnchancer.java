package com.github.vlsergey.tex2html.enchancers;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class NewCommandEnchancer {

	private static DocumentFragment importAsFragment(Document doc, NodeList toImport) {
		final DocumentFragment docFragment = doc.createDocumentFragment();
		for (int i = 0; i < toImport.getLength(); i++) {
			docFragment.appendChild(doc.importNode(toImport.item(i), true));
		}
		return docFragment;
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

	private final XPathFactory xPathFactory = XPathFactory.newInstance();

	@SneakyThrows
	private Node getCommandArgumentFromInvocation(Node commandInvocation, int pos) {
		return (Node) xPathFactory.newXPath().evaluate("./argument[position()=" + pos + "]", commandInvocation,
				XPathConstants.NODE);
	}

	@SneakyThrows
	private Element getCommandContentFromDefinition(Node commandDefNode) {
		return (Element) xPathFactory.newXPath().evaluate("./argument[@required='true'][position()=2]", commandDefNode,
				XPathConstants.NODE);
	}

	@SneakyThrows
	private String getCommandNameFromDefinition(Node commandDefNode) {
		return (String) xPathFactory.newXPath().evaluate("./argument[@required='true'][position()=1]/command/@name",
				commandDefNode, XPathConstants.STRING);
	}

	@SneakyThrows
	private NodeList getNodesByText(Node node, String text) {
		return (NodeList) xPathFactory.newXPath().evaluate(".//text()[.='" + text + "']", node, XPathConstants.NODESET);
	}

	@SneakyThrows
	public void process(Document doc) {
		Map<String, Element> commandDefs = new LinkedHashMap<>();

		visit(doc, node -> {
			if (node instanceof Element && node.getNodeName().equals("command")
					&& node.getAttributes().getNamedItem("name") != null
					&& StringUtils.equals(node.getAttributes().getNamedItem("name").getNodeValue(), "newcommand")) {
				final String commandName = getCommandNameFromDefinition(node);
				log.info("Found definition of '{}' command", commandName);
				commandDefs.put(commandName, (Element) node);
				return false;
			}

			if (node instanceof Element && node.getNodeName().equals("command")
					&& node.getAttributes().getNamedItem("name") != null
					&& commandDefs.containsKey(node.getAttributes().getNamedItem("name").getNodeValue())) {
				final String commandName = node.getAttributes().getNamedItem("name").getNodeValue();
				log.info("Found usage of previously defined '{}' command", commandName);

				final Element commandDefNode = commandDefs.get(commandName);
				final Element contentWithWrapper = (Element) doc
						.importNode(getCommandContentFromDefinition(commandDefNode), true);

				for (int i = 1; i <= 9; i++) {
					final NodeList toReplaceList = getNodesByText(contentWithWrapper, "#" + i);
					if (toReplaceList != null && toReplaceList.getLength() != 0) {
						Node parentToReplaceWith = getCommandArgumentFromInvocation(node, i);
						if (parentToReplaceWith != null) {
							for (int n = 0; n < toReplaceList.getLength(); n++) {
								final Node toReplace = toReplaceList.item(n);

								final DocumentFragment toReplaceWith = importAsFragment(doc,
										parentToReplaceWith.getChildNodes());
								toReplace.getParentNode().replaceChild(toReplaceWith, toReplace);
							}
						}
					}
				}

				node.getParentNode().replaceChild(importAsFragment(doc, contentWithWrapper.getChildNodes()), node);
			}
			return true;
		});
	}

}
