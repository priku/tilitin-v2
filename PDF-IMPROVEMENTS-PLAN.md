# PDF-parannukset Tilittimelle - Toteutussuunnitelma

**Päivitetty:** 2025-12-28
**Tarkoitus:** Yksityiskohtainen suunnitelma PDF-toimintojen parantamiseksi
**Tila:** SUUNNITTELU - EI TOTEUTETA VIELÄ

---

## Tiivistelmä

Tämä dokumentti määrittelee PDF-parannukset Tilittimelle, jotka perustuvat Kitsas-järjestelmän analyysiin. Suunnitelma sisältää:

1. **PDF-liitteet tositteisiin** - Tallenna PDF:iä tietokantaan
2. **Parannettu PDF-viewer** - Moderni PDF-näyttö (zoom, scroll, navigate)
3. **PDF-cache** - Nopeuttaa PDF:ien lataamista
4. **Tuonti-ominaisuudet** - Drag & drop, leikepöytä

---

## Nykyinen Tilanne

### Tilitin 2.1 - Nykytila

**PDF-kirjasto:** iText PDF 5.5.13.4

**Nykyiset ominaisuudet:**
- ✅ PDF-raporttien generointi (`PDFCanvas.java`)
- ✅ Tase, tuloslaskelma, päiväkirja, pääkirja PDF:ksi
- ❌ Ei PDF-liitteitä tositteisiin
- ❌ Ei PDF-vieweriä
- ❌ Ei PDF-tuontia

**PDFCanvas.java - Analyysi:**
```java
public class PDFCanvas implements PrintCanvas {
    private PdfContentByte cb;
    private BaseFont normalFont, boldFont, italicFont;

    // Vain PDF-GENEROINTI raporteille
    // EI PDF-KATSELUA tai LIITTEIDEN HALLINTAA
}
```

### Kitsas - Vertailukohta

**PDF-kirjasto:** QtPdf (Qt 6.4+)

**Ominaisuudet:**
- ✅ PDF-liitteet tositteisiin (`Liite`, `LiitteetModel`)
- ✅ PDF-viewer (`PdfRenderView`, QtPdfView)
- ✅ PDF-cache (80 MB muistivaraus, `LiiteCache`)
- ✅ Drag & drop PDF:ien lisääminen
- ✅ OCR-tunnistus (Tesseract)
- ✅ Thumbnail-generaattorit

**Arkkitehtuuri:**

```
Liite (domain object)
  ↓
LiitteetModel (QAbstractListModel)
  ↓
LiiteCache (muistinvaraus, LRU cache)
  ↓
PdfRenderView (QtPdfDocument + QtPdfView)
```

---

## Tavoitteet ja Prioriteetit

### Vaihe 1: PDF-liitteet (Prioriteetti 1)
**Työmäärä:** 1-2 viikkoa
**Hyöty:** ⭐⭐⭐⭐⭐ Kriittinen

- Tallenna PDF:iä tositteisiin (BLOB-kenttä tietokannassa)
- Listaa tositteiden liitteet
- Poista liitteitä
- Lataa ja tallenna PDF:iä

### Vaihe 2: PDF-viewer (Prioriteetti 1)
**Työmäärä:** 1 viikko
**Hyöty:** ⭐⭐⭐⭐ Tärkeä

- Näytä PDF:iä Swing-komponentissa (Apache PDFBox)
- Zoom in/out
- Navigoi sivujen välillä
- Scroll-tuki

### Vaihe 3: PDF-cache (Prioriteetti 2)
**Työmäärä:** 3-5 päivää
**Hyöty:** ⭐⭐⭐ Hyödyllinen

- Muistissa oleva PDF-cache (LRU)
- Nopeuttaa toistuvaa PDF:ien latausta
- Konfiguroitava max-koko

### Vaihe 4: Tuonti-ominaisuudet (Prioriteetti 2)
**Työmäärä:** 3-5 päivää
**Hyöty:** ⭐⭐⭐⭐ Tärkeä

- Drag & drop PDF:ien lisääminen
- Leikepöytä-tuki (Ctrl+V)
- Tiedostovalitsin (File chooser)

---

## Tekninen Suunnittelu

### 1. Tietokantamuutokset

#### Uusi taulu: `attachments`

