package kirjanpito.ui.javafx.dialogs

import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Parent
import javafx.scene.control.*
import javafx.scene.layout.GridPane
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.stage.Window
import kirjanpito.util.AppSettings

/**
 * JavaFX tulostusasetukset-dialogi.
 * 
 * Migrated from Java to Kotlin for better code conciseness and null-safety.
 * Uses BaseDialogFX for consistent dialog behavior.
 */
class PrintSettingsDialogFX(owner: Window?) : BaseDialogFX(
    owner = owner,
    title = "Tulostusasetukset",
    width = 400.0,
    height = 380.0,
    resizable = false
) {
    
    private val settings: AppSettings = AppSettings.getInstance()
    
    private lateinit var paperSizeCombo: ComboBox<String>
    private lateinit var orientationCombo: ComboBox<String>
    private lateinit var topMarginSpinner: Spinner<Int>
    private lateinit var bottomMarginSpinner: Spinner<Int>
    private lateinit var leftMarginSpinner: Spinner<Int>
    private lateinit var rightMarginSpinner: Spinner<Int>
    private lateinit var showGridLinesCheck: CheckBox
    private lateinit var showPageNumbersCheck: CheckBox
    
    override fun createOKButton(): Button {
        return Button("Tallenna").apply {
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
        }
    }
    
    override fun createContent(): Parent {
        val root = VBox(15.0).apply {
            padding = Insets(20.0)
            style = "-fx-background-color: #ffffff;"
        }
        
        // Paper settings
        val paperPane = TitledPane().apply {
            text = "Paperi"
            isCollapsible = false
        }
        
        val paperGrid = GridPane().apply {
            hgap = 15.0
            vgap = 10.0
            padding = Insets(10.0)
        }
        
        paperGrid.add(Label("Paperikoko:"), 0, 0)
        paperSizeCombo = ComboBox<String>().apply {
            items.addAll("A4", "A5", "Letter", "Legal")
            value = settings.getString("print.paperSize", "A4")
            prefWidth = 150.0
        }
        paperGrid.add(paperSizeCombo, 1, 0)
        
        paperGrid.add(Label("Suunta:"), 0, 1)
        orientationCombo = ComboBox<String>().apply {
            items.addAll("Pysty", "Vaaka")
            value = settings.getString("print.orientation", "Pysty")
            prefWidth = 150.0
        }
        paperGrid.add(orientationCombo, 1, 1)
        
        paperPane.content = paperGrid
        
        // Margins
        val marginsPane = TitledPane().apply {
            text = "Marginaalit (mm)"
            isCollapsible = false
        }
        
        val marginGrid = GridPane().apply {
            hgap = 15.0
            vgap = 10.0
            padding = Insets(10.0)
        }
        
        marginGrid.add(Label("Ylä:"), 0, 0)
        topMarginSpinner = Spinner<Int>().apply {
            valueFactory = javafx.scene.control.SpinnerValueFactory.IntegerSpinnerValueFactory(0, 50, settings.getInt("print.marginTop", 15))
            prefWidth = 80.0
        }
        marginGrid.add(topMarginSpinner, 1, 0)
        
        marginGrid.add(Label("Ala:"), 2, 0)
        bottomMarginSpinner = Spinner<Int>().apply {
            valueFactory = javafx.scene.control.SpinnerValueFactory.IntegerSpinnerValueFactory(0, 50, settings.getInt("print.marginBottom", 15))
            prefWidth = 80.0
        }
        marginGrid.add(bottomMarginSpinner, 3, 0)
        
        marginGrid.add(Label("Vasen:"), 0, 1)
        leftMarginSpinner = Spinner<Int>().apply {
            valueFactory = javafx.scene.control.SpinnerValueFactory.IntegerSpinnerValueFactory(0, 50, settings.getInt("print.marginLeft", 15))
            prefWidth = 80.0
        }
        marginGrid.add(leftMarginSpinner, 1, 1)
        
        marginGrid.add(Label("Oikea:"), 2, 1)
        rightMarginSpinner = Spinner<Int>().apply {
            valueFactory = javafx.scene.control.SpinnerValueFactory.IntegerSpinnerValueFactory(0, 50, settings.getInt("print.marginRight", 15))
            prefWidth = 80.0
        }
        marginGrid.add(rightMarginSpinner, 3, 1)
        
        marginsPane.content = marginGrid
        
        // Options
        val optionsPane = TitledPane().apply {
            text = "Lisäasetukset"
            isCollapsible = false
        }
        
        val optionsBox = VBox(8.0).apply {
            padding = Insets(10.0)
        }
        
        showGridLinesCheck = CheckBox("Näytä ruudukko").apply {
            isSelected = settings.getBoolean("print.showGridLines", true)
        }
        
        showPageNumbersCheck = CheckBox("Näytä sivunumerot").apply {
            isSelected = settings.getBoolean("print.showPageNumbers", true)
        }
        
        optionsBox.children.addAll(showGridLinesCheck, showPageNumbersCheck)
        optionsPane.content = optionsBox
        
        root.children.addAll(paperPane, marginsPane, optionsPane)
        
        return root
    }
    
    override fun onOK(): Boolean {
        save()
        return true
    }
    
    private fun save() {
        settings.set("print.paperSize", paperSizeCombo.value)
        settings.set("print.orientation", orientationCombo.value)
        settings.set("print.marginTop", topMarginSpinner.value)
        settings.set("print.marginBottom", bottomMarginSpinner.value)
        settings.set("print.marginLeft", leftMarginSpinner.value)
        settings.set("print.marginRight", rightMarginSpinner.value)
        settings.set("print.showGridLines", showGridLinesCheck.isSelected)
        settings.set("print.showPageNumbers", showPageNumbersCheck.isSelected)
        settings.save()
    }
}
