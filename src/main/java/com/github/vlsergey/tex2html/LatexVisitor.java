package com.github.vlsergey.tex2html;

import org.antlr.v4.runtime.tree.AbstractParseTreeVisitor;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

@AllArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class LatexVisitor extends AbstractParseTreeVisitor<Void> {

	@Getter
	protected final @NonNull LatexContext latexContext;

}