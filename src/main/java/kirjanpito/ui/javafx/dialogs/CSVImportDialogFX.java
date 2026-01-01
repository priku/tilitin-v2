package kirjanpito.ui.javafx.dialogs;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import kirjanpito.db.Account;
import kirjanpito.db.Period;
import kirjanpito.util.CSVReader;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * JavaFX dialog for importing CSV bank statements.
 * Supports Finnish bank CSV formats (Nordea, OP, etc.).
 */
public class CSVImportDialogFX {

    private Stage dialog;
    private Window owner;
    private File csvFile;
    private List<Account> accounts;
    private Period period;
    
    // UI Components
    private Label fileLabel;
    private ComboBox<String> encodingCombo;
    private ComboBox<String> separatorCombo;
    private ComboBox<String> dateFormatCombo;
    private ComboBox<Integer> dateColumnCombo;
    private ComboBox<Integer> descriptionColumnCombo;
    private ComboBox<Integer> amountColumnCombo;
    private ComboBox<Account> accountCombo;
    private CheckBox skipHeaderCheckBox;
    private TableView<CSVRow> previewTable;
    private ObservableList<CSVRow> previewData;
    private Label statusLabel;
    
    // Result
    private List<ImportedEntry> importedEntries;
    private boolean okPressed = false;
    
    // CSV data
    private List<String[]> csvData;
    private int columnCount = 0;

    public CSVImportDialogFX(Window owner) {
        this.owner = owner;
        dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(owner);
        dialog.setTitle("Tuo CSV-tiedosto");
        dialog.setMinWidth(800);
        dialog.setMinHeight(600);

        createContent();
    }

    private void createContent() {
        VBox root = new VBox(12);
        root.setPadding(new Insets(16));
        root.setStyle("-fx-background-color: #ffffff;");

        // File selection section
        HBox fileSection = createFileSection();
        
        // Settings section
        HBox settingsSection = createSettingsSection();
        
        // Column mapping section
        VBox mappingSection = createMappingSection();
        
        // Preview section
        VBox previewSection = createPreviewSection();
        VBox.setVgrow(previewSection, Priority.ALWAYS);
        
        // Status and buttons
        HBox buttonBox = createButtonBox();

        root.getChildren().addAll(fileSection, settingsSection, mappingSection, previewSection, buttonBox);

        Scene scene = new Scene(root, 850, 650);
        dialog.setScene(scene);
    }
    
    private HBox createFileSection() {
        HBox section = new HBox(10);
        section.setAlignment(Pos.CENTER_LEFT);
        section.setPadding(new Insets(10));
        section.setStyle("-fx-border-color: #e0e0e0; -fx-border-radius: 4; -fx-background-color: #f9fafb; -fx-background-radius: 4;");
        
        Label label = new Label("Tiedosto:");
        label.setStyle("-fx-font-weight: bold;");
        
        fileLabel = new Label("Ei valittu");
        fileLabel.setStyle("-fx-text-fill: #64748b;");
        HBox.setHgrow(fileLabel, Priority.ALWAYS);
        
        Button browseButton = new Button("Selaa...");
        browseButton.setOnAction(e -> selectFile());
        
        section.getChildren().addAll(label, fileLabel, browseButton);
        return section;
    }
    
    private HBox createSettingsSection() {
        HBox section = new HBox(20);
        section.setAlignment(Pos.CENTER_LEFT);
        section.setPadding(new Insets(10));
        section.setStyle("-fx-border-color: #e0e0e0; -fx-border-radius: 4; -fx-background-color: #f9fafb; -fx-background-radius: 4;");
        
        // Encoding
        VBox encodingBox = new VBox(4);
        Label encodingLabel = new Label("Merkistö:");
        encodingCombo = new ComboBox<>();
        encodingCombo.getItems().addAll("UTF-8", "ISO-8859-1", "ISO-8859-15", "Windows-1252");
        encodingCombo.setValue("UTF-8");
        encodingCombo.setOnAction(e -> reloadFile());
        encodingBox.getChildren().addAll(encodingLabel, encodingCombo);
        
        // Separator
        VBox sepBox = new VBox(4);
        Label sepLabel = new Label("Erotin:");
        separatorCombo = new ComboBox<>();
        separatorCombo.getItems().addAll("Pilkku (,)", "Puolipiste (;)", "Sarkain (Tab)");
        separatorCombo.setValue("Pilkku (,)");
        separatorCombo.setOnAction(e -> reloadFile());
        sepBox.getChildren().addAll(sepLabel, separatorCombo);
        
        // Date format
        VBox dateFormatBox = new VBox(4);
        Label dateFormatLabel = new Label("Päivämäärämuoto:");
        dateFormatCombo = new ComboBox<>();
        dateFormatCombo.getItems().addAll("dd.MM.yyyy", "yyyy-MM-dd", "d.M.yyyy", "dd/MM/yyyy");
        dateFormatCombo.setValue("dd.MM.yyyy");
        dateFormatBox.getChildren().addAll(dateFormatLabel, dateFormatCombo);
        
        // Skip header
        skipHeaderCheckBox = new CheckBox("Ohita otsikkorivi");
        skipHeaderCheckBox.setSelected(true);
        skipHeaderCheckBox.setOnAction(e -> updatePreview());
        
        section.getChildren().addAll(encodingBox, sepBox, dateFormatBox, skipHeaderCheckBox);
        return section;
    }
    
