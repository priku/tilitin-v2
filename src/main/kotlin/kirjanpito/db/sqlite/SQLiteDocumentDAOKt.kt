package kirjanpito.db.sqlite

import kirjanpito.db.*
import kirjanpito.db.sql.SQLDocumentDAOKt
import java.sql.PreparedStatement
import java.sql.SQLException

/**
 * SQLite-specific implementation of Document DAO.
 * 
 * @author Tommi Helineva (original Java)
 * @author Kotlin migration by Claude
 */
class SQLiteDocumentDAOKt(session: Session) : SQLDocumentDAOKt() {
    private val session: Session = session
    
    companion object {
        private const val SELECT_LAST_DOCUMENT = """
            SELECT number, date 
            FROM document 
            WHERE period_id = ? AND number BETWEEN ? AND ? 
            ORDER BY number DESC 
            LIMIT 1
        """
        
        private const val SELECT_BY_PERIOD_ID = """
            SELECT id, number, period_id, date 
            FROM document 
            WHERE period_id = ? AND number >= ? 
            ORDER BY number
        """
        
        private const val SELECT_COUNT_BY_PERIOD_ID = """
            SELECT count(*) 
            FROM document 
            WHERE period_id = ? AND number >= ?
        """
        
        private const val SELECT_BY_PERIOD_ID_AND_NUMBER = """
            SELECT id, number, period_id, date 
            FROM document 
            WHERE period_id = ? AND number BETWEEN ? AND ? 
            ORDER BY number 
            LIMIT ? OFFSET ?
        """
        
        private const val SELECT_COUNT_BY_PERIOD_ID_AND_NUMBER = """
            SELECT count(*) 
            FROM document 
            WHERE period_id = ? AND number BETWEEN ? AND ?
        """
        
        private const val SELECT_NUMBER_BY_PERIOD_ID_AND_NUMBER = """
            SELECT number 
            FROM document 
            WHERE period_id = ? AND number BETWEEN ? AND ? 
            ORDER BY number
        """
        
        private const val SELECT_COUNT_BY_PERIOD_ID_AND_PHRASE = """
            SELECT count(*) 
            FROM document d 
            WHERE d.period_id = ? 
            AND (SELECT count(*) FROM entry e WHERE e.document_id = d.id AND e.description LIKE ?) > 0
        """
        
        private const val SELECT_BY_PERIOD_ID_AND_PHRASE = """
            SELECT d.id, d.number, d.period_id, d.date 
            FROM document d 
            WHERE d.period_id = ? 
            AND (SELECT count(*) FROM entry e WHERE e.document_id = d.id AND e.description LIKE ?) > 0 
            ORDER BY d.number 
            LIMIT ? OFFSET ?
        """
        
        private const val SELECT_BY_PERIOD_ID_AND_DATE = """
            SELECT id, number, period_id, date 
            FROM document 
            WHERE period_id = ? AND date BETWEEN ? AND ? 
            ORDER BY number
        """
        
        private const val INSERT = """
            INSERT INTO document (number, period_id, date) 
            VALUES (?, ?, ?)
        """
        
        private const val UPDATE = """
            UPDATE document 
            SET number=?, period_id=?, date=? 
            WHERE id = ?
        """
        
        private const val DELETE = "DELETE FROM document WHERE id = ?"
        
        private const val DELETE_BY_PERIOD_ID = "DELETE FROM document WHERE period_id = ?"
        
        private const val NUMBER_SHIFT = """
            UPDATE document 
            SET number = number + ? 
            WHERE period_id = ? AND number BETWEEN ? AND ?
        """
    }
    
    @Throws(SQLException::class)
    override fun getSelectLastDocumentQuery(): PreparedStatement = 
        session.prepareStatement(SELECT_LAST_DOCUMENT)
    
    @Throws(SQLException::class)
    override fun getSelectByPeriodIdQuery(): PreparedStatement = 
        session.prepareStatement(SELECT_BY_PERIOD_ID)
    
    @Throws(SQLException::class)
    override fun getSelectCountByPeriodIdQuery(): PreparedStatement = 
        session.prepareStatement(SELECT_COUNT_BY_PERIOD_ID)
    
    @Throws(SQLException::class)
    override fun getSelectByPeriodIdAndNumberQuery(): PreparedStatement = 
        session.prepareStatement(SELECT_BY_PERIOD_ID_AND_NUMBER)
    
    @Throws(SQLException::class)
    override fun getSelectCountByPeriodIdAndNumberQuery(): PreparedStatement = 
        session.prepareStatement(SELECT_COUNT_BY_PERIOD_ID_AND_NUMBER)
    
    @Throws(SQLException::class)
    override fun getSelectNumberByPeriodIdAndNumberQuery(): PreparedStatement = 
        session.prepareStatement(SELECT_NUMBER_BY_PERIOD_ID_AND_NUMBER)
    
    @Throws(SQLException::class)
    override fun getSelectCountByPeriodIdAndPhraseQuery(): PreparedStatement = 
        session.prepareStatement(SELECT_COUNT_BY_PERIOD_ID_AND_PHRASE)
    
    @Throws(SQLException::class)
    override fun getSelectByPeriodIdAndPhraseQuery(): PreparedStatement = 
        session.prepareStatement(SELECT_BY_PERIOD_ID_AND_PHRASE)
    
    @Throws(SQLException::class)
    override fun getSelectByPeriodIdAndDateQuery(): PreparedStatement = 
        session.prepareStatement(SELECT_BY_PERIOD_ID_AND_DATE)
    
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
    override fun getDeleteByPeriodIdQuery(): PreparedStatement = 
        session.prepareStatement(DELETE_BY_PERIOD_ID)
    
    @Throws(SQLException::class)
    override fun getNumberShiftQuery(): PreparedStatement = 
        session.prepareStatement(NUMBER_SHIFT)
    
    @Throws(SQLException::class)
    override fun getGeneratedKey(): Int = session.insertId
}

