package kirjanpito.util.csv

import java.nio.charset.Charset
import java.nio.charset.StandardCharsets

/**
 * CSV Parser with automatic encoding and delimiter detection.
 * Supports UTF-8, UTF-8 BOM, ISO-8859-1, Windows-1252.
 * 
 * Based on Kitsas CsvTuonti implementation adapted for Kotlin.
 */
object CsvParser {

    /**
     * Detect character encoding from byte data.
     * Checks for UTF-8 BOM first, then tries to detect Finnish characters.
     */
    fun detectEncoding(data: ByteArray): Charset {
        // UTF-8 BOM detection
        if (data.size >= 3 &&
            data[0] == 0xEF.toByte() &&
            data[1] == 0xBB.toByte() &&
            data[2] == 0xBF.toByte()
        ) {
            return StandardCharsets.UTF_8
        }

        // Try to decode as UTF-8 and check for Finnish characters
        try {
            val text = String(data, StandardCharsets.UTF_8)
            if (text.contains(Regex("[äöåÄÖÅ€]"))) {
                return StandardCharsets.UTF_8
            }
        } catch (e: Exception) {
            // UTF-8 failed, try others
        }

        // Try ISO-8859-1 (Latin-1)
        try {
            val text = String(data, StandardCharsets.ISO_8859_1)
            if (text.contains(Regex("[äöåÄÖÅ]"))) {
                return StandardCharsets.ISO_8859_1
            }
        } catch (e: Exception) {
            // Continue
        }

        // Try Windows-1252
        try {
            val charset = Charset.forName("Windows-1252")
            val text = String(data, charset)
            if (text.contains(Regex("[äöåÄÖÅ€]"))) {
                return charset
            }
        } catch (e: Exception) {
            // Continue
        }

        // Default to UTF-8
        return StandardCharsets.UTF_8
    }

    /**
     * Detect CSV delimiter by counting occurrences outside quotes.
     * Supports comma, semicolon, and tab.
     */
    fun detectDelimiter(line: String): Char {
        var commaCount = 0
        var semicolonCount = 0
        var tabCount = 0
        var inQuotes = false

        for (c in line) {
            when (c) {
                '"' -> inQuotes = !inQuotes
                ',' -> if (!inQuotes) commaCount++
                ';' -> if (!inQuotes) semicolonCount++
                '\t' -> if (!inQuotes) tabCount++
            }
        }

        return when {
            semicolonCount >= commaCount && semicolonCount > tabCount -> ';'
            tabCount > commaCount && tabCount > semicolonCount -> '\t'
            else -> ','
        }
    }

    /**
     * Parse CSV data to CsvData object.
     */
    fun parse(data: ByteArray): CsvData {
        val charset = detectEncoding(data)
        var text = String(data, charset)
        
        // Remove BOM if present
        if (text.startsWith("\uFEFF")) {
            text = text.substring(1)
        }
        
        val lines = text.lines().filter { it.isNotBlank() }

        if (lines.isEmpty()) {
            return CsvData(emptyList(), ',', charset)
        }

        val delimiter = detectDelimiter(lines.first())
        val rows = lines.map { parseLine(it, delimiter) }

        return CsvData(rows, delimiter, charset)
    }

    /**
     * Parse a single CSV line respecting quotes.
     */
    private fun parseLine(line: String, delimiter: Char): List<String> {
        val fields = mutableListOf<String>()
        val currentField = StringBuilder()
        var inQuotes = false
        var i = 0

        while (i < line.length) {
            val c = line[i]

            when {
                c == '"' -> {
                    if (inQuotes && i + 1 < line.length && line[i + 1] == '"') {
                        // Escaped quote
                        currentField.append('"')
                        i++
                    } else {
                        // Toggle quote mode
                        inQuotes = !inQuotes
                    }
                }
                c == delimiter && !inQuotes -> {
                    // Field separator
                    fields.add(currentField.toString())
                    currentField.clear()
                }
                c != '\r' -> {
                    currentField.append(c)
                }
            }

            i++
        }

        // Add the last field
        fields.add(currentField.toString())

        return fields
    }

    /**
     * Validate if data is valid CSV (not binary, consistent columns).
     */
    fun isValidCsv(data: ByteArray): Boolean {
        // Check for binary data
        val sample = data.take(1024).toByteArray()
        val controlChars = sample.count { it < 10 && it != 9.toByte() }

        if (controlChars > sample.size / 3) {
            return false
        }

        // Parse and check consistency
        try {
            val csvData = parse(data.take(4096).toByteArray())

            if (csvData.rows.size < 2) {
                return false
            }

            // Check that rows have consistent column count
            val firstRowSize = csvData.rows[0].size
            
            if (firstRowSize < 2) {
                return false
            }
            
            for (row in csvData.rows.drop(1)) {
                // Allow +/- 1 column difference for trailing delimiters
                if (row.size < firstRowSize - 1 || row.size > firstRowSize + 1) {
                    return false
                }
            }

            return true
        } catch (e: Exception) {
            return false
        }
    }
}

/**
 * Container for parsed CSV data.
 */
data class CsvData(
    val rows: List<List<String>>,
    val delimiter: Char,
    val encoding: Charset
) {
    val headers: List<String> get() = rows.firstOrNull() ?: emptyList()
    val dataRows: List<List<String>> get() = rows.drop(1)
    val columnCount: Int get() = headers.size
    val rowCount: Int get() = rows.size
}
