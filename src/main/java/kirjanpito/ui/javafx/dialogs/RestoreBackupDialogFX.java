package kirjanpito.ui.javafx.dialogs;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * JavaFX dialog for restoring database from backup files.
 */
public class RestoreBackupDialogFX {

    private Stage dialog;
    private Window owner;
    
    private File[] backups;
    private ListView<BackupItem> backupListView;
    private ObservableList<BackupItem> backupItems;
    
    private File restoredDatabaseFile;
    private boolean okPressed = false;

    /**
     * Creates a new RestoreBackupDialogFX.
     * Automatically discovers backup files from the backup directory.
     */
    public RestoreBackupDialogFX(Window owner) {
        this.owner = owner;
        this.backups = discoverBackups();
        
        dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(owner);
        dialog.setTitle("Palauta varmuuskopiosta");
        dialog.setMinWidth(550);
        dialog.setMinHeight(400);

        createContent();
    }
    
    /**
     * Creates a new RestoreBackupDialogFX with provided backups.
     */
    public RestoreBackupDialogFX(Window owner, File[] backups) {
        this.owner = owner;
        this.backups = backups != null ? backups : new File[0];
        
        dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(owner);
        dialog.setTitle("Palauta varmuuskopiosta");
        dialog.setMinWidth(550);
        dialog.setMinHeight(400);

        createContent();
    }
    
    private File[] discoverBackups() {
        AppSettings settings = AppSettings.getInstance();
        String backupDir = settings.getString("backup-directory", null);
        
        if (backupDir == null) {
            // Default backup directory
            String userHome = System.getProperty("user.home");
            backupDir = userHome + File.separator + "Tilitin" + File.separator + "backups";
        }
        
        File backupFolder = new File(backupDir);
        if (!backupFolder.exists() || !backupFolder.isDirectory()) {
            return new File[0];
        }
        
        // List .sqlite backup files
        File[] files = backupFolder.listFiles((dir, name) -> 
            name.toLowerCase().endsWith(".sqlite"));
        
        return files != null ? files : new File[0];
    }

    private void createContent() {
        VBox root = new VBox(16);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #ffffff;");

        // Header
        Label headerLabel = new Label("Valitse palautettava varmuuskopio:");
        headerLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        
        // Backup list
        backupListView = new ListView<>();
        backupListView.setPrefHeight(250);
        backupListView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        VBox.setVgrow(backupListView, Priority.ALWAYS);
        
        // Populate list
        backupItems = FXCollections.observableArrayList();
        SimpleDateFormat sdf = new SimpleDateFormat("d.M.yyyy HH:mm:ss");
        
        for (File backup : backups) {
            backupItems.add(new BackupItem(backup, sdf));
        }
        
        backupListView.setItems(backupItems);
        backupListView.setCellFactory(lv -> new BackupListCell());
        
        if (!backupItems.isEmpty()) {
            backupListView.getSelectionModel().selectFirst();
        }
        
        // Info panel
        VBox infoBox = createInfoBox();
        
        // Buttons
        HBox buttonBox = createButtonBox();

        root.getChildren().addAll(headerLabel, backupListView, infoBox, buttonBox);

        Scene scene = new Scene(root, 550, 450);
        dialog.setScene(scene);
    }
    
    private VBox createInfoBox() {
        VBox box = new VBox(4);
        box.setPadding(new Insets(8));
        box.setStyle("-fx-background-color: #f1f5f9; -fx-border-radius: 4; -fx-background-radius: 4;");
        
        Label infoLabel = new Label("💡 Vinkki: Varmuuskopiot sisältävät koko tietokannan.");
        infoLabel.setStyle("-fx-text-fill: #475569; -fx-font-size: 11px;");
        
        Label warningLabel = new Label("⚠️ Palautus korvaa nykyisen tietokannan, jos se on samanniminen.");
        warningLabel.setStyle("-fx-text-fill: #b45309; -fx-font-size: 11px;");
        
        box.getChildren().addAll(infoLabel, warningLabel);
        return box;
    }
    
