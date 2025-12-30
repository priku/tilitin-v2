package kirjanpito.ui.javafx.dialogs;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.converter.BigDecimalStringConverter;

import kirjanpito.db.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * JavaFX tilikartan muokkausdialogi.
 */
public class COADialogFX {
    
    private Stage dialog;
    private TableView<Account> accountTable;
    private ObservableList<Account> accounts;
    private FilteredList<Account> filteredAccounts;
    private TextField searchField;
    
    private DataSource dataSource;
    private Consumer<Void> onSave;
    private boolean modified = false;
    private List<Account> deletedAccounts = new ArrayList<>();
    
    public COADialogFX(Window owner, DataSource dataSource) {
        this.dataSource = dataSource;
        
        dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(owner);
        dialog.setTitle("Tilikartta");
        dialog.setMinWidth(700);
        dialog.setMinHeight(500);
        
        createContent();
    }
    
    private void createContent() {
        VBox root = new VBox(10);
        root.setPadding(new Insets(16));
        root.setStyle("-fx-background-color: #ffffff;");
        
        // Toolbar
        HBox toolbar = new HBox(10);
        toolbar.setAlignment(Pos.CENTER_LEFT);
        
        Button addBtn = new Button("+ Lisää tili");
        addBtn.setStyle("-fx-background-color: #2563eb; -fx-text-fill: white;");
        addBtn.setOnAction(e -> addAccount());
        
        Button removeBtn = new Button("Poista");
        removeBtn.setOnAction(e -> removeAccount());
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        searchField = new TextField();
        searchField.setPromptText("Hae tilejä...");
        searchField.setPrefWidth(200);
        searchField.textProperty().addListener((obs, o, n) -> filterAccounts(n));
        
        toolbar.getChildren().addAll(addBtn, removeBtn, spacer, searchField);
        
        // Table
        accountTable = new TableView<>();
        accountTable.setEditable(true);
        VBox.setVgrow(accountTable, Priority.ALWAYS);
        
        TableColumn<Account, String> numberCol = new TableColumn<>("Numero");
        numberCol.setCellValueFactory(new PropertyValueFactory<>("number"));
        numberCol.setCellFactory(TextFieldTableCell.forTableColumn());
        numberCol.setOnEditCommit(e -> {
            e.getRowValue().setNumber(e.getNewValue());
            modified = true;
        });
        numberCol.setPrefWidth(80);
        
        TableColumn<Account, String> nameCol = new TableColumn<>("Nimi");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setCellFactory(TextFieldTableCell.forTableColumn());
        nameCol.setOnEditCommit(e -> {
            e.getRowValue().setName(e.getNewValue());
            modified = true;
        });
        nameCol.setPrefWidth(250);
        
        TableColumn<Account, String> typeCol = new TableColumn<>("Tyyppi");
        typeCol.setCellValueFactory(cellData -> {
            int type = cellData.getValue().getType();
            return new javafx.beans.property.SimpleStringProperty(getTypeName(type));
        });
        typeCol.setPrefWidth(100);
        
        TableColumn<Account, BigDecimal> vatCol = new TableColumn<>("ALV %");
        vatCol.setCellValueFactory(new PropertyValueFactory<>("vatRate"));
        vatCol.setCellFactory(col -> new TableCell<Account, BigDecimal>() {
            private TextField textField;
            
            @Override
            protected void updateItem(BigDecimal item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null || item.compareTo(BigDecimal.ZERO) == 0) {
                    setText("");
                } else {
                    setText(item.stripTrailingZeros().toPlainString() + "%");
                }
            }
        });
        vatCol.setPrefWidth(70);
        
