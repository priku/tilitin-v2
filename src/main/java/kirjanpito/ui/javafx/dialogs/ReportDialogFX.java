package kirjanpito.ui.javafx.dialogs;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.print.PrinterJob;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import kirjanpito.db.*;
import kirjanpito.models.DocumentModel;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * JavaFX raporttidialogi.
 * Näyttää päiväkirjan, pääkirjan, tuloslaskelman tai taseen.
 */
public class ReportDialogFX {
    
    public enum ReportType {
        JOURNAL("Päiväkirja"),
        LEDGER("Pääkirja"),
        INCOME_STATEMENT("Tuloslaskelma"),
        BALANCE_SHEET("Tase");
        
        private final String name;
        ReportType(String name) { this.name = name; }
        public String getName() { return name; }
    }
    
    private Stage dialog;
    private TextArea reportArea;
    private ReportType reportType;
    private DataSource dataSource;
    private Period period;
    private List<Account> accounts;
    
    private static final DecimalFormat MONEY_FORMAT = new DecimalFormat("#,##0.00");
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy");
    
    public ReportDialogFX(Window owner, ReportType type, DataSource dataSource, Period period, List<Account> accounts) {
        this.reportType = type;
        this.dataSource = dataSource;
        this.period = period;
        this.accounts = accounts;
        
        dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(owner);
        dialog.setTitle(type.getName());
        dialog.setMinWidth(800);
        dialog.setMinHeight(600);
        
        createContent();
        generateReport();
    }
    
    private void createContent() {
        VBox root = new VBox(10);
        root.setPadding(new Insets(16));
        root.setStyle("-fx-background-color: #ffffff;");
        
        // Toolbar
        HBox toolbar = new HBox(10);
        toolbar.setAlignment(Pos.CENTER_LEFT);
        
        Button printBtn = new Button("🖨 Tulosta");
        printBtn.setOnAction(e -> print());
        
        Button exportBtn = new Button("📄 Vie tiedostoon");
        exportBtn.setOnAction(e -> exportToFile());
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Button closeBtn = new Button("Sulje");
        closeBtn.setOnAction(e -> dialog.close());
        
        toolbar.getChildren().addAll(printBtn, exportBtn, spacer, closeBtn);
        
        // Report area
        reportArea = new TextArea();
        reportArea.setEditable(false);
        reportArea.setFont(Font.font("Consolas", FontWeight.NORMAL, 12));
        reportArea.setStyle("-fx-control-inner-background: #fafafa;");
        VBox.setVgrow(reportArea, Priority.ALWAYS);
        
        root.getChildren().addAll(toolbar, reportArea);
        
        Scene scene = new Scene(root, 850, 650);
        dialog.setScene(scene);
    }
    
    private void generateReport() {
        StringBuilder sb = new StringBuilder();
        
        // Header
        sb.append("═".repeat(70)).append("\n");
        sb.append(reportType.getName().toUpperCase()).append("\n");
        if (period != null) {
            sb.append("Tilikausi: ").append(DATE_FORMAT.format(period.getStartDate()))
              .append(" - ").append(DATE_FORMAT.format(period.getEndDate())).append("\n");
        }
        sb.append("Tulostettu: ").append(DATE_FORMAT.format(new Date())).append("\n");
        sb.append("═".repeat(70)).append("\n\n");
        
        try {
            switch (reportType) {
                case JOURNAL:
                    generateJournal(sb);
                    break;
                case LEDGER:
                    generateLedger(sb);
                    break;
                case INCOME_STATEMENT:
                    generateIncomeStatement(sb);
                    break;
                case BALANCE_SHEET:
                    generateBalanceSheet(sb);
                    break;
            }
        } catch (Exception e) {
            sb.append("Virhe raportin generoinnissa: ").append(e.getMessage());
        }
        
        reportArea.setText(sb.toString());
    }
    
