package kirjanpito.ui.javafx.dialogs;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import kirjanpito.db.DataAccessException;
import kirjanpito.models.ReportEditorModel;
import kirjanpito.reports.DrawCommandParser;
import kirjanpito.util.AppSettings;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.text.ParseException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * JavaFX-dialogi tulosteiden muokkaamiseen.
 * Korvaa Swing-toteutuksen ReportEditorDialog.java.
 */
public class ReportEditorDialogFX {

    private Stage dialog;
    private ReportEditorModel model;
    
    private TabPane tabPane;
    private TextArea[] reportTextAreas;
    private ComboBox<String> printComboBox;
    private TextArea headerTextArea;
    private TextArea footerTextArea;
    private int printIndex = 0;
    
    private static final String[] REPORT_NAMES = new String[] {
        "Tuloslaskelma", "Tuloslaskelma erittelyin",
        "Tase", "Tase erittelyin"
    };
    
    private static final String[] PRINT_NAMES = new String[] {
        "Tilien saldot", "Tosite", "Tiliote",
        "Tuloslaskelma", "Tuloslaskelma erittelyin",
        "Tase", "Tase erittelyin",
        "Päiväkirja", "Pääkirja",
        "ALV-laskelma tileittäin",
        "Tilikartta"
    };
    
    private static final Logger logger = Logger.getLogger("kirjanpito");

    public ReportEditorDialogFX(Window owner, ReportEditorModel model) {
        this.model = model;
        
        dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(owner);
        dialog.setTitle("Tulosteiden muokkaus");
        dialog.setWidth(700);
        dialog.setHeight(600);
        dialog.setMinWidth(500);
        dialog.setMinHeight(400);
        
        createContent();
        loadData();
    }
    
    private void createContent() {
        VBox root = new VBox();
        root.setStyle("-fx-background-color: #ffffff;");
        
        // TabPane
        tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        VBox.setVgrow(tabPane, Priority.ALWAYS);
        
        // Ylä- ja alatunnisteet -välilehti
        Tab headerTab = new Tab("Ylä- ja alatunnisteet");
        headerTab.setContent(createHeaderEditorTab());
        tabPane.getTabs().add(headerTab);
        
        // Raportit-välilehdet
        Font monoFont = Font.font("Monospaced", 12);
        reportTextAreas = new TextArea[REPORT_NAMES.length];
        
        for (int i = 0; i < REPORT_NAMES.length; i++) {
            Tab tab = new Tab(REPORT_NAMES[i]);
            TextArea textArea = new TextArea();
            textArea.setFont(monoFont);
            textArea.setWrapText(false);
            reportTextAreas[i] = textArea;
            tab.setContent(textArea);
            tabPane.getTabs().add(tab);
        }
        
        // Painikkeet
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.setPadding(new Insets(10));
        
        Button exportBtn = new Button("Vie");
        exportBtn.setPrefWidth(90);
        exportBtn.setOnAction(e -> saveToZip());
        
        Button importBtn = new Button("Tuo");
        importBtn.setPrefWidth(90);
        importBtn.setOnAction(e -> loadFromZip());
        
        Button helpBtn = new Button("Ohjeet");
        helpBtn.setPrefWidth(90);
        helpBtn.setOnAction(e -> showHelp());
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Button saveBtn = new Button("Tallenna");
        saveBtn.setPrefWidth(90);
        saveBtn.setDefaultButton(true);
        saveBtn.setOnAction(e -> save());
        
        Button cancelBtn = new Button("Peruuta");
        cancelBtn.setPrefWidth(90);
        cancelBtn.setOnAction(e -> dialog.close());
        
        buttonBox.getChildren().addAll(exportBtn, importBtn, helpBtn, spacer, saveBtn, cancelBtn);
        
        root.getChildren().addAll(tabPane, buttonBox);
        
        Scene scene = new Scene(root);
        dialog.setScene(scene);
    }
    
