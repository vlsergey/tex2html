package com.github.vlsergey.tex2html.processors;

import org.w3c.dom.Document;

import com.github.vlsergey.tex2html.Tex2HtmlOptions;

import lombok.NonNull;

public interface TexXmlProcessor {

	@NonNull
	Document process(@NonNull Tex2HtmlOptions options, @NonNull Document xmlDoc);

}
