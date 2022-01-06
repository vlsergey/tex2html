package com.github.vlsergey.tex2html;

import static com.github.vlsergey.tex2html.utils.DomUtils.writeAsXmlString;
import static org.apache.commons.io.FileUtils.forceMkdir;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.concurrent.Callable;

import javax.annotation.Nullable;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;

import com.github.vlsergey.tex2html.frames.TexFile;
import com.github.vlsergey.tex2html.output.OutputFormat;
import com.github.vlsergey.tex2html.output.OutputFormatter;
import com.github.vlsergey.tex2html.processors.TexXmlProcessor;
import com.github.vlsergey.tex2html.utils.FileUtils;

import lombok.AccessLevel;
import lombok.NonNull;
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

	@Option(names = "--format", description = "Output format (${COMPLETION-CANDIDATES}).", required = true, split = ",")
	@Setter(AccessLevel.PACKAGE)
	private @NonNull OutputFormat[] format;

	@Option(names = "--in", description = "Source TeX file.", required = true)
	@Setter(AccessLevel.PACKAGE)
	private @NonNull File in;

	@Option(names = "--indent", description = "Indent output.", required = false, defaultValue = "false")
	@Setter(AccessLevel.PACKAGE)
	private boolean indent;

	@Option(names = "--out", description = "Destination folder for output.", required = true)
	@Setter(AccessLevel.PACKAGE)
	private @NonNull File out;

	@Autowired
	private @NonNull List<OutputFormatter> outputFormatters;

	@Option(names = "--temp-images-folder", description = "Folder for temporary images. "
			+ "Can be used as rendered images cache between runs. "
			+ "Temporary folder will be used (and deleted) if not specified.", required = false)
	@Setter(AccessLevel.PACKAGE)
	private @Nullable File tempImagesFolder;

	@Autowired
	private @NonNull List<TexXmlProcessor> texXmlProcessors;

	@Override
	@SneakyThrows
	public Integer call() {
		final File tempImagesFolder = this.tempImagesFolder;
		if (tempImagesFolder != null) {
			forceMkdir(tempImagesFolder);
			this.callImpl(tempImagesFolder);
		} else {
			FileUtils.withTemporaryFolder("tex2html-", "-images", this::callImpl);
		}
		return 0;
	}

	private void callImpl(final @NonNull File temporaryImagesFolder)
			throws ParserConfigurationException, FileNotFoundException, TransformerException {
		final Tex2HtmlOptions options = new Tex2HtmlOptions(temporaryImagesFolder);
		options.setIndent(indent);

		final XmlWriter xmlWriter = new XmlWriter();
		final LatexVisitor visitor = new LatexVisitor(xmlWriter);

		visitor.visitFile(new TexFile(visitor, this.in.getPath()));

		Document doc = xmlWriter.getDoc();
		if (this.debugXml) {
			log.info("XML after parsing TEX before all processors:\n{}", writeAsXmlString(doc, this.indent));
		}

		for (TexXmlProcessor texXmlProcessor : texXmlProcessors) {
			doc = texXmlProcessor.process(options, doc);

			if (this.debugXml) {
				log.info("XML after parsing TEX after {} processor:\n{}", texXmlProcessor,
						writeAsXmlString(doc, this.indent));
			}
		}

		if (format.length == 1) {
			output(options, doc, this.format[0], this.out);
		} else {
			for (OutputFormat format : this.format) {
				output(options, doc, this.format[0], new File(this.out, format.getDefaultChildFileName()));
			}
		}
	}

	private void output(final @NonNull Tex2HtmlOptions options, final @NonNull Document doc,
			final @NonNull OutputFormat outputFormat, final @NonNull File out) {
		final OutputFormatter formatter = outputFormatters.stream().filter(f -> f.isSupported(outputFormat)).findFirst()
				.orElseThrow(() -> new AssertionError(
						"No formatters registered that supports " + outputFormat + " format. Contact tool developer."));

		formatter.process(options, doc, out);
	}

}
