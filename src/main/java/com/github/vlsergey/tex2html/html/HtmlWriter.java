package com.github.vlsergey.tex2html.html;

import java.nio.charset.StandardCharsets;
import java.util.Deque;
import java.util.LinkedList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class HtmlWriter {

	private final Document doc;

	private Deque<Element> stack = new LinkedList<>();

	public HtmlWriter() throws ParserConfigurationException {
		final DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		doc = documentBuilder.newDocument();
		doc.appendChild(doc.createElement("HTML"));
		stack.push(doc.getDocumentElement());
	}

	public void beginElement(String tagName) {
		final Element newElement = doc.createElement(tagName);
		stack.peek().appendChild(newElement);
		stack.push(newElement);
	}

	public void appendComment(String data) {
		stack.peek().appendChild(doc.createComment(data));
	}

	public void appendTextNode(String data) {
		stack.peek().appendChild(doc.createTextNode(data));
	}

	public void endElement(String tagName) {
		final Element toClose = stack.poll();
		if (!StringUtils.equals(toClose.getTagName(), tagName)) {
			throw new IllegalStateException("Closing wront tag: " + toClose.getTagName() + ", but assumed " + tagName);
		}
	}

	public void write(StreamResult to) throws TransformerException {
		Transformer transformer = TransformerFactory.newInstance().newTransformer();
		transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, "about:legacy-compat");
		transformer.setOutputProperty(OutputKeys.ENCODING, StandardCharsets.UTF_8.name());
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		transformer.setOutputProperty(OutputKeys.METHOD, "html");
		transformer.setOutputProperty(OutputKeys.VERSION, "5");
		transformer.transform(new DOMSource(doc), to);
	}
}
