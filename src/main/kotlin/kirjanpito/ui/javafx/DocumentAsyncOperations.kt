package kirjanpito.ui.javafx

import javafx.application.Platform
import kirjanpito.db.*
import kirjanpito.util.CoroutineUtils.launchDB
import kirjanpito.util.CoroutineUtils.withUI
import java.util.function.Consumer

/**
 * Async document operations using Kotlin coroutines.
 * 
 * Provides async versions of document loading, saving, and account refreshing
 * to keep the UI responsive during database operations.
 * 
 * All callbacks are executed on the JavaFX Application Thread.
 */
object DocumentAsyncOperations {
    
    /**
     * Loads a document and its entries asynchronously.
     * 
     * @param dataSource Data source for database operations
     * @param doc Document to load
     * @param onSuccess Callback called with loaded entries on success (runs on JavaFX thread)
     * @param onError Callback called with error message on failure (runs on JavaFX thread)
     */
    @JvmStatic
    fun loadDocumentAsync(
        dataSource: DataSource?,
        doc: Document?,
        onSuccess: Consumer<List<Entry>>,
        onError: Consumer<String>
    ) {
        if (dataSource == null || doc == null) {
            Platform.runLater { onError.accept("Data source or document is null") }
            return
        }
        
        launchDB {
            try {
                val session = dataSource.openSession()
                try {
                    val entryDAO = dataSource.getEntryDAO(session)
                    val entries = entryDAO.getByDocumentId(doc.id)
                    
                    withUI {
                        onSuccess.accept(entries)
                    }
                } finally {
                    session.close()
                }
            } catch (e: Exception) {
                withUI {
                    onError.accept("Error loading document: ${e.message}")
                }
            }
        }
    }
    
    /**
     * Saves a document and its entries asynchronously.
     * 
     * @param dataSource Data source for database operations
     * @param doc Document to save
     * @param entriesToSave List of entries to save
     * @param entriesToDelete List of entries to delete
     * @param onSuccess Callback called on success (runs on JavaFX thread)
     * @param onError Callback called with error message on failure (runs on JavaFX thread)
     */
    @JvmStatic
    fun saveDocumentAsync(
        dataSource: DataSource?,
        doc: Document?,
        entriesToSave: List<Entry>,
        entriesToDelete: List<Entry>?,
        onSuccess: Runnable,
        onError: Consumer<String>
    ) {
        if (dataSource == null || doc == null) {
            Platform.runLater { onError.accept("Data source or document is null") }
            return
        }
        
        launchDB {
            try {
                val session = dataSource.openSession()
                try {
                    // Save document
                    val documentDAO = dataSource.getDocumentDAO(session)
                    documentDAO.save(doc)
                    
                    // Save entries
                    val entryDAO = dataSource.getEntryDAO(session)
                    
                    // Delete entries that were removed
                    entriesToDelete?.forEach { entry ->
                        entryDAO.delete(entry.id)
                    }
                    
                    // Save/update entries
                    entriesToSave.forEach { entry ->
                        entryDAO.save(entry)
                    }
                    
                    // Commit transaction
                    session.commit()
                    
                    withUI {
                        onSuccess.run()
                    }
                } catch (e: Exception) {
                    session.rollback()
                    throw e
                } finally {
                    session.close()
                }
            } catch (e: Exception) {
                withUI {
                    onError.accept("Error saving document: ${e.message}")
                }
            }
        }
    }
    
    /**
     * Refreshes accounts list asynchronously.
     * 
     * @param dataSource Data source for database operations
     * @param onSuccess Callback called with loaded accounts on success (runs on JavaFX thread)
     * @param onError Callback called with error message on failure (runs on JavaFX thread)
     */
    @JvmStatic
    fun refreshAccountsAsync(
        dataSource: DataSource?,
        onSuccess: Consumer<List<Account>>,
        onError: Consumer<String>
    ) {
        if (dataSource == null) {
            Platform.runLater { onError.accept("Data source is null") }
            return
        }
        
        launchDB {
            try {
                val session = dataSource.openSession()
                try {
                    val accountDAO = dataSource.getAccountDAO(session)
                    val accounts = accountDAO.getAll()
                    
                    withUI {
                        onSuccess.accept(accounts)
                    }
                } finally {
                    session.close()
                }
            } catch (e: Exception) {
                withUI {
                    onError.accept("Error refreshing accounts: ${e.message}")
                }
            }
        }
    }
}
