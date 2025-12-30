package kirjanpito.ui.compose

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import androidx.compose.ui.awt.SwingPanel
import kirjanpito.util.AppSettings
import kirjanpito.util.Registry
import kirjanpito.models.DocumentModel
import kirjanpito.ui.DocumentFrame
import kirjanpito.ui.DocumentFramePanel
import java.awt.BorderLayout
import javax.swing.JPanel
import javax.swing.SwingUtilities
import javax.swing.UIManager

/**
 * Tilitin Compose Desktop -sovellus.
 * 
 * Moderni Kotlin Compose Desktop -toteutus Tilitin-sovellukselle.
 * 
 * @author Compose Desktop migration by Claude
 */
object TilitinApp {
    
    const val APP_NAME = "Tilitin"
    const val APP_VERSION = "2.0.3"
    
    @JvmStatic
    fun main(args: Array<String>) {
        // Parse command line arguments
        val config = parseArguments(args)
        
        // Initialize settings
        val settings = AppSettings.getInstance()
        if (config.configFile != null) {
            settings.load(config.configFile)
        } else {
            val file = java.io.File(
                AppSettings.buildDirectoryPath(APP_NAME),
                "asetukset.properties"
            )
            settings.load(file)
        }
        
        // Configure logging
        configureLogging(settings.getDirectoryPath(), config.debug)
        
        // Set up exception handler
        Thread.setDefaultUncaughtExceptionHandler { _, e ->
            java.util.logging.Logger.getLogger(LOGGER_NAME)
                .log(java.util.logging.Level.SEVERE, "Uncaught exception", e)
        }
        
        // Handle database URL from command line
        if (config.jdbcUrl != null) {
            var jdbcUrl = config.jdbcUrl
            if (!jdbcUrl.startsWith("jdbc:")) {
                jdbcUrl = String.format("jdbc:sqlite:%s", jdbcUrl.replace(java.io.File.pathSeparatorChar, '/'))
            }
            settings.set("database.url", jdbcUrl)
        }
        
        if (config.username != null) {
            settings.set("database.username", config.username)
        }
        
        if (config.password != null) {
            settings.set("database.password", config.password)
        }
        
        // Initialize registry and model
        val registry = Registry()
        val documentModel = DocumentModel(registry)
        
        // Launch Compose Desktop application
        application {
            val windowState = rememberWindowState(
                size = DpSize(1200.dp, 800.dp)
            )
            
            Window(
                onCloseRequest = ::exitApplication,
                title = "$APP_NAME $APP_VERSION",
                state = windowState
            ) {
                MaterialTheme {
                    MainContent(
                        registry = registry,
                        documentModel = documentModel,
                        settings = settings
                    )
                }
            }
        }
    }
    
