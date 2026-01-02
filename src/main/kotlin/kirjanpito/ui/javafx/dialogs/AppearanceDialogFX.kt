package kirjanpito.ui.javafx.dialogs

import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Parent
import javafx.scene.control.*
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.scene.text.Font
import javafx.stage.Window
import kirjanpito.util.AppSettings

/**
 * JavaFX ulkoasu-dialogi.
 * Mahdollistaa teeman ja fonttikoon vaihdon.
 * 
 * Migrated from Java to Kotlin for better code conciseness and null-safety.
 * Uses BaseDialogFX for consistent dialog behavior.
 */
class AppearanceDialogFX(owner: Window?) : BaseDialogFX(
    owner = owner,
    title = "Ulkoasu",
    width = 350.0,
    height = 280.0,
    resizable = false
) {
    
    private val settings: AppSettings = AppSettings.getInstance()
    private val originalTheme: String
    private val originalFontSize: Int
    
    private lateinit var themeCombo: ComboBox<String>
    private lateinit var fontSizeSpinner: Spinner<Int>
    private lateinit var previewLabel: Label
    
    private var onThemeChangedCallback: ((String) -> Unit)? = null
    
    init {
        originalTheme = settings.getString("theme", "Vaalea")
        originalFontSize = settings.getInt("fontSize", 13)
    }
    
    override fun createOKButton(): Button {
        return Button("OK").apply {
            isDefaultButton = true
            style = "-fx-background-color: #2563eb; -fx-text-fill: white; -fx-min-width: 80;"
            setOnAction {
                if (onOK()) {
                    dialog.close()
                }
            }
        }
    }
    
    override fun createCancelButton(): Button {
        return Button("Peruuta").apply {
            isCancelButton = true
            setOnAction { onCancel() }
        }
    }
    
    override fun createContent(): Parent {
        val root = VBox(15.0).apply {
            padding = Insets(20.0)
            style = "-fx-background-color: #ffffff;"
        }
        
        // Theme selection
        val themeBox = HBox(15.0).apply {
            alignment = Pos.CENTER_LEFT
        }
        
        val themeLabel = Label("Teema:").apply {
            minWidth = 100.0
        }
        
        themeCombo = ComboBox<String>().apply {
            items.addAll("Vaalea", "Tumma", "Järjestelmä")
            value = originalTheme
            prefWidth = 150.0
            setOnAction {
                val newTheme = value
                settings.set("theme", newTheme)
                onThemeChangedCallback?.invoke(newTheme)
            }
        }
        
        themeBox.children.addAll(themeLabel, themeCombo)
        
        // Font size selection
        val fontBox = HBox(15.0).apply {
            alignment = Pos.CENTER_LEFT
        }
        
        val fontLabel = Label("Fonttikoko:").apply {
            minWidth = 100.0
        }
        
        fontSizeSpinner = Spinner<Int>().apply {
            valueFactory = javafx.scene.control.SpinnerValueFactory.IntegerSpinnerValueFactory(8, 24, originalFontSize)
            isEditable = true
            prefWidth = 80.0
            valueProperty().addListener { _, _, _ ->
                updatePreview()
            }
        }
        
        val ptLabel = Label("pt")
        
        fontBox.children.addAll(fontLabel, fontSizeSpinner, ptLabel)
        
        // Preview
        val previewPane = TitledPane().apply {
            text = "Esikatselu"
            isCollapsible = false
        }
        
        previewLabel = Label("Tämä on esimerkkiteksti.\nFonttikoko: $originalFontSize pt").apply {
            font = Font.font(originalFontSize.toDouble())
            padding = Insets(10.0)
        }
        previewPane.content = previewLabel
        
        // Info text
        val infoLabel = Label("Teema vaihtuu heti. Fonttikoko tulee voimaan uudelleenkäynnistyksen jälkeen.").apply {
            style = "-fx-font-style: italic; -fx-text-fill: #666; -fx-font-size: 11px;"
            isWrapText = true
        }
        
        root.children.addAll(themeBox, fontBox, previewPane, infoLabel)
        
        return root
    }
    
    private fun updatePreview() {
        val size = fontSizeSpinner.value
        previewLabel.font = Font.font(size.toDouble())
        previewLabel.text = "Tämä on esimerkkiteksti.\nFonttikoko: $size pt"
    }
    
    fun setOnThemeChanged(callback: (String) -> Unit) {
        this.onThemeChangedCallback = callback
    }
    
    override fun onOK(): Boolean {
        settings.set("fontSize", fontSizeSpinner.value)
        settings.save()
        return true
    }
    
    override fun onCancel() {
        // Revert to original theme
        settings.set("theme", originalTheme)
        onThemeChangedCallback?.invoke(originalTheme)
        super.onCancel()
    }
}
