package kirjanpito.db.sql

import kirjanpito.db.*
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException
import java.sql.Timestamp
import java.util.Date

/**
 * Kotlin implementation of SQLEntryDAO.
 * 
 * Abstract base class for database-specific Entry DAO implementations.
 * Provides CRUD operations using Kotlin idioms and extension functions.
 * 
 * Subclasses must implement:
 * - getSelectByDocumentIdQuery()
 * - getSelectByDocumentIdsQuery()
 * - getSelectByPeriodIdOrderByNumberQuery()
 * - getSelectByPeriodIdOrderByDateQuery()
 * - getSelectByPeriodIdOrderByAccountAndNumberQuery()
 * - getSelectByPeriodIdOrderByAccountAndDateQuery()
 * - getSelectByAccountIdQuery()
 * - getSelectByPeriodIdAndAccountIdOrderByDateQuery()
 * - getSelectByPeriodIdAndAccountIdOrderByNumberQuery()
 * - getSelectByDateQuery()
 * - getSelectByPeriodIdAndDateQuery()
 * - getSelectByPeriodIdAndDateAndNumberQuery()
 * - getSelectByPeriodIdAndNumberQuery()
 * - getInsertQuery()
 * - getUpdateQuery()
 * - getDeleteQuery()
 * - getDeleteByPeriodIdQuery()
 * - getGeneratedKey()
 * 
 * @author Tommi Helineva (original Java)
 * @author Kotlin migration by Claude
 */
abstract class SQLEntryDAOKt : EntryDAO {
    
    /**
     * Hakee tietokannasta tiettyyn tositteeseen kuuluvat viennit.
     */
    override fun getByDocumentId(documentId: Int): List<Entry> = withDataAccess {
        getSelectByDocumentIdQuery().use { stmt ->
            stmt.setInt(1, documentId)
            stmt.executeQuery().use { rs ->
                buildList {
                    while (rs.next()) {
                        add(createObject(rs))
                    }
                }
            }
        }
    }
    
    /**
     * Hakee tietokannasta tiettyihin tositteisiin kuuluvat viennit.
     */
    override fun getByDocuments(
        documents: List<Document>,
        callback: DTOCallback<Entry>
    ): Unit = withDataAccess {
        val documentIds = documents.joinToString(",") { it.id.toString() }
        
        getSelectByDocumentIdsQuery(documentIds).use { stmt ->
            stmt.executeQuery().use { rs ->
                while (rs.next()) {
                    callback.process(createObject(rs))
                }
            }
        }
    }
    
    /**
     * Hakee tietokannasta kaikki tietyn tilikauden viennit.
     */
    override fun getByPeriodId(
        periodId: Int,
        orderBy: Int,
        callback: DTOCallback<Entry>
    ): Unit = withDataAccess {
        val stmt = when (orderBy) {
            EntryDAO.ORDER_BY_DOCUMENT_NUMBER -> getSelectByPeriodIdOrderByNumberQuery()
            EntryDAO.ORDER_BY_DOCUMENT_DATE -> getSelectByPeriodIdOrderByDateQuery()
            EntryDAO.ORDER_BY_ACCOUNT_NUMBER_AND_DOCUMENT_NUMBER -> getSelectByPeriodIdOrderByAccountAndNumberQuery()
            EntryDAO.ORDER_BY_ACCOUNT_NUMBER_AND_DOCUMENT_DATE -> getSelectByPeriodIdOrderByAccountAndDateQuery()
            else -> throw IllegalArgumentException("Invalid orderBy: $orderBy")
        }
        
        stmt.use { s ->
            s.setInt(1, periodId)
            s.executeQuery().use { rs ->
                while (rs.next()) {
                    callback.process(createObject(rs))
                }
            }
        }
    }
    
