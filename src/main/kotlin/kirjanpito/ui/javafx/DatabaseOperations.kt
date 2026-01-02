package kirjanpito.ui.javafx

import javafx.application.Platform
import javafx.scene.control.ProgressIndicator
import javafx.scene.layout.StackPane
import kirjanpito.db.*
import kirjanpito.db.getAllAsync
import kirjanpito.db.getByPeriodIdAsync
import kirjanpito.db.saveAsync
import kirjanpito.util.CoroutineUtils.launchDB
import kirjanpito.util.CoroutineUtils.withUI
import kotlinx.coroutines.Job
import java.util.function.Consumer

/**
 * Coroutine-pohjaiset tietokantaoperaatiot JavaFX:lle.
 * 
 * Tarjoaa helppokäyttöiset metodit tietokantakyselyihin, jotka:
 * - Suoritetaan taustasäikeessä (ei jäädytä UI:ta)
 * - Palauttavat tulokset JavaFX-säikeeseen
 * - Näyttävät latausanimaation tarvittaessa
 * 
 * Käyttöesimerkki Kotlinissa:
 * ```kotlin
 * DatabaseOperations.loadAccounts(dataSource) { accounts ->
 *     accountCombo.items.setAll(accounts)
 * }
 * ```
 * 
 * Käyttöesimerkki Javassa:
 * ```java
 * DatabaseOperations.loadAccountsJava(dataSource, accounts -> {
 *     accountCombo.getItems().setAll(accounts);
 * }, null);
 * ```
 */
object DatabaseOperations {
    
    /**
     * Lataa kaikki tilit taustasäikeessä.
     * 
     * @param dataSource Tietolähde
     * @param onSuccess Kutsutaan JavaFX-säikeessä kun tilit on ladattu
     * @param onError Kutsutaan JavaFX-säikeessä jos virhe (valinnainen)
     * @return Job jonka voi peruuttaa
     */
    fun loadAccounts(
        dataSource: DataSource,
        onSuccess: (List<Account>) -> Unit,
        onError: ((Exception) -> Unit)? = null
    ): Job = launchDB {
        try {
            val session = dataSource.openSession()
            try {
                val accountDAO = dataSource.getAccountDAO(session)
                val accounts = accountDAO.getAllAsync()
                
                withUI {
                    onSuccess(accounts)
                }
            } finally {
                session.close()
            }
        } catch (e: Exception) {
            withUI {
                onError?.invoke(e) ?: e.printStackTrace()
            }
        }
    }
    
    /**
     * Lataa kaikki tilikaudet taustasäikeessä.
     */
    fun loadPeriods(
        dataSource: DataSource,
        onSuccess: (List<Period>) -> Unit,
        onError: ((Exception) -> Unit)? = null
    ): Job = launchDB {
        try {
            val session = dataSource.openSession()
            try {
                val periodDAO = dataSource.getPeriodDAO(session)
                val periods = periodDAO.getAllAsync()
                
                withUI {
                    onSuccess(periods)
                }
            } finally {
                session.close()
            }
        } catch (e: Exception) {
            withUI {
                onError?.invoke(e) ?: e.printStackTrace()
            }
        }
    }
    
    /**
     * Lataa tositteet tietylle tilikaudelle taustasäikeessä.
     */
    fun loadDocuments(
        dataSource: DataSource,
        periodId: Int,
        documentTypeNumber: Int = 0,
        onSuccess: (List<Document>) -> Unit,
        onError: ((Exception) -> Unit)? = null
    ): Job = launchDB {
        try {
            val session = dataSource.openSession()
            try {
                val documentDAO = dataSource.getDocumentDAO(session)
                val documents = documentDAO.getByPeriodIdAsync(periodId, documentTypeNumber)
                
                withUI {
                    onSuccess(documents)
                }
            } finally {
                session.close()
            }
        } catch (e: Exception) {
            withUI {
                onError?.invoke(e) ?: e.printStackTrace()
            }
        }
    }
    
    /**
     * Lataa viennit tietylle tositteelle taustasäikeessä.
     */
    fun loadEntries(
        dataSource: DataSource,
        documentId: Int,
        onSuccess: (List<Entry>) -> Unit,
        onError: ((Exception) -> Unit)? = null
    ): Job = launchDB {
        try {
            val session = dataSource.openSession()
            try {
                val entryDAO = dataSource.getEntryDAO(session)
                val entries = entryDAO.getByDocumentIdAsync(documentId)
                
                withUI {
                    onSuccess(entries)
                }
            } finally {
                session.close()
            }
        } catch (e: Exception) {
            withUI {
                onError?.invoke(e) ?: e.printStackTrace()
            }
        }
    }
    