    @Composable
    private fun MainContent(
        registry: Registry,
        documentModel: DocumentModel,
        settings: AppSettings
    ) {
        // State to track if the application is initialized
        var isInitialized by remember { mutableStateOf(false) }
        var showSwingContent by remember { mutableStateOf(false) }
        var initError by remember { mutableStateOf<String?>(null) }
        
        // Initialize database on first composition
        LaunchedEffect(Unit) {
            try {
                if (documentModel.initialize()) {
                    isInitialized = true
                    showSwingContent = true
                } else {
                    // Database not found, but we can still show UI
                    isInitialized = true
                    showSwingContent = true
                }
            } catch (e: Exception) {
                java.util.logging.Logger.getLogger(LOGGER_NAME)
                    .log(java.util.logging.Level.SEVERE, "Database initialization failed", e)
                initError = e.message ?: "Tuntematon virhe"
                isInitialized = true
                showSwingContent = true
            }
        }
        
        if (!isInitialized) {
            // Show loading indicator while initializing
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Alustetaan...")
                }
            }
        } else if (showSwingContent) {
            // Show Swing content using SwingPanel
            Column(modifier = Modifier.fillMaxSize()) {
                // Error banner if there was an initialization error
                if (initError != null) {
                    Surface(
                        color = MaterialTheme.colors.error,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Varoitus: $initError",
                            color = MaterialTheme.colors.onError,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
                
                // Swing DocumentFrame content
                SwingPanel(
                    modifier = Modifier.fillMaxSize(),
                    factory = {
                        // Create DocumentFramePanel (JPanel wrapper for DocumentFrame)
                        val panel = DocumentFramePanel(registry, documentModel)
                        panel
                    },
                    update = { panel ->
                        // Update panel if needed (e.g., when theme changes)
                    }
                )
            }
        } else {
            // Fallback: Show welcome screen with button to open Swing version
            Column(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Tilitin Compose Desktop",
                    style = MaterialTheme.typography.h4
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Compose Desktop -siirtymä käynnissä...",
                    style = MaterialTheme.typography.body1
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Swing-Interoperability -tuki lisätty",
                    style = MaterialTheme.typography.body2,
                    color = MaterialTheme.colors.secondary
                )
                Spacer(modifier = Modifier.height(32.dp))
                Button(
                    onClick = { 
                        showSwingContent = true
                    }
                ) {
                    Text("Avaa kirjanpito")
                }
            }
        }
    }
    
    private data class AppConfig(
        val debug: Boolean = false,
        val configFile: java.io.File? = null,
        val jdbcUrl: String? = null,
        val username: String? = null,
        val password: String? = null
    )
    
    private fun parseArguments(args: Array<String>): AppConfig {
        var debug = false
        var configFile: java.io.File? = null
        var jdbcUrl: String? = null
        var username: String? = null
        var password: String? = null
        
        var i = 0
        while (i < args.size) {
            when (args[i]) {
                "-d", "--debug" -> debug = true
                "-c", "--config" -> {
                    if (i + 1 < args.size) {
                        configFile = java.io.File(args[++i])
                    }
                }
                "-u", "--username" -> {
                    if (i + 1 < args.size) {
                        username = args[++i]
                    }
                }
                "-p", "--password" -> {
                    if (i + 1 < args.size) {
                        password = args[++i]
                    }
                }
                else -> {
                    if (!args[i].startsWith("-") && i == args.size - 1) {
                        jdbcUrl = args[i]
                    }
                }
            }
            i++
        }
        
        return AppConfig(debug, configFile, jdbcUrl, username, password)
    }
    
    private fun configureLogging(directoryPath: String, debug: Boolean) {
        val level = if (debug) java.util.logging.Level.FINEST else java.util.logging.Level.WARNING
        val dir = java.io.File(directoryPath)
        var foundConsoleHandler = false
        var foundFileHandler = false

        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                System.err.println("Hakemiston $directoryPath luominen epäonnistui.")
            }
        }

        try {
            val handlers = java.util.logging.Logger.getLogger("").handlers

            // Tarkistetaan, onko ConsoleHandler tai FileHandler jo lisätty
            for (handler in handlers) {
                when (handler) {
                    is java.util.logging.ConsoleHandler -> {
                        foundConsoleHandler = true
                        handler.level = level
                    }
                    is java.util.logging.FileHandler -> {
                        foundFileHandler = true
                        handler.level = level
                    }
                }
            }

            if (!foundConsoleHandler && debug) {
                // Jos debug-asetus on päällä, kirjoitetaan loki myös päätteeseen
                val consoleHandler = java.util.logging.ConsoleHandler()
                consoleHandler.level = level
                consoleHandler.formatter = java.util.logging.SimpleFormatter()
                java.util.logging.Logger.getLogger(LOGGER_NAME).addHandler(consoleHandler)
            }

            if (!foundFileHandler) {
                // Kirjoitetaan loki tiedostoon
                val logFile = java.io.File(dir, "$LOGGER_NAME.log.txt")
                val fileHandler = java.util.logging.FileHandler(
                    logFile.absolutePath, 
                    20 * 1024, 
                    1, 
                    true
                )
                fileHandler.level = level
                fileHandler.formatter = java.util.logging.SimpleFormatter()
                java.util.logging.Logger.getLogger(LOGGER_NAME).addHandler(fileHandler)
            }

            java.util.logging.Logger.getLogger(LOGGER_NAME).level = level
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    private const val LOGGER_NAME = "kirjanpito"
}

