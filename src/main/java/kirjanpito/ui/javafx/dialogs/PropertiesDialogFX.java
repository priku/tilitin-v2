package kirjanpito.ui.javafx.dialogs;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.StringConverter;

import kirjanpito.db.DataAccessException;
import kirjanpito.db.Period;
import kirjanpito.db.Settings;
import kirjanpito.models.PropertiesModel;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * JavaFX-dialogi perustietojen (yritys, tilikaudet) muokkaamiseen.
 * Korvaa Swing-toteutuksen PropertiesDialog.java.
 */
public class PropertiesDialogFX {

    private Stage dialog;
    private PropertiesModel model;
    
    private TextField nameTextField;
    private TextField businessIdTextField;
    private TableView<PeriodRow> periodTable;
    private ObservableList<PeriodRow> periodRows;
    
    private boolean saved = false;
    
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    public PropertiesDialogFX(Window owner, PropertiesModel model) {
        this.model = model;
        
        dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(owner);
        dialog.setTitle("Perustiedot");
        dialog.setMinWidth(500);
        dialog.setMinHeight(450);
        dialog.setWidth(550);
        dialog.setHeight(500);
        
        createContent();
        loadData();
    }
    
    private void createContent() {
        VBox root = new VBox(16);
        root.setPadding(new Insets(16));
        root.setStyle("-fx-background-color: #ffffff;");
        
        // === Yrityksen tiedot ===
        Label companyLabel = new Label("Yrityksen tiedot");
        companyLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        
        GridPane companyGrid = new GridPane();
        companyGrid.setHgap(10);
        companyGrid.setVgap(10);
        companyGrid.setPadding(new Insets(10));
        companyGrid.setStyle("-fx-border-color: #e0e0e0; -fx-border-radius: 4; -fx-background-color: #f9fafb; -fx-background-radius: 4;");
        
        Label nameLabel = new Label("Nimi:");
        nameTextField = new TextField();
        nameTextField.setPrefWidth(300);
        nameTextField.setPromptText("Yrityksen nimi");
        
        Label businessIdLabel = new Label("Y-tunnus:");
        businessIdTextField = new TextField();
        businessIdTextField.setPrefWidth(150);
        businessIdTextField.setPromptText("1234567-8");
        
        companyGrid.add(nameLabel, 0, 0);
        companyGrid.add(nameTextField, 1, 0);
        companyGrid.add(businessIdLabel, 0, 1);
        companyGrid.add(businessIdTextField, 1, 1);
        
        GridPane.setHgrow(nameTextField, Priority.ALWAYS);
        
        // === Tilikaudet ===
        Label periodsLabel = new Label("Tilikaudet");
        periodsLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        
        VBox periodsBox = new VBox(10);
        periodsBox.setPadding(new Insets(10));
        periodsBox.setStyle("-fx-border-color: #e0e0e0; -fx-border-radius: 4; -fx-background-color: #f9fafb; -fx-background-radius: 4;");
        VBox.setVgrow(periodsBox, Priority.ALWAYS);
        
        // Taulukko tilikausille
        periodRows = FXCollections.observableArrayList();
        periodTable = new TableView<>(periodRows);
        periodTable.setEditable(true);
        periodTable.setPrefHeight(200);
        VBox.setVgrow(periodTable, Priority.ALWAYS);
        
        // Sarakkeet
        TableColumn<PeriodRow, Integer> numCol = new TableColumn<>("#");
        numCol.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getNumber()).asObject());
        numCol.setPrefWidth(40);
        numCol.setEditable(false);
        
        TableColumn<PeriodRow, String> startCol = new TableColumn<>("Alkaa");
        startCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStartDateString()));
        startCol.setCellFactory(TextFieldTableCell.forTableColumn());
        startCol.setOnEditCommit(event -> {
            PeriodRow row = event.getRowValue();
            row.setStartDateString(event.getNewValue());
        });
        startCol.setPrefWidth(150);
        startCol.setEditable(true);
        
        TableColumn<PeriodRow, String> endCol = new TableColumn<>("Päättyy");
        endCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getEndDateString()));
        endCol.setCellFactory(TextFieldTableCell.forTableColumn());
        endCol.setOnEditCommit(event -> {
            PeriodRow row = event.getRowValue();
            row.setEndDateString(event.getNewValue());
        });
        endCol.setPrefWidth(150);
        endCol.setEditable(true);
        
        TableColumn<PeriodRow, String> statusCol = new TableColumn<>("Tila");
        statusCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatus()));
        statusCol.setPrefWidth(100);
        statusCol.setEditable(false);
        
        periodTable.getColumns().addAll(numCol, startCol, endCol, statusCol);
        
        // Painikkeet tilikausille
        HBox periodButtons = new HBox(10);
        periodButtons.setAlignment(Pos.CENTER_LEFT);
        
        Button newPeriodBtn = new Button("Uusi tilikausi");
        newPeriodBtn.setOnAction(e -> createPeriod());
        
        Button deletePeriodBtn = new Button("Poista tilikausi");
        deletePeriodBtn.setOnAction(e -> deletePeriod());
        
        periodButtons.getChildren().addAll(newPeriodBtn, deletePeriodBtn);
        
        periodsBox.getChildren().addAll(periodTable, periodButtons);
        
        // === Painikkeet ===
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.setPadding(new Insets(10, 0, 0, 0));
        
        Button cancelBtn = new Button("Peruuta");
        cancelBtn.setPrefWidth(100);
        cancelBtn.setOnAction(e -> dialog.close());
        
        Button okBtn = new Button("OK");
        okBtn.setPrefWidth(100);
        okBtn.setDefaultButton(true);
        okBtn.setOnAction(e -> save());
        
        buttonBox.getChildren().addAll(cancelBtn, okBtn);
        
        // Kokoa layout
        root.getChildren().addAll(
            companyLabel, companyGrid,
            periodsLabel, periodsBox,
            buttonBox
        );
        
        Scene scene = new Scene(root);
        dialog.setScene(scene);
    }
    
    private void loadData() {
        // Lataa yrityksen tiedot
        Settings settings = model.getSettings();
        nameTextField.setText(settings.getName() != null ? settings.getName() : "");
        businessIdTextField.setText(settings.getBusinessId() != null ? settings.getBusinessId() : "");
        
        // Lataa tilikaudet
        periodRows.clear();
        for (int i = 0; i < model.getPeriodCount(); i++) {
            Period period = model.getPeriod(i);
            boolean isCurrent = (i == model.getCurrentPeriodIndex());
            periodRows.add(new PeriodRow(i + 1, period, isCurrent));
        }
        
        // Valitse nykyinen tilikausi
        if (model.getCurrentPeriodIndex() >= 0 && model.getCurrentPeriodIndex() < periodRows.size()) {
            periodTable.getSelectionModel().select(model.getCurrentPeriodIndex());
        }
    }
    
    private void createPeriod() {
        model.createPeriod();
        
        // Päivitä taulukko
        int newIndex = model.getPeriodCount() - 1;
        Period newPeriod = model.getPeriod(newIndex);
        periodRows.add(new PeriodRow(newIndex + 1, newPeriod, false));
        
        // Valitse uusi tilikausi
        periodTable.getSelectionModel().select(newIndex);
        periodTable.scrollTo(newIndex);
    }
    
    private void deletePeriod() {
        int selectedIndex = periodTable.getSelectionModel().getSelectedIndex();
        
        if (selectedIndex < 0) {
            showError("Valitse ensin poistettava tilikausi");
            return;
        }
        
        if (model.getPeriodCount() <= 1) {
            showError("Tilikautta ei voi poistaa, jos tietokannassa on vain yksi tilikausi.");
            return;
        }
        
        Period period = model.getPeriod(selectedIndex);
        String periodInfo = formatDate(period.getStartDate()) + " – " + formatDate(period.getEndDate());
        
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Poista tilikausi");
        confirm.setHeaderText("Haluatko varmasti poistaa tilikauden?");
        confirm.setContentText("Tilikausi: " + periodInfo + "\n\nTietoja ei voi palauttaa poistamisen jälkeen.");
        
        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                model.deletePeriod(selectedIndex);
                periodRows.remove(selectedIndex);
                
                // Päivitä rivinumerot
                for (int i = 0; i < periodRows.size(); i++) {
                    periodRows.get(i).setNumber(i + 1);
                }
                periodTable.refresh();
                
            } catch (DataAccessException e) {
                showError("Tilikauden poistaminen epäonnistui: " + e.getMessage());
            }
        }
    }
    
    private void save() {
        // Päivitä yrityksen tiedot
        Settings settings = model.getSettings();
        settings.setName(nameTextField.getText().trim());
        settings.setBusinessId(businessIdTextField.getText().trim());
        
        // Päivitä tilikaudet malliin
        for (int i = 0; i < periodRows.size(); i++) {
            PeriodRow row = periodRows.get(i);
            Period period = model.getPeriod(i);
            
            // Päivitä päivämäärät
            Date startDate = row.getStartDate();
            Date endDate = row.getEndDate();
            
            if (startDate == null) {
                showError("Syötä tilikauden " + (i + 1) + " alkamispäivämäärä.");
                return;
            }
            
            if (endDate == null) {
                showError("Syötä tilikauden " + (i + 1) + " päättymispäivämäärä.");
                return;
            }
            
            if (endDate.before(startDate)) {
                showError("Tilikauden " + (i + 1) + " päättymispäivämäärän on oltava alkamispäivämäärän jälkeen.");
                return;
            }
            
            period.setStartDate(startDate);
            period.setEndDate(endDate);
            model.updatePeriod(i);
        }
        
        // Päivitä nykyinen tilikausi valinnan mukaan
        int selectedIndex = periodTable.getSelectionModel().getSelectedIndex();
        if (selectedIndex >= 0) {
            model.setCurrentPeriodIndex(selectedIndex);
        }
        
        // Tallenna
        try {
            model.save();
            saved = true;
            dialog.close();
        } catch (DataAccessException e) {
            showError("Tallentaminen epäonnistui: " + e.getMessage());
        }
    }
    
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Virhe");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private String formatDate(Date date) {
        if (date == null) return "";
        return DATE_FORMAT.format(date);
    }
    
    public boolean isSaved() {
        return saved;
    }
    
    public void show() {
        dialog.showAndWait();
    }
    
    // === Sisäinen luokka tilikausirivien hallintaan ===
    
    public static class PeriodRow {
        private int number;
        private Period period;
        private boolean current;
        private Date startDate;
        private Date endDate;
        
        public PeriodRow(int number, Period period, boolean current) {
            this.number = number;
            this.period = period;
            this.current = current;
            this.startDate = period.getStartDate();
            this.endDate = period.getEndDate();
        }
        
        public int getNumber() { return number; }
        public void setNumber(int number) { this.number = number; }
        
        public Period getPeriod() { return period; }
        
        public Date getStartDate() { return startDate; }
        public Date getEndDate() { return endDate; }
        
        public String getStartDateString() {
            if (startDate == null) return "";
            return DATE_FORMAT.format(startDate);
        }
        
        public void setStartDateString(String value) {
            try {
                this.startDate = DATE_FORMAT.parse(value);
            } catch (ParseException e) {
                // Keep old value
            }
        }
        
        public String getEndDateString() {
            if (endDate == null) return "";
            return DATE_FORMAT.format(endDate);
        }
        
        public void setEndDateString(String value) {
            try {
                this.endDate = DATE_FORMAT.parse(value);
            } catch (ParseException e) {
                // Keep old value
            }
        }
        
        public String getStatus() {
            if (current) return "Nykyinen";
            if (period.isLocked()) return "Lukittu";
            return "";
        }
    }
}
