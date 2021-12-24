package com.github.vlsergey.tex2html.processors;

import static java.util.Collections.emptyMap;

import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.github.vlsergey.tex2html.utils.TexXmlUtils;

import lombok.Getter;
import lombok.Setter;

@Component
@Order(0)
@ConfigurationProperties(prefix = "app.languages")
public class LanguageProcessor implements TexXmlProcessor {

	private static final String ATTR_LANGUAGE = "language";

	private static final String ATTR_LANGUAGE_CODE = "language-code";

	@Getter
	@Setter
	private String fallback;

	@Getter
	@Setter
	private Map<String, String> langToCode = emptyMap();

	@Getter
	@Setter
	private Map<String, Map<String, String>> langToLabels = emptyMap();

	private Optional<String> findLanguage(Node node) {
		Node candidate = node;
		while (candidate != null) {
			if (candidate.getAttributes() != null && candidate.getAttributes().getNamedItem(ATTR_LANGUAGE) != null) {
				return Optional.ofNullable(node.getAttributes().getNamedItem(ATTR_LANGUAGE).getNodeValue());
			}

			String structureElementLanguage = TexXmlUtils.findPreviousStructuralSibling(candidate)
					.filter(element -> element.hasAttribute(ATTR_LANGUAGE))
					.map(element -> element.getAttribute(ATTR_LANGUAGE)).orElse(null);
			if (structureElementLanguage != null) {
				return Optional.of(structureElementLanguage);
			}

			candidate = candidate.getParentNode();
		}

		return Optional.empty();
	}

	@Override
	public Document process(Document xmlDoc) {
		TexXmlUtils.visitCommandNodes(xmlDoc, "selectlanguage", command -> {
			final String language = TexXmlUtils.findRequiredArgument(command, 1);
			if (StringUtils.isNotBlank(language)) {
				final String langCode = langToCode.getOrDefault(language.toLowerCase(), language);

				TexXmlUtils.findStructuralParent(command).ifPresent(section -> {
					section.setAttribute(ATTR_LANGUAGE, language);
					section.setAttribute(ATTR_LANGUAGE_CODE, langCode);
				});
			}

			command.getParentNode().removeChild(command);
		});

		TexXmlUtils.visitNodes(xmlDoc,
				node -> node instanceof Element
						&& (TexXmlUtils.STRUCTURAL_COMMANDS_SET.contains(((Element) node).getAttribute("name"))
								|| "figure".equals(((Element) node).getAttribute("name"))
								|| "table".equals(((Element) node).getAttribute("name"))),
				node -> {
					final Element command = (Element) node;
					final String language = findLanguage(command).orElse(fallback);
					final Map<String, String> localizedLabels = this.langToLabels.getOrDefault(language, emptyMap());
					final String commandName = command.getAttribute("name");
					final String label = localizedLabels.getOrDefault(commandName, StringUtils.capitalize(commandName));
					command.setAttribute("localized-label", label);
				});

		return xmlDoc;
	}
}
