package com.github.vlsergey.tex2html.utils;

import java.io.File;
import java.util.Optional;

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

}
