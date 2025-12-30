package kirjanpito.db.sql

import kirjanpito.db.*
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException
import java.sql.Timestamp
import java.util.Date

/**
 * Kotlin implementation of SQLDocumentDAO.
 * 
 * Abstract base class for database-specific Document DAO implementations.
 * Provides CRUD operations using Kotlin idioms and extension functions.
 * 
 * Subclasses must implement:
 * - getSelectLastDocumentQuery()
 * - getSelectByPeriodIdQuery()
 * - getSelectCountByPeriodIdQuery()
 * - getSelectByPeriodIdAndNumberQuery()
 * - getSelectCountByPeriodIdAndNumberQuery()
 * - getSelectNumberByPeriodIdAndNumberQuery()
 * - getSelectCountByPeriodIdAndPhraseQuery()
 * - getSelectByPeriodIdAndPhraseQuery()
 * - getSelectByPeriodIdAndDateQuery()
 * - getInsertQuery()
 * - getUpdateQuery()
 * - getDeleteQuery()
 * - getDeleteByPeriodIdQuery()
 * - getNumberShiftQuery()
 * - getGeneratedKey()
 * 
 * @author Tommi Helineva (original Java)
 * @author Kotlin migration by Claude
 */
abstract class SQLDocumentDAOKt : DocumentDAO {
    
    /**
     * Luo uuden tositteen. Tositenumeroksi asetetaan seuraava
     * vapaa numero, ja päivämäärä kopioidaan viimeisestä tositteesta.
     */
    override fun create(periodId: Int, numberStart: Int, numberEnd: Int): Document = withDataAccess {
        val (number, date) = getSelectLastDocumentQuery().use { stmt ->
            stmt.setInt(1, periodId)
            stmt.setInt(2, numberStart)
            stmt.setInt(3, numberEnd)
            stmt.executeQuery().use { rs ->
                if (rs.next()) {
                    Pair(rs.getInt(1) + 1, rs.getDate(2))
                } else {
                    Pair(numberStart, Date())
                }
            }
        }
        
        Document().apply {
            this.periodId = periodId
            this.number = number
            this.date = date
        }
    }
    
    /**
     * Hakee tietokannasta kaikki tietyn tilikauden tositteet.
     */
    override fun getByPeriodId(periodId: Int, numberOffset: Int): List<Document> = withDataAccess {
        getSelectByPeriodIdQuery().use { stmt ->
            stmt.setInt(1, periodId)
            stmt.setInt(2, numberOffset)
            stmt.executeQuery().use { rs ->
                buildList {
                    while (rs.next()) {
                        add(createObject(rs))
                    }
                }
            }
        }
    }
    
    /**
     * Hakee tietokannasta tietyn tilikauden tositteiden lukumäärän.
     */
    override fun getCountByPeriodId(periodId: Int, numberOffset: Int): Int = withDataAccess {
        getSelectCountByPeriodIdQuery().use { stmt ->
            stmt.setInt(1, periodId)
            stmt.setInt(2, numberOffset)
            stmt.executeQuery().use { rs ->
                if (rs.next()) rs.getInt(1) else 0
            }
        }
    }
    
    /**
     * Hakee tietokannasta tositteen numerolla tietyltä tilikaudelta.
     */
    override fun getByPeriodIdAndNumber(periodId: Int, number: Int): Document? = withDataAccess {
        getSelectByPeriodIdAndNumberQuery().use { stmt ->
            stmt.setInt(1, periodId)
            stmt.setInt(2, number)
            stmt.setInt(3, number)
            stmt.setInt(4, 1)
            stmt.setInt(5, 0)
            stmt.executeQuery().use { rs ->
                if (rs.next()) createObject(rs) else null
            }
        }
    }
    
    /**
     * Hakee tietokannasta tositteiden lukumäärän numeroväliltä.
     */
    override fun getCountByPeriodIdAndNumber(
        periodId: Int,
        startNumber: Int,
        endNumber: Int
    ): Int = withDataAccess {
        getSelectCountByPeriodIdAndNumberQuery().use { stmt ->
            stmt.setInt(1, periodId)
            stmt.setInt(2, startNumber)
            stmt.setInt(3, endNumber)
            stmt.executeQuery().use { rs ->
                if (rs.next()) rs.getInt(1) else 0
            }
        }
    }
    
