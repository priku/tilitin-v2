package kirjanpito.ui.javafx.dialogs;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import kirjanpito.util.AppSettings;
import kirjanpito.util.BackupService;
import kirjanpito.util.RecentDatabases;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * JavaFX dialog for backup settings.
 * Provides auto-backup configuration similar to Word AutoSave.
 */
public class BackupSettingsDialogFX {

    private Stage dialog;
    private Window owner;
    
    // UI Components
    private CheckBox enabledCheckBox;
    private CheckBox autoBackupCheckBox;
    private Spinner<Integer> intervalSpinner;
    private Spinner<Integer> maxBackupsSpinner;
    private TextField backupPathField;
    private Label lastBackupLabel;
    private VBox databasesBox;
    
    // Database checkboxes
    private Map<String, CheckBox> databaseCheckBoxes = new HashMap<>();
    
    private boolean okPressed = false;

    public BackupSettingsDialogFX(Window owner) {
        this.owner = owner;
        dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(owner);
        dialog.setTitle("Varmuuskopiointiasetukset");
        dialog.setMinWidth(500);
        dialog.setMinHeight(450);

        createContent();
        loadSettings();
    }

    private void createContent() {
        VBox root = new VBox(16);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #ffffff;");

        // Header
        Label headerLabel = new Label("Varmuuskopiointiasetukset");
        headerLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        
        // Main settings section
        VBox settingsBox = createSettingsSection();
        
        // Databases section
        VBox databasesSection = createDatabasesSection();
        VBox.setVgrow(databasesSection, Priority.ALWAYS);
        
        // Status section
        HBox statusBox = createStatusSection();
        
        // Buttons
        HBox buttonBox = createButtonBox();

        root.getChildren().addAll(headerLabel, settingsBox, databasesSection, statusBox, buttonBox);

        Scene scene = new Scene(root, 520, 500);
        dialog.setScene(scene);
    }
    
    private VBox createSettingsSection() {
        VBox box = new VBox(12);
        box.setPadding(new Insets(12));
        box.setStyle("-fx-border-color: #e2e8f0; -fx-border-radius: 4; -fx-background-color: #f9fafb; -fx-background-radius: 4;");
        
        // Enable backup
        enabledCheckBox = new CheckBox("Varmuuskopiointi käytössä");
        enabledCheckBox.setOnAction(e -> updateEnabledState());
        
        // Auto-backup with interval
        HBox autoBox = new HBox(12);
        autoBox.setAlignment(Pos.CENTER_LEFT);
        autoBackupCheckBox = new CheckBox("AutoBackup");
        autoBackupCheckBox.setTooltip(new Tooltip("Varmuuskopioi automaattisesti säännöllisin väliajoin"));
        
        Label intervalLabel = new Label("Väli:");
        intervalSpinner = new Spinner<>(1, 60, 5);
        intervalSpinner.setPrefWidth(70);
        intervalSpinner.setEditable(true);
        Label minutesLabel = new Label("min");
        
        autoBox.getChildren().addAll(autoBackupCheckBox, intervalLabel, intervalSpinner, minutesLabel);
        
        // Max backups
        HBox maxBox = new HBox(12);
        maxBox.setAlignment(Pos.CENTER_LEFT);
        Label maxLabel = new Label("Säilytä versioita:");
        maxBackupsSpinner = new Spinner<>(1, 100, 10);
        maxBackupsSpinner.setPrefWidth(70);
        maxBackupsSpinner.setEditable(true);
        maxBox.getChildren().addAll(maxLabel, maxBackupsSpinner);
        
        // Backup path
        HBox pathBox = new HBox(8);
        pathBox.setAlignment(Pos.CENTER_LEFT);
        Label pathLabel = new Label("Tallennuskansio:");
        backupPathField = new TextField();
        backupPathField.setPrefWidth(250);
        HBox.setHgrow(backupPathField, Priority.ALWAYS);
        Button browseButton = new Button("Selaa...");
        browseButton.setOnAction(e -> selectBackupPath());
        pathBox.getChildren().addAll(pathLabel, backupPathField, browseButton);
        
        box.getChildren().addAll(enabledCheckBox, autoBox, maxBox, pathBox);
        return box;
    }
    
