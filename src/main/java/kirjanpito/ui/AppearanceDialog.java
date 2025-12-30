package kirjanpito.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;

import kirjanpito.util.AppSettings;

import static kirjanpito.ui.UIConstants.*;

/**
 * Ulkoasun asetukset -dialogi.
 * Mahdollistaa teeman vaihdon lennossa.
 *
 * @since 2.0.2
 * @version 2.0.4 - Updated to use UIConstants and lambda expressions
 */
public class AppearanceDialog extends JDialog {
    private JComboBox<String> themeComboBox;
    private JButton okButton;
    private JButton cancelButton;
    private String originalTheme;
    
    private static final long serialVersionUID = 1L;
    private static final String[] THEME_NAMES = {"Vaalea", "Tumma"};
    private static final String[] THEME_VALUES = {"light", "dark"};
    
    public AppearanceDialog(Frame owner) {
        super(owner, "Ulkoasu", true);
    }
    
    /**
     * Luo ikkunan komponentit.
     */
    public void create() {
        setLayout(new BorderLayout());
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                revertTheme();
                dispose();
            }
        });
        
        createContentPanel();
        createButtonPanel();
        loadSettings();
        
        pack();
        setLocationRelativeTo(getOwner());
    }
    
    private void createContentPanel() {
        GridBagConstraints c;

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(DIALOG_BORDER);
        add(panel, BorderLayout.CENTER);

        // Teema-label
        c = new GridBagConstraints();
        c.insets = createInsets(0, 0, COMPONENT_SPACING, COMPONENT_SPACING);
        c.anchor = GridBagConstraints.WEST;
        panel.add(new JLabel("Teema:"), c);

        // Teema-pudotusvalikko
        themeComboBox = new JComboBox<>(THEME_NAMES);
        themeComboBox.setPreferredSize(new Dimension(150, 25));
        themeComboBox.addActionListener(e -> previewTheme());

        c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;
        c.insets = createInsets(0, 0, COMPONENT_SPACING, 0);
        panel.add(themeComboBox, c);

        // Info-teksti
        JLabel infoLabel = new JLabel("<html><i>Teema vaihtuu heti esikatselua varten.</i></html>");
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 1;
        c.gridwidth = 2;
        c.anchor = GridBagConstraints.WEST;
        c.insets = createInsets(UNIT, 0, 0, 0);
        panel.add(infoLabel, c);
    }
    
    private void createButtonPanel() {
        GridBagConstraints c;

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BUTTON_PANEL_BORDER);
        add(panel, BorderLayout.SOUTH);

        okButton = new JButton("OK");
        okButton.setMnemonic('O');
        okButton.setPreferredSize(BUTTON_SIZE);
        okButton.addActionListener(e -> {
            saveSettings();
            dispose();
        });

        cancelButton = new JButton("Peruuta");
        cancelButton.setMnemonic('P');
        cancelButton.setPreferredSize(BUTTON_SIZE);
        cancelButton.addActionListener(e -> {
            revertTheme();
            dispose();
        });

        c = new GridBagConstraints();
        c.weightx = 1.0;
        c.anchor = GridBagConstraints.EAST;
        c.insets = createInsets(0, 0, 0, UNIT);
        panel.add(cancelButton, c);

        c = new GridBagConstraints();
        panel.add(okButton, c);

        rootPane.setDefaultButton(okButton);
    }
    
    private void loadSettings() {
        AppSettings settings = AppSettings.getInstance();
        originalTheme = settings.getString("ui.theme", "light");
        
        // Aseta oikea valinta
        int selectedIndex = 0;
        for (int i = 0; i < THEME_VALUES.length; i++) {
            if (THEME_VALUES[i].equalsIgnoreCase(originalTheme)) {
                selectedIndex = i;
                break;
            }
        }
        themeComboBox.setSelectedIndex(selectedIndex);
    }
    
    private void previewTheme() {
        int selectedIndex = themeComboBox.getSelectedIndex();
        String themeValue = THEME_VALUES[selectedIndex];
        applyTheme(themeValue);
    }
    
    private void revertTheme() {
        applyTheme(originalTheme);
    }
    
    private void saveSettings() {
        int selectedIndex = themeComboBox.getSelectedIndex();
        String themeValue = THEME_VALUES[selectedIndex];
        
        AppSettings settings = AppSettings.getInstance();
        settings.set("ui.theme", themeValue);
        settings.save();
        
        // Päivitä originalTheme jotta peruutus ei palauta vanhaa
        originalTheme = themeValue;
    }
    
    private void applyTheme(String theme) {
        try {
            if ("dark".equalsIgnoreCase(theme)) {
                FlatDarkLaf.setup();
            } else {
                FlatLightLaf.setup();
            }
            
            // Aseta FlatLaf-spesifit asetukset
            UIManager.put("Button.arc", 10);
            UIManager.put("Component.arc", 10);
            UIManager.put("ProgressBar.arc", 10);
            UIManager.put("TextComponent.arc", 10);
            UIManager.put("ScrollBar.showButtons", true);
            UIManager.put("Table.showHorizontalLines", true);
            UIManager.put("Table.showVerticalLines", true);
            
            // Päivitä kaikki ikkunat
            for (java.awt.Window window : java.awt.Window.getWindows()) {
                SwingUtilities.updateComponentTreeUI(window);
            }
            
        } catch (Exception e) {
            System.err.println("Teeman vaihto epäonnistui: " + e.getMessage());
        }
    }
}
