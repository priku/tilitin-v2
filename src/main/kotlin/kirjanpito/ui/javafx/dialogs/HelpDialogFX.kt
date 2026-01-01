package kirjanpito.ui.javafx.dialogs

import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.scene.text.Font
import javafx.stage.Modality
import javafx.stage.Stage
import javafx.stage.Window

/**
 * JavaFX help dialog.
 * Migrated from Java to Kotlin for better code conciseness and null-safety.
 */
class HelpDialogFX(owner: Window?) {

    private val dialog: Stage

    init {
        dialog = Stage().apply {
            initModality(Modality.APPLICATION_MODAL)
            owner?.let { initOwner(it) }
            title = "Ohje"
            isResizable = false
        }

        createContent()
    }

    private fun createContent() {
        val root = VBox(20.0).apply {
            padding = Insets(20.0)
            style = "-fx-background-color: #ffffff;"
        }

        // Title
        val titleLabel = Label("Tilitin - Kirjanpito-ohjelma").apply {
            font = Font.font(18.0)
            style = "-fx-font-weight: bold;"
        }

        // Content
        val contentBox = VBox(15.0).apply {
            padding = Insets(10.0, 0.0, 0.0, 0.0)
        }

        val versionLabel = Label("Versio: 2.2.5")
        val copyrightLabel = Label("© 2025 Tilitin")

        val separator = Separator()

        val helpBox = VBox(10.0).apply {
            padding = Insets(10.0, 0.0, 0.0, 0.0)
        }

        val shortcutsLabel = Label("Pikanäppäimet:").apply {
            style = "-fx-font-weight: bold;"
        }

        val shortcutsBox = VBox(5.0).apply {
            padding = Insets(5.0, 0.0, 0.0, 20.0)
            children.addAll(
                Label("Ctrl+N - Uusi tosite"),
                Label("Ctrl+S - Tallenna"),
                Label("Ctrl+P - Tulosta"),
                Label("Ctrl+O - Avaa tietokanta"),
                Label("F9 - Tilikartan pikahaku"),
                Label("PageUp/PageDown - Navigoi tositteissa"),
                Label("Delete - Poista vienti")
            )
        }

        helpBox.children.addAll(shortcutsLabel, shortcutsBox)

        contentBox.children.addAll(
            versionLabel,
            copyrightLabel,
            separator,
            helpBox
        )

        // Buttons
        val buttonBox = HBox(10.0).apply {
            alignment = Pos.CENTER_RIGHT
            padding = Insets(10.0, 0.0, 0.0, 0.0)
        }

        val closeBtn = Button("Sulje").apply {
            isDefaultButton = true
            setOnAction { dialog.close() }
        }

        buttonBox.children.add(closeBtn)

        root.children.addAll(titleLabel, contentBox, buttonBox)

        val scene = Scene(root, 400.0, 350.0)
        dialog.scene = scene
    }

    fun show() {
        dialog.showAndWait()
    }
}