    private VBox createDatabasesSection() {
        VBox box = new VBox(8);
        
        Label label = new Label("Varmuuskopioitavat tietokannat:");
        label.setStyle("-fx-font-weight: 500;");
        
        databasesBox = new VBox(4);
        databasesBox.setPadding(new Insets(8));
        databasesBox.setStyle("-fx-border-color: #e2e8f0; -fx-border-radius: 4; -fx-background-color: #ffffff; -fx-background-radius: 4;");
        
        ScrollPane scrollPane = new ScrollPane(databasesBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(120);
        scrollPane.setStyle("-fx-background-color: transparent;");
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        
        box.getChildren().addAll(label, scrollPane);
        return box;
    }
    
    private HBox createStatusSection() {
        HBox box = new HBox(16);
        box.setAlignment(Pos.CENTER_LEFT);
        box.setPadding(new Insets(8));
        box.setStyle("-fx-border-color: #e2e8f0; -fx-border-radius: 4; -fx-background-color: #f9fafb; -fx-background-radius: 4;");
        
        Label statusLabel = new Label("Viimeisin:");
        lastBackupLabel = new Label("Ei varmuuskopioita");
        lastBackupLabel.setStyle("-fx-text-fill: #64748b;");
        
        Pane spacer = new Pane();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Button backupNowButton = new Button("Tee varmuuskopio nyt");
        backupNowButton.setOnAction(e -> performBackupNow());
        
        box.getChildren().addAll(statusLabel, lastBackupLabel, spacer, backupNowButton);
        return box;
    }
    
    private HBox createButtonBox() {
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.setPadding(new Insets(10, 0, 0, 0));

        Button okButton = new Button("OK");
        okButton.setDefaultButton(true);
        okButton.setStyle("-fx-background-color: #2563eb; -fx-text-fill: white; -fx-min-width: 80;");
        okButton.setOnAction(e -> {
            saveSettings();
            okPressed = true;
            dialog.close();
        });

        Button cancelButton = new Button("Peruuta");
        cancelButton.setCancelButton(true);
        cancelButton.setStyle("-fx-min-width: 80;");
        cancelButton.setOnAction(e -> dialog.close());

        buttonBox.getChildren().addAll(cancelButton, okButton);
        return buttonBox;
    }
    
    private void selectBackupPath() {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Valitse varmuuskopioiden tallennuskansio");
        
        String currentPath = backupPathField.getText();
        if (currentPath != null && !currentPath.isEmpty()) {
            File currentDir = new File(currentPath);
            if (currentDir.exists() && currentDir.isDirectory()) {
                chooser.setInitialDirectory(currentDir);
            }
        }
        
        File selected = chooser.showDialog(dialog);
        if (selected != null) {
            backupPathField.setText(selected.getAbsolutePath());
        }
    }
    
    private void updateEnabledState() {
        boolean enabled = enabledCheckBox.isSelected();
        autoBackupCheckBox.setDisable(!enabled);
        intervalSpinner.setDisable(!enabled || !autoBackupCheckBox.isSelected());
        maxBackupsSpinner.setDisable(!enabled);
        backupPathField.setDisable(!enabled);
        
        for (CheckBox cb : databaseCheckBoxes.values()) {
            cb.setDisable(!enabled);
        }
    }
    
    private void loadSettings() {
        AppSettings settings = AppSettings.getInstance();
        
        enabledCheckBox.setSelected(settings.getBoolean("backup.enabled", false));
        autoBackupCheckBox.setSelected(settings.getBoolean("backup.auto", false));
        intervalSpinner.getValueFactory().setValue(settings.getInt("backup.interval", 5));
        maxBackupsSpinner.getValueFactory().setValue(settings.getInt("backup.maxVersions", 10));
        
        String backupPath = settings.getString("backup.path", "");
        if (backupPath.isEmpty()) {
            // Default to user home backup folder
            backupPath = System.getProperty("user.home") + File.separator + "TilitinBackups";
        }
        backupPathField.setText(backupPath);
        
        // Load databases from recent list
        loadDatabasesList();
        
        // Update enabled state
        updateEnabledState();
        
        // Show last backup time
        updateLastBackupLabel();
    }
    
    private void loadDatabasesList() {
        databasesBox.getChildren().clear();
        databaseCheckBoxes.clear();
        
        RecentDatabases recent = RecentDatabases.getInstance();
        List<String> databases = recent.getRecentDatabases();
        
        AppSettings settings = AppSettings.getInstance();
        Set<String> enabledDbs = new HashSet<>(Arrays.asList(
            settings.getString("backup.databases", "").split(",")));
        
        if (databases.isEmpty()) {
            Label emptyLabel = new Label("Ei viimeisimpiä tietokantoja");
            emptyLabel.setStyle("-fx-text-fill: #94a3b8;");
            databasesBox.getChildren().add(emptyLabel);
        } else {
            for (String dbUrl : databases) {
                String displayName = RecentDatabases.getDisplayName(dbUrl);
                CheckBox cb = new CheckBox(displayName);
                cb.setSelected(enabledDbs.contains(dbUrl) || enabledDbs.isEmpty());
                cb.setUserData(dbUrl);
                databaseCheckBoxes.put(dbUrl, cb);
                databasesBox.getChildren().add(cb);
            }
        }
    }
    
    private void updateLastBackupLabel() {
        BackupService service = BackupService.getInstance();
        Date lastBackup = service.getLastBackupTime();
        
        if (lastBackup != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
            lastBackupLabel.setText(sdf.format(lastBackup));
            lastBackupLabel.setStyle("-fx-text-fill: #059669;");
        } else {
            lastBackupLabel.setText("Ei varmuuskopioita");
            lastBackupLabel.setStyle("-fx-text-fill: #64748b;");
        }
    }
    
    private void saveSettings() {
        AppSettings settings = AppSettings.getInstance();
        
        settings.set("backup.enabled", enabledCheckBox.isSelected());
        settings.set("backup.auto", autoBackupCheckBox.isSelected());
        settings.set("backup.interval", intervalSpinner.getValue());
        settings.set("backup.maxVersions", maxBackupsSpinner.getValue());
        settings.set("backup.path", backupPathField.getText());
        
        // Save selected databases
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, CheckBox> entry : databaseCheckBoxes.entrySet()) {
            if (entry.getValue().isSelected()) {
                if (sb.length() > 0) sb.append(",");
                sb.append(entry.getKey());
            }
        }
        settings.set("backup.databases", sb.toString());
        
        settings.save();
        
        // Update backup service
        BackupService service = BackupService.getInstance();
        service.setEnabled(enabledCheckBox.isSelected());
        if (autoBackupCheckBox.isSelected()) {
            service.setAutoBackupEnabled(true);
            service.setAutoBackupIntervalMinutes(intervalSpinner.getValue());
        } else {
            service.setAutoBackupEnabled(false);
        }
    }
    
