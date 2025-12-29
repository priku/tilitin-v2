package kirjanpito.util.csv

/**
 * Procountor CSV format preset.
 *
 * Procountor exports use semicolon (;) as delimiter and specific column structure.
 * This preset helps automatically detect and map Procountor CSV files.
 *
 * Standard Procountor columns:
 * - Column 0: Tyyppi (Type: Myynti/Osto)
 * - Column 1: Numero (Number)
 * - Column 2: Päivämäärä (Date: yyyy-MM-dd)
 * - Column 3: Arvopäivä (Value date)
 * - Column 4: Maksuväline (Payment method)
 * - Column 5: Summa (Amount with comma as decimal separator)
 * - Column 6: Vastaanottaja (Recipient)
 * - Column 7: Nimi (Name - validation column, must not be empty)
 * - Column 8: Viesti (Message/Description)
 * - Column 9: Viite (Reference)
 * - Column 10: Arkistointitunnus (Archive ID)
 * - Column 11: Tila (Status)
 * - Column 12: ALV (VAT)
 * - Column 13: Tilinumero (Account number)
 * - Column 14: IBAN
 */
object ProcountorCsvPreset {

    /**
     * Check if CSV data matches Procountor format.
     *
     * Heuristics:
     * - Uses semicolon delimiter
     * - Has headers: "Tyyppi", "Päivämäärä", "Summa", "Viesti", "Tilinumero"
     * - Has at least 13 columns
     */
    fun isProcountorFormat(csvData: CsvData): Boolean {
        // Must use semicolon
        if (csvData.delimiter != ';') return false

        // Must have at least 13 columns
        if (csvData.headers.size < 13) return false

        // Check for Procountor-specific headers
        val headers = csvData.headers.map { it.lowercase() }
        val procountorHeaders = listOf("tyyppi", "päivämäärä", "summa", "viesti", "tilinumero")

        return procountorHeaders.count { searchTerm ->
            headers.any { it.contains(searchTerm) }
        } >= 4 // At least 4 out of 5 key headers must match
    }

    /**
     * Apply Procountor-specific column mappings.
     *
     * This overrides auto-detection for known Procountor columns.
     */
    fun applyProcountorMappings(columns: List<ColumnInfo>): List<ColumnInfo> {
        val updatedColumns = columns.toMutableList()

        for (i in updatedColumns.indices) {
            val header = updatedColumns[i].header.lowercase()

            // Map Procountor columns by position and header
            updatedColumns[i] = when {
                // Column 2: Päivämäärä (Date)
                i == 2 || header.contains("päivämäärä") || header.contains("date") -> {
                    updatedColumns[i].copy(mapping = MappingType.DATE)
                }
                // Column 5: Summa (Amount)
                i == 5 || header.contains("summa") || header.contains("amount") -> {
                    updatedColumns[i].copy(mapping = MappingType.AMOUNT)
                }
                // Column 8: Viesti (Description)
                i == 8 || header.contains("viesti") || header.contains("message") || header.contains("selite") -> {
                    updatedColumns[i].copy(mapping = MappingType.DESCRIPTION)
                }
                // Column 13: Tilinumero (Account number)
                i == 13 || (header.contains("tilinumero") && !header.contains("iban")) -> {
                    updatedColumns[i].copy(mapping = MappingType.ACCOUNT_NUMBER)
                }
                // Column 9: Viite (Reference)
                i == 9 || header.contains("viite") || header.contains("reference") -> {
                    updatedColumns[i].copy(mapping = MappingType.REFERENCE)
                }
                // Column 14: IBAN
                i == 14 || header == "iban" -> {
                    updatedColumns[i].copy(mapping = MappingType.IBAN)
                }
                // Column 6: Vastaanottaja/Maksaja (Payee/Payer)
                i == 6 || header.contains("vastaanottaja") || header.contains("recipient") -> {
                    updatedColumns[i].copy(mapping = MappingType.PAYEE_PAYER)
                }
                // Column 12: ALV (VAT)
                i == 12 || header == "alv" || header == "vat" -> {
                    updatedColumns[i].copy(mapping = MappingType.VAT_PERCENT)
                }
                else -> updatedColumns[i]
            }
        }

        return updatedColumns
    }

    /**
     * Get recommended default account ID for Procountor imports.
     * This would typically be a bank account (e.g., 1910 - Pankkitili).
     */
    fun getDefaultBankAccountNumber(): String = "1910"

    /**
     * Validate Procountor CSV row.
     *
     * @return true if row is valid for import
     */
    fun isValidProcountorRow(row: List<String>): Boolean {
        // Must have at least 14 columns
        if (row.size < 14) return false

        // Column 7 (Nimi) must not be empty - this is the validation column
        if (row.getOrNull(7)?.isBlank() != false) return false

        return true
    }

    /**
     * Parse Procountor amount (handles comma as decimal separator).
     *
     * @param amountStr Amount string (e.g., "1500,00" or "-850,50")
     * @return BigDecimal amount or null if parsing fails
     */
    fun parseProcountorAmount(amountStr: String): java.math.BigDecimal? {
        return try {
            // Remove any whitespace and currency symbols
            val cleaned = amountStr.trim()
                .replace("€", "")
                .replace("EUR", "")
                .replace(" ", "")

            // Replace comma with dot for BigDecimal parsing
            java.math.BigDecimal(cleaned.replace(',', '.'))
        } catch (e: Exception) {
            null
        }
    }
}
