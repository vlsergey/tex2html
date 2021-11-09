package com.github.vlsergey.tex2html.frames;

import java.io.PrintWriter;

import org.apache.commons.lang3.StringUtils;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ProjectFrame implements Frame {

	@Override
	public ProjectFrame onEnter(final @NonNull PrintWriter out) {
		out.println("<!DOCTYPE html>");
		out.println("<HTML>");
		return this;
	}

	@Override
	public void onText(final @NonNull PrintWriter out, String text) {
		if (StringUtils.isBlank(text)) {
			return;
		}

		log.warn("Text in prologue: {}", text);
	}

	@Override
	public void onExit(final @NonNull PrintWriter out) {
		out.println("</HTML>");
	}

}