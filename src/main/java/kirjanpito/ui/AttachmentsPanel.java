package kirjanpito.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.filechooser.FileNameExtensionFilter;
import kirjanpito.db.DataAccessException;
import kirjanpito.db.DataSource;
import kirjanpito.db.Session;
import kirjanpito.db.AttachmentDAO;
import kirjanpito.models.Attachment;
import kirjanpito.models.AttachmentsTableModel;
import kirjanpito.util.PdfUtils;

/**
 * Panel for displaying and managing PDF attachments for a document.
 * 
 * @since 2.2.0
 */
public class AttachmentsPanel extends JPanel {
    private static final long serialVersionUID = 1L;
    
    private DataSource dataSource;
    private int documentId;
    private AttachmentsTableModel tableModel;
    private JTable table;
    private JButton addButton;
    private JButton removeButton;
    private JButton exportButton;
    private JFileChooser fileChooser;
    
    /**
     * Creates a new AttachmentsPanel.
     * 
     * @param dataSource data source for database access
     * @param documentId ID of the document
     */
    public AttachmentsPanel(DataSource dataSource, int documentId) {
        this.dataSource = dataSource;
        this.documentId = documentId;
        
        setLayout(new BorderLayout());
        setBorder(javax.swing.BorderFactory.createEmptyBorder(
            UIConstants.DIALOG_PADDING, 
            UIConstants.DIALOG_PADDING, 
            UIConstants.DIALOG_PADDING, 
            UIConstants.DIALOG_PADDING));
        
        createComponents();
        loadAttachments();
    }
    
    /**
     * Creates the UI components.
     */
    private void createComponents() {
        // Create table model and table
        tableModel = new AttachmentsTableModel();
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(table.getFontMetrics(table.getFont()).getHeight() + 6);
        table.getTableHeader().setReorderingAllowed(false);
        
        // Set column widths
        table.getColumnModel().getColumn(0).setPreferredWidth(200); // Tiedosto
        table.getColumnModel().getColumn(1).setPreferredWidth(80);  // Koko
        table.getColumnModel().getColumn(2).setPreferredWidth(60);  // Sivut
        table.getColumnModel().getColumn(3).setPreferredWidth(140); // Lisätty
        table.getColumnModel().getColumn(4).setPreferredWidth(200); // Kuvaus
        
        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);
        
        // Create button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, UIConstants.SMALL_PADDING, UIConstants.SMALL_PADDING));
        
        addButton = new JButton("Lisää PDF");
        addButton.addActionListener(e -> addAttachment());
        buttonPanel.add(addButton);
        
        removeButton = new JButton("Poista");
        removeButton.addActionListener(e -> removeAttachment());
        removeButton.setEnabled(false);
        buttonPanel.add(removeButton);
        
        exportButton = new JButton("Vie tiedostoksi");
        exportButton.addActionListener(e -> exportAttachment());
        exportButton.setEnabled(false);
        buttonPanel.add(exportButton);
        
        // Enable/disable buttons based on selection
        table.getSelectionModel().addListSelectionListener(e -> {
            boolean hasSelection = table.getSelectedRow() >= 0;
            removeButton.setEnabled(hasSelection);
            exportButton.setEnabled(hasSelection);
        });
        
        add(buttonPanel, BorderLayout.PAGE_END);
        
