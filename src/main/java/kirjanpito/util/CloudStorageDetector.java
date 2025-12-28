package kirjanpito.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.swing.filechooser.FileSystemView;

/**
 * Tunnistaa pilvipalvelut ja USB-asemat varmuuskopiointia varten.
 * Ei vaadi API-avaimia - toimii paikallisten kansioiden perusteella.
 */
public class CloudStorageDetector {
    
    private static final Logger logger = Logger.getLogger("kirjanpito");
    private static final String BACKUP_FOLDER_NAME = "Tilitin Backups";
    
    public enum StorageType {
        GOOGLE_DRIVE("Google Drive", "☁"),
        ONEDRIVE("OneDrive", "☁"),
        DROPBOX("Dropbox", "☁"),
        ICLOUD("iCloud Drive", "☁"),
        USB_DRIVE("USB-asema", "⛁"),
        LOCAL("Paikallinen", "💾");
        
        private final String displayName;
        private final String icon;
        
        StorageType(String displayName, String icon) {
            this.displayName = displayName;
            this.icon = icon;
        }
        
        public String getDisplayName() { return displayName; }
        public String getIcon() { return icon; }
        public boolean isCloud() { 
            return this == GOOGLE_DRIVE || this == ONEDRIVE || this == DROPBOX || this == ICLOUD; 
        }
        public boolean isRemovable() { return this == USB_DRIVE; }
    }
    
    // Vanha CloudType yhteensopivuuden vuoksi
    public enum CloudType {
        GOOGLE_DRIVE, ONEDRIVE, DROPBOX, ICLOUD
    }
    
    /**
     * Edustaa tallennuspaikkaa (pilvi tai USB).
     */
    public static class StorageLocation {
        private final String name;
        private final String path;
        private final StorageType type;
        private final String driveLabel;
        
        public StorageLocation(String name, String path, StorageType type) {
            this(name, path, type, null);
        }
        
        public StorageLocation(String name, String path, StorageType type, String driveLabel) {
            this.name = name;
            this.path = path;
            this.type = type;
            this.driveLabel = driveLabel;
        }
        
        public String getName() { return name; }
        public String getPath() { return path; }
        public StorageType getType() { return type; }
        public String getDriveLabel() { return driveLabel; }
        
        public String getBackupPath() {
            return path + File.separator + BACKUP_FOLDER_NAME;
        }
        
        public String getDisplayName() {
            if (driveLabel != null && !driveLabel.isEmpty()) {
                return type.getIcon() + " " + driveLabel;
            }
            return type.getIcon() + " " + name;
        }
        
        @Override
        public String toString() {
            return getDisplayName() + " (" + path + ")";
        }
    }
    
    /**
     * Vanha CloudService-luokka yhteensopivuuden vuoksi.
     */
    public static class CloudService {
        private final String name;
        private final String path;
        private final CloudType type;
        
        public CloudService(String name, String path, CloudType type) {
            this.name = name;
            this.path = path;
            this.type = type;
        }
        
        public String getName() { return name; }
        public String getPath() { return path; }
        public CloudType getType() { return type; }
        
        public String getBackupPath() {
            return path + File.separator + BACKUP_FOLDER_NAME;
        }
        
        @Override
        public String toString() {
            return name + " (" + path + ")";
        }
    }
    
    /**
     * Tunnistaa kaikki käytettävissä olevat tallennuspaikat.
     */
    public static List<StorageLocation> detectAllStorage() {
        List<StorageLocation> locations = new ArrayList<>();
        locations.addAll(detectCloudLocations());
        locations.addAll(detectUsbDrives());
        return locations;
    }
    
    /**
     * Tunnistaa pilvipalvelut StorageLocation-muodossa.
     */
    public static List<StorageLocation> detectCloudLocations() {
        List<StorageLocation> services = new ArrayList<>();
        
        StorageLocation googleDrive = detectGoogleDriveLocation();
        if (googleDrive != null) services.add(googleDrive);
        
        StorageLocation oneDrive = detectOneDriveLocation();
        if (oneDrive != null) services.add(oneDrive);
        
        StorageLocation dropbox = detectDropboxLocation();
        if (dropbox != null) services.add(dropbox);
        
        StorageLocation icloud = detectICloudLocation();
        if (icloud != null) services.add(icloud);
        
        return services;
    }
    