    /**
     * Tallentaa tositteen taustasäikeessä.
     */
    fun saveDocument(
        dataSource: DataSource,
        document: Document,
        onSuccess: () -> Unit,
        onError: ((Exception) -> Unit)? = null
    ): Job = launchDB {
        try {
            val session = dataSource.openSession()
            try {
                val documentDAO = dataSource.getDocumentDAO(session)
                documentDAO.saveAsync(document)
                
                withUI {
                    onSuccess()
                }
            } finally {
                session.close()
            }
        } catch (e: Exception) {
            withUI {
                onError?.invoke(e) ?: e.printStackTrace()
            }
        }
    }
    
    /**
     * Tallentaa viennin taustasäikeessä.
     */
    fun saveEntry(
        dataSource: DataSource,
        entry: Entry,
        onSuccess: () -> Unit,
        onError: ((Exception) -> Unit)? = null
    ): Job = launchDB {
        try {
            val session = dataSource.openSession()
            try {
                val entryDAO = dataSource.getEntryDAO(session)
                entryDAO.saveAsync(entry)
                
                withUI {
                    onSuccess()
                }
            } finally {
                session.close()
            }
        } catch (e: Exception) {
            withUI {
                onError?.invoke(e) ?: e.printStackTrace()
            }
        }
    }
    
    /**
     * Lataa kaikki tositetyypit taustasäikeessä.
     */
    fun loadDocumentTypes(
        dataSource: DataSource,
        onSuccess: (List<DocumentType>) -> Unit,
        onError: ((Exception) -> Unit)? = null
    ): Job = launchDB {
        try {
            val session = dataSource.openSession()
            try {
                val documentTypeDAO = dataSource.getDocumentTypeDAO(session)
                val types = documentTypeDAO.getAllAsync()
                
                withUI {
                    onSuccess(types)
                }
            } finally {
                session.close()
            }
        } catch (e: Exception) {
            withUI {
                onError?.invoke(e) ?: e.printStackTrace()
            }
        }
    }
    
    /**
     * Lataa kaikki vientimallit taustasäikeessä.
     */
    fun loadEntryTemplates(
        dataSource: DataSource,
        onSuccess: (List<EntryTemplate>) -> Unit,
        onError: ((Exception) -> Unit)? = null
    ): Job = launchDB {
        try {
            val session = dataSource.openSession()
            try {
                val entryTemplateDAO = dataSource.getEntryTemplateDAO(session)
                val templates = entryTemplateDAO.getAllAsync()
                
                withUI {
                    onSuccess(templates)
                }
            } finally {
                session.close()
            }
        } catch (e: Exception) {
            withUI {
                onError?.invoke(e) ?: e.printStackTrace()
            }
        }
    }
    
    /**
     * Suorittaa useita tietokantakyselyjä rinnakkain.
     * 
     * Käyttöesimerkki:
     * ```kotlin
     * DatabaseOperations.loadMultiple(
     *     dataSource,
     *     accounts = true,
     *     periods = true,
     *     documentTypes = true
     * ) { accounts, periods, documentTypes, _, _ ->
     *     // Kaikki data ladattu - päivitä UI
     *     accountList.setAll(accounts)
     *     periodList.setAll(periods)
     *     typeList.setAll(documentTypes)
     * }
     * ```
     */
    fun loadMultiple(
        dataSource: DataSource,
        accounts: Boolean = false,
        periods: Boolean = false,
        documentTypes: Boolean = false,
        entryTemplates: Boolean = false,
        coaHeadings: Boolean = false,
        onSuccess: (
            accounts: List<Account>?,
            periods: List<Period>?,
            documentTypes: List<DocumentType>?,
            entryTemplates: List<EntryTemplate>?,
            coaHeadings: List<COAHeading>?
        ) -> Unit,
        onError: ((Exception) -> Unit)? = null
    ): Job = launchDB {
        try {
            val session = dataSource.openSession()
            try {
                val accountList = if (accounts) dataSource.getAccountDAO(session).getAllAsync() else null
                val periodList = if (periods) dataSource.getPeriodDAO(session).getAllAsync() else null
                val typeList = if (documentTypes) dataSource.getDocumentTypeDAO(session).getAllAsync() else null
                val templateList = if (entryTemplates) dataSource.getEntryTemplateDAO(session).getAllAsync() else null
                val headingList = if (coaHeadings) dataSource.getCOAHeadingDAO(session).getAllAsync() else null
                
                withUI {
                    onSuccess(accountList, periodList, typeList, templateList, headingList)
                }
            } finally {
                session.close()
            }
        } catch (e: Exception) {
            withUI {
                onError?.invoke(e) ?: e.printStackTrace()
            }
        }
    }
    
