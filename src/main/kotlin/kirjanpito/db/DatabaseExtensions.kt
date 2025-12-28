package kirjanpito.db

import java.math.BigDecimal
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException
import java.sql.Types
import java.util.Date

/**
 * Kotlin extension functions for database operations.
 * 
 * Provides null-safe ResultSet getters and PreparedStatement helpers
 * for cleaner DAO implementations.
 * 
 * @author Kotlin migration by Claude
 */

// ============================================================================
// ResultSet Extensions - Null-safe getters
// ============================================================================

/**
 * Gets an Int value from ResultSet, returning null if the column value is NULL.
 */
fun ResultSet.getIntOrNull(columnIndex: Int): Int? {
    val value = getInt(columnIndex)
    return if (wasNull()) null else value
}

/**
 * Gets an Int value from ResultSet, returning defaultValue if the column value is NULL.
 */
fun ResultSet.getIntOrDefault(columnIndex: Int, defaultValue: Int): Int {
    val value = getInt(columnIndex)
    return if (wasNull()) defaultValue else value
}

/**
 * Gets an Int value from ResultSet, returning -1 if the column value is NULL.
 * This is the convention used in the original Tilitin code for missing FK references.
 */
fun ResultSet.getIntOrMinusOne(columnIndex: Int): Int = getIntOrDefault(columnIndex, -1)

/**
 * Gets a String value from ResultSet, returning null if the column value is NULL.
 */
fun ResultSet.getStringOrNull(columnIndex: Int): String? {
    val value = getString(columnIndex)
    return if (wasNull()) null else value
}

/**
 * Gets a String value from ResultSet, returning empty string if the column value is NULL.
 */
fun ResultSet.getStringOrEmpty(columnIndex: Int): String = getString(columnIndex) ?: ""

/**
 * Gets a BigDecimal value from ResultSet, returning null if the column value is NULL.
 */
fun ResultSet.getBigDecimalOrNull(columnIndex: Int): BigDecimal? {
    val value = getBigDecimal(columnIndex)
    return if (wasNull()) null else value
}

/**
 * Gets a BigDecimal value from ResultSet, returning BigDecimal.ZERO if NULL.
 */
fun ResultSet.getBigDecimalOrZero(columnIndex: Int): BigDecimal = 
    getBigDecimal(columnIndex) ?: BigDecimal.ZERO

/**
 * Gets a Date value from ResultSet, returning null if the column value is NULL.
 */
fun ResultSet.getDateOrNull(columnIndex: Int): Date? {
    val value = getDate(columnIndex)
    return if (wasNull()) null else value
}

/**
 * Gets a Boolean value from ResultSet (stored as Int 0/1).
 */
fun ResultSet.getBooleanAsInt(columnIndex: Int): Boolean = getInt(columnIndex) != 0

// ============================================================================
// ResultSet to Data Class Mapping
// ============================================================================

/**
 * Maps ResultSet row to AccountData.
 * Expected column order: id, number, name, type, vatCode, vatRate, vatAccount1Id, vatAccount2Id, flags
 */
fun ResultSet.toAccountData(): AccountData = AccountData(
    id = getInt(1),
    number = getStringOrEmpty(2),
    name = getStringOrEmpty(3),
    type = getInt(4),
    vatCode = getInt(5),
    vatRate = getBigDecimalOrZero(6),
    vatAccount1Id = getIntOrMinusOne(7),
    vatAccount2Id = getIntOrMinusOne(8),
    flags = getInt(9)
)

/**
 * Maps ResultSet row to AccountData for SQLite (vatRate stored as String).
 */
fun ResultSet.toAccountDataSQLite(): AccountData = AccountData(
    id = getInt(1),
    number = getStringOrEmpty(2),
    name = getStringOrEmpty(3),
    type = getInt(4),
    vatCode = getInt(5),
    vatRate = getString(6)?.let { BigDecimal(it) } ?: BigDecimal.ZERO,
    vatAccount1Id = getIntOrMinusOne(7),
    vatAccount2Id = getIntOrMinusOne(8),
    flags = getInt(9)
)

/**
 * Maps ResultSet row to DocumentData.
 * Expected column order: id, number, periodId, date
 */
fun ResultSet.toDocumentData(): DocumentData = DocumentData(
    id = getInt(1),
    number = getInt(2),
    periodId = getInt(3),
    date = getDate(4)
)

/**
 * Maps ResultSet row to EntryData.
 * Expected column order: id, documentId, accountId, debit, amount, description, rowNumber, flags
 */
fun ResultSet.toEntryData(): EntryData = EntryData(
    id = getInt(1),
    documentId = getInt(2),
    accountId = getInt(3),
    debit = getBooleanAsInt(4),
    amount = getBigDecimalOrZero(5),
    description = getStringOrEmpty(6),
    rowNumber = getInt(7),
    flags = getInt(8)
)

/**
 * Maps ResultSet row to PeriodData.
 * Expected column order: id, startDate, endDate, locked
 */
fun ResultSet.toPeriodData(): PeriodData = PeriodData(
    id = getInt(1),
    startDate = getDate(2),
    endDate = getDate(3),
    locked = getBooleanAsInt(4)
)

/**
 * Maps ResultSet row to DocumentTypeData.
 * Expected column order: id, number, name, numberStart, numberEnd
 */
fun ResultSet.toDocumentTypeData(): DocumentTypeData = DocumentTypeData(
    id = getInt(1),
    number = getInt(2),
    name = getStringOrEmpty(3),
    numberStart = getInt(4),
    numberEnd = getInt(5)
)

