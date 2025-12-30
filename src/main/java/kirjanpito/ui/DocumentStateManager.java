package kirjanpito.ui;

import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import java.awt.Color;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import kirjanpito.db.DataAccessException;
import kirjanpito.db.Document;
import kirjanpito.db.DocumentType;
import kirjanpito.db.Entry;
import kirjanpito.db.EntryTemplate;
import kirjanpito.db.Period;
import kirjanpito.models.DocumentModel;
import kirjanpito.models.EntryTableModel;
import kirjanpito.models.TextFieldWithLockIcon;
import kirjanpito.util.Registry;

/**
 * Manages document state and UI updates for DocumentFrame.
 *
 * This class was extracted from DocumentFrame as part of Phase 6 refactoring
 * to reduce the size of the DocumentFrame god object (originally 3,093 lines).
 *
 * Responsibilities:
 * - Document state persistence (save/load)
 * - UI component updates (labels, text fields)
 * - Validation logic
 * - Dirty state tracking
 * - Total row calculations
 *
 * @author Petri Kivikangas
 * @since 2.2.5
 */
public class DocumentStateManager {

	private final Registry registry;
	private final StateCallbacks callbacks;

	// UI Components - labels
	private final JLabel documentLabel;
	private final JLabel periodLabel;
	private final JLabel documentTypeLabel;
	private final JLabel debitTotalLabel;
	private final JLabel creditTotalLabel;
	private final JLabel differenceLabel;

	// UI Components - input fields
	private final TextFieldWithLockIcon numberTextField;
	private final DateTextField dateTextField;

	// UI Components - other
	private final AttachmentsPanel attachmentsPanel;

	// Formatting
	private final DecimalFormat formatter;

	// Internal state
	private BigDecimal debitTotal;
	private BigDecimal creditTotal;

	private static final Logger logger = Logger.getLogger(Kirjanpito.LOGGER_NAME);

	/**
	 * Callback interface for DocumentFrame interactions.
	 */
	public interface StateCallbacks {
		/** Returns the DocumentModel instance */
		DocumentModel getDocumentModel();

		/** Returns the EntryTableModel instance */
		EntryTableModel getEntryTableModel();

		/** Returns the entry table */
		JTable getEntryTable();

		/** Stops cell editing */
		void stopEditing();

		/** Refreshes model from database */
		void refreshModel(boolean positionChanged);

		/** Sets components enabled/disabled */
		void setComponentsEnabled(boolean docExists, boolean periodEditable, boolean docEditable);

		/** Returns parent window for dialogs */
		java.awt.Window getParentWindow();

		/** Returns search enabled state */
		boolean isSearchEnabled();

		/** Finds document type by number */
		int findDocumentTypeByNumber(int number);

		/** Removes empty entry from table */
		void removeEmptyEntry();
	}

	/**
	 * Constructor.
	 *
	 * @param registry Registry instance
	 * @param numberTextField Document number text field
	 * @param dateTextField Document date text field
	 * @param documentLabel Document position label
	 * @param periodLabel Period label
	 * @param documentTypeLabel Document type label
	 * @param debitTotalLabel Debit total label
	 * @param creditTotalLabel Credit total label
	 * @param differenceLabel Difference label
	 * @param attachmentsPanel Attachments panel
	 * @param formatter Currency formatter
	 * @param callbacks Callback interface to DocumentFrame
	 */
	public DocumentStateManager(Registry registry,
			TextFieldWithLockIcon numberTextField,
			DateTextField dateTextField,
			JLabel documentLabel,
			JLabel periodLabel,
			JLabel documentTypeLabel,
			JLabel debitTotalLabel,
			JLabel creditTotalLabel,
			JLabel differenceLabel,
			AttachmentsPanel attachmentsPanel,
			DecimalFormat formatter,
			StateCallbacks callbacks) {
		this.registry = registry;
		this.numberTextField = numberTextField;
		this.dateTextField = dateTextField;
		this.documentLabel = documentLabel;
		this.periodLabel = periodLabel;
		this.documentTypeLabel = documentTypeLabel;
		this.debitTotalLabel = debitTotalLabel;
		this.creditTotalLabel = creditTotalLabel;
		this.differenceLabel = differenceLabel;
		this.attachmentsPanel = attachmentsPanel;
		this.formatter = formatter;
		this.callbacks = callbacks;
		this.debitTotal = BigDecimal.ZERO;
		this.creditTotal = BigDecimal.ZERO;
	}

