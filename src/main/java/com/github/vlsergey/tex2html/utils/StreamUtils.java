package com.github.vlsergey.tex2html.utils;

import java.util.ArrayList;
import java.util.List;

import lombok.NonNull;
import lombok.experimental.UtilityClass;

@UtilityClass
public class StreamUtils {

	@NonNull
	public static <T, E extends Throwable> List<List<T>> group(
			final @NonNull ThrowingBiConsumer<List<T>, Runnable, E> nextGroupAndFlush) throws E {
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
