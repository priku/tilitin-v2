package kirjanpito.ui;

import java.awt.Image;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFrame;

import kirjanpito.db.DataAccessException;
import kirjanpito.models.PrintPreviewModel;
import kirjanpito.reports.Print;
import kirjanpito.reports.PrintModel;
import kirjanpito.util.Registry;
import kirjanpito.util.SwingUtils;

/**
 * Hallinnoi print-toiminnallisuutta DocumentFrame:lle.
 * 
 * Tämä luokka eriytetään DocumentFrame:stä Phase 5 -refaktoroinnin osana.
 * Se vastaa PrintPreviewFrame-instanssin hallinnasta ja print preview -ikkunan
 * näyttämisestä.
 * 
 * @since 2.2.0
 */
public class DocumentPrinter {
    
    private final JFrame parentFrame;
    private final Registry registry;
    
    private PrintPreviewFrame printPreviewFrame;
    
    private static final Logger logger = Logger.getLogger("kirjanpito");
    
    /**
     * Konstruktori.
     * 
     * @param parentFrame Pääikkuna (DocumentFrame)
     * @param registry Registry-instanssi
     */
    public DocumentPrinter(JFrame parentFrame, Registry registry) {
        this.parentFrame = parentFrame;
        this.registry = registry;
    }
    
    /**
     * Näyttää tulosteiden esikatseluikkunan.
     *
     * @param printModel tulosteen malli
     * @param print tuloste
     */
    public void showPrintPreview(PrintModel printModel, Print print) {
        try {
            printModel.run();
        }
        catch (DataAccessException e) {
            String message = "Tulosteen luominen epäonnistui";
            logger.log(Level.SEVERE, message, e);
            SwingUtils.showDataAccessErrorMessage(parentFrame, e, message);
            return;
        }

        print.setSettings(registry.getSettings());
        PrintPreviewModel previewModel;

        if (printPreviewFrame == null) {
            previewModel = new PrintPreviewModel();
            printPreviewFrame = new PrintPreviewFrame(parentFrame, previewModel);
            Image iconImage = parentFrame.getIconImage();
            if (iconImage != null) {
                printPreviewFrame.setIconImage(iconImage);
            }
            printPreviewFrame.create();
        }
        else {
            previewModel = printPreviewFrame.getModel();
        }

        previewModel.setPrintModel(printModel);
        previewModel.setPrint(print);
        printPreviewFrame.updatePrint();
        printPreviewFrame.setVisible(true);
    }

    /**
     * Sulkee tulosteiden esikatseluikkunan, jos se on auki.
     */
    public void closePrintPreview() {
        if (printPreviewFrame != null) {
            printPreviewFrame.setVisible(false);
        }
    }
    
    /**
     * Sulkee ja vapauttaa print preview ikkunan.
     * Kutsutaan kun DocumentFrame suljetaan.
     */
    public void disposePrintPreview() {
        if (printPreviewFrame != null) {
            printPreviewFrame.close();
            printPreviewFrame = null;
        }
    }
    
    /**
     * Palauttaa PrintPreviewFrame-instanssin.
     * 
     * @return PrintPreviewFrame tai null jos ei ole luotu
     */
    public PrintPreviewFrame getPrintPreviewFrame() {
        return printPreviewFrame;
    }
}

