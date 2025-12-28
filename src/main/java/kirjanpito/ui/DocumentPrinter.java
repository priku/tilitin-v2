package kirjanpito.ui;

import java.awt.Image;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTable;

import kirjanpito.db.Account;
import kirjanpito.db.DataAccessException;
import kirjanpito.db.Entry;
import kirjanpito.db.Settings;
import kirjanpito.models.DocumentModel;
import kirjanpito.models.PrintPreviewModel;
import kirjanpito.reports.AccountStatementModel;
import kirjanpito.reports.AccountStatementPrint;
import kirjanpito.reports.AccountSummaryModel;
import kirjanpito.reports.AccountSummaryPrint;
import kirjanpito.reports.COAPrint;
import kirjanpito.reports.COAPrintModel;
import kirjanpito.reports.DocumentPrint;
import kirjanpito.reports.DocumentPrintModel;
import kirjanpito.reports.FinancialStatementModel;
import kirjanpito.reports.FinancialStatementPrint;
import kirjanpito.reports.GeneralJournalModel;
import kirjanpito.reports.GeneralJournalModelT;
import kirjanpito.reports.GeneralJournalPrint;
import kirjanpito.reports.GeneralLedgerModel;
import kirjanpito.reports.GeneralLedgerModelT;
import kirjanpito.reports.GeneralLedgerPrint;
import kirjanpito.reports.Print;
import kirjanpito.reports.PrintModel;
import kirjanpito.reports.VATReportModel;
import kirjanpito.reports.VATReportPrint;
import kirjanpito.util.AppSettings;
import kirjanpito.util.Registry;

/**
 * Hallinnoi print-toiminnallisuutta DocumentFrame:lle.
 * 
 * Tämä luokka eriytetään DocumentFrame:stä Phase 5 -refaktoroinnin osana.
 * Se vastaa kaikista print-toiminnoista: print preview -ikkunan hallinnasta,
 * raporttien näyttämisestä ja print-asetusten hallinnasta.
 * 
 * @since 2.2.0
 */
public class DocumentPrinter {
    
    private final JFrame parentFrame;
    private final Registry registry;
    private final PrintCallbacks callbacks;
    
    private PrintPreviewFrame printPreviewFrame;
    
    private static final Logger logger = Logger.getLogger("kirjanpito");
    
    /**
     * Callback-rajapinta DocumentFrame:lle.
     */
    public interface PrintCallbacks {
        /** Kutsutaan ennen print-toimintoa - tallentaa dokumentin jos muuttunut */
        boolean saveDocumentIfChanged();
        
        /** Palauttaa DocumentModel-instanssin */
        DocumentModel getDocumentModel();
        
        /** Palauttaa entry table -komponentin */
        JTable getEntryTable();
    }
    
    /**
     * Konstruktori.
     * 
     * @param parentFrame Pääikkuna (DocumentFrame)
     * @param registry Registry-instanssi
     * @param callbacks Callback-rajapinta DocumentFrame:lle
     */
    public DocumentPrinter(JFrame parentFrame, Registry registry, PrintCallbacks callbacks) {
        this.parentFrame = parentFrame;
        this.registry = registry;
        this.callbacks = callbacks;
    }
    
    /**
     * Näyttää tulosteiden esikatseluikkunan.
     *
     * @param printModel tulosteen malli
     * @param print tuloste
     */
    public void showPrintPreview(PrintModel printModel, Print print) {
        try {
            printModel.run();
        }
        catch (DataAccessException e) {
            String message = "Tulosteen luominen epäonnistui";
            logger.log(Level.SEVERE, message, e);
            SwingUtils.showDataAccessErrorMessage(parentFrame, e, message);
            return;
        }

        print.setSettings(registry.getSettings());
        PrintPreviewModel previewModel;

        if (printPreviewFrame == null) {
            previewModel = new PrintPreviewModel();
            printPreviewFrame = new PrintPreviewFrame(parentFrame, previewModel);
            Image iconImage = parentFrame.getIconImage();
            if (iconImage != null) {
                printPreviewFrame.setIconImage(iconImage);
            }
            printPreviewFrame.create();
        }
        else {
            previewModel = printPreviewFrame.getModel();
        }

        previewModel.setPrintModel(printModel);
        previewModel.setPrint(print);
        printPreviewFrame.updatePrint();
        printPreviewFrame.setVisible(true);
    }

    /**
     * Sulkee tulosteiden esikatseluikkunan, jos se on auki.
     */
    public void closePrintPreview() {
        if (printPreviewFrame != null) {
            printPreviewFrame.setVisible(false);
        }
    }
    
