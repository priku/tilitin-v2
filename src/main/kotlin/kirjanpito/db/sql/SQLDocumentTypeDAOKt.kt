package kirjanpito.db.sql

import kirjanpito.db.*
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException

/**
 * Kotlin implementation of SQLDocumentTypeDAO.
 * 
 * Abstract base class for database-specific DocumentType DAO implementations.
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
abstract class SQLDocumentTypeDAOKt : DocumentTypeDAO {
    
    /**
     * Hakee tietokannasta kaikkien tositelajien tiedot numerojärjestyksessä.
     */
    override fun getAll(): List<DocumentType> = withDataAccess {
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
     * Tallentaa tositelajin tiedot tietokantaan.
     */
    override fun save(documentType: DocumentType): Unit = withDataAccess {
        if (documentType.id == 0) {
            executeInsertQuery(documentType)
        } else {
            executeUpdateQuery(documentType)
        }
    }
    
    /**
     * Poistaa tositelajin tiedot tietokannasta.
     */
    override fun delete(typeId: Int): Unit = withDataAccess {
        getDeleteQuery().use { stmt ->
            stmt.setInt(1, typeId)
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
     * Lukee ResultSetistä rivin kentät ja luo DocumentType-olion.
     */
    @Throws(SQLException::class)
    protected open fun createObject(rs: ResultSet): DocumentType {
        return rs.toDocumentTypeData().toDocumentType()
    }
    
    /**
     * Asettaa valmistellun kyselyn parametreiksi olion tiedot.
     */
    @Throws(SQLException::class)
    protected open fun setValuesToStatement(stmt: PreparedStatement, obj: DocumentType) {
        stmt.setDocumentTypeValues(obj.toDocumentTypeData())
    }
    
    // ========================================================================
    // Private Helper Methods
    // ========================================================================
    
    /**
     * Lisää tositelajin tiedot tietokantaan.
     */
    @Throws(SQLException::class)
    private fun executeInsertQuery(obj: DocumentType) {
        getInsertQuery().use { stmt ->
            setValuesToStatement(stmt, obj)
            stmt.executeUpdate()
        }
        obj.id = getGeneratedKey()
    }
    
    /**
     * Päivittää tositelajin tiedot tietokantaan.
     */
    @Throws(SQLException::class)
    private fun executeUpdateQuery(obj: DocumentType) {
        getUpdateQuery().use { stmt ->
            setValuesToStatement(stmt, obj)
            stmt.setInt(5, obj.id)
            stmt.executeUpdate()
        }
    }
}

