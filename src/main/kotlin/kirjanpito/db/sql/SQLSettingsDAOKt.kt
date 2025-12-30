package kirjanpito.db.sql

import kirjanpito.db.*
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException
import java.sql.Types

/**
 * Kotlin implementation of SQLSettingsDAO.
 * 
 * Abstract base class for database-specific Settings DAO implementations.
 * Provides CRUD operations using Kotlin idioms and extension functions.
 * 
 * Subclasses must implement:
 * - getSelectQuery()
 * - getInsertQuery()
 * - getUpdateQuery()
 * 
 * @author Tommi Helineva (original Java)
 * @author Kotlin migration by Claude
 */
abstract class SQLSettingsDAOKt : SettingsDAO {
    
    /**
     * Hakee tietokannasta asetukset.
     */
    override fun get(): Settings = withDataAccess {
        val settings = Settings()
        
        getSelectQuery().use { stmt ->
            stmt.executeQuery().use { rs ->
                if (rs.next()) {
                    settings.name = rs.getString(1)
                    settings.businessId = rs.getString(2)
                    settings.currentPeriodId = rs.getInt(3)
                    val docTypeId = rs.getInt(4)
                    settings.documentTypeId = if (rs.wasNull()) -1 else docTypeId
                    settings.parseProperties(rs.getString(5))
                }
            }
        }
        
        settings
    }
    
    /**
     * Tallentaa asetukset tietokantaan.
     */
    override fun save(settings: Settings): Unit = withDataAccess {
        getUpdateQuery().use { stmt ->
            setValuesToStatement(stmt, settings)
            val updatedRows = stmt.executeUpdate()
            
            if (updatedRows == 0) {
                getInsertQuery().use { insertStmt ->
                    setValuesToStatement(insertStmt, settings)
                    insertStmt.executeUpdate()
                }
            }
        }
    }
    
    // ========================================================================
    // Protected Abstract Methods - Must be implemented by subclasses
    // ========================================================================
    
    @Throws(SQLException::class)
    protected abstract fun getSelectQuery(): PreparedStatement
    
    @Throws(SQLException::class)
    protected abstract fun getInsertQuery(): PreparedStatement
    
    @Throws(SQLException::class)
    protected abstract fun getUpdateQuery(): PreparedStatement
    
    // ========================================================================
    // Protected Methods - Can be overridden for database-specific behavior
    // ========================================================================
    
    /**
     * Asettaa valmistellun kyselyn parametreiksi olion tiedot.
     */
    @Throws(SQLException::class)
    protected open fun setValuesToStatement(stmt: PreparedStatement, obj: Settings) {
        stmt.setString(1, obj.name)
        stmt.setString(2, obj.businessId)
        stmt.setInt(3, obj.currentPeriodId)
        
        if (obj.documentTypeId < 0) {
            stmt.setNull(4, Types.INTEGER)
        } else {
            stmt.setInt(4, obj.documentTypeId)
        }
        
        stmt.setString(5, obj.propertiesToString())
    }
}

