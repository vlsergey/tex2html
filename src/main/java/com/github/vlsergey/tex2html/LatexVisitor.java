package com.github.vlsergey.tex2html;

import java.util.InputMismatchException;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.function.Predicate;

import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.AbstractParseTreeVisitor;
import org.antlr.v4.runtime.tree.RuleNode;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.apache.commons.lang3.StringUtils;

import com.github.vlsergey.tex2html.frames.BeginEndCommandFrame;
import com.github.vlsergey.tex2html.frames.DocumentFrame;
import com.github.vlsergey.tex2html.frames.FileFrame;
import com.github.vlsergey.tex2html.frames.Frame;
import com.github.vlsergey.tex2html.frames.ProjectFrame;
import com.github.vlsergey.tex2html.frames.UnknownBeginEndCommandFrame;
import com.github.vlsergey.tex2html.grammar.LatexLexer;
import com.github.vlsergey.tex2html.grammar.LatexParser;
import com.github.vlsergey.tex2html.grammar.LatexParser.CommandContext;
import com.github.vlsergey.tex2html.html.HtmlWriter;

import lombok.NonNull;
import lombok.SneakyThrows;

final class LatexVisitor extends AbstractParseTreeVisitor<Void> {

	static enum KnownCommandsArgumentStrategy {
		IGNORE,

		UNWRAP,

		;
	}

	static final Map<String, KnownCommandsArgumentStrategy> KNOWN_IGNORED_COMMANDS;

	static {
		KNOWN_IGNORED_COMMANDS = new TreeMap<>();
		KNOWN_IGNORED_COMMANDS.put("\\documentclass", KnownCommandsArgumentStrategy.IGNORE);
	}

	private final @NonNull HtmlWriter out;
	private final @NonNull LinkedList<Frame> stack = new LinkedList<>();

	LatexVisitor(final @NonNull HtmlWriter htmlWriter) {
		this.out = htmlWriter;
		this.stack.push(new ProjectFrame());
	}

	public void withFrame(Frame frame, Runnable runnable) {
		this.stack.push(frame);
		try {
			runnable.run();
		} finally {
			final Frame polled = this.stack.poll();
			if (polled != frame) {
				throw new IllegalStateException("Wrong frame state, expected " + frame + ", but actual is " + polled);
			}
		}
	}

	private Optional<Frame> findFrame(Predicate<Frame> predicate) {
		return this.stack.stream().filter(predicate).findFirst();
	}

	@SuppressWarnings("unchecked")
	private <C extends Frame> Optional<C> findFrame(Class<C> cls) {
		return (Optional<C>) findFrame(cls::isInstance);
	}

	@Override
	@SneakyThrows
	public Void visitChildren(RuleNode node) {
		if (node.getPayload() instanceof RuleContext) {
			final @NonNull RuleContext ruleContext = (RuleContext) node.getPayload();

			if (ruleContext.getRuleIndex() == LatexParser.RULE_comment) {
				out.appendComment(StringUtils.trimToEmpty(ruleContext.getText()));
				return null;
			}

			if (ruleContext.getRuleIndex() == LatexParser.RULE_command) {
				final @NonNull CommandContext commandContext = (CommandContext) ruleContext;
				final @NonNull ParsedCommand parsedCommand = new ParsedCommand(commandContext);
				final String commandName = parsedCommand.getCommandName();

				switch (commandName) {
				case "\\begin": {
					final String innerCommandName = parsedCommand.getRequiredArgument(0).getText();
					if (StringUtils.equals(innerCommandName, "document")) {
						stack.push(new DocumentFrame().onEnter(out));
					} else {
						stack.push(new UnknownBeginEndCommandFrame(innerCommandName).onEnter(out));
					}
					// do not output children (i.e. arguments)
					return null;
				}
				case "\\input": {
					FileFrame fileFrame = findFrame(FileFrame.class).get();

					final String path = parsedCommand.getRequiredArgument(0).getText();
					final FileProcessor fileProcessor = new FileProcessor(fileFrame.getFile().getParentFile());
					fileProcessor.processFile(path, this);
					return null;
				}
				case "\\end": {
					final String innerCommandName = parsedCommand.getRequiredArgument(0).getText();
					final Frame frameToClose = stack.poll();
					if (!(frameToClose instanceof BeginEndCommandFrame)) {
						throw new InputMismatchException("Found end of command '" + innerCommandName
								+ "', but another context is not closed yet (" + frameToClose + ")");
					}
					final BeginEndCommandFrame beginEndCommandFrameToClose = (BeginEndCommandFrame) frameToClose;
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
				}

				final KnownCommandsArgumentStrategy argStrategy = KNOWN_IGNORED_COMMANDS.getOrDefault(commandName,
						KnownCommandsArgumentStrategy.UNWRAP);

				if (argStrategy == KnownCommandsArgumentStrategy.IGNORE) {
					return null;
				}
			}
		}

		return super.visitChildren(node);
	}

	@Override
	public Void visitTerminal(TerminalNode node) {
		if (node.getPayload() instanceof Token) {
			Token token = (Token) node.getPayload();

			switch (token.getType()) {
			case LatexLexer.ALPHANUMERIC:
			case LatexLexer.ETC:
			case LatexLexer.SPACES: {
				this.stack.peek().onText(out, token.getText());
				break;
			}
			case LatexLexer.ESCAPED_DOLLAR_SIGN:
				this.stack.peek().onText(out, "$");
				break;
			default:
				break;
			}

		}
		return super.visitTerminal(node);
	}
}