    /**
     * Sulkee ja vapauttaa print preview ikkunan.
     * Kutsutaan kun DocumentFrame suljetaan.
     */
    public void disposePrintPreview() {
        if (printPreviewFrame != null) {
            printPreviewFrame.close();
            printPreviewFrame = null;
        }
    }
    
    /**
     * Palauttaa PrintPreviewFrame-instanssin.
     * 
     * @return PrintPreviewFrame tai null jos ei ole luotu
     */
    public PrintPreviewFrame getPrintPreviewFrame() {
        return printPreviewFrame;
    }
    
    /**
     * Näyttää tilien saldot esikatseluikkunassa.
     */
    public void showAccountSummary() {
        if (!callbacks.saveDocumentIfChanged()) {
            return;
        }

        AppSettings settings = AppSettings.getInstance();
        AccountSummaryOptionsDialog dialog = new AccountSummaryOptionsDialog(parentFrame);
        dialog.create();
        dialog.setPeriod(registry.getPeriod());
        DocumentModel model = callbacks.getDocumentModel();
        dialog.setDocumentDate(model.getDocument().getDate());
        dialog.setDateSelectionMode(0);
        dialog.setPreviousPeriodVisible(settings.getBoolean("previous-period", false));
        dialog.setVisible(true);

        if (dialog.getResult() == JOptionPane.OK_OPTION) {
            boolean previousPeriodVisible = dialog.isPreviousPeriodVisible();
            settings.set("previous-period", previousPeriodVisible);
            int printedAccounts = dialog.getPrintedAccounts();
            AccountSummaryModel printModel = new AccountSummaryModel();
            printModel.setRegistry(registry);
            printModel.setPeriod(registry.getPeriod());
            printModel.setStartDate(dialog.getStartDate());
            printModel.setEndDate(dialog.getEndDate());
            printModel.setPreviousPeriodVisible(previousPeriodVisible);
            printModel.setPrintedAccounts(printedAccounts);
            showPrintPreview(printModel, new AccountSummaryPrint(printModel,
                    printedAccounts != 1));
        }

        dialog.dispose();
    }
    
    /**
     * Näyttää tositteen esikatseluikkunassa.
     */
    public void showDocumentPrint() {
        if (!callbacks.saveDocumentIfChanged()) {
            return;
        }

        DocumentModel model = callbacks.getDocumentModel();
        DocumentPrintModel printModel = new DocumentPrintModel();
        printModel.setRegistry(registry);
        printModel.setDocument(model.getDocument());
        showPrintPreview(printModel, new DocumentPrint(printModel));
    }
    
    /**
     * Näyttää tiliotteen esikatseluikkunassa.
     */
    public void showAccountStatement() {
        if (!callbacks.saveDocumentIfChanged()) {
            return;
        }

        JTable entryTable = callbacks.getEntryTable();
        DocumentModel model = callbacks.getDocumentModel();
        int row = entryTable.getSelectedRow();
        Account account = null;

        if (row >= 0) {
            Entry entry = model.getEntry(row);
            int accountId = entry.getAccountId();

            if (accountId >= 0) {
                account = registry.getAccountById(accountId);
            }
        }

        if (account == null) {
            Settings settings = registry.getSettings();
            int accountId = -1;

            try {
                accountId = Integer.parseInt(settings.getProperty("defaultAccount", ""));
            }
            catch (NumberFormatException e) {
            }

            account = registry.getAccountById(accountId);
        }

        AppSettings settings = AppSettings.getInstance();
        AccountStatementOptionsDialog dialog = new AccountStatementOptionsDialog(parentFrame, registry);
        dialog.create();
        dialog.setPeriod(registry.getPeriod());
        dialog.setDocumentDate(model.getDocument().getDate());
        dialog.setDateSelectionMode(1);
        dialog.setOrderByDate(settings.getString("sort-entries", "number").equals("date"));
        dialog.selectAccount(account);
        dialog.setVisible(true);

        if (dialog.getResult() == JOptionPane.OK_OPTION) {
            AccountStatementModel printModel = new AccountStatementModel();
            printModel.setDataSource(registry.getDataSource());
            printModel.setPeriod(registry.getPeriod());
            printModel.setSettings(registry.getSettings());
            printModel.setAccount(dialog.getSelectedAccount());
            printModel.setStartDate(dialog.getStartDate());
            printModel.setEndDate(dialog.getEndDate());
            printModel.setOrderBy(dialog.isOrderByDate() ? AccountStatementModel.ORDER_BY_DATE :
                AccountStatementModel.ORDER_BY_NUMBER);
            settings.set("sort-entries", dialog.isOrderByDate() ? "date" : "number");
            showPrintPreview(printModel, new AccountStatementPrint(printModel));
        }

        dialog.dispose();
    }
    
