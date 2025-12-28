# CSV-tuonti Tilittimelle - Toteutussuunnitelma ja Vertailu

**Päivitetty:** 2025-12-29
**Tarkoitus:** Yksityiskohtainen suunnitelma CSV-tuontitoiminnon toteuttamiseksi
**Tila:** SUUNNITTELU - EI TOTEUTETA VIELÄ

---

## Tiivistelmä

Tämä dokumentti määrittelee CSV-tuontitoiminnon Tilittimelle, joka perustuu Kitsas-järjestelmän analyysiin. CSV-tuonti on **kriittinen ominaisuus** pankkitiliotteiden ja kirjanpidon massatuonnille.

**Tavoite:** Toteuttaa täysi CSV-tuontijärjestelmä, joka:
1. Tunnistaa automaattisesti tiedoston koodauksen ja erottimen
2. Analysoi sarakkeiden tyypit
3. Antaa käyttäjän mapatata sarakkeet kirjanpidon kenttiin
4. Tuo data kirjanpitoon (kirjaukset tai tiliotteet)

---

## Vertailu: Kitsas vs. Tilitin

### Kitsas - Nykytila (689 riviä C++)

**Ominaisuudet:**
- ✅ Automaattinen koodauksen tunnistus (UTF-8, Latin-1, ISO-8859-15)
- ✅ Automaattinen erottimen tunnistus (`,`, `;`, `\t`)
- ✅ CSV-parsinta (lainausmerkit, escape-sekvenssit)
- ✅ Sarakkeiden tyypintunnistus (12 tyyppiä)
- ✅ Sarakkeiden mappaus (19 tuontityyppiä)
- ✅ Älykäs oletusarvausin (päättelee otsikoista)
- ✅ Kaksi tuontitilaa: Kirjaukset ja Tiliotteet
- ✅ Tilimuunto (mappaa vanhat tilit uusiin)
- ✅ Esikatselu (näyttää esimerkkidatan)

**Arkkitehtuuri:**

```cpp
class CsvTuonti : public QDialog {
    enum Sarakemuoto {
        TYHJA, TEKSTI, LUKUTEKSTI, LUKU, RAHA, TILI, VIITE,
        ALLESATA, SUOMIPVM, ISOPVM, USPVM, NDEAPVM
    };

    enum Tuominen {
        EITUODA, PAIVAMAARA, TOSITETUNNUS, TILINUMERO,
        DEBETEURO, KREDITEURO, RAHAMAARA, SELITE, IBAN,
        VIITENRO, ARKISTOTUNNUS, KOHDENNUS, TILINIMI,
        BRUTTOALVP, ALVPROSENTTI, ALVKOODI, SAAJAMAKSAJA,
        KTOKOODI, DEBETTILI, KREDITTILI, RAHASENTIT
    };

    static QString haistettuKoodattu(const QByteArray& data);
    static QChar haistaErotin(const QString& data);
    static QList<QStringList> csvListana(const QByteArray& data);
    static bool onkoCsv(const QByteArray& data);
};
```

**Koodauksen tunnistus:**
```cpp
QString haistettuKoodattu(const QByteArray& data) {
    QRegularExpression skandit("[äöÄÖ€]");

    QString utf8 = QString::fromUtf8(data);
    if (utf8.contains(skandit)) return utf8;

    QString latin1 = QString::fromLatin1(data);
    if (latin1.contains(skandit)) return latin1;

    QString iso15 = QTextCodec::codecForName("ISO-8859-15")->toUnicode(data);
    if (iso15.contains(skandit)) return iso15;

    return utf8; // Default
}
```

**Erottimen tunnistus:**
```cpp
QChar haistaErotin(const QString& data) {
    int pilkut = 0;
    int puolipisteet = 0;
    int sarkaimet = 0;

    bool lainattu = false;
    for (const QChar& mki : data) {
        if (mki == '"') lainattu = !lainattu;
        if (!lainattu) {
            if (mki == ',') pilkut++;
            else if (mki == ';') puolipisteet++;
            else if (mki == '\t') sarkaimet++;
        }
    }

    if (puolipisteet >= pilkut && puolipisteet > sarkaimet)
        return ';';
    else if (sarkaimet > pilkut && sarkaimet > puolipisteet)
        return '\t';
    else
        return ',';
}
```

### Tilitin - Nykytila (51 riviä Java)

