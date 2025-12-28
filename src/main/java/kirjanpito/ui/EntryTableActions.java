package kirjanpito.ui;

import java.awt.event.ActionEvent;
import java.math.BigDecimal;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JTable;
import javax.swing.JTextField;

import kirjanpito.db.Account;
import kirjanpito.db.Entry;
import kirjanpito.models.DocumentModel;
import kirjanpito.models.EntryTableModel;
import kirjanpito.util.Registry;

/**
 * Taulukon navigointitoiminnot DocumentFrame:lle.
 * 
 * Sisältää logiikan solujen välillä liikkumiseen entryTable-taulukossa,
 * debet/kredit-vaihdon ja muut taulukkoon liittyvät toiminnot.
 * 
 * @since 2.1.5
 */
public class EntryTableActions {
    
    private final JTable entryTable;
    private final DocumentModel model;
    private final EntryTableModel tableModel;
    private final Registry registry;
    private final JTextField dateTextField;
    private final DescriptionCellEditor descriptionCellEditor;
    private final ColumnMapper columnMapper;
    
    /**
     * Rajapinta sarakeindeksien muuntamiseen view/model välillä.
     */
    public interface ColumnMapper {
        int mapColumnIndexToModel(int viewIndex);
        int mapColumnIndexToView(int modelIndex);
    }
    
    /**
     * Callback-rajapinta DocumentFrame-toimintoihin.
     */
    public interface EntryTableCallback {
        void addEntry();
        void createDocument();
    }
    
    private final EntryTableCallback callback;
    
    public EntryTableActions(JTable entryTable, DocumentModel model, 
            EntryTableModel tableModel, Registry registry,
            JTextField dateTextField, DescriptionCellEditor descriptionCellEditor,
            ColumnMapper columnMapper, EntryTableCallback callback) {
        this.entryTable = entryTable;
        this.model = model;
        this.tableModel = tableModel;
        this.registry = registry;
        this.dateTextField = dateTextField;
        this.descriptionCellEditor = descriptionCellEditor;
        this.columnMapper = columnMapper;
        this.callback = callback;
    }
    
    /**
     * Luo action edelliseen soluun siirtymiseen (Shift+Tab).
     */
    public AbstractAction createPrevCellAction() {
        return new AbstractAction() {
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(ActionEvent e) {
                int column = columnMapper.mapColumnIndexToModel(entryTable.getSelectedColumn());
                int row = entryTable.getSelectedRow();
                boolean changed = false;

                if (entryTable.isEditing()) {
                    entryTable.getCellEditor().stopCellEditing();
                }

                if (row < 0) {
                    callback.addEntry();
                    return;
                }

                // Tilisarakkeesta siirrytään edellisen viennin selitteeseen
                if (column == 0) {
                    if (row > 0) {
                        row--;
                        column = 4;
                        changed = true;
                    }
                    else {
                        dateTextField.requestFocusInWindow();
                        return;
                    }
                }
                // Jos kreditsarakkeessa on 0,00, siirrytään debetsarakkeeseen
                else if (column == 1 || column == 2) {
                    BigDecimal amount = model.getEntry(row).getAmount();

                    if (amount.compareTo(BigDecimal.ZERO) == 0 && column == 2) {
                        column = 1;
                    }
                    else {
                        column = 0;
                    }

                    changed = true;
                }
                else {
                    Entry entry = model.getEntry(row);
                    column = entry.isDebit() ? 1 : 2;
                    changed = true;
                }

                if (changed) {
                    column = columnMapper.mapColumnIndexToView(column);
                    entryTable.changeSelection(row, column, false, false);
                    entryTable.editCellAt(row, column);
                }
            }
        };
    }
    