    private void generateJournal(StringBuilder sb) throws DataAccessException {
        Session session = dataSource.openSession();
        try {
            DocumentDAO docDao = dataSource.getDocumentDAO(session);
            EntryDAO entryDao = dataSource.getEntryDAO(session);
            
            List<Document> docs = docDao.getByPeriodId(period.getId(), 1);
            
            sb.append(String.format("%-6s %-10s %-30s %15s %15s%n", 
                "Nro", "Pvm", "Tili / Selite", "Debet", "Kredit"));
            sb.append("─".repeat(70)).append("\n");
            
            BigDecimal totalDebit = BigDecimal.ZERO;
            BigDecimal totalCredit = BigDecimal.ZERO;
            
            for (Document doc : docs) {
                List<Entry> entries = entryDao.getByDocumentId(doc.getId());
                
                boolean first = true;
                for (Entry entry : entries) {
                    Account acc = findAccount(entry.getAccountId());
                    String accName = acc != null ? acc.getNumber() + " " + acc.getName() : "";
                    
                    String debitStr = "";
                    String creditStr = "";
                    
                    if (entry.isDebit()) {
                        debitStr = MONEY_FORMAT.format(entry.getAmount());
                        totalDebit = totalDebit.add(entry.getAmount());
                    } else {
                        creditStr = MONEY_FORMAT.format(entry.getAmount());
                        totalCredit = totalCredit.add(entry.getAmount());
                    }
                    
                    if (first) {
                        sb.append(String.format("%-6d %-10s %-30s %15s %15s%n",
                            doc.getNumber(),
                            DATE_FORMAT.format(doc.getDate()),
                            truncate(accName, 30),
                            debitStr, creditStr));
                        first = false;
                    } else {
                        sb.append(String.format("%-6s %-10s %-30s %15s %15s%n",
                            "", "",
                            truncate(accName, 30),
                            debitStr, creditStr));
                    }
                    
                    if (entry.getDescription() != null && !entry.getDescription().isEmpty()) {
                        sb.append(String.format("%-17s %-30s%n", "", "  " + truncate(entry.getDescription(), 30)));
                    }
                }
                sb.append("\n");
            }
            
            sb.append("─".repeat(70)).append("\n");
            sb.append(String.format("%-47s %15s %15s%n", "YHTEENSÄ", 
                MONEY_FORMAT.format(totalDebit), MONEY_FORMAT.format(totalCredit)));
            
        } finally {
            session.close();
        }
    }
    
    private void generateLedger(StringBuilder sb) throws DataAccessException {
        Session session = dataSource.openSession();
        try {
            EntryDAO entryDao = dataSource.getEntryDAO(session);
            
            // Group by account
            Map<Integer, List<Entry>> byAccount = new LinkedHashMap<>();
            Map<Integer, BigDecimal> balances = new HashMap<>();
            
            entryDao.getByPeriodId(period.getId(), EntryDAO.ORDER_BY_ACCOUNT_NUMBER_AND_DOCUMENT_NUMBER, 
                entry -> {
                    byAccount.computeIfAbsent(entry.getAccountId(), k -> new ArrayList<>()).add(entry);
                });
            
            DocumentDAO docDao = dataSource.getDocumentDAO(session);
            Map<Integer, Document> docsById = new HashMap<>();
            for (Document doc : docDao.getByPeriodId(period.getId(), 1)) {
                docsById.put(doc.getId(), doc);
            }
            
            for (Account acc : accounts) {
                List<Entry> entries = byAccount.get(acc.getId());
                if (entries == null || entries.isEmpty()) continue;
                
                sb.append("\n").append(acc.getNumber()).append(" ").append(acc.getName()).append("\n");
                sb.append("─".repeat(70)).append("\n");
                sb.append(String.format("%-6s %-10s %-30s %15s %15s%n",
                    "Nro", "Pvm", "Selite", "Debet", "Kredit"));
                
                BigDecimal balance = BigDecimal.ZERO;
                
                for (Entry entry : entries) {
                    Document doc = docsById.get(entry.getDocumentId());
                    String date = doc != null ? DATE_FORMAT.format(doc.getDate()) : "";
                    int docNum = doc != null ? doc.getNumber() : 0;
                    
                    String debitStr = "";
                    String creditStr = "";
                    
                    if (entry.isDebit()) {
                        debitStr = MONEY_FORMAT.format(entry.getAmount());
                        balance = balance.add(entry.getAmount());
                    } else {
                        creditStr = MONEY_FORMAT.format(entry.getAmount());
                        balance = balance.subtract(entry.getAmount());
                    }
                    
                    sb.append(String.format("%-6d %-10s %-30s %15s %15s%n",
                        docNum, date,
                        truncate(entry.getDescription(), 30),
                        debitStr, creditStr));
                }
                
                sb.append("─".repeat(70)).append("\n");
                sb.append(String.format("%-47s %23s%n", "Saldo:", MONEY_FORMAT.format(balance)));
                balances.put(acc.getId(), balance);
            }
            
        } finally {
            session.close();
        }
    }
    
