package kirjanpito.db

/**
 * Factory for creating DataSource instances.
 * 
 * @author Tommi Helineva (original Java)
 * @author Kotlin migration by Claude
 */
object DataSourceFactoryKt {
    
    @JvmStatic
    @Throws(DataAccessException::class)
    fun create(url: String, username: String?, password: String?): DataSource {
        val prefixes = arrayOf(
            "jdbc:sqlite:",
            "jdbc:postgresql:",
            "jdbc:mysql:"
        )
        
        val classNames = arrayOf(
            "kirjanpito.db.sqlite.SQLiteDataSourceKt",  // Kotlin version
            "kirjanpito.db.postgresql.PSQLDataSource",
            "kirjanpito.db.mysql.MySQLDataSource"
        )
        
        val index = prefixes.indexOfFirst { url.startsWith(it) }
        
        if (index == -1) {
            throw DataAccessException("Virheellinen tietokantapalvelimen osoite")
        }
        
        val dataSource = try {
            Class.forName(classNames[index]).getDeclaredConstructor().newInstance() as DataSource
        } catch (e: Exception) {
            throw DataAccessException(
                "Ilmentymän luonti luokasta ${classNames[index]} epäonnistui", e
            )
        }
        
        dataSource.open(url, username, password)
        return dataSource
    }
}

