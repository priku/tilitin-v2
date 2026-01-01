package kirjanpito.ui.javafx.dialogs

import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.control.cell.TextFieldTableCell
import javafx.scene.layout.GridPane
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import javafx.stage.Modality
import javafx.stage.Stage
import javafx.stage.Window
import kirjanpito.db.DataAccessException
import kirjanpito.db.Period
import kirjanpito.db.Settings
import kirjanpito.models.PropertiesModel
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date

/**
 * JavaFX-dialogi perustietojen (yritys, tilikaudet) muokkaamiseen.
 * Migrated from Java to Kotlin for better code conciseness and null-safety.
 */
class PropertiesDialogFX(owner: Window?, private val model: PropertiesModel) {

    private val dialog: Stage
    
    private lateinit var nameTextField: TextField
    private lateinit var businessIdTextField: TextField
    private lateinit var periodTable: TableView<PeriodRow>
    private lateinit var periodRows: ObservableList<PeriodRow>
    
    private var saved = false
    
    companion object {
        private val DATE_FORMAT = SimpleDateFormat("dd.MM.yyyy")
    }

    init {
        dialog = Stage().apply {
            initModality(Modality.APPLICATION_MODAL)
            owner?.let { initOwner(it) }
            title = "Perustiedot"
            minWidth = 500.0
            minHeight = 450.0
            width = 550.0
            height = 500.0
        }

        createContent()
        loadData()
    }

    private fun createContent() {
        val root = VBox(16.0).apply {
            padding = Insets(16.0)
            style = "-fx-background-color: #ffffff;"
        }

        // === Yrityksen tiedot ===
        val companyLabel = Label("Yrityksen tiedot").apply {
            style = "-fx-font-weight: bold; -fx-font-size: 14px;"
        }

        val companyGrid = GridPane().apply {
            hgap = 10.0
            vgap = 10.0
            padding = Insets(10.0)
            style = "-fx-border-color: #e0e0e0; -fx-border-radius: 4; -fx-background-color: #f9fafb; -fx-background-radius: 4;"
        }

        val nameLabel = Label("Nimi:")
        nameTextField = TextField().apply {
            prefWidth = 300.0
            promptText = "Yrityksen nimi"
        }

        val businessIdLabel = Label("Y-tunnus:")
        businessIdTextField = TextField().apply {
            prefWidth = 150.0
            promptText = "1234567-8"
        }

        companyGrid.add(nameLabel, 0, 0)
        companyGrid.add(nameTextField, 1, 0)
        companyGrid.add(businessIdLabel, 0, 1)
        companyGrid.add(businessIdTextField, 1, 1)

        GridPane.setHgrow(nameTextField, Priority.ALWAYS)

        // === Tilikaudet ===
        val periodsLabel = Label("Tilikaudet").apply {
            style = "-fx-font-weight: bold; -fx-font-size: 14px;"
        }

        val periodsBox = VBox(10.0).apply {
            padding = Insets(10.0)
            style = "-fx-border-color: #e0e0e0; -fx-border-radius: 4; -fx-background-color: #f9fafb; -fx-background-radius: 4;"
        }
        VBox.setVgrow(periodsBox, Priority.ALWAYS)

        // Taulukko tilikausille
        periodRows = FXCollections.observableArrayList()
        periodTable = TableView(periodRows).apply {
            isEditable = true
            prefHeight = 200.0
        }
        VBox.setVgrow(periodTable, Priority.ALWAYS)

        // Sarakkeet
        val numCol = TableColumn<PeriodRow, Int>("#").apply {
            cellValueFactory = javafx.util.Callback { SimpleIntegerProperty(it.value.number).asObject() }
            prefWidth = 40.0
            isEditable = false
        }

        val startCol = TableColumn<PeriodRow, String>("Alkaa").apply {
            cellValueFactory = javafx.util.Callback { it.value.startDateStringProperty }
            cellFactory = TextFieldTableCell.forTableColumn()
            setOnEditCommit { event ->
                event.rowValue.startDateString = event.newValue
            }
            prefWidth = 150.0
            isEditable = true
        }

        val endCol = TableColumn<PeriodRow, String>("Päättyy").apply {
            cellValueFactory = javafx.util.Callback { it.value.endDateStringProperty }
            cellFactory = TextFieldTableCell.forTableColumn()
            setOnEditCommit { event ->
                event.rowValue.endDateString = event.newValue
            }
            prefWidth = 150.0
            isEditable = true
        }

        val statusCol = TableColumn<PeriodRow, String>("Tila").apply {
            cellValueFactory = javafx.util.Callback { SimpleStringProperty(it.value.status) }
            prefWidth = 100.0
            isEditable = false
        }

        periodTable.columns.addAll(numCol, startCol, endCol, statusCol)

        // Painikkeet tilikausille
        val periodButtons = HBox(10.0).apply {
            alignment = Pos.CENTER_LEFT
        }

        val newPeriodBtn = Button("Uusi tilikausi").apply {
            setOnAction { createPeriod() }
        }

        val deletePeriodBtn = Button("Poista tilikausi").apply {
            setOnAction { deletePeriod() }
        }

        periodButtons.children.addAll(newPeriodBtn, deletePeriodBtn)

        periodsBox.children.addAll(periodTable, periodButtons)

        // === Painikkeet ===
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
            setOnAction { save() }
        }

        buttonBox.children.addAll(cancelBtn, okBtn)

        // Kokoa layout
        root.children.addAll(
            companyLabel, companyGrid,
            periodsLabel, periodsBox,
            buttonBox
        )

