package com.github.vlsergey.tex2html.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

import lombok.experimental.UtilityClass;

@UtilityClass
public class StreamUtils {

	public static <T> List<List<T>> group(BiConsumer<List<T>, Runnable> nextGroupAndFlush) {
		final List<T> nextGroup = new ArrayList<>();
		final List<List<T>> grouped = new ArrayList<>();
		final Runnable flush = () -> {
			if (!nextGroup.isEmpty()) {
				grouped.add(new ArrayList<>(nextGroup));
			}
			nextGroup.clear();
		};

		nextGroupAndFlush.accept(nextGroup, flush);
		flush.run();
		return grouped;
	}

}
