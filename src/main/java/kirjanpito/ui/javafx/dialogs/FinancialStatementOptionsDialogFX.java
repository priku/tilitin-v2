package kirjanpito.ui.javafx.dialogs;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import kirjanpito.db.Period;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * JavaFX dialog for financial statement (tuloslaskelma/tase) report options.
 * Supports up to 3 columns for period comparison.
 * Ports functionality from FinancialStatementOptionsDialog (Swing).
 */
public class FinancialStatementOptionsDialogFX {

    public static final int TYPE_INCOME_STATEMENT = 1;
    public static final int TYPE_BALANCE_SHEET = 2;
    private static final int NUM_COLUMNS = 3;

    private Stage dialog;
    private int type;
    private DatePicker[] startDatePickers;
    private DatePicker[] endDatePickers;
    private CheckBox pageBreakCheckBox;
    private List<Period> periods;

    private Date[] startDates;
    private Date[] endDates;
    private boolean okPressed = false;

    public FinancialStatementOptionsDialogFX(Window owner, String title, int type) {
        this.type = type;
        dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(owner);
        dialog.setTitle(title);
        dialog.setMinWidth(600);
        dialog.setMinHeight(type == TYPE_INCOME_STATEMENT ? 350 : 300);

        createContent();
    }

    private void createContent() {
        VBox root = new VBox(16);
        root.setPadding(new Insets(16));
        root.setStyle("-fx-background-color: #ffffff;");

        // Column headers
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(10));
        grid.setStyle("-fx-border-color: #e0e0e0; -fx-border-radius: 4; -fx-background-color: #f9fafb; -fx-background-radius: 4;");

        startDatePickers = new DatePicker[NUM_COLUMNS];
        endDatePickers = new DatePicker[NUM_COLUMNS];

        // Headers
        for (int i = 0; i < NUM_COLUMNS; i++) {
            Label colLabel = new Label("Sarake " + (i + 1));
            colLabel.setStyle("-fx-font-weight: bold;");
            grid.add(colLabel, i + 1, 0);
        }

        // Start date row (only for income statement)
        int row = 1;
        if (type == TYPE_INCOME_STATEMENT) {
            Label startLabel = new Label("Alkamispäivämäärä:");
            grid.add(startLabel, 0, row);

            for (int i = 0; i < NUM_COLUMNS; i++) {
                startDatePickers[i] = new DatePicker();
                startDatePickers[i].setPromptText("pp.kk.vvvv");
                startDatePickers[i].setPrefWidth(150);
                grid.add(startDatePickers[i], i + 1, row);

                // Auto-fill end date when start date is first day of month
                final int col = i;
                startDatePickers[i].valueProperty().addListener((obs, oldVal, newVal) -> {
                    if (newVal != null && newVal.getDayOfMonth() == 1) {
                        // Set end date to last day of same month
                        LocalDate endDate = newVal.withDayOfMonth(newVal.lengthOfMonth());
                        if (endDatePickers[col].getValue() == null ||
                            endDatePickers[col].getValue().isBefore(newVal)) {
                            endDatePickers[col].setValue(endDate);
                        }
                    }
                });
            }
            row++;
        }

        // End date row
        Label endLabel = new Label(type == TYPE_INCOME_STATEMENT ? "Päättymispäivämäärä:" : "Päivämäärä:");
        grid.add(endLabel, 0, row);

        for (int i = 0; i < NUM_COLUMNS; i++) {
            endDatePickers[i] = new DatePicker();
            endDatePickers[i].setPromptText("pp.kk.vvvv");
            endDatePickers[i].setPrefWidth(150);
            grid.add(endDatePickers[i], i + 1, row);
        }

        // Column constraints
        ColumnConstraints labelCol = new ColumnConstraints();
        labelCol.setMinWidth(150);
        grid.getColumnConstraints().add(labelCol);

        for (int i = 0; i < NUM_COLUMNS; i++) {
            ColumnConstraints dataCol = new ColumnConstraints();
            dataCol.setHgrow(Priority.SOMETIMES);
            grid.getColumnConstraints().add(dataCol);
        }

        // Page break checkbox (balance sheet only)
        HBox optionsBox = new HBox();
        if (type == TYPE_BALANCE_SHEET) {
            pageBreakCheckBox = new CheckBox("Vastattavaa eri sivulle");
            optionsBox.getChildren().add(pageBreakCheckBox);
        }

        // Buttons
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.setPadding(new Insets(10, 0, 0, 0));

        Button resetButton = new Button(periods != null && periods.size() > 1 ?
            "Nykyinen ja edellinen tilikausi" : "Koko tilikausi");
        resetButton.setStyle("-fx-min-width: 80;");
        resetButton.setOnAction(e -> reset());

        Button okButton = new Button("OK");
        okButton.setDefaultButton(true);
        okButton.setStyle("-fx-background-color: #2563eb; -fx-text-fill: white; -fx-min-width: 80;");
        okButton.setOnAction(e -> handleOK());

        Button cancelButton = new Button("Peruuta");
        cancelButton.setCancelButton(true);
        cancelButton.setStyle("-fx-min-width: 80;");
        cancelButton.setOnAction(e -> dialog.close());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        buttonBox.getChildren().addAll(resetButton, spacer, cancelButton, okButton);