    /**
     * Luo action seuraavaan soluun siirtymiseen (Tab).
     */
    public AbstractAction createNextCellAction() {
        return new AbstractAction() {
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(ActionEvent e) {
                int column = columnMapper.mapColumnIndexToModel(entryTable.getSelectedColumn());
                int row = entryTable.getSelectedRow();
                boolean changed = false;

                if (entryTable.isEditing()) {
                    entryTable.getCellEditor().stopCellEditing();
                }

                if (row < 0) {
                    callback.addEntry();
                    return;
                }

                // Tilisarakkeesta siirrytään debet- tai kreditsarakkeeseen
                if (column == 0) {
                    column = model.getEntry(row).isDebit() ? 1 : 2;
                    changed = true;
                }
                // Jos debetsarakkeessa on 0,00, siirrytään kreditsarakkeeseen
                else if (column == 1 || column == 2) {
                    BigDecimal amount = model.getEntry(row).getAmount();

                    if (amount.compareTo(BigDecimal.ZERO) == 0 && column == 1) {
                        column = 2;
                    }
                    else {
                        column = 4;
                    }

                    changed = true;
                }
                else if (column == 3) {
                    column = 4;
                    changed = true;
                }
                else {
                    int lastRow = entryTable.getRowCount() - 1;

                    // Selitesarakkeesta siirrytään seuraavan rivin tilisarakkeeseen
                    if (row < lastRow) {
                        column = 0;
                        row++;
                        changed = true;
                    }
                    else if (row >= 0) {
                        String prevDescription = "";

                        if (row > 0) {
                            prevDescription = model.getEntry(row - 1).getDescription();
                        }

                        Entry entry = model.getEntry(row);

                        // Siirrytään uuteen tositteeseen, jos vientejä on jo vähintään kaksi
                        // ja selite on sama kuin edellisessä viennissä ja lisäksi
                        // tiliä ei ole valittu tai rahamäärä on nolla.
                        if (row == lastRow && row >= 1 && entry.getDescription().equals(prevDescription) &&
                                (entry.getAccountId() < 0 || BigDecimal.ZERO.compareTo(entry.getAmount()) == 0)) {
                            callback.createDocument();
                        }
                        else {
                            callback.addEntry();
                        }
                    }
                }

                if (changed) {
                    column = columnMapper.mapColumnIndexToView(column);
                    entryTable.changeSelection(row, column, false, false);
                    entryTable.editCellAt(row, column);
                }
            }
        };
    }
    
    /**
     * Luo action debet/kredit-vaihdon (* näppäin).
     */
    public AbstractAction createToggleDebitCreditAction() {
        return new AbstractAction() {
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(ActionEvent e) {
                if (entryTable.getSelectedRowCount() != 1 || entryTable.getSelectedColumnCount() != 1) {
                    return;
                }

                int column = entryTable.getSelectedColumn();

                if (column != 1 && column != 2) {
                    return;
                }

                boolean editing = entryTable.isEditing();
                int index = entryTable.getSelectedRow();

                if (editing) {
                    entryTable.getCellEditor().stopCellEditing();
                }

                boolean addVatEntries = model.getVatAmount(index).compareTo(BigDecimal.ZERO) != 0;
                Entry entry = model.getEntry(index);
                entry.setDebit(!entry.isDebit());
                model.updateAmount(index, model.getVatIncludedAmount(index), addVatEntries);
                model.setDocumentChanged();
                tableModel.fireTableRowsUpdated(index, index);

                if (editing) {
                    entryTable.editCellAt(index, entry.isDebit() ? 1 : 2);
                }
            }
        };
    }
    
    /**
     * Luo action edelliselle riville siirtymiseen.
     */
    public AbstractAction createPreviousRowAction(Action defaultAction) {
        return new AbstractAction() {
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(ActionEvent e) {
                int row = entryTable.getSelectedRow();
                defaultAction.actionPerformed(e);

                if (row <= 0) {
                    entryTable.transferFocusBackward();
                }
            }
        };
    }
    
    /**
     * Luo action päätteen poistamiseen (Ctrl+Backspace).
     */
    public AbstractAction createRemoveSuffixAction() {
        return new AbstractAction() {
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(ActionEvent e) {
                if (entryTable.isEditing()) {
                    if (entryTable.getSelectedColumn() != 4) return;
                }
                else {
                    int row = entryTable.getSelectedRow();
                    if (row < 0) return;
                    entryTable.editCellAt(row, 4);
                }

                descriptionCellEditor.removeSuffix();
            }
        };
    }
    
    /**
     * Luo action ALV-merkinnän ohittamiseen.
     */
    public AbstractAction createSetIgnoreFlagToEntryAction() {
        return new AbstractAction() {
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(ActionEvent e) {
                int[] rows = entryTable.getSelectedRows();

                if (rows.length == 0) {
                    return;
                }

                boolean ignore = !model.getEntry(rows[0]).getFlag(0);

                for (int index : rows) {
                    Entry entry = model.getEntry(index);
                    Account account = registry.getAccountById(entry.getAccountId());

                    if (account == null) {
                        continue;
                    }

                    if (account.getVatCode() == 2 || account.getVatCode() == 3) {
                        entry.setFlag(0, ignore);
                    }
                    else {
                        entry.setFlag(0, false);
                    }

                    model.setDocumentChanged();
                    tableModel.fireTableRowsUpdated(index, index);
                }
            }
        };
    }
}
