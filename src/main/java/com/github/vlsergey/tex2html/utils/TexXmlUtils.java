package com.github.vlsergey.tex2html.utils;

import java.io.File;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@UtilityClass
@Slf4j
public class TexXmlUtils {

	@SneakyThrows
	public static File findFileBasePath(Node node) {
		String path = (String) XPathFactory.newInstance().newXPath().evaluate("./ancestor::file/@parent-path", node,
				XPathConstants.STRING);
		return path == null ? null : new File(path);
	}

	@SneakyThrows
	public static Element findOptionalArgumentNode(Node commandNode, int oneBasedIndex) {
		return (Element) XPathFactory.newInstance().newXPath()
				.evaluate("./argument[@required='false'][" + oneBasedIndex + "]", commandNode, XPathConstants.NODE);
	}

	@SneakyThrows
	public static String findRequiredArgument(Node commandNode, int oneBasedIndex) {
		return (String) XPathFactory.newInstance().newXPath().evaluate(
				"./argument[@required='true'][" + oneBasedIndex + "]/text()", commandNode, XPathConstants.STRING);
	}

	public static boolean isCommandElement(Node node, String commandName) {
		return node instanceof Element && node.getNodeName().equals("command")
				&& node.getAttributes().getNamedItem("name") != null
				&& StringUtils.equals(node.getAttributes().getNamedItem("name").getNodeValue(), commandName);
	}

	public static <N extends Node, E extends Exception> N visitCommandNodes(N root, String commandName,
			ThrowingConsumer<Element, E> consumer) {
		DomUtils.visit(root, node -> {
			if (TexXmlUtils.isCommandElement(node, commandName)) {
				try {
					consumer.accept((Element) node);
				} catch (Exception exc) {
					log.error("Unable to process command '" + commandName + "': " + exc.getMessage(), exc);
				}
			}

			return true;
		});
		return root;
	}

}
