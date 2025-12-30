package kirjanpito.ui;

import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.Arrays;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JTable;
import javax.swing.JTextField;

import kirjanpito.db.Account;
import kirjanpito.db.Entry;
import kirjanpito.models.DocumentModel;
import kirjanpito.models.EntryTableModel;
import kirjanpito.util.Registry;

import java.awt.event.ActionEvent;

/**
 * Manages entry-related actions for DocumentFrame.
 * Handles adding, removing, copying, pasting entries and cell navigation.
 * 
 * Extracted from DocumentFrame as part of Phase 9 refactoring.
 */
public class DocumentEntryManager {

	private final DocumentModel model;
	private final EntryTableModel tableModel;
	private final JTable entryTable;
	private final Registry registry;
	private final JTextField dateTextField;
	private final DecimalFormat formatter;
	private final EntryCallbacks callbacks;

	/**
	 * Callback interface for entry operations that need DocumentFrame interaction.
	 */
	public interface EntryCallbacks {
		/** Updates the total row display */
		void updateTotalRow();
		
		/** Creates a new document */
		void createDocument();
		
		/** Maps view column index to model column index */
		int mapColumnIndexToModel(int viewIndex);
		
		/** Maps model column index to view column index */
		int mapColumnIndexToView(int modelIndex);
		
		/** Checks if document is editable */
		boolean isDocumentEditable();
		
		/** Gets VAT included amount for entry */
		BigDecimal getVatIncludedAmount(int index);
		
		/** Gets VAT amount for entry */
		BigDecimal getVatAmount(int index);
	}

	/**
	 * Constructor.
	 */
	public DocumentEntryManager(DocumentModel model, EntryTableModel tableModel,
			JTable entryTable, Registry registry, JTextField dateTextField,
			DecimalFormat formatter, EntryCallbacks callbacks) {
		this.model = model;
		this.tableModel = tableModel;
		this.entryTable = entryTable;
		this.registry = registry;
		this.dateTextField = dateTextField;
		this.formatter = formatter;
		this.callbacks = callbacks;
	}

	// ========== Public Entry Operations ==========

	/**
	 * Adds a new entry to the document.
	 */
	public void addEntry() {
		if (!callbacks.isDocumentEditable()) {
			return;
		}

		stopEditing();
		int index = model.addEntry();
		tableModel.fireTableRowsInserted(index, index);
		callbacks.updateTotalRow();
		entryTable.changeSelection(index, 0, false, false);
		entryTable.requestFocusInWindow();
	}

	/**
	 * Removes the selected entries.
	 */
	public void removeEntry() {
		if (!callbacks.isDocumentEditable()) {
			return;
		}

		int[] rows = entryTable.getSelectedRows();

		if (rows.length == 0) {
			return;
		}

		stopEditing();
		Arrays.sort(rows);
		int index = -1;

		for (int i = rows.length - 1; i >= 0; i--) {
			index = rows[i];
			model.removeEntry(index);
			tableModel.fireTableRowsDeleted(index, index);
		}

		callbacks.updateTotalRow();
		index = Math.min(index, tableModel.getRowCount() - 1);

		if (tableModel.getRowCount() > 0) {
			entryTable.setRowSelectionInterval(index, index);
			entryTable.requestFocusInWindow();
		}
		else if (entryTable.isFocusOwner()) {
			dateTextField.requestFocusInWindow();
		}
	}

	/**
	 * Copies selected entries to clipboard.
	 */
	public void copyEntries() {
		stopEditing();
		StringBuilder sb = new StringBuilder();
		int[] rows = entryTable.getSelectedRows();

		for (int i = 0; i < rows.length; i++) {
			Entry entry = model.getEntry(rows[i]);
			Account account = registry.getAccountById(entry.getAccountId());

			if (account == null) {
				sb.append('\t');
			}
			else {
				sb.append(account.getNumber());
				sb.append('\t');
				sb.append(account.getName());
			}

			sb.append('\t');

			if (entry.isDebit()) {
				sb.append(formatter.format(callbacks.getVatIncludedAmount(rows[i])));
				sb.append('\t');
			}
			else {
				sb.append('\t');
				sb.append(formatter.format(callbacks.getVatIncludedAmount(rows[i])));
			}

			sb.append('\t');
			sb.append(formatter.format(callbacks.getVatAmount(i)));
			sb.append('\t');
			sb.append(entry.getDescription());
			sb.append(System.getProperty("line.separator"));
		}

		Toolkit.getDefaultToolkit().getSystemClipboard()
				.setContents(new StringSelection(sb.toString()), null);
	}

