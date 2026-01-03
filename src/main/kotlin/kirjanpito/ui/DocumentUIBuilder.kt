package kirjanpito.ui

import kirjanpito.models.TextFieldWithLockIcon
import kirjanpito.ui.resources.Resources
import java.awt.BorderLayout
import java.awt.Cursor
import java.awt.Dimension
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.GridLayout
import java.awt.Insets
import java.awt.event.ActionListener
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import javax.swing.*
import javax.swing.border.BevelBorder
import javax.swing.border.EtchedBorder
import javax.swing.text.DefaultCaret

/**
 * Hallinnoi DocumentFrame:n UI-komponenttien luomista.
 *
 * Tämä luokka eriytetään DocumentFrame:stä Phase 7 -refaktoroinnin osana.
 * Se vastaa kaikkien UI-komponenttien luomisesta ja konfiguroinnista.
 * Migrated from Java to Kotlin for better code conciseness and null-safety.
 *
 * @since 2.2.4
 */
class DocumentUIBuilder(private val callbacks: UICallbacks) {

    /**
     * Callback-rajapinta DocumentFrame:lle.
     */
    interface UICallbacks {
        /** Kutsutaan kun tositenumero-kenttä muuttuu */
        fun onNumberFieldChanged()

        /** Kutsutaan kun päivämäärä-kenttä muuttuu */
        fun onDateFieldChanged()

        /** Kutsutaan kun haku-painiketta klikataan */
        fun onSearchButtonClicked()

        /** Kutsutaan kun haku-paneeli suljetaan */
        fun onSearchPanelClosed()

        /** Palauttaa addEntry-actionin */
        fun getAddEntryAction(): Action

        /** Palauttaa searchListener-actionin */
        fun getSearchListener(): ActionListener
    }

    /**
     * Komponenttien viitteet.
     */
    class UIComponents {
        @JvmField var numberTextField: TextFieldWithLockIcon? = null
        @JvmField var dateTextField: DateTextField? = null
        @JvmField var debitTotalLabel: JLabel? = null
        @JvmField var creditTotalLabel: JLabel? = null
        @JvmField var differenceLabel: JLabel? = null
        @JvmField var searchPanel: JPanel? = null
        @JvmField var searchPhraseTextField: JTextField? = null
        @JvmField var documentLabel: JLabel? = null
        @JvmField var periodLabel: JLabel? = null
        @JvmField var documentTypeLabel: JLabel? = null
        @JvmField var backupStatusLabel: JLabel? = null
    }

    val components = UIComponents()

    /**
     * Luo tekstikenttäpaneelin (tositenumero ja päivämäärä).
     *
     * @param container Paneeli, johon komponentit lisätään
     */
    fun createTextFieldPanel(container: JPanel) {
        val panel = JPanel().apply {
            layout = GridLayout(0, 2)
        }
        container.add(panel, BorderLayout.PAGE_START)

        // Left panel - Tositenumero
        val left = JPanel(GridBagLayout()).apply {
            // Tositenumero label
            add(JLabel("Tositenumero"), GridBagConstraints().apply {
                anchor = GridBagConstraints.WEST
                insets = Insets(8, 8, 8, 4)
            })

            // Tositenumero field
            this@DocumentUIBuilder.components.numberTextField = TextFieldWithLockIcon().apply {
                caret = DefaultCaret()
                addKeyListener(object : KeyAdapter() {
                    override fun keyReleased(e: KeyEvent) {
                        if (isEditable) {
                            callbacks.onNumberFieldChanged()
                        }
                    }
                })

                inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "transferFocus")
                actionMap.put("transferFocus", object : AbstractAction() {
                    override fun actionPerformed(e: java.awt.event.ActionEvent) {
                        transferFocus()
                    }
                })
            }

            add(this@DocumentUIBuilder.components.numberTextField!!, GridBagConstraints().apply {
                insets = Insets(8, 4, 8, 4)
                fill = GridBagConstraints.HORIZONTAL
                weightx = 1.0
            })

            // Separator
            add(JSeparator(SwingConstants.VERTICAL), GridBagConstraints().apply {
                fill = GridBagConstraints.VERTICAL
                weightx = 0.0
            })
        }
        panel.add(left)