    private VBox createMappingSection() {
        Label titleLabel = new Label("Sarakemääritykset");
        titleLabel.setStyle("-fx-font-weight: bold;");
        
        HBox mappingBox = new HBox(20);
        mappingBox.setAlignment(Pos.CENTER_LEFT);
        mappingBox.setPadding(new Insets(10));
        mappingBox.setStyle("-fx-border-color: #e0e0e0; -fx-border-radius: 4; -fx-background-color: #f9fafb; -fx-background-radius: 4;");
        
        // Date column
        VBox dateColBox = new VBox(4);
        Label dateColLabel = new Label("Päivämäärä:");
        dateColumnCombo = new ComboBox<>();
        dateColBox.getChildren().addAll(dateColLabel, dateColumnCombo);
        
        // Description column
        VBox descColBox = new VBox(4);
        Label descColLabel = new Label("Selite:");
        descriptionColumnCombo = new ComboBox<>();
        descColBox.getChildren().addAll(descColLabel, descriptionColumnCombo);
        
        // Amount column
        VBox amountColBox = new VBox(4);
        Label amountColLabel = new Label("Summa:");
        amountColumnCombo = new ComboBox<>();
        amountColBox.getChildren().addAll(amountColLabel, amountColumnCombo);
        
        // Account selection
        VBox accountBox = new VBox(4);
        Label accountLabel = new Label("Pankkitili:");
        accountCombo = new ComboBox<>();
        accountCombo.setPromptText("Valitse tili...");
        accountCombo.setPrefWidth(200);
        accountBox.getChildren().addAll(accountLabel, accountCombo);
        
        mappingBox.getChildren().addAll(dateColBox, descColBox, amountColBox, accountBox);
        
        VBox section = new VBox(4);
        section.getChildren().addAll(titleLabel, mappingBox);
        return section;
    }
    
    private VBox createPreviewSection() {
        Label titleLabel = new Label("Esikatselu");
        titleLabel.setStyle("-fx-font-weight: bold;");
        
        previewTable = new TableView<>();
        previewTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(previewTable, Priority.ALWAYS);
        
        previewData = FXCollections.observableArrayList();
        previewTable.setItems(previewData);
        
        statusLabel = new Label("Valitse CSV-tiedosto");
        statusLabel.setStyle("-fx-text-fill: #64748b;");
        
        VBox section = new VBox(4);
        section.getChildren().addAll(titleLabel, previewTable, statusLabel);
        VBox.setVgrow(section, Priority.ALWAYS);
        VBox.setVgrow(previewTable, Priority.ALWAYS);
        return section;
    }
    
    private HBox createButtonBox() {
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.setPadding(new Insets(10, 0, 0, 0));

        Button importButton = new Button("Tuo");
        importButton.setDefaultButton(true);
        importButton.setStyle("-fx-background-color: #2563eb; -fx-text-fill: white; -fx-min-width: 100;");
        importButton.setOnAction(e -> handleImport());

        Button cancelButton = new Button("Peruuta");
        cancelButton.setCancelButton(true);
        cancelButton.setStyle("-fx-min-width: 80;");
        cancelButton.setOnAction(e -> dialog.close());

        buttonBox.getChildren().addAll(cancelButton, importButton);
        return buttonBox;
    }
    
