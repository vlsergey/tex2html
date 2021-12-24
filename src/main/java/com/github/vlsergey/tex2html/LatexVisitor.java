package com.github.vlsergey.tex2html;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.tree.AbstractParseTreeVisitor;
import org.antlr.v4.runtime.tree.RuleNode;
import org.antlr.v4.runtime.tree.TerminalNode;

import com.github.vlsergey.tex2html.frames.Frame;
import com.github.vlsergey.tex2html.frames.ProjectFrame;
import com.github.vlsergey.tex2html.grammar.LatexParser;
import com.github.vlsergey.tex2html.grammar.LatexParser.BlockFormulaContext;
import com.github.vlsergey.tex2html.grammar.LatexParser.CommandContext;
import com.github.vlsergey.tex2html.grammar.LatexParser.CommentContext;
import com.github.vlsergey.tex2html.grammar.LatexParser.InlineFormulaContext;

import lombok.Getter;
import lombok.NonNull;
import lombok.SneakyThrows;

@Getter
public class LatexVisitor extends AbstractParseTreeVisitor<Void> {

	private final Map<String, CommandContext> commandDefinitions = new LinkedHashMap<>();
	private final @NonNull LinkedList<Frame> framesStack = new LinkedList<>();
	private final @NonNull LinkedList<Mode> modesStack = new LinkedList<>();
	private final @NonNull XmlWriter out;

	public LatexVisitor(final @NonNull XmlWriter xmlWriter) {
		this.out = xmlWriter;
		this.framesStack.push(new ProjectFrame());
		this.modesStack.push(new TextMode(this));
	}

	@SuppressWarnings("unchecked")
	public <C extends Frame> Optional<C> findFrame(Class<C> cls) {
		return (Optional<C>) findFrame(cls::isInstance);
	}

	public Optional<Frame> findFrame(Predicate<Frame> predicate) {
		return this.framesStack.stream().filter(predicate).findFirst();
	}

	public @NonNull Mode getMode() {
		return modesStack.peek();
	}

	public void poll(final Predicate<Frame> predicate, final Object expected) {
		final Frame frame = framesStack.poll();
		if (!predicate.test(frame)) {
			throw new IllegalStateException("Found '" + frame + "', but another is expected (" + expected + ")");
		}
		frame.onExit(out);
	}

	public void push(final @NonNull Frame frame) {
		this.framesStack.push(frame.onEnter(this.out));
	}

	@Override
	@SneakyThrows
	public Void visitChildren(RuleNode node) {
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

		return super.visitChildren(node);
	}

	@Override
	public Void visitTerminal(TerminalNode node) {
		getMode().visitTerminal(node);
		return null;
	}

	public void withFrame(Frame frame, Runnable runnable) {
		this.framesStack.push(frame);
		frame.onEnter(out);

		runnable.run();

		final Frame polled = this.framesStack.poll();
		if (polled != frame) {
			throw new IllegalStateException("Wrong frame state, expected " + frame + ", but actual is " + polled);
		}
		frame.onExit(out);
	}

	public void withMode(Mode mode, Runnable runnable) {
		this.modesStack.push(mode);

		runnable.run();

		final Mode polled = this.modesStack.poll();
		if (polled != mode) {
			throw new IllegalStateException("Wrong mode state, expected " + mode + ", but actual is " + polled);
		}
	}

}