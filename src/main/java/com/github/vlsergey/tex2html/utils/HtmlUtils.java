package com.github.vlsergey.tex2html.utils;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;

import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import lombok.NonNull;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@UtilityClass
@Slf4j
public class HtmlUtils {

	public static final String[] HEADERS = { "h1", "h2", "h3", "h4", "h5", "h6" };

	public static void replaceImagesWithLocalOnes(final @NonNull Document xhtml, final @NonNull File imagesFolder)
			throws IOException {
		DomUtils.visit(xhtml, node -> {
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
	}

	public static void setHtmlOutputProperties(final @NonNull Transformer transformer, final boolean indent) {
		transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, "about:legacy-compat");
		transformer.setOutputProperty(OutputKeys.ENCODING, StandardCharsets.UTF_8.name());
		transformer.setOutputProperty(OutputKeys.INDENT, indent ? "yes" : "no");
		transformer.setOutputProperty(OutputKeys.METHOD, "html");
		transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
		transformer.setOutputProperty(OutputKeys.VERSION, "5");
	}

}
