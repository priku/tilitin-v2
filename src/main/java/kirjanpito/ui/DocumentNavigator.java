package kirjanpito.ui;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.util.logging.Level;
import java.util.logging.Logger;

import kirjanpito.db.DataAccessException;
import kirjanpito.db.DocumentType;
import kirjanpito.models.DocumentModel;
import kirjanpito.util.Registry;

/**
 * Handles document navigation functionality for DocumentFrame.
 *
 * This class was extracted from DocumentFrame as part of Phase 8 refactoring
 * to reduce the size of the DocumentFrame god object.
 *
 * Responsibilities:
 * - Document navigation (create, delete, go to)
 * - Document search and filtering
 * - Search panel management
 *
 * @author Petri Kivikangas
 * @since 2.2.5
 */
public class DocumentNavigator {

	private final Registry registry;
	private final NavigationCallbacks callbacks;

	// UI Components
	private final JPanel searchPanel;
	private final JTextField searchPhraseTextField;

	// State
	private boolean searchEnabled;

	private static final Logger logger = Logger.getLogger(Kirjanpito.LOGGER_NAME);

	/**
	 * Callback interface for DocumentFrame interactions.
	 */
	public interface NavigationCallbacks {
		/** Returns the DocumentModel instance */
		DocumentModel getDocumentModel();

		/** Saves document if changed, returns false if save fails */
		boolean saveDocumentIfChanged();

		/** Updates position label */
		void updatePosition();

		/** Updates document fields */
		void updateDocument();

		/** Updates total row */
		void updateTotalRow();

		/** Selects document type menu item */
		void selectDocumentTypeMenuItem(int index);

		/** Returns parent window for dialogs */
		java.awt.Window getParentWindow();

		/** Finds document type by number */
		int findDocumentTypeByNumber(int number);

		/** Updates search panel visibility */
		void updateSearchPanel();
	}

	/**
	 * Constructor.
	 *
	 * @param registry Registry instance
	 * @param searchPanel Search panel UI component
	 * @param searchPhraseTextField Search phrase text field
	 * @param callbacks Callback interface to DocumentFrame
	 */
	public DocumentNavigator(Registry registry, JPanel searchPanel,
			JTextField searchPhraseTextField,
			NavigationCallbacks callbacks) {
		this.registry = registry;
		this.searchPanel = searchPanel;
		this.searchPhraseTextField = searchPhraseTextField;
		this.callbacks = callbacks;
		this.searchEnabled = false;
	}

	/**
	 * Luo uuden tositteen.
	 */
	public void createDocument() {
		if (!callbacks.saveDocumentIfChanged()) {
			return;
		}

		try {
			callbacks.getDocumentModel().createDocument();
		}
		catch (DataAccessException e) {
			String message = "Uuden tositteen luominen epäonnistui";
			logger.log(Level.SEVERE, message, e);
			SwingUtils.showDataAccessErrorMessage(callbacks.getParentWindow(), e, message);
		}

		callbacks.updatePosition();
		callbacks.updateDocument();
		callbacks.updateTotalRow();
	}

	/**
	 * Poistaa valitun tositteen.
	 */
	public void deleteDocument() {
		int result = JOptionPane.showConfirmDialog(callbacks.getParentWindow(),
				"Haluatko varmasti poistaa valitun tositteen?",
				Kirjanpito.APP_NAME, JOptionPane.YES_NO_OPTION,
				JOptionPane.QUESTION_MESSAGE);

		if (result == JOptionPane.YES_OPTION) {
			try {
				callbacks.getDocumentModel().deleteDocument();
			}
			catch (DataAccessException e) {
				String message = "Tositteen poistaminen epäonnistui";
				logger.log(Level.SEVERE, message, e);
				SwingUtils.showDataAccessErrorMessage(callbacks.getParentWindow(), e, message);
			}

			callbacks.updatePosition();
			callbacks.updateDocument();
			callbacks.updateTotalRow();
		}
	}

	/**
	 * Siirtyy toiseen tositteeseen. Ennen tietojen
	 * hakemista käyttäjän tekemät muutokset tallennetaan.
	 *
	 * @param index tositteen järjestysnumero
	 */
	public void goToDocument(int index) {
		if (!callbacks.saveDocumentIfChanged()) {
			return;
		}

		try {
			callbacks.getDocumentModel().goToDocument(index);
		}
		catch (DataAccessException e) {
			String message = "Tositetietojen hakeminen epäonnistui";
			logger.log(Level.SEVERE, message, e);
			SwingUtils.showDataAccessErrorMessage(callbacks.getParentWindow(), e, message);
		}

		callbacks.updatePosition();
		callbacks.updateDocument();
		callbacks.updateTotalRow();
	}

