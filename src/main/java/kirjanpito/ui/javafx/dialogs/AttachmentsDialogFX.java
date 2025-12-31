package kirjanpito.ui.javafx.dialogs;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
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

import kirjanpito.db.*;
import kirjanpito.models.Attachment;
import kirjanpito.util.PdfUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.util.List;

/**
 * JavaFX liitteiden hallintadialogi.
 */
public class AttachmentsDialogFX {
    
    private Stage dialog;
    private TableView<Attachment> attachmentTable;
    private ObservableList<Attachment> attachments;
    private Label statusLabel;
    
    private DataSource dataSource;
    private Document document;
    private int documentId;
    
    public AttachmentsDialogFX(Window owner, DataSource dataSource, Document document) {
        this.dataSource = dataSource;
        this.document = document;
        this.documentId = document.getId();
        
        dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(owner);
        dialog.setTitle("PDF-liitteet - Tosite " + document.getNumber());
        dialog.setMinWidth(600);
        dialog.setMinHeight(400);
        
        createContent();
        loadAttachments();
    }
    
    private void createContent() {
        VBox root = new VBox(10);
        root.setPadding(new Insets(16));
        root.setStyle("-fx-background-color: #ffffff;");
        
        // Toolbar
        HBox toolbar = new HBox(10);
        toolbar.setAlignment(Pos.CENTER_LEFT);
        
        Button addBtn = new Button("+ Lisää PDF...");
        addBtn.setStyle("-fx-background-color: #2563eb; -fx-text-fill: white;");
        addBtn.setOnAction(e -> addAttachment());
        
        Button removeBtn = new Button("Poista");
        removeBtn.setOnAction(e -> removeAttachment());
        
        Button exportBtn = new Button("Vie tiedostoon...");
        exportBtn.setOnAction(e -> exportAttachment());
        
        toolbar.getChildren().addAll(addBtn, removeBtn, exportBtn);
        
        // Table
        attachmentTable = new TableView<>();
        attachmentTable.setEditable(false);
        VBox.setVgrow(attachmentTable, Priority.ALWAYS);
        
        TableColumn<Attachment, String> filenameCol = new TableColumn<>("Tiedosto");
        filenameCol.setCellValueFactory(new PropertyValueFactory<>("filename"));
        filenameCol.setPrefWidth(200);
        
        TableColumn<Attachment, String> sizeCol = new TableColumn<>("Koko");
        sizeCol.setCellValueFactory(cellData -> {
            Attachment att = cellData.getValue();
            return new javafx.beans.property.SimpleStringProperty(att.formatFileSize());
        });
        sizeCol.setPrefWidth(100);
        
        TableColumn<Attachment, Integer> pagesCol = new TableColumn<>("Sivut");
        pagesCol.setCellValueFactory(cellData -> {
            Integer pages = cellData.getValue().getPageCount();
            return new javafx.beans.property.SimpleIntegerProperty(pages != null ? pages : 0).asObject();
        });
        pagesCol.setPrefWidth(70);
        
        attachmentTable.getColumns().addAll(filenameCol, sizeCol, pagesCol);
        
        attachmentTable.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> {
            boolean hasSelection = n != null;
            removeBtn.setDisable(!hasSelection);
            exportBtn.setDisable(!hasSelection);
        });
        
        // Status
        statusLabel = new Label("Ladataan...");
        statusLabel.setPadding(new Insets(5, 0, 0, 0));
        
        // Buttons
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.setPadding(new Insets(10, 0, 0, 0));
        
        Button closeBtn = new Button("Sulje");
        closeBtn.setDefaultButton(true);
        closeBtn.setOnAction(e -> dialog.close());
        
        buttonBox.getChildren().add(closeBtn);
        
        root.getChildren().addAll(toolbar, attachmentTable, statusLabel, buttonBox);
        
