package com.github.vlsergey.tex2html;

import com.github.vlsergey.tex2html.frames.MultlineFormulaFrame;
import com.github.vlsergey.tex2html.grammar.LatexParser.CommandContext;
import com.github.vlsergey.tex2html.grammar.LatexParser.RequiredArgumentContext;

import lombok.NonNull;

public abstract class MathMode extends Mode {

	protected MathMode(final @NonNull LatexVisitor latexVisitor) {
		super(latexVisitor);
	}

	@Override
	public Void visitCommand(@NonNull CommandContext commandContext) {
		final String commandName = commandContext.commandStart().getText().substring(1);

		final CommandContext userDefinition = latexVisitor.getCommandDefinitions().get(commandName);
		if (userDefinition != null) {
			return visitUserDefinedCommand(commandContext, commandName, userDefinition);
		}

		if (commandName.equals("end")) {
			final String innerCommandName = commandContext.commandArguments().getChild(RequiredArgumentContext.class, 0)
					.curlyToken().content().getText();

			switch (innerCommandName) {
			case ("multline*"): {
				latexVisitor.poll(MultlineFormulaFrame.class::isInstance, "multiline*");
				return null;
			}
			}
		}

		return latexVisitor.visitChildrenSuper(commandContext);
	}

}