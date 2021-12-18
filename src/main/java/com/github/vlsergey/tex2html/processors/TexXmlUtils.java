package com.github.vlsergey.tex2html.processors;

import java.io.File;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

@UtilityClass
class TexXmlUtils {

	@SneakyThrows
	static File findFileBasePath(Node node) {
		String path = (String) XPathFactory.newInstance().newXPath().evaluate("./ancestor::file/@parent-path", node,
				XPathConstants.STRING);
		return path == null ? null : new File(path);
	}

	@SneakyThrows
	static Element findOptionalArgumentNode(Node commandNode, int oneBasedIndex) {
		return (Element) XPathFactory.newInstance().newXPath()
				.evaluate("./argument[@required='false'][" + oneBasedIndex + "]", commandNode, XPathConstants.NODE);
	}

	@SneakyThrows
	static String findRequiredArgument(Node commandNode, int oneBasedIndex) {
		return (String) XPathFactory.newInstance().newXPath().evaluate(
				"./argument[@required='true'][" + oneBasedIndex + "]/text()", commandNode, XPathConstants.STRING);
	}

	static boolean isCommandElement(Node node, String commandName) {
		return node instanceof Element && node.getNodeName().equals("command")
				&& node.getAttributes().getNamedItem("name") != null
				&& StringUtils.equals(node.getAttributes().getNamedItem("name").getNodeValue(), commandName);
	}

}
