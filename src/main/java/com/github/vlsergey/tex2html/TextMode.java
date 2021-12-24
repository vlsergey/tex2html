package com.github.vlsergey.tex2html;

import java.io.File;
import java.io.FileNotFoundException;

import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;

import com.github.vlsergey.tex2html.frames.BlockFormulaFrame;
import com.github.vlsergey.tex2html.frames.CommandArgumentFrame;
import com.github.vlsergey.tex2html.frames.CommandContentFrame;
import com.github.vlsergey.tex2html.frames.CommandFrame;
import com.github.vlsergey.tex2html.frames.FileFrame;
import com.github.vlsergey.tex2html.frames.InnerFormulaFrame;
import com.github.vlsergey.tex2html.grammar.LatexLexer;
import com.github.vlsergey.tex2html.grammar.LatexParser;
import com.github.vlsergey.tex2html.grammar.LatexParser.BlockFormulaContext;
import com.github.vlsergey.tex2html.grammar.LatexParser.CommandArgumentsContext;
import com.github.vlsergey.tex2html.grammar.LatexParser.CommandContext;
import com.github.vlsergey.tex2html.grammar.LatexParser.InlineFormulaContext;
import com.github.vlsergey.tex2html.grammar.LatexParser.RequiredArgumentContext;
import com.github.vlsergey.tex2html.processors.bib.BibliographyResourceFactory;
import com.github.vlsergey.tex2html.utils.FileUtils;

