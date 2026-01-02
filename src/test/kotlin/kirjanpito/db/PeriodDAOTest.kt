package kirjanpito.db

import kirjanpito.db.sqlite.SQLiteDataSource
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import java.io.File
import java.sql.Date

/**
 * Tests for PeriodDAO operations.
 * Period represents an accounting period (fiscal year).
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PeriodDAOTest {

    private lateinit var dataSource: DataSource
    private lateinit var testDbFile: File

    @BeforeAll
    fun setupDatabase() {
        testDbFile = File.createTempFile("tilitin-test-", ".db")
        testDbFile.deleteOnExit()
        testDbFile.delete() // Delete so SQLiteDataSource creates fresh DB

        dataSource = SQLiteDataSource()
        dataSource.open("jdbc:sqlite:${testDbFile.absolutePath}", "", "")
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
    fun `test create and retrieve period`() {
        val session = dataSource.openSession()
        try {
            val periodDAO = dataSource.getPeriodDAO(session)

            // Create period
            val startDate = Date(System.currentTimeMillis() - 365L * 24 * 60 * 60 * 1000)
            val endDate = Date(System.currentTimeMillis())
            
            val period = Period().apply {
                setStartDate(startDate)
                setEndDate(endDate)
            }

            periodDAO.save(period)
            session.commit()

            assertTrue(period.id > 0)

            // Retrieve all periods and find ours
            val allPeriods = periodDAO.getAll()
            val retrieved = allPeriods.find { it.id == period.id }
            assertNotNull(retrieved)
            assertEquals(startDate, retrieved!!.startDate)
            assertEquals(endDate, retrieved.endDate)
        } finally {
            session.close()
        }
    }

    @Test
    fun `test update period`() {
        val session = dataSource.openSession()
        try {
            val periodDAO = dataSource.getPeriodDAO(session)

            // Create period
            val startDate = Date(System.currentTimeMillis() - 365L * 24 * 60 * 60 * 1000)
            val endDate = Date(System.currentTimeMillis())
            
            val period = Period().apply {
                setStartDate(startDate)
                setEndDate(endDate)
            }
            periodDAO.save(period)
            session.commit()

            val periodId = period.id

            // Update period
            val newEndDate = Date(System.currentTimeMillis() + 86400000L) // Tomorrow
            period.setEndDate(newEndDate)
            periodDAO.save(period)
            session.commit()

            // Verify update
            val allPeriods = periodDAO.getAll()
            val updated = allPeriods.find { it.id == periodId }
            assertNotNull(updated)
            assertEquals(newEndDate, updated!!.endDate)
        } finally {
            session.close()
        }
    }

    @Test
    fun `test delete period`() {
        val session = dataSource.openSession()
        try {
            val periodDAO = dataSource.getPeriodDAO(session)

            // Create period
            val startDate = Date(System.currentTimeMillis() - 365L * 24 * 60 * 60 * 1000)
            val endDate = Date(System.currentTimeMillis())
            
            val period = Period().apply {
                setStartDate(startDate)
                setEndDate(endDate)
            }
            periodDAO.save(period)
            session.commit()

            val periodId = period.id

            // Delete period
            periodDAO.delete(periodId)
            session.commit()

            // Verify deletion
            val allPeriods = periodDAO.getAll()
            val deleted = allPeriods.find { it.id == periodId }
            assertNull(deleted)
        } finally {
            session.close()
        }
    }

    @Test
    fun `test get all periods`() {
        val session = dataSource.openSession()
        try {
            val periodDAO = dataSource.getPeriodDAO(session)

            // Create multiple periods
            val now = System.currentTimeMillis()
            for (i in 1..3) {
                val period = Period().apply {
                    setStartDate(Date(now - (365L + i * 365L) * 24 * 60 * 60 * 1000))
                    setEndDate(Date(now - i * 365L * 24 * 60 * 60 * 1000))
                }
                periodDAO.save(period)
            }
            session.commit()

            // Get all periods
            val allPeriods = periodDAO.getAll()
            assertTrue(allPeriods.size >= 3)
        } finally {
            session.close()
        }
    }

    @Test
    fun `test period date range`() {
        val session = dataSource.openSession()
        try {
            val periodDAO = dataSource.getPeriodDAO(session)

            // Create period with specific date range
            val startDate = Date(System.currentTimeMillis() - 365L * 24 * 60 * 60 * 1000)
            val endDate = Date(System.currentTimeMillis())
            
            val period = Period().apply {
                setStartDate(startDate)
                setEndDate(endDate)
            }
            periodDAO.save(period)
            session.commit()

            // Verify dates
            assertTrue(period.id > 0)
            val allPeriods = periodDAO.getAll()
            val retrieved = allPeriods.find { it.id == period.id }
            assertNotNull(retrieved)
            assertTrue(retrieved!!.endDate.after(retrieved.startDate))
        } finally {
            session.close()
        }
    }
}
