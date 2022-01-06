package com.github.vlsergey.tex2html;

import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.apache.commons.lang3.StringUtils;

import com.github.vlsergey.tex2html.frames.CommandInvocationFrame;
import com.github.vlsergey.tex2html.frames.Frame;
import com.github.vlsergey.tex2html.grammar.BibParser.DefinitionContext;
import com.github.vlsergey.tex2html.grammar.LatexLexer;
import com.github.vlsergey.tex2html.grammar.LatexParser.BlockFormulaContext;
import com.github.vlsergey.tex2html.grammar.LatexParser.CommandContext;
import com.github.vlsergey.tex2html.grammar.LatexParser.CommentContext;
import com.github.vlsergey.tex2html.grammar.LatexParser.InlineFormulaContext;
import com.github.vlsergey.tex2html.grammar.LatexParser.OptionalArgumentContext;
import com.github.vlsergey.tex2html.grammar.LatexParser.RequiredArgumentContext;
import com.github.vlsergey.tex2html.grammar.LatexParser.VerbatimEnvContext;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@AllArgsConstructor
@Slf4j
public abstract class Mode implements Frame {

	@Getter
	protected final @NonNull LatexVisitor latexVisitor;

	public void visitBibDefinition(final @NonNull DefinitionContext definitionContext) {
		throw new UnsupportedOperationException("This type of child shall not appear in " + this);
	}

	public Void visitBlockFormula(final @NonNull BlockFormulaContext blockFormulaContext) {
		throw new UnsupportedOperationException("This type of child shall not appear in " + this);
	}

	public Void visitCommand(final @NonNull CommandContext commandContext) {
		throw new UnsupportedOperationException("This type of child shall not appear in " + this);
	}

	public Void visitComment(final @NonNull CommentContext ruleContext) {
		final String commentText = StringUtils.trimToNull(ruleContext.getText());
		if (commentText != null) {
			latexVisitor.getOut().appendComment(commentText);
		}
		return null;
	}

	public Void visitInlineFormula(final @NonNull InlineFormulaContext inlineFormulaContext) {
		throw new UnsupportedOperationException("This type of child shall not appear in " + this);
	}

	protected void visitRequiredArgument(final CommandContext command, final int argumentIndex) {
		final RequiredArgumentContext contentToVisit = command.commandArguments()
				.getChild(RequiredArgumentContext.class, argumentIndex);
		if (contentToVisit != null) {
			latexVisitor.visit(contentToVisit);
		}
	}

	protected Void visitSubstitution(final @NonNull TerminalNode node, final @NonNull Token token) {
		latexVisitor.findFrame(CommandInvocationFrame.class).ifPresent(frame -> {

			// XXX: in future here we also need to "cut" current stack until frame
			// (included) and restore after processing

			int index = Integer.parseInt(token.getText().substring(1)) - 1;
			final ParseTree arg = frame.getInvocation().commandArguments().getChild(index);
			if (arg instanceof OptionalArgumentContext) {
				latexVisitor.visit(((OptionalArgumentContext) arg).limitedContent());
			}
			if (arg instanceof RequiredArgumentContext) {
				latexVisitor.visit(((RequiredArgumentContext) arg).curlyToken().content());
			}
		});

		return null;
	}

	public void visitTerminal(final @NonNull TerminalNode node) {
		if (node.getPayload() instanceof Token) {
			final @NonNull XmlWriter xmlWriter = latexVisitor.getOut();
			final @NonNull Token token = (Token) node.getPayload();

			switch (token.getType()) {
			case LatexLexer.LINE_BREAK:
				xmlWriter.appendTextNode("\n");
				break;
			case LatexLexer.SUBSTITUTION:
				visitSubstitution(node, token);
				break;
			default:
				xmlWriter.appendTextNode(token.getText());
				break;
			}
		}
	}

	protected void visitUserDefinedCommand(final CommandContext invocationContext, final String commandName,
			final CommandContext definition) {
		log.debug("Found invocation of previously defined command '{}'", commandName);

		CommandInvocationFrame invocationFrame = new CommandInvocationFrame(definition, invocationContext);
		latexVisitor.with(invocationFrame, () -> {
			visitRequiredArgument(definition, 1);
		});
	}

	protected void visitUserDefinedEnvironmentBegin(final CommandContext invocationContext,
			final String environmentName, final CommandContext definition) {
		log.debug("Found begin of previously defined environment '{}'", environmentName);

		CommandInvocationFrame invocationFrame = new CommandInvocationFrame(definition, invocationContext);
		latexVisitor.push(invocationFrame);

		visitRequiredArgument(definition, 1);
	}

	protected void visitUserDefinedEnvironmentEnd(final CommandContext invocationContext, final String environmentName,
			final CommandContext definition) {
		log.debug("Found end of previously defined environment '{}'", environmentName);
		visitRequiredArgument(definition, 2);
		latexVisitor.poll(CommandInvocationFrame.class::isInstance,
				"CommandInvocationFrame for user defined environment " + environmentName);
	}

	public void visitVerbatimEnvironment(VerbatimEnvContext ruleContext) {
		throw new UnsupportedOperationException("This type of child shall not appear in " + this);
	}

}
