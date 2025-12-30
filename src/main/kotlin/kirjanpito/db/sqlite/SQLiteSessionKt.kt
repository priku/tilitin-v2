package kirjanpito.db.sqlite

import kirjanpito.db.*
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException
import java.util.logging.Level
import java.util.logging.Logger

/**
 * SQLite-tietokantaistunto.
 * 
 * @author Tommi Helineva (original Java)
 * @author Kotlin migration by Claude
 */
class SQLiteSessionKt(private val conn: Connection) : Session {
    
    companion object {
        private val logger = Logger.getLogger("kirjanpito.db.sqlite")
    }
    
    /**
     * Palauttaa tietokantayhteyden.
     */
    fun getConnection(): Connection = conn
    
    @Throws(DataAccessException::class)
    override fun commit() {
        try {
            conn.commit()
        } catch (e: SQLException) {
            throw DataAccessException(e.message ?: "Commit failed", e)
        }
    }
    
    @Throws(DataAccessException::class)
    override fun rollback() {
        try {
            conn.rollback()
        } catch (e: SQLException) {
            throw DataAccessException(e.message ?: "Rollback failed", e)
        }
    }
    
    override fun close() {
        // SQLiteSession doesn't close connection (managed by DataSource)
    }
    
    @Throws(SQLException::class)
    fun prepareStatement(sql: String): PreparedStatement {
        logger.log(Level.FINER, "Suoritetaan tietokantakysely: $sql")
        return conn.prepareStatement(sql)
    }
    
    /**
     * Palauttaa viimeksi lisätyn rivin AUTO_INCREMENT-kentän arvon.
     * 
     * @return laskurikentän arvo
     * @throws SQLException jos hakeminen epäonnistuu
     */
    @Throws(SQLException::class)
    fun getInsertId(): Int {
        prepareStatement("SELECT last_insert_rowid()").use { stmt ->
            stmt.executeQuery().use { rs ->
                return if (rs.next()) rs.getInt(1) else -1
            }
        }
    }
}

