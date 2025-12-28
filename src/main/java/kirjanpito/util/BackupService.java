package kirjanpito.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Hallinnoi tietokannan automaattisia varmuuskopioita.
 * Tukee Wordin AutoSave-tyylistä automaattista tallennusta pilveen.
 * 
 * Varmuuskopiot nimetään muodossa: nimi_TUNNISTE_aikaleima.sqlite
 * TUNNISTE on 6 merkin hash tietokannan polusta, joka estää eri 
 * tietokantojen sekoittumisen vaikka niillä olisi sama nimi.
 */
public class BackupService {
    
    private static final Logger logger = Logger.getLogger("kirjanpito");
    private static final String BACKUP_FOLDER_NAME = "Tilitin Backups";
    private static final int DEFAULT_MAX_BACKUPS = 10;
    private static final int DEFAULT_AUTO_BACKUP_INTERVAL = 5; // minuuttia
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
    private static final int HASH_LENGTH = 6; // Tunnisteen pituus
    
    private static BackupService instance;
    
    private boolean enabled = false;
    private boolean autoBackupEnabled = false;
    private int autoBackupIntervalMinutes = DEFAULT_AUTO_BACKUP_INTERVAL;
    private File backupDirectory; // Legacy: yhteinen oletussijainti
    private int maxBackups = DEFAULT_MAX_BACKUPS;
    private Date lastBackupTime;
    private String currentDatabasePath;
    private List<String> selectedDatabases = new ArrayList<>();
    
    // Per-tietokanta konfiguraatiot (uusi arkkitehtuuri)
    private java.util.Map<String, DatabaseBackupConfig> databaseConfigs = new java.util.HashMap<>();
    
    // Automaattinen varmuuskopiointi
    private ScheduledExecutorService scheduler;
    private ScheduledFuture<?> autoBackupTask;
    private BackupStatusListener statusListener;
    
    public interface BackupStatusListener {
        void onBackupStatusChanged(BackupStatus status);
    }
    
    public enum BackupStatus {
        DISABLED("Varmuuskopiointi pois käytöstä", "○"),
        ENABLED("Varmuuskopiointi käytössä", "●"),
        AUTO_ENABLED("AutoBackup käytössä", "◉"),
        BACKING_UP("Varmuuskopioidaan...", "↻"),
        SYNCING("Synkronoidaan pilveen...", "☁"),
        SUCCESS("Varmuuskopioitu", "✓"),
        ERROR("Virhe varmuuskopioinnissa", "✗");
        
        private final String description;
        private final String icon;
        
        BackupStatus(String description, String icon) {
            this.description = description;
            this.icon = icon;
        }
        
        public String getDescription() { return description; }
        public String getIcon() { return icon; }
    }
    
    private BackupService() {
        loadSettings();
    }
    
    public static synchronized BackupService getInstance() {
        if (instance == null) {
            instance = new BackupService();
        }
        return instance;
    }
    
    /**
     * Lataa asetukset.
     */
    private void loadSettings() {
        AppSettings settings = AppSettings.getInstance();
        enabled = settings.getBoolean("backup.enabled", false);
        autoBackupEnabled = settings.getBoolean("backup.autoEnabled", false);
        autoBackupIntervalMinutes = settings.getInt("backup.autoInterval", DEFAULT_AUTO_BACKUP_INTERVAL);
        String backupPath = settings.getString("backup.directory", null);
        maxBackups = settings.getInt("backup.maxCount", DEFAULT_MAX_BACKUPS);
        
        if (backupPath != null && !backupPath.isEmpty()) {
            backupDirectory = new File(backupPath);
        } else {
            // Yritä löytää pilvipalvelu
            CloudStorageDetector.CloudService cloud = CloudStorageDetector.getPrimaryCloudService();
            if (cloud != null) {
                backupDirectory = new File(cloud.getBackupPath());
            } else {
                // Oletuskansio Documents-kansiossa
                String userHome = System.getProperty("user.home");
                backupDirectory = new File(userHome + File.separator + "Documents" + File.separator + BACKUP_FOLDER_NAME);
            }
        }
        
        String lastBackupStr = settings.getString("backup.lastTime", null);
        if (lastBackupStr != null) {
            try {
                lastBackupTime = new Date(Long.parseLong(lastBackupStr));
            } catch (NumberFormatException e) {
                lastBackupTime = null;
            }
        }
        
        // Lataa valitut tietokannat
        String selectedDbsStr = settings.getString("backup.selectedDatabases", "");
        selectedDatabases.clear();
        if (!selectedDbsStr.isEmpty()) {
            for (String db : selectedDbsStr.split("\\|")) {
                if (!db.isEmpty()) {
                    selectedDatabases.add(db);
                }
            }
        }
        
        // Lataa per-tietokanta konfiguraatiot
        loadDatabaseConfigs(settings);
    }
    
