package com.github.vlsergey.tex2html;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

import com.github.vlsergey.tex2html.frames.Frame;
import com.github.vlsergey.tex2html.frames.ProjectFrame;
import com.github.vlsergey.tex2html.grammar.LatexParser.CommandContext;

import lombok.Getter;
import lombok.NonNull;

@Getter
public class LatexContext {

	private final Map<String, CommandContext> commandDefinitions = new LinkedHashMap<>();
	private final @NonNull XmlWriter out;
	private final @NonNull LinkedList<Frame> stack = new LinkedList<>();

	public LatexContext(final @NonNull XmlWriter xmlWriter) {
		this.out = xmlWriter;
		this.stack.push(new ProjectFrame());
	}

	@SuppressWarnings("unchecked")
	public <C extends Frame> Optional<C> findFrame(Class<C> cls) {
		return (Optional<C>) findFrame(cls::isInstance);
	}

	public Optional<Frame> findFrame(Predicate<Frame> predicate) {
		return this.stack.stream().filter(predicate).findFirst();
	}

	public void poll(final Predicate<Frame> predicate, final Object expected) {
		final Frame frame = stack.poll();
		if (!predicate.test(frame)) {
			throw new IllegalStateException("Found '" + frame + "', but another is expected (" + expected + ")");
		}
		frame.onExit(out);
	}

	public void push(final @NonNull Frame frame) {
		this.stack.push(frame.onEnter(this.out));
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