    /**
     * Tunnistaa USB-asemat (irrotettavat asemat).
     */
    public static List<StorageLocation> detectUsbDrives() {
        List<StorageLocation> drives = new ArrayList<>();
        FileSystemView fsv = FileSystemView.getFileSystemView();
        
        for (File root : File.listRoots()) {
            try {
                String description = fsv.getSystemTypeDescription(root);
                String displayName = fsv.getSystemDisplayName(root);
                
                // Tunnista irrotettavat asemat
                boolean isRemovable = false;
                if (description != null) {
                    String descLower = description.toLowerCase();
                    isRemovable = descLower.contains("removable") || 
                                  descLower.contains("siirrettävä") ||
                                  descLower.contains("irrotettava") ||
                                  descLower.contains("usb");
                }
                
                // Tarkista onko asema käytettävissä
                if (isRemovable && root.exists() && root.canWrite()) {
                    String label = displayName;
                    if (label == null || label.trim().isEmpty()) {
                        label = root.getPath();
                    }
                    
                    long totalSpace = root.getTotalSpace();
                    if (totalSpace > 0) {
                        drives.add(new StorageLocation(
                            "USB: " + label,
                            root.getPath(),
                            StorageType.USB_DRIVE,
                            label
                        ));
                        logger.fine("USB-asema havaittu: " + label);
                    }
                }
            } catch (Exception e) {
                logger.fine("Aseman tarkistus epäonnistui: " + root);
            }
        }
        
        return drives;
    }
    
    // === Pilvipalveluiden tunnistus ===
    
    private static StorageLocation detectGoogleDriveLocation() {
        String userHome = System.getProperty("user.home");
        
        String[] possiblePaths = {
            userHome + "\\Google Drive",
            userHome + "\\GoogleDrive", 
        };
        
        for (String path : possiblePaths) {
            if (path != null) {
                File dir = new File(path);
                if (dir.exists() && dir.isDirectory()) {
                    return new StorageLocation("Google Drive", path, StorageType.GOOGLE_DRIVE);
                }
            }
        }
        
        // Tarkista mountatut asemat
        for (char drive = 'G'; drive <= 'Z'; drive++) {
            File myDrive = new File(drive + ":\\My Drive");
            File omaDrive = new File(drive + ":\\Oma Drive");
            
            if (myDrive.exists()) {
                return new StorageLocation("Google Drive", myDrive.getPath(), StorageType.GOOGLE_DRIVE);
            }
            if (omaDrive.exists()) {
                return new StorageLocation("Google Drive", omaDrive.getPath(), StorageType.GOOGLE_DRIVE);
            }
        }
        
        return null;
    }
    
    private static StorageLocation detectOneDriveLocation() {
        // Tarkista ensin ympäristömuuttujat
        String[] envVars = {"OneDrive", "OneDriveConsumer", "OneDriveCommercial"};
        for (String envVar : envVars) {
            String path = System.getenv(envVar);
            if (path != null && new File(path).exists()) {
                logger.fine("OneDrive löytyi ympäristömuuttujasta: " + envVar + " = " + path);
                return new StorageLocation("OneDrive", path, StorageType.ONEDRIVE);
            }
        }
        
        String userHome = System.getProperty("user.home");
        
        // Kaikki mahdolliset OneDrive-polut
        String[] possiblePaths = {
            userHome + "\\OneDrive",
            userHome + "\\OneDrive - Personal",
            userHome + "\\OneDrive - Henkilökohtainen",
            System.getenv("USERPROFILE") + "\\OneDrive",
            System.getenv("LOCALAPPDATA") + "\\Microsoft\\OneDrive",
        };
        
        for (String path : possiblePaths) {
            if (path != null) {
                File dir = new File(path);
                if (dir.exists() && dir.isDirectory()) {
                    logger.fine("OneDrive löytyi polusta: " + path);
                    return new StorageLocation("OneDrive", path, StorageType.ONEDRIVE);
                }
            }
        }
        
        // Tarkista rekisteristä (Windows)
        try {
            String regPath = readWindowsRegistry(
                "HKEY_CURRENT_USER\\Software\\Microsoft\\OneDrive", "UserFolder");
            if (regPath != null && new File(regPath).exists()) {
                logger.fine("OneDrive löytyi rekisteristä: " + regPath);
                return new StorageLocation("OneDrive", regPath, StorageType.ONEDRIVE);
            }
        } catch (Exception e) {
            logger.fine("OneDrive rekisteriluku epäonnistui: " + e.getMessage());
        }
        
        return null;
    }
    
