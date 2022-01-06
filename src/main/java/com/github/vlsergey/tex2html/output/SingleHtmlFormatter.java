package com.github.vlsergey.tex2html.output;

import static org.apache.commons.io.FileUtils.forceMkdir;

import java.io.File;
import java.net.URI;
import java.nio.file.Files;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.github.vlsergey.tex2html.Tex2HtmlOptions;
import com.github.vlsergey.tex2html.XmlWriter;
import com.github.vlsergey.tex2html.utils.DomUtils;
import com.github.vlsergey.tex2html.utils.FileUtils;

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

		DomUtils.visit(newDoc, node -> {
			if (node.getNodeType() != Node.ELEMENT_NODE || !StringUtils.equalsIgnoreCase(node.getNodeName(), "img")) {
				return true;
			}

			final @NonNull Element img = (Element) node;
			final @NonNull String srcString = img.getAttribute("src");
			if (StringUtils.isBlank(srcString)) {
				return false;
			}

			final @NonNull URI src = URI.create(srcString);
			final @NonNull File srcFile = new File(src);
			final String md5 = FileUtils.md5(srcFile);
			String extension = StringUtils.substringAfterLast(srcFile.getName(), ".");
			extension = extension == null ? "" : "." + extension;

			File dstFile = new File(imagesFolder, md5 + extension);
			img.setAttribute("src", "images/" + md5 + extension);

			if (dstFile.exists()) {
				log.debug("Do not copy {} to {} (already exists)", srcFile, dstFile);
				return false;
			}

			log.info("Copy {} to {}...", srcFile, dstFile);
			Files.copy(srcFile.toPath(), dstFile.toPath());
			return false;
		});

		File resultHtml = new File(outputFolder, "index.html");

		log.info("Writing {} to {}...", OutputFormat.SINGLE_HTML, resultHtml);
		Transformer transformer = TransformerFactory.newInstance()
				.newTransformer(new StreamSource(XmlWriter.class.getResourceAsStream("/single-html.xslt")));
		transformer.transform(new DOMSource(newDoc), new StreamResult(resultHtml));
	}

}
