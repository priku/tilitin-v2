package kirjanpito.ui;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JOptionPane;
import javax.swing.JTable;

import kirjanpito.db.DataAccessException;
import kirjanpito.db.Document;
import kirjanpito.db.DocumentType;
import kirjanpito.db.Entry;
import kirjanpito.db.Period;
import kirjanpito.models.DocumentModel;
import kirjanpito.models.EntryTableModel;
import kirjanpito.util.Registry;

/**
 * Handles document validation and saving operations.
 * Validates document fields (number, date, entries) before saving.
 * 
 * Extracted from DocumentFrame as part of Phase 10 refactoring.
 */
public class DocumentValidator {

	private final DocumentModel model;
	private final EntryTableModel tableModel;
	private final Registry registry;
	private final ValidationCallbacks callbacks;
	
	private static final Logger logger = Logger.getLogger(Kirjanpito.LOGGER_NAME);

	/**
	 * Callback interface for validation operations.
	 */
	public interface ValidationCallbacks {
		/** Gets the document number from text field */
		String getNumberText();
		
		/** Sets the document number text field */
		void setNumberText(String text);
		
		/** Gets the document date from date field */
		java.util.Date getDate() throws ParseException;
		
		/** Requests focus on number field */
		void focusNumberField();
		
		/** Requests focus on date field */
		void focusDateField();
		
		/** Gets the entry table */
		JTable getEntryTable();
		
		/** Stops cell editing */
		void stopEditing();
		
		/** Gets debit total */
		BigDecimal getDebitTotal();
		
		/** Gets credit total */
		BigDecimal getCreditTotal();
		
		/** Shows confirmation dialog */
		int showConfirmDialog(String message, String title);
		
		/** Shows error message */
		void showErrorMessage(String message);
		
		/** Shows information message */
		void showInformationMessage(String message);
		
		/** Shows data access error */
		void showDataAccessError(DataAccessException e, String message);
		
		/** Updates position display */
		void updatePosition();
		
		/** Refreshes model */
		void refreshModel(boolean fullRefresh);
		
		/** Gets the parent window */
		java.awt.Window getParentWindow();
	}

	/**
	 * Result of validation/save operation.
	 */
	public enum SaveResult {
		/** Save successful, no number change */
		SUCCESS,
		/** Save successful, number changed */
		SUCCESS_NUMBER_CHANGED,
		/** Validation failed */
		VALIDATION_FAILED,
		/** Save failed due to database error */
		DATABASE_ERROR
	}

	/**
	 * Constructor.
	 */
	public DocumentValidator(DocumentModel model, EntryTableModel tableModel,
			Registry registry, ValidationCallbacks callbacks) {
		this.model = model;
		this.tableModel = tableModel;
		this.registry = registry;
		this.callbacks = callbacks;
	}

	/**
	 * Saves document if changed.
	 * 
	 * @return true if save was successful or not needed, false if failed
	 */
	public boolean saveDocumentIfChanged() {
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
				if (callbacks.getDebitTotal().compareTo(callbacks.getCreditTotal()) != 0) {
					callbacks.showInformationMessage("Debet- ja kredit-vientien summat eroavat toisistaan.");
				}
			}