	/**
	 * Kysyy käyttäjältä tositenumeroa, ja siirtyy tähän tositteeseen.
	 */
	public void findDocumentByNumber() {
		DocumentModel model = callbacks.getDocumentModel();
		boolean valid = false;
		int documentTypeIndex, index;
		int number = -1;

		while (!valid) {
			String s = JOptionPane.showInputDialog(callbacks.getParentWindow(), "Tositenumero?",
				Kirjanpito.APP_NAME, JOptionPane.PLAIN_MESSAGE);

			if (s != null) {
				try {
					number = Integer.parseInt(s);
				}
				catch (NumberFormatException e) {
					number = -1;
				}

				valid = (number > 0);

				if (!valid) {
					JOptionPane.showMessageDialog(callbacks.getParentWindow(),
							"Tositenumero saa sisältää vain numeroita.",
							Kirjanpito.APP_NAME, JOptionPane.ERROR_MESSAGE);
				}
			}
			else {
				return;
			}
		}

		documentTypeIndex = callbacks.findDocumentTypeByNumber(number);

		try {
			index = model.findDocumentByNumber(documentTypeIndex, number);
		}
		catch (DataAccessException e) {
			String message = "Tositetietojen hakeminen epäonnistui";
			logger.log(Level.SEVERE, message, e);
			SwingUtils.showDataAccessErrorMessage(callbacks.getParentWindow(), e, message);
			return;
		}

		if (index < 0) {
			SwingUtils.showInformationMessage(callbacks.getParentWindow(),
					"Tositetta ei löytynyt numerolla " + number + ".");
			return;
		}

		boolean invalidDocuments = false;

		if (searchEnabled) {
			searchEnabled = false;
			invalidDocuments = true;
			searchPanel.setVisible(false);
		}

		if (model.getDocumentTypeIndex() != documentTypeIndex) {
			callbacks.selectDocumentTypeMenuItem(documentTypeIndex);
			model.setDocumentTypeIndex(documentTypeIndex);
			invalidDocuments = true;
		}

		/* Tositetiedot on haettava tietokannasta, jos tositelaji on muuttunut
		 * tai haku on kytketty pois päältä. */
		if (invalidDocuments) {
			try {
				model.fetchDocuments(index);
			}
			catch (DataAccessException e) {
				String message = "Tositetietojen hakeminen epäonnistui";
				logger.log(Level.SEVERE, message, e);
				SwingUtils.showDataAccessErrorMessage(callbacks.getParentWindow(), e, message);
				return;
			}

			callbacks.updatePosition();
			callbacks.updateDocument();
			callbacks.updateTotalRow();
		}
		else {
			goToDocument(index);
		}
	}

	/**
	 * Kytkee tositteiden haun päälle tai pois päältä.
	 */
	public void toggleSearchPanel() {
		if (!callbacks.saveDocumentIfChanged()) {
			return;
		}

		searchEnabled = !searchEnabled;
		searchPanel.setVisible(searchEnabled);

		if (!searchEnabled) {
			/* Kun haku kytketään pois päältä, haetaan valitun
			 * tositelajin kaikki tositteet.
			 */
			try {
				callbacks.getDocumentModel().fetchDocuments(-1);
			}
			catch (DataAccessException e) {
				String message = "Tositteiden hakeminen epäonnistui";
				logger.log(Level.SEVERE, message, e);
				SwingUtils.showDataAccessErrorMessage(callbacks.getParentWindow(), e, message);
				return;
			}

			callbacks.updatePosition();
			callbacks.updateDocument();
			callbacks.updateTotalRow();
		}
	}

	/**
	 * Etsii tositteita käyttäjän antamalla hakusanalla.
	 */
	public void searchDocuments() {
		if (!callbacks.saveDocumentIfChanged()) {
			return;
		}

		int count;

		try {
			count = callbacks.getDocumentModel().search(searchPhraseTextField.getText());
		}
		catch (DataAccessException e) {
			String message = "Tositteiden hakeminen epäonnistui";
			logger.log(Level.SEVERE, message, e);
			SwingUtils.showDataAccessErrorMessage(callbacks.getParentWindow(), e, message);
			return;
		}

		if (count == 0) {
			SwingUtils.showInformationMessage(callbacks.getParentWindow(),
					"Yhtään tositetta ei löytynyt.");
		}
		else {
			callbacks.updatePosition();
			callbacks.updateDocument();
			callbacks.updateTotalRow();
		}
	}

	/**
	 * Returns search enabled state.
	 */
	public boolean isSearchEnabled() {
		return searchEnabled;
	}
}
