# JavaFX vs Swing UI/UX Feature Comparison

**Last Updated:** 2026-01-01
**JavaFX Overall Completion:** ~50-60%

This document provides a comprehensive comparison between the legacy Swing UI and the new JavaFX UI implementation of Tilitin.

---

## Summary Status

| Component | Implemented | Missing | Completion % |
|-----------|-------------|---------|--------------|
| **Dialogs** | 9 | 26 | 26% |
| **Menu Handlers** | 26 | 17 | 60% |
| **Entry Table UX** | Basic features | Advanced navigation | ~40% |
| **Keyboard Shortcuts** | Basic | 15+ advanced | ~50% |
| **Context Menus** | None | All | 0% |
| **Dynamic Menus** | Partial | Entry templates, doc types | ~30% |

---

## 1. Dialog Comparison

### ✅ Implemented JavaFX Dialogs (9/35)

1. **[AccountSelectionDialogFX.kt](src/main/kotlin/kirjanpito/ui/javafx/AccountSelectionDialogFX.kt)** - Account selection
2. **[DocumentTypeSelectionDialogFX.kt](src/main/kotlin/kirjanpito/ui/javafx/DocumentTypeSelectionDialogFX.kt)** - Document type selection
3. **[SettingsDialogFX.kt](src/main/kotlin/kirjanpito/ui/javafx/SettingsDialogFX.kt)** - Application settings
4. **[AccountEditorDialogFX.kt](src/main/kotlin/kirjanpito/ui/javafx/AccountEditorDialogFX.kt)** - Chart of accounts editor
5. **[DocumentTypeEditorDialogFX.kt](src/main/kotlin/kirjanpito/ui/javafx/DocumentTypeEditorDialogFX.kt)** - Document type editor
6. **[PeriodEditorDialogFX.kt](src/main/kotlin/kirjanpito/ui/javafx/PeriodEditorDialogFX.kt)** - Accounting period editor
7. **[EntryTemplateDialogFX.kt](src/main/kotlin/kirjanpito/ui/javafx/EntryTemplateDialogFX.kt)** - Entry template management
8. **[ReportEditorDialogFX.kt](src/main/kotlin/kirjanpito/ui/javafx/ReportEditorDialogFX.kt)** - Report structure editor
9. **[StartingBalanceDialogFX.kt](src/main/kotlin/kirjanpito/ui/javafx/StartingBalanceDialogFX.kt)** - Starting balances

### ❌ Missing JavaFX Dialogs (26/35)

#### High Priority - Data Management
- **BackupSettingsDialog** - Configure automatic backups
- **RestoreBackupDialog** - Restore from backup
- **PrintPreviewDialog** - Print document preview
- **PrintOptionsDialog** - Print settings

#### High Priority - Reports
- **AccountSummaryOptionsDialog** - Account summary report options
- **AccountStatementOptionsDialog** - Account statement options
- **VATReportDialog** - VAT/ALV report generation
- **BalanceComparisonDialog** - Balance comparison report
- **GeneralJournalOptionsDialog** - General journal options
- **ReportViewerDialog** - Generic report viewer

#### Medium Priority - Document Operations
- **SearchDialog** - Find entries/documents
- **FilterDialog** - Advanced filtering
- **DocumentPropertiesDialog** - Edit document metadata
- **EntryEditorDialog** - Standalone entry editor (may not be needed)

#### Medium Priority - Settings & Configuration
- **VATAccountSelectionDialog** - VAT account mapping
- **COAImportDialog** - Chart of accounts import
- **DataExportDialog** - Export data to various formats
- **DataImportDialog** - Import data from various formats

#### Lower Priority - Advanced Features
- **CalculatorDialog** - Built-in calculator
- **AboutDialog** - About Tilitin
- **HelpDialog** - Help documentation viewer
- **ShortcutHelpDialog** - Keyboard shortcut reference
- **ColorSchemeDialog** - UI theme settings
- **FontSettingsDialog** - Font customization
- **ErrorLogDialog** - View error log
- **DatabaseInfoDialog** - Database statistics

