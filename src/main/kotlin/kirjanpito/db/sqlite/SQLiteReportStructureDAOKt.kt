package kirjanpito.db.sqlite

import kirjanpito.db.*
import kirjanpito.db.sql.SQLReportStructureDAOKt
import java.sql.PreparedStatement
import java.sql.SQLException

/**
 * SQLite-specific implementation of ReportStructure DAO.
 * 
 * @author Tommi Helineva (original Java)
 * @author Kotlin migration by Claude
 */
class SQLiteReportStructureDAOKt(session: Session) : SQLReportStructureDAOKt() {
    private val session: Session = session
    
    companion object {
        private const val SELECT_BY_ID = """
            SELECT id, data 
            FROM report_structure 
            WHERE id = ?
        """
        
        private const val INSERT = """
            INSERT INTO report_structure (id, data) 
            VALUES (?, ?)
        """
        
        private const val UPDATE = """
            UPDATE report_structure 
            SET id=?, data=? 
            WHERE id = ?
        """
    }
    
    @Throws(SQLException::class)
    override fun getSelectByIdQuery(): PreparedStatement = 
        session.prepareStatement(SELECT_BY_ID)
    
    @Throws(SQLException::class)
    override fun getInsertQuery(): PreparedStatement = 
        session.prepareStatement(INSERT)
    
    @Throws(SQLException::class)
    override fun getUpdateQuery(): PreparedStatement = 
        session.prepareStatement(UPDATE)
}

