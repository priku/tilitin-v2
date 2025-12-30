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
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import kirjanpito.db.Account;

import java.util.List;
import java.util.function.Consumer;

/**
 * JavaFX tilinvalintadialogi (F9).
 * 
 * Näyttää tilikartan ja mahdollistaa tilin valinnan hakusanalla.
 */
public class AccountSelectionDialogFX {
    
    private Stage dialog;
    private TextField searchField;
    private TableView<Account> accountTable;
    private ObservableList<Account> allAccounts;
    private FilteredList<Account> filteredAccounts;
    private Consumer<Account> onAccountSelected;
    private Account selectedAccount;
    private CheckBox showFavoritesOnly;
    
    public AccountSelectionDialogFX(Window owner) {
        dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(owner);
        dialog.setTitle("Tilin valinta");
        dialog.setMinWidth(500);
        dialog.setMinHeight(400);
        
        createContent();
    }
    
    private void createContent() {
        VBox root = new VBox(10);
        root.setPadding(new Insets(16));
        root.setStyle("-fx-background-color: #ffffff;");
        
        // Search field
        HBox searchBox = new HBox(10);
        searchBox.setAlignment(Pos.CENTER_LEFT);
        
        Label searchLabel = new Label("Haku:");
        searchField = new TextField();
        searchField.setPromptText("Tilinumero tai nimi...");
        searchField.setPrefWidth(300);
        HBox.setHgrow(searchField, Priority.ALWAYS);
        
        showFavoritesOnly = new CheckBox("Vain suosikit");
        
        searchBox.getChildren().addAll(searchLabel, searchField, showFavoritesOnly);
        
        // Table
        accountTable = new TableView<>();
        accountTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(accountTable, Priority.ALWAYS);
        
        TableColumn<Account, String> numberCol = new TableColumn<>("Numero");
        numberCol.setCellValueFactory(new PropertyValueFactory<>("number"));
        numberCol.setPrefWidth(80);
        numberCol.setMinWidth(60);
        
        TableColumn<Account, String> nameCol = new TableColumn<>("Nimi");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setPrefWidth(300);
        
        TableColumn<Account, String> typeCol = new TableColumn<>("Tyyppi");
        typeCol.setCellValueFactory(cellData -> {
            Account acc = cellData.getValue();
            String type = getAccountTypeName(acc.getType());
            return new javafx.beans.property.SimpleStringProperty(type);
        });
        typeCol.setPrefWidth(100);
        
        accountTable.getColumns().addAll(numberCol, nameCol, typeCol);
        
        // Buttons
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.setPadding(new Insets(10, 0, 0, 0));
        
        Button okButton = new Button("Valitse");
        okButton.setDefaultButton(true);
        okButton.setStyle("-fx-background-color: #2563eb; -fx-text-fill: white;");
        okButton.setOnAction(e -> selectAndClose());
        
        Button cancelButton = new Button("Peruuta");
        cancelButton.setCancelButton(true);
        cancelButton.setOnAction(e -> dialog.close());
        
        buttonBox.getChildren().addAll(cancelButton, okButton);
        
        root.getChildren().addAll(searchBox, accountTable, buttonBox);
        
        // Event handlers
        setupEventHandlers();
        
        Scene scene = new Scene(root, 550, 450);
        dialog.setScene(scene);
    }
    
    private void setupEventHandlers() {
        // Search filter
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            filterAccounts(newVal);
        });
        
        // Favorites filter
        showFavoritesOnly.selectedProperty().addListener((obs, oldVal, newVal) -> {
            filterAccounts(searchField.getText());
        });
        
        // Double-click to select
        accountTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                selectAndClose();
            }
        });
        
        // Enter to select
        accountTable.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                selectAndClose();
            } else if (event.getCode() == KeyCode.ESCAPE) {
                dialog.close();
            }
        });
        
        // Search field Enter moves to table
        searchField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER || event.getCode() == KeyCode.DOWN) {
                if (!filteredAccounts.isEmpty()) {
                    accountTable.getSelectionModel().selectFirst();
                    accountTable.requestFocus();
                }
            } else if (event.getCode() == KeyCode.ESCAPE) {
                dialog.close();
            }
        });
    }
    
    private void filterAccounts(String search) {
        if (filteredAccounts == null) return;
        
        final String searchLower = search != null ? search.toLowerCase().trim() : "";
        final boolean favoritesOnly = showFavoritesOnly.isSelected();
        
        filteredAccounts.setPredicate(account -> {
            // Favorites filter (flag bit 0 = favorite)
            if (favoritesOnly && (account.getFlags() & 1) == 0) {
                return false;
            }
            
            // Search filter
            if (searchLower.isEmpty()) {
                return true;
            }
            
            // Match by number prefix
            if (account.getNumber().startsWith(searchLower)) {
                return true;
            }
            
            // Match by name
            if (account.getName().toLowerCase().contains(searchLower)) {
                return true;
            }
            
            return false;
        });
        
        // Select first match
        if (!filteredAccounts.isEmpty()) {
            accountTable.getSelectionModel().selectFirst();
        }
    }
    
    private void selectAndClose() {
        selectedAccount = accountTable.getSelectionModel().getSelectedItem();
        if (selectedAccount != null && onAccountSelected != null) {
            onAccountSelected.accept(selectedAccount);
        }
        dialog.close();
    }
    
    private String getAccountTypeName(int type) {
        switch (type) {
            case 1: return "Vastaavaa";
            case 2: return "Vastattavaa";
            case 3: return "Tulos";
            case 4: return "Oma pääoma";
            case 5: return "Edellisten voitto";
            default: return "";
        }
    }
    
    public void setAccounts(List<Account> accounts) {
        allAccounts = FXCollections.observableArrayList(accounts);
        filteredAccounts = new FilteredList<>(allAccounts, p -> true);
        accountTable.setItems(filteredAccounts);
    }
    
    public void setOnAccountSelected(Consumer<Account> callback) {
        this.onAccountSelected = callback;
    }
    
    public void setInitialSearch(String search) {
        if (search != null && !search.isEmpty()) {
            searchField.setText(search);
            filterAccounts(search);
        }
    }
    
    public Account getSelectedAccount() {
        return selectedAccount;
    }
    
    public void show() {
        // Focus search field
        Platform.runLater(() -> {
            searchField.requestFocus();
            searchField.selectAll();
        });
        
        dialog.showAndWait();
    }
    
    /**
     * Näyttää dialogin ja palauttaa valitun tilin.
     */
    public static Account showAndSelect(Window owner, List<Account> accounts, String initialSearch) {
        AccountSelectionDialogFX dialog = new AccountSelectionDialogFX(owner);
        dialog.setAccounts(accounts);
        if (initialSearch != null) {
            dialog.setInitialSearch(initialSearch);
        }
        dialog.show();
        return dialog.getSelectedAccount();
    }
}
