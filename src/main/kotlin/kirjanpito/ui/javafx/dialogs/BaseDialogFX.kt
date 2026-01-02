package kirjanpito.ui.javafx.dialogs

import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.Region
import javafx.scene.layout.VBox
import javafx.stage.Modality
import javafx.stage.Stage
import javafx.stage.Window

/**
 * Base class for JavaFX dialogs.
 * Provides common functionality and consistent styling.
 * 
 * Usage:
 * ```kotlin
 * class MyDialog(owner: Window?) : BaseDialogFX(owner, "My Dialog", 500.0, 400.0) {
 *     override fun createContent(): Parent {
 *         return VBox(10.0).apply {
 *             padding = Insets(16.0)
 *             children.add(Label("Hello"))
 *         }
 *     }
 *     
 *     override fun onOK(): Boolean {
 *         // Validate and return true if OK, false to keep dialog open
 *         return true
 *     }
 * }
 * ```
 */
abstract class BaseDialogFX(
    owner: Window?,
    title: String,
    width: Double = 500.0,
    height: Double = 400.0,
    resizable: Boolean = true
) {
    
    protected val dialog: Stage
    
    init {
        dialog = Stage().apply {
            initModality(Modality.APPLICATION_MODAL)
            owner?.let { initOwner(it) }
            this.title = title
            this.isResizable = resizable
            minWidth = width
            minHeight = height
        }
        
        val content = createContent()
        val root = if (hasButtons()) {
            createDialogWithButtons(content)
        } else {
            content
        }
        
        val scene = Scene(root, width, height)
        dialog.scene = scene
    }
    
    /**
     * Creates the main content of the dialog.
     * Must be implemented by subclasses.
     */
    abstract fun createContent(): Parent
    
    /**
     * Called when OK button is pressed.
     * Return true to close dialog, false to keep it open.
     * Default implementation always returns true.
     */
    open fun onOK(): Boolean = true
    
    /**
     * Called when Cancel button is pressed.
     * Default implementation closes the dialog.
     */
    open fun onCancel() {
        dialog.close()
    }
    
    /**
     * Whether this dialog should have OK/Cancel buttons.
     * Override to return false for dialogs without buttons.
     */
    open fun hasButtons(): Boolean = true
    
    /**
     * Creates OK button with default styling.
     */
    protected open fun createOKButton(): Button {
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
    
    /**
     * Creates Cancel button with default styling.
     */
    protected open fun createCancelButton(): Button {
        return Button("Peruuta").apply {
            isCancelButton = true
            setOnAction { onCancel() }
        }
    }
    
    /**
     * Creates button bar with OK and Cancel buttons.
     */
    private fun createButtonBar(): HBox {
        val buttonBox = HBox(10.0).apply {
            alignment = Pos.CENTER_RIGHT
            padding = Insets(10.0, 0.0, 0.0, 0.0)
        }
        
        buttonBox.children.addAll(createCancelButton(), createOKButton())
        return buttonBox
    }
    
    /**
     * Wraps content with button bar.
     */
    private fun createDialogWithButtons(content: Parent): VBox {
        val root = VBox(10.0).apply {
            padding = Insets(16.0)
            style = "-fx-background-color: #ffffff;"
        }
        
        val spacer = Region()
        VBox.setVgrow(spacer, Priority.ALWAYS)
        VBox.setVgrow(content, Priority.ALWAYS)
        
        root.children.addAll(content, spacer, createButtonBar())
        return root
    }
    
    /**
     * Shows the dialog and waits for it to close.
     */
    fun showAndWait() {
        dialog.showAndWait()
    }
    
    /**
     * Shows the dialog without waiting.
     */
    open fun show() {
        dialog.show()
    }
    
    /**
     * Closes the dialog.
     */
    fun close() {
        dialog.close()
    }
}