    /**
     * Näyttää tuloslaskelman esikatseluikkunassa.
     */
    public void showIncomeStatement(boolean detailed) {
        if (!callbacks.saveDocumentIfChanged()) {
            return;
        }

        FinancialStatementOptionsDialog dialog = new FinancialStatementOptionsDialog(
                registry, parentFrame, "Tuloslaskelma",
                FinancialStatementOptionsDialog.TYPE_INCOME_STATEMENT);

        try {
            dialog.fetchData();
        }
        catch (DataAccessException e) {
            String message = "Tietojen hakeminen epäonnistui";
            logger.log(Level.SEVERE, message, e);
            SwingUtils.showDataAccessErrorMessage(parentFrame, e, message);
            return;
        }

        dialog.create();
        dialog.setVisible(true);

        if (dialog.getStartDates() != null) {
            FinancialStatementModel printModel = new FinancialStatementModel(
                    detailed ? FinancialStatementModel.TYPE_INCOME_STATEMENT_DETAILED :
                        FinancialStatementModel.TYPE_INCOME_STATEMENT);
            printModel.setDataSource(registry.getDataSource());
            printModel.setSettings(registry.getSettings());
            printModel.setAccounts(registry.getAccounts());
            printModel.setStartDates(dialog.getStartDates());
            printModel.setEndDates(dialog.getEndDates());
            showPrintPreview(printModel, new FinancialStatementPrint(printModel));
        }
    }
    
    /**
     * Näyttää taseen esikatseluikkunassa.
     */
    public void showBalanceSheet(boolean detailed) {
        if (!callbacks.saveDocumentIfChanged()) {
            return;
        }

        FinancialStatementOptionsDialog dialog = new FinancialStatementOptionsDialog(
                registry, parentFrame, "Tase",
                FinancialStatementOptionsDialog.TYPE_BALANCE_SHEET);

        try {
            dialog.fetchData();
        }
        catch (DataAccessException e) {
            String message = "Tietojen hakeminen epäonnistui";
            logger.log(Level.SEVERE, message, e);
            SwingUtils.showDataAccessErrorMessage(parentFrame, e, message);
            return;
        }

        dialog.create();
        dialog.setVisible(true);

        if (dialog.getStartDates() != null) {
            FinancialStatementModel printModel = new FinancialStatementModel(
                    detailed ? FinancialStatementModel.TYPE_BALANCE_SHEET_DETAILED :
                        FinancialStatementModel.TYPE_BALANCE_SHEET);
            printModel.setDataSource(registry.getDataSource());
            printModel.setSettings(registry.getSettings());
            printModel.setAccounts(registry.getAccounts());
            printModel.setStartDates(dialog.getStartDates());
            printModel.setEndDates(dialog.getEndDates());
            printModel.setPageBreakEnabled(dialog.isPageBreakEnabled());
            showPrintPreview(printModel, new FinancialStatementPrint(printModel));
        }
    }
    
    /**
     * Näyttää päiväkirjan esikatseluikkunassa.
     */
    public void showGeneralJournal() {
        if (!callbacks.saveDocumentIfChanged()) {
            return;
        }

        AppSettings settings = AppSettings.getInstance();
        DocumentModel model = callbacks.getDocumentModel();
        GeneralLJOptionsDialog dialog = new GeneralLJOptionsDialog(parentFrame, "Päiväkirja");
        dialog.create();
        dialog.setPeriod(registry.getPeriod());
        dialog.setDocumentDate(model.getDocument().getDate());
        dialog.setDateSelectionMode(0);
        dialog.setOrderByDate(settings.getString("sort-entries", "number").equals("date"));
        dialog.setGroupByDocumentTypesEnabled(!registry.getDocumentTypes().isEmpty());
        dialog.setGroupByDocumentTypesSelected(settings.getBoolean("group-by-document-types", true));
        dialog.setTotalAmountVisible(settings.getBoolean("general-journal.total-amount-visible", true));
        dialog.setVisible(true);

        if (dialog.getResult() == JOptionPane.OK_OPTION) {
            GeneralJournalModel printModel = dialog.isGroupByDocumentTypesSelected() ?
                    new GeneralJournalModelT() : new GeneralJournalModel();
            printModel.setRegistry(registry);
            printModel.setPeriod(registry.getPeriod());
            printModel.setStartDate(dialog.getStartDate());
            printModel.setEndDate(dialog.getEndDate());
            printModel.setOrderBy(dialog.isOrderByDate() ? GeneralJournalModel.ORDER_BY_DATE :
                GeneralJournalModel.ORDER_BY_NUMBER);
            printModel.setTotalAmountVisible(dialog.isTotalAmountVisible());
            settings.set("sort-entries", dialog.isOrderByDate() ? "date" : "number");
            settings.set("group-by-document-types", dialog.isGroupByDocumentTypesSelected());
            settings.set("general-journal.total-amount-visible", dialog.isTotalAmountVisible());
            showPrintPreview(printModel, new GeneralJournalPrint(printModel));
        }

        dialog.dispose();
    }
    
