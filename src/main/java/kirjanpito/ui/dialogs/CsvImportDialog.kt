package kirjanpito.ui.dialogs

import kirjanpito.util.Registry
import kirjanpito.util.csv.*
import java.awt.*
import java.io.File
import javax.swing.*
import javax.swing.filechooser.FileNameExtensionFilter
import javax.swing.table.AbstractTableModel
import javax.swing.table.DefaultTableCellRenderer

/**
 * Dialog for importing CSV files into accounting entries.
 * 
 * Features:
 * - Automatic encoding detection (UTF-8, ISO-8859-1, Windows-1252)
 * - Automatic delimiter detection (comma, semicolon, tab)
 * - Column type detection (date, money, text, etc.)
 * - Smart column mapping suggestions
 * - Preview of CSV data
 */
class CsvImportDialog(
    parent: JFrame,
    private val registry: Registry
) : JDialog(parent, "CSV-tuonti", true) {

    private var csvData: CsvData? = null
    private var columns: List<ColumnInfo> = emptyList()
    private var selectedFile: File? = null

    // UI Components
    private val previewTable = JTable()
    private val mappingTable = JTable()
    private val statusLabel = JLabel("Valitse CSV-tiedosto")
    private val encodingLabel = JLabel("")
    private val delimiterLabel = JLabel("")

    private val fileButton = JButton("Valitse tiedosto...")
    private val importButton = JButton("Tuo kirjaukset")
    private val cancelButton = JButton("Peruuta")
    
    // Account selection for entries without mapped account
    private val accountComboBox = JComboBox<AccountItem>()

    // Import results
    data class ImportResult(
        val success: Boolean,
        val importedCount: Int,
        val errorCount: Int,
        val errors: List<String>
    )

    var importResult: ImportResult? = null
        private set
    
    // Helper class for account combo box
    private data class AccountItem(val id: Int, val number: String, val name: String) {
        override fun toString() = "$number $name"
    }


    init {
        layout = BorderLayout(10, 10)
        preferredSize = Dimension(1000, 700)
        minimumSize = Dimension(800, 500)

        buildUI()
        setupListeners()

        importButton.isEnabled = false

        pack()
        setLocationRelativeTo(parent)
    }

    private fun buildUI() {
        val contentPanel = JPanel(BorderLayout(10, 10))
        contentPanel.border = BorderFactory.createEmptyBorder(10, 10, 10, 10)

        // Top panel: File selection and info
        val topPanel = JPanel(BorderLayout(10, 5))
        
        val filePanel = JPanel(FlowLayout(FlowLayout.LEFT, 5, 0))
        filePanel.add(fileButton)
        filePanel.add(statusLabel)
        topPanel.add(filePanel, BorderLayout.NORTH)
        
        val infoPanel = JPanel(FlowLayout(FlowLayout.LEFT, 15, 0))
        infoPanel.add(encodingLabel)
        infoPanel.add(delimiterLabel)
        topPanel.add(infoPanel, BorderLayout.SOUTH)
        
        contentPanel.add(topPanel, BorderLayout.NORTH)

        // Center: Split pane with preview and mapping
        val splitPane = JSplitPane(JSplitPane.VERTICAL_SPLIT)
        splitPane.dividerLocation = 250
        splitPane.resizeWeight = 0.4

        // Preview panel
        val previewPanel = JPanel(BorderLayout())
        previewPanel.border = BorderFactory.createTitledBorder("Esikatselu CSV-datasta")
        previewTable.fillsViewportHeight = true
        previewTable.autoResizeMode = JTable.AUTO_RESIZE_ALL_COLUMNS
        previewPanel.add(JScrollPane(previewTable), BorderLayout.CENTER)
        splitPane.topComponent = previewPanel

        // Mapping panel
        val mappingPanel = JPanel(BorderLayout())
        mappingPanel.border = BorderFactory.createTitledBorder("Sarakkeiden mappaus")

        mappingTable.fillsViewportHeight = true
        mappingTable.rowHeight = 25
        mappingTable.setDefaultRenderer(Any::class.java, MappingCellRenderer())

        mappingPanel.add(JScrollPane(mappingTable), BorderLayout.CENTER)
        
        // Mapping help text
        val helpLabel = JLabel(
            "<html><small>Valitse miten kukin sarake tuodaan. " +
            "Päivämäärä ja Debet/Kredit/Summa ovat pakollisia.</small></html>"
        )
        helpLabel.border = BorderFactory.createEmptyBorder(5, 5, 0, 5)
        mappingPanel.add(helpLabel, BorderLayout.SOUTH)
        
        splitPane.bottomComponent = mappingPanel

        contentPanel.add(splitPane, BorderLayout.CENTER)

        // Bottom panel: Account selection and buttons
        val bottomPanel = JPanel(BorderLayout(10, 5))
        bottomPanel.border = BorderFactory.createEmptyBorder(10, 0, 0, 0)
        
        // Account selection panel
        val accountPanel = JPanel(FlowLayout(FlowLayout.LEFT, 5, 0))
        accountPanel.add(JLabel("Oletustili (pankkitili):"))
        populateAccountComboBox()
        accountComboBox.preferredSize = Dimension(300, 25)
        accountPanel.add(accountComboBox)
        bottomPanel.add(accountPanel, BorderLayout.WEST)
        
        // Buttons panel
        val buttonPanel = JPanel(FlowLayout(FlowLayout.RIGHT, 10, 0))
        buttonPanel.add(importButton)
        buttonPanel.add(cancelButton)
        bottomPanel.add(buttonPanel, BorderLayout.EAST)
        
        contentPanel.add(bottomPanel, BorderLayout.SOUTH)

        add(contentPanel)
    }
    
    private fun populateAccountComboBox() {
        accountComboBox.removeAllItems()
        
        // Add bank accounts (type 1 = rahoitusomaisuus / financial assets)
        // Look for accounts starting with 19 (pankkitilit)
        for (account in registry.accounts) {
            if (account.number.startsWith("19") || 
                account.number.startsWith("17") ||
                account.name.lowercase().contains("pankki") ||
                account.name.lowercase().contains("kassa")) {
                accountComboBox.addItem(AccountItem(account.id, account.number, account.name))
            }
        }
        
        // If no bank accounts found, add all asset accounts
        if (accountComboBox.itemCount == 0) {
            for (account in registry.accounts) {
                if (account.number.startsWith("1")) {
                    accountComboBox.addItem(AccountItem(account.id, account.number, account.name))
                }
            }
        }
    }

    private fun setupListeners() {
        fileButton.addActionListener { selectFile() }
        importButton.addActionListener { importData() }
        cancelButton.addActionListener { dispose() }
    }

    private fun selectFile() {
        val fileChooser = JFileChooser()
        fileChooser.fileFilter = FileNameExtensionFilter(
            "CSV-tiedostot (*.csv, *.txt)", "csv", "txt"
        )
        fileChooser.dialogTitle = "Valitse CSV-tiedosto"
        
        // Start from user's documents or last directory
        selectedFile?.parentFile?.let { fileChooser.currentDirectory = it }

        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            loadCsvFile(fileChooser.selectedFile)
        }
    }

    private fun loadCsvFile(file: File) {
        try {
            val data = file.readBytes()

            // Validate CSV
            if (!CsvParser.isValidCsv(data)) {
                JOptionPane.showMessageDialog(
                    this,
                    "Tiedosto ei ole validi CSV-tiedosto.\n\n" +
                    "Varmista että tiedosto:\n" +
                    "• Sisältää vähintään otsikkorivin ja yhden datarivin\n" +
                    "• Käyttää pilkkua, puolipistettä tai sarkaimia erottimena\n" +
                    "• Ei ole binääritiedosto (esim. Excel .xlsx)",
                    "Virheellinen tiedosto",
                    JOptionPane.ERROR_MESSAGE
                )
                return
            }

            // Parse CSV
            csvData = CsvParser.parse(data)
            selectedFile = file
            columns = CsvColumnAnalyzer.analyzeColumns(csvData!!)

            // Update UI
            updatePreviewTable()
            updateMappingTable()
            updateInfoLabels()

            statusLabel.text = "Ladattu: ${file.name}"
            importButton.isEnabled = true

        } catch (e: Exception) {
            JOptionPane.showMessageDialog(
                this,
                "Virhe ladattaessa tiedostoa:\n${e.message}",
                "Latausvirhe",
                JOptionPane.ERROR_MESSAGE
            )
        }
    }

    private fun updateInfoLabels() {
        val data = csvData ?: return
        
        val encodingName = when (data.encoding.name()) {
            "UTF-8" -> "UTF-8"
            "ISO-8859-1" -> "ISO-8859-1 (Latin-1)"
            "windows-1252" -> "Windows-1252"
            else -> data.encoding.name()
        }
        encodingLabel.text = "Koodaus: $encodingName"
        
        val delimiterName = when (data.delimiter) {
            ',' -> "pilkku (,)"
            ';' -> "puolipiste (;)"
            '\t' -> "sarkain (Tab)"
            else -> data.delimiter.toString()
        }
        delimiterLabel.text = "| Erotin: $delimiterName | ${data.rowCount} riviä, ${data.columnCount} saraketta"
    }

    private fun updatePreviewTable() {
        val data = csvData ?: return

        previewTable.model = object : AbstractTableModel() {
            override fun getRowCount() = minOf(10, data.dataRows.size)
            override fun getColumnCount() = data.columnCount
            override fun getColumnName(column: Int) = data.headers.getOrNull(column) ?: "Sarake ${column + 1}"
            override fun getValueAt(rowIndex: Int, columnIndex: Int) =
                data.dataRows.getOrNull(rowIndex)?.getOrNull(columnIndex) ?: ""
        }
    }

    private fun updateMappingTable() {
        val columnsCopy = columns.toMutableList()
        
        mappingTable.model = object : AbstractTableModel() {
            override fun getRowCount() = columnsCopy.size
            override fun getColumnCount() = 4

            override fun getColumnName(column: Int) = when (column) {
                0 -> "Sarake"
                1 -> "Tunnistettu tyyppi"
                2 -> "Tuo kenttänä"
                3 -> "Esimerkki"
                else -> ""
            }

            override fun getValueAt(rowIndex: Int, columnIndex: Int): Any {
                val col = columnsCopy[rowIndex]
                return when (columnIndex) {
                    0 -> col.header.ifEmpty { "Sarake ${col.index + 1}" }
                    1 -> getTypeName(col.type)
                    2 -> col.mapping
                    3 -> col.samples.firstOrNull() ?: ""
                    else -> ""
                }
            }

            override fun isCellEditable(rowIndex: Int, columnIndex: Int) = columnIndex == 2

            override fun setValueAt(value: Any?, rowIndex: Int, columnIndex: Int) {
                if (columnIndex == 2 && value is MappingType) {
                    columnsCopy[rowIndex].mapping = value
                    columns = columnsCopy
                    fireTableCellUpdated(rowIndex, columnIndex)
                }
            }

            override fun getColumnClass(columnIndex: Int) = when (columnIndex) {
                2 -> MappingType::class.java
                else -> String::class.java
            }
        }

        // Set combo box editor for mapping column
        val comboBox = JComboBox(MappingType.values())
        comboBox.renderer = object : DefaultListCellRenderer() {
            override fun getListCellRendererComponent(
                list: JList<*>?, value: Any?, index: Int, 
                isSelected: Boolean, cellHasFocus: Boolean
            ): Component {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus)
                if (value is MappingType) {
                    text = value.displayName
                }
                return this
            }
        }
        mappingTable.columnModel.getColumn(2).cellEditor = DefaultCellEditor(comboBox)
        
        // Set column widths
        mappingTable.columnModel.getColumn(0).preferredWidth = 150
        mappingTable.columnModel.getColumn(1).preferredWidth = 120
        mappingTable.columnModel.getColumn(2).preferredWidth = 150
        mappingTable.columnModel.getColumn(3).preferredWidth = 200
    }

    private fun getTypeName(type: ColumnType): String = when (type) {
        ColumnType.EMPTY -> "Tyhjä"
        ColumnType.TEXT -> "Teksti"
        ColumnType.NUMBER -> "Numero"
        ColumnType.MONEY -> "Rahasumma"
        ColumnType.DATE_FI -> "Päivämäärä (pp.kk.vvvv)"
        ColumnType.DATE_ISO -> "Päivämäärä (vvvv-kk-pp)"
        ColumnType.DATE_US -> "Päivämäärä (kk/pp/vvvv)"
        ColumnType.IBAN -> "IBAN"
        ColumnType.REFERENCE -> "Viitenumero"
        ColumnType.ACCOUNT_NUMBER -> "Tilinumero"
    }

    private fun importData() {
        val data = csvData ?: return

        // Find required mappings
        val dateColumn = columns.find { it.mapping == MappingType.DATE }
        val debitColumn = columns.find { it.mapping == MappingType.DEBIT }
        val creditColumn = columns.find { it.mapping == MappingType.CREDIT }
        val amountColumn = columns.find { it.mapping == MappingType.AMOUNT }

        // Validate mappings
        if (dateColumn == null) {
            JOptionPane.showMessageDialog(
                this,
                "Päivämäärä-sarake on pakollinen.\n\n" +
                "Valitse sarake ja aseta sen mappaukseksi 'Päivämäärä'.",
                "Puuttuva mappaus",
                JOptionPane.WARNING_MESSAGE
            )
            return
        }

        if (debitColumn == null && creditColumn == null && amountColumn == null) {
            JOptionPane.showMessageDialog(
                this,
                "Debet-, Kredit- tai Summa-sarake on pakollinen.\n\n" +
                "Valitse vähintään yksi rahasumma-sarake.",
                "Puuttuva mappaus",
                JOptionPane.WARNING_MESSAGE
            )
            return
        }
        
        // Get selected default account
        val selectedAccount = accountComboBox.selectedItem as? AccountItem
        if (selectedAccount == null) {
            JOptionPane.showMessageDialog(
                this,
                "Valitse oletustili (pankkitili) ennen tuontia.",
                "Tili puuttuu",
                JOptionPane.WARNING_MESSAGE
            )
            return
        }
        
        // Confirm import
        val confirmMessage = "Tuodaanko ${data.dataRows.size} riviä?\n\n" +
                "Oletustili: ${selectedAccount.number} ${selectedAccount.name}"
        
        val confirm = JOptionPane.showConfirmDialog(
            this,
            confirmMessage,
            "Vahvista tuonti",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        )
        
        if (confirm != JOptionPane.YES_OPTION) {
            return
        }
        
        // Perform import
        cursor = Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)
        importButton.isEnabled = false
        
        try {
            val importer = CsvImporter(registry)
            val result = importer.import(data, columns, selectedAccount.id)
            
            cursor = Cursor.getDefaultCursor()
            
            if (result.success) {
                val message = "Tuonti onnistui!\n\n" +
                        "Luotuja tositteita: ${result.documentsCreated}\n" +
                        "Luotuja vientejä: ${result.entriesCreated}" +
                        (if (result.warnings.isNotEmpty()) "\n\nVaroituksia: ${result.warnings.size}" else "")
                
                JOptionPane.showMessageDialog(
                    this,
                    message,
                    "Tuonti valmis",
                    JOptionPane.INFORMATION_MESSAGE
                )
                
                importResult = ImportResult(
                    success = true,
                    importedCount = result.entriesCreated,
                    errorCount = result.errors.size,
                    errors = result.errors
                )
                
                dispose()
            } else {
                val errorMessage = "Tuonti epäonnistui:\n\n" +
                        result.errors.take(5).joinToString("\n") +
                        (if (result.errors.size > 5) "\n...ja ${result.errors.size - 5} muuta virhettä" else "")
                
                JOptionPane.showMessageDialog(
                    this,
                    errorMessage,
                    "Tuontivirhe",
                    JOptionPane.ERROR_MESSAGE
                )
                
                importButton.isEnabled = true
            }
        } catch (e: Exception) {
            cursor = Cursor.getDefaultCursor()
            importButton.isEnabled = true
            
            JOptionPane.showMessageDialog(
                this,
                "Odottamaton virhe: ${e.message}",
                "Virhe",
                JOptionPane.ERROR_MESSAGE
            )
        }
    }

    /**
     * Custom cell renderer for mapping table.
     */
    private class MappingCellRenderer : DefaultTableCellRenderer() {
        override fun getTableCellRendererComponent(
            table: JTable,
            value: Any?,
            isSelected: Boolean,
            hasFocus: Boolean,
            row: Int,
            column: Int
        ): Component {
            val c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column)

            if (column == 2 && value is MappingType) {
                text = value.displayName
                
                // Highlight important mappings
                if (!isSelected) {
                    foreground = when (value) {
                        MappingType.DATE -> Color(0, 100, 0) // Dark green
                        MappingType.DEBIT, MappingType.CREDIT, MappingType.AMOUNT -> 
                            Color(0, 0, 139) // Dark blue
                        MappingType.DO_NOT_IMPORT -> Color.GRAY
                        else -> table.foreground
                    }
                }
            }

            return c
        }
    }
}
