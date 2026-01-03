package kirjanpito.ui.javafx.dialogs

import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.stage.Modality
import javafx.stage.Stage
import javafx.stage.Window
import kirjanpito.util.AppSettings

/**
 * JavaFX yleiset asetukset -dialogi.
 */
class SettingsDialogFX(owner: Window?) {
    
    private val dialog: Stage = Stage()
    private lateinit var autoSaveCheck: CheckBox
    private lateinit var showVatColumnCheck: CheckBox
    private lateinit var backupIntervalSpinner: Spinner<Int>
    
    private val settings: AppSettings = AppSettings.getInstance()
    
    init {
        dialog.initModality(Modality.APPLICATION_MODAL)
        dialog.initOwner(owner)
        dialog.title = "Yleiset asetukset"
        dialog.isResizable = false
        
        createContent()
    }
    
    private fun createContent() {
        val root = VBox(20.0).apply {
            padding = Insets(20.0)
            style = "-fx-background-color: #ffffff;"
        }
        
        // General settings
        val generalPane = TitledPane().apply {
            text = "Kirjaus"
            isCollapsible = false
        }
        
        val generalBox = VBox(10.0).apply {
            padding = Insets(10.0)
        }
        
        autoSaveCheck = CheckBox("Tallenna automaattisesti siirryttäessä tositteesta toiseen").apply {
            isSelected = settings.getBoolean("autoSave", true)
        }
        
        showVatColumnCheck = CheckBox("Näytä ALV-sarake vientitaulukossa").apply {
            isSelected = settings.getBoolean("showVatColumn", true)
        }
        
        generalBox.children.addAll(autoSaveCheck, showVatColumnCheck)
        generalPane.content = generalBox
        
        // Backup settings
        val backupPane = TitledPane().apply {
            text = "Varmuuskopiointi"
            isCollapsible = false
        }
        
        val backupBox = VBox(10.0).apply {
            padding = Insets(10.0)
        }
        
        val intervalBox = HBox(10.0).apply {
            alignment = Pos.CENTER_LEFT
        }
        
        val intervalLabel = Label("Varmuuskopiointiväli (minuuttia):")
        
        backupIntervalSpinner = Spinner<Int>(0, 60, settings.getInt("backupInterval", 10)).apply {
            isEditable = true
            prefWidth = 80.0
        }
        
        intervalBox.children.addAll(intervalLabel, backupIntervalSpinner)
        backupBox.children.add(intervalBox)
        backupPane.content = backupBox
        
        // Buttons
        val buttonBox = HBox(10.0).apply {
            alignment = Pos.CENTER_RIGHT
        }
        
        val saveBtn = Button("Tallenna").apply {
            isDefaultButton = true
            style = "-fx-background-color: #2563eb; -fx-text-fill: white;"
            setOnAction { save() }
        }
        
        val cancelBtn = Button("Peruuta").apply {
            isCancelButton = true
            setOnAction { dialog.close() }
        }
        
        buttonBox.children.addAll(cancelBtn, saveBtn)
        
        root.children.addAll(generalPane, backupPane, buttonBox)
        
        val scene = Scene(root, 450.0, 280.0)
        dialog.scene = scene
    }
    
    private fun save() {
        settings.set("autoSave", autoSaveCheck.isSelected)
        settings.set("showVatColumn", showVatColumnCheck.isSelected)
        settings.set("backupInterval", backupIntervalSpinner.value)
        
        settings.save()
        dialog.close()
    }
    
    fun show() {
        dialog.showAndWait()
    }
}
