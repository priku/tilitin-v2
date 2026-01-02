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
import kirjanpito.db.DataAccessException;
import kirjanpito.models.*;
import kirjanpito.reports.*;
import kirjanpito.ui.PrintPreviewFrame;
import kirjanpito.ui.javafx.cells.*;
import kirjanpito.ui.javafx.dialogs.*;
import kirjanpito.ui.javafx.EntryTableNavigationHandler;
import kirjanpito.util.AppSettings;
import kirjanpito.util.AutoCompleteSupport;
import kirjanpito.util.TreeMapAutoCompleteSupport;
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
    @FXML private Menu entryTemplateMenu;
    
    @FXML private TextField documentNumberField;
    @FXML private TextField searchField;
    @FXML private CheckMenuItem searchMenuItem;
    @FXML private Button searchBtn;
    @FXML private Menu docTypeMenu;
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
    
    // Document type selection
    private int currentDocumentTypeIndex = 0;
    private List<DocumentType> documentTypes;
    
    // UI data
    private ObservableList<EntryRowModel> entries;
    private DecimalFormat currencyFormat;
    
    // Clipboard for copy/paste
    private List<EntryRowModel> clipboard;
    
    // File chooser
    private FileChooser fileChooser;
    
    // Entry table navigation handler
    private EntryTableNavigationHandler navigationHandler;
    
    // Auto-complete support for descriptions
    private AutoCompleteSupport autoCompleteSupport;
    
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
        if (stage == null || stage.getScene() == null) {
            System.out.println("⚠️ applyTheme: stage or scene is null");
            return;
        }
        
        Scene scene = stage.getScene();
        AppSettings settings = AppSettings.getInstance();
        String theme = settings.getString("theme", "Vaalea");
        
        System.out.println("🎨 applyTheme: theme = '" + theme + "'");
        
        // Poista vanhat stylesheetit
        scene.getStylesheets().clear();
        System.out.println("🎨 Cleared stylesheets");
        
        // Lisää uusi teema
        String cssPath;
        if ("Tumma".equalsIgnoreCase(theme) || "Dark".equalsIgnoreCase(theme)) {
            cssPath = "/fxml/styles-dark.css";
        } else {
            // Vaalea, Järjestelmä tai oletus
            cssPath = "/fxml/styles.css";
        }
        
        var cssResource = getClass().getResource(cssPath);
        if (cssResource == null) {
            System.err.println("❌ CSS resource not found: " + cssPath);
            return;
        }
        
        String cssUrl = cssResource.toExternalForm();
        scene.getStylesheets().add(cssUrl);
        System.out.println("✅ Applied CSS: " + cssUrl);
        System.out.println("🎨 Stylesheets now: " + scene.getStylesheets());
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
        
        // Description column (editable with auto-complete)
        descriptionCol.setCellValueFactory(cellData -> cellData.getValue().descriptionProperty());
        descriptionCol.setCellFactory(col -> new DescriptionTableCell(
            autoCompleteSupport,
            rowIndex -> {
                // Hae tili-id rivin perusteella
                if (rowIndex >= 0 && rowIndex < entries.size()) {
                    Account acc = entries.get(rowIndex).getAccount();
                    return acc != null ? acc.getId() : -1;
                }
                return -1;
            }
        ));
        descriptionCol.setOnEditCommit(event -> {
            EntryRowModel row = event.getRowValue();
            row.setDescription(event.getNewValue());
            // Lisää auto-complete-tukeen
            if (autoCompleteSupport != null && row.getEntry() != null) {
                autoCompleteSupport.addEntry(row.getEntry());
            }
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
        
        // F9 = Account selection dialog (additional handler)
        entryTable.setOnKeyPressed(event -> {
            if (event.getCode() == javafx.scene.input.KeyCode.F9) {
                openAccountSelectionDialog();
                event.consume();
            }
        });
        
        // Set up context menu
        setupEntryTableContextMenu();
        
        // Set up smart navigation handler
        setupEntryTableNavigation();
    }
    
    /**
     * Luo kontekstivalikko vienti-taulukolle.
     */
    private void setupEntryTableContextMenu() {
        ContextMenu contextMenu = new ContextMenu();
        
        MenuItem insertAbove = new MenuItem("Lisää vienti yläpuolelle");
        insertAbove.setOnAction(e -> {
            int index = entryTable.getSelectionModel().getSelectedIndex();
            insertEntryAtIndex(index >= 0 ? index : 0);
        });
        
        MenuItem insertBelow = new MenuItem("Lisää vienti alapuolelle");
        insertBelow.setOnAction(e -> {
            int index = entryTable.getSelectionModel().getSelectedIndex();
            insertEntryAtIndex(index >= 0 ? index + 1 : entries.size());
        });
        
        MenuItem deleteEntry = new MenuItem("Poista vienti");
        deleteEntry.setAccelerator(new KeyCodeCombination(KeyCode.DELETE, KeyCombination.SHIFT_DOWN));
        deleteEntry.setOnAction(e -> handleRemoveEntry());
        
        SeparatorMenuItem sep1 = new SeparatorMenuItem();
        
        MenuItem copyEntry = new MenuItem("Kopioi vienti");
        copyEntry.setAccelerator(new KeyCodeCombination(KeyCode.C, KeyCombination.CONTROL_DOWN));
        copyEntry.setOnAction(e -> copySelectedEntries());
        
        MenuItem pasteEntry = new MenuItem("Liitä vienti");
        pasteEntry.setAccelerator(new KeyCodeCombination(KeyCode.V, KeyCombination.CONTROL_DOWN));
        pasteEntry.setOnAction(e -> pasteEntries());
        
        SeparatorMenuItem sep2 = new SeparatorMenuItem();
        
        MenuItem toggleDebitCredit = new MenuItem("Vaihda debet/kredit (*)");
        toggleDebitCredit.setOnAction(e -> {
            EntryRowModel selected = entryTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                selected.toggleDebitCredit();
                entryTable.refresh();
                updateTotals();
                setStatus("Debet/Kredit vaihdettu");
            }
        });
        
        MenuItem selectAccount = new MenuItem("Valitse tili... (F9)");
        selectAccount.setOnAction(e -> openAccountSelectionDialog());
        
        contextMenu.getItems().addAll(
            insertAbove, insertBelow, deleteEntry,
            sep1,
            copyEntry, pasteEntry,
            sep2,
            toggleDebitCredit, selectAccount
        );
        
        // Update menu item states
        contextMenu.setOnShowing(e -> {
            boolean hasSelection = !entryTable.getSelectionModel().getSelectedItems().isEmpty();
            deleteEntry.setDisable(!hasSelection);
            copyEntry.setDisable(!hasSelection);
            toggleDebitCredit.setDisable(!hasSelection);
            selectAccount.setDisable(!hasSelection);
            pasteEntry.setDisable(clipboard == null || clipboard.isEmpty());
        });
        
        entryTable.setContextMenu(contextMenu);
    }
    
    /**
     * Lisää vienti annettuun indeksiin.
     */
    private void insertEntryAtIndex(int index) {
        if (dataSource == null || currentDocument == null) return;
        
        Entry entry = new Entry();
        entry.setDocumentId(currentDocument.getId());
        entry.setRowNumber(index + 1);
        
        EntryRowModel newRow = new EntryRowModel(index + 1, entry, null);
        
        // Insert at index and update row numbers
        entries.add(index, newRow);
        for (int i = 0; i < entries.size(); i++) {
            entries.get(i).setRowNumber(i + 1);
            entries.get(i).getOriginalEntry().setRowNumber(i + 1);
        }
        
        entryTable.getSelectionModel().select(index);
        setStatus("Vienti lisätty riville " + (index + 1));
    }
    
    /**
     * Kopioi valitut viennit leikepöydälle.
     */
    private void copySelectedEntries() {
        ObservableList<EntryRowModel> selected = entryTable.getSelectionModel().getSelectedItems();
        if (selected.isEmpty()) return;
        
        clipboard = new ArrayList<>();
        for (EntryRowModel row : selected) {
            // Create a copy
            Entry entryCopy = new Entry();
            entryCopy.setAccountId(row.getOriginalEntry().getAccountId());
            entryCopy.setDescription(row.getOriginalEntry().getDescription());
            entryCopy.setDebit(row.getOriginalEntry().isDebit());
            entryCopy.setAmount(row.getOriginalEntry().getAmount());
            entryCopy.setFlags(row.getOriginalEntry().getFlags());
            
            EntryRowModel rowCopy = new EntryRowModel(0, entryCopy, row.getAccount());
            clipboard.add(rowCopy);
        }
        
        setStatus("Kopioitu " + clipboard.size() + " vientiä");
    }
    
    /**
     * Liitä viennit leikepöydältä.
     */
    private void pasteEntries() {
        if (clipboard == null || clipboard.isEmpty()) return;
        if (dataSource == null || currentDocument == null) return;
        
        int insertIndex = entryTable.getSelectionModel().getSelectedIndex();
        if (insertIndex < 0) insertIndex = entries.size();
        
        for (int i = 0; i < clipboard.size(); i++) {
            EntryRowModel source = clipboard.get(i);
            
            Entry entry = new Entry();
            entry.setDocumentId(currentDocument.getId());
            entry.setAccountId(source.getOriginalEntry().getAccountId());
            entry.setDescription(source.getOriginalEntry().getDescription());
            entry.setDebit(source.getOriginalEntry().isDebit());
            entry.setAmount(source.getOriginalEntry().getAmount());
            entry.setFlags(source.getOriginalEntry().getFlags());
            entry.setRowNumber(insertIndex + i + 1);
            
            EntryRowModel newRow = new EntryRowModel(insertIndex + i + 1, entry, source.getAccount());
            
            entries.add(insertIndex + i, newRow);
        }
        
        // Update row numbers
        for (int i = 0; i < entries.size(); i++) {
            entries.get(i).setRowNumber(i + 1);
            entries.get(i).getOriginalEntry().setRowNumber(i + 1);
        }
        
        updateTotals();
        setStatus("Liitetty " + clipboard.size() + " vientiä");
    }
    
    /**
     * Asenna Entry Table smart navigation.
     * Käsittelee Tab/Shift+Tab, asterisk (*) debet/kredit-vaihtoon, jne.
     */
    private void setupEntryTableNavigation() {
        navigationHandler = new EntryTableNavigationHandler(
            entryTable,
            accountCol,
            descriptionCol,
            debitCol,
            creditCol,
            new EntryTableNavigationHandler.EntryTableCallback() {
                @Override
                public void addEntry() {
                    handleAddEntry();
                }
                
                @Override
                public void createDocument() {
                    handleNewDocument();
                }
                
                @Override
                public void focusDateField() {
                    if (datePicker != null) {
                        datePicker.requestFocus();
                    }
                }
                
                @Override
                public void updateTotals() {
                    MainController.this.updateTotals();
                }
                
                @Override
                public void setStatus(String message) {
                    MainController.this.setStatus(message);
                }
                
                @Override
                public java.util.List<String> getDescriptionHistory() {
                    // TODO: Implement description history from database
                    return java.util.Collections.emptyList();
                }
            }
        );
        navigationHandler.install();
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
                // java.sql.Date doesn't support toInstant(), use getTime() instead
                java.util.Date date = currentDocument.getDate();
                LocalDate localDate = new java.sql.Date(date.getTime()).toLocalDate();
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
        Platform.exit();
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
            dialog.setOnSave(v -> {
                setStatus("Tositelajit tallennettu");
                // Päivitä registry ja valikko
                try {
                    registry.fetchDocumentTypes();
                    updateDocumentTypeMenu();
                } catch (Exception ex) {
                    System.err.println("Virhe päivitettäessä tositelajeja: " + ex.getMessage());
                }
            });
            dialog.show();
            
        } catch (Exception e) {
            showError("Virhe", e.getMessage());
        } finally {
            if (session != null) session.close();
        }
    }
    
    @FXML
    private void handlePeriodSettings() {
        if (registry == null || dataSource == null) {
            setStatus("Ei tietokantaa");
            return;
        }
        
        // Luo PropertiesModel ja näytä dialogi
        PropertiesModel propertiesModel = new PropertiesModel(registry);
        
        try {
            propertiesModel.initialize();
        } catch (DataAccessException e) {
            showError("Asetuksien hakeminen epäonnistui", e.getMessage());
            return;
        }
        
        PropertiesDialogFX dialog = new PropertiesDialogFX(stage, propertiesModel);
        dialog.show();
        
        // Jos tallennettiin, päivitä näyttö
        if (dialog.isSaved()) {
            try {
                // Päivitä tilikausi jos se vaihtui
                Period newPeriod = propertiesModel.getPeriod(propertiesModel.getCurrentPeriodIndex());
                if (newPeriod != null && (currentPeriod == null || newPeriod.getId() != currentPeriod.getId())) {
                    currentPeriod = newPeriod;
                    registry.setPeriod(currentPeriod);
                    updateUI();
                }
                
                setStatus("Asetukset tallennettu");
            } catch (Exception e) {
                // Ignore update errors, data was saved successfully
            }
        }
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
        dialog.show();
    }
    
    @FXML
    private void handleAppearance() {
        kirjanpito.ui.javafx.dialogs.AppearanceDialogFX dialog = 
            new kirjanpito.ui.javafx.dialogs.AppearanceDialogFX(stage);
        dialog.setOnThemeChanged(theme -> applyTheme());
        dialog.show();
    }
    
    @FXML
    private void handleKeyboardShortcuts() {
        KeyboardShortcutsDialogFX dialog = new KeyboardShortcutsDialogFX(stage);
        dialog.show();
    }
    
    @FXML
    private void handlePrintSettings() {
        PrintSettingsDialogFX dialog = new PrintSettingsDialogFX(stage);
        dialog.show();
    }
    
    @FXML
    private void handleExportSettings() {
        SettingsExportImportFX exportImport = new SettingsExportImportFX(stage);
        exportImport.exportSettings();
    }
    
    @FXML
    private void handleImportSettings() {
        SettingsExportImportFX exportImport = new SettingsExportImportFX(stage);
        exportImport.importSettings();
    }
    
    @FXML
    private void handleEditEntryTemplates() {
        if (dataSource == null || registry == null) {
            setStatus("Avaa ensin tietokanta");
            return;
        }
        
        try {
            kirjanpito.models.EntryTemplateModel templateModel = new kirjanpito.models.EntryTemplateModel(registry);
            EntryTemplateDialogFX dialog = new EntryTemplateDialogFX(stage, registry, templateModel);
            dialog.show();
        } catch (Exception e) {
            showError("Virhe avattaessa vientimallien muokkausikkunaa", e.getMessage());
            e.printStackTrace();
        }
    }
    
    @FXML
    private void handleCreateEntryTemplate() {
        if (dataSource == null || currentDocument == null) {
            setStatus("Avaa ensin tosite");
            return;
        }
        
        if (entries == null || entries.isEmpty()) {
            showError("Virhe", "Tositteessa ei ole vientejä.");
            return;
        }
        
        try {
            int templateNumber = createEntryTemplateFromCurrentDocument();
            
            if (templateNumber > 0) {
                String message = String.format("Vientimalli luotu numerolla %d", templateNumber);
                if (templateNumber >= 1 && templateNumber < 10) {
                    message += String.format(" (Alt+%d)", templateNumber);
                }
                showInfo("Vientimalli luotu", message);
                updateEntryTemplateMenu();
            }
        } catch (DataAccessException e) {
            showError("Virhe", "Vientimallin luominen epäonnistui: " + e.getMessage());
        }
    }
    
    private int createEntryTemplateFromCurrentDocument() throws DataAccessException {
        if (entries == null || entries.isEmpty()) {
            return -1;
        }
        
        // Find next available template number
        int number = 1;
        boolean match = true;
        List<kirjanpito.db.EntryTemplate> existingTemplates = registry.getEntryTemplates();
        
        while (match) {
            match = false;
            for (kirjanpito.db.EntryTemplate t : existingTemplates) {
                if (t.getNumber() == number) {
                    number++;
                    match = true;
                    break;
                }
            }
        }
        
        // Get template name from first entry description
        String name = entries.get(0).getDescription();
        if (name == null || name.trim().isEmpty()) {
            name = "Malli " + number;
        }
        
        Session sess = null;
        try {
            sess = dataSource.openSession();
            kirjanpito.db.EntryTemplateDAO dao = dataSource.getEntryTemplateDAO(sess);
            
            int rowNum = 0;
            for (EntryRowModel row : entries) {
                if (row.getAccount() == null) continue;
                
                kirjanpito.db.EntryTemplate template = new kirjanpito.db.EntryTemplate();
                template.setNumber(number);
                template.setName(name);
                template.setAccountId(row.getAccount().getId());
                template.setDebit(row.getDebit() != null && row.getDebit().compareTo(java.math.BigDecimal.ZERO) > 0);
                template.setDescription(row.getDescription());
                template.setRowNumber(rowNum++);
                
                // Set amount - use debit or credit
                java.math.BigDecimal amount = row.getDebit();
                if (amount == null || amount.compareTo(java.math.BigDecimal.ZERO) == 0) {
                    amount = row.getCredit();
                }
                template.setAmount(amount != null ? amount : java.math.BigDecimal.ZERO);
                
                dao.save(template);
            }
            
            sess.commit();
            registry.fetchEntryTemplates(sess);
        } catch (DataAccessException e) {
            if (sess != null) sess.rollback();
            throw e;
        } finally {
            if (sess != null) sess.close();
        }
        
        return number;
    }
    
    @FXML
    private void handleStartingBalances() {
        if (dataSource == null || registry == null) {
            setStatus("Avaa ensin tietokanta");
            return;
        }
        
        try {
            kirjanpito.models.StartingBalanceModel balanceModel = new kirjanpito.models.StartingBalanceModel(registry);
            balanceModel.initialize();
            StartingBalanceDialogFX dialog = new StartingBalanceDialogFX(stage, balanceModel);
            dialog.show();
        } catch (DataAccessException e) {
            showError("Virhe avattaessa alkusaldojen muokkausikkunaa", e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            showError("Virhe avattaessa alkusaldojen muokkausikkunaa", e.getMessage());
            e.printStackTrace();
        }
    }
    
    @FXML
    private void handleVatDocument() {
        if (registry == null || registry.getPeriod() == null) {
            showError("Virhe", "Avaa ensin tilikausi.");
            return;
        }
        
        // Save current document first
        handleSave();
        
        try {
            boolean result = createVatClosingDocument();
            
            if (!result) {
                showError("Virhe", "ALV-velkatiliä (vatCode=1) ei ole määritetty tilikarttaan.");
            } else {
                setStatus("ALV-tilien päättämistosite luotu");
            }
        } catch (DataAccessException e) {
            showError("Virhe", "ALV-tositteen luominen epäonnistui: " + e.getMessage());
        }
    }
    
    private boolean createVatClosingDocument() throws DataAccessException {
        // Calculate VAT account balances
        final java.util.Map<Integer, java.math.BigDecimal> vatBalances = new java.util.HashMap<>();
        Period period = registry.getPeriod();
        Session sess = null;
        
        try {
            sess = dataSource.openSession();
            
            // Get all entries and calculate VAT account balances
            dataSource.getEntryDAO(sess).getByPeriodId(period.getId(), 
                kirjanpito.db.EntryDAO.ORDER_BY_DOCUMENT_NUMBER,
                new kirjanpito.db.DTOCallback<Entry>() {
                    public void process(Entry entry) {
                        Account account = registry.getAccountById(entry.getAccountId());
                        if (account != null && (account.getVatCode() == 2 || account.getVatCode() == 3)) {
                            java.math.BigDecimal balance = vatBalances.getOrDefault(account.getId(), java.math.BigDecimal.ZERO);
                            java.math.BigDecimal amount = entry.getAmount();
                            if (entry.isDebit()) {
                                balance = balance.add(amount);
                            } else {
                                balance = balance.subtract(amount);
                            }
                            vatBalances.put(account.getId(), balance);
                        }
                    }
                });
            
            // Find VAT debt account (vatCode = 1)
            Account debtAccount = null;
            for (Account account : registry.getAccounts()) {
                if (account.getVatCode() == 1) {
                    debtAccount = account;
                    break;
                }
            }
            
            if (debtAccount == null) {
                return false;
            }
            
            // Create new document via handleNewDocument logic
            DocumentDAO documentDAO = dataSource.getDocumentDAO(sess);
            Document newDoc = documentDAO.create(currentPeriod.getId(), 1, 999999);
            sess.commit();
            
            // Switch to the new document
            documents.add(newDoc);
            documentCount = documents.size();
            currentDocumentIndex = documents.size() - 1;
            currentDocument = newDoc;
            
            // Clear entries
            entries.clear();
            
            // Add entries for each VAT account with non-zero balance
            java.math.BigDecimal totalDebt = java.math.BigDecimal.ZERO;
            int rowNum = 1;
            
            for (Account account : registry.getAccounts()) {
                java.math.BigDecimal balance = vatBalances.get(account.getId());
                if (balance != null && balance.compareTo(java.math.BigDecimal.ZERO) != 0) {
                    totalDebt = totalDebt.add(balance);
                    
                    EntryRowModel row = new EntryRowModel();
                    row.setRowNumber(rowNum++);
                    row.setAccount(account);
                    row.setDescription("");
                    
                    if (balance.compareTo(java.math.BigDecimal.ZERO) < 0) {
                        row.setCredit(balance.negate());
                    } else {
                        row.setDebit(balance);
                    }
                    
                    entries.add(row);
                }
            }
            
            // Add debt account entry
            if (totalDebt.compareTo(java.math.BigDecimal.ZERO) != 0) {
                EntryRowModel debtRow = new EntryRowModel();
                debtRow.setRowNumber(rowNum);
                debtRow.setAccount(debtAccount);
                debtRow.setDescription("");
                
                if (totalDebt.compareTo(java.math.BigDecimal.ZERO) < 0) {
                    debtRow.setDebit(totalDebt.negate());
                } else {
                    debtRow.setCredit(totalDebt);
                }
                
                entries.add(debtRow);
            }
            
            // Update UI
            loadDocument(currentDocument);
            
        } finally {
            if (sess != null) sess.close();
        }
        
        return true;
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
        System.out.println("📊 handleJournalReport kutsuttu");
        if (!checkReportPrereqs()) {
            System.out.println("❌ checkReportPrereqs palautti false");
            return;
        }
        System.out.println("✅ checkReportPrereqs OK, luodaan raportti...");
        try {
            List<Period> allPeriods = getAllPeriods();
            System.out.println("📋 Tilikaudet: " + allPeriods.size() + " kpl");
            kirjanpito.ui.javafx.dialogs.ReportDialog.Companion.create(
                stage, 
                kirjanpito.ui.javafx.dialogs.ReportDialog.ReportType.JOURNAL, 
                dataSource, currentPeriod, accounts, allPeriods,
                account -> {
                    openLedgerForAccount(account);
                    return kotlin.Unit.INSTANCE;
                }
            ).show();
            System.out.println("✅ ReportDialog.show() kutsuttu");
        } catch (Exception e) {
            System.err.println("❌ Virhe raportissa: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @FXML
    private void handleLedgerReport() {
        if (!checkReportPrereqs()) return;
        List<Period> allPeriods = getAllPeriods();
        kirjanpito.ui.javafx.dialogs.ReportDialog.Companion.create(
            stage,
            kirjanpito.ui.javafx.dialogs.ReportDialog.ReportType.LEDGER,
            dataSource, currentPeriod, accounts, allPeriods, null
        ).show();
    }
    
    @FXML
    private void handleIncomeStatement() {
        if (!checkReportPrereqs()) return;
        List<Period> allPeriods = getAllPeriods();
        kirjanpito.ui.javafx.dialogs.ReportDialog.Companion.create(
            stage,
            kirjanpito.ui.javafx.dialogs.ReportDialog.ReportType.INCOME_STATEMENT,
            dataSource, currentPeriod, accounts, allPeriods, null
        ).show();
    }
    
    @FXML
    private void handleBalanceSheet() {
        if (!checkReportPrereqs()) return;
        List<Period> allPeriods = getAllPeriods();
        kirjanpito.ui.javafx.dialogs.ReportDialog.Companion.create(
            stage,
            kirjanpito.ui.javafx.dialogs.ReportDialog.ReportType.BALANCE_SHEET,
            dataSource, currentPeriod, accounts, allPeriods, null
        ).show();
    }
    
    private List<Period> getAllPeriods() {
        try {
            Session session = dataSource.openSession();
            try {
                return dataSource.getPeriodDAO(session).getAll();
            } finally {
                session.close();
            }
        } catch (Exception e) {
            return java.util.Collections.singletonList(currentPeriod);
        }
    }
    
    private void openLedgerForAccount(kirjanpito.db.Account account) {
        // Open ledger filtered to specific account
        List<Period> allPeriods = getAllPeriods();
        kirjanpito.ui.javafx.dialogs.ReportDialog.Companion.create(
            stage,
            kirjanpito.ui.javafx.dialogs.ReportDialog.ReportType.LEDGER,
            dataSource, currentPeriod, accounts, allPeriods, null
        ).show();
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
        kirjanpito.ui.javafx.dialogs.AboutDialogFX.showAbout(stage);
    }
    
    @FXML
    private void handleBackupSettings() {
        try {
            kirjanpito.ui.javafx.dialogs.BackupSettingsDialogFX dialog = 
                new kirjanpito.ui.javafx.dialogs.BackupSettingsDialogFX(stage);
            dialog.showAndWait();
        } catch (Exception e) {
            showError("Virhe avattaessa varmuuskopiointiasetuksia", e.getMessage());
        }
    }
    
    @FXML
    private void handleRestoreBackup() {
        RestoreBackupDialogFX dialog = new RestoreBackupDialogFX(stage);
        dialog.showAndWait();
        
        // If a database was restored, try to open it
        File restoredFile = dialog.getRestoredDatabaseFile();
        if (restoredFile != null && restoredFile.exists()) {
            openDatabase(restoredFile);
        }
    }
    
    @FXML
    private void handleToggleSearch() {
        // Toggle search panel visibility
        if (searchField != null && searchMenuItem != null) {
            boolean isVisible = searchField.isVisible();
            searchField.setVisible(!isVisible);
            searchMenuItem.setSelected(!isVisible);
            
            // Also toggle search button visibility
            if (searchBtn != null) {
                searchBtn.setVisible(!isVisible);
            }
            
            setStatus(isVisible ? "Haku piilotettu" : "Haku näkyvissä");
        }
    }
    
    @FXML
    private void handleAccountSummary() {
        if (currentPeriod == null) {
            showError("Virhe", "Avaa ensin tietokanta");
            return;
        }
        
        // Save current document first
        handleSave();
        
        AppSettings appSettings = AppSettings.getInstance();
        AccountSummaryOptionsDialogFX dialog = AccountSummaryOptionsDialogFX.create(stage, currentPeriod);
        dialog.setPreviousPeriodVisible(appSettings.getBoolean("previous-period", false));
        
        if (dialog.showAndWait()) {
            boolean previousPeriodVisible = dialog.isPreviousPeriodVisible();
            appSettings.set("previous-period", previousPeriodVisible);
            int printedAccounts = dialog.getPrintedAccounts();
            
            // Create and run the model on background thread, then show preview on EDT
            javafx.concurrent.Task<Void> task = new javafx.concurrent.Task<>() {
                @Override
                protected Void call() throws Exception {
                    AccountSummaryModel printModel = new AccountSummaryModel();
                    printModel.setRegistry(registry);
                    printModel.setPeriod(currentPeriod);
                    printModel.setStartDate(dialog.getStartDate());
                    printModel.setEndDate(dialog.getEndDate());
                    printModel.setPreviousPeriodVisible(previousPeriodVisible);
                    printModel.setPrintedAccounts(printedAccounts);
                    printModel.run();
                    
                    // Show PrintPreviewFrame on EDT
                    javax.swing.SwingUtilities.invokeLater(() -> {
                        showPrintPreview(printModel, new AccountSummaryPrint(printModel, printedAccounts != 1));
                    });
                    return null;
                }
            };
            
            task.setOnFailed(e -> {
                Throwable ex = task.getException();
                showError("Virhe", "Tulosteen luominen epäonnistui: " + ex.getMessage());
            });
            
            new Thread(task).start();
        }
    }
    
    /** Shows JavaFX PrintPreviewStageFX for the given print model and print. */
    private void showPrintPreview(PrintModel printModel, kirjanpito.reports.Print print) {
        Platform.runLater(() -> {
            print.setSettings(registry.getSettings());
            PrintPreviewStageFX.showPreview(stage, printModel, print);
        });
    }
    
    @FXML
    private void handlePrintDocument() {
        handlePrint();
    }
    
    @FXML
    private void handleAccountStatement() {
        if (currentPeriod == null || dataSource == null) {
            showError("Virhe", "Avaa ensin tietokanta");
            return;
        }
        
        // Save current document first
        handleSave();
        
        // Get currently selected account for default selection
        Account defaultAccount = null;
        int selectedRow = entryTable.getSelectionModel().getSelectedIndex();
        if (selectedRow >= 0 && selectedRow < entries.size()) {
            EntryRowModel row = entries.get(selectedRow);
            if (row.getEntry() != null && row.getEntry().getAccountId() > 0) {
                defaultAccount = registry.getAccountById(row.getEntry().getAccountId());
            }
        }
        
        AppSettings appSettings = AppSettings.getInstance();
        AccountStatementOptionsDialogFX dialog = AccountStatementOptionsDialogFX.create(stage, registry, currentPeriod);
        dialog.setOrderByDate(appSettings.getString("sort-entries", "number").equals("date"));
        if (defaultAccount != null) {
            dialog.selectAccount(defaultAccount);
        }
        
        if (dialog.showAndWait()) {
            Account account = dialog.getSelectedAccount();
            if (account == null) {
                showError("Virhe", "Valitse tili");
                return;
            }
            
            boolean orderByDate = dialog.isOrderByDate();
            appSettings.set("sort-entries", orderByDate ? "date" : "number");
            
            // Create and run the model on background thread, then show preview on EDT
            javafx.concurrent.Task<Void> task = new javafx.concurrent.Task<>() {
                @Override
                protected Void call() throws Exception {
                    AccountStatementModel printModel = new AccountStatementModel();
                    printModel.setDataSource(dataSource);
                    printModel.setSettings(registry.getSettings());
                    printModel.setPeriod(currentPeriod);
                    printModel.setAccount(account);
                    printModel.setStartDate(dialog.getStartDate());
                    printModel.setEndDate(dialog.getEndDate());
                    printModel.setOrderBy(orderByDate ? AccountStatementModel.ORDER_BY_DATE : AccountStatementModel.ORDER_BY_NUMBER);
                    printModel.run();
                    
                    // Show PrintPreviewFrame on EDT
                    javax.swing.SwingUtilities.invokeLater(() -> {
                        showPrintPreview(printModel, new AccountStatementPrint(printModel));
                    });
                    return null;
                }
            };
            
            task.setOnFailed(e -> {
                Throwable ex = task.getException();
                showError("Virhe", "Tulosteen luominen epäonnistui: " + ex.getMessage());
            });
            
            new Thread(task).start();
        }
    }
    
    @FXML
    private void handleIncomeStatementDetailed() {
        showFinancialStatement(true, FinancialStatementModel.TYPE_INCOME_STATEMENT_DETAILED);
    }
    
    @FXML
    private void handleBalanceSheetDetailed() {
        showFinancialStatement(false, FinancialStatementModel.TYPE_BALANCE_SHEET_DETAILED);
    }
    
    /** Helper method for showing financial statements (income statement/balance sheet). */
    private void showFinancialStatement(boolean isIncomeStatement, int reportType) {
        if (currentPeriod == null || dataSource == null) {
            showError("Virhe", "Avaa ensin tietokanta");
            return;
        }
        
        handleSave();
        
        // Fetch periods for the dialog
        List<Period> periods;
        try {
            Session sess = dataSource.openSession();
            try {
                periods = dataSource.getPeriodDAO(sess).getAll();
                // Remove periods that start after current period
                periods.removeIf(p -> p.getStartDate().after(currentPeriod.getStartDate()));
            } finally {
                sess.close();
            }
        } catch (DataAccessException e) {
            showError("Virhe", "Tilikausien haku epäonnistui: " + e.getMessage());
            return;
        }
        
        FinancialStatementOptionsDialogFX dialog;
        if (isIncomeStatement) {
            dialog = FinancialStatementOptionsDialogFX.createIncomeStatement(stage, periods);
        } else {
            dialog = FinancialStatementOptionsDialogFX.createBalanceSheet(stage, periods);
        }
        
        if (dialog.showAndWait()) {
            Date[] startDates = dialog.getStartDates();
            Date[] endDates = dialog.getEndDates();
            boolean pageBreakEnabled = dialog.isPageBreakEnabled();
            
            if (startDates == null || endDates == null) {
                return;
            }
            
            javafx.concurrent.Task<Void> task = new javafx.concurrent.Task<>() {
                @Override
                protected Void call() throws Exception {
                    FinancialStatementModel printModel = new FinancialStatementModel(reportType);
                    printModel.setDataSource(dataSource);
                    printModel.setSettings(registry.getSettings());
                    printModel.setAccounts(registry.getAccounts());
                    printModel.setStartDates(startDates);
                    printModel.setEndDates(endDates);
                    if (!isIncomeStatement) {
                        printModel.setPageBreakEnabled(pageBreakEnabled);
                    }
                    printModel.run();
                    
                    javax.swing.SwingUtilities.invokeLater(() -> {
                        showPrintPreview(printModel, new FinancialStatementPrint(printModel));
                    });
                    return null;
                }
            };
            
            task.setOnFailed(e -> {
                Throwable ex = task.getException();
                showError("Virhe", "Tulosteen luominen epäonnistui: " + ex.getMessage());
            });
            
            new Thread(task).start();
        }
    }
    
    @FXML
    private void handleVatReport() {
        if (currentPeriod == null || dataSource == null) {
            showError("Virhe", "Avaa ensin tietokanta");
            return;
        }
        
        handleSave();
        
        VATReportDialogFX dialog = VATReportDialogFX.create(stage, currentPeriod);
        
        if (dialog.showAndWait()) {
            Date startDate = dialog.getStartDate();
            Date endDate = dialog.getEndDate();
            
            javafx.concurrent.Task<Void> task = new javafx.concurrent.Task<>() {
                @Override
                protected Void call() throws Exception {
                    VATReportModel printModel = new VATReportModel();
                    printModel.setDataSource(dataSource);
                    printModel.setSettings(registry.getSettings());
                    printModel.setPeriod(currentPeriod);
                    printModel.setStartDate(startDate);
                    printModel.setEndDate(endDate);
                    printModel.run();
                    
                    javax.swing.SwingUtilities.invokeLater(() -> {
                        showPrintPreview(printModel, new VATReportPrint(printModel));
                    });
                    return null;
                }
            };
            
            task.setOnFailed(e -> {
                Throwable ex = task.getException();
                showError("Virhe", "ALV-laskelman luominen epäonnistui: " + ex.getMessage());
            });
            
            new Thread(task).start();
        }
    }
    
    @FXML
    private void handleCoa0() {
        showCOAReport(COAPrintModel.ALL_ACCOUNTS); // All accounts
    }
    
    @FXML
    private void handleCoa1() {
        showCOAReport(COAPrintModel.USED_ACCOUNTS); // Used accounts only
    }
    
    @FXML
    private void handleCoa2() {
        showCOAReport(COAPrintModel.FAVOURITE_ACCOUNTS); // Favorite accounts only
    }
    
    /** Helper method for showing chart of accounts reports. */
    private void showCOAReport(int mode) {
        if (currentPeriod == null || dataSource == null) {
            showError("Virhe", "Avaa ensin tietokanta");
            return;
        }
        
        handleSave();
        
        javafx.concurrent.Task<Void> task = new javafx.concurrent.Task<>() {
            @Override
            protected Void call() throws Exception {
                COAPrintModel printModel = new COAPrintModel();
                printModel.setRegistry(registry);
                printModel.setMode(mode);
                printModel.run();
                
                javax.swing.SwingUtilities.invokeLater(() -> {
                    showPrintPreview(printModel, new COAPrint(printModel));
                });
                return null;
            }
        };
        
        task.setOnFailed(e -> {
            Throwable ex = task.getException();
            showError("Virhe", "Tilikartan tulostus epäonnistui: " + ex.getMessage());
        });
        
        new Thread(task).start();
    }
    
    @FXML
    private void handleEditReports() {
        if (registry == null) {
            showError("Virhe", "Avaa ensin tietokanta");
            return;
        }
        
        ReportEditorModel editorModel = new ReportEditorModel(registry);
        
        try {
            editorModel.load();
        } catch (DataAccessException e) {
            showError("Virhe", "Tulostetietojen hakeminen epäonnistui: " + e.getMessage());
            return;
        }
        
        ReportEditorDialogFX dialog = new ReportEditorDialogFX(stage, editorModel);
        dialog.show();
    }
    
    @FXML
    private void handleSetIgnoreFlag() {
        // Get selected entry rows
        var selectedItems = entryTable.getSelectionModel().getSelectedItems();
        
        if (selectedItems == null || selectedItems.isEmpty()) {
            setStatus("Valitse ensin vienti");
            return;
        }
        
        // Determine initial flag state from first selected row
        EntryRowModel firstRow = selectedItems.get(0);
        Entry firstEntry = firstRow.getEntry();
        boolean newIgnoreFlag = firstEntry == null || !firstEntry.getFlag(0);
        
        int updatedCount = 0;
        
        for (EntryRowModel row : selectedItems) {
            if (row.getAccount() == null) continue;
            
            Account account = row.getAccount();
            Entry entry = row.getEntry();
            
            // Only apply to VAT accounts (vatCode 2 or 3)
            if (account.getVatCode() == 2 || account.getVatCode() == 3) {
                if (entry == null) {
                    // Create entry if needed
                    entry = new Entry();
                    row.setEntry(entry);
                }
                entry.setFlag(0, newIgnoreFlag);
                row.setModified(true);
                updatedCount++;
            } else {
                // Non-VAT accounts: clear flag
                if (entry != null) {
                    entry.setFlag(0, false);
                }
            }
        }
        
        entryTable.refresh();
        
        if (updatedCount > 0) {
            String status = newIgnoreFlag 
                ? "ALV-laskelmasta ohitettu " + updatedCount + " vientiä"
                : "ALV-laskelmaan palautettu " + updatedCount + " vientiä";
            setStatus(status);
        } else {
            setStatus("Valitut viennit eivät ole ALV-tilejä");
        }
    }
    
    @FXML
    private void handleBalanceComparison() {
        if (registry == null || registry.getPeriod() == null) {
            showError("Virhe", "Avaa ensin tilikausi.");
            return;
        }
        
        try {
            kirjanpito.models.StatisticsModel statsModel = new kirjanpito.models.StatisticsModel(registry);
            BalanceComparisonDialogFX dialog = new BalanceComparisonDialogFX(stage, statsModel);
            dialog.show();
        } catch (Exception e) {
            showError("Virhe", "Saldovertailun avaaminen epäonnistui: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleNumberShift() {
        if (registry == null || registry.getPeriod() == null) {
            showError("Virhe", "Avaa ensin tilikausi.");
            return;
        }
        
        try {
            DocumentNumberShiftDialogFX dialog = new DocumentNumberShiftDialogFX(stage, registry);
            
            // Get current document number as start, or 1
            int startNum = currentDocument != null ? currentDocument.getNumber() : 1;
            dialog.fetchDocuments(startNum, 999999);
            
            if (dialog.showAndWait()) {
                // Reload documents after renumbering
                loadAllData();
                setStatus("Tositenumerot muutettu");
            }
        } catch (DataAccessException e) {
            showError("Virhe", "Tositteiden haku epäonnistui: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleVatChange() {
        VATChangeDialogFX dialog = new VATChangeDialogFX(stage, registry);
        dialog.show();
        // Refresh data after potential changes
        loadAllData();
    }
    
    @FXML
    private void handleExport() {
        if (currentPeriod == null) {
            showError("Virhe", "Avaa ensin tilikausi ennen vientiä.");
            return;
        }
        
        DataExportDialogFX.showExportDialog(stage, registry, currentPeriod);
    }
    
    @FXML
    private void handleCsvImport() {
        if (registry == null || dataSource == null) {
            showError("CSV-tuontivirhe", "Avaa ensin tietokanta ennen CSV-tuontia.");
            return;
        }
        
        try {
            List<Account> accounts = registry.getAccounts();
            Period period = currentPeriod;
            
            if (accounts == null || accounts.isEmpty()) {
                showError("CSV-tuontivirhe", "Tietokannassa ei ole tilejä. Luo ensin tilikartta.");
                return;
            }
            
            if (period == null) {
                showError("CSV-tuontivirhe", "Avaa ensin tilikausi ennen CSV-tuontia.");
                return;
            }
            
            var dialog = kirjanpito.ui.javafx.dialogs.CSVImportDialog.Companion.create(
                stage, accounts, period);
            
            if (dialog.showAndWait()) {
                List<kirjanpito.ui.javafx.dialogs.CSVImportDialog.ImportedEntry> importedEntries = 
                    dialog.getImportedEntries();
                
                if (importedEntries != null && !importedEntries.isEmpty()) {
                    // Import entries as new documents using DAO directly
                    Session session = null;
                    int imported = 0;
                    
                    try {
                        session = dataSource.openSession();
                        var documentDAO = dataSource.getDocumentDAO(session);
                        var entryDAO = dataSource.getEntryDAO(session);
                        
                        // Get next document number
                        int nextNumber = documentDAO.getCountByPeriodId(period.getId(), 1) + 1;
                        
                        for (var importEntry : importedEntries) {
                            try {
                                // Create a new document for each entry
                                kirjanpito.db.Document doc = new kirjanpito.db.Document();
                                doc.setDate(java.sql.Date.valueOf(importEntry.getDate()));
                                doc.setPeriodId(period.getId());
                                doc.setNumber(nextNumber++);
                                
                                documentDAO.save(doc);
                                
                                // Create debit entry (bank account)
                                kirjanpito.db.Entry bankEntry = new kirjanpito.db.Entry();
                                bankEntry.setDocumentId(doc.getId());
                                bankEntry.setAccountId(importEntry.getAccount().getId());
                                bankEntry.setDescription(importEntry.getDescription());
                                bankEntry.setRowNumber(0);
                                
                                if (importEntry.getAmount().compareTo(java.math.BigDecimal.ZERO) >= 0) {
                                    bankEntry.setDebit(true);
                                    bankEntry.setAmount(importEntry.getAmount());
                                } else {
                                    bankEntry.setDebit(false);
                                    bankEntry.setAmount(importEntry.getAmount().abs());
                                }
                                
                                entryDAO.save(bankEntry);
                                imported++;
                                
                            } catch (Exception e) {
                                System.err.println("Error importing entry: " + e.getMessage());
                            }
                        }
                        
                        // Commit the transaction
                        session.commit();
                        
                    } finally {
                        if (session != null) {
                            session.close();
                        }
                    }
                    
                    // Refresh document list
                    loadAllData();
                    
                    showInfo("Tuonti valmis", 
                        "Tuotiin " + imported + " tositetta.\n\n" +
                        "Huom: Vastatili on jätettävä tyhjäksi - täydennä manuaalisesti.");
                }
            }
            
        } catch (Exception e) {
            showError("CSV-tuontivirhe", e.getMessage());
        }
    }
    
    @FXML
    private void handleDebug() {
        DebugInfoDialogFX dialog = new DebugInfoDialogFX(stage, registry);
        dialog.show();
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
                // Poista vanha tiedosto
                file.delete();
            }
            
            // Avaa uusi tyhjä tietokanta
            openDatabase(file);
            
            // Tarkista onko tietokanta tyhjä (ei tilejä) ja näytä tilikarttamallin valinta
            if (accounts == null || accounts.isEmpty()) {
                initializeNewDatabase();
            }
            
        } catch (Exception e) {
            showError("Virhe luotaessa tietokantaa", e.getMessage());
        }
    }
    
    /**
     * Alustaa uuden tyhjän tietokannan tilikarttamallilla.
     */
    private void initializeNewDatabase() {
        System.out.println("=== initializeNewDatabase() kutsuttu ===");
        if (registry == null || dataSource == null) {
            System.out.println("registry tai dataSource on null, palataan");
            return;
        }
        
        try {
            // Luo model tilikarttamallien hakua varten
            DataSourceInitializationModel model = new DataSourceInitializationModel();
            System.out.println("Tilikarttamalleja: " + model.getModelCount());
            
            // Näytä dialogi tilikarttamallin valintaan
            DataSourceInitializationDialogFX dialog = new DataSourceInitializationDialogFX(
                stage, registry, model);
            System.out.println("Näytetään dialogi...");
            dialog.show();
            System.out.println("Dialogi suljettu");
            
            // Hae worker ja odota sen valmistumista
            DataSourceInitializationWorker worker = dialog.getWorker();
            System.out.println("Worker: " + (worker != null ? "OK" : "NULL (peruutettu)"));
            if (worker != null) {
                // Odota workerin valmistumista (get() blokkaa kunnes valmis)
                try {
                    System.out.println("Odotetaan workerin valmistumista...");
                    worker.get(); // Blokkaa kunnes worker on valmis
                    System.out.println("Worker valmis, ladataan data...");
                    loadAllData();
                    setStatus("Tietokanta alustettu");
                } catch (Exception ex) {
                    showError("Virhe alustettaessa tietokantaa", ex.getMessage());
                    ex.printStackTrace();
                }
            }
            
        } catch (Exception e) {
            showError("Virhe alustettaessa tietokantaa", e.getMessage());
            e.printStackTrace();
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
            
            // Lataa registry data
            try {
                registry.fetchChartOfAccounts(); // Lataa tilit registryyn
                registry.fetchDocumentTypes();
                registry.fetchEntryTemplates();
            } catch (DataAccessException e) {
                System.err.println("Virhe ladattaessa registry dataa: " + e.getMessage());
                e.printStackTrace();
            }
            
            // Tallenna viimeisimpien tietokantojen listaan
            String dbUrl = "jdbc:sqlite:" + file.getAbsolutePath().replace(File.separatorChar, '/');
            AppSettings settings = AppSettings.getInstance();
            settings.set("database.url", dbUrl);
            settings.save(); // Tallenna asetukset heti levylle
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
        
        // Alusta auto-complete
        autoCompleteSupport = new TreeMapAutoCompleteSupport();
        
        // Päivitä description-sarakkeen cell factory auto-completella
        descriptionCol.setCellFactory(col -> new DescriptionTableCell(
            autoCompleteSupport,
            rowIndex -> {
                if (rowIndex >= 0 && rowIndex < entries.size()) {
                    Account acc = entries.get(rowIndex).getAccount();
                    return acc != null ? acc.getId() : -1;
                }
                return -1;
            }
        ));
        
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
                
                // Päivitä valikot
                updateEntryTemplateMenu();
                updateDocumentTypeMenu();
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
            
            // Lisää viennit auto-complete tukeen
            if (autoCompleteSupport != null) {
                for (Entry entry : currentEntries) {
                    autoCompleteSupport.addEntry(entry);
                }
            }
            
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
    
    /**
     * Päivittää vientimallivalikon dynaamisesti.
     */
    private void updateEntryTemplateMenu() {
        if (entryTemplateMenu == null || registry == null) return;
        
        // Poista kaikki paitsi viimeiset kaksi itemiä (separaattori + Muokkaa + Luo tositteesta)
        // Tallenna viimeiset 3 (separator + 2 menuitem)
        int itemCount = entryTemplateMenu.getItems().size();
        var lastItems = new java.util.ArrayList<>(entryTemplateMenu.getItems().subList(Math.max(0, itemCount - 3), itemCount));
        
        entryTemplateMenu.getItems().clear();
        
        try {
            List<kirjanpito.db.EntryTemplate> templates = registry.getEntryTemplates();
            
            if (templates != null && !templates.isEmpty()) {
                int prevNumber = -1;
                int count = 0;
                
                for (kirjanpito.db.EntryTemplate template : templates) {
                    if (template.getNumber() != prevNumber) {
                        prevNumber = template.getNumber();
                        
                        MenuItem item = new MenuItem(template.getName());
                        final int templateNumber = template.getNumber();
                        item.setOnAction(e -> applyEntryTemplate(templateNumber));
                        
                        // Alt+1 - Alt+0 pikanäppäimet ensimmäisille 10:lle
                        if (template.getNumber() >= 1 && template.getNumber() <= 10) {
                            int keyNum = template.getNumber() % 10;
                            KeyCode keyCode = KeyCode.valueOf("DIGIT" + keyNum);
                            item.setAccelerator(new KeyCodeCombination(keyCode, KeyCombination.ALT_DOWN));
                        }
                        
                        entryTemplateMenu.getItems().add(item);
                        count++;
                    }
                }
                
                if (count == 0) {
                    MenuItem emptyItem = new MenuItem("Ei vientimalleja");
                    emptyItem.setDisable(true);
                    entryTemplateMenu.getItems().add(emptyItem);
                }
            } else {
                MenuItem emptyItem = new MenuItem("Ei vientimalleja");
                emptyItem.setDisable(true);
                entryTemplateMenu.getItems().add(emptyItem);
            }
            
        } catch (Exception e) {
            System.err.println("Virhe ladattaessa vientimalleja: " + e.getMessage());
            MenuItem errorItem = new MenuItem("Virhe ladattaessa malleja");
            errorItem.setDisable(true);
            entryTemplateMenu.getItems().add(errorItem);
        }
        
        // Lisää vakioitemet takaisin
        entryTemplateMenu.getItems().addAll(lastItems);
    }
    
    /**
     * Päivittää tositelajivalikon dynaamisesti.
     */
    private void updateDocumentTypeMenu() {
        if (docTypeMenu == null || registry == null) {
            return;
        }
        
        docTypeMenu.getItems().clear();
        
        try {
            documentTypes = registry.getDocumentTypes();
            
            if (documentTypes != null && !documentTypes.isEmpty()) {
                char[] accelerators = {'Q', 'W', 'E', 'R', 'T', 'Y', 'U', 'I', 'O', 'P'};
                int index = 0;
                
                for (DocumentType docType : documentTypes) {
                    CheckMenuItem item = new CheckMenuItem(docType.getName());
                    final int docTypeIndex = index;
                    item.setOnAction(e -> selectDocumentType(docTypeIndex));
                    
                    // Merkitään valittu tositelaji
                    item.setSelected(index == currentDocumentTypeIndex);
                    
                    // Alt+Q, Alt+W, jne. pikanäppäimet ensimmäisille 10:lle
                    if (docType.getNumber() >= 1 && docType.getNumber() <= 10) {
                        KeyCode keyCode = KeyCode.valueOf(String.valueOf(accelerators[docType.getNumber() - 1]));
                        item.setAccelerator(new KeyCodeCombination(keyCode, KeyCombination.ALT_DOWN));
                    }
                    
                    docTypeMenu.getItems().add(item);
                    index++;
                }
            }
            
            if (docTypeMenu.getItems().isEmpty()) {
                MenuItem emptyItem = new MenuItem("Ei tositelajeja");
                emptyItem.setDisable(true);
                docTypeMenu.getItems().add(emptyItem);
            }
            
            // Lisää erotin ja Muokkaa-komento
            docTypeMenu.getItems().add(new SeparatorMenuItem());
            MenuItem editItem = new MenuItem("Muokkaa tositelajeja...");
            editItem.setOnAction(e -> handleDocumentTypes());
            docTypeMenu.getItems().add(editItem);
            
        } catch (Exception e) {
            System.err.println("Virhe ladattaessa tositelajeja: " + e.getMessage());
            MenuItem errorItem = new MenuItem("Virhe ladattaessa tositelajeja");
            errorItem.setDisable(true);
            docTypeMenu.getItems().add(errorItem);
        }
    }
    
    /**
     * Valitsee tositelajin ja lataa sen tositteet.
     */
    private void selectDocumentType(int index) {
        if (documentTypes == null || index < 0 || index >= documentTypes.size()) return;
        
        // Tallenna nykyinen tosite ensin
        handleSave();
        
        currentDocumentTypeIndex = index;
        DocumentType selectedType = documentTypes.get(index);
        
        // Päivitä valintamerkit menussa
        updateDocumentTypeMenuSelection();
        
        // Lataa valitun tositelajin tositteet
        loadDocumentsForType(selectedType.getNumber());
        
        setStatus("Tositelaji: " + selectedType.getName());
    }
    
    /**
     * Päivittää tositelajivalikon valintamerkit.
     */
    private void updateDocumentTypeMenuSelection() {
        if (docTypeMenu == null) return;
        
        int index = 0;
        for (MenuItem item : docTypeMenu.getItems()) {
            if (item instanceof CheckMenuItem) {
                ((CheckMenuItem) item).setSelected(index == currentDocumentTypeIndex);
                index++;
            }
        }
    }
    
    /**
     * Lataa tietyn tositelajin tositteet.
     */
    private void loadDocumentsForType(int docTypeNumber) {
        if (dataSource == null || currentPeriod == null) return;
        
        Session session = null;
        try {
            session = dataSource.openSession();
            
            DocumentDAO documentDAO = dataSource.getDocumentDAO(session);
            documentCount = documentDAO.getCountByPeriodId(currentPeriod.getId(), docTypeNumber);
            documents = documentDAO.getByPeriodId(currentPeriod.getId(), docTypeNumber);
            
            System.out.println("Ladattu " + documents.size() + " tositetta tositelajille " + docTypeNumber);
            
            // Siirry ensimmäiseen tositteeseen
            if (!documents.isEmpty()) {
                currentDocumentIndex = 0;
                loadDocument(documents.get(0));
            } else {
                currentDocumentIndex = -1;
                currentDocument = null;
                currentEntries = null;
                entries.clear();
            }
            
            updateUI();
            
        } catch (Exception e) {
            System.err.println("Virhe ladattaessa tositteita: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }
    
    /**
     * Soveltaa vientimallin nykyiseen tositteeseen.
     */
    private void applyEntryTemplate(int templateNumber) {
        if (registry == null || currentDocument == null) {
            setStatus("Avaa ensin tosite");
            return;
        }
        
        try {
            List<kirjanpito.db.EntryTemplate> templates = registry.getEntryTemplates();
            
            // Etsi mallin viennit
            java.util.List<kirjanpito.db.EntryTemplate> templateEntries = new java.util.ArrayList<>();
            String templateName = null;
            
            for (kirjanpito.db.EntryTemplate t : templates) {
                if (t.getNumber() == templateNumber) {
                    templateEntries.add(t);
                    if (templateName == null) {
                        templateName = t.getName();
                    }
                }
            }
            
            if (templateEntries.isEmpty()) {
                setStatus("Vientimallia ei löytynyt");
                return;
            }
            
            // Lisää mallin viennit tositteeseen
            int addedCount = 0;
            for (kirjanpito.db.EntryTemplate t : templateEntries) {
                EntryRowModel newRow = new EntryRowModel();
                newRow.setRowNumber(entries.size() + 1);
                
                // Etsi tili
                for (Account acc : accounts) {
                    if (acc.getId() == t.getAccountId()) {
                        newRow.setAccount(acc);
                        break;
                    }
                }
                
                newRow.setDescription(t.getDescription() != null ? t.getDescription() : "");
                
                if (t.isDebit()) {
                    newRow.setDebit(t.getAmount());
                    newRow.setCredit(null);
                } else {
                    newRow.setDebit(null);
                    newRow.setCredit(t.getAmount());
                }
                
                entries.add(newRow);
                addedCount++;
            }
            
            entryTable.refresh();
            updateTotals();
            setStatus("Lisätty vientimalli: " + templateName + " (" + addedCount + " vientiä)");
            
        } catch (Exception e) {
            showError("Virhe sovellettaessa vientimallia", e.getMessage());
        }
    }
    
    private void showNotImplemented(String feature) {
        setStatus(feature + " - ei vielä toteutettu");
    }
    
    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
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
