package com.github.vlsergey.tex2html.processors;

import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.github.vlsergey.tex2html.Tex2HtmlOptions;
import com.github.vlsergey.tex2html.utils.DomUtils;

import lombok.NonNull;

@Component
@Order(2000)
public class HeaderIdsProcessor implements TexXmlProcessor {

	private static final String[] HEADERS = { "h1", "h2", "h3", "h4", "h5", "h6" };

	@Override
	public @NonNull Document process(final @NonNull Tex2HtmlOptions options, final @NonNull Document xmlDoc) {

		Set<String> already = new LinkedHashSet<>();

		DomUtils.visit(xmlDoc, node -> {

			if (node.getNodeType() != Node.ELEMENT_NODE || !StringUtils.equalsAnyIgnoreCase(node.getNodeName(), HEADERS)
					|| StringUtils.isNotBlank(((Element) node).getAttribute("id"))) {
				return true;
			}

			final StringBuffer buffer = new StringBuffer();
			for (char c : node.getTextContent().toCharArray()) {
				if (Character.isDigit(c) || Character.isLetter(c)) {
					buffer.append(c);
				} else {
					if (buffer.length() == 0 || buffer.charAt(buffer.length() - 1) != '_') {
						buffer.append('_');
					}
				}
			}
			final String filtered = buffer.toString();

			int counter = 1;
			String candidate = filtered;
			while (already.contains(candidate)) {
				candidate = candidate + (counter++);
			}

			((Element) node).setAttribute("id", candidate);
			return false;
		});
		return xmlDoc;
	}

}
