package com.github.vlsergey.tex2html;

import java.io.File;
import java.io.IOException;

import com.github.vlsergey.tex2html.utils.ThrowingConsumer;

import lombok.experimental.UtilityClass;

@UtilityClass
public class FileTestUtils {

	public static <E extends Throwable> void withTempFile(String prefix, String suffix,
			ThrowingConsumer<File, E> consumer) throws E, IOException {
		final File in = File.createTempFile(prefix, suffix);
		try {
			consumer.accept(in);
		} finally {
			if (!in.delete()) {
				in.deleteOnExit();
			}
		}
	}

}
