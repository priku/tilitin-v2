package kirjanpito.ui.javafx.dialogs;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import kirjanpito.models.DataSourceInitializationModel;
import kirjanpito.models.DataSourceInitializationWorker;
import kirjanpito.util.Registry;

import java.util.Arrays;

/**
 * JavaFX-dialogi tilikarttamallin valintaan uutta tietokantaa luotaessa.
 * Korvaa Swing-toteutuksen DataSourceInitializationDialog.java.
 */
public class DataSourceInitializationDialogFX {

    private Stage dialog;
    private DataSourceInitializationModel model;
    private Registry registry;
    private ComboBox<String> comboBox;
    private DataSourceInitializationWorker worker;

    public DataSourceInitializationDialogFX(Window owner, Registry registry,
            DataSourceInitializationModel model) {
        this.registry = registry;
        this.model = model;

        dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(owner);
        dialog.setTitle("Tietokannan luonti");
        dialog.setResizable(false);
        
        createContent();
    }

    private void createContent() {
        VBox root = new VBox(16);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #ffffff;");
        root.setPrefWidth(400);
        
        // Otsikko
        Label titleLabel = new Label("Valitse tilikarttamalli");
        titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        
        // Selite
        Label infoLabel = new Label("Tilikartta määrittää kirjanpidon tilit ja niiden numerot. " +
                "Valitse sopiva malli yrityksellesi.");
        infoLabel.setWrapText(true);
        infoLabel.setStyle("-fx-text-fill: #666666;");
        
        // Tilikarttamallit
        HBox comboRow = new HBox(10);
        comboRow.setAlignment(Pos.CENTER_LEFT);
        
        Label comboLabel = new Label("Tilikarttamalli:");
        
        String[] modelNames = model.getModelNames();
        Arrays.sort(modelNames);
        
        comboBox = new ComboBox<>(FXCollections.observableArrayList(modelNames));
        comboBox.setPrefWidth(250);
        if (modelNames.length > 0) {
            comboBox.getSelectionModel().selectFirst();
        }
        
        comboRow.getChildren().addAll(comboLabel, comboBox);
        HBox.setHgrow(comboBox, Priority.ALWAYS);
        
        // Painikkeet
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.setPadding(new Insets(10, 0, 0, 0));
        
        Button cancelBtn = new Button("Peruuta");
        cancelBtn.setPrefWidth(100);
        cancelBtn.setOnAction(e -> dialog.close());
        
        Button okBtn = new Button("OK");
        okBtn.setPrefWidth(100);
        okBtn.setDefaultButton(true);
        okBtn.setOnAction(e -> createWorker());
        
        buttonBox.getChildren().addAll(cancelBtn, okBtn);
        
        root.getChildren().addAll(titleLabel, infoLabel, comboRow, buttonBox);
        
        Scene scene = new Scene(root);
        dialog.setScene(scene);
        dialog.sizeToScene();
    }

    private void createWorker() {
        int selectedIndex = comboBox.getSelectionModel().getSelectedIndex();
        
        if (selectedIndex < 0) {
            return;
        }
        
        String modelName = comboBox.getSelectionModel().getSelectedItem();
        worker = new DataSourceInitializationWorker(
                registry.getDataSource(), model, modelName);
        dialog.close();
        worker.execute();
    }

    /**
     * Palauttaa workerin, joka alustaa tietokannan.
     * 
     * @return worker tai null jos käyttäjä peruutti
     */
    public DataSourceInitializationWorker getWorker() {
        return worker;
    }

    /**
     * Näyttää dialogin ja odottaa sen sulkemista.
     */
    public void show() {
        dialog.showAndWait();
    }
}
