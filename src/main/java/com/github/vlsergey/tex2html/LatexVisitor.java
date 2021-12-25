package com.github.vlsergey.tex2html;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.tree.AbstractParseTreeVisitor;
import org.antlr.v4.runtime.tree.RuleNode;
import org.antlr.v4.runtime.tree.TerminalNode;

import com.github.vlsergey.tex2html.frames.FileFrame;
import com.github.vlsergey.tex2html.frames.Frame;
import com.github.vlsergey.tex2html.frames.ProjectFrame;
import com.github.vlsergey.tex2html.grammar.BibParser.DefinitionContext;
import com.github.vlsergey.tex2html.grammar.LatexParser;
import com.github.vlsergey.tex2html.grammar.LatexParser.BlockFormulaContext;
import com.github.vlsergey.tex2html.grammar.LatexParser.CommandContext;
import com.github.vlsergey.tex2html.grammar.LatexParser.CommentContext;
import com.github.vlsergey.tex2html.grammar.LatexParser.InlineFormulaContext;

import lombok.Getter;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Getter
@Slf4j
public class LatexVisitor extends AbstractParseTreeVisitor<Void> {

	private final Map<String, CommandContext> commandDefinitions = new LinkedHashMap<>();
	private final Map<String, CommandContext> environmentDefinition = new LinkedHashMap<>();
	private final @NonNull XmlWriter out;
	private final @NonNull LinkedList<Frame> stack = new LinkedList<>();

	public LatexVisitor(final @NonNull XmlWriter xmlWriter) {
		this.out = xmlWriter;
		this.stack.push(new ProjectFrame(this));
	}

	@SuppressWarnings("unchecked")
	public <C extends Frame> Optional<C> findFrame(Class<C> cls) {
		return (Optional<C>) findFrame(cls::isInstance);
	}

	public Optional<Frame> findFrame(Predicate<Frame> predicate) {
		return this.stack.stream().filter(predicate).findFirst();
	}

	public Optional<File> getCurrentFolder() {
		return findFrame(FileFrame.class).map(FileFrame::getFile).map(File::getParentFile);
	}

	public @NonNull Mode getMode() {
		return findFrame(Mode.class).get();
	}

	public void poll(final Predicate<Frame> predicate, final Object expected) {
		final Frame frame = stack.poll();
		log.debug("Polled from stack: {}", frame);
		if (!predicate.test(frame)) {
			throw new IllegalStateException("Found '" + frame + "', but another is expected (" + expected + ")");
		}
		frame.onExit(out);
	}

	public void push(final @NonNull Frame frame) {
		log.debug("Push to stack: {}", frame);
		this.stack.push(frame.onEnter(this.out));
	}

	@Override
	@SneakyThrows
	public Void visitChildren(RuleNode node) {
		if (node.getPayload() instanceof DefinitionContext) {
			getMode().visitBibDefinition((DefinitionContext) node.getPayload());
			return null;
		}

		if (node.getPayload() instanceof RuleContext) {
			final @NonNull RuleContext ruleContext = (RuleContext) node.getPayload();

			if (ruleContext.getRuleIndex() == LatexParser.RULE_blockFormula) {
				return getMode().visitBlockFormula((BlockFormulaContext) ruleContext);
			}

			if (ruleContext.getRuleIndex() == LatexParser.RULE_command) {
				return getMode().visitCommand((CommandContext) ruleContext);
			}

			if (ruleContext.getRuleIndex() == LatexParser.RULE_comment) {
				return getMode().visitComment((CommentContext) ruleContext);
			}

			if (ruleContext.getRuleIndex() == LatexParser.RULE_inlineFormula) {
				return getMode().visitInlineFormula((InlineFormulaContext) ruleContext);
			}
		}

		return visitChildrenSuper(node);
	}

	public Void visitChildrenSuper(RuleNode node) {
		return super.visitChildren(node);
	}

	public void visitFile(FileFrame fileFrame) {
		with(fileFrame, () -> visit(fileFrame.parseFile()));
	}

	@Override
	public Void visitTerminal(TerminalNode node) {
		getMode().visitTerminal(node);
		return null;
	}

	public void with(Frame frame, Runnable runnable) {
		log.debug("Push to stack: {}", frame);

		this.stack.push(frame);
		frame.onEnter(out);

		runnable.run();

		final Frame polled = this.stack.poll();
		log.debug("Poll from stack: {}", polled);

		if (polled != frame) {
			throw new IllegalStateException("Wrong frame state, expected " + frame + ", but actual is " + polled);
		}
		frame.onExit(out);
	}

}