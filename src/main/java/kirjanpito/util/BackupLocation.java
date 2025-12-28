package kirjanpito.util;

import java.io.File;
import java.util.Objects;

/**
 * Backup-sijainti. Voi olla pilvipalvelu, USB-asema tai paikallinen kansio.
 */
public class BackupLocation {
    
    public enum LocationType {
        CLOUD("Pilvi"),
        USB("USB"),
        LOCAL("Paikallinen");
        
        private final String displayName;
        
        LocationType(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    private final String path;
    private final LocationType type;
    private final String cloudServiceName; // Google Drive, OneDrive, jne.
    private final boolean autoBackup; // Automaattinen vai vain manuaalinen
    
    public BackupLocation(String path, LocationType type, String cloudServiceName, boolean autoBackup) {
        this.path = path;
        this.type = type;
        this.cloudServiceName = cloudServiceName;
        this.autoBackup = autoBackup;
    }
    
    /**
     * Luo pilvipalvelu-sijainnin.
     */
    public static BackupLocation cloud(String path, String serviceName) {
        return new BackupLocation(path, LocationType.CLOUD, serviceName, true);
    }
    
    /**
     * Luo USB-sijainnin.
     */
    public static BackupLocation usb(String path, String driveLabel) {
        return new BackupLocation(path, LocationType.USB, driveLabel, false);
    }
    
    /**
     * Luo paikallisen sijainnin.
     */
    public static BackupLocation local(String path, boolean autoBackup) {
        return new BackupLocation(path, LocationType.LOCAL, null, autoBackup);
    }
    
    public String getPath() {
        return path;
    }
    
    public LocationType getType() {
        return type;
    }
    
    public String getCloudServiceName() {
        return cloudServiceName;
    }
    
    public boolean isAutoBackup() {
        return autoBackup;
    }
    
    /**
     * Tarkistaa onko sijainti käytettävissä (kansio olemassa ja kirjoitettavissa).
     */
    public boolean isAvailable() {
        File dir = new File(path);
        if (!dir.exists()) {
            // Yritä luoda kansio
            return dir.mkdirs();
        }
        return dir.canWrite();
    }
    
    /**
     * Palauttaa näyttönimen (esim. "Google Drive", "USB: BACKUP (E:)", "Paikallinen")
     */
    public String getDisplayName() {
        switch (type) {
            case CLOUD:
                return cloudServiceName != null ? cloudServiceName : "Pilvi";
            case USB:
                return "USB: " + (cloudServiceName != null ? cloudServiceName : path);
            case LOCAL:
                return "Paikallinen: " + path;
            default:
                return path;
        }
    }
    
    /**
     * Serialisoi sijainnin merkkijonoksi tallennusta varten.
     * Muoto: type|path|cloudServiceName|autoBackup
     */
    public String serialize() {
        return type.name() + "|" + path + "|" + 
               (cloudServiceName != null ? cloudServiceName : "") + "|" + autoBackup;
    }
    
    /**
     * Parsii sijainnin merkkijonosta.
     */
    public static BackupLocation deserialize(String str) {
        String[] parts = str.split("\\|", 4);
        if (parts.length < 4) return null;
        
        try {
            LocationType type = LocationType.valueOf(parts[0]);
            String path = parts[1];
            String cloudService = parts[2].isEmpty() ? null : parts[2];
            boolean autoBackup = Boolean.parseBoolean(parts[3]);
            return new BackupLocation(path, type, cloudService, autoBackup);
        } catch (Exception e) {
            return null;
        }
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BackupLocation that = (BackupLocation) o;
        return Objects.equals(path, that.path);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(path);
    }
    
    @Override
    public String toString() {
        return getDisplayName();
    }
}
