package kirjanpito.ui.javafx.dialogs;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.concurrent.Task;

import kirjanpito.db.Account;
import kirjanpito.db.DTOCallback;
import kirjanpito.db.DataAccessException;
import kirjanpito.db.DataSource;
import kirjanpito.db.Document;
import kirjanpito.db.Entry;
import kirjanpito.db.EntryDAO;
import kirjanpito.db.Period;
import kirjanpito.db.Session;
import kirjanpito.util.AppSettings;
import kirjanpito.util.CSVWriter;
import kirjanpito.util.Registry;

/**
 * JavaFX-dialogi kirjanpitotietojen viennille CSV-muotoon.
 * 
 * Tukee:
 * - CSV-vienti puolipisteerottimella (Excel Suomi)
 * - UTF-8 BOM merkistötunniste
 * - Vientien (entries) vienti valitulta tilikaudelta
 */
public class DataExportDialogFX {
    
    private final Stage stage;
    private final Registry registry;
    private final Period period;
    private boolean exported = false;
    
    // UI components
    private ComboBox<String> formatCombo;
    private ComboBox<String> delimiterCombo;
    private CheckBox includeHeadersCheckBox;
    private TextField filePathField;
    private Button browseButton;
    private Button exportButton;
    private Button cancelButton;
    private ProgressBar progressBar;
    private Label statusLabel;
    
    /**
     * Luo uuden DataExportDialogFX-dialogin.
     *
     * @param owner omistava ikkuna
     * @param registry kirjanpitorekisteri
     * @param period tilikausi jonka tiedot viedään
     */
    public DataExportDialogFX(Window owner, Registry registry, Period period) {
        this.registry = registry;
        this.period = period;
        
        stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initOwner(owner);
        stage.setTitle("Vie tiedostoon");
        stage.setResizable(false);
        
        initializeUI();
    }
    
    private void initializeUI() {
        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #f5f5f5;");
        
        // Title
        Label titleLabel = new Label("Vie kirjanpitotiedot");
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        
        // Period info
        SimpleDateFormat dateFormat = new SimpleDateFormat("d.M.yyyy");
        String periodInfo = String.format("Tilikausi: %s - %s",
            dateFormat.format(period.getStartDate()),
            dateFormat.format(period.getEndDate()));
        Label periodLabel = new Label(periodInfo);
        periodLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666666;");
        
        // Settings grid
        GridPane settingsGrid = new GridPane();
        settingsGrid.setHgap(10);
        settingsGrid.setVgap(10);
        
        // Format selection
        Label formatLabel = new Label("Tiedostomuoto:");
        formatCombo = new ComboBox<>();
        formatCombo.getItems().addAll("CSV (puolipiste)", "CSV (pilkku)", "CSV (tabulaattori)");
        formatCombo.setValue("CSV (puolipiste)");
        formatCombo.setPrefWidth(200);
        
        settingsGrid.add(formatLabel, 0, 0);
        settingsGrid.add(formatCombo, 1, 0);
        
        // Include headers checkbox
        includeHeadersCheckBox = new CheckBox("Sisällytä otsikkorivi");
        includeHeadersCheckBox.setSelected(true);
        settingsGrid.add(includeHeadersCheckBox, 1, 1);
        
        // File path selection
        Label fileLabel = new Label("Tiedosto:");
        filePathField = new TextField();
        filePathField.setPrefWidth(300);
        filePathField.setPromptText("Valitse tallennussijainti...");
        filePathField.setEditable(false);
        
        browseButton = new Button("Selaa...");
        browseButton.setOnAction(e -> browseForFile());
        
        HBox fileBox = new HBox(10);
        fileBox.getChildren().addAll(filePathField, browseButton);
        HBox.setHgrow(filePathField, Priority.ALWAYS);
        
        settingsGrid.add(fileLabel, 0, 2);
        settingsGrid.add(fileBox, 1, 2);
        
        // Progress section
        VBox progressBox = new VBox(5);
        progressBar = new ProgressBar(0);
        progressBar.setPrefWidth(400);
        progressBar.setVisible(false);
        
        statusLabel = new Label("");
        statusLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #666666;");
        
        progressBox.getChildren().addAll(progressBar, statusLabel);
        
        // Button bar
        HBox buttonBar = new HBox(10);
        buttonBar.setAlignment(Pos.CENTER_RIGHT);
        
        exportButton = new Button("Vie");
        exportButton.setDefaultButton(true);
        exportButton.setDisable(true);
        exportButton.setOnAction(e -> performExport());
        
        cancelButton = new Button("Peruuta");
        cancelButton.setCancelButton(true);
        cancelButton.setOnAction(e -> stage.close());
        
        buttonBar.getChildren().addAll(exportButton, cancelButton);
        
        // Assemble
        root.getChildren().addAll(
            titleLabel,
            periodLabel,
            settingsGrid,
            progressBox,
            buttonBar
        );
        
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.sizeToScene();
    }
    