    /**
     * Hakee tietokannasta tietyn tilikauden viennit, jotka kohdistuvat tiettyyn tiliin.
     */
    override fun getByPeriodIdAndAccountId(
        periodId: Int,
        accountId: Int,
        orderBy: Int,
        callback: DTOCallback<Entry>
    ): Unit = withDataAccess {
        val stmt = if (periodId < 0) {
            getSelectByAccountIdQuery().apply { setInt(1, accountId) }
        } else when (orderBy) {
            EntryDAO.ORDER_BY_DOCUMENT_NUMBER -> getSelectByPeriodIdAndAccountIdOrderByNumberQuery().apply {
                setInt(1, periodId)
                setInt(2, accountId)
            }
            EntryDAO.ORDER_BY_DOCUMENT_DATE -> getSelectByPeriodIdAndAccountIdOrderByDateQuery().apply {
                setInt(1, periodId)
                setInt(2, accountId)
            }
            else -> throw IllegalArgumentException("Invalid orderBy: $orderBy")
        }
        
        stmt.use { s ->
            s.executeQuery().use { rs ->
                while (rs.next()) {
                    callback.process(createObject(rs))
                }
            }
        }
    }
    
    /**
     * Hakee tietokannasta tietyn tilikauden viennit tietyltä aikaväliltä.
     */
    override fun getByPeriodIdAndDate(
        periodId: Int,
        startDate: Date,
        endDate: Date,
        callback: DTOCallback<Entry>
    ): Unit = withDataAccess {
        getByPeriodIdAndDate(periodId, startDate, endDate, -1, callback)
    }
    
    /**
     * Hakee tietokannasta tietyn tilikauden viennit tietyltä aikaväliltä.
     */
    override fun getByPeriodIdAndDate(
        periodId: Int,
        startDate: Date,
        endDate: Date,
        startNumber: Int,
        callback: DTOCallback<Entry>
    ): Unit = withDataAccess {
        val stmt = when {
            startNumber >= 0 -> getSelectByPeriodIdAndDateAndNumberQuery().apply {
                setInt(1, periodId)
                setTimestamp(2, Timestamp(startDate.time))
                setTimestamp(3, Timestamp(endDate.time))
                setInt(4, startNumber)
            }
            periodId > 0 -> getSelectByPeriodIdAndDateQuery().apply {
                setInt(1, periodId)
                setTimestamp(2, Timestamp(startDate.time))
                setTimestamp(3, Timestamp(endDate.time))
            }
            else -> getSelectByDateQuery().apply {
                setTimestamp(1, Timestamp(startDate.time))
                setTimestamp(2, Timestamp(endDate.time))
            }
        }
        
        stmt.use { s ->
            s.executeQuery().use { rs ->
                while (rs.next()) {
                    callback.process(createObject(rs))
                }
            }
        }
    }
    
    /**
     * Hakee tietokannasta viennit tositenumeroväliltä tietyltä tilikaudelta.
     */
    override fun getByPeriodIdAndNumber(
        periodId: Int,
        startNumber: Int,
        endNumber: Int,
        callback: DTOCallback<Entry>
    ): Unit = withDataAccess {
        getSelectByPeriodIdAndNumberQuery().use { stmt ->
            stmt.setInt(1, periodId)
            stmt.setInt(2, startNumber)
            stmt.setInt(3, endNumber)
            stmt.executeQuery().use { rs ->
                while (rs.next()) {
                    callback.process(createObject(rs))
                }
            }
        }
    }
    
    /**
     * Tallentaa viennin tiedot tietokantaan.
     */
    override fun save(entry: Entry): Unit = withDataAccess {
        if (entry.id == 0) {
            executeInsertQuery(entry)
        } else {
            executeUpdateQuery(entry)
        }
    }
    
    /**
     * Poistaa viennin tiedot tietokannasta.
     */
    override fun delete(entryId: Int): Unit = withDataAccess {
        getDeleteQuery().use { stmt ->
            stmt.setInt(1, entryId)
            stmt.executeUpdate()
        }
    }
    
    /**
     * Poistaa tietyn tilikauden kaikki viennit tietokannasta.
     */
    override fun deleteByPeriodId(periodId: Int): Unit = withDataAccess {
        getDeleteByPeriodIdQuery().use { stmt ->
            stmt.setInt(1, periodId)
            stmt.executeUpdate()
        }
    }
    
