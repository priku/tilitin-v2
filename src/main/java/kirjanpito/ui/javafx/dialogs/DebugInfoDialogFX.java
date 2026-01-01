package kirjanpito.ui.javafx.dialogs;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import kirjanpito.util.AppSettings;
import kirjanpito.util.Registry;

import java.io.File;
import java.text.SimpleDateFormat;

/**
 * JavaFX-dialogi debug-/virheenjäljitystietojen näyttämiselle.
 * Näyttää järjestelmätietoja ja sovelluksen tilasta.
 */
public class DebugInfoDialogFX {

    private final Stage dialog;
    private final Registry registry;

    public DebugInfoDialogFX(Window owner, Registry registry) {
        this.registry = registry;
        
        dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(owner);
        dialog.setTitle("Virheenjäljitystiedot");
        dialog.setMinWidth(550);
        dialog.setMinHeight(450);
        
        createContent();
    }

    private void createContent() {
        VBox root = new VBox(16);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #ffffff;");

        // Header
        Label headerLabel = new Label("Virheenjäljitystiedot");
        headerLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        
        Label descLabel = new Label("Nämä tiedot ovat hyödyllisiä virheiden selvittämisessä");
        descLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666666;");

        // Info text area
        TextArea infoArea = new TextArea();
        infoArea.setEditable(false);
        infoArea.setWrapText(true);
        infoArea.setStyle("-fx-font-family: 'Consolas', 'Courier New', monospace; -fx-font-size: 12px;");
        VBox.setVgrow(infoArea, Priority.ALWAYS);
        
        // Populate debug info
        StringBuilder sb = new StringBuilder();
        
        // Java info
        sb.append("=== JAVA ===\n");
        sb.append("Java versio: ").append(System.getProperty("java.version")).append("\n");
        sb.append("Java vendor: ").append(System.getProperty("java.vendor")).append("\n");
        sb.append("Java home: ").append(System.getProperty("java.home")).append("\n");
        sb.append("JavaFX versio: ").append(System.getProperty("javafx.version", "N/A")).append("\n");
        sb.append("\n");
        
        // OS info
        sb.append("=== KÄYTTÖJÄRJESTELMÄ ===\n");
        sb.append("OS: ").append(System.getProperty("os.name")).append("\n");
        sb.append("OS versio: ").append(System.getProperty("os.version")).append("\n");
        sb.append("Arkkitehtuuri: ").append(System.getProperty("os.arch")).append("\n");
        sb.append("\n");
        
        // Memory info
        sb.append("=== MUISTI ===\n");
        Runtime runtime = Runtime.getRuntime();
        long maxMemory = runtime.maxMemory() / (1024 * 1024);
        long totalMemory = runtime.totalMemory() / (1024 * 1024);
        long freeMemory = runtime.freeMemory() / (1024 * 1024);
        long usedMemory = totalMemory - freeMemory;
        sb.append("Käytössä: ").append(usedMemory).append(" MB\n");
        sb.append("Varattu: ").append(totalMemory).append(" MB\n");
        sb.append("Maksimi: ").append(maxMemory).append(" MB\n");
        sb.append("\n");
        
        // Application info
        sb.append("=== SOVELLUS ===\n");
        sb.append("Käyttäjän kotihakemisto: ").append(System.getProperty("user.home")).append("\n");
        sb.append("Työhakemisto: ").append(System.getProperty("user.dir")).append("\n");
        sb.append("\n");
        
        // Database info
        sb.append("=== TIETOKANTA ===\n");
        if (registry != null && registry.getDataSource() != null) {
            try {
                String dbPath = AppSettings.getInstance().getString("database", "N/A");
                sb.append("Tietokanta: ").append(dbPath).append("\n");
                
                File dbFile = new File(dbPath);
                if (dbFile.exists()) {
                    sb.append("Tiedostokoko: ").append(dbFile.length() / 1024).append(" KB\n");
                    SimpleDateFormat sdf = new SimpleDateFormat("d.M.yyyy HH:mm:ss");
                    sb.append("Muokattu: ").append(sdf.format(dbFile.lastModified())).append("\n");
                }
                
                if (registry.getPeriod() != null) {
                    SimpleDateFormat sdf = new SimpleDateFormat("d.M.yyyy");
                    sb.append("Tilikausi: ")
                      .append(sdf.format(registry.getPeriod().getStartDate()))
                      .append(" - ")
                      .append(sdf.format(registry.getPeriod().getEndDate()))
                      .append("\n");
                }
                
                sb.append("Tilejä: ").append(registry.getAccounts() != null ? registry.getAccounts().size() : 0).append("\n");
                sb.append("Vientimalleja: ").append(registry.getEntryTemplates() != null ? registry.getEntryTemplates().size() : 0).append("\n");
            } catch (Exception e) {
                sb.append("Virhe tietojen haussa: ").append(e.getMessage()).append("\n");
            }
        } else {
            sb.append("Ei avointa tietokantaa\n");
        }
        sb.append("\n");
        
        // Settings info
        sb.append("=== ASETUKSET ===\n");
        AppSettings settings = AppSettings.getInstance();
        sb.append("Varmuuskopiohakemisto: ").append(settings.getString("backup-directory", "N/A")).append("\n");
        sb.append("CSV-hakemisto: ").append(settings.getString("csv-directory", "N/A")).append("\n");
        sb.append("Auto-backup: ").append(settings.getBoolean("auto-backup", false) ? "Kyllä" : "Ei").append("\n");
        
        infoArea.setText(sb.toString());

        // Buttons
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.setPadding(new Insets(10, 0, 0, 0));

        Button copyButton = new Button("Kopioi leikepöydälle");
        copyButton.setOnAction(e -> {
            javafx.scene.input.Clipboard clipboard = javafx.scene.input.Clipboard.getSystemClipboard();
            javafx.scene.input.ClipboardContent content = new javafx.scene.input.ClipboardContent();
            content.putString(infoArea.getText());
            clipboard.setContent(content);
            
            // Show feedback
            copyButton.setText("✓ Kopioitu!");
            copyButton.setDisable(true);
            new Thread(() -> {
                try {
                    Thread.sleep(2000);
                    javafx.application.Platform.runLater(() -> {
                        copyButton.setText("Kopioi leikepöydälle");
                        copyButton.setDisable(false);
                    });
                } catch (InterruptedException ex) {}
            }).start();
        });

        Button closeButton = new Button("Sulje");
        closeButton.setCancelButton(true);
        closeButton.setOnAction(e -> dialog.close());
        closeButton.setPrefWidth(100);

        buttonBox.getChildren().addAll(copyButton, closeButton);

        root.getChildren().addAll(headerLabel, descLabel, infoArea, buttonBox);

        Scene scene = new Scene(root, 600, 500);
        dialog.setScene(scene);
    }

    public void show() {
        dialog.showAndWait();
    }
}
