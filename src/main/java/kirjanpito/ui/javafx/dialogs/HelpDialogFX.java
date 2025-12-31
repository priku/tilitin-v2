package kirjanpito.ui.javafx.dialogs;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

/**
 * JavaFX ohjeikkuna.
 */
public class HelpDialogFX {
    
    private Stage dialog;
    
    public HelpDialogFX(Window owner) {
        dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(owner);
        dialog.setTitle("Ohje");
        dialog.setResizable(false);
        
        createContent();
    }
    
    private void createContent() {
        VBox root = new VBox(20);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #ffffff;");
        
        // Title
        Label titleLabel = new Label("Tilitin - Kirjanpito-ohjelma");
        titleLabel.setFont(Font.font(18));
        titleLabel.setStyle("-fx-font-weight: bold;");
        
        // Content
        VBox contentBox = new VBox(15);
        contentBox.setPadding(new Insets(10, 0, 0, 0));
        
        Label versionLabel = new Label("Versio: 2.2.5");
        Label copyrightLabel = new Label("© 2025 Tilitin");
        
        Separator separator = new Separator();
        
        VBox helpBox = new VBox(10);
        helpBox.setPadding(new Insets(10, 0, 0, 0));
        
        Label shortcutsLabel = new Label("Pikanäppäimet:");
        shortcutsLabel.setStyle("-fx-font-weight: bold;");
        
        VBox shortcutsBox = new VBox(5);
        shortcutsBox.setPadding(new Insets(5, 0, 0, 20));
        shortcutsBox.getChildren().addAll(
            new Label("Ctrl+N - Uusi tosite"),
            new Label("Ctrl+S - Tallenna"),
            new Label("Ctrl+P - Tulosta"),
            new Label("Ctrl+O - Avaa tietokanta"),
            new Label("F9 - Tilikartan pikahaku"),
            new Label("PageUp/PageDown - Navigoi tositteissa"),
            new Label("Delete - Poista vienti")
        );
        
        helpBox.getChildren().addAll(shortcutsLabel, shortcutsBox);
        
        contentBox.getChildren().addAll(
            versionLabel,
            copyrightLabel,
            separator,
            helpBox
        );
        
        // Buttons
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.setPadding(new Insets(10, 0, 0, 0));
        
        Button closeBtn = new Button("Sulje");
        closeBtn.setDefaultButton(true);
        closeBtn.setOnAction(e -> dialog.close());
        
        buttonBox.getChildren().add(closeBtn);
        
        root.getChildren().addAll(titleLabel, contentBox, buttonBox);
        
        Scene scene = new Scene(root, 400, 350);
        dialog.setScene(scene);
    }
    
    public void show() {
        dialog.showAndWait();
    }
}