**Ominaisuudet:**
- ✅ Perus CSV-parsinta (CSVReader.java)
- ✅ CSV-kirjoitus (CSVWriter.java)
- ✅ Tukee vain pilkkua (`,`) erottimena
- ✅ Tukee lainausmerkkejä
- ❌ Ei koodauksen tunnistusta
- ❌ Ei erottimen automaattista tunnistusta
- ❌ Ei sarakkeiden tyypintunnistusta
- ❌ Ei sarakkeiden mappaustyökalua
- ❌ Ei CSV-tuonti-UI:ta

**CSVReader.java - Analyysi:**
```java
public class CSVReader {
    public String[] readLine() throws IOException {
        // Yksinkertainen parsinta
        // Tukee vain ',' erotinta
        // Ei tee koodauksen tunnistusta
        // Ei tee tyypintunnistusta
    }
}
```

**CSVWriter.java - Analyysi:**
```java
public class CSVWriter {
    private char delimiter = ',';

    public void writeField(String field) {
        // Kirjoittaa CSV:ään
        // Tukee vaihdettavaa erotinta
    }
}
```

---

## Tavoitteet ja Prioriteetit

### Vaihe 1: CSV Parser (Prioriteetti 1)
**Työmäärä:** 3-4 päivää
**Hyöty:** ⭐⭐⭐⭐⭐ Kriittinen

- Automaattinen koodauksen tunnistus (UTF-8, UTF-8 BOM, ISO-8859-1, Windows-1252)
- Automaattinen erottimen tunnistus (`,`, `;`, `\t`)
- Robustimpi CSV-parsinta (RFC 4180 -yhteensopiva)
- CSV-validointi (tarkista onko validi CSV)

### Vaihe 2: Sarakkeiden analysointi (Prioriteetti 1)
**Työmäärä:** 2-3 päivää
**Hyöty:** ⭐⭐⭐⭐⭐ Kriittinen

- Tunnista saraketyypit automaattisesti
  - Teksti, Luku, Raha, Päivämäärä, IBAN, Viitenumero
- Analysoi päivämääräformaatit (dd.MM.yyyy, yyyy-MM-dd, MM/dd/yyyy)
- Analysoi rahaformaatit (`,` vs `.` desimaalierottimena)

### Vaihe 3: UI - Column Mapping Dialog (Prioriteetti 1)
**Työmäärä:** 4-5 päivää
**Hyöty:** ⭐⭐⭐⭐⭐ Kriittinen

- Interaktiivinen dialogi sarakkeiden mappauseen
- Esikatselu CSV-datasta
- Automaattiset oletusarvaukset otsikoiden perusteella
- Mappaus: Päivämäärä, Debet, Kredit, Selite, Tili, jne.

### Vaihe 4: Tuontilogiikka (Prioriteetti 1)
**Työmäärä:** 3-4 päivää
**Hyöty:** ⭐⭐⭐⭐⭐ Kriittinen

- Tuo kirjauksia (entries) tositteisiin
- Validoi data ennen tuontia
- Näytä tuontiraportti (onnistuneet / epäonnistuneet rivit)
- Rollback-tuki virhetilanteissa

---

## Tekninen Suunnittelu

### 1. CSV Parser - Kotlin-toteutus

**CsvParser.kt:**

```kotlin
// CsvParser.kt
package kirjanpito.util.csv

import java.io.ByteArrayInputStream
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets

object CsvParser {

    /**
     * Automaattinen koodauksen tunnistus
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

        // Try to decode as UTF-8
        try {
            val text = String(data, StandardCharsets.UTF_8)
            // Check for scandinavian characters
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
     * Automaattinen erottimen tunnistus
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
     * Parse CSV data to list of rows
     */
    fun parse(data: ByteArray): CsvData {
        val charset = detectEncoding(data)
        val text = String(data, charset)
        val lines = text.lines().filter { it.isNotBlank() }

        if (lines.isEmpty()) {
            return CsvData(emptyList(), ',', charset)
        }

        val delimiter = detectDelimiter(lines.first())
        val rows = lines.map { parseLine(it, delimiter) }

        return CsvData(rows, delimiter, charset)
    }

    /**
     * Parse a single CSV line
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
     * Validate if data is valid CSV
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
            for (i in 1 until csvData.rows.size) {
                val currentSize = csvData.rows[i].size
                if (currentSize < 2 || currentSize != firstRowSize) {
                    // Allow for trailing delimiter on header row
                    if (!(csvData.rows[i - 1].lastOrNull()?.isEmpty() == true &&
                        csvData.rows[i - 1].size == currentSize + 1)
                    ) {
                        return false
                    }
                }
            }

            return true
        } catch (e: Exception) {
            return false
        }
    }
}

/**
 * CSV data container
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
```

