package kirjanpito.db

import kirjanpito.db.sqlite.SQLiteDataSource
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import java.io.File
import java.sql.Date

/**
 * Tests for DocumentTypeDAO operations.
 * DocumentType represents a document type (voucher type) in the accounting system.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DocumentTypeDAOTest {

    private lateinit var dataSource: DataSource
    private lateinit var testDbFile: File

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
            val period = Period().apply {
                setStartDate(Date(System.currentTimeMillis() - 365L * 24 * 60 * 60 * 1000))
                setEndDate(Date(System.currentTimeMillis()))
            }
            periodDAO.save(period)
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
    fun `test create and retrieve document type`() {
        val session = dataSource.openSession()
        try {
            val documentTypeDAO = dataSource.getDocumentTypeDAO(session)

            // Create document type
            val documentType = DocumentType().apply {
                setNumber(1)
                setName("Myyntilasku")
                setNumberStart(1)
                setNumberEnd(9999)
            }

            documentTypeDAO.save(documentType)
            session.commit()

            assertTrue(documentType.id > 0)

            // Retrieve all document types
            val allTypes = documentTypeDAO.getAll()
            val retrieved = allTypes.find { it.id == documentType.id }
            assertNotNull(retrieved)
            assertEquals("Myyntilasku", retrieved!!.name)
            assertEquals(1, retrieved.number)
            assertEquals(1, retrieved.numberStart)
            assertEquals(9999, retrieved.numberEnd)
        } finally {
            session.close()
        }
    }

    @Test
    fun `test update document type`() {
        val session = dataSource.openSession()
        try {
            val documentTypeDAO = dataSource.getDocumentTypeDAO(session)

            // Create document type
            val documentType = DocumentType().apply {
                setNumber(2)
                setName("Original Name")
                setNumberStart(1)
                setNumberEnd(100)
            }
            documentTypeDAO.save(documentType)
            session.commit()

            val typeId = documentType.id

            // Update document type
            documentType.setName("Updated Name")
            documentType.setNumberStart(10)
            documentType.setNumberEnd(200)
            documentTypeDAO.save(documentType)
            session.commit()

            // Verify update
            val allTypes = documentTypeDAO.getAll()
            val updated = allTypes.find { it.id == typeId }
            assertNotNull(updated)
            assertEquals("Updated Name", updated!!.name)
            assertEquals(10, updated.numberStart)
            assertEquals(200, updated.numberEnd)
        } finally {
            session.close()
        }
    }

    @Test
    fun `test delete document type`() {
        val session = dataSource.openSession()
        try {
            val documentTypeDAO = dataSource.getDocumentTypeDAO(session)

            // Create document type
            val documentType = DocumentType().apply {
                setNumber(3)
                setName("To be deleted")
                setNumberStart(1)
                setNumberEnd(50)
            }
            documentTypeDAO.save(documentType)
            session.commit()

            val typeId = documentType.id

            // Delete document type
            documentTypeDAO.delete(typeId)
            session.commit()

            // Verify deletion
            val allTypes = documentTypeDAO.getAll()
            val deleted = allTypes.find { it.id == typeId }
            assertNull(deleted)
        } finally {
            session.close()
        }
    }

    @Test
    fun `test get all document types`() {
        val session = dataSource.openSession()
        try {
            val documentTypeDAO = dataSource.getDocumentTypeDAO(session)

            // Create multiple document types
            for (i in 1..3) {
                val documentType = DocumentType().apply {
                    setNumber(i + 10)
                    setName("Type $i")
                    setNumberStart(i * 100)
                    setNumberEnd(i * 100 + 99)
                }
                documentTypeDAO.save(documentType)
            }
            session.commit()

            // Get all document types
            val allTypes = documentTypeDAO.getAll()
            assertTrue(allTypes.size >= 3)

            // Verify types exist
            val names = allTypes.map { it.name }.toSet()
            assertTrue(names.contains("Type 1"))
            assertTrue(names.contains("Type 3"))
        } finally {
            session.close()
        }
    }

    @Test
    fun `test document type number range`() {
        val session = dataSource.openSession()
        try {
            val documentTypeDAO = dataSource.getDocumentTypeDAO(session)

            // Create document type with specific number range
            val documentType = DocumentType().apply {
                setNumber(1)
                setName("Custom Range")
                setNumberStart(1000)
                setNumberEnd(1999)
            }
            documentTypeDAO.save(documentType)
            session.commit()

            assertTrue(documentType.id > 0)

            // Verify number range
            val allTypes = documentTypeDAO.getAll()
            val retrieved = allTypes.find { it.id == documentType.id }
            assertNotNull(retrieved)
            assertTrue(retrieved!!.numberEnd >= retrieved.numberStart)
        } finally {
            session.close()
        }
    }
}
