package com.github.vlsergey.tex2html.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Optional;

import javax.annotation.Nullable;

import org.apache.pdfbox.util.Hex;
import org.springframework.util.DigestUtils;

import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

@UtilityClass
public class FileUtils {

	public static Optional<File> findFile(File base, String path, String... supportedExtensions) {
		path = path.replace('/', File.separatorChar);
		File toLookup = new File(path);

		if (toLookup.isAbsolute()) {
			if (toLookup.exists()) {
				return Optional.of(toLookup);
			}

			for (String ext : supportedExtensions) {
				final File candidate = new File(path + "." + ext);
				if (candidate.exists()) {
					return Optional.of(candidate);
				}
			}

			return Optional.empty();
		}

		File relative = new File(base, path);
		if (relative.exists()) {
			return Optional.of(relative);
		}

		for (String ext : supportedExtensions) {
			final File candidate = new File(base, path + "." + ext);
			if (candidate.exists()) {
				return Optional.of(candidate);
			}
		}

		return Optional.empty();
	}

	@SneakyThrows
	public static String md5(File file) {
		try (InputStream is = Files.newInputStream(file.toPath())) {
			return Hex.getString(DigestUtils.md5Digest(is));
		}
	}

	public static <E extends Throwable> void withTemporaryFolder(final @NonNull String prefix,
			final @Nullable String suffix, ThrowingConsumer<@NonNull File, @NonNull E> consumer) throws IOException, E {
		final File file = File.createTempFile(prefix, suffix);
		if (!file.delete()) {
			throw new IOException("Unable to delete temp file " + file);
		}
		if (!file.mkdir()) {
			throw new IOException("Unable to create temp folder " + file);
		}

		try {
			consumer.accept(file);
		} finally {
			org.apache.commons.io.FileUtils.deleteDirectory(file);
		}
	}

}
