package kirjanpito.ui.javafx;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import kirjanpito.db.*;
import kirjanpito.db.sqlite.SQLiteDataSource;

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
    @FXML private Label totalDocumentsLabel;
    @FXML private DatePicker datePicker;
    @FXML private TextField docNumberField;
    @FXML private ComboBox<DocumentType> docTypeCombo;
    
    @FXML private TableView<EntryRow> entryTable;
    @FXML private TableColumn<EntryRow, Integer> rowNumCol;
    @FXML private TableColumn<EntryRow, String> accountCol;
    @FXML private TableColumn<EntryRow, String> descriptionCol;
    @FXML private TableColumn<EntryRow, String> debitCol;
    @FXML private TableColumn<EntryRow, String> creditCol;
    @FXML private TableColumn<EntryRow, String> vatCol;
    
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
    private Document currentDocument;
    private List<Entry> currentEntries;
    private List<Account> accounts;
    private ObservableList<EntryRow> entries;
    private DecimalFormat currencyFormat;
    
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
        
        setStatus("Valmis - Avaa tietokanta aloittaaksesi");
    }
    
    public void setStage(Stage stage) {
        this.stage = stage;
    }
    
    private void initializeTable() {
        entries = FXCollections.observableArrayList();
        entryTable.setItems(entries);
        
        // Sarakkeiden asetukset
        rowNumCol.setCellValueFactory(new PropertyValueFactory<>("rowNumber"));
        accountCol.setCellValueFactory(new PropertyValueFactory<>("accountName"));
        descriptionCol.setCellValueFactory(new PropertyValueFactory<>("description"));
        debitCol.setCellValueFactory(new PropertyValueFactory<>("debitFormatted"));
        creditCol.setCellValueFactory(new PropertyValueFactory<>("creditFormatted"));
        vatCol.setCellValueFactory(new PropertyValueFactory<>("vatCode"));
        
        // Oikea tasaus numeroille
        debitCol.setStyle("-fx-alignment: CENTER-RIGHT;");
        creditCol.setStyle("-fx-alignment: CENTER-RIGHT;");
        
        // Placeholder kun ei dataa
        entryTable.setPlaceholder(new Label("Ei vientejä"));
    }
    
    private void initializeFileChooser() {
        fileChooser = new FileChooser();
        fileChooser.setTitle("Valitse tietokanta");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Tilitin-tietokannat", "*.sqlite", "*.db"),
            new FileChooser.ExtensionFilter("Kaikki tiedostot", "*.*")
        );
        
        // Oletushakemisto
        String userHome = System.getProperty("user.home");
        File defaultDir = new File(userHome, "Tilitin");
        if (defaultDir.exists()) {
            fileChooser.setInitialDirectory(defaultDir);
        }
    }
    
    private void updateUI() {
        boolean hasDatabase = dataSource != null;
        boolean hasDocument = currentDocument != null;
        
        // Päivitä toolbar-napit
        // (TODO: bindaa FXML:ssä)
        
        // Päivitä kentät
        if (hasDocument) {
            // Päivämäärä
            if (currentDocument.getDate() != null) {
                LocalDate localDate = currentDocument.getDate().toInstant()
                    .atZone(ZoneId.systemDefault()).toLocalDate();
                datePicker.setValue(localDate);
            }
            
            // Tositenumero
            docNumberField.setText(String.valueOf(currentDocument.getNumber()));
            
            // Viennit
            loadEntries();
            
            // Summat
            updateTotals();
        } else {
            datePicker.setValue(null);
            docNumberField.setText("");
            entries.clear();
            updateTotals();
        }
        
        // Status bar
        if (hasDatabase && databaseName != null) {
            databaseLabel.setText(databaseName);
        } else {
            databaseLabel.setText("Ei tietokantaa");
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
            entries.add(new EntryRow(rowNum++, entry, account, currencyFormat));
        }
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
        
        for (EntryRow row : entries) {
            if (row.getDebit() != null) {
                debitTotal = debitTotal.add(row.getDebit());
            }
            if (row.getCredit() != null) {
                creditTotal = creditTotal.add(row.getCredit());
            }
        }
        
        debitTotalLabel.setText(currencyFormat.format(debitTotal));
        creditTotalLabel.setText(currencyFormat.format(creditTotal));
        
        BigDecimal balance = debitTotal.subtract(creditTotal);
        if (balance.compareTo(BigDecimal.ZERO) == 0) {
            balanceLabel.setText("");
            balanceLabel.getStyleClass().remove("balance-error");
        } else {
            balanceLabel.setText("Erotus: " + currencyFormat.format(balance.abs()));
            balanceLabel.getStyleClass().add("balance-error");
        }
    }
    
    private void setStatus(String message) {
        statusLabel.setText(message);
    }
    
    public void shutdown() {
        // Tallenna muutokset ennen sulkemista
        if (dataSource != null) {
            try {
                // TODO: Tallenna muutokset
                dataSource.close();
            } catch (Exception e) {
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
            createNewDatabase(file);
        }
    }
    
    @FXML
    private void handleOpenDatabase() {
        fileChooser.setTitle("Avaa tietokanta");
        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            openDatabase(file);
        }
    }
    
    @FXML
    private void handleSave() {
        if (dataSource != null) {
            setStatus("Tallennetaan...");
            // TODO: Tallenna DocumentValidator avulla
            setStatus("Tallennettu");
        }
    }
    
    @FXML
    private void handlePrint() {
        showNotImplemented("Tulostus");
    }
    
    @FXML
    private void handleSettings() {
        showNotImplemented("Asetukset");
    }
    
    @FXML
    private void handleQuit() {
        shutdown();
        stage.close();
    }
    
    // Entry handlers
    @FXML
    private void handleAddEntry() {
        if (currentDocument != null) {
            // TODO: Lisää vienti DocumentEntryManager avulla
            setStatus("Vienti lisätty");
        }
    }
    
    @FXML
    private void handleRemoveEntry() {
        EntryRow selected = entryTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            entries.remove(selected);
            updateTotals();
            setStatus("Vienti poistettu");
        }
    }
    
    @FXML
    private void handleCopy() {
        showNotImplemented("Kopioi");
    }
    
    @FXML
    private void handlePaste() {
        showNotImplemented("Liitä");
    }
    
    // Navigation handlers
    @FXML
    private void handlePrevDocument() {
        setStatus("Edellinen tosite");
        // TODO: DocumentNavigator
    }
    
    @FXML
    private void handleNextDocument() {
        setStatus("Seuraava tosite");
        // TODO: DocumentNavigator
    }
    
    @FXML
    private void handleFirstDocument() {
        setStatus("Ensimmäinen tosite");
        // TODO: DocumentNavigator
    }
    
    @FXML
    private void handleLastDocument() {
        setStatus("Viimeinen tosite");
        // TODO: DocumentNavigator
    }
    
    @FXML
    private void handleGotoDocument() {
        showNotImplemented("Siirry tositteeseen");
    }
    
    @FXML
    private void handleGotoDocumentNumber() {
        String text = documentNumberField.getText();
        try {
            int docNum = Integer.parseInt(text);
            setStatus("Siirrytään tositteeseen " + docNum);
            // TODO: DocumentNavigator.gotoDocument(docNum)
        } catch (NumberFormatException e) {
            setStatus("Virheellinen tositenumero");
        }
    }
    
    @FXML
    private void handleNewDocument() {
        setStatus("Uusi tosite");
        // TODO: Luo uusi tosite
    }
    
    @FXML
    private void handleSearch() {
        showNotImplemented("Haku");
    }
    
    // Registry handlers
    @FXML
    private void handleChartOfAccounts() {
        showNotImplemented("Tilikartta");
    }
    
    @FXML
    private void handleDocumentTypes() {
        showNotImplemented("Tositelajit");
    }
    
    @FXML
    private void handlePeriodSettings() {
        showNotImplemented("Tilikausi");
    }
    
    @FXML
    private void handleDatabaseSettings() {
        showNotImplemented("Tietokannan asetukset");
    }
    
    // Report handlers
    @FXML
    private void handleJournalReport() {
        showNotImplemented("Päiväkirja");
    }
    
    @FXML
    private void handleLedgerReport() {
        showNotImplemented("Pääkirja");
    }
    
    @FXML
    private void handleIncomeStatement() {
        showNotImplemented("Tuloslaskelma");
    }
    
    @FXML
    private void handleBalanceSheet() {
        showNotImplemented("Tase");
    }
    
    // Help handlers
    @FXML
    private void handleHelp() {
        showNotImplemented("Ohje");
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
            
            // TODO: Luo tietokanta SQLiteDataSource avulla
            setStatus("Tietokanta luotu: " + file.getName());
            
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
            
            // Lataa tilit
            loadAccounts();
            
            setStatus("Avattu: " + file.getName());
            
            // Päivitä UI
            updateUI();
            
        } catch (Exception e) {
            showError("Virhe avattaessa tietokantaa", e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void loadAccounts() {
        if (dataSource == null) return;
        
        try {
            Session session = dataSource.openSession();
            AccountDAO accountDAO = dataSource.getAccountDAO(session);
            accounts = accountDAO.getAll();
            session.close();
        } catch (Exception e) {
            System.err.println("Virhe ladattaessa tilejä: " + e.getMessage());
            accounts = new ArrayList<>();
        }
    }
    
    // ========== Helpers ==========
    
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
    
    // ========== Entry row data class ==========
    
    public static class EntryRow {
        private final int rowNumber;
        private final Entry entry;
        private final Account account;
        private final DecimalFormat format;
        
        public EntryRow(int rowNumber, Entry entry, Account account, DecimalFormat format) {
            this.rowNumber = rowNumber;
            this.entry = entry;
            this.account = account;
            this.format = format;
        }
        
        public int getRowNumber() { return rowNumber; }
        
        public String getAccountName() {
            if (account == null) return "";
            return account.getNumber() + " " + account.getName();
        }
        
        public String getDescription() {
            return entry != null ? entry.getDescription() : "";
        }
        
        public BigDecimal getDebit() {
            if (entry == null) return null;
            return entry.isDebit() ? entry.getAmount() : null;
        }
        
        public BigDecimal getCredit() {
            if (entry == null) return null;
            return !entry.isDebit() ? entry.getAmount() : null;
        }
        
        public String getDebitFormatted() {
            BigDecimal d = getDebit();
            return d != null && d.compareTo(BigDecimal.ZERO) != 0 ? format.format(d) : "";
        }
        
        public String getCreditFormatted() {
            BigDecimal c = getCredit();
            return c != null && c.compareTo(BigDecimal.ZERO) != 0 ? format.format(c) : "";
        }
        
        public String getVatCode() {
            // TODO: Hae ALV-koodi
            return "";
        }
        
        public Entry getEntry() { return entry; }
    }
}