        // Initialize file chooser
        fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("PDF-tiedostot (*.pdf)", "pdf"));
        fileChooser.setMultiSelectionEnabled(false);
    }
    
    /**
     * Loads attachments from the database.
     */
    public void loadAttachments() {
        if (documentId <= 0 || dataSource == null) {
            tableModel.clear();
            return;
        }
        
        Session session = null;
        try {
            session = dataSource.openSession();
            AttachmentDAO dao = dataSource.getAttachmentDAO(session);
            List<Attachment> attachments = dao.findByDocumentId(documentId);
            tableModel.setAttachments(attachments);
            session.commit();
        } catch (DataAccessException e) {
                JOptionPane.showMessageDialog(this,
                "Virhe liitteiden lataamisessa: " + e.getMessage(),
                "Virhe",
                JOptionPane.ERROR_MESSAGE);
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }
    
    /**
     * Sets the document ID and reloads attachments.
     * 
     * @param documentId new document ID
     */
    public void setDocumentId(int documentId) {
        this.documentId = documentId;
        loadAttachments();
    }
    
    /**
     * Sets the data source for database access.
     * 
     * @param dataSource new data source
     */
    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }
    
    /**
     * Adds a new attachment.
     */
    private void addAttachment() {
        if (documentId <= 0) {
            JOptionPane.showMessageDialog(this,
                "Valitse ensin tosite.",
                "Virhe",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int result = fileChooser.showOpenDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }
        
        File selectedFile = fileChooser.getSelectedFile();
        if (selectedFile == null || !selectedFile.exists()) {
            return;
        }
        
        try {
            // Read file
            byte[] fileData = java.nio.file.Files.readAllBytes(selectedFile.toPath());
            
            // Validate PDF
            if (!PdfUtils.INSTANCE.isValidPdf(fileData)) {
                JOptionPane.showMessageDialog(this,
                    "Valittu tiedosto ei ole kelvollinen PDF-tiedosto.",
                    "Virhe",
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Check file size
            if (fileData.length > Attachment.MAX_FILE_SIZE) {
                JOptionPane.showMessageDialog(this,
                    String.format("Tiedosto on liian suuri (%.2f MB). Maksimikoko on %d MB.",
                        fileData.length / 1024.0 / 1024.0,
                        Attachment.MAX_FILE_SIZE / 1024 / 1024),
                    "Virhe",
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Show warning for large files
            if (fileData.length > Attachment.WARNING_FILE_SIZE) {
                int confirm = JOptionPane.showConfirmDialog(this,
                    String.format("Tiedosto on suuri (%.2f MB). Haluatko jatkaa?",
                        fileData.length / 1024.0 / 1024.0),
                    "Varoitus",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);
                if (confirm != JOptionPane.YES_OPTION) {
                    return;
                }
            }
            
            // Calculate page count
            Integer pageCount = PdfUtils.INSTANCE.calculatePageCount(fileData);
            
            // Create attachment
            Attachment attachment = Attachment.Companion.fromFile(
                documentId,
                selectedFile.getName(),
                fileData,
                null // description - could be added later
            );
            
            // Set page count if calculated
            if (pageCount != null) {
                // Create new attachment with page count
                attachment = new Attachment(
                    attachment.getId(),
                    attachment.getDocumentId(),
                    attachment.getFilename(),
                    attachment.getContentType(),
                    attachment.getData(),
                    attachment.getFileSize(),
                    pageCount,
                    attachment.getCreatedDate(),
                    attachment.getDescription()
                );
            }
            
            // Save to database
            Session session = null;
            try {
                session = dataSource.openSession();
                AttachmentDAO dao = dataSource.getAttachmentDAO(session);
                dao.save(attachment);
                session.commit();
                
                // Reload attachments
                loadAttachments();
                
                JOptionPane.showMessageDialog(this,
                    "PDF-liite lisätty onnistuneesti.",
                    "Onnistui",
                    JOptionPane.INFORMATION_MESSAGE);
            } catch (DataAccessException e) {
                JOptionPane.showMessageDialog(this,
                    "Virhe liitteen tallentamisessa: " + e.getMessage(),
                    "Virhe",
                    JOptionPane.ERROR_MESSAGE);
            }
            
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this,
                "Virhe tiedoston lukemisessa: " + e.getMessage(),
                "Virhe",
                JOptionPane.ERROR_MESSAGE);
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this,
                e.getMessage(),
                "Virhe",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Removes the selected attachment.
     */
    private void removeAttachment() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            return;
        }
        
        Attachment attachment = tableModel.getAttachmentAt(selectedRow);
        if (attachment == null) {
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(this,
            "Haluatko varmasti poistaa liitteen \"" + attachment.getFilename() + "\"?",
            "Vahvista poisto",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);
        
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        
        Session session = null;
        try {
            session = dataSource.openSession();
            AttachmentDAO dao = dataSource.getAttachmentDAO(session);
            boolean deleted = dao.delete(attachment.getId());
            session.commit();
            
            if (deleted) {
                loadAttachments();
                JOptionPane.showMessageDialog(this,
                    "Liite poistettu onnistuneesti.",
                    "Onnistui",
                    JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this,
                    "Liitteen poistaminen epäonnistui.",
                    "Virhe",
                    JOptionPane.ERROR_MESSAGE);
            }
        } catch (DataAccessException e) {
            JOptionPane.showMessageDialog(this,
                "Virhe liitteen poistamisessa: " + e.getMessage(),
                "Virhe",
                JOptionPane.ERROR_MESSAGE);
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }
    
    /**
     * Exports the selected attachment to a file.
     */
    private void exportAttachment() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            return;
        }
        
        Attachment attachment = tableModel.getAttachmentAt(selectedRow);
        if (attachment == null) {
            return;
        }
        
        JFileChooser exportChooser = new JFileChooser();
        exportChooser.setFileFilter(new FileNameExtensionFilter("PDF-tiedostot (*.pdf)", "pdf"));
        exportChooser.setSelectedFile(new File(attachment.getFilename()));
        exportChooser.setDialogTitle("Vie PDF-tiedostoksi");
        
        int result = exportChooser.showSaveDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }
        
        File exportFile = exportChooser.getSelectedFile();
        if (exportFile == null) {
            return;
        }
        
        // Add .pdf extension if not present
        if (!exportFile.getName().toLowerCase().endsWith(".pdf")) {
            exportFile = new File(exportFile.getParent(), exportFile.getName() + ".pdf");
        }
        
        // Check if file exists
        if (exportFile.exists()) {
            int overwrite = JOptionPane.showConfirmDialog(this,
                "Tiedosto on jo olemassa. Haluatko korvata sen?",
                "Vahvista",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
            if (overwrite != JOptionPane.YES_OPTION) {
                return;
            }
        }
        
        try (FileOutputStream fos = new FileOutputStream(exportFile)) {
            fos.write(attachment.getData());
            JOptionPane.showMessageDialog(this,
                "PDF-tiedosto viety onnistuneesti.",
                "Onnistui",
                JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this,
                "Virhe tiedoston kirjoittamisessa: " + e.getMessage(),
                "Virhe",
                JOptionPane.ERROR_MESSAGE);
        }
    }
}

