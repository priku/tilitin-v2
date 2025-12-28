package kirjanpito.util.csv

import java.math.BigDecimal
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

/**
 * Detected column data type.
 */
enum class ColumnType {
    EMPTY,          // Empty column
    TEXT,           // Plain text
    NUMBER,         // Integer number
    MONEY,          // Money amount (with decimals)
    DATE_FI,        // Finnish date: dd.MM.yyyy or d.M.yyyy
    DATE_ISO,       // ISO date: yyyy-MM-dd
    DATE_US,        // US date: MM/dd/yyyy
    IBAN,           // IBAN bank account
    REFERENCE,      // Finnish reference number (viitenumero)
    ACCOUNT_NUMBER  // Account number (4-8 digits)
}

/**
 * How to map column to import field.
 */
enum class MappingType(val displayName: String) {
    DO_NOT_IMPORT("Älä tuo"),
    DATE("Päivämäärä"),
    DESCRIPTION("Selite"),
    DEBIT("Debet (€)"),
    CREDIT("Kredit (€)"),
    AMOUNT("Summa (€)"),
    ACCOUNT_NUMBER("Tilinumero"),
    ACCOUNT_NAME("Tilin nimi"),
    DOCUMENT_NUMBER("Tositenumero"),
    IBAN("IBAN"),
    REFERENCE("Viitenumero"),
    PAYEE_PAYER("Saaja/Maksaja"),
    VAT_PERCENT("ALV-%")
}

/**
 * Information about a CSV column.
 */
data class ColumnInfo(
    val index: Int,
    val header: String,
    val type: ColumnType,
    val samples: List<String>,
    var mapping: MappingType = MappingType.DO_NOT_IMPORT
)

/**
 * Analyzes CSV columns to detect types and suggest mappings.
 */
object CsvColumnAnalyzer {

    /**
     * Analyze all columns in CSV data.
     */
    fun analyzeColumns(csvData: CsvData): List<ColumnInfo> {
        val columns = mutableListOf<ColumnInfo>()

        for (i in csvData.headers.indices) {
            val header = csvData.headers[i]
            val samples = csvData.dataRows.take(10).mapNotNull { row ->
                row.getOrNull(i)?.takeIf { it.isNotBlank() }
            }

            val type = detectColumnType(samples)
            val mapping = guessMapping(header, type)

            columns.add(
                ColumnInfo(
                    index = i,
                    header = header,
                    type = type,
                    samples = samples,
                    mapping = mapping
                )
            )
        }

        return columns
    }

    /**
     * Detect column type from sample values.
     */
    fun detectColumnType(samples: List<String>): ColumnType {
        if (samples.isEmpty()) {
            return ColumnType.EMPTY
        }

        // Try IBAN (starts with country code, 15-34 chars)
        val ibanPattern = "^[A-Z]{2}[0-9]{2}[A-Z0-9]{11,30}$".toRegex()
        if (samples.all { it.replace(" ", "").matches(ibanPattern) }) {
            return ColumnType.IBAN
        }

        // Try Finnish reference number (7-20 digits, checksum valid)
        if (samples.all { isValidReference(it) }) {
            return ColumnType.REFERENCE
        }

        // Try Finnish date (d.M.yyyy)
        val fiDatePattern = DateTimeFormatter.ofPattern("d.M.yyyy")
        if (samples.all { tryParseDate(it, fiDatePattern) != null }) {
            return ColumnType.DATE_FI
        }

        // Try ISO date (yyyy-MM-dd)
        val isoDatePattern = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        if (samples.all { tryParseDate(it, isoDatePattern) != null }) {
            return ColumnType.DATE_ISO
        }

        // Try US date (M/d/yyyy)
        val usDatePattern = DateTimeFormatter.ofPattern("M/d/yyyy")
        if (samples.all { tryParseDate(it, usDatePattern) != null }) {
            return ColumnType.DATE_US
        }

        // Try money (with comma or dot as decimal separator)
        if (samples.all { parseMoney(it) != null }) {
            return ColumnType.MONEY
        }

        // Try integer number
        if (samples.all { it.replace(" ", "").toIntOrNull() != null }) {
            return ColumnType.NUMBER
        }

        // Try account number (4-8 digits)
        val accountPattern = "^\\d{4,8}$".toRegex()
        if (samples.all { it.matches(accountPattern) }) {
            return ColumnType.ACCOUNT_NUMBER
        }

        // Default to text
        return ColumnType.TEXT
    }

