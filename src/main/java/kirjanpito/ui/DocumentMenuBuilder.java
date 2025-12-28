package kirjanpito.ui;

import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import javax.swing.Action;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

/**
 * Rakentaa DocumentFrame:n valikkorivin.
 * 
 * Eriytetty DocumentFrame:sta modulaarisuuden parantamiseksi.
 * 
 * @author Tilitin Team
 */
public class DocumentMenuBuilder {
    
    private final int shortcutKeyMask;
    
    // Menu references (need to be accessed by DocumentFrame)
    private JMenu entryTemplateMenu;
    private JMenu docTypeMenu;
    private JMenu gotoMenu;
    private JMenu reportsMenu;
    private JMenu toolsMenu;
    private JMenu recentMenu;
    
    // MenuItem references
    private JMenuItem newDatabaseMenuItem;
    private JMenuItem openDatabaseMenuItem;
    private JMenuItem newDocMenuItem;
    private JMenuItem deleteDocMenuItem;
    private JMenuItem addEntryMenuItem;
    private JMenuItem removeEntryMenuItem;
    private JMenuItem pasteMenuItem;
    private JMenuItem coaMenuItem;
    private JMenuItem vatDocumentMenuItem;
    private JMenuItem editEntryTemplatesMenuItem;
    private JMenuItem createEntryTemplateMenuItem;
    private JMenuItem startingBalancesMenuItem;
    private JMenuItem propertiesMenuItem;
    private JMenuItem settingsMenuItem;
    private JCheckBoxMenuItem searchMenuItem;
    private JMenuItem editDocTypesMenuItem;
    private JMenuItem setIgnoreFlagMenuItem;
    
    public DocumentMenuBuilder() {
        // Use modern API instead of deprecated getMenuShortcutKeyMask()
        // Detect OS and use appropriate mask (Ctrl on Windows/Linux, Cmd on Mac)
        String osName = System.getProperty("os.name").toLowerCase();
        if (osName.contains("mac")) {
            this.shortcutKeyMask = InputEvent.META_DOWN_MASK;
        } else {
            this.shortcutKeyMask = InputEvent.CTRL_DOWN_MASK;
        }
    }
    
    /**
     * Luo valikkorivin kaikilla valikoilla.
     * 
     * @param listeners Kuuntelijat valikkojen toiminnoille
     * @return Valmis valikkorivi
     */
    public JMenuBar build(MenuListeners listeners) {
        JMenuBar menuBar = new JMenuBar();
        
        menuBar.add(createFileMenu(listeners));
        menuBar.add(createEditMenu(listeners));
        menuBar.add(createGotoMenu(listeners));
        menuBar.add(createDocTypeMenu(listeners));
        menuBar.add(createReportsMenu(listeners));
        menuBar.add(createToolsMenu(listeners));
        menuBar.add(createHelpMenu(listeners));
        
        return menuBar;
    }
    
    /**
     * Luo Tiedosto-valikon.
     */
    private JMenu createFileMenu(MenuListeners l) {
        JMenu menu = new JMenu("Tiedosto");
        menu.setMnemonic('T');
        
        newDatabaseMenuItem = SwingUtils.createMenuItem("Uusi…",
                null, 'U', KeyStroke.getKeyStroke('U', shortcutKeyMask), 
                l.newDatabaseListener);
        menu.add(newDatabaseMenuItem);
        
        openDatabaseMenuItem = SwingUtils.createMenuItem("Avaa…",
                null, 'A', KeyStroke.getKeyStroke('O', shortcutKeyMask),
                l.openDatabaseListener);
        menu.add(openDatabaseMenuItem);
        
        // Viimeisimmät tietokannat -alivalikko
        recentMenu = new JMenu("Viimeisimmät");
        recentMenu.setMnemonic('V');
        menu.add(recentMenu);
        
        menu.add(SwingUtils.createMenuItem("Tietokanta-asetukset…", null, 'T', 
                KeyStroke.getKeyStroke('D', shortcutKeyMask), l.databaseSettingsListener));
        menu.add(SwingUtils.createMenuItem("Varmuuskopiointi…", null, 'V',
                null, l.backupSettingsListener));
        menu.add(SwingUtils.createMenuItem("Palauta varmuuskopiosta…", null, 'P',
                null, l.restoreBackupListener));
        
        menu.addSeparator();
        menu.add(SwingUtils.createMenuItem("Lopeta", "quit-16x16.png", 'L',
                KeyStroke.getKeyStroke('Q', shortcutKeyMask),
                l.quitListener));
        
        return menu;
    }
    
