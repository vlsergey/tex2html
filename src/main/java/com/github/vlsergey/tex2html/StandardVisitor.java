package com.github.vlsergey.tex2html;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.RuleNode;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.apache.commons.lang3.StringUtils;

import com.github.vlsergey.tex2html.frames.BlockFormulaFrame;
import com.github.vlsergey.tex2html.frames.CommandArgumentFrame;
import com.github.vlsergey.tex2html.frames.CommandContentFrame;
import com.github.vlsergey.tex2html.frames.CommandFrame;
import com.github.vlsergey.tex2html.frames.CommandInvocationFrame;
import com.github.vlsergey.tex2html.frames.FileFrame;
import com.github.vlsergey.tex2html.frames.InnerFormulaFrame;
import com.github.vlsergey.tex2html.grammar.LatexLexer;
import com.github.vlsergey.tex2html.grammar.LatexParser;
import com.github.vlsergey.tex2html.grammar.LatexParser.BlockFormulaContext;
import com.github.vlsergey.tex2html.grammar.LatexParser.CommandArgumentsContext;
import com.github.vlsergey.tex2html.grammar.LatexParser.CommandContext;
import com.github.vlsergey.tex2html.grammar.LatexParser.InlineFormulaContext;
import com.github.vlsergey.tex2html.grammar.LatexParser.OptionalArgumentContext;
import com.github.vlsergey.tex2html.grammar.LatexParser.RequiredArgumentContext;
import com.github.vlsergey.tex2html.processors.bib.BibliographyResourceFactory;
import com.github.vlsergey.tex2html.utils.FileUtils;

