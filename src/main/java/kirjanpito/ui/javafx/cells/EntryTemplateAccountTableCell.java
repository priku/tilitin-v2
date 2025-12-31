package kirjanpito.ui.javafx.cells;

import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.util.Callback;
import javafx.util.StringConverter;
import kirjanpito.db.Account;
import kirjanpito.ui.javafx.EntryTemplateRowModel;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Editoitava taulukkosolu tilikartan valintaan EntryTemplateRowModelille.
 */
public class EntryTemplateAccountTableCell extends TableCell<EntryTemplateRowModel, Account> {
    
    private ComboBox<Account> comboBox;
    private final List<Account> allAccounts;
    
    public EntryTemplateAccountTableCell(List<Account> accounts) {
        this.allAccounts = accounts;
    }
    
    public static Callback<TableColumn<EntryTemplateRowModel, Account>, TableCell<EntryTemplateRowModel, Account>> forTableColumn(List<Account> accounts) {
        return param -> new EntryTemplateAccountTableCell(accounts);
    }
    
    @Override
    public void startEdit() {
        if (!isEmpty()) {
            super.startEdit();
            createComboBox();
            setText(null);
            setGraphic(comboBox);
            comboBox.requestFocus();
            comboBox.getEditor().selectAll();
        }
    }
    
    @Override
    public void cancelEdit() {
        super.cancelEdit();
        setText(getDisplayText(getItem()));
        setGraphic(null);
    }
    
    @Override
    public void updateItem(Account item, boolean empty) {
        super.updateItem(item, empty);
        
        if (empty) {
            setText(null);
            setGraphic(null);
        } else {
            if (isEditing()) {
                if (comboBox != null) {
                    comboBox.setValue(item);
                }
                setText(null);
                setGraphic(comboBox);
            } else {
                setText(getDisplayText(item));
                setGraphic(null);
            }
        }
    }
    
    private void createComboBox() {
        comboBox = new ComboBox<>();
        comboBox.setEditable(true);
        comboBox.setMaxWidth(Double.MAX_VALUE);
        comboBox.getItems().addAll(allAccounts);
        comboBox.setValue(getItem());
        
        comboBox.setConverter(new StringConverter<Account>() {
            @Override
            public String toString(Account account) {
                return account != null ? account.getNumber() + " " + account.getName() : "";
            }
            
            @Override
            public Account fromString(String string) {
                if (string == null || string.isEmpty()) return null;
                
                String search = string.trim().toLowerCase();
                
                for (Account acc : allAccounts) {
                    if (acc.getNumber().equals(search) || acc.getNumber().startsWith(search)) {
                        return acc;
                    }
                }
                
                for (Account acc : allAccounts) {
                    if (acc.getName().toLowerCase().contains(search)) {
                        return acc;
                    }
                }
                
                return getItem();
            }
        });
        
        comboBox.getEditor().textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.isEmpty()) {
                String search = newVal.toLowerCase();
                List<Account> filtered = allAccounts.stream()
                    .filter(a -> a.getNumber().startsWith(search) || 
                                a.getName().toLowerCase().contains(search))
                    .collect(Collectors.toList());
                
                if (!filtered.isEmpty() && !comboBox.isShowing()) {
                    comboBox.show();
                }
            }
        });
        
        comboBox.setOnAction(e -> {
            Account selected = comboBox.getValue();
            if (selected != null) {
                commitEdit(selected);
            }
        });
        
        comboBox.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ESCAPE) {
                cancelEdit();
            } else if (e.getCode() == KeyCode.TAB || e.getCode() == KeyCode.ENTER) {
                Account selected = comboBox.getConverter().fromString(comboBox.getEditor().getText());
                if (selected != null) {
                    commitEdit(selected);
                }
            }
        });
    }
    
    private String getDisplayText(Account account) {
        if (account == null) return "";
        return account.getNumber() + " " + account.getName();
    }
}
