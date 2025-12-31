package kirjanpito.ui.javafx;

import javafx.print.PrinterJob;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Window;

import kirjanpito.db.*;
import kirjanpito.reports.DocumentPrint;
import kirjanpito.reports.DocumentPrintModel;
import kirjanpito.util.Registry;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Apuluokka dokumenttien tulostamiseen JavaFX:ssä.
 */
public class PrintHelper {
    
    private static final DecimalFormat MONEY_FORMAT = new DecimalFormat("#,##0.00");
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy");
    
    /**
     * Tulostaa nykyisen tositteen.
     */
    public static void printDocument(Window owner, DataSource dataSource, Document document, 
                                     List<Entry> entries, List<Account> accounts, Registry registry) {
        if (document == null || entries == null || entries.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Tulostus");
            alert.setHeaderText("Ei tulostettavaa");
            alert.setContentText("Valitse tosite jolla on vientejä.");
            alert.showAndWait();
            return;
        }
        
        try {
            // Create print model
            DocumentPrintModel model = new DocumentPrintModel();
            model.setRegistry(registry);
            model.setDocument(document);
            model.run();
            
            // Create print
            DocumentPrint print = new DocumentPrint(model);
            
            // Generate text representation
            String text = generateDocumentText(document, entries, accounts);
            
            // Create printable node
            TextFlow textFlow = new TextFlow();
            Text textNode = new Text(text);
            textNode.setStyle("-fx-font-family: 'Courier New'; -fx-font-size: 10pt;");
            textFlow.getChildren().add(textNode);
            
            // Show print dialog
            PrinterJob job = PrinterJob.createPrinterJob();
            if (job != null && job.showPrintDialog(owner)) {
                boolean success = job.printPage(textFlow);
                if (success) {
                    job.endJob();
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Tulostus");
                    alert.setHeaderText(null);
                    alert.setContentText("Tosite tulostettu onnistuneesti.");
                    alert.showAndWait();
                } else {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Tulostusvirhe");
                    alert.setHeaderText(null);
                    alert.setContentText("Tulostus epäonnistui.");
                    alert.showAndWait();
                }
            }
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Tulostusvirhe");
            alert.setHeaderText(null);
            alert.setContentText("Virhe tulostettaessa tositetta: " + e.getMessage());
            alert.showAndWait();
        }
    }
    
    private static String generateDocumentText(Document document, List<Entry> entries, List<Account> accounts) {
        StringBuilder sb = new StringBuilder();
        
        sb.append("═".repeat(60)).append("\n");
        sb.append("TOSITE\n");
        sb.append("═".repeat(60)).append("\n");
        sb.append("Tositenumero: ").append(document.getNumber()).append("\n");
        sb.append("Päivämäärä: ").append(DATE_FORMAT.format(document.getDate())).append("\n");
        sb.append("\n");
        sb.append(String.format("%-8s %-30s %15s %15s%n", "Tili", "Selite", "Debet", "Kredit"));
        sb.append("─".repeat(60)).append("\n");
        
        BigDecimal totalDebit = BigDecimal.ZERO;
        BigDecimal totalCredit = BigDecimal.ZERO;
        
        for (Entry entry : entries) {
            Account account = findAccount(entry.getAccountId(), accounts);
            String accountStr = account != null ? account.getNumber() + " " + account.getName() : "";
            if (accountStr.length() > 38) {
                accountStr = accountStr.substring(0, 35) + "...";
            }
            
            String debitStr = "";
            String creditStr = "";
            
            if (entry.isDebit()) {
                debitStr = MONEY_FORMAT.format(entry.getAmount());
                totalDebit = totalDebit.add(entry.getAmount());
            } else {
                creditStr = MONEY_FORMAT.format(entry.getAmount());
                totalCredit = totalCredit.add(entry.getAmount());
            }
            
            sb.append(String.format("%-8s %-30s %15s %15s%n",
                account != null ? account.getNumber() : "",
                truncate(entry.getDescription(), 30),
                debitStr, creditStr));
        }
        
        sb.append("─".repeat(60)).append("\n");
        sb.append(String.format("%-39s %15s %15s%n", "YHTEENSÄ",
            MONEY_FORMAT.format(totalDebit), MONEY_FORMAT.format(totalCredit)));
        
        return sb.toString();
    }
    
    private static Account findAccount(int id, List<Account> accounts) {
        for (Account acc : accounts) {
            if (acc.getId() == id) return acc;
        }
        return null;
    }
    
    private static String truncate(String s, int max) {
        if (s == null) return "";
        return s.length() > max ? s.substring(0, max - 2) + ".." : s;
    }
}
