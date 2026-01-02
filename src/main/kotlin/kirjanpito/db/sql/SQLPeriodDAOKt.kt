package kirjanpito.db.sql

import kirjanpito.db.*
import java.sql.Date
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException

/**
 * Kotlin implementation of SQLPeriodDAO.
 * 
 * Abstract base class for database-specific Period DAO implementations.
 * Provides CRUD operations using Kotlin idioms.
 * 
 * Subclasses must implement:
 * - getSelectAllQuery()
 * - getSelectCurrentQuery()
 * - getInsertQuery()
 * - getUpdateQuery()
 * - getDeleteQuery()
 * - getGeneratedKey()
 * 
 * @author Tommi Helineva (original Java)
 * @author Kotlin migration
 */
abstract class SQLPeriodDAOKt : PeriodDAO {
    
    /**
     * Hakee tietokannasta kaikkien tilikausien tiedot aikajärjestyksessä.
     * Returns a mutable ArrayList for compatibility with Java code that modifies the list.
     */
    @Throws(DataAccessException::class)
    override fun getAll(): List<Period> {
        try {
            getSelectAllQuery().use { stmt ->
                stmt.executeQuery().use { rs ->
                    val periods = ArrayList<Period>()
                    while (rs.next()) {
                        periods.add(createObject(rs))
                    }
                    return periods
                }
            }
        } catch (e: SQLException) {
            throw DataAccessException(e.message, e)
        }
    }
    
    /**
     * Hakee tietokannasta nykyisen tilikauden tiedot.
     */
    @Throws(DataAccessException::class)
    override fun getCurrent(): Period? {
        try {
            getSelectCurrentQuery().use { stmt ->
                stmt.executeQuery().use { rs ->
                    return if (rs.next()) createObject(rs) else null
                }
            }
        } catch (e: SQLException) {
            throw DataAccessException(e.message, e)
        }
    }
    
    /**
     * Tallentaa tilikauden tiedot tietokantaan.
     */
    @Throws(DataAccessException::class)
    override fun save(period: Period) {
        try {
            if (period.id == 0) {
                executeInsertQuery(period)
            } else {
                executeUpdateQuery(period)
            }
        } catch (e: SQLException) {
            throw DataAccessException(e.message, e)
        }
    }
    
    /**
     * Poistaa tilikauden tiedot tietokannasta.
     */
    @Throws(DataAccessException::class)
    override fun delete(periodId: Int) {
        try {
            getDeleteQuery().use { stmt ->
                stmt.setInt(1, periodId)
                stmt.executeUpdate()
            }
        } catch (e: SQLException) {
            throw DataAccessException(e.message, e)
        }
    }
    
    // ========================================================================
    // Protected Abstract Methods - Must be implemented by subclasses
    // ========================================================================
    
    @Throws(SQLException::class)
    protected abstract fun getSelectAllQuery(): PreparedStatement
    
    @Throws(SQLException::class)
    protected abstract fun getSelectCurrentQuery(): PreparedStatement
    
    @Throws(SQLException::class)
    protected abstract fun getInsertQuery(): PreparedStatement
    
    @Throws(SQLException::class)
    protected abstract fun getUpdateQuery(): PreparedStatement
    
    @Throws(SQLException::class)
    protected abstract fun getDeleteQuery(): PreparedStatement
    
    @Throws(SQLException::class)
    protected abstract fun getGeneratedKey(): Int
    
    // ========================================================================
    // Protected Methods
    // ========================================================================
    
    /**
     * Lukee ResultSetistä rivin kentät ja luo Period-olion.
     */
    @Throws(SQLException::class)
    protected open fun createObject(rs: ResultSet): Period {
        return Period().apply {
            id = rs.getInt("id")
            startDate = Date(rs.getLong("start_date"))
            endDate = Date(rs.getLong("end_date"))
            isLocked = rs.getInt("locked") != 0
        }
    }
    
    /**
     * Asettaa valmistellun kyselyn parametreiksi olion tiedot.
     */
    @Throws(SQLException::class)
    protected open fun setValuesToStatement(stmt: PreparedStatement, period: Period) {
        stmt.setLong(1, period.startDate.time)
        stmt.setLong(2, period.endDate.time)
        stmt.setInt(3, if (period.isLocked) 1 else 0)
    }
    
    // ========================================================================
    // Private Helper Methods
    // ========================================================================
    
    /**
     * Lisää tilikauden tiedot tietokantaan.
     */
    @Throws(SQLException::class)
    private fun executeInsertQuery(period: Period) {
        getInsertQuery().use { stmt ->
            setValuesToStatement(stmt, period)
            stmt.executeUpdate()
        }
        period.id = getGeneratedKey()
    }
    
    /**
     * Päivittää tilikauden tiedot tietokantaan.
     */
    @Throws(SQLException::class)
    private fun executeUpdateQuery(period: Period) {
        getUpdateQuery().use { stmt ->
            setValuesToStatement(stmt, period)
            stmt.setInt(4, period.id)
            stmt.executeUpdate()
        }
    }
}