```sql
CREATE TABLE IF NOT EXISTS attachments (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    document_id INTEGER NOT NULL,
    filename VARCHAR(255) NOT NULL,
    content_type VARCHAR(100) DEFAULT 'application/pdf',
    data BLOB NOT NULL,
    file_size INTEGER NOT NULL,
    page_count INTEGER,
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    description TEXT,
    FOREIGN KEY (document_id) REFERENCES documents(id) ON DELETE CASCADE
);

CREATE INDEX idx_attachments_document_id ON attachments(document_id);
```

**Kentät:**
- `id` - Liitteen uniikki tunniste
- `document_id` - Viittaus tositteeseen
- `filename` - Alkuperäinen tiedostonimi
- `content_type` - MIME-tyyppi (application/pdf)
- `data` - PDF-data (BLOB, voi olla iso!)
- `file_size` - Tiedoston koko tavuina
- `page_count` - Sivumäärä (lasketaan lisäyksessä)
- `created_date` - Lisäyspäivämäärä
- `description` - Valinnainen kuvaus

#### Huomiot:

**BLOB-koon rajoitukset:**
- SQLite: BLOB max 1-2 GB (riippuu build-asetuksista)
- MySQL: MEDIUMBLOB (16 MB) tai LONGBLOB (4 GB)
- PostgreSQL: BYTEA (ei rajoitusta)

**Suositus:**
- Rajoita liitteen max-koko 10 MB per PDF
- Näytä varoitus, jos PDF > 5 MB

### 2. Domain Model - Kotlin Data Class

```kotlin
// Attachment.kt
package kirjanpito.models

import java.time.LocalDateTime

data class Attachment(
    val id: Int = 0,
    val documentId: Int,
    val filename: String,
    val contentType: String = "application/pdf",
    val data: ByteArray,
    val fileSize: Int = data.size,
    val pageCount: Int? = null,
    val createdDate: LocalDateTime = LocalDateTime.now(),
    val description: String? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Attachment

        if (id != other.id) return false
        if (documentId != other.documentId) return false
        if (filename != other.filename) return false
        if (!data.contentEquals(other.data)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + documentId
        result = 31 * result + filename.hashCode()
        result = 31 * result + data.contentHashCode()
        return result
    }

    companion object {
        const val MAX_FILE_SIZE = 10 * 1024 * 1024 // 10 MB
        const val WARNING_FILE_SIZE = 5 * 1024 * 1024 // 5 MB

        fun fromFile(
            documentId: Int,
            filename: String,
            data: ByteArray,
            description: String? = null
        ): Attachment {
            require(data.isNotEmpty()) { "PDF data cannot be empty" }
            require(data.size <= MAX_FILE_SIZE) {
                "PDF file too large: ${data.size / 1024 / 1024} MB (max: 10 MB)"
            }

            return Attachment(
                documentId = documentId,
                filename = filename,
                data = data,
                description = description
            )
        }
    }
}
```

### 3. DAO Layer