	/**
	 * Pastes entries from clipboard.
	 */
	public void pasteEntries() {
		Transferable t = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
		String text = null;

		try {
			if (t != null && t.isDataFlavorSupported(DataFlavor.stringFlavor)) {
				text = t.getTransferData(DataFlavor.stringFlavor).toString();
			}
		}
		catch (UnsupportedFlavorException e) {
		}
		catch (IOException e) {
		}

		if (text == null) {
			return;
		}

		String[] lines = text.split("\n");
		stopEditing();

		for (String line : lines) {
			String[] cols = line.split("\t", 6);

			if (cols.length != 6) {
				continue;
			}

			int index = model.addEntry();
			Entry entry = model.getEntry(index);
			Account account = registry.getAccountByNumber(cols[0]);
			boolean vatEntries = true;

			if (account != null) {
				model.updateAccountId(index, account.getId());
			}

			if (!cols[4].isEmpty()) {
				/* Lasketaan ALV, jos ALV-sarakkeen rahamäärä on erisuuri kuin 0,00. */
				try {
					vatEntries = ((BigDecimal)formatter.parse(cols[4])).compareTo(BigDecimal.ZERO) != 0;
				}
				catch (ParseException e) {
				}
			}

			if (!cols[2].isEmpty()) {
				entry.setDebit(true);

				try {
					model.updateAmount(index, (BigDecimal)formatter.parse(cols[2]), vatEntries);
				}
				catch (ParseException e) {
				}
			}

			if (!cols[3].isEmpty()) {
				entry.setDebit(false);

				try {
					model.updateAmount(index, (BigDecimal)formatter.parse(cols[3]), vatEntries);
				}
				catch (ParseException e) {
				}
			}

			entry.setDescription(cols[5].trim());
			tableModel.fireTableRowsInserted(index, index);
		}

		callbacks.updateTotalRow();
	}

	// ========== Action Getters ==========

	/**
	 * Returns the add entry action.
	 */
	public Action getAddEntryAction() {
		return addEntryAction;
	}

	/**
	 * Returns the remove entry action.
	 */
	public Action getRemoveEntryAction() {
		return removeEntryAction;
	}

	/**
	 * Returns the copy entries action.
	 */
	public Action getCopyEntriesAction() {
		return copyEntriesAction;
	}

	/**
	 * Returns the paste entries action.
	 */
	public Action getPasteEntriesAction() {
		return pasteEntriesAction;
	}

	/**
	 * Returns the previous cell navigation action.
	 */
	public Action getPrevCellAction() {
		return prevCellAction;
	}

	/**
	 * Returns the next cell navigation action.
	 */
	public Action getNextCellAction() {
		return nextCellAction;
	}

	/**
	 * Returns the toggle debit/credit action.
	 */
	public Action getToggleDebitCreditAction() {
		return toggleDebitCreditAction;
	}

	// ========== Private Helper Methods ==========

	private void stopEditing() {
		if (entryTable.isEditing()) {
			entryTable.getCellEditor().stopCellEditing();
		}
	}

	// ========== Action Implementations ==========

	/* Add entry action */
	private AbstractAction addEntryAction = new AbstractAction() {
		private static final long serialVersionUID = 1L;
		@Override
		public void actionPerformed(ActionEvent e) {
			if (!entryTable.isEditing()) {
				addEntry();
			}
		}
	};

	/* Remove entry action */
	private AbstractAction removeEntryAction = new AbstractAction() {
		private static final long serialVersionUID = 1L;
		@Override
		public void actionPerformed(ActionEvent e) {
			if (!entryTable.isEditing()) {
				removeEntry();
				if (entryTable.getRowCount() == 0) {
					dateTextField.requestFocusInWindow();
				}
			}
		}
	};

