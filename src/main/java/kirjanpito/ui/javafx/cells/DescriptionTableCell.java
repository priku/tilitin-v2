package kirjanpito.ui.javafx.cells;

import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import kirjanpito.ui.javafx.EntryRowModel;

/**
 * Editoitava taulukkosolu selitteelle.
 */
public class DescriptionTableCell extends TableCell<EntryRowModel, String> {
    
    private TextField textField;
    
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
        setText(getItem());
        setGraphic(null);
    }
    
    @Override
    public void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);
        
        if (empty) {
            setText(null);
            setGraphic(null);
        } else {
            if (isEditing()) {
                if (textField != null) {
                    textField.setText(item);
                }
                setText(null);
                setGraphic(textField);
            } else {
                setText(item);
                setGraphic(null);
            }
        }
    }
    
    private void createTextField() {
        textField = new TextField(getItem());
        
        // Commit on Enter
        textField.setOnAction(e -> commitEdit(textField.getText()));
        
        // Keyboard handling
        textField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ESCAPE) {
                cancelEdit();
            } else if (e.getCode() == KeyCode.TAB) {
                commitEdit(textField.getText());
            }
        });
        
        // Focus lost = commit
        textField.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (!isNowFocused && isEditing()) {
                commitEdit(textField.getText());
            }
        });
    }
}
