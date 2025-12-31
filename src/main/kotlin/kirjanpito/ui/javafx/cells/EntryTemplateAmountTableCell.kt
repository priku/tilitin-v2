package kirjanpito.ui.javafx.cells

import javafx.scene.control.*
import javafx.scene.input.KeyCode
import javafx.util.Callback
import kirjanpito.ui.javafx.EntryTemplateRowModel
import java.math.BigDecimal
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

/**
 * Editoitava taulukkosolu rahasummille EntryTemplateRowModelille.
 */
class EntryTemplateAmountTableCell(
    private val currencyFormat: DecimalFormat
) : TableCell<EntryTemplateRowModel, BigDecimal>() {
    
    private var textField: TextField? = null
    
    companion object {
        private val PARSE_FORMAT = DecimalFormat("#,##0.00", DecimalFormatSymbols(Locale("fi", "FI"))).apply {
            isParseBigDecimal = true
        }

        @JvmStatic
        fun forTableColumn(format: DecimalFormat): Callback<TableColumn<EntryTemplateRowModel, BigDecimal>, TableCell<EntryTemplateRowModel, BigDecimal>> {
            return Callback { EntryTemplateAmountTableCell(format) }
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
        text = formatAmount(item)
        graphic = null
    }
    
    override fun updateItem(item: BigDecimal?, empty: Boolean) {
        super.updateItem(item, empty)
        
        if (empty) {
            text = null
            graphic = null
        } else {
            if (isEditing) {
                textField?.text = formatAmount(item)
                text = null
                graphic = textField
            } else {
                text = formatAmount(item)
                graphic = null
            }
        }
    }
    
    private fun createTextField() {
        textField = TextField(formatAmount(item)).apply {
            style = "-fx-alignment: CENTER-RIGHT;"
            
            setOnAction { commitAmount() }
            
            setOnKeyPressed { e ->
                when (e.code) {
                    KeyCode.ESCAPE -> cancelEdit()
                    KeyCode.TAB, KeyCode.ENTER -> commitAmount()
                    else -> {}
                }
            }
            
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
            val normalized = text.trim().replace(" ", "").replace(",", ".")
            BigDecimal(normalized)
        } catch (e: NumberFormatException) {
            try {
                val parsed = PARSE_FORMAT.parse(text.trim())
                when (parsed) {
                    is BigDecimal -> parsed
                    else -> BigDecimal(parsed.toString())
                }
            } catch (pe: Exception) {
                item
            }
        }
    }
    
    private fun formatAmount(amount: BigDecimal?): String {
        return if (amount == null || amount.compareTo(BigDecimal.ZERO) == 0) {
            ""
        } else {
            currencyFormat.format(amount)
        }
    }
}
