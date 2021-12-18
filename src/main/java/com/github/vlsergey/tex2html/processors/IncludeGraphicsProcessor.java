package com.github.vlsergey.tex2html.processors;

import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.toMap;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Map;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

import com.github.vlsergey.tex2html.grammar.AttributesLexer;
import com.github.vlsergey.tex2html.grammar.AttributesParser;
import com.github.vlsergey.tex2html.grammar.AttributesParser.AttributeContext;
import com.github.vlsergey.tex2html.grammar.AttributesParser.AttributesContext;
import com.github.vlsergey.tex2html.grammar.AttributesParser.TextWidthRelativeContext;
import com.github.vlsergey.tex2html.utils.AntlrUtils;
import com.github.vlsergey.tex2html.utils.DomUtils;
import com.github.vlsergey.tex2html.utils.FileUtils;
import com.github.vlsergey.tex2html.utils.TexXmlUtils;

import lombok.extern.slf4j.Slf4j;

@Component
@Order(0)
@Slf4j
public class IncludeGraphicsProcessor implements TexXmlProcessor {

	private static final String[] SUPPORTED_IMAGE_EXTENSIONS = { "png", "jpg", "jpeg", "pdf", "svg" };

	private static String attributesToString(Node attributesAttribute) {
		StringBuilder builder = new StringBuilder();
		DomUtils.childrenStream(attributesAttribute).forEach(child -> {
			if (child instanceof Text) {
				builder.append(child.getNodeValue());
			} else if (TexXmlUtils.isCommandElement(child, "textwidth")) {
				builder.append("\\textwidth");
			}
		});
		return builder.toString();
	}

	private static String attrValueToHtml(ParserRuleContext attrValue) {
		if (attrValue.children.size() > 1) {
			log.warn("Unsupportted attribute value format: {}", attrValue.getText());
			return attrValue.getText();
		}

		final ParseTree child = attrValue.getChild(0);

		if (child instanceof TextWidthRelativeContext) {
			BigDecimal number = new BigDecimal(((TextWidthRelativeContext) child).number().getText());
			number = number.scaleByPowerOfTen(2);
			return number.toPlainString() + "%";
		}

		log.warn("Unsupportted attribute value format: {}", attrValue.getText());
		return attrValue.getText();
	}

	private static Map<String, String> parseAttributes(String attrsString) throws IOException {
		final AttributesContext attrsContext = AntlrUtils
				.parse(AttributesLexer::new, AttributesParser::new, attrsString, log).attributes();

		return attrsContext.children.stream().filter(AttributeContext.class::isInstance)
				.map(attr -> (AttributeContext) attr).filter(attr -> attr.name() != null && attr.value() != null)
				.collect(toMap(attr -> attr.name().getText(), attr -> attrValueToHtml(attr.value())));
	}

	@Override
	public Document process(Document xmlDoc) {
		return TexXmlUtils.visitCommandNodes(xmlDoc, "includegraphics", this::processImpl);
	}

	private void processImpl(Node node) {
		final String filePath = TexXmlUtils.findRequiredArgument(node, 1);
		final Node imageAttributesNode = TexXmlUtils.findOptionalArgumentNode(node, 1);

		if (filePath != null) {
			try {
				final File basePath = TexXmlUtils.findFileBasePath(node);

				final File input = FileUtils.findFile(basePath, filePath, SUPPORTED_IMAGE_EXTENSIONS)
						.orElseThrow(() -> new FileNotFoundException(
								"Image " + filePath + "' not found with base '" + basePath.getPath()
										+ "' and one of possible extensions: " + SUPPORTED_IMAGE_EXTENSIONS));

				final Element img = node.getOwnerDocument().createElement("include-graphics");
				img.setAttribute("src", input.toURI().toASCIIString());

				final Map<String, String> imageAttributes = imageAttributesNode == null ? emptyMap()
						: parseAttributes(attributesToString(imageAttributesNode));
				imageAttributes.forEach(img::setAttribute);

				node.getParentNode().replaceChild(img, node);

			} catch (Exception exc) {
				log.error("Unable to process includegraphics with path " + filePath + ": " + exc.getMessage(), exc);
			}
		}

	}

}