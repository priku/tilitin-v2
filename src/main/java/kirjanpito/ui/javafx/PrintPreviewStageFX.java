package kirjanpito.ui.javafx;

import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.print.PrinterJob;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import kirjanpito.models.PrintPreviewModel;
import kirjanpito.reports.Print;
import kirjanpito.reports.PrintModel;
import kirjanpito.util.AppSettings;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * JavaFX-toteutus tulosteiden esikatseluikkunasta.
 * Korvaa Swing-pohjaisen PrintPreviewFrame:n.
 */
public class PrintPreviewStageFX {

    private static final Logger logger = Logger.getLogger("kirjanpito");
    
    private static final double[] ZOOM_LEVELS = {0.50, 0.70, 0.85, 1.00, 1.25, 1.50, 1.75, 2.00};
    private static final String[] ZOOM_TEXTS = {"50 %", "70 %", "85 %", "100 %", "125 %", "150 %", "175 %", "200 %"};

    private final Stage stage;
    private final PrintPreviewModel model;
    private final ScrollPane scrollPane;
    private final ImageView imageView;
    private final Label pageLabel;
    private final ComboBox<String> zoomComboBox;
    
    private int currentPage = 0;
    private double currentZoom = 1.0;

    public PrintPreviewStageFX(Window owner, PrintPreviewModel model) {
        this.model = model;
        this.stage = new Stage();
        stage.initModality(Modality.NONE);
        stage.initOwner(owner);
        stage.setTitle("Tulosteen esikatselu");
        
        // Create image view for the preview
        imageView = new ImageView();
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);
        
        // Scroll pane for the preview
        scrollPane = new ScrollPane(imageView);
        scrollPane.setFitToWidth(false);
        scrollPane.setFitToHeight(false);
        scrollPane.setPannable(true);
        scrollPane.setStyle("-fx-background-color: #606060;");
        
        // Page label
        pageLabel = new Label("Sivu 1 / 1");
        
        // Zoom combo box
        zoomComboBox = new ComboBox<>();
        zoomComboBox.getItems().addAll(ZOOM_TEXTS);
        zoomComboBox.getSelectionModel().select(3); // 100%
        zoomComboBox.setOnAction(e -> {
            int idx = zoomComboBox.getSelectionModel().getSelectedIndex();
            if (idx >= 0 && idx < ZOOM_LEVELS.length) {
                currentZoom = ZOOM_LEVELS[idx];
                updatePreview();
            }
        });
        
        // Create toolbar
        ToolBar toolBar = createToolBar();
        
        // Layout
        BorderPane root = new BorderPane();
        root.setTop(toolBar);
        root.setCenter(scrollPane);
        
        // Load saved size
        AppSettings settings = AppSettings.getInstance();
        int width = settings.getInt("print-preview-window.width", 800);
        int height = settings.getInt("print-preview-window.height", 600);
        
        Scene scene = new Scene(root, Math.max(600, width), Math.max(400, height));
        setupKeyboardShortcuts(scene);
        stage.setScene(scene);
        
