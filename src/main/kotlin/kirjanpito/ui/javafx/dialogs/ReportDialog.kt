package kirjanpito.ui.javafx.dialogs

import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.print.PrinterJob
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.layout.*
import javafx.scene.paint.Color
import javafx.scene.shape.SVGPath
import javafx.scene.web.WebView
import javafx.stage.FileChooser
import javafx.stage.Modality
import javafx.stage.Stage
import javafx.stage.Window
import kirjanpito.db.*
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.common.PDRectangle
import org.apache.pdfbox.pdmodel.font.PDType1Font
import org.apache.pdfbox.pdmodel.font.Standard14Fonts
import java.io.File
import java.io.PrintWriter
import java.math.BigDecimal
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

/**
 * Moderni JavaFX raporttidialogi WebView + HTML/CSS -pohjaisena.
 */
class ReportDialog private constructor(
    owner: Window?,
    private var reportType: ReportType,
    private val dataSource: DataSource,
    private var currentPeriod: Period,
    private val allPeriods: List<Period>,
    private val accounts: List<Account>,
    private val onOpenLedger: ((Account) -> Unit)? = null
) {
    enum class ReportType(val displayName: String) {
        JOURNAL("Päiväkirja"),
        LEDGER("Pääkirja"),
        INCOME_STATEMENT("Tuloslaskelma"),
        BALANCE_SHEET("Tase")
    }

    private val dialog = Stage()
    private val webView = WebView()
    private var htmlContent = ""
    private lateinit var periodCombo: ComboBox<Period>

    // Reference to period for report generation
    private val period: Period get() = currentPeriod

    init {
        dialog.initModality(Modality.APPLICATION_MODAL)
        owner?.let { dialog.initOwner(it) }
        dialog.title = reportType.displayName
        dialog.minWidth = 900.0
        dialog.minHeight = 700.0
        
        // Load app icon
        loadAppIcon()

        createContent()
        generateReport()
        setupAccountLinkHandler()
    }
    
    private fun loadAppIcon() {
        listOf("/tilitin-24x24.png", "/tilitin-32x32.png", "/tilitin-48x48.png").forEach { iconPath ->
            javaClass.getResourceAsStream(iconPath)?.let { stream ->
                dialog.icons.add(javafx.scene.image.Image(stream))
            }
        }
    }
    
    private fun showAlert(type: Alert.AlertType, header: String, content: String) {
        Alert(type).apply {
            title = "Tilitin"
            headerText = header
            contentText = content
            // Add icon to alert dialog
            (dialogPane.scene.window as? javafx.stage.Stage)?.let { stage ->
                listOf("/tilitin-24x24.png", "/tilitin-32x32.png", "/tilitin-48x48.png").forEach { iconPath ->
                    javaClass.getResourceAsStream(iconPath)?.let { stream ->
                        stage.icons.add(javafx.scene.image.Image(stream))
                    }
                }
            }
        }.showAndWait()
    }

    private fun setupAccountLinkHandler() {
        webView.engine.locationProperty().addListener { _, _, newLocation ->
            if (newLocation != null && newLocation.startsWith("account:")) {
                val accountNumber = newLocation.substringAfter("account:")
                val account = accounts.find { it.number == accountNumber }
                if (account != null && onOpenLedger != null) {
                    onOpenLedger.invoke(account)
                }
                // Prevent navigation
                webView.engine.loadContent(htmlContent)
            }
        }
    }

    private fun createContent() {
        val root = VBox(10.0).apply {
            padding = Insets(16.0)
            style = "-fx-background-color: #f5f5f5;"
        }

        // Modern toolbar
        val toolbar = HBox(10.0).apply {
            alignment = Pos.CENTER_LEFT
            padding = Insets(8.0, 12.0, 8.0, 12.0)
            style = """
                -fx-background-color: white;
                -fx-background-radius: 8;
                -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 4, 0, 0, 1);
            """.trimIndent()
        }

        val printBtn = Button("Tulosta").apply {
            style = BUTTON_STYLE
            setOnAction { print() }
        }

        val exportPdfBtn = Button("Vie PDF").apply {
            style = BUTTON_STYLE_SECONDARY
            setOnAction { exportToPdf() }
        }

        val exportHtmlBtn = Button("Vie HTML").apply {
            style = BUTTON_STYLE_SECONDARY
            setOnAction { exportToHtml() }
        }

        // Period selection
        val periodLabel = Label("Tilikausi:").apply {
            style = "-fx-text-fill: #495057; -fx-padding: 0 0 0 20;"
        }

        periodCombo = ComboBox<Period>().apply {
            items.addAll(allPeriods)
            
            // Use converter for both display and dropdown
            converter = object : javafx.util.StringConverter<Period>() {
                override fun toString(period: Period?): String {
                    return if (period == null) "" 
                           else "${DATE_FORMAT.format(period.startDate)} – ${DATE_FORMAT.format(period.endDate)}"
                }
                override fun fromString(string: String?): Period? = null
            }
            
            value = currentPeriod
            
            setOnAction {
                currentPeriod = value
                generateReport()
            }
        }

        val spacer = Region().also { HBox.setHgrow(it, Priority.ALWAYS) }

        val closeBtn = Button("Sulje").apply {
            style = """
                -fx-background-color: #6c757d;
                -fx-text-fill: white;
                -fx-padding: 8 16;
                -fx-background-radius: 4;
                -fx-cursor: hand;
            """.trimIndent()
            setOnAction { dialog.close() }
        }

        toolbar.children.addAll(printBtn, exportPdfBtn, exportHtmlBtn, periodLabel, periodCombo, spacer, closeBtn)

        // WebView for HTML report
        VBox.setVgrow(webView, Priority.ALWAYS)

        root.children.addAll(toolbar, webView)

        dialog.scene = Scene(root, 950.0, 750.0)
    }

    private fun generateReport() {
        htmlContent = buildString {
            append(HTML_HEAD)
            append("<body>")
            append(generateHeader())

            try {
                when (reportType) {
                    ReportType.JOURNAL -> append(generateJournal())
                    ReportType.LEDGER -> append(generateLedger())
                    ReportType.INCOME_STATEMENT -> append(generateIncomeStatement())
                    ReportType.BALANCE_SHEET -> append(generateBalanceSheet())
                }
            } catch (e: Exception) {
                append("<div class='error'>Virhe raportin generoinnissa: ${e.message}</div>")
            }

            append("</body></html>")
        }

        webView.engine.loadContent(htmlContent)
    }

    private fun generateHeader(): String = """
        <div class="header">
            <h1>${reportType.displayName}</h1>
            <div class="meta">
                <span>Tilikausi: ${DATE_FORMAT.format(period.startDate)} – ${DATE_FORMAT.format(period.endDate)}</span>
                <span>Tulostettu: ${DATE_FORMAT.format(Date())}</span>
            </div>
        </div>
    """.trimIndent()

    private fun generateJournal(): String = buildString {
        val session = dataSource.openSession()
        try {
            val docDao = dataSource.getDocumentDAO(session)
            val entryDao = dataSource.getEntryDAO(session)
            
            // DEBUG: Tulostetaan tilikauden tiedot
            println("📊 generateJournal - period.id=${period.id}")
            println("📊 period.startDate=${period.startDate}, period.endDate=${period.endDate}")
            
            val docs = docDao.getByPeriodId(period.id, 1)
            
            // DEBUG: Tulostetaan haettujen tositteiden tiedot
            println("📊 Haettiin ${docs.size} tositetta period_id=${period.id}")
            if (docs.isNotEmpty()) {
                val firstDoc = docs.first()
                val lastDoc = docs.last()
                println("📊 Ensimmäinen tosite: nro=${firstDoc.number}, pvm=${firstDoc.date}")
                println("📊 Viimeinen tosite: nro=${lastDoc.number}, pvm=${lastDoc.date}")
            }

            append("""
                <table class="report-table">
                    <thead>
                        <tr>
                            <th class="num">Nro</th>
                            <th class="date">Pvm</th>
                            <th class="account">Tili / Selite</th>
                            <th class="amount">Debet</th>
                            <th class="amount">Kredit</th>
                        </tr>
                    </thead>
                    <tbody>
            """.trimIndent())

            var totalDebit = BigDecimal.ZERO
            var totalCredit = BigDecimal.ZERO

            for (doc in docs) {
                val entries = entryDao.getByDocumentId(doc.id)
                var first = true

                for (entry in entries) {
                    val acc = accounts.find { it.id == entry.accountId }
                    val accLink = acc?.let { 
                        "<a href='account:${it.number}' class='account-link'>${it.number} ${it.name.escapeHtml()}</a>" 
                    } ?: ""

                    val (debitStr, creditStr) = if (entry.isDebit) {
                        totalDebit = totalDebit.add(entry.amount)
                        MONEY_FORMAT.format(entry.amount) to ""
                    } else {
                        totalCredit = totalCredit.add(entry.amount)
                        "" to MONEY_FORMAT.format(entry.amount)
                    }

                    if (first) {
                        append("""
                            <tr class="entry-row">
                                <td class="num">${doc.number}</td>
                                <td class="date">${DATE_FORMAT.format(doc.date)}</td>
                                <td class="account">$accLink</td>
                                <td class="amount debit">$debitStr</td>
                                <td class="amount credit">$creditStr</td>
                            </tr>
                        """.trimIndent())
                        first = false
                    } else {
                        append("""
                            <tr class="entry-row continuation">
                                <td></td>
                                <td></td>
                                <td class="account">$accLink</td>
                                <td class="amount debit">$debitStr</td>
                                <td class="amount credit">$creditStr</td>
                            </tr>
                        """.trimIndent())
                    }

                    if (!entry.description.isNullOrBlank()) {
                        append("""
                            <tr class="description-row">
                                <td colspan="2"></td>
                                <td class="description" colspan="3">${entry.description.escapeHtml()}</td>
                            </tr>
                        """.trimIndent())
                    }
                }
            }

            append("""
                    </tbody>
                    <tfoot>
                        <tr class="total-row">
                            <td colspan="3">YHTEENSÄ</td>
                            <td class="amount debit">${MONEY_FORMAT.format(totalDebit)}</td>
                            <td class="amount credit">${MONEY_FORMAT.format(totalCredit)}</td>
                        </tr>
                    </tfoot>
                </table>
            """.trimIndent())

        } finally {
            session.close()
        }
    }

    private fun generateLedger(): String = buildString {
        val session = dataSource.openSession()
        try {
            val entryDao = dataSource.getEntryDAO(session)
            val docDao = dataSource.getDocumentDAO(session)

            val byAccount = mutableMapOf<Int, MutableList<Entry>>()
            entryDao.getByPeriodId(period.id, EntryDAO.ORDER_BY_ACCOUNT_NUMBER_AND_DOCUMENT_NUMBER) { entry ->
                byAccount.getOrPut(entry.accountId) { mutableListOf() }.add(entry)
            }

            val docsById = docDao.getByPeriodId(period.id, 1).associateBy { it.id }

            for (acc in accounts) {
                val entries = byAccount[acc.id] ?: continue
                if (entries.isEmpty()) continue

                append("""
                    <div class="account-section">
                        <h3 class="account-header">${acc.number} ${acc.name.escapeHtml()}</h3>
                        <table class="report-table ledger-table">
                            <thead>
                                <tr>
                                    <th class="num">Nro</th>
                                    <th class="date">Pvm</th>
                                    <th class="description-col">Selite</th>
                                    <th class="amount">Debet</th>
                                    <th class="amount">Kredit</th>
                                </tr>
                            </thead>
                            <tbody>
                """.trimIndent())

                var balance = BigDecimal.ZERO

                for (entry in entries) {
                    val doc = docsById[entry.documentId]
                    val date = doc?.let { DATE_FORMAT.format(it.date) } ?: ""
                    val docNum = doc?.number ?: 0

                    val (debitStr, creditStr) = if (entry.isDebit) {
                        balance = balance.add(entry.amount)
                        MONEY_FORMAT.format(entry.amount) to ""
                    } else {
                        balance = balance.subtract(entry.amount)
                        "" to MONEY_FORMAT.format(entry.amount)
                    }

                    append("""
                        <tr>
                            <td class="num">$docNum</td>
                            <td class="date">$date</td>
                            <td class="description-col">${(entry.description ?: "").escapeHtml()}</td>
                            <td class="amount debit">$debitStr</td>
                            <td class="amount credit">$creditStr</td>
                        </tr>
                    """.trimIndent())
                }

                val balanceClass = if (balance >= BigDecimal.ZERO) "positive" else "negative"
                append("""
                            </tbody>
                            <tfoot>
                                <tr class="balance-row">
                                    <td colspan="3">Saldo:</td>
                                    <td colspan="2" class="balance $balanceClass">${MONEY_FORMAT.format(balance)}</td>
                                </tr>
                            </tfoot>
                        </table>
                    </div>
                """.trimIndent())
            }

        } finally {
            session.close()
        }
    }

    private fun generateIncomeStatement(): String = buildString {
        val balances = calculateBalances()

        // Income section
        append("""
            <div class="statement-section">
                <h3 class="section-header income">TULOT</h3>
                <table class="statement-table">
        """.trimIndent())

        var totalIncome = BigDecimal.ZERO
        for (acc in accounts) {
            if (acc.type == 3 && acc.number.startsWith("3")) {
                val bal = (balances[acc.id] ?: BigDecimal.ZERO).negate()
                if (bal.compareTo(BigDecimal.ZERO) != 0) {
                    append("""
                        <tr>
                            <td class="account-name">${acc.number} ${acc.name.escapeHtml()}</td>
                            <td class="amount">${MONEY_FORMAT.format(bal)}</td>
                        </tr>
                    """.trimIndent())
                    totalIncome = totalIncome.add(bal)
                }
            }
        }

        append("""
                    <tr class="subtotal">
                        <td>TULOT YHTEENSÄ</td>
                        <td class="amount">${MONEY_FORMAT.format(totalIncome)}</td>
                    </tr>
                </table>
            </div>
        """.trimIndent())

        // Expenses section
        append("""
            <div class="statement-section">
                <h3 class="section-header expense">MENOT</h3>
                <table class="statement-table">
        """.trimIndent())

        var totalExpenses = BigDecimal.ZERO
        for (acc in accounts) {
            if (acc.type == 3 && !acc.number.startsWith("3")) {
                val bal = balances[acc.id] ?: BigDecimal.ZERO
                if (bal.compareTo(BigDecimal.ZERO) != 0) {
                    append("""
                        <tr>
                            <td class="account-name">${acc.number} ${acc.name.escapeHtml()}</td>
                            <td class="amount">${MONEY_FORMAT.format(bal)}</td>
                        </tr>
                    """.trimIndent())
                    totalExpenses = totalExpenses.add(bal)
                }
            }
        }

        append("""
                    <tr class="subtotal">
                        <td>MENOT YHTEENSÄ</td>
                        <td class="amount">${MONEY_FORMAT.format(totalExpenses)}</td>
                    </tr>
                </table>
            </div>
        """.trimIndent())

        // Result
        val result = totalIncome.subtract(totalExpenses)
        val resultClass = if (result >= BigDecimal.ZERO) "profit" else "loss"
        val resultLabel = if (result >= BigDecimal.ZERO) "TILIKAUDEN VOITTO" else "TILIKAUDEN TAPPIO"

        append("""
            <div class="result-section $resultClass">
                <span class="result-label">$resultLabel</span>
                <span class="result-amount">${MONEY_FORMAT.format(result.abs())}</span>
            </div>
        """.trimIndent())
    }

    private fun generateBalanceSheet(): String = buildString {
        val balances = calculateBalances()

        // Assets
        append("""
            <div class="balance-sheet-section">
                <h3 class="section-header assets">VASTAAVAA (Aktiva)</h3>
                <table class="statement-table">
        """.trimIndent())

        var totalAssets = BigDecimal.ZERO
        for (acc in accounts) {
            if (acc.type == 1) {
                val bal = balances[acc.id] ?: BigDecimal.ZERO
                if (bal.compareTo(BigDecimal.ZERO) != 0) {
                    append("""
                        <tr>
                            <td class="account-name">${acc.number} ${acc.name.escapeHtml()}</td>
                            <td class="amount">${MONEY_FORMAT.format(bal)}</td>
                        </tr>
                    """.trimIndent())
                    totalAssets = totalAssets.add(bal)
                }
            }
        }

        append("""
                    <tr class="subtotal">
                        <td>VASTAAVAA YHTEENSÄ</td>
                        <td class="amount">${MONEY_FORMAT.format(totalAssets)}</td>
                    </tr>
                </table>
            </div>
        """.trimIndent())

        // Liabilities & Equity
        append("""
            <div class="balance-sheet-section">
                <h3 class="section-header liabilities">VASTATTAVAA (Passiva)</h3>
                <table class="statement-table">
        """.trimIndent())

        var totalLiabilities = BigDecimal.ZERO
        for (acc in accounts) {
            if (acc.type == 2 || acc.type == 4) {
                val bal = (balances[acc.id] ?: BigDecimal.ZERO).negate()
                if (bal.compareTo(BigDecimal.ZERO) != 0) {
                    append("""
                        <tr>
                            <td class="account-name">${acc.number} ${acc.name.escapeHtml()}</td>
                            <td class="amount">${MONEY_FORMAT.format(bal)}</td>
                        </tr>
                    """.trimIndent())
                    totalLiabilities = totalLiabilities.add(bal)
                }
            }
        }

        append("""
                    <tr class="subtotal">
                        <td>VASTATTAVAA YHTEENSÄ</td>
                        <td class="amount">${MONEY_FORMAT.format(totalLiabilities)}</td>
                    </tr>
                </table>
            </div>
        """.trimIndent())

        // Balance check
        val diff = totalAssets.subtract(totalLiabilities)
        if (diff.abs().compareTo(BigDecimal("0.01")) > 0) {
            append("""
                <div class="warning">
                    ⚠️ Tase ei täsmää! Erotus: ${MONEY_FORMAT.format(diff)}
                </div>
            """.trimIndent())
        }
    }

    private fun calculateBalances(): Map<Int, BigDecimal> {
        val balances = mutableMapOf<Int, BigDecimal>()
        val session = dataSource.openSession()
        try {
            val entryDao = dataSource.getEntryDAO(session)
            entryDao.getByPeriodId(period.id, EntryDAO.ORDER_BY_ACCOUNT_NUMBER_AND_DOCUMENT_NUMBER) { entry ->
                val current = balances.getOrDefault(entry.accountId, BigDecimal.ZERO)
                balances[entry.accountId] = if (entry.isDebit) {
                    current.add(entry.amount)
                } else {
                    current.subtract(entry.amount)
                }
            }
        } finally {
            session.close()
        }
        return balances
    }

    private fun print() {
        val job = PrinterJob.createPrinterJob()
        if (job != null && job.showPrintDialog(dialog)) {
            webView.engine.print(job)
            job.endJob()
        }
    }

    private fun exportToPdf() {
        val fc = FileChooser().apply {
            title = "Vie PDF"
            extensionFilters.add(FileChooser.ExtensionFilter("PDF-tiedosto", "*.pdf"))
            initialFileName = "${reportType.displayName.lowercase().replace(" ", "_")}.pdf"
        }

        fc.showSaveDialog(dialog)?.let { file ->
            try {
                generatePdf(file)
                showAlert(Alert.AlertType.INFORMATION, "PDF tallennettu", file.name)
            } catch (e: Exception) {
                showAlert(Alert.AlertType.ERROR, "Virhe PDF:n luonnissa", e.message ?: "Tuntematon virhe")
            }
        }
    }

    private fun generatePdf(file: File) {
        PDDocument().use { document ->
            val normalFont = PDType1Font(Standard14Fonts.FontName.HELVETICA)
            val boldFont = PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD)
            
            var page = PDPage(PDRectangle.A4)
            document.addPage(page)
            var contentStream = PDPageContentStream(document, page)
            
            val pageWidth = PDRectangle.A4.width
            val pageHeight = PDRectangle.A4.height
            val margin = 50f
            var y = pageHeight - margin
            val lineHeight = 14f
            
            fun newPage() {
                contentStream.close()
                page = PDPage(PDRectangle.A4)
                document.addPage(page)
                contentStream = PDPageContentStream(document, page)
                y = pageHeight - margin
            }
            
            fun drawText(text: String, x: Float, font: PDType1Font = normalFont, size: Float = 10f) {
                if (y < margin + 50) newPage()
                contentStream.beginText()
                contentStream.setFont(font, size)
                contentStream.newLineAtOffset(x, y)
                contentStream.showText(text)
                contentStream.endText()
            }
            
            fun drawLine() {
                if (y < margin + 50) newPage()
                contentStream.setLineWidth(0.5f)
                contentStream.moveTo(margin, y)
                contentStream.lineTo(pageWidth - margin, y)
                contentStream.stroke()
                y -= 5f
            }
            
            // Header
            drawText(reportType.displayName, pageWidth / 2 - 40, boldFont, 18f)
            y -= lineHeight * 2
            
            val periodText = "Tilikausi: ${DATE_FORMAT.format(period.startDate)} – ${DATE_FORMAT.format(period.endDate)}"
            drawText(periodText, margin, normalFont, 9f)
            y -= lineHeight
            
            drawText("Tulostettu: ${DATE_FORMAT.format(java.util.Date())}", margin, normalFont, 9f)
            y -= lineHeight * 2
            
            drawLine()
            y -= lineHeight
            
            // Generate content based on report type
            when (reportType) {
                ReportType.JOURNAL -> generateJournalPdf(contentStream, normalFont, boldFont, margin, pageWidth, lineHeight) { newPage(); y = pageHeight - margin }
                ReportType.LEDGER -> generateLedgerPdf(contentStream, normalFont, boldFont, margin, pageWidth, lineHeight) { newPage(); y = pageHeight - margin }
                ReportType.INCOME_STATEMENT -> generateIncomeStatementPdf(contentStream, normalFont, boldFont, margin, pageWidth, lineHeight) { newPage(); y = pageHeight - margin }
                ReportType.BALANCE_SHEET -> generateBalanceSheetPdf(contentStream, normalFont, boldFont, margin, pageWidth, lineHeight) { newPage(); y = pageHeight - margin }
            }
            
            contentStream.close()
            document.save(file)
        }
    }
    
    private fun generateJournalPdf(
        cs: PDPageContentStream, 
        normalFont: PDType1Font, 
        boldFont: PDType1Font,
        margin: Float,
        pageWidth: Float,
        lineHeight: Float,
        newPage: () -> Unit
    ) {
        val session = dataSource.openSession()
        try {
            val docDao = dataSource.getDocumentDAO(session)
            val entryDao = dataSource.getEntryDAO(session)
            val docs = docDao.getByPeriodId(period.id, 1)
            
            var y = PDRectangle.A4.height - margin - 80f
            
            // Column headers
            cs.beginText()
            cs.setFont(boldFont, 9f)
            cs.newLineAtOffset(margin, y)
            cs.showText("Nro")
            cs.endText()
            
            cs.beginText()
            cs.setFont(boldFont, 9f)
            cs.newLineAtOffset(margin + 40, y)
            cs.showText("Pvm")
            cs.endText()
            
            cs.beginText()
            cs.setFont(boldFont, 9f)
            cs.newLineAtOffset(margin + 110, y)
            cs.showText("Tili / Selite")
            cs.endText()
            
            cs.beginText()
            cs.setFont(boldFont, 9f)
            cs.newLineAtOffset(pageWidth - margin - 120, y)
            cs.showText("Debet")
            cs.endText()
            
            cs.beginText()
            cs.setFont(boldFont, 9f)
            cs.newLineAtOffset(pageWidth - margin - 50, y)
            cs.showText("Kredit")
            cs.endText()
            
            y -= lineHeight * 1.5f
            
            var totalDebit = BigDecimal.ZERO
            var totalCredit = BigDecimal.ZERO
            
            for (doc in docs) {
                val entries = entryDao.getByDocumentId(doc.id)
                var first = true
                
                for (entry in entries) {
                    val acc = accounts.find { it.id == entry.accountId }
                    val accName = acc?.let { "${it.number} ${it.name}".take(35) } ?: ""
                    
                    cs.beginText()
                    cs.setFont(normalFont, 9f)
                    cs.newLineAtOffset(margin, y)
                    cs.showText(if (first) doc.number.toString() else "")
                    cs.endText()
                    
                    cs.beginText()
                    cs.newLineAtOffset(margin + 40, y)
                    cs.showText(if (first) DATE_FORMAT.format(doc.date) else "")
                    cs.endText()
                    
                    cs.beginText()
                    cs.newLineAtOffset(margin + 110, y)
                    cs.showText(accName)
                    cs.endText()
                    
                    if (entry.isDebit) {
                        totalDebit = totalDebit.add(entry.amount)
                        cs.beginText()
                        cs.newLineAtOffset(pageWidth - margin - 120, y)
                        cs.showText(MONEY_FORMAT.format(entry.amount))
                        cs.endText()
                    } else {
                        totalCredit = totalCredit.add(entry.amount)
                        cs.beginText()
                        cs.newLineAtOffset(pageWidth - margin - 50, y)
                        cs.showText(MONEY_FORMAT.format(entry.amount))
                        cs.endText()
                    }
                    
                    y -= lineHeight
                    first = false
                }
                y -= lineHeight / 2
            }
            
            // Total
            y -= lineHeight
            cs.setLineWidth(0.5f)
            cs.moveTo(margin, y + lineHeight)
            cs.lineTo(pageWidth - margin, y + lineHeight)
            cs.stroke()
            
            cs.beginText()
            cs.setFont(boldFont, 9f)
            cs.newLineAtOffset(margin + 110, y)
            cs.showText("YHTEENSÄ")
            cs.endText()
            
            cs.beginText()
            cs.newLineAtOffset(pageWidth - margin - 120, y)
            cs.showText(MONEY_FORMAT.format(totalDebit))
            cs.endText()
            
            cs.beginText()
            cs.newLineAtOffset(pageWidth - margin - 50, y)
            cs.showText(MONEY_FORMAT.format(totalCredit))
            cs.endText()
            
        } finally {
            session.close()
        }
    }
    
    private fun generateLedgerPdf(cs: PDPageContentStream, normalFont: PDType1Font, boldFont: PDType1Font, margin: Float, pageWidth: Float, lineHeight: Float, newPage: () -> Unit) {
        // Simplified - just show message for now
        cs.beginText()
        cs.setFont(normalFont, 10f)
        cs.newLineAtOffset(margin, PDRectangle.A4.height - margin - 80f)
        cs.showText("Pääkirja - katso HTML-vienti täydelliselle raportille")
        cs.endText()
    }
    
    private fun generateIncomeStatementPdf(cs: PDPageContentStream, normalFont: PDType1Font, boldFont: PDType1Font, margin: Float, pageWidth: Float, lineHeight: Float, newPage: () -> Unit) {
        cs.beginText()
        cs.setFont(normalFont, 10f)
        cs.newLineAtOffset(margin, PDRectangle.A4.height - margin - 80f)
        cs.showText("Tuloslaskelma - katso HTML-vienti täydelliselle raportille")
        cs.endText()
    }
    
    private fun generateBalanceSheetPdf(cs: PDPageContentStream, normalFont: PDType1Font, boldFont: PDType1Font, margin: Float, pageWidth: Float, lineHeight: Float, newPage: () -> Unit) {
        cs.beginText()
        cs.setFont(normalFont, 10f)
        cs.newLineAtOffset(margin, PDRectangle.A4.height - margin - 80f)
        cs.showText("Tase - katso HTML-vienti täydelliselle raportille")
        cs.endText()
    }

    private fun exportToHtml() {
        val fc = FileChooser().apply {
            title = "Vie HTML"
            extensionFilters.add(FileChooser.ExtensionFilter("HTML-tiedosto", "*.html"))
            initialFileName = "${reportType.displayName.lowercase().replace(" ", "_")}.html"
        }

        fc.showSaveDialog(dialog)?.let { file ->
            try {
                file.writeText(htmlContent, Charsets.UTF_8)
                showAlert(Alert.AlertType.INFORMATION, "HTML-raportti tallennettu", file.name)
            } catch (e: Exception) {
                showAlert(Alert.AlertType.ERROR, "Virhe HTML:n tallennuksessa", e.message ?: "Tuntematon virhe")
            }
        }
    }

    fun show() {
        dialog.showAndWait()
    }

    private fun String.escapeHtml(): String = this
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")

    companion object {
        private val MONEY_FORMAT = DecimalFormat("#,##0.00")
        private val DATE_FORMAT = SimpleDateFormat("dd.MM.yyyy")

        private const val BUTTON_STYLE = """
            -fx-background-color: #0d6efd;
            -fx-text-fill: white;
            -fx-padding: 8 16;
            -fx-background-radius: 4;
            -fx-cursor: hand;
        """
        
        private const val BUTTON_STYLE_SECONDARY = """
            -fx-background-color: #6c757d;
            -fx-text-fill: white;
            -fx-padding: 8 16;
            -fx-background-radius: 4;
            -fx-cursor: hand;
        """

        private val HTML_HEAD = """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    * { box-sizing: border-box; }
                    
                    body {
                        font-family: 'Segoe UI', system-ui, -apple-system, sans-serif;
                        font-size: 13px;
                        line-height: 1.5;
                        color: #212529;
                        background: #fff;
                        margin: 0;
                        padding: 24px;
                    }
                    
                    .header {
                        text-align: center;
                        margin-bottom: 32px;
                        padding-bottom: 16px;
                        border-bottom: 2px solid #0d6efd;
                    }
                    
                    .header h1 {
                        margin: 0 0 8px 0;
                        font-size: 28px;
                        font-weight: 600;
                        color: #0d6efd;
                    }
                    
                    .header .meta {
                        color: #6c757d;
                        font-size: 12px;
                    }
                    
                    .header .meta span {
                        margin: 0 12px;
                    }
                    
                    .report-table {
                        width: 100%;
                        border-collapse: collapse;
                        margin-bottom: 24px;
                    }
                    
                    .report-table th {
                        background: #f8f9fa;
                        padding: 12px 8px;
                        text-align: left;
                        font-weight: 600;
                        border-bottom: 2px solid #dee2e6;
                        color: #495057;
                    }
                    
                    .report-table td {
                        padding: 8px;
                        border-bottom: 1px solid #e9ecef;
                    }
                    
                    .report-table .num { width: 60px; }
                    .report-table .date { width: 90px; }
                    .report-table .amount { 
                        width: 100px; 
                        text-align: right;
                        font-family: 'Consolas', monospace;
                    }
                    
                    .report-table .debit { color: #dc3545; }
                    .report-table .credit { color: #198754; }
                    
                    .entry-row { transition: background-color 0.15s ease; }
                    .entry-row:hover { background: #e7f1ff !important; cursor: pointer; }
                    .entry-row.continuation td { color: #6c757d; }
                    
                    /* Account links */
                    .account-link {
                        color: #0d6efd;
                        text-decoration: none;
                        border-bottom: 1px dashed #0d6efd;
                        transition: all 0.15s ease;
                    }
                    .account-link:hover {
                        color: #0a58ca;
                        border-bottom-style: solid;
                    }
                    
                    /* Statement table hover */
                    .statement-table tr { transition: background-color 0.15s ease; }
                    .statement-table tr:hover:not(.subtotal) { background: #f0f7ff; }
                    
                    .description-row td {
                        padding-top: 0;
                        font-style: italic;
                        color: #6c757d;
                        font-size: 12px;
                    }
                    
                    .total-row {
                        background: #e7f1ff;
                        font-weight: 600;
                    }
                    
                    .total-row td {
                        padding: 12px 8px;
                        border-top: 2px solid #0d6efd;
                    }
                    
                    /* Ledger specific */
                    .account-section {
                        margin-bottom: 32px;
                        page-break-inside: avoid;
                    }
                    
                    .account-header {
                        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                        color: white;
                        padding: 12px 16px;
                        margin: 0 0 0 0;
                        border-radius: 8px 8px 0 0;
                        font-size: 14px;
                    }
                    
                    .ledger-table {
                        border: 1px solid #dee2e6;
                        border-top: none;
                        border-radius: 0 0 8px 8px;
                    }
                    
                    .balance-row {
                        background: #f8f9fa;
                        font-weight: 600;
                    }
                    
                    .balance {
                        text-align: right !important;
                        font-family: 'Consolas', monospace;
                    }
                    
                    .balance.positive { color: #198754; }
                    .balance.negative { color: #dc3545; }
                    
                    /* Statement specific */
                    .statement-section {
                        margin-bottom: 24px;
                    }
                    
                    .section-header {
                        padding: 10px 16px;
                        margin: 0;
                        font-size: 14px;
                        border-radius: 6px 6px 0 0;
                    }
                    
                    .section-header.income { background: #d1e7dd; color: #0f5132; }
                    .section-header.expense { background: #f8d7da; color: #842029; }
                    .section-header.assets { background: #cfe2ff; color: #084298; }
                    .section-header.liabilities { background: #e2e3e5; color: #41464b; }
                    
                    .statement-table {
                        width: 100%;
                        border-collapse: collapse;
                        border: 1px solid #dee2e6;
                        border-top: none;
                    }
                    
                    .statement-table td {
                        padding: 8px 16px;
                        border-bottom: 1px solid #e9ecef;
                    }
                    
                    .statement-table .account-name { width: 70%; }
                    .statement-table .amount { 
                        text-align: right; 
                        font-family: 'Consolas', monospace;
                    }
                    
                    .statement-table .subtotal {
                        background: #f8f9fa;
                        font-weight: 600;
                        border-top: 2px solid #dee2e6;
                    }
                    
                    .result-section {
                        display: flex;
                        justify-content: space-between;
                        align-items: center;
                        padding: 16px 24px;
                        margin-top: 24px;
                        border-radius: 8px;
                        font-size: 18px;
                        font-weight: 600;
                    }
                    
                    .result-section.profit {
                        background: linear-gradient(135deg, #198754 0%, #20c997 100%);
                        color: white;
                    }
                    
                    .result-section.loss {
                        background: linear-gradient(135deg, #dc3545 0%, #fd7e14 100%);
                        color: white;
                    }
                    
                    .result-amount {
                        font-family: 'Consolas', monospace;
                        font-size: 24px;
                    }
                    
                    .warning {
                        background: #fff3cd;
                        color: #856404;
                        padding: 12px 16px;
                        border-radius: 6px;
                        margin-top: 16px;
                    }
                    
                    .error {
                        background: #f8d7da;
                        color: #842029;
                        padding: 16px;
                        border-radius: 6px;
                    }
                    
                    /* Print styles */
                    @media print {
                        body { padding: 0; }
                        .account-section { page-break-inside: avoid; }
                    }
                </style>
            </head>
        """.trimIndent()

        /**
         * Factory method for creating the dialog.
         */
        fun create(
            owner: Window?,
            type: ReportType,
            dataSource: DataSource,
            period: Period,
            accounts: List<Account>,
            allPeriods: List<Period> = listOf(period),
            onOpenLedger: ((Account) -> Unit)? = null
        ): ReportDialog {
            return ReportDialog(owner, type, dataSource, period, allPeriods, accounts, onOpenLedger)
        }
    }
}
