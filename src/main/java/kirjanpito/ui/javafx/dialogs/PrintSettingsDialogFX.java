package kirjanpito.ui.javafx.dialogs;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import kirjanpito.util.AppSettings;

/**
 * JavaFX tulostusasetukset-dialogi.
 */
public class PrintSettingsDialogFX {
    
    private Stage dialog;
    private ComboBox<String> paperSizeCombo;
    private ComboBox<String> orientationCombo;
    private Spinner<Integer> topMarginSpinner;
    private Spinner<Integer> bottomMarginSpinner;
    private Spinner<Integer> leftMarginSpinner;
    private Spinner<Integer> rightMarginSpinner;
    private CheckBox showGridLinesCheck;
    private CheckBox showPageNumbersCheck;
    
    private AppSettings settings;
    
    public PrintSettingsDialogFX(Window owner) {
        settings = AppSettings.getInstance();
        
        dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(owner);
        dialog.setTitle("Tulostusasetukset");
        dialog.setResizable(false);
        
        createContent();
    }
    
    private void createContent() {
        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #ffffff;");
        
        // Paper settings
        TitledPane paperPane = new TitledPane();
        paperPane.setText("Paperi");
        paperPane.setCollapsible(false);
        
        GridPane paperGrid = new GridPane();
        paperGrid.setHgap(15);
        paperGrid.setVgap(10);
        paperGrid.setPadding(new Insets(10));
        
        paperGrid.add(new Label("Paperikoko:"), 0, 0);
        paperSizeCombo = new ComboBox<>();
        paperSizeCombo.getItems().addAll("A4", "A5", "Letter", "Legal");
        paperSizeCombo.setValue(settings.getString("print.paperSize", "A4"));
        paperSizeCombo.setPrefWidth(150);
        paperGrid.add(paperSizeCombo, 1, 0);
        
        paperGrid.add(new Label("Suunta:"), 0, 1);
        orientationCombo = new ComboBox<>();
        orientationCombo.getItems().addAll("Pysty", "Vaaka");
        orientationCombo.setValue(settings.getString("print.orientation", "Pysty"));
        orientationCombo.setPrefWidth(150);
        paperGrid.add(orientationCombo, 1, 1);
        
        paperPane.setContent(paperGrid);
        
        // Margins
        TitledPane marginsPane = new TitledPane();
        marginsPane.setText("Marginaalit (mm)");
        marginsPane.setCollapsible(false);
        
        GridPane marginGrid = new GridPane();
        marginGrid.setHgap(15);
        marginGrid.setVgap(10);
        marginGrid.setPadding(new Insets(10));
        
        marginGrid.add(new Label("Ylä:"), 0, 0);
        topMarginSpinner = new Spinner<>(0, 50, settings.getInt("print.marginTop", 15));
        topMarginSpinner.setPrefWidth(80);
        marginGrid.add(topMarginSpinner, 1, 0);
        
        marginGrid.add(new Label("Ala:"), 2, 0);
        bottomMarginSpinner = new Spinner<>(0, 50, settings.getInt("print.marginBottom", 15));
        bottomMarginSpinner.setPrefWidth(80);
        marginGrid.add(bottomMarginSpinner, 3, 0);
        
        marginGrid.add(new Label("Vasen:"), 0, 1);
        leftMarginSpinner = new Spinner<>(0, 50, settings.getInt("print.marginLeft", 15));
        leftMarginSpinner.setPrefWidth(80);
        marginGrid.add(leftMarginSpinner, 1, 1);
        
        marginGrid.add(new Label("Oikea:"), 2, 1);
        rightMarginSpinner = new Spinner<>(0, 50, settings.getInt("print.marginRight", 15));
        rightMarginSpinner.setPrefWidth(80);
        marginGrid.add(rightMarginSpinner, 3, 1);
        
        marginsPane.setContent(marginGrid);
        
        // Options
        TitledPane optionsPane = new TitledPane();
        optionsPane.setText("Lisäasetukset");
        optionsPane.setCollapsible(false);
        
        VBox optionsBox = new VBox(8);
        optionsBox.setPadding(new Insets(10));
        
        showGridLinesCheck = new CheckBox("Näytä ruudukko");
        showGridLinesCheck.setSelected(settings.getBoolean("print.showGridLines", true));
        
        showPageNumbersCheck = new CheckBox("Näytä sivunumerot");
        showPageNumbersCheck.setSelected(settings.getBoolean("print.showPageNumbers", true));
        
        optionsBox.getChildren().addAll(showGridLinesCheck, showPageNumbersCheck);
        optionsPane.setContent(optionsBox);
        
        // Buttons
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.setPadding(new Insets(10, 0, 0, 0));
        
        Button saveBtn = new Button("Tallenna");
        saveBtn.setDefaultButton(true);
        saveBtn.setStyle("-fx-background-color: #2563eb; -fx-text-fill: white;");
        saveBtn.setOnAction(e -> {
            save();
            dialog.close();
        });
        
        Button cancelBtn = new Button("Peruuta");
        cancelBtn.setCancelButton(true);
        cancelBtn.setOnAction(e -> dialog.close());
        
        buttonBox.getChildren().addAll(cancelBtn, saveBtn);
        
        root.getChildren().addAll(paperPane, marginsPane, optionsPane, buttonBox);
        
        Scene scene = new Scene(root, 400, 380);
        dialog.setScene(scene);
    }
    
    private void save() {
        settings.set("print.paperSize", paperSizeCombo.getValue());
        settings.set("print.orientation", orientationCombo.getValue());
        settings.set("print.marginTop", topMarginSpinner.getValue());
        settings.set("print.marginBottom", bottomMarginSpinner.getValue());
        settings.set("print.marginLeft", leftMarginSpinner.getValue());
        settings.set("print.marginRight", rightMarginSpinner.getValue());
        settings.set("print.showGridLines", showGridLinesCheck.isSelected());
        settings.set("print.showPageNumbers", showPageNumbersCheck.isSelected());
        settings.save();
    }
    
    public void show() {
        dialog.showAndWait();
    }
}