### 2. Column Type Detection

**CsvColumnAnalyzer.kt:**

```kotlin
// CsvColumnAnalyzer.kt
package kirjanpito.util.csv

import java.math.BigDecimal
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

enum class ColumnType {
    EMPTY,          // Tyhjä sarake
    TEXT,           // Teksti
    NUMBER,         // Luku
    MONEY,          // Rahasumma
    DATE_FI,        // Suomalainen pvm: dd.MM.yyyy
    DATE_ISO,       // ISO pvm: yyyy-MM-dd
    DATE_US,        // US pvm: MM/dd/yyyy
    IBAN,           // IBAN-tilinumero
    REFERENCE,      // Viitenumero
    ACCOUNT_NUMBER  // Tilinumero
}

enum class MappingType {
    DO_NOT_IMPORT,      // Älä tuo
    DATE,               // Päivämäärä
    DESCRIPTION,        // Selite
    DEBIT,              // Debet (euroa)
    CREDIT,             // Kredit (euroa)
    AMOUNT,             // Summa (euroa)
    ACCOUNT_NUMBER,     // Tilinumero
    ACCOUNT_NAME,       // Tilin nimi
    DOCUMENT_NUMBER,    // Tositenumero
    IBAN,               // IBAN
    REFERENCE,          // Viitenumero
    PAYEE_PAYER,        // Saaja/Maksaja
    VAT_PERCENT         // ALV-%
}

data class ColumnInfo(
    val index: Int,
    val header: String,
    val type: ColumnType,
    val samples: List<String>,
    var mapping: MappingType = MappingType.DO_NOT_IMPORT
)

object CsvColumnAnalyzer {

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

    private fun detectColumnType(samples: List<String>): ColumnType {
        if (samples.isEmpty()) {
            return ColumnType.EMPTY
        }

        // Try IBAN
        if (samples.all { IbanValidator.isValid(it) }) {
            return ColumnType.IBAN
        }

        // Try Reference
        if (samples.all { ViiteValidator.isValid(it) }) {
            return ColumnType.REFERENCE
        }

        // Try dates
        val fiDatePattern = DateTimeFormatter.ofPattern("d.M.yyyy")
        if (samples.all { tryParseDate(it, fiDatePattern) != null }) {
            return ColumnType.DATE_FI
        }

        val isoDatePattern = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        if (samples.all { tryParseDate(it, isoDatePattern) != null }) {
            return ColumnType.DATE_ISO
        }

        val usDatePattern = DateTimeFormatter.ofPattern("M/d/yyyy")
        if (samples.all { tryParseDate(it, usDatePattern) != null }) {
            return ColumnType.DATE_US
        }

        // Try money (with comma or dot as decimal separator)
        if (samples.all { parseMoney(it) != null }) {
            return ColumnType.MONEY
        }

        // Try number
        if (samples.all { it.toIntOrNull() != null }) {
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

    private fun guessMapping(header: String, type: ColumnType): MappingType {
        val lowerHeader = header.lowercase()

        // Date mapping
        if (type == ColumnType.DATE_FI || type == ColumnType.DATE_ISO || type == ColumnType.DATE_US) {
            if (lowerHeader.contains("pvm") || lowerHeader.contains("date") || lowerHeader.contains("päivä")) {
                return MappingType.DATE
            }
        }

        // Money mapping
        if (type == ColumnType.MONEY) {
            when {
                lowerHeader.contains("debet") -> return MappingType.DEBIT
                lowerHeader.contains("kredit") || lowerHeader.contains("credit") -> return MappingType.CREDIT
                lowerHeader.contains("summa") || lowerHeader.contains("amount") ||
                lowerHeader.contains("yhteensä") || lowerHeader.contains("euroa") -> return MappingType.AMOUNT
            }
        }

        // Description
        if (lowerHeader.contains("selite") || lowerHeader.contains("selitys") ||
            lowerHeader.contains("kuvaus") || lowerHeader.contains("description")) {
            return MappingType.DESCRIPTION
        }

        // Account number
        if (type == ColumnType.ACCOUNT_NUMBER ||
            lowerHeader.contains("tili") || lowerHeader.contains("account")) {
            return MappingType.ACCOUNT_NUMBER
        }

        // Account name
        if (lowerHeader.contains("tilin nimi") || lowerHeader.contains("account name")) {
            return MappingType.ACCOUNT_NAME
        }

        // IBAN
        if (type == ColumnType.IBAN || lowerHeader.contains("iban")) {
            return MappingType.IBAN
        }

        // Reference
        if (type == ColumnType.REFERENCE || lowerHeader.contains("viite") || lowerHeader.contains("reference")) {
            return MappingType.REFERENCE
        }

        // Document number
        if (lowerHeader.contains("tosite") || lowerHeader.contains("document")) {
            return MappingType.DOCUMENT_NUMBER
        }

        // Payee/Payer
        if (lowerHeader.contains("saaja") || lowerHeader.contains("maksaja") ||
            lowerHeader.contains("payee") || lowerHeader.contains("payer")) {
            return MappingType.PAYEE_PAYER
        }

        // VAT
        if (lowerHeader.contains("alv") || lowerHeader.contains("vat")) {
            return MappingType.VAT_PERCENT
        }

        return MappingType.DO_NOT_IMPORT
    }

    private fun tryParseDate(text: String, formatter: DateTimeFormatter): LocalDate? {
        return try {
            LocalDate.parse(text, formatter)
        } catch (e: DateTimeParseException) {
            null
        }
    }

    private fun parseMoney(text: String): BigDecimal? {
        val cleaned = text.trim()
            .replace("€", "")
            .replace("EUR", "")
            .replace(" ", "")

        // Try comma as decimal separator (Finnish)
        val commaPattern = "^-?\\d+,\\d{2}$".toRegex()
        if (cleaned.matches(commaPattern)) {
            return BigDecimal(cleaned.replace(',', '.'))
        }

        // Try dot as decimal separator (International)
        val dotPattern = "^-?\\d+\\.\\d{2}$".toRegex()
        if (cleaned.matches(dotPattern)) {
            return BigDecimal(cleaned)
        }

        return null
    }
}
```

