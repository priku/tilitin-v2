package kirjanpito.ui;

import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFileChooser;

import kirjanpito.db.Account;
import kirjanpito.db.Entry;
import kirjanpito.models.DocumentModel;
import kirjanpito.util.AppSettings;

/**
 * Handles menu action listeners for DocumentFrame.
 *
 * This class was extracted from DocumentFrame as part of a refactoring effort
 * to reduce the size of the DocumentFrame god object (originally 3,093 lines).
 *
 * The listeners in this class delegate back to DocumentFrame methods for the
 * actual business logic. This keeps the separation of concerns while reducing
 * DocumentFrame's line count.
 *
 * @see DocumentFrame
 * @see DOCUMENTFRAME-MENU-REFACTORING.md
 */
public class DocumentMenuHandler {

	private final DocumentFrame frame;

	public DocumentMenuHandler(DocumentFrame frame) {
		this.frame = frame;
	}

	// ========================================
	// FILE MENU LISTENERS
	// ========================================

	public ActionListener getNewDatabaseListener() {
		return e -> {
			File dbDir = frame.getModel().getDatabaseDir();
			if (dbDir == null) {
				dbDir = new File(AppSettings.getInstance().getDirectoryPath());
			}
			
			// Ehdota automaattisesti vapaata nimeä
			File suggestedFile = frame.generateUniqueFileName(dbDir, "kirjanpito");
			
			final JFileChooser fileChooser = new JFileChooser(dbDir);
			fileChooser.setFileFilter(frame.getSqliteFileFilter());
			fileChooser.setAcceptAllFileFilterUsed(false);
			fileChooser.setDialogTitle("Uusi tietokanta");
			fileChooser.setSelectedFile(suggestedFile);
			File file = null;

			while (true) {
				fileChooser.showSaveDialog(frame);
				file = fileChooser.getSelectedFile();

				if (file == null) {
					return;
				}

				if (!file.getName().toLowerCase().endsWith(".sqlite")) {
					file = new File(file.getAbsolutePath() + ".sqlite");
				}

				if (file.exists()) {
					SwingUtils.showErrorMessage(frame, String.format(
							"Tiedosto %s on jo olemassa. Valitse toinen nimi.", file.getAbsolutePath()));
					// Ehdota seuraavaa vapaata nimeä
					String currentName = file.getName().replace(".sqlite", "");
					File nextSuggestion = frame.generateUniqueFileName(file.getParentFile(), currentName);
					fileChooser.setSelectedFile(nextSuggestion);
				}
				else {
					break;
				}
			}

			frame.openSqliteDataSource(file);
		};
	}

	public ActionListener getOpenDatabaseListener() {
		return e -> {
			final JFileChooser fileChooser = new JFileChooser(frame.getModel().getDatabaseDir());
			fileChooser.setFileFilter(frame.getSqliteFileFilter());
			fileChooser.setAcceptAllFileFilterUsed(false);
			fileChooser.setDialogTitle("Avaa tietokanta");
			fileChooser.showOpenDialog(frame);
			File file = fileChooser.getSelectedFile();

			if (file == null) {
				return;
			}

			frame.openSqliteDataSource(file);
		};
	}

	public ActionListener getDatabaseSettingsListener() {
		return e -> frame.showDatabaseSettings();
	}

	public ActionListener getQuitListener() {
		return e -> frame.quit();
	}

	// ========================================
	// GO MENU LISTENERS
	// ========================================

	public ActionListener getPrevDocListener() {
		return e -> frame.goToDocument(DocumentModel.FETCH_PREVIOUS);
	}

	public ActionListener getNextDocListener() {
		return e -> frame.goToDocument(DocumentModel.FETCH_NEXT);
	}

	public ActionListener getFirstDocListener() {
		return e -> frame.goToDocument(DocumentModel.FETCH_FIRST);
	}

	public ActionListener getLastDocListener() {
		return e -> frame.goToDocument(DocumentModel.FETCH_LAST);
	}

	public ActionListener getFindDocumentByNumberListener() {
		return e -> frame.findDocumentByNumber();
	}

	public ActionListener getSearchListener() {
		return e -> frame.toggleSearchPanel();
	}

	// ========================================
	// EDIT MENU LISTENERS
	// ========================================

	public ActionListener getNewDocListener() {
		return e -> frame.createDocument();
	}

	public ActionListener getDeleteDocListener() {
		return e -> frame.deleteDocument();
	}

	public ActionListener getEditEntryTemplatesListener() {
		return e -> frame.editEntryTemplates();
	}

	public ActionListener getCreateEntryTemplateListener() {
		return e -> frame.createEntryTemplateFromDocument();
	}

	// ========================================
	// SETTINGS MENU LISTENERS
	// ========================================

	public ActionListener getExportListener() {
		return e -> frame.export();
	}

	public ActionListener getCsvImportListener() {
		return e -> frame.showCsvImportDialog();
	}

