package kirjanpito.db.sqlite

import kirjanpito.db.*
import kirjanpito.db.sql.SQLDocumentTypeDAOKt
import java.sql.PreparedStatement
import java.sql.SQLException

/**
 * SQLite-specific implementation of DocumentType DAO.
 * 
 * @author Tommi Helineva (original Java)
 * @author Kotlin migration by Claude
 */
class SQLiteDocumentTypeDAOKt(session: Session) : SQLDocumentTypeDAOKt() {
    private val session: Session = session
    
    companion object {
        private const val SELECT_ALL = """
            SELECT id, number, name, number_start, number_end 
            FROM document_type 
            ORDER BY number
        """
        
        private const val INSERT = """
            INSERT INTO document_type (number, name, number_start, number_end) 
            VALUES (?, ?, ?, ?)
        """
        
        private const val UPDATE = """
            UPDATE document_type 
            SET number=?, name=?, number_start=?, number_end=? 
            WHERE id = ?
        """
        
        private const val DELETE = "DELETE FROM document_type WHERE id = ?"
    }
    
    @Throws(SQLException::class)
    override fun getSelectAllQuery(): PreparedStatement = 
        session.prepareStatement(SELECT_ALL)
    
    @Throws(SQLException::class)
    override fun getInsertQuery(): PreparedStatement = 
        session.prepareStatement(INSERT)
    
    @Throws(SQLException::class)
    override fun getUpdateQuery(): PreparedStatement = 
        session.prepareStatement(UPDATE)
    
    @Throws(SQLException::class)
    override fun getDeleteQuery(): PreparedStatement = 
        session.prepareStatement(DELETE)
    
    @Throws(SQLException::class)
    override fun getGeneratedKey(): Int = session.insertId
}

