# Tilitin v2.1.1 - Comprehensive Testing Guide

**Date:** 2026-01-02  
**Version:** 2.1.1  
**Purpose:** Manual testing checklist and guide

---

## 🚀 Getting Started

### Prerequisites
- Java 21+ installed
- Project built successfully (`./gradlew build`)
- Test data files available (in `test-data/` directory)

### Launch Application
```bash
cd C:\Github\tilitin-v2
./gradlew run
```

**Expected:** Application window opens without errors. Main window should show:
- Menu bar (Tiedosto, Muokkaa, Tulosteet, Työkalut, Ohje)
- Toolbar with buttons
- Empty document area (until database is opened)

---

## 📋 Test Scenarios

### 1. Application Startup ✅

#### Test 1.1: Launch Application
**Steps:**
1. Run `./gradlew run`
2. Wait for application window to appear

**Expected Results:**
- ✅ Application window opens
- ✅ No console errors
- ✅ Menu bar visible
- ✅ Toolbar visible
- ✅ Status bar visible (bottom)
- ✅ Window title: "Tilitin"

**Screenshot locations to check:**
- Main window layout
- Menu items are visible
- No error dialogs

---

### 2. Database Operations

#### Test 2.1: Create New Database
**Steps:**
1. Click **Tiedosto** → **Uusi tietokanta...** (or Ctrl+N)
2. Choose save location (e.g., `C:\Temp\test-tilitin.db`)
3. Click Save
4. If prompted for initialization, select a chart of accounts template
5. Click OK/Initialize

**Expected Results:**
- ✅ File chooser dialog appears
- ✅ Database file is created
- ✅ Application shows "Tietokanta avattu" or similar status
- ✅ Document area becomes active
- ✅ Period indicator shows current period

**What to verify:**
- Database file exists on disk
- No error messages
- UI is responsive

#### Test 2.2: Open Existing Database
**Steps:**
1. Click **Tiedosto** → **Avaa tietokanta...** (or Ctrl+O)
2. Navigate to an existing `.db` file
3. Select and click Open

**Expected Results:**
- ✅ File chooser dialog appears
- ✅ Database opens successfully
- ✅ Documents load (if any exist)
- ✅ Status shows database name

**Test with:**
- Empty database
- Database with existing documents
- Invalid/corrupted database (should show error)

#### Test 2.3: Database Properties
**Steps:**
1. With database open, click **Tiedosto** → **Perustiedot...**
2. View database information

**Expected Results:**
- ✅ PropertiesDialogFX opens
- ✅ Shows database name, path, period info
- ✅ Can close dialog

---

### 3. Document Operations

#### Test 3.1: Create New Document
**Steps:**
1. With database open, click **Tiedosto** → **Uusi tosite** (or Ctrl+T)
2. Or click "Uusi tosite" button in toolbar

**Expected Results:**
- ✅ New empty document appears
- ✅ Document number is auto-assigned
- ✅ Date defaults to today
- ✅ Entry table is empty
- ✅ Totals show 0.00 / 0.00

**What to verify:**
- Document number increments correctly
- Date picker works
- Document type can be selected

#### Test 3.2: Add Entry to Document
**Steps:**
1. Create new document (Test 3.1)
2. Click in first row, "Tili" column
3. Type account number or click to open account selection
4. Fill in:
   - Description
   - Debit amount (or Credit amount)
5. Press Tab to move to next field

**Expected Results:**
- ✅ AccountSelectionDialogFX opens when clicking account field
- ✅ Can search/filter accounts
- ✅ Account auto-complete works
- ✅ Description field accepts text
- ✅ Amount fields accept numbers
- ✅ Tab navigation works between fields

**Test scenarios:**
- Add debit entry
- Add credit entry
- Add entry with VAT
- Add multiple entries

#### Test 3.3: Entry Table UX Features
**Steps:**
1. Create document with entries
2. Test keyboard navigation:
   - Tab: Move to next field
   - Shift+Tab: Move to previous field
   - Enter: Move to next row
   - Ctrl+Enter: Add new entry row
3. Test asterisk (*) toggle:
   - In debit field, type `*` → should toggle to credit
   - In credit field, type `*` → should toggle to debit

**Expected Results:**
- ✅ Tab navigation works smoothly
- ✅ Enter moves to next row
- ✅ Ctrl+Enter adds new row
- ✅ Asterisk (*) toggles debit/credit
- ✅ Description auto-complete suggests previous descriptions

#### Test 3.4: Save Document
**Steps:**
1. Create document with entries
2. Click **Tiedosto** → **Tallenna** (or Ctrl+S)
3. Or click "Tallenna" button

**Expected Results:**
- ✅ Document saves without errors
- ✅ Status shows "Tallennettu" or similar
- ✅ Document persists after closing/reopening database

