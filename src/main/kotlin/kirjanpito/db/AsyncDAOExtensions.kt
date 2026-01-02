package kirjanpito.db

import kirjanpito.util.CoroutineUtils.withDB

/**
 * Async extensions for DAO operations.
 * 
 * These suspend functions allow non-blocking database access from coroutines.
 * The database operations run on a dedicated thread pool to prevent UI freezing.
 * 
 * Usage:
 * ```kotlin
 * launchIO {
 *     val accounts = accountDAO.getAllAsync()
 *     val documents = documentDAO.getByPeriodIdAsync(periodId, 0)
 *     
 *     withUI {
 *         updateTable(accounts)
 *     }
 * }
 * ```
 */

// ============= AccountDAO Extensions =============

suspend fun AccountDAO.getAllAsync(): List<Account> = withDB { getAll() }

suspend fun AccountDAO.saveAsync(account: Account) = withDB { save(account) }

suspend fun AccountDAO.deleteAsync(id: Int) = withDB { delete(id) }


// ============= DocumentDAO Extensions =============

suspend fun DocumentDAO.createAsync(periodId: Int, numberStart: Int, numberEnd: Int): Document = 
    withDB { create(periodId, numberStart, numberEnd) }

suspend fun DocumentDAO.getByPeriodIdAsync(periodId: Int, numberOffset: Int = 0): List<Document> = 
    withDB { getByPeriodId(periodId, numberOffset) }

suspend fun DocumentDAO.getByPeriodIdAndNumberAsync(periodId: Int, number: Int): Document? = 
    withDB { getByPeriodIdAndNumber(periodId, number) }

suspend fun DocumentDAO.saveAsync(document: Document) = withDB { save(document) }

suspend fun DocumentDAO.deleteAsync(id: Int) = withDB { delete(id) }

suspend fun DocumentDAO.deleteByPeriodIdAsync(periodId: Int) = withDB { deleteByPeriodId(periodId) }


// ============= EntryDAO Extensions =============

suspend fun EntryDAO.getByDocumentIdAsync(documentId: Int): List<Entry> = 
    withDB { getByDocumentId(documentId) }

suspend fun EntryDAO.saveAsync(entry: Entry) = withDB { save(entry) }

suspend fun EntryDAO.deleteAsync(id: Int) = withDB { delete(id) }


// ============= PeriodDAO Extensions =============

suspend fun PeriodDAO.getAllAsync(): List<Period> = withDB { getAll() }

suspend fun PeriodDAO.saveAsync(period: Period) = withDB { save(period) }

suspend fun PeriodDAO.deleteAsync(id: Int) = withDB { delete(id) }


// ============= COAHeadingDAO Extensions =============

suspend fun COAHeadingDAO.getAllAsync(): List<COAHeading> = withDB { getAll() }

suspend fun COAHeadingDAO.saveAsync(heading: COAHeading) = withDB { save(heading) }

suspend fun COAHeadingDAO.deleteAsync(id: Int) = withDB { delete(id) }


// ============= DocumentTypeDAO Extensions =============

suspend fun DocumentTypeDAO.getAllAsync(): List<DocumentType> = withDB { getAll() }

suspend fun DocumentTypeDAO.saveAsync(documentType: DocumentType) = withDB { save(documentType) }

suspend fun DocumentTypeDAO.deleteAsync(id: Int) = withDB { delete(id) }


// ============= EntryTemplateDAO Extensions =============

suspend fun EntryTemplateDAO.getAllAsync(): List<EntryTemplate> = withDB { getAll() }

suspend fun EntryTemplateDAO.saveAsync(template: EntryTemplate) = withDB { save(template) }

suspend fun EntryTemplateDAO.deleteAsync(id: Int) = withDB { delete(id) }


// ============= ReportStructureDAO Extensions =============

suspend fun ReportStructureDAO.saveAsync(structure: ReportStructure) = withDB { save(structure) }
