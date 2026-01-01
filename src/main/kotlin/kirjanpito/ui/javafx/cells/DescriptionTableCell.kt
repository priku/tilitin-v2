package kirjanpito.ui.javafx.cells

import javafx.application.Platform
import javafx.geometry.Bounds
import javafx.scene.control.*
import javafx.scene.input.KeyCode
import javafx.scene.layout.VBox
import javafx.stage.Popup
import javafx.util.Callback
import kirjanpito.ui.javafx.EntryRowModel
import kirjanpito.util.AutoCompleteSupport

/**
 * Editoitava taulukkosolu selitteelle.
 * 
 * Sisältää auto-complete toiminnallisuuden:
 * - Ehdottaa selitteitä kirjoitettaessa (vähintään 2 merkkiä)
 * - F12 / Ctrl+Backspace poistaa selitteen päätteen
 * - Käyttää AutoCompleteSupport-rajapintaa ehdotusten hakemiseen
 */
class DescriptionTableCell(
    private val autoCompleteSupport: AutoCompleteSupport? = null,
    private val accountIdProvider: ((Int) -> Int)? = null
) : TableCell<EntryRowModel, String>() {
    
    companion object {
        fun forTableColumn(): Callback<TableColumn<EntryRowModel, String>, TableCell<EntryRowModel, String>> {
            return Callback { DescriptionTableCell() }
        }
        
        fun forTableColumn(
            autoCompleteSupport: AutoCompleteSupport?,
            accountIdProvider: ((Int) -> Int)?
        ): Callback<TableColumn<EntryRowModel, String>, TableCell<EntryRowModel, String>> {
            return Callback { DescriptionTableCell(autoCompleteSupport, accountIdProvider) }
        }
    }
    
    private var textField: TextField? = null
    private var popup: Popup? = null
    private var suggestionList: ListView<String>? = null
    private var lastAutoCompletion: String? = null
    
    override fun startEdit() {
        if (!isEmpty) {
            super.startEdit()
            createTextField()
            text = null
            graphic = textField
            textField?.requestFocus()
            textField?.selectAll()
        }
    }
    
    override fun cancelEdit() {
        super.cancelEdit()
        text = item
        graphic = null
        hidePopup()
    }
    
    override fun updateItem(item: String?, empty: Boolean) {
        super.updateItem(item, empty)
        
        if (empty) {
            text = null
            graphic = null
        } else {
            if (isEditing) {
                textField?.text = item
                text = null
                graphic = textField
            } else {
                text = item
                graphic = null
            }
        }
    }
    
    private fun createTextField() {
        textField = TextField(item).apply {
            // Commit on Enter
            setOnAction { 
                hidePopup()
                commitEdit(text) 
            }
            
            // Keyboard handling
            setOnKeyPressed { e ->
                when (e.code) {
                    KeyCode.ESCAPE -> {
                        hidePopup()
                        cancelEdit()
                    }
                    KeyCode.TAB -> {
                        hidePopup()
                        commitEdit(text)
                    }
                    KeyCode.F12 -> {
                        removeSuffix()
                        e.consume()
                    }
                    KeyCode.BACK_SPACE -> {
                        if (e.isControlDown) {
                            removeSuffix()
                            e.consume()
                        }
                    }
                    KeyCode.DOWN -> {
                        // Jos popup on auki, siirry ehdotuksiin
                        if (popup?.isShowing == true) {
                            suggestionList?.requestFocus()
                            suggestionList?.selectionModel?.selectFirst()
                            e.consume()
                        }
                    }
                    else -> {}
                }
            }
            
            // Auto-complete on key release
            setOnKeyReleased { e ->
                if (Character.isLetterOrDigit(e.text.firstOrNull() ?: ' ') || 
                    e.code == KeyCode.SPACE) {
                    handleAutoComplete()
                }
            }
            
            // Focus lost = commit
            focusedProperty().addListener { _, _, isNowFocused ->
                if (!isNowFocused && isEditing) {
                    hidePopup()
                    commitEdit(text)
                }
            }
        }
    }
    
    /**
     * Käsittele auto-complete.
     */
    private fun handleAutoComplete() {
        val text = textField?.text ?: return
        val caretPos = textField?.caretPosition ?: return
        
        // Vähintään 2 merkkiä vaaditaan
        if (caretPos < 2) {
            hidePopup()
            return
        }
        
        // Hae tili-id nykyiseltä riviltä
        val accountId = getAccountIdForCurrentRow()
        
        // Hae auto-complete ehdotus
        val suggestion = autoCompleteSupport?.autoCompleteEntryDescription(accountId, text)
        
        if (suggestion != null && suggestion != text && 
            suggestion.regionMatches(0, text, 0, text.length, ignoreCase = true)) {
            // Aseta ehdotus tekstikenttään
            lastAutoCompletion = suggestion
            textField?.text = suggestion
            textField?.positionCaret(suggestion.length)
            textField?.selectRange(caretPos, suggestion.length)
        } else {
            lastAutoCompletion = null
        }
    }
    
    /**
     * Hae nykyisen rivin tili-id.
     */
    private fun getAccountIdForCurrentRow(): Int {
        // Kokeile ensin custom provideria
        accountIdProvider?.let { provider ->
            return provider(index)
        }
        
        // Muuten hae rivimallista
        val rowModel = tableView?.items?.getOrNull(index)
        return rowModel?.getAccount()?.id ?: -1
    }
    
    /**
     * Poista selitteestä pääte (pilkun jälkeinen osa).
     */
    private fun removeSuffix() {
        val text = textField?.text ?: return
        val pos = text.lastIndexOf(',')
        
        val newText = if (pos < 0) {
            // Jos pilkkua ei löydy, tyhjennä
            ""
        } else {
            val trimmed = text.trim()
            val notWhitespace = pos + 1 < text.length && text[pos + 1] != ' '
            val prefix = text.substring(0, pos)
            
            if (trimmed.isNotEmpty() && trimmed.last() != ',') {
                prefix + if (notWhitespace) "," else ", "
            } else {
                prefix
            }
        }
        
        textField?.text = newText
        textField?.positionCaret(newText.length)
    }
    
    /**
     * Piilota ehdotus-popup.
     */
    private fun hidePopup() {
        popup?.hide()
    }
}
