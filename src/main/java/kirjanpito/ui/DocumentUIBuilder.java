package kirjanpito.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;

import kirjanpito.models.TextFieldWithLockIcon;
import kirjanpito.ui.resources.Resources;

/**
 * Hallinnoi DocumentFrame:n UI-komponenttien luomista.
 * 
 * Tämä luokka eriytetään DocumentFrame:stä Phase 7 -refaktoroinnin osana.
 * Se vastaa kaikkien UI-komponenttien luomisesta ja konfiguroinnista.
 * 
 * @since 2.2.4
 */
public class DocumentUIBuilder {
    
    private final UICallbacks callbacks;
    
    /**
     * Callback-rajapinta DocumentFrame:lle.
     */
    public interface UICallbacks {
        /** Kutsutaan kun tositenumero-kenttä muuttuu */
        void onNumberFieldChanged();
        
        /** Kutsutaan kun päivämäärä-kenttä muuttuu */
        void onDateFieldChanged();
        
        /** Kutsutaan kun haku-painiketta klikataan */
        void onSearchButtonClicked();
        
        /** Kutsutaan kun haku-paneeli suljetaan */
        void onSearchPanelClosed();
        
        /** Palauttaa addEntry-actionin */
        Action getAddEntryAction();
        
        /** Palauttaa searchListener-actionin */
        ActionListener getSearchListener();
    }
    
    /**
     * Komponenttien viitteet.
     */
    public static class UIComponents {
        public TextFieldWithLockIcon numberTextField;
        public DateTextField dateTextField;
        public JLabel debitTotalLabel;
        public JLabel creditTotalLabel;
        public JLabel differenceLabel;
        public JPanel searchPanel;
        public JTextField searchPhraseTextField;
        public JLabel documentLabel;
        public JLabel periodLabel;
        public JLabel documentTypeLabel;
        public JLabel backupStatusLabel;
    }
    
    private final UIComponents components;
    
    /**
     * Konstruktori.
     * 
     * @param callbacks Callback-rajapinta DocumentFrame:lle
     */
    public DocumentUIBuilder(UICallbacks callbacks) {
        this.callbacks = callbacks;
        this.components = new UIComponents();
    }
    
    /**
     * Palauttaa luodut komponentit.
     */
    public UIComponents getComponents() {
        return components;
    }
    