```kotlin
// AttachmentDAO.kt
package kirjanpito.db

import kirjanpito.models.Attachment
import java.sql.Connection

interface AttachmentDAO {
    fun findById(id: Int): Attachment?
    fun findByDocumentId(documentId: Int): List<Attachment>
    fun save(attachment: Attachment): Int
    fun delete(id: Int): Boolean
    fun countByDocumentId(documentId: Int): Int
    fun getTotalSize(documentId: Int): Long
}

// SQLiteAttachmentDAO.kt
class SQLiteAttachmentDAO(private val dataSource: DataSource) : AttachmentDAO {

    override fun findById(id: Int): Attachment? {
        val sql = """
            SELECT id, document_id, filename, content_type, data,
                   file_size, page_count, created_date, description
            FROM attachments
            WHERE id = ?
        """

        dataSource.connection.use { conn ->
            conn.prepareStatement(sql).use { stmt ->
                stmt.setInt(1, id)
                stmt.executeQuery().use { rs ->
                    if (rs.next()) {
                        return Attachment(
                            id = rs.getInt("id"),
                            documentId = rs.getInt("document_id"),
                            filename = rs.getString("filename"),
                            contentType = rs.getString("content_type"),
                            data = rs.getBytes("data"),
                            fileSize = rs.getInt("file_size"),
                            pageCount = rs.getInt("page_count").takeIf { !rs.wasNull() },
                            createdDate = rs.getTimestamp("created_date").toLocalDateTime(),
                            description = rs.getString("description")
                        )
                    }
                    return null
                }
            }
        }
    }

    override fun findByDocumentId(documentId: Int): List<Attachment> {
        val sql = """
            SELECT id, document_id, filename, content_type, data,
                   file_size, page_count, created_date, description
            FROM attachments
            WHERE document_id = ?
            ORDER BY created_date ASC
        """

        val attachments = mutableListOf<Attachment>()

        dataSource.connection.use { conn ->
            conn.prepareStatement(sql).use { stmt ->
                stmt.setInt(1, documentId)
                stmt.executeQuery().use { rs ->
                    while (rs.next()) {
                        attachments.add(
                            Attachment(
                                id = rs.getInt("id"),
                                documentId = rs.getInt("document_id"),
                                filename = rs.getString("filename"),
                                contentType = rs.getString("content_type"),
                                data = rs.getBytes("data"),
                                fileSize = rs.getInt("file_size"),
                                pageCount = rs.getInt("page_count").takeIf { !rs.wasNull() },
                                createdDate = rs.getTimestamp("created_date").toLocalDateTime(),
                                description = rs.getString("description")
                            )
                        )
                    }
                }
            }
        }

        return attachments
    }

    override fun save(attachment: Attachment): Int {
        val pageCount = calculatePageCount(attachment.data)

        val sql = if (attachment.id == 0) {
            // Insert
            """
            INSERT INTO attachments
            (document_id, filename, content_type, data, file_size, page_count, description)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """
        } else {
            // Update
            """
            UPDATE attachments
            SET document_id = ?, filename = ?, content_type = ?,
                data = ?, file_size = ?, page_count = ?, description = ?
            WHERE id = ?
            """
        }

        dataSource.connection.use { conn ->
            conn.prepareStatement(sql, Connection.RETURN_GENERATED_KEYS).use { stmt ->
                stmt.setInt(1, attachment.documentId)
                stmt.setString(2, attachment.filename)
                stmt.setString(3, attachment.contentType)
                stmt.setBytes(4, attachment.data)
                stmt.setInt(5, attachment.fileSize)
                if (pageCount != null) {
                    stmt.setInt(6, pageCount)
                } else {
                    stmt.setNull(6, java.sql.Types.INTEGER)
                }
                stmt.setString(7, attachment.description)

                if (attachment.id != 0) {
                    stmt.setInt(8, attachment.id)
                }

                stmt.executeUpdate()

                if (attachment.id == 0) {
                    stmt.generatedKeys.use { rs ->
                        if (rs.next()) {
                            return rs.getInt(1)
                        }
                    }
                }

                return attachment.id
            }
        }
    }

    override fun delete(id: Int): Boolean {
        val sql = "DELETE FROM attachments WHERE id = ?"

        dataSource.connection.use { conn ->
            conn.prepareStatement(sql).use { stmt ->
                stmt.setInt(1, id)
                return stmt.executeUpdate() > 0
            }
        }
    }

    override fun countByDocumentId(documentId: Int): Int {
        val sql = "SELECT COUNT(*) FROM attachments WHERE document_id = ?"

        dataSource.connection.use { conn ->
            conn.prepareStatement(sql).use { stmt ->
                stmt.setInt(1, documentId)
                stmt.executeQuery().use { rs ->
                    if (rs.next()) {
                        return rs.getInt(1)
                    }
                    return 0
                }
            }
        }
    }

    override fun getTotalSize(documentId: Int): Long {
        val sql = "SELECT SUM(file_size) FROM attachments WHERE document_id = ?"

        dataSource.connection.use { conn ->
            conn.prepareStatement(sql).use { stmt ->
                stmt.setInt(1, documentId)
                stmt.executeQuery().use { rs ->
                    if (rs.next()) {
                        return rs.getLong(1)
                    }
                    return 0L
                }
            }
        }
    }

    private fun calculatePageCount(pdfData: ByteArray): Int? {
        return try {
            PDDocument.load(pdfData).use { document ->
                document.numberOfPages
            }
        } catch (e: Exception) {
            null
        }
    }
}
```

### 4. PDF Cache - Muistinvaraus

