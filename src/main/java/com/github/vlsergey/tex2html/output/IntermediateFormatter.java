package com.github.vlsergey.tex2html.output;

import java.io.File;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.springframework.stereotype.Component;
import org.w3c.dom.Document;

import com.github.vlsergey.tex2html.Tex2HtmlOptions;
import com.github.vlsergey.tex2html.utils.HtmlUtils;

import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class IntermediateFormatter implements OutputFormatter {

	@Override
	public boolean isSupported(OutputFormat format) {
		return format == OutputFormat.INTERMEDIATE;
	}

	@Override
	@SneakyThrows
	public void process(@NonNull Tex2HtmlOptions options, final @NonNull Document xml, final @NonNull File outputFile) {
		log.info("Writing {} to {}...", OutputFormat.INTERMEDIATE, outputFile);
		final @NonNull Transformer transformer = TransformerFactory.newInstance().newTransformer();
		HtmlUtils.setHtmlOutputProperties(transformer, options.isIndent());
		transformer.transform(new DOMSource(xml), new StreamResult(outputFile));
	}

}
