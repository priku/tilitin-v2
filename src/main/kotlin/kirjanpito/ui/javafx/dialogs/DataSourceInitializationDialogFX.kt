package kirjanpito.ui.javafx.dialogs

import javafx.collections.FXCollections
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import javafx.stage.Modality
import javafx.stage.Stage
import javafx.stage.Window
import kirjanpito.models.DataSourceInitializationModel
import kirjanpito.models.DataSourceInitializationWorker
import kirjanpito.util.Registry

/**
 * JavaFX-dialogi tilikarttamallin valintaan uutta tietokantaa luotaessa.
 * Korvaa Swing-toteutuksen DataSourceInitializationDialog.java.
 */
class DataSourceInitializationDialogFX(
    owner: Window?,
    private val registry: Registry,
    private val model: DataSourceInitializationModel
) {
    private val dialog: Stage = Stage()
    private lateinit var comboBox: ComboBox<String>
    
    /**
     * Palauttaa workerin, joka alustaa tietokannan.
     * @return worker tai null jos käyttäjä peruutti
     */
    var worker: DataSourceInitializationWorker? = null
        private set

    init {
        dialog.initModality(Modality.APPLICATION_MODAL)
        dialog.initOwner(owner)
        dialog.title = "Tietokannan luonti"
        dialog.isResizable = false
        
        createContent()
    }

    private fun createContent() {
        val root = VBox(16.0).apply {
            padding = Insets(20.0)
            style = "-fx-background-color: #ffffff;"
            prefWidth = 400.0
        }
        
        // Otsikko
        val titleLabel = Label("Valitse tilikarttamalli").apply {
            style = "-fx-font-weight: bold; -fx-font-size: 14px;"
        }
        
        // Selite
        val infoLabel = Label(
            "Tilikartta määrittää kirjanpidon tilit ja niiden numerot. " +
            "Valitse sopiva malli yrityksellesi."
        ).apply {
            isWrapText = true
            style = "-fx-text-fill: #666666;"
        }
        
        // Tilikarttamallit
        val comboRow = HBox(10.0).apply {
            alignment = Pos.CENTER_LEFT
        }
        
        val comboLabel = Label("Tilikarttamalli:")
        
        val modelNames = model.modelNames.sorted().toTypedArray()
        
        comboBox = ComboBox(FXCollections.observableArrayList(*modelNames)).apply {
            prefWidth = 250.0
            if (modelNames.isNotEmpty()) {
                selectionModel.selectFirst()
            }
        }
        
        comboRow.children.addAll(comboLabel, comboBox)
        HBox.setHgrow(comboBox, Priority.ALWAYS)
        
        // Painikkeet
        val buttonBox = HBox(10.0).apply {
            alignment = Pos.CENTER_RIGHT
            padding = Insets(10.0, 0.0, 0.0, 0.0)
        }
        
        val cancelBtn = Button("Peruuta").apply {
            prefWidth = 100.0
            setOnAction { dialog.close() }
        }
        
        val okBtn = Button("OK").apply {
            prefWidth = 100.0
            isDefaultButton = true
            setOnAction { createWorker() }
        }
        
        buttonBox.children.addAll(cancelBtn, okBtn)
        
        root.children.addAll(titleLabel, infoLabel, comboRow, buttonBox)
        
        dialog.scene = Scene(root)
        dialog.sizeToScene()
    }

    private fun createWorker() {
        val selectedIndex = comboBox.selectionModel.selectedIndex
        
        if (selectedIndex < 0) {
            return
        }
        
        val modelName = comboBox.selectionModel.selectedItem
        worker = DataSourceInitializationWorker(
            registry.dataSource, model, modelName
        )
        dialog.close()
        worker?.execute()
    }

    /**
     * Näyttää dialogin ja odottaa sen sulkemista.
     */
    fun show() {
        dialog.showAndWait()
    }
}
