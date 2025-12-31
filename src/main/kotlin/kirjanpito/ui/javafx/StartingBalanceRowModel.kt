package kirjanpito.ui.javafx

import javafx.beans.property.*
import kirjanpito.db.Account
import java.math.BigDecimal
import java.text.DecimalFormat

/**
 * JavaFX Properties -pohjainen malli alkusaldo-riville TableView:ssä.
 */
class StartingBalanceRowModel(
    account: Account?,
    balance: BigDecimal?,
    format: DecimalFormat
) {
    private val accountProperty = SimpleObjectProperty(account)
    private val accountNumber = SimpleStringProperty(account?.number ?: "")
    private val accountName = SimpleStringProperty(account?.name ?: "")
    private val balanceProperty = SimpleObjectProperty<BigDecimal>(balance ?: BigDecimal.ZERO)
    private val balanceFormatted = SimpleStringProperty(formatAmount(balanceProperty.get()))
    private val currencyFormat = format

    init {
        // Listen for balance changes
        balanceProperty.addListener { _, _, newVal ->
            balanceFormatted.set(formatAmount(newVal))
        }
    }

    private fun formatAmount(amount: BigDecimal?): String {
        return if (amount == null || amount.compareTo(BigDecimal.ZERO) == 0) {
            ""
        } else {
            currencyFormat.format(amount)
        }
    }

    // Getters for properties
    fun accountProperty() = accountProperty
    fun accountNumberProperty() = accountNumber
    fun accountNameProperty() = accountName
    fun balanceProperty() = balanceProperty
    fun balanceFormattedProperty() = balanceFormatted

    // Getters for values
    fun getAccount() = accountProperty.get()
    fun getAccountNumber() = accountNumber.get()
    fun getAccountName() = accountName.get()
    fun getBalance() = balanceProperty.get()
    fun getBalanceFormatted() = balanceFormatted.get()

    // Setters
    fun setBalance(balance: BigDecimal?) {
        balanceProperty.set(balance ?: BigDecimal.ZERO)
    }
}