    /**
     * Guess mapping based on header name and column type.
     */
    fun guessMapping(header: String, type: ColumnType): MappingType {
        val lowerHeader = header.lowercase().trim()

        // Date mapping
        if (type == ColumnType.DATE_FI || type == ColumnType.DATE_ISO || type == ColumnType.DATE_US) {
            if (lowerHeader.contains("pvm") || 
                lowerHeader.contains("date") || 
                lowerHeader.contains("päivä") ||
                lowerHeader.contains("kirjauspäivä") ||
                lowerHeader.contains("maksupäivä") ||
                lowerHeader.contains("arvopäivä")) {
                return MappingType.DATE
            }
        }

        // Money mapping
        if (type == ColumnType.MONEY) {
            when {
                lowerHeader.contains("debet") || lowerHeader.contains("veloitus") -> 
                    return MappingType.DEBIT
                lowerHeader.contains("kredit") || lowerHeader.contains("credit") || 
                lowerHeader.contains("hyvitys") -> 
                    return MappingType.CREDIT
                lowerHeader.contains("summa") || lowerHeader.contains("amount") ||
                lowerHeader.contains("yhteensä") || lowerHeader.contains("euroa") ||
                lowerHeader.contains("määrä") -> 
                    return MappingType.AMOUNT
            }
        }

        // Description / memo
        if (lowerHeader.contains("selite") || lowerHeader.contains("selitys") ||
            lowerHeader.contains("kuvaus") || lowerHeader.contains("description") ||
            lowerHeader.contains("viesti") || lowerHeader.contains("message") ||
            lowerHeader.contains("memo")) {
            return MappingType.DESCRIPTION
        }

        // Account number
        if (type == ColumnType.ACCOUNT_NUMBER ||
            lowerHeader == "tili" || lowerHeader.contains("tilinumero") ||
            lowerHeader.contains("account")) {
            return MappingType.ACCOUNT_NUMBER
        }

        // Account name
        if (lowerHeader.contains("tilin nimi") || lowerHeader.contains("account name")) {
            return MappingType.ACCOUNT_NAME
        }

        // IBAN
        if (type == ColumnType.IBAN || lowerHeader.contains("iban") ||
            lowerHeader.contains("tilinumero") && type != ColumnType.ACCOUNT_NUMBER) {
            return MappingType.IBAN
        }

        // Reference
        if (type == ColumnType.REFERENCE || lowerHeader.contains("viite") || 
            lowerHeader.contains("reference")) {
            return MappingType.REFERENCE
        }

        // Document number
        if (lowerHeader.contains("tosite") || lowerHeader.contains("document") ||
            lowerHeader.contains("nro") && lowerHeader.contains("tosite")) {
            return MappingType.DOCUMENT_NUMBER
        }

        // Payee/Payer
        if (lowerHeader.contains("saaja") || lowerHeader.contains("maksaja") ||
            lowerHeader.contains("payee") || lowerHeader.contains("payer") ||
            lowerHeader.contains("nimi") || lowerHeader.contains("name")) {
            return MappingType.PAYEE_PAYER
        }

        // VAT
        if (lowerHeader.contains("alv") || lowerHeader.contains("vat") ||
            lowerHeader.contains("vero")) {
            return MappingType.VAT_PERCENT
        }

        return MappingType.DO_NOT_IMPORT
    }

    /**
     * Try to parse a date string with given formatter.
     */
    private fun tryParseDate(text: String, formatter: DateTimeFormatter): LocalDate? {
        return try {
            LocalDate.parse(text.trim(), formatter)
        } catch (e: DateTimeParseException) {
            null
        }
    }

    /**
     * Try to parse money value (Finnish or international format).
     */
    fun parseMoney(text: String): BigDecimal? {
        val cleaned = text.trim()
            .replace("€", "")
            .replace("EUR", "", ignoreCase = true)
            .replace(" ", "")
            .replace("\u00A0", "") // Non-breaking space

        if (cleaned.isEmpty()) return null

        // Try Finnish format (comma as decimal separator): -1234,56 or 1234,56
        val fiPattern = "^-?\\d{1,3}(\\.?\\d{3})*(,\\d{1,2})?$".toRegex()
        if (cleaned.matches(fiPattern)) {
            val normalized = cleaned
                .replace(".", "") // Remove thousand separators
                .replace(',', '.') // Normalize decimal separator
            return try {
                BigDecimal(normalized)
            } catch (e: NumberFormatException) {
                null
            }
        }

        // Try international format (dot as decimal separator): -1234.56 or 1,234.56
        val intlPattern = "^-?\\d{1,3}(,?\\d{3})*(\\.\\d{1,2})?$".toRegex()
        if (cleaned.matches(intlPattern)) {
            val normalized = cleaned.replace(",", "") // Remove thousand separators
            return try {
                BigDecimal(normalized)
            } catch (e: NumberFormatException) {
                null
            }
        }

        // Simple number without formatting
        return try {
            BigDecimal(cleaned.replace(',', '.'))
        } catch (e: NumberFormatException) {
            null
        }
    }

    /**
     * Check if string is a valid Finnish reference number.
     * Reference numbers are 4-20 digits with valid checksum.
     */
    private fun isValidReference(text: String): Boolean {
        val cleaned = text.replace(" ", "").replace("-", "")
        
        if (cleaned.length < 4 || cleaned.length > 20) return false
        if (!cleaned.all { it.isDigit() }) return false
        
        // Validate checksum (modulo 10, weights 7-3-1)
        val weights = intArrayOf(7, 3, 1)
        val digits = cleaned.dropLast(1) // All except check digit
        val checkDigit = cleaned.last().digitToInt()
        
        var sum = 0
        for (i in digits.indices.reversed()) {
            sum += digits[i].digitToInt() * weights[(digits.length - 1 - i) % 3]
        }
        
        val calculated = (10 - (sum % 10)) % 10
        return calculated == checkDigit
    }

    /**
     * Parse date from string based on column type.
     */
    fun parseDate(text: String, type: ColumnType): LocalDate? {
        val formatter = when (type) {
            ColumnType.DATE_FI -> DateTimeFormatter.ofPattern("d.M.yyyy")
            ColumnType.DATE_ISO -> DateTimeFormatter.ofPattern("yyyy-MM-dd")
            ColumnType.DATE_US -> DateTimeFormatter.ofPattern("M/d/yyyy")
            else -> return null
        }
        return tryParseDate(text, formatter)
    }
}
