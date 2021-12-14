package com.github.vlsergey.tex2html;

import com.github.vlsergey.tex2html.grammar.LatexParser.CommandContext;
import com.github.vlsergey.tex2html.grammar.LatexParser.ContentContext;
import com.github.vlsergey.tex2html.grammar.LatexParser.RequiredArgumentContext;

import lombok.AllArgsConstructor;
import lombok.NonNull;

@AllArgsConstructor
public class ParsedCommand {

	private final @NonNull CommandContext commandContext;

	/**
	 * @return name including starting slash '\', like '\document'
	 */
	public String getCommandName() {
		return commandContext.commandStart().getText();
	}

	public ContentContext getRequiredArgument(int index) {
		return commandContext.getChild(RequiredArgumentContext.class, index).curlyToken().content();
	}

}
