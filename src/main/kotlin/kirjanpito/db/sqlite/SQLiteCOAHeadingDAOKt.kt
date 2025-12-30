package kirjanpito.db.sqlite

import kirjanpito.db.*
import kirjanpito.db.sql.SQLCOAHeadingDAOKt
import java.sql.PreparedStatement
import java.sql.SQLException

/**
 * SQLite-specific implementation of COAHeading DAO.
 * 
 * @author Tommi Helineva (original Java)
 * @author Kotlin migration by Claude
 */
class SQLiteCOAHeadingDAOKt(session: Session) : SQLCOAHeadingDAOKt() {
    private val session: Session = session
    
    companion object {
        private const val SELECT_ALL = """
            SELECT id, number, text, level 
            FROM coa_heading 
            ORDER BY number, level
        """
        
        private const val INSERT = """
            INSERT INTO coa_heading (id, number, text, level) 
            VALUES (NULL, ?, ?, ?)
        """
        
        private const val UPDATE = """
            UPDATE coa_heading 
            SET number=?, text=?, level=? 
            WHERE id = ?
        """
        
        private const val DELETE = "DELETE FROM coa_heading WHERE id = ?"
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

