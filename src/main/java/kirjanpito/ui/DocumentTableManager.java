package kirjanpito.ui;

import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.JLabel;
import javax.swing.table.TableColumnModel;

import kirjanpito.db.Settings;
import kirjanpito.models.DocumentModel;
import kirjanpito.models.EntryTableModel;
import kirjanpito.util.AppSettings;
import kirjanpito.util.Registry;

/**
 * Hallinnoi entry table -komponentin luomista ja konfigurointia DocumentFrame:lle.
 * 
 * Tämä luokka eriytetään DocumentFrame:stä Phase 3b -refaktoroinnin osana.
 * Se vastaa taulukon luomisesta, renderereiden/editorien asettamisesta,
 * sarakkeiden leveyksien hallinnasta ja näppäimistöoikotieiden määrittelystä.
 * 
 * @since 2.2.0
 */
public class DocumentTableManager {
    
    private final JTable entryTable;
    private final EntryTableModel tableModel;
    private final Registry registry;
    private final DocumentModel model;
    private final JPanel container;
    
    // Cell renderers and editors
    private AccountCellRenderer accountCellRenderer;
    private AccountCellEditor accountCellEditor;
    private DescriptionCellEditor descriptionCellEditor;
    private CurrencyCellRenderer currencyCellRenderer;
    private CurrencyCellEditor currencyCellEditor;
    
    // Header renderer
    private EntryTableHeaderRenderer tableHeaderRenderer;
    
    // Column widths
    private static final int[] DEFAULT_COLUMN_WIDTHS = {190, 80, 80, 80, 190};
    
    /**
     * Callback-rajapinta DocumentFrame:lle.
     */
    public interface TableCallbacks {
        /** Kutsutaan kun taulukon malli muuttuu (päivittää total row) */
        void onTableModelChanged();
        
        /** Kutsutaan kun tarvitaan tilivalinta dialogi */
        void showAccountSelection(String query);
        
        /** Kutsutaan kun tarvitaan toggle debit/credit action */
        Action getToggleDebitCreditAction();
    }
    
    /**
     * Rajapinta table actions -kokoelmalle.
     * DocumentFrame antaa nämä actionsit DocumentTableManager:ille.
     */
    public interface TableActions {
        Action getPrevCellAction();
        Action getNextCellAction();
        Action getAddEntryAction();
        Action getRemoveEntryAction();
        Action getCopyEntriesAction();
        Action getPasteEntriesAction();
        Action getPrevDocumentAction();
        Action getNextDocumentAction();
        Action getSetIgnoreFlagAction();
    }
    
    /**
     * Rajapinta sarakeindeksien muuntamiseen view/model välillä.
     */
    public interface ColumnMapper {
        int mapColumnIndexToModel(int viewIndex);
        int mapColumnIndexToView(int modelIndex);
    }
    
    private final TableCallbacks callbacks;
    private final ColumnMapper columnMapper;
    private TableActions tableActions;
    private TableColumn vatColumn;
    
    /**
     * Konstruktori.
     * 
     * @param registry Registry-instanssi
     * @param model DocumentModel-instanssi
     * @param container Paneeli, johon taulukko lisätään
     * @param callbacks Callback-rajapinta DocumentFrame:lle
     * @param columnMapper Sarakeindeksien muunnin
     */
    public DocumentTableManager(Registry registry, DocumentModel model, 
            JPanel container, TableCallbacks callbacks, ColumnMapper columnMapper) {
        this.registry = registry;
        this.model = model;
        this.container = container;
        this.callbacks = callbacks;
        this.columnMapper = columnMapper;
        
        // Create table model
        this.tableModel = new EntryTableModel(model);
        tableModel.addTableModelListener(e -> callbacks.onTableModelChanged());
        
        // Create table
        this.entryTable = new JTable(tableModel);
        
        // Initialize components
        initializeTable();
        setupCellRenderersAndEditors();
        configureColumns();
        // Keyboard shortcuts will be set up after tableActions is provided
        addTableToContainer();
    }
    
    /**
     * Asettaa table actionsit ja konfiguroi keyboard shortcuts.
     * Tämä kutsutaan DocumentFrame:sta sen jälkeen, kun actionsit on luotu.
     * 
     * @param tableActions Actionsit taulukon toiminnoille
     */
    public void setTableActions(TableActions tableActions) {
        this.tableActions = tableActions;
        setupKeyboardShortcuts();
    }
    
    /**
     * Palauttaa luodun JTable-instanssin.
     */
    public JTable getTable() {
        return entryTable;
    }
    
