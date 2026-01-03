package kirjanpito.ui

import kirjanpito.db.DataAccessException
import kirjanpito.models.DocumentModel
import kirjanpito.util.Registry
import java.util.logging.Level
import java.util.logging.Logger
import javax.swing.JOptionPane
import javax.swing.JPanel
import javax.swing.JTextField

/**
 * Handles document navigation functionality for DocumentFrame.
 *
 * This class was extracted from DocumentFrame as part of Phase 8 refactoring
 * to reduce the size of the DocumentFrame god object.
 * Migrated from Java to Kotlin for better code conciseness and null-safety.
 *
 * Responsibilities:
 * - Document navigation (create, delete, go to)
 * - Document search and filtering
 * - Search panel management
 *
 * @author Petri Kivikangas
 * @since 2.2.5
 */
class DocumentNavigator(
    private val registry: Registry,
    private val searchPanel: JPanel,
    private val searchPhraseTextField: JTextField,
    private val callbacks: NavigationCallbacks
) {

    /**
     * Callback interface for DocumentFrame interactions.
     */
    interface NavigationCallbacks {
        /** Returns the DocumentModel instance */
        fun getDocumentModel(): DocumentModel

        /** Saves document if changed, returns false if save fails */
        fun saveDocumentIfChanged(): Boolean

        /** Updates position label */
        fun updatePosition()

        /** Updates document fields */
        fun updateDocument()

        /** Updates total row */
        fun updateTotalRow()

        /** Selects document type menu item */
        fun selectDocumentTypeMenuItem(index: Int)

        /** Returns parent window for dialogs */
        fun getParentWindow(): java.awt.Window

        /** Finds document type by number */
        fun findDocumentTypeByNumber(number: Int): Int

        /** Updates search panel visibility */
        fun updateSearchPanel()
    }

    // State
    var isSearchEnabled: Boolean = false
        private set

    companion object {
        private val logger = Logger.getLogger(Kirjanpito.LOGGER_NAME)
    }

    /**
     * Luo uuden tositteen.
     */
    fun createDocument() {
        if (!callbacks.saveDocumentIfChanged()) {
            return
        }

        try {
            callbacks.getDocumentModel().createDocument()
        } catch (e: DataAccessException) {
            val message = "Uuden tositteen luominen epäonnistui"
            logger.log(Level.SEVERE, message, e)
            SwingUtils.showDataAccessErrorMessage(callbacks.getParentWindow(), e, message)
        }

        callbacks.updatePosition()
        callbacks.updateDocument()
        callbacks.updateTotalRow()
    }

    /**
     * Poistaa valitun tositteen.
     */
    fun deleteDocument() {
        val result = JOptionPane.showConfirmDialog(
            callbacks.getParentWindow(),
            "Haluatko varmasti poistaa valitun tositteen?",
            Kirjanpito.APP_NAME,
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        )

        if (result == JOptionPane.YES_OPTION) {
            try {
                callbacks.getDocumentModel().deleteDocument()
            } catch (e: DataAccessException) {
                val message = "Tositteen poistaminen epäonnistui"
                logger.log(Level.SEVERE, message, e)
                SwingUtils.showDataAccessErrorMessage(callbacks.getParentWindow(), e, message)
            }

            callbacks.updatePosition()
            callbacks.updateDocument()
            callbacks.updateTotalRow()
        }
    }

    /**
     * Siirtyy toiseen tositteeseen. Ennen tietojen
     * hakemista käyttäjän tekemät muutokset tallennetaan.
     *
     * @param index tositteen järjestysnumero
     */
    fun goToDocument(index: Int) {
        if (!callbacks.saveDocumentIfChanged()) {
            return
        }

        try {
            callbacks.getDocumentModel().goToDocument(index)
        } catch (e: DataAccessException) {
            val message = "Tositetietojen hakeminen epäonnistui"
            logger.log(Level.SEVERE, message, e)
            SwingUtils.showDataAccessErrorMessage(callbacks.getParentWindow(), e, message)
        }

        callbacks.updatePosition()
        callbacks.updateDocument()
        callbacks.updateTotalRow()
    }

    /**
     * Kysyy käyttäjältä tositenumeroa, ja siirtyy tähän tositteeseen.
     */
    fun findDocumentByNumber() {
        val model = callbacks.getDocumentModel()
        var valid = false
        var number = -1

        while (!valid) {
            val input = JOptionPane.showInputDialog(
                callbacks.getParentWindow(),
                "Tositenumero?",
                Kirjanpito.APP_NAME,
                JOptionPane.PLAIN_MESSAGE
            )

            if (input != null) {
                number = input.toIntOrNull() ?: -1
                valid = number > 0

                if (!valid) {
                    JOptionPane.showMessageDialog(
                        callbacks.getParentWindow(),
                        "Tositenumero saa sisältää vain numeroita.",
                        Kirjanpito.APP_NAME,
                        JOptionPane.ERROR_MESSAGE
                    )
                }
            } else {
                return
            }
        }

        val documentTypeIndex = callbacks.findDocumentTypeByNumber(number)

        val index = try {
            model.findDocumentByNumber(documentTypeIndex, number)
        } catch (e: DataAccessException) {
            val message = "Tositetietojen hakeminen epäonnistui"
            logger.log(Level.SEVERE, message, e)
            SwingUtils.showDataAccessErrorMessage(callbacks.getParentWindow(), e, message)
            return
        }

        if (index < 0) {
            SwingUtils.showInformationMessage(
                callbacks.getParentWindow(),
                "Tositetta ei löytynyt numerolla $number."
            )
            return
        }

        var invalidDocuments = false

        if (isSearchEnabled) {
            isSearchEnabled = false
            invalidDocuments = true
            searchPanel.isVisible = false
        }

        if (model.documentTypeIndex != documentTypeIndex) {
            callbacks.selectDocumentTypeMenuItem(documentTypeIndex)
            model.documentTypeIndex = documentTypeIndex
            invalidDocuments = true
        }

        /* Tositetiedot on haettava tietokannasta, jos tositelaji on muuttunut
         * tai haku on kytketty pois päältä. */
        if (invalidDocuments) {
            try {
                model.fetchDocuments(index)
            } catch (e: DataAccessException) {
                val message = "Tositetietojen hakeminen epäonnistui"
                logger.log(Level.SEVERE, message, e)
                SwingUtils.showDataAccessErrorMessage(callbacks.getParentWindow(), e, message)
                return
            }

            callbacks.updatePosition()
            callbacks.updateDocument()
            callbacks.updateTotalRow()
        } else {
            goToDocument(index)
        }
    }

    /**
     * Kytkee tositteiden haun päälle tai pois päältä.
     */
    fun toggleSearchPanel() {
        if (!callbacks.saveDocumentIfChanged()) {
            return
        }

        isSearchEnabled = !isSearchEnabled
        searchPanel.isVisible = isSearchEnabled

        if (!isSearchEnabled) {
            /* Kun haku kytketään pois päältä, haetaan valitun
             * tositelajin kaikki tositteet.
             */
            try {
                callbacks.getDocumentModel().fetchDocuments(-1)
            } catch (e: DataAccessException) {
                val message = "Tositteiden hakeminen epäonnistui"
                logger.log(Level.SEVERE, message, e)
                SwingUtils.showDataAccessErrorMessage(callbacks.getParentWindow(), e, message)
                return
            }

            callbacks.updatePosition()
            callbacks.updateDocument()
            callbacks.updateTotalRow()
        }
    }

    /**
     * Etsii tositteita käyttäjän antamalla hakusanalla.
     */
    fun searchDocuments() {
        if (!callbacks.saveDocumentIfChanged()) {
            return
        }

        val count = try {
            callbacks.getDocumentModel().search(searchPhraseTextField.text)
        } catch (e: DataAccessException) {
            val message = "Tositteiden hakeminen epäonnistui"
            logger.log(Level.SEVERE, message, e)
            SwingUtils.showDataAccessErrorMessage(callbacks.getParentWindow(), e, message)
            return
        }

        if (count == 0) {
            SwingUtils.showInformationMessage(
                callbacks.getParentWindow(),
                "Yhtään tositetta ei löytynyt."
            )
        } else {
            callbacks.updatePosition()
            callbacks.updateDocument()
            callbacks.updateTotalRow()
        }
    }
}
