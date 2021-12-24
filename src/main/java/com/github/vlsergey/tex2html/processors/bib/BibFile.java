package com.github.vlsergey.tex2html.processors.bib;

import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.LinkedHashMap;
import java.util.List;

import org.antlr.v4.runtime.tree.ParseTree;

import com.github.vlsergey.tex2html.LatexVisitor;
import com.github.vlsergey.tex2html.Mode;
import com.github.vlsergey.tex2html.XmlWriter;
import com.github.vlsergey.tex2html.frames.BibliographyAttributeFrame;
import com.github.vlsergey.tex2html.frames.FileFrame;
import com.github.vlsergey.tex2html.grammar.BibLexer;
import com.github.vlsergey.tex2html.grammar.BibParser;
import com.github.vlsergey.tex2html.grammar.BibParser.AttrValueContext;
import com.github.vlsergey.tex2html.grammar.BibParser.AttrValuesArrayContext;
import com.github.vlsergey.tex2html.grammar.BibParser.AttributeContext;
import com.github.vlsergey.tex2html.grammar.BibParser.ContentUnwrappedContext;
import com.github.vlsergey.tex2html.grammar.BibParser.DefinitionContext;
import com.github.vlsergey.tex2html.grammar.LatexLexer;
import com.github.vlsergey.tex2html.grammar.LatexParser;
import com.github.vlsergey.tex2html.grammar.LatexParser.ContentContext;
import com.github.vlsergey.tex2html.utils.AntlrUtils;
import com.github.vlsergey.tex2html.utils.FileUtils;

import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BibFile extends Mode implements FileFrame {

	@Getter
	private final File file;

	public BibFile(final @NonNull LatexVisitor latexVisitor, final String path) throws FileNotFoundException {
		super(latexVisitor);

		final File currentFolder = latexVisitor.getCurrentFolder().orElse(new File("."));
		this.file = FileUtils.findFile(currentFolder, path, "bib").orElseThrow(
				() -> new FileNotFoundException("Input '" + path + "' not found with base '" + currentFolder + "'"));
	}

	@Override
	public ParseTree parseFile() {
		return AntlrUtils.parse(BibLexer::new, BibParser::new, getFile(), log).definitions();
	}

	@Override
	public void visitBibDefinition(@NonNull DefinitionContext def) {
		final String type = def.defType().getText().trim();
		final String name = def.defName().getText().trim();
		final LinkedHashMap<String, List<String>> attrs = new LinkedHashMap<>();

		def.attributes().attribute().forEach((AttributeContext attr) -> {
			final String attrName = attr.attrName().getText().trim();

			final AttrValueContext attrValue = attr.attrValue();
			if (attrValue.contentPlain() != null) {
				attrs.put(attrName, singletonList(attrValue.getText()));
				return;
			}

			final AttrValuesArrayContext valuesArray = attrValue.attrValuesArray();
			attrs.put(attrName,
					valuesArray.contentUnwrapped().stream().map(ContentUnwrappedContext::getText).collect(toList()));
		});

		final @NonNull XmlWriter xmlWriter = latexVisitor.getOut();
		xmlWriter.inElement(type, () -> {
			xmlWriter.setAttribute("name", name);
			attrs.forEach((attrName, attrValues) -> attrValues.forEach(attrValue -> {
				xmlWriter.inElement("attr", () -> {
					xmlWriter.setAttribute("name", attrName);

					final @NonNull LatexParser parser = AntlrUtils.parse(LatexLexer::new, LatexParser::new, attrValue,
							log);
					final ContentContext contentContext = parser.content();
					latexVisitor.with(new BibliographyAttributeFrame(), () -> {
						latexVisitor.visit(contentContext);
					});
				});
			}));
		});
	}

}
