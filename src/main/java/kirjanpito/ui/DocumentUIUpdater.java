package kirjanpito.ui;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.List;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import java.awt.Color;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;

import kirjanpito.db.Document;
import kirjanpito.db.DocumentType;
import kirjanpito.db.EntryTemplate;
import kirjanpito.db.Period;
import kirjanpito.db.Settings;
import kirjanpito.models.DocumentModel;
import kirjanpito.models.EntryTableModel;
import kirjanpito.models.TextFieldWithLockIcon;
import kirjanpito.ui.DateTextField;
import kirjanpito.util.BackupService;
import kirjanpito.util.Registry;

/**
 * Hallinnoi DocumentFrame:n UI-komponenttien päivityksiä.
 * 
 * Tämä luokka eriytetään DocumentFrame:stä Phase 7 -refaktoroinnin osana.
 * Se vastaa kaikkien UI-komponenttien päivityksestä kun dokumentti, sijainti,
 * tai muut tiedot muuttuvat.
 * 
 * @since 2.2.4
 */
public class DocumentUIUpdater {
    
    private final DocumentModel model;
    private final Registry registry;
    private final UIComponents components;
    private final UICallbacks callbacks;
    private final DecimalFormat formatter;
    
    /**
     * Callback-rajapinta DocumentFrame:lle.
     */
    public interface UICallbacks {
        /** Kutsutaan kun tarvitaan entryTable-viite */
        JTable getEntryTable();
        
        /** Kutsutaan kun tarvitaan tableModel-viite */
        EntryTableModel getTableModel();
        
        /** Kutsutaan kun tarvitaan attachmentsPanel-viite */
        Object getAttachmentsPanel();
        
        /** Kutsutaan kun tarvitaan entryTemplateListener */
        ActionListener getEntryTemplateListener();
        
        /** Kutsutaan kun tarvitaan docTypeListener */
        ActionListener getDocTypeListener();
        
        /** Kutsutaan kun tarvitaan editDocTypesMenuItem */
        JMenuItem getEditDocTypesMenuItem();
        
        /** Kutsutaan kun tarvitaan createEntryTemplateMenuItem */
        JMenuItem getCreateEntryTemplateMenuItem();
        
        /** Kutsutaan kun tarvitaan editEntryTemplatesMenuItem */
        JMenuItem getEditEntryTemplatesMenuItem();
        
        /** Kutsutaan kun tarvitaan findDocumentTypeByNumber */
        int findDocumentTypeByNumber(int number);
        
        /** Kutsutaan kun tarvitaan setComponentsEnabled */
        void setComponentsEnabled(boolean read, boolean create, boolean edit);
        
        /** Kutsutaan kun tarvitaan setTitle */
        void setTitle(String title);
        
        /** Kutsutaan kun tarvitaan updateBackupStatusLabel */
        void updateBackupStatusLabel();
        
        /** Kutsutaan kun tarvitaan updateTableSettings */
        void updateTableSettings();
    }
    
    /**
     * UI-komponenttien viitteet.
     */
    public static class UIComponents {
        public TextFieldWithLockIcon numberTextField;
        public DateTextField dateTextField;
        public JLabel debitTotalLabel;
        public JLabel creditTotalLabel;
        public JLabel differenceLabel;
        public JPanel searchPanel;
        public JTextField searchPhraseTextField;
        public JLabel documentLabel;
        public JLabel periodLabel;
        public JLabel documentTypeLabel;
        public JLabel backupStatusLabel;
        public JMenu entryTemplateMenu;
        public JMenu docTypeMenu;
        public JCheckBoxMenuItem[] docTypeMenuItems;
        public JCheckBoxMenuItem searchMenuItem;
    }
    
    /**
     * Konstruktori.
     * 
     * @param model DocumentModel-instanssi
     * @param registry Registry-instanssi
     * @param components UI-komponenttien viitteet
     * @param callbacks Callback-rajapinta DocumentFrame:lle
     * @param formatter DecimalFormat-instanssi
     */
    public DocumentUIUpdater(DocumentModel model, Registry registry, 
            UIComponents components, UICallbacks callbacks, DecimalFormat formatter) {
        this.model = model;
        this.registry = registry;
        this.components = components;
        this.callbacks = callbacks;
        this.formatter = formatter;
    }
    
