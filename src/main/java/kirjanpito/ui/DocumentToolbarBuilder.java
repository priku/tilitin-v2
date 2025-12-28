package kirjanpito.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JToolBar;

/**
 * Rakentaa DocumentFrame:n työkalurivin.
 * 
 * Eriytetty DocumentFrame:sta modulaarisuuden parantamiseksi.
 * 
 * @author Tilitin Team
 */
public class DocumentToolbarBuilder {
    
    // Button references (need to be accessed by DocumentFrame)
    private JButton prevButton;
    private JButton nextButton;
    private JButton newDocButton;
    private JButton addEntryButton;
    private JButton removeEntryButton;
    private JButton findByNumberButton;
    private JButton searchButton;
    
    /**
     * Luo työkalurivin kaikilla painikkeilla.
     * 
     * @param listeners Kuuntelijat painikkeiden toiminnoille
     * @return Valmis työkalurivi
     */
    public JToolBar build(ToolbarListeners listeners) {
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        toolBar.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        
        // Navigointi-osio
        prevButton = SwingUtils.createToolButton("go-previous-22x22.png",
                "Edellinen tosite (Page Up)", listeners.prevDocListener, false);
        
        nextButton = SwingUtils.createToolButton("go-next-22x22.png",
                "Seuraava tosite (Page Down)", listeners.nextDocListener, false);
        
        toolBar.add(prevButton);
        toolBar.add(nextButton);
        toolBar.addSeparator(new Dimension(16, 0));
        
        // Tosite-osio
        newDocButton = SwingUtils.createToolButton("document-new-22x22.png",
                "Uusi tosite (Ctrl+N)", listeners.newDocListener, true);
        
        toolBar.add(newDocButton);
        toolBar.addSeparator(new Dimension(16, 0));
        
        // Vienti-osio
        addEntryButton = SwingUtils.createToolButton("list-add-22x22.png",
                "Lisää vienti (F8)", listeners.addEntryListener, true);
        
        removeEntryButton = SwingUtils.createToolButton("list-remove-22x22.png",
                "Poista vienti", listeners.removeEntryListener, true);
        
        toolBar.add(addEntryButton);
        toolBar.add(removeEntryButton);
        toolBar.addSeparator(new Dimension(16, 0));
        
        // Haku-osio
        findByNumberButton = SwingUtils.createToolButton("jump-22x22.png",
                "Hae numerolla (Ctrl+G)", listeners.findDocumentByNumberListener, true);
        
        searchButton = SwingUtils.createToolButton("find-22x22.png",
                "Etsi (Ctrl+F)", listeners.searchListener, true);
        
        toolBar.add(findByNumberButton);
        toolBar.add(searchButton);
        
        return toolBar;
    }
    
    /**
     * Lisää työkalurivin annettuun paneliin PAGE_START-sijaintiin.
     * 
     * @param panel Paneeli johon työkalurivi lisätään
     * @param listeners Kuuntelijat painikkeiden toiminnoille
     */
    public void addToPanel(JPanel panel, ToolbarListeners listeners) {
        JToolBar toolBar = build(listeners);
        panel.add(toolBar, BorderLayout.PAGE_START);
    }
    
    // Getters for button references
    public JButton getPrevButton() { return prevButton; }
    public JButton getNextButton() { return nextButton; }
    public JButton getNewDocButton() { return newDocButton; }
    public JButton getAddEntryButton() { return addEntryButton; }
    public JButton getRemoveEntryButton() { return removeEntryButton; }
    public JButton getFindByNumberButton() { return findByNumberButton; }
    public JButton getSearchButton() { return searchButton; }
    
    /**
     * Sisältää kaikki työkalurivin tarvitsemat kuuntelijat.
     */
    public static class ToolbarListeners {
        public ActionListener prevDocListener;
        public ActionListener nextDocListener;
        public ActionListener newDocListener;
        public ActionListener addEntryListener;
        public ActionListener removeEntryListener;
        public ActionListener findDocumentByNumberListener;
        public ActionListener searchListener;
    }
}
