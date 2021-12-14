package com.github.vlsergey.tex2html.frames;

import com.github.vlsergey.tex2html.html.HtmlWriter;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class UnknownBeginEndCommandFrame extends BeginEndCommandFrame {

	public UnknownBeginEndCommandFrame(final @NonNull String commandName) {
		super(commandName);
	}

	@Override
	public void onExit(final @NonNull HtmlWriter out) {
		out.appendComment("end of " + getCommandName());
		log.warn("End of unknown command '{}' reached", getCommandName());
	}

	@Override
	public @NonNull Frame onEnter(final @NonNull HtmlWriter out) {
		out.appendComment("start of " + getCommandName());
		log.warn("Start of unknown command '{}'", getCommandName());
		return this;
	}

}