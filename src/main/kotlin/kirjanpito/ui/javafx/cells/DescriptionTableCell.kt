package kirjanpito.ui.javafx.cells

import javafx.scene.control.*
import javafx.scene.input.KeyCode
import javafx.util.Callback
import kirjanpito.ui.javafx.EntryRowModel

/**
 * Editoitava taulukkosolu selitteelle.
 */
class DescriptionTableCell : TableCell<EntryRowModel, String>() {
    
    companion object {
        fun forTableColumn(): Callback<TableColumn<EntryRowModel, String>, TableCell<EntryRowModel, String>> {
            return Callback { DescriptionTableCell() }
        }
    }
    
    private var textField: TextField? = null
    
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
            setOnAction { commitEdit(text) }
            
            // Keyboard handling
            setOnKeyPressed { e ->
                when (e.code) {
                    KeyCode.ESCAPE -> cancelEdit()
                    KeyCode.TAB -> commitEdit(text)
                    else -> {}
                }
            }
            
            // Focus lost = commit
            focusedProperty().addListener { _, wasFocused, isNowFocused ->
                if (!isNowFocused && isEditing) {
                    commitEdit(text)
                }
            }
        }
    }
}
