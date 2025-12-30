package kirjanpito.db.sql

import kirjanpito.db.*
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException

/**
 * Kotlin implementation of SQLEntryTemplateDAO.
 * 
 * Abstract base class for database-specific EntryTemplate DAO implementations.
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
abstract class SQLEntryTemplateDAOKt : EntryTemplateDAO {
    
    /**
     * Hakee tietokannasta kaikkien vientimallien tiedot numerojärjestyksessä.
     */
    override fun getAll(): List<EntryTemplate> = withDataAccess {
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
     * Tallentaa vientimallin tiedot tietokantaan.
     */
    override fun save(template: EntryTemplate): Unit = withDataAccess {
        if (template.id == 0) {
            executeInsertQuery(template)
        } else {
            executeUpdateQuery(template)
        }
    }
    
    /**
     * Poistaa vientimallin tiedot tietokannasta.
     */
    override fun delete(templateId: Int): Unit = withDataAccess {
        getDeleteQuery().use { stmt ->
            stmt.setInt(1, templateId)
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
     * Lukee ResultSetistä rivin kentät ja luo EntryTemplate-olion.
     * 
     * Default implementation uses standard BigDecimal reading.
     * SQLite override reads amount as String.
     */
    @Throws(SQLException::class)
    protected open fun createObject(rs: ResultSet): EntryTemplate {
        return EntryTemplate().apply {
            id = rs.getInt(1)
            number = rs.getInt(2)
            name = rs.getString(3)
            accountId = rs.getInt(4)
            setDebit(rs.getBoolean(5))
            amount = rs.getBigDecimal(6)
            description = rs.getString(7)
            rowNumber = rs.getInt(8)
        }
    }
    
    /**
     * Asettaa valmistellun kyselyn parametreiksi olion tiedot.
     * 
     * Default implementation uses standard BigDecimal.
     * SQLite override writes amount as String.
     */
    @Throws(SQLException::class)
    protected open fun setValuesToStatement(stmt: PreparedStatement, obj: EntryTemplate) {
        stmt.setInt(1, obj.number)
        stmt.setString(2, obj.name)
        stmt.setInt(3, obj.accountId)
        stmt.setBoolean(4, obj.isDebit())
        stmt.setBigDecimal(5, obj.amount)
        stmt.setString(6, obj.description)
        stmt.setInt(7, obj.rowNumber)
    }
    
    // ========================================================================
    // Private Helper Methods
    // ========================================================================
    
    /**
     * Lisää vientimallin tiedot tietokantaan.
     */
    @Throws(SQLException::class)
    private fun executeInsertQuery(obj: EntryTemplate) {
        getInsertQuery().use { stmt ->
            setValuesToStatement(stmt, obj)
            stmt.executeUpdate()
        }
        obj.id = getGeneratedKey()
    }
    
    /**
     * Päivittää vientimallin tiedot tietokantaan.
     */
    @Throws(SQLException::class)
    private fun executeUpdateQuery(obj: EntryTemplate) {
        getUpdateQuery().use { stmt ->
            setValuesToStatement(stmt, obj)
            stmt.setInt(8, obj.id)
            stmt.executeUpdate()
        }
    }
}

