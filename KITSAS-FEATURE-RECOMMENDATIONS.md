# Kitsas-ominaisuudet Tilittimelle - Suositukset

**Päivitetty:** 2025-12-28
**Tarkoitus:** Ehdotukset ominaisuuksista, jotka voisi tuoda Kitsaasta Tilittimeen
**Tila:** SUUNNITTELU - EI TOTEUTETA VIELÄ

---

## Tiivistelmä

Tämä dokumentti analysoi Kitsas-kirjanpito-ohjelman ominaisuuksia ja ehdottaa, mitkä niistä olisi järkevää tuoda Tilittimeen. Suositukset on jaoteltu **prioriteettien** mukaan ja arvioitu **toteutuksen vaativuuden** perusteella.

---

## Suositusten Priorisointi

### Prioriteetti 1 (Kriittinen) - Toteutettava ensin
Ominaisuudet, jotka tuovat **merkittävää lisäarvoa** ja ovat **suhteellisen helposti** toteutettavissa.

### Prioriteetti 2 (Tärkeä) - Toteutettava seuraavaksi
Ominaisuudet, jotka parantavat **käyttökokemusta** merkittävästi.

### Prioriteetti 3 (Hyödyllinen) - Harkittava
Ominaisuudet, jotka ovat **nice-to-have** mutta eivät kriittisiä.

### Prioriteetti 4 (Valinnainen) - Tulevaisuus
Ominaisuudet, jotka vaativat **paljon työtä** ja ovat vähemmän kriittisiä.

---

## 1. Validaattorit (Prioriteetti 1)

### Miksi tärkeä?
Validaattorit parantavat **datan laatua** ja **käyttökokemusta** estämällä virheelliset syötteet.

### Kitsas-toteutus:

Kitsaassa on 3 validaattoria:
1. **YTunnusValidator** - Y-tunnuksen muodon tarkastus
2. **IbanValidator** - IBAN-tilinumeron validointi
3. **ViiteValidator** - Viitenumeron tarkastus

```cpp
// YTunnusValidator
class YTunnusValidator : public QValidator {
    State validate(QString &input, int &pos) const;
    static bool kelpaako(const QString& input, bool alvtunnuksia = false);
};

// IbanValidator
class IbanValidator : public QValidator {
    State validate(QString &input, int &pos) const override;
    static bool kelpaako(const QString& input);
};
```

### Suositus Tilittimelle:

**Toteutettava Kotlin/Java:**

```kotlin
// IbanValidator.kt
class IbanValidator {
    companion object {
        fun isValid(iban: String): Boolean {
            val cleaned = iban.replace("\\s".toRegex(), "").uppercase()
            if (!cleaned.matches("^[A-Z]{2}[0-9]{2}[A-Z0-9]+$".toRegex())) return false

            // IBAN mod-97 tarkistus
            val rearranged = cleaned.substring(4) + cleaned.substring(0, 4)
            val numeric = rearranged.map {
                if (it.isDigit()) it.toString()
                else (it.code - 55).toString()
            }.joinToString("")

            return numeric.toBigInteger().mod(97.toBigInteger()) == 1.toBigInteger()
        }

        fun format(iban: String): String {
            val cleaned = iban.replace("\\s".toRegex(), "").uppercase()
            return cleaned.chunked(4).joinToString(" ")
        }
    }
}

// YTunnusValidator.kt
class YTunnusValidator {
    companion object {
        fun isValid(ytunnus: String): Boolean {
            val pattern = "^(\\d{7})-(\\d)$".toRegex()
            val match = pattern.matchEntire(ytunnus) ?: return false

            val (number, checkDigit) = match.destructured
            val weights = intArrayOf(7, 9, 10, 5, 8, 4, 2)

            val sum = number.mapIndexed { i, c ->
                (c.toString().toInt() * weights[i])
            }.sum()

            val remainder = sum % 11
            val expected = when (remainder) {
                0 -> '0'
                1 -> return false // Ei kelpaa
                else -> ('0' + (11 - remainder))
            }

            return checkDigit[0] == expected
        }

        fun format(ytunnus: String): String {
            val cleaned = ytunnus.replace("[^0-9]".toRegex(), "")
            return if (cleaned.length == 8) {
                "${cleaned.substring(0, 7)}-${cleaned[7]}"
            } else ytunnus
        }
    }
}

// ViiteValidator.kt (Suomalainen viitenumero)
class ViiteValidator {
    companion object {
        fun isValid(viite: String): Boolean {
            val cleaned = viite.replace("\\s".toRegex(), "")
            if (!cleaned.matches("^\\d+$".toRegex())) return false
            if (cleaned.length < 4 || cleaned.length > 20) return false

            // Tarkistusnumero (modulo 10)
            val checkDigit = cleaned.last().toString().toInt()
            val numbers = cleaned.dropLast(1).reversed()

            val weights = intArrayOf(7, 3, 1)
            val sum = numbers.mapIndexed { i, c ->
                (c.toString().toInt() * weights[i % 3])
            }.sum()

            val expected = (10 - (sum % 10)) % 10
            return checkDigit == expected
        }

        fun generate(base: String): String {
            val cleaned = base.replace("\\s".toRegex(), "")
            val numbers = cleaned.reversed()

            val weights = intArrayOf(7, 3, 1)
            val sum = numbers.mapIndexed { i, c ->
                (c.toString().toInt() * weights[i % 3])
            }.sum()

            val checkDigit = (10 - (sum % 10)) % 10
            return cleaned + checkDigit
        }

        fun format(viite: String): String {
            val cleaned = viite.replace("\\s".toRegex(), "")
            return cleaned.chunked(5).joinToString(" ")
        }
    }
}
```

