package kirjanpito.ui.javafx.dialogs

import javafx.application.Platform
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.collections.transformation.FilteredList
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Parent
import javafx.scene.control.*
import javafx.scene.control.cell.PropertyValueFactory
import javafx.scene.input.KeyCode
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import javafx.stage.Window
import kirjanpito.db.Account

/**
 * JavaFX tilinvalintadialogi (F9).
 * 
 * Näyttää tilikartan ja mahdollistaa tilin valinnan hakusanalla.
 * 
 * Migrated from Java to Kotlin for better code conciseness and null-safety.
 */
class AccountSelectionDialogFX(owner: Window?) : BaseDialogFX(
    owner = owner,
    title = "Tilin valinta",
    width = 550.0,
    height = 450.0,
    resizable = true
) {
    
    private var searchField: TextField? = null
    private var accountTable: TableView<Account>? = null
    private var allAccounts: ObservableList<Account>? = null
    private var filteredAccounts: FilteredList<Account>? = null
    private var onAccountSelected: ((Account) -> Unit)? = null
    private var selectedAccount: Account? = null
    private var showFavoritesOnly: CheckBox? = null
    
    override fun hasButtons(): Boolean = true
    
    override fun createOKButton(): Button {
        return Button("Valitse").apply {
            isDefaultButton = true
            style = "-fx-background-color: #2563eb; -fx-text-fill: white; -fx-min-width: 80;"
            setOnAction {
                if (onOK()) {
                    dialog.close()
                }
            }
        }
    }
    
    override fun createContent(): Parent {
        val root = VBox(10.0).apply {
            padding = Insets(16.0)
            style = "-fx-background-color: #ffffff;"
        }
        
        // Search field
        val searchBox = HBox(10.0).apply {
            alignment = Pos.CENTER_LEFT
        }
        
        val searchLabel = Label("Haku:")
        searchField = TextField().apply {
            promptText = "Tilinumero tai nimi..."
            prefWidth = 300.0
            HBox.setHgrow(this, Priority.ALWAYS)
        }
        
        showFavoritesOnly = CheckBox("Vain suosikit")
        
        searchBox.children.addAll(searchLabel, searchField, showFavoritesOnly)
        
        // Table
        accountTable = TableView<Account>().apply {
            columnResizePolicy = TableView.CONSTRAINED_RESIZE_POLICY
            VBox.setVgrow(this, Priority.ALWAYS)
        }
        
        val numberCol = TableColumn<Account, String>("Numero").apply {
            cellValueFactory = PropertyValueFactory("number")
            prefWidth = 80.0
            minWidth = 60.0
        }
        
        val nameCol = TableColumn<Account, String>("Nimi").apply {
            cellValueFactory = PropertyValueFactory("name")
            prefWidth = 300.0
        }
        
        val typeCol = TableColumn<Account, String>("Tyyppi").apply {
            cellValueFactory = javafx.util.Callback<TableColumn.CellDataFeatures<Account, String>, javafx.beans.value.ObservableValue<String>> { cellData ->
                val account = cellData.value
                javafx.beans.property.SimpleStringProperty(getAccountTypeName(account.type))
            }
            prefWidth = 100.0
        }
        
        accountTable?.columns?.addAll(numberCol, nameCol, typeCol)
        
        root.children.addAll(searchBox, accountTable)
        
        // Event handlers
        setupEventHandlers()
        
        return root
    }
    
    private fun setupEventHandlers() {
        val search = searchField ?: return
        val table = accountTable ?: return
        val favorites = showFavoritesOnly ?: return
        
        // Search filter
        search.textProperty().addListener { _, _, newVal ->
            filterAccounts(newVal)
        }
        
        // Favorites filter
        favorites.selectedProperty().addListener { _, _, _ ->
            filterAccounts(search.text)
        }
        
        // Double-click to select
        table.setOnMouseClicked { event ->
            if (event.clickCount == 2) {
                selectAndClose()
            }
        }
        
        // Enter to select, Escape to close
        table.setOnKeyPressed { event ->
            when (event.code) {
                KeyCode.ENTER -> selectAndClose()
                KeyCode.ESCAPE -> dialog.close()
                else -> {}
            }
        }
        
        // Search field Enter/Down moves to table
        search.setOnKeyPressed { event ->
            when (event.code) {
                KeyCode.ENTER, KeyCode.DOWN -> {
                    if (!filteredAccounts.isNullOrEmpty()) {
                        table.selectionModel.selectFirst()
                        table.requestFocus()
                    }
                }
                KeyCode.ESCAPE -> dialog.close()
                else -> {}
            }
        }
    }
    
    private fun filterAccounts(search: String?) {
        val filtered = filteredAccounts ?: return
        val favorites = showFavoritesOnly ?: return
        
        val searchLower = search?.lowercase()?.trim() ?: ""
        val favoritesOnly = favorites.isSelected
        
        filtered.setPredicate { account: Account ->
            // Favorites filter (flag bit 0 = favorite)
            if (favoritesOnly && (account.flags and 1) == 0) {
                return@setPredicate false
            }
            
            // Search filter
            if (searchLower.isEmpty()) {
                return@setPredicate true
            }
            
            // Match by number prefix
            if (account.number.startsWith(searchLower, ignoreCase = true)) {
                return@setPredicate true
            }
            
            // Match by name
            account.name.lowercase().contains(searchLower)
        }
        
        // Select first match
        if (filtered.isNotEmpty()) {
            accountTable?.selectionModel?.selectFirst()
        }
    }
    
    private fun selectAndClose() {
        val table = accountTable ?: return
        selectedAccount = table.selectionModel.selectedItem
        selectedAccount?.let { account ->
            onAccountSelected?.invoke(account)
        }
        dialog.close()
    }
    
    private fun getAccountTypeName(type: Int): String {
        return when (type) {
            1 -> "Vastaavaa"
            2 -> "Vastattavaa"
            3 -> "Tulos"
            4 -> "Oma pääoma"
            5 -> "Edellisten voitto"
            else -> ""
        }
    }
    
    fun setAccounts(accounts: List<Account>) {
        allAccounts = FXCollections.observableArrayList(accounts)
        filteredAccounts = FilteredList(allAccounts!!) { true }
        accountTable?.items = filteredAccounts
    }
    
    fun setOnAccountSelected(callback: (Account) -> Unit) {
        this.onAccountSelected = callback
    }
    
    fun setInitialSearch(search: String?) {
        if (!search.isNullOrEmpty()) {
            searchField?.text = search
            filterAccounts(search)
        }
    }
    
    fun getSelectedAccount(): Account? = selectedAccount
    
    override fun onOK(): Boolean {
        selectAndClose()
        return true
    }
    
    override fun show() {
        // Focus search field
        Platform.runLater {
            searchField?.requestFocus()
            searchField?.selectAll()
        }
        
        super.show()
    }
    
    companion object {
        /**
         * Näyttää dialogin ja palauttaa valitun tilin.
         */
        @JvmStatic
        fun showAndSelect(owner: Window?, accounts: List<Account>, initialSearch: String?): Account? {
            val dialog = AccountSelectionDialogFX(owner)
            dialog.setAccounts(accounts)
            if (initialSearch != null) {
                dialog.setInitialSearch(initialSearch)
            }
            dialog.showAndWait()
            return dialog.getSelectedAccount()
        }
    }
}