        val scene = Scene(root)
        dialog.scene = scene
    }

    private fun loadData() {
        // Lataa yrityksen tiedot
        val settings = model.settings
        nameTextField.text = settings.name ?: ""
        businessIdTextField.text = settings.businessId ?: ""

        // Lataa tilikaudet
        periodRows.clear()
        for (i in 0 until model.periodCount) {
            val period = model.getPeriod(i)
            val isCurrent = (i == model.currentPeriodIndex)
            periodRows.add(PeriodRow(i + 1, period, isCurrent))
        }

        // Valitse nykyinen tilikausi
        val currentIndex = model.currentPeriodIndex
        if (currentIndex >= 0 && currentIndex < periodRows.size) {
            periodTable.selectionModel.select(currentIndex)
        }
    }

    private fun createPeriod() {
        model.createPeriod()

        // Päivitä taulukko
        val newIndex = model.periodCount - 1
        val newPeriod = model.getPeriod(newIndex)
        periodRows.add(PeriodRow(newIndex + 1, newPeriod, false))

        // Valitse uusi tilikausi
        periodTable.selectionModel.select(newIndex)
        periodTable.scrollTo(newIndex)
    }

    private fun deletePeriod() {
        val selectedIndex = periodTable.selectionModel.selectedIndex

        if (selectedIndex < 0) {
            showError("Valitse ensin poistettava tilikausi")
            return
        }

        if (model.periodCount <= 1) {
            showError("Tilikautta ei voi poistaa, jos tietokannassa on vain yksi tilikausi.")
            return
        }

        val period = model.getPeriod(selectedIndex)
        val periodInfo = "${formatDate(period.startDate)} – ${formatDate(period.endDate)}"

        val confirm = Alert(Alert.AlertType.CONFIRMATION).apply {
            title = "Poista tilikausi"
            headerText = "Haluatko varmasti poistaa tilikauden?"
            contentText = "Tilikausi: $periodInfo\n\nTietoja ei voi palauttaa poistamisen jälkeen."
        }

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                model.deletePeriod(selectedIndex)
                periodRows.removeAt(selectedIndex)

                // Päivitä rivinumerot
                periodRows.forEachIndexed { index, row ->
                    row.number = index + 1
                }
                periodTable.refresh()

            } catch (e: DataAccessException) {
                showError("Tilikauden poistaminen epäonnistui: ${e.message}")
            }
        }
    }

    private fun save() {
        // Päivitä yrityksen tiedot
        val settings = model.settings
        settings.setName(nameTextField.text.trim())
        settings.setBusinessId(businessIdTextField.text.trim())

        // Päivitä tilikaudet malliin
        periodRows.forEachIndexed { i, row ->
            val period = model.getPeriod(i)

            // Päivitä päivämäärät
            val startDate = row.startDate
            val endDate = row.endDate

            if (startDate == null) {
                showError("Syötä tilikauden ${i + 1} alkamispäivämäärä.")
                return
            }

            if (endDate == null) {
                showError("Syötä tilikauden ${i + 1} päättymispäivämäärä.")
                return
            }

            if (endDate.before(startDate)) {
                showError("Tilikauden ${i + 1} päättymispäivämäärän on oltava alkamispäivämäärän jälkeen.")
                return
            }

            period.setStartDate(startDate)
            period.setEndDate(endDate)
            model.updatePeriod(i)
        }

        // Päivitä nykyinen tilikausi valinnan mukaan
        val selectedIndex = periodTable.selectionModel.selectedIndex
        if (selectedIndex >= 0) {
            model.setCurrentPeriodIndex(selectedIndex)
        }

        // Tallenna
        try {
            model.save()
            saved = true
            dialog.close()
        } catch (e: DataAccessException) {
            showError("Tallentaminen epäonnistui: ${e.message}")
        }
    }

    private fun showError(message: String) {
        Alert(Alert.AlertType.ERROR).apply {
            title = "Virhe"
            headerText = null
            contentText = message
        }.showAndWait()
    }

    private fun formatDate(date: Date?): String {
        if (date == null) return ""
        return DATE_FORMAT.format(date)
    }

    fun isSaved(): Boolean = saved

    fun show() {
        dialog.showAndWait()
    }

    // === Sisäinen luokka tilikausirivien hallintaan ===

    class PeriodRow(
        var number: Int,
        val period: Period,
        private val current: Boolean
    ) {
        var startDate: Date? = period.startDate
        var endDate: Date? = period.endDate

        private val _startDateString = SimpleStringProperty()
        val startDateStringProperty: SimpleStringProperty
            get() = _startDateString

        var startDateString: String
            get() = startDate?.let { DATE_FORMAT.format(it) } ?: ""
            set(value) {
                try {
                    startDate = DATE_FORMAT.parse(value)
                    _startDateString.value = value
                } catch (e: ParseException) {
                    // Keep old value
                }
            }

        private val _endDateString = SimpleStringProperty()
        val endDateStringProperty: SimpleStringProperty
            get() = _endDateString

        var endDateString: String
            get() = endDate?.let { DATE_FORMAT.format(it) } ?: ""
            set(value) {
                try {
                    endDate = DATE_FORMAT.parse(value)
                    _endDateString.value = value
                } catch (e: ParseException) {
                    // Keep old value
                }
            }

        init {
            _startDateString.value = startDateString
            _endDateString.value = endDateString
        }

        val status: String
            get() = when {
                current -> "Nykyinen"
                period.isLocked -> "Lukittu"
                else -> ""
            }
    }
}