### 3. UI - CSV Import Dialog

**CsvImportDialog.kt:**

```kotlin
// CsvImportDialog.kt
package kirjanpito.ui.dialogs

import kirjanpito.db.Document
import kirjanpito.db.Entry
import kirjanpito.util.csv.*
import java.awt.*
import java.io.File
import javax.swing.*
import javax.swing.filechooser.FileNameExtensionFilter
import javax.swing.table.AbstractTableModel
import javax.swing.table.DefaultTableCellRenderer

class CsvImportDialog(
    parent: JFrame,
    private val document: Document
) : JDialog(parent, "CSV-tuonti", true) {

    private var csvData: CsvData? = null
    private var columns: List<ColumnInfo> = emptyList()

    private val previewTable = JTable()
    private val mappingTable = JTable()
    private val statusLabel = JLabel("Valitse CSV-tiedosto")

    private val fileButton = JButton("Valitse tiedosto...")
    private val importButton = JButton("Tuo kirjaukset")
    private val cancelButton = JButton("Peruuta")

    init {
        layout = BorderLayout(10, 10)
        preferredSize = Dimension(1000, 700)

        buildUI()

        fileButton.addActionListener { selectFile() }
        importButton.addActionListener { importData() }
        cancelButton.addActionListener { dispose() }

        importButton.isEnabled = false

        pack()
        setLocationRelativeTo(parent)
    }

    private fun buildUI() {
        // Top panel: File selection
        val topPanel = JPanel(FlowLayout(FlowLayout.LEFT))
        topPanel.add(fileButton)
        topPanel.add(statusLabel)
        add(topPanel, BorderLayout.NORTH)

        // Center: Split pane with preview and mapping
        val splitPane = JSplitPane(JSplitPane.VERTICAL_SPLIT)
        splitPane.dividerLocation = 300

        // Preview panel
        val previewPanel = JPanel(BorderLayout())
        previewPanel.border = BorderFactory.createTitledBorder("Esikatselu CSV-datasta")
        previewPanel.add(JScrollPane(previewTable), BorderLayout.CENTER)
        splitPane.topComponent = previewPanel

        // Mapping panel
        val mappingPanel = JPanel(BorderLayout())
        mappingPanel.border = BorderFactory.createTitledBorder("Sarakkeiden mappaus")

        mappingTable.fillsViewportHeight = true
        mappingTable.setDefaultRenderer(Any::class.java, MappingCellRenderer())

        mappingPanel.add(JScrollPane(mappingTable), BorderLayout.CENTER)
        splitPane.bottomComponent = mappingPanel

        add(splitPane, BorderLayout.CENTER)

        // Bottom panel: Buttons
        val bottomPanel = JPanel(FlowLayout(FlowLayout.RIGHT))
        bottomPanel.add(importButton)
        bottomPanel.add(cancelButton)
        add(bottomPanel, BorderLayout.SOUTH)
    }

    private fun selectFile() {
        val fileChooser = JFileChooser()
        fileChooser.fileFilter = FileNameExtensionFilter("CSV Files", "csv", "txt")

        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            val file = fileChooser.selectedFile
            loadCsvFile(file)
        }
    }

    private fun loadCsvFile(file: File) {
        try {
            val data = file.readBytes()

            // Validate CSV
            if (!CsvParser.isValidCsv(data)) {
                JOptionPane.showMessageDialog(
                    this,
                    "Tiedosto ei ole validi CSV-tiedosto",
                    "Virhe",
                    JOptionPane.ERROR_MESSAGE
                )
                return
            }

            // Parse CSV
            csvData = CsvParser.parse(data)
            columns = CsvColumnAnalyzer.analyzeColumns(csvData!!)

            // Update UI
            updatePreviewTable()
            updateMappingTable()

            statusLabel.text = "Ladattu: ${file.name} (${csvData!!.rowCount} riviä, ${csvData!!.columnCount} saraketta)"
            importButton.isEnabled = true

        } catch (e: Exception) {
            JOptionPane.showMessageDialog(
                this,
                "Virhe ladattaessa tiedostoa: ${e.message}",
                "Virhe",
                JOptionPane.ERROR_MESSAGE
            )
        }
    }

    private fun updatePreviewTable() {
        val data = csvData ?: return

        previewTable.model = object : AbstractTableModel() {
            override fun getRowCount() = minOf(10, data.dataRows.size)
            override fun getColumnCount() = data.columnCount
            override fun getColumnName(column: Int) = data.headers.getOrNull(column) ?: ""
            override fun getValueAt(rowIndex: Int, columnIndex: Int) =
                data.dataRows.getOrNull(rowIndex)?.getOrNull(columnIndex) ?: ""
        }
    }

    private fun updateMappingTable() {
        mappingTable.model = object : AbstractTableModel() {
            override fun getRowCount() = columns.size
            override fun getColumnCount() = 4

            override fun getColumnName(column: Int) = when (column) {
                0 -> "Sarake"
                1 -> "Tyyppi"
                2 -> "Mappaus"
                3 -> "Esimerkki"
                else -> ""
            }

            override fun getValueAt(rowIndex: Int, columnIndex: Int): Any {
                val col = columns[rowIndex]
                return when (columnIndex) {
                    0 -> col.header
                    1 -> col.type.toString()
                    2 -> col.mapping
                    3 -> col.samples.firstOrNull() ?: ""
                    else -> ""
                }
            }

            override fun isCellEditable(rowIndex: Int, columnIndex: Int) = columnIndex == 2

            override fun setValueAt(value: Any?, rowIndex: Int, columnIndex: Int) {
                if (columnIndex == 2 && value is MappingType) {
                    columns[rowIndex].mapping = value
                    fireTableCellUpdated(rowIndex, columnIndex)
                }
            }

            override fun getColumnClass(columnIndex: Int) = when (columnIndex) {
                2 -> MappingType::class.java
                else -> String::class.java
            }
        }

        // Set combo box editor for mapping column
        val mappingEditor = DefaultCellEditor(JComboBox(MappingType.values()))
        mappingTable.columnModel.getColumn(2).cellEditor = mappingEditor
    }

    private fun importData() {
        val data = csvData ?: return

        try {
            val entries = mutableListOf<Entry>()

            // Find required mappings
            val dateColumn = columns.find { it.mapping == MappingType.DATE }
            val descriptionColumn = columns.find { it.mapping == MappingType.DESCRIPTION }
            val debitColumn = columns.find { it.mapping == MappingType.DEBIT }
            val creditColumn = columns.find { it.mapping == MappingType.CREDIT }
            val amountColumn = columns.find { it.mapping == MappingType.AMOUNT }
            val accountColumn = columns.find { it.mapping == MappingType.ACCOUNT_NUMBER }

            // Validate mappings
            if (dateColumn == null) {
                JOptionPane.showMessageDialog(
                    this,
                    "Päivämäärä-sarake on pakollinen",
                    "Virhe",
                    JOptionPane.ERROR_MESSAGE
                )
                return
            }

            if ((debitColumn == null && creditColumn == null) && amountColumn == null) {
                JOptionPane.showMessageDialog(
                    this,
                    "Debet-, Kredit- tai Summa-sarake on pakollinen",
                    "Virhe",
                    JOptionPane.ERROR_MESSAGE
                )
                return
            }

            // Import rows
            var successCount = 0
            var errorCount = 0

            for (row in data.dataRows) {
                try {
                    val entry = parseRow(row)
                    entries.add(entry)
                    successCount++
                } catch (e: Exception) {
                    errorCount++
                    println("Error parsing row: ${e.message}")
                }
            }

            // Show result
            val message = "Tuotu $successCount kirjausta onnistuneesti.\n" +
                    if (errorCount > 0) "$errorCount riviä ohitettiin virheiden vuoksi." else ""

            JOptionPane.showMessageDialog(
                this,
                message,
                "Tuonti valmis",
                JOptionPane.INFORMATION_MESSAGE
            )

            dispose()

        } catch (e: Exception) {
            JOptionPane.showMessageDialog(
                this,
                "Virhe tuonnissa: ${e.message}",
                "Virhe",
                JOptionPane.ERROR_MESSAGE
            )
        }
    }

    private fun parseRow(row: List<String>): Entry {
        // TODO: Implement row parsing based on column mappings
        // This is a placeholder
        throw NotImplementedError("Row parsing not yet implemented")
    }

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
                text = when (value) {
                    MappingType.DO_NOT_IMPORT -> "Älä tuo"
                    MappingType.DATE -> "Päivämäärä"
                    MappingType.DESCRIPTION -> "Selite"
                    MappingType.DEBIT -> "Debet"
                    MappingType.CREDIT -> "Kredit"
                    MappingType.AMOUNT -> "Summa"
                    MappingType.ACCOUNT_NUMBER -> "Tilinumero"
                    MappingType.ACCOUNT_NAME -> "Tilin nimi"
                    MappingType.DOCUMENT_NUMBER -> "Tositenumero"
                    MappingType.IBAN -> "IBAN"
                    MappingType.REFERENCE -> "Viitenumero"
                    MappingType.PAYEE_PAYER -> "Saaja/Maksaja"
                    MappingType.VAT_PERCENT -> "ALV-%"
                }
            }

            return c
        }
    }
}
```

