package kirjanpito.db.sqlite

import kirjanpito.db.*
import kirjanpito.db.sql.SQLEntryTemplateDAOKt
import java.math.BigDecimal
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException

/**
 * SQLite-specific implementation of EntryTemplate DAO.
 * 
 * Key differences from MySQL/PostgreSQL:
 * - amount stored as TEXT (not DECIMAL)
 * - Uses last_insert_rowid() for generated keys
 * 
 * @author Tommi Helineva (original Java)
 * @author Kotlin migration by Claude
 */
class SQLiteEntryTemplateDAOKt(session: Session) : SQLEntryTemplateDAOKt() {
    private val session: Session = session
    
    companion object {
        private const val SELECT_ALL = """
            SELECT id, number, name, account_id, debit, amount, description, row_number 
            FROM entry_template 
            ORDER BY number, row_number
        """
        
        private const val INSERT = """
            INSERT INTO entry_template (id, number, name, account_id, debit, amount, description, row_number) 
            VALUES (NULL, ?, ?, ?, ?, ?, ?, ?)
        """
        
        private const val UPDATE = """
            UPDATE entry_template 
            SET number=?, name=?, account_id=?, debit=?, amount=?, description=?, row_number=? 
            WHERE id = ?
        """
        
        private const val DELETE = "DELETE FROM entry_template WHERE id = ?"
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
    
    /**
     * SQLite stores amount as TEXT, not DECIMAL.
     */
    @Throws(SQLException::class)
    override fun createObject(rs: ResultSet): EntryTemplate {
        return EntryTemplate().apply {
            id = rs.getInt(1)
            number = rs.getInt(2)
            name = rs.getString(3)
            accountId = rs.getInt(4)
            setDebit(rs.getBoolean(5))
            // SQLite stores amount as TEXT
            amount = BigDecimal(rs.getString(6))
            description = rs.getString(7)
            rowNumber = rs.getInt(8)
        }
    }
    
    /**
     * SQLite stores amount as TEXT.
     */
    @Throws(SQLException::class)
    override fun setValuesToStatement(stmt: PreparedStatement, obj: EntryTemplate) {
        stmt.setInt(1, obj.number)
        stmt.setString(2, obj.name)
        stmt.setInt(3, obj.accountId)
        stmt.setBoolean(4, obj.isDebit())
        // SQLite stores amount as String
        stmt.setString(5, obj.amount?.toString() ?: "0")
        stmt.setString(6, obj.description)
        stmt.setInt(7, obj.rowNumber)
    }
}

