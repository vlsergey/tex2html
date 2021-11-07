package com.github.vlsergey.tex2html;

import java.io.File;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.InputMismatchException;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.Callable;

import org.antlr.v4.runtime.ANTLRFileStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.AbstractParseTreeVisitor;
import org.antlr.v4.runtime.tree.RuleNode;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.github.vlsergey.tex2html.grammar.LatexLexer;
import com.github.vlsergey.tex2html.grammar.LatexParser;
import com.github.vlsergey.tex2html.grammar.LatexParser.CommandContext;
import com.github.vlsergey.tex2html.grammar.LatexParser.ContentContext;

import lombok.Getter;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Component
@Command(name = "tex2html", mixinStandardHelpOptions = true)
public class Tex2HtmlCommand implements Callable<Integer> {

	private static enum KnownCommandsArgumentStrategy {
		IGNORE,

		UNWRAP,

		;
	}

	private static final Map<String, KnownCommandsArgumentStrategy> KNOWN_IGNORED_COMMANDS;

	static {
		KNOWN_IGNORED_COMMANDS = new TreeMap<>();
		KNOWN_IGNORED_COMMANDS.put("\\documentclass", KnownCommandsArgumentStrategy.IGNORE);
	}

	@Option(names = "--in", description = "source TeX file", required = true)
	private File in;

	@Option(names = "--out", description = "destination directory", required = false)
	private File out;

	private interface Frame {
		void onExit(@NonNull PrintWriter out);

		@NonNull
		Frame onEnter(@NonNull PrintWriter out);

		void onText(@NonNull PrintWriter out, String text);
	}

	private static class DocumentFrame extends BeginEndCommandFrame {

		public DocumentFrame() {
			super("document");
		}

		@Override
		public @NonNull DocumentFrame onEnter(PrintWriter out) {
			out.println("<BODY>");
			return this;
		}

		@Override
		public void onExit(PrintWriter out) {
			out.println("</BODY>");
		}

		@Override
		public void onText(PrintWriter out, String text) {
			out.append(text);
		}
	}

	@Slf4j
	private static class UnknownBeginEndCommandFrame extends BeginEndCommandFrame {

		public UnknownBeginEndCommandFrame(final @NonNull String commandName) {
			super(commandName);
		}

		@Override
		public void onExit(@NonNull PrintWriter out) {
			out.println("<-- end of " + getCommandName() + " -->");
			log.warn("End of unknown command '{}' reached", getCommandName());
		}

		@Override
		public @NonNull Frame onEnter(@NonNull PrintWriter out) {
			out.println("<-- start of " + getCommandName() + " -->");
			log.warn("Start of unknown command '{}'", getCommandName());
			return this;
		}

		@Override
		public void onText(@NonNull PrintWriter out, String text) {
			out.println(StringEscapeUtils.escapeHtml4(text));
		}

	}

	private abstract static class BeginEndCommandFrame implements Frame {

		@Getter
		private final @NonNull String commandName;

		protected BeginEndCommandFrame(final @NonNull String commandName) {
			this.commandName = commandName;
		}

	}

	@Slf4j
	private static class ProjectFrame implements Frame {

		@Override
		public ProjectFrame onEnter(final @NonNull PrintWriter out) {
			out.println("<!DOCTYPE html>");
			out.println("<HTML>");
			return this;
		}

		@Override
		public void onText(final @NonNull PrintWriter out, String text) {
			if (StringUtils.isBlank(text)) {
				return;
			}

			log.warn("Text in prologue: {}", text);
		}

		@Override
		public void onExit(final @NonNull PrintWriter out) {
			out.println("</HTML>");
		}

	}

	@Override
	@SneakyThrows
	public Integer call() {
		final ANTLRFileStream inStream = new ANTLRFileStream(this.in.getPath(), StandardCharsets.UTF_8.name());
		final LatexLexer lexer = new LatexLexer(inStream);
		final LatexParser parser = new LatexParser(new CommonTokenStream(lexer));
		final ContentContext contentContext = parser.content();

		try (PrintWriter out = this.out != null ? new PrintWriter(this.out, StandardCharsets.UTF_8)
				: new PrintWriter(System.out)) {

			LinkedList<Frame> stack = new LinkedList<>();
			stack.push(new ProjectFrame().onEnter(out));

			final AbstractParseTreeVisitor<Void> visitor = new AbstractParseTreeVisitor<Void>() {

				@Override
				public Void visitChildren(RuleNode node) {
					if (node.getPayload() instanceof RuleContext) {
						final @NonNull RuleContext ruleContext = (RuleContext) node.getPayload();
						if (ruleContext.getRuleIndex() == LatexParser.RULE_command) {
							final @NonNull CommandContext commandContext = (CommandContext) ruleContext;

							final String commandName = commandContext.getStart().getText();

							switch (commandName) {
							case "\\begin": {
								final String innerCommandName = commandContext.commandArguments().curlyToken().content()
										.getText();
								if (StringUtils.equals(innerCommandName, "document")) {
									stack.push(new DocumentFrame().onEnter(out));
								} else {
									stack.push(new UnknownBeginEndCommandFrame(innerCommandName).onEnter(out));
								}
								// do not output children (i.e. arguments)
								return null;
							}
							case "\\end": {
								final String innerCommandName = commandContext.commandArguments().curlyToken().content()
										.getText();
								final Frame frameToClose = stack.peek();
								if (!(frameToClose instanceof BeginEndCommandFrame)) {
									throw new InputMismatchException("Found end of command '" + innerCommandName
											+ "', but another context is not closed yet (" + frameToClose + ")");
								}
								final BeginEndCommandFrame beginEndCommandFrameToClose = (BeginEndCommandFrame) frameToClose;
								if (!StringUtils.equals(innerCommandName,
										beginEndCommandFrameToClose.getCommandName())) {
									throw new InputMismatchException("Found end of command '" + innerCommandName
											+ "', but another command '" + beginEndCommandFrameToClose.getCommandName()
											+ "' is not closed yet");
								}
								// everything is okay, close it and forget about it
								beginEndCommandFrameToClose.onExit(out);
								// do not output children (i.e. arguments)
								return null;
							}
							}

							final KnownCommandsArgumentStrategy argStrategy = KNOWN_IGNORED_COMMANDS
									.getOrDefault(commandName, KnownCommandsArgumentStrategy.UNWRAP);

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
							out.print(StringEscapeUtils.escapeHtml4(token.getText()));
							break;
						}
						case LatexLexer.ESCAPED_DOLLAR_SIGN:
							out.print("$");
							break;
						default:
							break;
						}

					}
					return super.visitTerminal(node);
				}
			};
			visitor.visit(contentContext);

			// TODO assertions
			stack.poll().onExit(out);
		}
		return 0;
	}

}
