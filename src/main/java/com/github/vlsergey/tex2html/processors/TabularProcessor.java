package com.github.vlsergey.tex2html.processors;

import static java.util.Collections.unmodifiableMap;
import static java.util.stream.Collectors.toList;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.antlr.v4.runtime.tree.ParseTree;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.github.vlsergey.tex2html.grammar.ColumnSpecLexer;
import com.github.vlsergey.tex2html.grammar.ColumnSpecParser;
import com.github.vlsergey.tex2html.grammar.ColumnSpecParser.BorderSpecContext;
import com.github.vlsergey.tex2html.grammar.ColumnSpecParser.ColumnSpecContext;
import com.github.vlsergey.tex2html.grammar.ColumnSpecParser.SpecContext;
import com.github.vlsergey.tex2html.utils.AntlrUtils;
import com.github.vlsergey.tex2html.utils.DomUtils;
import com.github.vlsergey.tex2html.utils.StreamUtils;
import com.github.vlsergey.tex2html.utils.TexXmlUtils;

import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Component
@Order(0)
@Slf4j
public class TabularProcessor implements TexXmlProcessor {

	private static final Map<String, String> COLUMN_SPEC_TO_TEXT_ALIGN;

	static {
		final Map<String, String> map = new LinkedHashMap<>(3);
		map.put("l", "left");
		map.put("c", "center");
		map.put("r", "right");
		COLUMN_SPEC_TO_TEXT_ALIGN = unmodifiableMap(map);
	}

	private static boolean isHLine(Node node) {
		return TexXmlUtils.isCommandElement(node, "hline");
	}

	private static boolean isLineBreak(Node node) {
		return node instanceof Element && "line-break".equals(node.getNodeName());
	}

	private final XPathFactory xPathFactory = XPathFactory.newInstance();

	private List<Map<String, String>> parseCellProps(Element command) throws IOException, XPathExpressionException {
		final String columnsSpecStr = (String) xPathFactory.newXPath().evaluate("./argument[@required='true'][2]",
				command, XPathConstants.STRING);
		final SpecContext columnsSpec = AntlrUtils
				.parse(ColumnSpecLexer::new, ColumnSpecParser::new, columnsSpecStr, log).spec();

		final List<ParseTree> withoutSpaces = columnsSpec.children.stream()
				.filter(c -> c instanceof BorderSpecContext || c instanceof ColumnSpecContext).collect(toList());

		final List<Map<String, String>> result = new ArrayList<>();
		for (int i = 0; i < withoutSpaces.size(); i++) {
			final ParseTree child = withoutSpaces.get(i);
			if (child instanceof BorderSpecContext) {
				continue;
			}

			final ColumnSpecContext columnSpec = (ColumnSpecContext) child;
			Map<String, String> cellProps = parseCellProps(withoutSpaces, i, columnSpec);

			final int repeat = columnSpec.colSpecMultiplier() != null
					? Integer.parseInt(columnSpec.colSpecMultiplier().number().getText())
					: 1;

			for (int k = 0; k < repeat; k++) {
				result.add(cellProps);
			}
		}
		return result;
	}

	private Map<String, String> parseCellProps(List<ParseTree> withoutSpaces, int i, ColumnSpecContext child) {
		final Function<ParseTree, String> borderWidthF = pt -> {
			if (!(pt instanceof BorderSpecContext))
				return null;

			final BorderSpecContext borderSpec = (BorderSpecContext) pt;
			return borderSpec.DOUBLE_BORDER() != null ? "double" //
					: borderSpec.SINGLE_BORDER() != null ? "solid thin" //
							: null;
		};

		final String borderLeft = i == 0 ? null : borderWidthF.apply(withoutSpaces.get(i - 1));
		final String borderRight = i == withoutSpaces.size() - 1 ? null : borderWidthF.apply(withoutSpaces.get(i + 1));
		final String textAlign = COLUMN_SPEC_TO_TEXT_ALIGN.get(child.getText());

		final Map<String, String> cellProps = new LinkedHashMap<>();
		if (borderLeft != null)
			cellProps.put("border-left", borderLeft);
		if (borderRight != null)
			cellProps.put("border-right", borderRight);
		if (textAlign != null)
			cellProps.put("text-align", textAlign);
		return cellProps;
	}

