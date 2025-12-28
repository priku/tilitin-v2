# Quick Test Guide - PDF Attachments Feature

## Build Status: ✅ Ready to Test

The application has been successfully compiled and packaged.

## How to Run

### Option 1: Using Maven (Easiest)

```bash
mvn exec:java
```

This will automatically:
- Compile the project
- Run `kirjanpito.ui.Kirjanpito` main class
- Include all dependencies in classpath

### Option 2: Using Java directly

```bash
# Windows PowerShell
java -cp "target/tilitin-2.1.6.jar;target/lib/*" kirjanpito.ui.Kirjanpito

# Linux/Mac
java -cp "target/tilitin-2.1.6.jar:target/lib/*" kirjanpito.ui.Kirjanpito
```

### Option 3: From IDE

1. Open `src/main/java/kirjanpito/ui/Kirjanpito.java`
2. Right-click on `main()` method
3. Select "Run 'Kirjanpito.main()'"

## Quick Test Checklist

### ✅ Basic Functionality

1. **Start Application**
   - [ ] Application starts without errors
   - [ ] DocumentFrame opens

2. **Open/Create Database**
   - [ ] File → New Database (or Open Database)
   - [ ] Create a test database if needed

3. **Create a Document**
   - [ ] Create at least one entry
   - [ ] Document must be saved (have entries)

4. **Find Attachments Panel**
   - [ ] Scroll down in DocumentFrame
   - [ ] See "PDF-liitteet" section at bottom
   - [ ] Panel shows empty list initially

5. **Add PDF Attachment**
   - [ ] Click "Lisää PDF" button
   - [ ] File chooser opens
   - [ ] Select a PDF file
   - [ ] PDF is validated
   - [ ] Success message appears
   - [ ] PDF appears in list

6. **View Attachment Info**
   - [ ] Filename displayed
   - [ ] File size displayed (KB/MB)
   - [ ] Page count displayed (or "-")
   - [ ] Created date displayed
   - [ ] Description column (may be empty)

7. **Export PDF**
   - [ ] Select attachment from list
   - [ ] Click "Vie tiedostoksi"
   - [ ] Save dialog opens
   - [ ] File is saved
   - [ ] Verify saved file opens correctly

8. **Remove Attachment**
   - [ ] Select attachment
   - [ ] Click "Poista"
   - [ ] Confirm deletion
   - [ ] Attachment removed from list

9. **Document Navigation**
   - [ ] Navigate to different document
   - [ ] Attachments panel updates
   - [ ] Shows attachments for current document

### ⚠️ Error Cases

- [ ] Try adding attachment to unsaved document → Warning shown
- [ ] Try adding non-PDF file → Error shown
- [ ] Try adding very large PDF (>10MB) → Error shown
- [ ] Try adding large PDF (5-10MB) → Warning shown

## What to Look For

### ✅ Should Work
- PDF attachments can be added
- List displays correctly
- Export works
- Remove works
- Panel updates when document changes

### ❌ Not Yet Implemented (Sprint 3)
- PDF viewer (view PDF content)
- Zoom controls
- Page navigation
- Drag & drop
- Clipboard paste

## Troubleshooting

**Application won't start:**
- Check Java version: `java -version` (should be 25+)
- Check Maven: `mvn -version`
- Check console for errors

**Attachments panel not visible:**
- Scroll down in DocumentFrame
- Check that document is open
- Check console for errors

**Can't add attachment:**
- Make sure document is saved (has entries)
- Check PDF file is valid
- Check file size (< 10 MB)
- Check console for errors

**Database errors:**
- Check database version is 15
- Check console for SQL errors
- Try creating new database

## Success Criteria

✅ Application starts
✅ Attachments panel visible
✅ Can add PDF attachments
✅ Can view attachment list
✅ Can export PDFs
✅ Can remove attachments
✅ Panel updates with document changes

---

**Ready to test!** Run the application and follow the checklist above.