	/**
	 * Päivittää tilikauden nimen.
	 */
	public void updatePeriod() {
		Period period = registry.getPeriod();

		if (period == null) {
			periodLabel.setText("");
		}
		else {
			DateFormat dateFormat = new SimpleDateFormat("d.M.yyyy");
			String text = "Tilikausi " +
				dateFormat.format(period.getStartDate()) + " - " +
				dateFormat.format(period.getEndDate());

			if (!period.isLocked()) {
				text = text + " (lukitsematon)";
			}

			periodLabel.setText(text);
		}
	}

	/**
	 * Päivittää tositteen sijainnin.
	 */
	public void updatePosition() {
		DocumentModel model = callbacks.getDocumentModel();
		int count = model.getDocumentCount();
		int countTotal = model.getDocumentCountTotal();

		if (count != countTotal) {
			documentLabel.setText(String.format("Tosite %d / %d (%d)",
					model.getDocumentPosition() + 1,
					model.getDocumentCount(),
					model.getDocumentCountTotal()));
		}
		else {
			documentLabel.setText(String.format("Tosite %d / %d",
					model.getDocumentPosition() + 1,
					model.getDocumentCount()));
		}

		DocumentType type;

		if (callbacks.isSearchEnabled()) {
			int index = callbacks.findDocumentTypeByNumber(model.getDocument().getNumber());
			type = (index < 0) ? null : registry.getDocumentTypes().get(index);
		}
		else {
			type = model.getDocumentType();
		}

		if (type == null) {
			documentTypeLabel.setText("");
		}
		else {
			documentTypeLabel.setText(type.getName());
		}
	}

	/**
	 * Päivittää tositteen tiedot.
	 */
	public void updateDocument() {
		DocumentModel model = callbacks.getDocumentModel();
		Document document = model.getDocument();

		if (document == null) {
			numberTextField.setText("");
			dateTextField.setDate(null);
		}
		else {
			numberTextField.setText(Integer.toString(document.getNumber()));
			dateTextField.setDate(document.getDate());
			dateTextField.setBaseDate(document.getDate());

			/* Uuden tositteen päivämäärä on kopioitu edellisestä
			 * tositteesta. Valitaan päivämääräkentän teksti, jotta
			 * uusi päivämäärä voidaan kirjoittaa päälle. */
			if (document.getId() <= 0) {
				dateTextField.select(0, dateTextField.getText().length());
			}

			dateTextField.requestFocus();
		}

		boolean documentEditable = model.isDocumentEditable();
		callbacks.getEntryTableModel().fireTableDataChanged();
		numberTextField.setLockIconVisible(document != null && !documentEditable);
		callbacks.setComponentsEnabled(document != null, model.isPeriodEditable(), documentEditable);

		// Update attachments panel with current document ID
		if (attachmentsPanel != null) {
			int documentId = (document != null && document.getId() > 0) ? document.getId() : 0;
			attachmentsPanel.setDocumentId(documentId);
		}
	}

	/**
	 * Päivittää summarivin tiedot.
	 */
	public void updateTotalRow() {
		DocumentModel model = callbacks.getDocumentModel();
		debitTotal = BigDecimal.ZERO;
		creditTotal = BigDecimal.ZERO;
		int count = model.getEntryCount();
		Entry entry;

		for (int i = 0; i < count; i++) {
			entry = model.getEntry(i);

			if (entry.isDebit()) {
				debitTotal = debitTotal.add(model.getVatIncludedAmount(i));
			}
			else {
				creditTotal = creditTotal.add(model.getVatIncludedAmount(i));
			}
		}

		BigDecimal difference = creditTotal.subtract(debitTotal).abs();
		debitTotalLabel.setText(formatter.format(debitTotal));
		creditTotalLabel.setText(formatter.format(creditTotal));
		// Käytä teeman mukaista error-väriä epätasapainon korostamiseen
		Color errorColor = UIManager.getColor("Actions.Red");
		if (errorColor == null) {
			errorColor = Color.RED; // Fallback
		}
		differenceLabel.setForeground(difference.compareTo(BigDecimal.ZERO) == 0 ?
				debitTotalLabel.getForeground() : errorColor);
		differenceLabel.setText(formatter.format(difference));
	}

