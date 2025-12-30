package kirjanpito.ui;

import java.awt.event.ActionListener;

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

	public ActionListener getQuitListener() {
		return e -> frame.quit();
	}

	// ========================================
	// GO MENU LISTENERS
	// ========================================

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
}