    /**
     * Lataa per-tietokanta konfiguraatiot.
     */
    private void loadDatabaseConfigs(AppSettings settings) {
        databaseConfigs.clear();
        String configsStr = settings.getString("backup.databaseConfigs", "");
        if (!configsStr.isEmpty()) {
            // Muoto: dbPath1@@config1|||dbPath2@@config2|||...
            for (String entry : configsStr.split("\\|\\|\\|")) {
                String[] parts = entry.split("@@", 2);
                if (parts.length == 2) {
                    String dbPath = parts[0];
                    DatabaseBackupConfig config = DatabaseBackupConfig.deserialize(dbPath, parts[1]);
                    databaseConfigs.put(dbPath, config);
                }
            }
        }
    }
    
    /**
     * Tallentaa asetukset.
     */
    public void saveSettings() {
        AppSettings settings = AppSettings.getInstance();
        settings.set("backup.enabled", String.valueOf(enabled));
        settings.set("backup.autoEnabled", String.valueOf(autoBackupEnabled));
        settings.set("backup.autoInterval", String.valueOf(autoBackupIntervalMinutes));
        settings.set("backup.directory", backupDirectory != null ? backupDirectory.getAbsolutePath() : "");
        settings.set("backup.maxCount", String.valueOf(maxBackups));
        if (lastBackupTime != null) {
            settings.set("backup.lastTime", String.valueOf(lastBackupTime.getTime()));
        }
        
        // Tallenna valitut tietokannat
        String selectedDbsStr = String.join("|", selectedDatabases);
        settings.set("backup.selectedDatabases", selectedDbsStr);
        
        // Tallenna per-tietokanta konfiguraatiot
        saveDatabaseConfigs(settings);
        
        settings.save();
    }
    
    /**
     * Tallentaa per-tietokanta konfiguraatiot.
     */
    private void saveDatabaseConfigs(AppSettings settings) {
        StringBuilder sb = new StringBuilder();
        for (java.util.Map.Entry<String, DatabaseBackupConfig> entry : databaseConfigs.entrySet()) {
            if (sb.length() > 0) {
                sb.append("|||");
            }
            sb.append(entry.getKey()).append("@@").append(entry.getValue().serialize());
        }
        settings.set("backup.databaseConfigs", sb.toString());
    }
    
    /**
     * Asettaa nykyisen tietokannan ja käynnistää automaattisen varmuuskopioinnin.
     */
    public void setCurrentDatabase(String databasePath) {
        this.currentDatabasePath = databasePath;
        
        if (autoBackupEnabled && enabled && databasePath != null && databasePath.contains("sqlite")) {
            startAutoBackup();
        }
    }
    
    /**
     * Palauttaa nykyisen tietokannan polun.
     */
    public String getCurrentDatabase() {
        return currentDatabasePath;
    }
    
    /**
     * Palauttaa valitut tietokannat.
     */
    public List<String> getSelectedDatabases() {
        return new ArrayList<>(selectedDatabases);
    }
    
    /**
     * Asettaa valitut tietokannat.
     */
    public void setSelectedDatabases(List<String> databases) {
        this.selectedDatabases = new ArrayList<>(databases);
    }
    