    private void generateIncomeStatement(StringBuilder sb) throws DataAccessException {
        Map<Integer, BigDecimal> balances = calculateBalances();
        
        sb.append("TULOT\n");
        sb.append("─".repeat(50)).append("\n");
        
        BigDecimal totalIncome = BigDecimal.ZERO;
        for (Account acc : accounts) {
            if (acc.getType() == 3 && acc.getNumber().startsWith("3")) { // Income accounts
                BigDecimal bal = balances.getOrDefault(acc.getId(), BigDecimal.ZERO).negate();
                if (bal.compareTo(BigDecimal.ZERO) != 0) {
                    sb.append(String.format("%-40s %15s%n", 
                        acc.getNumber() + " " + truncate(acc.getName(), 35),
                        MONEY_FORMAT.format(bal)));
                    totalIncome = totalIncome.add(bal);
                }
            }
        }
        sb.append("─".repeat(50)).append("\n");
        sb.append(String.format("%-40s %15s%n", "TULOT YHTEENSÄ", MONEY_FORMAT.format(totalIncome)));
        
        sb.append("\nMENOT\n");
        sb.append("─".repeat(50)).append("\n");
        
        BigDecimal totalExpenses = BigDecimal.ZERO;
        for (Account acc : accounts) {
            if (acc.getType() == 3 && !acc.getNumber().startsWith("3")) { // Expense accounts
                BigDecimal bal = balances.getOrDefault(acc.getId(), BigDecimal.ZERO);
                if (bal.compareTo(BigDecimal.ZERO) != 0) {
                    sb.append(String.format("%-40s %15s%n",
                        acc.getNumber() + " " + truncate(acc.getName(), 35),
                        MONEY_FORMAT.format(bal)));
                    totalExpenses = totalExpenses.add(bal);
                }
            }
        }
        sb.append("─".repeat(50)).append("\n");
        sb.append(String.format("%-40s %15s%n", "MENOT YHTEENSÄ", MONEY_FORMAT.format(totalExpenses)));
        
        sb.append("\n").append("═".repeat(50)).append("\n");
        BigDecimal result = totalIncome.subtract(totalExpenses);
        sb.append(String.format("%-40s %15s%n", 
            result.compareTo(BigDecimal.ZERO) >= 0 ? "TILIKAUDEN VOITTO" : "TILIKAUDEN TAPPIO",
            MONEY_FORMAT.format(result.abs())));
    }
    