```kotlin
// PdfCache.kt
package kirjanpito.util

import kirjanpito.models.Attachment
import java.util.LinkedHashMap

class PdfCache(
    private val maxSizeBytes: Long = 80 * 1024 * 1024 // 80 MB kuten Kitsaassa
) {
    private val cache = object : LinkedHashMap<Int, CachedPdf>(16, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<Int, CachedPdf>?): Boolean {
            return currentSize > maxSizeBytes
        }
    }

    private var currentSize: Long = 0L

    data class CachedPdf(
        val attachment: Attachment,
        val timestamp: Long = System.currentTimeMillis()
    )

    @Synchronized
    fun get(attachmentId: Int): Attachment? {
        return cache[attachmentId]?.attachment
    }

    @Synchronized
    fun put(attachment: Attachment) {
        val existing = cache[attachment.id]
        if (existing != null) {
            currentSize -= existing.attachment.fileSize
        }

        cache[attachment.id] = CachedPdf(attachment)
        currentSize += attachment.fileSize.toLong()

        // Varmista että cache ei ylitä max-kokoa
        while (currentSize > maxSizeBytes && cache.isNotEmpty()) {
            val eldest = cache.entries.iterator().next()
            currentSize -= eldest.value.attachment.fileSize
            cache.remove(eldest.key)
        }
    }

    @Synchronized
    fun remove(attachmentId: Int) {
        val removed = cache.remove(attachmentId)
        if (removed != null) {
            currentSize -= removed.attachment.fileSize
        }
    }

    @Synchronized
    fun clear() {
        cache.clear()
        currentSize = 0L
    }

    @Synchronized
    fun getStats(): CacheStats {
        return CacheStats(
            entries = cache.size,
            sizeBytes = currentSize,
            maxSizeBytes = maxSizeBytes,
            hitRate = 0.0 // TODO: Track hit/miss statistics
        )
    }

    data class CacheStats(
        val entries: Int,
        val sizeBytes: Long,
        val maxSizeBytes: Long,
        val hitRate: Double
    ) {
        val sizeMB: Double get() = sizeBytes / 1024.0 / 1024.0
        val maxSizeMB: Double get() = maxSizeBytes / 1024.0 / 1024.0
        val usagePercent: Double get() = (sizeBytes.toDouble() / maxSizeBytes) * 100.0
    }
}
```

### 5. PDF Viewer - Apache PDFBox

**Lisää riippuvuus pom.xml:**

```xml
<dependency>
    <groupId>org.apache.pdfbox</groupId>
    <artifactId>pdfbox</artifactId>
    <version>3.0.1</version>
</dependency>
```

**PdfViewerPanel.kt:**

