package kirjanpito.db.integration

import kirjanpito.db.*
import kirjanpito.db.sqlite.SQLiteDataSource
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import java.io.File
import java.math.BigDecimal
import java.sql.Date

/**
 * Integration tests for document workflow.
 * Tests the complete workflow: Period -> Document -> Entries
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DocumentWorkflowTest {

    private lateinit var dataSource: DataSource
    private lateinit var testDbFile: File
    private lateinit var testPeriod: Period
    private lateinit var assetAccount: Account
    private lateinit var incomeAccount: Account

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
            // Create period
            val periodDAO = dataSource.getPeriodDAO(session)
            testPeriod = Period().apply {
                setStartDate(Date(System.currentTimeMillis() - 365L * 24 * 60 * 60 * 1000))
                setEndDate(Date(System.currentTimeMillis()))
            }
            periodDAO.save(testPeriod)

            // Create accounts
            val accountDAO = dataSource.getAccountDAO(session)
            assetAccount = Account().apply {
                setNumber("1910")
                setName("Pankkitili")
                setType(Account.TYPE_ASSET)
            }
            accountDAO.save(assetAccount)

            incomeAccount = Account().apply {
                setNumber("3000")
                setName("Myynti")
                setType(Account.TYPE_REVENUE)
            }
            accountDAO.save(incomeAccount)

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
    fun `test complete document workflow - create document with balanced entries`() {
        val session = dataSource.openSession()
        try {
            val documentDAO = dataSource.getDocumentDAO(session)
            val entryDAO = dataSource.getEntryDAO(session)

            // Step 1: Create document
            val document = Document().apply {
                setPeriodId(testPeriod.id)
                setNumber(1)
                setDate(Date(System.currentTimeMillis()))
            }
            documentDAO.save(document)
            session.commit()

            assertTrue(document.id > 0)

            // Step 2: Create balanced entries (debit = credit)
            val debitEntry = Entry().apply {
                setDocumentId(document.id)
                setAccountId(assetAccount.id)
                setDescription("Myyntisaamiset")
                setDebit(true)
                setAmount(BigDecimal("1000.00"))
                setRowNumber(1)
            }

            val creditEntry = Entry().apply {
                setDocumentId(document.id)
                setAccountId(incomeAccount.id)
                setDescription("Myynti")
                setDebit(false)
                setAmount(BigDecimal("1000.00"))
                setRowNumber(2)
            }

            entryDAO.save(debitEntry)
            entryDAO.save(creditEntry)
            session.commit()

            // Step 3: Verify document and entries
            val retrievedDocument = documentDAO.getByPeriodIdAndNumber(testPeriod.id, 1)
            assertNotNull(retrievedDocument)

            val entries = entryDAO.getByDocumentId(document.id)
            assertEquals(2, entries.size)

            val debit = entries.find { it.isDebit() }
            val credit = entries.find { !it.isDebit() }

            assertNotNull(debit)
            assertNotNull(credit)
            assertEquals(0, debit!!.amount.compareTo(credit!!.amount)) // Balanced
        } finally {
            session.close()
        }
    }

    @Test
    fun `test document with multiple entries`() {
        val session = dataSource.openSession()
        try {
            val documentDAO = dataSource.getDocumentDAO(session)
            val entryDAO = dataSource.getEntryDAO(session)

            // Create document
            val document = Document().apply {
                setPeriodId(testPeriod.id)
                setNumber(2)
                setDate(Date(System.currentTimeMillis()))
            }
            documentDAO.save(document)
            session.commit()

            // Create multiple entries
            val entries = listOf(
                Entry().apply {
                    setDocumentId(document.id)
                    setAccountId(assetAccount.id)
                    setDescription("Entry 1")
                    setDebit(true)
                    setAmount(BigDecimal("100.00"))
                    setRowNumber(1)
                },
                Entry().apply {
                    setDocumentId(document.id)
                    setAccountId(assetAccount.id)
                    setDescription("Entry 2")
                    setDebit(true)
                    setAmount(BigDecimal("200.00"))
                    setRowNumber(2)
                },
                Entry().apply {
                    setDocumentId(document.id)
                    setAccountId(incomeAccount.id)
                    setDescription("Entry 3")
                    setDebit(false)
                    setAmount(BigDecimal("300.00"))
                    setRowNumber(3)
                }
            )

            entries.forEach { entryDAO.save(it) }
            session.commit()

            // Verify all entries are saved
            val retrievedEntries = entryDAO.getByDocumentId(document.id)
            assertEquals(3, retrievedEntries.size)
        } finally {
            session.close()
        }
    }

    @Test
    fun `test delete document with entries`() {
        val session = dataSource.openSession()
        try {
            val documentDAO = dataSource.getDocumentDAO(session)
            val entryDAO = dataSource.getEntryDAO(session)

            // Create document with entries
            val document = Document().apply {
                setPeriodId(testPeriod.id)
                setNumber(3)
                setDate(Date(System.currentTimeMillis()))
            }
            documentDAO.save(document)
            session.commit()

            val entry = Entry().apply {
                setDocumentId(document.id)
                setAccountId(assetAccount.id)
                setDescription("Entry to delete")
                setDebit(true)
                setAmount(BigDecimal("50.00"))
            }
            entryDAO.save(entry)
            session.commit()

            val entryId = entry.id
            val documentId = document.id

            // Delete document
            documentDAO.delete(documentId)
            session.commit()

            // Verify document is deleted
            val deletedDocument = documentDAO.getByPeriodIdAndNumber(testPeriod.id, 3)
            assertNull(deletedDocument)

            // Note: Entries might still exist (depending on foreign key constraints)
            // In real application, entries should be deleted first or cascade delete should be configured
        } finally {
            session.close()
        }
    }

    @Test
    fun `test document count by period`() {
        val session = dataSource.openSession()
        try {
            val documentDAO = dataSource.getDocumentDAO(session)

            // Create multiple documents
            for (i in 1..5) {
                val document = Document().apply {
                    setPeriodId(testPeriod.id)
                    setNumber(10 + i)
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
}
