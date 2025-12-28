package kirjanpito.ui;

import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.table.*;

import kirjanpito.util.BackupLocation;
import kirjanpito.util.BackupService;
import kirjanpito.util.CloudStorageDetector;
import kirjanpito.util.CloudStorageDetector.CloudService;
import kirjanpito.util.CloudStorageDetector.StorageLocation;
import kirjanpito.util.DatabaseBackupConfig;

/**
 * Dialogi yksittäisen tietokannan backup-sijaintien hallintaan.
 */
public class DatabaseBackupConfigDialog extends JDialog {
    
    private static final long serialVersionUID = 1L;
    
    private final String databasePath;
    private final String databaseName;
    private DatabaseBackupConfig config;
    
    private JCheckBox enabledCheckBox;
    private JSpinner maxBackupsSpinner;
    private JTable locationsTable;
    private DefaultTableModel tableModel;
    private JButton addCloudButton;
    private JButton addUsbButton;
    private JButton addLocalButton;
    private JButton removeButton;
    
    private boolean result = false;
    
    public DatabaseBackupConfigDialog(Dialog owner, String databasePath) {
        super(owner, "Tietokannan backup-asetukset", true);
        this.databasePath = databasePath;
        this.databaseName = extractDatabaseName(databasePath);
        
        // Lataa tai luo konfiguraatio
        this.config = BackupService.getInstance().getDatabaseConfig(databasePath);
        
        initComponents();
        loadConfig();
        pack();
        setLocationRelativeTo(owner);
    }
    
    private String extractDatabaseName(String path) {
        if (path == null) return "tuntematon";
        String p = path.replace("jdbc:sqlite:", "");
        int lastSlash = Math.max(p.lastIndexOf('/'), p.lastIndexOf('\\'));
        return lastSlash >= 0 ? p.substring(lastSlash + 1) : p;
    }
    
    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        // Yläosa: tietokannan nimi ja perusasetukset
        JPanel topPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(3, 5, 3, 5);
        
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        JLabel nameLabel = new JLabel("Tietokanta: " + databaseName);
        nameLabel.setFont(nameLabel.getFont().deriveFont(Font.BOLD));
        topPanel.add(nameLabel, gbc);
        
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        enabledCheckBox = new JCheckBox("Varmuuskopiointi käytössä tälle tietokannalle");
        topPanel.add(enabledCheckBox, gbc);
        
        gbc.gridy = 2;
        topPanel.add(new JLabel("Säilytä versioita per sijainti:"), gbc);
        
        gbc.gridx = 1;
        maxBackupsSpinner = new JSpinner(new SpinnerNumberModel(10, 1, 100, 1));
        topPanel.add(maxBackupsSpinner, gbc);
        
        mainPanel.add(topPanel, BorderLayout.NORTH);
        
