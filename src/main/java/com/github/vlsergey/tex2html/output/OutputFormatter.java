package com.github.vlsergey.tex2html.output;

import java.io.File;

import org.w3c.dom.Document;

import com.github.vlsergey.tex2html.OutputFormat;
import com.github.vlsergey.tex2html.Tex2HtmlOptions;

import lombok.NonNull;

public interface OutputFormatter {

	boolean isSupported(OutputFormat format);

	void process(@NonNull Tex2HtmlOptions options, @NonNull Document xml, @NonNull File output);

}