    /**
     * Palauttaa EntryTableModel-instanssin.
     */
    public EntryTableModel getTableModel() {
        return tableModel;
    }
    
    /**
     * Palauttaa AccountCellRenderer-instanssin.
     */
    public AccountCellRenderer getAccountCellRenderer() {
        return accountCellRenderer;
    }
    
    /**
     * Palauttaa AccountCellEditor-instanssin.
     */
    public AccountCellEditor getAccountCellEditor() {
        return accountCellEditor;
    }
    
    /**
     * Palauttaa DescriptionCellEditor-instanssin.
     */
    public DescriptionCellEditor getDescriptionCellEditor() {
        return descriptionCellEditor;
    }
    
    /**
     * Palauttaa EntryTableHeaderRenderer-instanssin.
     */
    public EntryTableHeaderRenderer getTableHeaderRenderer() {
        return tableHeaderRenderer;
    }
    
    /**
     * Asettaa rivin korkeuden fonttikoon perusteella.
     */
    public void setRowHeight(FontMetrics fontMetrics) {
        entryTable.setRowHeight(fontMetrics.getHeight() + 6);
    }
    
    /**
     * Tallentaa sarakkeiden leveydet asetuksiin.
     */
    public void saveColumnWidths() {
        AppSettings settings = AppSettings.getInstance();
        TableColumnModel columnModel = entryTable.getColumnModel();
        
        for (int i = 0; i < 5; i++) {
            int columnIndex = columnMapper.mapColumnIndexToView(i);
            
            if (columnIndex >= 0) {
                settings.set("table.columns." + i, 
                    columnModel.getColumn(columnIndex).getWidth());
            }
        }
    }
    
    /**
     * Päivittää taulukon asetukset (ALV-sarakkeen näkyvyys, muokattavuus, jne.).
     * 
     * @param settings Asetukset
     */
    public void updateTableSettings(Settings settings) {
        if (settings == null) {
            return;
        }

        TableColumnModel columnModel = entryTable.getColumnModel();
        boolean vatVisible = !settings.getProperty("vatVisible", "true").equals("false");

        if (vatVisible && vatColumn != null) {
            /* Näytetään ALV-sarake. */
            columnModel.addColumn(vatColumn);
            columnModel.moveColumn(columnModel.getColumnCount() - 1, 3);
            vatColumn = null;
        }
        else if (!vatVisible && vatColumn == null) {
            /* Piilotetaan ALV-sarake. */
            vatColumn = columnModel.getColumn(3);
            columnModel.removeColumn(vatColumn);
        }

        boolean vatEditable = settings.getProperty("vatLocked", "true").equals("false");
        tableModel.setVatEditable(vatEditable);

        boolean autoCompleteEnabled = !settings.getProperty("autoCompleteEnabled", "true").equals("false");
        model.setAutoCompleteEnabled(autoCompleteEnabled);
    }
    
    /**
     * Muuntaa sarakeindeksin view-indeksiksi model-indeksistä.
     * 
     * @param modelIndex Model-indeksi
     * @return View-indeksi tai -1 jos sarake ei ole näkyvissä
     */
    public int mapColumnIndexToView(int modelIndex) {
        int[] indexes = {0, 1, 2, 3, 4};

        if (vatColumn != null) {
            indexes[3] = -1;
            indexes[4] = 3;
        }

        if (modelIndex < 0 || modelIndex >= indexes.length) {
            return -1;
        }

        return indexes[modelIndex];
    }
    
    /**
     * Muuntaa sarakeindeksin model-indeksiksi view-indeksistä.
     * 
     * @param viewIndex View-indeksi
     * @return Model-indeksi tai -1 jos sarake ei ole näkyvissä
     */
    public int mapColumnIndexToModel(int viewIndex) {
        int[] indexes = {0, 1, 2, 3, 4};

        if (vatColumn != null) {
            indexes[3] = 4;
            indexes[4] = -1;
        }

        if (viewIndex < 0 || viewIndex >= indexes.length) {
            return -1;
        }

        return indexes[viewIndex];
    }
    
    /**
     * Palauttaa vatColumn-referenssin (tarvitaan DocumentFrame:ssa).
     */
    public TableColumn getVatColumn() {
        return vatColumn;
    }
    
