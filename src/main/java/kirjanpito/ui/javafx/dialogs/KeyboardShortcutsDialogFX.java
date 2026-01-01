package kirjanpito.ui.javafx.dialogs;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

/**
 * JavaFX pikanäppäimet-dialogi.
 * Näyttää listan kaikista käytettävissä olevista pikanäppäimistä.
 */
public class KeyboardShortcutsDialogFX {
    
    private Stage dialog;
    
    public KeyboardShortcutsDialogFX(Window owner) {
        dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(owner);
        dialog.setTitle("Pikanäppäimet");
        dialog.setResizable(true);
        
        createContent();
    }
    
    private void createContent() {
        VBox root = new VBox(10);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #ffffff;");
        
        // Create scrollable content
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(400);
        
        VBox content = new VBox(15);
        content.setPadding(new Insets(10));
        
        // File operations
        content.getChildren().add(createSection("Tiedosto", new String[][] {
            {"Ctrl+U", "Uusi tietokanta"},
            {"Ctrl+O", "Avaa tietokanta"},
            {"Ctrl+Q", "Lopeta"}
        }));
        
        // Document operations
        content.getChildren().add(createSection("Tosite", new String[][] {
            {"Ctrl+N", "Uusi tosite"},
            {"Ctrl+Delete", "Poista tosite"},
            {"Ctrl+S", "Tallenna"},
            {"F8", "Lisää vienti"},
            {"Shift+Delete", "Poista vienti"}
        }));
        
        // Navigation
        content.getChildren().add(createSection("Navigointi", new String[][] {
            {"Page Up", "Edellinen tosite"},
            {"Page Down", "Seuraava tosite"},
            {"Ctrl+Page Up", "Ensimmäinen tosite"},
            {"Ctrl+Page Down", "Viimeinen tosite"},
            {"Ctrl+G", "Siirry tositteeseen"},
            {"Ctrl+F", "Etsi"},
            {"Ctrl+Left", "Edellinen tosite"},
            {"Ctrl+Right", "Seuraava tosite"}
        }));
        
        // Editing
        content.getChildren().add(createSection("Muokkaus", new String[][] {
            {"Ctrl+C", "Kopioi"},
            {"Ctrl+V", "Liitä"},
            {"Ctrl+T", "Tilikartta"},
            {"Ctrl+B", "Alkusaldot"},
            {"Ctrl+M", "Muokkaa vientimalleja"},
            {"Ctrl+K", "Luo vientimalli tositteesta"},
            {"F9", "Valitse tili"}
        }));
        
        // Document types
        content.getChildren().add(createSection("Tositelajit", new String[][] {
            {"Alt+Q", "Tositelaji 1"},
            {"Alt+W", "Tositelaji 2"},
            {"Alt+E", "Tositelaji 3"},
            {"Alt+R", "Tositelaji 4"},
            {"Alt+T", "Tositelaji 5"},
            {"Alt+Y", "Tositelaji 6"},
            {"Alt+U", "Tositelaji 7"},
            {"Alt+I", "Tositelaji 8"},
            {"Alt+O", "Tositelaji 9"},
            {"Alt+P", "Tositelaji 10"}
        }));
        
        // Reports
        content.getChildren().add(createSection("Tulosteet", new String[][] {
            {"Ctrl+1", "Tilien saldot"},
            {"Ctrl+2", "Tosite"},
            {"Ctrl+3", "Tiliote"},
            {"Ctrl+4", "Tuloslaskelma"},
            {"Ctrl+5", "Tuloslaskelma erittelyin"},
            {"Ctrl+6", "Tase"},
            {"Ctrl+7", "Tase erittelyin"},
            {"Ctrl+8", "Päiväkirja"},
            {"Ctrl+9", "Pääkirja"},
            {"Ctrl+0", "ALV-laskelma"},
            {"Ctrl+P", "Tulosta"}
        }));
        
        // Settings
        content.getChildren().add(createSection("Asetukset", new String[][] {
            {"Ctrl+Shift+S", "Yleiset asetukset"},
            {"Ctrl+Shift+A", "Ulkoasu"},
            {"Ctrl+Shift+P", "Perustiedot"},
            {"Ctrl+Shift+V", "ALV-merkintä"}
        }));
        
        // Help
        content.getChildren().add(createSection("Ohje", new String[][] {
            {"F1", "Ohje"}
        }));
        
        scrollPane.setContent(content);
        
        // Close button
        HBox buttonBox = new HBox();
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.setPadding(new Insets(10, 0, 0, 0));
        
        Button closeBtn = new Button("Sulje");
        closeBtn.setDefaultButton(true);
        closeBtn.setCancelButton(true);
        closeBtn.setOnAction(e -> dialog.close());
        
        buttonBox.getChildren().add(closeBtn);
        
        root.getChildren().addAll(scrollPane, buttonBox);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        
        Scene scene = new Scene(root, 450, 500);
        dialog.setScene(scene);
    }
    
    private TitledPane createSection(String title, String[][] shortcuts) {
        TitledPane pane = new TitledPane();
        pane.setText(title);
        pane.setCollapsible(true);
        pane.setExpanded(true);
        
        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(5);
        grid.setPadding(new Insets(10));
        
        for (int i = 0; i < shortcuts.length; i++) {
            Label keyLabel = new Label(shortcuts[i][0]);
            keyLabel.setStyle("-fx-font-family: monospace; -fx-font-weight: bold; -fx-background-color: #e5e7eb; -fx-padding: 2 6; -fx-background-radius: 3;");
            
            Label descLabel = new Label(shortcuts[i][1]);
            
            grid.add(keyLabel, 0, i);
            grid.add(descLabel, 1, i);
        }
        
        pane.setContent(grid);
        return pane;
    }
    
    public void show() {
        dialog.showAndWait();
    }
}
