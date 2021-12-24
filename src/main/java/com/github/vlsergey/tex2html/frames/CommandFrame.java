package com.github.vlsergey.tex2html.frames;

import com.github.vlsergey.tex2html.XmlWriter;
import com.github.vlsergey.tex2html.grammar.LatexParser.CommandContext;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NonNull;

@AllArgsConstructor
@Data
public class CommandFrame implements Frame {

	@Getter
	private final @NonNull CommandContext commandContext;

	@Getter
	private final @NonNull String commandName;

	@Override
	public @NonNull Frame onEnter(@NonNull XmlWriter out) {
		out.beginElement("command");
		out.setAttribute("name", commandName);
		return this;
	}

	@Override
	public void onExit(@NonNull XmlWriter out) {
		out.endElement("command");
	}
}