package com.github.vlsergey.tex2html;

import java.io.File;

import lombok.Data;
import lombok.NonNull;

@Data
public class Tex2HtmlOptions {

	private boolean indent;

	private @NonNull File tempImagesFolder;

}
