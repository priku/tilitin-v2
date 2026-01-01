package kirjanpito.ui.javafx.dialogs

import javafx.beans.property.SimpleStringProperty
import javafx.collections.FXCollections
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.layout.*
import javafx.stage.FileChooser
import javafx.stage.Modality
import javafx.stage.Stage
import javafx.stage.Window
import kirjanpito.db.Account
import kirjanpito.db.Period
import kirjanpito.util.CSVReader
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader
import java.math.BigDecimal
import java.nio.charset.Charset
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * JavaFX-dialogi CSV-tiliotteiden tuontiin.
 * Tukee suomalaisia pankkien CSV-muotoja (Nordea, OP, jne.).
 */
class CSVImportDialog(owner: Window?) {

    private val dialog = Stage().apply {
        initModality(Modality.APPLICATION_MODAL)
        initOwner(owner)
        title = "Tuo CSV-tiedosto"
        minWidth = 800.0
        minHeight = 600.0
    }

    // UI Components
    private val fileLabel = Label("Ei valittu").apply {
        style = "-fx-text-fill: #64748b;"
    }
    private val encodingCombo = ComboBox<String>()
    private val separatorCombo = ComboBox<String>()
    private val dateFormatCombo = ComboBox<String>()
    private val dateColumnCombo = ComboBox<String>()
    private val descriptionColumnCombo = ComboBox<String>()
    private val amountColumnCombo = ComboBox<String>()
    private val accountCombo = ComboBox<Account>()
    private val skipHeaderCheckBox = CheckBox("Ohita otsikkorivi").apply { isSelected = true }
    private val previewTable = TableView<CSVRow>()
    private val previewData = FXCollections.observableArrayList<CSVRow>()
    private val statusLabel = Label("Valitse CSV-tiedosto").apply {
        style = "-fx-text-fill: #64748b;"
    }

    // State
    private var csvFile: File? = null
    private var csvData: MutableList<Array<String>> = mutableListOf()
    private var columnCount = 0
    private var accounts: List<Account> = emptyList()
    private var period: Period? = null

    // Result
    private var _importedEntries: List<ImportedEntry>? = null
    val importedEntries: List<ImportedEntry>? get() = _importedEntries
    private var okPressed = false

    init {
        createContent()
    }

    private fun createContent() {
        val root = VBox(12.0).apply {
            padding = Insets(16.0)
            style = "-fx-background-color: #ffffff;"
        }

        val fileSection = createFileSection()
        val settingsSection = createSettingsSection()
        val mappingSection = createMappingSection()
        val previewSection = createPreviewSection().also {
            VBox.setVgrow(it, Priority.ALWAYS)
        }
        val buttonBox = createButtonBox()

        root.children.addAll(fileSection, settingsSection, mappingSection, previewSection, buttonBox)

        dialog.scene = Scene(root, 850.0, 650.0)
    }

    private fun createFileSection(): HBox {
        return HBox(10.0).apply {
            alignment = Pos.CENTER_LEFT
            padding = Insets(10.0)
            style = "-fx-border-color: #e0e0e0; -fx-border-radius: 4; -fx-background-color: #f9fafb; -fx-background-radius: 4;"

            val label = Label("Tiedosto:").apply {
                style = "-fx-font-weight: bold;"
            }
            HBox.setHgrow(fileLabel, Priority.ALWAYS)

            val browseButton = Button("Selaa...").apply {
                setOnAction { selectFile() }
            }

            children.addAll(label, fileLabel, browseButton)
        }
    }

    private fun createSettingsSection(): HBox {
        return HBox(20.0).apply {
            alignment = Pos.CENTER_LEFT
            padding = Insets(10.0)
            style = "-fx-border-color: #e0e0e0; -fx-border-radius: 4; -fx-background-color: #f9fafb; -fx-background-radius: 4;"

            // Encoding
            val encodingBox = VBox(4.0).apply {
                children.addAll(
                    Label("Merkistö:"),
                    encodingCombo.apply {
                        items.addAll("UTF-8", "ISO-8859-1", "ISO-8859-15", "Windows-1252")
                        value = "UTF-8"
                        setOnAction { reloadFile() }
                    }
                )
            }

            // Separator
            val sepBox = VBox(4.0).apply {
                children.addAll(
                    Label("Erotin:"),
                    separatorCombo.apply {
                        items.addAll("Pilkku (,)", "Puolipiste (;)", "Sarkain (Tab)")
                        value = "Puolipiste (;)"  // Default to semicolon for Finnish banks
                        setOnAction { reloadFile() }
                    }
                )
            }

            // Date format
            val dateFormatBox = VBox(4.0).apply {
                children.addAll(
                    Label("Päivämäärämuoto:"),
                    dateFormatCombo.apply {
                        items.addAll("dd.MM.yyyy", "yyyy-MM-dd", "d.M.yyyy", "dd/MM/yyyy")
                        value = "dd.MM.yyyy"
                    }
                )
            }

            // Skip header
            skipHeaderCheckBox.setOnAction { updatePreview() }

            children.addAll(encodingBox, sepBox, dateFormatBox, skipHeaderCheckBox)
        }
    }

