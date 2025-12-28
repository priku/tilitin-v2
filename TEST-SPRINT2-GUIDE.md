# Sprint 2 Testing Guide - PDF Attachments UI

## Quick Test Steps

### 1. Build the Application

```bash
mvn clean package -DskipTests
```

### 2. Run the Application

```bash
# Windows
java -cp "target/tilitin-2.1.6.jar;target/lib/*" kirjanpito.ui.Kirjanpito

# Linux/Mac
java -cp "target/tilitin-2.1.6.jar:target/lib/*" kirjanpito.ui.Kirjanpito
```

Or run from your IDE by executing `kirjanpito.ui.Kirjanpito.main()`.

### 3. Test PDF Attachments Feature

#### Test Scenario 1: Add PDF Attachment

1. **Open or create a database**
   - File → New Database (or Open Database)
   - Create a test database if needed

2. **Create or open a document**
   - Make sure you have at least one document open
   - The document must be saved (ID > 0) to attach files

3. **Scroll down to PDF-liitteet section**
   - The attachments panel should be visible at the bottom of DocumentFrame
   - It should show "PDF-liitteet" as a titled border

4. **Click "Lisää PDF" button**
   - File chooser should open
   - Only PDF files should be visible/selectable

5. **Select a PDF file**
   - Choose any PDF file from your system
   - Click Open

6. **Verify**
   - If file is > 5MB, you should see a warning dialog
   - If file is > 10MB, you should see an error
   - PDF should be validated before saving
   - Success message should appear
   - PDF should appear in the list with:
     - Filename
     - File size (formatted)
     - Page count (if calculated)
     - Created date
     - Description (if provided)

#### Test Scenario 2: View Attachments List

1. **Check the table displays correctly**
   - Filename column
   - Size column (KB/MB format)
   - Pages column (number or "-")
   - Created date column
   - Description column

2. **Verify multiple attachments**
   - Add 2-3 PDF files
   - All should appear in the list
   - List should be sorted by creation date

#### Test Scenario 3: Export PDF

1. **Select an attachment** from the list
   - Click on a row to select it

2. **Click "Vie tiedostoksi" button**
   - Should be enabled when attachment is selected
   - Save dialog should open
   - Default filename should be the attachment filename

3. **Save the file**
   - Choose location
   - Click Save
   - If file exists, confirm overwrite

4. **Verify**
   - File should be saved to selected location
   - Open the file to verify it's a valid PDF
   - Content should match original

#### Test Scenario 4: Remove Attachment

1. **Select an attachment** from the list

2. **Click "Poista" button**
   - Should be enabled when attachment is selected
   - Confirmation dialog should appear

3. **Confirm deletion**
   - Click Yes
   - Attachment should be removed from list
   - Success message should appear

4. **Verify**
   - Attachment should disappear from list
   - Database should be updated (attachment deleted)

#### Test Scenario 5: Document Navigation

1. **Navigate between documents**
   - Use Previous/Next buttons
   - Or use document number field

2. **Verify attachments panel updates**
   - Panel should show attachments for current document
   - If document has no attachments, list should be empty
   - If document has attachments, they should appear

#### Test Scenario 6: New Document

1. **Create a new document**
   - Click "New Document" button
   - New document is created (ID = 0 or negative)

2. **Try to add attachment**
   - Click "Lisää PDF"
   - Should show warning: "Valitse ensin tosite."
   - Attachment should not be added

3. **Save the document**
   - Fill in entries
   - Document gets a real ID

4. **Add attachment again**
   - Should work now

### 4. Error Cases to Test

#### Large File Warning
- Add a PDF file between 5-10 MB
- Should show warning dialog
- Should allow continuing or canceling

#### Invalid PDF
- Try to add a non-PDF file (rename .txt to .pdf)
- Should show error: "Valittu tiedosto ei ole kelvollinen PDF-tiedosto."

#### File Too Large
- Try to add a PDF > 10 MB
- Should show error with size limit message

#### Database Errors
- Close database connection (if possible)
- Try to add/remove attachment
- Should show appropriate error message

### 5. UI/UX Checks

- [ ] Attachments panel is visible at bottom of DocumentFrame
- [ ] Panel has titled border "PDF-liitteet"
- [ ] Buttons are properly enabled/disabled based on selection
- [ ] Table columns are properly sized
- [ ] File sizes are formatted correctly (KB/MB)
- [ ] Dates are formatted correctly
- [ ] Messages are in Finnish (or appropriate language)
- [ ] Error messages are clear and helpful
- [ ] Success messages appear after operations

### 6. Expected Behavior

✅ **Working:**
- Add PDF attachments to saved documents
- View list of attachments
- Export attachments to file system
- Remove attachments with confirmation
- Panel updates when document changes
- File size validation
- PDF validation

❌ **Not Yet Implemented (Sprint 3):**
- PDF viewer (view PDF content)
- Zoom controls
- Page navigation
- PDF cache
- Drag & drop
- Clipboard paste

### 7. Troubleshooting

**Issue: Attachments panel not visible**
- Check that document is open
- Scroll down in DocumentFrame
- Check console for errors

**Issue: "Valitse ensin tosite" when adding**
- Document must be saved first (have ID > 0)
- Create entries and save document

**Issue: PDF validation fails**
- Make sure file is a valid PDF
- Try with a known-good PDF file
- Check console for detailed error

**Issue: Database errors**
- Check database is open
- Verify database version is 15 (migration completed)
- Check console for SQL errors

### 8. Test Data

Create test PDFs if needed:
- Small PDF (< 1 MB) - should work without warnings
- Medium PDF (2-5 MB) - should work without warnings
- Large PDF (6-9 MB) - should show warning
- Very large PDF (> 10 MB) - should be rejected

You can create test PDFs using any PDF generator or use existing PDFs.

---

## Success Criteria

✅ All test scenarios pass
✅ No compilation errors
✅ No runtime exceptions
✅ UI is responsive
✅ Error messages are clear
✅ Success messages appear
✅ Database operations work correctly

---

**Next:** Once Sprint 2 is verified working, proceed to Sprint 3 (PDF Viewer).

