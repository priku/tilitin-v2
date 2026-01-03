package kirjanpito.ui.javafx.dialogs

import javafx.geometry.Insets
import javafx.geometry.Orientation
import javafx.geometry.Pos
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.control.cell.PropertyValueFactory
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.Region
import javafx.scene.layout.VBox
import javafx.stage.FileChooser
import javafx.stage.Modality
import javafx.stage.Stage
import javafx.stage.Window
import kirjanpito.db.DataAccessException
import kirjanpito.models.StatisticsModel
import kirjanpito.util.AppSettings
import java.io.File
import java.io.IOException
import java.time.LocalDate
import java.time.ZoneId
import java.util.*
import java.util.logging.Level
import java.util.logging.Logger

/**
 * JavaFX dialog for balance comparison statistics.
 * Shows account balances over weekly or monthly periods.
 * Ports functionality from BalanceComparisonDialog (Swing).
 */
class BalanceComparisonDialogFX(owner: Window?, private val model: StatisticsModel) {

    private val dialog: Stage = Stage()
    private lateinit var table: TableView<Any>
    private lateinit var startDatePicker: DatePicker
    private lateinit var endDatePicker: DatePicker
    private lateinit var weeklyRadio: RadioButton
    private lateinit var monthlyRadio: RadioButton
    private lateinit var saveButton: Button

    companion object {
        private val logger = Logger.getLogger("Kirjanpito")

        /**
         * Static factory method.
         */
        @JvmStatic
        fun create(owner: Window?, model: StatisticsModel): BalanceComparisonDialogFX {
            return BalanceComparisonDialogFX(owner, model)
        }
    }

    init {
        dialog.initModality(Modality.APPLICATION_MODAL)
        dialog.initOwner(owner)
        dialog.title = "Tilien saldojen vertailu"
        dialog.minWidth = 700.0
        dialog.minHeight = 500.0

        createContent()
        initializeDates()
    }

    private fun createContent() {
        val root = VBox().apply {
            style = "-fx-background-color: #ffffff;"
        }

        // Toolbar
        val toolbar = createToolBar()

        // Options panel
        val optionsPanel = createOptionsPanel()

        // Table
        @Suppress("DEPRECATION")
        table = TableView<Any>().apply {
            columnResizePolicy = TableView.CONSTRAINED_RESIZE_POLICY
            selectionModel.selectionMode = SelectionMode.MULTIPLE
        }
        VBox.setVgrow(table, Priority.ALWAYS)

        root.children.addAll(toolbar, optionsPanel, table)

        dialog.scene = Scene(root, 800.0, 550.0)
    }

    private fun createToolBar(): HBox {
        val toolbar = HBox(10.0).apply {
            padding = Insets(10.0)
            style = "-fx-background-color: #f5f5f5; -fx-border-color: #e0e0e0; -fx-border-width: 0 0 1 0;"
        }

        val updateButton = Button("Päivitä").apply {
            style = "-fx-background-color: #2563eb; -fx-text-fill: white; -fx-min-width: 80;"
            setOnAction { updateTable() }
        }

        saveButton = Button("Vie").apply {
            style = "-fx-min-width: 80;"
            isDisable = true
            setOnAction { save() }
        }

        val closeButton = Button("Sulje").apply {
            style = "-fx-min-width: 80;"
            setOnAction { dialog.close() }
        }

        val spacer = Region()
        HBox.setHgrow(spacer, Priority.ALWAYS)

        toolbar.children.addAll(updateButton, saveButton, spacer, closeButton)
        return toolbar
    }

    private fun createOptionsPanel(): HBox {
        val panel = HBox(20.0).apply {
            padding = Insets(15.0)
            alignment = Pos.CENTER_LEFT
            style = "-fx-background-color: #fafafa; -fx-border-color: #e0e0e0; -fx-border-width: 0 0 1 0;"
        }

        // Date range section
        val dateSection = VBox(8.0)

        val startBox = HBox(10.0).apply {
            alignment = Pos.CENTER_LEFT
        }
        val startLabel = Label("Alkaa:").apply {
            minWidth = 60.0
        }
        startDatePicker = DatePicker().apply {
            promptText = "pp.kk.vvvv"
            prefWidth = 150.0
        }
        startBox.children.addAll(startLabel, startDatePicker)

        val endBox = HBox(10.0).apply {
            alignment = Pos.CENTER_LEFT
        }
        val endLabel = Label("Päättyy:").apply {
            minWidth = 60.0
        }
        endDatePicker = DatePicker().apply {
            promptText = "pp.kk.vvvv"
            prefWidth = 150.0
        }
        endBox.children.addAll(endLabel, endDatePicker)

        dateSection.children.addAll(startBox, endBox)

        // Separator
        val separator = Separator().apply {
            orientation = Orientation.VERTICAL
        }

        // Period type section
        val periodSection = VBox(8.0).apply {
            padding = Insets(0.0, 0.0, 0.0, 10.0)
        }

        weeklyRadio = RadioButton("Viikoittain").apply {
            isSelected = true
        }
        monthlyRadio = RadioButton("Kuukausittain")

        val periodGroup = ToggleGroup()
        weeklyRadio.toggleGroup = periodGroup
        monthlyRadio.toggleGroup = periodGroup

        periodSection.children.addAll(weeklyRadio, monthlyRadio)

        panel.children.addAll(dateSection, separator, periodSection)
        return panel
    }