---

## 2. Menu Handler Comparison

### ✅ Implemented Handlers in MainController.java (26/43)

#### File Menu (8 handlers)
- `handleNewFile()` - Create new accounting file
- `handleOpen()` - Open existing file
- `handleClose()` - Close current file
- `handleSave()` - Save document
- `handleSaveAs()` - Save with new name
- `handlePrint()` - Print document
- `handleQuit()` - Exit application
- `handleSettings()` - Application settings

#### Edit Menu (5 handlers)
- `handleUndo()` - Undo last change
- `handleRedo()` - Redo change
- `handleCut()` - Cut selection
- `handleCopy()` - Copy selection
- `handlePaste()` - Paste from clipboard

#### Document Menu (6 handlers)
- `handleNewDocument()` - Create new document
- `handleDeleteDocument()` - Delete current document
- `handleNextDocument()` - Navigate to next
- `handlePreviousDocument()` - Navigate to previous
- `handleFirstDocument()` - Jump to first
- `handleLastDocument()` - Jump to last

#### Data Management (7 handlers)
- `handleAccounts()` - Chart of accounts editor
- `handleDocumentTypes()` - Document type editor
- `handlePeriods()` - Accounting period editor
- `handleEntryTemplates()` - Entry template editor
- `handleReportStructure()` - Report structure editor
- `handleStartingBalances()` - Starting balances
- `handleBackupSettings()` - Backup configuration

### ⏳ Placeholder/Missing Handlers (17/43)

#### Reports (6 handlers - all placeholder)
- `handleGeneralJournal()` - General journal report
- `handleAccountStatement()` - Account statement
- `handleAccountSummary()` - Account summary
- `handleVATReport()` - VAT report
- `handleBalanceComparison()` - Balance comparison
- `handleCustomReport()` - Custom report viewer

#### Tools (5 handlers - all placeholder)
- `handleSearch()` - Search entries/documents
- `handleFilter()` - Advanced filtering
- `handleCalculator()` - Built-in calculator
- `handleExportData()` - Export to CSV/Excel
- `handleImportData()` - Import from external files

#### View (3 handlers - partial)
- `handleToggleEntryTable()` - Show/hide entry table (placeholder)
- `handleToggleStatusBar()` - Show/hide status bar (placeholder)
- `handleZoomIn()` - Increase font size (placeholder)

#### Help (3 handlers - all placeholder)
- `handleHelp()` - Help documentation
- `handleKeyboardShortcuts()` - Shortcut reference
- `handleAbout()` - About dialog

---

## 3. Entry Table UX Comparison

### ✅ Basic Features Implemented (~40%)

**Working in JavaFX:**
- Basic cell editing (account, description, amount, debet/credit)
- Add/remove entry rows
- Basic keyboard navigation (arrow keys, Enter)
- Tab key moves to next cell
- Delete key clears cell content
- Account selection dropdown
- Amount formatting (Finnish locale, 2 decimals)
- Debet/Credit toggle column

### ❌ Missing Advanced Features (~60%)

#### Smart Navigation (from [EntryTableActions.java:1-333](src/main/java/kirjanpito/ui/swing/EntryTableActions.java#L1-L333))

**Missing Tab Logic:**
The Swing version has sophisticated Tab key behavior (333 lines in `EntryTableActions.java`):
- Tab in account cell → moves to debet/credit toggle cell
- Tab in toggle cell → moves to amount cell
- Tab in amount cell → moves to description cell
- Tab in description cell → moves to account cell of **next row**
- Tab in last row → **creates new row automatically**
- Shift+Tab reverses the direction

**Current JavaFX behavior:**
- Tab just moves to next cell (default TableView behavior)
- No automatic row creation
- No smart column skipping

#### Debet/Credit Toggle

**Swing implementation:**
- Asterisk (*) key toggles debet/credit for current entry
- Visual feedback with checkbox/toggle in dedicated column
- Works from any cell in the row

**JavaFX status:**
- Toggle column exists visually
- Asterisk key handler **not implemented**
- Must click checkbox manually

