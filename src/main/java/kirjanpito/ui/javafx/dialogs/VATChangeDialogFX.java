package kirjanpito.ui.javafx.dialogs;

import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.StringConverter;

import kirjanpito.db.Account;
import kirjanpito.db.DataAccessException;
import kirjanpito.models.VATChangeModel;
import kirjanpito.models.VATChangeWorker;
import kirjanpito.util.Registry;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.concurrent.ExecutionException;

/**
 * JavaFX-dialogi ALV-kantojen muutoksille.
 * Mahdollistaa ALV-prosenttien päivityksen tileille (esim. 23% -> 24%).
 */
public class VATChangeDialogFX {

    private final Stage dialog;
    private final Registry registry;
    private final VATChangeModel model;
    
    private TableView<RuleRow> ruleTable;
    private ObservableList<RuleRow> ruleRows;
    private Button doChangesButton;

    public VATChangeDialogFX(Window owner, Registry registry) {
        this.registry = registry;
        this.model = new VATChangeModel(registry);
        
        dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(owner);
        dialog.setTitle("ALV-kantojen muutokset");
        dialog.setMinWidth(700);
        dialog.setMinHeight(400);
        
        createContent();
        loadRules();
    }

    private void createContent() {
        VBox root = new VBox(16);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #ffffff;");

        // Header
        Label headerLabel = new Label("ALV-kantojen muutokset");
        headerLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        
        Label descLabel = new Label("Määritä säännöt ALV-prosenttien muuttamiseksi. Ohjelma luo vanhoille ALV-prosenteille uudet tilit.");
        descLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666666;");
        descLabel.setWrapText(true);

        // Table
        ruleTable = new TableView<>();
        ruleTable.setEditable(true);
        ruleRows = FXCollections.observableArrayList();
        ruleTable.setItems(ruleRows);
        VBox.setVgrow(ruleTable, Priority.ALWAYS);

        // Account column
        TableColumn<RuleRow, String> accountCol = new TableColumn<>("Tili");
        accountCol.setCellValueFactory(data -> data.getValue().accountProperty());
        accountCol.setCellFactory(col -> new AccountCell());
        accountCol.setPrefWidth(200);
        accountCol.setEditable(true);

        // Old rate column
        TableColumn<RuleRow, BigDecimal> oldRateCol = new TableColumn<>("Vanha %");
        oldRateCol.setCellValueFactory(data -> data.getValue().oldRateProperty());
        oldRateCol.setCellFactory(col -> new PercentCell());
        oldRateCol.setPrefWidth(100);
        oldRateCol.setEditable(true);
        oldRateCol.setOnEditCommit(event -> {
            event.getRowValue().setOldRate(event.getNewValue());
            int idx = ruleTable.getItems().indexOf(event.getRowValue());
            if (idx >= 0) {
                model.setOldVatRate(idx, event.getNewValue());
            }
        });

        // New rate column
        TableColumn<RuleRow, BigDecimal> newRateCol = new TableColumn<>("Uusi %");
        newRateCol.setCellValueFactory(data -> data.getValue().newRateProperty());
        newRateCol.setCellFactory(col -> new PercentCell());
        newRateCol.setPrefWidth(100);
        newRateCol.setEditable(true);
        newRateCol.setOnEditCommit(event -> {
            event.getRowValue().setNewRate(event.getNewValue());
            int idx = ruleTable.getItems().indexOf(event.getRowValue());
            if (idx >= 0) {
                model.setNewVatRate(idx, event.getNewValue());
            }
        });

        // Description column
        TableColumn<RuleRow, String> descCol = new TableColumn<>("Kuvaus");
        descCol.setCellValueFactory(data -> data.getValue().descriptionProperty());
        descCol.setPrefWidth(280);

        ruleTable.getColumns().addAll(accountCol, oldRateCol, newRateCol, descCol);

        // Button panel on right side
        VBox buttonPanel = new VBox(8);
        buttonPanel.setAlignment(Pos.TOP_CENTER);
        
        Button addButton = new Button("+");
        addButton.setTooltip(new Tooltip("Lisää muutos"));
        addButton.setPrefWidth(40);
        addButton.setOnAction(e -> addRule());
        
        Button removeButton = new Button("-");
        removeButton.setTooltip(new Tooltip("Poista muutos"));
        removeButton.setPrefWidth(40);
        removeButton.setOnAction(e -> removeSelectedRule());
        
        buttonPanel.getChildren().addAll(addButton, removeButton);

        // Table with buttons
        HBox tableBox = new HBox(10);
        tableBox.getChildren().addAll(ruleTable, buttonPanel);
        HBox.setHgrow(ruleTable, Priority.ALWAYS);
        VBox.setVgrow(tableBox, Priority.ALWAYS);

        // Bottom buttons
        HBox bottomButtons = new HBox(10);
        bottomButtons.setAlignment(Pos.CENTER_RIGHT);
        bottomButtons.setPadding(new Insets(10, 0, 0, 0));

        Button closeButton = new Button("Sulje");
        closeButton.setCancelButton(true);
        closeButton.setOnAction(e -> dialog.close());
        closeButton.setPrefWidth(100);

        doChangesButton = new Button("Tee muutokset");
        doChangesButton.setDefaultButton(true);
        doChangesButton.setOnAction(e -> doChanges());
        doChangesButton.setPrefWidth(120);

        bottomButtons.getChildren().addAll(closeButton, doChangesButton);

        root.getChildren().addAll(headerLabel, descLabel, tableBox, bottomButtons);

        Scene scene = new Scene(root, 750, 450);
        dialog.setScene(scene);
    }