    /**
     * Luo tekstikenttäpaneelin (tositenumero ja päivämäärä).
     * 
     * @param container Paneeli, johon komponentit lisätään
     */
    public void createTextFieldPanel(JPanel container) {
        GridBagConstraints c;
        JPanel panel = new JPanel();
        container.add(panel, BorderLayout.PAGE_START);
        panel.setLayout(new GridLayout(0, 2));

        JPanel left = new JPanel();
        left.setLayout(new GridBagLayout());
        panel.add(left);

        /* Lisätään paneeliin tositenumeronimiö ja -tekstikenttä. */
        JLabel numberLabel = new JLabel("Tositenumero");
        c = new GridBagConstraints();
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(8, 8, 8, 4);
        left.add(numberLabel, c);

        components.numberTextField = new TextFieldWithLockIcon();
        components.numberTextField.setCaret(new javax.swing.text.DefaultCaret());
        components.numberTextField.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                if (components.numberTextField.isEditable()) {
                    callbacks.onNumberFieldChanged();
                }
            }
        });

        components.numberTextField.getInputMap().put(
                KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "transferFocus");

        components.numberTextField.getActionMap().put("transferFocus", new AbstractAction() {
            private static final long serialVersionUID = 1L;
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                components.numberTextField.transferFocus();
            }
        });

        c = new GridBagConstraints();
        c.insets = new Insets(8, 4, 8, 4);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;
        left.add(components.numberTextField, c);

        c.fill = GridBagConstraints.VERTICAL;
        c.weightx = 0.0;
        left.add(new JSeparator(SwingConstants.VERTICAL), c);

        JPanel right = new JPanel();
        right.setLayout(new GridBagLayout());
        panel.add(right);

        /* Lisätään paneeliin päivämääränimiö ja -tekstikenttä. */
        JLabel dateLabel = new JLabel("Päivämäärä");
        c = new GridBagConstraints();
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(8, 8, 8, 4);
        right.add(dateLabel, c);

        components.dateTextField = new DateTextField();
        components.dateTextField.setCaret(new javax.swing.text.DefaultCaret());
        components.dateTextField.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                if (components.dateTextField.isEditable() && Character.isDigit(e.getKeyChar())) {
                    callbacks.onDateFieldChanged();
                }
            }
        });

        components.dateTextField.getInputMap().put(
                KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "addEntry");

        components.dateTextField.getActionMap().put("addEntry", callbacks.getAddEntryAction());

        components.dateTextField.getInputMap().put(
                KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "firstEntry");

        components.dateTextField.getActionMap().put("firstEntry", new AbstractAction() {
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                components.dateTextField.transferFocus();
                // Note: First entry selection logic will be handled by DocumentFrame
            }
        });

        c = new GridBagConstraints();
        c.insets = new Insets(8, 4, 8, 8);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;
        right.add(components.dateTextField, c);
    }
    
    /**
     * Luo summarivin (debet/kredit yhteenvedot).
     * 
     * @param container Paneeli, johon rivi lisätään
     */
    public void createTotalRow(JPanel container) {
        GridBagLayout layout = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(2, 2, 2, 6);
        c.anchor = GridBagConstraints.WEST;

        JPanel panel = new JPanel(layout);
        panel.setBorder(BorderFactory.createEmptyBorder(2, 2, 5, 2));
        container.add(panel);

        components.debitTotalLabel = new JLabel("0,00");
        Dimension minSize = components.debitTotalLabel.getMinimumSize();
        components.debitTotalLabel.setPreferredSize(new Dimension(80, minSize.height));
        components.creditTotalLabel = new JLabel("0,00");
        components.creditTotalLabel.setPreferredSize(new Dimension(80, minSize.height));
        components.differenceLabel = new JLabel("0,00");
        components.differenceLabel.setPreferredSize(new Dimension(80, minSize.height));

        panel.add(new JLabel("Debet yht."), c);
        panel.add(components.debitTotalLabel, c);
        panel.add(new JLabel("Kredit yht."), c);
        panel.add(components.creditTotalLabel, c);
        panel.add(new JLabel("Erotus"), c);
        c.weightx = 1.0;
        panel.add(components.differenceLabel, c);
    }
    
    /**
     * Luo hakupalkin.
     * 
     * @param container Paneeli, johon hakupalkki lisätään
     */
    public void createSearchBar(JPanel container) {
        GridBagLayout layout = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(8, 5, 8, 5);
        c.anchor = GridBagConstraints.WEST;

        JPanel panel = components.searchPanel = new JPanel(layout);
        panel.setBorder(BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));
        panel.setVisible(false);
        container.add(panel);

        JTextField textField = components.searchPhraseTextField = new JTextField();
        textField.setCaret(new javax.swing.text.DefaultCaret());
        textField.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    callbacks.onSearchButtonClicked();
                    e.consume();
                }
            }
        });
        c.weightx = 1.0;
        c.fill = GridBagConstraints.HORIZONTAL;
        panel.add(textField, c);

        JButton button = new JButton("Etsi", new ImageIcon(
                Resources.loadAsImage("find-16x16.png")));

        button.addActionListener(e -> callbacks.onSearchButtonClicked());
        button.setMnemonic('H');
        c.weightx = 0.0;
        c.fill = GridBagConstraints.BOTH;
        panel.add(button, c);

        button = new JButton(new ImageIcon(Resources.loadAsImage("close-16x16.png")));
        button.addActionListener(callbacks.getSearchListener());
        panel.add(button, c);
    }
    
    /**
     * Luo tilarivin (status bar).
     * 
     * @param frame JFrame, johon status bar lisätään
     */
    public void createStatusBar(javax.swing.JFrame frame) {
        JPanel statusBarPanel = new JPanel(new BorderLayout());
        components.documentLabel = new JLabel();
        components.documentLabel.setBorder(new javax.swing.border.EtchedBorder());
        components.documentLabel.setPreferredSize(new Dimension(150, 0));
        components.periodLabel = new JLabel(" ");
        components.periodLabel.setBorder(new javax.swing.border.EtchedBorder());
        components.documentTypeLabel = new JLabel();
        components.documentTypeLabel.setBorder(new javax.swing.border.EtchedBorder());
        components.documentTypeLabel.setPreferredSize(new Dimension(200, 0));
        
        // Backup-indikaattori (Word AutoSave -tyylinen)
        components.backupStatusLabel = new JLabel();
        components.backupStatusLabel.setBorder(new javax.swing.border.EtchedBorder());
        components.backupStatusLabel.setPreferredSize(new Dimension(120, 0));
        components.backupStatusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        // Backup-indikaattoria klikkaamalla avautuu asetukset
        components.backupStatusLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                // Callback will be set by DocumentFrame
            }
        });
        components.backupStatusLabel.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        components.backupStatusLabel.setToolTipText("Klikkaa muuttaaksesi varmuuskopiointiasetuksia");
        
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.add(components.backupStatusLabel, BorderLayout.WEST);
        rightPanel.add(components.documentTypeLabel, BorderLayout.CENTER);
        
        statusBarPanel.add(components.documentLabel, BorderLayout.WEST);
        statusBarPanel.add(components.periodLabel, BorderLayout.CENTER);
        statusBarPanel.add(rightPanel, BorderLayout.EAST);
        frame.add(statusBarPanel, BorderLayout.PAGE_END);
    }
}

