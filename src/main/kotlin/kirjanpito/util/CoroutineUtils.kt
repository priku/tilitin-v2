package kirjanpito.util

import javafx.application.Platform
import kotlinx.coroutines.*
import kotlinx.coroutines.javafx.JavaFx
import java.util.concurrent.Executors

/**
 * Coroutine utilities for asynchronous database operations.
 * 
 * Provides a clean way to run database queries in the background
 * while keeping the UI responsive.
 * 
 * Usage:
 * ```kotlin
 * // In a JavaFX controller or dialog:
 * launchIO {
 *     val accounts = accountDAO.getAll()
 *     withUI {
 *         updateTable(accounts)
 *     }
 * }
 * ```
 */
object CoroutineUtils {
    
    /**
     * Dedicated thread pool for database operations.
     * Using a fixed pool to prevent too many concurrent DB connections.
     */
    private val dbDispatcher = Executors.newFixedThreadPool(4).asCoroutineDispatcher()
    
    /**
     * Application-wide coroutine scope for background tasks.
     * Uses SupervisorJob so one failing task doesn't cancel others.
     */
    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    
    /**
     * Launch a coroutine on the IO dispatcher (for database/file operations).
     * Results can be posted to UI using withUI { }.
     */
    fun launchIO(block: suspend CoroutineScope.() -> Unit): Job {
        return applicationScope.launch(Dispatchers.IO, block = block)
    }
    
    /**
     * Launch a coroutine on the database dispatcher.
     * Use this for heavy database operations to limit concurrent connections.
     */
    fun launchDB(block: suspend CoroutineScope.() -> Unit): Job {
        return applicationScope.launch(dbDispatcher, block = block)
    }
    
    /**
     * Launch a coroutine on the JavaFX UI thread.
     */
    fun launchUI(block: suspend CoroutineScope.() -> Unit): Job {
        return applicationScope.launch(Dispatchers.JavaFx, block = block)
    }
    
    /**
     * Switch to UI thread within a coroutine.
     */
    suspend fun <T> withUI(block: suspend CoroutineScope.() -> T): T {
        return withContext(Dispatchers.JavaFx, block)
    }
    
    /**
     * Switch to IO thread within a coroutine.
     */
    suspend fun <T> withIO(block: suspend CoroutineScope.() -> T): T {
        return withContext(Dispatchers.IO, block)
    }
    
    /**
     * Switch to database thread within a coroutine.
     */
    suspend fun <T> withDB(block: suspend CoroutineScope.() -> T): T {
        return withContext(dbDispatcher, block)
    }
    
    /**
     * Run multiple database queries in parallel and wait for all results.
     * 
     * Example:
     * ```kotlin
     * val (accounts, entries, documents) = parallel(
     *     { accountDAO.getAll() },
     *     { entryDAO.getByPeriod(periodId) },
     *     { documentDAO.getByPeriod(periodId) }
     * )
     * ```
     */
    suspend fun <A, B> parallel(
        block1: suspend () -> A,
        block2: suspend () -> B
    ): Pair<A, B> = coroutineScope {
        val result1 = async(Dispatchers.IO) { block1() }
        val result2 = async(Dispatchers.IO) { block2() }
        Pair(result1.await(), result2.await())
    }
    
    suspend fun <A, B, C> parallel(
        block1: suspend () -> A,
        block2: suspend () -> B,
        block3: suspend () -> C
    ): Triple<A, B, C> = coroutineScope {
        val result1 = async(Dispatchers.IO) { block1() }
        val result2 = async(Dispatchers.IO) { block2() }
        val result3 = async(Dispatchers.IO) { block3() }
        Triple(result1.await(), result2.await(), result3.await())
    }
    
    /**
     * Shutdown the application scope. Call this when the app exits.
     */
    fun shutdown() {
        applicationScope.cancel()
        dbDispatcher.close()
    }
}

/**
 * Extension function for easy coroutine launching from any class.
 */
fun Any.launchIO(block: suspend CoroutineScope.() -> Unit): Job = CoroutineUtils.launchIO(block)
fun Any.launchDB(block: suspend CoroutineScope.() -> Unit): Job = CoroutineUtils.launchDB(block)
fun Any.launchUI(block: suspend CoroutineScope.() -> Unit): Job = CoroutineUtils.launchUI(block)
