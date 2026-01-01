package kirjanpito.ui.javafx.cells

import javafx.scene.control.*
import javafx.scene.input.KeyCode
import kirjanpito.ui.javafx.EntryRowModel
import java.math.BigDecimal
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

/**
 * Editoitava taulukkosolu rahasummille.
 * Tukee suomalaista numeromuotoa (pilkku desimaalierottimena).
 */
class AmountTableCell(
    private val isDebit: Boolean
) : TableCell<EntryRowModel, BigDecimal>() {
    
    private var textField: TextField? = null
    
    companion object {
        private val DISPLAY_FORMAT = DecimalFormat("#,##0.00")
        private val PARSE_FORMAT = DecimalFormat("#,##0.00", DecimalFormatSymbols(Locale("fi", "FI"))).apply {
            isParseBigDecimal = true
        }
    }
    
    init {
        style = "-fx-alignment: CENTER-RIGHT;"
    }
    
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
        text = getDisplayText(item)
        graphic = null
    }
    
    override fun updateItem(item: BigDecimal?, empty: Boolean) {
        super.updateItem(item, empty)
        
        if (empty) {
            text = null
            graphic = null
        } else {
            if (isEditing) {
                textField?.text = getEditText(item)
                text = null
                graphic = textField
            } else {
                text = getDisplayText(item)
                graphic = null
                
                // Color coding
                if (item != null && item.compareTo(BigDecimal.ZERO) != 0) {
                    style = if (isDebit) {
                        "-fx-alignment: CENTER-RIGHT; -fx-text-fill: #059669;"
                    } else {
                        "-fx-alignment: CENTER-RIGHT; -fx-text-fill: #dc2626;"
                    }
                } else {
                    style = "-fx-alignment: CENTER-RIGHT; -fx-text-fill: #94a3b8;"
                }
            }
        }
    }
    
    private fun createTextField() {
        textField = TextField(getEditText(item)).apply {
            style = "-fx-alignment: CENTER-RIGHT;"
            
            // Commit on Enter
            setOnAction { commitAmount() }
            
            // Keyboard handling
            setOnKeyPressed { e ->
                when (e.code) {
                    KeyCode.ESCAPE -> cancelEdit()
                    KeyCode.TAB -> {
                        commitAmount()
                        // Tab handling delegated to EntryTableNavigationHandler
                    }
                    KeyCode.MULTIPLY -> {
                        // Asterisk key - allow parent handler to process
                        // Don't consume - let it bubble up
                    }
                    else -> {}
                }
            }
            
            // Handle asterisk typed as character
            setOnKeyTyped { e ->
                if (e.character == "*") {
                    // Don't add asterisk to text field
                    e.consume()
                    // Commit current value and let navigation handler toggle
                    commitAmount()
                }
            }
            
            // Focus lost = commit
            focusedProperty().addListener { _, _, isNowFocused ->
                if (!isNowFocused && isEditing) {
                    commitAmount()
                }
            }
        }
    }
    
    private fun commitAmount() {
        val value = parseAmount(textField?.text)
        commitEdit(value)
    }
    
    private fun parseAmount(text: String?): BigDecimal? {
        if (text.isNullOrBlank()) {
            return null
        }
        
        return try {
            // Replace comma with dot for parsing, handle Finnish format
            val normalized = text.trim()
                .replace(" ", "")
                .replace(",", ".")
            
            BigDecimal(normalized)
        } catch (e: NumberFormatException) {
            // Try Finnish locale parsing
            try {
                val parsed = PARSE_FORMAT.parse(text.trim())
                when (parsed) {
                    is BigDecimal -> parsed
                    else -> BigDecimal(parsed.toString())
                }
            } catch (pe: Exception) {
                item // Keep current value
            }
        }
    }
    
    private fun getDisplayText(amount: BigDecimal?): String {
        return if (amount == null || amount.compareTo(BigDecimal.ZERO) == 0) {
            ""
        } else {
            DISPLAY_FORMAT.format(amount)
        }
    }
    
    private fun getEditText(amount: BigDecimal?): String {
        return if (amount == null || amount.compareTo(BigDecimal.ZERO) == 0) {
            ""
        } else {
            DISPLAY_FORMAT.format(amount)
        }
    }
}
