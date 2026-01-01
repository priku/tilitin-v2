package kirjanpito.ui.javafx

import javafx.beans.property.*
import kirjanpito.db.Account
import kirjanpito.db.Entry
import java.math.BigDecimal
import java.text.DecimalFormat

/**
 * JavaFX-yhteensopiva Entry-rivi TableView:lle.
 * Käyttää JavaFX propertyja editoinnin mahdollistamiseksi.
 */
class EntryRowModel(
    rowNum: Int = 0,
    entry: Entry? = null,
    account: Account? = null
) {
    private val rowNumber = SimpleIntegerProperty(rowNum)
    private val accountProperty = SimpleObjectProperty<Account>(account)
    private val description = SimpleStringProperty(entry?.getDescription() ?: "")
    private val debit = SimpleObjectProperty<BigDecimal>()
    private val credit = SimpleObjectProperty<BigDecimal>()
    private val vatCode = SimpleStringProperty("")
    
    // Original entry for saving
    var originalEntry: Entry? = entry ?: Entry()
        private set
    private var modified = false
    
    companion object {
        private val CURRENCY_FORMAT = DecimalFormat("#,##0.00")
    }
    
    init {
        entry?.let {
            if (it.isDebit()) {
                debit.set(it.getAmount())
                credit.set(null)
            } else {
                debit.set(null)
                credit.set(it.getAmount())
            }
        }
        
        // Aseta ALV tilin perusteella
        updateVatFromAccount()
    }
    
    // Row number
    fun getRowNumber() = rowNumber.get()
    fun setRowNumber(value: Int) = rowNumber.set(value)
    fun rowNumberProperty() = rowNumber
    
    // Account
    fun getAccount() = accountProperty.get()
    fun setAccount(value: Account?) {
        accountProperty.set(value)
        originalEntry?.let {
            value?.let { acc ->
                it.setAccountId(acc.id)
            }
        }
        updateVatFromAccount()
        modified = true
    }
    fun accountProperty() = accountProperty
    
    fun getAccountDisplay(): String {
        val acc = accountProperty.get()
        return acc?.let { "${it.number} ${it.name}" } ?: ""
    }
    
    // Description
    fun getDescription() = description.get()
    fun setDescription(value: String) {
        description.set(value)
        originalEntry?.setDescription(value)
        modified = true
    }
    fun descriptionProperty() = description
    
    // Debit
    fun getDebit() = debit.get()
    fun setDebit(value: BigDecimal?) {
        debit.set(value)
        if (value != null && value.compareTo(BigDecimal.ZERO) != 0) {
            credit.set(null) // Clear credit if debit is set
            originalEntry?.let {
                it.setDebit(true)
                it.setAmount(value)
            }
        }
        modified = true
    }
    fun debitProperty() = debit
    
    fun getDebitFormatted(): String {
        val d = debit.get()
        return if (d != null && d.compareTo(BigDecimal.ZERO) != 0) {
            CURRENCY_FORMAT.format(d)
        } else {
            ""
        }
    }
    
    // Credit
    fun getCredit() = credit.get()
    fun setCredit(value: BigDecimal?) {
        credit.set(value)
        if (value != null && value.compareTo(BigDecimal.ZERO) != 0) {
            debit.set(null) // Clear debit if credit is set
            originalEntry?.let {
                it.setDebit(false)
                it.setAmount(value)
            }
        }
        modified = true
    }
    fun creditProperty() = credit
    
    fun getCreditFormatted(): String {
        val c = credit.get()
        return if (c != null && c.compareTo(BigDecimal.ZERO) != 0) {
            CURRENCY_FORMAT.format(c)
        } else {
            ""
        }
    }
    
    // VAT
    fun getVatCode() = vatCode.get()
    fun setVatCode(value: String) = vatCode.set(value)
    fun vatCodeProperty() = vatCode
    
    fun updateVatFromAccount() {
        val acc = accountProperty.get()
        val rate = acc?.vatRate
        vatCode.set(
            if (rate != null && rate.compareTo(BigDecimal.ZERO) > 0) {
                "${rate.stripTrailingZeros().toPlainString()}%"
            } else {
                ""
            }
        )
    }
    
    // Entry access
    fun getEntry() = originalEntry
    fun setEntry(entry: Entry?) {
        this.originalEntry = entry ?: Entry()
    }
    
    fun isEmpty(): Boolean {
        return accountProperty.get() == null &&
               (description.get() == null || description.get().isEmpty()) &&
               debit.get() == null &&
               credit.get() == null
    }

    // Modified flag
    fun isModified() = modified
    fun setModified(value: Boolean) {
        modified = value
    }
    
    /**
     * Päivittää Entry-olion tiedot tämän rivin tiedoilla.
     */
    fun updateEntry() {
        val entry = originalEntry ?: Entry().also { originalEntry = it }
        
        entry.setRowNumber(rowNumber.get())
        entry.setDescription(description.get())
        
        accountProperty.get()?.let {
            entry.setAccountId(it.id)
        }
        
        val d = debit.get()
        val c = credit.get()
        
        when {
            d != null && d.compareTo(BigDecimal.ZERO) != 0 -> {
                entry.setDebit(true)
                entry.setAmount(d)
            }
            c != null && c.compareTo(BigDecimal.ZERO) != 0 -> {
                entry.setDebit(false)
                entry.setAmount(c)
            }
            else -> {
                entry.setAmount(BigDecimal.ZERO)
            }
        }
    }
    
    /**
     * Vaihda debet ja kredit keskenään.
     * Jos debetissä on arvo, siirretään se kreditiin ja päinvastoin.
     */
    fun toggleDebitCredit() {
        val d = debit.get()
        val c = credit.get()
        
        when {
            d != null && d.compareTo(BigDecimal.ZERO) != 0 -> {
                // Siirry debet → kredit
                credit.set(d)
                debit.set(null)
                originalEntry?.let {
                    it.setDebit(false)
                    it.setAmount(d)
                }
            }
            c != null && c.compareTo(BigDecimal.ZERO) != 0 -> {
                // Siirry kredit → debet
                debit.set(c)
                credit.set(null)
                originalEntry?.let {
                    it.setDebit(true)
                    it.setAmount(c)
                }
            }
            else -> {
                // Ei arvoa, ei vaihdeta
            }
        }
        modified = true
    }
    
    /**
     * Tarkista onko vienti debet-puolella.
     */
    fun isDebit(): Boolean {
        val d = debit.get()
        return d != null && d.compareTo(BigDecimal.ZERO) != 0
    }
}
