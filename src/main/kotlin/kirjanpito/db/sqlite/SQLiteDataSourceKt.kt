package kirjanpito.db.sqlite

import kirjanpito.db.*
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet
import java.sql.SQLException
import java.sql.Statement
import java.text.SimpleDateFormat
import java.util.Date
import java.util.logging.Level
import java.util.logging.Logger

/**
 * SQLite DataSource implementation in Kotlin.
 * 
 * @author Tommi Helineva (original Java)
 * @author Kotlin migration by Claude
 */
class SQLiteDataSourceKt : DataSource {
    private var url: String? = null
    private var file: File? = null
    private var conn: Connection? = null

    companion object {
        private const val JDBC_DRIVER_CLASS = "org.sqlite.JDBC"
    }

    @Throws(DataAccessException::class)
    override fun open(url: String, username: String?, password: String?) {
        try {
            Class.forName(JDBC_DRIVER_CLASS)
        } catch (e: ClassNotFoundException) {
            throw DataAccessException("SQLite-tietokanta-ajuria ei löytynyt", e)
        }

        if (!url.startsWith("jdbc:sqlite:")) {
            throw DataAccessException("Virheellinen SQLite-JDBC-osoite: $url")
        }

        val filename = url.substring(12)
        this.url = url
        this.file = File(filename)
        val tablesExist = file!!.exists()

        try {
            conn = DriverManager.getConnection(url)
            conn!!.autoCommit = false

            if (tablesExist) {
                upgradeDatabase(conn!!, file!!)
            } else {
                createTables(conn!!)
            }
        } catch (e: SQLException) {
            throw DataAccessException(e.message ?: "SQL error", e)
        }
    }

    override fun close() {
        try {
            conn?.close()
        } catch (e: SQLException) {
            // Ignore
        }
    }

    @Throws(DataAccessException::class)
    override fun backup() {
        close()
        backupDatabase(file!!)
        open(url!!, null, null)
    }

    override fun getAccountDAO(session: Session): AccountDAO =
        SQLiteAccountDAOKt(session)

    override fun getCOAHeadingDAO(session: Session): COAHeadingDAO =
        SQLiteCOAHeadingDAOKt(session)

    override fun getDocumentDAO(session: Session): DocumentDAO =
        SQLiteDocumentDAOKt(session)

    override fun getEntryDAO(session: Session): EntryDAO =
        SQLiteEntryDAOKt(session)

    override fun getPeriodDAO(session: Session): PeriodDAO =
        SQLitePeriodDAOKt(session)

    override fun getSettingsDAO(session: Session): SettingsDAO =
        SQLiteSettingsDAOKt(session)

    override fun getReportStructureDAO(session: Session): ReportStructureDAO =
        SQLiteReportStructureDAOKt(session)

    override fun getEntryTemplateDAO(session: Session): EntryTemplateDAO =
        SQLiteEntryTemplateDAOKt(session)

    override fun getDocumentTypeDAO(session: Session): DocumentTypeDAO =
        SQLiteDocumentTypeDAOKt(session)

    override fun getAttachmentDAO(session: Session): AttachmentDAO =
        SQLiteAttachmentDAO(session)

    @Throws(DataAccessException::class)
    override fun openSession(): Session {
        return SQLiteSessionKt(conn!!)
    }

    @Throws(DataAccessException::class)
    private fun createTables(conn: Connection) {
        try {
            DatabaseUpgradeUtil.executeQueries(
                conn,
                SQLiteDataSourceKt::class.java.getResourceAsStream("/schema/sqlite.sql")
            )
        } catch (e: SQLException) {
            throw DataAccessException(e.message ?: "SQL error", e)
        } catch (e: IOException) {
            throw DataAccessException(e.message, e)
        }
    }

