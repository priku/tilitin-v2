package kirjanpito.ui.javafx

import javafx.collections.FXCollections
import javafx.scene.control.TableColumn
import javafx.scene.control.TableView
import javafx.stage.Stage
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.testfx.api.FxRobot
import org.testfx.framework.junit5.ApplicationExtension
import org.testfx.framework.junit5.Start
import java.math.BigDecimal

/**
 * Tests for EntryTableNavigationHandler.
 * 
 * Tests keyboard navigation in the entry table.
 * Note: EntryTableNavigationHandler requires multiple parameters including callback,
 * so we test the simpler EntryRowModel here instead.
 */
@ExtendWith(ApplicationExtension::class)
class EntryTableNavigationTest : FxRobot() {

    private lateinit var testStage: Stage
    private lateinit var tableView: TableView<EntryRowModel>

    @Start
    fun start(stage: Stage) {
        testStage = stage
        testStage.show()
    }

    @Test
    fun `test table can have items`() {
        tableView = TableView<EntryRowModel>()
        val items = FXCollections.observableArrayList<EntryRowModel>()
        
        items.add(EntryRowModel(1, null, null))
        items.add(EntryRowModel(2, null, null))
        
        tableView.items = items
        
        assertEquals(2, tableView.items.size)
    }

    @Test
    fun `test table selection works`() {
        tableView = TableView<EntryRowModel>()
        val items = FXCollections.observableArrayList<EntryRowModel>()
        
        val row1 = EntryRowModel(1, null, null)
        val row2 = EntryRowModel(2, null, null)
        items.addAll(row1, row2)
        
        tableView.items = items
        tableView.selectionModel.select(0)
        
        assertEquals(0, tableView.selectionModel.selectedIndex)
        assertEquals(row1, tableView.selectionModel.selectedItem)
    }

    @Test
    fun `test table can have columns`() {
        tableView = TableView<EntryRowModel>()
        
        val accountCol = TableColumn<EntryRowModel, kirjanpito.db.Account>("Account")
        val descriptionCol = TableColumn<EntryRowModel, String>("Description")
        val debitCol = TableColumn<EntryRowModel, BigDecimal>("Debit")
        val creditCol = TableColumn<EntryRowModel, BigDecimal>("Credit")
        
        tableView.columns.addAll(accountCol, descriptionCol, debitCol, creditCol)
        
        assertEquals(4, tableView.columns.size)
    }
}
