package com.github.vlsergey.tex2html.processors.bib;

import static java.util.Collections.unmodifiableMap;
import static java.util.stream.Collectors.toList;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import com.github.vlsergey.tex2html.utils.DomUtils;

import lombok.NonNull;
import lombok.SneakyThrows;

@Component
public class GostRenderer {

	private static final Consumer<String> emptyConsumer = (a) -> {
	};

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

	private static <T> void withSeparator(List<T> src, Consumer<T> itemConsumer, Runnable separator) {
		for (int i = 0; i < src.size(); i++) {
			if (i != 0) {
				separator.run();
			}
			itemConsumer.accept(src.get(i));
		}
	}

	private final XPathFactory xPathFactory = XPathFactory.newInstance();

	private final void append(Element def, String attrName, String prefix, BiConsumer<Element, Element> attr2dst,
			String separator, String suffix, Consumer<String> onTextContent, Element dst) {
		final Document doc = def.getOwnerDocument();
		final List<Element> values = getAttribute(def, attrName);
		if (!values.isEmpty()) {
			dst.appendChild(doc.createTextNode(prefix));

			final Element container = doc.createElement("span");

			withSeparator(values, item -> attr2dst.accept(item, container),
					() -> container.appendChild(doc.createTextNode(separator)));

			onTextContent.accept(container.getTextContent());
			dst.appendChild(container);
			dst.appendChild(doc.createTextNode(suffix));
		}
	}

	private void appendAuthor(Element attribute, Element dst) {
		final Document doc = dst.getOwnerDocument();
		final List<List<Node>> splitted = DomUtils.splitOnce(attribute, ",");

		if (splitted.size() == 1) {
			DomUtils.childrenStream(attribute).forEach(child -> dst.appendChild(child.cloneNode(true)));
		}

		splitted.get(0).forEach(child -> dst.appendChild(child.cloneNode(true)));
		dst.appendChild(doc.createTextNode(" "));
		DomUtils.split(splitted.get(0).stream(), " ").forEach(givenNameNodes -> {
			if (givenNameNodes.get(0) instanceof Text) {
				final String letterWithDot = givenNameNodes.get(0).getNodeValue().substring(0, 1).toUpperCase() + ".";
				dst.appendChild(doc.createTextNode(letterWithDot));
			} else {
				givenNameNodes.forEach(child -> dst.appendChild(child.cloneNode(true)));
			}
		});

		// trim last
		final Node last = dst.getChildNodes().item(dst.getChildNodes().getLength() - 1);
		if (last instanceof Text && last.getNodeValue().endsWith(" ")) {
			last.setNodeValue(last.getNodeValue().stripTrailing());
		}
	}

	@SneakyThrows
	private @NonNull List<Element> getAttribute(final @NonNull Element def, final String attrName) {
		final XPath xPath = xPathFactory.newXPath();
		return DomUtils.stream((NodeList) xPath.evaluate("attr[@name='" + attrName + "']", def, XPathConstants.NODESET))
				.map(node -> (Element) node).collect(toList());
	}

	public RendererSource render(Element def) {
		final Document doc = def.getOwnerDocument();
		final Element result = doc.createElement(def.getNodeName());

		SortKey sortKey = new SortKey();

		final List<Element> authors = getAttribute(def, "author");
		if (!authors.isEmpty() && authors.size() < 3) {

			final Element authorsContainer = doc.createElement("i");

			withSeparator(authors, author -> appendAuthor(author, authorsContainer),
					() -> authorsContainer.appendChild(doc.createTextNode(", ")));

			result.appendChild(authorsContainer);
			sortKey.getName().append(authorsContainer.getTextContent());
			result.appendChild(doc.createTextNode(" "));
		}

		append(def, "title", "", DomUtils::copyChildren, ", ", "", sortKey.getTitle()::append, result);
		append(def, "journal", " // ", DomUtils::copyChildren, ", ", ".", emptyConsumer, result);

		final @NonNull List<Element> locations = getAttribute(def, "location");
		final @NonNull List<Element> publishers = getAttribute(def, "publisher");
		if (!locations.isEmpty() || publishers.isEmpty()) {
			result.appendChild(doc.createTextNode(" — "));
			if (locations != null) {
				withSeparator(locations, location -> DomUtils.copyChildren(location, result),
						() -> result.appendChild(doc.createTextNode(", ")));
			}
			if (locations != null && publishers != null) {
				result.appendChild(doc.createTextNode(" : "));
			}
			if (publishers != null) {
				withSeparator(publishers, publisher -> DomUtils.copyChildren(publisher, result),
						() -> result.appendChild(doc.createTextNode(", ")));
			}
			result.appendChild(doc.createTextNode("."));
		}

		append(def, "year", " — ", DomUtils::copyChildren, ", ", ".", sortKey.getYear()::append, result);

		append(def, "month", " — ",
				(month, monthesContainer) -> Optional.ofNullable(RENDER_MONTHES.get(month.getTextContent()))
						.ifPresentOrElse(monthStr -> monthesContainer.appendChild(doc.createTextNode(monthStr)),
								() -> DomUtils.copyChildren(month, monthesContainer)),
				", ", ".", emptyConsumer, result);

		final @NonNull List<Element> volumes = getAttribute(def, "volume");
		final @NonNull List<Element> numbers = getAttribute(def, "number");
		if (!volumes.isEmpty() || !numbers.isEmpty()) {
			result.appendChild(doc.createTextNode(" — "));
			append(def, "volume", "Т. ", DomUtils::copyChildren, ", ", "", emptyConsumer, result);
			if (!volumes.isEmpty() && !numbers.isEmpty()) {
				result.appendChild(doc.createTextNode(", "));
			}
			append(def, "number", "№ ", DomUtils::copyChildren, ", ", "", emptyConsumer, result);
			result.appendChild(doc.createTextNode("."));
		}

		append(def, "pages", " — С. ", DomUtils::copyChildren, ", ", ". ", emptyConsumer, result);
		append(def, "issn", " — ISSN ", DomUtils::copyChildren, ", ", ". ", emptyConsumer, result);

		append(def, "doi", " — DOI ", (doiSrc, doiContainer) -> {
			final Element a = doc.createElement("a");
			a.setAttribute("href", "https://doi.org/" + doiSrc.getTextContent());
			a.setAttribute("style", "font-family: monospace;");
			DomUtils.copyChildren(doiSrc, a);
			doiContainer.appendChild(a);
		}, ", ", ". ", emptyConsumer, result);

		return new RendererSource(def.getAttribute("name"), result, sortKey);
	}

}
