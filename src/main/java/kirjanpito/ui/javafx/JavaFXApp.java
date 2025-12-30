package kirjanpito.ui.javafx;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.InputStream;

/**
 * Tilitin JavaFX -sovelluksen pääluokka.
 * 
 * Käynnistää JavaFX-sovelluksen ja lataa pääikkunan FXML:stä.
 */
public class JavaFXApp extends Application {
    
    private static final String APP_TITLE = "Tilitin";
    private static final int DEFAULT_WIDTH = 1200;
    private static final int DEFAULT_HEIGHT = 700;
    
    private Stage primaryStage;
    private MainController mainController;
    
    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        
        try {
            // Lataa FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MainView.fxml"));
            Parent root = loader.load();
            
            // Hae controller
            mainController = loader.getController();
            mainController.setStage(primaryStage);
            
            // Luo scene
            Scene scene = new Scene(root, DEFAULT_WIDTH, DEFAULT_HEIGHT);
            
            // Lataa CSS-tyyli
            String css = getClass().getResource("/fxml/styles.css").toExternalForm();
            scene.getStylesheets().add(css);
            
            // Aseta ikkuna
            primaryStage.setTitle(APP_TITLE);
            primaryStage.setScene(scene);
            
            // Lataa ikoni
            loadAppIcon();
            
            // Näytä
            primaryStage.show();
            
            System.out.println("✅ Tilitin JavaFX käynnistetty");
            
        } catch (IOException e) {
            System.err.println("❌ Virhe ladattaessa FXML: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void loadAppIcon() {
        try {
            InputStream iconStream = getClass().getResourceAsStream("/tilitin-48x48.png");
            if (iconStream != null) {
                primaryStage.getIcons().add(new Image(iconStream));
            }
        } catch (Exception e) {
            System.err.println("Ikonia ei voitu ladata: " + e.getMessage());
        }
    }
    
    @Override
    public void stop() {
        System.out.println("Tilitin suljetaan...");
        if (mainController != null) {
            mainController.shutdown();
        }
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}
