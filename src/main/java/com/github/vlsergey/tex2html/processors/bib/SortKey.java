package com.github.vlsergey.tex2html.processors.bib;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

import lombok.Data;
import lombok.NonNull;

@Data
public class SortKey {

	private static final Map<Character, Function<SortKey, StringBuilder>> char2func = new LinkedHashMap<>();

	/**
	 * https://ru.overleaf.com/learn/latex/Articles/Getting_started_with_BibLaTeX
	 */
	public static final String DEFAULT_SORTING = "nty";

	static {
		char2func.put('a', SortKey::getAlphabeticLabel);
		char2func.put('n', SortKey::getName);
		char2func.put('t', SortKey::getTitle);
		char2func.put('v', SortKey::getVolume);
		char2func.put('y', SortKey::getYear);
	}

	public static Comparator<SortKey> comparator(String sortingOptions) {
		return Comparator.comparing((SortKey s) -> s.toString(sortingOptions).toLowerCase());
	}

	private final StringBuilder alphabeticLabel = new StringBuilder();

	private final StringBuilder name = new StringBuilder();

	private final StringBuilder title = new StringBuilder();

	private final StringBuilder volume = new StringBuilder();

	private final StringBuilder year = new StringBuilder();

	public String toString(final @NonNull String sortingOptions) {
		if (char2func.keySet().stream().allMatch(c -> sortingOptions.indexOf(c) == -1)) {
			return toString(DEFAULT_SORTING);
		}

		StringBuilder builder = new StringBuilder();
		for (char c : sortingOptions.toCharArray()) {
			Function<SortKey, StringBuilder> func = char2func.get(c);
			if (func == null) {
				continue;
			}
			builder.append(func.apply(this));
		}
		return builder.toString();
	}

}