    // ========================================================================
    // Protected Abstract Methods - Must be implemented by subclasses
    // ========================================================================
    
    @Throws(SQLException::class)
    protected abstract fun getSelectByDocumentIdQuery(): PreparedStatement
    
    @Throws(SQLException::class)
    protected abstract fun getSelectByDocumentIdsQuery(documentIds: String): PreparedStatement
    
    @Throws(SQLException::class)
    protected abstract fun getSelectByPeriodIdOrderByNumberQuery(): PreparedStatement
    
    @Throws(SQLException::class)
    protected abstract fun getSelectByPeriodIdOrderByDateQuery(): PreparedStatement
    
    @Throws(SQLException::class)
    protected abstract fun getSelectByPeriodIdOrderByAccountAndNumberQuery(): PreparedStatement
    
    @Throws(SQLException::class)
    protected abstract fun getSelectByPeriodIdOrderByAccountAndDateQuery(): PreparedStatement
    
    @Throws(SQLException::class)
    protected abstract fun getSelectByAccountIdQuery(): PreparedStatement
    
    @Throws(SQLException::class)
    protected abstract fun getSelectByPeriodIdAndAccountIdOrderByDateQuery(): PreparedStatement
    
    @Throws(SQLException::class)
    protected abstract fun getSelectByPeriodIdAndAccountIdOrderByNumberQuery(): PreparedStatement
    
    @Throws(SQLException::class)
    protected abstract fun getSelectByDateQuery(): PreparedStatement
    
    @Throws(SQLException::class)
    protected abstract fun getSelectByPeriodIdAndDateQuery(): PreparedStatement
    
    @Throws(SQLException::class)
    protected abstract fun getSelectByPeriodIdAndDateAndNumberQuery(): PreparedStatement
    
    @Throws(SQLException::class)
    protected abstract fun getSelectByPeriodIdAndNumberQuery(): PreparedStatement
    
    @Throws(SQLException::class)
    protected abstract fun getInsertQuery(): PreparedStatement
    
    @Throws(SQLException::class)
    protected abstract fun getUpdateQuery(): PreparedStatement
    
    @Throws(SQLException::class)
    protected abstract fun getDeleteQuery(): PreparedStatement
    
    @Throws(SQLException::class)
    protected abstract fun getDeleteByPeriodIdQuery(): PreparedStatement
    
    @Throws(SQLException::class)
    protected abstract fun getGeneratedKey(): Int
    
    // ========================================================================
    // Protected Methods - Can be overridden for database-specific behavior
    // ========================================================================
    
    /**
     * Lukee ResultSetistä rivin kentät ja luo Entry-olion.
     * 
     * Default implementation uses standard BigDecimal reading.
     * SQLite override reads amount as String.
     */
    @Throws(SQLException::class)
    protected open fun createObject(rs: ResultSet): Entry {
        return rs.toEntryData().toEntry()
    }
    
    /**
     * Asettaa valmistellun kyselyn parametreiksi olion tiedot.
     * 
     * Default implementation uses standard BigDecimal.
     * SQLite override writes amount as String.
     */
    @Throws(SQLException::class)
    protected open fun setValuesToStatement(stmt: PreparedStatement, obj: Entry) {
        stmt.setEntryValues(obj.toEntryData())
    }
    
    // ========================================================================
    // Private Helper Methods
    // ========================================================================
    
    /**
     * Lisää viennin tiedot tietokantaan.
     */
    @Throws(SQLException::class)
    private fun executeInsertQuery(obj: Entry) {
        getInsertQuery().use { stmt ->
            setValuesToStatement(stmt, obj)
            stmt.executeUpdate()
        }
        obj.id = getGeneratedKey()
    }
    
    /**
     * Päivittää viennin tiedot tietokantaan.
     */
    @Throws(SQLException::class)
    private fun executeUpdateQuery(obj: Entry) {
        getUpdateQuery().use { stmt ->
            setValuesToStatement(stmt, obj)
            stmt.setInt(8, obj.id)
            stmt.executeUpdate()
        }
    }
}