        // Right panel - Päivämäärä
        val right = JPanel(GridBagLayout()).apply {
            // Päivämäärä label
            add(JLabel("Päivämäärä"), GridBagConstraints().apply {
                anchor = GridBagConstraints.WEST
                insets = Insets(8, 8, 8, 4)
            })

            // Päivämäärä field
            this@DocumentUIBuilder.components.dateTextField = DateTextField().apply {
                caret = DefaultCaret()
                addKeyListener(object : KeyAdapter() {
                    override fun keyReleased(e: KeyEvent) {
                        if (isEditable && e.keyChar.isDigit()) {
                            callbacks.onDateFieldChanged()
                        }
                    }
                })

                inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "addEntry")
                actionMap.put("addEntry", callbacks.getAddEntryAction())

                inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "firstEntry")
                actionMap.put("firstEntry", object : AbstractAction() {
                    override fun actionPerformed(e: java.awt.event.ActionEvent) {
                        transferFocus()
                        // Note: First entry selection logic will be handled by DocumentFrame
                    }
                })
            }

            add(this@DocumentUIBuilder.components.dateTextField!!, GridBagConstraints().apply {
                insets = Insets(8, 4, 8, 8)
                fill = GridBagConstraints.HORIZONTAL
                weightx = 1.0
            })
        }
        panel.add(right)
    }

    /**
     * Luo summarivin (debet/kredit yhteenvedot).
     *
     * @param container Paneeli, johon rivi lisätään
     */
    fun createTotalRow(container: JPanel) {
        val panel = JPanel(GridBagLayout()).apply {
            border = BorderFactory.createEmptyBorder(2, 2, 5, 2)
        }
        container.add(panel)

        val constraints = GridBagConstraints().apply {
            insets = Insets(2, 2, 2, 6)
            anchor = GridBagConstraints.WEST
        }

        components.debitTotalLabel = JLabel("0,00").apply {
            preferredSize = Dimension(80, minimumSize.height)
        }
        components.creditTotalLabel = JLabel("0,00").apply {
            preferredSize = Dimension(80, minimumSize.height)
        }
        components.differenceLabel = JLabel("0,00").apply {
            preferredSize = Dimension(80, minimumSize.height)
        }

        panel.add(JLabel("Debet yht."), constraints)
        panel.add(components.debitTotalLabel, constraints)
        panel.add(JLabel("Kredit yht."), constraints)
        panel.add(components.creditTotalLabel, constraints)
        panel.add(JLabel("Erotus"), constraints)
        panel.add(components.differenceLabel, constraints.apply { weightx = 1.0 })
    }

    /**
     * Luo hakupalkin.
     *
     * @param container Paneeli, johon hakupalkki lisätään
     */
    fun createSearchBar(container: JPanel) {
        val panel = JPanel(GridBagLayout()).apply {
            border = BorderFactory.createBevelBorder(BevelBorder.LOWERED)
            isVisible = false
        }
        components.searchPanel = panel
        container.add(panel)

        val constraints = GridBagConstraints().apply {
            insets = Insets(8, 5, 8, 5)
            anchor = GridBagConstraints.WEST
        }

        // Search text field
        this@DocumentUIBuilder.components.searchPhraseTextField = JTextField().apply {
            caret = DefaultCaret()
            addKeyListener(object : KeyAdapter() {
                override fun keyPressed(e: KeyEvent) {
                    if (e.keyCode == KeyEvent.VK_ENTER) {
                        callbacks.onSearchButtonClicked()
                        e.consume()
                    }
                }
            })
        }

        panel.add(components.searchPhraseTextField, constraints.apply {
            weightx = 1.0
            fill = GridBagConstraints.HORIZONTAL
        })

        // Search button
        val searchButton = JButton("Etsi", ImageIcon(Resources.loadAsImage("find-16x16.png"))).apply {
            addActionListener { callbacks.onSearchButtonClicked() }
            mnemonic = 'H'.code
        }

        panel.add(searchButton, constraints.apply {
            weightx = 0.0
            fill = GridBagConstraints.BOTH
        })

        // Close button
        val closeButton = JButton(ImageIcon(Resources.loadAsImage("close-16x16.png"))).apply {
            addActionListener(callbacks.getSearchListener())
        }

        panel.add(closeButton, constraints)
    }

    /**
     * Luo tilarivin (status bar).
     *
     * @param frame JFrame, johon status bar lisätään
     */
    fun createStatusBar(frame: JFrame) {
        val statusBarPanel = JPanel(BorderLayout())

        this@DocumentUIBuilder.components.documentLabel = JLabel().apply {
            border = EtchedBorder()
            preferredSize = Dimension(150, 0)
        }

        components.periodLabel = JLabel(" ").apply {
            border = EtchedBorder()
        }

        this@DocumentUIBuilder.components.documentTypeLabel = JLabel().apply {
            border = EtchedBorder()
            preferredSize = Dimension(200, 0)
        }

        // Backup-indikaattori (Word AutoSave -tyylinen)
        this@DocumentUIBuilder.components.backupStatusLabel = JLabel().apply {
            border = EtchedBorder()
            preferredSize = Dimension(120, 0)
            horizontalAlignment = SwingConstants.CENTER
            cursor = Cursor(Cursor.HAND_CURSOR)
            toolTipText = "Klikkaa muuttaaksesi varmuuskopiointiasetuksia"

            addMouseListener(object : java.awt.event.MouseAdapter() {
                override fun mouseClicked(e: java.awt.event.MouseEvent) {
                    // Callback will be set by DocumentFrame
                }
            })
        }

        val rightPanel = JPanel(BorderLayout()).apply {
            add(this@DocumentUIBuilder.components.backupStatusLabel!!, BorderLayout.WEST)
            add(this@DocumentUIBuilder.components.documentTypeLabel!!, BorderLayout.CENTER)
        }

        statusBarPanel.apply {
            add(this@DocumentUIBuilder.components.documentLabel!!, BorderLayout.WEST)
            add(this@DocumentUIBuilder.components.periodLabel!!, BorderLayout.CENTER)
            add(rightPanel, BorderLayout.EAST)
        }

        frame.add(statusBarPanel, BorderLayout.PAGE_END)
    }
}
