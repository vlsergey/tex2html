package com.github.vlsergey.tex2html.grammar;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.util.Arrays;

import org.junit.jupiter.api.Test;

import com.github.vlsergey.tex2html.utils.AntlrUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
class ColumnSpecParserTest {

	@Test
	void test() throws IOException {
		assertEquals(
				"(spec (columnSpec (colAlign l))   (columnSpec (columnSpecMultiplied * (number 6) (colAlign c)))   (columnSpec (colAlign r)))",
				AntlrUtils.parse(ColumnSpecLexer::new, ColumnSpecParser::new, "l *6c r", log).spec()
						.toStringTree(Arrays.asList(ColumnSpecParser.ruleNames)));

		assertEquals(
				"(spec (columnSpec (colAlign l)) (columnSpec (columnSpecMultiplied * { (number 6) } { (colAlign c) })) (columnSpec (colAlign r)))",
				AntlrUtils.parse(ColumnSpecLexer::new, ColumnSpecParser::new, "l*{6}{c}r", log).spec()
						.toStringTree(Arrays.asList(ColumnSpecParser.ruleNames)));
	}

}