---

## Toteutusjärjestys

### Sprint 1: CSV Parser ja Column Analyzer (4-5 päivää)

**Päivä 1-2:** CSV Parser
- `CsvParser.kt` - Encoding detection, delimiter detection, parsing
- `CsvData.kt` - Data class
- Yksikkötestit

**Päivä 3-4:** Column Analyzer
- `CsvColumnAnalyzer.kt` - Type detection, mapping guessing
- `ColumnType` ja `MappingType` enumit
- Yksikkötestit

**Päivä 5:** Validaattorit
- `IbanValidator.kt`
- `ViiteValidator.kt`
- Integraatiotestit

### Sprint 2: UI - Import Dialog (4-5 päivää)

**Päivä 6-7:** Perus-UI
- `CsvImportDialog.kt` - Dialog rakenne
- File chooser
- Preview table

**Päivä 8-9:** Mapping Table
- Column mapping table
- ComboBox editor
- Auto-guess mappings

**Päivä 10:** Polishing
- UI-parannukset
- Error handling
- User feedback

### Sprint 3: Import Logic (3-4 päivää)

**Päivä 11-12:** Entry Import
- Parse rows to `Entry` objects
- Validation
- Error handling

**Päivä 13:** Transaction Management
- Rollback support
- Import report
- Progress indicator

