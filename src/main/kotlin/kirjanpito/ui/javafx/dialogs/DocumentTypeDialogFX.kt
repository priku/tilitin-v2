package kirjanpito.ui.javafx.dialogs

import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.control.cell.PropertyValueFactory
import javafx.scene.control.cell.TextFieldTableCell
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import javafx.stage.Modality
import javafx.stage.Stage
import javafx.stage.Window
import javafx.util.converter.IntegerStringConverter
import kirjanpito.db.DataSource
import kirjanpito.db.DocumentType
import java.util.function.Consumer

/**
 * JavaFX tositelajien muokkausdialogi.
 */
class DocumentTypeDialogFX(owner: Window?, private val dataSource: DataSource) {
    
    private val dialog: Stage = Stage()
    private lateinit var table: TableView<DocumentType>
    private lateinit var items: ObservableList<DocumentType>
    
    private var onSave: Consumer<Void?>? = null
    private var modified = false
    private val deleted = mutableListOf<DocumentType>()
    
    init {
        dialog.initModality(Modality.APPLICATION_MODAL)
        dialog.initOwner(owner)
        dialog.title = "Tositelajit"
        dialog.minWidth = 500.0
        dialog.minHeight = 400.0
        
        createContent()
    }
    
    private fun createContent() {
        val root = VBox(10.0).apply {
            padding = Insets(16.0)
            style = "-fx-background-color: #ffffff;"
        }
        
        // Toolbar
        val toolbar = HBox(10.0)
        
        val addBtn = Button("+ Lisää").apply {
            style = "-fx-background-color: #2563eb; -fx-text-fill: white;"
            setOnAction { addItem() }
        }
        
        val removeBtn = Button("Poista").apply {
            setOnAction { removeItem() }
        }
        
        toolbar.children.addAll(addBtn, removeBtn)
        
        // Table
        table = TableView<DocumentType>().apply {
            isEditable = true
        }
        VBox.setVgrow(table, Priority.ALWAYS)
        
        val nameCol = TableColumn<DocumentType, String>("Nimi").apply {
            cellValueFactory = PropertyValueFactory("name")
            cellFactory = TextFieldTableCell.forTableColumn()
            setOnEditCommit { e ->
                e.rowValue.name = e.newValue
                modified = true
            }
            prefWidth = 200.0
        }
        
        val startCol = TableColumn<DocumentType, Int>("Alku").apply {
            cellValueFactory = PropertyValueFactory("numberStart")
            cellFactory = TextFieldTableCell.forTableColumn(IntegerStringConverter())
            setOnEditCommit { e ->
                e.rowValue.numberStart = e.newValue
                modified = true
            }
            prefWidth = 80.0
        }
        
        val endCol = TableColumn<DocumentType, Int>("Loppu").apply {
            cellValueFactory = PropertyValueFactory("numberEnd")
            cellFactory = TextFieldTableCell.forTableColumn(IntegerStringConverter())
            setOnEditCommit { e ->
                e.rowValue.numberEnd = e.newValue
                modified = true
            }
            prefWidth = 80.0
        }
        
        table.columns.addAll(nameCol, startCol, endCol)
        
        // Buttons
        val buttonBox = HBox(10.0).apply {
            alignment = Pos.CENTER_RIGHT
            padding = Insets(10.0, 0.0, 0.0, 0.0)
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
        
        root.children.addAll(toolbar, table, buttonBox)
        
        dialog.scene = Scene(root, 500.0, 400.0)
    }
    
    private fun addItem() {
        val item = DocumentType().apply {
            name = "Uusi tositelaji"
            numberStart = 1
            numberEnd = 999999
        }
        
        items.add(item)
        table.selectionModel.select(item)
        modified = true
    }
    
    private fun removeItem() {
        val selected = table.selectionModel.selectedItem ?: return
        
        if (selected.id > 0) {
            deleted.add(selected)
        }
        items.remove(selected)
        modified = true
    }
    
    private fun save() {
        if (!modified) {
            dialog.close()
            return
        }
        
        var session: kirjanpito.db.Session? = null
        try {
            session = dataSource.openSession()
            val dao = dataSource.getDocumentTypeDAO(session)
            
            for (item in deleted) {
                dao.delete(item.id)
            }
            
            for (item in items) {
                dao.save(item)
            }
            
            session.commit()
            
            onSave?.accept(null)
            
            dialog.close()
            
        } catch (e: Exception) {
            session?.let {
                try { it.rollback() } catch (_: Exception) {}
            }
            Alert(Alert.AlertType.ERROR, "Tallennusvirhe: ${e.message}").showAndWait()
        } finally {
            session?.close()
        }
    }
    
    fun setItems(list: List<DocumentType>) {
        items = FXCollections.observableArrayList(list)
        table.items = items
    }
    
    fun setOnSave(callback: Consumer<Void?>) {
        this.onSave = callback
    }
    
    fun show() {
        dialog.showAndWait()
    }
}
