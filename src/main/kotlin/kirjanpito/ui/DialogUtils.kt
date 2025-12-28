package kirjanpito.ui

import java.awt.Component
import java.awt.Window
import javax.swing.JDialog
import javax.swing.JFileChooser
import javax.swing.SwingUtilities
import javax.swing.filechooser.FileFilter
import java.io.File

/**
 * Dialog utility functions
 * Simplifies common dialog operations with Kotlin idioms
 */
object DialogUtils {

    /**
     * Centers dialog on parent window
     */
    fun JDialog.centerOnParent() {
        setLocationRelativeTo(owner)
    }

    /**
     * Centers dialog on screen
     */
    fun JDialog.centerOnScreen() {
        setLocationRelativeTo(null)
    }

    /**
     * Shows file chooser dialog and returns selected file
     */
    fun Component.showFileChooser(
        title: String? = null,
        currentDirectory: File? = null,
        fileFilter: FileFilter? = null,
        approveButtonText: String? = null
    ): File? {
        val chooser = JFileChooser().apply {
            title?.let { dialogTitle = it }
            currentDirectory?.let { this.currentDirectory = it }
            fileFilter?.let {
                this.fileFilter = it
                isAcceptAllFileFilterUsed = false
            }
            approveButtonText?.let { this.approveButtonText = it }
        }

        return if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            chooser.selectedFile
        } else {
            null
        }
    }

    /**
     * Shows save file chooser dialog and returns selected file
     */
    fun Component.showSaveFileChooser(
        title: String? = null,
        currentDirectory: File? = null,
        fileFilter: FileFilter? = null,
        suggestedFileName: String? = null
    ): File? {
        val chooser = JFileChooser().apply {
            title?.let { dialogTitle = it }
            currentDirectory?.let { this.currentDirectory = it }
            fileFilter?.let {
                this.fileFilter = it
                isAcceptAllFileFilterUsed = false
            }
            suggestedFileName?.let { selectedFile = File(it) }
        }

        return if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            chooser.selectedFile
        } else {
            null
        }
    }

    /**
     * Gets parent window of component
     */
    fun Component.getParentWindow(): Window? {
        return SwingUtilities.getWindowAncestor(this)
    }

    /**
     * Executes action on EDT (Event Dispatch Thread)
     */
    inline fun onEDT(crossinline action: () -> Unit) {
        if (SwingUtilities.isEventDispatchThread()) {
            action()
        } else {
            SwingUtilities.invokeLater { action() }
        }
    }

    /**
     * Executes action on EDT and waits for completion
     */
    inline fun onEDTAndWait(crossinline action: () -> Unit) {
        if (SwingUtilities.isEventDispatchThread()) {
            action()
        } else {
            SwingUtilities.invokeAndWait { action() }
        }
    }
}

/**
 * Extension function to run code on EDT
 */
inline fun runOnEDT(crossinline action: () -> Unit) {
    DialogUtils.onEDT(action)
}

/**
 * Extension function to run code on EDT and wait
 */
inline fun runOnEDTAndWait(crossinline action: () -> Unit) {
    DialogUtils.onEDTAndWait(action)
}

/**
 * Creates a simple file filter
 */
fun fileFilter(description: String, vararg extensions: String): FileFilter {
    return object : FileFilter() {
        override fun accept(f: File): Boolean {
            if (f.isDirectory) return true
            val ext = f.extension.lowercase()
            return extensions.any { it.lowercase() == ext }
        }

        override fun getDescription(): String = description
    }
}