**Test scenarios:**
- Save valid balanced document
- Try to save unbalanced document (should show error)
- Save document with attachments

#### Test 3.5: Navigate Between Documents
**Steps:**
1. Create multiple documents
2. Use navigation:
   - **Edellinen tosite** (Previous) button
   - **Seuraava tosite** (Next) button
   - **Ensimmäinen tosite** (First) button
   - **Viimeinen tosite** (Last) button
3. Or use **Tiedosto** → **Siirry tositteeseen...** (Ctrl+G)

**Expected Results:**
- ✅ Navigation buttons work correctly
- ✅ Document number updates
- ✅ Document content loads correctly
- ✅ Go to dialog accepts document number

---

### 4. Dialog Testing

#### Test 4.1: Settings Dialog
**Steps:**
1. Click **Tiedosto** → **Asetukset...**
2. Explore all tabs:
   - General settings
   - Appearance
   - Keyboard shortcuts
   - Print settings

**Expected Results:**
- ✅ SettingsDialogFX opens
- ✅ All tabs are accessible
- ✅ Settings can be changed
- ✅ Changes persist after closing/reopening
- ✅ OK/Cancel buttons work

**What to test:**
- Change theme (Light/Dark)
- Modify keyboard shortcuts
- Change print settings
- Export/import settings

#### Test 4.2: Appearance Dialog
**Steps:**
1. Click **Tiedosto** → **Ulkoasu...**
2. Or open from Settings → Appearance tab

**Expected Results:**
- ✅ AppearanceDialogFX opens
- ✅ Theme selection works (Light/Dark)
- ✅ Font size can be adjusted
- ✅ Preview updates in real-time
- ✅ Changes apply immediately

#### Test 4.3: Chart of Accounts (COA) Dialog
**Steps:**
1. Click **Muokkaa** → **Tilikartta...** (or Ctrl+K)
2. Explore the dialog:
   - View accounts
   - Add new account
   - Edit account
   - Delete account

**Expected Results:**
- ✅ COADialogFX opens
- ✅ Account list displays correctly
- ✅ Can add/edit/delete accounts
- ✅ Account hierarchy is visible
- ✅ Changes save correctly

#### Test 4.4: Document Type Dialog
**Steps:**
1. Click **Muokkaa** → **Tositelajit...**
2. Test:
   - View document types
   - Add new document type
   - Edit document type
   - Delete document type

**Expected Results:**
- ✅ DocumentTypeDialogFX opens
- ✅ Document types list correctly
- ✅ Can modify document types
- ✅ Number ranges work correctly

#### Test 4.5: Entry Template Dialog
**Steps:**
1. Click **Muokkaa** → **Vientipohjat...**
2. Test:
   - View templates
   - Create new template
   - Edit template
   - Delete template

**Expected Results:**
- ✅ EntryTemplateDialogFX opens
- ✅ Templates list correctly
- ✅ Can create/edit templates
- ✅ Templates can be used when creating documents

#### Test 4.6: Report Editor Dialog ⭐ IMPORTANT
**Steps:**
1. Click **Tulosteet** → **Muokkaa tulosteita...**
2. Explore the dialog:
   - View report templates
   - Edit header/footer
   - Edit report content
   - Export/import templates

**Expected Results:**
- ✅ **ReportEditorDialogFX opens** (verify it exists!)
- ✅ All tabs accessible
- ✅ Can edit report templates
- ✅ Save works correctly
- ✅ Export/import works

**This is critical** - The audit found this dialog exists, verify it works!

#### Test 4.7: Account Summary Options Dialog
**Steps:**
1. Click **Tulosteet** → **Tilien saldot...**
2. Configure options:
   - Date range
   - Accounts to include
   - Previous period visibility
3. Click OK

**Expected Results:**
- ✅ AccountSummaryOptionsDialogFX opens
- ✅ Options can be configured
- ✅ Report generates correctly
- ✅ Print preview appears

#### Test 4.8: Financial Statement Options Dialog
**Steps:**
1. Click **Tulosteet** → **Tuloslaskelma...** or **Tase...**
2. Configure:
   - Period selection
   - Date ranges
   - Page break options
3. Click OK

**Expected Results:**
- ✅ FinancialStatementOptionsDialogFX opens
- ✅ Can select multiple periods
- ✅ Options save correctly
- ✅ Report generates

#### Test 4.9: VAT Report Dialog
**Steps:**
1. Click **Tulosteet** → **ALV-laskelma...**
2. Configure VAT report options
3. Click OK

**Expected Results:**
- ✅ VATReportDialogFX opens
- ✅ Options configurable
- ✅ Report generates correctly

