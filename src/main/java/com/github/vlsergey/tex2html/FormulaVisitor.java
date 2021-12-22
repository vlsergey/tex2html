package com.github.vlsergey.tex2html;

import java.io.IOException;

import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.RuleNode;
import org.antlr.v4.runtime.tree.TerminalNode;

import com.github.vlsergey.tex2html.frames.CommandInvocationFrame;
import com.github.vlsergey.tex2html.grammar.LatexLexer;
import com.github.vlsergey.tex2html.grammar.LatexParser;
import com.github.vlsergey.tex2html.grammar.LatexParser.CommandContext;
import com.github.vlsergey.tex2html.grammar.LatexParser.OptionalArgumentContext;
import com.github.vlsergey.tex2html.grammar.LatexParser.RequiredArgumentContext;

import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
final class FormulaVisitor extends LatexVisitor {

	public FormulaVisitor(LatexContext context) {
		super(context);
	}

	@Override
	@SneakyThrows
	public Void visitChildren(final @NonNull RuleNode node) {
		if (node.getPayload() instanceof RuleContext) {
			final @NonNull RuleContext ruleContext = (RuleContext) node.getPayload();

			if (ruleContext.getRuleIndex() == LatexParser.RULE_command) {
				return visitCommand(ruleContext);
			}
		}

		return super.visitChildren(node);
	}

	private Void visitCommand(final RuleContext ruleContext) throws IOException {
		final @NonNull CommandContext commandContext = (CommandContext) ruleContext;
		final String commandName = commandContext.commandStart().getText().substring(1);

		final CommandContext userDefinition = this.latexContext.getCommandDefinitions().get(commandName);
		if (userDefinition != null) {
			log.info("Found invocation of previously defined command '{}'", commandName);

			CommandInvocationFrame invocationFrame = new CommandInvocationFrame(userDefinition, commandContext);
			latexContext.withFrame(invocationFrame, () -> {
				final RequiredArgumentContext contentToVisit = userDefinition.commandArguments()
						.getChild(RequiredArgumentContext.class, 1);
				if (contentToVisit != null) {
					visit(contentToVisit);
				}
			});
			return null;
		}

		return super.visitChildren(commandContext);
	}

	@Override
	public Void visitTerminal(TerminalNode node) {
		if (node.getPayload() instanceof Token) {
			Token token = (Token) node.getPayload();

			switch (token.getType()) {
			case LatexLexer.SUBSTITUTION: {
				this.latexContext.findFrame(CommandInvocationFrame.class).ifPresent(frame -> {

					// XXX: in future here we also need to "cut" current stack until frame
					// (included) and restore after processing

					int index = Integer.parseInt(token.getText().substring(1)) - 1;
					final ParseTree arg = frame.getInvocation().commandArguments().getChild(index);
					if (arg instanceof OptionalArgumentContext) {
						visit(((OptionalArgumentContext) arg).squareToken().content());
					}
					if (arg instanceof RequiredArgumentContext) {
						visit(((RequiredArgumentContext) arg).curlyToken().content());
					}
				});
				break;
			}
			default:
				this.latexContext.getOut().appendTextNode(token.getText());
				break;
			}
		}
		return super.visitTerminal(node);
	}

}