**Käyttö Swing-komponenteissa:**

```kotlin
// Swing InputVerifier
class IbanInputVerifier : InputVerifier() {
    override fun verify(input: JComponent): Boolean {
        val textField = input as? JTextField ?: return false
        val text = textField.text.trim()
        if (text.isEmpty()) return true
        return IbanValidator.isValid(text)
    }

    override fun shouldYieldFocus(input: JComponent): Boolean {
        val valid = verify(input)
        if (!valid) {
            JOptionPane.showMessageDialog(
                input,
                "Virheellinen IBAN-tilinumero",
                "Validointivirhe",
                JOptionPane.ERROR_MESSAGE
            )
        }
        return valid
    }
}

// Käyttö
ibanTextField.inputVerifier = IbanInputVerifier()
ytunnusTextField.inputVerifier = YTunnusInputVerifier()
viiteTextField.inputVerifier = ViiteInputVerifier()
```

**Vaativuus:** ⭐ Helppo
**Hyöty:** ⭐⭐⭐⭐⭐ Erittäin suuri
**Prioriteetti:** 1 (KRIITTINEN)

---

## 2. CSV-tuonti (Prioriteetti 1)

### Miksi tärkeä?
CSV-tuonti on **kriittinen ominaisuus** pankkitiliotteiden ja kirjanpidon **massatuonnille**.

### Kitsas-toteutus:

Kitsaassa on edistynyt CSV-tuontijärjestelmä:

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

    static QString haistettuKoodattu(const QByteArray& data); // Koodauksen tunnistus
    static QChar haistaErotin(const QString& data);          // Erottimen tunnistus
    static QList<QStringList> csvListana(const QByteArray& data);
};
```

**Ominaisuudet:**
- Automaattinen **koodauksen tunnistus** (UTF-8, ISO-8859-1, Windows-1252)
- Automaattinen **erottimen tunnistus** (`,`, `;`, `TAB`)
- **Sarakkeiden mappaus** käyttöliittymässä
- Tuki useille **päivämääräformaateille** (FI: dd.MM.yyyy, ISO: yyyy-MM-dd, US: MM/dd/yyyy)
- **Rahaformaattien** tunnistus (`,` ja `.` desimaalerottimena)

### Suositus Tilittimelle:

**Toteutettava Kotlin:**

```kotlin
// CsvImporter.kt
class CsvImporter {
    enum class ColumnType {
        EMPTY, TEXT, NUMBER, MONEY, ACCOUNT,
        DATE_FI, DATE_ISO, DATE_US,
        DEBIT, CREDIT, DESCRIPTION, REFERENCE
    }

    data class CsvColumn(
        val index: Int,
        val header: String,
        val type: ColumnType,
        val mapping: String? = null
    )

