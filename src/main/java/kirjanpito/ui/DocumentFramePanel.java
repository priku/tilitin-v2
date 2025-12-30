package kirjanpito.ui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Image;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JPanel;

import kirjanpito.db.DataAccessException;
import kirjanpito.models.DocumentModel;
import kirjanpito.util.Registry;

/**
 * JPanel-versio DocumentFrame:sta Compose Desktop SwingPanel-integraatiota varten.
 * 
 * Tämä luokka wrappaa DocumentFrame:n sisällön JPanel:iin, jotta sitä voidaan
 * käyttää Compose Desktop:n SwingPanel-komponentin sisällä.
 * 
 * @author Compose Desktop integration
 */
public class DocumentFramePanel extends JPanel {
    
    private DocumentFrame documentFrame;
    private JFrame containerFrame;
    
    /**
     * Luo DocumentFramePanel.
     * 
     * @param registry Rekisteri
     * @param model Dokumenttimalli
     */
    public DocumentFramePanel(Registry registry, DocumentModel model) {
        super(new BorderLayout());
        
        // Luo DocumentFrame
        this.documentFrame = new DocumentFrame(registry, model);
        
        // Luo piilotettu JFrame joka toimii containerina (tarvitaan menulle ja muille)
        this.containerFrame = new JFrame();
        containerFrame.setUndecorated(true);
        containerFrame.setVisible(false);
        
        // Luo DocumentFrame:n komponentit
        documentFrame.create();
        
        // Siirrä menuBar containerFrameen (ei voi olla JPanel:issa)
        JMenuBar menuBar = documentFrame.getJMenuBar();
        if (menuBar != null) {
            containerFrame.setJMenuBar(menuBar);
        }
        
        // Siirrä DocumentFrame:n sisältö tähän paneliin
        Container contentPane = documentFrame.getContentPane();
        
        // Poista kaikki contentPane:sta ja lisää tähän paneliin
        while (contentPane.getComponentCount() > 0) {
            add(contentPane.getComponent(0), BorderLayout.CENTER);
        }
    }
    
    /**
     * Palauttaa alla olevan DocumentFrame:n.
     * 
     * @return DocumentFrame
     */
    public DocumentFrame getDocumentFrame() {
        return documentFrame;
    }
    
    /**
     * Palauttaa container JFrame:n (menujen hallintaa varten).
     * 
     * @return Container JFrame
     */
    public JFrame getContainerFrame() {
        return containerFrame;
    }
    
    /**
     * Palauttaa menubarin, jotta Compose voi näyttää sen erikseen.
     * 
     * @return JMenuBar tai null
     */
    public JMenuBar getMenuBar() {
        return containerFrame.getJMenuBar();
    }
    
    /**
     * Kutsuu DocumentFrame:n quit-metodia.
     */
    public void quit() {
        documentFrame.quit();
    }
    
    /**
     * Alustaa tietokannan.
     * 
     * @return true jos onnistui
     * @throws DataAccessException jos tietokantavirhe
     */
    public boolean initializeDatabase() throws DataAccessException {
        return documentFrame.model.initialize();
    }
}
