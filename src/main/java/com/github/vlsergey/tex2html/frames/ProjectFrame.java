package com.github.vlsergey.tex2html.frames;

import org.apache.commons.lang3.StringUtils;

import com.github.vlsergey.tex2html.html.HtmlWriter;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ProjectFrame implements Frame {

	@Override
	public void onText(final @NonNull HtmlWriter out, String text) {
		if (StringUtils.isBlank(text)) {
			return;
		}

		log.warn("Text in prologue: {}", text);
	}

}