    /**
     * Päivittää tositteen tiedot.
     */
    public void updateDocument() {
        Document document = model.getDocument();

        if (document == null) {
            components.numberTextField.setText("");
            components.dateTextField.setDate(null);
        }
        else {
            components.numberTextField.setText(Integer.toString(document.getNumber()));
            components.dateTextField.setDate(document.getDate());
            components.dateTextField.setBaseDate(document.getDate());

            /* Uuden tositteen päivämäärä on kopioitu edellisestä
             * tositteesta. Valitaan päivämääräkentän teksti, jotta
             * uusi päivämäärä voidaan kirjoittaa päälle. */
            if (document.getId() <= 0) {
                components.dateTextField.select(0, components.dateTextField.getText().length());
            }

            components.dateTextField.requestFocus();
        }

        boolean documentEditable = model.isDocumentEditable();
        callbacks.getTableModel().fireTableDataChanged();
        components.numberTextField.setLockIconVisible(document != null && !documentEditable);
        callbacks.setComponentsEnabled(document != null, model.isPeriodEditable(), documentEditable);
        
        // Update attachments panel with current document ID
        Object attachmentsPanel = callbacks.getAttachmentsPanel();
        if (attachmentsPanel != null) {
            int documentId = (document != null && document.getId() > 0) ? document.getId() : 0;
            try {
                // Use reflection to call setDocumentId if available
                attachmentsPanel.getClass().getMethod("setDocumentId", int.class)
                    .invoke(attachmentsPanel, documentId);
            } catch (Exception e) {
                // Ignore if method doesn't exist
            }
        }
    }
    
    /**
     * Päivittää tositteen järjestysnumeron, tositteiden
     * lukumäärän ja tositelajin tilariville.
     */
    public void updatePosition() {
        int count = model.getDocumentCount();
        int countTotal = model.getDocumentCountTotal();

        if (count != countTotal) {
            components.documentLabel.setText(String.format("Tosite %d / %d (%d)",
                    model.getDocumentPosition() + 1,
                    model.getDocumentCount(),
                    model.getDocumentCountTotal()));
        }
        else {
            components.documentLabel.setText(String.format("Tosite %d / %d",
                    model.getDocumentPosition() + 1,
                    model.getDocumentCount()));
        }

        DocumentType type;

        // Note: searchEnabled check will be handled by DocumentFrame
        // For now, always use model's document type
        type = model.getDocumentType();

        if (type == null) {
            components.documentTypeLabel.setText("");
        }
        else {
            components.documentTypeLabel.setText(type.getName());
        }
    }
    
    /**
     * Päivittää summarivin tiedot.
     */
    public void updateTotalRow(BigDecimal debitTotal, BigDecimal creditTotal) {
        BigDecimal difference = creditTotal.subtract(debitTotal).abs();
        components.debitTotalLabel.setText(formatter.format(debitTotal));
        components.creditTotalLabel.setText(formatter.format(creditTotal));
        
        // Käytä teeman mukaista error-väriä epätasapainon korostamiseen
        Color errorColor = UIConstants.getErrorColor();
        components.differenceLabel.setForeground(difference.compareTo(BigDecimal.ZERO) == 0 ?
                components.debitTotalLabel.getForeground() : errorColor);
        components.differenceLabel.setText(formatter.format(difference));
    }
    
    /**
     * Päivittää tilikauden tiedot tilariville.
     */
    public void updatePeriod() {
        Period period = registry.getPeriod();

        if (period == null) {
            components.periodLabel.setText("");
        }
        else {
            DateFormat dateFormat = new SimpleDateFormat("d.M.yyyy");
            String text = "Tilikausi " +
                dateFormat.format(period.getStartDate()) + " - " +
                dateFormat.format(period.getEndDate());
            components.periodLabel.setText(text);
        }
    }
    
    /**
     * Päivittää ikkunan otsikkorivin.
     */
    public void updateTitle() {
        Settings settings = registry.getSettings();
        String name = (settings == null) ? null : settings.getName();
        String title;

        if (name == null || name.length() == 0) {
            title = "Tilitin";
        }
        else {
            title = name + " - Tilitin";
        }

        callbacks.setTitle(title);
    }
    
    /**
     * Päivittää backup-indikaattorin tilariville.
     */
    public void updateBackupStatusLabel() {
        BackupService backup = BackupService.getInstance();
        
        if (!backup.isEnabled()) {
            components.backupStatusLabel.setText("○ Backup");
            components.backupStatusLabel.setForeground(UIConstants.getMutedColor());
        } else if (backup.isAutoBackupEnabled()) {
            String cloudName = backup.getCloudServiceName();
            if (cloudName != null) {
                components.backupStatusLabel.setText("☁ AutoBackup");
                components.backupStatusLabel.setForeground(UIConstants.getSuccessColor());
                components.backupStatusLabel.setToolTipText("AutoBackup käytössä • " + cloudName + " • Klikkaa muuttaaksesi");
            } else {
                components.backupStatusLabel.setText("◉ AutoBackup");
                components.backupStatusLabel.setForeground(UIConstants.getInfoColor());
                components.backupStatusLabel.setToolTipText("AutoBackup käytössä • Klikkaa muuttaaksesi");
            }
        } else {
            components.backupStatusLabel.setText("● Backup");
            components.backupStatusLabel.setForeground(UIConstants.getMutedColor());
            components.backupStatusLabel.setToolTipText("Varmuuskopiointi käytössä • Klikkaa muuttaaksesi");
        }
    }
    