        // Save size on close
        stage.setOnCloseRequest(e -> {
            settings.set("print-preview-window.width", (int) stage.getWidth());
            settings.set("print-preview-window.height", (int) stage.getHeight());
        });
    }

    private ToolBar createToolBar() {
        ToolBar toolBar = new ToolBar();
        
        // Close button
        Button closeBtn = new Button("Sulje");
        closeBtn.setOnAction(e -> stage.close());
        
        // Print button
        Button printBtn = new Button("Tulosta");
        printBtn.setOnAction(e -> print());
        
        // Save button
        Button saveBtn = new Button("Tallenna PDF");
        saveBtn.setOnAction(e -> savePDF());
        
        // Separator
        Separator sep1 = new Separator();
        
        // Navigation buttons
        Button firstBtn = new Button("⏮");
        firstBtn.setTooltip(new Tooltip("Ensimmäinen sivu"));
        firstBtn.setOnAction(e -> goToPage(0));
        
        Button prevBtn = new Button("◀");
        prevBtn.setTooltip(new Tooltip("Edellinen sivu"));
        prevBtn.setOnAction(e -> goToPage(currentPage - 1));
        
        Button nextBtn = new Button("▶");
        nextBtn.setTooltip(new Tooltip("Seuraava sivu"));
        nextBtn.setOnAction(e -> goToPage(currentPage + 1));
        
        Button lastBtn = new Button("⏭");
        lastBtn.setTooltip(new Tooltip("Viimeinen sivu"));
        lastBtn.setOnAction(e -> goToPage(getPageCount() - 1));
        
        Separator sep2 = new Separator();
        
        // Zoom controls
        Button zoomOutBtn = new Button("-");
        zoomOutBtn.setTooltip(new Tooltip("Pienennä"));
        zoomOutBtn.setOnAction(e -> zoomOut());
        
        Button zoomInBtn = new Button("+");
        zoomInBtn.setTooltip(new Tooltip("Suurenna"));
        zoomInBtn.setOnAction(e -> zoomIn());
        
        toolBar.getItems().addAll(
            closeBtn, printBtn, saveBtn, sep1,
            firstBtn, prevBtn, pageLabel, nextBtn, lastBtn, sep2,
            zoomOutBtn, zoomComboBox, zoomInBtn
        );
        
        return toolBar;
    }

    private void setupKeyboardShortcuts(Scene scene) {
        // Escape or Ctrl+W to close
        scene.getAccelerators().put(
            new KeyCodeCombination(KeyCode.ESCAPE),
            () -> stage.close()
        );
        scene.getAccelerators().put(
            new KeyCodeCombination(KeyCode.W, KeyCombination.CONTROL_DOWN),
            () -> stage.close()
        );
        
        // Page navigation
        scene.getAccelerators().put(
            new KeyCodeCombination(KeyCode.PAGE_UP),
            () -> goToPage(currentPage - 1)
        );
        scene.getAccelerators().put(
            new KeyCodeCombination(KeyCode.PAGE_DOWN),
            () -> goToPage(currentPage + 1)
        );
        scene.getAccelerators().put(
            new KeyCodeCombination(KeyCode.HOME, KeyCombination.CONTROL_DOWN),
            () -> goToPage(0)
        );
        scene.getAccelerators().put(
            new KeyCodeCombination(KeyCode.END, KeyCombination.CONTROL_DOWN),
            () -> goToPage(getPageCount() - 1)
        );
        
        // Print
        scene.getAccelerators().put(
            new KeyCodeCombination(KeyCode.P, KeyCombination.CONTROL_DOWN),
            this::print
        );
        
        // Save
        scene.getAccelerators().put(
            new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN),
            this::savePDF
        );
    }

    private int getPageCount() {
        Print print = model.getPrint();
        return print != null ? print.getPageCount() : 1;
    }

    private void goToPage(int page) {
        int pageCount = getPageCount();
        currentPage = Math.max(0, Math.min(page, pageCount - 1));
        model.setPageIndex(currentPage);
        updatePreview();
    }

    private void zoomIn() {
        int idx = zoomComboBox.getSelectionModel().getSelectedIndex();
        if (idx < ZOOM_LEVELS.length - 1) {
            zoomComboBox.getSelectionModel().select(idx + 1);
        }
    }

    private void zoomOut() {
        int idx = zoomComboBox.getSelectionModel().getSelectedIndex();
        if (idx > 0) {
            zoomComboBox.getSelectionModel().select(idx - 1);
        }
    }

    private void updatePreview() {
        Print print = model.getPrint();
        if (print == null) return;
        
        // Update page label
        int pageCount = getPageCount();
        pageLabel.setText(String.format("Sivu %d / %d", currentPage + 1, pageCount));
        
        // Render the page to an image
        PageFormat pageFormat = model.createPageFormat();
        int width = (int) pageFormat.getWidth();
        int height = (int) pageFormat.getHeight();
        
        // Apply zoom
        int scaledWidth = (int) (width * currentZoom);
        int scaledHeight = (int) (height * currentZoom);
        
        // Create buffered image and render
        BufferedImage bufferedImage = new BufferedImage(scaledWidth, scaledHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = bufferedImage.createGraphics();
        
        // White background
        g2d.setColor(java.awt.Color.WHITE);
        g2d.fillRect(0, 0, scaledWidth, scaledHeight);
        
        // Apply zoom transform
        g2d.scale(currentZoom, currentZoom);
        
        // Enable antialiasing
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        
        try {
            // Use AWTPrintable from the print model - pass null for canvas, it will create one
            kirjanpito.reports.AWTPrintable printable = new kirjanpito.reports.AWTPrintable(print, null);
            printable.print(g2d, pageFormat, currentPage);
        } catch (Exception e) {
            logger.log(Level.WARNING, "Sivun piirtäminen epäonnistui", e);
        }
        
        g2d.dispose();
        
        // Convert to JavaFX image
        WritableImage fxImage = SwingFXUtils.toFXImage(bufferedImage, null);
        imageView.setImage(fxImage);
    }

    private void print() {
        PrinterJob job = PrinterJob.createPrinterJob();
        if (job != null && job.showPrintDialog(stage)) {
            // Use the existing model's print method via Swing
            try {
                model.print();
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Tulostaminen epäonnistui", e);
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.initOwner(stage);
                alert.setTitle("Virhe");
                alert.setHeaderText(null);
                alert.setContentText("Tulostaminen epäonnistui: " + e.getMessage());
                alert.showAndWait();
            }
        }
    }

    private void savePDF() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Tallenna PDF");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("PDF-tiedosto", "*.pdf"),
            new FileChooser.ExtensionFilter("CSV-tiedosto", "*.csv"),
            new FileChooser.ExtensionFilter("ODS-tiedosto", "*.ods")
        );
        
        AppSettings settings = AppSettings.getInstance();
        String lastDir = settings.getString("pdf-directory", System.getProperty("user.home"));
        fileChooser.setInitialDirectory(new File(lastDir));
        
        File file = fileChooser.showSaveDialog(stage);
        if (file != null) {
            settings.set("pdf-directory", file.getParent());
            
            try {
                String filename = file.getName().toLowerCase();
                
                if (filename.endsWith(".pdf")) {
                    model.writePDF(file);
                } else if (filename.endsWith(".csv")) {
                    model.writeCSV(file, ';');
                } else if (filename.endsWith(".ods")) {
                    model.writeODS(file);
                }
                
                showInfo("Tiedosto tallennettu: " + file.getName());
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Tallentaminen epäonnistui", e);
                showError("Tallentaminen epäonnistui: " + e.getMessage());
            }
        }
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.initOwner(stage);
        alert.setTitle("Tiedoksi");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.initOwner(stage);
        alert.setTitle("Virhe");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void show() {
        updatePreview();
        stage.show();
    }

    /**
     * Static factory method to create and show print preview.
     * Note: print.setSettings() should be called before this method.
     */
    public static void showPreview(Window owner, PrintModel printModel, Print print) {
        PrintPreviewModel previewModel = new PrintPreviewModel();
        previewModel.setPrintModel(printModel);
        previewModel.setPrint(print);
        
        PrintPreviewStageFX preview = new PrintPreviewStageFX(owner, previewModel);
        preview.show();
    }
}