    /**
     * Luo Muokkaa-valikon.
     */
    private JMenu createEditMenu(MenuListeners l) {
        JMenu menu = new JMenu("Muokkaa");
        menu.setMnemonic('M');
        
        menu.add(SwingUtils.createMenuItem("Kopioi", null, 'K',
                KeyStroke.getKeyStroke(KeyEvent.VK_C, shortcutKeyMask), 
                l.copyEntriesAction));
        
        pasteMenuItem = SwingUtils.createMenuItem("Liitä", null, 'L',
                KeyStroke.getKeyStroke(KeyEvent.VK_V, shortcutKeyMask), 
                l.pasteEntriesAction);
        menu.add(pasteMenuItem);
        menu.addSeparator();
        
        newDocMenuItem = SwingUtils.createMenuItem("Uusi tosite", "document-new-16x16.png", 'U',
                KeyStroke.getKeyStroke('N', shortcutKeyMask),
                l.newDocListener);
        
        deleteDocMenuItem = SwingUtils.createMenuItem("Poista tosite", "delete-16x16.png", 'P',
                KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, shortcutKeyMask), 
                l.deleteDocListener);
        
        menu.add(newDocMenuItem);
        menu.add(deleteDocMenuItem);
        menu.addSeparator();
        
        addEntryMenuItem = SwingUtils.createMenuItem("Lisää vienti",
                "list-add-16x16.png", 'L',
                KeyStroke.getKeyStroke(KeyEvent.VK_F8, 0), l.addEntryListener);
        