    companion object {
        fun detectEncoding(data: ByteArray): Charset {
            // UTF-8 BOM detection
            if (data.size >= 3 &&
                data[0] == 0xEF.toByte() &&
                data[1] == 0xBB.toByte() &&
                data[2] == 0xBF.toByte()) {
                return Charsets.UTF_8
            }

            // Try UTF-8, fallback to ISO-8859-1
            return try {
                CharsetDecoder().decode(ByteBuffer.wrap(data))
                Charsets.UTF_8
            } catch (e: CharacterCodingException) {
                Charset.forName("ISO-8859-1")
            }
        }

        fun detectDelimiter(line: String): Char {
            val delimiters = mapOf(',' to 0, ';' to 0, '\t' to 0)
            line.forEach { c ->
                if (c in delimiters.keys) delimiters[c]++
            }
            return delimiters.maxByOrNull { it.value }?.key ?: ','
        }

        fun parseCsv(data: ByteArray): List<List<String>> {
            val charset = detectEncoding(data)
            val text = String(data, charset)
            val lines = text.lines().filter { it.isNotBlank() }

            if (lines.isEmpty()) return emptyList()

            val delimiter = detectDelimiter(lines[0])
            return lines.map { line ->
                parseCsvLine(line, delimiter)
            }
        }

        private fun parseCsvLine(line: String, delimiter: Char): List<String> {
            val result = mutableListOf<String>()
            var current = StringBuilder()
            var inQuotes = false

            for (i in line.indices) {
                val c = line[i]
                when {
                    c == '"' -> inQuotes = !inQuotes
                    c == delimiter && !inQuotes -> {
                        result.add(current.toString().trim())
                        current = StringBuilder()
                    }
                    else -> current.append(c)
                }
            }
            result.add(current.toString().trim())
            return result
        }

        fun parseDate(text: String): LocalDate? {
            // Try FI format: dd.MM.yyyy
            val fiPattern = "^(\\d{1,2})\\.(\\d{1,2})\\.(\\d{4})$".toRegex()
            fiPattern.matchEntire(text)?.let { match ->
                val (day, month, year) = match.destructured
                return LocalDate.of(year.toInt(), month.toInt(), day.toInt())
            }

            // Try ISO format: yyyy-MM-dd
            val isoPattern = "^(\\d{4})-(\\d{1,2})-(\\d{1,2})$".toRegex()
            isoPattern.matchEntire(text)?.let { match ->
                val (year, month, day) = match.destructured
                return LocalDate.of(year.toInt(), month.toInt(), day.toInt())
            }

            // Try US format: MM/dd/yyyy
            val usPattern = "^(\\d{1,2})/(\\d{1,2})/(\\d{4})$".toRegex()
            usPattern.matchEntire(text)?.let { match ->
                val (month, day, year) = match.destructured
                return LocalDate.of(year.toInt(), month.toInt(), day.toInt())
            }

            return null
        }

        fun parseMoney(text: String): BigDecimal? {
            // Remove whitespace and currency symbols
            val cleaned = text.replace("\\s".toRegex(), "")
                .replace("€", "")
                .replace("EUR", "")
                .trim()

            if (cleaned.isEmpty()) return null

            // Try comma as decimal separator (Finnish)
            val commaPattern = "^-?(\\d+),(\\d{2})$".toRegex()
            commaPattern.matchEntire(cleaned)?.let {
                return BigDecimal(cleaned.replace(',', '.'))
            }

            // Try dot as decimal separator (International)
            val dotPattern = "^-?(\\d+)\\.(\\d{2})$".toRegex()
            dotPattern.matchEntire(cleaned)?.let {
                return BigDecimal(cleaned)
            }

            return null
        }
    }
}

