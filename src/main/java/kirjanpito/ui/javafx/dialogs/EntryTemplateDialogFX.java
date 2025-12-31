package kirjanpito.ui.javafx.dialogs;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.KeyCode;
import javafx.util.converter.IntegerStringConverter;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import kirjanpito.db.*;
import kirjanpito.models.EntryTemplateModel;
import kirjanpito.util.Registry;
import kirjanpito.ui.javafx.EntryTemplateRowModel;
import kirjanpito.ui.javafx.cells.EntryTemplateAccountTableCell;
import kirjanpito.ui.javafx.cells.EntryTemplateAmountTableCell;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.List;

/**
 * JavaFX vientimallien muokkausdialogi.
 */
public class EntryTemplateDialogFX {
    
    private Stage dialog;
    private TableView<EntryTemplateRowModel> table;
    private ObservableList<EntryTemplateRowModel> templates;
    private EntryTemplateModel model;
    private Registry registry;
    private List<Account> allAccounts;
    private DecimalFormat currencyFormat;
    private boolean modified = false;
    
    public EntryTemplateDialogFX(Window owner, Registry registry, EntryTemplateModel model) {
        this.registry = registry;
        this.model = model;
        this.allAccounts = registry.getAccounts();
        
        // Currency format
        currencyFormat = new DecimalFormat();
        currencyFormat.setMinimumFractionDigits(2);
        currencyFormat.setMaximumFractionDigits(2);
        
        dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(owner);
        dialog.setTitle("Vientimallit");
        dialog.setMinWidth(700);
        dialog.setMinHeight(500);
        
        createContent();
        loadTemplates();
    }
    
