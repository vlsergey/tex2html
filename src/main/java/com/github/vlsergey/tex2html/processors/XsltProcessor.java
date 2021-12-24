package com.github.vlsergey.tex2html.processors;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;

import com.github.vlsergey.tex2html.Tex2HtmlOptions;
import com.github.vlsergey.tex2html.XmlWriter;

import lombok.SneakyThrows;

@Component
@Order(1000)
public class XsltProcessor implements TexXmlProcessor {

	@Override
	@SneakyThrows
	public Document process(Tex2HtmlOptions command, Document xmlDoc) {
		Transformer transformer = TransformerFactory.newInstance()
				.newTransformer(new StreamSource(XmlWriter.class.getResourceAsStream("/latex-xml2html.xslt")));
		final DOMResult outputTarget = new DOMResult();
		transformer.transform(new DOMSource(xmlDoc), outputTarget);
		return (Document) outputTarget.getNode();
	}

}
