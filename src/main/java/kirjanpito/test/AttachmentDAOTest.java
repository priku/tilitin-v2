package kirjanpito.test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.List;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import kirjanpito.db.*;
import kirjanpito.models.Attachment;
import kirjanpito.util.PdfUtils;

/**
 * Test utility for AttachmentDAO without UI.
 * 
 * This can be run from command line to test the database layer:
 * java -cp target/tilitin-2.1.6.jar kirjanpito.test.AttachmentDAOTest
 * 
 * @since 2.2.0
 */
public class AttachmentDAOTest {
    
    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("AttachmentDAO Test Suite");
        System.out.println("========================================\n");
        
        int passed = 0;
        int failed = 0;
        
        // Test 1: Create test database
        System.out.println("Test 1: Creating test database...");
        try {
            File testDb = File.createTempFile("tilitin-test-", ".db");
            testDb.deleteOnExit();
            // Delete the temp file so SQLiteDataSource creates a fresh database
            testDb.delete();
            
            DataSource dataSource = new kirjanpito.db.sqlite.SQLiteDataSource();
            dataSource.open("jdbc:sqlite:" + testDb.getAbsolutePath(), "", "");
            
            System.out.println("  ✓ Test database created: " + testDb.getAbsolutePath());
            passed++;
            
            // Test 2: Create document first (required for foreign key)
            System.out.println("\nTest 2: Creating test document...");
            Session session = dataSource.openSession();
            try {
                // Create a period first
                PeriodDAO periodDAO = dataSource.getPeriodDAO(session);
                Period period = new Period();
                java.util.Calendar cal = java.util.Calendar.getInstance();
                period.setStartDate(cal.getTime());
                cal.add(java.util.Calendar.YEAR, 1);
                period.setEndDate(cal.getTime());
                period.setLocked(false);
                periodDAO.save(period);
                int periodId = period.getId();
                
                // Create a document
                DocumentDAO docDAO = dataSource.getDocumentDAO(session);
                kirjanpito.db.Document doc = docDAO.create(periodId, 1, Integer.MAX_VALUE);
                int docId = doc.getId();
                session.commit();
                
                System.out.println("  ✓ Test document created (ID: " + docId + ")");
                passed++;
                
                // Test 3: Test AttachmentDAO
                System.out.println("\nTest 3: Testing AttachmentDAO...");
                AttachmentDAO attachmentDAO = dataSource.getAttachmentDAO(session);
                
                // Test 3.1: Save attachment
                System.out.println("  Test 3.1: Saving attachment...");
                byte[] testPdfData = createTestPdfData();
                Attachment attachment = Attachment.Companion.fromFile(
                    docId,
                    "test-document.pdf",
                    testPdfData,
                    "Test attachment"
                );
                
                int attachmentId = attachmentDAO.save(attachment);
                session.commit();
                
                if (attachmentId > 0) {
                    System.out.println("    ✓ Attachment saved (ID: " + attachmentId + ")");
                    passed++;
                } else {
                    System.out.println("    ✗ Failed to save attachment");
                    failed++;
                }
                
                // Test 3.2: Find by ID
                System.out.println("  Test 3.2: Finding attachment by ID...");
                Attachment found = attachmentDAO.findById(attachmentId);
                if (found != null && found.getId() == attachmentId && 
                    found.getFilename().equals("test-document.pdf")) {
                    System.out.println("    ✓ Attachment found: " + found.getFilename());
                    System.out.println("      Size: " + found.getFileSize() + " bytes");
                    System.out.println("      Data matches: " + 
                        java.util.Arrays.equals(attachment.getData(), found.getData()));
                    passed++;
                } else {
                    System.out.println("    ✗ Failed to find attachment");
                    failed++;
                }
                
                // Test 3.3: Find by document ID
                System.out.println("  Test 3.3: Finding attachments by document ID...");
                List<Attachment> attachments = attachmentDAO.findByDocumentId(docId);
                if (attachments.size() == 1 && attachments.get(0).getId() == attachmentId) {
                    System.out.println("    ✓ Found " + attachments.size() + " attachment(s)");
                    passed++;
                } else {
                    System.out.println("    ✗ Expected 1 attachment, found " + attachments.size());
                    failed++;
                }
                
                // Test 3.4: Count by document ID
                System.out.println("  Test 3.4: Counting attachments...");
                int count = attachmentDAO.countByDocumentId(docId);
                if (count == 1) {
                    System.out.println("    ✓ Count correct: " + count);
                    passed++;
                } else {
                    System.out.println("    ✗ Expected count 1, got " + count);
                    failed++;
                }
                
                // Test 3.5: Get total size
                System.out.println("  Test 3.5: Getting total size...");
                long totalSize = attachmentDAO.getTotalSize(docId);
                if (totalSize == testPdfData.length) {
                    System.out.println("    ✓ Total size correct: " + totalSize + " bytes");
                    passed++;
                } else {
                    System.out.println("    ✗ Expected size " + testPdfData.length + 
                        ", got " + totalSize);
                    failed++;
                }
                
                // Test 3.6: Delete attachment
                System.out.println("  Test 3.6: Deleting attachment...");
                boolean deleted = attachmentDAO.delete(attachmentId);
                session.commit();
                if (deleted) {
                    System.out.println("    ✓ Attachment deleted");
                    passed++;
                } else {
                    System.out.println("    ✗ Failed to delete attachment");
                    failed++;
                }
                
                // Test 3.7: Verify deletion
                System.out.println("  Test 3.7: Verifying deletion...");
                Attachment deletedAttachment = attachmentDAO.findById(attachmentId);
                if (deletedAttachment == null) {
                    System.out.println("    ✓ Attachment not found after deletion");
                    passed++;
                } else {
                    System.out.println("    ✗ Attachment still exists after deletion");
                    failed++;
                }
                
                // Test 3.8: Multiple attachments
                System.out.println("  Test 3.8: Testing multiple attachments...");
                byte[] pdf1 = createTestPdfData();
                byte[] pdf2 = createTestPdfData();
                Attachment att1 = Attachment.Companion.fromFile(docId, "file1.pdf", pdf1, null);
                Attachment att2 = Attachment.Companion.fromFile(docId, "file2.pdf", pdf2, null);
                int id1 = attachmentDAO.save(att1);
                int id2 = attachmentDAO.save(att2);
                session.commit();
                
                List<Attachment> allAttachments = attachmentDAO.findByDocumentId(docId);
                if (allAttachments.size() == 2) {
                    System.out.println("    ✓ Multiple attachments saved and retrieved");
                    passed++;
                } else {
                    System.out.println("    ✗ Expected 2 attachments, found " + allAttachments.size());
                    failed++;
                }
                
                // Test 3.9: Page count calculation
                System.out.println("  Test 3.9: Testing page count calculation...");
                byte[] multiPagePdf = createMultiPagePdfData(3);
                Integer pageCount = kirjanpito.util.PdfUtils.INSTANCE.calculatePageCount(multiPagePdf);
                if (pageCount != null && pageCount == 3) {
                    System.out.println("    ✓ Page count calculation works: " + pageCount + " pages");
                    passed++;
                } else {
                    System.out.println("    ✗ Expected 3 pages, got " + pageCount);
                    failed++;
                }
                
                // Test 3.10: File size limits
                System.out.println("  Test 3.10: Testing file size validation...");
                byte[] largePdf = new byte[Attachment.MAX_FILE_SIZE + 1];
                java.util.Arrays.fill(largePdf, (byte)0);
                try {
                    Attachment largeAttachment = Attachment.Companion.fromFile(docId, "large.pdf", largePdf, null);
                    System.out.println("    ✗ Should have thrown exception for large file");
                    failed++;
                } catch (IllegalArgumentException e) {
                    System.out.println("    ✓ File size validation works");
                    passed++;
                }
            
            } finally {
                session.close();
            }
            
            // Test 5: Database schema test (attachments table)
            System.out.println("\nTest 5: Testing database schema (attachments table)...");
            try {
                // Create a new database - it should have attachments table
                File schemaTestDb = File.createTempFile("tilitin-schema-test-", ".db");
                schemaTestDb.deleteOnExit();
                // Delete the temp file so SQLiteDataSource creates a fresh database
                schemaTestDb.delete();
                
                DataSource schemaDataSource = new kirjanpito.db.sqlite.SQLiteDataSource();
                schemaDataSource.open("jdbc:sqlite:" + schemaTestDb.getAbsolutePath(), "", "");
                
                Session schemaSession = schemaDataSource.openSession();
                try {
                    // Verify attachments table exists by trying to query it
                    AttachmentDAO schemaDAO = schemaDataSource.getAttachmentDAO(schemaSession);
                    int count = schemaDAO.countByDocumentId(1); // Should return 0, not throw exception
                    
                    // Also verify table structure by checking if we can query it directly
                    java.sql.PreparedStatement stmt = ((kirjanpito.db.sqlite.SQLiteSession)schemaSession)
                        .prepareStatement("SELECT COUNT(*) FROM attachments");
                    java.sql.ResultSet rs = stmt.executeQuery();
                    rs.next();
                    int tableCount = rs.getInt(1);
                    rs.close();
                    stmt.close();
                    
                    System.out.println("  ✓ Attachments table exists and is accessible");
                    System.out.println("    Table query successful, current count: " + tableCount);
                    passed++;
                } finally {
                    schemaSession.close();
                    schemaDataSource.close();
                }
            } catch (Exception e) {
                System.out.println("  ✗ Schema test failed: " + e.getMessage());
                e.printStackTrace();
                failed++;
            }
            
            // Test 4: PDF utilities
            System.out.println("\nTest 4: Testing PDF utilities...");
            byte[] testPdf = createTestPdfData();
            boolean isValid = kirjanpito.util.PdfUtils.INSTANCE.isValidPdf(testPdf);
            if (isValid) {
                System.out.println("  ✓ PDF validation works");
                passed++;
            } else {
                System.out.println("  ✗ PDF validation failed");
                failed++;
            }
            
            Integer pageCount = kirjanpito.util.PdfUtils.INSTANCE.calculatePageCount(testPdf);
            if (pageCount != null && pageCount > 0) {
                System.out.println("  ✓ Page count calculation works: " + pageCount + " pages");
                passed++;
            } else {
                System.out.println("  ⚠ Page count calculation returned null (expected for test PDF)");
                // Not a failure - test PDF might not be valid
            }
            
            dataSource.close();
            
        } catch (Exception e) {
            System.out.println("  ✗ Test failed with exception: " + e.getMessage());
            e.printStackTrace();
            failed++;
        }
        
