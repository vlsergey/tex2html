package com.github.vlsergey.tex2html;

import java.io.FileNotFoundException;

import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;

import com.github.vlsergey.tex2html.frames.BlockFormulaFrame;
import com.github.vlsergey.tex2html.frames.CommandArgumentFrame;
import com.github.vlsergey.tex2html.frames.CommandContentFrame;
import com.github.vlsergey.tex2html.frames.CommandFrame;
import com.github.vlsergey.tex2html.frames.InnerFormulaFrame;
import com.github.vlsergey.tex2html.frames.MultlineFormulaFrame;
import com.github.vlsergey.tex2html.frames.TexFile;
import com.github.vlsergey.tex2html.grammar.LatexLexer;
import com.github.vlsergey.tex2html.grammar.LatexParser;
import com.github.vlsergey.tex2html.grammar.LatexParser.BlockFormulaContext;
import com.github.vlsergey.tex2html.grammar.LatexParser.CommandArgumentsContext;
import com.github.vlsergey.tex2html.grammar.LatexParser.CommandContext;
import com.github.vlsergey.tex2html.grammar.LatexParser.InlineFormulaContext;
import com.github.vlsergey.tex2html.grammar.LatexParser.RequiredArgumentContext;
import com.github.vlsergey.tex2html.grammar.LatexParser.VerbatimEnvContext;
import com.github.vlsergey.tex2html.processors.bib.BibFile;

