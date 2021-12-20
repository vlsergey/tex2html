package com.github.vlsergey.tex2html.processors;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.github.vlsergey.tex2html.utils.TexXmlUtils;

import lombok.SneakyThrows;

@Component
@Order(0)
public class LabelProcessor implements TexXmlProcessor {

	private final XPathFactory xPathFactory = XPathFactory.newInstance();

	@Override
	@SneakyThrows
	public Document process(Document xmlDoc) {
		return TexXmlUtils.visitCommandNodes(xmlDoc, "label", labelCommand -> {
			final String labelName = TexXmlUtils.findRequiredArgument(labelCommand, 1);
			if (labelName == null) {
				return;
			}

			final Optional<Element> structuralSibling = TexXmlUtils.findPreviousStructuralSibling(labelCommand);
			if (structuralSibling.isPresent()) {
				final Element structural = structuralSibling.get();
				structural.setAttribute("label", labelName);
				labelCommand.getParentNode().removeChild(labelCommand);
				return;
			}

			Element candidate = (Element) xPathFactory.newXPath().evaluate("ancestor::command[@name='figure' or @name='subcaptionbox'][1]", labelCommand, XPathConstants.NODE);
			if (candidate != null) {
				candidate.setAttribute("label", labelName);
				labelCommand.getParentNode().removeChild(labelCommand);
				return;
			}

			labelCommand.setAttribute("label", labelName);
		});
	}

}