    private fun createMappingSection(): VBox {
        val titleLabel = Label("Sarakemääritykset").apply {
            style = "-fx-font-weight: bold;"
        }

        val mappingBox = HBox(20.0).apply {
            alignment = Pos.CENTER_LEFT
            padding = Insets(10.0)
            style = "-fx-border-color: #e0e0e0; -fx-border-radius: 4; -fx-background-color: #f9fafb; -fx-background-radius: 4;"

            val dateColBox = VBox(4.0).apply {
                children.addAll(Label("Päivämäärä:"), dateColumnCombo)
            }

            val descColBox = VBox(4.0).apply {
                children.addAll(Label("Selite:"), descriptionColumnCombo)
            }

            val amountColBox = VBox(4.0).apply {
                children.addAll(Label("Summa:"), amountColumnCombo)
            }

            val accountBox = VBox(4.0).apply {
                children.addAll(
                    Label("Pankkitili:"),
                    accountCombo.apply {
                        promptText = "Valitse tili..."
                        prefWidth = 250.0
                        // Näytä tilin numero ja nimi
                        setCellFactory { 
                            object : ListCell<Account>() {
                                override fun updateItem(item: Account?, empty: Boolean) {
                                    super.updateItem(item, empty)
                                    text = if (empty || item == null) null else "${item.number} ${item.name}"
                                }
                            }
                        }
                        buttonCell = object : ListCell<Account>() {
                            override fun updateItem(item: Account?, empty: Boolean) {
                                super.updateItem(item, empty)
                                text = if (empty || item == null) null else "${item.number} ${item.name}"
                            }
                        }
                    }
                )
            }

            children.addAll(dateColBox, descColBox, amountColBox, accountBox)
        }

        return VBox(4.0).apply {
            children.addAll(titleLabel, mappingBox)
        }
    }

    private fun createPreviewSection(): VBox {
        val titleLabel = Label("Esikatselu").apply {
            style = "-fx-font-weight: bold;"
        }

        previewTable.apply {
            columnResizePolicy = TableView.CONSTRAINED_RESIZE_POLICY
            items = previewData
        }
        VBox.setVgrow(previewTable, Priority.ALWAYS)

        return VBox(4.0).apply {
            children.addAll(titleLabel, previewTable, statusLabel)
            VBox.setVgrow(previewTable, Priority.ALWAYS)
        }.also {
            VBox.setVgrow(it, Priority.ALWAYS)
        }
    }

    private fun createButtonBox(): HBox {
        return HBox(10.0).apply {
            alignment = Pos.CENTER_RIGHT
            padding = Insets(10.0, 0.0, 0.0, 0.0)

            val importButton = Button("Tuo").apply {
                isDefaultButton = true
                style = "-fx-background-color: #2563eb; -fx-text-fill: white; -fx-min-width: 100;"
                setOnAction { handleImport() }
            }

            val cancelButton = Button("Peruuta").apply {
                isCancelButton = true
                style = "-fx-min-width: 80;"
                setOnAction { dialog.close() }
            }

            children.addAll(cancelButton, importButton)
        }
    }

    private fun selectFile() {
        val fileChooser = FileChooser().apply {
            title = "Valitse CSV-tiedosto"
            extensionFilters.addAll(
                FileChooser.ExtensionFilter("CSV-tiedostot", "*.csv"),
                FileChooser.ExtensionFilter("Kaikki tiedostot", "*.*")
            )
        }

        fileChooser.showOpenDialog(dialog)?.let { file ->
            csvFile = file
            fileLabel.text = file.name
            loadFile()
        }
    }

    private fun reloadFile() {
        if (csvFile != null) loadFile()
    }

