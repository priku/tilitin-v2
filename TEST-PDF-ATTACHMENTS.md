# PDF Attachments - Testing Guide

## Overview

The PDF attachments feature can be tested without UI using the `AttachmentDAOTest` class. This test suite verifies:

1. Database migration (version 14 → 15)
2. AttachmentDAO CRUD operations
3. PDF utilities (validation, page count)
4. File size validation
5. Multiple attachments handling

## Running the Tests

### Prerequisites

1. Build the project:
   ```bash
   mvn clean compile
   ```

2. Package the JAR:
   ```bash
   mvn package
   ```

### Method 1: Run from Command Line

```bash
# Windows (PowerShell)
java -cp "target/tilitin-2.1.6.jar;target/lib/*" kirjanpito.test.AttachmentDAOTest

# Linux/Mac
java -cp "target/tilitin-2.1.6.jar:target/lib/*" kirjanpito.test.AttachmentDAOTest
```

### Method 2: Run with Maven

Add this to `pom.xml` (if not already present):

```xml
<plugin>
    <groupId>org.codehaus.mojo</groupId>
    <artifactId>exec-maven-plugin</artifactId>
    <version>3.1.0</version>
    <configuration>
        <mainClass>kirjanpito.test.AttachmentDAOTest</mainClass>
    </configuration>
</plugin>
```

Then run:
```bash
mvn exec:java
```

### Method 3: Run from IDE

1. Open `src/main/java/kirjanpito/test/AttachmentDAOTest.java`
2. Right-click on the `main` method
3. Select "Run 'AttachmentDAOTest.main()'"

## Test Coverage

### Test 1: Database Creation
- Creates a temporary SQLite database
- Verifies database can be opened

### Test 2: Document Setup
- Creates a test period
- Creates a test document (required for foreign key)

### Test 3: AttachmentDAO Operations

#### 3.1: Save Attachment
- Creates a test PDF using iText
- Saves attachment to database
- Verifies ID is generated

#### 3.2: Find by ID
- Retrieves attachment by ID
- Verifies data integrity

#### 3.3: Find by Document ID
- Retrieves all attachments for a document
- Verifies correct filtering

#### 3.4: Count by Document ID
- Counts attachments for a document
- Verifies count accuracy

#### 3.5: Get Total Size
- Calculates total size of all attachments
- Verifies size calculation

#### 3.6: Delete Attachment
- Deletes an attachment
- Verifies deletion succeeds

#### 3.7: Verify Deletion
- Attempts to find deleted attachment
- Verifies it's not found

#### 3.8: Multiple Attachments
- Saves multiple attachments
- Verifies all are retrieved correctly

#### 3.9: Page Count Calculation
- Creates a 3-page PDF
- Verifies page count is calculated correctly

#### 3.10: File Size Validation
- Attempts to save file exceeding MAX_FILE_SIZE
- Verifies exception is thrown

### Test 4: PDF Utilities
- Tests `PdfUtils.isValidPdf()`
- Tests `PdfUtils.calculatePageCount()`

### Test 5: Database Migration
- Creates a new database
- Verifies migration from version 14 → 15
- Verifies attachments table exists and is accessible

## Expected Output

```
========================================
AttachmentDAO Test Suite
========================================

Test 1: Creating test database...
  ✓ Test database created: C:\Users\...\tilitin-test-12345.db

Test 2: Creating test document...
  ✓ Test document created (ID: 1)

Test 3: Testing AttachmentDAO...
  Test 3.1: Saving attachment...
    ✓ Attachment saved (ID: 1)
  Test 3.2: Finding attachment by ID...
    ✓ Attachment found: test-document.pdf
      Size: 1234 bytes
      Data matches: true
  Test 3.3: Finding attachments by document ID...
    ✓ Found 1 attachment(s)
  Test 3.4: Counting attachments...
    ✓ Count correct: 1
  Test 3.5: Getting total size...
    ✓ Total size correct: 1234 bytes
  Test 3.6: Deleting attachment...
    ✓ Attachment deleted
  Test 3.7: Verifying deletion...
    ✓ Attachment not found after deletion
  Test 3.8: Testing multiple attachments...
    ✓ Multiple attachments saved and retrieved
  Test 3.9: Testing page count calculation...
    ✓ Page count calculation works: 3 pages
  Test 3.10: Testing file size validation...
    ✓ File size validation works

Test 4: Testing PDF utilities...
  ✓ PDF validation works
  ✓ Page count calculation works: 1 pages

Test 5: Testing database migration (version 14 → 15)...
  ✓ Database migration successful (version: 15)
  ✓ Attachments table exists and is accessible

========================================
Test Summary
========================================
Passed: 15
Failed: 0
Total:  15

✓ All tests passed!
```

## Troubleshooting

### Issue: ClassNotFoundException

**Solution:** Make sure all dependencies are in the classpath:
```bash
# Include all JARs from target/lib/
java -cp "target/tilitin-2.1.6.jar;target/lib/*" kirjanpito.test.AttachmentDAOTest
```

### Issue: PDFBox Loader not found

**Solution:** Verify PDFBox is in dependencies:
```bash
mvn dependency:tree | grep pdfbox
```

### Issue: Database migration fails

**Solution:** Check that `upgrade14to15()` is called in DataSource classes:
- `SQLiteDataSource.java`
- `MySQLDataSource.java`
- `PSQLDataSource.java`

### Issue: Tests fail with SQLException

**Solution:** 
1. Check database file permissions
2. Verify SQLite JDBC driver is available
3. Check that schema files are in `src/main/resources/schema/`

## Manual Testing Checklist

After automated tests pass, manually verify:

- [ ] Open existing database (version 14) - migration should run automatically
- [ ] Check database version is now 15
- [ ] Verify `attachments` table exists
- [ ] Try saving a real PDF file (from file system)
- [ ] Verify PDF can be retrieved
- [ ] Test with large PDF files (near 10 MB limit)
- [ ] Test with corrupted PDF files (should handle gracefully)

## Next Steps

Once tests pass:
1. ✅ Database layer is verified
2. ✅ DAO layer is verified
3. ✅ PDF utilities are verified
4. → Proceed to UI implementation (Sprint 2)

