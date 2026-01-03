package kirjanpito.ui.javafx.dialogs

import javafx.scene.control.Alert
import javafx.scene.control.ButtonType
import javafx.stage.FileChooser
import javafx.stage.Window
import kirjanpito.util.AppSettings
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.time.LocalDateTime
import java.util.Properties

/**
 * Asetusten vienti ja tuonti.
 */
class SettingsExportImportFX(private val owner: Window?) {
    
    private val settings: AppSettings = AppSettings.getInstance()
    
    /**
     * Vie asetukset tiedostoon.
     */
    fun exportSettings() {
        val fileChooser = FileChooser().apply {
            title = "Vie asetukset"
            extensionFilters.add(
                FileChooser.ExtensionFilter("Asetustiedosto (*.properties)", "*.properties")
            )
            initialFileName = "tilitin-asetukset.properties"
        }
        
        val file = fileChooser.showSaveDialog(owner)
        if (file != null) {
            try {
                // Lue nykyiset asetukset
                val settingsFile = settings.file
                if (settingsFile != null && settingsFile.exists()) {
                    val props = Properties()
                    FileInputStream(settingsFile).use { fis ->
                        props.load(fis)
                    }
                    
                    // Kirjoita uuteen tiedostoon
                    FileOutputStream(file).use { fos ->
                        props.store(fos, "Tilitin asetukset - viety ${LocalDateTime.now()}")
                    }
                    
                    showInfo("Asetukset viety", "Asetukset vietiin onnistuneesti:\n${file.absolutePath}")
                } else {
                    showError("Virhe", "Asetustiedostoa ei löytynyt.")
                }
            } catch (e: IOException) {
                showError("Virhe viennissä", "Asetusten vienti epäonnistui: ${e.message}")
            }
        }
    }
    
    /**
     * Tuo asetukset tiedostosta.
     */
    fun importSettings() {
        val fileChooser = FileChooser().apply {
            title = "Tuo asetukset"
            extensionFilters.add(
                FileChooser.ExtensionFilter("Asetustiedosto (*.properties)", "*.properties")
            )
        }
        
        val file = fileChooser.showOpenDialog(owner)
        if (file != null) {
            // Varmista käyttäjältä
            val confirm = Alert(Alert.AlertType.CONFIRMATION).apply {
                initOwner(this@SettingsExportImportFX.owner)
                title = "Vahvista tuonti"
                headerText = "Tuodaanko asetukset?"
                contentText = "Nykyiset asetukset korvataan tuotavilla asetuksilla.\n\nHaluatko jatkaa?"
            }
            
            val result = confirm.showAndWait()
            if (result.isPresent && result.get() == ButtonType.OK) {
                try {
                    // Lue tuotavat asetukset
                    val importProps = Properties()
                    FileInputStream(file).use { fis ->
                        importProps.load(fis)
                    }
                    
                    // Päivitä asetukset
                    val settingsFile = settings.file
                    if (settingsFile != null) {
                        FileOutputStream(settingsFile).use { fos ->
                            importProps.store(fos, "Tilitin asetukset - tuotu ${LocalDateTime.now()}")
                        }
                        
                        // Lataa asetukset uudelleen
                        settings.load(settingsFile)
                        
                        showInfo(
                            "Asetukset tuotu",
                            "Asetukset tuotiin onnistuneesti.\n\nOsa asetuksista astuu voimaan ohjelman uudelleenkäynnistyksen jälkeen."
                        )
                    }
                } catch (e: IOException) {
                    showError("Virhe tuonnissa", "Asetusten tuonti epäonnistui: ${e.message}")
                }
            }
        }
    }
    
    private fun showInfo(title: String, content: String) {
        Alert(Alert.AlertType.INFORMATION).apply {
            initOwner(this@SettingsExportImportFX.owner)
            this.title = title
            headerText = null
            contentText = content
        }.showAndWait()
    }
    
    private fun showError(title: String, content: String) {
        Alert(Alert.AlertType.ERROR).apply {
            initOwner(this@SettingsExportImportFX.owner)
            this.title = title
            headerText = null
            contentText = content
        }.showAndWait()
    }
}
