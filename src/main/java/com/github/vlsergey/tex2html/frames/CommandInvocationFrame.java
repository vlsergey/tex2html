package com.github.vlsergey.tex2html.frames;

import com.github.vlsergey.tex2html.grammar.LatexParser.CommandContext;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

@AllArgsConstructor
@Getter
public class CommandInvocationFrame implements Frame {

	private final @NonNull CommandContext definition;

	private final @NonNull CommandContext invocation;

}
