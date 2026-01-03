package kirjanpito.ui.javafx.dialogs

import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.control.cell.PropertyValueFactory
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import javafx.stage.Modality
import javafx.stage.Stage
import javafx.stage.Window
import kirjanpito.db.DataAccessException
import kirjanpito.models.StartingBalanceModel
import kirjanpito.ui.javafx.StartingBalanceRowModel
import kirjanpito.ui.javafx.cells.StartingBalanceAmountTableCell
import java.math.BigDecimal
import java.text.DecimalFormat

/**
 * JavaFX alkusaldojen muokkausdialogi.
 */
class StartingBalanceDialogFX(owner: Window?, private val model: StartingBalanceModel) {
    
    private val dialog: Stage = Stage()
    private lateinit var table: TableView<StartingBalanceRowModel>
    private lateinit var balances: ObservableList<StartingBalanceRowModel>
    private val currencyFormat: DecimalFormat = DecimalFormat().apply {
        minimumFractionDigits = 2
        maximumFractionDigits = 2
    }
    private lateinit var assetsTotalLabel: Label
    private lateinit var liabilitiesTotalLabel: Label
    private var modified = false
    
    init {
        dialog.initModality(Modality.APPLICATION_MODAL)
        dialog.initOwner(owner)
        dialog.title = "Alkusaldot"
        dialog.minWidth = 600.0
        dialog.minHeight = 500.0
        
        createContent()
        loadBalances()
    }
    
    private fun createContent() {
        val root = VBox(10.0).apply {
            padding = Insets(15.0)
            style = "-fx-background-color: #ffffff;"
        }
        
        // Toolbar
        val toolbar = HBox(10.0).apply {
            alignment = Pos.CENTER_LEFT
            padding = Insets(0.0, 0.0, 10.0, 0.0)
        }
        
        val saveBtn = Button("💾 Tallenna").apply {
            style = "-fx-background-color: #10b981; -fx-text-fill: white;"
            isDisable = !model.isEditable
            setOnAction { save() }
        }
        
        val copyBtn = Button("📋 Kopioi edellisen tilikauden loppusaldot").apply {
            isDisable = !model.isEditable
            setOnAction { copyFromPreviousPeriod() }
        }
        
        val closeBtn = Button("Sulje").apply {
            setOnAction { close() }
        }
        
        toolbar.children.addAll(saveBtn, copyBtn, Separator(), closeBtn)
        
        // Table
        @Suppress("DEPRECATION")
        table = TableView<StartingBalanceRowModel>().apply {
            isEditable = model.isEditable
            columnResizePolicy = TableView.CONSTRAINED_RESIZE_POLICY
        }
        
        // Columns
        val numberCol = TableColumn<StartingBalanceRowModel, String>("Nro").apply {
            cellValueFactory = PropertyValueFactory("accountNumber")
            prefWidth = 60.0
            isEditable = false
        }
        
        val nameCol = TableColumn<StartingBalanceRowModel, String>("Tili").apply {
            cellValueFactory = PropertyValueFactory("accountName")
            prefWidth = 400.0
            isEditable = false
        }
        
        val balanceCol = TableColumn<StartingBalanceRowModel, BigDecimal>("Alkusaldo").apply {
            cellValueFactory = PropertyValueFactory("balance")
            cellFactory = StartingBalanceAmountTableCell.forTableColumn(currencyFormat)
            prefWidth = 80.0
            isEditable = model.isEditable
        }
        
        table.columns.addAll(numberCol, nameCol, balanceCol)
        
        // Totals row
        val totalsRow = HBox(20.0).apply {
            padding = Insets(10.0, 0.0, 0.0, 0.0)
            alignment = Pos.CENTER_LEFT
            style = "-fx-border-color: #e2e8f0 transparent transparent transparent; -fx-border-width: 1 0 0 0; -fx-padding: 10 0 0 0;"
        }
        
        val assetsLabel = Label("Vastaavaa yht.:")
        assetsTotalLabel = Label("0,00").apply {
            style = "-fx-font-weight: 600;"
        }
        
        val liabilitiesLabel = Label("Vastattavaa yht.:")
        liabilitiesTotalLabel = Label("0,00").apply {
            style = "-fx-font-weight: 600;"
        }
        
        totalsRow.children.addAll(assetsLabel, assetsTotalLabel, Separator(), liabilitiesLabel, liabilitiesTotalLabel)
        
        root.children.addAll(toolbar, table, totalsRow)
        VBox.setVgrow(table, Priority.ALWAYS)
        
        dialog.scene = Scene(root, 600.0, 500.0)
    }
    
