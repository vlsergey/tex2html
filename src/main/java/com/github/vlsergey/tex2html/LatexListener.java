package com.github.vlsergey.tex2html;

public interface LatexListener {

	default void onCommand(String commandName) {
		// NO OP
	}

	default void onText(String text) {
		// NO OP
	}

}