	/**
	 * Tallentaa käyttäjän tekemät muutokset tositteeseen.
	 *
	 * @return <code>true</code>, jos tallennus onnistui;
	 * <code>false</code>, jos tallennus epäonnistui
	 */
	public boolean saveDocumentIfChanged() {
		DocumentModel model = callbacks.getDocumentModel();
		callbacks.stopEditing();

		if (!model.isDocumentChanged() || !model.isDocumentEditable()) {
			return true;
		}

		if (logger.isLoggable(Level.FINE)) {
			Document document = model.getDocument();
			logger.fine(String.format("Tallennetaan tosite %d (ID %d)",
					document.getNumber(), document.getId()));
		}

		boolean numberChanged = false;

		try {
			int result = updateModel();

			if (result < 0) {
				return false;
			}

			if (registry.getSettings().getProperty("debitCreditRemark", "false").equals("true")) {
				if (debitTotal.compareTo(creditTotal) != 0) {
					SwingUtils.showInformationMessage(callbacks.getParentWindow(),
							"Debet- ja kredit-vientien summat eroavat toisistaan.");
				}
			}

			model.saveDocument();
			numberChanged = (result == 1);
		}
		catch (DataAccessException e) {
			String message = "Tositetietojen tallentaminen epäonnistui";
			logger.log(Level.SEVERE, message, e);
			SwingUtils.showDataAccessErrorMessage(callbacks.getParentWindow(), e, message);
			return false;
		}

		if (numberChanged) {
			callbacks.refreshModel(false);
		}
		else {
			updatePosition();
		}

		return true;
	}

	/**
	 * Päivittää käyttäjän syöttämät tiedot <code>DocumentModel</code>ille.
	 *
	 * @return -1, jos tiedot ovat virheellisiä; 0, jos tietojen päivittäminen onnistui;
	 * 1, jos päivittäminen onnistui ja tositenumero on muuttunut
	 */
	protected int updateModel() {
		DocumentModel model = callbacks.getDocumentModel();
		Document document = model.getDocument();
		int result = 0;
		callbacks.stopEditing();

		try {
			int number = Integer.parseInt(numberTextField.getText());

			if (number != document.getNumber() && JOptionPane.showConfirmDialog(
					callbacks.getParentWindow(), "Haluatko varmasti muuttaa tositenumeroa?",
					Kirjanpito.APP_NAME, JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE)
					!= JOptionPane.YES_OPTION) {
				numberTextField.setText(Integer.toString(document.getNumber()));
				number = document.getNumber();
			}

			/* Tarkistetaan tositenumeron oikeellisuus, jos käyttäjä on muuttanut sitä. */
			if (number != document.getNumber()) {
				int r;

				try {
					r = model.validateDocumentNumber(number);
				}
				catch (DataAccessException e) {
					String message = "Tositetietojen hakeminen epäonnistui";
					logger.log(Level.SEVERE, message, e);
					SwingUtils.showDataAccessErrorMessage(callbacks.getParentWindow(), e, message);
					return -1;
				}

				if (r < 0) {
					SwingUtils.showInformationMessage(callbacks.getParentWindow(),
							"Tositenumero " + number + " on jo käytössä.");
					return -1;
				}

				result = 1;
			}

			document.setNumber(number);
		}
		catch (NumberFormatException e) {
			SwingUtils.showInformationMessage(callbacks.getParentWindow(),
					"Tositenumero saa sisältää vain numeroita.");
			return -1;
		}

		try {
			document.setDate(dateTextField.getDate());
		}
		catch (IllegalArgumentException | ParseException e) {
			SwingUtils.showInformationMessage(callbacks.getParentWindow(),
					"Virheellinen päivämäärä.");
			return -1;
		}

		if (!model.isMonthEditable(document.getDate())) {
			SwingUtils.showInformationMessage(callbacks.getParentWindow(),
					"Tositetta ei voi tallentaa lukitulle kuukaudelle.");
			return -1;
		}

		if (!callbacks.getEntryTable().isEditing()) {
			callbacks.removeEmptyEntry();
		}

		return result;
	}

	/**
	 * Getters for state
	 */
	public BigDecimal getDebitTotal() {
		return debitTotal;
	}

	public BigDecimal getCreditTotal() {
		return creditTotal;
	}
}
