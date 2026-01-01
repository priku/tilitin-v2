package kirjanpito.ui.javafx.dialogs

import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.layout.Priority
import javafx.scene.layout.Region
import javafx.scene.layout.VBox
import javafx.scene.text.Text
import javafx.scene.text.TextAlignment
import javafx.stage.Modality
import javafx.stage.Stage
import javafx.stage.Window
import java.awt.Desktop
import java.net.URI

/**
 * JavaFX About dialog showing application information.
 * Migrated from Java to Kotlin for better code conciseness and null-safety.
 */
class AboutDialogFX(owner: Window?) {

    private val dialog: Stage

    companion object {
        private const val VERSION = "3.0.0"
        private const val BUILD_DATE = "2026-01-01"

        /**
         * Static factory method for showing the about dialog.
         */
        @JvmStatic
        fun showAbout(owner: Window?) {
            AboutDialogFX(owner).show()
        }
    }

    init {
        dialog = Stage().apply {
            initModality(Modality.APPLICATION_MODAL)
            owner?.let { initOwner(it) }
            title = "Tietoja ohjelmasta"
            isResizable = false
        }

        createContent()
    }

    private fun createContent() {
        val root = VBox(16.0).apply {
            padding = Insets(24.0)
            alignment = Pos.CENTER
            style = "-fx-background-color: #ffffff;"
        }

        // Application icon/logo
        val logoLabel = Label("📒").apply {
            style = "-fx-font-size: 48px;"
        }

        // Application name
        val nameLabel = Label("Tilitin").apply {
            style = "-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #1e3a8a;"
        }

        // Version info
        val versionLabel = Label("Versio $VERSION").apply {
            style = "-fx-font-size: 14px; -fx-text-fill: #64748b;"
        }

        val buildLabel = Label("Käännetty: $BUILD_DATE").apply {
            style = "-fx-font-size: 12px; -fx-text-fill: #94a3b8;"
        }

        // Description
        val descText = Text(
            "Ilmainen kirjanpito-ohjelma suomalaisille\n" +
            "yhdistyksille ja pienyrityksille.\n\n" +
            "Tilitin on avoimen lähdekoodin ohjelma,\n" +
            "joka on lisensoitu GNU GPL v3 -lisenssillä."
        ).apply {
            textAlignment = TextAlignment.CENTER
            style = "-fx-fill: #475569;"
        }

        // Separator
        val sep = Separator().apply {
            padding = Insets(8.0, 0.0, 8.0, 0.0)
        }

        // Credits
        val creditsBox = VBox(4.0).apply {
            alignment = Pos.CENTER
        }

        val originalLabel = Label("Alkuperäinen kehittäjä:").apply {
            style = "-fx-font-size: 11px; -fx-text-fill: #94a3b8;"
        }

        val authorLabel = Label("Tommi Helineva").apply {
            style = "-fx-font-size: 12px; -fx-text-fill: #64748b; -fx-font-weight: 500;"
        }

        val modernizedLabel = Label("JavaFX-modernisointi:").apply {
            style = "-fx-font-size: 11px; -fx-text-fill: #94a3b8; -fx-padding: 8 0 0 0;"
        }

        val modernAuthorLabel = Label("GitHub Copilot & priku").apply {
            style = "-fx-font-size: 12px; -fx-text-fill: #64748b; -fx-font-weight: 500;"
        }

        creditsBox.children.addAll(originalLabel, authorLabel, modernizedLabel, modernAuthorLabel)

        // Links
        val websiteLink = Hyperlink("helineva.net/tilitin").apply {
            style = "-fx-text-fill: #2563eb;"
            setOnAction {
                try {
                    Desktop.getDesktop().browse(URI("https://helineva.net/tilitin"))
                } catch (e: Exception) {
                    // Ignore
                }
            }
        }

        val githubLink = Hyperlink("GitHub: tilitin-modernized").apply {
            style = "-fx-text-fill: #2563eb;"
            setOnAction {
                try {
                    Desktop.getDesktop().browse(URI("https://github.com/priku/tilitin-modernized"))
                } catch (e: Exception) {
                    // Ignore
                }
            }
        }

        // Close button
        val closeButton = Button("Sulje").apply {
            isDefaultButton = true
            isCancelButton = true
            style = "-fx-background-color: #2563eb; -fx-text-fill: white; -fx-min-width: 100;"
            setOnAction { dialog.close() }
        }

        val spacer = Region()
        VBox.setVgrow(spacer, Priority.ALWAYS)

        root.children.addAll(
            logoLabel, nameLabel, versionLabel, buildLabel,
            Separator(),
            descText,
            sep,
            creditsBox,
            websiteLink, githubLink,
            spacer,
            closeButton
        )

        val scene = Scene(root, 350.0, 480.0)
        dialog.scene = scene
    }

    fun show() {
        dialog.showAndWait()
    }
}
