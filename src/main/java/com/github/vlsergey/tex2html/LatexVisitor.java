package com.github.vlsergey.tex2html;

import java.util.InputMismatchException;
import java.util.LinkedList;
import java.util.Optional;
import java.util.function.Predicate;

import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.AbstractParseTreeVisitor;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.RuleNode;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.apache.commons.lang3.StringUtils;

import com.github.vlsergey.tex2html.frames.CommandArgumentFrame;
import com.github.vlsergey.tex2html.frames.CommandContentFrame;
import com.github.vlsergey.tex2html.frames.CommandFrame;
import com.github.vlsergey.tex2html.frames.FileFrame;
import com.github.vlsergey.tex2html.frames.Frame;
import com.github.vlsergey.tex2html.frames.ProjectFrame;
import com.github.vlsergey.tex2html.grammar.LatexLexer;
import com.github.vlsergey.tex2html.grammar.LatexParser;
import com.github.vlsergey.tex2html.grammar.LatexParser.CommandArgumentsContext;
import com.github.vlsergey.tex2html.grammar.LatexParser.CommandContext;
import com.github.vlsergey.tex2html.grammar.LatexParser.RequiredArgumentContext;

import lombok.NonNull;
import lombok.SneakyThrows;

final class LatexVisitor extends AbstractParseTreeVisitor<Void> {

	static enum KnownCommandsArgumentStrategy {
		IGNORE,

		UNWRAP,

		;
	}

	private final @NonNull XmlWriter out;
	private final @NonNull LinkedList<Frame> stack = new LinkedList<>();

	LatexVisitor(final @NonNull XmlWriter xmlWriter) {
		this.out = xmlWriter;
		this.stack.push(new ProjectFrame());
	}

	@SuppressWarnings("unchecked")
	private <C extends Frame> Optional<C> findFrame(Class<C> cls) {
		return (Optional<C>) findFrame(cls::isInstance);
	}

	private Optional<Frame> findFrame(Predicate<Frame> predicate) {
		return this.stack.stream().filter(predicate).findFirst();
	}

	@Override
	@SneakyThrows
	public Void visitChildren(RuleNode node) {
		if (node.getPayload() instanceof RuleContext) {
			final @NonNull RuleContext ruleContext = (RuleContext) node.getPayload();

			if (ruleContext.getRuleIndex() == LatexParser.RULE_comment) {
				final String commentText = StringUtils.trimToNull(ruleContext.getText());
				if (commentText != null) {
					out.appendComment(commentText);
				}
				return null;
			}

			if (ruleContext.getRuleIndex() == LatexParser.RULE_command) {
				final @NonNull CommandContext commandContext = (CommandContext) ruleContext;
				final String commandName = commandContext.commandStart().getText().substring(1);

				switch (commandName) {
				case "begin": {
					final String innerCommandName = commandContext.commandArguments()
							.getChild(RequiredArgumentContext.class, 0).curlyToken().content().getText();

					stack.push(new CommandFrame(innerCommandName).onEnter(out));
					appendCommandArguments(commandContext);
					stack.push(new CommandContentFrame().onEnter(out));
					return null;
				}
				case "input": {
					FileFrame fileFrame = findFrame(FileFrame.class).get();

					final String path = commandContext.commandArguments().getChild(RequiredArgumentContext.class, 0)
							.curlyToken().content().getText();

					final FileProcessor fileProcessor = new FileProcessor(fileFrame.getFile().getParentFile());
					fileProcessor.processFile(path, this);
					return null;
				}
				case "end": {
					final String innerCommandName = commandContext.commandArguments()
							.getChild(RequiredArgumentContext.class, 0).curlyToken().content().getText();

					final Frame commandContentFrameToClose = stack.poll();
					if (!(commandContentFrameToClose instanceof CommandContentFrame)) {
						throw new InputMismatchException("Found end of command '" + innerCommandName
								+ "', but another context is not closed yet (" + commandContentFrameToClose + ")");
					}
					commandContentFrameToClose.onExit(out);

					final Frame frameToClose = stack.poll();
					if (!(frameToClose instanceof CommandFrame)) {
						throw new InputMismatchException("Found end of command '" + innerCommandName
								+ "', but another context is not closed yet (" + frameToClose + ")");
					}
					final CommandFrame beginEndCommandFrameToClose = (CommandFrame) frameToClose;
					if (!StringUtils.equals(innerCommandName, beginEndCommandFrameToClose.getCommandName())) {
						throw new InputMismatchException(
								"Found end of command '" + innerCommandName + "', but another command '"
										+ beginEndCommandFrameToClose.getCommandName() + "' is not closed yet");
					}
					// everything is okay, close it and forget about it
					beginEndCommandFrameToClose.onExit(out);
					// do not output children (i.e. arguments)
					return null;
				}
				default:
					withFrame(new CommandFrame(commandName), () -> {
						appendCommandArguments(commandContext);
					});
					return null;
				}

			}
		}

		return super.visitChildren(node);
	}

	private void appendCommandArguments(final CommandContext commandContext) {
		final CommandArgumentsContext parsedArgs = commandContext.commandArguments();
		if (parsedArgs == null || parsedArgs.children == null) {
			return;
		}

		for (ParseTree child : parsedArgs.children) {
			if (child instanceof RuleContext) {
				RuleContext argRuleContext = (RuleContext) child;
				if (argRuleContext.getRuleIndex() == LatexParser.RULE_optionalArgument
						|| argRuleContext.getRuleIndex() == LatexParser.RULE_requiredArgument) {
					withFrame(
							new CommandArgumentFrame(
									argRuleContext.getRuleIndex() == LatexParser.RULE_requiredArgument),
							() -> LatexVisitor.this.visitChildren(argRuleContext));
				}
			}
		}
	}

	@Override
	public Void visitTerminal(TerminalNode node) {
		if (node.getPayload() instanceof Token) {
			Token token = (Token) node.getPayload();

			switch (token.getType()) {
			case LatexLexer.ALPHANUMERIC:
			case LatexLexer.ASTERIX:
			case LatexLexer.AT:
			case LatexLexer.DOLLAR_SIGN:
			case LatexLexer.ETC:
			case LatexLexer.SPACES: {
				out.appendTextNode(token.getText());
				break;
			}
			case LatexLexer.TILDA: {
				out.appendElement("tilda");
				break;
			}
			case LatexLexer.ESCAPED_DOLLAR_SIGN: {
				out.appendTextNode("$");
				break;
			}
			default:
				break;
			}

		}
		return super.visitTerminal(node);
	}

	public void withFrame(Frame frame, Runnable runnable) {
		this.stack.push(frame);
		frame.onEnter(out);

		runnable.run();

		final Frame polled = this.stack.poll();
		if (polled != frame) {
			throw new IllegalStateException("Wrong frame state, expected " + frame + ", but actual is " + polled);
		}
		frame.onExit(out);
	}
}