    private VBox createHeaderEditorTab() {
        VBox content = new VBox(10);
        content.setPadding(new Insets(10));
        
        // Tulosteen valinta
        HBox comboRow = new HBox(10);
        comboRow.setAlignment(Pos.CENTER_LEFT);
        
        Label comboLabel = new Label("Tuloste:");
        printComboBox = new ComboBox<>();
        printComboBox.getItems().addAll(PRINT_NAMES);
        printComboBox.getSelectionModel().selectFirst();
        printComboBox.setOnAction(e -> {
            saveHeaderAndFooter();
            loadHeaderAndFooter();
        });
        
        comboRow.getChildren().addAll(comboLabel, printComboBox);
        
        // Ylätunniste
        VBox headerBox = new VBox(5);
        VBox.setVgrow(headerBox, Priority.ALWAYS);
        
        HBox headerLabelRow = new HBox(10);
        headerLabelRow.setAlignment(Pos.CENTER_LEFT);
        Label headerLabel = new Label("Ylätunniste:");
        Region headerSpacer = new Region();
        HBox.setHgrow(headerSpacer, Priority.ALWAYS);
        Button restoreHeaderBtn = new Button("Palauta");
        restoreHeaderBtn.setOnAction(e -> headerTextArea.setText(model.getDefaultHeader(printIndex)));
        headerLabelRow.getChildren().addAll(headerLabel, headerSpacer, restoreHeaderBtn);
        
        headerTextArea = new TextArea();
        headerTextArea.setFont(Font.font("Monospaced", 12));
        headerTextArea.setWrapText(false);
        VBox.setVgrow(headerTextArea, Priority.ALWAYS);
        
        headerBox.getChildren().addAll(headerLabelRow, headerTextArea);
        
        // Alatunniste
        VBox footerBox = new VBox(5);
        VBox.setVgrow(footerBox, Priority.ALWAYS);
        
        HBox footerLabelRow = new HBox(10);
        footerLabelRow.setAlignment(Pos.CENTER_LEFT);
        Label footerLabel = new Label("Alatunniste:");
        Region footerSpacer = new Region();
        HBox.setHgrow(footerSpacer, Priority.ALWAYS);
        Button restoreFooterBtn = new Button("Palauta");
        restoreFooterBtn.setOnAction(e -> footerTextArea.setText(model.getDefaultFooter(printIndex)));
        footerLabelRow.getChildren().addAll(footerLabel, footerSpacer, restoreFooterBtn);
        
        footerTextArea = new TextArea();
        footerTextArea.setFont(Font.font("Monospaced", 12));
        footerTextArea.setWrapText(false);
        VBox.setVgrow(footerTextArea, Priority.ALWAYS);
        
        footerBox.getChildren().addAll(footerLabelRow, footerTextArea);
        
        content.getChildren().addAll(comboRow, headerBox, footerBox);
        VBox.setVgrow(content, Priority.ALWAYS);
        
        return content;
    }
    
    private void loadData() {
        // Lataa ylä- ja alatunnisteet
        loadHeaderAndFooter();
        
        // Lataa raportit
        int index = 0;
        for (String id : ReportEditorModel.REPORTS) {
            reportTextAreas[index].setText(model.getContent(id));
            reportTextAreas[index].positionCaret(0);
            index++;
        }
    }
    
    private void loadHeaderAndFooter() {
        printIndex = printComboBox.getSelectionModel().getSelectedIndex();
        headerTextArea.setText(model.getHeader(printIndex));
        footerTextArea.setText(model.getFooter(printIndex));
    }
    
    private void saveHeaderAndFooter() {
        model.setHeader(printIndex, headerTextArea.getText());
        model.setFooter(printIndex, footerTextArea.getText());
    }
    