    private fun initializeDates() {
        val cal = Calendar.getInstance()
        val endDate = cal.time
        endDatePicker.value = endDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()

        cal.isLenient = true
        cal.add(Calendar.MONTH, -1)
        cal.set(Calendar.DAY_OF_MONTH, 1)
        val startDate = cal.time
        startDatePicker.value = startDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
    }

    private fun updateTable() {
        // Validate dates
        if (startDatePicker.value == null || endDatePicker.value == null) {
            showError("Virhe", "Valitse sekä alku- että loppupäivämäärä")
            return
        }

        val start = startDatePicker.value
        val end = endDatePicker.value

        if (start.isAfter(end)) {
            showError("Virhe", "Alkupäivämäärä ei voi olla loppupäivämäärän jälkeen")
            return
        }

        // Convert to Date
        val startDate = Date.from(start.atStartOfDay(ZoneId.systemDefault()).toInstant())
        val endDate = Date.from(end.atStartOfDay(ZoneId.systemDefault()).toInstant())

        try {
            model.startDate = startDate
            model.endDate = endDate

            if (monthlyRadio.isSelected) {
                model.calculateMonthlyStatistics()
            } else {
                model.calculateWeeklyStatistics()
            }

            // Update table structure
            updateTableColumns()
            saveButton.isDisable = false

        } catch (e: DataAccessException) {
            val message = "Tositetietojen hakeminen epäonnistui"
            logger.log(Level.SEVERE, message, e)
            showError("Virhe", "$message: ${e.message}")
        }
    }

    private fun updateTableColumns() {
        table.columns.clear()
        table.items.clear()

        // Note: This is a simplified version. The full implementation would need
        // to integrate with StatisticsModel properly and create dynamic columns
        // based on the period count. For now, we show a placeholder implementation.

        val accountColumn = TableColumn<Any, String>("Tili").apply {
            prefWidth = 200.0
            cellValueFactory = PropertyValueFactory("account")
        }
        table.columns.add(accountColumn)

        // Add dynamic period columns based on model.getPeriodCount()
        val periodCount = model.periodCount
        for (i in 0 until periodCount) {
            val periodColumn = TableColumn<Any, String>("Jakso ${i + 1}").apply {
                cellValueFactory = PropertyValueFactory("period$i")
            }
            table.columns.add(periodColumn)
        }

        // Populate table data from model
        // This would need StatisticsTableModel integration
        table.refresh()
    }

    private fun save() {
        val settings = AppSettings.getInstance()
        val path = settings.getString("csv-directory", ".")

        val fileChooser = FileChooser().apply {
            title = "Tallenna CSV-tiedostona"
            initialDirectory = File(path)
            extensionFilters.add(
                FileChooser.ExtensionFilter("CSV-tiedostot", "*.csv")
            )
        }

        val file = fileChooser.showSaveDialog(dialog)
        if (file != null) {
            settings.set("csv-directory", file.parentFile.absolutePath)

            try {
                model.save(file)
                showInfo("Tallennettu", "Tiedot tallennettu onnistuneesti")
            } catch (e: IOException) {
                logger.log(Level.SEVERE, "Tietojen tallentaminen epäonnistui", e)
                showError("Virhe", "Tietojen tallentaminen epäonnistui: ${e.message}")
            }
        }
    }

    private fun showError(title: String, message: String) {
        Alert(Alert.AlertType.ERROR).apply {
            initOwner(dialog)
            this.title = title
            headerText = null
            contentText = message
        }.showAndWait()
    }

    private fun showInfo(title: String, message: String) {
        Alert(Alert.AlertType.INFORMATION).apply {
            initOwner(dialog)
            this.title = title
            headerText = null
            contentText = message
        }.showAndWait()
    }

    // Public API

    fun show() {
        dialog.show()
    }

    fun showAndWait() {
        dialog.showAndWait()
    }
}
