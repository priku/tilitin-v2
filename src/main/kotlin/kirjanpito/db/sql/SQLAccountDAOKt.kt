package kirjanpito.db.sql

import kirjanpito.db.*
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException

/**
 * Kotlin implementation of SQLAccountDAO.
 * 
 * Abstract base class for database-specific Account DAO implementations.
 * Provides CRUD operations using Kotlin idioms and extension functions.
 * 
 * Subclasses must implement:
 * - getSelectAllQuery()
 * - getInsertQuery()
 * - getUpdateQuery()
 * - getDeleteQuery()
 * - getGeneratedKey()
 * 
 * @author Tommi Helineva (original Java)
 * @author Kotlin migration by Claude
 */
abstract class SQLAccountDAOKt : AccountDAO {
    
    /**
     * Hakee tietokannasta kaikkien tilien tiedot numerojärjestyksessä.
     * 
     * @return tilit
     * @throws DataAccessException jos tietojen hakeminen epäonnistuu
     */
    override fun getAll(): List<Account> = withDataAccess {
        getSelectAllQuery().use { stmt ->
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
     * Tallentaa tilin tiedot tietokantaan.
     * 
     * @param account tallennettava tili
     * @throws DataAccessException jos tallentaminen epäonnistuu
     */
    override fun save(account: Account): Unit = withDataAccess {
        if (account.id == 0) {
            executeInsertQuery(account)
        } else {
            executeUpdateQuery(account)
        }
    }
    
    /**
     * Poistaa tilin tiedot tietokannasta.
     * 
     * @param accountId poistettavan tilin tunniste
     * @throws DataAccessException jos poistaminen epäonnistuu
     */
    override fun delete(accountId: Int): Unit = withDataAccess {
        getDeleteQuery().use { stmt ->
            stmt.setInt(1, accountId)
            stmt.executeUpdate()
        }
    }
    
    // ========================================================================
    // Protected Abstract Methods - Must be implemented by subclasses
    // ========================================================================
    
    /**
     * Palauttaa SELECT-kyselyn, jonka avulla haetaan kaikkien rivien kaikki kentät.
     */
    @Throws(SQLException::class)
    protected abstract fun getSelectAllQuery(): PreparedStatement
    
    /**
     * Palauttaa INSERT-kyselyn, jonka avulla rivi lisätään.
     */
    @Throws(SQLException::class)
    protected abstract fun getInsertQuery(): PreparedStatement
    
    /**
     * Palauttaa UPDATE-kyselyn, jonka avulla rivin kaikki kentät päivitetään.
     */
    @Throws(SQLException::class)
    protected abstract fun getUpdateQuery(): PreparedStatement
    
    /**
     * Palauttaa DELETE-kyselyn, jonka avulla poistetaan rivi.
     */
    @Throws(SQLException::class)
    protected abstract fun getDeleteQuery(): PreparedStatement
    
    /**
     * Palauttaa tietokantaan lisätyn tilin tunnisteen.
     */
    @Throws(SQLException::class)
    protected abstract fun getGeneratedKey(): Int
    
    // ========================================================================
    // Protected Methods - Can be overridden for database-specific behavior
    // ========================================================================
    
    /**
     * Lukee ResultSetistä rivin kentät ja luo Account-olion.
     * 
     * Default implementation uses standard BigDecimal reading.
     * SQLite override reads vatRate as String.
     */
    @Throws(SQLException::class)
    protected open fun createObject(rs: java.sql.ResultSet): Account {
        return rs.toAccountData().toAccount()
    }
    
    /**
     * Asettaa valmistellun kyselyn parametreiksi olion tiedot.
     * 
     * Default implementation uses standard BigDecimal.
     * SQLite override writes vatRate as String.
     */
    @Throws(SQLException::class)
    protected open fun setValuesToStatement(stmt: PreparedStatement, obj: Account) {
        stmt.setAccountValues(obj.toAccountData())
    }
    
    // ========================================================================
    // Private Helper Methods
    // ========================================================================
    
    /**
     * Lisää tilin tiedot tietokantaan.
     */
    @Throws(SQLException::class)
    private fun executeInsertQuery(obj: Account) {
        getInsertQuery().use { stmt ->
            setValuesToStatement(stmt, obj)
            stmt.executeUpdate()
        }
        obj.id = getGeneratedKey()
    }
    
    /**
     * Päivittää tilin tiedot tietokantaan.
     */
    @Throws(SQLException::class)
    private fun executeUpdateQuery(obj: Account) {
        getUpdateQuery().use { stmt ->
            setValuesToStatement(stmt, obj)
            stmt.setInt(9, obj.id)
            stmt.executeUpdate()
        }
    }
}
