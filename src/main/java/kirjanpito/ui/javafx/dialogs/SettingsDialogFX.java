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

/**
 * JavaFX asetukset-dialogi.
 */
public class SettingsDialogFX {
    
    private Stage dialog;
    private CheckBox autoSaveCheck;
    private CheckBox showVatColumnCheck;
    private Spinner<Integer> backupIntervalSpinner;
    private ComboBox<String> themeCombo;
    
    private AppSettings settings;
    
    public SettingsDialogFX(Window owner) {
        settings = AppSettings.getInstance();
        
        dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(owner);
        dialog.setTitle("Asetukset");
        dialog.setResizable(false);
        
        createContent();
    }
    
    private void createContent() {
        VBox root = new VBox(20);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #ffffff;");
        
        // General settings
        TitledPane generalPane = new TitledPane();
        generalPane.setText("Yleiset");
        generalPane.setCollapsible(false);
        
        VBox generalBox = new VBox(10);
        generalBox.setPadding(new Insets(10));
        
        autoSaveCheck = new CheckBox("Tallenna automaattisesti siirryttäessä tositteesta toiseen");
        autoSaveCheck.setSelected(settings.getBoolean("autoSave", true));
        
        showVatColumnCheck = new CheckBox("Näytä ALV-sarake vientitaulukossa");
        showVatColumnCheck.setSelected(settings.getBoolean("showVatColumn", true));
        
        generalBox.getChildren().addAll(autoSaveCheck, showVatColumnCheck);
        generalPane.setContent(generalBox);
        
        // Backup settings
        TitledPane backupPane = new TitledPane();
        backupPane.setText("Varmuuskopiointi");
        backupPane.setCollapsible(false);
        
        VBox backupBox = new VBox(10);
        backupBox.setPadding(new Insets(10));
        
        HBox intervalBox = new HBox(10);
        intervalBox.setAlignment(Pos.CENTER_LEFT);
        Label intervalLabel = new Label("Varmuuskopiointiväli (minuuttia):");
        backupIntervalSpinner = new Spinner<>(0, 60, settings.getInt("backupInterval", 10));
        backupIntervalSpinner.setEditable(true);
        backupIntervalSpinner.setPrefWidth(80);
        intervalBox.getChildren().addAll(intervalLabel, backupIntervalSpinner);
        
        backupBox.getChildren().add(intervalBox);
        backupPane.setContent(backupBox);
        
        // Appearance
        TitledPane appearancePane = new TitledPane();
        appearancePane.setText("Ulkoasu");
        appearancePane.setCollapsible(false);
        
        VBox appearanceBox = new VBox(10);
        appearanceBox.setPadding(new Insets(10));
        
        HBox themeBox = new HBox(10);
        themeBox.setAlignment(Pos.CENTER_LEFT);
        Label themeLabel = new Label("Teema:");
        themeCombo = new ComboBox<>();
        themeCombo.getItems().addAll("Vaalea", "Tumma", "Järjestelmä");
        themeCombo.setValue(settings.getString("theme", "Vaalea"));
        themeBox.getChildren().addAll(themeLabel, themeCombo);
        
        appearanceBox.getChildren().add(themeBox);
        appearancePane.setContent(appearanceBox);
        
        // Buttons
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        
        Button saveBtn = new Button("Tallenna");
        saveBtn.setDefaultButton(true);
        saveBtn.setStyle("-fx-background-color: #2563eb; -fx-text-fill: white;");
        saveBtn.setOnAction(e -> save());
        
        Button cancelBtn = new Button("Peruuta");
        cancelBtn.setCancelButton(true);
        cancelBtn.setOnAction(e -> dialog.close());
        
        buttonBox.getChildren().addAll(cancelBtn, saveBtn);
        
        root.getChildren().addAll(generalPane, backupPane, appearancePane, buttonBox);
        
        Scene scene = new Scene(root, 450, 350);
        dialog.setScene(scene);
    }
    
    private void save() {
        settings.set("autoSave", autoSaveCheck.isSelected());
        settings.set("showVatColumn", showVatColumnCheck.isSelected());
        settings.set("backupInterval", backupIntervalSpinner.getValue());
        settings.set("theme", themeCombo.getValue());
        
        settings.save();
        dialog.close();
    }
    
    public void show() {
        dialog.showAndWait();
    }
}
