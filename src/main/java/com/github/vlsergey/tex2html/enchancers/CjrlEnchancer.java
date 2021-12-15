package com.github.vlsergey.tex2html.enchancers;

import java.io.StringReader;
import java.util.function.Function;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.UnbufferedCharStream;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.github.vlsergey.tex2html.grammar.CjrlLexer;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class CjrlEnchancer {

	private static final char[] MAPPING;

	static {
		MAPPING = new char[CjrlLexer.ruleNames.length + 1];
		MAPPING[CjrlLexer.ALEFH] = 'א';
		MAPPING[CjrlLexer.BET] = 'ב';
		MAPPING[CjrlLexer.KAPH] = 'ך';
		MAPPING[CjrlLexer.LAMED] = 'ל';
		MAPPING[CjrlLexer.SHIN] = 'ש';
	}

	private static String replaceWithUnicode(String cjEncoded) {
		final CjrlLexer cjrlLexer = new CjrlLexer(new UnbufferedCharStream(new StringReader(cjEncoded)));

		StringBuilder stringBuilder = new StringBuilder(cjEncoded.length());
		while (true) {
			final Token nextToken = cjrlLexer.nextToken();
			if (nextToken.getType() == CjrlLexer.EOF) {
				break;
			}

			if (nextToken.getType() <= 0 || MAPPING[nextToken.getType()] == 0) {
				stringBuilder.append('?');
			} else {
				stringBuilder.append(MAPPING[nextToken.getType()]);
			}
		}

		return stringBuilder.toString();
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
	private String getText(Node commandInvocation) {
		return (String) xPathFactory.newXPath().evaluate("./argument/text()", commandInvocation, XPathConstants.STRING);
	}

	@SneakyThrows
	public void process(Document doc) {
		visit(doc, node -> {
			if (node instanceof Element && node.getNodeName().equals("command")
					&& node.getAttributes().getNamedItem("name") != null
					&& StringUtils.equals(node.getAttributes().getNamedItem("name").getNodeValue(), "cjRL")) {
				final String text = getText(node);
				final String replacement = replaceWithUnicode(text);
				log.info("Will replace cjRL text '{}' with '{}'", text, replacement);

				final Element span = doc.createElement("span");
				span.setAttribute("lang", "he");
				span.setTextContent(replacement);
				node.getParentNode().replaceChild(span, node);

				return false;
			}

			return true;
		});
	}

}