#### Auto-complete for Descriptions

**Swing implementation** ([DescriptionCellEditor.java](src/main/java/kirjanpito/ui/swing/DescriptionCellEditor.java)):
- Suggests previously used descriptions as you type
- Ctrl+Space opens completion popup
- Learns from historical entries in database
- Fuzzy matching for partial inputs

**JavaFX status:**
- Plain text field
- No auto-complete
- No suggestions

#### Suffix Removal

**Swing:**
- Ctrl+Backspace removes suffix from description (e.g., "Invoice #123" → "Invoice")

**JavaFX:**
- Not implemented

#### Context Menu (Right-click)

**Swing:**
- Insert entry above
- Insert entry below
- Delete entry
- Copy entry
- Paste entry
- Clear cell
- Toggle debet/credit

**JavaFX:**
- No context menu

#### Drag & Drop

**Swing:**
- Drag entries to reorder rows
- Visual feedback during drag

**JavaFX:**
- Not implemented

---

## 4. Keyboard Shortcut Comparison

### ✅ Implemented Shortcuts (~50%)

| Shortcut | Action | Status |
|----------|--------|--------|
| Ctrl+N | New document | ✅ Working |
| Ctrl+O | Open file | ✅ Working |
| Ctrl+S | Save | ✅ Working |
| Ctrl+P | Print | ✅ Working |
| Ctrl+Q | Quit | ✅ Working |
| Ctrl+Z | Undo | ✅ Working |
| Ctrl+Y | Redo | ✅ Working |
| Ctrl+X | Cut | ✅ Working |
| Ctrl+C | Copy | ✅ Working |
| Ctrl+V | Paste | ✅ Working |
| Delete | Delete document | ✅ Working |
| PgUp | Previous document | ✅ Working |
| PgDn | Next document | ✅ Working |
| Home | First document | ✅ Working |
| End | Last document | ✅ Working |

### ❌ Missing Shortcuts (~50%)

| Shortcut | Action | File Reference |
|----------|--------|----------------|
| **F8** | Add new entry row | EntryTableActions.java |
| **Asterisk (*)** | Toggle debet/credit | EntryTableActions.java |
| **Ctrl+Backspace** | Remove suffix from description | DescriptionCellEditor.java |
| **Ctrl+Space** | Open description auto-complete | DescriptionCellEditor.java |
| **Ctrl+M** | Edit entry templates | DocumentFrame.java |
| **Ctrl+F** | Search/Find | DocumentFrame.java |
| **Ctrl+G** | General journal report | DocumentFrame.java |
| **Ctrl+R** | Account statement | DocumentFrame.java |
| **Ctrl+L** | Account summary | DocumentFrame.java |
| **Ctrl+E** | Export data | DocumentFrame.java |
| **Ctrl+I** | Import data | DocumentFrame.java |
| **Alt+Left** | Navigate back | DocumentFrame.java |
| **Alt+Right** | Navigate forward | DocumentFrame.java |
| **F1** | Help | DocumentFrame.java |
| **F5** | Refresh | DocumentFrame.java |

---

## 5. Dynamic Menu Features

### ✅ Partial Implementation (~30%)

**Working:**
- Static menu structure loads correctly
- Basic menu items invoke handlers
- Enable/disable states update based on document state

### ❌ Missing Dynamic Behavior (~70%)

#### Entry Template Menu Population