// CsvImportDialog.kt
class CsvImportDialog(
    private val data: ByteArray,
    parent: JFrame
) : JDialog(parent, "CSV-tuonti", true) {

    private val csvData = CsvImporter.parseCsv(data)
    private val columnMappings = mutableMapOf<Int, CsvImporter.ColumnType>()

    fun showDialog(): List<Entry>? {
        // Build UI with table showing CSV preview
        // Allow user to map columns to Entry fields
        // Return parsed entries or null if cancelled
    }
}
```

**Käyttö:**

```kotlin
// MenuBuilder tai DocumentFrame
fun importCsv() {
    val fileChooser = JFileChooser()
    fileChooser.fileFilter = FileNameExtensionFilter("CSV-tiedostot", "csv", "txt")

    if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
        val file = fileChooser.selectedFile
        val data = file.readBytes()

        val dialog = CsvImportDialog(data, this)
        val entries = dialog.showDialog()

        entries?.let {
            // Import entries to current document
            importEntries(it)
        }
    }
}
```

**Vaativuus:** ⭐⭐⭐ Keskitaso
**Hyöty:** ⭐⭐⭐⭐⭐ Erittäin suuri
**Prioriteetti:** 1 (KRIITTINEN)

---

## 3. Kirjausapurit (Apuri-järjestelmä) (Prioriteetti 2)

### Miksi tärkeä?
Kirjausapurit **yksinkertaistavat** yleisiä kirjauksia ja **vähentävät virheitä**.

### Kitsas-toteutus:

Kitsaassa on 3 kirjausapuria:
1. **TuloMenoApuri** - Tulo/meno-kirjaukset
2. **SiirtoApuri** - Tilisiirrot
3. **PalkkaApuri** - Palkkakirjaukset

```cpp
// ApuriWidget - Abstrakti base class
class ApuriWidget : public QWidget {
    Q_OBJECT
public:
    ApuriWidget(QWidget *parent, Tosite *pTosite);
    virtual void tuo(QVariantMap map);

public slots:
    virtual void reset();
    virtual bool tositteelle(); // Vie tiedot tositteelle

protected:
    virtual void teeReset() = 0;
    virtual bool teeTositteelle() = 0;

    Tosite* pTosite_;
};

// TuloMenoApuri - Yksinkertainen tulo/meno
// Käyttäjä syöttää: summa, tili, selite, ALV
// Apuri luo automaattisesti vastatili-kirjauksen
```

### Suositus Tilittimelle:

**Toteutettava Kotlin/Swing:**

```kotlin
// EntryAssistant.kt - Base class
abstract class EntryAssistant(
    protected val document: Document,
    protected val accountDAO: AccountDAO,
    protected val entryDAO: EntryDAO
) {
    abstract fun createEntries(): List<Entry>
    abstract fun buildUI(): JPanel

    fun showDialog(parent: JFrame): Boolean {
        val dialog = JDialog(parent, getTitle(), true)
        dialog.contentPane = buildUI()

        val okButton = JButton("OK").apply {
            addActionListener {
                if (validate()) {
                    val entries = createEntries()
                    entries.forEach { entryDAO.save(it) }
                    dialog.dispose()
                }
            }
        }

        // ... build dialog ...
        dialog.isVisible = true
        return dialog.isVisible
    }

    abstract fun validate(): Boolean
    abstract fun getTitle(): String
}

// SimpleIncomeExpenseAssistant.kt
class SimpleIncomeExpenseAssistant(
    document: Document,
    accountDAO: AccountDAO,
    entryDAO: EntryDAO
) : EntryAssistant(document, accountDAO, entryDAO) {

    private lateinit var amountField: JTextField
    private lateinit var accountCombo: JComboBox<Account>
    private lateinit var descriptionField: JTextField
    private lateinit var contraAccountCombo: JComboBox<Account>
    private lateinit var vatCombo: JComboBox<VatType>

    override fun buildUI(): JPanel {
        val panel = JPanel(GridBagLayout())

        panel.add(JLabel("Summa:"))
        amountField = JTextField(10)
        panel.add(amountField)

        panel.add(JLabel("Tili:"))
        accountCombo = JComboBox(accountDAO.findAll().toTypedArray())
        panel.add(accountCombo)

        panel.add(JLabel("Selite:"))
        descriptionField = JTextField(30)
        panel.add(descriptionField)

        panel.add(JLabel("Vastatili:"))
        contraAccountCombo = JComboBox(getBankAccounts().toTypedArray())
        panel.add(contraAccountCombo)

        panel.add(JLabel("ALV:"))
        vatCombo = JComboBox(VatType.values())
        panel.add(vatCombo)

        return panel
    }

    override fun createEntries(): List<Entry> {
        val amount = BigDecimal(amountField.text)
        val account = accountCombo.selectedItem as Account
        val description = descriptionField.text
        val contraAccount = contraAccountCombo.selectedItem as Account
        val vat = vatCombo.selectedItem as VatType

        // Create debit entry
        val debitEntry = Entry(
            documentId = document.id,
            accountId = account.id,
            debit = amount,
            credit = BigDecimal.ZERO,
            description = description
        )

        // Create credit entry (contra)
        val creditEntry = Entry(
            documentId = document.id,
            accountId = contraAccount.id,
            debit = BigDecimal.ZERO,
            credit = amount,
            description = description
        )

        return listOf(debitEntry, creditEntry)
    }

    override fun validate(): Boolean {
        if (amountField.text.isBlank()) {
            JOptionPane.showMessageDialog(null, "Summa puuttuu")
            return false
        }
        // ... more validation ...
        return true
    }

    override fun getTitle() = "Tulo/Meno-kirjaus"

    private fun getBankAccounts(): List<Account> {
        return accountDAO.findAll().filter { it.type == AccountType.BANK }
    }
}