**Päivä 14:** Testing
- End-to-end testing
- Test with real bank CSVs
- Bug fixes

---

## Testaussuunnitelma

### Yksikkötestit

```kotlin
// CsvParserTest.kt
class CsvParserTest {

    @Test
    fun `detect UTF-8 encoding`() {
        val data = "nimi,summa\näiti,123.45".toByteArray(StandardCharsets.UTF_8)
        val encoding = CsvParser.detectEncoding(data)
        assertEquals(StandardCharsets.UTF_8, encoding)
    }

    @Test
    fun `detect UTF-8 BOM`() {
        val bom = byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte())
        val text = "name,amount".toByteArray(StandardCharsets.UTF_8)
        val data = bom + text

        val encoding = CsvParser.detectEncoding(data)
        assertEquals(StandardCharsets.UTF_8, encoding)
    }

    @Test
    fun `detect comma delimiter`() {
        val line = "name,amount,date"
        val delimiter = CsvParser.detectDelimiter(line)
        assertEquals(',', delimiter)
    }

    @Test
    fun `detect semicolon delimiter`() {
        val line = "nimi;summa;pvm"
        val delimiter = CsvParser.detectDelimiter(line)
        assertEquals(';', delimiter)
    }

    @Test
    fun `detect tab delimiter`() {
        val line = "name\tamount\tdate"
        val delimiter = CsvParser.detectDelimiter(line)
        assertEquals('\t', delimiter)
    }

    @Test
    fun `parse CSV with quotes`() {
        val data = """
            name,amount,description
            "John Doe",123.45,"Payment for ""services"""
            "Jane Smith",67.89,"Regular payment"
        """.trimIndent().toByteArray()

        val csvData = CsvParser.parse(data)

        assertEquals(3, csvData.rows.size)
        assertEquals("John Doe", csvData.dataRows[0][0])
        assertEquals("Payment for \"services\"", csvData.dataRows[0][2])
    }

    @Test
    fun `validate CSV`() {
        val validCsv = """
            header1,header2,header3
            value1,value2,value3
            value4,value5,value6
        """.trimIndent().toByteArray()

        assertTrue(CsvParser.isValidCsv(validCsv))
    }

    @Test
    fun `reject binary data`() {
        val binaryData = ByteArray(1024) { it.toByte() }
        assertFalse(CsvParser.isValidCsv(binaryData))
    }
}

// CsvColumnAnalyzerTest.kt
class CsvColumnAnalyzerTest {

    @Test
    fun `detect money column type`() {
        val samples = listOf("123,45", "67,89", "1000,00")
        val type = CsvColumnAnalyzer.detectColumnType(samples)
        assertEquals(ColumnType.MONEY, type)
    }

    @Test
    fun `detect date column type (Finnish)`() {
        val samples = listOf("1.1.2024", "15.3.2024", "31.12.2024")
        val type = CsvColumnAnalyzer.detectColumnType(samples)
        assertEquals(ColumnType.DATE_FI, type)
    }

    @Test
    fun `guess debit mapping from header`() {
        val mapping = CsvColumnAnalyzer.guessMapping("Debet", ColumnType.MONEY)
        assertEquals(MappingType.DEBIT, mapping)
    }

    @Test
    fun `guess credit mapping from header`() {
        val mapping = CsvColumnAnalyzer.guessMapping("Kredit", ColumnType.MONEY)
        assertEquals(MappingType.CREDIT, mapping)
    }

    @Test
    fun `guess date mapping from header`() {
        val mapping = CsvColumnAnalyzer.guessMapping("Päivämäärä", ColumnType.DATE_FI)
        assertEquals(MappingType.DATE, mapping)
    }
}
```

