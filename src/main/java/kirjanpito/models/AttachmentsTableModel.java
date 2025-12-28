package kirjanpito.models;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.swing.table.AbstractTableModel;
import kirjanpito.models.Attachment;

/**
 * TableModel for displaying attachments in a JTable.
 * 
 * @since 2.2.0
 */
public class AttachmentsTableModel extends AbstractTableModel {
    private static final long serialVersionUID = 1L;
    
    private static final String[] COLUMN_CAPTIONS = new String[] {
        "Tiedosto", "Koko", "Sivut", "Lisätty", "Kuvaus"
    };
    
    private List<Attachment> attachments;
    private SimpleDateFormat dateFormat;
    
    public AttachmentsTableModel() {
        this.attachments = new ArrayList<>();
        this.dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm");
    }
    
    /**
     * Sets the attachments to display.
     * 
     * @param attachments list of attachments
     */
    public void setAttachments(List<Attachment> attachments) {
        this.attachments = attachments != null ? new ArrayList<>(attachments) : new ArrayList<>();
        fireTableDataChanged();
    }
    
    /**
     * Gets the attachment at the specified row.
     * 
     * @param row row index
     * @return attachment or null if row is invalid
     */
    public Attachment getAttachmentAt(int row) {
        if (row >= 0 && row < attachments.size()) {
            return attachments.get(row);
        }
        return null;
    }
    
    /**
     * Adds an attachment to the model.
     * 
     * @param attachment attachment to add
     */
    public void addAttachment(Attachment attachment) {
        attachments.add(attachment);
        fireTableRowsInserted(attachments.size() - 1, attachments.size() - 1);
    }
    
    /**
     * Removes an attachment from the model.
     * 
     * @param row row index of attachment to remove
     */
    public void removeAttachment(int row) {
        if (row >= 0 && row < attachments.size()) {
            attachments.remove(row);
            fireTableRowsDeleted(row, row);
        }
    }
    
    /**
     * Removes an attachment by ID.
     * 
     * @param attachmentId ID of attachment to remove
     */
    public void removeAttachmentById(int attachmentId) {
        for (int i = 0; i < attachments.size(); i++) {
            if (attachments.get(i).getId() == attachmentId) {
                attachments.remove(i);
                fireTableRowsDeleted(i, i);
                break;
            }
        }
    }
    
    /**
     * Clears all attachments.
     */
    public void clear() {
        int size = attachments.size();
        attachments.clear();
        if (size > 0) {
            fireTableRowsDeleted(0, size - 1);
        }
    }
    
    @Override
    public int getColumnCount() {
        return COLUMN_CAPTIONS.length;
    }
    
    @Override
    public int getRowCount() {
        return attachments.size();
    }
    
    @Override
    public String getColumnName(int column) {
        return COLUMN_CAPTIONS[column];
    }
    
    @Override
    public Object getValueAt(int row, int column) {
        if (row < 0 || row >= attachments.size()) {
            return null;
        }
        
        Attachment attachment = attachments.get(row);
        
        switch (column) {
            case 0: // Tiedosto
                return attachment.getFilename();
            case 1: // Koko
                return attachment.formatFileSize();
            case 2: // Sivut
                Integer pageCount = attachment.getPageCount();
                return pageCount != null ? pageCount.toString() : "-";
            case 3: // Lisätty
                return dateFormat.format(Date.from(
                    attachment.getCreatedDate().atZone(java.time.ZoneId.systemDefault()).toInstant()));
            case 4: // Kuvaus
                return attachment.getDescription() != null ? attachment.getDescription() : "";
            default:
                return null;
        }
    }
    
    @Override
    public Class<?> getColumnClass(int column) {
        switch (column) {
            case 0: // Tiedosto
            case 1: // Koko
            case 2: // Sivut
            case 3: // Lisätty
            case 4: // Kuvaus
                return String.class;
            default:
                return Object.class;
        }
    }
    
    /**
     * Gets all attachments in the model.
     * 
     * @return list of attachments
     */
    public List<Attachment> getAttachments() {
        return new ArrayList<>(attachments);
    }
}