// TransferAssistant.kt - Tilisiirto
class TransferAssistant(...) : EntryAssistant(...) {
    // Siirtää rahaa tilin A ja tilin B välillä
    // Luo automaattisesti molemmat kirjaukset
}
```

**Vaativuus:** ⭐⭐⭐ Keskitaso
**Hyöty:** ⭐⭐⭐⭐ Suuri
**Prioriteetti:** 2 (TÄRKEÄ)

---

## 4. PDF-käsittelyn parannukset (Prioriteetti 2)

### Miksi tärkeä?
Modernissa kirjanpidossa **lähes kaikki tositteet** ovat PDF-muodossa.

### Kitsas-toteutus:

Kitsaassa on edistynyt PDF-järjestelmä:
- **QtPdf** - Natiivi PDF-renderöinti
- **PDF-cache** - Nopeuttaa PDF:ien lataamista
- **PDF-tuonti** - OCR-tunnistus (Tesseract)
- **PDF-liitteet** - Tositteisiin liitettävät PDF:t

```cpp
// liite/pdfrenderview.h
class PdfRenderView : public QWidget {
    // QtPdf-pohjainen PDF-näyttö
    // Zoom, scroll, search
};

// tuonti/tesseracttuonti.h
class TesserActTuonti : public QObject {
    void tuo(const QByteArray& data);
signals:
    void tuotu(const QVariantMap& map); // Tunnistetut tiedot
};
```

### Suositus Tilittimelle:

**Tilittimessä jo:**
- iText PDF -kirjasto (5.5.13.4)
- PDF-generointi raporteille

**Parannettava:**

```kotlin
// PdfAttachment.kt
data class PdfAttachment(
    val id: Int,
    val documentId: Int,
    val filename: String,
    val data: ByteArray,
    val pageCount: Int,
    val createdDate: LocalDateTime
)

// PdfAttachmentDAO.kt
interface PdfAttachmentDAO {
    fun findByDocumentId(documentId: Int): List<PdfAttachment>
    fun save(attachment: PdfAttachment): Int
    fun delete(id: Int)
}

// PdfViewerPanel.kt - PDF-näyttö
class PdfViewerPanel : JPanel() {
    private val pdfRenderer: PDFRenderer
    private val currentPage = 0

    fun loadPdf(data: ByteArray) {
        val document = PDDocument.load(data)
        pdfRenderer = PDFRenderer(document)
        renderPage(0)
    }

    private fun renderPage(pageIndex: Int) {
        val image = pdfRenderer.renderImageWithDPI(pageIndex, 150f)
        // Display image in JLabel or custom component
    }

    fun nextPage() { renderPage(currentPage + 1) }
    fun previousPage() { renderPage(currentPage - 1) }
    fun zoom(factor: Double) { /* ... */ }
}
```

**Lisäkirjasto:**
```xml
<!-- pom.xml -->
<dependency>
    <groupId>org.apache.pdfbox</groupId>
    <artifactId>pdfbox</artifactId>
    <version>3.0.1</version>
