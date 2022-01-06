package com.github.vlsergey.tex2html.utils;

import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Node;

import lombok.NonNull;
import lombok.experimental.UtilityClass;

@UtilityClass
public class XmlUtils {

	public static String writeAsXmlString(final @NonNull Node doc, final boolean indent) throws TransformerException {
		Transformer transformer = TransformerFactory.newInstance().newTransformer();
		transformer.setOutputProperty(OutputKeys.ENCODING, StandardCharsets.UTF_8.name());
		transformer.setOutputProperty(OutputKeys.INDENT, indent ? "yes" : "no");
		transformer.setOutputProperty(OutputKeys.METHOD, "xml");

		final StringWriter writer = new StringWriter();
		StreamResult streamResult = new StreamResult(writer);
		transformer.transform(new DOMSource(doc), streamResult);
		return writer.getBuffer().toString();
	}

}
