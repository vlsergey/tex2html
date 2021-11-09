package com.github.vlsergey.tex2html.frames;

import lombok.Getter;
import lombok.NonNull;

public abstract class BeginEndCommandFrame implements Frame {

	@Getter
	private final @NonNull String commandName;

	protected BeginEndCommandFrame(final @NonNull String commandName) {
		this.commandName = commandName;
	}

}