    /**
     * Tarkistaa onko tietokanta valittu varmuuskopioitavaksi.
     */
    public boolean isDatabaseSelected(String databasePath) {
        if (selectedDatabases.isEmpty()) {
            // Jos ei ole valittu mitään, varmuuskopioidaan nykyinen
            return databasePath.equals(currentDatabasePath);
        }
        return selectedDatabases.contains(databasePath);
    }
    
    // ========== Per-tietokanta konfiguraatiot ==========
    
    /**
     * Palauttaa tietokannan backup-konfiguraation.
     * Luo uuden konfiguraation jos sitä ei ole.
     */
    public DatabaseBackupConfig getDatabaseConfig(String databasePath) {
        return databaseConfigs.computeIfAbsent(databasePath, DatabaseBackupConfig::new);
    }
    
    /**
     * Asettaa tietokannan backup-konfiguraation.
     */
    public void setDatabaseConfig(String databasePath, DatabaseBackupConfig config) {
        databaseConfigs.put(databasePath, config);
    }
    
    /**
     * Palauttaa kaikki tietokantakonfiguraatiot.
     */
    public java.util.Map<String, DatabaseBackupConfig> getAllDatabaseConfigs() {
        return new java.util.HashMap<>(databaseConfigs);
    }
    
    /**
     * Lisää backup-sijainnin tietokannalle.
     */
    public void addLocationToDatabase(String databasePath, BackupLocation location) {
        getDatabaseConfig(databasePath).addLocation(location);
    }
    
    /**
     * Poistaa backup-sijainnin tietokannalta.
     */
    public void removeLocationFromDatabase(String databasePath, BackupLocation location) {
        DatabaseBackupConfig config = databaseConfigs.get(databasePath);
        if (config != null) {
            config.removeLocation(location);
        }
    }
    
    /**
     * Suorittaa varmuuskopioinnin tietylle tietokannalle kaikkiin sen sijainteihin.
     * @param databasePath tietokannan polku
     * @param force jos true, ohittaa enabled-tarkistuksen
     * @return onnistuneiden sijaintien määrä
     */
    public int performBackupToAllLocations(String databasePath, boolean force) {
        DatabaseBackupConfig config = databaseConfigs.get(databasePath);
        
        // Jos ei ole per-tietokanta konfiguraatiota, käytä vanhaa logiikkaa
        if (config == null || !config.hasLocations()) {
            boolean success = performBackup(databasePath, force);
            return success ? 1 : 0;
        }
        
        if (!force && !config.isEnabled()) {
            return 0;
        }
        
        int successCount = 0;
        List<BackupLocation> locations = force ? config.getAvailableLocations() : config.getAutoBackupLocations();
        
        for (BackupLocation location : locations) {
            if (performBackupToLocation(databasePath, location, config.getMaxBackupsPerLocation())) {
                successCount++;
            }
        }
        
        return successCount;
    }
    