	/* Copy entries action */
	private Action copyEntriesAction = new AbstractAction() {
		private static final long serialVersionUID = 1L;
		@Override
		public void actionPerformed(ActionEvent e) {
			copyEntries();
		}
	};

	/* Paste entries action */
	private Action pasteEntriesAction = new AbstractAction() {
		private static final long serialVersionUID = 1L;
		@Override
		public void actionPerformed(ActionEvent e) {
			pasteEntries();
		}
	};

	/* Previous cell navigation action (Shift+Enter) */
	private AbstractAction prevCellAction = new AbstractAction() {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			int column = callbacks.mapColumnIndexToModel(entryTable.getSelectedColumn());
			int row = entryTable.getSelectedRow();
			boolean changed = false;

			if (entryTable.isEditing())
				entryTable.getCellEditor().stopCellEditing();

			if (row < 0) {
				addEntry();
				return;
			}

			/* Tilisarakkeesta siirrytään edellisen viennin selitteeseen. */
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
			/* Jos kreditsarakkeessa on 0,00, siirrytään debetsarakkeeseen.
			 * Muussa tapauksessa kredit- ja debetsarakkeesta siirrytään
			 * selitesarakkeeseen. */
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
				column = callbacks.mapColumnIndexToView(column);
				entryTable.changeSelection(row, column, false, false);
				entryTable.editCellAt(row, column);
			}
		}
	};

	/* Next cell navigation action (Enter, Tab) */
	private AbstractAction nextCellAction = new AbstractAction() {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			int column = callbacks.mapColumnIndexToModel(entryTable.getSelectedColumn());
			int row = entryTable.getSelectedRow();
			boolean changed = false;

			if (entryTable.isEditing())
				entryTable.getCellEditor().stopCellEditing();

			if (row < 0) {
				addEntry();
				return;
			}

			/* Tilisarakkeesta siirrytään debet- tai kreditsarakkeeseen. */
			if (column == 0) {
				column = model.getEntry(row).isDebit() ? 1 : 2;
				changed = true;
			}
			/* Jos debetsarakkeessa on 0,00, siirrytään kreditsarakkeeseen.
			 * Muussa tapauksessa kredit- ja debetsarakkeesta siirrytään
			 * selitesarakkeeseen. */
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

				/* Selitesarakkeesta siirrytään seuraavan rivin
				 * tilisarakkeeseen. */
				if (row < lastRow) {
					column = 0;
					row++;
					changed = true;
				}
				else if (row >= 0) {
					String prevDescription = "";

					if (row > 0) {
						prevDescription = model.getEntry(
								row - 1).getDescription();
					}

					Entry entry = model.getEntry(row);

					/* Siirrytään uuteen tositteeseen, jos vientejä on jo vähintään kaksi
					 * ja selite on sama kuin edellisessä viennissä ja lisäksi
					 * tiliä ei ole valittu tai rahamäärä on nolla. */
					if (row == lastRow && row >= 1 && entry.getDescription().equals(prevDescription) &&
							(entry.getAccountId() < 0 || BigDecimal.ZERO.compareTo(entry.getAmount()) == 0)) {
						callbacks.createDocument();
					}
					else {
						addEntry();
					}
				}
			}

			if (changed) {
				column = callbacks.mapColumnIndexToView(column);
				entryTable.changeSelection(row, column, false, false);
				entryTable.editCellAt(row, column);
			}
		}
	};

	/* Toggle debit/credit action */
	private AbstractAction toggleDebitCreditAction = new AbstractAction() {
		private static final long serialVersionUID = 1L;

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

			boolean addVatEntries = callbacks.getVatAmount(index).compareTo(BigDecimal.ZERO) != 0;
			Entry entry = model.getEntry(index);
			entry.setDebit(!entry.isDebit());
			model.updateAmount(index, callbacks.getVatIncludedAmount(index), addVatEntries);
			model.setDocumentChanged();
			tableModel.fireTableRowsUpdated(index, index);

			if (editing) {
				entryTable.editCellAt(index, entry.isDebit() ? 1 : 2);
			}
		}
	};
}
