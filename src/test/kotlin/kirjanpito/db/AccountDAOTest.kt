package kirjanpito.db

import kirjanpito.db.sqlite.SQLiteDataSource
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import java.io.File
import java.sql.Date

/**
 * Tests for AccountDAO operations.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AccountDAOTest {

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
    fun `test create and retrieve account`() {
        val session = dataSource.openSession()
        try {
            val accountDAO = dataSource.getAccountDAO(session)
            
            // Create account
            val account = Account().apply {
                setNumber("1000")
                setName("Test Account")
                setType(Account.TYPE_ASSET)
            }
            
            accountDAO.save(account)
            session.commit()
            
            assertTrue(account.id > 0)
            
            // Retrieve account by getting all and finding it
            val allAccounts = accountDAO.getAll()
            val retrieved = allAccounts.find { it.id == account.id }
            assertNotNull(retrieved)
            assertEquals("1000", retrieved!!.number)
            assertEquals("Test Account", retrieved.name)
            assertEquals(Account.TYPE_ASSET, retrieved.type)
        } finally {
            session.close()
        }
    }

    @Test
    fun `test update account`() {
        val session = dataSource.openSession()
        try {
            val accountDAO = dataSource.getAccountDAO(session)
            
            // Create account
            val account = Account().apply {
                setNumber("2000")
                setName("Original Name")
                setType(Account.TYPE_LIABILITY)
            }
            accountDAO.save(account)
            session.commit()
            
            val accountId = account.id
            
            // Update account
            account.setName("Updated Name")
            accountDAO.save(account)
            session.commit()
            
            // Verify update
            val allAccounts = accountDAO.getAll()
            val updated = allAccounts.find { it.id == accountId }
            assertNotNull(updated)
            assertEquals("Updated Name", updated!!.name)
        } finally {
            session.close()
        }
    }

    @Test
    fun `test delete account`() {
        val session = dataSource.openSession()
        try {
            val accountDAO = dataSource.getAccountDAO(session)
            
            // Create account
            val account = Account().apply {
                setNumber("3000")
                setName("To Be Deleted")
                setType(Account.TYPE_ASSET)
            }
            accountDAO.save(account)
            session.commit()
            
            val accountId = account.id
            
            // Delete account
            accountDAO.delete(accountId)
            session.commit()
            
            // Verify deletion
            val allAccounts = accountDAO.getAll()
            val deleted = allAccounts.find { it.id == accountId }
            assertNull(deleted)
        } finally {
            session.close()
        }
    }

    @Test
    fun `test get all accounts`() {
        val session = dataSource.openSession()
        try {
            val accountDAO = dataSource.getAccountDAO(session)
            
            // Create multiple accounts
            for (i in 1..5) {
                val account = Account().apply {
                    setNumber("400$i")
                    setName("Account $i")
                    setType(Account.TYPE_ASSET)
                }
                accountDAO.save(account)
            }
            session.commit()
            
            // Get all accounts
            val allAccounts = accountDAO.getAll()
            assertTrue(allAccounts.size >= 5)
            
            // Verify accounts exist
            val accountNumbers = allAccounts.map { it.number }.toSet()
            assertTrue(accountNumbers.contains("4001"))
            assertTrue(accountNumbers.contains("4005"))
        } finally {
            session.close()
        }
    }

    @Test
    fun `test get account by number`() {
        val session = dataSource.openSession()
        try {
            val accountDAO = dataSource.getAccountDAO(session)
            
            // Create account
            val account = Account().apply {
                setNumber("5000")
                setName("Find Me")
                setType(Account.TYPE_ASSET)
            }
            accountDAO.save(account)
            session.commit()
            
            // Find by number
            val allAccounts = accountDAO.getAll()
            val found = allAccounts.find { it.number == "5000" }
            assertNotNull(found)
            assertEquals("Find Me", found!!.name)
        } finally {
            session.close()
        }
    }
}
