package com.github.vlsergey.tex2html.output;

import static org.apache.commons.io.FileUtils.forceMkdir;

import java.io.File;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.springframework.stereotype.Component;
import org.w3c.dom.Document;

import com.github.vlsergey.tex2html.Tex2HtmlOptions;
import com.github.vlsergey.tex2html.XmlWriter;
import com.github.vlsergey.tex2html.utils.DomUtils;
import com.github.vlsergey.tex2html.utils.HtmlUtils;

import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class SingleHtmlFormatter implements OutputFormatter {

	@Override
	public boolean isSupported(OutputFormat format) {
		return format == OutputFormat.SINGLE_HTML;
	}

	@Override
	@SneakyThrows
	public void process(@NonNull Tex2HtmlOptions options, final @NonNull Document xml,
			final @NonNull File outputFolder) {
		forceMkdir(outputFolder);

		Document newDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
		newDoc.appendChild(newDoc.importNode(xml.getDocumentElement(), true));

		File imagesFolder = new File(outputFolder, "images");
		forceMkdir(imagesFolder);

		HtmlUtils.replaceImagesWithLocalOnes(newDoc, imagesFolder);

		newDoc = DomUtils.transform(newDoc, "/generate-toc.xslt");
		newDoc = DomUtils.transform(newDoc, "/mathjax.xslt");
		newDoc = DomUtils.transform(newDoc, "/bootstrap.xslt");

		File resultHtml = new File(outputFolder, "index.html");

		log.info("Writing {} to {}...", OutputFormat.SINGLE_HTML, resultHtml);
		final @NonNull Transformer transformer = TransformerFactory.newInstance()
				.newTransformer(new StreamSource(XmlWriter.class.getResourceAsStream("/single-html.xslt")));
		HtmlUtils.setHtmlOutputProperties(transformer, options.isIndent());
		transformer.transform(new DOMSource(newDoc), new StreamResult(resultHtml));
	}

}