import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class StandardVisitor extends LatexVisitor {

	private final BibliographyResourceFactory bibliographyResourceFactory = new BibliographyResourceFactory();

	public StandardVisitor(LatexContext context) {
		super(context);
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
					latexContext.withFrame(
							new CommandArgumentFrame(
									argRuleContext.getRuleIndex() == LatexParser.RULE_requiredArgument),
							() -> StandardVisitor.this.visitChildren(argRuleContext));
				}
			}
		}
	}

	@Override
	@SneakyThrows
	public Void visitChildren(RuleNode node) {
		if (node.getPayload() instanceof RuleContext) {
			final @NonNull RuleContext ruleContext = (RuleContext) node.getPayload();

			if (ruleContext.getRuleIndex() == LatexParser.RULE_blockFormula) {
				final BlockFormulaContext blockFormula = (BlockFormulaContext) ruleContext;
				this.latexContext.withFrame(new BlockFormulaFrame(), () -> {
					new FormulaVisitor(latexContext).visitChildren(blockFormula.formulaContent());
				});
				return null;
			}

			if (ruleContext.getRuleIndex() == LatexParser.RULE_command) {
				return visitCommand(ruleContext);
			}

			if (ruleContext.getRuleIndex() == LatexParser.RULE_comment) {
				return visitComment(ruleContext);
			}

			if (ruleContext.getRuleIndex() == LatexParser.RULE_inlineFormula) {
				final InlineFormulaContext inlineFormula = (InlineFormulaContext) ruleContext;
				if (inlineFormula.formulaContent() != null) {
					this.latexContext.withFrame(new InnerFormulaFrame(), () -> {
						new FormulaVisitor(latexContext).visitChildren(inlineFormula.formulaContent());
					});
				}
				return null;
			}
		}

		return super.visitChildren(node);
	}

	private Void visitCommand(final RuleContext ruleContext) throws IOException {
		final @NonNull CommandContext commandContext = (CommandContext) ruleContext;
		final String commandName = commandContext.commandStart().getText().substring(1);

		switch (commandName) {
		case "begin": {
			final String innerCommandName = commandContext.commandArguments().getChild(RequiredArgumentContext.class, 0)
					.curlyToken().content().getText();

			this.latexContext.push(new CommandFrame(innerCommandName));
			appendCommandArguments(commandContext);
			this.latexContext.push(new CommandContentFrame());
			return null;
		}
		case "addbibresource": {
			visitAddBibResourceCommand(commandContext);
			return null;
		}
		case "input": {
			FileFrame fileFrame = this.latexContext.findFrame(FileFrame.class).get();

			final String path = commandContext.commandArguments().getChild(RequiredArgumentContext.class, 0)
					.curlyToken().content().getText();

			final FileProcessor fileProcessor = new FileProcessor(fileFrame.getFile().getParentFile());
			fileProcessor.processFile(path, this);
			return null;
		}
		case "subimport*": {
			FileFrame fileFrame = this.latexContext.findFrame(FileFrame.class).get();

			final String folder = commandContext.commandArguments().getChild(RequiredArgumentContext.class, 0)
					.curlyToken().content().getText();
			final String file = commandContext.commandArguments().getChild(RequiredArgumentContext.class, 1)
					.curlyToken().content().getText();

			final FileProcessor fileProcessor = new FileProcessor(fileFrame.getFile().getParentFile());
			fileProcessor.processFile(folder + "/" + file, this);
			return null;
		}
		case "newcommand": {
			final String definedCommandName = commandContext.commandArguments()
					.getChild(RequiredArgumentContext.class, 0).curlyToken().content().getText();
			this.latexContext.getCommandDefinitions().put(definedCommandName.substring(1), commandContext);
			log.info("Found '{}' command definition", definedCommandName);
			return null;
		}
		case "end": {
			final String innerCommandName = commandContext.commandArguments().getChild(RequiredArgumentContext.class, 0)
					.curlyToken().content().getText();

			this.latexContext.poll(CommandContentFrame.class::isInstance,
					"command '" + innerCommandName + "' content frame");

			this.latexContext.poll(
					frame -> frame instanceof CommandFrame
							&& innerCommandName.equals(((CommandFrame) frame).getCommandName()),
					"command '" + innerCommandName + "' frame");

			return null;
		}
		default:
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

			latexContext.withFrame(new CommandFrame(commandName), () -> {
				appendCommandArguments(commandContext);
			});
			return null;
		}
	}

	private void visitAddBibResourceCommand(final CommandContext commandContext) throws FileNotFoundException {
		final @NonNull FileFrame fileFrame = this.latexContext.findFrame(FileFrame.class).get();

		final String path = commandContext.commandArguments().getChild(RequiredArgumentContext.class, 0).curlyToken()
				.content().getText();
		final File base = fileFrame.getFile().getParentFile();
		final File input = FileUtils.findFile(base, path, "bib").orElseThrow(
				() -> new FileNotFoundException("Input '" + path + "' not found with base '" + base + "'"));

		bibliographyResourceFactory.visitBibFile(this, input);
	}

	private Void visitComment(final RuleContext ruleContext) {
		final String commentText = StringUtils.trimToNull(ruleContext.getText());
		if (commentText != null) {
			this.latexContext.getOut().appendComment(commentText);
		}
		return null;
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
				this.latexContext.getOut().appendTextNode(token.getText());
				break;
			}

			case LatexLexer.AMPERSAND:
				this.latexContext.getOut().appendElement("ampersand");
				break;
			case LatexLexer.GTGT:
				this.latexContext.getOut().appendTextNode("»");
				break;
			case LatexLexer.LINE_BREAK:
				this.latexContext.getOut().appendTextNode("\n");
				break;
			case LatexLexer.LTLT:
				this.latexContext.getOut().appendTextNode("«");
				break;
			case LatexLexer.DOUBLE_MINUS:
				this.latexContext.getOut().appendTextNode("\u2013"); // en dash
				break;
			case LatexLexer.TRIPLE_MINUS:
				this.latexContext.getOut().appendTextNode("\u2014"); // em dash
				break;
			case LatexLexer.DOUBLE_SLASH:
				this.latexContext.getOut().appendElement("line-break"); // en dash
				break;

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
			case LatexLexer.TILDA: {
				this.latexContext.getOut().appendElement("nonbreaking-interword-space");
				break;
			}
			case LatexLexer.ESCAPED_AMPERSAND: {
				this.latexContext.getOut().appendTextNode("&");
				break;
			}
			case LatexLexer.ESCAPED_COMMA: {
				this.latexContext.getOut().appendElement("nonbreaking-fixed-size-space");
				break;
			}
			case LatexLexer.ESCAPED_APOSTROPHE: {
				this.latexContext.getOut().appendTextNode("\u0301");
				break;
			}
			case LatexLexer.ESCAPED_DOLLAR_SIGN: {
				this.latexContext.getOut().appendTextNode("$");
				break;
			}
			case LatexLexer.ESCAPED_MINUS: {
				this.latexContext.getOut().appendElement("wbr");
				break;
			}
			default:
				break;
			}
		}
		return super.visitTerminal(node);
	}

}