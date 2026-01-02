package kirjanpito.db

import kirjanpito.db.sqlite.SQLiteDataSource
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import java.io.File
import java.math.BigDecimal
import java.sql.Date

/**
 * Tests for EntryDAO operations.
 * Entry is the core business entity representing a single accounting entry.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class EntryDAOTest {

    private lateinit var dataSource: DataSource
    private lateinit var testDbFile: File
    private lateinit var testPeriod: Period
    private lateinit var testAccount: Account
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
            
            // Create an account
            val accountDAO = dataSource.getAccountDAO(session)
            testAccount = Account().apply {
                setNumber("1910")
                setName("Pankkitili")
                setType(Account.TYPE_ASSET)
            }
            accountDAO.save(testAccount)
            
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
    fun `test create and retrieve entry`() {
        val session = dataSource.openSession()
        try {
            val entryDAO = dataSource.getEntryDAO(session)

            // Create entry
            val entry = Entry().apply {
                setDocumentId(testDocument.id)
                setAccountId(testAccount.id)
                setDescription("Test entry")
                setDebit(true)
                setAmount(BigDecimal("100.00"))
            }

            entryDAO.save(entry)
            session.commit()

            assertTrue(entry.id > 0)

            // Retrieve entries by document
            val entries = entryDAO.getByDocumentId(testDocument.id)
            val retrieved = entries.find { it.id == entry.id }
            assertNotNull(retrieved)
            assertEquals("Test entry", retrieved!!.description)
            assertEquals(0, BigDecimal("100.00").compareTo(retrieved.amount))
            assertTrue(retrieved.isDebit())
            assertEquals(testAccount.id, retrieved.accountId)
        } finally {
            session.close()
        }
    }

    @Test
    fun `test update entry`() {
        val session = dataSource.openSession()
        try {
            val entryDAO = dataSource.getEntryDAO(session)

            // Create entry
            val entry = Entry().apply {
                setDocumentId(testDocument.id)
                setAccountId(testAccount.id)
                setDescription("Original description")
                setDebit(true)
                setAmount(BigDecimal("50.00"))
            }
            entryDAO.save(entry)
            session.commit()

            val entryId = entry.id

            // Update entry
            entry.setDescription("Updated description")
            entry.setAmount(BigDecimal("75.00"))
            entryDAO.save(entry)
            session.commit()

            // Verify update
            val entries = entryDAO.getByDocumentId(testDocument.id)
            val updated = entries.find { it.id == entryId }
            assertNotNull(updated)
            assertEquals("Updated description", updated!!.description)
            assertEquals(0, BigDecimal("75.00").compareTo(updated.amount))
        } finally {
            session.close()
        }
    }

    @Test
    fun `test delete entry`() {
        val session = dataSource.openSession()
        try {
            val entryDAO = dataSource.getEntryDAO(session)

            // Create entry
            val entry = Entry().apply {
                setDocumentId(testDocument.id)
                setAccountId(testAccount.id)
                setDescription("To be deleted")
                setDebit(true)
                setAmount(BigDecimal("25.00"))
            }
            entryDAO.save(entry)
            session.commit()

            val entryId = entry.id

            // Delete entry
            entryDAO.delete(entryId)
            session.commit()

            // Verify deletion
            val entries = entryDAO.getByDocumentId(testDocument.id)
            val deleted = entries.find { it.id == entryId }
            assertNull(deleted)
        } finally {
            session.close()
        }
    }

    @Test
    fun `test get entries by document id`() {
        val session = dataSource.openSession()
        try {
            val entryDAO = dataSource.getEntryDAO(session)
            val documentDAO = dataSource.getDocumentDAO(session)

            // Create a new document
            val document = Document().apply {
                setPeriodId(testPeriod.id)
                setNumber(2)
                setDate(Date(System.currentTimeMillis()))
            }
            documentDAO.save(document)
            session.commit()

            // Create multiple entries for this document
            val entry1 = Entry().apply {
                setDocumentId(document.id)
                setAccountId(testAccount.id)
                setDescription("Entry 1")
                setDebit(true)
                setAmount(BigDecimal("100.00"))
            }
            val entry2 = Entry().apply {
                setDocumentId(document.id)
                setAccountId(testAccount.id)
                setDescription("Entry 2")
                setDebit(false) // Credit side
                setAmount(BigDecimal("100.00"))
            }

            entryDAO.save(entry1)
            entryDAO.save(entry2)
            session.commit()

            // Get entries by document
            val entries = entryDAO.getByDocumentId(document.id)
            assertEquals(2, entries.size)
            assertTrue(entries.any { it.description == "Entry 1" })
            assertTrue(entries.any { it.description == "Entry 2" })
        } finally {
            session.close()
        }
    }

    @Test
    fun `test entry with debit and credit sides`() {
        val session = dataSource.openSession()
        try {
            val entryDAO = dataSource.getEntryDAO(session)

            // Create debit entry
            val debitEntry = Entry().apply {
                setDocumentId(testDocument.id)
                setAccountId(testAccount.id)
                setDescription("Debit entry")
                setDebit(true)
                setAmount(BigDecimal("100.00"))
            }

            entryDAO.save(debitEntry)
            session.commit()

            assertTrue(debitEntry.id > 0)

            // Verify debit entry
            val entries = entryDAO.getByDocumentId(testDocument.id)
            val retrieved = entries.find { it.id == debitEntry.id }
            assertNotNull(retrieved)
            assertTrue(retrieved!!.isDebit())
            assertEquals(0, BigDecimal("100.00").compareTo(retrieved.amount))
        } finally {
            session.close()
        }
    }

    @Test
    fun `test delete by period id`() {
        val session = dataSource.openSession()
        try {
            val entryDAO = dataSource.getEntryDAO(session)
            val documentDAO = dataSource.getDocumentDAO(session)

            // Create a new document in the test period
            val document = Document().apply {
                setPeriodId(testPeriod.id)
                setNumber(3)
                setDate(Date(System.currentTimeMillis()))
            }
            documentDAO.save(document)
            session.commit()

            // Create entries
            val entry1 = Entry().apply {
                setDocumentId(document.id)
                setAccountId(testAccount.id)
                setDescription("Entry to delete")
                setDebit(true)
                setAmount(BigDecimal("50.00"))
            }
            val entry2 = Entry().apply {
                setDocumentId(document.id)
                setAccountId(testAccount.id)
                setDescription("Another entry to delete")
                setDebit(false) // Credit side
                setAmount(BigDecimal("50.00"))
            }

            entryDAO.save(entry1)
            entryDAO.save(entry2)
            session.commit()

            // Verify entries exist
            var entries = entryDAO.getByDocumentId(document.id)
            assertEquals(2, entries.size)

            // Delete all entries for this period
            entryDAO.deleteByPeriodId(testPeriod.id)
            session.commit()

            // Verify all entries are deleted
            entries = entryDAO.getByDocumentId(document.id)
            assertEquals(0, entries.size)
        } finally {
            session.close()
        }
    }
}
