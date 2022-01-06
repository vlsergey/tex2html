package com.github.vlsergey.tex2html.utils;

@FunctionalInterface
public interface ThrowingBiConsumer<T, U, E extends Throwable> {

	void accept(T t, U u) throws E;

}