#### Test 4.10: CSV Import Dialog
**Steps:**
1. Click **Työkalut** → **Tuo CSV-tiedostosta...** (or Ctrl+I)
2. Select a test file from `test-data/`:
   - `nordea-tiliote.csv`
   - `op-tiliote.csv`
   - `procountor-tiliote.csv`
3. Configure column mapping
4. Click "Tuo kirjaukset"

**Expected Results:**
- ✅ CSVImportDialog opens (Kotlin dialog)
- ✅ File chooser works
- ✅ CSV preview shows correctly
- ✅ Column mapping works
- ✅ Import creates documents correctly

**Test files available:**
- `test-data/nordea-tiliote.csv`
- `test-data/op-tiliote.csv`
- `test-data/procountor-tiliote.csv`

---

### 5. Report Generation

#### Test 5.1: Account Summary Report
**Steps:**
1. Open database with data
2. Click **Tulosteet** → **Tilien saldot...**
3. Configure options
4. Click OK
5. Review print preview

**Expected Results:**
- ✅ Report generates without errors
- ✅ Print preview window opens (Swing PrintPreviewFrame)
- ✅ Report content is correct
- ✅ Can print or export

#### Test 5.2: Income Statement
**Steps:**
1. Click **Tulosteet** → **Tuloslaskelma...**
2. Configure periods
3. Generate report

**Expected Results:**
- ✅ ReportDialog.kt opens (Kotlin)
- ✅ Report displays in WebView
- ✅ Can switch between periods
- ✅ Can export to PDF/HTML
- ✅ Can print

#### Test 5.3: Balance Sheet
**Steps:**
1. Click **Tulosteet** → **Tase...**
2. Generate report

**Expected Results:**
- ✅ ReportDialog.kt opens
- ✅ Balance sheet displays correctly
- ✅ Assets = Liabilities + Equity (balanced)

#### Test 5.4: Journal Report
**Steps:**
1. Click **Tulosteet** → **Päiväkirja...**
2. Generate report

**Expected Results:**
- ✅ ReportDialog.kt opens
- ✅ All entries listed chronologically
- ✅ Can click account numbers to open ledger

#### Test 5.5: Ledger Report
**Steps:**
1. Click **Tulosteet** → **Pääkirja...**
2. Generate report

**Expected Results:**
- ✅ ReportDialog.kt opens
- ✅ Accounts listed with entries
- ✅ Can click account to drill down

---

### 6. Data Management

#### Test 6.1: CSV Import
**Steps:**
1. Use test file from `test-data/`
2. Import CSV (see Test 4.10)
3. Verify imported documents

**Expected Results:**
- ✅ Documents created correctly
- ✅ Entries match CSV data
- ✅ Dates parsed correctly
- ✅ Amounts correct

#### Test 6.2: Data Export
**Steps:**
1. Click **Työkalut** → **Vie tiedot...**
2. Choose export format
3. Select destination
4. Export

**Expected Results:**
- ✅ DataExportDialogFX opens
- ✅ Export completes successfully
- ✅ Exported file is valid

#### Test 6.3: Backup Settings
**Steps:**
1. Click **Tiedosto** → **Varmuuskopiointiasetukset...**
2. Configure backup settings
3. Save

**Expected Results:**
- ✅ BackupSettingsDialogFX opens
- ✅ Settings can be configured
- ✅ Auto-backup can be enabled

#### Test 6.4: Restore Backup
**Steps:**
1. Click **Tiedosto** → **Palauta varmuuskopiosta...**
2. Select backup file
3. Restore

**Expected Results:**
- ✅ RestoreBackupDialogFX opens
- ✅ Can select backup file
- ✅ Restore works correctly
- ✅ Database restored successfully

---

### 7. Advanced Features

#### Test 7.1: Attachments
**Steps:**
1. Open a document
2. Click attachment button or **Tiedosto** → **Liitteet...**
3. Add PDF attachment
4. View attachment

**Expected Results:**
- ✅ AttachmentsDialogFX opens
- ✅ Can add PDF files
- ✅ Attachments list correctly
- ✅ Can view/open attachments

#### Test 7.2: Document Number Shift
**Steps:**
1. Click **Työkalut** → **Siirrä tositenumeroita...**
2. Configure shift
3. Execute

**Expected Results:**
- ✅ DocumentNumberShiftDialogFX opens
- ✅ Can shift document numbers
- ✅ Changes apply correctly

#### Test 7.3: VAT Change
**Steps:**
1. Click **Työkalut** → **Muuta ALV-prosentteja...**
2. Configure VAT changes
3. Apply

**Expected Results:**
- ✅ VATChangeDialogFX opens
- ✅ Can change VAT rates
- ✅ Changes apply to entries

#### Test 7.4: Balance Comparison
**Steps:**
1. Click **Työkalut** → **Tilien saldovertailu...**
2. Generate comparison

