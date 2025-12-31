package kirjanpito.ui.javafx;

import javafx.beans.property.*;
import kirjanpito.db.Account;
import kirjanpito.db.EntryTemplate;

import java.math.BigDecimal;
import java.text.DecimalFormat;

/**
 * JavaFX Properties -pohjainen malli vientimallin riville TableView:ssä.
 */
public class EntryTemplateRowModel {
    private final ObjectProperty<EntryTemplate> template;
    private final IntegerProperty number;
    private final StringProperty name;
    private final ObjectProperty<Account> account;
    private final StringProperty accountName;
    private final ObjectProperty<BigDecimal> debit;
    private final ObjectProperty<BigDecimal> credit;
    private final StringProperty debitFormatted;
    private final StringProperty creditFormatted;
    private final StringProperty description;
    private final DecimalFormat currencyFormat;

    public EntryTemplateRowModel(EntryTemplate template, Account account, DecimalFormat format) {
        this.template = new SimpleObjectProperty<>(template);
        this.currencyFormat = format;

        this.number = new SimpleIntegerProperty(template != null ? template.getNumber() : 1);
        this.name = new SimpleStringProperty(template != null ? template.getName() : "");
        this.account = new SimpleObjectProperty<>(account);
        this.accountName = new SimpleStringProperty(account != null ? account.getNumber() + " " + account.getName() : "");
        this.description = new SimpleStringProperty(template != null ? template.getDescription() : "");

        if (template != null) {
            this.debit = new SimpleObjectProperty<>(template.isDebit() ? template.getAmount() : null);
            this.credit = new SimpleObjectProperty<>(!template.isDebit() ? template.getAmount() : null);
        } else {
            this.debit = new SimpleObjectProperty<>(null);
            this.credit = new SimpleObjectProperty<>(null);
        }

        this.debitFormatted = new SimpleStringProperty(formatAmount(this.debit.get()));
        this.creditFormatted = new SimpleStringProperty(formatAmount(this.credit.get()));

        // Listen for changes
        this.debit.addListener((obs, oldVal, newVal) -> {
            debitFormatted.set(formatAmount(newVal));
            if (this.template.get() != null) {
                this.template.get().setDebit(newVal != null);
                this.template.get().setAmount(newVal != null ? newVal : BigDecimal.ZERO);
            }
        });

        this.credit.addListener((obs, oldVal, newVal) -> {
            creditFormatted.set(formatAmount(newVal));
            if (this.template.get() != null) {
                this.template.get().setDebit(newVal == null);
                this.template.get().setAmount(newVal != null ? newVal : BigDecimal.ZERO);
            }
        });

        this.account.addListener((obs, oldVal, newVal) -> {
            if (newVal != null && this.template.get() != null) {
                this.template.get().setAccountId(newVal.getId());
                accountName.set(newVal.getNumber() + " " + newVal.getName());
            } else if (this.template.get() != null) {
                this.template.get().setAccountId(-1);
                accountName.set("");
            }
        });

        this.number.addListener((obs, oldVal, newVal) -> {
            if (this.template.get() != null) {
                this.template.get().setNumber(newVal.intValue());
            }
        });

        this.name.addListener((obs, oldVal, newVal) -> {
            if (this.template.get() != null) {
                this.template.get().setName(newVal);
            }
        });

        this.description.addListener((obs, oldVal, newVal) -> {
            if (this.template.get() != null) {
                this.template.get().setDescription(newVal);
            }
        });
    }

    private String formatAmount(BigDecimal amount) {
        return amount != null && amount.compareTo(BigDecimal.ZERO) != 0 ? currencyFormat.format(amount) : "";
    }

    // Getters for properties
    public ObjectProperty<EntryTemplate> templateProperty() { return template; }
    public IntegerProperty numberProperty() { return number; }
    public StringProperty nameProperty() { return name; }
    public ObjectProperty<Account> accountProperty() { return account; }
    public StringProperty accountNameProperty() { return accountName; }
    public ObjectProperty<BigDecimal> debitProperty() { return debit; }
    public ObjectProperty<BigDecimal> creditProperty() { return credit; }
    public StringProperty debitFormattedProperty() { return debitFormatted; }
    public StringProperty creditFormattedProperty() { return creditFormatted; }
    public StringProperty descriptionProperty() { return description; }

    // Getters for values
    public EntryTemplate getTemplate() { return template.get(); }
    public int getNumber() { return number.get(); }
    public String getName() { return name.get(); }
    public Account getAccount() { return account.get(); }
    public String getAccountName() { return accountName.get(); }
    public BigDecimal getDebit() { return debit.get(); }
    public BigDecimal getCredit() { return credit.get(); }
    public String getDebitFormatted() { return debitFormatted.get(); }
    public String getCreditFormatted() { return creditFormatted.get(); }
    public String getDescription() { return description.get(); }

    // Setters
    public void setNumber(int number) { this.number.set(number); }
    public void setName(String name) { this.name.set(name); }
    public void setAccount(Account account) { this.account.set(account); }
    public void setDebit(BigDecimal debit) { this.debit.set(debit); }
    public void setCredit(BigDecimal credit) { this.credit.set(credit); }
    public void setDescription(String description) { this.description.set(description); }
}
