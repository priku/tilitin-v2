package kirjanpito.ui.javafx.dialogs

import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.layout.GridPane
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import javafx.stage.Modality
import javafx.stage.Stage
import javafx.stage.Window

/**
 * JavaFX pikanäppäimet-dialogi.
 * Näyttää listan kaikista käytettävissä olevista pikanäppäimistä.
 * 
 * Migrated from Java to Kotlin for better code conciseness.
 */
class KeyboardShortcutsDialogFX(owner: Window?) {

    private val dialog: Stage

    init {
        dialog = Stage().apply {
            initModality(Modality.APPLICATION_MODAL)
            owner?.let { initOwner(it) }
            title = "Pikanäppäimet"
            isResizable = true
        }

        createContent()
    }

    private fun createContent() {
        val root = VBox(10.0).apply {
            padding = Insets(20.0)
            style = "-fx-background-color: #ffffff;"
        }

        // Create scrollable content
        val scrollPane = ScrollPane().apply {
            isFitToWidth = true
            prefHeight = 400.0
        }

        val content = VBox(15.0).apply {
            padding = Insets(10.0)
        }

        // File operations
        content.children.add(createSection("Tiedosto", arrayOf(
            arrayOf("Ctrl+U", "Uusi tietokanta"),
            arrayOf("Ctrl+O", "Avaa tietokanta"),
            arrayOf("Ctrl+Q", "Lopeta")
        )))

        // Document operations
        content.children.add(createSection("Tosite", arrayOf(
            arrayOf("Ctrl+N", "Uusi tosite"),
            arrayOf("Ctrl+Delete", "Poista tosite"),
            arrayOf("Ctrl+S", "Tallenna"),
            arrayOf("F8", "Lisää vienti"),
            arrayOf("Shift+Delete", "Poista vienti")
        )))

        // Navigation
        content.children.add(createSection("Navigointi", arrayOf(
            arrayOf("Page Up", "Edellinen tosite"),
            arrayOf("Page Down", "Seuraava tosite"),
            arrayOf("Ctrl+Page Up", "Ensimmäinen tosite"),
            arrayOf("Ctrl+Page Down", "Viimeinen tosite"),
            arrayOf("Ctrl+G", "Siirry tositteeseen"),
            arrayOf("Ctrl+F", "Etsi"),
            arrayOf("Ctrl+Left", "Edellinen tosite"),
            arrayOf("Ctrl+Right", "Seuraava tosite")
        )))

        // Editing
        content.children.add(createSection("Muokkaus", arrayOf(
            arrayOf("Ctrl+C", "Kopioi"),
            arrayOf("Ctrl+V", "Liitä"),
            arrayOf("Ctrl+T", "Tilikartta"),
            arrayOf("Ctrl+B", "Alkusaldot"),
            arrayOf("Ctrl+M", "Muokkaa vientimalleja"),
            arrayOf("Ctrl+K", "Luo vientimalli tositteesta"),
            arrayOf("F9", "Valitse tili")
        )))

        // Document types
        content.children.add(createSection("Tositelajit", arrayOf(
            arrayOf("Alt+Q", "Tositelaji 1"),
            arrayOf("Alt+W", "Tositelaji 2"),
            arrayOf("Alt+E", "Tositelaji 3"),
            arrayOf("Alt+R", "Tositelaji 4"),
            arrayOf("Alt+T", "Tositelaji 5"),
            arrayOf("Alt+Y", "Tositelaji 6"),
            arrayOf("Alt+U", "Tositelaji 7"),
            arrayOf("Alt+I", "Tositelaji 8"),
            arrayOf("Alt+O", "Tositelaji 9"),
            arrayOf("Alt+P", "Tositelaji 10")
        )))

        // Reports
        content.children.add(createSection("Tulosteet", arrayOf(
            arrayOf("Ctrl+1", "Tilien saldot"),
            arrayOf("Ctrl+2", "Tosite"),
            arrayOf("Ctrl+3", "Tiliote"),
            arrayOf("Ctrl+4", "Tuloslaskelma"),
            arrayOf("Ctrl+5", "Tuloslaskelma erittelyin"),
            arrayOf("Ctrl+6", "Tase"),
            arrayOf("Ctrl+7", "Tase erittelyin"),
            arrayOf("Ctrl+8", "Päiväkirja"),
            arrayOf("Ctrl+9", "Pääkirja"),
            arrayOf("Ctrl+0", "ALV-laskelma"),
            arrayOf("Ctrl+P", "Tulosta")
        )))

        // Settings
        content.children.add(createSection("Asetukset", arrayOf(
            arrayOf("Ctrl+Shift+S", "Yleiset asetukset"),
            arrayOf("Ctrl+Shift+A", "Ulkoasu"),
            arrayOf("Ctrl+Shift+P", "Perustiedot"),
            arrayOf("Ctrl+Shift+V", "ALV-merkintä")
        )))

        // Help
        content.children.add(createSection("Ohje", arrayOf(
            arrayOf("F1", "Ohje")
        )))

        scrollPane.content = content

        // Close button
        val buttonBox = HBox().apply {
            alignment = Pos.CENTER_RIGHT
            padding = Insets(10.0, 0.0, 0.0, 0.0)
        }

        val closeBtn = Button("Sulje").apply {
            isDefaultButton = true
            isCancelButton = true
            setOnAction { dialog.close() }
        }

        buttonBox.children.add(closeBtn)

        root.children.addAll(scrollPane, buttonBox)
        VBox.setVgrow(scrollPane, Priority.ALWAYS)

        val scene = Scene(root, 450.0, 500.0)
        dialog.scene = scene
    }

    private fun createSection(title: String, shortcuts: Array<Array<String>>): TitledPane {
        val pane = TitledPane().apply {
            text = title
            isCollapsible = true
            isExpanded = true
        }

        val grid = GridPane().apply {
            hgap = 20.0
            vgap = 5.0
            padding = Insets(10.0)
        }

        shortcuts.forEachIndexed { index, shortcut ->
            val keyLabel = Label(shortcut[0]).apply {
                style = "-fx-font-family: monospace; -fx-font-weight: bold; -fx-background-color: #e5e7eb; -fx-padding: 2 6; -fx-background-radius: 3;"
            }

            val descLabel = Label(shortcut[1])

            grid.add(keyLabel, 0, index)
            grid.add(descLabel, 1, index)
        }

        pane.content = grid
        return pane
    }

    fun show() {
        dialog.showAndWait()
    }
}
