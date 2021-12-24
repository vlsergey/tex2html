package com.github.vlsergey.tex2html;

import java.io.File;
import java.util.List;
import java.util.concurrent.Callable;

import javax.xml.transform.stream.StreamResult;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;

import com.github.vlsergey.tex2html.processors.TexXmlProcessor;
import com.github.vlsergey.tex2html.utils.XmlUtils;

import lombok.AccessLevel;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Component
@Command(name = "tex2html", mixinStandardHelpOptions = true)
@Slf4j
public class Tex2HtmlCommand implements Callable<Integer> {

	@Option(names = "--debugXml", description = "Print all intermediate XMLs after each processor.", required = false, defaultValue = "false")
	@Setter(AccessLevel.PACKAGE)
	private boolean debugXml;

	@Option(names = "--in", description = "Source TeX file.", required = true)
	@Setter(AccessLevel.PACKAGE)
	private File in;

	@Option(names = "--indent", description = "Indent output.", required = false, defaultValue = "false")
	@Setter(AccessLevel.PACKAGE)
	private boolean indent;

	@Option(names = "--out", description = "Destination HTML file. Output result to console if not specified.", required = false)
	@Setter(AccessLevel.PACKAGE)
	private File out;

	@Autowired
	private List<TexXmlProcessor> texXmlProcessors;

	@Override
	@SneakyThrows
	public Integer call() {
		final XmlWriter xmlWriter = new XmlWriter();
		final LatexVisitor visitor = new LatexVisitor(xmlWriter);

		final FileProcessor fileProcessor = new FileProcessor(new File("."));
		fileProcessor.processFile(visitor, this.in.getPath());

		Document doc = xmlWriter.getDoc();
		if (this.debugXml) {
			log.info("XML after parsing TEX before all processors:\n{}", XmlUtils.writeAsXmlString(doc, this.indent));
		}

		for (TexXmlProcessor texXmlProcessor : texXmlProcessors) {
			doc = texXmlProcessor.process(doc);

			if (this.debugXml) {
				log.info("XML after parsing TEX after {} processor:\n{}", texXmlProcessor,
						XmlUtils.writeAsXmlString(doc, this.indent));
			}
		}

		if (this.out == null) {
			XmlUtils.writeAsHtml(doc, this.indent, new StreamResult(System.out));
			return 0;
		}

		XmlUtils.writeAsHtml(doc, this.indent, new StreamResult(this.out));
		return 0;
	}

}
