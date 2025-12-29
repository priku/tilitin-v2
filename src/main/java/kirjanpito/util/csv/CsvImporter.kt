package kirjanpito.util.csv

import kirjanpito.db.*
import kirjanpito.util.Registry
import java.math.BigDecimal
import java.time.LocalDate
import java.time.ZoneId
import java.util.*

/**
 * Handles importing CSV data into accounting entries.
 * 
 * Creates documents and entries from parsed CSV data based on column mappings.
 */
class CsvImporter(
    private val registry: Registry
) {
    /**
     * Result of an import operation.
     */
    data class ImportResult(
        val success: Boolean,
        val documentsCreated: Int,
        val entriesCreated: Int,
        val errors: List<String>,
        val warnings: List<String>
    )
    
    /**
     * A single parsed row ready for import.
     */
    data class ParsedRow(
        val date: LocalDate,
        val description: String,
        val amount: BigDecimal,
        val isDebit: Boolean,
        val accountNumber: String?,
        val reference: String?,
        val payee: String?,
        val originalRowIndex: Int
    )
    
    /**
     * Import CSV data into accounting entries.
     * 
     * @param csvData Parsed CSV data
     * @param columns Column mapping information
     * @param defaultAccountId Account ID to use when no account mapping exists
     * @return ImportResult with statistics and any errors
     */
    fun import(
        csvData: CsvData,
        columns: List<ColumnInfo>,
        defaultAccountId: Int
    ): ImportResult {
        val errors = mutableListOf<String>()
        val warnings = mutableListOf<String>()
        var documentsCreated = 0
        var entriesCreated = 0
        
        // Find mapped columns
        val dateColumn = columns.find { it.mapping == MappingType.DATE }
        val descriptionColumn = columns.find { it.mapping == MappingType.DESCRIPTION }
        val debitColumn = columns.find { it.mapping == MappingType.DEBIT }
        val creditColumn = columns.find { it.mapping == MappingType.CREDIT }
        val amountColumn = columns.find { it.mapping == MappingType.AMOUNT }
        val accountColumn = columns.find { it.mapping == MappingType.ACCOUNT_NUMBER }
        val referenceColumn = columns.find { it.mapping == MappingType.REFERENCE }
        val payeeColumn = columns.find { it.mapping == MappingType.PAYEE_PAYER }
        
        if (dateColumn == null) {
            return ImportResult(false, 0, 0, listOf("Päivämäärä-sarake puuttuu"), emptyList())
        }
        
        if (debitColumn == null && creditColumn == null && amountColumn == null) {
            return ImportResult(false, 0, 0, listOf("Rahasumma-sarake puuttuu"), emptyList())
        }
        
        // Parse all rows first
        val parsedRows = mutableListOf<ParsedRow>()
        
        for ((rowIndex, row) in csvData.dataRows.withIndex()) {
            try {
                val parsed = parseRow(
                    row = row,
                    rowIndex = rowIndex,
                    dateColumn = dateColumn,
                    descriptionColumn = descriptionColumn,
                    debitColumn = debitColumn,
                    creditColumn = creditColumn,
                    amountColumn = amountColumn,
                    accountColumn = accountColumn,
                    referenceColumn = referenceColumn,
                    payeeColumn = payeeColumn
                )
                
                if (parsed != null) {
                    parsedRows.add(parsed)
                } else {
                    warnings.add("Rivi ${rowIndex + 2}: Ohitettu (tyhjä tai virheellinen data)")
                }
            } catch (e: Exception) {
                errors.add("Rivi ${rowIndex + 2}: ${e.message}")
            }
        }
        
        if (parsedRows.isEmpty()) {
            return ImportResult(false, 0, 0, errors.ifEmpty { listOf("Ei tuotavia rivejä") }, warnings)
        }
        
        // Sort by date
        val sortedRows = parsedRows.sortedBy { it.date }
        
        // Import to database
        val dataSource = registry.dataSource
        val period = registry.period
        var sess: Session? = null
        
        try {
            sess = dataSource.openSession()
            val documentDAO = dataSource.getDocumentDAO(sess)
            val entryDAO = dataSource.getEntryDAO(sess)
            
            // Group rows by date for creating documents
            val rowsByDate = sortedRows.groupBy { it.date }
            
            for ((date, rows) in rowsByDate) {
                // Create document for this date
                val document = documentDAO.create(period.id, 1, Int.MAX_VALUE)
                document.date = localDateToDate(date)
                
                // Save document FIRST to get the ID
                documentDAO.save(document)
                documentsCreated++
                
                // Now document.id is set - create entries
                var rowNumber = 0
                
                for (parsedRow in rows) {
                    // Find account by number if provided
                    val accountId = if (parsedRow.accountNumber != null) {
                        findAccountByNumber(parsedRow.accountNumber) ?: defaultAccountId
                    } else {
                        defaultAccountId
                    }
                    
                    // Build description with payee if available
                    val fullDescription = buildDescription(parsedRow)
                    
                    // Create main entry - set documentId AFTER document is saved
                    val entry = Entry()
                    entry.setDocumentId(document.id)  // Use setter explicitly
                    entry.setAccountId(accountId)
                    entry.setDebit(parsedRow.isDebit)
                    entry.setAmount(parsedRow.amount.abs())
                    entry.setDescription(fullDescription)
                    entry.setRowNumber(rowNumber++)
                    
                    entryDAO.save(entry)
                    entriesCreated++
                    
                    // Create counter entry on default account if different
                    if (accountId != defaultAccountId) {
                        val counterEntry = Entry()
                        counterEntry.setDocumentId(document.id)
                        counterEntry.setAccountId(defaultAccountId)
                        counterEntry.setDebit(!parsedRow.isDebit)
                        counterEntry.setAmount(parsedRow.amount.abs())
                        counterEntry.setDescription(fullDescription)
                        counterEntry.setRowNumber(rowNumber++)
                        
                        entryDAO.save(counterEntry)
                        entriesCreated++
                    }
                }
            }
            
            sess.commit()
            
        } catch (e: DataAccessException) {
            errors.add("Tietokantavirhe: ${e.message}")
            return ImportResult(false, documentsCreated, entriesCreated, errors, warnings)
        } finally {
            sess?.close()
        }
        
        return ImportResult(
            success = errors.isEmpty(),
            documentsCreated = documentsCreated,
            entriesCreated = entriesCreated,
            errors = errors,
            warnings = warnings
        )
    }
    
    private fun parseRow(
        row: List<String>,
        rowIndex: Int,
        dateColumn: ColumnInfo,
        descriptionColumn: ColumnInfo?,
        debitColumn: ColumnInfo?,
        creditColumn: ColumnInfo?,
        amountColumn: ColumnInfo?,
        accountColumn: ColumnInfo?,
        referenceColumn: ColumnInfo?,
        payeeColumn: ColumnInfo?
    ): ParsedRow? {
        // Parse date
        val dateStr = row.getOrNull(dateColumn.index)?.trim() ?: return null
        val date = CsvColumnAnalyzer.parseDate(dateStr, dateColumn.type) ?: return null
        
        // Parse amount
        var amount: BigDecimal? = null
        var isDebit = true
        
        if (amountColumn != null) {
            val amountStr = row.getOrNull(amountColumn.index)?.trim()
            amount = if (!amountStr.isNullOrEmpty()) {
                CsvColumnAnalyzer.parseMoney(amountStr)
            } else null
            
            if (amount != null) {
                // Negative amounts are credits (income)
                isDebit = amount < BigDecimal.ZERO
                amount = amount.abs()
            }
        }
        
        if (amount == null && debitColumn != null) {
            val debitStr = row.getOrNull(debitColumn.index)?.trim()
            if (!debitStr.isNullOrEmpty()) {
                amount = CsvColumnAnalyzer.parseMoney(debitStr)
                isDebit = true
            }
        }
        
        if (amount == null && creditColumn != null) {
            val creditStr = row.getOrNull(creditColumn.index)?.trim()
            if (!creditStr.isNullOrEmpty()) {
                amount = CsvColumnAnalyzer.parseMoney(creditStr)
                isDebit = false
            }
        }
        
        if (amount == null || amount == BigDecimal.ZERO) {
            return null
        }
        
        // Parse description
        val description = descriptionColumn?.let { 
            row.getOrNull(it.index)?.trim() 
        } ?: ""
        
        // Parse optional fields
        val accountNumber = accountColumn?.let { row.getOrNull(it.index)?.trim() }
        val reference = referenceColumn?.let { row.getOrNull(it.index)?.trim() }
        val payee = payeeColumn?.let { row.getOrNull(it.index)?.trim() }
        
        return ParsedRow(
            date = date,
            description = description,
            amount = amount,
            isDebit = isDebit,
            accountNumber = accountNumber,
            reference = reference,
            payee = payee,
            originalRowIndex = rowIndex
        )
    }
    
    private fun findAccountByNumber(accountNumber: String): Int? {
        val number = accountNumber.trim()
        
        for (account in registry.accounts) {
            if (account.number == number) {
                return account.id
            }
        }
        
        return null
    }
    
    private fun buildDescription(row: ParsedRow): String {
        val parts = mutableListOf<String>()
        
        if (row.payee?.isNotBlank() == true) {
            parts.add(row.payee)
        }
        
        if (row.description.isNotBlank()) {
            parts.add(row.description)
        }
        
        if (row.reference?.isNotBlank() == true) {
            parts.add("Viite: ${row.reference}")
        }
        
        return parts.joinToString(" - ").take(200) // Max 200 chars
    }
    
    private fun localDateToDate(localDate: LocalDate): Date {
        return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant())
    }
}
