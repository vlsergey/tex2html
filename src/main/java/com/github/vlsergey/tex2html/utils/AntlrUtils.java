package com.github.vlsergey.tex2html.utils;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.function.Function;

import org.antlr.v4.runtime.ANTLRFileStream;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.TokenStream;
import org.slf4j.Logger;

import lombok.NonNull;
import lombok.experimental.UtilityClass;

@UtilityClass
public class AntlrUtils {

	public static <P extends Parser> @NonNull P parse(final @NonNull Function<CharStream, Lexer> lexerProvider,
			final @NonNull Function<TokenStream, P> parserProvider, final @NonNull CharStream src,
			final BaseErrorListener errorListener) throws IOException {
		final Lexer lexer = lexerProvider.apply(src);

		lexer.removeErrorListeners();
		lexer.addErrorListener(errorListener);

		final CommonTokenStream tokenStream = new CommonTokenStream(lexer);
		final P parser = parserProvider.apply(tokenStream);

		parser.removeErrorListeners();
		parser.addErrorListener(errorListener);

		return parser;
	}

	public static <P extends Parser> @NonNull P parse(final @NonNull Function<CharStream, Lexer> lexerProvider,
			final @NonNull Function<TokenStream, P> parserProvider, final @NonNull File src, final @NonNull Logger log)
			throws IOException {
		final BaseErrorListener errorListener = new BaseErrorListener() {
			@Override
			public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line,
					int charPositionInLine, String msg, RecognitionException e) {
				log.warn("Problem with parsing {}:{}:{}: {}", src, line, charPositionInLine, msg);
			}
		};

		final ANTLRInputStream inputStream = new ANTLRFileStream(src.getCanonicalPath());
		return parse(lexerProvider, parserProvider, inputStream, errorListener);
	}

	public static <P extends Parser> @NonNull P parse(final @NonNull Function<CharStream, Lexer> lexerProvider,
			final @NonNull Function<TokenStream, P> parserProvider, final @NonNull String src,
			final @NonNull Logger log) throws IOException {

		final BaseErrorListener errorListener = new BaseErrorListener() {
			@Override
			public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line,
					int charPositionInLine, String msg, RecognitionException e) {
				log.warn("Problem with parsing '{}' @ {}: {}", src, charPositionInLine, msg);
			}
		};

		final ANTLRInputStream inputStream = new ANTLRInputStream(new StringReader(src));
		return parse(lexerProvider, parserProvider, inputStream, errorListener);
	}

}
