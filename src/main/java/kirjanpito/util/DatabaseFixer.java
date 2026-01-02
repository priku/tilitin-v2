package kirjanpito.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;

/**
 * Apuluokka tietokannan korjaukseen.
 * Luo 2024 tilikauden ja siirtää väärät tositteet sinne.
 */
public class DatabaseFixer {
    
    public static void main(String[] args) {
        String appData = System.getenv("APPDATA");
        String dbPath = appData + "\\Tilitin\\kirjanpito.sqlite";
        String jdbcUrl = "jdbc:sqlite:" + dbPath;
        
        System.out.println("🔧 Tietokannan korjaus");
        System.out.println("📂 Tietokanta: " + dbPath);
        
        try (Connection conn = DriverManager.getConnection(jdbcUrl)) {
            conn.setAutoCommit(false);
            Statement stmt = conn.createStatement();
            
            // 1. Näytä tietokannan rakenne
            System.out.println("\n📋 Taulujen rakenne:");
            ResultSet rs = stmt.executeQuery("SELECT name FROM sqlite_master WHERE type='table' ORDER BY name");
            while (rs.next()) {
                String tableName = rs.getString("name");
                System.out.println("   Taulu: " + tableName);
            }
            
            // Näytä document-taulun sarakkeet
            System.out.println("\n📋 Document-taulun sarakkeet:");
            rs = stmt.executeQuery("PRAGMA table_info(document)");
            while (rs.next()) {
                System.out.printf("   %s (%s)%n", rs.getString("name"), rs.getString("type"));
            }
            
            // Näytä period-taulun sarakkeet
            System.out.println("\n📋 Period-taulun sarakkeet:");
            rs = stmt.executeQuery("PRAGMA table_info(period)");
            while (rs.next()) {
                System.out.printf("   %s (%s)%n", rs.getString("name"), rs.getString("type"));
            }
            
            // 2. Näytä nykyiset tilikaudet (päivämäärät ovat millisekunteina)
            System.out.println("\n📋 Nykyiset tilikaudet:");
            rs = stmt.executeQuery("SELECT id, start_date, end_date FROM period ORDER BY start_date");
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            while (rs.next()) {
                long startMs = rs.getLong("start_date");
                long endMs = rs.getLong("end_date");
                String startStr = sdf.format(new java.util.Date(startMs));
                String endStr = sdf.format(new java.util.Date(endMs));
                System.out.printf("   ID=%d: %s - %s%n", rs.getInt("id"), startStr, endStr);
            }
            
            // 3. Näytä tositteiden päivämääräjakauma
            System.out.println("\n📋 Tositteiden päivämääräjakauma:");
            rs = stmt.executeQuery("SELECT strftime('%Y', date/1000, 'unixepoch') as year, COUNT(*) as cnt FROM document GROUP BY year ORDER BY year");
            while (rs.next()) {
                System.out.printf("   Vuosi %s: %d tositetta%n", rs.getString("year"), rs.getInt("cnt"));
            }
            
            // 4. Tarkista onko 2024 tilikausi jo olemassa
            // 2024-01-01 00:00:00 UTC = 1704067200000 ms
            // 2024-12-31 23:59:59 UTC = 1735689599000 ms  
            long start2024 = 1704067200000L; // 2024-01-01
            long end2024 = 1735689599000L;   // 2024-12-31
            
            // 2023 tilikausi
            long start2023 = 1672531200000L; // 2023-01-01
            long end2023 = 1704067199000L;   // 2023-12-31
            
            // Luo 2023 tilikausi jos tarvitaan
            rs = stmt.executeQuery("SELECT id FROM period WHERE start_date >= " + (start2023 - 86400000L) + 
                                   " AND start_date <= " + (start2023 + 86400000L));
            int period2023Id;
            if (!rs.next()) {
                stmt.executeUpdate("INSERT INTO period (start_date, end_date, locked) VALUES (" + 
                                   start2023 + ", " + end2023 + ", 0)");
                rs = stmt.executeQuery("SELECT last_insert_rowid()");
                rs.next();
                period2023Id = rs.getInt(1);
                System.out.println("\n✅ Luotu uusi tilikausi 2023 (ID=" + period2023Id + ")");
                
                // Siirrä 2023 tositteet
                String update2023 = "UPDATE document SET period_id = " + period2023Id + 
                    " WHERE date >= " + start2023 + " AND date <= " + end2023 + 
                    " AND period_id != " + period2023Id;
                int moved2023 = stmt.executeUpdate(update2023);
                System.out.println("✅ Siirretty " + moved2023 + " tositetta tilikauteen 2023");
            }
            
            rs = stmt.executeQuery("SELECT id FROM period WHERE start_date >= " + (start2024 - 86400000L) + 
                                   " AND start_date <= " + (start2024 + 86400000L));
            int period2024Id;
            
            if (rs.next()) {
                period2024Id = rs.getInt("id");
                System.out.println("\n✅ Tilikausi 2024 on jo olemassa (ID=" + period2024Id + ")");
            } else {
                // Luo uusi tilikausi 2024
                stmt.executeUpdate("INSERT INTO period (start_date, end_date, locked) VALUES (" + 
                                   start2024 + ", " + end2024 + ", 0)");
                rs = stmt.executeQuery("SELECT last_insert_rowid()");
                rs.next();
                period2024Id = rs.getInt(1);
                System.out.println("\n✅ Luotu uusi tilikausi 2024 (ID=" + period2024Id + ")");
            }
            
            // 5. Siirrä vuoden 2024 tositteet oikeaan tilikauteen
            // Tositteet joiden date on välillä 2024-01-01 - 2024-12-31
            String updateSql = "UPDATE document SET period_id = " + period2024Id + 
                " WHERE date >= " + start2024 + " AND date <= " + end2024 + 
                " AND period_id != " + period2024Id;
            System.out.println("SQL: " + updateSql);
            int updated = stmt.executeUpdate(updateSql);
            System.out.println("✅ Siirretty " + updated + " tositetta tilikauteen 2024");
            
            // 6. Näytä lopputilanne
            System.out.println("\n📋 Lopputilanne - tilikaudet:");
            rs = stmt.executeQuery("SELECT p.id, p.start_date, p.end_date, COUNT(d.id) as doc_count " +
                                   "FROM period p LEFT JOIN document d ON p.id = d.period_id " +
                                   "GROUP BY p.id ORDER BY p.start_date");
            while (rs.next()) {
                long startMs = rs.getLong("start_date");
                long endMs = rs.getLong("end_date");
                String startStr = sdf.format(new java.util.Date(startMs));
                String endStr = sdf.format(new java.util.Date(endMs));
                System.out.printf("   ID=%d: %s - %s (%d tositetta)%n", 
                    rs.getInt("id"), startStr, endStr, rs.getInt("doc_count"));
            }
            
            conn.commit();
            System.out.println("\n✅ Tietokanta korjattu onnistuneesti!");
            
        } catch (Exception e) {
            System.err.println("❌ Virhe: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
