package kirjanpito.ui.javafx.dialogs;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import kirjanpito.util.AppSettings;

import java.io.*;
import java.util.Properties;
import java.util.Optional;

/**
 * Asetusten vienti ja tuonti.
 */
public class SettingsExportImportFX {
    
    private Window owner;
    private AppSettings settings;
    
    public SettingsExportImportFX(Window owner) {
        this.owner = owner;
        this.settings = AppSettings.getInstance();
    }
    
    /**
     * Vie asetukset tiedostoon.
     */
    public void exportSettings() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Vie asetukset");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Asetustiedosto (*.properties)", "*.properties")
        );
        fileChooser.setInitialFileName("tilitin-asetukset.properties");
        
        File file = fileChooser.showSaveDialog(owner);
        if (file != null) {
            try {
                // Lue nykyiset asetukset
                File settingsFile = settings.getFile();
                if (settingsFile != null && settingsFile.exists()) {
                    Properties props = new Properties();
                    try (FileInputStream fis = new FileInputStream(settingsFile)) {
                        props.load(fis);
                    }
                    
                    // Kirjoita uuteen tiedostoon
                    try (FileOutputStream fos = new FileOutputStream(file)) {
                        props.store(fos, "Tilitin asetukset - viety " + java.time.LocalDateTime.now());
                    }
                    
                    showInfo("Asetukset viety", "Asetukset vietiin onnistuneesti:\n" + file.getAbsolutePath());
                } else {
                    showError("Virhe", "Asetustiedostoa ei löytynyt.");
                }
            } catch (IOException e) {
                showError("Virhe viennissä", "Asetusten vienti epäonnistui: " + e.getMessage());
            }
        }
    }
    
    /**
     * Tuo asetukset tiedostosta.
     */
    public void importSettings() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Tuo asetukset");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Asetustiedosto (*.properties)", "*.properties")
        );
        
        File file = fileChooser.showOpenDialog(owner);
        if (file != null) {
            // Varmista käyttäjältä
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.initOwner(owner);
            confirm.setTitle("Vahvista tuonti");
            confirm.setHeaderText("Tuodaanko asetukset?");
            confirm.setContentText("Nykyiset asetukset korvataan tuotavilla asetuksilla.\n\nHaluatko jatkaa?");
            
            Optional<ButtonType> result = confirm.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                try {
                    // Lue tuotavat asetukset
                    Properties importProps = new Properties();
                    try (FileInputStream fis = new FileInputStream(file)) {
                        importProps.load(fis);
                    }
                    
                    // Päivitä asetukset
                    File settingsFile = settings.getFile();
                    if (settingsFile != null) {
                        try (FileOutputStream fos = new FileOutputStream(settingsFile)) {
                            importProps.store(fos, "Tilitin asetukset - tuotu " + java.time.LocalDateTime.now());
                        }
                        
                        // Lataa asetukset uudelleen
                        settings.load(settingsFile);
                        
                        showInfo("Asetukset tuotu", 
                            "Asetukset tuotiin onnistuneesti.\n\nOsa asetuksista astuu voimaan ohjelman uudelleenkäynnistyksen jälkeen.");
                    }
                } catch (IOException e) {
                    showError("Virhe tuonnissa", "Asetusten tuonti epäonnistui: " + e.getMessage());
                }
            }
        }
    }
    
    private void showInfo(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.initOwner(owner);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.initOwner(owner);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
