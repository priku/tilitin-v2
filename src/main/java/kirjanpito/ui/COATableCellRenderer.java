package kirjanpito.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableCellRenderer;

import kirjanpito.db.Account;
import kirjanpito.ui.resources.Resources;
import kirjanpito.util.ChartOfAccounts;

/**
 * <code>TableCellRenderer</code>in toteuttava luokka, joka näyttää
 * tilikartan rivin.
 *
 * @author Tommi Helineva
 */
public class COATableCellRenderer extends DefaultTableCellRenderer {
	private ChartOfAccounts coa;
	private boolean indentEnabled;
	private boolean highlightFavouriteAccounts;
	private Color favouriteColor;
	private Image favouriteImage;
	private boolean imageVisible;
	private String percentageText;
	private DecimalFormat formatter;

	private static final long serialVersionUID = 1L;

	public COATableCellRenderer() {
		this.indentEnabled = true;
		this.highlightFavouriteAccounts = true;
		// Käytä teeman mukaista highlight-väriä suosikkitileille
		// FlatLaf tarjoaa automaattisesti teemaan sopivan accent-värin
		this.favouriteColor = UIManager.getColor("List.selectionInactiveBackground");
		if (this.favouriteColor == null) {
			// Fallback jos UIManager ei tarjoa väriä
			this.favouriteColor = new Color(245, 208, 169);
		}
		this.formatter = new DecimalFormat();
		this.formatter.setMinimumFractionDigits(0);
		this.formatter.setMaximumFractionDigits(2);
		this.favouriteImage = Resources.loadAsImage("favourite-16x16.png");
	}

	/**
	 * Palauttaa tilikartan, jonka rivit näytetään.
	 *
	 * @return tilikartta
	 */
	public ChartOfAccounts getChartOfAccounts() {
		return coa;
	}

	/**
	 * Asettaa tilikartan, jonka rivit näytetään.
	 *
	 * @param coa tilikartta
	 */
	public void setChartOfAccounts(ChartOfAccounts coa) {
		this.coa = coa;
	}

	public boolean isIndentEnabled() {
		return indentEnabled;
	}

	public void setIndentEnabled(boolean indentEnabled) {
		this.indentEnabled = indentEnabled;
	}

	public boolean isHighlightFavouriteAccounts() {
		return highlightFavouriteAccounts;
	}

	public void setHighlightFavouriteAccounts(boolean highlightFavouriteAccounts) {
		this.highlightFavouriteAccounts = highlightFavouriteAccounts;
	}

	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column)
	{
		super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		row = table.convertRowIndexToModel(row);
		setForeground(isSelected ? table.getSelectionForeground() : table.getForeground());
		setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
		Font font = getFont();
		int level;

		if (coa.getType(row) == ChartOfAccounts.TYPE_HEADING) {
			imageVisible = false;
			percentageText = null;

			if (coa.getHeading(row).getLevel() == 0) {
				// Käytä teeman mukaista aksentti-/korostusväriä pääotsikkotasolle
				Color accentColor = UIManager.getColor("Component.accentColor");
				if (accentColor == null) {
					// Fallback: käytä error-väriä jos accent ei ole saatavilla
					accentColor = UIManager.getColor("Actions.Red");
					if (accentColor == null) {
						accentColor = Color.RED; // Ultimate fallback
					}
				}
				setForeground(accentColor);
			}

			setFont(font.deriveFont(Font.BOLD));
			level = coa.getHeading(row).getLevel() * 2;
		}
		else {
			Account account = coa.getAccount(row);
			imageVisible = highlightFavouriteAccounts && (account.getFlags() & 0x01) != 0;
			int accountVatCode = account.getVatCode();
			boolean vatPercentEnabled = (accountVatCode == 4 ||
					accountVatCode == 5 || accountVatCode == 9 || accountVatCode == 11);
			percentageText = vatPercentEnabled ?
					formatter.format(coa.getAccount(row).getVatRate()) + " %" : null;

			if (!isSelected && imageVisible) {
				setBackground(favouriteColor);
			}

			setFont(font.deriveFont(Font.PLAIN));
			level = 12;
		}

		if (!indentEnabled) {
			level = 0;
		}

		/* Sisennetään tekstiä. */
		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < level; i++) sb.append(' ');

		if (value != null)
			sb.append(value.toString());

		setText(sb.toString());
		return this;
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);

		if (imageVisible) {
			g.drawImage(favouriteImage, getWidth() - 25, (getHeight() - 16) / 2, null);
		}

		if (percentageText != null) {
			Graphics2D g2d = (Graphics2D)g;
			Toolkit tk = Toolkit.getDefaultToolkit();
			@SuppressWarnings("rawtypes")
			Map desktopHints = (Map)tk.getDesktopProperty("awt.font.desktophints");

			if (desktopHints != null) {
			    g2d.addRenderingHints(desktopHints);
			}

			FontMetrics metrics = g.getFontMetrics();
			g2d.drawString(percentageText, getWidth() - 40 -
					(int)metrics.getStringBounds(percentageText, g).getWidth(),
					metrics.getHeight());
		}
	}
}