        removeEntryMenuItem = SwingUtils.createMenuItem("Poista vienti",
                "list-remove-16x16.png", 'o',
                KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, InputEvent.SHIFT_DOWN_MASK), 
                l.removeEntryListener);
        
        entryTemplateMenu = new JMenu("Vientimallit");
        editEntryTemplatesMenuItem = SwingUtils.createMenuItem("Muokkaa", null, 'M',
                KeyStroke.getKeyStroke(KeyEvent.VK_M, shortcutKeyMask),
                l.editEntryTemplatesListener);
        
        createEntryTemplateMenuItem = SwingUtils.createMenuItem("Luo tositteesta", null, 'K',
                KeyStroke.getKeyStroke(KeyEvent.VK_K, shortcutKeyMask),
                l.createEntryTemplateListener);
        
        menu.add(addEntryMenuItem);
        menu.add(removeEntryMenuItem);
        menu.add(entryTemplateMenu);
        
        menu.addSeparator();
        
        coaMenuItem = SwingUtils.createMenuItem("Tilikartta…", null, 'T',
                KeyStroke.getKeyStroke(KeyEvent.VK_T, shortcutKeyMask), 
                l.chartOfAccountsListener);
        
        startingBalancesMenuItem = SwingUtils.createMenuItem("Alkusaldot…", null, 's',
                KeyStroke.getKeyStroke('B', shortcutKeyMask), l.startingBalancesListener);
        
        propertiesMenuItem = SwingUtils.createMenuItem("Perustiedot…", null, 'e',
                KeyStroke.getKeyStroke('P', shortcutKeyMask), l.propertiesListener);
        
        settingsMenuItem = SwingUtils.createMenuItem("Kirjausasetukset…", null, 'K',
                KeyStroke.getKeyStroke('S', shortcutKeyMask | InputEvent.SHIFT_DOWN_MASK), 
                l.settingsListener);
        
        JMenuItem appearanceMenuItem = SwingUtils.createMenuItem("Ulkoasu…", null, 'U',
                KeyStroke.getKeyStroke('A', shortcutKeyMask | InputEvent.SHIFT_DOWN_MASK), 
                l.appearanceListener);
        
        menu.add(coaMenuItem);
        menu.add(startingBalancesMenuItem);
        menu.add(propertiesMenuItem);
        menu.add(settingsMenuItem);
        menu.add(appearanceMenuItem);
        
        return menu;
    }
    
    /**
     * Luo Siirry-valikon.
     */
    private JMenu createGotoMenu(MenuListeners l) {
        JMenu menu = gotoMenu = new JMenu("Siirry");
        menu.setMnemonic('S');
        
        menu.add(SwingUtils.createMenuItem("Edellinen", "go-previous-16x16.png", 'E',
                KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_UP, 0),
                l.prevDocListener));
        
        menu.add(SwingUtils.createMenuItem("Seuraava", "go-next-16x16.png", 'S',
                KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN, 0),
                l.nextDocListener));
        
        menu.addSeparator();
        
        menu.add(SwingUtils.createMenuItem("Ensimmäinen", "go-first-16x16.png", 'n',
                KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_UP, shortcutKeyMask), 
                l.firstDocListener));
        
        menu.add(SwingUtils.createMenuItem("Viimeinen", "go-last-16x16.png", 'V',
                KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN, shortcutKeyMask), 
                l.lastDocListener));
        
        menu.addSeparator();
        
        menu.add(SwingUtils.createMenuItem("Hae numerolla…",
                null, 'n', KeyStroke.getKeyStroke(KeyEvent.VK_G, shortcutKeyMask), 
                l.findDocumentByNumberListener));
        
        searchMenuItem = new JCheckBoxMenuItem("Etsi…");
        searchMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, shortcutKeyMask));
        searchMenuItem.addActionListener(l.searchListener);
        menu.add(searchMenuItem);
        
        return menu;
    }
    
    /**
     * Luo Tositelaji-valikon.
     */
    private JMenu createDocTypeMenu(MenuListeners l) {
        JMenu menu = docTypeMenu = new JMenu("Tositelaji");
        menu.setMnemonic('l');
        
        editDocTypesMenuItem = SwingUtils.createMenuItem("Muokkaa", null, 'M',
                KeyStroke.getKeyStroke(KeyEvent.VK_L, shortcutKeyMask),
                l.editDocTypesListener);
        menu.add(editDocTypesMenuItem);
        
        return menu;
    }
    
    /**
     * Luo Tulosteet-valikon.
     */
    private JMenu createReportsMenu(MenuListeners l) {
        JMenu menu = reportsMenu = new JMenu("Tulosteet");
        menu.setMnemonic('u');
        
        JMenuItem menuItem;
        
        menuItem = SwingUtils.createMenuItem("Tilien saldot", null, 's',
                KeyStroke.getKeyStroke(KeyEvent.VK_1, shortcutKeyMask), l.printListener);
        menuItem.setActionCommand("accountSummary");
        menu.add(menuItem);
        
        menuItem = SwingUtils.createMenuItem("Tosite", null, 'O',
                KeyStroke.getKeyStroke(KeyEvent.VK_2, shortcutKeyMask), l.printListener);
        menuItem.setActionCommand("document");
        menu.add(menuItem);
        
        menuItem = SwingUtils.createMenuItem("Tiliote", null, 'T',
                KeyStroke.getKeyStroke(KeyEvent.VK_3, shortcutKeyMask), l.printListener);
        menuItem.setActionCommand("accountStatement");
        menu.add(menuItem);
        
        menuItem = SwingUtils.createMenuItem("Tuloslaskelma", null, 'u',
                KeyStroke.getKeyStroke(KeyEvent.VK_4, shortcutKeyMask), l.printListener);
        menuItem.setActionCommand("incomeStatement");
        menu.add(menuItem);
        
        menuItem = SwingUtils.createMenuItem("Tuloslaskelma erittelyin", null, 'e',
                KeyStroke.getKeyStroke(KeyEvent.VK_5, shortcutKeyMask), l.printListener);
        menuItem.setActionCommand("incomeStatementDetailed");
        menu.add(menuItem);
        
        menuItem = SwingUtils.createMenuItem("Tase", null, 'a',
                KeyStroke.getKeyStroke(KeyEvent.VK_6, shortcutKeyMask), l.printListener);
        menuItem.setActionCommand("balanceSheet");
        menu.add(menuItem);
        
        menuItem = SwingUtils.createMenuItem("Tase erittelyin", null, 'e',
                KeyStroke.getKeyStroke(KeyEvent.VK_7, shortcutKeyMask), l.printListener);
        menuItem.setActionCommand("balanceSheetDetailed");
        menu.add(menuItem);
        
        menuItem = SwingUtils.createMenuItem("Päiväkirja", null, 'P',
                KeyStroke.getKeyStroke(KeyEvent.VK_8, shortcutKeyMask), l.printListener);
        menuItem.setActionCommand("generalJournal");
        menu.add(menuItem);
        
        menuItem = SwingUtils.createMenuItem("Pääkirja", null, 'k',
                KeyStroke.getKeyStroke(KeyEvent.VK_9, shortcutKeyMask), l.printListener);
        menuItem.setActionCommand("generalLedger");
        menu.add(menuItem);
        
        menuItem = SwingUtils.createMenuItem("ALV-laskelma tileittäin", null, 'V',
                KeyStroke.getKeyStroke(KeyEvent.VK_0, shortcutKeyMask), l.printListener);
        menuItem.setActionCommand("vatReport");
        menu.add(menuItem);
        
        JMenu submenu = new JMenu("Tilikartta");
        submenu.setMnemonic('r');
        menu.add(submenu);
        
        menuItem = SwingUtils.createMenuItem("Vain käytössä olevat tilit", null, 'V', null, l.printListener);
        menuItem.setActionCommand("coa1");
        submenu.add(menuItem);
        
        menuItem = SwingUtils.createMenuItem("Vain suosikkitilit", null, 's', null, l.printListener);
        menuItem.setActionCommand("coa2");
        submenu.add(menuItem);
        
        menuItem = SwingUtils.createMenuItem("Kaikki tilit", null, 'k', null, l.printListener);
        menuItem.setActionCommand("coa0");
        submenu.add(menuItem);
        
        menu.addSeparator();
        menu.add(SwingUtils.createMenuItem("Muokkaa", null, 'M', null, l.editReportsListener));
        
        return menu;
    }
    
    /**
     * Luo Työkalut-valikon.
     */
    private JMenu createToolsMenu(MenuListeners l) {
        JMenu menu = toolsMenu = new JMenu("Työkalut");
        menu.setMnemonic('y');
        
        vatDocumentMenuItem = SwingUtils.createMenuItem("ALV-tilien päättäminen",
                null, 'p', KeyStroke.getKeyStroke(KeyEvent.VK_R, shortcutKeyMask), 
                l.vatDocumentListener);
        menu.add(vatDocumentMenuItem);
        
        setIgnoreFlagMenuItem = SwingUtils.createMenuItem("Ohita vienti ALV-laskelmassa", null, 'O',
                KeyStroke.getKeyStroke(KeyEvent.VK_H, shortcutKeyMask),
                l.setIgnoreFlagToEntryAction);
        menu.add(setIgnoreFlagMenuItem);
        
        menu.add(SwingUtils.createMenuItem("Tilien saldojen vertailu", null, 'T',
                null, l.balanceComparisonListener));
        
        menu.add(SwingUtils.createMenuItem("Muuta tositenumeroita", null, 'n',
                null, l.numberShiftListener));
        
        menu.add(SwingUtils.createMenuItem("ALV-kantojen muutokset", null, 'm',
                null, l.vatChangeListener));
        
        menu.add(SwingUtils.createMenuItem("Vie tiedostoon",
                null, 'V', KeyStroke.getKeyStroke('E', shortcutKeyMask), l.exportListener));
        
        menu.addSeparator();
        
        menu.add(SwingUtils.createMenuItem("Tuo CSV-tiedostosta…",
                null, 'C', KeyStroke.getKeyStroke('I', shortcutKeyMask), l.csvImportListener));
        
        return menu;
    }
    
    /**
     * Luo Ohje-valikon.
     */
    private JMenu createHelpMenu(MenuListeners l) {
        JMenu menu = new JMenu("Ohje");
        menu.setMnemonic('O');
        
        menu.add(SwingUtils.createMenuItem("Sisältö", null, 'S',
                KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0),
                l.helpListener));
        
        menu.add(SwingUtils.createMenuItem("Virheenjäljitystietoja", null, 'V',
                null, l.debugListener));
        
        if (!System.getProperty("os.name").toLowerCase().startsWith("mac os x")) {
            // Macilla tämä menee sovellusvalikkoon (setAboutAction)
            menu.add(SwingUtils.createMenuItem("Tietoja ohjelmasta", null, 'T',
                    null, l.aboutListener));
        }
        
        return menu;
    }
    
    // Getters for menu references
    public JMenu getEntryTemplateMenu() { return entryTemplateMenu; }
    public JMenu getDocTypeMenu() { return docTypeMenu; }
    public JMenu getGotoMenu() { return gotoMenu; }
    public JMenu getReportsMenu() { return reportsMenu; }
    public JMenu getToolsMenu() { return toolsMenu; }
    public JMenu getRecentMenu() { return recentMenu; }
    
    // Getters for menu items
    public JMenuItem getNewDatabaseMenuItem() { return newDatabaseMenuItem; }
    public JMenuItem getOpenDatabaseMenuItem() { return openDatabaseMenuItem; }
    public JMenuItem getNewDocMenuItem() { return newDocMenuItem; }
    public JMenuItem getDeleteDocMenuItem() { return deleteDocMenuItem; }
    public JMenuItem getAddEntryMenuItem() { return addEntryMenuItem; }
    public JMenuItem getRemoveEntryMenuItem() { return removeEntryMenuItem; }
    public JMenuItem getPasteMenuItem() { return pasteMenuItem; }
    public JMenuItem getCoaMenuItem() { return coaMenuItem; }
    public JMenuItem getVatDocumentMenuItem() { return vatDocumentMenuItem; }
    public JMenuItem getEditEntryTemplatesMenuItem() { return editEntryTemplatesMenuItem; }
    public JMenuItem getCreateEntryTemplateMenuItem() { return createEntryTemplateMenuItem; }
    public JMenuItem getStartingBalancesMenuItem() { return startingBalancesMenuItem; }
    public JMenuItem getPropertiesMenuItem() { return propertiesMenuItem; }
    public JMenuItem getSettingsMenuItem() { return settingsMenuItem; }
    public JCheckBoxMenuItem getSearchMenuItem() { return searchMenuItem; }
    public JMenuItem getEditDocTypesMenuItem() { return editDocTypesMenuItem; }
    public JMenuItem getSetIgnoreFlagMenuItem() { return setIgnoreFlagMenuItem; }
    
    /**
     * Sisältää kaikki valikkojen tarvitsemat kuuntelijat.
     */
    public static class MenuListeners {
        public ActionListener newDatabaseListener;
        public ActionListener openDatabaseListener;
        public ActionListener databaseSettingsListener;
        public ActionListener backupSettingsListener;
        public ActionListener restoreBackupListener;
        public ActionListener quitListener;
        public Action copyEntriesAction;
        public Action pasteEntriesAction;
        public ActionListener newDocListener;
        public ActionListener deleteDocListener;
        public ActionListener addEntryListener;
        public ActionListener removeEntryListener;
        public ActionListener editEntryTemplatesListener;
        public ActionListener createEntryTemplateListener;
        public ActionListener chartOfAccountsListener;
        public ActionListener startingBalancesListener;
        public ActionListener propertiesListener;
        public ActionListener settingsListener;
        public ActionListener appearanceListener;
        public ActionListener prevDocListener;
        public ActionListener nextDocListener;
        public ActionListener firstDocListener;
        public ActionListener lastDocListener;
        public ActionListener findDocumentByNumberListener;
        public ActionListener searchListener;
        public ActionListener editDocTypesListener;
        public ActionListener printListener;
        public ActionListener editReportsListener;
        public ActionListener vatDocumentListener;
        public Action setIgnoreFlagToEntryAction;
        public ActionListener balanceComparisonListener;
        public ActionListener numberShiftListener;
        public ActionListener vatChangeListener;
        public ActionListener exportListener;
        public ActionListener csvImportListener;
        public ActionListener helpListener;
        public ActionListener debugListener;
        public ActionListener aboutListener;
    }
}
