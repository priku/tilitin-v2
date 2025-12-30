package kirjanpito.ui.javafx;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.math.BigDecimal;

/**
 * JavaFX-testiluokka - varmistaa että JavaFX toimii projektissa.
 * 
 * Käynnistä: gradlew run -PmainClass=kirjanpito.ui.javafx.JavaFXTest
 */
public class JavaFXTest extends Application {
    
    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Tilitin - JavaFX Test");
        
        // Menu Bar (macOS: siirtyy yläpalkkiin)
        MenuBar menuBar = new MenuBar();
        menuBar.setUseSystemMenuBar(true);  // macOS native menu bar!
        
        Menu fileMenu = new Menu("Tiedosto");
        fileMenu.getItems().addAll(
            new MenuItem("Uusi"),
            new MenuItem("Avaa"),
            new SeparatorMenuItem(),
            new MenuItem("Lopeta")
        );
        
        Menu editMenu = new Menu("Muokkaa");
        editMenu.getItems().addAll(
            new MenuItem("Kumoa"),
            new MenuItem("Kopioi"),
            new MenuItem("Liitä")
        );
        
        menuBar.getMenus().addAll(fileMenu, editMenu);
        
        // TableView - Entry-taulukon prototyyppi
        TableView<TestEntry> tableView = new TableView<>();
        
        TableColumn<TestEntry, Integer> numCol = new TableColumn<>("Nro");
        numCol.setCellValueFactory(new PropertyValueFactory<>("number"));
        numCol.setPrefWidth(50);
        
        TableColumn<TestEntry, String> accountCol = new TableColumn<>("Tili");
        accountCol.setCellValueFactory(new PropertyValueFactory<>("account"));
        accountCol.setPrefWidth(150);
        
        TableColumn<TestEntry, String> descCol = new TableColumn<>("Selite");
        descCol.setCellValueFactory(new PropertyValueFactory<>("description"));
        descCol.setPrefWidth(250);
        
        TableColumn<TestEntry, BigDecimal> debitCol = new TableColumn<>("Debet");
        debitCol.setCellValueFactory(new PropertyValueFactory<>("debit"));
        debitCol.setPrefWidth(100);
        
        TableColumn<TestEntry, BigDecimal> creditCol = new TableColumn<>("Kredit");
        creditCol.setCellValueFactory(new PropertyValueFactory<>("credit"));
        creditCol.setPrefWidth(100);
        
        tableView.getColumns().addAll(numCol, accountCol, descCol, debitCol, creditCol);
        
        // Testidataa
        ObservableList<TestEntry> data = FXCollections.observableArrayList(
            new TestEntry(1, "1910 Pankkitili", "Myyntilasku 001", new BigDecimal("1240.00"), BigDecimal.ZERO),
            new TestEntry(2, "3000 Myynti", "Myyntilasku 001", BigDecimal.ZERO, new BigDecimal("1000.00")),
            new TestEntry(3, "2939 ALV-velka", "Myyntilasku 001", BigDecimal.ZERO, new BigDecimal("240.00"))
        );
        tableView.setItems(data);
        
        // Status bar
        Label statusLabel = new Label("✅ JavaFX toimii! TableView, MenuBar ja macOS-tuki valmiina.");
        statusLabel.setPadding(new Insets(5));
        
        // Layout
        BorderPane root = new BorderPane();
        root.setTop(menuBar);
        root.setCenter(tableView);
        root.setBottom(statusLabel);
        
        Scene scene = new Scene(root, 800, 500);
        primaryStage.setScene(scene);
        primaryStage.show();
        
        System.out.println("✅ JavaFX käynnistyi onnistuneesti!");
        System.out.println("📋 TableView: " + tableView.getItems().size() + " riviä");
        System.out.println("🍎 useSystemMenuBar: " + menuBar.isUseSystemMenuBar());
    }
    
    public static void main(String[] args) {
        launch(args);
    }
    
    // Testidataluokka
    public static class TestEntry {
        private final int number;
        private final String account;
        private final String description;
        private final BigDecimal debit;
        private final BigDecimal credit;
        
        public TestEntry(int number, String account, String description, BigDecimal debit, BigDecimal credit) {
            this.number = number;
            this.account = account;
            this.description = description;
            this.debit = debit;
            this.credit = credit;
        }
        
        public int getNumber() { return number; }
        public String getAccount() { return account; }
        public String getDescription() { return description; }
        public BigDecimal getDebit() { return debit; }
        public BigDecimal getCredit() { return credit; }
    }
}