    private fun loadFile() {
        val file = csvFile ?: return

        try {
            val encoding = encodingCombo.value
            val separator = getSeparator()
            csvData = mutableListOf()

            InputStreamReader(FileInputStream(file), Charset.forName(encoding)).use { reader ->
                val csvReader = CSVReader(reader, separator)
                var line: Array<String>?
                while (csvReader.readLine().also { line = it } != null) {
                    csvData.add(line!!)
                }
            }

            if (csvData.isNotEmpty()) {
                columnCount = csvData.maxOfOrNull { it.size } ?: 0
                updateColumnCombos()
                updatePreview()
                statusLabel.text = "Ladattu ${csvData.size} riviä"
            } else {
                statusLabel.text = "Tiedosto on tyhjä"
            }

        } catch (e: Exception) {
            statusLabel.text = "Virhe: ${e.message}"
            showError("Virhe luettaessa tiedostoa", e.message ?: "Tuntematon virhe")
        }
    }

    private fun updateColumnCombos() {
        dateColumnCombo.items.clear()
        descriptionColumnCombo.items.clear()
        amountColumnCombo.items.clear()

        // Get sample row
        val sampleRow = if (csvData.isNotEmpty()) {
            val sampleIndex = if (skipHeaderCheckBox.isSelected && csvData.size > 1) 1 else 0
            csvData[sampleIndex]
        } else null

        // Add column options with sample values
        for (i in 0 until columnCount) {
            val sample = sampleRow?.getOrNull(i)?.truncate(20) ?: ""
            val label = "${i + 1}: $sample"
            dateColumnCombo.items.add(label)
            descriptionColumnCombo.items.add(label)
            amountColumnCombo.items.add(label)
        }

        // Auto-detect columns
        var detectedDateCol = -1
        var detectedAmountCol = -1
        var detectedDescCol = -1

        sampleRow?.forEachIndexed { i, value ->
            val trimmed = value.trim()
            when {
                detectedDateCol < 0 && trimmed.isDateLike() -> detectedDateCol = i
                trimmed.isAmountLike() -> detectedAmountCol = i
            }
        }

        // Find description column
        sampleRow?.forEachIndexed { i, value ->
            if (i != detectedDateCol && i != detectedAmountCol) {
                val trimmed = value.trim()
                if (trimmed.isNotEmpty() && !trimmed.matches(Regex("-?\\d+[,.]?\\d*"))) {
                    detectedDescCol = i
                    if (i >= 2) return@forEachIndexed
                }
            }
        }

        // Set detected values
        dateColumnCombo.value = dateColumnCombo.items.getOrNull(detectedDateCol) 
            ?: dateColumnCombo.items.firstOrNull()
        
        descriptionColumnCombo.value = descriptionColumnCombo.items.getOrNull(detectedDescCol)
            ?: descriptionColumnCombo.items.getOrNull(2)
        
        amountColumnCombo.value = amountColumnCombo.items.getOrNull(detectedAmountCol)
            ?: amountColumnCombo.items.lastOrNull()
    }

    private fun updatePreview() {
        previewData.clear()
        previewTable.columns.clear()

        if (csvData.isEmpty()) return

        // Create columns
        for (i in 0 until columnCount) {
            val col = TableColumn<CSVRow, String>("Sarake ${i + 1}").apply {
                setCellValueFactory { cellData ->
                    SimpleStringProperty(cellData.value.data.getOrNull(i) ?: "")
                }
                prefWidth = 150.0
            }
            previewTable.columns.add(col)
        }

        // Add data rows (limit to 100)
        val startRow = if (skipHeaderCheckBox.isSelected) 1 else 0
        val maxRows = minOf(csvData.size, startRow + 100)

        for (i in startRow until maxRows) {
            previewData.add(CSVRow(csvData[i]))
        }
    }

