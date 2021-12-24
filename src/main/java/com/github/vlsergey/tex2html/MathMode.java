package com.github.vlsergey.tex2html;

import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.TerminalNode;

import com.github.vlsergey.tex2html.grammar.LatexLexer;
import com.github.vlsergey.tex2html.grammar.LatexParser.CommandContext;

import lombok.NonNull;

class MathMode extends Mode {

	public MathMode(final @NonNull LatexVisitor latexVisitor) {
		super(latexVisitor);
	}

	@Override
	public Void visitCommand(@NonNull CommandContext commandContext) {
		final String commandName = commandContext.commandStart().getText().substring(1);

		final CommandContext userDefinition = latexVisitor.getCommandDefinitions().get(commandName);
		if (userDefinition != null) {
			return visitUserDefinedCommand(commandContext, commandName, userDefinition);
		}

		return null;
	}

	@Override
	public void visitTerminal(TerminalNode node) {
		if (node.getPayload() instanceof Token) {
			Token token = (Token) node.getPayload();

			switch (token.getType()) {
			case LatexLexer.SUBSTITUTION:
				visitSubstitution(node, token);
				break;
			default:
				latexVisitor.getOut().appendTextNode(token.getText());
				break;
			}
		}
	}

}