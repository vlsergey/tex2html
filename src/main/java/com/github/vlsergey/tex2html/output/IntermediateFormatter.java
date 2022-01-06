package com.github.vlsergey.tex2html.output;

import java.io.File;
import java.nio.charset.StandardCharsets;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.springframework.stereotype.Component;
import org.w3c.dom.Document;

import com.github.vlsergey.tex2html.Tex2HtmlOptions;

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
		Transformer transformer = TransformerFactory.newInstance().newTransformer();
		transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, "about:legacy-compat");
		transformer.setOutputProperty(OutputKeys.ENCODING, StandardCharsets.UTF_8.name());
		transformer.setOutputProperty(OutputKeys.INDENT, options.isIndent() ? "yes" : "no");
		transformer.setOutputProperty(OutputKeys.METHOD, "html");
		transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
		transformer.setOutputProperty(OutputKeys.VERSION, "5");
		transformer.transform(new DOMSource(xml), new StreamResult(outputFile));
	}

}