    private fun handleImport() {
        if (csvData.isEmpty()) {
            showError("Virhe", "Valitse ensin CSV-tiedosto")
            return
        }

        if (dateColumnCombo.value == null ||
            descriptionColumnCombo.value == null ||
            amountColumnCombo.value == null) {
            showError("Virhe", "Määritä kaikki sarakkeet (päivämäärä, selite, summa)")
            return
        }

        if (accountCombo.value == null) {
            showError("Virhe", "Valitse pankkitili")
            return
        }

        // Parse entries
        val entries = mutableListOf<ImportedEntry>()
        val dateCol = getColumnNumber(dateColumnCombo.value) - 1
        val descCol = getColumnNumber(descriptionColumnCombo.value) - 1
        val amountCol = getColumnNumber(amountColumnCombo.value) - 1
        val formatter = DateTimeFormatter.ofPattern(dateFormatCombo.value)

        val startRow = if (skipHeaderCheckBox.isSelected) 1 else 0
        var errors = 0

        for (i in startRow until csvData.size) {
            val row = csvData[i]
            try {
                val dateStr = row.getOrNull(dateCol)?.trim() ?: ""
                val date = LocalDate.parse(dateStr, formatter)

                val description = row.getOrNull(descCol)?.trim() ?: ""

                val amountStr = row.getOrNull(amountCol)?.trim()
                    ?.replace(",", ".")
                    ?.replace(" ", "")
                    ?.replace("€", "") ?: "0"
                val amount = BigDecimal(amountStr)

                entries.add(ImportedEntry(
                    date = date,
                    description = description,
                    amount = amount,
                    account = accountCombo.value
                ))
            } catch (e: Exception) {
                errors++
            }
        }

        if (entries.isEmpty()) {
            showError("Virhe", "Yhtään riviä ei voitu tuoda. Tarkista sarakemääritykset ja päivämäärämuoto.")
            return
        }

        val message = buildString {
            append("Tuodaan ${entries.size} vientiä")
            if (errors > 0) append(" ($errors riviä ohitettu virheiden vuoksi)")
        }

        val confirm = Alert(Alert.AlertType.CONFIRMATION).apply {
            initOwner(dialog)
            title = "Vahvista tuonti"
            headerText = message
            contentText = "Haluatko jatkaa?"
        }

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            _importedEntries = entries
            okPressed = true
            dialog.close()
        }
    }

    private fun getSeparator(): Char {
        val selected = separatorCombo.value ?: return ','
        return when {
            selected.contains("Puolipiste") || selected.contains(";") -> ';'
            selected.contains("Sarkain") || selected.contains("Tab") -> '\t'
            else -> ','
        }
    }

    private fun getColumnNumber(comboValue: String?): Int {
        if (comboValue.isNullOrEmpty()) return -1
        val colonIndex = comboValue.indexOf(':')
        return if (colonIndex > 0) {
            comboValue.substring(0, colonIndex).trim().toIntOrNull() ?: -1
        } else -1
    }

    private fun showError(title: String, message: String) {
        Alert(Alert.AlertType.ERROR).apply {
            initOwner(dialog)
            this.title = title
            headerText = null
            contentText = message
        }.showAndWait()
    }

    // Public API

    fun setAccounts(accounts: List<Account>) {
        this.accounts = accounts
        accountCombo.items.clear()

        // Filter to bank accounts (19xx in Finnish chart)
        val bankAccounts = accounts.filter { acc ->
            acc.number.startsWith("19") ||
            acc.name.lowercase().contains("pankki") ||
            acc.name.lowercase().contains("shekki")
        }

        if (bankAccounts.isNotEmpty()) {
            accountCombo.items.addAll(bankAccounts)
        } else {
            accountCombo.items.addAll(accounts)
        }
    }

    fun setPeriod(period: Period) {
        this.period = period
    }

    fun showAndWait(): Boolean {
        dialog.showAndWait()
        return okPressed
    }

    // Extension functions
    private fun String.truncate(maxLen: Int): String {
        val trimmed = this.trim()
        return if (trimmed.length <= maxLen) trimmed else trimmed.take(maxLen) + "..."
    }

    private fun String.isDateLike(): Boolean =
        matches(Regex("\\d{1,2}\\.\\d{1,2}\\.\\d{4}")) ||
        matches(Regex("\\d{4}-\\d{2}-\\d{2}"))

    private fun String.isAmountLike(): Boolean {
        // Match Finnish format: -1500,00 or 1 234,56 or -1.234,56
        // Also international: -1500.00 or 1,234.56
        // Require decimal separator to distinguish from reference numbers
        val cleaned = this.replace(" ", "").replace(" ", "") // Normal and non-breaking space
        return cleaned.matches(Regex("-?\\d{1,3}([.,]\\d{3})*[,.]\\d{2}")) ||  // With thousands separator
               cleaned.matches(Regex("-?\\d+[,.]\\d{2}"))                       // Simple decimal
    }

    // Data classes
    data class CSVRow(val data: Array<String>)

    data class ImportedEntry(
        val date: LocalDate,
        val description: String,
        val amount: BigDecimal,
        val account: Account
    )

    companion object {
        /**
         * Factory method for creating the dialog.
         */
        fun create(owner: Window?, accounts: List<Account>, period: Period): CSVImportDialog {
            return CSVImportDialog(owner).apply {
                setAccounts(accounts)
                setPeriod(period)
            }
        }
    }
}
