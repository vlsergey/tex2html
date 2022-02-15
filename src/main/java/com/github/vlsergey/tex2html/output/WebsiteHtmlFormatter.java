package com.github.vlsergey.tex2html.output;

import static org.apache.commons.io.FileUtils.forceMkdir;

import java.io.File;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import javax.annotation.Nullable;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.github.vlsergey.tex2html.Tex2HtmlOptions;
import com.github.vlsergey.tex2html.XmlWriter;
import com.github.vlsergey.tex2html.utils.DomUtils;
import com.github.vlsergey.tex2html.utils.HtmlUtils;
import com.github.vlsergey.tex2html.utils.StreamUtils;
import com.github.vlsergey.tex2html.utils.TranslitirateRu;

import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class WebsiteHtmlFormatter implements OutputFormatter {

	public static final String[] OWN_FILE_HEADERS = { "h1", "h2" };

	private static boolean onlyHeaders(List<@NonNull Node> nextGroup) {
		return nextGroup.stream()
				.allMatch(node -> node.getNodeType() == Node.ELEMENT_NODE
						&& StringUtils.equalsAnyIgnoreCase(node.getNodeName(), OWN_FILE_HEADERS)
						|| StringUtils.isBlank(node.getTextContent()));
	}

	private final TransformerFactory transformerFactory = TransformerFactory.newInstance();

	private final XPathFactory xPathFactory = XPathFactory.newInstance();

	@SneakyThrows
	private @NonNull Document buildHtml(final @NonNull Document xml, final @NonNull File outputFolder) {
		Document newDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
		newDoc.appendChild(newDoc.importNode(xml.getDocumentElement(), true));

		File imagesFolder = new File(outputFolder, "images");
		forceMkdir(imagesFolder);

		HtmlUtils.replaceImagesWithLocalOnes(newDoc, imagesFolder);

		newDoc = DomUtils.transform(newDoc, "/generate-toc.xslt");

		return newDoc;
	}

	@Override
	public boolean isSupported(OutputFormat format) {
		return format == OutputFormat.WEBSITE;
	}

	@Override
	@SneakyThrows
	public void process(@NonNull Tex2HtmlOptions options, final @NonNull Document xml,
			final @NonNull File outputFolder) {
		forceMkdir(outputFolder);

		Document singleHtml = buildHtml(xml, outputFolder);

		log.info("Splitting...");
		List<@NonNull List<@NonNull Node>> splitted = StreamUtils.group((nextGroup, flush) -> {
			final NodeList topLevelElements = (NodeList) xPathFactory.newXPath().evaluate("/html/body/node()",
					singleHtml, XPathConstants.NODESET);

			for (int i = 0; i < topLevelElements.getLength(); i++) {
				final Node item = topLevelElements.item(i);
				if (item.getNodeType() == Node.ELEMENT_NODE
						&& StringUtils.equalsAnyIgnoreCase(item.getNodeName(), OWN_FILE_HEADERS)) {
					Element header = (Element) item;
					if (header.getAttribute("id") != "table-of-contents") {
						if (!onlyHeaders(nextGroup)) {
							flush.run();
						}
					}
				}
				nextGroup.add(item);
			}
		});

		Map<@NonNull String, @NonNull Document> parts = new LinkedHashMap<>();
		splitted.forEach(group -> {
			final @NonNull Document part = toNewHtml(singleHtml, group);
			final @Nullable String partTitle = getPartTitle(part);

			if (parts.isEmpty()) {
				parts.put("index", part);
			} else {
				final @NonNull String partFileName = generateFileName(parts.keySet(), partTitle,
						() -> ("part_" + (parts.size() + 1)));
				parts.put(partFileName, part);
			}
		});

		updateInternalLinks(parts);

		final @NonNull Transformer toHtml = transformerFactory.newTransformer();
		HtmlUtils.setHtmlOutputProperties(toHtml, options.isIndent());

		final @NonNull Transformer bootstrap = transformerFactory
				.newTransformer(new StreamSource(XmlWriter.class.getResourceAsStream("/bootstrap.xslt")));

		final String[] partKeys = parts.keySet().toArray(String[]::new);
		for (int i = 0; i < partKeys.length; i++) {
			final String partKey = partKeys[i];
			log.info("Generating {}...", partKey + ".html");

			@NonNull
			Document toWrite = parts.get(partKey);

			bootstrap.setParameter("prevPartLink", i == 0 ? Boolean.FALSE : partKeys[i - 1] + ".html");
			bootstrap.setParameter("prevPartTitle", i == 0 ? Boolean.FALSE : partKeys[i - 1]);
			bootstrap.setParameter("nextPartLink",
					i == partKeys.length - 1 ? Boolean.FALSE : partKeys[i + 1] + ".html");
			bootstrap.setParameter("nextPartTitle", i == partKeys.length - 1 ? Boolean.FALSE : partKeys[i + 1]);
			toWrite = DomUtils.transform(toWrite, bootstrap);

			toWrite = DomUtils.transform(toWrite, "/format-to-html.xslt");
			toWrite = DomUtils.transform(toWrite, "/mathjax.xslt");

			toHtml.transform(new DOMSource(toWrite), new StreamResult(new File(outputFolder, partKey + ".html")));
		}
	}

	@NonNull
	private String generateFileName(final Set<@NonNull String> alreadyUsed, final @Nullable String partTitle,
			final @NonNull Supplier<@NonNull String> defaultTitle) {
		if (partTitle == null || partTitle.isBlank()) {
			return defaultTitle.get();
		}

		String translitirated = TranslitirateRu.transliterate(partTitle);
		if (translitirated.matches("^[0-9_]*$")) {
			return defaultTitle.get();
		}

		String candidate = translitirated;
		int counter = 1;
		while (alreadyUsed.contains(candidate)) {
			candidate = translitirated + "_" + (++counter);
		}
		return candidate;
	}

	@Nullable
	@SneakyThrows
	private String getPartTitle(final Document part) {
		return xPathFactory.newXPath().evaluateExpression("//*[name()='h1' or name()='h2']//text()", part,
				String.class);
	}

	@NonNull
	@SneakyThrows
	private Document toNewHtml(final @NonNull Document singleHtml, final @NonNull List<@NonNull Node> group) {
		final @NonNull Document doc = DomUtils.transform(singleHtml, "/no-body-children.xslt");
		final @NonNull Element body = (Element) xPathFactory.newXPath().evaluateExpression("/html/body", doc,
				Node.class);
		group.forEach(node -> body.appendChild(doc.importNode(node, true)));
		return doc;
	}

	private void updateInternalLinks(Map<@NonNull String, @NonNull Document> parts) {
		final Map<String, String> linkToPartName = new LinkedHashMap<>();
		parts.forEach((partName, part) -> DomUtils.visit(part, node -> {
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				Element element = (Element) node;
				final String id = element.getAttribute("id");
				if (StringUtils.isNotBlank(id)) {
					linkToPartName.put(id, partName);
				}

				if (StringUtils.equalsIgnoreCase(element.getNodeName(), "a")) {
					final String name = element.getAttribute("name");
					if (StringUtils.isNotBlank(name)) {
						linkToPartName.put(name, partName);
					}
				}
			}
			return true;
		}));

		log.info("Collected {} internal links and IDs", linkToPartName.size());

		parts.forEach((partName, part) -> DomUtils.visit(part, node -> {
			if (node.getNodeType() == Node.ELEMENT_NODE && StringUtils.equalsIgnoreCase(node.getNodeName(), "a")) {
				final Element a = (Element) node;
				final String href = a.getAttribute("href");
				if (StringUtils.startsWith(href, "#")) {
					final String target = href.substring(1);
					final String targetPart = linkToPartName.get(target);
					if (targetPart == null) {
						log.warn("Missing internal link target: '{}'", target);
					} else {
						a.setAttribute("href", targetPart + ".html#" + target);
					}
				}
			}
			return true;
		}));
	}

}