			model.saveDocument();
			numberChanged = (result == 1);
		}
		catch (DataAccessException e) {
			String message = "Tositetietojen tallentaminen epäonnistui";
			logger.log(Level.SEVERE, message, e);
			callbacks.showDataAccessError(e, message);
			return false;
		}

		if (numberChanged) {
			callbacks.refreshModel(false);
		}
		else {
			callbacks.updatePosition();
		}

		return true;
	}

	/**
	 * Updates the document model with user input.
	 * 
	 * @return -1 if validation failed, 0 if success, 1 if success with number change
	 */
	public int updateModel() {
		Document document = model.getDocument();
		int result = 0;
		callbacks.stopEditing();

		// Validate document number
		try {
			int number = Integer.parseInt(callbacks.getNumberText());

			if (number != document.getNumber() && callbacks.showConfirmDialog(
					"Haluatko varmasti muuttaa tositenumeroa?",
					Kirjanpito.APP_NAME) != JOptionPane.YES_OPTION) {
				callbacks.setNumberText(Integer.toString(document.getNumber()));
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
					callbacks.showDataAccessError(e, message);
					return -2;
				}

				if (r == -1) {
					callbacks.showErrorMessage(String.format("Tositenumero %d on jo käytössä.", number));
					return -1;
				}
				else if (r == -2) {
					DocumentType documentType = model.getDocumentType();
					callbacks.showErrorMessage(String.format("Tositenumero %d ei kuulu tositelajin \"%s\" numerovälille (%d-%d).",
							number, documentType.getName(), documentType.getNumberStart(), documentType.getNumberEnd()));
					return -1;
				}

				document.setNumber(number);
				result = 1;
			}
		}
		catch (NumberFormatException e) {
			callbacks.showErrorMessage("Virheellinen tositenumero.");
			callbacks.focusNumberField();
			return -1;
		}

		// Validate date
		java.util.Date date;
		try {
			date = callbacks.getDate();
		}
		catch (ParseException e) {
			callbacks.showErrorMessage("Virheellinen päivämäärä.");
			callbacks.focusDateField();
			return -1;
		}

		if (date == null) {
			callbacks.showErrorMessage("Syötä tositteen päivämäärä ennen tallentamista.");
			callbacks.focusDateField();
			return -1;
		}

		document.setDate(date);

		if (!model.isMonthEditable(date)) {
			SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM yyyy");
			callbacks.showErrorMessage(String.format("Kuukausi %s on lukittu.",
					dateFormat.format(date)));
			callbacks.focusDateField();
			return -1;
		}

		Period period = registry.getPeriod();

		if (date.before(period.getStartDate()) || date.after(period.getEndDate())) {
			SimpleDateFormat dateFormat = new SimpleDateFormat("d.M.yyyy");
			callbacks.showErrorMessage(String.format("Päivämäärä ei kuulu nykyiselle tilikaudelle\n%s - %s.",
					dateFormat.format(period.getStartDate()),
					dateFormat.format(period.getEndDate())));
			callbacks.focusDateField();
			return -1;
		}

		// Remove empty entries and validate remaining
		removeEmptyEntry();
		int count = model.getEntryCount();

		for (int i = 0; i < count; i++) {
			if (model.getEntry(i).getAccountId() < 1) {
				callbacks.showErrorMessage("Valitse tili ennen tallentamista.");
				callbacks.getEntryTable().changeSelection(i, 0, false, false);
				callbacks.getEntryTable().requestFocusInWindow();
				return -1;
			}

			if (model.getEntry(i).getAmount() == null) {
				callbacks.showErrorMessage("Syötä viennin rahamäärä ennen tallentamista.");
				return -1;
			}
		}

		return result;
	}

	/**
	 * Removes the last entry if it's empty (same description as previous and zero amount or no account).
	 */
	public void removeEmptyEntry() {
		int count = model.getEntryCount();

		if (count > 0) {
			String prevDescription = "";

			if (count - 2 >= 0) {
				prevDescription = model.getEntry(count - 2).getDescription();
			}

			Entry lastEntry = model.getEntry(count - 1);

			if ((lastEntry.getAccountId() <= 0 ||
					BigDecimal.ZERO.compareTo(lastEntry.getAmount()) == 0) &&
					lastEntry.getDescription().equals(prevDescription)) {

				model.removeEntry(count - 1);
				tableModel.fireTableRowsDeleted(count - 1, count - 1);
			}
		}
	}

	/**
	 * Saves document type if changed.
	 */
	public void saveDocumentTypeIfChanged() {
		try {
			model.saveDocumentType();
		}
		catch (DataAccessException e) {
			String message = "Asetuksien tallentaminen epäonnistui";
			logger.log(Level.SEVERE, message, e);
			callbacks.showDataAccessError(e, message);
		}
	}
}
