package kirjanpito.ui.javafx.dialogs;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import kirjanpito.db.*;
import kirjanpito.util.Registry;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * JavaFX-dialogi tositenumeroiden muuttamiselle.
 * Siirtää tositenumeroita ylös tai alas valitulla välillä.
 */
public class DocumentNumberShiftDialogFX {

    private final Stage dialog;
    private final Registry registry;
    
    private Spinner<Integer> startSpinner;
    private Spinner<Integer> endSpinner;
    private Spinner<Integer> shiftSpinner;
    private TextFlow previewTextFlow;
    private Button okButton;
    
    private List<Document> documents;
    private Set<Integer> numberSet;
    private boolean accepted = false;

    public DocumentNumberShiftDialogFX(Window owner, Registry registry) {
        this.registry = registry;
        
        dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(owner);
        dialog.setTitle("Muuta tositenumeroita");
        dialog.setMinWidth(450);
        dialog.setMinHeight(350);
        
        createContent();
    }

    private void createContent() {
        VBox root = new VBox(16);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #ffffff;");

        // Header
        Label headerLabel = new Label("Muuta tositenumeroita");
        headerLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        
        Label descLabel = new Label("Valitse numeroväli ja muutos:");
        descLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666666;");

        // Spinners grid
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(10);
        
        // Start number
        Label startLabel = new Label("Alkaa:");
        startSpinner = new Spinner<>(1, 999999, 1);
        startSpinner.setEditable(true);
        startSpinner.setPrefWidth(120);
        startSpinner.valueProperty().addListener((obs, oldVal, newVal) -> updatePreview());
        
        grid.add(startLabel, 0, 0);
        grid.add(startSpinner, 1, 0);
        
        // End number
        Label endLabel = new Label("Päättyy:");
        endSpinner = new Spinner<>(1, 999999, 1);
        endSpinner.setEditable(true);
        endSpinner.setPrefWidth(120);
        endSpinner.valueProperty().addListener((obs, oldVal, newVal) -> updatePreview());
        
        grid.add(endLabel, 0, 1);
        grid.add(endSpinner, 1, 1);
        
        // Shift amount
        Label shiftLabel = new Label("Muutos:");
        shiftSpinner = new Spinner<>(-999999, 999999, 1);
        shiftSpinner.setEditable(true);
        shiftSpinner.setPrefWidth(120);
        shiftSpinner.valueProperty().addListener((obs, oldVal, newVal) -> updatePreview());
        
        grid.add(shiftLabel, 2, 0);
        grid.add(shiftSpinner, 3, 0);
        GridPane.setRowSpan(shiftSpinner, 2);
        
        // Preview area
        Label previewLabel = new Label("Esikatselu:");
        previewLabel.setStyle("-fx-font-weight: bold;");
        
        previewTextFlow = new TextFlow();
        previewTextFlow.setPadding(new Insets(10));
        previewTextFlow.setStyle("-fx-background-color: #f8fafc; -fx-border-color: #e2e8f0; -fx-border-radius: 4;");
        
        ScrollPane scrollPane = new ScrollPane(previewTextFlow);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(150);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        
        // Buttons
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.setPadding(new Insets(10, 0, 0, 0));

        okButton = new Button("OK");
        okButton.setDefaultButton(true);
        okButton.setDisable(true);
        okButton.setOnAction(e -> accept());
        okButton.setPrefWidth(100);

        Button cancelButton = new Button("Peruuta");
        cancelButton.setCancelButton(true);
        cancelButton.setOnAction(e -> dialog.close());
        cancelButton.setPrefWidth(100);

        buttonBox.getChildren().addAll(okButton, cancelButton);

        root.getChildren().addAll(
            headerLabel, 
            descLabel,
            grid, 
            previewLabel,
            scrollPane, 
            buttonBox
        );

        Scene scene = new Scene(root, 480, 400);
        dialog.setScene(scene);
    }

