package kirjanpito.db

import kirjanpito.db.sqlite.SQLiteDataSource
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import java.io.File
import java.sql.Date

/**
 * Tests for SettingsDAO operations.
 * Settings contains application-wide settings like company name, business ID, etc.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SettingsDAOTest {

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
    fun `test save and retrieve settings`() {
        val session = dataSource.openSession()
        try {
            val settingsDAO = dataSource.getSettingsDAO(session)

            // Create settings
            val settings = Settings().apply {
                setName("Test Company")
                setBusinessId("1234567-8")
                setCurrentPeriodId(1)
            }

            settingsDAO.save(settings)
            session.commit()

            // Retrieve settings
            val retrieved = settingsDAO.get()
            assertNotNull(retrieved)
            assertEquals("Test Company", retrieved.name)
            assertEquals("1234567-8", retrieved.businessId)
            assertEquals(1, retrieved.currentPeriodId)
        } finally {
            session.close()
        }
    }

    @Test
    fun `test update settings`() {
        val session = dataSource.openSession()
        try {
            val settingsDAO = dataSource.getSettingsDAO(session)

            // Create initial settings
            val settings = Settings().apply {
                setName("Original Company")
                setBusinessId("1111111-1")
            }
            settingsDAO.save(settings)
            session.commit()

            // Update settings
            settings.setName("Updated Company")
            settings.setBusinessId("2222222-2")
            settingsDAO.save(settings)
            session.commit()

            // Verify update
            val retrieved = settingsDAO.get()
            assertNotNull(retrieved)
            assertEquals("Updated Company", retrieved.name)
            assertEquals("2222222-2", retrieved.businessId)
        } finally {
            session.close()
        }
    }

    @Test
    fun `test settings with properties`() {
        val session = dataSource.openSession()
        try {
            val settingsDAO = dataSource.getSettingsDAO(session)

            // Create settings with custom properties
            val settings = Settings().apply {
                setName("Company with Properties")
                setBusinessId("1234567-8")
                setProperty("key1", "value1")
                setProperty("key2", "value2")
            }

            settingsDAO.save(settings)
            session.commit()

            // Retrieve and verify
            val retrieved = settingsDAO.get()
            assertNotNull(retrieved)
            assertEquals("value1", retrieved.getProperty("key1", ""))
            assertEquals("value2", retrieved.getProperty("key2", ""))
        } finally {
            session.close()
        }
    }

    @Test
    fun `test settings current period id`() {
        val session = dataSource.openSession()
        try {
            val periodDAO = dataSource.getPeriodDAO(session)
            val settingsDAO = dataSource.getSettingsDAO(session)

            // Create period
            val period = Period().apply {
                setStartDate(Date(System.currentTimeMillis() - 365L * 24 * 60 * 60 * 1000))
                setEndDate(Date(System.currentTimeMillis()))
            }
            periodDAO.save(period)
            session.commit()

            // Set current period in settings
            val settings = Settings().apply {
                setName("Test")
                setBusinessId("1234567-8")
                setCurrentPeriodId(period.id)
            }
            settingsDAO.save(settings)
            session.commit()

            // Verify
            val retrieved = settingsDAO.get()
            assertNotNull(retrieved)
            assertEquals(period.id, retrieved.currentPeriodId)
        } finally {
            session.close()
        }
    }
}
