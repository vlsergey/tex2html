package com.github.vlsergey.tex2html.utils;

@FunctionalInterface
public interface ThrowingRunnable<E extends Throwable> {

	void run() throws E;

}
