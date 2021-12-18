package com.github.vlsergey.tex2html.utils;

import static java.util.Collections.unmodifiableSet;

import java.io.File;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@UtilityClass
@Slf4j
public class TexXmlUtils {

	public static final String[] STRUCTURAL_COMMANDS = { "document", "part", "chapter", "section", "subsection",
			"subsubsection", "paragraph", "subparagraph" };

	public static final Set<String> STRUCTURAL_COMMANDS_SET = unmodifiableSet(
			new LinkedHashSet<>(Arrays.asList(STRUCTURAL_COMMANDS)));

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

	public static Optional<Element> findStructuralParent(Node node) {
		Node levelToCheck = node;
		while (levelToCheck != null) {
			final Optional<Element> opCandidate = findPreviousStructuralSibling(levelToCheck);
			if (opCandidate.isPresent()) {
				return opCandidate;
			}
			levelToCheck = levelToCheck.getParentNode();
		}
		return Optional.empty();
	}

	public static Optional<Element> findPreviousStructuralSibling(Node node) {
		Node candidate = node.getPreviousSibling();
		while (candidate != null) {
			if (isStructuralCommandElement(candidate)) {
				return Optional.of((Element) candidate);
			}
			candidate = candidate.getPreviousSibling();
		}
		return Optional.empty();
	}

	public static boolean isCommandElement(Node node, String commandName) {
		return node instanceof Element && node.getNodeName().equals("command")
				&& node.getAttributes().getNamedItem("name") != null
				&& StringUtils.equals(node.getAttributes().getNamedItem("name").getNodeValue(), commandName);
	}

	public static boolean isStructuralCommandElement(Node node) {
		return node instanceof Element && node.getNodeName().equals("command")
				&& node.getAttributes().getNamedItem("name") != null
				&& STRUCTURAL_COMMANDS_SET.contains(node.getAttributes().getNamedItem("name").getNodeValue());
	}

	public static <N extends Node, E extends Exception> N visitNodes(N root, Predicate<Node> predicate,
			ThrowingConsumer<Node, E> consumer) throws E {
		DomUtils.visit(root, node -> {
			if (predicate.test(node)) {
				consumer.accept(node);
			}
			return true;
		});
		return root;
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
