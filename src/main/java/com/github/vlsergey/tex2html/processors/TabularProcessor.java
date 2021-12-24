package com.github.vlsergey.tex2html.processors;

import static java.util.Collections.unmodifiableMap;
import static java.util.stream.Collectors.toList;

import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.antlr.v4.runtime.tree.ParseTree;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.github.vlsergey.tex2html.Tex2HtmlCommand;
import com.github.vlsergey.tex2html.Tex2HtmlOptions;
import com.github.vlsergey.tex2html.grammar.ColumnSpecLexer;
import com.github.vlsergey.tex2html.grammar.ColumnSpecParser;
import com.github.vlsergey.tex2html.grammar.ColumnSpecParser.BorderSpecContext;
import com.github.vlsergey.tex2html.grammar.ColumnSpecParser.ColAlignContext;
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

	private static boolean isLine(Node node) {
		return TexXmlUtils.isCommandElement(node, "hline") || TexXmlUtils.isCommandElement(node, "cline");
	}

	private static boolean isLineBreak(Node node) {
		return node instanceof Element && "line-break".equals(node.getNodeName());
	}

	private final XPathFactory xPathFactory = XPathFactory.newInstance();

	private boolean isAmpersand(Node node) {
		return node instanceof Element && "ampersand".equals(node.getNodeName());
	}

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

			final ColAlignContext colAlign;
			final int repeat;
			if (columnSpec.columnSpecMultiplied() != null) {
				colAlign = columnSpec.columnSpecMultiplied().colAlign();
				repeat = Integer.parseInt(columnSpec.columnSpecMultiplied().number().getText());
			} else {
				colAlign = columnSpec.colAlign();
				repeat = 1;
			}

			Map<String, String> cellProps = parseCellProps(withoutSpaces, i, colAlign);

			for (int k = 0; k < repeat; k++) {
				result.add(cellProps);
			}
		}
		return result;
	}

	private Map<String, String> parseCellProps(List<ParseTree> withoutSpaces, int i, ColAlignContext align) {
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
		final String textAlign = COLUMN_SPEC_TO_TEXT_ALIGN.get(align.getText());

		final Map<String, String> cellProps = new LinkedHashMap<>();
		if (borderLeft != null)
			cellProps.put("border-left", borderLeft);
		if (borderRight != null)
			cellProps.put("border-right", borderRight);
		if (textAlign != null)
			cellProps.put("text-align", textAlign);
		return cellProps;
	}

	@Override
	public Document process(Tex2HtmlOptions command, Document xmlDoc) {
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

				if (isLine(child)) {
					if (!nextGroup.isEmpty() && !isLine(nextGroup.get(0))) {
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

				if (!nextGroup.isEmpty() && isLine(nextGroup.get(0))) {
					flush.run();
				}

				nextGroup.add(child);
			});
		});

		rowsAndBorders = rowsAndBorders.stream().map(DomUtils::trim).filter(row -> !row.isEmpty()).collect(toList());
		for (int i = rowsAndBorders.size() - 1; i > 0; i--) {
			List<Node> first = rowsAndBorders.get(i - 1);
			List<Node> second = rowsAndBorders.get(i);

			if (isLine(first.get(0)) && isLine(second.get(0))) {
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

		List<Element> rows = new ArrayList<>();
		for (int i = 0; i < rowsAndBorders.size(); i++) {
			List<Node> rowNodes = rowsAndBorders.get(i);
			if (isLine(rowNodes.get(0))) {
				continue;
			}

			final String borderTop = i == 0 ? null
					: isLine(rowsAndBorders.get(i - 1).get(0))
							? rowsAndBorders.get(i - 1).size() == 1 ? "solid thin" : "double"
							: null;
			final String borderBottom = i == rowsAndBorders.size() - 1 ? null
					: isLine(rowsAndBorders.get(i + 1).get(0))
							? rowsAndBorders.get(i + 1).size() == 1 ? "solid thin" : "double"
							: null;

			final Element row = doc.createElement("row");
			if (borderTop != null)
				row.setAttribute("border-top", borderTop);
			if (borderBottom != null)
				row.setAttribute("border-bottom", borderBottom);
			tabular.appendChild(row);
			rows.add(row);

			processRow(rowNodes, row);
		}

		/*
		 * There is difference between rowspan behavior in LaTeX and HTML. In HTML we
		 * assume that "spanned" cell shall not present (like with multicolumn), but in
		 * LaTeX multirow bottom cell is still present. It need to be removed from HTML
		 */
		final @NonNull BitSet[] toRemove = new BitSet[rows.size()];
		for (int r = 0; r < rows.size(); r++) {
			toRemove[r] = new BitSet();
		}
		for (int r = 0; r < rows.size(); r++) {
			NodeList cells = rows.get(r).getChildNodes();
			for (int c = 0; c < cells.getLength(); c++) {
				final Element cell = (Element) cells.item(c);
				final String rowSpanStr = cell.getAttribute("rowspan");
				if (StringUtils.isNotBlank(rowSpanStr)) {
					int rowSpan = Integer.parseInt(rowSpanStr);
					for (int r2 = r + 1; r2 < Math.min(rows.size(), r + rowSpan); r2++) {
						toRemove[r2].set(c);
					}
				}
			}
		}

		for (int r = 0; r < rows.size(); r++) {
			final Element row = rows.get(r);
			final NodeList cells = row.getChildNodes();
			for (int c = cells.getLength() - 1; c >= 0; c--) {
				if (toRemove[r].get(c)) {
					row.removeChild(row.getChildNodes().item(c));
				}
			}
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
			DomUtils.trim(cellContent);

			final Element cell = doc.createElement("cell");
			cellContent.forEach(cell::appendChild);
			row.appendChild(cell);

			processSpans(cell);
		});
	}

	@SneakyThrows
	private void processSpans(Element cell) {
		boolean check = true;
		while (check) {
			check = false;

			if (cell.getChildNodes().getLength() == 1) {
				final Node singleChild = cell.getChildNodes().item(0);
				if (TexXmlUtils.isCommandElement(singleChild, "multicolumn")) {
					final String colspan = (String) xPathFactory.newXPath()
							.evaluate("./argument[@required='true'][1]/text()", singleChild, XPathConstants.STRING);
					final Node contentContainer = (Node) xPathFactory.newXPath()
							.evaluate("./argument[@required='true'][3]", singleChild, XPathConstants.NODE);
					cell.setAttribute("colspan", colspan);

					cell.removeChild(singleChild);
					DomUtils.childrenStream(contentContainer).forEach(node -> cell.appendChild(node.cloneNode(true)));
					check = true;
					continue;
				}

				if (TexXmlUtils.isCommandElement(singleChild, "multirow")) {
					final String rowspan = (String) xPathFactory.newXPath()
							.evaluate("./argument[@required='true'][1]/text()", singleChild, XPathConstants.STRING);
					final Node contentContainer = (Node) xPathFactory.newXPath()
							.evaluate("./argument[@required='true'][3]", singleChild, XPathConstants.NODE);
					cell.setAttribute("rowspan", rowspan);

					cell.removeChild(singleChild);
					DomUtils.childrenStream(contentContainer).forEach(node -> cell.appendChild(node.cloneNode(true)));
					check = true;
					continue;
				}
			}
		}
	}

}
