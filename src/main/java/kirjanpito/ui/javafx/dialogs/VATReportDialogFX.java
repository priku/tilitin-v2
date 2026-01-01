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
import java.util.Date;

/**
 * JavaFX dialog for VAT (ALV) report options.
 * Allows selection of period for VAT calculation and reporting.
 */
public class VATReportDialogFX {

    private Stage dialog;
    private DatePicker startDatePicker;
    private DatePicker endDatePicker;
    private RadioButton periodRadio;
    private RadioButton customRadio;
    private CheckBox detailedReportCheckBox;
    private CheckBox groupByAccountCheckBox;

    private Period period;
    private Date startDate;
    private Date endDate;
    private boolean okPressed = false;

    public VATReportDialogFX(Window owner) {
        dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(owner);
        dialog.setTitle("ALV-laskelma tileittäin");
        dialog.setMinWidth(500);
        dialog.setMinHeight(350);

        createContent();
    }

    private void createContent() {
        VBox root = new VBox(16);
        root.setPadding(new Insets(16));
        root.setStyle("-fx-background-color: #ffffff;");

        // Date selection section
        Label dateLabel = new Label("Aikaväli");
        dateLabel.setStyle("-fx-font-weight: bold;");

        VBox dateSection = new VBox(10);
        dateSection.setPadding(new Insets(10));
        dateSection.setStyle("-fx-border-color: #e0e0e0; -fx-border-radius: 4; -fx-background-color: #f9fafb; -fx-background-radius: 4;");

        // Period radio
        periodRadio = new RadioButton("Koko tilikausi");
        periodRadio.setSelected(true);

        // Custom date range radio
        customRadio = new RadioButton("Aikaväli:");

        ToggleGroup dateGroup = new ToggleGroup();
        periodRadio.setToggleGroup(dateGroup);
        customRadio.setToggleGroup(dateGroup);

        // Date pickers
        HBox datePickerBox = new HBox(10);
        datePickerBox.setAlignment(Pos.CENTER_LEFT);
        datePickerBox.setPadding(new Insets(0, 0, 0, 30));

        startDatePicker = new DatePicker();
        startDatePicker.setPromptText("Alkupvm");
        startDatePicker.setPrefWidth(150);
        startDatePicker.setDisable(true);

        Label toLabel = new Label("—");

        endDatePicker = new DatePicker();
        endDatePicker.setPromptText("Loppupvm");
        endDatePicker.setPrefWidth(150);
        endDatePicker.setDisable(true);

        datePickerBox.getChildren().addAll(startDatePicker, toLabel, endDatePicker);

        dateSection.getChildren().addAll(periodRadio, customRadio, datePickerBox);

        // Options section
        Label optionsLabel = new Label("Raportin asetukset");
        optionsLabel.setStyle("-fx-font-weight: bold;");

        VBox optionsSection = new VBox(10);
        optionsSection.setPadding(new Insets(10));
        optionsSection.setStyle("-fx-border-color: #e0e0e0; -fx-border-radius: 4; -fx-background-color: #f9fafb; -fx-background-radius: 4;");

        detailedReportCheckBox = new CheckBox("Yksityiskohtainen raportti");
        detailedReportCheckBox.setTooltip(new Tooltip("Näytä kaikki viennit ALV-tileittäin"));

        groupByAccountCheckBox = new CheckBox("Ryhmittele tileittäin");
        groupByAccountCheckBox.setSelected(true);
        groupByAccountCheckBox.setTooltip(new Tooltip("Ryhmittele ALV-viennit tileittäin"));

        optionsSection.getChildren().addAll(detailedReportCheckBox, groupByAccountCheckBox);

        // Info text
        Label infoLabel = new Label(
            "ALV-laskelma näyttää arvonlisäveroilmoitukseen tarvittavat tiedot.\n" +
            "Raportti sisältää ALV-tilit ja niiden saldot valitulta ajanjaksolta."
        );
        infoLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #666666;");
        infoLabel.setWrapText(true);

        // Buttons
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.setPadding(new Insets(10, 0, 0, 0));

        Button okButton = new Button("Luo raportti");
        okButton.setDefaultButton(true);
        okButton.setStyle("-fx-background-color: #2563eb; -fx-text-fill: white; -fx-min-width: 100;");
        okButton.setOnAction(e -> handleOK());

        Button cancelButton = new Button("Peruuta");
        cancelButton.setCancelButton(true);
        cancelButton.setStyle("-fx-min-width: 80;");
        cancelButton.setOnAction(e -> dialog.close());

        buttonBox.getChildren().addAll(cancelButton, okButton);

        // Add all sections to root
        root.getChildren().addAll(
            dateLabel, dateSection,
            optionsLabel, optionsSection,
            infoLabel,
            new Region(), // Spacer
            buttonBox
        );

        VBox.setVgrow(root.getChildren().get(root.getChildren().size() - 2), Priority.ALWAYS);

        // Event handlers
        setupEventHandlers();

        Scene scene = new Scene(root, 550, 450);
        dialog.setScene(scene);
    }

    private void setupEventHandlers() {
        // Enable/disable date pickers based on radio selection
        periodRadio.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                startDatePicker.setDisable(true);
                endDatePicker.setDisable(true);
            }
        });

        customRadio.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                startDatePicker.setDisable(false);
                endDatePicker.setDisable(false);
            }
        });

        // Detailed report disables grouping
        detailedReportCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                groupByAccountCheckBox.setSelected(false);
                groupByAccountCheckBox.setDisable(true);
            } else {
                groupByAccountCheckBox.setDisable(false);
            }
        });
    }

    private void handleOK() {
        // Validate dates if custom range selected
        if (customRadio.isSelected()) {
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

            // Convert LocalDate to Date
            startDate = Date.from(start.atStartOfDay(ZoneId.systemDefault()).toInstant());
            endDate = Date.from(end.atStartOfDay(ZoneId.systemDefault()).toInstant());
        } else if (period != null) {
            // Use period dates
            startDate = period.getStartDate();
            endDate = period.getEndDate();
        } else {
            showError("Virhe", "Tilikautta ei ole asetettu");
            return;
        }

        okPressed = true;
        dialog.close();
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

    public void setPeriod(Period period) {
        this.period = period;
        if (period != null) {
            // Update date pickers with period dates
            LocalDate start = period.getStartDate().toInstant()
                .atZone(ZoneId.systemDefault()).toLocalDate();
            LocalDate end = period.getEndDate().toInstant()
                .atZone(ZoneId.systemDefault()).toLocalDate();

            startDatePicker.setValue(start);
            endDatePicker.setValue(end);
        }
    }

    public Period getPeriod() {
        return period;
    }

    public Date getStartDate() {
        return startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public boolean isDetailedReport() {
        return detailedReportCheckBox.isSelected();
    }

    public void setDetailedReport(boolean detailed) {
        detailedReportCheckBox.setSelected(detailed);
    }

    public boolean isGroupByAccount() {
        return groupByAccountCheckBox.isSelected() && !detailedReportCheckBox.isSelected();
    }

    public void setGroupByAccount(boolean group) {
        if (!detailedReportCheckBox.isSelected()) {
            groupByAccountCheckBox.setSelected(group);
        }
    }

    public boolean showAndWait() {
        dialog.showAndWait();
        return okPressed;
    }

    /**
     * Static factory method.
     */
    public static VATReportDialogFX create(Window owner, Period period) {
        VATReportDialogFX dialog = new VATReportDialogFX(owner);
        dialog.setPeriod(period);
        return dialog;
    }
}