**Expected Results:**
- ✅ BalanceComparisonDialogFX opens
- ✅ Comparison report generates
- ✅ Shows period differences

---

### 8. Keyboard Shortcuts

#### Test 8.1: Common Shortcuts
Test these keyboard shortcuts:

| Shortcut | Action | Expected |
|----------|--------|----------|
| Ctrl+N | New database | ✅ Opens file chooser |
| Ctrl+O | Open database | ✅ Opens file chooser |
| Ctrl+S | Save document | ✅ Saves current document |
| Ctrl+T | New document | ✅ Creates new document |
| Ctrl+K | Chart of accounts | ✅ Opens COADialogFX |
| Ctrl+I | CSV import | ✅ Opens CSVImportDialog |
| Ctrl+G | Go to document | ✅ Opens go-to dialog |
| F1 | Help | ✅ Opens HelpDialogFX |

**Expected Results:**
- ✅ All shortcuts work
- ✅ No conflicts
- ✅ Shortcuts match menu items

---

### 9. Error Handling

#### Test 9.1: Invalid Operations
Test error handling:

1. **Try to save unbalanced document**
   - Expected: Error dialog appears
   - Expected: Document not saved

2. **Try to delete account in use**
   - Expected: Error message
   - Expected: Account not deleted

3. **Try to open invalid database**
   - Expected: Error dialog
   - Expected: Application doesn't crash

4. **Try to import invalid CSV**
   - Expected: Error message
   - Expected: Import fails gracefully

**Expected Results:**
- ✅ Error messages are clear
- ✅ Application doesn't crash
- ✅ User can recover from errors

---

### 10. UI/UX Testing

#### Test 10.1: Theme Switching
**Steps:**
1. Switch between Light and Dark theme
2. Verify all dialogs respect theme
3. Check contrast and readability

**Expected Results:**
- ✅ Theme applies immediately
- ✅ All dialogs use correct theme
- ✅ Text is readable in both themes

#### Test 10.2: Window Resizing
**Steps:**
1. Resize main window
2. Resize dialogs
3. Minimize/maximize

**Expected Results:**
- ✅ Window resizes correctly
- ✅ Content adapts to size
- ✅ No layout issues

#### Test 10.3: Multiple Dialogs
**Steps:**
1. Open multiple dialogs
2. Switch between them
3. Close dialogs

**Expected Results:**
- ✅ Dialogs can be opened simultaneously
- ✅ Focus handling works
- ✅ Closing works correctly

---

## 🐛 Bug Reporting Template

If you find bugs, document them using this template:

```markdown
### Bug: [Short Description]

**Severity:** Critical / High / Medium / Low

**Steps to Reproduce:**
1. Step 1
2. Step 2
3. Step 3

**Expected Behavior:**
What should happen

**Actual Behavior:**
What actually happens

**Screenshot:**
[If applicable]

**Environment:**
- OS: Windows 10/11 / macOS / Linux
- Java Version: [e.g., 21.0.1]
- Application Version: 2.1.1
```

---

## ✅ Testing Checklist Summary

### Critical Tests (Must Pass)
- [ ] Application launches
- [ ] Create/open database
- [ ] Create/save document
- [ ] Add entries
- [ ] ReportEditorDialogFX works (verify it exists!)
- [ ] Generate at least one report
- [ ] CSV import works

### Important Tests (Should Pass)
- [ ] All major dialogs open
- [ ] Entry table UX features
- [ ] Keyboard shortcuts
- [ ] Theme switching
- [ ] Data export/import

### Nice-to-Have Tests
- [ ] All dialogs tested
- [ ] All reports generated
- [ ] Error handling
- [ ] Edge cases

---

## 📊 Test Results Tracking

Use this section to track your testing progress:

**Date:** _____________  
**Tester:** _____________  
**Build:** 2.1.1

### Results:
- [ ] Test 1: Application Startup
- [ ] Test 2: Database Operations
- [ ] Test 3: Document Operations
- [ ] Test 4: Dialog Testing
- [ ] Test 5: Report Generation
- [ ] Test 6: Data Management
- [ ] Test 7: Advanced Features
- [ ] Test 8: Keyboard Shortcuts
- [ ] Test 9: Error Handling
- [ ] Test 10: UI/UX Testing

### Bugs Found:
[List any bugs found during testing]

### Notes:
[Any additional observations]

---

## 🎯 Quick Test (5 minutes)

If you only have 5 minutes, test these critical paths:

1. ✅ Launch application (`./gradlew run`)
2. ✅ Create new database
3. ✅ Create new document
4. ✅ Add one entry
5. ✅ Save document
6. ✅ Open ReportEditorDialogFX (verify it exists!)
7. ✅ Generate one report

If all these pass, the application is functional!

---

**Good luck with testing!** 🚀
