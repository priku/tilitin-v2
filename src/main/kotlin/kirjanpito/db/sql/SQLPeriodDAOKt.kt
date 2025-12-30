package kirjanpito.db.sql

import kirjanpito.db.*
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException

/**
 * Kotlin implementation of SQLPeriodDAO.
 * 
 * Abstract base class for database-specific Period DAO implementations.
 * Provides CRUD operations using Kotlin idioms and extension functions.
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
 * @author Kotlin migration by Claude
 */
abstract class SQLPeriodDAOKt : PeriodDAO {
    
    /**
     * Hakee tietokannasta kaikkien tilikausien tiedot aikajärjestyksessä.
     */
    override fun getAll(): List<Period> = withDataAccess {
        getSelectAllQuery().use { stmt ->
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
     * Hakee tietokannasta nykyisen tilikauden tiedot.
     */
    override fun getCurrent(): Period? = withDataAccess {
        getSelectCurrentQuery().use { stmt ->
            stmt.executeQuery().use { rs ->
                if (rs.next()) createObject(rs) else null
            }
        }
    }
    
    /**
     * Tallentaa tilikauden tiedot tietokantaan.
     */
    override fun save(period: Period): Unit = withDataAccess {
        if (period.id == 0) {
            executeInsertQuery(period)
        } else {
            executeUpdateQuery(period)
        }
    }
    
    /**
     * Poistaa tilikauden tiedot tietokannasta.
     */
    override fun delete(periodId: Int): Unit = withDataAccess {
        getDeleteQuery().use { stmt ->
            stmt.setInt(1, periodId)
            stmt.executeUpdate()
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
    // Protected Methods - Can be overridden for database-specific behavior
    // ========================================================================
    
    /**
     * Lukee ResultSetistä rivin kentät ja luo Period-olion.
     */
    @Throws(SQLException::class)
    protected open fun createObject(rs: ResultSet): Period {
        return rs.toPeriodData().toPeriod()
    }
    
    /**
     * Asettaa valmistellun kyselyn parametreiksi olion tiedot.
     */
    @Throws(SQLException::class)
    protected open fun setValuesToStatement(stmt: PreparedStatement, obj: Period) {
        stmt.setPeriodValues(obj.toPeriodData())
    }
    
    // ========================================================================
    // Private Helper Methods
    // ========================================================================
    
    /**
     * Lisää tilikauden tiedot tietokantaan.
     */
    @Throws(SQLException::class)
    private fun executeInsertQuery(obj: Period) {
        getInsertQuery().use { stmt ->
            setValuesToStatement(stmt, obj)
            stmt.executeUpdate()
        }
        obj.id = getGeneratedKey()
    }
    
    /**
     * Päivittää tilikauden tiedot tietokantaan.
     */
    @Throws(SQLException::class)
    private fun executeUpdateQuery(obj: Period) {
        getUpdateQuery().use { stmt ->
            setValuesToStatement(stmt, obj)
            stmt.setInt(4, obj.id)
            stmt.executeUpdate()
        }
    }
}

