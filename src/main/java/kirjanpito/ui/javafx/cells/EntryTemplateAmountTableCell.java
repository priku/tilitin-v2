package kirjanpito.ui.javafx.cells;

import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.util.Callback;
import kirjanpito.ui.javafx.EntryTemplateRowModel;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.util.Locale;

/**
 * Editoitava taulukkosolu rahasummille EntryTemplateRowModelille.
 */
public class EntryTemplateAmountTableCell extends TableCell<EntryTemplateRowModel, BigDecimal> {
    
    private TextField textField;
    private final DecimalFormat currencyFormat;
    private static final DecimalFormat PARSE_FORMAT;
    
    static {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(new Locale("fi", "FI"));
        PARSE_FORMAT = new DecimalFormat("#,##0.00", symbols);
        PARSE_FORMAT.setParseBigDecimal(true);
    }
    
    public EntryTemplateAmountTableCell(DecimalFormat format) {
        this.currencyFormat = format;
        setStyle("-fx-alignment: CENTER-RIGHT;");
    }
    
    public static Callback<TableColumn<EntryTemplateRowModel, BigDecimal>, TableCell<EntryTemplateRowModel, BigDecimal>> forTableColumn(DecimalFormat format) {
        return param -> new EntryTemplateAmountTableCell(format);
    }
    
    @Override
    public void startEdit() {
        if (!isEmpty()) {
            super.startEdit();
            createTextField();
            setText(null);
            setGraphic(textField);
            textField.requestFocus();
            textField.selectAll();
        }
    }
    
    @Override
    public void cancelEdit() {
        super.cancelEdit();
        setText(formatAmount(getItem()));
        setGraphic(null);
    }
    
    @Override
    public void updateItem(BigDecimal item, boolean empty) {
        super.updateItem(item, empty);
        
        if (empty) {
            setText(null);
            setGraphic(null);
        } else {
            if (isEditing()) {
                if (textField != null) {
                    textField.setText(formatAmount(item));
                }
                setText(null);
                setGraphic(textField);
            } else {
                setText(formatAmount(item));
                setGraphic(null);
            }
        }
    }
    
    private void createTextField() {
        textField = new TextField(formatAmount(getItem()));
        textField.setStyle("-fx-alignment: CENTER-RIGHT;");
        
        textField.setOnAction(e -> commitAmount());
        
        textField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ESCAPE) {
                cancelEdit();
            } else if (e.getCode() == KeyCode.TAB || e.getCode() == KeyCode.ENTER) {
                commitAmount();
            }
        });
        
        textField.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (!isNowFocused && isEditing()) {
                commitAmount();
            }
        });
    }
    
    private void commitAmount() {
        BigDecimal value = parseAmount(textField.getText());
        commitEdit(value);
    }
    
    private BigDecimal parseAmount(String text) {
        if (text == null || text.trim().isEmpty()) {
            return null;
        }
        
        try {
            String normalized = text.trim().replace(" ", "").replace(",", ".");
            return new BigDecimal(normalized);
        } catch (NumberFormatException e) {
            try {
                Number parsed = PARSE_FORMAT.parse(text.trim());
                return parsed instanceof BigDecimal ? (BigDecimal) parsed : new BigDecimal(parsed.toString());
            } catch (ParseException pe) {
                return getItem();
            }
        }
    }
    
    private String formatAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) == 0) {
            return "";
        }
        return currencyFormat.format(amount);
    }
}