    private HBox createButtonBox() {
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.setPadding(new Insets(10, 0, 0, 0));

        Button restoreDefaultButton = new Button("Palauta oletuskansioon");
        restoreDefaultButton.setStyle("-fx-background-color: #2563eb; -fx-text-fill: white;");
        restoreDefaultButton.setOnAction(e -> restoreToDefault());

        Button restoreToButton = new Button("Palauta kansioon...");
        restoreToButton.setOnAction(e -> restoreToSelected());

        Button cancelButton = new Button("Peruuta");
        cancelButton.setCancelButton(true);
        cancelButton.setOnAction(e -> dialog.close());

        buttonBox.getChildren().addAll(restoreDefaultButton, restoreToButton, cancelButton);
        return buttonBox;
    }
    
    private void restoreToDefault() {
        BackupItem selected = backupListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Valitse varmuuskopio ensin.");
            return;
        }

        // Get default Tilitin directory
        AppSettings settings = AppSettings.getInstance();
        String defaultDir = settings.getString("database-directory", null);
        if (defaultDir == null) {
            defaultDir = System.getProperty("user.home") + File.separator + "Tilitin";
        }
        File targetDir = new File(defaultDir);

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.initOwner(dialog);
        confirm.setTitle("Vahvista palautus");
        confirm.setHeaderText("Palautetaan varmuuskopio");
        confirm.setContentText(
            "Palautetaan:\n" + selected.getFile().getName() + "\n\n" +
            "Kohdekansioon:\n" + targetDir.getAbsolutePath() + "\n\n" +
            "Jatketaanko?");

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            restoredDatabaseFile = restoreBackup(selected.getFile(), targetDir);
            if (restoredDatabaseFile != null) {
                okPressed = true;
                showInfo("Varmuuskopio palautettu onnistuneesti:\n" + restoredDatabaseFile.getAbsolutePath());
                dialog.close();
            } else {
                showError("Varmuuskopion palautus epäonnistui.");
            }
        }
    }
    
    private void restoreToSelected() {
        BackupItem selected = backupListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Valitse varmuuskopio ensin.");
            return;
        }

        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Valitse kohdekansio");
        chooser.setInitialDirectory(new File(System.getProperty("user.home")));
        
        File targetDir = chooser.showDialog(dialog);
        if (targetDir != null) {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.initOwner(dialog);
            confirm.setTitle("Vahvista palautus");
            confirm.setHeaderText("Palautetaan varmuuskopio");
            confirm.setContentText(
                "Palautetaan:\n" + selected.getFile().getName() + "\n\n" +
                "Kohdekansioon:\n" + targetDir.getAbsolutePath() + "\n\n" +
                "Jatketaanko?");

            if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
                restoredDatabaseFile = restoreBackup(selected.getFile(), targetDir);
                if (restoredDatabaseFile != null) {
                    okPressed = true;
                    showInfo("Varmuuskopio palautettu onnistuneesti:\n" + restoredDatabaseFile.getAbsolutePath());
                    dialog.close();
                } else {
                    showError("Varmuuskopion palautus epäonnistui.");
                }
            }
        }
    }
    
    /**
     * Restores a backup file to the target directory.
     */
    private File restoreBackup(File backupFile, File targetDir) {
        try {
            if (!targetDir.exists()) {
                targetDir.mkdirs();
            }
            
            // Extract original database name from backup filename
            String originalName = extractOriginalDatabaseName(backupFile.getName());
            File targetFile = new File(targetDir, originalName);
            
            // Copy the backup to the target location
            Files.copy(backupFile.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            
            return targetFile;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Extracts the original database name from a backup filename.
     * Backup format: originalname_hash_timestamp.sqlite
     */
    private String extractOriginalDatabaseName(String backupFileName) {
        String name = backupFileName;
        
        // Remove timestamp (19 chars: YYYY-MM-DD_HH-MM-SS + underscore before it)
        // Pattern: name_hash_timestamp.sqlite
        if (name.length() > 20) {
            String possibleTimestamp = name.substring(name.length() - 26, name.length() - 7);
            if (possibleTimestamp.matches("_[a-f0-9]{6}_\\d{4}-\\d{2}-\\d{2}")) {
                // Has hash + timestamp
                name = name.substring(0, name.length() - 27) + ".sqlite";
            } else {
                // Just remove timestamp if present
                possibleTimestamp = name.substring(name.length() - 19 - 7, name.length() - 7);
                if (possibleTimestamp.matches("\\d{4}-\\d{2}-\\d{2}_\\d{2}-\\d{2}-\\d{2}")) {
                    name = name.substring(0, name.length() - 27) + ".sqlite";
                }
            }
        }
        
        // If extraction failed, just use the backup filename as-is
        if (!name.endsWith(".sqlite")) {
            name = backupFileName;
        }
        
        return name;
    }
    
    private void showWarning(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.initOwner(dialog);
        alert.setTitle("Huomio");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.initOwner(dialog);
        alert.setTitle("Virhe");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.initOwner(dialog);
        alert.setTitle("Tiedoksi");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public File getRestoredDatabaseFile() {
        return restoredDatabaseFile;
    }

    public boolean showAndWait() {
        dialog.showAndWait();
        return okPressed;
    }

    /**
     * Backup item for the list view.
     */
    public static class BackupItem {
        private final File file;
        private final String displayName;
        private final String dateStr;
        private final long sizeKb;

        public BackupItem(File file, SimpleDateFormat sdf) {
            this.file = file;
            this.dateStr = sdf.format(new Date(file.lastModified()));
            this.sizeKb = file.length() / 1024;
            this.displayName = extractDatabaseName(file.getName());
        }

        private String extractDatabaseName(String backupFileName) {
            String name = backupFileName;
            if (name.endsWith(".sqlite")) {
                name = name.substring(0, name.length() - 7);
            }

            // Remove timestamp (19 chars + underscore)
            if (name.length() > 20) {
                String possibleTimestamp = name.substring(name.length() - 19);
                if (possibleTimestamp.matches("\\d{4}-\\d{2}-\\d{2}_\\d{2}-\\d{2}-\\d{2}")) {
                    name = name.substring(0, name.length() - 20);
                }
            }

            // Remove hash (6 chars + underscore)
            if (name.length() > 7) {
                String possibleHash = name.substring(name.length() - 6);
                if (possibleHash.matches("[a-f0-9]{6}")) {
                    name = name.substring(0, name.length() - 7);
                }
            }

            return name;
        }

        public File getFile() { return file; }
        public String getDisplayName() { return displayName; }
        public String getDateStr() { return dateStr; }
        public long getSizeKb() { return sizeKb; }
    }

    /**
     * Custom cell for displaying backup items.
     */
    private static class BackupListCell extends ListCell<BackupItem> {
        @Override
        protected void updateItem(BackupItem item, boolean empty) {
            super.updateItem(item, empty);
            
            if (empty || item == null) {
                setText(null);
                setGraphic(null);
            } else {
                VBox box = new VBox(2);
                box.setPadding(new Insets(4));
                
                Label nameLabel = new Label(item.getDisplayName());
                nameLabel.setStyle("-fx-font-weight: bold;");
                
                HBox detailsBox = new HBox(16);
                Label dateLabel = new Label("📅 " + item.getDateStr());
                dateLabel.setStyle("-fx-text-fill: #64748b; -fx-font-size: 11px;");
                Label sizeLabel = new Label("💾 " + item.getSizeKb() + " KB");
                sizeLabel.setStyle("-fx-text-fill: #64748b; -fx-font-size: 11px;");
                detailsBox.getChildren().addAll(dateLabel, sizeLabel);
                
                box.getChildren().addAll(nameLabel, detailsBox);
                setGraphic(box);
            }
        }
    }
}
