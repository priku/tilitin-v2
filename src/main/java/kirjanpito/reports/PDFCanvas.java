package kirjanpito.reports;

import java.io.IOException;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

public class PDFCanvas implements PrintCanvas {
	private PDPageContentStream contentStream;
	private float pageWidth, pageHeight;
	private float lineHeight;
	private boolean textMode;
	private PDFont currentFont;
	private float currentFontSize;
	private PDFont normalFont;
	private PDFont boldFont;
	private PDFont italicFont;

	public PDFCanvas(PDDocument document, PDPage page) throws IOException {
		PDRectangle mediaBox = page.getMediaBox();
		this.pageWidth = mediaBox.getWidth();
		this.pageHeight = mediaBox.getHeight();
		this.contentStream = new PDPageContentStream(document, page);

		// Standard PDF fonts (equivalent to iText's Helvetica)
		this.normalFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
		this.boldFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
		this.italicFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA_OBLIQUE);
	}

	public int getPageWidth() {
		return (int)pageWidth;
	}

	public int getPageHeight() {
		return (int)pageHeight;
	}

	public int getImageableHeight() {
		return (int)pageHeight;
	}

	public int getImageableWidth() {
		return (int)pageWidth;
	}

	public int getImageableX() {
		return 0;
	}

	public int getImageableY() {
		return 0;
	}

	public void close() {
		endText();
		try {
			if (contentStream != null) {
				contentStream.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void setHeadingStyle() {
		beginText();
		currentFont = normalFont;
		currentFontSize = 14f;
		calculateLineHeight(normalFont, 14f);
	}

	public void setNormalStyle() {
		beginText();
		currentFont = normalFont;
		currentFontSize = 10f;
		calculateLineHeight(normalFont, 10f);
	}

	public void setSmallStyle() {
		beginText();
		currentFont = normalFont;
		currentFontSize = 9f;
		calculateLineHeight(normalFont, 9f);
	}

	public void setBoldStyle() {
		beginText();
		currentFont = boldFont;
		currentFontSize = 10f;
		calculateLineHeight(boldFont, 10f);
	}

	public void setItalicStyle() {
		beginText();
		currentFont = italicFont;
		currentFontSize = 10f;
		calculateLineHeight(italicFont, 10f);
	}

	public void drawText(int x, int y, String s) {
		try {
			beginText();
			contentStream.setFont(currentFont, currentFontSize);
			contentStream.newLineAtOffset(x, pageHeight - lineHeight - y);
			contentStream.showText(s);
			endText();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void drawTextCenter(int x, int y, String s) {
		try {
			float textWidth = currentFont.getStringWidth(s) / 1000 * currentFontSize;
			float centerX = x - textWidth / 2;
			beginText();
			contentStream.setFont(currentFont, currentFontSize);
			contentStream.newLineAtOffset(centerX, pageHeight - lineHeight - y);
			contentStream.showText(s);
			endText();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void drawTextRight(int x, int y, String s) {
		try {
			float textWidth = currentFont.getStringWidth(s) / 1000 * currentFontSize;
			float rightX = x - textWidth;
			beginText();
			contentStream.setFont(currentFont, currentFontSize);
			contentStream.newLineAtOffset(rightX, pageHeight - lineHeight - y);
			contentStream.showText(s);
			endText();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void drawLine(int x1, int y1, int x2, int y2, float lineWidth) {
		try {
			endText();
			contentStream.setLineWidth(lineWidth);
			contentStream.moveTo(x1, pageHeight - y1);
			contentStream.lineTo(x2, pageHeight - y2);
			contentStream.stroke();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public int stringWidth(String s) {
		try {
			return (int)(currentFont.getStringWidth(s) / 1000 * currentFontSize);
		} catch (IOException e) {
			e.printStackTrace();
			return 0;
		}
	}

	private void calculateLineHeight(PDFont font, float size) {
		currentFont = font;
		currentFontSize = size;
		try {
			// PDFBox uses different units than iText
			// Ascent and descent are in 1000ths of a unit
			float ascent = font.getFontDescriptor().getAscent() / 1000 * size;
			float descent = font.getFontDescriptor().getDescent() / 1000 * size;
			lineHeight = ascent - descent - 5;
		} catch (Exception e) {
			// Fallback if font descriptor is not available
			lineHeight = size;
		}
	}

	private void beginText() {
		try {
			if (!textMode) {
				contentStream.beginText();
				textMode = true;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void endText() {
		try {
			if (textMode) {
				contentStream.endText();
				textMode = false;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
