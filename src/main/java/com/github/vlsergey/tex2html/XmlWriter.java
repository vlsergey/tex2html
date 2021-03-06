package com.github.vlsergey.tex2html;

import java.util.Deque;
import java.util.LinkedList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.github.vlsergey.tex2html.utils.ThrowingRunnable;

import lombok.Getter;
import lombok.NonNull;

public class XmlWriter {

	@Getter
	private final @NonNull Document doc;

	private final @NonNull Deque<Element> stack = new LinkedList<>();

	public XmlWriter() throws ParserConfigurationException {
		final DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		doc = documentBuilder.newDocument();
		doc.appendChild(doc.createElement("project"));
		stack.push(doc.getDocumentElement());
	}

	public void appendComment(String data) {
		stack.peek().appendChild(doc.createComment(data));
	}

	public void appendElement(String tagName) {
		stack.peek().appendChild(doc.createElement(tagName));
	}

	public void appendTextNode(String data) {
		stack.peek().appendChild(doc.createTextNode(data));
	}

	public void beginElement(String tagName) {
		final Element newElement = doc.createElement(tagName);
		stack.peek().appendChild(newElement);
		stack.push(newElement);
	}

	public void endElement(String tagName) {
		final Element toClose = stack.poll();
		if (!StringUtils.equals(toClose.getTagName(), tagName)) {
			throw new IllegalStateException("Closing wront tag: " + toClose.getTagName() + ", but assumed " + tagName);
		}
	}

	public <E extends Throwable> void inElement(String tagName, ThrowingRunnable<E> runnable) throws E {
		final Element newElement = doc.createElement(tagName);
		stack.peek().appendChild(newElement);
		stack.push(newElement);

		// do not use finally here to prevent main error loosing

		runnable.run();
		final Element toClose = stack.poll();
		if (!StringUtils.equals(toClose.getTagName(), tagName)) {
			throw new IllegalStateException("Closing wront tag: " + toClose.getTagName() + ", but expected " + tagName);
		}
	}

	public void setAttribute(String name, String value) {
		stack.peek().setAttribute(name, value);
	}

}