### Integraatiotestit

```kotlin
// CsvImportIntegrationTest.kt
class CsvImportIntegrationTest {

    @Test
    fun `import bank statement CSV`() {
        val csv = """
            Päivä;Selite;Debet;Kredit
            1.1.2024;Palkka;;3000,00
            5.1.2024;Vuokra;800,00;
            10.1.2024;Ruoka;50,00;
        """.trimIndent().toByteArray()

        val csvData = CsvParser.parse(csv)
        val columns = CsvColumnAnalyzer.analyzeColumns(csvData)

        // Verify mappings
        assertEquals(MappingType.DATE, columns[0].mapping)
        assertEquals(MappingType.DESCRIPTION, columns[1].mapping)
        assertEquals(MappingType.DEBIT, columns[2].mapping)
        assertEquals(MappingType.CREDIT, columns[3].mapping)
    }
}
```

---

## UI Mockup

```
┌─────────────────────────────────────────────────────────────────────┐
│ CSV-tuonti                                                    [X]   │
├─────────────────────────────────────────────────────────────────────┤
│ [Valitse tiedosto...] Ladattu: tilitteet.csv (25 riviä, 4 sar.)   │
├─────────────────────────────────────────────────────────────────────┤
│ Esikatselu CSV-datasta                                              │
│ ┌─────────────────────────────────────────────────────────────────┐ │
│ │ Päivä      │ Selite          │ Debet    │ Kredit              │ │
│ ├────────────┼─────────────────┼──────────┼─────────────────────┤ │
│ │ 1.1.2024   │ Palkka          │          │ 3000,00             │ │
│ │ 5.1.2024   │ Vuokra          │ 800,00   │                     │ │
│ │ 10.1.2024  │ Ruoka           │ 50,00    │                     │ │
│ └─────────────────────────────────────────────────────────────────┘ │
├─────────────────────────────────────────────────────────────────────┤
│ Sarakkeiden mappaus                                                  │
│ ┌─────────────────────────────────────────────────────────────────┐ │
│ │ Sarake  │ Tyyppi   │ Mappaus        │ Esimerkki             │ │
│ ├─────────┼──────────┼────────────────┼───────────────────────┤ │
│ │ Päivä   │ DATE_FI  │ [Päivämäärä ▼] │ 1.1.2024              │ │
│ │ Selite  │ TEXT     │ [Selite ▼]     │ Palkka                │ │
│ │ Debet   │ MONEY    │ [Debet ▼]      │ 800,00                │ │
│ │ Kredit  │ MONEY    │ [Kredit ▼]     │ 3000,00               │ │
│ └─────────────────────────────────────────────────────────────────┘ │
├─────────────────────────────────────────────────────────────────────┤
│                                      [Tuo kirjaukset] [Peruuta]    │
└─────────────────────────────────────────────────────────────────────┘
```