```kotlin
// PdfViewerPanel.kt
package kirjanpito.ui.components

import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.rendering.PDFRenderer
import java.awt.*
import java.awt.image.BufferedImage
import javax.swing.*

class PdfViewerPanel : JPanel() {
    private var pdfDocument: PDDocument? = null
    private var pdfRenderer: PDFRenderer? = null
    private var currentPage = 0
    private var zoomLevel = 1.0

    private val imageLabel = JLabel()
    private val scrollPane = JScrollPane(imageLabel)

    private val pageLabel = JLabel("Page 0/0")
    private val prevButton = JButton("◀ Previous")
    private val nextButton = JButton("Next ▶")
    private val zoomInButton = JButton("Zoom In (+)")
    private val zoomOutButton = JButton("Zoom Out (-)")
    private val fitWidthButton = JButton("Fit Width")
    private val fitPageButton = JButton("Fit Page")

    init {
        layout = BorderLayout()

        // Toolbar
        val toolbar = JPanel(FlowLayout(FlowLayout.LEFT))
        toolbar.add(prevButton)
        toolbar.add(nextButton)
        toolbar.add(pageLabel)
        toolbar.add(JSeparator(SwingConstants.VERTICAL))
        toolbar.add(zoomOutButton)
        toolbar.add(zoomInButton)
        toolbar.add(fitWidthButton)
        toolbar.add(fitPageButton)

        add(toolbar, BorderLayout.NORTH)
        add(scrollPane, BorderLayout.CENTER)

        // Button listeners
        prevButton.addActionListener { previousPage() }
        nextButton.addActionListener { nextPage() }
        zoomInButton.addActionListener { zoom(1.25) }
        zoomOutButton.addActionListener { zoom(0.8) }
        fitWidthButton.addActionListener { fitToWidth() }
        fitPageButton.addActionListener { fitToPage() }

        updateButtons()
    }

    fun loadPdf(pdfData: ByteArray) {
        closePdf()

        try {
            pdfDocument = PDDocument.load(pdfData)
            pdfRenderer = PDFRenderer(pdfDocument!!)
            currentPage = 0
            zoomLevel = 1.0

            renderCurrentPage()
            updateButtons()
        } catch (e: Exception) {
            JOptionPane.showMessageDialog(
                this,
                "Error loading PDF: ${e.message}",
                "PDF Error",
                JOptionPane.ERROR_MESSAGE
            )
        }
    }

    fun closePdf() {
        pdfDocument?.close()
        pdfDocument = null
        pdfRenderer = null
        imageLabel.icon = null
        currentPage = 0
        updateButtons()
    }

    private fun renderCurrentPage() {
        val doc = pdfDocument ?: return
        val renderer = pdfRenderer ?: return

        try {
            // Render at DPI scaled by zoom level
            val dpi = (72 * zoomLevel).toFloat()
            val image: BufferedImage = renderer.renderImageWithDPI(currentPage, dpi)

            imageLabel.icon = ImageIcon(image)
            imageLabel.revalidate()

            pageLabel.text = "Page ${currentPage + 1}/${doc.numberOfPages}"
        } catch (e: Exception) {
            JOptionPane.showMessageDialog(
                this,
                "Error rendering page: ${e.message}",
                "Rendering Error",
                JOptionPane.ERROR_MESSAGE
            )
        }
    }

    fun nextPage() {
        val doc = pdfDocument ?: return
        if (currentPage < doc.numberOfPages - 1) {
            currentPage++
            renderCurrentPage()
            updateButtons()
        }
    }

    fun previousPage() {
        if (currentPage > 0) {
            currentPage--
            renderCurrentPage()
            updateButtons()
        }
    }

    fun zoom(factor: Double) {
        zoomLevel *= factor
        zoomLevel = zoomLevel.coerceIn(0.25, 4.0)
        renderCurrentPage()
    }

    fun fitToWidth() {
        val doc = pdfDocument ?: return
        val viewportWidth = scrollPane.viewport.width

        // Calculate zoom to fit width
        val pageWidth = doc.getPage(currentPage).mediaBox.width
        zoomLevel = viewportWidth / pageWidth.toDouble()

        renderCurrentPage()
    }

    fun fitToPage() {
        val doc = pdfDocument ?: return
        val viewportWidth = scrollPane.viewport.width
        val viewportHeight = scrollPane.viewport.height

        val page = doc.getPage(currentPage)
        val pageWidth = page.mediaBox.width
        val pageHeight = page.mediaBox.height

        val widthRatio = viewportWidth / pageWidth.toDouble()
        val heightRatio = viewportHeight / pageHeight.toDouble()

        zoomLevel = minOf(widthRatio, heightRatio)

        renderCurrentPage()
    }

    private fun updateButtons() {
        val doc = pdfDocument
        val hasDoc = doc != null
        val pageCount = doc?.numberOfPages ?: 0

        prevButton.isEnabled = hasDoc && currentPage > 0
        nextButton.isEnabled = hasDoc && currentPage < pageCount - 1
        zoomInButton.isEnabled = hasDoc && zoomLevel < 4.0
        zoomOutButton.isEnabled = hasDoc && zoomLevel > 0.25
        fitWidthButton.isEnabled = hasDoc
        fitPageButton.isEnabled = hasDoc

        pageLabel.text = if (hasDoc) {
            "Page ${currentPage + 1}/$pageCount"
        } else {
            "No PDF loaded"
        }
    }

    override fun getPreferredSize(): Dimension {
        return Dimension(800, 600)
    }
}
```

### 6. UI Integration - DocumentFrame

**AttachmentsPanel.kt:**