	public ActionListener getChartOfAccountsListener() {
		return e -> frame.showChartOfAccounts();
	}

	public ActionListener getStartingBalancesListener() {
		return e -> frame.showStartingBalances();
	}

	public ActionListener getPropertiesListener() {
		return e -> frame.showProperties();
	}

	public ActionListener getSettingsListener() {
		return e -> frame.showSettings();
	}

	public ActionListener getAppearanceListener() {
		return e -> frame.showAppearanceDialog();
	}

	public ActionListener getRestoreBackupListener() {
		return e -> frame.restoreFromBackup();
	}

	// ========================================
	// DOCUMENT TYPE MENU LISTENERS
	// ========================================

	public ActionListener getEditDocTypesListener() {
		return e -> frame.editDocumentTypes();
	}

	// ========================================
	// REPORTS MENU LISTENERS
	// ========================================

	public ActionListener getEditReportsListener() {
		return e -> frame.editReports();
	}

	// ========================================
	// TOOLS MENU LISTENERS
	// ========================================

	public ActionListener getBalanceComparisonListener() {
		return e -> frame.showBalanceComparison();
	}

	public ActionListener getVatDocumentListener() {
		return e -> frame.createVATDocument();
	}

	public ActionListener getNumberShiftListener() {
		return e -> frame.showDocumentNumberShiftDialog();
	}

	public ActionListener getVatChangeListener() {
		return e -> frame.showVATChangeDialog();
	}

	/**
	 * Palauttaa Ohita vienti ALV-laskelmassa -toiminnon.
	 * Tämä on AbstractAction koska sitä käytetään ActionMap:issa.
	 */
	public Action getSetIgnoreFlagToEntryAction() {
		return new AbstractAction() {
			private static final long serialVersionUID = 1L;

			public void actionPerformed(java.awt.event.ActionEvent e) {
				int[] rows = frame.getEntryTable().getSelectedRows();

				if (rows.length == 0) {
					return;
				}

				boolean ignore = !frame.getModel().getEntry(rows[0]).getFlag(0);

				for (int index : rows) {
					Entry entry = frame.getModel().getEntry(index);
					Account account = frame.getRegistry().getAccountById(entry.getAccountId());

					if (account == null) {
						continue;
					}

					if (account.getVatCode() == 2 || account.getVatCode() == 3) {
						entry.setFlag(0, ignore);
					}
					else {
						entry.setFlag(0, false);
					}

					frame.getModel().setDocumentChanged();
					frame.getTableModel().fireTableRowsUpdated(index, index);
				}
			}
		};
	}

	// ========================================
	// HELP MENU LISTENERS
	// ========================================

	public ActionListener getHelpListener() {
		return e -> frame.showHelp();
	}

	public ActionListener getDebugListener() {
		return e -> frame.showLogMessages();
	}

	public ActionListener getAboutListener() {
		return e -> frame.showAboutDialog();
	}

	// ========================================
	// MEDIUM COMPLEXITY LISTENERS
	// ========================================
	// These listeners have simple logic (command parsing, etc.)

	public ActionListener getEntryTemplateListener() {
		return e -> {
			String command = e.getActionCommand();
			if (command != null) {
				frame.addEntriesFromTemplate(Integer.parseInt(command));
			}
		};
	}

	public ActionListener getDocTypeListener() {
		return e -> {
			String command = e.getActionCommand();
			if (command != null) {
				frame.setDocumentType(Integer.parseInt(command));
			}
		};
	}

	public ActionListener getBackupSettingsListener() {
		return e -> BackupSettingsDialog.show(frame);
	}

	// ========================================
	// COMPLEX LISTENERS
	// ========================================
	// These listeners have more complex logic and are extracted for clarity

	public ActionListener getPrintListener() {
		return e -> {
			String cmd = e.getActionCommand();
			if (cmd == null) return;

			switch (cmd) {
				case "accountSummary":
					frame.showAccountSummary();
					break;
				case "document":
					frame.showDocumentPrint();
					break;
				case "accountStatement":
					frame.showAccountStatement();
					break;
				case "incomeStatement":
					frame.showIncomeStatement(false);
					break;
				case "incomeStatementDetailed":
					frame.showIncomeStatement(true);
					break;
				case "balanceSheet":
					frame.showBalanceSheet(false);
					break;
				case "balanceSheetDetailed":
					frame.showBalanceSheet(true);
					break;
				case "generalJournal":
					frame.showGeneralJournal();
					break;
				case "generalLedger":
					frame.showGeneralLedger();
					break;
				case "vatReport":
					frame.showVATReport();
					break;
				case "coa0":
					frame.showChartOfAccountsPrint(0);
					break;
				case "coa1":
					frame.showChartOfAccountsPrint(1);
					break;
				case "coa2":
					frame.showChartOfAccountsPrint(2);
					break;
			}
		};
	}
}