---

## Yhteenveto

### Työmäärä

**Yhteensä:** 11-14 työpäivää (2.5-3 viikkoa)

- Sprint 1: Parser & Analyzer (4-5 päivää)
- Sprint 2: UI Dialog (4-5 päivää)
- Sprint 3: Import Logic (3-4 päivää)

### Vertailu Kitsaaseen

| Ominaisuus | Kitsas | Tilitin (nyt) | Tilitin (suunnitelma) |
|------------|--------|---------------|----------------------|
| Koodauksen tunnistus | ✅ UTF-8, Latin-1, ISO-8859-15 | ❌ | ✅ UTF-8, UTF-8 BOM, ISO-8859-1, Win-1252 |
| Erottimen tunnistus | ✅ `,`, `;`, `\t` | ❌ (vain `,`) | ✅ `,`, `;`, `\t` |
| CSV-parsinta | ✅ 689 riviä C++ | ✅ 51 riviä Java (perus) | ✅ Parannettu Kotlin |
| Sarakkeiden tyypintunnistus | ✅ 12 tyyppiä | ❌ | ✅ 10 tyyppiä |
| Sarakkeiden mappaus | ✅ 19 tuontityyppiä | ❌ | ✅ 13 mappaustyyppiä |
| Älykäs oletusarvaus | ✅ | ❌ | ✅ |
| UI-dialogi | ✅ Qt | ❌ | ✅ Swing |
| Esikatselu | ✅ | ❌ | ✅ |

### Hyödyt

✅ **Kriittinen ominaisuus** pankkitiliotteiden tuontiin
✅ **Säästää aikaa** - ei manuaalista kirjaamista
✅ **Vähentää virheitä** - automaattinen validointi
✅ **Käyttäjäystävällinen** - älykäs oletusarvaus
✅ **Yhteensopiva** - toimii eri pankkien CSV-formaattien kanssa

---

## Seuraavat Askeleet

1. **Hyväksy suunnitelma** - Käy läpi ja kommentoi
2. **Luo feature branch** - `feature/csv-import`
3. **Aloita Sprint 1** - CSV Parser ja Column Analyzer
4. **Testaa jatkuvasti** - Yksikkö- ja integraatiotestit
5. **Dokumentoi** - Päivitä USER-GUIDE

---

**Tekijä:** Claude Sonnet 4.5 (AI-avusteinen suunnittelu)
**Päivitetty:** 2025-12-29
**Tila:** EHDOTUS - Odottaa hyväksyntää