</dependency>
```

**Vaativuus:** ⭐⭐⭐ Keskitaso
**Hyöty:** ⭐⭐⭐⭐ Suuri
**Prioriteetti:** 2 (TÄRKEÄ)

---

## 5. Raporttien parannus (Prioriteetti 2)

### Kitsas-toteutus:

Kitsaassa on 11 raportin laatijaa (`raportti/laatijat/`):
- LaalijanaAlv - ALV-raportti
- LaatijaKuukausiraportti - Kuukausiraportti
- LaatilanLaskut - Laskuraportti
- LaatilanMyynti - Myyntiraportti
- LaatilanPaakirja - Pääkirja
- LaatilanPaivakirja - Päiväkirja
- LaatilanTaseerittely - Tase-erittely
- LaatilanTasetulos - Tase ja tuloslaskelma
- LaatilanTilikartta - Tilikarttaraportti
- LaatilanTositeluettelo - Tositeluettelo

**Yhteinen arkkitehtuuri:**

```cpp
class LaatilanRaportti {
    virtual void kirjoita(const QDate& alkaa, const QDate& paattyy);
    void lisaaRivi(const RaporttiRivi& rivi);
    void lisaaSummaRivi(const Euro& summa);
    void lisaaTyhjapRivi();
};
```

### Suositus Tilittimelle:

**Tilittimessä jo:**
- Perusraportit (Tase, Tuloslaskelma, Päiväkirja, Pääkirja)

**Parannettava:**

```kotlin
// ReportGenerator.kt - Base class
abstract class ReportGenerator(
    protected val periodDAO: PeriodDAO,
    protected val accountDAO: AccountDAO,
    protected val entryDAO: EntryDAO
) {
    abstract fun generate(startDate: LocalDate, endDate: LocalDate): Report

    protected fun createReportRow(
        level: Int,
        label: String,
        amount: BigDecimal,
        bold: Boolean = false
    ): ReportRow {
        return ReportRow(level, label, amount, bold)
    }
}

// MonthlyReportGenerator.kt
class MonthlyReportGenerator(...) : ReportGenerator(...) {
    override fun generate(startDate: LocalDate, endDate: LocalDate): Report {
        val report = Report("Kuukausiraportti")

        // Group by month
        val months = generateMonths(startDate, endDate)

        months.forEach { month ->
            val entries = entryDAO.findByDateRange(
                month.atDay(1),
                month.atEndOfMonth()
            )

            val totalDebit = entries.sumOf { it.debit }
            val totalCredit = entries.sumOf { it.credit }

            report.addRow(0, month.toString(), totalDebit - totalCredit)
        }

        return report
    }
}

// CostCenterReportGenerator.kt
class CostCenterReportGenerator(...) : ReportGenerator(...) {
    // Kustannuspaikkaraportti
    // Ryhmittely kohdennuksittain
}
```

**Vaativuus:** ⭐⭐ Helppo-Keskitaso
**Hyöty:** ⭐⭐⭐⭐ Suuri
**Prioriteetti:** 2 (TÄRKEÄ)

---

## 6. Laskutusjärjestelmä (Prioriteetti 3-4)

### Miksi vaikea?
Kitsaassa on **66 header-tiedostoa** pelkästään laskutukseen. Tämä on **massiivinen** ominaisuus.

### Kitsas-laskutusmoduulit:

```
laskutus/
├── laskudlg/          (14 tiedostoa) - Laskudialogit
├── tulostus/          (8 tiedostoa)  - Laskun tulostus
├── toimittaja/        (6 tiedostoa)  - Laskun toimitus
├── ryhmalasku/        (5 tiedostoa)  - Massalaskutus
├── huoneisto/         (5 tiedostoa)  - Huoneistolaskutus
├── tuotetuonti/       (3 tiedostoa)  - Tuotteiden tuonti
└── vakioviite/        (2 tiedostoa)  - Vakioviitteet
```

**Lasku-luokka (150+ riviä):**
```cpp
class Lasku : public KantaVariantti {
    QString email();
    QString kieli();
    int maksutapa();
    int lahetystapa();
    QString numero();
    QString osoite();
    QString otsikko();
    QString saate();
    QDate toimituspvm();
    QDate jaksopvm();
    Euro summa();
    QString iban();
    ViiteNumero viite();
    // ... 50+ metodia lisää ...
};
```

### Suositus Tilittimelle:

**Prioriteetti 3:** Yksinkertainen laskutusjärjestelmä
- Perustiedot: asiakas, tuotteet, summa
- PDF-tulostus
- Viitenumeron generointi

**Prioriteetti 4:** Täysi laskutusjärjestelmä
- Verkkolaskut (Finvoice)
- Sähköpostilähetys
- Maksumuistutukset
- Hyvityslaskut
- Asiakasrekisteri

**Vaativuus:** ⭐⭐⭐⭐⭐ Erittäin vaikea
**Hyöty:** ⭐⭐⭐⭐⭐ Erittäin suuri (mutta vasta pitkällä aikavälillä)
**Prioriteetti:** 3-4 (VALINNAINEN, tulevaisuus)

**Arvio:** **500-1000 tuntia** täydellä laskutusjärjestelmällä.
**Suositus:** Aloita yksinkertaisesta versiosta (50-100 tuntia).

---

## 7. Lisäominaisuudet (Prioriteetti 3)

### 7.1 Harjoittelutila

**Kitsas:**
```cpp
bool onkoHarjoitus() const { return asetukset()->onko("Harjoitus"); }
QDate paivamaara() const {
    // Harjoittelutilassa voi muuttaa päivämäärää
}
```

**Tilitin:**
```kotlin
class Settings {
    var practiceMode: Boolean = false
    var practiceDate: LocalDate? = null

