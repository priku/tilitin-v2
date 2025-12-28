package kirjanpito.ui

import java.awt.Component
import java.awt.Container
import java.awt.GridBagConstraints
import java.awt.Insets
import javax.swing.*

/**
 * Kotlin extension functions for Swing components
 * Modernizes Swing UI code with idiomatic Kotlin
 */

/**
 * Extension function to simplify GridBagConstraints creation
 */
fun gridBagConstraints(
    gridx: Int = GridBagConstraints.RELATIVE,
    gridy: Int = GridBagConstraints.RELATIVE,
    gridwidth: Int = 1,
    gridheight: Int = 1,
    weightx: Double = 0.0,
    weighty: Double = 0.0,
    anchor: Int = GridBagConstraints.CENTER,
    fill: Int = GridBagConstraints.NONE,
    insets: Insets = Insets(0, 0, 0, 0),
    ipadx: Int = 0,
    ipady: Int = 0
): GridBagConstraints {
    return GridBagConstraints().apply {
        this.gridx = gridx
        this.gridy = gridy
        this.gridwidth = gridwidth
        this.gridheight = gridheight
        this.weightx = weightx
        this.weighty = weighty
        this.anchor = anchor
        this.fill = fill
        this.insets = insets
        this.ipadx = ipadx
        this.ipady = ipady
    }
}

/**
 * Extension function to add component with GridBagConstraints in one call
 */
fun Container.addWithConstraints(
    component: Component,
    constraints: GridBagConstraints
) {
    add(component, constraints)
}

/**
 * Extension function to create standard insets
 */
fun insets(
    top: Int = 0,
    left: Int = 0,
    bottom: Int = 0,
    right: Int = 0
): Insets = Insets(top, left, bottom, right)

/**
 * Extension function to create uniform insets
 */
fun insets(all: Int): Insets = Insets(all, all, all, all)

/**
 * Extension property for easier null-safe text access
 */
var JTextField.textOrEmpty: String
    get() = text ?: ""
    set(value) { text = value }

/**
 * Extension function to show error dialog
 */
fun Component.showError(message: String, title: String = "Virhe") {
    JOptionPane.showMessageDialog(
        this,
        message,
        title,
        JOptionPane.ERROR_MESSAGE
    )
}

/**
 * Extension function to show info dialog
 */
fun Component.showInfo(message: String, title: String = "Tiedote") {
    JOptionPane.showMessageDialog(
        this,
        message,
        title,
        JOptionPane.INFORMATION_MESSAGE
    )
}

/**
 * Extension function to show confirmation dialog
 */
fun Component.showConfirmation(
    message: String,
    title: String = "Vahvistus"
): Boolean {
    return JOptionPane.showConfirmDialog(
        this,
        message,
        title,
        JOptionPane.YES_NO_OPTION
    ) == JOptionPane.YES_OPTION
}

/**
 * Extension function to enable/disable component and all children
 */
fun Component.setEnabledRecursive(enabled: Boolean) {
    isEnabled = enabled
    if (this is Container) {
        components.forEach { it.setEnabledRecursive(enabled) }
    }
}

/**
 * Type-safe builder for JPanel
 */
inline fun panel(init: JPanel.() -> Unit): JPanel {
    return JPanel().apply(init)
}

/**
 * Type-safe builder for JButton with Runnable (Java-compatible)
 */
fun button(text: String, action: Runnable): JButton {
    return JButton(text).apply {
        addActionListener { action.run() }
    }
}

/**
 * Extension to make KeyStroke creation more readable
 */
fun keyStroke(keyCode: Int, modifiers: Int = 0): KeyStroke {
    return KeyStroke.getKeyStroke(keyCode, modifiers)
}
