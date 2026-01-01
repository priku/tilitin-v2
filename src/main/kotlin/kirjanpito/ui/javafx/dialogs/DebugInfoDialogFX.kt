package kirjanpito.ui.javafx.dialogs

import javafx.application.Platform
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.input.Clipboard
import javafx.scene.input.ClipboardContent
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import javafx.stage.Modality
import javafx.stage.Stage
import javafx.stage.Window
import kirjanpito.util.AppSettings
import kirjanpito.util.Registry
import java.io.File
import java.text.SimpleDateFormat

/**
 * JavaFX-dialogi debug-/virheenjäljitystietojen näyttämiselle.
 * Näyttää järjestelmätietoja ja sovelluksen tilasta.
 * 
 * Migrated from Java to Kotlin for better code conciseness and null-safety.
 */
class DebugInfoDialogFX(owner: Window?, private val registry: Registry?) {

    private val dialog: Stage

    init {
        dialog = Stage().apply {
            initModality(Modality.APPLICATION_MODAL)
            owner?.let { initOwner(it) }
            title = "Virheenjäljitystiedot"
            minWidth = 550.0
            minHeight = 450.0
        }

        createContent()
    }

    private fun createContent() {
        val root = VBox(16.0).apply {
            padding = Insets(20.0)
            style = "-fx-background-color: #ffffff;"
        }

        // Header
        val headerLabel = Label("Virheenjäljitystiedot").apply {
            style = "-fx-font-size: 16px; -fx-font-weight: bold;"
        }

        val descLabel = Label("Nämä tiedot ovat hyödyllisiä virheiden selvittämisessä").apply {
            style = "-fx-font-size: 12px; -fx-text-fill: #666666;"
        }

        // Info text area
        val infoArea = TextArea().apply {
            isEditable = false
            isWrapText = true
            style = "-fx-font-family: 'Consolas', 'Courier New', monospace; -fx-font-size: 12px;"
        }
        VBox.setVgrow(infoArea, Priority.ALWAYS)

        // Populate debug info
        val debugInfo = buildString {
            // Java info
            appendLine("=== JAVA ===")
            appendLine("Java versio: ${System.getProperty("java.version")}")
            appendLine("Java vendor: ${System.getProperty("java.vendor")}")
            appendLine("Java home: ${System.getProperty("java.home")}")
            appendLine("JavaFX versio: ${System.getProperty("javafx.version", "N/A")}")
            appendLine()

            // OS info
            appendLine("=== KÄYTTÖJÄRJESTELMÄ ===")
            appendLine("OS: ${System.getProperty("os.name")}")
            appendLine("OS versio: ${System.getProperty("os.version")}")
            appendLine("Arkkitehtuuri: ${System.getProperty("os.arch")}")
            appendLine()

            // Memory info
            appendLine("=== MUISTI ===")
            val runtime = Runtime.getRuntime()
            val maxMemory = runtime.maxMemory() / (1024 * 1024)
            val totalMemory = runtime.totalMemory() / (1024 * 1024)
            val freeMemory = runtime.freeMemory() / (1024 * 1024)
            val usedMemory = totalMemory - freeMemory
            appendLine("Käytössä: $usedMemory MB")
            appendLine("Varattu: $totalMemory MB")
            appendLine("Maksimi: $maxMemory MB")
            appendLine()

            // Application info
            appendLine("=== SOVELLUS ===")
            appendLine("Käyttäjän kotihakemisto: ${System.getProperty("user.home")}")
            appendLine("Työhakemisto: ${System.getProperty("user.dir")}")
            appendLine()

            // Database info
            appendLine("=== TIETOKANTA ===")
            if (registry != null && registry.getDataSource() != null) {
                try {
                    val dbPath = AppSettings.getInstance().getString("database", "N/A")
                    appendLine("Tietokanta: $dbPath")

                    val dbFile = File(dbPath)
                    if (dbFile.exists()) {
                        appendLine("Tiedostokoko: ${dbFile.length() / 1024} KB")
                        val sdf = SimpleDateFormat("d.M.yyyy HH:mm:ss")
                        appendLine("Muokattu: ${sdf.format(dbFile.lastModified())}")
                    }

                    registry.getPeriod()?.let { period ->
                        val sdf = SimpleDateFormat("d.M.yyyy")
                        appendLine("Tilikausi: ${sdf.format(period.getStartDate())} - ${sdf.format(period.getEndDate())}")
                    }

                    appendLine("Tilejä: ${registry.getAccounts()?.size ?: 0}")
                    appendLine("Vientimalleja: ${registry.getEntryTemplates()?.size ?: 0}")
                } catch (e: Exception) {
                    appendLine("Virhe tietojen haussa: ${e.message}")
                }
            } else {
                appendLine("Ei avointa tietokantaa")
            }
            appendLine()

            // Settings info
            appendLine("=== ASETUKSET ===")
            val settings = AppSettings.getInstance()
            appendLine("Varmuuskopiohakemisto: ${settings.getString("backup-directory", "N/A")}")
            appendLine("CSV-hakemisto: ${settings.getString("csv-directory", "N/A")}")
            appendLine("Auto-backup: ${if (settings.getBoolean("auto-backup", false)) "Kyllä" else "Ei"}")
        }

        infoArea.text = debugInfo

        // Buttons
        val buttonBox = HBox(10.0).apply {
            alignment = Pos.CENTER_RIGHT
            padding = Insets(10.0, 0.0, 0.0, 0.0)
        }

        val copyButton = Button("Kopioi leikepöydälle").apply {
            setOnAction {
                val clipboard = Clipboard.getSystemClipboard()
                val content = ClipboardContent().apply {
                    putString(infoArea.text)
                }
                clipboard.setContent(content)

                // Show feedback
                text = "✓ Kopioitu!"
                isDisable = true
                Thread {
                    try {
                        Thread.sleep(2000)
                        Platform.runLater {
                            text = "Kopioi leikepöydälle"
                            isDisable = false
                        }
                    } catch (e: InterruptedException) {
                        // Ignore
                    }
                }.start()
            }
        }

        val closeButton = Button("Sulje").apply {
            isCancelButton = true
            setOnAction { dialog.close() }
            prefWidth = 100.0
        }

        buttonBox.children.addAll(copyButton, closeButton)

        root.children.addAll(headerLabel, descLabel, infoArea, buttonBox)

        val scene = Scene(root, 600.0, 500.0)
        dialog.scene = scene
    }

    fun show() {
        dialog.showAndWait()
    }
}
