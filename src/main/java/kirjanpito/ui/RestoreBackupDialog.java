package kirjanpito.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

import kirjanpito.util.BackupService;

import static kirjanpito.ui.UIConstants.*;

/**
 * Dialogi varmuuskopion palauttamiseksi.
 *
 * @since 2.0.3
 * @version 2.0.4 - Updated to extend BaseDialog and use UIConstants
 */
public class RestoreBackupDialog extends BaseDialog {

    private static final long serialVersionUID = 1L;

    private File[] backups;
    private BackupService backupService;
    private JList<String> backupList;
    private DefaultListModel<String> listModel;
    private File restoredFile;

    public RestoreBackupDialog(Frame owner, File[] backups, BackupService backupService) {
        super(owner, "Palauta varmuuskopiosta");
        this.backups = backups;
        this.backupService = backupService;
        initialize();
    }

    @Override
    protected JPanel createContentPanel() {
        JPanel panel = new JPanel(new BorderLayout(COMPONENT_SPACING, COMPONENT_SPACING));

        // Otsikko
        JLabel titleLabel = new JLabel("Valitse palautettava varmuuskopio:");
        titleLabel.setBorder(TITLE_BORDER);
        panel.add(titleLabel, BorderLayout.NORTH);

        // Lista varmuuskopioista
        listModel = new DefaultListModel<>();
        SimpleDateFormat sdf = new SimpleDateFormat("d.M.yyyy HH:mm:ss");

        for (File backup : backups) {
            String displayName = formatBackupName(backup, sdf);
            listModel.addElement(displayName);
        }

        backupList = new JList<>(listModel);
        backupList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        backupList.setSelectedIndex(0);

        JScrollPane scrollPane = new JScrollPane(backupList);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(
            UNIT, MEDIUM_PADDING, UNIT, MEDIUM_PADDING));
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    @Override
    protected JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, COMPONENT_SPACING, 0));
        panel.setBorder(BUTTON_PANEL_BORDER);

        // Custom-painikkeet tälle dialogille
        JButton restoreHereButton = new JButton("Palauta oletuskansioon");
        restoreHereButton.addActionListener(e -> restoreToDefault());

        JButton restoreToButton = new JButton("Palauta kansioon...");
        restoreToButton.addActionListener(e -> restoreToSelected());

        cancelButton = new JButton("Peruuta");
        cancelButton.addActionListener(e -> onCancel());

        panel.add(restoreHereButton);
        panel.add(restoreToButton);
        panel.add(cancelButton);

        return panel;
    }

    @Override
    protected void initialize() {
        super.initialize();
        setSize(500, 350);
        setLocationRelativeTo(getOwner());
    }

    private String formatBackupName(File backup, SimpleDateFormat sdf) {
        String name = backup.getName();
        Date modified = new Date(backup.lastModified());
        long sizeKb = backup.length() / 1024;

        // Yritä purkaa tietokannan nimi
        String dbName = extractDatabaseName(name);

        return String.format("%s  |  %s  |  %d KB", dbName, sdf.format(modified), sizeKb);
    }

    private String extractDatabaseName(String backupFileName) {
        // nimi_HASH_aikaleima.sqlite -> nimi
        String name = backupFileName;
        if (name.endsWith(".sqlite")) {
            name = name.substring(0, name.length() - 7);
        }

        // Poista aikaleima (19 merkkiä + alaviiva)
        if (name.length() > 20) {
            String possibleTimestamp = name.substring(name.length() - 19);
            if (possibleTimestamp.matches("\\d{4}-\\d{2}-\\d{2}_\\d{2}-\\d{2}-\\d{2}")) {
                name = name.substring(0, name.length() - 20);
            }
        }

        // Poista hash (6 merkkiä + alaviiva)
        if (name.length() > 7) {
            String possibleHash = name.substring(name.length() - 6);
            if (possibleHash.matches("[a-f0-9]{6}")) {
                name = name.substring(0, name.length() - 7);
            }
        }

        return name;
    }

    private void restoreToDefault() {
        int selectedIndex = backupList.getSelectedIndex();
        if (selectedIndex < 0) {
            JOptionPane.showMessageDialog(this, "Valitse varmuuskopio ensin.");
            return;
        }

        File selectedBackup = backups[selectedIndex];

        int confirm = JOptionPane.showConfirmDialog(this,
            "Palautetaan:\n" + selectedBackup.getName() + "\n\n" +
            "Kohdekansioon: Tilitin-oletuskansio\n\n" +
            "Jatketaanko?",
            "Vahvista palautus",
            JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            restoredFile = backupService.restoreBackup(selectedBackup, null);
            if (restoredFile != null) {
                result = true;
                dispose();
            } else {
                JOptionPane.showMessageDialog(this,
                    "Varmuuskopion palautus epäonnistui.",
                    "Virhe",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void restoreToSelected() {
        int selectedIndex = backupList.getSelectedIndex();
        if (selectedIndex < 0) {
            JOptionPane.showMessageDialog(this, "Valitse varmuuskopio ensin.");
            return;
        }

        // Valitse kohdekansio
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setDialogTitle("Valitse kohdekansio");
        chooser.setCurrentDirectory(new File(System.getProperty("user.home")));

        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File targetDir = chooser.getSelectedFile();
            File selectedBackup = backups[selectedIndex];

            int confirm = JOptionPane.showConfirmDialog(this,
                "Palautetaan:\n" + selectedBackup.getName() + "\n\n" +
                "Kohdekansioon:\n" + targetDir.getAbsolutePath() + "\n\n" +
                "Jatketaanko?",
                "Vahvista palautus",
                JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                restoredFile = backupService.restoreBackup(selectedBackup, targetDir);
                if (restoredFile != null) {
                    result = true;
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(this,
                        "Varmuuskopion palautus epäonnistui.",
                        "Virhe",
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    public File getRestoredFile() {
        return restoredFile;
    }
}