    private void generateBalanceSheet(StringBuilder sb) throws DataAccessException {
        Map<Integer, BigDecimal> balances = calculateBalances();
        
        sb.append("VASTAAVAA (Aktiva)\n");
        sb.append("─".repeat(50)).append("\n");
        
        BigDecimal totalAssets = BigDecimal.ZERO;
        for (Account acc : accounts) {
            if (acc.getType() == 1) { // Assets
                BigDecimal bal = balances.getOrDefault(acc.getId(), BigDecimal.ZERO);
                if (bal.compareTo(BigDecimal.ZERO) != 0) {
                    sb.append(String.format("%-40s %15s%n",
                        acc.getNumber() + " " + truncate(acc.getName(), 35),
                        MONEY_FORMAT.format(bal)));
                    totalAssets = totalAssets.add(bal);
                }
            }
        }
        sb.append("─".repeat(50)).append("\n");
        sb.append(String.format("%-40s %15s%n", "VASTAAVAA YHTEENSÄ", MONEY_FORMAT.format(totalAssets)));
        
        sb.append("\nVASTATTAVAA (Passiva)\n");
        sb.append("─".repeat(50)).append("\n");
        
        BigDecimal totalLiabilities = BigDecimal.ZERO;
        for (Account acc : accounts) {
            if (acc.getType() == 2 || acc.getType() == 4) { // Liabilities + Equity
                BigDecimal bal = balances.getOrDefault(acc.getId(), BigDecimal.ZERO).negate();
                if (bal.compareTo(BigDecimal.ZERO) != 0) {
                    sb.append(String.format("%-40s %15s%n",
                        acc.getNumber() + " " + truncate(acc.getName(), 35),
                        MONEY_FORMAT.format(bal)));
                    totalLiabilities = totalLiabilities.add(bal);
                }
            }
        }
        sb.append("─".repeat(50)).append("\n");
        sb.append(String.format("%-40s %15s%n", "VASTATTAVAA YHTEENSÄ", MONEY_FORMAT.format(totalLiabilities)));
    }
    
    private Map<Integer, BigDecimal> calculateBalances() throws DataAccessException {
        Map<Integer, BigDecimal> balances = new HashMap<>();
        
        Session session = dataSource.openSession();
        try {
            EntryDAO entryDao = dataSource.getEntryDAO(session);
            
            entryDao.getByPeriodId(period.getId(), EntryDAO.ORDER_BY_ACCOUNT_NUMBER_AND_DOCUMENT_NUMBER,
                entry -> {
                    BigDecimal current = balances.getOrDefault(entry.getAccountId(), BigDecimal.ZERO);
                    if (entry.isDebit()) {
                        balances.put(entry.getAccountId(), current.add(entry.getAmount()));
                    } else {
                        balances.put(entry.getAccountId(), current.subtract(entry.getAmount()));
                    }
                });
        } finally {
            session.close();
        }
        
        return balances;
    }
    
    private Account findAccount(int id) {
        for (Account acc : accounts) {
            if (acc.getId() == id) return acc;
        }
        return null;
    }
    
    private String truncate(String s, int max) {
        if (s == null) return "";
        return s.length() > max ? s.substring(0, max - 2) + ".." : s;
    }
    
    private void print() {
        PrinterJob job = PrinterJob.createPrinterJob();
        if (job != null && job.showPrintDialog(dialog)) {
            // Simple text print
            if (job.printPage(reportArea)) {
                job.endJob();
            }
        }
    }
    
    private void exportToFile() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Vie raportti");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Tekstitiedosto", "*.txt"));
        fc.setInitialFileName(reportType.getName().toLowerCase().replace(" ", "_") + ".txt");
        
        File file = fc.showSaveDialog(dialog);
        if (file != null) {
            try (PrintWriter pw = new PrintWriter(new FileWriter(file))) {
                pw.print(reportArea.getText());
                new Alert(Alert.AlertType.INFORMATION, "Raportti tallennettu: " + file.getName()).showAndWait();
            } catch (Exception e) {
                new Alert(Alert.AlertType.ERROR, "Virhe: " + e.getMessage()).showAndWait();
            }
        }
    }
    
    public void show() {
        dialog.showAndWait();
    }
}
