package com.github.vlsergey.tex2html.utils;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import lombok.NonNull;

public class TranslitirateRu {

	private static final Character[] abcCyrLc = { 'а', 'б', 'в', 'г', 'д', 'е', 'ё', 'ж', 'з', 'и', 'й', 'к', 'л', 'м',
			'н', 'о', 'п', 'р', 'с', 'т', 'у', 'ф', 'х', 'ц', 'ч', 'ш', 'щ', 'ъ', 'ы', 'ь', 'э', 'ю', 'я' };
	private static final String[] abcLatLc = { "a", "b", "v", "g", "d", "e", "e", "zh", "z", "i", "y", "k", "l", "m",
			"n", "o", "p", "r", "s", "t", "u", "f", "h", "ts", "ch", "sh", "sch", "", "i", "", "e", "ju", "ja" };

	private static final Map<Character, String> replacements;

	static {
		final Map<Character, String> map = new HashMap<>();
		for (int i = 0; i < abcCyrLc.length; i++) {
			map.put(abcCyrLc[i], abcLatLc[i]);
			map.put(Character.toUpperCase(abcCyrLc[i]), StringUtils.capitalize(abcLatLc[i]));
		}
		replacements = map;
	}

	@NonNull
	public static String transliterate(final @NonNull String src) {
		StringBuilder sb = new StringBuilder(src.length() * 2);
		src.chars().forEach(c -> {
			if ('A' <= c && c <= 'Z' || 'a' <= c && c <= 'z' || '0' <= c && c <= '9') {
				sb.append((char) c);
				return;
			}
			String replacement = replacements.get((char) c);
			if (replacement != null) {
				sb.append(replacement);
				return;
			}

			if (sb.length() == 0 || sb.charAt(sb.length() - 1) != '_') {
				sb.append('_');
			}
		});

		return sb.toString();
	}

}
