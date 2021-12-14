package com.github.vlsergey.tex2html.frames;

import java.io.File;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class FileFrame implements Frame {

	@Getter
	private final File file;

}