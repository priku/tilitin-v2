package kirjanpito.ui.javafx.dialogs

import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.Region
import javafx.scene.layout.VBox
import javafx.stage.Modality
import javafx.stage.Stage
import javafx.stage.Window
import kirjanpito.db.Period
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date

/**
 * JavaFX dialog for account summary report options.
 * Ports functionality from AccountSummaryOptionsDialog (Swing).
 */
class AccountSummaryOptionsDialogFX(owner: Window?) {

    private val dialog: Stage = Stage()
    private lateinit var startDatePicker: DatePicker
    private lateinit var endDatePicker: DatePicker
    private lateinit var periodRadio: RadioButton
    private lateinit var customRadio: RadioButton
    private lateinit var accountsComboBox: ComboBox<String>
    private lateinit var previousPeriodCheckBox: CheckBox

    var period: Period? = null
        set(value) {
            field = value
            if (value != null) {
                val start = value.startDate.toInstant()
                    .atZone(ZoneId.systemDefault()).toLocalDate()
                val end = value.endDate.toInstant()
                    .atZone(ZoneId.systemDefault()).toLocalDate()

                startDatePicker.value = start
                endDatePicker.value = end
            }
        }

    var startDate: Date? = null
        private set
    var endDate: Date? = null
        private set

    private var okPressed = false

    init {
        dialog.initModality(Modality.APPLICATION_MODAL)
        dialog.initOwner(owner)
        dialog.title = "Tilien saldot"
        dialog.minWidth = 500.0
        dialog.minHeight = 350.0

        createContent()
    }

    private fun createContent() {
        val root = VBox(16.0).apply {
            padding = Insets(16.0)
            style = "-fx-background-color: #ffffff;"
        }

        // Date selection section
        val dateLabel = Label("Aikaväli").apply {
            style = "-fx-font-weight: bold;"
        }

        val dateSection = VBox(10.0).apply {
            padding = Insets(10.0)
            style = "-fx-border-color: #e0e0e0; -fx-border-radius: 4; -fx-background-color: #f9fafb; -fx-background-radius: 4;"
        }

        // Period radio
        periodRadio = RadioButton("Koko tilikausi").apply {
            isSelected = true
        }

        // Custom date range radio
        customRadio = RadioButton("Aikaväli:")

        val dateGroup = ToggleGroup()
        periodRadio.toggleGroup = dateGroup
        customRadio.toggleGroup = dateGroup

        // Date pickers
        val datePickerBox = HBox(10.0).apply {
            alignment = Pos.CENTER_LEFT
            padding = Insets(0.0, 0.0, 0.0, 30.0)
        }

        startDatePicker = DatePicker().apply {
            promptText = "Alkupvm"
            prefWidth = 150.0
            isDisable = true
        }

        val toLabel = Label("—")

        endDatePicker = DatePicker().apply {
            promptText = "Loppupvm"
            prefWidth = 150.0
            isDisable = true
        }

        datePickerBox.children.addAll(startDatePicker, toLabel, endDatePicker)
        dateSection.children.addAll(periodRadio, customRadio, datePickerBox)

        // Options section
        val optionsLabel = Label("Asetukset").apply {
            style = "-fx-font-weight: bold;"
        }

        val optionsSection = VBox(10.0).apply {
            padding = Insets(10.0)
            style = "-fx-border-color: #e0e0e0; -fx-border-radius: 4; -fx-background-color: #f9fafb; -fx-background-radius: 4;"
        }

        // Previous period checkbox
        previousPeriodCheckBox = CheckBox("Edellisen tilikauden vertailu")

        // Accounts combo
        val accountsBox = HBox(10.0).apply {
            alignment = Pos.CENTER_LEFT
        }

        val accountsLabel = Label("Tilit:")
        accountsComboBox = ComboBox<String>().apply {
            items.addAll(
                "Kaikki tilit",
                "Taseen tilit",
                "Tuloslaskelman tilit"
            )
            selectionModel.select(0)
            prefWidth = 200.0
        }

        accountsBox.children.addAll(accountsLabel, accountsComboBox)
        optionsSection.children.addAll(previousPeriodCheckBox, accountsBox)

        // Buttons
        val buttonBox = HBox(10.0).apply {
            alignment = Pos.CENTER_RIGHT
            padding = Insets(10.0, 0.0, 0.0, 0.0)
        }

        val okButton = Button("OK").apply {
            isDefaultButton = true
            style = "-fx-background-color: #2563eb; -fx-text-fill: white; -fx-min-width: 80;"
            setOnAction { handleOK() }
        }

        val cancelButton = Button("Peruuta").apply {
            isCancelButton = true
            style = "-fx-min-width: 80;"
            setOnAction { dialog.close() }
        }

        buttonBox.children.addAll(cancelButton, okButton)

        // Spacer
        val spacer = Region()

        // Add all sections to root
        root.children.addAll(
            dateLabel, dateSection,
            optionsLabel, optionsSection,
            spacer,
            buttonBox
        )

        VBox.setVgrow(spacer, Priority.ALWAYS)

        // Event handlers
        setupEventHandlers()

        dialog.scene = Scene(root, 550.0, 400.0)
    }

    private fun setupEventHandlers() {
        // Enable/disable date pickers based on radio selection
        periodRadio.selectedProperty().addListener { _, _, newVal ->
            if (newVal) {
                startDatePicker.isDisable = true
                endDatePicker.isDisable = true
            }
        }

        customRadio.selectedProperty().addListener { _, _, newVal ->
            if (newVal) {
                startDatePicker.isDisable = false
                endDatePicker.isDisable = false
            }
        }
    }

    private fun handleOK() {
        // Validate dates if custom range selected
        if (customRadio.isSelected) {
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

            // Convert LocalDate to Date
            startDate = Date.from(start.atStartOfDay(ZoneId.systemDefault()).toInstant())
            endDate = Date.from(end.atStartOfDay(ZoneId.systemDefault()).toInstant())
        } else if (period != null) {
            // Use period dates
            startDate = period?.startDate
            endDate = period?.endDate
        } else {
            showError("Virhe", "Tilikautta ei ole asetettu")
            return
        }

        okPressed = true
        dialog.close()
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

    var isPreviousPeriodVisible: Boolean
        get() = previousPeriodCheckBox.isSelected
        set(visible) { previousPeriodCheckBox.isSelected = visible }

    var printedAccounts: Int
        get() = accountsComboBox.selectionModel.selectedIndex
        set(index) { accountsComboBox.selectionModel.select(index) }

    fun showAndWait(): Boolean {
        dialog.showAndWait()
        return okPressed
    }

    companion object {
        /**
         * Static factory method matching Swing pattern.
         */
        @JvmStatic
        fun create(owner: Window?, period: Period?): AccountSummaryOptionsDialogFX {
            return AccountSummaryOptionsDialogFX(owner).apply {
                this.period = period
            }
        }
    }
}
