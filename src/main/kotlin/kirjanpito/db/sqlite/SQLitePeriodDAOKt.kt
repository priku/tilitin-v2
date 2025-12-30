package kirjanpito.db.sqlite

import kirjanpito.db.*
import kirjanpito.db.sql.SQLPeriodDAOKt
import java.sql.PreparedStatement
import java.sql.SQLException

/**
 * SQLite-specific implementation of Period DAO.
 * 
 * @author Tommi Helineva (original Java)
 * @author Kotlin migration by Claude
 */
class SQLitePeriodDAOKt(session: Session) : SQLPeriodDAOKt() {
    private val session: Session = session
    
    companion object {
        private const val SELECT_ALL = """
            SELECT id, start_date, end_date, locked 
            FROM period 
            ORDER BY start_date
        """
        
        private const val SELECT_CURRENT = """
            SELECT p.id, p.start_date, p.end_date, p.locked 
            FROM period p 
            INNER JOIN settings s ON s.current_period_id = p.id
        """
        
        private const val INSERT = """
            INSERT INTO period (id, start_date, end_date, locked) 
            VALUES (NULL, ?, ?, ?)
        """
        
        private const val UPDATE = """
            UPDATE period 
            SET start_date=?, end_date=?, locked=? 
            WHERE id = ?
        """
        
        private const val DELETE = "DELETE FROM period WHERE id = ?"
    }
    
    @Throws(SQLException::class)
    override fun getSelectAllQuery(): PreparedStatement = 
        session.prepareStatement(SELECT_ALL)
    
    @Throws(SQLException::class)
    override fun getSelectCurrentQuery(): PreparedStatement = 
        session.prepareStatement(SELECT_CURRENT)
    
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

