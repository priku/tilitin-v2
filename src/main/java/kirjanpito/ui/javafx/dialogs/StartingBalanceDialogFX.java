package kirjanpito.ui.javafx.dialogs;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import kirjanpito.db.*;
import kirjanpito.models.StartingBalanceModel;
import kirjanpito.ui.javafx.StartingBalanceRowModel;
import kirjanpito.ui.javafx.cells.StartingBalanceAmountTableCell;

import java.math.BigDecimal;
import java.text.DecimalFormat;

/**
 * JavaFX alkusaldojen muokkausdialogi.
 */
public class StartingBalanceDialogFX {
    
    private Stage dialog;
    private TableView<StartingBalanceRowModel> table;
    private ObservableList<StartingBalanceRowModel> balances;
    private StartingBalanceModel model;
    private DecimalFormat currencyFormat;
    private Label assetsTotalLabel;
    private Label liabilitiesTotalLabel;
    private boolean modified = false;
    
    public StartingBalanceDialogFX(Window owner, StartingBalanceModel model) {
        this.model = model;
        
        // Currency format
        currencyFormat = new DecimalFormat();
        currencyFormat.setMinimumFractionDigits(2);
        currencyFormat.setMaximumFractionDigits(2);
        
        dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(owner);
        dialog.setTitle("Alkusaldot");
        dialog.setMinWidth(600);
        dialog.setMinHeight(500);
        
        createContent();
        loadBalances();
    }
    
    private void createContent() {
        VBox root = new VBox(10);
        root.setPadding(new Insets(15));
        root.setStyle("-fx-background-color: #ffffff;");
        
        // Toolbar
        HBox toolbar = new HBox(10);
        toolbar.setAlignment(Pos.CENTER_LEFT);
        toolbar.setPadding(new Insets(0, 0, 10, 0));
        
        Button saveBtn = new Button("💾 Tallenna");
        saveBtn.setStyle("-fx-background-color: #10b981; -fx-text-fill: white;");
        saveBtn.setDisable(!model.isEditable());
        saveBtn.setOnAction(e -> save());
        
        Button copyBtn = new Button("📋 Kopioi edellisen tilikauden loppusaldot");
        copyBtn.setDisable(!model.isEditable());
        copyBtn.setOnAction(e -> copyFromPreviousPeriod());
        
        Button closeBtn = new Button("Sulje");
        closeBtn.setOnAction(e -> close());
        
        toolbar.getChildren().addAll(saveBtn, copyBtn, new Separator(), closeBtn);
        
        // Table
        table = new TableView<>();
        table.setEditable(model.isEditable());
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        // Columns
        TableColumn<StartingBalanceRowModel, String> numberCol = new TableColumn<>("Nro");
        numberCol.setCellValueFactory(new PropertyValueFactory<>("accountNumber"));
        numberCol.setPrefWidth(60);
        numberCol.setEditable(false);
        
        TableColumn<StartingBalanceRowModel, String> nameCol = new TableColumn<>("Tili");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("accountName"));
        nameCol.setPrefWidth(400);
        nameCol.setEditable(false);
        
        TableColumn<StartingBalanceRowModel, BigDecimal> balanceCol = new TableColumn<>("Alkusaldo");
        balanceCol.setCellValueFactory(new PropertyValueFactory<>("balance"));
        balanceCol.setCellFactory(StartingBalanceAmountTableCell.forTableColumn(currencyFormat));
        balanceCol.setPrefWidth(80);
        balanceCol.setEditable(model.isEditable());
        
        table.getColumns().addAll(numberCol, nameCol, balanceCol);
        
        // Totals row
        HBox totalsRow = new HBox(20);
        totalsRow.setPadding(new Insets(10, 0, 0, 0));
        totalsRow.setAlignment(Pos.CENTER_LEFT);
        totalsRow.setStyle("-fx-border-color: #e2e8f0 transparent transparent transparent; -fx-border-width: 1 0 0 0; -fx-padding: 10 0 0 0;");
        
        Label assetsLabel = new Label("Vastaavaa yht.:");
        assetsTotalLabel = new Label("0,00");
        assetsTotalLabel.setStyle("-fx-font-weight: 600;");
        
        Label liabilitiesLabel = new Label("Vastattavaa yht.:");
        liabilitiesTotalLabel = new Label("0,00");
        liabilitiesTotalLabel.setStyle("-fx-font-weight: 600;");
        
        totalsRow.getChildren().addAll(assetsLabel, assetsTotalLabel, new Separator(), liabilitiesLabel, liabilitiesTotalLabel);
        
        root.getChildren().addAll(toolbar, table, totalsRow);
        VBox.setVgrow(table, Priority.ALWAYS);
        
        Scene scene = new Scene(root, 600, 500);
        dialog.setScene(scene);
    }
    
    private void loadBalances() {
        balances = FXCollections.observableArrayList();
        
        for (int i = 0; i < model.getAccountCount(); i++) {
            Account account = model.getAccount(i);
            BigDecimal balance = model.getBalance(i);
            balances.add(new StartingBalanceRowModel(account, balance, currencyFormat));
        }
        
        table.setItems(balances);
        updateTotals();
        
        // Listen for balance changes - update model when user edits
        // Note: The cell editor will call commitEdit which updates the row model's balance property
        // This listener will then update the underlying model
        for (int i = 0; i < balances.size(); i++) {
            final int index = i;
            StartingBalanceRowModel row = balances.get(i);
            row.balanceProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null && (oldVal == null || !newVal.equals(oldVal))) {
                    model.setBalance(index, newVal);
                    modified = true;
                    updateTotals();
                }
            });
        }
    }
    
    private void updateTotals() {
        assetsTotalLabel.setText(currencyFormat.format(model.getAssetsTotal()));
        liabilitiesTotalLabel.setText(currencyFormat.format(model.getLiabilitiesTotal()));
    }
    
    private void copyFromPreviousPeriod() {
        try {
            if (!model.copyFromPreviousPeriod()) {
                showInfo("Edellisen tilikauden tietoja ei löytynyt.");
                return;
            }
            
            // Reload balances
            loadBalances();
            modified = true;
            showInfo("Alkusaldot kopioitu edelliseltä tilikaudelta.");
        } catch (DataAccessException e) {
            showError("Tositetietojen hakeminen epäonnistui: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void save() {
        try {
            model.save();
            modified = false;
            showInfo("Alkusaldot tallennettu.");
            dialog.close();
        } catch (DataAccessException e) {
            showError("Alkusaldojen tallentaminen epäonnistui: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void close() {
        if (modified && model.isChanged()) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Tallennetaanko muutokset?");
            alert.setHeaderText("Tallennetaanko muutokset?");
            alert.setContentText("Olet tehnyt muutoksia alkusaldoihin. Haluatko tallentaa ne?");
            
            ButtonType yesButton = new ButtonType("Kyllä");
            ButtonType noButton = new ButtonType("Ei");
            ButtonType cancelButton = new ButtonType("Peruuta", ButtonBar.ButtonData.CANCEL_CLOSE);
            
            alert.getButtonTypes().setAll(yesButton, noButton, cancelButton);
            
            alert.showAndWait().ifPresent(type -> {
                if (type == yesButton) {
                    try {
                        model.save();
                        dialog.close();
                    } catch (DataAccessException e) {
                        showError("Tallentaminen epäonnistui: " + e.getMessage());
                    }
                } else if (type == noButton) {
                    dialog.close();
                }
            });
        } else {
            dialog.close();
        }
    }
    
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Virhe");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Tieto");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    public void show() {
        dialog.showAndWait();
    }
}