        // Add all to root
        root.getChildren().addAll(grid, optionsBox, new Region(), buttonBox);
        VBox.setVgrow(root.getChildren().get(root.getChildren().size() - 2), Priority.ALWAYS);

        Scene scene = new Scene(root, 650, type == TYPE_INCOME_STATEMENT ? 350 : 300);
        dialog.setScene(scene);
    }

    private void handleOK() {
        // Count non-empty columns
        int numCols = 0;
        for (int i = 0; i < NUM_COLUMNS; i++) {
            if (type == TYPE_INCOME_STATEMENT) {
                if (startDatePickers[i].getValue() != null || endDatePickers[i].getValue() != null) {
                    numCols++;
                }
            } else {
                if (endDatePickers[i].getValue() != null) {
                    numCols++;
                }
            }
        }

        if (numCols == 0) {
            String message = type == TYPE_INCOME_STATEMENT ?
                "Syötä alkamis- ja päättymispäivämäärä" : "Syötä päivämäärä";
            showError("Virhe", message);
            return;
        }

        // Validate and collect dates
        List<Date> startList = new ArrayList<>();
        List<Date> endList = new ArrayList<>();

        for (int i = 0; i < NUM_COLUMNS; i++) {
            LocalDate start = type == TYPE_INCOME_STATEMENT ? startDatePickers[i].getValue() : null;
            LocalDate end = endDatePickers[i].getValue();

            // Skip empty columns
            if (type == TYPE_INCOME_STATEMENT && start == null && end == null) continue;
            if (type != TYPE_INCOME_STATEMENT && end == null) continue;

            // Validate income statement dates
            if (type == TYPE_INCOME_STATEMENT) {
                if (start == null) {
                    showError("Virhe", "Syötä alkamispäivämäärä sarakkeeseen " + (i + 1));
                    return;
                }
                if (end == null) {
                    showError("Virhe", "Syötä päättymispäivämäärä sarakkeeseen " + (i + 1));
                    return;
                }
                if (start.isAfter(end)) {
                    showError("Virhe", "Alkamispäivämäärä ei voi olla päättymispäivämäärän jälkeen (sarake " + (i + 1) + ")");
                    return;
                }
                startList.add(Date.from(start.atStartOfDay(ZoneId.systemDefault()).toInstant()));
            }

            endList.add(Date.from(end.atStartOfDay(ZoneId.systemDefault()).toInstant()));
        }

        startDates = type == TYPE_INCOME_STATEMENT ? startList.toArray(new Date[0]) : null;
        endDates = endList.toArray(new Date[0]);
        okPressed = true;
        dialog.close();
    }

    private void reset() {
        if (periods == null || periods.isEmpty()) return;

        // Fill column 1 with current period
        Period current = periods.get(periods.size() - 1);
        if (type == TYPE_INCOME_STATEMENT) {
            startDatePickers[0].setValue(current.getStartDate().toInstant()
                .atZone(ZoneId.systemDefault()).toLocalDate());
        }
        endDatePickers[0].setValue(current.getEndDate().toInstant()
            .atZone(ZoneId.systemDefault()).toLocalDate());

        // Fill column 2 with previous period if available
        if (periods.size() > 1) {
            Period previous = periods.get(periods.size() - 2);
            if (type == TYPE_INCOME_STATEMENT) {
                startDatePickers[1].setValue(previous.getStartDate().toInstant()
                    .atZone(ZoneId.systemDefault()).toLocalDate());
            }
            endDatePickers[1].setValue(previous.getEndDate().toInstant()
                .atZone(ZoneId.systemDefault()).toLocalDate());
        } else {
            // Clear column 2
            if (type == TYPE_INCOME_STATEMENT) {
                startDatePickers[1].setValue(null);
            }
            endDatePickers[1].setValue(null);
        }

        // Clear column 3
        if (type == TYPE_INCOME_STATEMENT) {
            startDatePickers[2].setValue(null);
        }
        endDatePickers[2].setValue(null);
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.initOwner(dialog);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Public API

    public void setPeriods(List<Period> periods) {
        this.periods = periods;
    }

    public Date[] getStartDates() {
        return startDates;
    }

    public Date[] getEndDates() {
        return endDates;
    }

    public boolean isPageBreakEnabled() {
        return pageBreakCheckBox != null && pageBreakCheckBox.isSelected();
    }

    public void setPageBreakEnabled(boolean enabled) {
        if (pageBreakCheckBox != null) {
            pageBreakCheckBox.setSelected(enabled);
        }
    }

    public boolean showAndWait() {
        dialog.showAndWait();
        return okPressed;
    }

    /**
     * Static factory method for income statement.
     */
    public static FinancialStatementOptionsDialogFX createIncomeStatement(Window owner, List<Period> periods) {
        FinancialStatementOptionsDialogFX dialog = new FinancialStatementOptionsDialogFX(
            owner, "Tuloslaskelma", TYPE_INCOME_STATEMENT);
        dialog.setPeriods(periods);
        return dialog;
    }

    /**
     * Static factory method for balance sheet.
     */
    public static FinancialStatementOptionsDialogFX createBalanceSheet(Window owner, List<Period> periods) {
        FinancialStatementOptionsDialogFX dialog = new FinancialStatementOptionsDialogFX(
            owner, "Tase", TYPE_BALANCE_SHEET);
        dialog.setPeriods(periods);
        return dialog;
    }
}
