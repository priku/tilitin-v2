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

/**
 * Sets EntryData values to PreparedStatement for INSERT/UPDATE.
 * Parameter order: documentId, accountId, debit, amount, description, rowNumber, flags
 * 
 * @param startIndex 1-based index where to start setting parameters (default 1)
 */
fun PreparedStatement.setEntryValues(entry: EntryData, startIndex: Int = 1) {
    setInt(startIndex, entry.documentId)
    setInt(startIndex + 1, entry.accountId)
    setBooleanAsInt(startIndex + 2, entry.debit)
    setBigDecimal(startIndex + 3, entry.amount ?: BigDecimal.ZERO)
    setNullableString(startIndex + 4, entry.description)
    setInt(startIndex + 5, entry.rowNumber)
    setInt(startIndex + 6, entry.flags)
}

/**
 * Sets EntryData values for SQLite (amount as String).
 */
fun PreparedStatement.setEntryValuesSQLite(entry: EntryData, startIndex: Int = 1) {
    setInt(startIndex, entry.documentId)
    setInt(startIndex + 1, entry.accountId)
    setBooleanAsInt(startIndex + 2, entry.debit)
    setString(startIndex + 3, (entry.amount ?: BigDecimal.ZERO).toString())
    setNullableString(startIndex + 4, entry.description)
    setInt(startIndex + 5, entry.rowNumber)
    setInt(startIndex + 6, entry.flags)
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
    DataAccessException(message ?: "SQL error", this)

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

/**
 * Converts Java Entry to Kotlin EntryData.
 */
fun Entry.toEntryData(): EntryData = EntryData(
    id = id,
    documentId = documentId,
    accountId = accountId,
    debit = isDebit(),
    amount = amount,
    description = description,
    rowNumber = rowNumber,
    flags = flags
)

/**
 * Converts Kotlin EntryData to Java Entry.
 */
fun EntryData.toEntry(): Entry = Entry().apply {
    id = this@toEntry.id
    documentId = this@toEntry.documentId
    accountId = this@toEntry.accountId
    setDebit(this@toEntry.debit)
    amount = this@toEntry.amount
    description = this@toEntry.description
    rowNumber = this@toEntry.rowNumber
    flags = this@toEntry.flags
}

/**
 * Copies values from EntryData to existing Java Entry.
 */
fun EntryData.copyTo(target: Entry) {
    target.id = id
    target.documentId = documentId
    target.accountId = accountId
    target.setDebit(debit)
    target.amount = amount
    target.description = description
    target.rowNumber = rowNumber
    target.flags = flags
}

// ============================================================================
// Document Conversion Functions
// ============================================================================

/**
 * Converts Java Document to Kotlin DocumentData.
 */
fun Document.toDocumentData(): DocumentData = DocumentData(
    id = id,
    number = number,
    periodId = periodId,
    date = date
)

/**
 * Converts Kotlin DocumentData to Java Document.
 */
fun DocumentData.toDocument(): Document = Document().apply {
    id = this@toDocument.id
    number = this@toDocument.number
    periodId = this@toDocument.periodId
    date = this@toDocument.date
}

/**
 * Copies values from DocumentData to existing Java Document.
 */
fun DocumentData.copyTo(target: Document) {
    target.id = id
    target.number = number
    target.periodId = periodId
    target.date = date
}

/**
 * Sets DocumentData values to PreparedStatement for INSERT/UPDATE.
 * Parameter order: number, periodId, date
 * 
 * @param startIndex 1-based index where to start setting parameters (default 1)
 */
fun PreparedStatement.setDocumentValues(
    data: DocumentData,
    startIndex: Int = 1
) {
    setInt(startIndex, data.number)
    setInt(startIndex + 1, data.periodId)
    setDate(startIndex + 2, data.date?.let { java.sql.Date(it.time) })
}

// ============================================================================
// Period Conversion Functions
// ============================================================================

/**
 * Converts Java Period to Kotlin PeriodData.
 */
fun Period.toPeriodData(): PeriodData = PeriodData(
    id = id,
    startDate = startDate,
    endDate = endDate,
    locked = isLocked()
)

/**
 * Converts Kotlin PeriodData to Java Period.
 */
fun PeriodData.toPeriod(): Period = Period().apply {
    id = this@toPeriod.id
    startDate = this@toPeriod.startDate
    endDate = this@toPeriod.endDate
    setLocked(this@toPeriod.locked)
}

/**
 * Copies values from PeriodData to existing Java Period.
 */
fun PeriodData.copyTo(target: Period) {
    target.id = id
    target.startDate = startDate
    target.endDate = endDate
    target.setLocked(locked)
}

/**
 * Sets PeriodData values to PreparedStatement for INSERT/UPDATE.
 * Parameter order: startDate, endDate, locked
 * 
 * @param startIndex 1-based index where to start setting parameters (default 1)
 */
fun PreparedStatement.setPeriodValues(
    data: PeriodData,
    startIndex: Int = 1
) {
    setDate(startIndex, data.startDate?.let { java.sql.Date(it.time) })
    setDate(startIndex + 1, data.endDate?.let { java.sql.Date(it.time) })
    setBooleanAsInt(startIndex + 2, data.locked)
}

// ============================================================================
// DocumentType Conversion Functions
// ============================================================================

/**
 * Converts Java DocumentType to Kotlin DocumentTypeData.
 */
fun DocumentType.toDocumentTypeData(): DocumentTypeData = DocumentTypeData(
    id = id,
    number = number,
    name = name ?: "",
    numberStart = numberStart,
    numberEnd = numberEnd
)

/**
 * Converts Kotlin DocumentTypeData to Java DocumentType.
 */
fun DocumentTypeData.toDocumentType(): DocumentType = DocumentType().apply {
    id = this@toDocumentType.id
    number = this@toDocumentType.number
    name = this@toDocumentType.name
    numberStart = this@toDocumentType.numberStart
    numberEnd = this@toDocumentType.numberEnd
}

/**
 * Copies values from DocumentTypeData to existing Java DocumentType.
 */
fun DocumentTypeData.copyTo(target: DocumentType) {
    target.id = id
    target.number = number
    target.name = name
    target.numberStart = numberStart
    target.numberEnd = numberEnd
}

/**
 * Sets DocumentTypeData values to PreparedStatement for INSERT/UPDATE.
 * Parameter order: number, name, numberStart, numberEnd
 * 
 * @param startIndex 1-based index where to start setting parameters (default 1)
 */
fun PreparedStatement.setDocumentTypeValues(
    data: DocumentTypeData,
    startIndex: Int = 1
) {
    setInt(startIndex, data.number)
    setString(startIndex + 1, data.name)
    setInt(startIndex + 2, data.numberStart)
    setInt(startIndex + 3, data.numberEnd)
}

// ============================================================================
// COAHeading Conversion Functions
// ============================================================================

/**
 * Converts Java COAHeading to Kotlin COAHeadingData.
 */
fun COAHeading.toCOAHeadingData(): COAHeadingData = COAHeadingData(
    id = id,
    number = number ?: "",
    text = text ?: "",
    level = level
)

/**
 * Converts Kotlin COAHeadingData to Java COAHeading.
 */
fun COAHeadingData.toCOAHeading(): COAHeading = COAHeading().apply {
    id = this@toCOAHeading.id
    number = this@toCOAHeading.number
    text = this@toCOAHeading.text
    level = this@toCOAHeading.level
}

/**
 * Copies values from COAHeadingData to existing Java COAHeading.
 */
fun COAHeadingData.copyTo(target: COAHeading) {
    target.id = id
    target.number = number
    target.text = text
    target.level = level
}

/**
 * Sets COAHeadingData values to PreparedStatement for INSERT/UPDATE.
 * Parameter order: number, text, level
 * 
 * @param startIndex 1-based index where to start setting parameters (default 1)
 */
fun PreparedStatement.setCOAHeadingValues(
    data: COAHeadingData,
    startIndex: Int = 1
) {
    setString(startIndex, data.number)
    setString(startIndex + 1, data.text)
    setInt(startIndex + 2, data.level)
}

// ============================================================================
// Session Extensions
// ============================================================================

/**
 * Extension property for SQLiteSession to get insertId.
 * Works with both SQLiteSession (Java) and SQLiteSessionKt (Kotlin).
 */
val Session.insertId: Int
    @Throws(SQLException::class)
    get() = when (this) {
        is kirjanpito.db.sqlite.SQLiteSession -> this.getInsertId()
        is kirjanpito.db.sqlite.SQLiteSessionKt -> this.getInsertId()
        else -> throw UnsupportedOperationException("insertId not supported for ${this::class.simpleName}")
    }

/**
 * Extension function for Session to prepare statements.
 * Works with both SQLiteSession (Java) and SQLiteSessionKt (Kotlin).
 */
@Throws(SQLException::class)
fun Session.prepareStatement(sql: String): PreparedStatement = when (this) {
    is kirjanpito.db.sqlite.SQLiteSession -> this.prepareStatement(sql)
    is kirjanpito.db.sqlite.SQLiteSessionKt -> this.prepareStatement(sql)
    else -> throw UnsupportedOperationException("prepareStatement not supported for ${this::class.simpleName}")
}