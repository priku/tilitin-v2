package kirjanpito.ui.javafx.dialogs;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import kirjanpito.db.DataAccessException;
import kirjanpito.models.StatisticsModel;
import kirjanpito.util.AppSettings;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * JavaFX dialog for balance comparison statistics.
 * Shows account balances over weekly or monthly periods.
 * Ports functionality from BalanceComparisonDialog (Swing).
 */
public class BalanceComparisonDialogFX {

    private Stage dialog;
    private StatisticsModel model;
    private TableView<Object> table;
    private DatePicker startDatePicker;
    private DatePicker endDatePicker;
    private RadioButton weeklyRadio;
    private RadioButton monthlyRadio;
    private Button saveButton;

    private static final Logger logger = Logger.getLogger("Kirjanpito");

    public BalanceComparisonDialogFX(Window owner, StatisticsModel model) {
        this.model = model;
        dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(owner);
        dialog.setTitle("Tilien saldojen vertailu");
        dialog.setMinWidth(700);
        dialog.setMinHeight(500);

        createContent();
        initializeDates();
    }

    private void createContent() {
        VBox root = new VBox();
        root.setStyle("-fx-background-color: #ffffff;");

        // Toolbar
        HBox toolbar = createToolBar();

        // Options panel
        HBox optionsPanel = createOptionsPanel();

        // Table
        table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        VBox.setVgrow(table, Priority.ALWAYS);

        root.getChildren().addAll(toolbar, optionsPanel, table);

        Scene scene = new Scene(root, 800, 550);
        dialog.setScene(scene);
    }

    private HBox createToolBar() {
        HBox toolbar = new HBox(10);
        toolbar.setPadding(new Insets(10));
        toolbar.setStyle("-fx-background-color: #f5f5f5; -fx-border-color: #e0e0e0; -fx-border-width: 0 0 1 0;");

        Button updateButton = new Button("Päivitä");
        updateButton.setStyle("-fx-background-color: #2563eb; -fx-text-fill: white; -fx-min-width: 80;");
        updateButton.setOnAction(e -> updateTable());

        saveButton = new Button("Vie");
        saveButton.setStyle("-fx-min-width: 80;");
        saveButton.setDisable(true);
        saveButton.setOnAction(e -> save());

        Button closeButton = new Button("Sulje");
        closeButton.setStyle("-fx-min-width: 80;");
        closeButton.setOnAction(e -> dialog.close());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        toolbar.getChildren().addAll(updateButton, saveButton, spacer, closeButton);
        return toolbar;
    }

    private HBox createOptionsPanel() {
        HBox panel = new HBox(20);
        panel.setPadding(new Insets(15));
        panel.setAlignment(Pos.CENTER_LEFT);
        panel.setStyle("-fx-background-color: #fafafa; -fx-border-color: #e0e0e0; -fx-border-width: 0 0 1 0;");

        // Date range section
        VBox dateSection = new VBox(8);

        HBox startBox = new HBox(10);
        startBox.setAlignment(Pos.CENTER_LEFT);
        Label startLabel = new Label("Alkaa:");
        startLabel.setMinWidth(60);
        startDatePicker = new DatePicker();
        startDatePicker.setPromptText("pp.kk.vvvv");
        startDatePicker.setPrefWidth(150);
        startBox.getChildren().addAll(startLabel, startDatePicker);

        HBox endBox = new HBox(10);
        endBox.setAlignment(Pos.CENTER_LEFT);
        Label endLabel = new Label("Päättyy:");
        endLabel.setMinWidth(60);
        endDatePicker = new DatePicker();
        endDatePicker.setPromptText("pp.kk.vvvv");
        endDatePicker.setPrefWidth(150);
        endBox.getChildren().addAll(endLabel, endDatePicker);

        dateSection.getChildren().addAll(startBox, endBox);

        // Separator
        Separator separator = new Separator();
        separator.setOrientation(javafx.geometry.Orientation.VERTICAL);

        // Period type section
        VBox periodSection = new VBox(8);
        periodSection.setPadding(new Insets(0, 0, 0, 10));

        weeklyRadio = new RadioButton("Viikoittain");
        weeklyRadio.setSelected(true);
        monthlyRadio = new RadioButton("Kuukausittain");

        ToggleGroup periodGroup = new ToggleGroup();
        weeklyRadio.setToggleGroup(periodGroup);
        monthlyRadio.setToggleGroup(periodGroup);

        periodSection.getChildren().addAll(weeklyRadio, monthlyRadio);

        panel.getChildren().addAll(dateSection, separator, periodSection);
        return panel;
    }

