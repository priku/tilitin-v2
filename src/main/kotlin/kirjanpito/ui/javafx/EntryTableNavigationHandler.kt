package kirjanpito.ui.javafx

import javafx.application.Platform
import javafx.scene.control.TableColumn
import javafx.scene.control.TableView
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import kirjanpito.db.Account
import java.math.BigDecimal

/**
 * Entry Table smart navigation handler.
 * 
 * Portaus EntryTableActions.java:sta JavaFX:ään.
 * Käsittelee Tab/Shift+Tab navigoinnin, debet/kredit-vaihdon (*) ja muut pikanäppäimet.
 * 
 * Navigointilogiikka:
 * - Tab: Tili → Debet/Kredit → Summa → Selite → Seuraava rivi
 * - Shift+Tab: Käänteinen järjestys
 * - Asterisk (*): Vaihda debet/kredit
 * - Enter: Lisää uusi vienti tai luo uusi tosite
 * - Ctrl+Backspace: Poista selitteen pääte
 * 
 * @since 2.1.5
 */
class EntryTableNavigationHandler(
    private val entryTable: TableView<EntryRowModel>,
    private val accountCol: TableColumn<EntryRowModel, Account>,
    private val descriptionCol: TableColumn<EntryRowModel, String>,
    private val debitCol: TableColumn<EntryRowModel, BigDecimal>,
    private val creditCol: TableColumn<EntryRowModel, BigDecimal>,
    private val callback: EntryTableCallback
) {
    
    /**
     * Callback-rajapinta päätoiminnoille.
     */
    interface EntryTableCallback {
        fun addEntry()
        fun createDocument()
        fun focusDateField()
        fun updateTotals()
        fun setStatus(message: String)
        fun getDescriptionHistory(): List<String>
    }
    
    // Column indices (vastaavat Swing-malleja)
    companion object {
        const val COL_ACCOUNT = 0
        const val COL_DEBIT = 1
        const val COL_CREDIT = 2
        const val COL_VAT = 3
        const val COL_DESCRIPTION = 4
    }
    
    /**
     * Asenna näppäimistökäsittelijät.
     */
    fun install() {
        // Add event filter for key handling (captures before cell editors)
        entryTable.addEventFilter(KeyEvent.KEY_PRESSED) { event ->
            handleKeyPress(event)
        }
    }
    
    private fun handleKeyPress(event: KeyEvent) {
        when {
            // Tab - seuraava solu
            event.code == KeyCode.TAB && !event.isShiftDown -> {
                handleNextCell(event)
            }
            // Shift+Tab - edellinen solu
            event.code == KeyCode.TAB && event.isShiftDown -> {
                handlePrevCell(event)
            }
            // Asterisk (*) - vaihda debet/kredit
            event.text == "*" || event.code == KeyCode.MULTIPLY -> {
                handleToggleDebitCredit(event)
            }
            // Enter - lisää vienti tai luo tosite
            event.code == KeyCode.ENTER && !event.isControlDown -> {
                handleEnter(event)
            }
            // Ctrl+Enter - luo uusi tosite
            event.code == KeyCode.ENTER && event.isControlDown -> {
                event.consume()
                callback.createDocument()
            }
            // Ctrl+Backspace - poista selitteen pääte
            event.code == KeyCode.BACK_SPACE && event.isControlDown -> {
                handleRemoveSuffix(event)
            }
            // Up arrow - edellinen rivi, fokus päivämäärään jos ylin rivi
            event.code == KeyCode.UP -> {
                handleUpArrow(event)
            }
        }
    }
    
    /**
     * Käsittele Tab-näppäin: siirry seuraavaan soluun.
     */
    private fun handleNextCell(event: KeyEvent) {
        val editingCell = entryTable.editingCell
        val row = if (editingCell != null) editingCell.row else entryTable.selectionModel.selectedIndex
        
        if (row < 0) {
            callback.addEntry()
            event.consume()
            return
        }
        
        // Lopeta mahdollinen editointi
        commitCurrentEdit()
        
        val colIndex = getCurrentColumnIndex()
        val rowModel = entryTable.items.getOrNull(row)
        
        when (colIndex) {
            // Tilisarakkeesta → debet tai kredit
            COL_ACCOUNT, -1 -> {
                val isDebit = rowModel?.getDebit() != null && rowModel.getDebit().compareTo(BigDecimal.ZERO) != 0
                val targetCol = if (isDebit || rowModel?.getCredit() == null) debitCol else creditCol
                editCell(row, targetCol)
                event.consume()
            }
            // Debetistä → kredit (jos tyhjä) tai selite
            COL_DEBIT -> {
                val debitAmount = rowModel?.getDebit()
                if (debitAmount == null || debitAmount.compareTo(BigDecimal.ZERO) == 0) {
                    // Debet on tyhjä, siirry kreditiin
                    editCell(row, creditCol)
                } else {
                    // Debet ei ole tyhjä, siirry selitteeseen
                    editCell(row, descriptionCol)
                }
                event.consume()
            }
            // Kreditistä → selite
            COL_CREDIT -> {
                editCell(row, descriptionCol)
                event.consume()
            }
            // VAT-sarakkeesta → selite
            COL_VAT -> {
                editCell(row, descriptionCol)
                event.consume()
            }
            // Selitteestä → seuraava rivi tai uusi vienti
            COL_DESCRIPTION -> {
                handleMoveToNextRow(row, rowModel)
                event.consume()
            }
        }
    }
    
    /**
     * Käsittele Shift+Tab: siirry edelliseen soluun.
     */
    private fun handlePrevCell(event: KeyEvent) {
        val editingCell = entryTable.editingCell
        val row = if (editingCell != null) editingCell.row else entryTable.selectionModel.selectedIndex
        
        if (row < 0) {
            callback.focusDateField()
            event.consume()
            return
        }
        
        commitCurrentEdit()
        
        val colIndex = getCurrentColumnIndex()
        val rowModel = entryTable.items.getOrNull(row)
        
        when (colIndex) {
            // Tilisarakkeesta → edellisen rivin selite tai päivämäärä
            COL_ACCOUNT, -1 -> {
                if (row > 0) {
                    editCell(row - 1, descriptionCol)
                } else {
                    callback.focusDateField()
                }
                event.consume()
            }
            // Debetistä/Kreditistä → tili
            COL_DEBIT -> {
                // Tarkista onko kreditissä arvo
                val creditAmount = rowModel?.getCredit()
                if (creditAmount != null && creditAmount.compareTo(BigDecimal.ZERO) != 0) {
                    editCell(row, creditCol)
                } else {
                    editCell(row, accountCol)
                }
                event.consume()
            }
            COL_CREDIT -> {
                val debitAmount = rowModel?.getDebit()
                if (debitAmount != null && debitAmount.compareTo(BigDecimal.ZERO) != 0) {
                    editCell(row, debitCol)
                } else {
                    editCell(row, accountCol)
                }
                event.consume()
            }
            // Selitteestä → kredit tai debet
            COL_DESCRIPTION -> {
                val isDebit = rowModel?.getDebit() != null && rowModel.getDebit().compareTo(BigDecimal.ZERO) != 0
                val targetCol = if (isDebit) debitCol else creditCol
                editCell(row, targetCol)
                event.consume()
            }
        }
    }
    
    /**
     * Käsittele seuraavalle riville siirtyminen.
     */
    private fun handleMoveToNextRow(currentRow: Int, rowModel: EntryRowModel?) {
        val lastRow = entryTable.items.size - 1
        
        if (currentRow < lastRow) {
            // Siirry seuraavan rivin tilisarakkeeseen
            editCell(currentRow + 1, accountCol)
        } else if (currentRow >= 0) {
            // Ollaan viimeisellä rivillä
            val prevDescription = if (currentRow > 0) {
                entryTable.items.getOrNull(currentRow - 1)?.getDescription() ?: ""
            } else ""
            
            val currentDescription = rowModel?.getDescription() ?: ""
            val hasAccount = rowModel?.getAccount() != null
            val amount = rowModel?.getDebit() ?: rowModel?.getCredit() ?: BigDecimal.ZERO
            val hasAmount = amount.compareTo(BigDecimal.ZERO) != 0
            
            // Luo uusi tosite jos:
            // - Vähintään 2 vientiä
            // - Selite sama kuin edellisellä rivillä
            // - Tiliä ei ole valittu TAI summa on nolla
            if (currentRow >= 1 && 
                currentDescription == prevDescription && 
                (!hasAccount || !hasAmount)) {
                callback.createDocument()
            } else {
                callback.addEntry()
            }
        }
    }
    
    /**
     * Käsittele asterisk (*) - vaihda debet/kredit.
     */
    private fun handleToggleDebitCredit(event: KeyEvent) {
        val row = entryTable.selectionModel.selectedIndex
        if (row < 0) return
        
        val colIndex = getCurrentColumnIndex()
        
        // Vaihda vain jos ollaan debet- tai kredit-sarakkeessa
        if (colIndex != COL_DEBIT && colIndex != COL_CREDIT) return
        
        val rowModel = entryTable.items.getOrNull(row) ?: return
        val wasEditing = entryTable.editingCell != null
        
        // Lopeta editointi
        commitCurrentEdit()
        
        // Toggle debet/kredit
        rowModel.toggleDebitCredit()
        
        // Päivitä summat
        callback.updateTotals()
        
        // Päivitä taulukko
        entryTable.refresh()
        
        // Jatka editointia oikeassa sarakkeessa
        if (wasEditing) {
            Platform.runLater {
                val isNowDebit = rowModel.getDebit() != null && rowModel.getDebit().compareTo(BigDecimal.ZERO) != 0
                val targetCol = if (isNowDebit) debitCol else creditCol
                editCell(row, targetCol)
            }
        }
        
        event.consume()
        callback.setStatus("Debet/Kredit vaihdettu")
    }
    
    /**
     * Käsittele Enter-näppäin.
     */
    private fun handleEnter(event: KeyEvent) {
        val editingCell = entryTable.editingCell
        
        // Jos ollaan editoimassa, anna Enter-näppäimen toimia normaalisti
        if (editingCell != null) {
            return
        }
        
        // Muuten lisää uusi vienti
        val row = entryTable.selectionModel.selectedIndex
        if (row >= 0) {
            val colIndex = getCurrentColumnIndex()
            
            // Selitesarakkeessa Enter siirtää seuraavaan riviin
            if (colIndex == COL_DESCRIPTION) {
                handleMoveToNextRow(row, entryTable.items.getOrNull(row))
                event.consume()
            } else {
                // Muissa sarakkeissa aloita editointi käyttäen tunnettuja sarakkeita
                when (colIndex) {
                    COL_ACCOUNT -> editCell(row, accountCol)
                    COL_DEBIT -> editCell(row, debitCol)
                    COL_CREDIT -> editCell(row, creditCol)
                    else -> editCell(row, descriptionCol)
                }
                event.consume()
            }
        } else {
            // Ei riviä valittuna, lisää uusi
            callback.addEntry()
            event.consume()
        }
    }
    
    /**
     * Käsittele ylänuoli - edellinen rivi.
     */
    private fun handleUpArrow(event: KeyEvent) {
        val row = entryTable.selectionModel.selectedIndex
        val isEditing = entryTable.editingCell != null
        
        // Jos editoidaan, anna nuolinäppäimen toimia tekstikentässä
        if (isEditing) return
        
        if (row <= 0) {
            // Ylimmällä rivillä, siirry päivämääräkenttään
            callback.focusDateField()
            event.consume()
        }
    }
    
    /**
     * Käsittele Ctrl+Backspace - poista selitteen pääte.
     */
    private fun handleRemoveSuffix(event: KeyEvent) {
        val editingCell = entryTable.editingCell
        if (editingCell == null) return
        
        val colIndex = getCurrentColumnIndex()
        if (colIndex != COL_DESCRIPTION) return
        
        val rowModel = entryTable.items.getOrNull(editingCell.row) ?: return
        val description = rowModel.getDescription() ?: return
        
        // Poista viimeinen sana tai merkkiryhmä
        val newDescription = removeSuffix(description)
        if (newDescription != description) {
            rowModel.setDescription(newDescription)
            entryTable.refresh()
            
            // Jatka editointia
            Platform.runLater {
                editCell(editingCell.row, descriptionCol)
            }
        }
        
        event.consume()
    }
    
    /**
     * Poista selitteestä pääte (viimeinen sana).
     */
    private fun removeSuffix(text: String): String {
        val trimmed = text.trimEnd()
        if (trimmed.isEmpty()) return ""
        
        // Etsi viimeinen sanaväli
        val lastSpace = trimmed.lastIndexOf(' ')
        return if (lastSpace > 0) {
            trimmed.substring(0, lastSpace)
        } else {
            ""
        }
    }
    
    /**
     * Selvitä nykyisen sarakkeen indeksi.
     */
    private fun getCurrentColumnIndex(): Int {
        val editingCell = entryTable.editingCell
        if (editingCell != null) {
            return getColumnIndex(editingCell.tableColumn)
        }
        
        val selectedCells = entryTable.selectionModel.selectedCells
        if (selectedCells.isNotEmpty()) {
            return getColumnIndex(selectedCells.first().tableColumn)
        }
        
        return -1
    }
    
    /**
     * Muunna TableColumn sarakeindeksiksi.
     */
    private fun getColumnIndex(column: TableColumn<*, *>?): Int {
        return when (column) {
            accountCol -> COL_ACCOUNT
            debitCol -> COL_DEBIT
            creditCol -> COL_CREDIT
            descriptionCol -> COL_DESCRIPTION
            else -> {
                // Tarkista column ID:n perusteella
                when (column?.id) {
                    "accountCol" -> COL_ACCOUNT
                    "debitCol" -> COL_DEBIT
                    "creditCol" -> COL_CREDIT
                    "descriptionCol" -> COL_DESCRIPTION
                    "vatCol" -> COL_VAT
                    else -> -1
                }
            }
        }
    }
    
    /**
     * Lopeta nykyinen editointi.
     */
    private fun commitCurrentEdit() {
        if (entryTable.editingCell != null) {
            // Yritä committaa editointi
            Platform.runLater {
                entryTable.edit(-1, null)
            }
        }
    }
    
    /**
     * Aloita solun editointi.
     */
    private fun <T> editCell(row: Int, column: TableColumn<EntryRowModel, T>) {
        Platform.runLater {
            entryTable.selectionModel.select(row)
            entryTable.scrollTo(row)
            entryTable.selectionModel.clearAndSelect(row, column)
            entryTable.edit(row, column)
        }
    }
}
