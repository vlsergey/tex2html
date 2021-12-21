package com.github.vlsergey.tex2html.processors.bib;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

import lombok.Data;

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
		Comparator<SortKey> comparator = null;

		for (char c : sortingOptions.toCharArray()) {
			Function<SortKey, StringBuilder> func = char2func.get(c);
			if (func == null) {
				continue;
			}

			if (comparator == null) {
				comparator = Comparator.comparing(func);
			} else {
				comparator = comparator.thenComparing(Comparator.comparing(func));
			}
		}

		if (comparator == null) {
			return comparator(DEFAULT_SORTING);
		}

		return comparator;
	}

	private final StringBuilder alphabeticLabel = new StringBuilder();

	private final StringBuilder name = new StringBuilder();

	private final StringBuilder title = new StringBuilder();

	private final StringBuilder volume = new StringBuilder();

	private final StringBuilder year = new StringBuilder();

}