    private void browseForFile() {
        AppSettings settings = AppSettings.getInstance();
        String lastDir = settings.getString("csv-directory", System.getProperty("user.home"));
        
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Vie tiedostoon");
        fileChooser.setInitialDirectory(new File(lastDir));
        
        // Set extension filter based on format
        FileChooser.ExtensionFilter csvFilter = 
            new FileChooser.ExtensionFilter("CSV-tiedostot (*.csv)", "*.csv");
        fileChooser.getExtensionFilters().add(csvFilter);
        
        // Generate default filename
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String defaultName = "kirjanpito_" + dateFormat.format(period.getEndDate()) + ".csv";
        fileChooser.setInitialFileName(defaultName);
        
        File file = fileChooser.showSaveDialog(stage);
        if (file != null) {
            // Ensure .csv extension
            String path = file.getAbsolutePath();
            if (!path.toLowerCase().endsWith(".csv")) {
                path += ".csv";
                file = new File(path);
            }
            
            filePathField.setText(file.getAbsolutePath());
            exportButton.setDisable(false);
            
            // Save directory for next time
            settings.set("csv-directory", file.getParent());
        }
    }
    
    private void performExport() {
        File file = new File(filePathField.getText());
        
        // Confirm overwrite if file exists
        if (file.exists()) {
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("Vahvista korvaus");
            confirmAlert.setHeaderText("Tiedosto on jo olemassa");
            confirmAlert.setContentText("Haluatko korvata tiedoston:\n" + file.getName() + "?");
            
            Optional<ButtonType> result = confirmAlert.showAndWait();
            if (result.isEmpty() || result.get() != ButtonType.OK) {
                return;
            }
        }
        
        // Disable UI during export
        exportButton.setDisable(true);
        browseButton.setDisable(true);
        formatCombo.setDisable(true);
        includeHeadersCheckBox.setDisable(true);
        progressBar.setVisible(true);
        progressBar.setProgress(-1); // Indeterminate
        statusLabel.setText("Vie tietoja...");
        
        // Get delimiter based on format selection
        char delimiter = getDelimiter();
        boolean includeHeaders = includeHeadersCheckBox.isSelected();
        
        // Run export in background
        Task<Void> exportTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                exportToCSV(file, delimiter, includeHeaders);
                return null;
            }
        };
        
        exportTask.setOnSucceeded(e -> {
            progressBar.setProgress(1.0);
            statusLabel.setText("Vienti valmis: " + file.getName());
            exported = true;
            
            // Show success message
            Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
            successAlert.setTitle("Vienti valmis");
            successAlert.setHeaderText(null);
            successAlert.setContentText("Tiedot viety onnistuneesti tiedostoon:\n" + file.getAbsolutePath());
            successAlert.showAndWait();
            
            stage.close();
        });
        
        exportTask.setOnFailed(e -> {
            progressBar.setVisible(false);
            statusLabel.setText("Vienti epäonnistui");
            
            // Re-enable UI
            exportButton.setDisable(false);
            browseButton.setDisable(false);
            formatCombo.setDisable(false);
            includeHeadersCheckBox.setDisable(false);
            
            // Show error
            Throwable error = exportTask.getException();
            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
            errorAlert.setTitle("Virhe viennissä");
            errorAlert.setHeaderText("Tiedoston vienti epäonnistui");
            errorAlert.setContentText(error != null ? error.getMessage() : "Tuntematon virhe");
            errorAlert.showAndWait();
        });
        
        Thread exportThread = new Thread(exportTask);
        exportThread.setDaemon(true);
        exportThread.start();
    }
    
    private char getDelimiter() {
        String format = formatCombo.getValue();
        if (format.contains("pilkku")) {
            return ',';
        } else if (format.contains("tabulaattori")) {
            return '\t';
        }
        return ';'; // Default: semicolon
    }
    
    private void exportToCSV(File file, char delimiter, boolean includeHeaders) 
            throws IOException, DataAccessException {
        
        DataSource dataSource = registry.getDataSource();
        Session sess = null;
        
        try {
            sess = dataSource.openSession();
            
            // Load documents for this period
            List<Document> documents = dataSource.getDocumentDAO(sess)
                .getByPeriodId(period.getId(), 1);
            
            final HashMap<Integer, Document> documentMap = new HashMap<>();
            for (Document document : documents) {
                documentMap.put(document.getId(), document);
            }
            
            // Create file with UTF-8 BOM for Excel compatibility
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(0xEF); // UTF-8 BOM
            fos.write(0xBB);
            fos.write(0xBF);
            
            OutputStreamWriter osw = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
            BufferedWriter bw = new BufferedWriter(osw);
            
            final CSVWriter writer = new CSVWriter(bw);
            writer.setDelimiter(delimiter);
            
            final SimpleDateFormat dateFormat = new SimpleDateFormat("d.M.yyyy");
            final DecimalFormat numberFormat = new DecimalFormat("0.00",
                new java.text.DecimalFormatSymbols(java.util.Locale.US));
            numberFormat.setGroupingUsed(false);
            
            // Write header row if requested
            if (includeHeaders) {
                writer.writeField("Tosite");
                writer.writeField("Päivämäärä");
                writer.writeField("Nro");
                writer.writeField("Tili");
                writer.writeField("Debet");
                writer.writeField("Kredit");
                writer.writeField("Selite");
                writer.writeLine();
            }
            
            // Export entries
            final IOException[] exception = {null};
            
            dataSource.getEntryDAO(sess).getByPeriodId(period.getId(),
                EntryDAO.ORDER_BY_DOCUMENT_NUMBER, new DTOCallback<Entry>() {
                    public void process(Entry entry) {
                        Document document = documentMap.get(entry.getDocumentId());
                        if (document == null) return;
                        
                        Account account = registry.getAccountById(entry.getAccountId());
                        if (account == null) return;
                        
                        try {
                            writer.writeField(Integer.toString(document.getNumber()));
                            writer.writeField(dateFormat.format(document.getDate()));
                            writer.writeField(account.getNumber());
                            writer.writeField(account.getName());
                            writer.writeField(entry.isDebit() ?
                                numberFormat.format(entry.getAmount()) : "");
                            writer.writeField(!entry.isDebit() ?
                                numberFormat.format(entry.getAmount()) : "");
                            writer.writeField(entry.getDescription());
                            writer.writeLine();
                        } catch (IOException e) {
                            exception[0] = e;
                        }
                    }
                });
            
            writer.close();
            
            if (exception[0] != null) {
                throw exception[0];
            }
            
            sess.commit();
        } catch (Exception e) {
            if (sess != null) sess.rollback();
            throw e;
        } finally {
            if (sess != null) sess.close();
        }
    }
    
    /**
     * Näyttää dialogin ja odottaa käyttäjän toimia.
     *
     * @return true jos vienti suoritettiin onnistuneesti
     */
    public boolean showAndWait() {
        stage.showAndWait();
        return exported;
    }
    
    /**
     * Luo ja näyttää vientidialogin.
     *
     * @param owner omistava ikkuna
     * @param registry kirjanpitorekisteri
     * @param period tilikausi
     * @return true jos vienti onnistui
     */
    public static boolean showExportDialog(Window owner, Registry registry, Period period) {
        DataExportDialogFX dialog = new DataExportDialogFX(owner, registry, period);
        return dialog.showAndWait();
    }
}