    @Throws(DataAccessException::class)
    private fun upgradeDatabase(conn: Connection, file: File) {
        var version = 0

        try {
            val stmt = conn.createStatement()
            val rs = stmt.executeQuery("SELECT version FROM settings")

            if (rs.next()) {
                version = rs.getInt(1)
            }

            if (version == 1) {
                backupDatabase(file)
                upgrade1to2(conn, stmt)
                version = 2
            }

            if (version == 2) {
                backupDatabase(file)
                upgrade2to3(conn, stmt)
                version = 3
            }

            if (version == 3) {
                backupDatabase(file)
                DatabaseUpgradeUtil.upgrade3to4(conn, stmt)
                version = 4
            }

            if (version == 4) {
                backupDatabase(file)
                DatabaseUpgradeUtil.upgrade4to5(conn, stmt)
                version = 5
            }

            if (version == 5) {
                backupDatabase(file)
                DatabaseUpgradeUtil.upgrade5to6(conn, stmt)
                version = 6
            }

            if (version == 6) {
                backupDatabase(file)
                DatabaseUpgradeUtil.upgrade6to7(conn, stmt)
                version = 7
            }

            if (version == 7) {
                backupDatabase(file)
                DatabaseUpgradeUtil.upgrade7to8(conn, stmt)
                version = 8
            }

            if (version == 8) {
                backupDatabase(file)
                DatabaseUpgradeUtil.upgrade8to9(conn, stmt)
                version = 9
            }

            if (version == 9) {
                backupDatabase(file)
                DatabaseUpgradeUtil.upgrade9to10(conn, stmt)
                version = 10
            }

            if (version == 10) {
                backupDatabase(file)
                DatabaseUpgradeUtil.upgrade10to11(conn, stmt)
                version = 11
            }

            if (version == 11) {
                backupDatabase(file)
                DatabaseUpgradeUtil.upgrade11to12(conn, stmt)
                version = 12
            }

            if (version == 12) {
                backupDatabase(file)
                DatabaseUpgradeUtil.upgrade12to13(conn, stmt)
                version = 13
            }

            if (version == 13) {
                backupDatabase(file)
                DatabaseUpgradeUtil.upgrade13to14(conn, stmt, true)
                version = 14
            }

            // upgrade14to15 - POISTETTU: PDF-liitteet tiedostoina, ei tietokantaan

            stmt.close()
        } catch (e: Exception) {
            try {
                conn.rollback()
            } catch (exc: SQLException) {
                // Ignore
            }

            val logger = Logger.getLogger("kirjanpito.db.sqlite")
            logger.log(Level.SEVERE, "Tietokannan päivittäminen epäonnistui", e)
            throw DataAccessException(e.message, e)
        }
    }

    @Throws(SQLException::class)
    private fun upgrade1to2(conn: Connection, stmt: Statement) {
        stmt.execute(
            "CREATE TABLE entry_template (id integer PRIMARY KEY AUTOINCREMENT NOT NULL, " +
            "number integer NOT NULL, name varchar(100) NOT NULL, account_id integer NOT NULL, " +
            "debit bool NOT NULL, amount numeric(10, 2) NOT NULL, description varchar(100) NOT NULL, " +
            "row_number integer NOT NULL, FOREIGN KEY (account_id) REFERENCES account (id))"
        )
        stmt.executeUpdate("UPDATE settings SET version=2")
        conn.commit()

        val logger = Logger.getLogger("kirjanpito.db.sqlite")
        logger.info("Tietokannan päivittäminen versioon 2 onnistui")
    }

    @Throws(SQLException::class)
    private fun upgrade2to3(conn: Connection, stmt: Statement) {
        stmt.execute(
            "CREATE TABLE document_type (id integer PRIMARY KEY AUTOINCREMENT NOT NULL, " +
            "number integer NOT NULL, name varchar(100) NOT NULL, number_start integer NOT NULL, " +
            "number_end integer NOT NULL)"
        )
        stmt.execute("ALTER TABLE settings ADD document_type_id integer REFERENCES document_type (id)")
        stmt.executeUpdate("UPDATE settings SET version=3")
        conn.commit()

        val logger = Logger.getLogger("kirjanpito.db.sqlite")
        logger.info("Tietokannan päivittäminen versioon 3 onnistui")
    }

    private fun backupDatabase(file: File) {
        val dir = file.parentFile
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH-mm-ss")
        val destination = File(dir, "kirjanpito-${dateFormat.format(Date())}.sqlite")

        if (destination.exists()) {
            return
        }

        val logger = Logger.getLogger("kirjanpito.db.sqlite")
        logger.severe("Varmuuskopioidaan tietokanta, $file -> $destination")

        try {
            copyFile(file, destination)
        } catch (e: IOException) {
            logger.log(Level.SEVERE, "Tietokannan varmuuskopiointi epäonnistui", e)
        }
    }

    @Throws(IOException::class)
    private fun copyFile(src: File, dst: File) {
        FileInputStream(src).use { input ->
            FileOutputStream(dst).use { output ->
                val buf = ByteArray(1024)
                var len: Int
                while (input.read(buf).also { len = it } > 0) {
                    output.write(buf, 0, len)
                }
            }
        }
    }
}