    /**
     * Hakee tositteet tietokannasta ja asettaa oletusarvot.
     */
    public void fetchDocuments(int startNumber, int endNumber) throws DataAccessException {
        DataSource dataSource = registry.getDataSource();
        Session sess = null;
        int lastNumber = startNumber;
        
        try {
            sess = dataSource.openSession();
            documents = dataSource.getDocumentDAO(sess).getByPeriodId(
                registry.getPeriod().getId(), 1);
            numberSet = new HashSet<>();
            
            for (Document document : documents) {
                numberSet.add(document.getNumber());
                
                if (document.getNumber() < endNumber) {
                    lastNumber = Math.max(lastNumber, document.getNumber());
                }
            }
        } finally {
            if (sess != null) sess.close();
        }
        
        startSpinner.getValueFactory().setValue(startNumber);
        endSpinner.getValueFactory().setValue(lastNumber);
        shiftSpinner.getValueFactory().setValue(1);
        
        updatePreview();
    }

    private void updatePreview() {
        previewTextFlow.getChildren().clear();
        
        if (documents == null || documents.isEmpty()) {
            Text noDocsText = new Text("Ei tositteita valitulla välillä");
            noDocsText.setStyle("-fx-fill: #64748b;");
            previewTextFlow.getChildren().add(noDocsText);
            okButton.setDisable(true);
            return;
        }
        
        int shift = shiftSpinner.getValue();
        
        if (shift == 0) {
            Text noShiftText = new Text("Muutos on 0 - numeroita ei muuteta");
            noShiftText.setStyle("-fx-fill: #64748b;");
            previewTextFlow.getChildren().add(noShiftText);
            okButton.setDisable(true);
            return;
        }
        
        int start = startSpinner.getValue();
        int end = endSpinner.getValue();
        int newStart = start + shift;
        int newEnd = end + shift;
        
        if (shift < 0) {
            newEnd = Math.min(newEnd, start - 1);
        } else {
            newStart = Math.max(newStart, end + 1);
        }
        
        boolean hasConflicts = false;
        boolean first = true;
        
        for (Document document : documents) {
            if (document.getNumber() < start || document.getNumber() > end) {
                continue;
            }
            
            int newNumber = document.getNumber() + shift;
            String text = String.format("(%d → %d)", document.getNumber(), newNumber);
            
            boolean isConflict = newNumber < 1 || 
                (newNumber >= newStart && newNumber <= newEnd && numberSet.contains(newNumber));
            
            if (isConflict) {
                hasConflicts = true;
            }
            
            if (!first) {
                Text space = new Text(" ");
                previewTextFlow.getChildren().add(space);
            }
            first = false;
            
            Text numText = new Text(text);
            if (isConflict) {
                numText.setStyle("-fx-fill: #dc2626; -fx-font-weight: bold;");
            } else {
                numText.setStyle("-fx-fill: #16a34a;");
            }
            previewTextFlow.getChildren().add(numText);
        }
        
        if (first) {
            Text noMatchText = new Text("Ei tositteita valitulla välillä");
            noMatchText.setStyle("-fx-fill: #64748b;");
            previewTextFlow.getChildren().add(noMatchText);
            okButton.setDisable(true);
        } else {
            okButton.setDisable(hasConflicts);
            
            if (hasConflicts) {
                Text conflictWarning = new Text("\n\n⚠️ Numeroristiriitoja - korjaa ennen jatkamista");
                conflictWarning.setStyle("-fx-fill: #dc2626;");
                previewTextFlow.getChildren().add(conflictWarning);
            }
        }
    }

    private void accept() {
        try {
            shiftNumbers();
            accepted = true;
            dialog.close();
        } catch (DataAccessException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.initOwner(dialog);
            alert.setTitle("Virhe");
            alert.setHeaderText("Tositenumeroiden muuttaminen epäonnistui");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    private void shiftNumbers() throws DataAccessException {
        DataSource dataSource = registry.getDataSource();
        Session sess = null;
        int shift = shiftSpinner.getValue();
        int start = startSpinner.getValue();
        int end = endSpinner.getValue();
        
        try {
            sess = dataSource.openSession();
            dataSource.getDocumentDAO(sess).shiftNumbers(
                registry.getPeriod().getId(), start, end, shift);
            sess.commit();
        } catch (DataAccessException e) {
            if (sess != null) sess.rollback();
            throw e;
        } finally {
            if (sess != null) sess.close();
        }
    }

    public boolean showAndWait() {
        dialog.showAndWait();
        return accepted;
    }

    public boolean isAccepted() {
        return accepted;
    }
}