	private boolean isAmpersand(Node node) {
		return node instanceof Element && "ampersand".equals(node.getNodeName());
	}

	@Override
	public Document process(Document xmlDoc) {
		return TexXmlUtils.visitCommandNodes(xmlDoc, "tabular", this::processImpl);
	}

	@SneakyThrows
	private void processImpl(Element command) {

		Element contentElement = (Element) xPathFactory.newXPath().evaluate("./content", command, XPathConstants.NODE);
		if (contentElement == null) {
			return;
		}

		DomUtils.concatenateTextNodes(command);

		List<List<Node>> rowsAndBorders = StreamUtils.group((nextGroup, flush) -> {
			DomUtils.stream(contentElement.getChildNodes()).forEach(child -> {

				if (isHLine(child)) {
					if (!nextGroup.isEmpty() && !isHLine(nextGroup.get(0))) {
						flush.run();
					}
					nextGroup.add(child);
					return;
				}

				if (isLineBreak(child)) {
					if (!nextGroup.isEmpty()) {
						flush.run();
					}
					return;
				}

				if (!nextGroup.isEmpty() && isHLine(nextGroup.get(0))) {
					flush.run();
				}

				nextGroup.add(child);
			});
		});

		rowsAndBorders = rowsAndBorders.stream().map(DomUtils::trim).filter(row -> !row.isEmpty()).collect(toList());
		for (int i = rowsAndBorders.size() - 1; i > 0; i--) {
			List<Node> first = rowsAndBorders.get(i - 1);
			List<Node> second = rowsAndBorders.get(i);

			if (isHLine(first.get(0)) && isHLine(second.get(0))) {
				first.addAll(second);
				rowsAndBorders.remove(i);
			}
		}

		final Document doc = command.getOwnerDocument();
		final Element tabular = doc.createElement("tabular");
		command.getParentNode().replaceChild(tabular, command);

		final Element columns = doc.createElement("columns");
		tabular.appendChild(columns);
		parseCellProps(command).forEach(cellProps -> {
			final Element column = doc.createElement("column");
			cellProps.forEach(column::setAttribute);
			columns.appendChild(column);
		});

		for (int i = 0; i < rowsAndBorders.size(); i++) {
			List<Node> rowNodes = rowsAndBorders.get(i);
			if (isHLine(rowNodes.get(0))) {
				continue;
			}

			final String borderTop = i == 0 ? null
					: isHLine(rowsAndBorders.get(i - 1).get(0))
							? rowsAndBorders.get(i - 1).size() == 1 ? "solid thin" : "double"
							: null;
			final String borderBottom = i == rowsAndBorders.size() - 1 ? null
					: isHLine(rowsAndBorders.get(i + 1).get(0))
							? rowsAndBorders.get(i + 1).size() == 1 ? "solid thin" : "double"
							: null;

			final Element row = doc.createElement("row");
			if (borderTop != null)
				row.setAttribute("border-top", borderTop);
			if (borderBottom != null)
				row.setAttribute("border-bottom", borderBottom);
			tabular.appendChild(row);

			processRow(rowNodes, row);
		}

	}

	private void processRow(final @NonNull List<Node> rowNodes, final @NonNull Element row) {
		final Document doc = row.getOwnerDocument();

		final List<List<Node>> cells = StreamUtils.group((nextGroup, flush) -> {
			rowNodes.forEach(child -> {
				if (isAmpersand(child)) {
					if (nextGroup.isEmpty()) {
						nextGroup.add(doc.createTextNode("\u00a0"));
					}
					flush.run();
					return;
				}

				nextGroup.add(child);
			});
		});

		cells.forEach(cellContent -> {
			final Element cell = doc.createElement("cell");
			cellContent.forEach(cell::appendChild);
			row.appendChild(cell);
		});
	}

}