    private fun loadBalances() {
        balances = FXCollections.observableArrayList()
        
        for (i in 0 until model.accountCount) {
            val account = model.getAccount(i)
            val balance = model.getBalance(i)
            balances.add(StartingBalanceRowModel(account, balance, currencyFormat))
        }
        
        table.items = balances
        updateTotals()
        
        // Listen for balance changes - update model when user edits
        for (i in balances.indices) {
            val index = i
            val row = balances[i]
            row.balanceProperty().addListener { _, oldVal, newVal ->
                if (newVal != null && (oldVal == null || newVal != oldVal)) {
                    model.setBalance(index, newVal)
                    modified = true
                    updateTotals()
                }
            }
        }
    }
    
    private fun updateTotals() {
        assetsTotalLabel.text = currencyFormat.format(model.assetsTotal)
        liabilitiesTotalLabel.text = currencyFormat.format(model.liabilitiesTotal)
    }
    
    private fun copyFromPreviousPeriod() {
        try {
            if (!model.copyFromPreviousPeriod()) {
                showInfo("Edellisen tilikauden tietoja ei löytynyt.")
                return
            }
            
            // Reload balances
            loadBalances()
            modified = true
            showInfo("Alkusaldot kopioitu edelliseltä tilikaudelta.")
        } catch (e: DataAccessException) {
            showError("Tositetietojen hakeminen epäonnistui: ${e.message}")
            e.printStackTrace()
        }
    }
    
    private fun save() {
        try {
            model.save()
            modified = false
            showInfo("Alkusaldot tallennettu.")
            dialog.close()
        } catch (e: DataAccessException) {
            showError("Alkusaldojen tallentaminen epäonnistui: ${e.message}")
            e.printStackTrace()
        }
    }
    
    private fun close() {
        if (modified && model.isChanged) {
            val alert = Alert(Alert.AlertType.CONFIRMATION).apply {
                title = "Tallennetaanko muutokset?"
                headerText = "Tallennetaanko muutokset?"
                contentText = "Olet tehnyt muutoksia alkusaldoihin. Haluatko tallentaa ne?"
            }
            
            val yesButton = ButtonType("Kyllä")
            val noButton = ButtonType("Ei")
            val cancelButton = ButtonType("Peruuta", ButtonBar.ButtonData.CANCEL_CLOSE)
            
            alert.buttonTypes.setAll(yesButton, noButton, cancelButton)
            
            alert.showAndWait().ifPresent { type ->
                when (type) {
                    yesButton -> {
                        try {
                            model.save()
                            dialog.close()
                        } catch (e: DataAccessException) {
                            showError("Tallentaminen epäonnistui: ${e.message}")
                        }
                    }
                    noButton -> dialog.close()
                }
            }
        } else {
            dialog.close()
        }
    }
    
    private fun showError(message: String) {
        Alert(Alert.AlertType.ERROR).apply {
            title = "Virhe"
            headerText = null
            contentText = message
        }.showAndWait()
    }
    
    private fun showInfo(message: String) {
        Alert(Alert.AlertType.INFORMATION).apply {
            title = "Tieto"
            headerText = null
            contentText = message
        }.showAndWait()
    }
    
    fun show() {
        dialog.showAndWait()
    }
}