    /**
     * Hakee tietokannasta sen tositteen järjestysnumeron,
     * jonka numero on annettu.
     */
    override fun getIndexByPeriodIdAndNumber(
        periodId: Int,
        startNumber: Int,
        endNumber: Int,
        number: Int
    ): Int = withDataAccess {
        getSelectNumberByPeriodIdAndNumberQuery().use { stmt ->
            stmt.setInt(1, periodId)
            stmt.setInt(2, startNumber)
            stmt.setInt(3, endNumber)
            stmt.executeQuery().use { rs ->
                var index = 0
                while (rs.next()) {
                    if (rs.getInt(1) == number) {
                        return index
                    }
                    index++
                }
                -1
            }
        }
    }
    
    /**
     * Hakee tietokannasta tositteet numeroväliltä.
     */
    override fun getByPeriodIdAndNumber(
        periodId: Int,
        startNumber: Int,
        endNumber: Int,
        offset: Int,
        limit: Int
    ): List<Document> = withDataAccess {
        getSelectByPeriodIdAndNumberQuery().use { stmt ->
            stmt.setInt(1, periodId)
            stmt.setInt(2, startNumber)
            stmt.setInt(3, endNumber)
            stmt.setInt(4, limit)
            stmt.setInt(5, offset)
            stmt.executeQuery().use { rs ->
                buildList {
                    while (rs.next()) {
                        add(createObject(rs))
                    }
                }
            }
        }
    }
    
    /**
     * Etsii tositteita hakusanalla ja palauttaa tulosten lukumäärän.
     */
    override fun getCountByPeriodIdAndPhrase(periodId: Int, q: String): Int = withDataAccess {
        getSelectCountByPeriodIdAndPhraseQuery().use { stmt ->
            stmt.setInt(1, periodId)
            stmt.setString(2, escapePhrase(q))
            stmt.executeQuery().use { rs ->
                if (rs.next()) rs.getInt(1) else 0
            }
        }
    }
    
    /**
     * Etsii tositteita hakusanalla ja palauttaa löytyneet tositteet.
     */
    override fun getByPeriodIdAndPhrase(
        periodId: Int,
        q: String,
        offset: Int,
        limit: Int
    ): List<Document> = withDataAccess {
        getSelectByPeriodIdAndPhraseQuery().use { stmt ->
            stmt.setInt(1, periodId)
            stmt.setString(2, escapePhrase(q))
            stmt.setInt(3, limit)
            stmt.setInt(4, offset)
            stmt.executeQuery().use { rs ->
                buildList {
                    while (rs.next()) {
                        add(createObject(rs))
                    }
                }
            }
        }
    }
    
    /**
     * Hakee tietokannasta tositteet tietyltä aikaväliltä.
     */
    override fun getByPeriodIdAndDate(
        periodId: Int,
        startDate: Date,
        endDate: Date
    ): List<Document> = withDataAccess {
        getSelectByPeriodIdAndDateQuery().use { stmt ->
            stmt.setInt(1, periodId)
            stmt.setDate(2, java.sql.Date(startDate.time))
            stmt.setDate(3, java.sql.Date(endDate.time))
            stmt.executeQuery().use { rs ->
                buildList {
                    while (rs.next()) {
                        add(createObject(rs))
                    }
                }
            }
        }
    }
    
    /**
     * Tallentaa tositteen tiedot tietokantaan.
     */
    override fun save(document: Document): Unit = withDataAccess {
        if (document.id == 0) {
            executeInsertQuery(document)
        } else {
            executeUpdateQuery(document)
        }
    }
    
    /**
     * Poistaa tositteen tiedot tietokannasta.
     */
    override fun delete(documentId: Int): Unit = withDataAccess {
        getDeleteQuery().use { stmt ->
            stmt.setInt(1, documentId)
            stmt.executeUpdate()
        }
    }
    
    /**
     * Poistaa tietyn tilikauden kaikki tositteet tietokannasta.
     */
    override fun deleteByPeriodId(periodId: Int): Unit = withDataAccess {
        getDeleteByPeriodIdQuery().use { stmt ->
            stmt.setInt(1, periodId)
            stmt.executeUpdate()
        }
    }
    