/**
 * Maps ResultSet row to COAHeadingData.
 * Expected column order: id, number, text, level
 */
fun ResultSet.toCOAHeadingData(): COAHeadingData = COAHeadingData(
    id = getInt(1),
    number = getStringOrEmpty(2),
    text = getStringOrEmpty(3),
    level = getInt(4)
)

// ============================================================================
// PreparedStatement Extensions
// ============================================================================

/**
 * Sets an Int parameter, using NULL if the value is <= 0.
 * This is the convention used in Tilitin for optional FK references.
 */
fun PreparedStatement.setIntOrNull(parameterIndex: Int, value: Int) {
    if (value <= 0) {
        setNull(parameterIndex, Types.INTEGER)
    } else {
        setInt(parameterIndex, value)
    }
}

/**
 * Sets a nullable Int parameter.
 */
fun PreparedStatement.setNullableInt(parameterIndex: Int, value: Int?) {
    if (value == null) {
        setNull(parameterIndex, Types.INTEGER)
    } else {
        setInt(parameterIndex, value)
    }
}

/**
 * Sets a nullable String parameter.
 */
fun PreparedStatement.setNullableString(parameterIndex: Int, value: String?) {
    if (value == null) {
        setNull(parameterIndex, Types.VARCHAR)
    } else {
        setString(parameterIndex, value)
    }
}

/**
 * Sets a boolean as Int (0 or 1).
 */
fun PreparedStatement.setBooleanAsInt(parameterIndex: Int, value: Boolean) {
    setInt(parameterIndex, if (value) 1 else 0)
}

/**
 * Sets AccountData values to PreparedStatement for INSERT/UPDATE.
 * Parameter order: number, name, type, vatCode, vatRate, vatAccount1Id, vatAccount2Id, flags
 * 
 * @param startIndex 1-based index where to start setting parameters (default 1)
 */
fun PreparedStatement.setAccountValues(account: AccountData, startIndex: Int = 1) {
    setString(startIndex, account.number)
    setString(startIndex + 1, account.name)
    setInt(startIndex + 2, account.type)
    setInt(startIndex + 3, account.vatCode)
    setBigDecimal(startIndex + 4, account.vatRate)
    setIntOrNull(startIndex + 5, account.vatAccount1Id)
    setIntOrNull(startIndex + 6, account.vatAccount2Id)
    setInt(startIndex + 7, account.flags)
}

/**
 * Sets AccountData values for SQLite (vatRate as String).
 */
fun PreparedStatement.setAccountValuesSQLite(account: AccountData, startIndex: Int = 1) {
    setString(startIndex, account.number)
    setString(startIndex + 1, account.name)
    setInt(startIndex + 2, account.type)
    setInt(startIndex + 3, account.vatCode)
    setString(startIndex + 4, account.vatRate.toString())
    setIntOrNull(startIndex + 5, account.vatAccount1Id)
    setIntOrNull(startIndex + 6, account.vatAccount2Id)
    setInt(startIndex + 7, account.flags)
}

// ============================================================================
// ResultSet Iteration
// ============================================================================

/**
 * Iterates over all rows in ResultSet, applying the mapping function.
 * Automatically closes the ResultSet after iteration.
 */
inline fun <T> ResultSet.mapToList(mapper: (ResultSet) -> T): List<T> {
    val list = mutableListOf<T>()
    use {
        while (next()) {
            list.add(mapper(this))
        }
    }
    return list
}

/**
 * Executes query and maps all results to a list.
 */
inline fun <T> PreparedStatement.executeAndMap(mapper: (ResultSet) -> T): List<T> {
    return executeQuery().mapToList(mapper)
}

// ============================================================================
// Transaction Helpers
// ============================================================================

/**
 * Wraps a SQLException into DataAccessException.
 */
fun SQLException.toDataAccessException(): DataAccessException = 
    DataAccessException(message, this)

/**
 * Executes block and wraps any SQLException into DataAccessException.
 */
@Throws(DataAccessException::class)
inline fun <T> withDataAccess(block: () -> T): T {
    return try {
        block()
    } catch (e: SQLException) {
        throw e.toDataAccessException()
    }
}

// ============================================================================
// Java ↔ Kotlin Conversion Helpers
// ============================================================================

/**
 * Converts Java Account to Kotlin AccountData.
 */
fun Account.toAccountData(): AccountData = AccountData(
    id = id,
    number = number ?: "",
    name = name ?: "",
    type = type,
    vatCode = vatCode,
    vatRate = vatRate ?: BigDecimal.ZERO,
    vatAccount1Id = vatAccount1Id,
    vatAccount2Id = vatAccount2Id,
    flags = flags
)

/**
 * Converts Kotlin AccountData to Java Account.
 */
fun AccountData.toAccount(): Account = Account().apply {
    id = this@toAccount.id
    number = this@toAccount.number
    name = this@toAccount.name
    type = this@toAccount.type
    vatCode = this@toAccount.vatCode
    vatRate = this@toAccount.vatRate
    vatAccount1Id = this@toAccount.vatAccount1Id
    vatAccount2Id = this@toAccount.vatAccount2Id
    flags = this@toAccount.flags
}

/**
 * Copies values from AccountData to existing Java Account.
 */
fun AccountData.copyTo(target: Account) {
    target.id = id
    target.number = number
    target.name = name
    target.type = type
    target.vatCode = vatCode
    target.vatRate = vatRate
    target.vatAccount1Id = vatAccount1Id
    target.vatAccount2Id = vatAccount2Id
    target.flags = flags
}
