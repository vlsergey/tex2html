package com.github.vlsergey.tex2html.processors.bib;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.github.vlsergey.tex2html.processors.TexXmlProcessor;
import com.github.vlsergey.tex2html.utils.DomUtils;
import com.github.vlsergey.tex2html.utils.TexXmlUtils;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Component
@Order(0)
@Slf4j
public class BibliographyProcessor implements TexXmlProcessor {

	@Autowired
	private GostRenderer gostRenderer;

	private final XPathFactory xPathFactory = XPathFactory.newInstance();

	@Override
	@SneakyThrows
	public Document process(Document xmlDoc) {
		Map<String, Element> bibliographyElements = new LinkedHashMap<>();

		DomUtils.stream((NodeList) xPathFactory.newXPath().evaluate("/project//bibliography-resource/*", xmlDoc,
				XPathConstants.NODESET)).map(node -> (Element) node)
				.forEach(element -> bibliographyElements.put(element.getAttribute("name"), element));

		Set<String> used = new LinkedHashSet<>();

		TexXmlUtils.visitCommandNodes(xmlDoc, "cite", command -> {
			final String refStr = TexXmlUtils.findRequiredArgument(command, 1);
			if (StringUtils.isBlank(refStr)) {
				return;
			}

			final String[] refs = StringUtils.split(refStr, ", ");
			final Element cite = xmlDoc.createElement("cite");
			for (String ref : refs) {
				final Element refElement = xmlDoc.createElement("ref");
				refElement.setAttribute("name", ref);
				cite.appendChild(refElement);
				used.add(ref);
			}

			command.getParentNode().replaceChild(cite, command);
		});

		log.info("Found {} different bib sources references with \\cite", used.size());

		List<RendererSource> renderedSourcesList = new ArrayList<>(used.size());
		used.forEach(citeName -> {
			Element def = bibliographyElements.get(citeName);
			if (def == null) {
				log.warn("At least 1 reference to unknown bib source '{}'", citeName);
				return;
			}

			renderedSourcesList.add(gostRenderer.render(def));
		});

		Collections.sort(renderedSourcesList,
				Comparator.comparing(RendererSource::getSortKey, SortKey.comparator(SortKey.DEFAULT_SORTING)));
		for (int i = 0; i < renderedSourcesList.size(); i++) {
			final RendererSource rendered = renderedSourcesList.get(i);
			rendered.getResult().setAttribute("index", String.valueOf(i + 1));
			rendered.getResult().setAttribute("name", rendered.getAlphabeticLabel());
		}

		TexXmlUtils.visitCommandNodes(xmlDoc, "printbibliography", command -> {
			Element bibliographyElement = xmlDoc.createElement("printbibliography");
			renderedSourcesList.forEach(rs -> {
				bibliographyElement.appendChild(rs.getResult().cloneNode(true));
			});

			command.getParentNode().replaceChild(bibliographyElement, command);
		});

		return xmlDoc;
	}

}
