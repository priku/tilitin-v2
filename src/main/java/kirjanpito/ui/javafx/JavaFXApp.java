package kirjanpito.ui.javafx;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import kirjanpito.util.AppSettings;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * Tilitin JavaFX -sovelluksen pääluokka.
 * 
 * Käynnistää JavaFX-sovelluksen ja lataa pääikkunan FXML:stä.
 */
public class JavaFXApp extends Application {
    
    private static final String APP_TITLE = "Tilitin";
    private static final String APP_DATA_NAME = "Tilitin";
    private static final int DEFAULT_WIDTH = 1200;
    private static final int DEFAULT_HEIGHT = 700;
    
    private Stage primaryStage;
    private MainController mainController;
    
    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        
        // Lataa asetukset ensin
        initSettings();
        
        try {
            // Lataa FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MainView.fxml"));
            Parent root = loader.load();
            
            // Hae controller
            mainController = loader.getController();
            
            // Luo scene ENSIN
            Scene scene = new Scene(root, DEFAULT_WIDTH, DEFAULT_HEIGHT);
            
            // Aseta scene stageen ENNEN setStage-kutsua
            primaryStage.setTitle(APP_TITLE);
            primaryStage.setScene(scene);
            
            // NYT vasta kutsu setStage - scene on jo asetettu, joten applyTheme toimii
            mainController.setStage(primaryStage);
            
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
    
    /**
     * Alustaa AppSettings-olion lataamalla asetukset tiedostosta.
     */
    private void initSettings() {
        AppSettings settings = AppSettings.getInstance();
        File settingsFile = new File(AppSettings.buildDirectoryPath(APP_DATA_NAME), "asetukset.properties");
        settings.load(settingsFile);
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
