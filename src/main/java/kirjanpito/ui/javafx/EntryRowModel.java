package kirjanpito.ui.javafx;

import javafx.beans.property.*;
import kirjanpito.db.Account;
import kirjanpito.db.Entry;

import java.math.BigDecimal;
import java.text.DecimalFormat;

/**
 * JavaFX-yhteensopiva Entry-rivi TableView:lle.
 * Käyttää JavaFX propertyja editoinnin mahdollistamiseksi.
 */
public class EntryRowModel {
    
    private final IntegerProperty rowNumber = new SimpleIntegerProperty();
    private final ObjectProperty<Account> account = new SimpleObjectProperty<>();
    private final StringProperty description = new SimpleStringProperty();
    private final ObjectProperty<BigDecimal> debit = new SimpleObjectProperty<>();
    private final ObjectProperty<BigDecimal> credit = new SimpleObjectProperty<>();
    private final StringProperty vatCode = new SimpleStringProperty();
    
    // Original entry for saving
    private Entry originalEntry;
    private boolean modified = false;
    
    private static final DecimalFormat CURRENCY_FORMAT = new DecimalFormat("#,##0.00");
    
    public EntryRowModel() {
        this.originalEntry = new Entry();
    }
    
    public EntryRowModel(int rowNum, Entry entry, Account account) {
        this.rowNumber.set(rowNum);
        this.originalEntry = entry;
        this.account.set(account);
        this.description.set(entry != null ? entry.getDescription() : "");
        
        if (entry != null) {
            if (entry.isDebit()) {
                this.debit.set(entry.getAmount());
                this.credit.set(null);
            } else {
                this.debit.set(null);
                this.credit.set(entry.getAmount());
            }
        }
        
        // Aseta ALV tilin perusteella
        updateVatFromAccount();
    }
    
    // Row number
    public int getRowNumber() { return rowNumber.get(); }
    public void setRowNumber(int value) { rowNumber.set(value); }
    public IntegerProperty rowNumberProperty() { return rowNumber; }
    
    // Account
    public Account getAccount() { return account.get(); }
    public void setAccount(Account value) { 
        account.set(value);
        if (originalEntry != null && value != null) {
            originalEntry.setAccountId(value.getId());
        }
        updateVatFromAccount();
        modified = true;
    }
    public ObjectProperty<Account> accountProperty() { return account; }
    
    public String getAccountDisplay() {
        Account acc = account.get();
        if (acc == null) return "";
        return acc.getNumber() + " " + acc.getName();
    }
    
    // Description
    public String getDescription() { return description.get(); }
    public void setDescription(String value) { 
        description.set(value);
        if (originalEntry != null) {
            originalEntry.setDescription(value);
        }
        modified = true;
    }
    public StringProperty descriptionProperty() { return description; }
    
    // Debit
    public BigDecimal getDebit() { return debit.get(); }
    public void setDebit(BigDecimal value) { 
        debit.set(value);
        if (value != null && value.compareTo(BigDecimal.ZERO) != 0) {
            credit.set(null); // Clear credit if debit is set
            if (originalEntry != null) {
                originalEntry.setDebit(true);
                originalEntry.setAmount(value);
            }
        }
        modified = true;
    }
    public ObjectProperty<BigDecimal> debitProperty() { return debit; }
    
    public String getDebitFormatted() {
        BigDecimal d = debit.get();
        return d != null && d.compareTo(BigDecimal.ZERO) != 0 ? CURRENCY_FORMAT.format(d) : "";
    }
    
    // Credit
    public BigDecimal getCredit() { return credit.get(); }
    public void setCredit(BigDecimal value) { 
        credit.set(value);
        if (value != null && value.compareTo(BigDecimal.ZERO) != 0) {
            debit.set(null); // Clear debit if credit is set
            if (originalEntry != null) {
                originalEntry.setDebit(false);
                originalEntry.setAmount(value);
            }
        }
        modified = true;
    }
    public ObjectProperty<BigDecimal> creditProperty() { return credit; }
    
    public String getCreditFormatted() {
        BigDecimal c = credit.get();
        return c != null && c.compareTo(BigDecimal.ZERO) != 0 ? CURRENCY_FORMAT.format(c) : "";
    }
    
    // VAT
    public String getVatCode() { return vatCode.get(); }
    public void setVatCode(String value) { vatCode.set(value); }
    public StringProperty vatCodeProperty() { return vatCode; }
    
    public void updateVatFromAccount() {
        Account acc = account.get();
        if (acc != null && acc.getVatRate() != null) {
            java.math.BigDecimal rate = acc.getVatRate();
            if (rate.compareTo(java.math.BigDecimal.ZERO) > 0) {
                vatCode.set(rate.stripTrailingZeros().toPlainString() + "%");
            } else {
                vatCode.set("");
            }
        } else {
            vatCode.set("");
        }
    }
    
    // Entry access
    public Entry getEntry() { return originalEntry; }
    public void setEntry(Entry entry) { this.originalEntry = entry; }
    
    public boolean isModified() { return modified; }
    public void setModified(boolean value) { this.modified = value; }
    
    public boolean isEmpty() {
        return account.get() == null && 
               (description.get() == null || description.get().isEmpty()) &&
               debit.get() == null && 
               credit.get() == null;
    }
    
    /**
     * Päivittää Entry-olion tiedot tämän rivin tiedoilla.
     */
    public void updateEntry() {
        if (originalEntry == null) {
            originalEntry = new Entry();
        }
        
        originalEntry.setRowNumber(rowNumber.get());
        originalEntry.setDescription(description.get());
        
        Account acc = account.get();
        if (acc != null) {
            originalEntry.setAccountId(acc.getId());
        }
        
        BigDecimal d = debit.get();
        BigDecimal c = credit.get();
        
        if (d != null && d.compareTo(BigDecimal.ZERO) != 0) {
            originalEntry.setDebit(true);
            originalEntry.setAmount(d);
        } else if (c != null && c.compareTo(BigDecimal.ZERO) != 0) {
            originalEntry.setDebit(false);
            originalEntry.setAmount(c);
        } else {
            originalEntry.setAmount(BigDecimal.ZERO);
        }
    }
}
