package kirjanpito.ui.javafx.dialogs;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import kirjanpito.util.AppSettings;

import java.util.function.Consumer;

/**
 * JavaFX ulkoasu-dialogi.
 * Mahdollistaa teeman ja fonttikoon vaihdon.
 */
public class AppearanceDialogFX {
    
    private Stage dialog;
    private ComboBox<String> themeCombo;
    private Spinner<Integer> fontSizeSpinner;
    private Label previewLabel;
    
    private AppSettings settings;
    private String originalTheme;
    private int originalFontSize;
    private Consumer<String> onThemeChangedCallback;
    
    public AppearanceDialogFX(Window owner) {
        settings = AppSettings.getInstance();
        originalTheme = settings.getString("theme", "Vaalea");
        originalFontSize = settings.getInt("fontSize", 13);
        
        dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(owner);
        dialog.setTitle("Ulkoasu");
        dialog.setResizable(false);
        
        createContent();
    }
    
    private void createContent() {
        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #ffffff;");
        
        // Theme selection
        HBox themeBox = new HBox(15);
        themeBox.setAlignment(Pos.CENTER_LEFT);
        
        Label themeLabel = new Label("Teema:");
        themeLabel.setMinWidth(100);
        
        themeCombo = new ComboBox<>();
        themeCombo.getItems().addAll("Vaalea", "Tumma", "Järjestelmä");
        themeCombo.setValue(originalTheme);
        themeCombo.setPrefWidth(150);
        
        themeCombo.setOnAction(e -> {
            String newTheme = themeCombo.getValue();
            settings.set("theme", newTheme);
            if (onThemeChangedCallback != null) {
                onThemeChangedCallback.accept(newTheme);
            }
        });
        
        themeBox.getChildren().addAll(themeLabel, themeCombo);
        
        // Font size selection
        HBox fontBox = new HBox(15);
        fontBox.setAlignment(Pos.CENTER_LEFT);
        
        Label fontLabel = new Label("Fonttikoko:");
        fontLabel.setMinWidth(100);
        
        fontSizeSpinner = new Spinner<>(8, 24, originalFontSize);
        fontSizeSpinner.setEditable(true);
        fontSizeSpinner.setPrefWidth(80);
        fontSizeSpinner.valueProperty().addListener((obs, oldVal, newVal) -> {
            updatePreview();
        });
        
        Label ptLabel = new Label("pt");
        
        fontBox.getChildren().addAll(fontLabel, fontSizeSpinner, ptLabel);
        
        // Preview
        TitledPane previewPane = new TitledPane();
        previewPane.setText("Esikatselu");
        previewPane.setCollapsible(false);
        
        previewLabel = new Label("Tämä on esimerkkiteksti.\nFonttikoko: " + originalFontSize + " pt");
        previewLabel.setFont(Font.font(originalFontSize));
        previewLabel.setPadding(new Insets(10));
        previewPane.setContent(previewLabel);
        
        // Info text
        Label infoLabel = new Label("Teema vaihtuu heti. Fonttikoko tulee voimaan uudelleenkäynnistyksen jälkeen.");
        infoLabel.setStyle("-fx-font-style: italic; -fx-text-fill: #666; -fx-font-size: 11px;");
        infoLabel.setWrapText(true);
        
        // Buttons
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.setPadding(new Insets(10, 0, 0, 0));
        
        Button okBtn = new Button("OK");
        okBtn.setDefaultButton(true);
        okBtn.setStyle("-fx-background-color: #2563eb; -fx-text-fill: white;");
        okBtn.setPrefWidth(80);
        okBtn.setOnAction(e -> {
            settings.set("fontSize", fontSizeSpinner.getValue());
            settings.save();
            dialog.close();
        });
        
        Button cancelBtn = new Button("Peruuta");
        cancelBtn.setCancelButton(true);
        cancelBtn.setPrefWidth(80);
        cancelBtn.setOnAction(e -> {
            // Revert to original
            settings.set("theme", originalTheme);
            if (onThemeChangedCallback != null) {
                onThemeChangedCallback.accept(originalTheme);
            }
            dialog.close();
        });
        
        buttonBox.getChildren().addAll(cancelBtn, okBtn);
        
        root.getChildren().addAll(themeBox, fontBox, previewPane, infoLabel, buttonBox);
        
        Scene scene = new Scene(root, 350, 280);
        dialog.setScene(scene);
    }
    
    private void updatePreview() {
        int size = fontSizeSpinner.getValue();
        previewLabel.setFont(Font.font(size));
        previewLabel.setText("Tämä on esimerkkiteksti.\nFonttikoko: " + size + " pt");
    }
    
    public void setOnThemeChanged(Consumer<String> callback) {
        this.onThemeChangedCallback = callback;
    }
    
    public void show() {
        dialog.showAndWait();
    }
}
