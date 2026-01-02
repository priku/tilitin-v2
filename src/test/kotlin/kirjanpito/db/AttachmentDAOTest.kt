package kirjanpito.db

import kirjanpito.db.sqlite.SQLiteDataSource
import kirjanpito.models.Attachment
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import java.io.File
import java.sql.Date

/**
 * Tests for AttachmentDAO operations.
 * Attachment represents a PDF attachment linked to a document.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AttachmentDAOTest {

    private lateinit var dataSource: DataSource
    private lateinit var testDbFile: File
    private lateinit var testPeriod: Period
    private lateinit var testDocument: Document

    @BeforeAll
    fun setupDatabase() {
        testDbFile = File.createTempFile("tilitin-test-", ".db")
        testDbFile.deleteOnExit()
        testDbFile.delete() // Delete so SQLiteDataSource creates fresh DB

        dataSource = SQLiteDataSource()
        dataSource.open("jdbc:sqlite:${testDbFile.absolutePath}", "", "")

        // Initialize database schema
        val session = dataSource.openSession()
        try {
            // Create a period first (required for foreign keys)
            val periodDAO = dataSource.getPeriodDAO(session)
            testPeriod = Period().apply {
                setStartDate(Date(System.currentTimeMillis() - 365L * 24 * 60 * 60 * 1000))
                setEndDate(Date(System.currentTimeMillis()))
            }
            periodDAO.save(testPeriod)

            // Create a document
            val documentDAO = dataSource.getDocumentDAO(session)
            testDocument = Document().apply {
                setPeriodId(testPeriod.id)
                setNumber(1)
                setDate(Date(System.currentTimeMillis()))
            }
            documentDAO.save(testDocument)

            session.commit()
        } finally {
            session.close()
        }
    }

    @AfterAll
    fun cleanupDatabase() {
        if (::dataSource.isInitialized) {
            dataSource.close()
        }
        if (::testDbFile.isInitialized && testDbFile.exists()) {
            testDbFile.delete()
        }
    }

    @Test
    fun `test create and retrieve attachment`() {
        val session = dataSource.openSession()
        try {
            val attachmentDAO = dataSource.getAttachmentDAO(session)

            // Create attachment
            val attachment = Attachment(
                id = 0,
                documentId = testDocument.id,
                filename = "test.pdf",
                data = ByteArray(1024) { 0x42.toByte() } // Sample PDF data
            )

            val attachmentId = attachmentDAO.save(attachment)
            session.commit()

            assertTrue(attachmentId > 0)

            // Retrieve attachment
            val retrieved = attachmentDAO.findById(attachmentId)
            assertNotNull(retrieved)
            assertEquals("test.pdf", retrieved!!.filename)
            assertEquals(1024, retrieved.fileSize)
            assertEquals(testDocument.id, retrieved.documentId)
        } finally {
            session.close()
        }
    }

    @Test
    fun `test find attachments by document id`() {
        val session = dataSource.openSession()
        try {
            val attachmentDAO = dataSource.getAttachmentDAO(session)

            // Create multiple attachments for the same document
            val attachment1 = Attachment(
                id = 0,
                documentId = testDocument.id,
                filename = "file1.pdf",
                data = ByteArray(512) { 0x41.toByte() }
            )

            val attachment2 = Attachment(
                id = 0,
                documentId = testDocument.id,
                filename = "file2.pdf",
                data = ByteArray(256) { 0x43.toByte() }
            )

            attachmentDAO.save(attachment1)
            attachmentDAO.save(attachment2)
            session.commit()

            // Find attachments by document id
            val attachments = attachmentDAO.findByDocumentId(testDocument.id)
            assertTrue(attachments.size >= 2)

            val fileNames = attachments.map { it.filename }.toSet()
            assertTrue(fileNames.contains("file1.pdf"))
            assertTrue(fileNames.contains("file2.pdf"))
        } finally {
            session.close()
        }
    }

    @Test
    fun `test delete attachment`() {
        val session = dataSource.openSession()
        try {
            val attachmentDAO = dataSource.getAttachmentDAO(session)

            // Create attachment
            val attachment = Attachment(
                id = 0,
                documentId = testDocument.id,
                filename = "to-delete.pdf",
                data = ByteArray(128) { 0x44.toByte() }
            )

            val attachmentId = attachmentDAO.save(attachment)
            session.commit()

            // Delete attachment
            val deleted = attachmentDAO.delete(attachmentId)
            session.commit()

            assertTrue(deleted)

            // Verify deletion
            val retrieved = attachmentDAO.findById(attachmentId)
            assertNull(retrieved)
        } finally {
            session.close()
        }
    }

    @Test
    fun `test count attachments by document id`() {
        val session = dataSource.openSession()
        try {
            val attachmentDAO = dataSource.getAttachmentDAO(session)

            // Create multiple attachments
            for (i in 1..3) {
                val attachment = Attachment(
                    id = 0,
                    documentId = testDocument.id,
                    filename = "file$i.pdf",
                    data = ByteArray(i * 100) { 0x45.toByte() }
                )
                attachmentDAO.save(attachment)
            }
            session.commit()

            // Count attachments
            val count = attachmentDAO.countByDocumentId(testDocument.id)
            assertTrue(count >= 3)
        } finally {
            session.close()
        }
    }

    @Test
    fun `test get total size by document id`() {
        val session = dataSource.openSession()
        try {
            val attachmentDAO = dataSource.getAttachmentDAO(session)

            // Create attachments with known sizes
            val sizes = listOf(100, 200, 300)
            sizes.forEach { size ->
                val attachment = Attachment(
                    id = 0,
                    documentId = testDocument.id,
                    filename = "size-$size.pdf",
                    data = ByteArray(size) { 0x46.toByte() }
                )
                attachmentDAO.save(attachment)
            }
            session.commit()

            // Get total size
            val totalSize = attachmentDAO.getTotalSize(testDocument.id)
            assertTrue(totalSize >= 600) // 100 + 200 + 300
        } finally {
            session.close()
        }
    }

    @Test
    fun `test delete non-existent attachment`() {
        val session = dataSource.openSession()
        try {
            val attachmentDAO = dataSource.getAttachmentDAO(session)

            // Try to delete non-existent attachment
            val deleted = attachmentDAO.delete(99999)
            session.commit()

            assertFalse(deleted)
        } finally {
            session.close()
        }
    }
}