    private void save() {
        saveHeaderAndFooter();
        DrawCommandParser parser = new DrawCommandParser();
        
        // Validoi ylä- ja alatunnisteet
        for (int i = 0; i < REPORT_NAMES.length; i++) {
            try {
                parser.parse(model.getHeader(i));
            } catch (ParseException e) {
                showError(String.format("Virhe tulosteen %s ylätunnisteessa rivillä %d:\n%s.",
                        REPORT_NAMES[i].toLowerCase(), e.getErrorOffset(), e.getMessage()));
                return;
            }
            
            try {
                parser.parse(model.getFooter(i));
            } catch (ParseException e) {
                showError(String.format("Virhe tulosteen %s alatunnisteessa rivillä %d:\n%s.",
                        REPORT_NAMES[i].toLowerCase(), e.getErrorOffset(), e.getMessage()));
                return;
            }
        }
        
        // Validoi ja tallenna raportit
        int index = 0;
        for (String id : ReportEditorModel.REPORTS) {
            try {
                model.parseContent(id, reportTextAreas[index].getText());
            } catch (ParseException e) {
                tabPane.getSelectionModel().select(index + 1);
                showError("Virhe rivillä " + e.getErrorOffset() + ": " + e.getMessage());
                return;
            }
            index++;
        }
        
        // Tallenna tietokantaan
        try {
            model.save();
            dialog.close();
        } catch (DataAccessException e) {
            logger.log(Level.SEVERE, "Tulostetietojen tallentaminen epäonnistui", e);
            showError("Tulostetietojen tallentaminen epäonnistui.");
        }
    }
    
    private void saveToZip() {
        saveHeaderAndFooter();
        
        AppSettings settings = AppSettings.getInstance();
        String path = settings.getString("print-settings-directory", ".");
        
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Vie tulosteasetukset");
        fileChooser.setInitialDirectory(new File(path));
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Tulosteasetukset", "*.zip")
        );
        
        File file = fileChooser.showSaveDialog(dialog);
        if (file != null) {
            settings.set("print-settings-directory", file.getParentFile().getAbsolutePath());
            
            try {
                if (!file.getName().endsWith(".zip")) {
                    file = new File(file.getAbsolutePath() + ".zip");
                }
                model.saveToZip(file);
                showInfo("Tulosteasetukset viety tiedostoon:\n" + file.getName());
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Tulosteasetusten vienti epäonnistui", e);
                showError("Tulosteasetusten vienti epäonnistui: " + e.getMessage());
            }
        }
    }
    
    private void loadFromZip() {
        saveHeaderAndFooter();
        
        AppSettings settings = AppSettings.getInstance();
        String path = settings.getString("print-settings-directory", ".");
        
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Tuo tulosteasetukset");
        fileChooser.setInitialDirectory(new File(path));
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Tulosteasetukset", "*.zip")
        );
        
        File file = fileChooser.showOpenDialog(dialog);
        if (file != null) {
            settings.set("print-settings-directory", file.getParentFile().getAbsolutePath());
            
            try {
                model.loadFromZip(file);
                
                // Päivitä UI
                loadHeaderAndFooter();
                int index = 0;
                for (String id : ReportEditorModel.REPORTS) {
                    reportTextAreas[index].setText(model.getContent(id));
                    reportTextAreas[index].positionCaret(0);
                    index++;
                }
                
                showInfo("Tulosteasetukset tuotu tiedostosta:\n" + file.getName());
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Tulosteasetusten tuonti epäonnistui", e);
                showError("Tulosteasetusten tuonti epäonnistui: " + e.getMessage());
            }
        }
    }
    
    private void showHelp() {
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(
                    new URI("http://helineva.net/tilitin/ohjeet/#tulosteiden-muokkaus"));
            }
        } catch (Exception e) {
            showError("Web-selaimen avaaminen epäonnistui.\n" +
                    "Ohjeet löytyvät osoitteesta:\n" +
                    "http://helineva.net/tilitin/ohjeet/#tulosteiden-muokkaus");
        }
    }
    
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Virhe");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Tiedot");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    public void show() {
        dialog.showAndWait();
    }
}
