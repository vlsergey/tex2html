package com.github.vlsergey.tex2html.processors.bib;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.github.vlsergey.tex2html.grammar.BibLexer;
import com.github.vlsergey.tex2html.grammar.BibParser;
import com.github.vlsergey.tex2html.grammar.BibParser.AttrValueContext;
import com.github.vlsergey.tex2html.grammar.BibParser.AttrValuesArrayContext;
import com.github.vlsergey.tex2html.grammar.BibParser.AttributeContext;
import com.github.vlsergey.tex2html.grammar.BibParser.ContentUnwrappedContext;
import com.github.vlsergey.tex2html.grammar.BibParser.DefinitionContext;
import com.github.vlsergey.tex2html.processors.TexXmlProcessor;
import com.github.vlsergey.tex2html.utils.AntlrUtils;
import com.github.vlsergey.tex2html.utils.FileUtils;
import com.github.vlsergey.tex2html.utils.TexXmlUtils;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Component
@Order(0)
@Slf4j
public class BibliographyProcessor implements TexXmlProcessor {

	private static final String[] SUPPORTED_EXTENSIONS = new String[] { ".bib" };

	@Override
	public Document process(Document xmlDoc) {
		Map<String, SourceDef> bibliography = new LinkedHashMap<>();

		TexXmlUtils.visitCommandNodes(xmlDoc, "addbibresource", command -> {
			final String filePath = TexXmlUtils.findRequiredArgument(command, 1);
			final File basePath = TexXmlUtils.findFileBasePath(command);

			final File input = FileUtils.findFile(basePath, filePath, SUPPORTED_EXTENSIONS).orElseThrow(
					() -> new FileNotFoundException("Bibliography source " + filePath + "' not found with base '"
							+ basePath.getPath() + "' and one of possible extensions: " + SUPPORTED_EXTENSIONS));

			final @NonNull BibParser bibParser = AntlrUtils.parse(BibLexer::new, BibParser::new, input, log);
			final List<DefinitionContext> defs = bibParser.definitions().definition();
			log.info("Parsed {} bib definitions from {}", defs.size(), input);

			defs.forEach(def -> {
				final String type = def.defType().getText().trim();
				final String name = def.defName().getText().trim();
				final LinkedHashMap<String, String[]> attrs = new LinkedHashMap<>();

				def.attributes().attribute().forEach((AttributeContext attr) -> {
					final String attrName = attr.attrName().getText().trim();

					final AttrValueContext attrValue = attr.attrValue();
					if (attrValue.contentPlain() != null) {
						attrs.put(attrName, new String[] { attrValue.getText() });
						return;
					}

					final AttrValuesArrayContext valuesArray = attrValue.attrValuesArray();
					attrs.put(attrName, valuesArray.contentUnwrapped().stream().map(ContentUnwrappedContext::getText)
							.toArray(String[]::new));
				});

				bibliography.put(name, new SourceDef(type, name, attrs));
			});

		});

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
			SourceDef def = bibliography.get(citeName);
			if (def == null) {
				log.warn("At least 1 reference to unknown bib source '{}'", citeName);
				return;
			}

			renderedSourcesList.add(gostRenderer.render(xmlDoc, def));
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

	@Autowired
	private GostRenderer gostRenderer;

}
