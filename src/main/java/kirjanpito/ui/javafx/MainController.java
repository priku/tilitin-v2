package kirjanpito.ui.javafx;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;

import kirjanpito.db.*;
import kirjanpito.db.sqlite.SQLiteDataSource;
import kirjanpito.ui.javafx.cells.*;
import kirjanpito.ui.javafx.dialogs.*;
import kirjanpito.util.AppSettings;
import kirjanpito.util.RecentDatabases;
import kirjanpito.util.Registry;

import java.io.File;
import java.math.BigDecimal;
import java.net.URL;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

/**
 * JavaFX pääikkunan controller.
 * 
 * Käyttää olemassa olevia manager-luokkia:
 * - DocumentModel (tietojen hallinta)
 * - DocumentNavigator (navigointi)
 * - DocumentValidator (validointi)
 * - DocumentEntryManager (viennit)
 */
public class MainController implements Initializable {
    
    // FXML-injektoidut komponentit
    @FXML private MenuBar menuBar;
    @FXML private ToolBar toolBar;
    @FXML private Menu recentDatabasesMenu;
    
    @FXML private TextField documentNumberField;
    @FXML private TextField searchField;
    @FXML private Label totalDocumentsLabel;
    @FXML private Label periodIndicator;
    @FXML private DatePicker datePicker;
    @FXML private Label docNumberDisplay;
    @FXML private ComboBox<DocumentType> docTypeCombo;
    @FXML private Label balanceStatusLabel;
    
    @FXML private TableView<EntryRowModel> entryTable;
    @FXML private TableColumn<EntryRowModel, Integer> rowNumCol;
    @FXML private TableColumn<EntryRowModel, Account> accountCol;
    @FXML private TableColumn<EntryRowModel, String> descriptionCol;
    @FXML private TableColumn<EntryRowModel, BigDecimal> debitCol;
    @FXML private TableColumn<EntryRowModel, BigDecimal> creditCol;
    @FXML private TableColumn<EntryRowModel, String> vatCol;
    
    @FXML private Label debitTotalLabel;
    @FXML private Label creditTotalLabel;
    @FXML private Label balanceLabel;
    
    @FXML private Label statusLabel;
    @FXML private Label periodLabel;
    @FXML private Label databaseLabel;
    
    // State
    private Stage stage;
    private DataSource dataSource;
    private String databaseName;  // SQLite tiedostonimi
    private Registry registry;
    
    // Period and documents
    private Period currentPeriod;
    private List<Document> documents;
    private int currentDocumentIndex = -1;
    private Document currentDocument;
    private List<Entry> currentEntries;
    private List<Account> accounts;
    private int documentCount = 0;
    
    // UI data
    private ObservableList<EntryRowModel> entries;
    private DecimalFormat currencyFormat;
    
    // Clipboard for copy/paste
    private List<EntryRowModel> clipboard;
    
    // File chooser
    private FileChooser fileChooser;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("MainController.initialize()");
        
        // Alusta formaatti
        currencyFormat = new DecimalFormat("#,##0.00");
        
        // Alusta taulukko
        initializeTable();
        
        // Alusta file chooser
        initializeFileChooser();
        
        // Alusta UI
        updateUI();
        
        // Täytä Recent Databases -menu
        updateRecentDatabasesMenu();
        