    private void createContent() {
        VBox root = new VBox(10);
        root.setPadding(new Insets(15));
        root.setStyle("-fx-background-color: #ffffff;");
        
        // Toolbar
        HBox toolbar = new HBox(10);
        toolbar.setAlignment(Pos.CENTER_LEFT);
        toolbar.setPadding(new Insets(0, 0, 10, 0));
        
        Button addBtn = new Button("＋ Lisää");
        addBtn.setStyle("-fx-background-color: #2563eb; -fx-text-fill: white;");
        addBtn.setOnAction(e -> addTemplate());
        
        Button removeBtn = new Button("− Poista");
        removeBtn.setOnAction(e -> removeTemplate());
        
        Button saveBtn = new Button("💾 Tallenna");
        saveBtn.setStyle("-fx-background-color: #10b981; -fx-text-fill: white;");
        saveBtn.setOnAction(e -> save());
        
        Button closeBtn = new Button("Sulje");
        closeBtn.setOnAction(e -> close());
        
        toolbar.getChildren().addAll(addBtn, removeBtn, new Separator(), saveBtn, closeBtn);
        
        // Table
        table = new TableView<>();
        table.setEditable(true);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        // Columns
        TableColumn<EntryTemplateRowModel, Integer> numberCol = new TableColumn<>("Nro");
        numberCol.setCellValueFactory(new PropertyValueFactory<>("number"));
        numberCol.setCellFactory(TextFieldTableCell.forTableColumn(new javafx.util.converter.IntegerStringConverter()));
        numberCol.setPrefWidth(50);
        numberCol.setEditable(true);
        
        TableColumn<EntryTemplateRowModel, String> nameCol = new TableColumn<>("Nimi");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setCellFactory(TextFieldTableCell.forTableColumn());
        nameCol.setPrefWidth(120);
        nameCol.setEditable(true);
        
        TableColumn<EntryTemplateRowModel, Account> accountCol = new TableColumn<>("Tili");
        accountCol.setCellValueFactory(new PropertyValueFactory<>("account"));
        accountCol.setCellFactory(EntryTemplateAccountTableCell.forTableColumn(allAccounts));
        accountCol.setPrefWidth(190);
        accountCol.setEditable(true);
        
        TableColumn<EntryTemplateRowModel, BigDecimal> debitCol = new TableColumn<>("Debet");
        debitCol.setCellValueFactory(new PropertyValueFactory<>("debit"));
        debitCol.setCellFactory(EntryTemplateAmountTableCell.forTableColumn(currencyFormat));
        debitCol.setPrefWidth(80);
        debitCol.setEditable(true);
        
        TableColumn<EntryTemplateRowModel, BigDecimal> creditCol = new TableColumn<>("Kredit");
        creditCol.setCellValueFactory(new PropertyValueFactory<>("credit"));
        creditCol.setCellFactory(EntryTemplateAmountTableCell.forTableColumn(currencyFormat));
        creditCol.setPrefWidth(80);
        creditCol.setEditable(true);
        
        TableColumn<EntryTemplateRowModel, String> descriptionCol = new TableColumn<>("Selite");
        descriptionCol.setCellValueFactory(new PropertyValueFactory<>("description"));
        // Use TextFieldTableCell for description
        descriptionCol.setCellFactory(TextFieldTableCell.forTableColumn());
        descriptionCol.setPrefWidth(190);
        descriptionCol.setEditable(true);
        
        table.getColumns().addAll(numberCol, nameCol, accountCol, debitCol, creditCol, descriptionCol);
        
        // Keyboard shortcuts
        table.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.DELETE && e.isShiftDown()) {
                removeTemplate();
            }
        });
        
        root.getChildren().addAll(toolbar, table);
        
        Scene scene = new Scene(root, 700, 500);
        dialog.setScene(scene);
    }
    
    private void loadTemplates() {
        templates = FXCollections.observableArrayList();
        
        for (int i = 0; i < model.getEntryTemplateCount(); i++) {
            EntryTemplate template = model.getEntryTemplate(i);
            Account account = registry.getAccountById(template.getAccountId());
            templates.add(new EntryTemplateRowModel(template, account, currencyFormat));
        }
        
        table.setItems(templates);
    }
    
    private void addTemplate() {
        int index = model.addEntryTemplate();
        EntryTemplate template = model.getEntryTemplate(index);
        templates.add(new EntryTemplateRowModel(template, null, currencyFormat));
        modified = true;
        
        // Select and edit the new row
        table.getSelectionModel().select(templates.size() - 1);
        table.scrollTo(templates.size() - 1);
        table.edit(templates.size() - 1, table.getColumns().get(2)); // Edit account column
    }
    
    private void removeTemplate() {
        int selectedIndex = table.getSelectionModel().getSelectedIndex();
        if (selectedIndex < 0) {
            return;
        }
        
        model.removeEntryTemplate(selectedIndex);
        templates.remove(selectedIndex);
        modified = true;
        
        // Select previous row or first row
        if (selectedIndex > 0) {
            table.getSelectionModel().select(selectedIndex - 1);
        } else if (templates.size() > 0) {
            table.getSelectionModel().select(0);
        }
    }
    
    private boolean validate() {
        for (EntryTemplateRowModel row : templates) {
            EntryTemplate template = row.getTemplate();
            if (template.getAccountId() < 1) {
                showError("Valitse tili ennen tallentamista.");
                return false;
            }
            if (template.getAmount() == null || template.getAmount().compareTo(BigDecimal.ZERO) == 0) {
                showError("Syötä rahamäärä ennen tallentamista.");
                return false;
            }
        }
        return true;
    }
    
    private void save() {
        if (!validate()) {
            return;
        }
        
        try {
            model.save();
            modified = false;
            showInfo("Vientimallit tallennettu.");
            dialog.close();
        } catch (DataAccessException e) {
            showError("Vientimallien tallentaminen epäonnistui: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void close() {
        if (modified && model.isChanged()) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Tallennetaanko muutokset?");
            alert.setHeaderText("Tallennetaanko muutokset?");
            alert.setContentText("Olet tehnyt muutoksia vientimalleihin. Haluatko tallentaa ne?");
            
            ButtonType yesButton = new ButtonType("Kyllä");
            ButtonType noButton = new ButtonType("Ei");
            ButtonType cancelButton = new ButtonType("Peruuta", ButtonBar.ButtonData.CANCEL_CLOSE);
            
            alert.getButtonTypes().setAll(yesButton, noButton, cancelButton);
            
            alert.showAndWait().ifPresent(type -> {
                if (type == yesButton) {
                    if (validate()) {
                        try {
                            model.save();
                            dialog.close();
                        } catch (DataAccessException e) {
                            showError("Tallentaminen epäonnistui: " + e.getMessage());
                        }
                    }
                } else if (type == noButton) {
                    try {
                        model.discardChanges();
                        dialog.close();
                    } catch (DataAccessException e) {
                        showError("Muutosten hylkääminen epäonnistui: " + e.getMessage());
                    }
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
