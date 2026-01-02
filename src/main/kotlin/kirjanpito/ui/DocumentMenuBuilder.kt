package kirjanpito.ui

import java.awt.event.ActionListener
import java.awt.event.InputEvent
import java.awt.event.KeyEvent
import javax.swing.Action
import javax.swing.JCheckBoxMenuItem
import javax.swing.JMenu
import javax.swing.JMenuBar
import javax.swing.JMenuItem
import javax.swing.KeyStroke

/**
 * Rakentaa DocumentFrame:n valikkorivin.
 * 
 * Eriytetty DocumentFrame:sta modulaarisuuden parantamiseksi.
 * Migrated from Java to Kotlin for better code conciseness and null-safety.
 * 
 * @author Tilitin Team
 */
class DocumentMenuBuilder {
    
    private val shortcutKeyMask: Int
    
    // Menu references (need to be accessed by DocumentFrame)
    private lateinit var entryTemplateMenu: JMenu
    private lateinit var docTypeMenu: JMenu
    private lateinit var gotoMenu: JMenu
    private lateinit var reportsMenu: JMenu
    private lateinit var toolsMenu: JMenu
    private lateinit var recentMenu: JMenu
    
    // MenuItem references
    private lateinit var newDatabaseMenuItem: JMenuItem
    private lateinit var openDatabaseMenuItem: JMenuItem
    private lateinit var newDocMenuItem: JMenuItem
    private lateinit var deleteDocMenuItem: JMenuItem
    private lateinit var addEntryMenuItem: JMenuItem
    private lateinit var removeEntryMenuItem: JMenuItem
    private lateinit var pasteMenuItem: JMenuItem
    private lateinit var coaMenuItem: JMenuItem
    private lateinit var vatDocumentMenuItem: JMenuItem
    private lateinit var editEntryTemplatesMenuItem: JMenuItem
    private lateinit var createEntryTemplateMenuItem: JMenuItem
    private lateinit var startingBalancesMenuItem: JMenuItem
    private lateinit var propertiesMenuItem: JMenuItem
    private lateinit var settingsMenuItem: JMenuItem
    private lateinit var searchMenuItem: JCheckBoxMenuItem
    private lateinit var editDocTypesMenuItem: JMenuItem
    private lateinit var setIgnoreFlagMenuItem: JMenuItem
    
    init {
        // Use modern API instead of deprecated getMenuShortcutKeyMask()
        // Detect OS and use appropriate mask (Ctrl on Windows/Linux, Cmd on Mac)
        val osName = System.getProperty("os.name").lowercase()
        shortcutKeyMask = if (osName.contains("mac")) {
            InputEvent.META_DOWN_MASK
        } else {
            InputEvent.CTRL_DOWN_MASK
        }
    }
    
    /**
     * Luo valikkorivin kaikilla valikoilla.
     * 
     * @param listeners Kuuntelijat valikkojen toiminnoille
     * @return Valmis valikkorivi
     */
    fun build(listeners: MenuListeners): JMenuBar {
        val menuBar = JMenuBar()
        
        menuBar.add(createFileMenu(listeners))
        menuBar.add(createEditMenu(listeners))
        menuBar.add(createGotoMenu(listeners))
        menuBar.add(createDocTypeMenu(listeners))
        menuBar.add(createReportsMenu(listeners))
        menuBar.add(createToolsMenu(listeners))
        menuBar.add(createHelpMenu(listeners))
        
        return menuBar
    }
    
    /**
     * Luo Tiedosto-valikon.
     */
    private fun createFileMenu(l: MenuListeners): JMenu {
        val menu = JMenu("Tiedosto").apply {
            mnemonic = 'T'.code
        }
        
        newDatabaseMenuItem = SwingUtils.createMenuItem(
            "Uusi…", null, 'U',
            KeyStroke.getKeyStroke('U', shortcutKeyMask),
            l.newDatabaseListener
        )
        menu.add(newDatabaseMenuItem)
        
        openDatabaseMenuItem = SwingUtils.createMenuItem(
            "Avaa…", null, 'A',
            KeyStroke.getKeyStroke('O', shortcutKeyMask),
            l.openDatabaseListener
        )
        menu.add(openDatabaseMenuItem)
        
        // Viimeisimmät tietokannat -alivalikko
        recentMenu = JMenu("Viimeisimmät").apply {
            mnemonic = 'V'.code
        }
        menu.add(recentMenu)
        
        menu.add(SwingUtils.createMenuItem(
            "Tietokanta-asetukset…", null, 'T',
            KeyStroke.getKeyStroke('D', shortcutKeyMask),
            l.databaseSettingsListener
        ))
        menu.add(SwingUtils.createMenuItem(
            "Varmuuskopiointi…", null, 'V',
            null, l.backupSettingsListener
        ))
        menu.add(SwingUtils.createMenuItem(
            "Palauta varmuuskopiosta…", null, 'P',
            null, l.restoreBackupListener
        ))
        
        menu.addSeparator()
        menu.add(SwingUtils.createMenuItem(
            "Lopeta", "quit-16x16.png", 'L',
            KeyStroke.getKeyStroke('Q', shortcutKeyMask),
            l.quitListener
        ))
        
        return menu
    }
    
