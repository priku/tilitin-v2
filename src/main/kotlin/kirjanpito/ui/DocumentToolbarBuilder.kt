package kirjanpito.ui

import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.event.ActionListener
import javax.swing.BorderFactory
import javax.swing.JButton
import javax.swing.JPanel
import javax.swing.JToolBar

/**
 * Rakentaa DocumentFrame:n työkalurivin.
 *
 * Eriytetty DocumentFrame:sta modulaarisuuden parantamiseksi.
 * Migrated from Java to Kotlin for better code conciseness and null-safety.
 *
 * @author Tilitin Team
 */
class DocumentToolbarBuilder {

    // Button references (need to be accessed by DocumentFrame)
    lateinit var prevButton: JButton
        private set
    lateinit var nextButton: JButton
        private set
    lateinit var newDocButton: JButton
        private set
    lateinit var addEntryButton: JButton
        private set
    lateinit var removeEntryButton: JButton
        private set
    lateinit var findByNumberButton: JButton
        private set
    lateinit var searchButton: JButton
        private set

    /**
     * Luo työkalurivin kaikilla painikkeilla.
     *
     * @param listeners Kuuntelijat painikkeiden toiminnoille
     * @return Valmis työkalurivi
     */
    fun build(listeners: ToolbarListeners): JToolBar = JToolBar().apply {
        isFloatable = false
        border = BorderFactory.createEmptyBorder(4, 8, 4, 8)

        // Navigointi-osio
        prevButton = SwingUtils.createToolButton(
            "go-previous-22x22.png",
            "Edellinen tosite (Page Up)",
            listeners.prevDocListener,
            false
        )

        nextButton = SwingUtils.createToolButton(
            "go-next-22x22.png",
            "Seuraava tosite (Page Down)",
            listeners.nextDocListener,
            false
        )

        add(prevButton)
        add(nextButton)
        addSeparator(Dimension(16, 0))

        // Tosite-osio
        newDocButton = SwingUtils.createToolButton(
            "document-new-22x22.png",
            "Uusi tosite (Ctrl+N)",
            listeners.newDocListener,
            true
        )

        add(newDocButton)
        addSeparator(Dimension(16, 0))

        // Vienti-osio
        addEntryButton = SwingUtils.createToolButton(
            "list-add-22x22.png",
            "Lisää vienti (F8)",
            listeners.addEntryListener,
            true
        )

        removeEntryButton = SwingUtils.createToolButton(
            "list-remove-22x22.png",
            "Poista vienti",
            listeners.removeEntryListener,
            true
        )

        add(addEntryButton)
        add(removeEntryButton)
        addSeparator(Dimension(16, 0))

        // Haku-osio
        findByNumberButton = SwingUtils.createToolButton(
            "jump-22x22.png",
            "Hae numerolla (Ctrl+G)",
            listeners.findDocumentByNumberListener,
            true
        )

        searchButton = SwingUtils.createToolButton(
            "find-22x22.png",
            "Etsi (Ctrl+F)",
            listeners.searchListener,
            true
        )

        add(findByNumberButton)
        add(searchButton)
    }

    /**
     * Lisää työkalurivin annettuun paneliin PAGE_START-sijaintiin.
     *
     * @param panel Paneeli johon työkalurivi lisätään
     * @param listeners Kuuntelijat painikkeiden toiminnoille
     */
    fun addToPanel(panel: JPanel, listeners: ToolbarListeners) {
        val toolBar = build(listeners)
        panel.add(toolBar, BorderLayout.PAGE_START)
    }

    /**
     * Sisältää kaikki työkalurivin tarvitsemat kuuntelijat.
     */
    data class ToolbarListeners(
        val prevDocListener: ActionListener,
        val nextDocListener: ActionListener,
        val newDocListener: ActionListener,
        val addEntryListener: ActionListener,
        val removeEntryListener: ActionListener,
        val findDocumentByNumberListener: ActionListener,
        val searchListener: ActionListener
    )
}