    /**
     * Alustaa taulukon perusasetukset.
     */
    private void initializeTable() {
        entryTable.setFillsViewportHeight(true);
        entryTable.setPreferredScrollableViewportSize(new Dimension(680, 250));
        entryTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        entryTable.setSurrendersFocusOnKeystroke(true);
        entryTable.setColumnSelectionAllowed(true);
        
        // Setup header renderer
        tableHeaderRenderer = new EntryTableHeaderRenderer(
                entryTable.getTableHeader().getDefaultRenderer(), columnMapper);
        entryTable.getTableHeader().setDefaultRenderer(tableHeaderRenderer);
    }
    
    /**
     * Asettaa cell rendererit ja editorit.
     */
    private void setupCellRenderersAndEditors() {
        accountCellRenderer = new AccountCellRenderer(registry, tableModel);
        accountCellEditor = new AccountCellEditor(registry, tableModel, e -> {
            String q = accountCellEditor.getTextField().getText();
            callbacks.showAccountSelection(q);
        });
        
        descriptionCellEditor = new DescriptionCellEditor(model);
        
        currencyCellRenderer = new CurrencyCellRenderer();
        currencyCellEditor = new CurrencyCellEditor();
        currencyCellEditor.setActionListener(callbacks.getToggleDebitCreditAction());
        tableModel.setCurrencyCellEditor(currencyCellEditor);
    }
    
    /**
     * Konfiguroi sarakkeet (leveydet, rendererit, editorit).
     */
    private void configureColumns() {
        TableCellRenderer[] renderers = new TableCellRenderer[] {
            accountCellRenderer, currencyCellRenderer, currencyCellRenderer,
            currencyCellRenderer, null };
        
        TableCellEditor[] editors = new TableCellEditor[] {
            accountCellEditor, currencyCellEditor, currencyCellEditor,
            currencyCellEditor, descriptionCellEditor };
        
        AppSettings settings = AppSettings.getInstance();
        TableColumnModel columnModel = entryTable.getColumnModel();
        
        for (int i = 0; i < DEFAULT_COLUMN_WIDTHS.length; i++) {
            TableColumn column = columnModel.getColumn(i);
            int width = settings.getInt("table.columns." + i, 0);
            
            if (width > 0) {
                column.setPreferredWidth(width);
            } else {
                column.setPreferredWidth(DEFAULT_COLUMN_WIDTHS[i]);
            }
            
            if (renderers[i] != null) {
                column.setCellRenderer(renderers[i]);
            }
            
            if (editors[i] != null) {
                column.setCellEditor(editors[i]);
            }
        }
    }
    