    /**
     * Luo Muokkaa-valikon.
     */
    private fun createEditMenu(l: MenuListeners): JMenu {
        val menu = JMenu("Muokkaa").apply {
            mnemonic = 'M'.code
        }
        
        menu.add(SwingUtils.createMenuItem(
            "Kopioi", null, 'K',
            KeyStroke.getKeyStroke(KeyEvent.VK_C, shortcutKeyMask),
            l.copyEntriesAction
        ))
        
        pasteMenuItem = SwingUtils.createMenuItem(
            "Liitä", null, 'L',
            KeyStroke.getKeyStroke(KeyEvent.VK_V, shortcutKeyMask),
            l.pasteEntriesAction
        )
        menu.add(pasteMenuItem)
        menu.addSeparator()
        
        newDocMenuItem = SwingUtils.createMenuItem(
            "Uusi tosite", "document-new-16x16.png", 'U',
            KeyStroke.getKeyStroke('N', shortcutKeyMask),
            l.newDocListener
        )
        
        deleteDocMenuItem = SwingUtils.createMenuItem(
            "Poista tosite", "delete-16x16.png", 'P',
            KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, shortcutKeyMask),
            l.deleteDocListener
        )
        
        menu.add(newDocMenuItem)
        menu.add(deleteDocMenuItem)
        menu.addSeparator()
        
        addEntryMenuItem = SwingUtils.createMenuItem(
            "Lisää vienti", "list-add-16x16.png", 'L',
            KeyStroke.getKeyStroke(KeyEvent.VK_F8, 0),
            l.addEntryListener
        )
        
