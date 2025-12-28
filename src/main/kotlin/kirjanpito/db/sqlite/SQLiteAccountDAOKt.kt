package kirjanpito.db.sqlite

import kirjanpito.db.*
import kirjanpito.db.sql.SQLAccountDAOKt
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException

/**
 * SQLite-specific implementation of Account DAO.
 * 
 * Key differences from MySQL/PostgreSQL:
 * - vatRate stored as TEXT (not DECIMAL)
 * - Uses last_insert_rowid() for generated keys
 * 
 * @author Tommi Helineva (original Java)
 * @author Kotlin migration by Claude
 */
class SQLiteAccountDAOKt(private val session: SQLiteSession) : SQLAccountDAOKt() {
    
    companion object {
        private const val SELECT_ALL = """
            SELECT id, number, name, type, vat_code, vat_percentage, 
                   vat_account1_id, vat_account2_id, flags 
            FROM account 
            ORDER BY number
        """
        
        private const val INSERT = """
            INSERT INTO account 
            (id, number, name, type, vat_code, vat_percentage, 
             vat_account1_id, vat_account2_id, flags) 
            VALUES (NULL, ?, ?, ?, ?, ?, ?, ?, ?)
        """
        
        private const val UPDATE = """
            UPDATE account 
            SET number=?, name=?, type=?, vat_code=?, vat_percentage=?, 
                vat_account1_id=?, vat_account2_id=?, flags=? 
            WHERE id = ?
        """
        
        private const val DELETE = "DELETE FROM account WHERE id = ?"
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
     * SQLite stores vatRate as TEXT, not DECIMAL.
     */
    @Throws(SQLException::class)
    override fun createObject(rs: ResultSet): Account {
        return rs.toAccountDataSQLite().toAccount()
    }
    
    /**
     * SQLite stores vatRate as TEXT.
     */
    @Throws(SQLException::class)
    override fun setValuesToStatement(stmt: PreparedStatement, obj: Account) {
        stmt.setAccountValuesSQLite(obj.toAccountData())
    }
}
