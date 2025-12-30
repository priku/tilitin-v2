package kirjanpito.ui.javafx;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;

import kirjanpito.db.*;
import kirjanpito.db.sqlite.SQLiteDataSource;
import kirjanpito.ui.javafx.cells.*;

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
        
        // Double-click to edit
        entryTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                TablePosition<EntryRowModel, ?> pos = entryTable.getFocusModel().getFocusedCell();
                if (pos != null) {
                    entryTable.edit(pos.getRow(), pos.getTableColumn());
                }
            }
        });
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
        showNotImplemented("Kopioi");
    }
    
    @FXML
    private void handlePaste() {
        showNotImplemented("Liitä");
    }
    
    // Navigation handlers
    @FXML
    private void handlePrevDocument() {
        if (documents == null || documents.isEmpty()) return;
        
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
        
        currentDocumentIndex = 0;
        loadDocument(documents.get(0));
        setStatus("Ensimmäinen tosite: " + currentDocument.getNumber());
    }
    
    @FXML
    private void handleLastDocument() {
        if (documents == null || documents.isEmpty()) return;
        
        currentDocumentIndex = documents.size() - 1;
        loadDocument(documents.get(currentDocumentIndex));
        setStatus("Viimeinen tosite: " + currentDocument.getNumber());
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
    
    @FXML
    private void handleAttachment() {
        showNotImplemented("Liite");
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