**Swing behavior** ([DocumentFrame.java:2000-2100](src/main/java/kirjanpito/ui/swing/DocumentFrame.java#L2000-L2100)):
- "Data → Entry Templates" menu dynamically populated
- Shows list of all entry templates from database
- Click template → inserts pre-filled entries into current document
- Updates when templates are added/removed

**JavaFX status:**
- Static menu item exists
- Opens EntryTemplateDialogFX for editing only
- No dynamic submenu with template list
- No quick insert functionality

#### Document Type Menu

**Swing behavior:**
- "Document → Type" submenu lists all document types
- Select type → changes current document's type
- Checkmark shows current type

**JavaFX status:**
- Not implemented
- Document type can only be set via DocumentTypeSelectionDialogFX when creating new document

#### Recent Files Menu

**Swing behavior:**
- "File → Recent Files" shows last 10 opened files
- Click to reopen
- List persists across sessions

**JavaFX status:**
- Not implemented

---

## 6. Visual/UX Differences

### Layout & Styling

**Swing:**
- Classic Swing look & feel
- BorderLayout with split panes
- Toolbar with icons
- Status bar at bottom

**JavaFX:**
- Modern CSS-styled UI
- FXML-based layouts
- MenuBar (no toolbar yet)
- Status bar placeholder exists

### Table Rendering

**Swing JTable:**
- Custom cell renderers for each column type
- Color-coded debet (black) vs credit (red) amounts
- Alternating row colors
- Custom selection colors

**JavaFX TableView:**
- CSS-styled cells
- Color coding not fully matching Swing
- Modern selection highlighting
- Better font rendering

### Dialogs

**Swing:**
- JDialog-based
- Custom layouts with GroupLayout
- OK/Cancel buttons bottom-right
- Modal by default

**JavaFX:**
- Stage-based with FXML
- Modern CSS styling
- Consistent button placement
- Scene-based architecture

---

## 7. Data Binding & State Management

### Swing Approach

**DocumentFrame.java** (3,856 lines originally):
- Direct model manipulation
- Manual UI updates via listeners
- Document state stored in instance fields
- Heavy coupling between UI and business logic

### JavaFX Approach

**MainController.java** (current):
- Observable properties (`ObservableList<EntryRowModel>`)
- Automatic UI updates via bindings
- Clean separation with Manager classes:
  - DocumentNavigator
  - DocumentValidator
  - DocumentEntryManager
  - DocumentStateManager
- Callback-based loose coupling

**Advantage:**
- More maintainable
- Better testability
- Reactive updates

**Challenge:**
- Migration complexity
- Some Swing patterns don't translate directly
- Two-way binding requires careful design

---

## 8. Report Generation

### Swing Implementation

**Working reports:**
- General Journal (GeneralJournalDialog.java)
- Account Statement (AccountStatementDialog.java)
- Account Summary (AccountSummaryDialog.java)
- VAT Report (VATReportDialog.java)
- Balance Comparison (BalanceComparisonDialog.java)
- Custom Reports (ReportDialog.java)

**Features:**
- PDF export
- Print preview
- Configurable date ranges
- Account filtering
- Format options (HTML, PDF, CSV)

### JavaFX Status

**Implemented:**
- ReportEditorDialogFX (structure editing only)

**Missing:**
- All report generation dialogs (6 dialogs)
- Report viewer UI
- PDF export functionality
- Print preview
- No report rendering engine

---

## 9. Database Operations

### Parity Status: ✅ Equal

Both Swing and JavaFX use the same underlying DAO layer:
- [DataAccessObject.java](src/main/java/kirjanpito/db/DataAccessObject.java)
- Same database schema
- Same SQL queries
- Same transaction handling

**No functional differences** - JavaFX uses same database API.

---

## 10. Critical Missing Features Summary

### Must Implement (Blocks 100% parity)

1. **Entry Table Smart Navigation** (EntryTableActions.java logic)
   - Tab key smart behavior
   - Auto-create rows
   - F8 to add entry

2. **Entry Table Advanced UX**
   - Asterisk toggle debet/credit
   - Description auto-complete
   - Context menu
   - Ctrl+Backspace suffix removal

3. **Report Generation Suite** (6 dialogs + viewer)
   - General Journal
   - Account Statement
   - Account Summary
   - VAT Report
   - Balance Comparison
   - Custom Reports

4. **Dynamic Menus**
   - Entry template quick-insert menu
   - Document type selection menu
   - Recent files menu

### Should Implement (Improves usability)

5. **Missing Dialogs** (26 dialogs)
   - Search/Filter
   - Print preview
   - Data import/export
   - Backup/Restore

6. **Missing Keyboard Shortcuts** (15+ shortcuts)
   - F8, Ctrl+M, Ctrl+F, Ctrl+G, etc.

7. **Context Menus**
   - Entry table right-click
   - Document list right-click

### Nice to Have (Optional)

8. **Advanced Features**
   - Drag & drop reordering
   - Calculator dialog
   - Color scheme customization
   - Font settings

---

## 11. Testing Coverage

### Swing Version
- Manual testing only
- No unit tests for UI
- Integration testing via user acceptance

### JavaFX Version
- Same manual testing approach
- Potential for better testability (with TestFX)
- Manager classes easier to unit test
- Current status: No automated UI tests yet

---

## 12. Performance Comparison

### Swing
- Mature, optimized over years
- Known memory usage patterns
- Fast rendering for simple UIs

### JavaFX
- Modern rendering pipeline
- Better hardware acceleration
- Slightly higher memory footprint
- Smoother animations and transitions

**Overall:** No significant performance differences observed for Tilitin's use case.

---

## 13. Migration Strategy Recommendations

### Phase 1: Complete Core UX (High Priority)
1. Implement Entry Table smart navigation
2. Add debet/credit toggle (asterisk key)
3. Implement description auto-complete
4. Add context menus

**Effort:** ~2-3 weeks
**Impact:** Makes JavaFX version actually usable for daily work

### Phase 2: Report Generation (High Priority)
1. Port ReportDialog and ReportViewerDialog
2. Implement 6 report generation dialogs
3. Add PDF export
4. Add print preview

**Effort:** ~3-4 weeks
**Impact:** Critical for period-end reporting

### Phase 3: Missing Dialogs (Medium Priority)
1. Search/Filter dialogs
2. Import/Export dialogs
3. Backup/Restore dialogs
4. Print options dialog

**Effort:** ~2-3 weeks
**Impact:** Improves workflow efficiency

### Phase 4: Polish & Advanced Features (Lower Priority)
1. Dynamic menus (templates, recent files)
2. Remaining keyboard shortcuts
3. Drag & drop
4. Calculator, About, Help dialogs

**Effort:** ~1-2 weeks
**Impact:** Nice-to-have quality of life improvements

---

## 14. File Reference Map

### JavaFX Files (Kotlin)
- Controllers: [MainController.java](src/main/java/kirjanpito/ui/javafx/MainController.java)
- Dialogs: `src/main/kotlin/kirjanpito/ui/javafx/*DialogFX.kt`
- Models: `src/main/kotlin/kirjanpito/ui/javafx/*RowModel.kt`
- Cells: `src/main/kotlin/kirjanpito/ui/javafx/cells/*TableCell.kt`
- FXML: `src/main/resources/kirjanpito/ui/javafx/*.fxml`

### Swing Files (Java) - Reference for Missing Features
- Main: [DocumentFrame.java:1-3856](src/main/java/kirjanpito/ui/swing/DocumentFrame.java)
- Entry Navigation: [EntryTableActions.java:1-333](src/main/java/kirjanpito/ui/swing/EntryTableActions.java)
- Auto-complete: [DescriptionCellEditor.java](src/main/java/kirjanpito/ui/swing/DescriptionCellEditor.java)
- Dialogs: `src/main/java/kirjanpito/ui/swing/*Dialog.java` (35 files)

### Shared (Database Layer)
- DAO: [DataAccessObject.java](src/main/java/kirjanpito/db/DataAccessObject.java)
- Models: `src/main/java/kirjanpito/db/*.java` (Account, Entry, Document, etc.)

---

## Conclusion

The JavaFX version is **50-60% complete** in terms of feature parity with Swing. Core functionality works, but critical UX features (entry table navigation, reports) and many dialogs are missing. The architecture is cleaner and more maintainable, but substantial work remains to achieve full parity.

**Recommended Next Steps:**
1. Implement Entry Table smart navigation (highest user impact)
2. Port report generation suite (critical for accounting workflows)
3. Gradually add missing dialogs based on user priority feedback
