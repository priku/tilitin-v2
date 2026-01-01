package kirjanpito.ui.javafx.dialogs;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

/**
 * JavaFX About dialog showing application information.
 */
public class AboutDialogFX {

    private Stage dialog;

    private static final String VERSION = "2.1.5";
    private static final String BUILD_DATE = "2026-01-01";

    public AboutDialogFX(Window owner) {
        dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(owner);
        dialog.setTitle("Tietoja ohjelmasta");
        dialog.setResizable(false);

        createContent();
    }

    private void createContent() {
        VBox root = new VBox(16);
        root.setPadding(new Insets(24));
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: #ffffff;");

        // Application icon/logo
        Label logoLabel = new Label("📒");
        logoLabel.setStyle("-fx-font-size: 48px;");

        // Application name
        Label nameLabel = new Label("Tilitin");
        nameLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #1e3a8a;");

        // Version info
        Label versionLabel = new Label("Versio " + VERSION);
        versionLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #64748b;");

        Label buildLabel = new Label("Käännetty: " + BUILD_DATE);
        buildLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #94a3b8;");

        // Description
        Text descText = new Text(
            "Ilmainen kirjanpito-ohjelma suomalaisille\n" +
            "yhdistyksille ja pienyrityksille.\n\n" +
            "Tilitin on avoimen lähdekoodin ohjelma,\n" +
            "joka on lisensoitu GNU GPL v3 -lisenssillä."
        );
        descText.setTextAlignment(TextAlignment.CENTER);
        descText.setStyle("-fx-fill: #475569;");

        // Separator
        Separator sep = new Separator();
        sep.setPadding(new Insets(8, 0, 8, 0));

        // Credits
        VBox creditsBox = new VBox(4);
        creditsBox.setAlignment(Pos.CENTER);

        Label originalLabel = new Label("Alkuperäinen kehittäjä:");
        originalLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #94a3b8;");
        
        Label authorLabel = new Label("Tommi Helineva");
        authorLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b; -fx-font-weight: 500;");

        Label modernizedLabel = new Label("JavaFX-modernisointi:");
        modernizedLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #94a3b8; -fx-padding: 8 0 0 0;");
        
        Label modernAuthorLabel = new Label("GitHub Copilot & priku");
        modernAuthorLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b; -fx-font-weight: 500;");

        creditsBox.getChildren().addAll(originalLabel, authorLabel, modernizedLabel, modernAuthorLabel);

        // Links
        Hyperlink websiteLink = new Hyperlink("helineva.net/tilitin");
        websiteLink.setStyle("-fx-text-fill: #2563eb;");
        websiteLink.setOnAction(e -> {
            try {
                java.awt.Desktop.getDesktop().browse(new java.net.URI("https://helineva.net/tilitin"));
            } catch (Exception ex) {
                // Ignore
            }
        });

        Hyperlink githubLink = new Hyperlink("GitHub: tilitin-modernized");
        githubLink.setStyle("-fx-text-fill: #2563eb;");
        githubLink.setOnAction(e -> {
            try {
                java.awt.Desktop.getDesktop().browse(new java.net.URI("https://github.com/priku/tilitin-modernized"));
            } catch (Exception ex) {
                // Ignore
            }
        });

        // Close button
        Button closeButton = new Button("Sulje");
        closeButton.setDefaultButton(true);
        closeButton.setCancelButton(true);
        closeButton.setStyle("-fx-background-color: #2563eb; -fx-text-fill: white; -fx-min-width: 100;");
        closeButton.setOnAction(e -> dialog.close());

        root.getChildren().addAll(
            logoLabel, nameLabel, versionLabel, buildLabel,
            new Separator(),
            descText,
            sep,
            creditsBox,
            websiteLink, githubLink,
            new Region(),
            closeButton
        );
        VBox.setVgrow(root.getChildren().get(root.getChildren().size() - 2), Priority.ALWAYS);

        Scene scene = new Scene(root, 350, 480);
        dialog.setScene(scene);
    }

    public void show() {
        dialog.showAndWait();
    }

    /**
     * Static factory method.
     */
    public static void showAbout(Window owner) {
        AboutDialogFX dialog = new AboutDialogFX(owner);
        dialog.show();
    }
}
