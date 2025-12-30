package kirjanpito.ui.javafx.cells;

import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import kirjanpito.ui.javafx.EntryRowModel;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.util.Locale;

/**
 * Editoitava taulukkosolu rahasummille.
 * Tukee suomalaista numeromuotoa (pilkku desimaalierottimena).
 */
public class AmountTableCell extends TableCell<EntryRowModel, BigDecimal> {
    
    private TextField textField;
    private final boolean isDebit;
    private static final DecimalFormat DISPLAY_FORMAT = new DecimalFormat("#,##0.00");
    private static final DecimalFormat PARSE_FORMAT;
    
    static {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(new Locale("fi", "FI"));
        PARSE_FORMAT = new DecimalFormat("#,##0.00", symbols);
        PARSE_FORMAT.setParseBigDecimal(true);
    }
    
    public AmountTableCell(boolean isDebit) {
        this.isDebit = isDebit;
        
        // Style for alignment
        setStyle("-fx-alignment: CENTER-RIGHT;");
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
        setText(getDisplayText(getItem()));
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
                    textField.setText(getEditText(item));
                }
                setText(null);
                setGraphic(textField);
            } else {
                setText(getDisplayText(item));
                setGraphic(null);
                
                // Color coding
                if (item != null && item.compareTo(BigDecimal.ZERO) != 0) {
                    if (isDebit) {
                        setStyle("-fx-alignment: CENTER-RIGHT; -fx-text-fill: #059669;"); // Green
                    } else {
                        setStyle("-fx-alignment: CENTER-RIGHT; -fx-text-fill: #dc2626;"); // Red
                    }
                } else {
                    setStyle("-fx-alignment: CENTER-RIGHT; -fx-text-fill: #94a3b8;"); // Muted
                }
            }
        }
    }
    
    private void createTextField() {
        textField = new TextField(getEditText(getItem()));
        textField.setStyle("-fx-alignment: CENTER-RIGHT;");
        
        // Commit on Enter
        textField.setOnAction(e -> commitAmount());
        
        // Keyboard handling
        textField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ESCAPE) {
                cancelEdit();
            } else if (e.getCode() == KeyCode.TAB) {
                commitAmount();
            }
        });
        
        // Focus lost = commit
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
            // Replace comma with dot for parsing, handle Finnish format
            String normalized = text.trim()
                .replace(" ", "")
                .replace(",", ".");
            
            return new BigDecimal(normalized);
        } catch (NumberFormatException e) {
            // Try Finnish locale parsing
            try {
                Number parsed = PARSE_FORMAT.parse(text.trim());
                return parsed instanceof BigDecimal ? (BigDecimal) parsed : new BigDecimal(parsed.toString());
            } catch (ParseException pe) {
                return getItem(); // Keep current value
            }
        }
    }
    
    private String getDisplayText(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) == 0) {
            return "";
        }
        return DISPLAY_FORMAT.format(amount);
    }
    
    private String getEditText(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) == 0) {
            return "";
        }
        return DISPLAY_FORMAT.format(amount);
    }
}
