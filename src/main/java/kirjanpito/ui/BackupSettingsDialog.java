package kirjanpito.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

import kirjanpito.util.BackupService;
import kirjanpito.util.RecentDatabases;

import static kirjanpito.ui.UIConstants.*;

/**
 * Dialogi varmuuskopiointiasetuksille - Word AutoSave -tyylinen.
 */
public class BackupSettingsDialog extends JDialog {
    
    private static final long serialVersionUID = 1L;
    
    private JCheckBox enabledCheckBox;
    private JCheckBox autoBackupCheckBox;
    private JSpinner autoIntervalSpinner;
    private JSpinner maxBackupsSpinner;
    private JTextField lastBackupLabel;
    private JButton backupNowButton;
    
    // Tietokantavalinnat
    private JPanel databasesPanel;
    private Map<String, JCheckBox> databaseCheckBoxes = new HashMap<>();
    
    private boolean result = false;
    
    public BackupSettingsDialog(Frame owner) {
        super(owner, "Varmuuskopiointiasetukset", true);
        initComponents();
        loadSettings();
    }
    
    private void initComponents() {
        setLayout(new BorderLayout(COMPONENT_SPACING, COMPONENT_SPACING));

        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(DIALOG_TOP_BORDER);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = TIGHT_INSETS;
        
        // Käytössä-valinta (checkbox + label samassa paneelissa)
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 4;
        gbc.anchor = GridBagConstraints.WEST;
        JPanel enabledPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        enabledCheckBox = new JCheckBox();
        enabledCheckBox.addActionListener(e -> updateEnabledState());
        enabledPanel.add(enabledCheckBox);
        JTextField enabledLabel = createSelectableLabel("Varmuuskopiointi käytössä");
        enabledLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                enabledCheckBox.doClick();
            }
        });
        enabledPanel.add(enabledLabel);
        mainPanel.add(enabledPanel, gbc);
        
        // AutoBackup-valinta (checkbox + label + väli + spinner samalla rivillä)
        gbc.gridy = 1;
        gbc.gridwidth = 4;
        JPanel autoBackupPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        autoBackupCheckBox = new JCheckBox();
        autoBackupCheckBox.setToolTipText("Varmuuskopioi automaattisesti säännöllisin väliajoin (kuten Word AutoSave)");
        autoBackupCheckBox.addActionListener(e -> updateEnabledState());
        autoBackupPanel.add(autoBackupCheckBox);
        JTextField autoBackupLabel = createSelectableLabel("AutoBackup");
        autoBackupLabel.setToolTipText("Varmuuskopioi automaattisesti säännöllisin väliajoin (kuten Word AutoSave)");
        autoBackupLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (autoBackupCheckBox.isEnabled()) {
                    autoBackupCheckBox.doClick();
                }
            }
        });
        autoBackupPanel.add(autoBackupLabel);
        autoBackupPanel.add(Box.createHorizontalStrut(30));
        autoBackupPanel.add(createSelectableLabel("Väli:"));
        autoBackupPanel.add(Box.createHorizontalStrut(5));
        autoIntervalSpinner = new JSpinner(new SpinnerNumberModel(5, 1, 60, 1));
        autoBackupPanel.add(autoIntervalSpinner);
        autoBackupPanel.add(Box.createHorizontalStrut(5));
        autoBackupPanel.add(createSelectableLabel("min"));
        mainPanel.add(autoBackupPanel, gbc);
        
        // Säilytettävien varmuuskopioiden määrä
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        mainPanel.add(createSelectableLabel("Säilytä versioita:"), gbc);
        
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        JPanel spinnerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        maxBackupsSpinner = new JSpinner(new SpinnerNumberModel(10, 1, 100, 1));
        spinnerPanel.add(maxBackupsSpinner);
        mainPanel.add(spinnerPanel, gbc);
        
        // Tietokannat-osio
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 3;
        gbc.anchor = GridBagConstraints.WEST;
        mainPanel.add(createSelectableLabel("Varmuuskopioitavat tietokannat:"), gbc);
        
        gbc.gridy = 4;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        databasesPanel = new JPanel();
        databasesPanel.setLayout(new BoxLayout(databasesPanel, BoxLayout.Y_AXIS));
        databasesPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(javax.swing.UIManager.getColor("Component.borderColor")),
            CONTENT_BORDER));
        mainPanel.add(databasesPanel, gbc);
        
        // Viimeisin varmuuskopio ja tee nyt -painike
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        gbc.insets = SECTION_INSETS;
        lastBackupLabel = createSelectableLabel();
        mainPanel.add(lastBackupLabel, gbc);

        gbc.gridx = 2;
        gbc.gridwidth = 1;
        backupNowButton = new JButton("Tee nyt");
        backupNowButton.addActionListener(e -> performBackupNow());
        mainPanel.add(backupNowButton, gbc);

        // Infoteksti
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 3;
        gbc.insets = SECTION_INSETS;
        javax.swing.JTextArea infoArea = new javax.swing.JTextArea(
            "💡 Määritä backup-sijainnit 'Sijainnit...' napista. 'Tee nyt' varmuuskopioi heti.");
        infoArea.setEditable(false);
        infoArea.setOpaque(false);
        infoArea.setFont(javax.swing.UIManager.getFont("Label.font").deriveFont(11f));
        infoArea.setForeground(javax.swing.UIManager.getColor("Label.disabledForeground"));
        infoArea.setBorder(null);
        mainPanel.add(infoArea, gbc);
        
        // Kääri mainPanel ylös
        JPanel topWrapper = new JPanel(new BorderLayout());
        topWrapper.add(mainPanel, BorderLayout.NORTH);
        add(topWrapper, BorderLayout.CENTER);
        
        // Painikkeet
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        JButton okButton = new JButton("OK");
        okButton.addActionListener(e -> {
            saveSettings();
            result = true;
            dispose();
        });
        buttonPanel.add(okButton);
        
        JButton cancelButton = new JButton("Peruuta");
        cancelButton.addActionListener(e -> dispose());
        buttonPanel.add(cancelButton);
        
        add(buttonPanel, BorderLayout.SOUTH);
        
        getRootPane().setDefaultButton(okButton);
        
        setSize(700, 320);
        setMinimumSize(new Dimension(600, 280));
        setLocationRelativeTo(getOwner());
        setResizable(true);
    }
    
    private void loadSettings() {
        BackupService backupService = BackupService.getInstance();
        
        enabledCheckBox.setSelected(backupService.isEnabled());
        autoBackupCheckBox.setSelected(backupService.isAutoBackupEnabled());
        autoIntervalSpinner.setValue(backupService.getAutoBackupIntervalMinutes());
        maxBackupsSpinner.setValue(backupService.getMaxBackups());
        
        // Lataa tietokannat
        loadDatabases();
        
        updateLastBackupLabel();
        updateEnabledState();
    }
    
    private void loadDatabases() {
        databasesPanel.removeAll();
        databaseCheckBoxes.clear();
        
        BackupService backupService = BackupService.getInstance();
        List<String> selectedDatabases = backupService.getSelectedDatabases();
        
        // Kerää kaikki tietokannat (normalisoituna)
        List<String> allDatabases = new ArrayList<>();
        java.util.Set<String> seenPaths = new java.util.HashSet<>();
        
        // 1. Lisää nykyinen tietokanta
        String currentDb = backupService.getCurrentDatabase();
        if (currentDb != null) {
            String normalizedPath = normalizePath(currentDb);
            if (!seenPaths.contains(normalizedPath)) {
                allDatabases.add(currentDb);
                seenPaths.add(normalizedPath);
            }
        }
        
        // 2. Hae viimeisimmät tietokannat
        for (String db : RecentDatabases.getInstance().getRecentDatabases()) {
            String normalizedPath = normalizePath(db);
            if (!seenPaths.contains(normalizedPath)) {
                allDatabases.add(db);
                seenPaths.add(normalizedPath);
            }
        }
        
        // 3. Skannaa Tilitin-oletuskansio (AppData\Roaming\Tilitin)
        File defaultDir = getDefaultTilitinDirectory();
        if (defaultDir != null && defaultDir.exists()) {
            File[] sqliteFiles = defaultDir.listFiles((dir, name) -> 
                name.toLowerCase().endsWith(".sqlite") || name.toLowerCase().endsWith(".db"));
            
            if (sqliteFiles != null) {
                for (File f : sqliteFiles) {
                    String jdbcUrl = "jdbc:sqlite:" + f.getAbsolutePath().replace('\\', '/');
                    String normalizedPath = normalizePath(jdbcUrl);
                    if (!seenPaths.contains(normalizedPath)) {
                        allDatabases.add(jdbcUrl);
                        seenPaths.add(normalizedPath);
                    }
                }
            }
        }
        
        if (allDatabases.isEmpty()) {
            JTextField noDbLabel = createSelectableLabel("(ei tietokantoja)");
            noDbLabel.setForeground(java.awt.Color.GRAY);
            databasesPanel.add(noDbLabel);
        } else {
            for (String dbUrl : allDatabases) {
                // Näytä vain SQLite-tietokannat
                if (!dbUrl.contains("sqlite")) continue;
                
                // Luo rivi tietokannalle: [checkbox] nimi [sijaintien määrä] [muokkaa-nappi]
                JPanel dbRow = new JPanel(new BorderLayout(5, 0));
                
                // Poimi tiedoston nimi polusta
                String displayName = extractDatabaseName(dbUrl);
                
                JCheckBox cb = new JCheckBox(displayName);
                cb.setToolTipText(dbUrl);
                
                // Valitse oletuksena jos on valituissa tai on nykyinen
                boolean isSelected = selectedDatabases.isEmpty() 
                    ? dbUrl.equals(currentDb)  // Oletus: vain nykyinen
                    : selectedDatabases.contains(dbUrl);
                cb.setSelected(isSelected);
                
                databaseCheckBoxes.put(dbUrl, cb);
                dbRow.add(cb, BorderLayout.CENTER);
                
                // Sijainnit-nappi
                JButton configButton = new JButton("Sijainnit...");
                configButton.setToolTipText("Muokkaa tietokannan backup-sijainteja");
                configButton.addActionListener(e -> openDatabaseConfigDialog(dbUrl));
                
                // Näytä sijaintien määrä
                kirjanpito.util.DatabaseBackupConfig config = backupService.getDatabaseConfig(dbUrl);
                int locCount = config.getLocations().size();
                String locLabel;
                if (locCount == 0) {
                    locLabel = "Ei sijainteja";
                } else if (locCount == 1) {
                    locLabel = "1 sijainti";
                } else {
                    locLabel = locCount + " sijaintia";
                }
                
                JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
                JLabel locCountLabel = new JLabel(locLabel);
                locCountLabel.setForeground(locCount == 0 ? new java.awt.Color(180, 80, 80) : java.awt.Color.GRAY);
                rightPanel.add(locCountLabel);
                rightPanel.add(configButton);
                dbRow.add(rightPanel, BorderLayout.EAST);
                
                databasesPanel.add(dbRow);
            }
        }
        
        databasesPanel.revalidate();
        databasesPanel.repaint();
    }
    
    /**
     * Palauttaa Tilitin:n oletuskansion (AppData\Roaming\Tilitin).
     */
    private File getDefaultTilitinDirectory() {
        String appData = System.getenv("APPDATA");
        if (appData != null) {
            return new File(appData, "Tilitin");
        }
        // Fallback
        String userHome = System.getProperty("user.home");
        return new File(userHome + File.separator + "AppData" + File.separator + "Roaming" + File.separator + "Tilitin");
    }
    
    /**
     * Normalisoi tietokantapolun vertailua varten.
     * Poistaa jdbc:sqlite: prefiksin ja muuttaa pieniksi kirjaimiksi.
     */
    private String normalizePath(String jdbcUrl) {
        String path = jdbcUrl;
        if (path.contains("sqlite:")) {
            path = path.substring(path.indexOf("sqlite:") + 7);
        }
        // Muuta kaikki kenoviivat kauttaviivoiksi ja pieniksi kirjaimiksi
        return path.replace('\\', '/').toLowerCase();
    }
    
    private String extractDatabaseName(String jdbcUrl) {
        // jdbc:sqlite:C:/path/to/kirjanpito.sqlite -> kirjanpito.sqlite
        if (jdbcUrl.contains("sqlite:")) {
            String path = jdbcUrl.substring(jdbcUrl.indexOf("sqlite:") + 7);
            File f = new File(path);
            return f.getName() + " (" + f.getParent() + ")";
        }
        return jdbcUrl;
    }
    
    private void updateLastBackupLabel() {
        BackupService backupService = BackupService.getInstance();
        Date lastBackup = backupService.getLastBackupTime();
        
        if (lastBackup != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("d.M.yyyy HH:mm");
            lastBackupLabel.setText("Viimeisin: " + sdf.format(lastBackup));
        } else {
            lastBackupLabel.setText("Ei aiempia varmuuskopioita");
        }
    }
    
    /**
     * Avaa tietokannan backup-asetusten dialogin.
     */
    private void openDatabaseConfigDialog(String databasePath) {
        DatabaseBackupConfigDialog dialog = new DatabaseBackupConfigDialog(this, databasePath);
        if (dialog.showDialog()) {
            // Päivitä näyttö
            loadDatabases();
        }
    }
    
    private void saveSettings() {
        BackupService backupService = BackupService.getInstance();
        
        backupService.setEnabled(enabledCheckBox.isSelected());
        backupService.setAutoBackupEnabled(autoBackupCheckBox.isSelected());
        backupService.setAutoBackupIntervalMinutes((Integer) autoIntervalSpinner.getValue());
        backupService.setMaxBackups((Integer) maxBackupsSpinner.getValue());
        
        // Tallenna valitut tietokannat
        List<String> selectedDbs = new ArrayList<>();
        for (Map.Entry<String, JCheckBox> entry : databaseCheckBoxes.entrySet()) {
            if (entry.getValue().isSelected()) {
                selectedDbs.add(entry.getKey());
            }
        }
        backupService.setSelectedDatabases(selectedDbs);
        
        backupService.saveSettings();
    }
    
    private void updateEnabledState() {
        boolean enabled = enabledCheckBox.isSelected();
        autoBackupCheckBox.setEnabled(enabled);
        autoIntervalSpinner.setEnabled(enabled && autoBackupCheckBox.isSelected());
        maxBackupsSpinner.setEnabled(enabled);
        // "Tee nyt" -nappi on aina käytössä - manuaalinen backup toimii ilman automaattista backupia
    }
    
    private void performBackupNow() {
        // Tallenna ensin asetukset
        saveSettings();
        
        BackupService backupService = BackupService.getInstance();
        
        // Tarkista onko tietokantoja valittu
        java.util.List<String> selectedDbs = backupService.getSelectedDatabases();
        if (selectedDbs == null || selectedDbs.isEmpty()) {
            showCopyableMessage(this,
                "Ei valittuja tietokantoja varmuuskopioitavaksi.\n\n" +
                "Valitse vähintään yksi tietokanta listasta.",
                "Varmuuskopiointi",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Suorita varmuuskopiointi kaikille valituille tietokannoille
        int successCount = 0;
        int failCount = 0;
        StringBuilder results = new StringBuilder();
        
        for (String dbUrl : selectedDbs) {
            try {
                String dbName = extractDbName(dbUrl);
                kirjanpito.util.DatabaseBackupConfig dbConfig = backupService.getDatabaseConfig(dbUrl);
                java.util.List<kirjanpito.util.BackupLocation> locations = dbConfig.getLocations();
                
                // Jos ei ole sijainteja, varoita
                if (locations.isEmpty()) {
                    results.append("⚠ ").append(dbName).append(" (ei sijainteja - lisää 'Sijainnit...')\n");
                    failCount++;
                    continue;
                }
                
                int dbSuccess = 0;
                int dbFail = 0;
                
                // Tee backup kaikkiin sijainteihin
                for (kirjanpito.util.BackupLocation location : locations) {
                    boolean success = backupService.performBackupToLocation(dbUrl, location, dbConfig.getMaxBackupsPerLocation());
                    if (success) {
                        dbSuccess++;
                    } else {
                        dbFail++;
                    }
                }
                
                if (dbFail == 0 && dbSuccess > 0) {
                    successCount++;
                    results.append("✓ ").append(dbName).append(" (").append(dbSuccess).append(" sijaintiin)\n");
                } else if (dbSuccess > 0) {
                    successCount++;
                    failCount++;
                    results.append("⚠ ").append(dbName).append(" (").append(dbSuccess).append("/").append(dbSuccess + dbFail).append(" sijaintiin)\n");
                } else {
                    failCount++;
                    results.append("✗ ").append(dbName).append(" (epäonnistui)\n");
                }
            } catch (Exception e) {
                failCount++;
                results.append("✗ ").append(extractDbName(dbUrl)).append(": ").append(e.getMessage()).append("\n");
            }
        }
        
        // Näytä tulos
        String message;
        int messageType;
        if (failCount == 0 && successCount > 0) {
            message = "Varmuuskopiointi onnistui!\n\n" +
                      successCount + " tietokantaa kopioitu:\n" + results.toString();
            messageType = JOptionPane.INFORMATION_MESSAGE;
        } else if (successCount > 0) {
            message = "Varmuuskopiointi osittain onnistui.\n\n" +
                      results.toString();
            messageType = JOptionPane.WARNING_MESSAGE;
        } else {
            message = "Varmuuskopiointi epäonnistui!\n\n" + results.toString() +
                      "\nLisää backup-sijainteja 'Sijainnit...' napista.";
            messageType = JOptionPane.ERROR_MESSAGE;
        }
        
        showCopyableMessage(this, message, "Varmuuskopiointi", messageType);
        
        // Päivitä viimeisin backup -teksti
        updateLastBackupLabel();
    }
    
    /**
     * Palauttaa tietokannan nimen URL:sta.
     */
    private String extractDbName(String dbUrl) {
        if (dbUrl == null) return "tuntematon";
        String path = dbUrl.replace("jdbc:sqlite:", "");
        int lastSlash = Math.max(path.lastIndexOf('/'), path.lastIndexOf('\\'));
        return lastSlash >= 0 ? path.substring(lastSlash + 1) : path;
    }
    
    /**
     * Näyttää viestin jonka tekstin voi kopioida.
     */
    private void showCopyableMessage(java.awt.Component parent, String message, String title, int messageType) {
        javax.swing.JTextArea textArea = new javax.swing.JTextArea(message);
        textArea.setEditable(false);
        textArea.setOpaque(false);
        textArea.setFont(javax.swing.UIManager.getFont("Label.font"));
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        
        // Laske sopiva koko
        int width = Math.min(400, message.length() * 7);
        int height = Math.min(300, (message.split("\n").length + 2) * 20);
        textArea.setPreferredSize(new Dimension(width, height));
        
        JOptionPane.showMessageDialog(parent, textArea, title, messageType);
    }
    
    /**
     * Luo JTextFieldin joka näyttää JLabelilta mutta josta voi kopioida tekstiä.
     */
    private JTextField createSelectableLabel() {
        return createSelectableLabel("");
    }
    
    /**
     * Luo JTextFieldin joka näyttää JLabelilta mutta josta voi kopioida tekstiä.
     */
    private JTextField createSelectableLabel(String text) {
        JTextField field = new JTextField(text);
        field.setEditable(false);
        field.setBorder(null);
        field.setOpaque(false);
        field.setFont(javax.swing.UIManager.getFont("Label.font"));
        field.setForeground(javax.swing.UIManager.getColor("Label.foreground"));
        return field;
    }
    
    public boolean showDialog() {
        setVisible(true);
        return result;
    }
    
    public static boolean show(Component parent) {
        Frame frame = parent != null ? 
            (Frame) javax.swing.SwingUtilities.getWindowAncestor(parent) : null;
        BackupSettingsDialog dialog = new BackupSettingsDialog(frame);
        return dialog.showDialog();
    }
}