        TableColumn<Account, Boolean> favCol = new TableColumn<>("★");
        favCol.setCellValueFactory(cellData -> {
            boolean isFav = (cellData.getValue().getFlags() & 1) != 0;
            return new javafx.beans.property.SimpleBooleanProperty(isFav);
        });
        favCol.setCellFactory(col -> new TableCell<Account, Boolean>() {
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText("");
                } else {
                    setText(item ? "★" : "");
                    setStyle("-fx-alignment: CENTER; -fx-text-fill: #f59e0b;");
                }
            }
        });
        favCol.setPrefWidth(40);
        
        accountTable.getColumns().addAll(numberCol, nameCol, typeCol, vatCol, favCol);
        
        // Toggle favorite on double-click
        accountTable.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                Account selected = accountTable.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    int flags = selected.getFlags();
                    selected.setFlags(flags ^ 1); // Toggle bit 0
                    accountTable.refresh();
                    modified = true;
                }
            }
        });
        
        // Buttons
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.setPadding(new Insets(10, 0, 0, 0));
        
        Button saveBtn = new Button("Tallenna");
        saveBtn.setDefaultButton(true);
        saveBtn.setStyle("-fx-background-color: #2563eb; -fx-text-fill: white;");
        saveBtn.setOnAction(e -> save());
        
        Button cancelBtn = new Button("Peruuta");
        cancelBtn.setCancelButton(true);
        cancelBtn.setOnAction(e -> dialog.close());
        
        buttonBox.getChildren().addAll(cancelBtn, saveBtn);
        
        root.getChildren().addAll(toolbar, accountTable, buttonBox);
        
        Scene scene = new Scene(root, 750, 550);
        dialog.setScene(scene);
    }
    
    private void filterAccounts(String search) {
        if (filteredAccounts == null) return;
        
        String s = search != null ? search.toLowerCase().trim() : "";
        filteredAccounts.setPredicate(acc -> {
            if (s.isEmpty()) return true;
            return acc.getNumber().startsWith(s) || 
                   acc.getName().toLowerCase().contains(s);
        });
    }
    
    private void addAccount() {
        Account newAcc = new Account();
        newAcc.setNumber("");
        newAcc.setName("Uusi tili");
        newAcc.setType(1);
        newAcc.setVatRate(BigDecimal.ZERO);
        
        accounts.add(newAcc);
        accountTable.getSelectionModel().select(newAcc);
        accountTable.scrollTo(newAcc);
        modified = true;
    }
    
    private void removeAccount() {
        Account selected = accountTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Poista tili");
        confirm.setHeaderText("Poista tili " + selected.getNumber() + "?");
        confirm.setContentText("HUOM: Tilin poistaminen voi aiheuttaa ongelmia jos siihen on vientejä.");
        
        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            if (selected.getId() > 0) {
                deletedAccounts.add(selected);
            }
            accounts.remove(selected);
            modified = true;
        }
    }
    
    private void save() {
        if (!modified) {
            dialog.close();
            return;
        }
        
        Session session = null;
        try {
            session = dataSource.openSession();
            AccountDAO dao = dataSource.getAccountDAO(session);
            
            // Delete removed accounts
            for (Account acc : deletedAccounts) {
                dao.delete(acc.getId());
            }
            
            // Save all accounts
            for (Account acc : accounts) {
                dao.save(acc);
            }
            
            session.commit();
            
            if (onSave != null) {
                onSave.accept(null);
            }
            
            dialog.close();
            
        } catch (Exception e) {
            if (session != null) {
                try { session.rollback(); } catch (Exception re) {}
            }
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Virhe");
            alert.setHeaderText("Tallennusvirhe");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }
    
    private String getTypeName(int type) {
        switch (type) {
            case 1: return "Vastaavaa";
            case 2: return "Vastattavaa";
            case 3: return "Tulos";
            case 4: return "Oma pääoma";
            case 5: return "Edellisten voitto";
            default: return "";
        }
    }
    
    public void setAccounts(List<Account> accountList) {
        accounts = FXCollections.observableArrayList(accountList);
        filteredAccounts = new FilteredList<>(accounts, p -> true);
        accountTable.setItems(filteredAccounts);
    }
    
    public void setOnSave(Consumer<Void> callback) {
        this.onSave = callback;
    }
    
    public void show() {
        Platform.runLater(() -> searchField.requestFocus());
        dialog.showAndWait();
    }
}