        // Keskiosa: sijaintitaulukko
        String[] columns = {"Tyyppi", "Sijainti", "AutoBackup"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public Class<?> getColumnClass(int column) {
                if (column == 2) return Boolean.class;
                return String.class;
            }
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 2; // Vain AutoBackup-sarake muokattavissa
            }
        };
        locationsTable = new JTable(tableModel);
        locationsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        locationsTable.getColumnModel().getColumn(0).setPreferredWidth(80);
        locationsTable.getColumnModel().getColumn(1).setPreferredWidth(300);
        locationsTable.getColumnModel().getColumn(2).setPreferredWidth(80);
        
        JScrollPane scrollPane = new JScrollPane(locationsTable);
        scrollPane.setPreferredSize(new Dimension(500, 150));
        
        JPanel centerPanel = new JPanel(new BorderLayout(5, 5));
        centerPanel.add(new JLabel("Backup-sijainnit:"), BorderLayout.NORTH);
        centerPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Sijainti-painikkeet
        JPanel locationButtons = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        
        addCloudButton = new JButton("☁ Lisää pilvi");
        addCloudButton.addActionListener(e -> addCloudLocation());
        locationButtons.add(addCloudButton);
        
        addUsbButton = new JButton("⛁ Lisää USB");
        addUsbButton.addActionListener(e -> addUsbLocation());
        locationButtons.add(addUsbButton);
        
        addLocalButton = new JButton("📁 Lisää kansio");
        addLocalButton.addActionListener(e -> addLocalLocation());
        locationButtons.add(addLocalButton);
        
        removeButton = new JButton("Poista");
        removeButton.addActionListener(e -> removeSelectedLocation());
        locationButtons.add(removeButton);
        
        centerPanel.add(locationButtons, BorderLayout.SOUTH);
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        
        add(mainPanel, BorderLayout.CENTER);
        
        // Alapainikkeet
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        JButton okButton = new JButton("OK");
        okButton.addActionListener(e -> {
            saveConfig();
            result = true;
            dispose();
        });
        buttonPanel.add(okButton);
        
        JButton cancelButton = new JButton("Peruuta");
        cancelButton.addActionListener(e -> dispose());
        buttonPanel.add(cancelButton);
        
        add(buttonPanel, BorderLayout.SOUTH);
        
        getRootPane().setDefaultButton(okButton);
    }
    
    private void loadConfig() {
        enabledCheckBox.setSelected(config.isEnabled());
        maxBackupsSpinner.setValue(config.getMaxBackupsPerLocation());
        
        tableModel.setRowCount(0);
        
        // Jos ei ole omia sijainteja, lisää globaali backup-kansio jos sellainen on
        if (config.getLocations().isEmpty()) {
            addGlobalBackupLocationIfExists();
        }
        
        for (BackupLocation loc : config.getLocations()) {
            tableModel.addRow(new Object[]{
                loc.getType().getDisplayName(),
                loc.getPath(),
                loc.isAutoBackup()
            });
        }
    }
    
    /**
     * Lisää globaalin backup-kansion sijainniksi jos sellainen on määritelty.
     */
    private void addGlobalBackupLocationIfExists() {
        BackupService backupService = BackupService.getInstance();
        File globalDir = backupService.getBackupDirectory();
        
        if (globalDir != null && globalDir.exists()) {
            String path = globalDir.getAbsolutePath();
            
            // Tunnista onko pilvi, USB vai paikallinen
            BackupLocation.LocationType type = BackupLocation.LocationType.LOCAL;
            String serviceName = null;
            
            // Tarkista pilvipalvelut
            for (CloudStorageDetector.CloudService cloud : CloudStorageDetector.detectAll()) {
                if (path.toLowerCase().startsWith(cloud.getPath().toLowerCase())) {
                    type = BackupLocation.LocationType.CLOUD;
                    serviceName = cloud.getName();
                    break;
                }
            }
            
            // Tarkista USB-asemat
            if (type == BackupLocation.LocationType.LOCAL) {
                for (CloudStorageDetector.StorageLocation usb : CloudStorageDetector.detectUsbDrives()) {
                    if (path.toLowerCase().startsWith(usb.getPath().toLowerCase())) {
                        type = BackupLocation.LocationType.USB;
                        serviceName = usb.getName();
                        break;
                    }
                }
            }
            
            // Lisää sijainti configiin
            BackupLocation location;
            if (type == BackupLocation.LocationType.CLOUD) {
                location = BackupLocation.cloud(path, serviceName != null ? serviceName : "Pilvi");
            } else if (type == BackupLocation.LocationType.USB) {
                location = BackupLocation.usb(path, serviceName != null ? serviceName : "USB");
            } else {
                location = BackupLocation.local(path, true);
            }
            config.addLocation(location);
        }
    }
    
    private void saveConfig() {
        config.setEnabled(enabledCheckBox.isSelected());
        config.setMaxBackupsPerLocation((Integer) maxBackupsSpinner.getValue());
        
        // Päivitä sijainnit taulukosta
        List<BackupLocation> locations = new ArrayList<>();
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            String typeStr = (String) tableModel.getValueAt(i, 0);
            String path = (String) tableModel.getValueAt(i, 1);
            Boolean autoBackup = (Boolean) tableModel.getValueAt(i, 2);
            
            BackupLocation.LocationType type;
            String serviceName = null;
            
            if ("Pilvi".equals(typeStr)) {
                type = BackupLocation.LocationType.CLOUD;
                // Yritä tunnistaa pilvipalvelun nimi
                for (CloudService cloud : CloudStorageDetector.detectAll()) {
                    if (path.startsWith(cloud.getPath())) {
                        serviceName = cloud.getName();
                        break;
                    }
                }
            } else if ("USB".equals(typeStr)) {
                type = BackupLocation.LocationType.USB;
            } else {
                type = BackupLocation.LocationType.LOCAL;
            }
            
            locations.add(new BackupLocation(path, type, serviceName, autoBackup));
        }
        config.setLocations(locations);
        
        // Tallenna BackupServiceen
        BackupService.getInstance().setDatabaseConfig(databasePath, config);
    }
    
    private void addCloudLocation() {
        List<CloudService> clouds = CloudStorageDetector.detectAll();
        
        if (clouds.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Pilvipalvelua ei havaittu.\n\n" +
                "Tuetut palvelut: Google Drive, OneDrive, iCloud Drive, Dropbox",
                "Ei pilvipalvelua",
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        // Suodata pois jo lisätyt sijainnit
        List<CloudService> availableClouds = clouds.stream()
            .filter(c -> !isLocationAlreadyAdded(c.getBackupPath()))
            .toList();
        
        if (availableClouds.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Kaikki havaitut pilvipalvelut on jo lisätty.",
                "Ei lisättäviä",
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        // Valitse pilvipalvelu
        String[] options = availableClouds.stream()
            .map(c -> c.getName() + " (" + c.getPath() + ")")
            .toArray(String[]::new);
        
        String selected = (String) JOptionPane.showInputDialog(this,
            "Valitse pilvipalvelu:",
            "Lisää pilvi-sijainti",
            JOptionPane.QUESTION_MESSAGE,
            null,
            options,
            options[0]);
        
        if (selected != null) {
            for (CloudService cloud : availableClouds) {
                if (selected.startsWith(cloud.getName())) {
                    String backupPath = cloud.getBackupPath();
                    tableModel.addRow(new Object[]{
                        "Pilvi",
                        backupPath,
                        true // Pilvi on oletuksena autobackup
                    });
                    break;
                }
            }
        }
    }
    
    private void addUsbLocation() {
        List<StorageLocation> usbDrives = CloudStorageDetector.detectUsbDrives();
        
        if (usbDrives.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "USB-asemaa ei havaittu.\n\n" +
                "Liitä USB-tikku tai ulkoinen kovalevy ja yritä uudelleen.",
                "Ei USB-asemaa",
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        // Suodata pois jo lisätyt
        List<StorageLocation> availableUsb = usbDrives.stream()
            .filter(u -> !isLocationAlreadyAdded(u.getBackupPath()))
            .toList();
        
        if (availableUsb.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Kaikki havaitut USB-asemat on jo lisätty.",
                "Ei lisättäviä",
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        String[] options = availableUsb.stream()
            .map(u -> u.getDriveLabel() + " (" + u.getPath() + ")")
            .toArray(String[]::new);
        
        String selected = (String) JOptionPane.showInputDialog(this,
            "Valitse USB-asema:",
            "Lisää USB-sijainti",
            JOptionPane.QUESTION_MESSAGE,
            null,
            options,
            options[0]);
        
        if (selected != null) {
            for (StorageLocation usb : availableUsb) {
                if (selected.startsWith(usb.getDriveLabel())) {
                    String backupPath = usb.getBackupPath();
                    tableModel.addRow(new Object[]{
                        "USB",
                        backupPath,
                        false // USB on oletuksena manuaalinen
                    });
                    break;
                }
            }
        }
    }
    
    private void addLocalLocation() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setDialogTitle("Valitse varmuuskopiokansio");
        
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File selected = chooser.getSelectedFile();
            String backupPath = selected.getAbsolutePath() + File.separator + "Tilitin Backups";
            
            if (isLocationAlreadyAdded(backupPath)) {
                JOptionPane.showMessageDialog(this,
                    "Tämä sijainti on jo lisätty.",
                    "Sijainti jo olemassa",
                    JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            // Kysy onko autobackup
            int autoAnswer = JOptionPane.showConfirmDialog(this,
                "Käytetäänkö tätä sijaintia automaattiseen varmuuskopiointiin?",
                "AutoBackup",
                JOptionPane.YES_NO_OPTION);
            
            tableModel.addRow(new Object[]{
                "Paikallinen",
                backupPath,
                autoAnswer == JOptionPane.YES_OPTION
            });
        }
    }
    
    private void removeSelectedLocation() {
        int row = locationsTable.getSelectedRow();
        if (row >= 0) {
            tableModel.removeRow(row);
        }
    }
    
    private boolean isLocationAlreadyAdded(String path) {
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            if (path.equals(tableModel.getValueAt(i, 1))) {
                return true;
            }
        }
        return false;
    }
    
    public boolean showDialog() {
        setVisible(true);
        return result;
    }
}
