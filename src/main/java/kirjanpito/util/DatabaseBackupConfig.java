package kirjanpito.util;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Per-tietokanta backup-asetukset.
 * Sisältää listan backup-sijainneista ja asetukset.
 */
public class DatabaseBackupConfig {
    
    private final String databasePath; // jdbc:sqlite:... polku
    private boolean enabled = true;
    private int maxBackupsPerLocation = 10;
    private List<BackupLocation> locations = new ArrayList<>();
    
    public DatabaseBackupConfig(String databasePath) {
        this.databasePath = databasePath;
    }
    
    public String getDatabasePath() {
        return databasePath;
    }
    
    /**
     * Palauttaa tietokannan nimen (ilman polkua).
     */
    public String getDatabaseName() {
        if (databasePath == null) return "tuntematon";
        String path = databasePath.replace("jdbc:sqlite:", "");
        int lastSlash = Math.max(path.lastIndexOf('/'), path.lastIndexOf('\\'));
        return lastSlash >= 0 ? path.substring(lastSlash + 1) : path;
    }
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    public int getMaxBackupsPerLocation() {
        return maxBackupsPerLocation;
    }
    
    public void setMaxBackupsPerLocation(int maxBackupsPerLocation) {
        this.maxBackupsPerLocation = maxBackupsPerLocation;
    }
    
    public List<BackupLocation> getLocations() {
        return new ArrayList<>(locations);
    }
    
    public void setLocations(List<BackupLocation> locations) {
        this.locations = new ArrayList<>(locations);
    }
    
    public void addLocation(BackupLocation location) {
        if (!locations.contains(location)) {
            locations.add(location);
        }
    }
    
    public void removeLocation(BackupLocation location) {
        locations.remove(location);
    }
    
    public void removeLocation(String path) {
        locations.removeIf(loc -> loc.getPath().equals(path));
    }
    
    /**
     * Palauttaa automaattiseen backuppiin käytettävät sijainnit.
     */
    public List<BackupLocation> getAutoBackupLocations() {
        return locations.stream()
            .filter(BackupLocation::isAutoBackup)
            .filter(BackupLocation::isAvailable)
            .collect(Collectors.toList());
    }
    
    /**
     * Palauttaa kaikki käytettävissä olevat sijainnit (manuaalista backuppia varten).
     */
    public List<BackupLocation> getAvailableLocations() {
        return locations.stream()
            .filter(BackupLocation::isAvailable)
            .collect(Collectors.toList());
    }
    
    /**
     * Tarkistaa onko tietokannalla yhtään backup-sijaintia.
     */
    public boolean hasLocations() {
        return !locations.isEmpty();
    }
    
    /**
     * Tarkistaa onko tietokannalla automaattisia backup-sijainteja.
     */
    public boolean hasAutoBackupLocations() {
        return locations.stream().anyMatch(BackupLocation::isAutoBackup);
    }
    
    /**
     * Serialisoi konfiguraation merkkijonoksi.
     * Muoto: enabled;maxBackups;location1##location2##...
     */
    public String serialize() {
        StringBuilder sb = new StringBuilder();
        sb.append(enabled).append(";");
        sb.append(maxBackupsPerLocation).append(";");
        
        String locationsStr = locations.stream()
            .map(BackupLocation::serialize)
            .collect(Collectors.joining("##"));
        sb.append(locationsStr);
        
        return sb.toString();
    }
    
    /**
     * Parsii konfiguraation merkkijonosta.
     */
    public static DatabaseBackupConfig deserialize(String databasePath, String str) {
        DatabaseBackupConfig config = new DatabaseBackupConfig(databasePath);
        
        String[] parts = str.split(";", 3);
        if (parts.length >= 2) {
            config.enabled = Boolean.parseBoolean(parts[0]);
            try {
                config.maxBackupsPerLocation = Integer.parseInt(parts[1]);
            } catch (NumberFormatException e) {
                config.maxBackupsPerLocation = 10;
            }
        }
        
        if (parts.length >= 3 && !parts[2].isEmpty()) {
            String[] locationStrs = parts[2].split("##");
            for (String locStr : locationStrs) {
                BackupLocation loc = BackupLocation.deserialize(locStr);
                if (loc != null) {
                    config.locations.add(loc);
                }
            }
        }
        
        return config;
    }
    
    @Override
    public String toString() {
        return getDatabaseName() + " (" + locations.size() + " sijaintia)";
    }
}
