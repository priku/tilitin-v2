package kirjanpito.ui.javafx

import javafx.beans.property.*
import kirjanpito.db.Account
import kirjanpito.db.EntryTemplate
import java.math.BigDecimal
import java.text.DecimalFormat

/**
 * JavaFX Properties -pohjainen malli vientimallin riville TableView:ssä.
 */
class EntryTemplateRowModel(
    template: EntryTemplate?,
    account: Account?,
    format: DecimalFormat
) {
    private val templateProperty = SimpleObjectProperty(template)
    private val number = SimpleIntegerProperty(template?.number ?: 1)
    private val name = SimpleStringProperty(template?.name ?: "")
    private val accountProperty = SimpleObjectProperty(account)
    private val accountName = SimpleStringProperty(
        account?.let { "${it.number} ${it.name}" } ?: ""
    )
    private val debit = SimpleObjectProperty<BigDecimal>(
        template?.takeIf { it.isDebit }?.amount
    )
    private val credit = SimpleObjectProperty<BigDecimal>(
        template?.takeIf { !it.isDebit }?.amount
    )
    private val debitFormatted = SimpleStringProperty(formatAmount(debit.get()))
    private val creditFormatted = SimpleStringProperty(formatAmount(credit.get()))
    private val description = SimpleStringProperty(template?.description ?: "")
    private val currencyFormat = format

    init {
        // Listen for changes
        debit.addListener { _, _, newVal ->
            debitFormatted.set(formatAmount(newVal))
            templateProperty.get()?.let { t ->
                t.setDebit(newVal != null)
                t.amount = newVal ?: BigDecimal.ZERO
            }
        }

        credit.addListener { _, _, newVal ->
            creditFormatted.set(formatAmount(newVal))
            templateProperty.get()?.let { t ->
                t.setDebit(newVal == null)
                t.amount = newVal ?: BigDecimal.ZERO
            }
        }

        accountProperty.addListener { _, _, newVal ->
            templateProperty.get()?.let { t ->
                if (newVal != null) {
                    t.accountId = newVal.id
                    accountName.set("${newVal.number} ${newVal.name}")
                } else {
                    t.accountId = -1
                    accountName.set("")
                }
            }
        }

        number.addListener { _, _, newVal ->
            templateProperty.get()?.number = newVal.toInt()
        }

        name.addListener { _, _, newVal ->
            templateProperty.get()?.name = newVal
        }

        description.addListener { _, _, newVal ->
            templateProperty.get()?.description = newVal
        }
    }

    private fun formatAmount(amount: BigDecimal?): String {
        return if (amount != null && amount.compareTo(BigDecimal.ZERO) != 0) {
            currencyFormat.format(amount)
        } else {
            ""
        }
    }

    // Getters for properties
    fun templateProperty() = templateProperty
    fun numberProperty() = number
    fun nameProperty() = name
    fun accountProperty() = accountProperty
    fun accountNameProperty() = accountName
    fun debitProperty() = debit
    fun creditProperty() = credit
    fun debitFormattedProperty() = debitFormatted
    fun creditFormattedProperty() = creditFormatted
    fun descriptionProperty() = description

    // Getters for values
    fun getTemplate() = templateProperty.get()
    fun getNumber() = number.get()
    fun getName() = name.get()
    fun getAccount() = accountProperty.get()
    fun getAccountName() = accountName.get()
    fun getDebit() = debit.get()
    fun getCredit() = credit.get()
    fun getDebitFormatted() = debitFormatted.get()
    fun getCreditFormatted() = creditFormatted.get()
    fun getDescription() = description.get()

    // Setters
    fun setNumber(number: Int) = this.number.set(number)
    fun setName(name: String) = this.name.set(name)
    fun setAccount(account: Account?) = accountProperty.set(account)
    fun setDebit(debit: BigDecimal?) = this.debit.set(debit)
    fun setCredit(credit: BigDecimal?) = this.credit.set(credit)
    fun setDescription(description: String) = this.description.set(description)
}