import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TextMode extends Mode {

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
					latexVisitor.with(
							new CommandArgumentFrame(
									argRuleContext.getRuleIndex() == LatexParser.RULE_requiredArgument),
							() -> latexVisitor.visitChildren(argRuleContext));
				}
			}
		}
	}

	private void visitAddBibResourceCommand(final CommandContext commandContext) throws FileNotFoundException {
		final String path = commandContext.commandArguments().getChild(RequiredArgumentContext.class, 0).curlyToken()
				.content().getText();
		final BibFile bibFile = new BibFile(latexVisitor, path);
		latexVisitor.visitFile(bibFile);
	}

	private void visitBeginCommand(final CommandContext commandContext) {
		final String innerCommandName = commandContext.commandArguments().getChild(RequiredArgumentContext.class, 0)
				.curlyToken().content().getText();

		switch (innerCommandName) {
		case "multline*": {
			latexVisitor.push(new MultlineFormulaFrame(latexVisitor));
			return;
		}
		default: {
			final CommandContext envDef = latexVisitor.getEnvironmentDefinition().get(innerCommandName);
			if (envDef != null) {
				visitUserDefinedEnvironmentBegin(commandContext, innerCommandName, envDef);
				return;
			}

			latexVisitor.push(new CommandFrame(commandContext, innerCommandName));
			appendCommandArguments(commandContext);
			latexVisitor.push(new CommandContentFrame());
			return;
		}
		}
	}

	@Override
	public Void visitBlockFormula(final @NonNull BlockFormulaContext blockFormulaContext) {
		if (blockFormulaContext.formulaContent() != null) {
			latexVisitor.with(new BlockFormulaFrame(latexVisitor), () -> {
				latexVisitor.visitChildren(blockFormulaContext.formulaContent());
			});
		}
		return null;
	}

	@Override
	@SneakyThrows
	public Void visitCommand(final @NonNull CommandContext commandContext) {
		final String commandName = commandContext.commandStart().getText().substring(1);

		switch (commandName) {
		case "begin":
			visitBeginCommand(commandContext);
			return null;
		case "addbibresource":
			visitAddBibResourceCommand(commandContext);
			return null;
		case "end":
			visitEndCommand(commandContext);
			return null;
		case "input":
			visitInputCommand(commandContext);
			return null;
		case "makeatletter":
			return null;
		case "makeatother":
			return null;
		case "newcommand":
			visitNewCommandCommand(commandContext);
			return null;
		case "newenvironment":
			visitNewEnvironmentCommand(commandContext);
			return null;
		case "subimport*":
			visitSubImportCommand(commandContext);
			return null;
		default:
			final CommandContext userDefinition = latexVisitor.getCommandDefinitions().get(commandName);
			if (userDefinition != null) {
				visitUserDefinedCommand(commandContext, commandName, userDefinition);
				return null;
			}

			latexVisitor.with(new CommandFrame(commandContext, commandName), () -> {
				appendCommandArguments(commandContext);
			});
			return null;
		}
	}

	private void visitEndCommand(final CommandContext commandContext) {
		final String innerCommandName = commandContext.commandArguments().getChild(RequiredArgumentContext.class, 0)
				.curlyToken().content().getText();

		final CommandContext envDef = latexVisitor.getEnvironmentDefinition().get(innerCommandName);
		if (envDef != null) {
			visitUserDefinedEnvironmentEnd(commandContext, innerCommandName, envDef);
			return;
		}

		latexVisitor.poll(CommandContentFrame.class::isInstance, "command '" + innerCommandName + "' content frame");

		latexVisitor.poll(
				frame -> frame instanceof CommandFrame
						&& innerCommandName.equals(((CommandFrame) frame).getCommandName()),
				"command '" + innerCommandName + "' frame");
	}

	@Override
	public Void visitInlineFormula(@NonNull InlineFormulaContext inlineFormulaContext) {
		if (inlineFormulaContext.formulaContent() == null) {
			return null;
		}

		latexVisitor.with(new InnerFormulaFrame(latexVisitor), () -> {
			latexVisitor.visitChildren(inlineFormulaContext.formulaContent());
		});

		return null;
	}

	private void visitInputCommand(final CommandContext commandContext) throws FileNotFoundException {
		final String path = commandContext.commandArguments().getChild(RequiredArgumentContext.class, 0).curlyToken()
				.content().getText();

		latexVisitor.visitFile(new TexFile(latexVisitor, path));
	}

	private void visitNewCommandCommand(final CommandContext commandContext) {
		final String definedCommandName = commandContext.commandArguments().getChild(RequiredArgumentContext.class, 0)
				.curlyToken().content().getText();
		latexVisitor.getCommandDefinitions().put(definedCommandName.substring(1), commandContext);
		log.info("Found '{}' command definition", definedCommandName);
	}

	private void visitNewEnvironmentCommand(final CommandContext commandContext) {
		final String definedEnvironmentName = commandContext.commandArguments()
				.getChild(RequiredArgumentContext.class, 0).curlyToken().content().getText();
		latexVisitor.getEnvironmentDefinition().put(definedEnvironmentName, commandContext);
		log.info("Found '{}' environment definition", definedEnvironmentName);
	}

	private void visitSubImportCommand(final CommandContext commandContext) throws FileNotFoundException {
		final String folder = commandContext.commandArguments().getChild(RequiredArgumentContext.class, 0).curlyToken()
				.content().getText();
		final String file = commandContext.commandArguments().getChild(RequiredArgumentContext.class, 1).curlyToken()
				.content().getText();

		final TexFile texFile = new TexFile(latexVisitor, folder + "/" + file);
		latexVisitor.visitFile(texFile);
	}

	@Override
	public void visitTerminal(TerminalNode node) {
		final @NonNull XmlWriter xmlWriter = this.latexVisitor.getOut();

		if (node.getPayload() instanceof Token) {
			Token token = (Token) node.getPayload();

			switch (token.getType()) {
			case LatexLexer.ALPHA:
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
				if (latexVisitor.findFrame(frame -> frame instanceof CommandFrame
						&& ((CommandFrame) frame).getCommandName().equals("tabular")).isPresent()) {
					xmlWriter.appendElement("ampersand");
				} else {
					xmlWriter.appendTextNode("&");
				}

				break;
			case LatexLexer.GTGT:
				xmlWriter.appendTextNode("»");
				break;
			case LatexLexer.HASH:
				xmlWriter.appendTextNode("#");
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
			case LatexLexer.TILDA:
				xmlWriter.appendElement("nonbreaking-interword-space");
				break;
			case LatexLexer.UNDERSCORE:
				xmlWriter.appendTextNode("_");
				break;
			case LatexLexer.ESCAPED_AMPERSAND:
				xmlWriter.appendTextNode("&");
				break;
			case LatexLexer.ESCAPED_COMMA:
				xmlWriter.appendElement("nonbreaking-fixed-size-space");
				break;
			case LatexLexer.ESCAPED_APOSTROPHE:
				xmlWriter.appendTextNode("\u0301");
				break;
			case LatexLexer.ESCAPED_HASH:
				xmlWriter.appendTextNode("#");
				break;
			case LatexLexer.ESCAPED_DOLLAR_SIGN:
				xmlWriter.appendTextNode("$");
				break;
			case LatexLexer.ESCAPED_MINUS:
				xmlWriter.appendElement("wbr");
				break;
			case LatexLexer.ESCAPED_UNDERSCORE:
				xmlWriter.appendElement("_");
				break;
			}
		}
	}

	@Override
	public void visitVerbatimEnvironment(VerbatimEnvContext ruleContext) {
		final @NonNull XmlWriter xmlWrriter = latexVisitor.getOut();
		xmlWrriter.inElement("verbatim", () -> {
			xmlWrriter.appendTextNode(ruleContext.verbatimContent().getText().trim());
		});
	}

}