package kirjanpito.ui.javafx.cells

import javafx.scene.control.*
import javafx.scene.input.KeyCode
import javafx.util.StringConverter
import kirjanpito.db.Account
import kirjanpito.ui.javafx.EntryRowModel

/**
 * Editoitava taulukkosolu tilikartan valintaan.
 * Tukee autocomplete-hakua tilinumerolla tai nimellä.
 */
class AccountTableCell(
    private val allAccounts: List<Account>
) : TableCell<EntryRowModel, Account>() {
    
    private var comboBox: ComboBox<Account>? = null
    
    override fun startEdit() {
        if (!isEmpty) {
            super.startEdit()
            createComboBox()
            text = null
            graphic = comboBox
            comboBox?.requestFocus()
            comboBox?.editor?.selectAll()
        }
    }
    
    override fun cancelEdit() {
        super.cancelEdit()
        text = getDisplayText(item)
        graphic = null
    }
    
    override fun updateItem(item: Account?, empty: Boolean) {
        super.updateItem(item, empty)
        
        if (empty) {
            text = null
            graphic = null
        } else {
            if (isEditing) {
                comboBox?.value = item
                text = null
                graphic = comboBox
            } else {
                text = getDisplayText(item)
                graphic = null
            }
        }
    }
    
    private fun createComboBox() {
        comboBox = ComboBox<Account>().apply {
            isEditable = true
            maxWidth = Double.MAX_VALUE
            items.addAll(allAccounts)
            value = item
            
            // Converter for display
            converter = object : StringConverter<Account>() {
                override fun toString(account: Account?): String {
                    return account?.let { "${it.number} ${it.name}" } ?: ""
                }
                
                override fun fromString(string: String?): Account? {
                    if (string.isNullOrEmpty()) return null
                    
                    val search = string.trim().lowercase()
                    
                    // Exact number match
                    allAccounts.firstOrNull { acc ->
                        acc.number == search || acc.number.startsWith(search)
                    }?.let { return it }
                    
                    // Name search
                    allAccounts.firstOrNull { acc ->
                        acc.name.lowercase().contains(search)
                    }?.let { return it }
                    
                    return item // Keep current if not found
                }
            }
            
            // Filter on text input
            editor.textProperty().addListener { _, _, newVal ->
                if (!newVal.isNullOrEmpty()) {
                    val search = newVal.lowercase()
                    val filtered = allAccounts.filter { acc ->
                        acc.number.startsWith(search) || 
                        acc.name.lowercase().contains(search)
                    }
                    
                    if (filtered.isNotEmpty() && !isShowing) {
                        show()
                    }
                }
            }
            
            // Commit on selection
            setOnAction {
                value?.let { commitEdit(it) }
            }
            
            // Keyboard handling
            setOnKeyPressed { e ->
                when (e.code) {
                    KeyCode.ESCAPE -> cancelEdit()
                    KeyCode.TAB, KeyCode.ENTER -> {
                        converter.fromString(editor.text)?.let { commitEdit(it) }
                    }
                    else -> {}
                }
            }
        }
    }
    
    private fun getDisplayText(account: Account?): String {
        return account?.let { "${it.number} ${it.name}" } ?: ""
    }
}
