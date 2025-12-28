package kirjanpito.ui;

import java.awt.Color;
import java.awt.Frame;
import java.io.File;
import java.util.logging.Logger;

import javax.swing.JLabel;
import javax.swing.JOptionPane;

import kirjanpito.util.BackupService;

/**
 * Hallinnoi DocumentFrame:n backup-toiminnallisuutta.
 *
 * Vastaa:
 * - Backup-statusindikaattorin päivityksestä
 * - Varmuuskopioin suorittamisesta sulkemisen yhteydessä
 * - Varmuuskopion palauttamisesta dialogin kautta
 *
 * @since 2.1.0
 */
public class DocumentBackupManager {

    private static final Logger logger = Logger.getLogger("kirjanpito");

    private final Frame parentFrame;
    private final JLabel backupStatusLabel;
    private final DatabaseOpener databaseOpener;

    /**
     * Callback-rajapinta tietokannan avaamiseksi.
     * DocumentFrame toteuttaa tämän.
     */
    public interface DatabaseOpener {
        void openSqliteDataSource(File file);
    }

    /**
     * Luo uuden BackupManager-instanssin.
     *
     * @param parentFrame parent-ikkuna dialogeille
     * @param backupStatusLabel status-label jota päivitetään
     * @param databaseOpener callback tietokannan avaamiseksi
     */
    public DocumentBackupManager(Frame parentFrame, JLabel backupStatusLabel,
                                  DatabaseOpener databaseOpener) {
        this.parentFrame = parentFrame;
        this.backupStatusLabel = backupStatusLabel;
        this.databaseOpener = databaseOpener;
    }

    /**
     * Päivittää backup-indikaattorin tilariville.
     */
    public void updateBackupStatusLabel() {
        BackupService backup = BackupService.getInstance();

        if (!backup.isEnabled()) {
            backupStatusLabel.setText("○ Backup");
            backupStatusLabel.setForeground(Color.GRAY);
            backupStatusLabel.setToolTipText(null);
        } else if (backup.isAutoBackupEnabled()) {
            String cloudName = backup.getCloudServiceName();
            if (cloudName != null) {
                backupStatusLabel.setText("☁ AutoBackup");
                backupStatusLabel.setForeground(new Color(0, 128, 0)); // Dark green
                backupStatusLabel.setToolTipText("AutoBackup käytössä • " + cloudName + " • Klikkaa muuttaaksesi");
            } else {
                backupStatusLabel.setText("◉ AutoBackup");
                backupStatusLabel.setForeground(new Color(0, 100, 200)); // Blue
                backupStatusLabel.setToolTipText("AutoBackup käytössä • Klikkaa muuttaaksesi");
            }
        } else {
            backupStatusLabel.setText("● Backup");
            backupStatusLabel.setForeground(Color.DARK_GRAY);
            backupStatusLabel.setToolTipText("Varmuuskopiointi käytössä • Klikkaa muuttaaksesi");
        }
    }

    /**
     * Suorittaa varmuuskopioinnin sulkemisen yhteydessä.
     * Ei estä sulkemista vaikka varmuuskopiointi epäonnistuisi.
     */
    public void performBackupOnClose() {
        BackupService backupService = BackupService.getInstance();

        if (!backupService.isEnabled()) {
            return;
        }

        String uri = backupService.getCurrentDatabase();
        if (uri == null || !uri.contains("sqlite")) {
            return;
        }

        boolean success = backupService.performBackup(uri);

        if (!success) {
            // Näytä varoitus mutta älä estä sulkemista
            logger.warning("Varmuuskopiointi epäonnistui sulkemisen yhteydessä");
        }
    }

    /**
     * Näyttää dialogin varmuuskopion palauttamiseksi.
     * Jos palautus onnistuu, kysyy käyttäjältä haluaako avata palautetun tietokannan.
     */
    public void restoreFromBackup() {
        BackupService backupService = BackupService.getInstance();
        File backupDir = backupService.getBackupDirectory();

        // Tarkista onko backup-kansio määritetty
        if (backupDir == null || !backupDir.exists()) {
            if (!promptToConfigureBackup(backupService)) {
                return; // Käyttäjä peruutti
            }
        }

        // Listaa varmuuskopiot
        File[] backups = backupService.listAllBackups();

        if (backups.length == 0) {
            showNoBackupsMessage(backupDir);
            return;
        }

        // Näytä valintadialogi
        RestoreBackupDialog dialog = new RestoreBackupDialog(parentFrame, backups, backupService);
        dialog.setVisible(true);

        // Käsittele palautettu tiedosto
        File restoredFile = dialog.getRestoredFile();
        if (restoredFile != null) {
            promptToOpenRestoredDatabase(restoredFile);
        }
    }

    /**
     * Kysyy käyttäjältä haluaako määrittää varmuuskopiointiasetukset.
     * @return true jos asetukset määritettiin ja backup-kansio on nyt olemassa
     */
    private boolean promptToConfigureBackup(BackupService backupService) {
        String[] options = {"Määritä asetukset", "Peruuta"};
        int choice = JOptionPane.showOptionDialog(parentFrame,
            "Varmuuskopiokansiota ei ole määritetty tai se ei ole olemassa.\n\n" +
            "Haluatko määrittää varmuuskopiointiasetukset nyt?",
            "Ei varmuuskopioita",
            JOptionPane.DEFAULT_OPTION,
            JOptionPane.INFORMATION_MESSAGE,
            null,
            options,
            options[0]);

        if (choice == 0) {
            // Avaa asetukset
            BackupSettingsDialog.show(parentFrame);

            // Tarkista uudelleen
            File backupDir = backupService.getBackupDirectory();
            return backupDir != null && backupDir.exists();
        }

        return false;
    }

    /**
     * Näyttää viestin että varmuuskopioita ei löytynyt.
     */
    private void showNoBackupsMessage(File backupDir) {
        JOptionPane.showMessageDialog(parentFrame,
            "Varmuuskopiokansiossa ei ole varmuuskopioita.\n\n" +
            "Kansio: " + backupDir.getAbsolutePath(),
            "Ei varmuuskopioita",
            JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Kysyy käyttäjältä haluaako avata palautetun tietokannan.
     */
    private void promptToOpenRestoredDatabase(File restoredFile) {
        int result = JOptionPane.showConfirmDialog(parentFrame,
            "Tietokanta palautettu:\n" + restoredFile.getAbsolutePath() + "\n\n" +
            "Haluatko avata sen nyt?",
            "Palautus onnistui",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);

        if (result == JOptionPane.YES_OPTION) {
            databaseOpener.openSqliteDataSource(restoredFile);
        }
    }
}
