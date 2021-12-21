package com.github.vlsergey.tex2html.processors.bib;

import static java.util.Collections.unmodifiableMap;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.joining;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

@Component
public class GostRenderer {

	private static final Map<String, String> RENDER_MONTHES;

	static {
		final Map<String, String> map = new LinkedHashMap<>();
		map.put("1", "Янв");
		map.put("2", "Фев");
		map.put("3", "Март");
		map.put("4", "Апр");
		map.put("5", "Май");
		map.put("6", "Июнь");
		map.put("7", "Июль");
		map.put("8", "Авг");
		map.put("9", "Сен");
		map.put("10", "Окт");
		map.put("11", "Нов");
		map.put("12", "Дек");
		RENDER_MONTHES = unmodifiableMap(map);
	}

	public RendererSource render(Document doc, SourceDef def) {

		Element element = doc.createElement(def.getType());

		SortKey sortKey = new SortKey();

		final String[] authors = def.getAttributes().get("author");
		if (authors != null && authors.length < 3) {
			final String renderedAuthors = Arrays.stream(authors).map(this::renderAuthor).collect(joining(", "));

			final Element i = doc.createElement("i");
			i.setAttribute("class", "bib-authors");
			i.setTextContent(renderedAuthors);
			element.appendChild(i);
			element.appendChild(doc.createTextNode(" "));

			sortKey.getName().append(renderedAuthors);
		}

		final String title = StringUtils.join(def.getAttributes().get("title"), " and ");
		if (title != null) {
			element.appendChild(doc.createTextNode(title));
			sortKey.getTitle().append(title);
		}

		final String journal = StringUtils.join(def.getAttributes().get("journal"), " and ");
		if (journal != null) {
			if (title != null) {
				element.appendChild(doc.createTextNode(" // "));
			}

			element.appendChild(doc.createTextNode(journal));
			element.appendChild(doc.createTextNode("."));
		}

		final String location = StringUtils.join(def.getAttributes().get("location"), " and ");
		final String publisher = StringUtils.join(def.getAttributes().get("publisher"), " and ");
		if (location != null || publisher != null) {
			element.appendChild(doc.createTextNode(" — "));
			if (location != null) {
				element.appendChild(doc.createTextNode(location));
			}
			if (location != null && publisher != null) {
				element.appendChild(doc.createTextNode(" : "));
			}
			if (publisher != null) {
				element.appendChild(doc.createTextNode(publisher));
			}
			element.appendChild(doc.createTextNode("."));
		}

		append(def, "year", "", year -> {
			sortKey.getYear().append(year).append(";");
			return doc.createTextNode(year);
		}, ", ", element);
		append(def, "month", "", str -> doc.createTextNode(RENDER_MONTHES.getOrDefault(str, str)), ", ", element);

		final String[] volumes = def.getAttributes().get("volume");
		final String[] numbers = def.getAttributes().get("number");
		if (volumes != null || numbers != null) {
			element.appendChild(doc.createTextNode(" — "));

			if (volumes != null) {
				final String joined = StringUtils.join(volumes, ", ");
				element.appendChild(doc.createTextNode("Т. " + joined));
				sortKey.getVolume().append(joined);
			}
			if (volumes != null && numbers != null) {
				element.appendChild(doc.createTextNode(", "));
			}
			if (numbers != null) {
				element.appendChild(
						doc.createTextNode(numbers.length > 1 ? "№№ " : "№ " + StringUtils.join(numbers, ", ")));
			}

			element.appendChild(doc.createTextNode("."));
		}

		append(def, "pages", "С. ", doc::createTextNode, ", ", element);
		append(def, "issn", "ISSN ", doc::createTextNode, ", ", element);
		append(def, "doi", "DOI ", doi -> {
			final Element a = doc.createElement("a");
			a.setAttribute("href", "https://doi.org/" + doi);
			a.setAttribute("style", "font-family: monospace;");
			a.appendChild(doc.createTextNode(doi));
			return a;
		}, ", ", element);

		return new RendererSource(def.getAlphabeticLabel(), element, sortKey);
	}

	private static void append(SourceDef def, String sourceAttr, String prefix,
			Function<String, Node> singleElementMapping, String separator, Element parent) {
		final String[] data = def.getAttributes().get(sourceAttr);
		if (data == null || data.length == 0) {
			return;
		}

		Document doc = parent.getOwnerDocument();

		parent.appendChild(doc.createTextNode(" — " + prefix));

		for (int i = 0; i < data.length; i++) {
			if (i != 0) {
				parent.appendChild(doc.createTextNode(separator));
			}
			parent.appendChild(singleElementMapping.apply(data[i]));
		}
		parent.appendChild(doc.createTextNode("."));
	}

	private String renderAuthor(String src) {
		if (src.contains(",")) {
			String familyNameStr = StringUtils.substringBefore(src, ",").trim();
			String givenNamesStr = StringUtils.substringAfter(src, ",").trim();

			String givenNamesShort = Arrays.stream(givenNamesStr.split(" ")).map(String::trim)
					.map(str -> str.substring(0, 1)).map(letter -> letter + ".").collect(joining(" "));
			return familyNameStr + " " + givenNamesShort;
		}

		throw new UnsupportedOperationException("NYI");
	}

}