```kotlin
// AttachmentsPanel.kt
package kirjanpito.ui.components

import kirjanpito.db.AttachmentDAO
import kirjanpito.models.Attachment
import kirjanpito.models.Document
import java.awt.*
import java.io.File
import javax.swing.*
import javax.swing.filechooser.FileNameExtensionFilter

class AttachmentsPanel(
    private val document: Document,
    private val attachmentDAO: AttachmentDAO,
    private val pdfCache: PdfCache
) : JPanel() {

    private val attachmentListModel = DefaultListModel<Attachment>()
    private val attachmentList = JList(attachmentListModel)
    private val pdfViewer = PdfViewerPanel()

    private val addButton = JButton("Add PDF...")
    private val removeButton = JButton("Remove")
    private val exportButton = JButton("Export...")

    init {
        layout = BorderLayout()

        // Left sidebar: attachment list
        val sidebar = JPanel(BorderLayout())
        sidebar.preferredSize = Dimension(200, 0)

        val listScrollPane = JScrollPane(attachmentList)
        sidebar.add(listScrollPane, BorderLayout.CENTER)

        // Buttons
        val buttonPanel = JPanel(GridLayout(3, 1, 5, 5))
        buttonPanel.add(addButton)
        buttonPanel.add(removeButton)
        buttonPanel.add(exportButton)
        sidebar.add(buttonPanel, BorderLayout.SOUTH)

        add(sidebar, BorderLayout.WEST)
        add(pdfViewer, BorderLayout.CENTER)

        // List selection listener
        attachmentList.addListSelectionListener { e ->
            if (!e.valueIsAdjusting) {
                val selected = attachmentList.selectedValue
                if (selected != null) {
                    showAttachment(selected)
                }
            }
        }

        // Button listeners
        addButton.addActionListener { addAttachment() }
        removeButton.addActionListener { removeSelectedAttachment() }
        exportButton.addActionListener { exportSelectedAttachment() }

        // Custom cell renderer
        attachmentList.cellRenderer = object : DefaultListCellRenderer() {
            override fun getListCellRendererComponent(
                list: JList<*>,
                value: Any?,
                index: Int,
                isSelected: Boolean,
                cellHasFocus: Boolean
            ): Component {
                val label = super.getListCellRendererComponent(
                    list, value, index, isSelected, cellHasFocus
                ) as JLabel

                if (value is Attachment) {
                    val sizeKB = value.fileSize / 1024
                    label.text = "${value.filename} (${sizeKB} KB)"
                    label.icon = UIManager.getIcon("FileView.fileIcon")
                }

                return label
            }
        }

        loadAttachments()
    }

    private fun loadAttachments() {
        attachmentListModel.clear()
        val attachments = attachmentDAO.findByDocumentId(document.id)
        attachments.forEach { attachmentListModel.addElement(it) }

        updateButtons()
    }

    private fun showAttachment(attachment: Attachment) {
        // Try cache first
        val cached = pdfCache.get(attachment.id)
        val data = cached?.data ?: attachment.data

        pdfViewer.loadPdf(data)

        // Add to cache if not already there
        if (cached == null) {
            pdfCache.put(attachment)
        }
    }

    private fun addAttachment() {
        val fileChooser = JFileChooser()
        fileChooser.fileFilter = FileNameExtensionFilter("PDF Files", "pdf")
        fileChooser.isMultiSelectionEnabled = false

        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            val file = fileChooser.selectedFile

            try {
                val data = file.readBytes()

                // Check file size
                if (data.size > Attachment.WARNING_FILE_SIZE) {
                    val result = JOptionPane.showConfirmDialog(
                        this,
                        "PDF file is large (${data.size / 1024 / 1024} MB). Continue?",
                        "Large File",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE
                    )
                    if (result != JOptionPane.YES_OPTION) return
                }

                if (data.size > Attachment.MAX_FILE_SIZE) {
                    JOptionPane.showMessageDialog(
                        this,
                        "PDF file is too large (max 10 MB)",
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                    )
                    return
                }

                val attachment = Attachment.fromFile(
                    documentId = document.id,
                    filename = file.name,
                    data = data
                )

                val id = attachmentDAO.save(attachment)

                loadAttachments()

                JOptionPane.showMessageDialog(
                    this,
                    "PDF attached successfully",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE
                )
            } catch (e: Exception) {
                JOptionPane.showMessageDialog(
                    this,
                    "Error adding PDF: ${e.message}",
                    "Error",
                    JOptionPane.ERROR_MESSAGE
                )
            }
        }
    }

    private fun removeSelectedAttachment() {
        val selected = attachmentList.selectedValue ?: return

        val result = JOptionPane.showConfirmDialog(
            this,
            "Remove attachment '${selected.filename}'?",
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        )

        if (result == JOptionPane.YES_OPTION) {
            attachmentDAO.delete(selected.id)
            pdfCache.remove(selected.id)
            loadAttachments()
            pdfViewer.closePdf()
        }
    }

    private fun exportSelectedAttachment() {
        val selected = attachmentList.selectedValue ?: return

        val fileChooser = JFileChooser()
        fileChooser.selectedFile = File(selected.filename)

        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                val file = fileChooser.selectedFile
                file.writeBytes(selected.data)

                JOptionPane.showMessageDialog(
                    this,
                    "PDF exported successfully",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE
                )
            } catch (e: Exception) {
                JOptionPane.showMessageDialog(
                    this,
                    "Error exporting PDF: ${e.message}",
                    "Error",
                    JOptionPane.ERROR_MESSAGE
                )
            }
        }
    }

    private fun updateButtons() {
        val hasSelection = attachmentList.selectedValue != null
        removeButton.isEnabled = hasSelection
        exportButton.isEnabled = hasSelection
    }
}
```

