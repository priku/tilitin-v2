package kirjanpito.db

import kirjanpito.db.sqlite.SQLiteDataSource
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import java.io.File
import java.math.BigDecimal
import java.sql.Date

/**
 * Tests for EntryTemplateDAO operations.
 * EntryTemplate represents a reusable entry template for quick entry creation.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class EntryTemplateDAOTest {

    private lateinit var dataSource: DataSource
    private lateinit var testDbFile: File
    private lateinit var testPeriod: Period
    private lateinit var testAccount: Account

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
    fun `test create and retrieve entry template`() {
        val session = dataSource.openSession()
        try {
            val templateDAO = dataSource.getEntryTemplateDAO(session)

            // Create template
            val template = EntryTemplate().apply {
                setNumber(1)
                setName("Template 1")
                setAccountId(testAccount.id)
                setDescription("Template description")
                setDebit(true)
                setAmount(BigDecimal("100.00"))
            }

            templateDAO.save(template)
            session.commit()

            assertTrue(template.id > 0)

            // Retrieve all templates
            val allTemplates = templateDAO.getAll()
            val retrieved = allTemplates.find { it.id == template.id }
            assertNotNull(retrieved)
            assertEquals("Template description", retrieved!!.description)
            assertEquals(0, BigDecimal("100.00").compareTo(retrieved.amount))
            assertTrue(retrieved.isDebit())
            assertEquals(testAccount.id, retrieved.accountId)
        } finally {
            session.close()
        }
    }

    @Test
    fun `test update entry template`() {
        val session = dataSource.openSession()
        try {
            val templateDAO = dataSource.getEntryTemplateDAO(session)

            // Create template
            val template = EntryTemplate().apply {
                setNumber(1)
                setName("Original Template")
                setAccountId(testAccount.id)
                setDescription("Original description")
                setDebit(true)
                setAmount(BigDecimal("50.00"))
            }
            templateDAO.save(template)
            session.commit()

            val templateId = template.id

            // Update template
            template.setDescription("Updated description")
            template.setAmount(BigDecimal("75.00"))
            templateDAO.save(template)
            session.commit()

            // Verify update
            val allTemplates = templateDAO.getAll()
            val updated = allTemplates.find { it.id == templateId }
            assertNotNull(updated)
            assertEquals("Updated description", updated!!.description)
            assertEquals(0, BigDecimal("75.00").compareTo(updated.amount))
        } finally {
            session.close()
        }
    }

    @Test
    fun `test delete entry template`() {
        val session = dataSource.openSession()
        try {
            val templateDAO = dataSource.getEntryTemplateDAO(session)

            // Create template
            val template = EntryTemplate().apply {
                setNumber(1)
                setName("Delete Template")
                setAccountId(testAccount.id)
                setDescription("To be deleted")
                setDebit(true)
                setAmount(BigDecimal("25.00"))
            }
            templateDAO.save(template)
            session.commit()

            val templateId = template.id

            // Delete template
            templateDAO.delete(templateId)
            session.commit()

            // Verify deletion
            val allTemplates = templateDAO.getAll()
            val deleted = allTemplates.find { it.id == templateId }
            assertNull(deleted)
        } finally {
            session.close()
        }
    }

    @Test
    fun `test get all entry templates`() {
        val session = dataSource.openSession()
        try {
            val templateDAO = dataSource.getEntryTemplateDAO(session)

            // Create multiple templates
            for (i in 1..3) {
                val template = EntryTemplate().apply {
                    setNumber(i)
                    setName("Template $i")
                    setAccountId(testAccount.id)
                    setDescription("Template $i")
                    setDebit(true)
                    setAmount(BigDecimal("${i * 10}.00"))
                }
                templateDAO.save(template)
            }
            session.commit()

            // Get all templates
            val allTemplates = templateDAO.getAll()
            assertTrue(allTemplates.size >= 3)

            // Verify templates exist
            val descriptions = allTemplates.map { it.description }.toSet()
            assertTrue(descriptions.contains("Template 1"))
            assertTrue(descriptions.contains("Template 3"))
        } finally {
            session.close()
        }
    }

    @Test
    fun `test entry template with credit side`() {
        val session = dataSource.openSession()
        try {
            val templateDAO = dataSource.getEntryTemplateDAO(session)

            // Create credit template
            val template = EntryTemplate().apply {
                setNumber(1)
                setName("Credit Template")
                setAccountId(testAccount.id)
                setDescription("Credit template")
                setDebit(false) // Credit side
                setAmount(BigDecimal("200.00"))
            }

            templateDAO.save(template)
            session.commit()

            assertTrue(template.id > 0)

            // Verify
            val allTemplates = templateDAO.getAll()
            val retrieved = allTemplates.find { it.id == template.id }
            assertNotNull(retrieved)
            assertFalse(retrieved!!.isDebit())
            assertEquals(0, BigDecimal("200.00").compareTo(retrieved.amount))
        } finally {
            session.close()
        }
    }
}
