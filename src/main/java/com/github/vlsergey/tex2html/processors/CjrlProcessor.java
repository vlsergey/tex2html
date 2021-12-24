package com.github.vlsergey.tex2html.processors;

import java.io.IOException;
import java.io.StringReader;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.Token;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.github.vlsergey.tex2html.Tex2HtmlOptions;
import com.github.vlsergey.tex2html.grammar.CjrlLexer;
import com.github.vlsergey.tex2html.utils.TexXmlUtils;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Component
@Order(0)
@Slf4j
public class CjrlProcessor implements TexXmlProcessor {

	private static final char[] MAPPING;

	static {
		MAPPING = new char[CjrlLexer.ruleNames.length + 1];
		MAPPING[CjrlLexer.ALEFH] = 'א';
		MAPPING[CjrlLexer.BET] = 'ב';
		MAPPING[CjrlLexer.KAPH] = 'ך';
		MAPPING[CjrlLexer.LAMED] = 'ל';
		MAPPING[CjrlLexer.SHIN] = 'ש';
	}

	private static String replaceWithUnicode(String cjEncoded) throws IOException {
		final CjrlLexer cjrlLexer = new CjrlLexer(new ANTLRInputStream(new StringReader(cjEncoded)));

		StringBuilder stringBuilder = new StringBuilder(cjEncoded.length());
		while (true) {
			final Token nextToken = cjrlLexer.nextToken();
			if (nextToken.getType() == Recognizer.EOF) {
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

	private final XPathFactory xPathFactory = XPathFactory.newInstance();

	@SneakyThrows
	private String getText(Node commandInvocation) {
		return (String) xPathFactory.newXPath().evaluate("./argument/text()", commandInvocation, XPathConstants.STRING);
	}

	@Override
	public Document process(Tex2HtmlOptions command, Document xmlDoc) {
		return TexXmlUtils.visitCommandNodes(xmlDoc, "cjRL", this::processImpl);
	}

	private void processImpl(Node node) throws IOException {
		final String text = getText(node);
		final String replacement = replaceWithUnicode(text);
		log.info("Will replace cjRL text '{}' with '{}'", text, replacement);

		final Element span = node.getOwnerDocument().createElement("span");
		span.setAttribute("lang", "he");
		span.setTextContent(replacement);
		node.getParentNode().replaceChild(span, node);
	}

}