### 7. DocumentFrame Integration

**DocumentFrame.java - Lisäys:**

```java
// DocumentFrame.java - Tab lisäys

private void initializeUI() {
    // ... existing code ...

    // Add Attachments tab
    JPanel attachmentsTab = createAttachmentsTab();
    tabbedPane.addTab("Attachments", attachmentsTab);
}

private JPanel createAttachmentsTab() {
    AttachmentDAO attachmentDAO = new SQLiteAttachmentDAO(dataSource);
    PdfCache pdfCache = new PdfCache(); // Singleton?

    return new AttachmentsPanel(currentDocument, attachmentDAO, pdfCache);
}
```

---

## Toteutusjärjestys

### Sprint 1: Tietokanta ja DAO (3-4 päivää)

1. **Päivä 1-2:** Tietokantamuutokset
   - Luo `attachments`-taulu
   - Lisää migraatioskripti
   - Testaa SQLite, MySQL, PostgreSQL

2. **Päivä 3-4:** DAO-kerros
   - `Attachment.kt` data class
   - `AttachmentDAO` interface
   - `SQLiteAttachmentDAO` implementaatio
   - Yksikkötestit

### Sprint 2: PDF Cache ja Viewer (4-5 päivää)

3. **Päivä 5-6:** PDF Cache
   - `PdfCache.kt` LRU-toteutus
   - Cache statistics
   - Testit

4. **Päivä 7-9:** PDF Viewer
   - Apache PDFBox -integraatio
   - `PdfViewerPanel.kt`
   - Zoom, navigate, scroll
   - Testit

### Sprint 3: UI Integration (3-4 päivää)

5. **Päivä 10-11:** AttachmentsPanel
   - List view
   - Add/Remove/Export buttons
   - Integration with PdfViewer

6. **Päivä 12-13:** DocumentFrame Integration
   - Add Attachments tab
   - Wire up DAO and cache
   - End-to-end testing

### Sprint 4: Tuonti-ominaisuudet (2-3 päivää)

7. **Päivä 14-15:** Drag & Drop
   - TransferHandler implementation
   - Drop target on AttachmentsList
   - Visual feedback

8. **Päivä 16:** Leikepöytä-tuki
   - Ctrl+V support
   - Paste from clipboard

---

## Testaussuunnitelma

### Yksikkötestit

```kotlin
// AttachmentDAOTest.kt
class AttachmentDAOTest {
    private lateinit var dataSource: DataSource
    private lateinit var dao: AttachmentDAO

    @Before
    fun setup() {
        dataSource = DataSourceFactory.createInMemory()
        dao = SQLiteAttachmentDAO(dataSource)
        // Create schema
    }

    @Test
    fun `save and retrieve attachment`() {
        val attachment = Attachment.fromFile(
            documentId = 1,
            filename = "test.pdf",
            data = ByteArray(1024)
        )

        val id = dao.save(attachment)
        assert(id > 0)

        val retrieved = dao.findById(id)
        assertNotNull(retrieved)
        assertEquals(attachment.filename, retrieved!!.filename)
        assertArrayEquals(attachment.data, retrieved.data)
    }

    @Test
    fun `find attachments by document ID`() {
        // Create multiple attachments
        val attachment1 = Attachment.fromFile(1, "doc1.pdf", ByteArray(100))
        val attachment2 = Attachment.fromFile(1, "doc2.pdf", ByteArray(200))
        val attachment3 = Attachment.fromFile(2, "other.pdf", ByteArray(300))

        dao.save(attachment1)
        dao.save(attachment2)
        dao.save(attachment3)

        val attachments = dao.findByDocumentId(1)
        assertEquals(2, attachments.size)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `reject file too large`() {
        val largeData = ByteArray(Attachment.MAX_FILE_SIZE + 1)
        Attachment.fromFile(1, "large.pdf", largeData)
    }
}
```