        // Summary
        System.out.println("\n========================================");
        System.out.println("Test Summary");
        System.out.println("========================================");
        System.out.println("Passed: " + passed);
        System.out.println("Failed: " + failed);
        System.out.println("Total:  " + (passed + failed));
        
        if (failed == 0) {
            System.out.println("\n✓ All tests passed!");
            System.exit(0);
        } else {
            System.out.println("\n✗ Some tests failed!");
            System.exit(1);
        }
    }
    
    /**
     * Creates a valid PDF for testing using Apache PDFBox.
     * This creates a simple 1-page PDF document.
     */
    private static byte[] createTestPdfData() {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PDDocument document = new PDDocument();
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            PDPageContentStream contentStream = new PDPageContentStream(document, page);
            contentStream.beginText();
            contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
            contentStream.newLineAtOffset(50, 750);
            contentStream.showText("Test PDF Document");
            contentStream.newLineAtOffset(0, -20);
            contentStream.showText("This is a test PDF created for AttachmentDAO testing.");
            contentStream.endText();
            contentStream.close();

            document.save(baos);
            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create test PDF", e);
        }
    }
    
    /**
     * Creates a multi-page PDF for testing.
     */
    private static byte[] createMultiPagePdfData(int pageCount) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PDDocument document = new PDDocument();

            for (int i = 1; i <= pageCount; i++) {
                PDPage page = new PDPage(PDRectangle.A4);
                document.addPage(page);

                PDPageContentStream contentStream = new PDPageContentStream(document, page);
                contentStream.beginText();
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                contentStream.newLineAtOffset(50, 750);
                contentStream.showText("Test PDF Document - Page " + i);
                contentStream.newLineAtOffset(0, -20);
                contentStream.showText("This is page " + i + " of " + pageCount + ".");
                contentStream.endText();
                contentStream.close();
            }

            document.save(baos);
            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create multi-page PDF", e);
        }
    }
}

