package kirjanpito.db.sqlite

import kirjanpito.db.*
import kirjanpito.db.sql.SQLEntryDAOKt
import java.math.BigDecimal
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException

/**
 * SQLite-specific implementation of Entry DAO.
 * 
 * Key differences from MySQL/PostgreSQL:
 * - amount stored as TEXT (not DECIMAL)
 * - Uses last_insert_rowid() for generated keys
 * 
 * @author Tommi Helineva (original Java)
 * @author Kotlin migration by Claude
 */
class SQLiteEntryDAOKt(session: Session) : SQLEntryDAOKt() {
    private val session: Session = session
    
    companion object {
        private const val SELECT_BY_DOCUMENT_ID = """
            SELECT id, document_id, account_id, debit, amount, description, row_number, flags 
            FROM entry 
            WHERE document_id = ? 
            ORDER BY row_number
        """
        
        private const val SELECT_BY_DOCUMENT_IDS = """
            SELECT id, document_id, account_id, debit, amount, description, row_number, flags 
            FROM entry 
            WHERE document_id IN (%s) 
            ORDER BY row_number
        """
        
        private const val SELECT_BY_PERIOD_ID_ORDER_BY_NUMBER = """
            SELECT e.id, e.document_id, e.account_id, e.debit, e.amount, e.description, e.row_number, e.flags 
            FROM entry e 
            INNER JOIN document d ON d.id = e.document_id 
            WHERE d.period_id = ? 
            ORDER BY d.number, e.row_number
        """
        
        private const val SELECT_BY_PERIOD_ID_ORDER_BY_DATE = """
            SELECT e.id, e.document_id, e.account_id, e.debit, e.amount, e.description, e.row_number, e.flags 
            FROM entry e 
            INNER JOIN document d ON d.id = e.document_id 
            WHERE d.period_id = ? 
            ORDER BY d.date, d.number, e.row_number
        """
        
        private const val SELECT_BY_PERIOD_ID_ORDER_BY_ACCOUNT_AND_NUMBER = """
            SELECT e.id, e.document_id, e.account_id, e.debit, e.amount, e.description, e.row_number, e.flags 
            FROM entry e 
            INNER JOIN account a ON a.id = e.account_id 
            INNER JOIN document d ON d.id = e.document_id 
            WHERE d.period_id = ? 
            ORDER BY a.number, d.number, e.row_number
        """
        
        private const val SELECT_BY_PERIOD_ID_ORDER_BY_ACCOUNT_AND_DATE = """
            SELECT e.id, e.document_id, e.account_id, e.debit, e.amount, e.description, e.row_number, e.flags 
            FROM entry e 
            INNER JOIN account a ON a.id = e.account_id 
            INNER JOIN document d ON d.id = e.document_id 
            WHERE d.period_id = ? 
            ORDER BY a.number, d.date, d.number, e.row_number
        """
        
        private const val SELECT_BY_ACCOUNT_ID = """
            SELECT e.id, e.document_id, e.account_id, e.debit, e.amount, e.description, e.row_number, e.flags 
            FROM entry e 
            INNER JOIN document d ON d.id = e.document_id 
            WHERE e.account_id = ? 
            ORDER BY d.number, e.row_number
        """
        
        private const val SELECT_BY_PERIOD_ID_AND_ACCOUNT_ID_ORDER_BY_DATE = """
            SELECT e.id, e.document_id, e.account_id, e.debit, e.amount, e.description, e.row_number, e.flags 
            FROM entry e 
            INNER JOIN document d ON d.id = e.document_id 
            WHERE d.period_id = ? AND e.account_id = ? 
            ORDER BY d.date, d.number, e.row_number
        """
        
        private const val SELECT_BY_PERIOD_ID_AND_ACCOUNT_ID_ORDER_BY_NUMBER = """
            SELECT e.id, e.document_id, e.account_id, e.debit, e.amount, e.description, e.row_number, e.flags 
            FROM entry e 
            INNER JOIN document d ON d.id = e.document_id 
            WHERE d.period_id = ? AND e.account_id = ? 
            ORDER BY d.number, d.date, e.row_number
        """
        
        private const val SELECT_BY_DATE = """
            SELECT e.id, e.document_id, e.account_id, e.debit, e.amount, e.description, e.row_number, e.flags 
            FROM entry e 
            INNER JOIN document d ON d.id = e.document_id 
            WHERE d.date >= ? AND d.date <= ? 
            ORDER BY d.number, e.row_number
        """
        
        private const val SELECT_BY_PERIOD_ID_AND_DATE = """
            SELECT e.id, e.document_id, e.account_id, e.debit, e.amount, e.description, e.row_number, e.flags 
            FROM entry e 
            INNER JOIN document d ON d.id = e.document_id 
            WHERE d.period_id = ? AND d.date >= ? AND d.date <= ? 
            ORDER BY d.date, d.number, e.row_number
        """
        
        private const val SELECT_BY_PERIOD_ID_AND_DATE_AND_NUMBER = """
            SELECT e.id, e.document_id, e.account_id, e.debit, e.amount, e.description, e.row_number, e.flags 
            FROM entry e 
            INNER JOIN document d ON d.id = e.document_id 
            WHERE d.period_id = ? AND d.date >= ? AND d.date <= ? AND d.number >= ? 
            ORDER BY d.date, d.number, e.row_number
        """
        
        private const val SELECT_BY_PERIOD_ID_AND_NUMBER = """
            SELECT e.id, e.document_id, e.account_id, e.debit, e.amount, e.description, e.row_number, e.flags 
            FROM entry e 
            INNER JOIN document d ON d.id = e.document_id 
            WHERE d.period_id = ? AND d.number BETWEEN ? AND ? 
            ORDER BY e.document_id, e.row_number
        """
        
        private const val INSERT = """
            INSERT INTO entry (document_id, account_id, debit, amount, description, row_number, flags) 
            VALUES (?, ?, ?, ?, ?, ?, ?)
        """
        
        private const val UPDATE = """
            UPDATE entry 
            SET document_id=?, account_id=?, debit=?, amount=?, description=?, row_number=?, flags=? 
            WHERE id = ?
        """
        
        private const val DELETE = "DELETE FROM entry WHERE id = ?"
        
        private const val DELETE_BY_PERIOD_ID = """
            DELETE FROM entry 
            WHERE document_id IN (SELECT id FROM document WHERE period_id = ?)
        """
    }
    