    private void initializeDates() {
        Calendar cal = Calendar.getInstance();
        Date endDate = cal.getTime();
        endDatePicker.setValue(endDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());

        cal.setLenient(true);
        cal.add(Calendar.MONTH, -1);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        Date startDate = cal.getTime();
        startDatePicker.setValue(startDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
    }

    private void updateTable() {
        // Validate dates
        if (startDatePicker.getValue() == null || endDatePicker.getValue() == null) {
            showError("Virhe", "Valitse sekä alku- että loppupäivämäärä");
            return;
        }

        LocalDate start = startDatePicker.getValue();
        LocalDate end = endDatePicker.getValue();

        if (start.isAfter(end)) {
            showError("Virhe", "Alkupäivämäärä ei voi olla loppupäivämäärän jälkeen");
            return;
        }

        // Convert to Date
        Date startDate = Date.from(start.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date endDate = Date.from(end.atStartOfDay(ZoneId.systemDefault()).toInstant());

        try {
            model.setStartDate(startDate);
            model.setEndDate(endDate);

            if (monthlyRadio.isSelected()) {
                model.calculateMonthlyStatistics();
            } else {
                model.calculateWeeklyStatistics();
            }

            // Update table structure
            updateTableColumns();
            saveButton.setDisable(false);

        } catch (DataAccessException e) {
            String message = "Tositetietojen hakeminen epäonnistui";
            logger.log(Level.SEVERE, message, e);
            showError("Virhe", message + ": " + e.getMessage());
        }
    }

    private void updateTableColumns() {
        table.getColumns().clear();
        table.getItems().clear();

        // Note: This is a simplified version. The full implementation would need
        // to integrate with StatisticsModel properly and create dynamic columns
        // based on the period count. For now, we show a placeholder implementation.

        TableColumn<Object, String> accountColumn = new TableColumn<>("Tili");
        accountColumn.setPrefWidth(200);
        accountColumn.setCellValueFactory(new PropertyValueFactory<>("account"));
        table.getColumns().add(accountColumn);

        // Add dynamic period columns based on model.getPeriodCount()
        int periodCount = model.getPeriodCount();
        for (int i = 0; i < periodCount; i++) {
            TableColumn<Object, String> periodColumn = new TableColumn<>("Jakso " + (i + 1));
            periodColumn.setCellValueFactory(new PropertyValueFactory<>("period" + i));
            table.getColumns().add(periodColumn);
        }

        // Populate table data from model
        // This would need StatisticsTableModel integration
        table.refresh();
    }

    private void save() {
        AppSettings settings = AppSettings.getInstance();
        String path = settings.getString("csv-directory", ".");

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Tallenna CSV-tiedostona");
        fileChooser.setInitialDirectory(new File(path));
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("CSV-tiedostot", "*.csv")
        );

        File file = fileChooser.showSaveDialog(dialog);
        if (file != null) {
            settings.set("csv-directory", file.getParentFile().getAbsolutePath());

            try {
                model.save(file);
                showInfo("Tallennettu", "Tiedot tallennettu onnistuneesti");
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Tietojen tallentaminen epäonnistui", e);
                showError("Virhe", "Tietojen tallentaminen epäonnistui: " + e.getMessage());
            }
        }
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.initOwner(dialog);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.initOwner(dialog);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Public API

    public void show() {
        dialog.show();
    }

    public void showAndWait() {
        dialog.showAndWait();
    }

    /**
     * Static factory method.
     */
    public static BalanceComparisonDialogFX create(Window owner, StatisticsModel model) {
        return new BalanceComparisonDialogFX(owner, model);
    }
}
