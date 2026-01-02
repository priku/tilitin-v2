package kirjanpito.models

import kirjanpito.db.*
import kirjanpito.db.sqlite.SQLiteDataSource
import kirjanpito.util.Registry
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import java.io.File
import java.sql.Date

/**
 * Tests for PropertiesModel operations.
 * PropertiesModel manages company settings and periods.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PropertiesModelTest {

    private lateinit var dataSource: DataSource
    private lateinit var testDbFile: File
    private lateinit var registry: Registry
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
            // Create period
            val periodDAO = dataSource.getPeriodDAO(session)
            testPeriod = Period().apply {
                setStartDate(Date(System.currentTimeMillis() - 365L * 24 * 60 * 60 * 1000))
                setEndDate(Date(System.currentTimeMillis()))
            }
            periodDAO.save(testPeriod)

            // Create settings
            val settingsDAO = dataSource.getSettingsDAO(session)
            val settings = Settings().apply {
                setName("Test Company")
                setBusinessId("1234567-8")
                setCurrentPeriodId(testPeriod.id)
            }
            settingsDAO.save(settings)

            session.commit()
        } finally {
            session.close()
        }

        // Initialize registry
        registry = Registry()
        registry.setDataSource(dataSource)
        registry.setPeriod(testPeriod)
        
        // Fetch settings into registry (required for PropertiesModel)
        val settingsSession = dataSource.openSession()
        try {
            registry.fetchSettings(settingsSession)
        } finally {
            settingsSession.close()
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
    fun `test create properties model`() {
        val model = PropertiesModel(registry)
        assertNotNull(model)
    }

    @Test
    fun `test get settings`() {
        val model = PropertiesModel(registry)
        val settings = model.settings
        assertNotNull(settings)
        assertEquals("Test Company", settings.name)
        assertEquals("1234567-8", settings.businessId)
    }

    @Test
    fun `test initialize properties model`() {
        val model = PropertiesModel(registry)
        model.initialize()
        
        // After initialization, model should have loaded periods
        assertTrue(model.periodCount >= 1)
    }

    @Test
    fun `test get period count`() {
        val model = PropertiesModel(registry)
        model.initialize()
        
        val count = model.periodCount
        assertTrue(count >= 1)
    }

    @Test
    fun `test get period`() {
        val model = PropertiesModel(registry)
        model.initialize()
        
        if (model.periodCount > 0) {
            val period = model.getPeriod(0)
            assertNotNull(period)
        }
    }

    @Test
    fun `test get current period index`() {
        val model = PropertiesModel(registry)
        model.initialize()
        
        val currentIndex = model.currentPeriodIndex
        assertTrue(currentIndex >= -1) // -1 means no current period
    }

    @Test
    fun `test create period`() {
        val model = PropertiesModel(registry)
        model.initialize()
        
        val initialCount = model.periodCount
        model.createPeriod()
        
        // Period count should increase
        assertTrue(model.periodCount > initialCount)
    }
}
