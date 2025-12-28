package kirjanpito.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.io.File;
import java.io.FileOutputStream;
import java.util.List;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingWorker;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;

import kirjanpito.db.AttachmentDAO;
import kirjanpito.db.DataAccessException;
import kirjanpito.db.DataSource;
import kirjanpito.db.Document;
import kirjanpito.db.Session;
import kirjanpito.models.Attachment;
import kirjanpito.util.PdfUtils;

/**
 * Dialogi PDF-liitteiden hallintaan.
 * 
 * Näyttää tositteen liitteet listana ja mahdollistaa:
 * - Liitteiden lisäämisen (tiedostosta)
 * - Liitteiden poistamisen
 * - Liitteiden viemisen tiedostojärjestelmään
 * 
 * @since 2.2.0
 */
public class AttachmentsDialog extends BaseDialog {
    
    private static final long serialVersionUID = 1L;
    
    private final DataSource dataSource;
    private final Document document;
    private final int documentId;
    
    private JList<Attachment> attachmentList;
    private DefaultListModel<Attachment> listModel;
    private JButton addButton;
    private JButton removeButton;
    private JButton exportButton;
    private JLabel statusLabel;
    
    /**
     * Luo uusi liitteiden hallintadialogi.
     * 
     * @param owner parent frame
     * @param dataSource tietokantayhteys
     * @param document tosite, jolle liitteet kuuluvat
     */
    public AttachmentsDialog(Frame owner, DataSource dataSource, Document document) {
        super(owner, "PDF-liitteet - Tosite " + document.getNumber());
        this.dataSource = dataSource;
        this.document = document;
        this.documentId = document.getId();
        
        initialize();
        loadAttachments();
    }
    