    /**
     * Suorittaa varmuuskopioinnin tiettyyn sijaintiin.
     */
    public boolean performBackupToLocation(String databasePath, BackupLocation location, int maxBackupsForLocation) {
        if (databasePath == null || databasePath.isEmpty()) {
            logger.warning("Tietokannan polku on tyhjä");
            return false;
        }
        
        // Vain SQLite-tietokannat
        if (!databasePath.contains("sqlite")) {
            logger.fine("Varmuuskopiointi ohitettu: ei SQLite-tietokanta");
            return true;
        }
        
        if (!location.isAvailable()) {
            logger.warning("Backup-sijainti ei ole käytettävissä: " + location.getPath());
            return false;
        }
        
        notifyStatusChanged(BackupStatus.BACKING_UP);
        
        File sourceFile = new File(databasePath.replace("jdbc:sqlite:", ""));
        if (!sourceFile.exists()) {
            logger.warning("Tietokantatiedostoa ei löydy: " + sourceFile);
            notifyStatusChanged(BackupStatus.ERROR);
            return false;
        }
        
        try {
            File backupDir = new File(location.getPath());
            if (!backupDir.exists()) {
                if (!backupDir.mkdirs()) {
                    logger.severe("Varmuuskopiokansion luonti epäonnistui: " + backupDir);
                    notifyStatusChanged(BackupStatus.ERROR);
                    return false;
                }
            }
            
            // Luo backup-tiedoston nimi
            String pathHash = generatePathHash(sourceFile.getAbsolutePath());
            String baseName = sourceFile.getName().replace(".sqlite", "");
            String timestamp = DATE_FORMAT.format(new Date());
            String backupName = baseName + "_" + pathHash + "_" + timestamp + ".sqlite";
            File backupFile = new File(backupDir, backupName);
            
            // Kopioi tiedosto
            Files.copy(sourceFile.toPath(), backupFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            
            lastBackupTime = new Date();
            logger.info("Varmuuskopio luotu: " + backupFile.getAbsolutePath());
            
            // Tarkista onko pilvipalvelussa
            if (location.getType() == BackupLocation.LocationType.CLOUD) {
                notifyStatusChanged(BackupStatus.SYNCING);
                try { Thread.sleep(500); } catch (InterruptedException ignored) {}
            }
            
            // Siivoa vanhat backupit tästä sijainnista
            cleanupOldBackupsInLocation(sourceFile, backupDir, maxBackupsForLocation);
            
            saveSettings();
            notifyStatusChanged(autoBackupEnabled ? BackupStatus.AUTO_ENABLED : BackupStatus.SUCCESS);
            
            return true;
            
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Varmuuskopiointi epäonnistui: " + location.getPath(), e);
            notifyStatusChanged(BackupStatus.ERROR);
            return false;
        }
    }
    
    /**
     * Siivoa vanhat backupit tietystä sijainnista.
     */
    private void cleanupOldBackupsInLocation(File sourceFile, File backupDir, int maxBackupsForLocation) {
        String pattern = getBackupPattern(sourceFile);
        
        File[] backups = backupDir.listFiles((dir, name) -> 
            name.startsWith(pattern) && name.endsWith(".sqlite"));
        
        if (backups == null || backups.length <= maxBackupsForLocation) {
            return;
        }
        
        Arrays.sort(backups, Comparator.comparingLong(File::lastModified));
        
        int toDelete = backups.length - maxBackupsForLocation;
        for (int i = 0; i < toDelete; i++) {
            if (backups[i].delete()) {
                logger.info("Vanha varmuuskopio poistettu: " + backups[i].getName());
            }
        }
    }
    
    // ========== Vanhat metodit (yhteensopivuus) ==========
    
    /**
     * Käynnistää automaattisen varmuuskopioinnin.
     */
    public void startAutoBackup() {
        stopAutoBackup();
        
        if (!enabled || !autoBackupEnabled || currentDatabasePath == null) {
            return;
        }
        
        scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "AutoBackup");
            t.setDaemon(true);
            return t;
        });
        
        autoBackupTask = scheduler.scheduleAtFixedRate(() -> {
            try {
                performAutoBackup();
            } catch (Exception e) {
                logger.log(Level.WARNING, "Automaattinen varmuuskopiointi epäonnistui", e);
                notifyStatusChanged(BackupStatus.ERROR);
            }
        }, autoBackupIntervalMinutes, autoBackupIntervalMinutes, TimeUnit.MINUTES);
        
        logger.info("AutoBackup käynnistetty: " + autoBackupIntervalMinutes + " min välein");
        notifyStatusChanged(BackupStatus.AUTO_ENABLED);
    }
    
    /**
     * Suorittaa automaattisen varmuuskopioinnin valituille tietokannoille.
     */
    private void performAutoBackup() {
        if (selectedDatabases.isEmpty()) {
            // Varmuuskopioi vain nykyinen tietokanta
            if (currentDatabasePath != null) {
                performBackup(currentDatabasePath);
            }
        } else {
            // Varmuuskopioi kaikki valitut
            for (String dbPath : selectedDatabases) {
                if (isDatabaseAvailable(dbPath)) {
                    performBackup(dbPath);
                }
            }
        }
    }
    
    /**
     * Tarkistaa onko tietokanta saatavilla (tiedosto on olemassa).
     */
    private boolean isDatabaseAvailable(String jdbcUrl) {
        if (jdbcUrl.contains("sqlite:")) {
            String path = jdbcUrl.substring(jdbcUrl.indexOf("sqlite:") + 7);
            File dbFile = new File(path);
            return dbFile.exists() && dbFile.canRead();
        }
        return false;
    }
    
    /**
     * Pysäyttää automaattisen varmuuskopioinnin.
     */
    public void stopAutoBackup() {
        if (autoBackupTask != null) {
            autoBackupTask.cancel(false);;
            autoBackupTask = null;
        }
        if (scheduler != null) {
            scheduler.shutdown();
            scheduler = null;
        }
    }
    
    /**
     * Suorittaa varmuuskopioinnin jos se on käytössä.
     */
    public boolean performBackup(File databaseFile) {
        if (!enabled || databaseFile == null || !databaseFile.exists()) {
            return true;
        }
        return performBackup(databaseFile.getAbsolutePath());
    }
    
    /**
     * Suorittaa varmuuskopioinnin.
     */
    public synchronized boolean performBackup(String databasePath) {
        return performBackup(databasePath, false);
    }
    
    /**
     * Suorittaa varmuuskopioinnin.
     * @param force jos true, ohittaa enabled-tarkistuksen (käytetään "Tee nyt" -painikkeesta)
     */
    public synchronized boolean performBackup(String databasePath, boolean force) {
        if (!force && !enabled) {
            logger.fine("Varmuuskopiointi ei ole käytössä");
            return true;
        }
        
        if (databasePath == null || databasePath.isEmpty()) {
            logger.warning("Tietokannan polku on tyhjä");
            return false;
        }
        
        // Vain SQLite-tietokannat
        if (!databasePath.contains("sqlite")) {
            logger.fine("Varmuuskopiointi ohitettu: ei SQLite-tietokanta");
            return true;
        }
        
        notifyStatusChanged(BackupStatus.BACKING_UP);
        
        File sourceFile = new File(databasePath.replace("jdbc:sqlite:", ""));
        if (!sourceFile.exists()) {
            logger.warning("Tietokantatiedostoa ei löydy: " + sourceFile);
            notifyStatusChanged(BackupStatus.ERROR);
            return false;
        }
        
        try {
            // Luo backup-kansio jos ei ole olemassa
            if (!backupDirectory.exists()) {
                if (!backupDirectory.mkdirs()) {
                    logger.severe("Varmuuskopiokansion luonti epäonnistui: " + backupDirectory);
                    notifyStatusChanged(BackupStatus.ERROR);
                    return false;
                }
            }
            
            // Luo yksilöivä tunniste tietokannan polusta
            // Tämä estää eri tietokantojen (sama nimi, eri sijainti) sekoittumisen
            String pathHash = generatePathHash(sourceFile.getAbsolutePath());
            
            // Luo backup-tiedoston nimi: nimi_TUNNISTE_aikaleima.sqlite
            // Esim: kirjanpito_a1b2c3_2025-12-28_14-30-00.sqlite
            String baseName = sourceFile.getName().replace(".sqlite", "");
            String timestamp = DATE_FORMAT.format(new Date());
            String backupName = baseName + "_" + pathHash + "_" + timestamp + ".sqlite";
            File backupFile = new File(backupDirectory, backupName);
            
            // Kopioi tiedosto
            Files.copy(sourceFile.toPath(), backupFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            
            lastBackupTime = new Date();
            logger.info("Varmuuskopio luotu: " + backupFile.getAbsolutePath());
            
            // Tarkista onko pilvipalvelussa
            if (isInCloudStorage()) {
                notifyStatusChanged(BackupStatus.SYNCING);
                // Pilvipalvelu synkronoi automaattisesti - näytä hetki ja sitten success
                try { Thread.sleep(500); } catch (InterruptedException ignored) {}
            }
            
            // Poista vanhat varmuuskopiot (käyttää tunnistetta)
            cleanupOldBackups(sourceFile);
            
            // Tallenna viimeinen backup-aika
            saveSettings();
            
            notifyStatusChanged(autoBackupEnabled ? BackupStatus.AUTO_ENABLED : BackupStatus.SUCCESS);
            
            return true;
            
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Varmuuskopiointi epäonnistui", e);
            notifyStatusChanged(BackupStatus.ERROR);
            return false;
        }
    }
    
    /**
     * Tarkistaa onko varmuuskopiokansio pilvipalvelussa.
     */
    public boolean isInCloudStorage() {
        if (backupDirectory == null) return false;
        
        for (CloudStorageDetector.CloudService cloud : CloudStorageDetector.detectAll()) {
            if (backupDirectory.getAbsolutePath().startsWith(cloud.getPath())) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Palauttaa pilvipalvelun nimen jos käytössä.
     */
    public String getCloudServiceName() {
        if (backupDirectory == null) return null;
        
        for (CloudStorageDetector.CloudService cloud : CloudStorageDetector.detectAll()) {
            if (backupDirectory.getAbsolutePath().startsWith(cloud.getPath())) {
                return cloud.getName();
            }
        }
        return null;
    }
    
    private void notifyStatusChanged(BackupStatus status) {
        if (statusListener != null) {
            // Kutsu UI-säikeessä
            javax.swing.SwingUtilities.invokeLater(() -> 
                statusListener.onBackupStatusChanged(status));
        }
    }
    
    /**
     * Luo yksilöivän tunnisteen tietokannan polusta.
     * Sama polku tuottaa aina saman tunnisteen.
     * 
     * @param absolutePath tietokannan absoluuttinen polku
     * @return 6 merkin tunniste (hex)
     */
    private String generatePathHash(String absolutePath) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hash = md.digest(absolutePath.getBytes());
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < HASH_LENGTH / 2; i++) {
                sb.append(String.format("%02x", hash[i]));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            // Fallback: käytä hashCode-metodia
            return String.format("%06x", Math.abs(absolutePath.hashCode()) % 0xFFFFFF);
        }
    }
    
    /**
     * Luo hakumallin tietyn tietokannan varmuuskopioille.
     * Malli: nimi_TUNNISTE_*.sqlite
     */
    private String getBackupPattern(File sourceFile) {
        String baseName = sourceFile.getName().replace(".sqlite", "");
        String pathHash = generatePathHash(sourceFile.getAbsolutePath());
        return baseName + "_" + pathHash + "_";
    }
    
    /**
     * Poistaa vanhat varmuuskopiot, säilyttää vain maxBackups kappaletta.
     * Käyttää tunnistetta varmistaakseen että vain saman tietokannan kopiot siivotaan.
     */
    private void cleanupOldBackups(File sourceFile) {
        if (backupDirectory == null || !backupDirectory.exists()) {
            return;
        }
        
        String pattern = getBackupPattern(sourceFile);
        
        File[] backups = backupDirectory.listFiles((dir, name) -> 
            name.startsWith(pattern) && name.endsWith(".sqlite"));
        
        if (backups == null || backups.length <= maxBackups) {
            return;
        }
        
        // Järjestä vanhimmasta uusimpaan
        Arrays.sort(backups, Comparator.comparingLong(File::lastModified));
        
        // Poista vanhimmat
        int toDelete = backups.length - maxBackups;
        for (int i = 0; i < toDelete; i++) {
            if (backups[i].delete()) {
                logger.info("Vanha varmuuskopio poistettu: " + backups[i].getName());
            }
        }
    }
    
    /**
     * Palauttaa varmuuskopioiden määrän tietylle tietokannalle.
     * @param databasePath tietokannan absoluuttinen polku
     */
    public int getBackupCount(String databasePath) {
        if (backupDirectory == null || !backupDirectory.exists() || databasePath == null) {
            return 0;
        }
        
        File sourceFile = new File(databasePath.replace("jdbc:sqlite:", ""));
        String pattern = getBackupPattern(sourceFile);
        
        File[] backups = backupDirectory.listFiles((dir, name) -> 
            name.startsWith(pattern) && name.endsWith(".sqlite"));
        
        return backups != null ? backups.length : 0;
    }
    
    /**
     * Listaa varmuuskopiot tietylle tietokannalle.
     * @param databasePath tietokannan absoluuttinen polku
     */
    public File[] listBackups(String databasePath) {
        if (backupDirectory == null || !backupDirectory.exists() || databasePath == null) {
            return new File[0];
        }
        
        File sourceFile = new File(databasePath.replace("jdbc:sqlite:", ""));
        String pattern = getBackupPattern(sourceFile);
        
        File[] backups = backupDirectory.listFiles((dir, name) -> 
            name.startsWith(pattern) && name.endsWith(".sqlite"));
        
        if (backups != null) {
            // Järjestä uusimmasta vanhimpaan
            Arrays.sort(backups, (a, b) -> Long.compare(b.lastModified(), a.lastModified()));
        }
        
        return backups != null ? backups : new File[0];
    }
    
    /**
     * Listaa KAIKKI varmuuskopiot kansiosta (kaikki tietokannat).
     */
    public File[] listAllBackups() {
        if (backupDirectory == null || !backupDirectory.exists()) {
            return new File[0];
        }
        
        File[] backups = backupDirectory.listFiles((dir, name) -> 
            name.endsWith(".sqlite"));
        
        if (backups != null) {
            // Järjestä uusimmasta vanhimpaan
            Arrays.sort(backups, (a, b) -> Long.compare(b.lastModified(), a.lastModified()));
        }
        
        return backups != null ? backups : new File[0];
    }
    
    /**
     * Palauttaa varmuuskopion uuteen sijaintiin.
     * 
     * @param backupFile varmuuskopiotiedosto
     * @param targetDirectory kohdekansio (null = Tilitin-oletuskansio)
     * @return palautettu tiedosto tai null jos epäonnistui
     */
    public File restoreBackup(File backupFile, File targetDirectory) {
        if (backupFile == null || !backupFile.exists()) {
            logger.warning("Varmuuskopiotiedostoa ei löydy: " + backupFile);
            return null;
        }
        
        // Oletuskansio
        if (targetDirectory == null) {
            targetDirectory = getDefaultTilitinDirectory();
        }
        
        // Luo kohdekansio jos ei ole
        if (!targetDirectory.exists()) {
            targetDirectory.mkdirs();
        }
        
        // Pura alkuperäinen tietokannan nimi varmuuskopion nimestä
        // Muoto: nimi_HASH_aikaleima.sqlite
        String backupName = backupFile.getName();
        String originalName = extractOriginalDatabaseName(backupName);
        
        File targetFile = new File(targetDirectory, originalName);
        
        // Jos kohdetiedosto on jo olemassa, kysy uusi nimi
        if (targetFile.exists()) {
            // Lisää aikaleima nimeen
            String baseName = originalName.replace(".sqlite", "");
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            targetFile = new File(targetDirectory, baseName + "_restored_" + timestamp + ".sqlite");
        }
        
        try {
            Files.copy(backupFile.toPath(), targetFile.toPath(), StandardCopyOption.COPY_ATTRIBUTES);
            logger.info("Varmuuskopio palautettu: " + targetFile.getAbsolutePath());
            return targetFile;
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Varmuuskopion palautus epäonnistui", e);
            return null;
        }
    }
    
    /**
     * Palauttaa Tilitin:n oletuskansion.
     */
    private File getDefaultTilitinDirectory() {
        String appData = System.getenv("APPDATA");
        if (appData != null) {
            return new File(appData, "Tilitin");
        }
        String userHome = System.getProperty("user.home");
        return new File(userHome + File.separator + "AppData" + File.separator + "Roaming" + File.separator + "Tilitin");
    }
    
    /**
     * Purkaa alkuperäisen tietokannan nimen varmuuskopion nimestä.
     * nimi_HASH_aikaleima.sqlite -> nimi.sqlite
     */
    private String extractOriginalDatabaseName(String backupFileName) {
        // Poista .sqlite lopusta
        String name = backupFileName;
        if (name.endsWith(".sqlite")) {
            name = name.substring(0, name.length() - 7);
        }
        
        // Etsi viimeinen _ ennen aikaleimaa (muoto: yyyy-MM-dd_HH-mm-ss)
        // Aikaleima on 19 merkkiä
        if (name.length() > 19) {
            String possibleTimestamp = name.substring(name.length() - 19);
            if (possibleTimestamp.matches("\\d{4}-\\d{2}-\\d{2}_\\d{2}-\\d{2}-\\d{2}")) {
                name = name.substring(0, name.length() - 20); // 19 + 1 alaviiva
            }
        }
        
        // Poista hash (6 merkkiä + alaviiva)
        if (name.length() > 7) {
            String possibleHash = name.substring(name.length() - 6);
            if (possibleHash.matches("[a-f0-9]{6}")) {
                name = name.substring(0, name.length() - 7); // 6 + 1 alaviiva
            }
        }
        
        return name + ".sqlite";
    }
    
    // Getterit ja setterit
    
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { 
        this.enabled = enabled;
        notifyStatusChanged(enabled ? 
            (autoBackupEnabled ? BackupStatus.AUTO_ENABLED : BackupStatus.ENABLED) : 
            BackupStatus.DISABLED);
    }
    
    public boolean isAutoBackupEnabled() { return autoBackupEnabled; }
    public void setAutoBackupEnabled(boolean autoBackupEnabled) { 
        this.autoBackupEnabled = autoBackupEnabled;
        if (autoBackupEnabled && enabled) {
            startAutoBackup();
        } else {
            stopAutoBackup();
        }
    }
    
    public int getAutoBackupIntervalMinutes() { return autoBackupIntervalMinutes; }
    public void setAutoBackupIntervalMinutes(int minutes) { 
        this.autoBackupIntervalMinutes = Math.max(1, Math.min(60, minutes));
        if (autoBackupEnabled) {
            startAutoBackup(); // Käynnistä uudelleen uudella intervallilla
        }
    }
    
    public File getBackupDirectory() { return backupDirectory; }
    public void setBackupDirectory(File backupDirectory) { this.backupDirectory = backupDirectory; }
    
    public int getMaxBackups() { return maxBackups; }
    public void setMaxBackups(int maxBackups) { this.maxBackups = Math.max(1, Math.min(100, maxBackups)); }
    
    public Date getLastBackupTime() { return lastBackupTime; }
    
    public void setStatusListener(BackupStatusListener listener) { this.statusListener = listener; }
    
    /**
     * Palauttaa selkokielisen kuvauksen varmuuskopioinnin tilasta.
     */
    public String getStatusMessage() {
        if (!enabled) {
            return "Varmuuskopiointi ei ole käytössä";
        }
        
        StringBuilder sb = new StringBuilder();
        
        if (autoBackupEnabled) {
            sb.append("AutoBackup käytössä (").append(autoBackupIntervalMinutes).append(" min)");
        } else {
            sb.append("Varmuuskopiointi käytössä");
        }
        
        String cloudName = getCloudServiceName();
        if (cloudName != null) {
            sb.append(" • ☁ ").append(cloudName);
        }
        
        if (lastBackupTime != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("d.M.yyyy HH:mm");
            sb.append("\nViimeisin: ").append(sdf.format(lastBackupTime));
        }
        
        return sb.toString();
    }
}