    @Throws(SQLException::class)
    override fun getSelectByDocumentIdQuery(): PreparedStatement = 
        session.prepareStatement(SELECT_BY_DOCUMENT_ID)
    
    @Throws(SQLException::class)
    override fun getSelectByDocumentIdsQuery(documentIds: String): PreparedStatement = 
        session.prepareStatement(SELECT_BY_DOCUMENT_IDS.replace("%s", documentIds))
    
    @Throws(SQLException::class)
    override fun getSelectByPeriodIdOrderByNumberQuery(): PreparedStatement = 
        session.prepareStatement(SELECT_BY_PERIOD_ID_ORDER_BY_NUMBER)
    
    @Throws(SQLException::class)
    override fun getSelectByPeriodIdOrderByDateQuery(): PreparedStatement = 
        session.prepareStatement(SELECT_BY_PERIOD_ID_ORDER_BY_DATE)
    
    @Throws(SQLException::class)
    override fun getSelectByPeriodIdOrderByAccountAndNumberQuery(): PreparedStatement = 
        session.prepareStatement(SELECT_BY_PERIOD_ID_ORDER_BY_ACCOUNT_AND_NUMBER)
    
    @Throws(SQLException::class)
    override fun getSelectByPeriodIdOrderByAccountAndDateQuery(): PreparedStatement = 
        session.prepareStatement(SELECT_BY_PERIOD_ID_ORDER_BY_ACCOUNT_AND_DATE)
    
    @Throws(SQLException::class)
    override fun getSelectByAccountIdQuery(): PreparedStatement = 
        session.prepareStatement(SELECT_BY_ACCOUNT_ID)
    
    @Throws(SQLException::class)
    override fun getSelectByPeriodIdAndAccountIdOrderByDateQuery(): PreparedStatement = 
        session.prepareStatement(SELECT_BY_PERIOD_ID_AND_ACCOUNT_ID_ORDER_BY_DATE)
    
    @Throws(SQLException::class)
    override fun getSelectByPeriodIdAndAccountIdOrderByNumberQuery(): PreparedStatement = 
        session.prepareStatement(SELECT_BY_PERIOD_ID_AND_ACCOUNT_ID_ORDER_BY_NUMBER)
    
    @Throws(SQLException::class)
    override fun getSelectByDateQuery(): PreparedStatement = 
        session.prepareStatement(SELECT_BY_DATE)
    
    @Throws(SQLException::class)
    override fun getSelectByPeriodIdAndDateQuery(): PreparedStatement = 
        session.prepareStatement(SELECT_BY_PERIOD_ID_AND_DATE)
    
    @Throws(SQLException::class)
    override fun getSelectByPeriodIdAndDateAndNumberQuery(): PreparedStatement = 
        session.prepareStatement(SELECT_BY_PERIOD_ID_AND_DATE_AND_NUMBER)
    
    @Throws(SQLException::class)
    override fun getSelectByPeriodIdAndNumberQuery(): PreparedStatement = 
        session.prepareStatement(SELECT_BY_PERIOD_ID_AND_NUMBER)
    
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
    override fun getGeneratedKey(): Int = session.insertId
    
    /**
     * SQLite stores amount as TEXT, not DECIMAL.
     */
    @Throws(SQLException::class)
    override fun createObject(rs: ResultSet): Entry {
        val entry = Entry()
        entry.id = rs.getInt(1)
        entry.documentId = rs.getInt(2)
        entry.accountId = rs.getInt(3)
        entry.setDebit(rs.getBoolean(4))
        // SQLite stores amount as TEXT
        entry.amount = BigDecimal(rs.getString(5))
        entry.description = rs.getString(6)
        entry.rowNumber = rs.getInt(7)
        entry.flags = rs.getInt(8)
        return entry
    }
    
    /**
     * SQLite stores amount as TEXT.
     */
    @Throws(SQLException::class)
    override fun setValuesToStatement(stmt: PreparedStatement, obj: Entry) {
        stmt.setInt(1, obj.documentId)
        stmt.setInt(2, obj.accountId)
        stmt.setBoolean(3, obj.isDebit())
        // SQLite stores amount as String
        stmt.setString(4, obj.amount?.toString() ?: "0")
        stmt.setString(5, obj.description)
        stmt.setInt(6, obj.rowNumber)
        stmt.setInt(7, obj.flags)
    }
}