        Scene scene = new Scene(root, 650, 450);
        dialog.setScene(scene);
    }
    
    private void loadAttachments() {
        Task<List<Attachment>> task = new Task<List<Attachment>>() {
            @Override
            protected List<Attachment> call() throws Exception {
                Session session = dataSource.openSession();
                try {
                    AttachmentDAO dao = dataSource.getAttachmentDAO(session);
                    return dao.findByDocumentId(documentId);
                } finally {
                    session.close();
                }
            }
        };
        
        task.setOnSucceeded(e -> {
            try {
                attachments = FXCollections.observableArrayList(task.getValue());
                attachmentTable.setItems(attachments);
                updateStatus();
            } catch (Exception ex) {
                showError("Virhe", "Virhe ladattaessa liitteitä: " + ex.getMessage());
            }
        });
        
        task.setOnFailed(e -> {
            showError("Virhe", "Virhe ladattaessa liitteitä: " + task.getException().getMessage());
        });
        
        new Thread(task).start();
    }
    
    private void addAttachment() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Valitse PDF-tiedosto");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        
        File file = fileChooser.showOpenDialog(dialog);
        if (file == null) return;
        
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                byte[] data = Files.readAllBytes(file.toPath());
                
                // Validate PDF
                if (!PdfUtils.INSTANCE.isValidPdf(data)) {
                    throw new IllegalArgumentException("Tiedosto ei ole kelvollinen PDF-tiedosto");
                }
                
                // Check file size
                if (data.length > Attachment.MAX_FILE_SIZE) {
                    throw new IllegalArgumentException(
                        String.format("Tiedosto on liian suuri (%.2f MB, max 10 MB)", 
                            data.length / 1024.0 / 1024.0));
                }
                
                // Show warning for large files
                if (data.length > Attachment.WARNING_FILE_SIZE) {
                    Platform.runLater(() -> {
                        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                        alert.setTitle("Suuri tiedosto");
                        alert.setHeaderText("PDF-tiedosto on suuri");
                        alert.setContentText(String.format("Tiedoston koko on %.2f MB. Haluatko jatkaa?", 
                            data.length / 1024.0 / 1024.0));
                        
                        if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.CANCEL) {
                            cancel();
                            return;
                        }
                    });
                    if (isCancelled()) return null;
                }
                
                // Calculate page count
                Integer pageCount = PdfUtils.INSTANCE.calculatePageCount(data);
                
                // Create attachment
                Attachment attachment = Attachment.Companion.fromFile(
                    documentId,
                    file.getName(),
                    data,
                    null
                );
                
                // Save to database
                Session session = dataSource.openSession();
                try {
                    AttachmentDAO dao = dataSource.getAttachmentDAO(session);
                    dao.save(attachment);
                    session.commit();
                } finally {
                    session.close();
                }
                
                return null;
            }
        };
        
        task.setOnSucceeded(e -> {
            loadAttachments();
            new Alert(Alert.AlertType.INFORMATION, "Liite lisätty onnistuneesti").showAndWait();
        });
        
        task.setOnFailed(e -> {
            showError("Virhe", "Virhe lisättäessä liitettä: " + task.getException().getMessage());
        });
        
        new Thread(task).start();
    }
    
    private void removeAttachment() {
        Attachment selected = attachmentTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Poista liite");
        confirm.setHeaderText("Poista liite?");
        confirm.setContentText("Haluatko varmasti poistaa liitteen \"" + selected.getFilename() + "\"?");
        
        if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
            return;
        }
        
        Task<Boolean> task = new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                Session session = dataSource.openSession();
                try {
                    AttachmentDAO dao = dataSource.getAttachmentDAO(session);
                    boolean deleted = dao.delete(selected.getId());
                    if (deleted) {
                        session.commit();
                    }
                    return deleted;
                } finally {
                    session.close();
                }
            }
        };
        
        task.setOnSucceeded(e -> {
            if (task.getValue()) {
                loadAttachments();
                new Alert(Alert.AlertType.INFORMATION, "Liite poistettu").showAndWait();
            } else {
                showError("Virhe", "Liitteen poisto epäonnistui");
            }
        });
        
        task.setOnFailed(e -> {
            showError("Virhe", "Virhe poistettaessa liitettä: " + task.getException().getMessage());
        });
        
        new Thread(task).start();
    }
    
    private void exportAttachment() {
        Attachment selected = attachmentTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Tallenna PDF-tiedosto");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        fileChooser.setInitialFileName(selected.getFilename());
        
        File file = fileChooser.showSaveDialog(dialog);
        if (file == null) return;
        
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                try (FileOutputStream out = new FileOutputStream(file)) {
                    out.write(selected.getData());
                }
                return null;
            }
        };
        
        task.setOnSucceeded(e -> {
            new Alert(Alert.AlertType.INFORMATION, 
                "PDF tallennettu: " + file.getAbsolutePath()).showAndWait();
        });
        
        task.setOnFailed(e -> {
            showError("Virhe", "Virhe tallennettaessa tiedostoa: " + task.getException().getMessage());
        });
        
        new Thread(task).start();
    }
    
    private void updateStatus() {
        int count = attachments != null ? attachments.size() : 0;
        if (count == 0) {
            statusLabel.setText("Ei liitteitä");
        } else {
            statusLabel.setText(String.format("%d liitettä", count));
        }
    }
    
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    public void show() {
        dialog.showAndWait();
    }
}
