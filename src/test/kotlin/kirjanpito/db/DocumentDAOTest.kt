package kirjanpito.db

import kirjanpito.db.sqlite.SQLiteDataSource
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import java.io.File
import java.sql.Date

/**
 * Tests for DocumentDAO operations.
 * Document represents a voucher/document in the accounting system.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DocumentDAOTest {

    private lateinit var dataSource: DataSource
    private lateinit var testDbFile: File
    private lateinit var testPeriod: Period

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
    fun `test create and retrieve document`() {
        val session = dataSource.openSession()
        try {
            val documentDAO = dataSource.getDocumentDAO(session)

            // Create document
            val document = Document().apply {
                setPeriodId(testPeriod.id)
                setNumber(1)
                setDate(Date(System.currentTimeMillis()))
            }

            documentDAO.save(document)
            session.commit()

            assertTrue(document.id > 0)

            // Retrieve document by period and number
            val retrieved = documentDAO.getByPeriodIdAndNumber(testPeriod.id, 1)
            assertNotNull(retrieved)
            assertEquals(1, retrieved!!.number)
            assertEquals(testPeriod.id, retrieved.periodId)
        } finally {
            session.close()
        }
    }

    @Test
    fun `test update document`() {
        val session = dataSource.openSession()
        try {
            val documentDAO = dataSource.getDocumentDAO(session)

            // Create document
            val document = Document().apply {
                setPeriodId(testPeriod.id)
                setNumber(2)
                setDate(Date(System.currentTimeMillis()))
            }
            documentDAO.save(document)
            session.commit()

            val documentId = document.id

            // Update document
            val newDate = Date(System.currentTimeMillis() - 86400000L) // Yesterday
            document.setDate(newDate)
            document.setNumber(3)
            documentDAO.save(document)
            session.commit()

            // Verify update
            val updated = documentDAO.getByPeriodIdAndNumber(testPeriod.id, 3)
            assertNotNull(updated)
            assertEquals(documentId, updated!!.id)
            assertEquals(newDate, updated.date)
        } finally {
            session.close()
        }
    }

    @Test
    fun `test delete document`() {
        val session = dataSource.openSession()
        try {
            val documentDAO = dataSource.getDocumentDAO(session)

            // Create document
            val document = Document().apply {
                setPeriodId(testPeriod.id)
                setNumber(4)
                setDate(Date(System.currentTimeMillis()))
            }
            documentDAO.save(document)
            session.commit()

            val documentId = document.id

            // Delete document
            documentDAO.delete(documentId)
            session.commit()

            // Verify deletion
            val deleted = documentDAO.getByPeriodIdAndNumber(testPeriod.id, 4)
            assertNull(deleted)
        } finally {
            session.close()
        }
    }

    @Test
    fun `test get count by period id`() {
        val session = dataSource.openSession()
        try {
            val documentDAO = dataSource.getDocumentDAO(session)

            // Create multiple documents
            for (i in 1..5) {
                val document = Document().apply {
                    setPeriodId(testPeriod.id)
                    setNumber(i + 10) // Use numbers 11-15
                    setDate(Date(System.currentTimeMillis()))
                }
                documentDAO.save(document)
            }
            session.commit()

            // Get count (numberOffset = 1 means documents with number >= 1)
            val count = documentDAO.getCountByPeriodId(testPeriod.id, 1)
            assertTrue(count >= 5)
        } finally {
            session.close()
        }
    }

    @Test
    fun `test get by period id and number`() {
        val session = dataSource.openSession()
        try {
            val documentDAO = dataSource.getDocumentDAO(session)

            // Create document with specific number
            val document = Document().apply {
                setPeriodId(testPeriod.id)
                setNumber(100)
                setDate(Date(System.currentTimeMillis()))
            }
            documentDAO.save(document)
            session.commit()

            // Retrieve by period and number
            val retrieved = documentDAO.getByPeriodIdAndNumber(testPeriod.id, 100)
            assertNotNull(retrieved)
            assertEquals(100, retrieved!!.number)
            assertEquals(testPeriod.id, retrieved.periodId)
        } finally {
            session.close()
        }
    }

    @Test
    fun `test document with zero number`() {
        val session = dataSource.openSession()
        try {
            val documentDAO = dataSource.getDocumentDAO(session)

            // Create document with number 0 (starting balance document)
            val document = Document().apply {
                setPeriodId(testPeriod.id)
                setNumber(0)
                setDate(testPeriod.startDate)
            }
            documentDAO.save(document)
            session.commit()

            assertTrue(document.id > 0)

            // Retrieve zero document
            val retrieved = documentDAO.getByPeriodIdAndNumber(testPeriod.id, 0)
            assertNotNull(retrieved)
            assertEquals(0, retrieved!!.number)
        } finally {
            session.close()
        }
    }
}