import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TextMode extends Mode {

	private final BibliographyResourceFactory bibliographyResourceFactory = new BibliographyResourceFactory();

	public TextMode(final @NonNull LatexVisitor latexVisitor) {
		super(latexVisitor);
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
					latexVisitor.withFrame(
							new CommandArgumentFrame(
									argRuleContext.getRuleIndex() == LatexParser.RULE_requiredArgument),
							() -> latexVisitor.visitChildren(argRuleContext));
				}
			}
		}
	}

	private void visitAddBibResourceCommand(final CommandContext commandContext) throws FileNotFoundException {
		final @NonNull FileFrame fileFrame = latexVisitor.findFrame(FileFrame.class).get();

		final String path = commandContext.commandArguments().getChild(RequiredArgumentContext.class, 0).curlyToken()
				.content().getText();
		final File base = fileFrame.getFile().getParentFile();
		final File input = FileUtils.findFile(base, path, "bib").orElseThrow(
				() -> new FileNotFoundException("Input '" + path + "' not found with base '" + base + "'"));

		bibliographyResourceFactory.visitBibFile(latexVisitor, input);
	}

	@Override
	public Void visitBlockFormula(final @NonNull BlockFormulaContext blockFormulaContext) {
		latexVisitor.withFrame(new BlockFormulaFrame(), () -> {
			latexVisitor.withMode(new MathMode(latexVisitor), () -> {
				latexVisitor.visitChildren(blockFormulaContext.formulaContent());
			});
		});
		return null;
	}

	@Override
	public Void visitInlineFormula(@NonNull InlineFormulaContext inlineFormulaContext) {
		if (inlineFormulaContext.formulaContent() == null) {
			return null;
		}

		latexVisitor.withFrame(new InnerFormulaFrame(), () -> {
			latexVisitor.withMode(new MathMode(latexVisitor), () -> {
				latexVisitor.visitChildren(inlineFormulaContext.formulaContent());
			});
		});

		return null;
	}

	@Override
	@SneakyThrows
	public Void visitCommand(final @NonNull CommandContext commandContext) {
		final String commandName = commandContext.commandStart().getText().substring(1);

		switch (commandName) {
		case "begin": {
			final String innerCommandName = commandContext.commandArguments().getChild(RequiredArgumentContext.class, 0)
					.curlyToken().content().getText();

			latexVisitor.push(new CommandFrame(innerCommandName));
			appendCommandArguments(commandContext);
			latexVisitor.push(new CommandContentFrame());
			return null;
		}
		case "addbibresource": {
			visitAddBibResourceCommand(commandContext);
			return null;
		}
		case "input": {
			FileFrame fileFrame = latexVisitor.findFrame(FileFrame.class).get();

			final String path = commandContext.commandArguments().getChild(RequiredArgumentContext.class, 0)
					.curlyToken().content().getText();

			final FileProcessor fileProcessor = new FileProcessor(fileFrame.getFile().getParentFile());
			fileProcessor.processFile(latexVisitor, path);
			return null;
		}
		case "newcommand": {
			final String definedCommandName = commandContext.commandArguments()
					.getChild(RequiredArgumentContext.class, 0).curlyToken().content().getText();
			latexVisitor.getCommandDefinitions().put(definedCommandName.substring(1), commandContext);
			log.info("Found '{}' command definition", definedCommandName);
			return null;
		}
		case "subimport*": {
			FileFrame fileFrame = latexVisitor.findFrame(FileFrame.class).get();

			final String folder = commandContext.commandArguments().getChild(RequiredArgumentContext.class, 0)
					.curlyToken().content().getText();
			final String file = commandContext.commandArguments().getChild(RequiredArgumentContext.class, 1)
					.curlyToken().content().getText();

			final FileProcessor fileProcessor = new FileProcessor(fileFrame.getFile().getParentFile());
			fileProcessor.processFile(latexVisitor, folder + "/" + file);
			return null;
		}
		case "end": {
			final String innerCommandName = commandContext.commandArguments().getChild(RequiredArgumentContext.class, 0)
					.curlyToken().content().getText();

			latexVisitor.poll(CommandContentFrame.class::isInstance,
					"command '" + innerCommandName + "' content frame");

			latexVisitor.poll(
					frame -> frame instanceof CommandFrame
							&& innerCommandName.equals(((CommandFrame) frame).getCommandName()),
					"command '" + innerCommandName + "' frame");

			return null;
		}
		default:
			final CommandContext userDefinition = latexVisitor.getCommandDefinitions().get(commandName);
			if (userDefinition != null) {
				return visitUserDefinedCommand(commandContext, commandName, userDefinition);
			}

			latexVisitor.withFrame(new CommandFrame(commandName), () -> {
				appendCommandArguments(commandContext);
			});
			return null;
		}
	}

	@Override
	public void visitTerminal(TerminalNode node) {
		final @NonNull XmlWriter xmlWriter = this.latexVisitor.getOut();

		if (node.getPayload() instanceof Token) {
			Token token = (Token) node.getPayload();

			switch (token.getType()) {
			case LatexLexer.ALPHANUMERIC:
			case LatexLexer.ASTERIX:
			case LatexLexer.AT:
			case LatexLexer.DOLLAR_SIGN:
			case LatexLexer.ESCAPED_PIPE:
			case LatexLexer.ETC:
			case LatexLexer.PIPE:
			case LatexLexer.SPACES: {
				xmlWriter.appendTextNode(token.getText());
				break;
			}

			case LatexLexer.AMPERSAND:
				xmlWriter.appendElement("ampersand");
				break;
			case LatexLexer.GTGT:
				xmlWriter.appendTextNode("»");
				break;
			case LatexLexer.LINE_BREAK:
				xmlWriter.appendTextNode("\n");
				break;
			case LatexLexer.LTLT:
				xmlWriter.appendTextNode("«");
				break;
			case LatexLexer.DOUBLE_MINUS:
				xmlWriter.appendTextNode("\u2013"); // en dash
				break;
			case LatexLexer.TRIPLE_MINUS:
				xmlWriter.appendTextNode("\u2014"); // em dash
				break;
			case LatexLexer.DOUBLE_SLASH:
				xmlWriter.appendElement("line-break"); // en dash
				break;
			case LatexLexer.SUBSTITUTION:
				visitSubstitution(node, token);
				break;
			case LatexLexer.TILDA: {
				xmlWriter.appendElement("nonbreaking-interword-space");
				break;
			}
			case LatexLexer.ESCAPED_AMPERSAND: {
				xmlWriter.appendTextNode("&");
				break;
			}
			case LatexLexer.ESCAPED_COMMA: {
				xmlWriter.appendElement("nonbreaking-fixed-size-space");
				break;
			}
			case LatexLexer.ESCAPED_APOSTROPHE: {
				xmlWriter.appendTextNode("\u0301");
				break;
			}
			case LatexLexer.ESCAPED_DOLLAR_SIGN: {
				xmlWriter.appendTextNode("$");
				break;
			}
			case LatexLexer.ESCAPED_MINUS: {
				xmlWriter.appendElement("wbr");
				break;
			}
			}
		}
	}

}