    /**
     * Näyttää pääkirjan esikatseluikkunassa.
     */
    public void showGeneralLedger() {
        if (!callbacks.saveDocumentIfChanged()) {
            return;
        }

        AppSettings settings = AppSettings.getInstance();
        DocumentModel model = callbacks.getDocumentModel();
        GeneralLJOptionsDialog dialog = new GeneralLJOptionsDialog(parentFrame, "Pääkirja");
        dialog.create();
        dialog.setPeriod(registry.getPeriod());
        dialog.setDocumentDate(model.getDocument().getDate());
        dialog.setDateSelectionMode(0);
        dialog.setOrderByDate(settings.getString("sort-entries", "number").equals("date"));
        dialog.setGroupByDocumentTypesEnabled(!registry.getDocumentTypes().isEmpty());
        dialog.setGroupByDocumentTypesSelected(settings.getBoolean("group-by-document-types", true));
        dialog.setTotalAmountVisible(settings.getBoolean("general-ledger.total-amount-visible", true));
        dialog.setVisible(true);

        if (dialog.getResult() == JOptionPane.OK_OPTION) {
            GeneralLedgerModel printModel = dialog.isGroupByDocumentTypesSelected() ?
                    new GeneralLedgerModelT() : new GeneralLedgerModel();
            printModel.setRegistry(registry);
            printModel.setPeriod(registry.getPeriod());
            printModel.setStartDate(dialog.getStartDate());
            printModel.setEndDate(dialog.getEndDate());
            printModel.setOrderBy(dialog.isOrderByDate() ? GeneralLedgerModel.ORDER_BY_DATE :
                GeneralLedgerModel.ORDER_BY_NUMBER);
            printModel.setTotalAmountVisible(dialog.isTotalAmountVisible());
            settings.set("sort-entries", dialog.isOrderByDate() ? "date" : "number");
            settings.set("group-by-document-types", dialog.isGroupByDocumentTypesSelected());
            settings.set("general-ledger.total-amount-visible", dialog.isTotalAmountVisible());
            showPrintPreview(printModel, new GeneralLedgerPrint(printModel));
        }

        dialog.dispose();
    }
    
    /**
     * Näyttää ALV-laskelman esikatseluikkunassa.
     */
    public void showVATReport() {
        if (!callbacks.saveDocumentIfChanged()) {
            return;
        }

        DocumentModel model = callbacks.getDocumentModel();
        PrintOptionsDialog dialog = new PrintOptionsDialog(parentFrame, "ALV-laskelma");
        dialog.create();
        dialog.setPeriod(registry.getPeriod());
        dialog.setDocumentDate(model.getDocument().getDate());
        dialog.setDateSelectionMode(1);
        dialog.setVisible(true);

        if (dialog.getResult() == JOptionPane.OK_OPTION) {
            VATReportModel printModel = new VATReportModel();
            printModel.setDataSource(registry.getDataSource());
            printModel.setPeriod(registry.getPeriod());
            printModel.setSettings(registry.getSettings());
            printModel.setAccounts(registry.getAccounts());
            printModel.setStartDate(dialog.getStartDate());
            printModel.setEndDate(dialog.getEndDate());
            showPrintPreview(printModel, new VATReportPrint(printModel));
        }

        dialog.dispose();
    }
    
    /**
     * Näyttää tilikartan esikatseluikkunassa.
     */
    public void showChartOfAccountsPrint(int mode) {
        COAPrintModel printModel = new COAPrintModel();
        printModel.setRegistry(registry);
        printModel.setMode(mode);

        try {
            printModel.run();
        }
        catch (DataAccessException e) {
            String message = "Tulosteen luominen epäonnistui";
            logger.log(Level.SEVERE, message, e);
            SwingUtils.showDataAccessErrorMessage(parentFrame, e, message);
            return;
        }

        showPrintPreview(printModel, new COAPrint(printModel));
    }
}

