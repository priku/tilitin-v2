package kirjanpito.db;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import kirjanpito.models.Attachment;

/**
 * Tiedostopohjainen AttachmentDAO-toteutus.
 * Tallentaa PDF-liitteet tiedostoina kannan viereen.
 * YHTEENSOPIVA v1.6.1:n kanssa - ei muuta tietokantaa.
 */
public class FileBasedAttachmentDAO implements AttachmentDAO {
    private final Path attachmentsDir;
    
    public FileBasedAttachmentDAO(String databasePath) {
        // Määritä attachments-kansio SQLite-kannan viereen
        Path dbPath = Paths.get(databasePath);
        this.attachmentsDir = dbPath.getParent().resolve("attachments");
        try {
            Files.createDirectories(attachmentsDir);
        } catch (IOException e) {
            throw new RuntimeException("Ei voitu luoda attachments-kansiota", e);
        }
    }
    
    private Path getFilePath(int documentId, String filename) {
        return attachmentsDir.resolve(documentId + "_" + filename);
    }
    
    @Override
    public Attachment findById(int id) throws DataAccessException {
        // Ei tueta - käytä findByDocumentId
        return null;
    }
    
    @Override
    public List<Attachment> findByDocumentId(int documentId) throws DataAccessException {
        List<Attachment> attachments = new ArrayList<>();
        try {
            String prefix = documentId + "_";
            Files.list(attachmentsDir)
                .filter(p -> p.getFileName().toString().startsWith(prefix))
                .forEach(p -> {
                    try {
                        String filename = p.getFileName().toString().substring(prefix.length());
                        byte[] data = Files.readAllBytes(p);
                        Attachment att = Attachment.Companion.fromFile(documentId, filename, data, null);
                        attachments.add(att);
                    } catch (IOException e) {
                        // Ohita virheelliset tiedostot
                    }
                });
        } catch (IOException e) {
            throw new DataAccessException("Liitteiden haku epäonnistui", e);
        }
        return attachments;
    }
    
    @Override
    public int save(Attachment attachment) throws DataAccessException {
        try {
            Path filePath = getFilePath(attachment.getDocumentId(), attachment.getFilename());
            Files.write(filePath, attachment.getData());
            return attachment.getDocumentId();
        } catch (IOException e) {
            throw new DataAccessException("Liitteen tallennus epäonnistui", e);
        }
    }
    
    @Override
    public boolean delete(int id) throws DataAccessException {
        // Ei tueta - käytä document ID + filename
        return false;
    }
    
    public boolean delete(int documentId, String filename) throws DataAccessException {
        try {
            Path filePath = getFilePath(documentId, filename);
            return Files.deleteIfExists(filePath);
        } catch (IOException e) {
            throw new DataAccessException("Liitteen poisto epäonnistui", e);
        }
    }
    
    @Override
    public int countByDocumentId(int documentId) throws DataAccessException {
        return findByDocumentId(documentId).size();
    }
    
    @Override
    public long getTotalSize(int documentId) throws DataAccessException {
        return findByDocumentId(documentId).stream()
            .mapToLong(Attachment::getFileSize)
            .sum();
    }
}