    private static StorageLocation detectICloudLocation() {
        String userHome = System.getProperty("user.home");
        
        String[] possiblePaths = {
            userHome + "\\iCloudDrive",
            userHome + "\\iCloud Drive",
            System.getenv("USERPROFILE") + "\\iCloudDrive",
            System.getenv("LOCALAPPDATA") + "\\Apple\\CloudStorage\\iCloud Drive",
            System.getenv("LOCALAPPDATA") + "\\Apple Inc\\iCloud\\iCloudDrive",
        };
        
        for (String path : possiblePaths) {
            if (path != null) {
                File dir = new File(path);
                if (dir.exists() && dir.isDirectory()) {
                    logger.fine("iCloud löytyi polusta: " + path);
                    return new StorageLocation("iCloud Drive", path, StorageType.ICLOUD);
                }
            }
        }
        
        // Tarkista myös Apple Mobile Device Support -kansio
        String programFiles = System.getenv("ProgramFiles");
        if (programFiles != null) {
            File appleDir = new File(programFiles + "\\Apple");
            if (appleDir.exists()) {
                // iCloud on todennäköisesti asennettuna
                String iCloudPath = userHome + "\\iCloudDrive";
                File iCloud = new File(iCloudPath);
                if (iCloud.exists()) {
                    return new StorageLocation("iCloud Drive", iCloudPath, StorageType.ICLOUD);
                }
            }
        }
        
        return null;
    }
    
    /**
     * Lukee arvon Windows-rekisteristä.
     */
    private static String readWindowsRegistry(String key, String valueName) {
        try {
            ProcessBuilder pb = new ProcessBuilder(
                "reg", "query", key, "/v", valueName);
            pb.redirectErrorStream(true);
            Process p = pb.start();
            
            java.io.BufferedReader reader = new java.io.BufferedReader(
                new java.io.InputStreamReader(p.getInputStream(), "UTF-8"));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains(valueName)) {
                    String[] parts = line.split("REG_SZ|REG_EXPAND_SZ");
                    if (parts.length > 1) {
                        return parts[1].trim();
                    }
                }
            }
            p.waitFor();
        } catch (Exception e) {
            // Ignore
        }
        return null;
    }
    
    private static StorageLocation detectDropboxLocation() {
        String userHome = System.getProperty("user.home");
        
        String[] possiblePaths = {
            userHome + "\\Dropbox",
            userHome + "\\Dropbox (Personal)",
        };
        
        for (String path : possiblePaths) {
            File dir = new File(path);
            if (dir.exists() && dir.isDirectory()) {
                return new StorageLocation("Dropbox", path, StorageType.DROPBOX);
            }
        }
        
        return null;
    }
    
    // === Vanhat metodit yhteensopivuuden vuoksi ===
    
    public static List<CloudService> detectAll() {
        List<CloudService> services = new ArrayList<>();
        
        StorageLocation google = detectGoogleDriveLocation();
        if (google != null) {
            services.add(new CloudService(google.getName(), google.getPath(), CloudType.GOOGLE_DRIVE));
        }
        
        StorageLocation oneDrive = detectOneDriveLocation();
        if (oneDrive != null) {
            services.add(new CloudService(oneDrive.getName(), oneDrive.getPath(), CloudType.ONEDRIVE));
        }
        
        StorageLocation dropbox = detectDropboxLocation();
        if (dropbox != null) {
            services.add(new CloudService(dropbox.getName(), dropbox.getPath(), CloudType.DROPBOX));
        }
        
        StorageLocation icloud = detectICloudLocation();
        if (icloud != null) {
            services.add(new CloudService(icloud.getName(), icloud.getPath(), CloudType.ICLOUD));
        }
        
        return services;
    }
    
    public static CloudService getPrimaryCloudService() {
        List<CloudService> services = detectAll();
        return services.isEmpty() ? null : services.get(0);
    }
    
    /**
     * Palauttaa ensisijaisen tallennuspaikan (pilvi > USB > null).
     */
    public static StorageLocation getPrimaryStorage() {
        List<StorageLocation> clouds = detectCloudLocations();
        if (!clouds.isEmpty()) {
            return clouds.get(0);
        }
        
        List<StorageLocation> usb = detectUsbDrives();
        if (!usb.isEmpty()) {
            return usb.get(0);
        }
        
        return null;
    }
    
    /**
     * Palauttaa ensisijaisen USB-aseman.
     */
    public static StorageLocation getPrimaryUsbDrive() {
        List<StorageLocation> usb = detectUsbDrives();
        return usb.isEmpty() ? null : usb.get(0);
    }
}
