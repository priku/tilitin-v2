package kirjanpito.db.sql

import kirjanpito.db.*
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException

/**
 * Kotlin implementation of SQLCOAHeadingDAO.
 * 
 * Abstract base class for database-specific COAHeading DAO implementations.
 * Provides CRUD operations using Kotlin idioms and extension functions.
 * 
 * Subclasses must implement:
 * - getSelectAllQuery()
 * - getInsertQuery()
 * - getUpdateQuery()
 * - getDeleteQuery()
 * - getGeneratedKey()
 * 
 * @author Tommi Helineva (original Java)
 * @author Kotlin migration by Claude
 */
abstract class SQLCOAHeadingDAOKt : COAHeadingDAO {
    
    /**
     * Hakee kaikkien otsikoiden tiedot tietokannasta numerojärjestyksessä.
     */
    override fun getAll(): List<COAHeading> = withDataAccess {
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
     * Tallentaa tilikartan otsikon tiedot tietokantaan.
     */
    override fun save(heading: COAHeading): Unit = withDataAccess {
        if (heading.id == 0) {
            executeInsertQuery(heading)
        } else {
            executeUpdateQuery(heading)
        }
    }
    
    /**
     * Poistaa tilikartan otsikon tiedot tietokannasta.
     */
    override fun delete(headingId: Int): Unit = withDataAccess {
        getDeleteQuery().use { stmt ->
            stmt.setInt(1, headingId)
            stmt.executeUpdate()
        }
    }
    
    // ========================================================================
    // Protected Abstract Methods - Must be implemented by subclasses
    // ========================================================================
    
    @Throws(SQLException::class)
    protected abstract fun getSelectAllQuery(): PreparedStatement
    
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
     * Lukee ResultSetistä rivin kentät ja luo COAHeading-olion.
     */
    @Throws(SQLException::class)
    protected open fun createObject(rs: ResultSet): COAHeading {
        return rs.toCOAHeadingData().toCOAHeading()
    }
    
    /**
     * Asettaa valmistellun kyselyn parametreiksi olion tiedot.
     */
    @Throws(SQLException::class)
    protected open fun setValuesToStatement(stmt: PreparedStatement, obj: COAHeading) {
        stmt.setCOAHeadingValues(obj.toCOAHeadingData())
    }
    
    // ========================================================================
    // Private Helper Methods
    // ========================================================================
    
    /**
     * Lisää otsikon tiedot tietokantaan.
     */
    @Throws(SQLException::class)
    private fun executeInsertQuery(obj: COAHeading) {
        getInsertQuery().use { stmt ->
            setValuesToStatement(stmt, obj)
            stmt.executeUpdate()
        }
        obj.id = getGeneratedKey()
    }
    
    /**
     * Päivittää otsikon tiedot tietokantaan.
     */
    @Throws(SQLException::class)
    private fun executeUpdateQuery(obj: COAHeading) {
        getUpdateQuery().use { stmt ->
            setValuesToStatement(stmt, obj)
            stmt.setInt(4, obj.id)
            stmt.executeUpdate()
        }
    }
}