    private void selectFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Valitse CSV-tiedosto");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("CSV-tiedostot", "*.csv"),
            new FileChooser.ExtensionFilter("Kaikki tiedostot", "*.*")
        );
        
        File file = fileChooser.showOpenDialog(dialog);
        if (file != null) {
            csvFile = file;
            fileLabel.setText(file.getName());
            loadFile();
        }
    }
    
    private void reloadFile() {
        if (csvFile != null) {
            loadFile();
        }
    }
    
    private void loadFile() {
        if (csvFile == null) return;
        
        try {
            String encoding = encodingCombo.getValue();
            csvData = new ArrayList<>();
            
            try (InputStreamReader reader = new InputStreamReader(
                    new FileInputStream(csvFile), Charset.forName(encoding))) {
                
                CSVReader csvReader = new CSVReader(reader);
                String[] line;
                while ((line = csvReader.readLine()) != null) {
                    csvData.add(line);
                }
            }
            
            if (!csvData.isEmpty()) {
                columnCount = csvData.stream().mapToInt(row -> row.length).max().orElse(0);
                updateColumnCombos();
                updatePreview();
                statusLabel.setText("Ladattu " + csvData.size() + " riviä");
            } else {
                statusLabel.setText("Tiedosto on tyhjä");
            }
            
        } catch (Exception e) {
            statusLabel.setText("Virhe: " + e.getMessage());
            showError("Virhe luettaessa tiedostoa", e.getMessage());
        }
    }
    
    private void updateColumnCombos() {
        dateColumnCombo.getItems().clear();
        descriptionColumnCombo.getItems().clear();
        amountColumnCombo.getItems().clear();
        
        for (int i = 0; i < columnCount; i++) {
            dateColumnCombo.getItems().add(i + 1);
            descriptionColumnCombo.getItems().add(i + 1);
            amountColumnCombo.getItems().add(i + 1);
        }
        
        // Auto-detect columns based on first data row
        if (!csvData.isEmpty() && csvData.size() > 1) {
            String[] firstRow = skipHeaderCheckBox.isSelected() && csvData.size() > 1 
                ? csvData.get(1) : csvData.get(0);
            
            for (int i = 0; i < firstRow.length; i++) {
                String value = firstRow[i].trim().toLowerCase();
                
                // Try to detect date column
                if (isDateLike(value) && dateColumnCombo.getValue() == null) {
                    dateColumnCombo.setValue(i + 1);
                }
                // Try to detect amount column
                else if (isAmountLike(value) && amountColumnCombo.getValue() == null) {
                    amountColumnCombo.setValue(i + 1);
                }
            }
            
            // Set description to remaining column
            if (descriptionColumnCombo.getValue() == null) {
                for (int i = 0; i < columnCount; i++) {
                    Integer col = i + 1;
                    if (!col.equals(dateColumnCombo.getValue()) && 
                        !col.equals(amountColumnCombo.getValue())) {
                        descriptionColumnCombo.setValue(col);
                        break;
                    }
                }
            }
        }
        
        // Default values if not detected
        if (dateColumnCombo.getValue() == null && columnCount >= 1) {
            dateColumnCombo.setValue(1);
        }
        if (descriptionColumnCombo.getValue() == null && columnCount >= 2) {
            descriptionColumnCombo.setValue(columnCount > 2 ? 2 : 1);
        }
        if (amountColumnCombo.getValue() == null && columnCount >= 1) {
            amountColumnCombo.setValue(columnCount);
        }
    }
    
    private boolean isDateLike(String value) {
        return value.matches("\\d{1,2}\\.\\d{1,2}\\.\\d{4}") ||
               value.matches("\\d{4}-\\d{2}-\\d{2}");
    }
    
    private boolean isAmountLike(String value) {
        return value.matches("-?\\d+[,.]\\d{2}") ||
               value.matches("-?\\d+");
    }
    
    private void updatePreview() {
        previewData.clear();
        previewTable.getColumns().clear();
        
        if (csvData == null || csvData.isEmpty()) return;
        
        // Create columns
        for (int i = 0; i < columnCount; i++) {
            final int colIndex = i;
            TableColumn<CSVRow, String> col = new TableColumn<>("Sarake " + (i + 1));
            col.setCellValueFactory(cellData -> {
                String[] data = cellData.getValue().getData();
                return new javafx.beans.property.SimpleStringProperty(
                    colIndex < data.length ? data[colIndex] : "");
            });
            col.setPrefWidth(150);
            previewTable.getColumns().add(col);
        }
        
        // Add data rows (limit to 100 for preview)
        int startRow = skipHeaderCheckBox.isSelected() ? 1 : 0;
        int maxRows = Math.min(csvData.size(), startRow + 100);
        
        for (int i = startRow; i < maxRows; i++) {
            previewData.add(new CSVRow(csvData.get(i)));
        }
    }
    
    private void handleImport() {
        if (csvData == null || csvData.isEmpty()) {
            showError("Virhe", "Valitse ensin CSV-tiedosto");
            return;
        }
        
        if (dateColumnCombo.getValue() == null || 
            descriptionColumnCombo.getValue() == null ||
            amountColumnCombo.getValue() == null) {
            showError("Virhe", "Määritä kaikki sarakkeet (päivämäärä, selite, summa)");
            return;
        }
        
        if (accountCombo.getValue() == null) {
            showError("Virhe", "Valitse pankkitili");
            return;
        }
        
        // Parse entries
        importedEntries = new ArrayList<>();
        int dateCol = dateColumnCombo.getValue() - 1;
        int descCol = descriptionColumnCombo.getValue() - 1;
        int amountCol = amountColumnCombo.getValue() - 1;
        String dateFormat = dateFormatCombo.getValue();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(dateFormat);
        
        int startRow = skipHeaderCheckBox.isSelected() ? 1 : 0;
        int errors = 0;
        
        for (int i = startRow; i < csvData.size(); i++) {
            String[] row = csvData.get(i);
            
            try {
                // Parse date
                String dateStr = dateCol < row.length ? row[dateCol].trim() : "";
                LocalDate date = LocalDate.parse(dateStr, formatter);
                
                // Parse description
                String description = descCol < row.length ? row[descCol].trim() : "";
                
                // Parse amount
                String amountStr = amountCol < row.length ? row[amountCol].trim() : "0";
                amountStr = amountStr.replace(",", ".").replace(" ", "").replace("€", "");
                BigDecimal amount = new BigDecimal(amountStr);
                
                ImportedEntry entry = new ImportedEntry();
                entry.date = date;
                entry.description = description;
                entry.amount = amount;
                entry.account = accountCombo.getValue();
                
                importedEntries.add(entry);
                
            } catch (Exception e) {
                errors++;
            }
        }
        
        if (importedEntries.isEmpty()) {
            showError("Virhe", "Yhtään riviä ei voitu tuoda. Tarkista sarakemääritykset ja päivämäärämuoto.");
            return;
        }
        
        String message = "Tuodaan " + importedEntries.size() + " vientiä";
        if (errors > 0) {
            message += " (" + errors + " riviä ohitettu virheiden vuoksi)";
        }
        
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.initOwner(dialog);
        confirm.setTitle("Vahvista tuonti");
        confirm.setHeaderText(message);
        confirm.setContentText("Haluatko jatkaa?");
        
        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            okPressed = true;
            dialog.close();
        }
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.initOwner(dialog);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Public API

    public void setAccounts(List<Account> accounts) {
        this.accounts = accounts;
        accountCombo.getItems().clear();
        
        // Filter to bank accounts (typically 19xx accounts in Finnish chart)
        for (Account acc : accounts) {
            if (acc.getNumber().startsWith("19") || 
                acc.getName().toLowerCase().contains("pankki") ||
                acc.getName().toLowerCase().contains("shekki")) {
                accountCombo.getItems().add(acc);
            }
        }
        
        // If no bank accounts found, show all
        if (accountCombo.getItems().isEmpty()) {
            accountCombo.getItems().addAll(accounts);
        }
    }

    public void setPeriod(Period period) {
        this.period = period;
    }

    public List<ImportedEntry> getImportedEntries() {
        return importedEntries;
    }

    public boolean showAndWait() {
        dialog.showAndWait();
        return okPressed;
    }

    /**
     * Static factory method.
     */
    public static CSVImportDialogFX create(Window owner, List<Account> accounts, Period period) {
        CSVImportDialogFX dialog = new CSVImportDialogFX(owner);
        dialog.setAccounts(accounts);
        dialog.setPeriod(period);
        return dialog;
    }

    /**
     * Row wrapper for TableView.
     */
    public static class CSVRow {
        private final String[] data;

        public CSVRow(String[] data) {
            this.data = data;
        }

        public String[] getData() {
            return data;
        }
    }

    /**
     * Imported entry data.
     */
    public static class ImportedEntry {
        public LocalDate date;
        public String description;
        public BigDecimal amount;
        public Account account;
    }
}