        removeEntryMenuItem = SwingUtils.createMenuItem(
            "Poista vienti", "list-remove-16x16.png", 'o',
            KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, InputEvent.SHIFT_DOWN_MASK),
            l.removeEntryListener
        )
        
        entryTemplateMenu = JMenu("Vientimallit")
        editEntryTemplatesMenuItem = SwingUtils.createMenuItem(
            "Muokkaa", null, 'M',
            KeyStroke.getKeyStroke(KeyEvent.VK_M, shortcutKeyMask),
            l.editEntryTemplatesListener
        )
        
        createEntryTemplateMenuItem = SwingUtils.createMenuItem(
            "Luo tositteesta", null, 'K',
            KeyStroke.getKeyStroke(KeyEvent.VK_K, shortcutKeyMask),
            l.createEntryTemplateListener
        )
        
        menu.add(addEntryMenuItem)
        menu.add(removeEntryMenuItem)
        menu.add(entryTemplateMenu)
        
        menu.addSeparator()
        
        coaMenuItem = SwingUtils.createMenuItem(
            "Tilikartta…", null, 'T',
            KeyStroke.getKeyStroke(KeyEvent.VK_T, shortcutKeyMask),
            l.chartOfAccountsListener
        )
        
        startingBalancesMenuItem = SwingUtils.createMenuItem(
            "Alkusaldot…", null, 's',
            KeyStroke.getKeyStroke('B', shortcutKeyMask),
            l.startingBalancesListener
        )
        
        propertiesMenuItem = SwingUtils.createMenuItem(
            "Perustiedot…", null, 'e',
            KeyStroke.getKeyStroke('P', shortcutKeyMask or InputEvent.SHIFT_DOWN_MASK),
            l.propertiesListener
        )
        
        settingsMenuItem = SwingUtils.createMenuItem(
            "Kirjausasetukset…", null, 'K',
            KeyStroke.getKeyStroke('S', shortcutKeyMask or InputEvent.SHIFT_DOWN_MASK),
            l.settingsListener
        )
        
        val appearanceMenuItem = SwingUtils.createMenuItem(
            "Ulkoasu…", null, 'U',
            KeyStroke.getKeyStroke('A', shortcutKeyMask or InputEvent.SHIFT_DOWN_MASK),
            l.appearanceListener
        )
        
        menu.add(coaMenuItem)
        menu.add(startingBalancesMenuItem)
        menu.add(propertiesMenuItem)
        menu.add(settingsMenuItem)
        menu.add(appearanceMenuItem)
        
        return menu
    }
    
    /**
     * Luo Siirry-valikon.
     */
    private fun createGotoMenu(l: MenuListeners): JMenu {
        gotoMenu = JMenu("Siirry").apply {
            mnemonic = 'S'.code
        }
        val menu = gotoMenu
        
        menu.add(SwingUtils.createMenuItem(
            "Edellinen", "go-previous-16x16.png", 'E',
            KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_UP, 0),
            l.prevDocListener
        ))
        
        menu.add(SwingUtils.createMenuItem(
            "Seuraava", "go-next-16x16.png", 'S',
            KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN, 0),
            l.nextDocListener
        ))
        
        menu.addSeparator()
        
        menu.add(SwingUtils.createMenuItem(
            "Ensimmäinen", "go-first-16x16.png", 'n',
            KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_UP, shortcutKeyMask),
            l.firstDocListener
        ))
        
        menu.add(SwingUtils.createMenuItem(
            "Viimeinen", "go-last-16x16.png", 'V',
            KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN, shortcutKeyMask),
            l.lastDocListener
        ))
        
        menu.addSeparator()
        
        menu.add(SwingUtils.createMenuItem(
            "Hae numerolla…", null, 'n',
            KeyStroke.getKeyStroke(KeyEvent.VK_G, shortcutKeyMask),
            l.findDocumentByNumberListener
        ))
        
        searchMenuItem = JCheckBoxMenuItem("Etsi…").apply {
            accelerator = KeyStroke.getKeyStroke(KeyEvent.VK_F, shortcutKeyMask)
            addActionListener(l.searchListener)
        }
        menu.add(searchMenuItem)
        
        return menu
    }
    
    /**
     * Luo Tositelaji-valikon.
     */
    private fun createDocTypeMenu(l: MenuListeners): JMenu {
        docTypeMenu = JMenu("Tositelaji").apply {
            mnemonic = 'l'.code
        }
        val menu = docTypeMenu
        
        editDocTypesMenuItem = SwingUtils.createMenuItem(
            "Muokkaa", null, 'M',
            KeyStroke.getKeyStroke(KeyEvent.VK_L, shortcutKeyMask),
            l.editDocTypesListener
        )
        menu.add(editDocTypesMenuItem)
        
        return menu
    }
    
    /**
     * Luo Tulosteet-valikon.
     */
    private fun createReportsMenu(l: MenuListeners): JMenu {
        reportsMenu = JMenu("Tulosteet").apply {
            mnemonic = 'u'.code
        }
        val menu = reportsMenu
        
        var menuItem: JMenuItem
        
        menuItem = SwingUtils.createMenuItem(
            "Tilien saldot", null, 's',
            KeyStroke.getKeyStroke(KeyEvent.VK_1, shortcutKeyMask),
            l.printListener
        ).apply {
            actionCommand = "accountSummary"
        }
        menu.add(menuItem)
        
        menuItem = SwingUtils.createMenuItem(
            "Tosite", null, 'O',
            KeyStroke.getKeyStroke(KeyEvent.VK_2, shortcutKeyMask),
            l.printListener
        ).apply {
            actionCommand = "document"
        }
        menu.add(menuItem)
        
        menuItem = SwingUtils.createMenuItem(
            "Tiliote", null, 'T',
            KeyStroke.getKeyStroke(KeyEvent.VK_3, shortcutKeyMask),
            l.printListener
        ).apply {
            actionCommand = "accountStatement"
        }
        menu.add(menuItem)
        
        menuItem = SwingUtils.createMenuItem(
            "Tuloslaskelma", null, 'u',
            KeyStroke.getKeyStroke(KeyEvent.VK_4, shortcutKeyMask),
            l.printListener
        ).apply {
            actionCommand = "incomeStatement"
        }
        menu.add(menuItem)
        
        menuItem = SwingUtils.createMenuItem(
            "Tuloslaskelma erittelyin", null, 'e',
            KeyStroke.getKeyStroke(KeyEvent.VK_5, shortcutKeyMask),
            l.printListener
        ).apply {
            actionCommand = "incomeStatementDetailed"
        }
        menu.add(menuItem)
        
        menuItem = SwingUtils.createMenuItem(
            "Tase", null, 'a',
            KeyStroke.getKeyStroke(KeyEvent.VK_6, shortcutKeyMask),
            l.printListener
        ).apply {
            actionCommand = "balanceSheet"
        }
        menu.add(menuItem)
        
        menuItem = SwingUtils.createMenuItem(
            "Tase erittelyin", null, 'e',
            KeyStroke.getKeyStroke(KeyEvent.VK_7, shortcutKeyMask),
            l.printListener
        ).apply {
            actionCommand = "balanceSheetDetailed"
        }
        menu.add(menuItem)
        
        menuItem = SwingUtils.createMenuItem(
            "Päiväkirja", null, 'P',
            KeyStroke.getKeyStroke(KeyEvent.VK_8, shortcutKeyMask),
            l.printListener
        ).apply {
            actionCommand = "generalJournal"
        }
        menu.add(menuItem)
        
        menuItem = SwingUtils.createMenuItem(
            "Pääkirja", null, 'k',
            KeyStroke.getKeyStroke(KeyEvent.VK_9, shortcutKeyMask),
            l.printListener
        ).apply {
            actionCommand = "generalLedger"
        }
        menu.add(menuItem)
        
        menuItem = SwingUtils.createMenuItem(
            "ALV-laskelma tileittäin", null, 'V',
            KeyStroke.getKeyStroke(KeyEvent.VK_0, shortcutKeyMask),
            l.printListener
        ).apply {
            actionCommand = "vatReport"
        }
        menu.add(menuItem)
        
        val submenu = JMenu("Tilikartta").apply {
            mnemonic = 'r'.code
        }
        menu.add(submenu)
        
        menuItem = SwingUtils.createMenuItem(
            "Vain käytössä olevat tilit", null, 'V',
            null, l.printListener
        ).apply {
            actionCommand = "coa1"
        }
        submenu.add(menuItem)
        
        menuItem = SwingUtils.createMenuItem(
            "Vain suosikkitilit", null, 's',
            null, l.printListener
        ).apply {
            actionCommand = "coa2"
        }
        submenu.add(menuItem)
        
        menuItem = SwingUtils.createMenuItem(
            "Kaikki tilit", null, 'k',
            null, l.printListener
        ).apply {
            actionCommand = "coa0"
        }
        submenu.add(menuItem)
        
        menu.addSeparator()
        menu.add(SwingUtils.createMenuItem(
            "Muokkaa", null, 'M',
            null, l.editReportsListener
        ))
        
        return menu
    }
    
    /**
     * Luo Työkalut-valikon.
     */
    private fun createToolsMenu(l: MenuListeners): JMenu {
        toolsMenu = JMenu("Työkalut").apply {
            mnemonic = 'y'.code
        }
        val menu = toolsMenu
        
        vatDocumentMenuItem = SwingUtils.createMenuItem(
            "ALV-tilien päättäminen", null, 'p',
            KeyStroke.getKeyStroke('V', shortcutKeyMask or InputEvent.SHIFT_DOWN_MASK),
            l.vatDocumentListener
        )
        menu.add(vatDocumentMenuItem)
        
        setIgnoreFlagMenuItem = SwingUtils.createMenuItem(
            "Ohita vienti ALV-laskelmassa", null, 'O',
            null, l.setIgnoreFlagToEntryAction
        )
        menu.add(setIgnoreFlagMenuItem)
        
        menu.add(SwingUtils.createMenuItem(
            "Tilien saldojen vertailu", null, 'T',
            null, l.balanceComparisonListener
        ))
        
        menu.add(SwingUtils.createMenuItem(
            "Muuta tositenumeroita", null, 'n',
            null, l.numberShiftListener
        ))
        
        menu.add(SwingUtils.createMenuItem(
            "ALV-kantojen muutokset", null, 'm',
            null, l.vatChangeListener
        ))
        
        menu.add(SwingUtils.createMenuItem(
            "Vie tiedostoon", null, 'V',
            KeyStroke.getKeyStroke('E', shortcutKeyMask),
            l.exportListener
        ))
        
        menu.addSeparator()
        
        menu.add(SwingUtils.createMenuItem(
            "Tuo CSV-tiedostosta…", null, 'C',
            KeyStroke.getKeyStroke('I', shortcutKeyMask),
            l.csvImportListener
        ))
        
        return menu
    }
    
    /**
     * Luo Ohje-valikon.
     */
    private fun createHelpMenu(l: MenuListeners): JMenu {
        val menu = JMenu("Ohje").apply {
            mnemonic = 'O'.code
        }
        
        menu.add(SwingUtils.createMenuItem(
            "Sisältö", null, 'S',
            KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0),
            l.helpListener
        ))
        
        menu.add(SwingUtils.createMenuItem(
            "Virheenjäljitystietoja", null, 'V',
            null, l.debugListener
        ))
        
        if (!System.getProperty("os.name").lowercase().startsWith("mac os x")) {
            // Macilla tämä menee sovellusvalikkoon (setAboutAction)
            menu.add(SwingUtils.createMenuItem(
                "Tietoja ohjelmasta", null, 'T',
                null, l.aboutListener
            ))
        }
        
        return menu
    }
    
    // Getters for menu references
    fun getEntryTemplateMenu(): JMenu = entryTemplateMenu
    fun getDocTypeMenu(): JMenu = docTypeMenu
    fun getGotoMenu(): JMenu = gotoMenu
    fun getReportsMenu(): JMenu = reportsMenu
    fun getToolsMenu(): JMenu = toolsMenu
    fun getRecentMenu(): JMenu = recentMenu
    
    // Getters for menu items
    fun getNewDatabaseMenuItem(): JMenuItem = newDatabaseMenuItem
    fun getOpenDatabaseMenuItem(): JMenuItem = openDatabaseMenuItem
    fun getNewDocMenuItem(): JMenuItem = newDocMenuItem
    fun getDeleteDocMenuItem(): JMenuItem = deleteDocMenuItem
    fun getAddEntryMenuItem(): JMenuItem = addEntryMenuItem
    fun getRemoveEntryMenuItem(): JMenuItem = removeEntryMenuItem
    fun getPasteMenuItem(): JMenuItem = pasteMenuItem
    fun getCoaMenuItem(): JMenuItem = coaMenuItem
    fun getVatDocumentMenuItem(): JMenuItem = vatDocumentMenuItem
    fun getEditEntryTemplatesMenuItem(): JMenuItem = editEntryTemplatesMenuItem
    fun getCreateEntryTemplateMenuItem(): JMenuItem = createEntryTemplateMenuItem
    fun getStartingBalancesMenuItem(): JMenuItem = startingBalancesMenuItem
    fun getPropertiesMenuItem(): JMenuItem = propertiesMenuItem
    fun getSettingsMenuItem(): JMenuItem = settingsMenuItem
    fun getSearchMenuItem(): JCheckBoxMenuItem = searchMenuItem
    fun getEditDocTypesMenuItem(): JMenuItem = editDocTypesMenuItem
    fun getSetIgnoreFlagMenuItem(): JMenuItem = setIgnoreFlagMenuItem
    
    /**
     * Sisältää kaikki valikkojen tarvitsemat kuuntelijat.
     */
    class MenuListeners {
        @JvmField var newDatabaseListener: ActionListener? = null
        @JvmField var openDatabaseListener: ActionListener? = null
        @JvmField var databaseSettingsListener: ActionListener? = null
        @JvmField var backupSettingsListener: ActionListener? = null
        @JvmField var restoreBackupListener: ActionListener? = null
        @JvmField var quitListener: ActionListener? = null
        @JvmField var copyEntriesAction: Action? = null
        @JvmField var pasteEntriesAction: Action? = null
        @JvmField var newDocListener: ActionListener? = null
        @JvmField var deleteDocListener: ActionListener? = null
        @JvmField var addEntryListener: ActionListener? = null
        @JvmField var removeEntryListener: ActionListener? = null
        @JvmField var editEntryTemplatesListener: ActionListener? = null
        @JvmField var createEntryTemplateListener: ActionListener? = null
        @JvmField var chartOfAccountsListener: ActionListener? = null
        @JvmField var startingBalancesListener: ActionListener? = null
        @JvmField var propertiesListener: ActionListener? = null
        @JvmField var settingsListener: ActionListener? = null
        @JvmField var appearanceListener: ActionListener? = null
        @JvmField var prevDocListener: ActionListener? = null
        @JvmField var nextDocListener: ActionListener? = null
        @JvmField var firstDocListener: ActionListener? = null
        @JvmField var lastDocListener: ActionListener? = null
        @JvmField var findDocumentByNumberListener: ActionListener? = null
        @JvmField var searchListener: ActionListener? = null
        @JvmField var editDocTypesListener: ActionListener? = null
        @JvmField var printListener: ActionListener? = null
        @JvmField var editReportsListener: ActionListener? = null
        @JvmField var vatDocumentListener: ActionListener? = null
        @JvmField var setIgnoreFlagToEntryAction: Action? = null
        @JvmField var balanceComparisonListener: ActionListener? = null
        @JvmField var numberShiftListener: ActionListener? = null
        @JvmField var vatChangeListener: ActionListener? = null
        @JvmField var exportListener: ActionListener? = null
        @JvmField var csvImportListener: ActionListener? = null
        @JvmField var helpListener: ActionListener? = null
        @JvmField var debugListener: ActionListener? = null
        @JvmField var aboutListener: ActionListener? = null
    }
}