    fun getCurrentDate(): LocalDate {
        return if (practiceMode && practiceDate != null) {
            practiceDate!!
        } else {
            LocalDate.now()
        }
    }
}
```

**Vaativuus:** ⭐ Erittäin helppo
**Hyöty:** ⭐⭐⭐ Keskitaso
**Prioriteetti:** 3

### 7.2 Tositteen kierto (Workflow)

**Kitsas:**
```cpp
class KiertoModel {
    // Tositteiden hyväksymiskierto
    // Multi-user workflow
};
```

**Tilitin:**
Vaatii monikäyttäjätuen → Prioriteetti 4

---

## Yhteenveto ja Suositukset

### Priorisoitu toteutusjärjestys:

#### Vaihe 1: Perusparannukset (Sprint 2.3-2.4)
**Työmäärä:** ~2-3 viikkoa

1. **Validaattorit** (1 viikko)
   - IbanValidator
   - YTunnusValidator
   - ViiteValidator

2. **Harjoittelutila** (2 päivää)
   - Practice mode settings
   - Date override

#### Vaihe 2: Tuontitoiminnot (Sprint 2.5-2.6)
**Työmäärä:** ~3-4 viikkoa

3. **CSV-tuonti** (2-3 viikkoa)
   - Encoding detection
   - Delimiter detection
   - Column mapping UI
   - Data parsing

#### Vaihe 3: Käyttökokemus (Sprint 3.0)
**Työmäärä:** ~2-3 viikkoa

4. **Kirjausapurit** (2 viikkoa)
   - SimpleIncomeExpenseAssistant
   - TransferAssistant

5. **PDF-parannukset** (1 viikko)
   - PDF attachments
   - Better PDF viewer

#### Vaihe 4: Raportit (Sprint 3.1)
**Työmäärä:** ~1-2 viikkoa

6. **Lisäraportit** (1-2 viikkoa)
   - MonthlyReport
   - CostCenterReport

#### Vaihe 5: Tulevaisuus (v3.x)
**Työmäärä:** Kuukausia

7. **Yksinkertainen laskutus** (2-3 kuukautta)
   - Invoice data model
   - PDF generation
   - Reference number generation

8. **Täysi laskutus** (6-12 kuukautta)
   - E-invoicing
   - Email sending
   - Customer registry

---

## Koodiesimerkit GitHubissa

Kitsas-koodin tutkiminen:
```
c:\Github\Prod\kitupiikki\kitsas\
├── validator/     - Validaattorit (mallit)
├── tuonti/        - Tuontitoiminnot (CSV, Tesseract)
├── apuri/         - Kirjausapurit
├── liite/         - PDF-käsittely
├── raportti/      - Raporttijärjestelmä
└── laskutus/      - Laskutusjärjestelmä (MASSIIVINEN)
```

---

## Lopuksi

**Kriittisimmät ominaisuudet Tilittimelle:**

1. ✅ **Validaattorit** - Helppoja toteuttaa, suuri hyöty
2. ✅ **CSV-tuonti** - Kriittinen toiminto, kohtuullinen työmäärä
3. ✅ **Kirjausapurit** - Parantaa käyttökokemusta merkittävästi
4. ⚠️ **Laskutus** - Valtava työmäärä, harkittava tarkkaan

**Älä toteuta vielä:**
- Pilvipalvelu (vaatii palvelininfran)
- Monikäyttäjätuki (vaatii käyttöoikeusjärjestelmän)
- Verkkolaskut (vaatii laskutusjärjestelmän ensin)

**Seuraavat askeleet:**
1. Toteuta validaattorit (Sprint 2.3)
2. Lisää harjoittelutila (Sprint 2.3)
3. Suunnittele CSV-tuonti (Sprint 2.4)
4. Arvioi laskutuksen tarve käyttäjäpalautteen perusteella

---

**Tekijä:** Claude Sonnet 4.5 (AI-avusteinen analyysi)
**Päivitetty:** 2025-12-28
**Tila:** EHDOTUS - Odottaa hyväksyntää
