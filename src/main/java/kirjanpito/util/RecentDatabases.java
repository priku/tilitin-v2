package kirjanpito.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

/**
 * Hallinnoi viimeksi avattuja tietokantoja.
 * Tallentaa ja lataa viimeisimmät tietokannat Java Preferences API:n avulla.
 */
public class RecentDatabases {
    private static final String PREF_KEY_PREFIX = "recentDatabase";
    private static final int MAX_RECENT = 10;
    private static RecentDatabases instance;
    private Preferences prefs;
    private List<String> recentList;
    
    private RecentDatabases() {
        prefs = Preferences.userNodeForPackage(RecentDatabases.class);
        recentList = new ArrayList<>();
        loadFromPreferences();
    }
    
    /**
     * Palauttaa singleton-instanssin.
     */
    public static synchronized RecentDatabases getInstance() {
        if (instance == null) {
            instance = new RecentDatabases();
        }
        return instance;
    }
    
    /**
     * Lataa viimeisimmät tietokannat asetuksista.
     */
    private void loadFromPreferences() {
        recentList.clear();
        for (int i = 0; i < MAX_RECENT; i++) {
            String path = prefs.get(PREF_KEY_PREFIX + i, null);
            if (path != null && !path.isEmpty()) {
                // Tarkista että tiedosto on olemassa (vain tiedostopohjaisille)
                if (path.startsWith("jdbc:sqlite:")) {
                    String filePath = path.substring("jdbc:sqlite:".length());
                    File file = new File(filePath);
                    if (file.exists()) {
                        recentList.add(path);
                    }
                } else if (path.startsWith("jdbc:")) {
                    // MySQL/PostgreSQL - lisää aina
                    recentList.add(path);
                } else {
                    // Pelkkä tiedostopolku
                    File file = new File(path);
                    if (file.exists()) {
                        recentList.add(path);
                    }
                }
            }
        }
    }
    
    /**
     * Tallentaa viimeisimmät tietokannat asetuksiin.
     */
    private void saveToPreferences() {
        // Tyhjennä kaikki
        for (int i = 0; i < MAX_RECENT; i++) {
            prefs.remove(PREF_KEY_PREFIX + i);
        }
        // Tallenna nykyinen lista
        for (int i = 0; i < recentList.size() && i < MAX_RECENT; i++) {
            prefs.put(PREF_KEY_PREFIX + i, recentList.get(i));
        }
    }
    
    /**
     * Lisää tietokanta viimeisimpien listaan.
     * Siirtää tietokannan listan alkuun jos se on jo listassa.
     * 
     * @param jdbcUrl tietokannan JDBC-URL tai tiedostopolku
     */
    public void addDatabase(String jdbcUrl) {
        if (jdbcUrl == null || jdbcUrl.isEmpty()) {
            return;
        }
        
        // Poista jos jo listassa
        recentList.remove(jdbcUrl);
        
        // Lisää alkuun
        recentList.add(0, jdbcUrl);
        
        // Rajoita maksimimäärään
        while (recentList.size() > MAX_RECENT) {
            recentList.remove(recentList.size() - 1);
        }
        
        saveToPreferences();
    }
    
    /**
     * Palauttaa viimeisimpien tietokantojen listan.
     * 
     * @return kopio listasta
     */
    public List<String> getRecentDatabases() {
        return new ArrayList<>(recentList);
    }
    
    /**
     * Poistaa tietokannan listasta.
     * 
     * @param jdbcUrl poistettava tietokanta
     */
    public void removeDatabase(String jdbcUrl) {
        recentList.remove(jdbcUrl);
        saveToPreferences();
    }
    
    /**
     * Tyhjentää viimeisimpien listan.
     */
    public void clearAll() {
        recentList.clear();
        saveToPreferences();
    }
    
    /**
     * Palauttaa tietokannan näyttönimen URL:sta.
     * 
     * @param jdbcUrl tietokannan URL
     * @return luettava nimi
     */
    public static String getDisplayName(String jdbcUrl) {
        if (jdbcUrl == null) {
            return "";
        }
        
        if (jdbcUrl.startsWith("jdbc:sqlite:")) {
            String path = jdbcUrl.substring("jdbc:sqlite:".length());
            File file = new File(path);
            return file.getName() + " (" + file.getParent() + ")";
        } else if (jdbcUrl.startsWith("jdbc:mysql:")) {
            // jdbc:mysql://host:port/database
            String rest = jdbcUrl.substring("jdbc:mysql://".length());
            return "MySQL: " + rest;
        } else if (jdbcUrl.startsWith("jdbc:postgresql:")) {
            String rest = jdbcUrl.substring("jdbc:postgresql://".length());
            return "PostgreSQL: " + rest;
        } else if (!jdbcUrl.startsWith("jdbc:")) {
            // Pelkkä tiedostopolku
            File file = new File(jdbcUrl);
            return file.getName();
        }
        
        return jdbcUrl;
    }
    
    /**
     * Palauttaa viimeisimpien tietokantojen määrän.
     */
    public int getCount() {
        return recentList.size();
    }
    
    /**
     * Tarkistaa onko listassa tietokantoja.
     */
    public boolean isEmpty() {
        return recentList.isEmpty();
    }
}