    /**
     * Päivittää vientimallivalikon.
     */
    public void updateEntryTemplates() {
        List<EntryTemplate> templates = registry.getEntryTemplates();
        JMenuItem menuItem;
        int count = 0;

        components.entryTemplateMenu.removeAll();

        if (templates != null) {
            int prevNumber = -1;

            for (EntryTemplate template : templates) {
                if (template.getNumber() != prevNumber) {
                    prevNumber = template.getNumber();
                    menuItem = new JMenuItem(template.getName());
                    menuItem.addActionListener(callbacks.getEntryTemplateListener());

                    /* 10 ensimmäiselle vientimallille näppäinoikotie. */
                    if (template.getNumber() >= 1 && template.getNumber() <= 10) {
                        menuItem.setAccelerator(KeyStroke.getKeyStroke(
                                '0' + (template.getNumber() % 10),
                                InputEvent.ALT_DOWN_MASK));
                    }

                    components.entryTemplateMenu.add(menuItem);
                    menuItem.setActionCommand(Integer.toString(template.getNumber()));
                    count++;
                }
            }
        }

        if (count == 0) {
            menuItem = new JMenuItem("Ei vientimalleja");
            menuItem.setEnabled(false);
            components.entryTemplateMenu.add(menuItem);
        }

        components.entryTemplateMenu.addSeparator();
        components.entryTemplateMenu.add(callbacks.getCreateEntryTemplateMenuItem());
        components.entryTemplateMenu.add(callbacks.getEditEntryTemplatesMenuItem());
    }
    
    /**
     * Päivittää tositelajivalikon.
     */
    public void updateDocumentTypes() {
        char[] accelerators = {'Q', 'W', 'E', 'R', 'T', 'Y', 'U', 'I', 'O', 'P'};
        List<DocumentType> docTypes = registry.getDocumentTypes();
        JCheckBoxMenuItem menuItem;
        int selectedIndex = model.getDocumentTypeIndex();
        int index = 0;

        components.docTypeMenu.removeAll();

        if (docTypes != null) {
            components.docTypeMenuItems = new JCheckBoxMenuItem[docTypes.size()];

            for (DocumentType type : docTypes) {
                menuItem = new JCheckBoxMenuItem(type.getName());
                menuItem.addActionListener(callbacks.getDocTypeListener());
                menuItem.setSelected(index == selectedIndex);

                /* 10 ensimmäiselle tositelajille näppäinoikotie. */
                if (type.getNumber() >= 1 && type.getNumber() <= 10) {
                    menuItem.setAccelerator(KeyStroke.getKeyStroke(
                            accelerators[type.getNumber() - 1],
                            InputEvent.ALT_DOWN_MASK));
                }

                components.docTypeMenu.add(menuItem);
                components.docTypeMenuItems[index] = menuItem;
                menuItem.setActionCommand(Integer.toString(index++));
            }
        }

        if (index == 0) {
            JMenuItem tmp = new JMenuItem("Ei tositelajeja");
            tmp.setEnabled(false);
            components.docTypeMenu.add(tmp);
        }

        components.docTypeMenu.addSeparator();
        components.docTypeMenu.add(callbacks.getEditDocTypesMenuItem());
    }
    
    /**
     * Näyttää tai piilottaa hakupaneelin.
     */
    public void updateSearchPanel(boolean searchEnabled) {
        components.searchPhraseTextField.setText("");
        components.searchPanel.setVisible(searchEnabled);
        components.searchMenuItem.setSelected(searchEnabled);

        if (searchEnabled) {
            components.searchPhraseTextField.requestFocusInWindow();
        }
    }
    
    /**
     * Päivittää tositelajivalinnan.
     *
     * @param index uuden tositelajin järjestysnumero
     */
    public void selectDocumentTypeMenuItem(int index) {
        int oldIndex = model.getDocumentTypeIndex();

        // Note: saveDocumentIfChanged check will be handled by DocumentFrame
        // This method is called after saveDocumentIfChanged has been checked

        if (oldIndex >= 0)
            components.docTypeMenuItems[oldIndex].setSelected(false);

        components.docTypeMenuItems[index].setSelected(true);
    }
}

