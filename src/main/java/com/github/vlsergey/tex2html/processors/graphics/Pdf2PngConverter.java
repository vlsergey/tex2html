package com.github.vlsergey.tex2html.processors.graphics;

import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.stereotype.Component;

import lombok.NonNull;
import lombok.SneakyThrows;

@Component
public class Pdf2PngConverter implements GraphicsConverter {

	@Override
	@SneakyThrows
	public void convert(final @NonNull File src, final @NonNull File dst) {
		FileUtils.forceMkdirParent(dst);
		try (PDDocument doc = PDDocument.load(src)) {
			PDFRenderer pdfRenderer = new PDFRenderer(doc);
			BufferedImage bImage = pdfRenderer.renderImageWithDPI(0, 300, ImageType.ARGB);
			ImageIO.write(bImage, StringUtils.substringAfterLast(dst.getPath(), "."), dst);
		}
	}

}