### Integraatiotestit

```kotlin
// PdfViewerIntegrationTest.kt
class PdfViewerIntegrationTest {
    @Test
    fun `load and display PDF`() {
        val viewer = PdfViewerPanel()
        val pdfData = loadTestPdf("sample.pdf")

        viewer.loadPdf(pdfData)

        // Verify rendering
        assertNotNull(viewer.imageLabel.icon)
    }

    @Test
    fun `navigate pages`() {
        val viewer = PdfViewerPanel()
        val pdfData = loadTestPdf("multipage.pdf")

        viewer.loadPdf(pdfData)
        assertEquals(0, viewer.currentPage)

        viewer.nextPage()
        assertEquals(1, viewer.currentPage)

        viewer.previousPage()
        assertEquals(0, viewer.currentPage)
    }
}
```

---

## Riskianalyysi ja Mitigaatio

### Riski 1: BLOB-koko rajoitukset

**Ongelma:** SQLite BLOB max 1-2 GB, mutta suuret PDF:t voivat hidastaa.

**Mitigaatio:**
- Rajoita max-koko 10 MB
- Varoita käyttäjää >5 MB tiedostoista
- Harkitse ulkoista tallennusta (file system) suurille tiedostoille

### Riski 2: Muistinkäyttö (PDF Cache)

**Ongelma:** 80 MB cache voi kasvaa suureksi.

**Mitigaatio:**
- LRU-cache poistaa vanhimmat automaattisesti
- Konfiguroitava max-koko
- Monitoroi cache-tilastoja

### Riski 3: PDF-renderöinnin suorituskyky

**Ongelma:** Suuret PDF:t voivat olla hitaita renderöidä.

**Mitigaatio:**
- Renderöi taustasäikeessä (SwingWorker)
- Näytä loading-indikaattori
- Cache renderöidyt sivut

### Riski 4: Tietokannan koko kasvaa

**Ongelma:** BLOB-data kasvattaa tietokannan kokoa nopeasti.

**Mitigaatio:**
- Näytä varoitus käyttäjälle
- Tarjoa "Clean up attachments" -toiminto
- Harkitse kompressiota (gzip BLOB:it)

---

## Yhteenveto

### Työmäärä

**Yhteensä:** ~12-16 työpäivää (2.5-3 viikkoa)

- Sprint 1: Tietokanta & DAO (3-4 päivää)
- Sprint 2: Cache & Viewer (4-5 päivää)
- Sprint 3: UI Integration (3-4 päivää)
- Sprint 4: Tuonti (2-3 päivää)

### Riippuvuudet

**pom.xml - Uudet riippuvuudet:**
```xml
<dependency>
    <groupId>org.apache.pdfbox</groupId>
    <artifactId>pdfbox</artifactId>
    <version>3.0.1</version>
</dependency>
```

### Dokumentaatio

**Päivitettävät tiedostot:**
- CHANGELOG.md - Uudet ominaisuudet
- USER-GUIDE.md - PDF-liitteiden käyttö
- BUILDING.md - Uudet riippuvuudet

---

## Seuraavat Askeleet

1. **Hyväksy suunnitelma** - Käy läpi ja kommentoi
2. **Luo feature branch** - `feature/pdf-attachments`
3. **Aloita Sprint 1** - Tietokanta ja DAO
4. **Testaa jatkuvasti** - Yksikkö- ja integraatiotestit
5. **Dokumentoi** - Päivitä USER-GUIDE

---

**Tekijä:** Claude Sonnet 4.5 (AI-avusteinen suunnittelu)
**Päivitetty:** 2025-12-28
**Tila:** EHDOTUS - Odottaa hyväksyntää