        // Yritä avata viimeisin tietokanta automaattisesti
        tryAutoOpenLastDatabase();
    }
    
    public void setStage(Stage stage) {
        this.stage = stage;
        setupKeyboardShortcuts();
        applyTheme(); // Lataa teema käynnistyksessä
    }
    
    /**
     * Soveltaa teeman Scene:en.
     */
    private void applyTheme() {
        if (stage == null || stage.getScene() == null) return;
        
        Scene scene = stage.getScene();
        AppSettings settings = AppSettings.getInstance();
        String theme = settings.getString("theme", "Vaalea");
        
        // Poista vanhat stylesheetit
        scene.getStylesheets().clear();
        
        // Lisää uusi teema
        if ("Tumma".equalsIgnoreCase(theme) || "Dark".equalsIgnoreCase(theme)) {
            String darkCss = getClass().getResource("/fxml/styles-dark.css").toExternalForm();
            scene.getStylesheets().add(darkCss);
        } else if ("Järjestelmä".equalsIgnoreCase(theme) || "System".equalsIgnoreCase(theme)) {
            // Järjestelmäteema - käytä oletusta
            String lightCss = getClass().getResource("/fxml/styles.css").toExternalForm();
            scene.getStylesheets().add(lightCss);
        } else {
            // Vaalea (oletus)
            String lightCss = getClass().getResource("/fxml/styles.css").toExternalForm();
            scene.getStylesheets().add(lightCss);
        }
    }
    
    private void setupKeyboardShortcuts() {
        if (stage == null || stage.getScene() == null) return;
        
        stage.getScene().setOnKeyPressed(e -> {
            if (e.isControlDown()) {
                switch (e.getCode()) {
                    case N:
                        handleNewDocument();
                        e.consume();
                        break;
                    case S:
                        handleSave();
                        e.consume();
                        break;
                    case P:
                        handlePrint();
                        e.consume();
                        break;
                    case O:
                        handleOpenDatabase();
                        e.consume();
                        break;
                    case LEFT:
                        handlePrevDocument();
                        e.consume();
                        break;
                    case RIGHT:
                        handleNextDocument();
                        e.consume();
                        break;
                    default:
                        break;
                }
            } else {
                switch (e.getCode()) {
                    case F9:
                        openAccountSelectionDialog();
                        e.consume();
                        break;
                    case PAGE_UP:
                        handlePrevDocument();
                        e.consume();
                        break;
                    case PAGE_DOWN:
                        handleNextDocument();
                        e.consume();
                        break;
                    case DELETE:
                        if (entryTable.isFocused() && entryTable.getEditingCell() == null) {
                            handleRemoveEntry();
                            e.consume();
                        }
                        break;
                    case HOME:
                        if (e.isControlDown()) {
                            handleFirstDocument();
                            e.consume();
                        }
                        break;
                    case END:
                        if (e.isControlDown()) {
                            handleLastDocument();
                            e.consume();
                        }
                        break;
                    default:
                        break;
                }
            }
        });
    }
    
    private void initializeTable() {
        entries = FXCollections.observableArrayList();
        entryTable.setItems(entries);
        entryTable.setEditable(true);
        
        // Row number column (read-only)
        rowNumCol.setCellValueFactory(cellData -> cellData.getValue().rowNumberProperty().asObject());
        rowNumCol.setEditable(false);
        rowNumCol.setStyle("-fx-alignment: CENTER;");
        
        // Account column (editable with autocomplete)
        accountCol.setCellValueFactory(cellData -> cellData.getValue().accountProperty());
        accountCol.setCellFactory(col -> new AccountTableCell(accounts != null ? accounts : new ArrayList<>()));
        accountCol.setOnEditCommit(event -> {
            EntryRowModel row = event.getRowValue();
            row.setAccount(event.getNewValue());
            setStatus("Tili muutettu: " + event.getNewValue().getNumber());
        });
        accountCol.setEditable(true);
        
        // Description column (editable)
        descriptionCol.setCellValueFactory(cellData -> cellData.getValue().descriptionProperty());
        descriptionCol.setCellFactory(col -> new DescriptionTableCell());
        descriptionCol.setOnEditCommit(event -> {
            EntryRowModel row = event.getRowValue();
            row.setDescription(event.getNewValue());
        });
        descriptionCol.setEditable(true);
        
        // Debit column (editable)
        debitCol.setCellValueFactory(cellData -> cellData.getValue().debitProperty());
        debitCol.setCellFactory(col -> new AmountTableCell(true));
        debitCol.setOnEditCommit(event -> {
            EntryRowModel row = event.getRowValue();
            row.setDebit(event.getNewValue());
            updateTotals();
        });
        debitCol.setEditable(true);
        debitCol.setStyle("-fx-alignment: CENTER-RIGHT;");
        
        // Credit column (editable)
        creditCol.setCellValueFactory(cellData -> cellData.getValue().creditProperty());
        creditCol.setCellFactory(col -> new AmountTableCell(false));
        creditCol.setOnEditCommit(event -> {
            EntryRowModel row = event.getRowValue();
            row.setCredit(event.getNewValue());
            updateTotals();
        });
        creditCol.setEditable(true);
        creditCol.setStyle("-fx-alignment: CENTER-RIGHT;");
        
        // VAT column (read-only for now)
        vatCol.setCellValueFactory(cellData -> cellData.getValue().vatCodeProperty());
        vatCol.setEditable(false);
        
        // Placeholder
        entryTable.setPlaceholder(new Label("Ei vientejä - paina Enter lisätäksesi"));
        
        // Enable multi-select for copy
        entryTable.getSelectionModel().setSelectionMode(javafx.scene.control.SelectionMode.MULTIPLE);
        
        // Double-click to edit
        entryTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                TablePosition<EntryRowModel, ?> pos = entryTable.getFocusModel().getFocusedCell();
                if (pos != null) {
                    entryTable.edit(pos.getRow(), pos.getTableColumn());
                }
            }
        });
        
        // F9 = Account selection dialog
        entryTable.setOnKeyPressed(event -> {
            if (event.getCode() == javafx.scene.input.KeyCode.F9) {
                openAccountSelectionDialog();
                event.consume();
            }
        });
    }
    
    private void openAccountSelectionDialog() {
        if (accounts == null || accounts.isEmpty()) {
            setStatus("Tilikartta ei ole ladattu");
            return;
        }
        
        // Hae nykyinen hakusana tilisolusta
        String initialSearch = "";
        EntryRowModel selectedRow = entryTable.getSelectionModel().getSelectedItem();
        if (selectedRow != null && selectedRow.getAccount() != null) {
            initialSearch = selectedRow.getAccount().getNumber();
        }
        
        // Näytä dialogi
        Account selected = AccountSelectionDialogFX.showAndSelect(
            stage, accounts, initialSearch);
        
        if (selected != null && selectedRow != null) {
            selectedRow.setAccount(selected);
            entryTable.refresh();
            setStatus("Tili valittu: " + selected.getNumber() + " " + selected.getName());
        }
    }
    
    private void refreshAccountCells() {
        // Update account column cell factory with new accounts list
        accountCol.setCellFactory(col -> new AccountTableCell(accounts != null ? accounts : new ArrayList<>()));
    }
    
    private void initializeFileChooser() {
        fileChooser = new FileChooser();
        fileChooser.setTitle("Valitse tietokanta");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Tilitin-tietokannat", "*.sqlite", "*.db"),
            new FileChooser.ExtensionFilter("Kaikki tiedostot", "*.*")
        );
        
        // Lataa viimeisin hakemisto asetuksista
        // Yritä ensin hakea viimeisimmän tietokannan hakemisto
        AppSettings settings = AppSettings.getInstance();
        String dbUrl = settings.getString("database.url", null);
        
        File lastDir = null;
        if (dbUrl != null && dbUrl.startsWith("jdbc:sqlite:")) {
            String filePath = dbUrl.substring("jdbc:sqlite:".length());
            File dbFile = new File(filePath);
            if (dbFile.exists()) {
                lastDir = dbFile.getParentFile();
            }
        }
        
        // Jos ei löydy tietokannasta, yritä asetuksista
        if (lastDir == null) {
            String lastDirStr = settings.getString("database.directory", null);
            if (lastDirStr != null && !lastDirStr.isEmpty()) {
                lastDir = new File(lastDirStr);
            }
        }
        
        if (lastDir != null && lastDir.exists() && lastDir.isDirectory()) {
            fileChooser.setInitialDirectory(lastDir);
        } else {
            setDefaultDirectory();
        }
    }
    
    private void setDefaultDirectory() {
        String userHome = System.getProperty("user.home");
        File defaultDir = new File(userHome, "Tilitin");
        if (defaultDir.exists()) {
            fileChooser.setInitialDirectory(defaultDir);
        } else {
            // Jos Tilitin-kansiota ei ole, käytä Documents-kansiota
            File documentsDir = new File(userHome, "Documents");
            if (documentsDir.exists()) {
                fileChooser.setInitialDirectory(documentsDir);
            } else {
                fileChooser.setInitialDirectory(new File(userHome));
            }
        }
    }
    
    private void updateUI() {
        boolean hasDatabase = dataSource != null;
        boolean hasDocument = currentDocument != null;
        
        // Päivitä tositelaskuri
        totalDocumentsLabel.setText(String.valueOf(documentCount));
        
        // Päivitä kentät
        if (hasDocument) {
            // Päivämäärä
            if (currentDocument.getDate() != null) {
                LocalDate localDate = currentDocument.getDate().toInstant()
                    .atZone(ZoneId.systemDefault()).toLocalDate();
                datePicker.setValue(localDate);
            }
            
            // Tositenumero
            docNumberDisplay.setText(String.valueOf(currentDocument.getNumber()));
            documentNumberField.setText(String.valueOf(currentDocument.getNumber()));
            
            // Viennit
            loadEntries();
            
            // Summat
            updateTotals();
        } else {
            datePicker.setValue(LocalDate.now());
            docNumberDisplay.setText("-");
            documentNumberField.setText("");
            entries.clear();
            updateTotals();
        }
        
        // Status bar
        if (hasDatabase && databaseName != null) {
            databaseLabel.setText(databaseName);
        } else {
            databaseLabel.setText("Ei tietokantaa");
            periodIndicator.setText("");
        }
    }
    
    private void loadEntries() {
        entries.clear();
        
        if (currentEntries == null) {
            return;
        }
        
        int rowNum = 1;
        for (Entry entry : currentEntries) {
            Account account = findAccount(entry.getAccountId());
            entries.add(new EntryRowModel(rowNum++, entry, account));
        }
        
        // Lisää tyhjä rivi uuden viennin lisäämistä varten
        addEmptyRow();
    }
    
    private void addEmptyRow() {
        EntryRowModel emptyRow = new EntryRowModel();
        emptyRow.setRowNumber(entries.size() + 1);
        entries.add(emptyRow);
    }
    
    private Account findAccount(int accountId) {
        if (accounts == null) return null;
        
        for (Account account : accounts) {
            if (account.getId() == accountId) {
                return account;
            }
        }
        return null;
    }
    
    private void updateTotals() {
        BigDecimal debitTotal = BigDecimal.ZERO;
        BigDecimal creditTotal = BigDecimal.ZERO;
        
        for (EntryRowModel row : entries) {
            BigDecimal d = row.getDebit();
            BigDecimal c = row.getCredit();
            if (d != null) {
                debitTotal = debitTotal.add(d);
            }
            if (c != null) {
                creditTotal = creditTotal.add(c);
            }
        }
        
        debitTotalLabel.setText(currencyFormat.format(debitTotal) + " €");
        creditTotalLabel.setText(currencyFormat.format(creditTotal) + " €");
        
        BigDecimal balance = debitTotal.subtract(creditTotal);
        if (balance.compareTo(BigDecimal.ZERO) == 0) {
            balanceStatusLabel.setText("✓ Tasapaino");
            balanceStatusLabel.getStyleClass().removeAll("balance-error");
            balanceStatusLabel.getStyleClass().add("balance-ok");
            balanceLabel.setText("");
        } else {
            balanceStatusLabel.setText("⚠ Epätasapaino");
            balanceStatusLabel.getStyleClass().removeAll("balance-ok");
            balanceStatusLabel.getStyleClass().add("balance-error");
            balanceLabel.setText(currencyFormat.format(balance.abs()) + " €");
        }
    }
    
    private void setStatus(String message) {
        statusLabel.setText(message);
    }
    
    public void shutdown() {
        // Tallenna muutokset ennen sulkemista
        if (dataSource != null && currentDocument != null) {
            try {
                // Tallenna nykyinen tosite jos on muutoksia
                if (hasUnsavedChanges()) {
                    handleSave();
                }
            } catch (Exception e) {
                System.err.println("Virhe tallennettaessa tositetta ennen sulkemista: " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        // Sulje tietokantayhteys
        if (dataSource != null) {
            try {
                dataSource.close();
            } catch (Exception e) {
                System.err.println("Virhe suljettaessa tietokantaa: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    // ========== Menu handlers ==========
    
    @FXML
    private void handleNewDatabase() {
        fileChooser.setTitle("Uusi tietokanta");
        File file = fileChooser.showSaveDialog(stage);
        if (file != null) {
            // Tallenna viimeisin hakemisto
            saveLastDirectory(file.getParentFile());
            createNewDatabase(file);
        }
    }
    
    @FXML
    private void handleOpenDatabase() {
        fileChooser.setTitle("Avaa tietokanta");
        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            // Tallenna viimeisin hakemisto
            saveLastDirectory(file.getParentFile());
            openDatabase(file);
        }
    }
    
    /**
     * Tallentaa viimeisimmän hakemiston asetuksiin.
     */
    private void saveLastDirectory(File directory) {
        if (directory != null && directory.exists() && directory.isDirectory()) {
            AppSettings settings = AppSettings.getInstance();
            settings.set("database.directory", directory.getAbsolutePath());
        }
    }
    
    @FXML
    private void handleSave() {
        if (dataSource == null || currentDocument == null) {
            setStatus("Ei tallennettavaa");
            return;
        }
        
        setStatus("Tallennetaan...");
        
        Session session = null;
        try {
            session = dataSource.openSession();
            
            // 1. Tallenna tositteen tiedot (päivämäärä)
            if (datePicker.getValue() != null) {
                java.util.Date date = java.util.Date.from(
                    datePicker.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant());
                currentDocument.setDate(date);
            }
            
            DocumentDAO documentDAO = dataSource.getDocumentDAO(session);
            documentDAO.save(currentDocument);
            
            // 2. Tallenna viennit
            EntryDAO entryDAO = dataSource.getEntryDAO(session);
            
            int savedCount = 0;
            int deletedCount = 0;
            
            for (EntryRowModel row : entries) {
                // Ohita tyhjät rivit
                if (row.isEmpty()) {
                    continue;
                }
                
                // Päivitä Entry-olio
                row.updateEntry();
                Entry entry = row.getEntry();
                
                // Aseta dokumentin id jos uusi
                if (entry.getDocumentId() == 0) {
                    entry.setDocumentId(currentDocument.getId());
                }
                
                // Tallenna
                if (entry.getAccountId() > 0 && entry.getAmount() != null) {
                    entryDAO.save(entry);
                    savedCount++;
                    row.setModified(false);
                }
            }
            
            // 3. Poista poistetut viennit
            // (vertaa currentEntries vs entries)
            if (currentEntries != null) {
                for (Entry oldEntry : currentEntries) {
                    boolean found = false;
                    for (EntryRowModel row : entries) {
                        if (row.getEntry() != null && row.getEntry().getId() == oldEntry.getId()) {
                            found = true;
                            break;
                        }
                    }
                    if (!found && oldEntry.getId() > 0) {
                        entryDAO.delete(oldEntry.getId());
                        deletedCount++;
                    }
                }
            }
            
            session.commit();
            
            // Päivitä currentEntries
            currentEntries = entryDAO.getByDocumentId(currentDocument.getId());
            
            setStatus("Tallennettu: " + savedCount + " vientiä" + 
                     (deletedCount > 0 ? ", poistettu " + deletedCount : ""));
            
        } catch (Exception e) {
            if (session != null) {
                try { session.rollback(); } catch (Exception re) {}
            }
            showError("Tallennusvirhe", e.getMessage());
            e.printStackTrace();
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }
    
    @FXML
    private void handlePrint() {
        if (currentDocument == null || currentEntries == null || currentEntries.isEmpty()) {
            setStatus("Ei tulostettavaa");
            return;
        }
        
        PrintHelper.printDocument(stage, dataSource, currentDocument, currentEntries, accounts, registry);
    }
    
    @FXML
    private void handleQuit() {
        shutdown();
        stage.close();
    }
    
    // Entry handlers
    @FXML
    private void handleAddEntry() {
        if (currentDocument == null) {
            setStatus("Avaa ensin tietokanta");
            return;
        }
        
        // Lisää tyhjä rivi ennen viimeistä (tyhjää) riviä
        int insertIndex = Math.max(0, entries.size() - 1);
        EntryRowModel newRow = new EntryRowModel();
        newRow.setRowNumber(insertIndex + 1);
        entries.add(insertIndex, newRow);
        
        // Päivitä rivinumerot
        for (int i = 0; i < entries.size(); i++) {
            entries.get(i).setRowNumber(i + 1);
        }
        
        // Valitse ja aloita editointi
        entryTable.getSelectionModel().select(insertIndex);
        entryTable.scrollTo(insertIndex);
        
        // Aloita tilikartan editointi
        Platform.runLater(() -> {
            entryTable.edit(insertIndex, accountCol);
        });
        
        setStatus("Uusi vienti lisätty");
    }
    
    @FXML
    private void handleRemoveEntry() {
        EntryRowModel selected = entryTable.getSelectionModel().getSelectedItem();
        if (selected != null && !selected.isEmpty()) {
            entries.remove(selected);
            
            // Päivitä rivinumerot
            for (int i = 0; i < entries.size(); i++) {
                entries.get(i).setRowNumber(i + 1);
            }
            
            updateTotals();
            setStatus("Vienti poistettu");
        }
    }
    
    @FXML
    private void handleCopy() {
        // Kopioi valitut rivit
        List<EntryRowModel> selected = new ArrayList<>(entryTable.getSelectionModel().getSelectedItems());
        if (selected.isEmpty()) {
            setStatus("Valitse kopioitavat viennit");
            return;
        }
        
        // Suodata tyhjät
        clipboard = new ArrayList<>();
        for (EntryRowModel row : selected) {
            if (!row.isEmpty()) {
                clipboard.add(row);
            }
        }
        
        if (!clipboard.isEmpty()) {
            setStatus("Kopioitu " + clipboard.size() + " vientiä");
        }
    }
    
    @FXML
    private void handlePaste() {
        if (clipboard == null || clipboard.isEmpty()) {
            setStatus("Ei kopioituja vientejä");
            return;
        }
        
        if (currentDocument == null) {
            setStatus("Avaa ensin tietokanta");
            return;
        }
        
        // Liitä ennen viimeistä tyhjää riviä
        int insertIndex = Math.max(0, entries.size() - 1);
        
        for (EntryRowModel source : clipboard) {
            EntryRowModel newRow = new EntryRowModel();
            newRow.setRowNumber(insertIndex + 1);
            newRow.setAccount(source.getAccount());
            newRow.setDescription(source.getDescription());
            newRow.setDebit(source.getDebit());
            newRow.setCredit(source.getCredit());
            
            entries.add(insertIndex, newRow);
            insertIndex++;
        }
        
        // Päivitä rivinumerot
        for (int i = 0; i < entries.size(); i++) {
            entries.get(i).setRowNumber(i + 1);
        }
        
        updateTotals();
        setStatus("Liitetty " + clipboard.size() + " vientiä");
    }
    
    // Navigation handlers
    @FXML
    private void handlePrevDocument() {
        if (documents == null || documents.isEmpty()) return;
        
        // Tallenna ennen siirtymistä
        if (hasUnsavedChanges()) {
            handleSave();
        }
        
        if (currentDocumentIndex > 0) {
            currentDocumentIndex--;
            loadDocument(documents.get(currentDocumentIndex));
            setStatus("Tosite " + currentDocument.getNumber());
        } else {
            setStatus("Ensimmäinen tosite");
        }
    }
    
    @FXML
    private void handleNextDocument() {
        if (documents == null || documents.isEmpty()) return;
        
        // Tallenna ennen siirtymistä
        if (hasUnsavedChanges()) {
            handleSave();
        }
        
        if (currentDocumentIndex < documents.size() - 1) {
            currentDocumentIndex++;
            loadDocument(documents.get(currentDocumentIndex));
            setStatus("Tosite " + currentDocument.getNumber());
        } else {
            setStatus("Viimeinen tosite");
        }
    }
    
    @FXML
    private void handleFirstDocument() {
        if (documents == null || documents.isEmpty()) return;
        
        // Tallenna ennen siirtymistä
        if (hasUnsavedChanges()) {
            handleSave();
        }
        
        currentDocumentIndex = 0;
        loadDocument(documents.get(0));
        setStatus("Ensimmäinen tosite: " + currentDocument.getNumber());
    }
    
    @FXML
    private void handleLastDocument() {
        if (documents == null || documents.isEmpty()) return;
        
        // Tallenna ennen siirtymistä
        if (hasUnsavedChanges()) {
            handleSave();
        }
        
        currentDocumentIndex = documents.size() - 1;
        loadDocument(documents.get(currentDocumentIndex));
        setStatus("Viimeinen tosite: " + currentDocument.getNumber());
    }
    
    private boolean hasUnsavedChanges() {
        for (EntryRowModel row : entries) {
            if (row.isModified() && !row.isEmpty()) {
                return true;
            }
        }
        return false;
    }
    
    @FXML
    private void handleGotoDocument() {
        // Näytä dialogi
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Siirry tositteeseen");
        dialog.setHeaderText("Syötä tositenumero");
        dialog.setContentText("Tosite:");
        
        dialog.showAndWait().ifPresent(text -> {
            try {
                int docNum = Integer.parseInt(text);
                gotoDocumentNumber(docNum);
            } catch (NumberFormatException e) {
                setStatus("Virheellinen tositenumero");
            }
        });
    }
    
    @FXML
    private void handleDeleteDocument() {
        if (currentDocument == null || dataSource == null) {
            setStatus("Ei poistettavaa tositetta");
            return;
        }
        
        // Vahvistus
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Poista tosite");
        confirm.setHeaderText("Haluatko varmasti poistaa tositteen " + currentDocument.getNumber() + "?");
        confirm.setContentText("Tositteen kaikki viennit poistetaan. Toimintoa ei voi perua.");
        
        if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
            return;
        }
        
        Session session = null;
        try {
            session = dataSource.openSession();
            
            // Poista viennit
            EntryDAO entryDAO = dataSource.getEntryDAO(session);
            if (currentEntries != null) {
                for (Entry entry : currentEntries) {
                    if (entry.getId() > 0) {
                        entryDAO.delete(entry.getId());
                    }
                }
            }
            
            // Poista tosite
            DocumentDAO documentDAO = dataSource.getDocumentDAO(session);
            documentDAO.delete(currentDocument.getId());
            
            session.commit();
            
            // Poista listasta
            int deletedIndex = currentDocumentIndex;
            documents.remove(deletedIndex);
            documentCount = documents.size();
            
            // Siirry edelliseen tai seuraavaan
            if (!documents.isEmpty()) {
                if (deletedIndex >= documents.size()) {
                    currentDocumentIndex = documents.size() - 1;
                }
                loadDocument(documents.get(currentDocumentIndex));
            } else {
                currentDocument = null;
                currentEntries = null;
                currentDocumentIndex = -1;
                updateUI();
            }
            
            setStatus("Tosite poistettu");
            
        } catch (Exception e) {
            if (session != null) {
                try { session.rollback(); } catch (Exception re) {}
            }
            showError("Virhe poistettaessa tositetta", e.getMessage());
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }
    
    @FXML
    private void handleGotoDocumentNumber() {
        String text = documentNumberField.getText();
        try {
            int docNum = Integer.parseInt(text);
            gotoDocumentNumber(docNum);
        } catch (NumberFormatException e) {
            setStatus("Virheellinen tositenumero");
        }
    }
    
    private void gotoDocumentNumber(int docNum) {
        if (documents == null) return;
        
        for (int i = 0; i < documents.size(); i++) {
            if (documents.get(i).getNumber() == docNum) {
                currentDocumentIndex = i;
                loadDocument(documents.get(i));
                setStatus("Tosite " + docNum);
                return;
            }
        }
        setStatus("Tositetta " + docNum + " ei löytynyt");
    }
    
    @FXML
    private void handleNewDocument() {
        if (dataSource == null || currentPeriod == null) {
            setStatus("Avaa ensin tietokanta");
            return;
        }
        
        Session session = null;
        try {
            session = dataSource.openSession();
            DocumentDAO documentDAO = dataSource.getDocumentDAO(session);
            
            // Luo uusi tosite
            Document newDoc = documentDAO.create(currentPeriod.getId(), 1, 999999);
            session.commit();
            
            // Lisää listaan ja siirry siihen
            documents.add(newDoc);
            documentCount = documents.size();
            currentDocumentIndex = documents.size() - 1;
            loadDocument(newDoc);
            
            setStatus("Uusi tosite " + newDoc.getNumber() + " luotu");
            
        } catch (Exception e) {
            showError("Virhe luotaessa tositetta", e.getMessage());
            e.printStackTrace();
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }
    
    @FXML
    private void handleSearch() {
        String query = searchField != null ? searchField.getText() : "";
        if (query.isEmpty()) {
            setStatus("Kirjoita hakusana");
            return;
        }
        
        if (dataSource == null || currentPeriod == null) return;
        
        Session session = null;
        try {
            session = dataSource.openSession();
            DocumentDAO documentDAO = dataSource.getDocumentDAO(session);
            
            List<Document> results = documentDAO.getByPeriodIdAndPhrase(
                currentPeriod.getId(), query, 0, 100);
            
            if (!results.isEmpty()) {
                documents = results;
                documentCount = results.size();
                currentDocumentIndex = 0;
                loadDocument(results.get(0));
                setStatus("Löytyi " + results.size() + " tositetta hakusanalla '" + query + "'");
            } else {
                setStatus("Ei tuloksia hakusanalla '" + query + "'");
            }
            
        } catch (Exception e) {
            setStatus("Hakuvirhe: " + e.getMessage());
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }
    
    // Registry handlers
    @FXML
    private void handleChartOfAccounts() {
        if (dataSource == null) {
            setStatus("Avaa ensin tietokanta");
            return;
        }
        
        COADialogFX dialog = new COADialogFX(stage, dataSource);
        dialog.setAccounts(new ArrayList<>(accounts));
        dialog.setOnSave(v -> {
            // Reload accounts
            loadAllData();
            setStatus("Tilikartta tallennettu");
        });
        dialog.show();
    }
    
    @FXML
    private void handleDocumentTypes() {
        if (dataSource == null) {
            setStatus("Avaa ensin tietokanta");
            return;
        }
        
        Session session = null;
        try {
            session = dataSource.openSession();
            DocumentTypeDAO dao = dataSource.getDocumentTypeDAO(session);
            List<DocumentType> types = dao.getAll();
            session.close();
            session = null;
            
            DocumentTypeDialogFX dialog = new DocumentTypeDialogFX(stage, dataSource);
            dialog.setItems(types);
            dialog.setOnSave(v -> setStatus("Tositelajit tallennettu"));
            dialog.show();
            
        } catch (Exception e) {
            showError("Virhe", e.getMessage());
        } finally {
            if (session != null) session.close();
        }
    }
    
    @FXML
    private void handlePeriodSettings() {
        if (currentPeriod == null) {
            setStatus("Ei tilikautta");
            return;
        }
        
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd.MM.yyyy");
        String info = "Tilikausi: " + sdf.format(currentPeriod.getStartDate()) + 
                      " - " + sdf.format(currentPeriod.getEndDate()) +
                      "\nLukittu: " + (currentPeriod.isLocked() ? "Kyllä" : "Ei");
        
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Tilikauden tiedot");
        alert.setHeaderText("Nykyinen tilikausi");
        alert.setContentText(info);
        alert.showAndWait();
    }
    
    @FXML
    private void handleDatabaseSettings() {
        if (databaseName == null) {
            setStatus("Ei tietokantaa");
            return;
        }
        
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Tietokannan tiedot");
        alert.setHeaderText(databaseName);
        alert.setContentText("Tyyppi: SQLite\nTilejä: " + (accounts != null ? accounts.size() : 0) +
                            "\nTositteita: " + documentCount);
        alert.showAndWait();
    }
    
    @FXML
    private void handleSettings() {
        SettingsDialogFX dialog = new SettingsDialogFX(stage);
        dialog.setOnThemeChanged(theme -> {
            // Sovella teema heti kun se vaihdetaan
            applyTheme();
        });
        dialog.show();
    }
    
    @FXML
    private void handleEditEntryTemplates() {
        showNotImplemented("Vientimallien muokkaus");
    }
    
    @FXML
    private void handleCreateEntryTemplate() {
        showNotImplemented("Vientimallin luominen");
    }
    
    @FXML
    private void handleStartingBalances() {
        showNotImplemented("Alkusaldot");
    }
    
    @FXML
    private void handleVatDocument() {
        showNotImplemented("ALV-tilien päättäminen");
    }
    
    @FXML
    private void handleAttachment() {
        if (currentDocument == null) {
            setStatus("Valitse ensin tosite");
            return;
        }
        
        AttachmentsDialogFX dialog = new AttachmentsDialogFX(stage, dataSource, currentDocument);
        dialog.show();
    }
    
    // Report handlers
    @FXML
    private void handleJournalReport() {
        if (!checkReportPrereqs()) return;
        new ReportDialogFX(stage, ReportDialogFX.ReportType.JOURNAL, dataSource, currentPeriod, accounts).show();
    }
    
    @FXML
    private void handleLedgerReport() {
        if (!checkReportPrereqs()) return;
        new ReportDialogFX(stage, ReportDialogFX.ReportType.LEDGER, dataSource, currentPeriod, accounts).show();
    }
    
    @FXML
    private void handleIncomeStatement() {
        if (!checkReportPrereqs()) return;
        new ReportDialogFX(stage, ReportDialogFX.ReportType.INCOME_STATEMENT, dataSource, currentPeriod, accounts).show();
    }
    
    @FXML
    private void handleBalanceSheet() {
        if (!checkReportPrereqs()) return;
        new ReportDialogFX(stage, ReportDialogFX.ReportType.BALANCE_SHEET, dataSource, currentPeriod, accounts).show();
    }
    
    private boolean checkReportPrereqs() {
        if (dataSource == null || currentPeriod == null) {
            setStatus("Avaa ensin tietokanta");
            return false;
        }
        if (accounts == null || accounts.isEmpty()) {
            setStatus("Tilikartta puuttuu");
            return false;
        }
        return true;
    }
    
    // Help handlers
    @FXML
    private void handleHelp() {
        HelpDialogFX dialog = new HelpDialogFX(stage);
        dialog.show();
    }
    
    @FXML
    private void handleAbout() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Tietoja");
        alert.setHeaderText("Tilitin");
        alert.setContentText("Kirjanpito-ohjelma\nVersio 2.3.0 (JavaFX)\n\n© 2025");
        alert.showAndWait();
    }
    
    // ========== Database operations ==========
    
    private void createNewDatabase(File file) {
        try {
            setStatus("Luodaan tietokanta: " + file.getName());
            
            // Varmista .sqlite-pääte
            String path = file.getAbsolutePath();
            if (!path.endsWith(".sqlite") && !path.endsWith(".db")) {
                file = new File(path + ".sqlite");
            }
            
            // Tarkista onko tiedosto jo olemassa
            if (file.exists()) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Tiedosto on olemassa");
                alert.setHeaderText("Tiedosto on jo olemassa");
                alert.setContentText("Tiedosto " + file.getName() + " on jo olemassa.\n\nHaluatko korvata sen?");
                
                if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.CANCEL) {
                    return;
                }
            }
            
            // SQLite luo tietokannan automaattisesti jos se ei ole olemassa
            // Avaa tietokanta (luo sen jos ei ole olemassa)
            openDatabase(file);
            
        } catch (Exception e) {
            showError("Virhe luotaessa tietokantaa", e.getMessage());
        }
    }
    
    private void openDatabase(File file) {
        try {
            setStatus("Avataan: " + file.getName());
            
            // Sulje edellinen tietokanta
            if (dataSource != null) {
                dataSource.close();
            }
            
            // Avaa SQLite-tietokanta
            SQLiteDataSource ds = new SQLiteDataSource();
            String jdbcUrl = "jdbc:sqlite:" + file.getAbsolutePath();
            ds.open(jdbcUrl, null, null);
            
            this.dataSource = ds;
            this.databaseName = file.getName();
            
            // Alusta Registry
            registry = new Registry();
            registry.setDataSource(dataSource);
            
            // Tallenna viimeisimpien tietokantojen listaan
            String dbUrl = "jdbc:sqlite:" + file.getAbsolutePath().replace(File.separatorChar, '/');
            AppSettings settings = AppSettings.getInstance();
            settings.set("database.url", dbUrl);
            RecentDatabases.getInstance().addDatabase(dbUrl);
            updateRecentDatabasesMenu();
            
            // Lataa kaikki data
            loadAllData();
            
            setStatus("Avattu: " + file.getName() + " - " + documentCount + " tositetta");
            
        } catch (Exception e) {
            showError("Virhe avattaessa tietokantaa", e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void loadAllData() {
        if (dataSource == null) return;
        
        Session session = null;
        try {
            session = dataSource.openSession();
            
            // 1. Lataa tilit
            AccountDAO accountDAO = dataSource.getAccountDAO(session);
            accounts = accountDAO.getAll();
            System.out.println("Ladattu " + accounts.size() + " tiliä");
            
            // Päivitä taulukon tili-sarake
            refreshAccountCells();
            
            // 2. Lataa tilikausi
            PeriodDAO periodDAO = dataSource.getPeriodDAO(session);
            currentPeriod = periodDAO.getCurrent();
            
            if (currentPeriod != null) {
                System.out.println("Tilikausi: " + currentPeriod.getStartDate() + " - " + currentPeriod.getEndDate());
                
                // 3. Lataa tositteet
                DocumentDAO documentDAO = dataSource.getDocumentDAO(session);
                documentCount = documentDAO.getCountByPeriodId(currentPeriod.getId(), 1);
                documents = documentDAO.getByPeriodId(currentPeriod.getId(), 1);
                System.out.println("Ladattu " + documents.size() + " tositetta");
                
                // 4. Siirry ensimmäiseen tositteeseen
                if (!documents.isEmpty()) {
                    currentDocumentIndex = 0;
                    loadDocument(documents.get(0));
                } else {
                    currentDocumentIndex = -1;
                    currentDocument = null;
                    currentEntries = null;
                }
                
                // Päivitä UI
                updateUI();
                updatePeriodLabel();
            } else {
                setStatus("Ei tilikautta");
            }
            
        } catch (Exception e) {
            System.err.println("Virhe ladattaessa dataa: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }
    
    private void loadDocument(Document doc) {
        if (dataSource == null || doc == null) return;
        
        Session session = null;
        try {
            session = dataSource.openSession();
            
            currentDocument = doc;
            
            // Lataa viennit
            EntryDAO entryDAO = dataSource.getEntryDAO(session);
            currentEntries = entryDAO.getByDocumentId(doc.getId());
            System.out.println("Tosite " + doc.getNumber() + ": " + currentEntries.size() + " vientiä");
            
            // Päivitä UI
            updateUI();
            
        } catch (Exception e) {
            System.err.println("Virhe ladattaessa tositetta: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }
    
    private void updatePeriodLabel() {
        if (currentPeriod != null) {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd.MM.yyyy");
            String periodStr = sdf.format(currentPeriod.getStartDate()) + " - " + sdf.format(currentPeriod.getEndDate());
            periodLabel.setText("Tilikausi: " + periodStr);
            periodIndicator.setText(periodStr);
        } else {
            periodLabel.setText("Tilikausi: -");
            periodIndicator.setText("");
        }
    }
    
    // ========== Helpers ==========
    
    /**
     * Yrittää avata viimeisimmän tietokannan automaattisesti käynnistyksessä.
     */
    private void tryAutoOpenLastDatabase() {
        AppSettings settings = AppSettings.getInstance();
        String dbUrl = settings.getString("database.url", null);
        
        if (dbUrl != null && !dbUrl.isEmpty()) {
            // Tarkista onko SQLite-tiedosto
            if (dbUrl.startsWith("jdbc:sqlite:")) {
                String filePath = dbUrl.substring("jdbc:sqlite:".length());
                File file = new File(filePath);
                
                if (file.exists()) {
                    // Avaa automaattisesti taustalla
                    Platform.runLater(() -> {
                        try {
                            openDatabase(file);
                        } catch (Exception e) {
                            System.err.println("Virhe avattaessa viimeisintä tietokantaa: " + e.getMessage());
                            setStatus("Ei voitu avata viimeisintä tietokantaa");
                        }
                    });
                    return;
                }
            }
        }
        
        setStatus("Valmis - Avaa tietokanta aloittaaksesi");
    }
    
    /**
     * Päivittää Recent Databases -menun.
     */
    private void updateRecentDatabasesMenu() {
        if (recentDatabasesMenu == null) return;
        
        recentDatabasesMenu.getItems().clear();
        
        RecentDatabases recent = RecentDatabases.getInstance();
        List<String> databases = recent.getRecentDatabases();
        
        if (databases.isEmpty()) {
            MenuItem emptyItem = new MenuItem("(ei viimeisimpiä)");
            emptyItem.setDisable(true);
            recentDatabasesMenu.getItems().add(emptyItem);
        } else {
            int index = 1;
            for (String dbUrl : databases) {
                String displayName = RecentDatabases.getDisplayName(dbUrl);
                MenuItem item = new MenuItem(index + ". " + displayName);
                
                // Määritä accelerator (1-9)
                if (index <= 9) {
                    KeyCode keyCode = KeyCode.getKeyCode("DIGIT" + index);
                    if (keyCode != null) {
                        item.setAccelerator(new KeyCodeCombination(keyCode, KeyCombination.CONTROL_DOWN));
                    }
                }
                
                // Avaa tietokanta kun klikataan
                String finalDbUrl = dbUrl;
                item.setOnAction(e -> openRecentDatabase(finalDbUrl));
                
                recentDatabasesMenu.getItems().add(item);
                index++;
            }
            
            recentDatabasesMenu.getItems().add(new SeparatorMenuItem());
            
            MenuItem clearItem = new MenuItem("Tyhjennä lista");
            clearItem.setOnAction(e -> {
                RecentDatabases.getInstance().clearAll();
                updateRecentDatabasesMenu();
            });
            recentDatabasesMenu.getItems().add(clearItem);
        }
    }
    
    /**
     * Avaa viimeisimmistä listasta valitun tietokannan.
     */
    private void openRecentDatabase(String dbUrl) {
        if (dbUrl == null || dbUrl.isEmpty()) return;
        
        try {
            if (dbUrl.startsWith("jdbc:sqlite:")) {
                String filePath = dbUrl.substring("jdbc:sqlite:".length());
                File file = new File(filePath);
                
                if (file.exists()) {
                    openDatabase(file);
                } else {
                    showError("Tiedosto ei löydy", "Tiedostoa ei löydy: " + filePath);
                    // Poista listasta jos tiedosto ei ole olemassa
                    RecentDatabases.getInstance().removeDatabase(dbUrl);
                    updateRecentDatabasesMenu();
                }
            } else {
                setStatus("Vain SQLite-tietokannat tuetaan tällä hetkellä");
            }
        } catch (Exception e) {
            showError("Virhe", "Virhe avattaessa tietokantaa: " + e.getMessage());
        }
    }
    
    private void showNotImplemented(String feature) {
        setStatus(feature + " - ei vielä toteutettu");
    }
    
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Virhe");
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
        setStatus("Virhe: " + title);
    }
    
}