    private void loadRules() {
        model.addDefaultRules();
        refreshTable();
    }

    private void refreshTable() {
        ruleRows.clear();
        for (int i = 0; i < model.getRuleCount(); i++) {
            Account account = model.getAccount(i);
            String accountStr = account != null ? account.getNumber() + " " + account.getName() : "(Kaikki tilit)";
            BigDecimal oldRate = model.getOldVatRate(i);
            BigDecimal newRate = model.getNewVatRate(i);
            
            String desc = String.format("ALV %s %% → %s %%", 
                formatPercent(oldRate), formatPercent(newRate));
            
            ruleRows.add(new RuleRow(accountStr, oldRate, newRate, desc));
        }
    }

    private String formatPercent(BigDecimal value) {
        if (value == null) return "0";
        DecimalFormat df = new DecimalFormat("0.##");
        return df.format(value);
    }

    private void addRule() {
        model.addRule();
        refreshTable();
        ruleTable.getSelectionModel().selectLast();
    }

    private void removeSelectedRule() {
        int index = ruleTable.getSelectionModel().getSelectedIndex();
        if (index >= 0) {
            model.removeRule(index);
            refreshTable();
        }
    }

    private void doChanges() {
        model.sortRules();
        refreshTable();
        
        // Disable UI during operation
        doChangesButton.setDisable(true);
        doChangesButton.setText("Käsitellään...");
        
        // Use SwingWorker with PropertyChangeListener
        VATChangeWorker vatWorker = new VATChangeWorker(registry, model);
        vatWorker.addPropertyChangeListener(evt -> {
            if ("state".equals(evt.getPropertyName()) && 
                evt.getNewValue() == javax.swing.SwingWorker.StateValue.DONE) {
                Platform.runLater(() -> {
                    doChangesButton.setDisable(false);
                    doChangesButton.setText("Tee muutokset");
                    
                    try {
                        int changes = vatWorker.get();
                        if (changes == 0) {
                            showInfo("Tilikarttaan ei tehty muutoksia.");
                        } else {
                            showInfo(String.format("Tilikarttaan lisättiin %d uutta tiliä.", changes));
                            dialog.close();
                        }
                    } catch (Exception e) {
                        showError("Virhe: " + e.getMessage());
                    }
                });
            }
        });
        vatWorker.execute();
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.initOwner(dialog);
        alert.setTitle("Tiedoksi");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.initOwner(dialog);
        alert.setTitle("Virhe");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void show() {
        dialog.showAndWait();
    }

    /**
     * Row data class for the table.
     */
    public static class RuleRow {
        private final SimpleStringProperty account;
        private final SimpleObjectProperty<BigDecimal> oldRate;
        private final SimpleObjectProperty<BigDecimal> newRate;
        private final SimpleStringProperty description;

        public RuleRow(String account, BigDecimal oldRate, BigDecimal newRate, String description) {
            this.account = new SimpleStringProperty(account);
            this.oldRate = new SimpleObjectProperty<>(oldRate);
            this.newRate = new SimpleObjectProperty<>(newRate);
            this.description = new SimpleStringProperty(description);
        }

        public SimpleStringProperty accountProperty() { return account; }
        public SimpleObjectProperty<BigDecimal> oldRateProperty() { return oldRate; }
        public SimpleObjectProperty<BigDecimal> newRateProperty() { return newRate; }
        public SimpleStringProperty descriptionProperty() { return description; }
        
        public void setOldRate(BigDecimal rate) { oldRate.set(rate); updateDescription(); }
        public void setNewRate(BigDecimal rate) { newRate.set(rate); updateDescription(); }
        
        private void updateDescription() {
            DecimalFormat df = new DecimalFormat("0.##");
            description.set(String.format("ALV %s %% → %s %%", 
                df.format(oldRate.get()), df.format(newRate.get())));
        }
    }

    /**
     * Custom cell for account display.
     */
    private class AccountCell extends TableCell<RuleRow, String> {
        @Override
        protected void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setText(null);
            } else {
                setText(item);
            }
        }
    }

    /**
     * Editable cell for percent values.
     */
    private static class PercentCell extends TableCell<RuleRow, BigDecimal> {
        private TextField textField;
        private final DecimalFormat format = new DecimalFormat("0.##");
        
        @Override
        public void startEdit() {
            super.startEdit();
            if (textField == null) {
                createTextField();
            }
            setText(null);
            setGraphic(textField);
            textField.selectAll();
            textField.requestFocus();
        }
        
        @Override
        public void cancelEdit() {
            super.cancelEdit();
            setText(format.format(getItem()));
            setGraphic(null);
        }
        
        @Override
        protected void updateItem(BigDecimal item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setText(null);
                setGraphic(null);
            } else {
                if (isEditing()) {
                    if (textField != null) {
                        textField.setText(format.format(item));
                    }
                    setText(null);
                    setGraphic(textField);
                } else {
                    setText(format.format(item));
                    setGraphic(null);
                }
            }
        }
        
        private void createTextField() {
            textField = new TextField(format.format(getItem()));
            textField.setMinWidth(this.getWidth() - this.getGraphicTextGap() * 2);
            textField.setOnAction(e -> commitEdit(parseValue(textField.getText())));
            textField.focusedProperty().addListener((obs, oldVal, newVal) -> {
                if (!newVal) {
                    commitEdit(parseValue(textField.getText()));
                }
            });
        }
        
        private BigDecimal parseValue(String text) {
            try {
                return new BigDecimal(text.replace(",", "."));
            } catch (NumberFormatException e) {
                return BigDecimal.ZERO;
            }
        }
    }
}
