package com.github.vlsergey.tex2html.frames;

import java.io.PrintWriter;

import org.apache.commons.lang3.StringEscapeUtils;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class UnknownBeginEndCommandFrame extends BeginEndCommandFrame {

	public UnknownBeginEndCommandFrame(final @NonNull String commandName) {
		super(commandName);
	}

	@Override
	public void onExit(@NonNull PrintWriter out) {
		out.println("<-- end of " + getCommandName() + " -->");
		log.warn("End of unknown command '{}' reached", getCommandName());
	}

	@Override
	public @NonNull Frame onEnter(@NonNull PrintWriter out) {
		out.println("<-- start of " + getCommandName() + " -->");
		log.warn("Start of unknown command '{}'", getCommandName());
		return this;
	}

	@Override
	public void onText(@NonNull PrintWriter out, String text) {
		out.println(StringEscapeUtils.escapeHtml4(text));
	}

}