    /**
     * Näyttää latausanimaation suorituksen aikana.
     * 
     * @param container StackPane johon indikaattori lisätään
     * @param operation Funktio joka suoritetaan
     */
    fun withLoadingIndicator(
        container: StackPane,
        operation: () -> Job
    ): Job {
        val indicator = ProgressIndicator().apply {
            maxWidth = 50.0
            maxHeight = 50.0
        }
        
        Platform.runLater {
            container.children.add(indicator)
        }
        
        val job = operation()
        
        job.invokeOnCompletion {
            Platform.runLater {
                container.children.remove(indicator)
            }
        }
        
        return job
    }
    
    // ===== JAVA-YHTEENSOPIVAT METODIT =====
    // Käytä näitä Java-koodista Consumer-rajapinnalla
    
    /**
     * Java-yhteensopiva versio loadAccounts-metodista.
     * 
     * Käyttöesimerkki:
     * ```java
     * DatabaseOperations.INSTANCE.loadAccountsJava(dataSource, accounts -> {
     *     accountList.setAll(accounts);
     * }, error -> {
     *     showError("Virhe", error.getMessage());
     * });
     * ```
     */
    @JvmStatic
    fun loadAccountsJava(
        dataSource: DataSource,
        onSuccess: Consumer<List<Account>>,
        onError: Consumer<Exception>?
    ): Job = loadAccounts(dataSource, { onSuccess.accept(it) }, onError?.let { { e -> it.accept(e) } })
    
    /**
     * Java-yhteensopiva versio loadPeriods-metodista.
     */
    @JvmStatic
    fun loadPeriodsJava(
        dataSource: DataSource,
        onSuccess: Consumer<List<Period>>,
        onError: Consumer<Exception>?
    ): Job = loadPeriods(dataSource, { onSuccess.accept(it) }, onError?.let { { e -> it.accept(e) } })
    
    /**
     * Java-yhteensopiva versio loadDocuments-metodista.
     */
    @JvmStatic
    fun loadDocumentsJava(
        dataSource: DataSource,
        periodId: Int,
        documentTypeNumber: Int,
        onSuccess: Consumer<List<Document>>,
        onError: Consumer<Exception>?
    ): Job = loadDocuments(dataSource, periodId, documentTypeNumber, { onSuccess.accept(it) }, onError?.let { { e -> it.accept(e) } })
    
    /**
     * Java-yhteensopiva versio loadEntries-metodista.
     */
    @JvmStatic
    fun loadEntriesJava(
        dataSource: DataSource,
        documentId: Int,
        onSuccess: Consumer<List<Entry>>,
        onError: Consumer<Exception>?
    ): Job = loadEntries(dataSource, documentId, { onSuccess.accept(it) }, onError?.let { { e -> it.accept(e) } })
    
    /**
     * Java-yhteensopiva versio loadDocumentTypes-metodista.
     */
    @JvmStatic
    fun loadDocumentTypesJava(
        dataSource: DataSource,
        onSuccess: Consumer<List<DocumentType>>,
        onError: Consumer<Exception>?
    ): Job = loadDocumentTypes(dataSource, { onSuccess.accept(it) }, onError?.let { { e -> it.accept(e) } })
    
    /**
     * Java-yhteensopiva versio loadEntryTemplates-metodista.
     */
    @JvmStatic
    fun loadEntryTemplatesJava(
        dataSource: DataSource,
        onSuccess: Consumer<List<EntryTemplate>>,
        onError: Consumer<Exception>?
    ): Job = loadEntryTemplates(dataSource, { onSuccess.accept(it) }, onError?.let { { e -> it.accept(e) } })
}
