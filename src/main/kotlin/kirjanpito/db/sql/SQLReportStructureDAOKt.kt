package kirjanpito.db.sql

import kirjanpito.db.*
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException

/**
 * Kotlin implementation of SQLReportStructureDAO.
 * 
 * Abstract base class for database-specific ReportStructure DAO implementations.
 * Provides CRUD operations using Kotlin idioms and extension functions.
 * 
 * Subclasses must implement:
 * - getSelectByIdQuery()
 * - getInsertQuery()
 * - getUpdateQuery()
 * 
 * @author Tommi Helineva (original Java)
 * @author Kotlin migration by Claude
 */
abstract class SQLReportStructureDAOKt : ReportStructureDAO {
    
    /**
     * Hakee tulosteen rakennetiedot tunnisteen perusteella.
     */
    override fun getById(id: String): ReportStructure? = withDataAccess {
        getSelectByIdQuery().use { stmt ->
            stmt.setString(1, id)
            stmt.executeQuery().use { rs ->
                if (rs.next()) createObject(rs) else null
            }
        }
    }
    
    /**
     * Tallentaa tulosteen rakennetiedot tietokantaan.
     */
    override fun save(structure: ReportStructure): Unit = withDataAccess {
        if (!executeUpdateQuery(structure)) {
            executeInsertQuery(structure)
        }
    }
    
    // ========================================================================
    // Protected Abstract Methods - Must be implemented by subclasses
    // ========================================================================
    
    @Throws(SQLException::class)
    protected abstract fun getSelectByIdQuery(): PreparedStatement
    
    @Throws(SQLException::class)
    protected abstract fun getInsertQuery(): PreparedStatement
    
    @Throws(SQLException::class)
    protected abstract fun getUpdateQuery(): PreparedStatement
    
    // ========================================================================
    // Protected Methods - Can be overridden for database-specific behavior
    // ========================================================================
    
    /**
     * Lukee ResultSetistä rivin kentät ja luo ReportStructure-olion.
     */
    @Throws(SQLException::class)
    protected open fun createObject(rs: ResultSet): ReportStructure {
        return ReportStructure().apply {
            id = rs.getString(1)
            data = rs.getString(2)
        }
    }
    
    /**
     * Asettaa valmistellun kyselyn parametreiksi olion tiedot.
     */
    @Throws(SQLException::class)
    protected open fun setValuesToStatement(stmt: PreparedStatement, obj: ReportStructure) {
        stmt.setString(1, obj.id)
        stmt.setString(2, obj.data)
    }
    
    // ========================================================================
    // Private Helper Methods
    // ========================================================================
    
    /**
     * Lisää tulosteen rakennemäärittelyt tietokantaan.
     */
    @Throws(SQLException::class)
    private fun executeInsertQuery(obj: ReportStructure) {
        getInsertQuery().use { stmt ->
            setValuesToStatement(stmt, obj)
            stmt.executeUpdate()
        }
    }
    
    /**
     * Päivittää tulosteen rakennemäärittelyt tietokantaan.
     */
    @Throws(SQLException::class)
    private fun executeUpdateQuery(obj: ReportStructure): Boolean {
        return getUpdateQuery().use { stmt ->
            setValuesToStatement(stmt, obj)
            stmt.setString(3, obj.id)
            stmt.executeUpdate() > 0
        }
    }
}

