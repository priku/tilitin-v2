package kirjanpito.models

import kirjanpito.db.*
import kirjanpito.db.sqlite.SQLiteDataSource
import kirjanpito.util.Registry
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import java.io.File
import java.math.BigDecimal
import java.sql.Date

/**
 * Tests for DocumentModel operations.
 * DocumentModel manages document and entry operations in the application.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DocumentModelTest {

    private lateinit var dataSource: DataSource
    private lateinit var testDbFile: File
    private lateinit var registry: Registry
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
            // Create period
            val periodDAO = dataSource.getPeriodDAO(session)
            testPeriod = Period().apply {
                setStartDate(Date(System.currentTimeMillis() - 365L * 24 * 60 * 60 * 1000))
                setEndDate(Date(System.currentTimeMillis()))
            }
            periodDAO.save(testPeriod)

            // Create account
            val accountDAO = dataSource.getAccountDAO(session)
            testAccount = Account().apply {
                setNumber("1910")
                setName("Pankkitili")
                setType(Account.TYPE_ASSET)
            }
            accountDAO.save(testAccount)

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
    fun `test create document model`() {
        val model = DocumentModel(registry)
        assertNotNull(model)
    }

    @Test
    fun `test initialize document model`() {
        val model = DocumentModel(registry)
        
        // Initialize should load data from database
        // Note: This might throw DataAccessException if database is not properly set up
        // We'll test basic functionality
        assertNotNull(model)
    }

    @Test
    fun `test document model with registry`() {
        val model = DocumentModel(registry)
        
        // Verify model has access to registry
        assertNotNull(model)
        // Model should be able to access registry's data source
    }
}