    @Override
    protected JPanel createContentPanel() {
        JPanel panel = new JPanel(new BorderLayout(UIConstants.COMPONENT_SPACING, UIConstants.COMPONENT_SPACING));
        panel.setBorder(UIConstants.DIALOG_TOP_BORDER);
        
        // List model
        listModel = new DefaultListModel<>();
        attachmentList = new JList<>(listModel);
        attachmentList.setCellRenderer(new AttachmentListCellRenderer());
        attachmentList.addListSelectionListener(e -> updateButtons());
        
        JScrollPane scrollPane = new JScrollPane(attachmentList);
        scrollPane.setPreferredSize(new java.awt.Dimension(500, 300));
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, UIConstants.COMPONENT_SPACING, 0));
        
        addButton = new JButton("Lisää PDF...");
        addButton.addActionListener(e -> addAttachment());
        buttonPanel.add(addButton);
        
        removeButton = new JButton("Poista");
        removeButton.addActionListener(e -> removeAttachment());
        removeButton.setEnabled(false);
        buttonPanel.add(removeButton);
        
        exportButton = new JButton("Vie tiedostoon...");
        exportButton.addActionListener(e -> exportAttachment());
        exportButton.setEnabled(false);
        buttonPanel.add(exportButton);
        
        panel.add(buttonPanel, BorderLayout.NORTH);
        
        // Status label
        statusLabel = new JLabel(" ");
        statusLabel.setBorder(UIConstants.SMALL_BORDER);
        panel.add(statusLabel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    @Override
    protected JPanel createButtonPanel() {
        // Override to only show Close button (which is OK button)
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, UIConstants.COMPONENT_SPACING, 0));
        panel.setBorder(UIConstants.BUTTON_PANEL_BORDER);
        
        okButton = new JButton("Sulje");
        okButton.addActionListener(e -> onOK());
        panel.add(okButton);
        
        getRootPane().setDefaultButton(okButton);
        
        return panel;
    }
    
    /**
     * Lataa liitteet tietokannasta.
     */
    private void loadAttachments() {
        SwingWorker<List<Attachment>, Void> worker = new SwingWorker<List<Attachment>, Void>() {
            @Override
            protected List<Attachment> doInBackground() throws Exception {
                Session session = dataSource.openSession();
                try {
                    AttachmentDAO dao = dataSource.getAttachmentDAO(session);
                    return dao.findByDocumentId(documentId);
                } finally {
                    session.close();
                }
            }
            
            @Override
            protected void done() {
                try {
                    List<Attachment> attachments = get();
                    listModel.clear();
                    for (Attachment att : attachments) {
                        listModel.addElement(att);
                    }
                    updateStatus();
                    updateButtons();
                } catch (Exception e) {
                    SwingUtils.showErrorMessage(AttachmentsDialog.this, 
                        "Virhe ladattaessa liitteitä: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }
    
    /**
     * Lisää uuden liitteen.
     */
    private void addAttachment() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("PDF Files", "pdf"));
        fileChooser.setDialogTitle("Valitse PDF-tiedosto");
        
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            
            SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    byte[] data = java.nio.file.Files.readAllBytes(file.toPath());
                    
                    // Validate PDF
                    if (!PdfUtils.INSTANCE.isValidPdf(data)) {
                        throw new IllegalArgumentException("Tiedosto ei ole kelvollinen PDF-tiedosto");
                    }
                    
                    // Check file size
                    if (data.length > Attachment.MAX_FILE_SIZE) {
                        throw new IllegalArgumentException(
                            String.format("Tiedosto on liian suuri (%.2f MB, max 10 MB)", 
                                data.length / 1024.0 / 1024.0));
                    }
                    
                    // Show warning for large files
                    if (data.length > Attachment.WARNING_FILE_SIZE) {
                        SwingUtilities.invokeLater(() -> {
                            int result = JOptionPane.showConfirmDialog(
                                AttachmentsDialog.this,
                                String.format("PDF-tiedosto on suuri (%.2f MB). Haluatko jatkaa?", 
                                    data.length / 1024.0 / 1024.0),
                                "Suuri tiedosto",
                                JOptionPane.YES_NO_OPTION,
                                JOptionPane.WARNING_MESSAGE);
                            if (result != JOptionPane.YES_OPTION) {
                                cancel(true);
                            }
                        });
                        if (isCancelled()) {
                            return null;
                        }
                    }
                    
                    // Calculate page count
                    Integer pageCount = PdfUtils.INSTANCE.calculatePageCount(data);
                    
                    // Create attachment
                    Attachment attachment = Attachment.Companion.fromFile(
                        documentId,
                        file.getName(),
                        data,
                        null
                    );
                    
                    // Save to database
                    Session session = dataSource.openSession();
                    try {
                        AttachmentDAO dao = dataSource.getAttachmentDAO(session);
                        dao.save(attachment);
                        session.commit();
                    } finally {
                        session.close();
                    }
                    
                    return null;
                }
                
                @Override
                protected void done() {
                    try {
                        get(); // Check for exceptions
                        loadAttachments(); // Reload list
                        JOptionPane.showMessageDialog(AttachmentsDialog.this, 
                            "Liite lisätty onnistuneesti", 
                            "Tilitin", 
                            JOptionPane.INFORMATION_MESSAGE);
                    } catch (Exception e) {
                        SwingUtils.showErrorMessage(AttachmentsDialog.this, 
                            "Virhe lisättäessä liitettä: " + e.getMessage());
                    }
                }
            };
            worker.execute();
        }
    }
    
    /**
     * Poistaa valitun liitteen.
     */
    private void removeAttachment() {
        Attachment selected = attachmentList.getSelectedValue();
        if (selected == null) {
            return;
        }
        
        int result = JOptionPane.showConfirmDialog(
            this,
            "Haluatko varmasti poistaa liitteen \"" + selected.getFilename() + "\"?",
            "Poista liite",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);
        
        if (result != JOptionPane.YES_OPTION) {
            return;
        }
        
        SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                Session session = dataSource.openSession();
                try {
                    AttachmentDAO dao = dataSource.getAttachmentDAO(session);
                    boolean deleted = dao.delete(selected.getId());
                    if (deleted) {
                        session.commit();
                    }
                    return deleted;
                } finally {
                    session.close();
                }
            }
            
            @Override
            protected void done() {
                try {
                    if (get()) {
                        loadAttachments(); // Reload list
                        JOptionPane.showMessageDialog(AttachmentsDialog.this, 
                            "Liite poistettu", 
                            "Tilitin", 
                            JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        SwingUtils.showErrorMessage(AttachmentsDialog.this, "Liitteen poisto epäonnistui");
                    }
                } catch (Exception e) {
                    SwingUtils.showErrorMessage(AttachmentsDialog.this, 
                        "Virhe poistettaessa liitettä: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }
    
    /**
     * Vie valitun liitteen tiedostojärjestelmään.
     */
    private void exportAttachment() {
        Attachment selected = attachmentList.getSelectedValue();
        if (selected == null) {
            return;
        }
        
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("PDF Files", "pdf"));
        fileChooser.setDialogTitle("Tallenna PDF-tiedosto");
        fileChooser.setSelectedFile(new File(selected.getFilename()));
        
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            
            SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    try (FileOutputStream out = new FileOutputStream(file)) {
                        out.write(selected.getData());
                    }
                    return null;
                }
                
                @Override
                protected void done() {
                    try {
                        get(); // Check for exceptions
                        JOptionPane.showMessageDialog(AttachmentsDialog.this, 
                            "PDF tallennettu: " + file.getAbsolutePath(), 
                            "Tilitin", 
                            JOptionPane.INFORMATION_MESSAGE);
                    } catch (Exception e) {
                        SwingUtils.showErrorMessage(AttachmentsDialog.this, 
                            "Virhe tallennettaessa tiedostoa: " + e.getMessage());
                    }
                }
            };
            worker.execute();
        }
    }
    
    /**
     * Päivittää painikkeiden tilan.
     */
    private void updateButtons() {
        boolean hasSelection = attachmentList.getSelectedValue() != null;
        removeButton.setEnabled(hasSelection);
        exportButton.setEnabled(hasSelection);
    }
    
    /**
     * Päivittää tilakentän.
     */
    private void updateStatus() {
        int count = listModel.getSize();
        if (count == 0) {
            statusLabel.setText("Ei liitteitä");
        } else {
            statusLabel.setText(String.format("%d liitettä", count));
        }
    }
    
    /**
     * List cell renderer for attachments.
     */
    private static class AttachmentListCellRenderer extends javax.swing.DefaultListCellRenderer {
        @Override
        public java.awt.Component getListCellRendererComponent(
                JList<?> list, Object value, int index,
                boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            
            if (value instanceof Attachment) {
                Attachment att = (Attachment) value;
                setText(String.format("%s (%s, %d sivua)", 
                    att.getFilename(), 
                    att.formatFileSize(),
                    att.getPageCount() != null ? att.getPageCount() : 0));
            }
            
            return this;
        }
    }
}