    /**
     * Asettaa näppäimistöoikotiet taulukkoon.
     */
    private void setupKeyboardShortcuts() {
        if (tableActions == null) {
            return; // Actions not set yet
        }
        
        // Use modern API instead of deprecated getMenuShortcutKeyMask()
        String osName = System.getProperty("os.name").toLowerCase();
        int shortcutKeyMask = osName.contains("mac") 
            ? InputEvent.META_DOWN_MASK 
            : InputEvent.CTRL_DOWN_MASK;
        
        InputMap inputMap = entryTable.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        InputMap focusedInputMap = entryTable.getInputMap(JComponent.WHEN_FOCUSED);
        
        // Enter: next/prev cell
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "nextCell");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, shortcutKeyMask), "nextCell");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, KeyEvent.SHIFT_DOWN_MASK), "prevCell");
        entryTable.getActionMap().put("prevCell", tableActions.getPrevCellAction());
        entryTable.getActionMap().put("nextCell", tableActions.getNextCellAction());
        
        // Previous row action (UP key)
        Object upKey = inputMap.get(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0));
        AbstractAction previousRowAction = new PreviousRowAction(
                entryTable.getActionMap().get(upKey), entryTable);
        entryTable.getActionMap().put(upKey, previousRowAction);
        
        // Insert/F8: add entry
        focusedInputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_INSERT, 0), "insertRow");
        focusedInputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F8, 0), "insertRow");
        entryTable.getActionMap().put("insertRow", tableActions.getAddEntryAction());
        
        // §: toggle debit/credit
        inputMap.put(KeyStroke.getKeyStroke('§'), "toggleDebitCredit");
        entryTable.getActionMap().put("toggleDebitCredit", callbacks.getToggleDebitCreditAction());
        
        // Copy/Paste (Ctrl+C, Ctrl+V)
        focusedInputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_C, shortcutKeyMask), "copy");
        entryTable.getActionMap().put("copy", tableActions.getCopyEntriesAction());
        
        focusedInputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_V, shortcutKeyMask), "paste");
        entryTable.getActionMap().put("paste", tableActions.getPasteEntriesAction());
        
        // Page Up/Down: navigate documents
        focusedInputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_UP, 0), "prevDocument");
        entryTable.getActionMap().put("prevDocument", tableActions.getPrevDocumentAction());
        
        focusedInputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN, 0), "nextDocument");
        entryTable.getActionMap().put("nextDocument", tableActions.getNextDocumentAction());
        
        // Delete: remove entry
        focusedInputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "removeRow");
        entryTable.getActionMap().put("removeRow", tableActions.getRemoveEntryAction());
        
        // Ctrl+H: set ignore flag
        focusedInputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_H, shortcutKeyMask), "setIgnoreFlag");
        entryTable.getActionMap().put("setIgnoreFlag", tableActions.getSetIgnoreFlagAction());
        
        // F12: remove suffix
        RemoveSuffixAction removeSuffixAction = new RemoveSuffixAction(
                entryTable, descriptionCellEditor);
        focusedInputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F12, 0), "removeSuffix");
        entryTable.getActionMap().put("removeSuffix", removeSuffixAction);
        
        // Also add to description cell editor text field
        descriptionCellEditor.getTextField().getInputMap(JComponent.WHEN_FOCUSED).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_F12, 0), "removeSuffix");
        descriptionCellEditor.getTextField().getActionMap().put(
                "removeSuffix", removeSuffixAction);
    }
    
    /**
     * Lisää taulukon container-paneeliin ScrollPanen sisällä.
     */
    private void addTableToContainer() {
        container.add(new JScrollPane(entryTable,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER));
    }
    
    /**
     * Header renderer entry table:lle.
     * Asettaa oikeat tasaukset sarakkeille.
     */
    public static class EntryTableHeaderRenderer extends DefaultTableCellRenderer {
        private static final long serialVersionUID = 1L;
        private final TableCellRenderer defaultRenderer;
        private final ColumnMapper columnMapper;
        private static final int[] alignments = {
            JLabel.LEFT, JLabel.RIGHT, JLabel.RIGHT, JLabel.RIGHT, JLabel.LEFT};
        
        public EntryTableHeaderRenderer(TableCellRenderer defaultRenderer, 
                ColumnMapper columnMapper) {
            this.defaultRenderer = defaultRenderer;
            this.columnMapper = columnMapper;
        }
        
        @Override
        public java.awt.Component getTableCellRendererComponent(JTable table,
                Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            
            java.awt.Component comp = defaultRenderer.getTableCellRendererComponent(table,
                    value, isSelected, hasFocus, row, column);
            
            if (comp instanceof javax.swing.JLabel) {
                int modelColumn = columnMapper.mapColumnIndexToModel(column);
                if (modelColumn >= 0 && modelColumn < alignments.length) {
                    ((javax.swing.JLabel)comp).setHorizontalAlignment(alignments[modelColumn]);
                }
            }
            
            return comp;
        }
    }
    
    /**
     * Action ylänuolen toiminnallisuuden muuntamiseen.
     * Kun ollaan ensimmäisellä rivillä, siirrytään takaisin päivämääräkenttään.
     */
    private static class PreviousRowAction extends AbstractAction {
        private static final long serialVersionUID = 1L;
        private final Action defaultAction;
        private final JTable entryTable;
        
        public PreviousRowAction(Action defaultAction, JTable entryTable) {
            this.defaultAction = defaultAction;
            this.entryTable = entryTable;
        }
        
        @Override
        public void actionPerformed(ActionEvent e) {
            int row = entryTable.getSelectedRow();
            defaultAction.actionPerformed(e);
            
            if (row <= 0) {
                entryTable.transferFocusBackward();
            }
        }
    }
    
    /**
     * Action selitteen päätteen poistamiseen (F12).
     */
    public static class RemoveSuffixAction extends AbstractAction {
        private static final long serialVersionUID = 1L;
        private final JTable entryTable;
        private final DescriptionCellEditor descriptionCellEditor;
        
        public RemoveSuffixAction(JTable entryTable, 
                DescriptionCellEditor descriptionCellEditor) {
            this.entryTable = entryTable;
            this.descriptionCellEditor = descriptionCellEditor;
        }
        
        @Override
        public void actionPerformed(ActionEvent e) {
            if (entryTable.isEditing()) {
                if (entryTable.getSelectedColumn() != 4) return;
            } else {
                int row = entryTable.getSelectedRow();
                if (row < 0) return;
                entryTable.editCellAt(row, 4);
            }
            
            descriptionCellEditor.removeSuffix();
        }
    }
}