    private void performBackupNow() {
        try {
            BackupService service = BackupService.getInstance();
            
            // Get selected databases
            List<String> selectedDbs = new ArrayList<>();
            for (Map.Entry<String, CheckBox> entry : databaseCheckBoxes.entrySet()) {
                if (entry.getValue().isSelected()) {
                    selectedDbs.add(entry.getKey());
                }
            }
            
            if (selectedDbs.isEmpty()) {
                showInfo("Valitse tietokannat", "Valitse vähintään yksi tietokanta varmuuskopioitavaksi.");
                return;
            }
            
            String backupPath = backupPathField.getText();
            if (backupPath == null || backupPath.isEmpty()) {
                showInfo("Valitse kansio", "Valitse varmuuskopioiden tallennuskansio.");
                return;
            }
            
            // Ensure backup directory exists
            File backupDir = new File(backupPath);
            if (!backupDir.exists()) {
                backupDir.mkdirs();
            }
            
            int count = 0;
            for (String dbUrl : selectedDbs) {
                try {
                    // Extract file path from JDBC URL
                    String filePath = dbUrl;
                    if (dbUrl.startsWith("jdbc:sqlite:")) {
                        filePath = dbUrl.substring("jdbc:sqlite:".length());
                    }
                    service.performBackup(filePath);
                    count++;
                } catch (Exception e) {
                    System.err.println("Backup failed for " + dbUrl + ": " + e.getMessage());
                }
            }
            
            if (count > 0) {
                updateLastBackupLabel();
                showInfo("Varmuuskopiointi valmis", 
                    "Varmuuskopioitiin " + count + " tietokantaa kansioon:\n" + backupPath);
            }
            
        } catch (Exception e) {
            showError("Varmuuskopiointivirhe", e.getMessage());
        }
    }
    
    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.initOwner(dialog);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.initOwner(dialog);
        alert.setTitle("Virhe");
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public boolean showAndWait() {
        dialog.showAndWait();
        return okPressed;
    }
}
