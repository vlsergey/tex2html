package com.github.vlsergey.tex2html.frames;

import java.io.PrintWriter;

import lombok.NonNull;

public interface Frame {
	void onExit(@NonNull PrintWriter out);

	@NonNull
	Frame onEnter(@NonNull PrintWriter out);

	void onText(@NonNull PrintWriter out, String text);
}