    /**
     * Muuttaa tositenumeroita välillä.
     */
    override fun shiftNumbers(
        periodId: Int,
        startNumber: Int,
        endNumber: Int,
        shift: Int
    ): Unit = withDataAccess {
        getNumberShiftQuery().use { stmt ->
            stmt.setInt(1, shift)
            stmt.setInt(2, periodId)
            stmt.setInt(3, startNumber)
            stmt.setInt(4, endNumber)
            stmt.executeUpdate()
        }
    }
    
    // ========================================================================
    // Protected Abstract Methods - Must be implemented by subclasses
    // ========================================================================
    
    @Throws(SQLException::class)
    protected abstract fun getSelectLastDocumentQuery(): PreparedStatement
    
    @Throws(SQLException::class)
    protected abstract fun getSelectByPeriodIdQuery(): PreparedStatement
    
    @Throws(SQLException::class)
    protected abstract fun getSelectCountByPeriodIdQuery(): PreparedStatement
    
    @Throws(SQLException::class)
    protected abstract fun getSelectByPeriodIdAndNumberQuery(): PreparedStatement
    
    @Throws(SQLException::class)
    protected abstract fun getSelectCountByPeriodIdAndNumberQuery(): PreparedStatement
    
    @Throws(SQLException::class)
    protected abstract fun getSelectNumberByPeriodIdAndNumberQuery(): PreparedStatement
    
    @Throws(SQLException::class)
    protected abstract fun getSelectCountByPeriodIdAndPhraseQuery(): PreparedStatement
    
    @Throws(SQLException::class)
    protected abstract fun getSelectByPeriodIdAndPhraseQuery(): PreparedStatement
    
    @Throws(SQLException::class)
    protected abstract fun getSelectByPeriodIdAndDateQuery(): PreparedStatement
    
    @Throws(SQLException::class)
    protected abstract fun getInsertQuery(): PreparedStatement
    
    @Throws(SQLException::class)
    protected abstract fun getUpdateQuery(): PreparedStatement
    
    @Throws(SQLException::class)
    protected abstract fun getDeleteQuery(): PreparedStatement
    
    @Throws(SQLException::class)
    protected abstract fun getDeleteByPeriodIdQuery(): PreparedStatement
    
    @Throws(SQLException::class)
    protected abstract fun getNumberShiftQuery(): PreparedStatement
    
    @Throws(SQLException::class)
    protected abstract fun getGeneratedKey(): Int
    
    // ========================================================================
    // Protected Methods - Can be overridden for database-specific behavior
    // ========================================================================
    
    /**
     * Lukee ResultSetistä rivin kentät ja luo Document-olion.
     */
    @Throws(SQLException::class)
    protected open fun createObject(rs: ResultSet): Document {
        return rs.toDocumentData().toDocument()
    }
    
    /**
     * Asettaa valmistellun kyselyn parametreiksi olion tiedot.
     */
    @Throws(SQLException::class)
    protected open fun setValuesToStatement(stmt: PreparedStatement, obj: Document) {
        stmt.setDocumentValues(obj.toDocumentData())
    }
    
    // ========================================================================
    // Private Helper Methods
    // ========================================================================
    
    /**
     * Lisää tositteen tiedot tietokantaan.
     */
    @Throws(SQLException::class)
    private fun executeInsertQuery(obj: Document) {
        getInsertQuery().use { stmt ->
            setValuesToStatement(stmt, obj)
            stmt.executeUpdate()
        }
        obj.id = getGeneratedKey()
    }
    
    /**
     * Päivittää tositteen tiedot tietokantaan.
     */
    @Throws(SQLException::class)
    private fun executeUpdateQuery(obj: Document) {
        getUpdateQuery().use { stmt ->
            setValuesToStatement(stmt, obj)
            stmt.setInt(4, obj.id)
            stmt.executeUpdate()
        }
    }
    
    /**
     * Escapes search phrase for SQL LIKE queries.
     */
    private fun escapePhrase(q: String): String {
        var escaped = q.replace("%", "\\%").replace("_", "\\_").replace("*", "%")
        if (!escaped.endsWith("%")) {
            escaped += "%"
        }
        return escaped
    }
}

