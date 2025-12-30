package kirjanpito.ui.javafx.dialogs;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.converter.IntegerStringConverter;

import kirjanpito.db.*;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * JavaFX tositelajien muokkausdialogi.
 */
public class DocumentTypeDialogFX {
    
    private Stage dialog;
    private TableView<DocumentType> table;
    private ObservableList<DocumentType> items;
    
    private DataSource dataSource;
    private Consumer<Void> onSave;
    private boolean modified = false;
    private List<DocumentType> deleted = new ArrayList<>();
    
    public DocumentTypeDialogFX(Window owner, DataSource dataSource) {
        this.dataSource = dataSource;
        
        dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(owner);
        dialog.setTitle("Tositelajit");
        dialog.setMinWidth(500);
        dialog.setMinHeight(400);
        
        createContent();
    }
    
    private void createContent() {
        VBox root = new VBox(10);
        root.setPadding(new Insets(16));
        root.setStyle("-fx-background-color: #ffffff;");
        
        // Toolbar
        HBox toolbar = new HBox(10);
        
        Button addBtn = new Button("+ Lisää");
        addBtn.setStyle("-fx-background-color: #2563eb; -fx-text-fill: white;");
        addBtn.setOnAction(e -> addItem());
        
        Button removeBtn = new Button("Poista");
        removeBtn.setOnAction(e -> removeItem());
        
        toolbar.getChildren().addAll(addBtn, removeBtn);
        
        // Table
        table = new TableView<>();
        table.setEditable(true);
        VBox.setVgrow(table, Priority.ALWAYS);
        
        TableColumn<DocumentType, String> nameCol = new TableColumn<>("Nimi");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setCellFactory(TextFieldTableCell.forTableColumn());
        nameCol.setOnEditCommit(e -> {
            e.getRowValue().setName(e.getNewValue());
            modified = true;
        });
        nameCol.setPrefWidth(200);
        
        TableColumn<DocumentType, Integer> startCol = new TableColumn<>("Alku");
        startCol.setCellValueFactory(new PropertyValueFactory<>("numberStart"));
        startCol.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
        startCol.setOnEditCommit(e -> {
            e.getRowValue().setNumberStart(e.getNewValue());
            modified = true;
        });
        startCol.setPrefWidth(80);
        
        TableColumn<DocumentType, Integer> endCol = new TableColumn<>("Loppu");
        endCol.setCellValueFactory(new PropertyValueFactory<>("numberEnd"));
        endCol.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
        endCol.setOnEditCommit(e -> {
            e.getRowValue().setNumberEnd(e.getNewValue());
            modified = true;
        });
        endCol.setPrefWidth(80);
        
        table.getColumns().addAll(nameCol, startCol, endCol);
        
        // Buttons
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.setPadding(new Insets(10, 0, 0, 0));
        
        Button saveBtn = new Button("Tallenna");
        saveBtn.setDefaultButton(true);
        saveBtn.setStyle("-fx-background-color: #2563eb; -fx-text-fill: white;");
        saveBtn.setOnAction(e -> save());
        
        Button cancelBtn = new Button("Peruuta");
        cancelBtn.setCancelButton(true);
        cancelBtn.setOnAction(e -> dialog.close());
        
        buttonBox.getChildren().addAll(cancelBtn, saveBtn);
        
        root.getChildren().addAll(toolbar, table, buttonBox);
        
        Scene scene = new Scene(root, 500, 400);
        dialog.setScene(scene);
    }
    
    private void addItem() {
        DocumentType item = new DocumentType();
        item.setName("Uusi tositelaji");
        item.setNumberStart(1);
        item.setNumberEnd(999999);
        
        items.add(item);
        table.getSelectionModel().select(item);
        modified = true;
    }
    
    private void removeItem() {
        DocumentType selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        
        if (selected.getId() > 0) {
            deleted.add(selected);
        }
        items.remove(selected);
        modified = true;
    }
    
    private void save() {
        if (!modified) {
            dialog.close();
            return;
        }
        
        Session session = null;
        try {
            session = dataSource.openSession();
            DocumentTypeDAO dao = dataSource.getDocumentTypeDAO(session);
            
            for (DocumentType item : deleted) {
                dao.delete(item.getId());
            }
            
            for (DocumentType item : items) {
                dao.save(item);
            }
            
            session.commit();
            
            if (onSave != null) {
                onSave.accept(null);
            }
            
            dialog.close();
            
        } catch (Exception e) {
            if (session != null) {
                try { session.rollback(); } catch (Exception re) {}
            }
            new Alert(Alert.AlertType.ERROR, "Tallennusvirhe: " + e.getMessage()).showAndWait();
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }
    
    public void setItems(List<DocumentType> list) {
        items = FXCollections.observableArrayList(list);
        table.setItems(items);
    }
    
    public void setOnSave(Consumer<Void> callback) {
        this.onSave = callback;
    }
    
    public void show() {
        dialog.showAndWait();
    }
}
