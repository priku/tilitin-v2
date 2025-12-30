package kirjanpito.db.sqlite

import kirjanpito.db.*
import kirjanpito.db.sql.SQLSettingsDAOKt
import java.sql.PreparedStatement
import java.sql.SQLException

/**
 * SQLite-specific implementation of Settings DAO.
 * 
 * @author Tommi Helineva (original Java)
 * @author Kotlin migration by Claude
 */
class SQLiteSettingsDAOKt(session: Session) : SQLSettingsDAOKt() {
    private val session: Session = session
    
    companion object {
        private const val SELECT = """
            SELECT name, business_id, current_period_id, document_type_id, properties 
            FROM settings
        """
        
        private const val INSERT = """
            INSERT INTO settings (version, name, business_id, current_period_id, document_type_id, properties) 
            VALUES (15, ?, ?, ?, ?, ?)
        """
        
        private const val UPDATE = """
            UPDATE settings 
            SET name=?, business_id=?, current_period_id=?, document_type_id=?, properties=?
        """
    }
    
    @Throws(SQLException::class)
    override fun getSelectQuery(): PreparedStatement = 
        session.prepareStatement(SELECT)
    
    @Throws(SQLException::class)
    override fun getInsertQuery(): PreparedStatement = 
        session.prepareStatement(INSERT)
    
    @Throws(SQLException::class)
    override fun getUpdateQuery(): PreparedStatement = 
        session.prepareStatement(UPDATE)
}

