# JavaFX Development Action Plan

**Created:** 2026-01-01
**Updated:** 2026-01-01
**Current Status:** ~65-70% feature parity with Swing
**See also:** [JAVAFX-VS-SWING-COMPARISON.md](JAVAFX-VS-SWING-COMPARISON.md)

---

## Phase 1: Entry Table UX (CRITICAL) ✅ COMPLETED

**Goal:** Make JavaFX version usable for daily accounting work

### 1.1 Smart Tab Navigation ✅ DONE

**Implementation:**

- ✅ Ported logic from [EntryTableActions.java](src/main/java/kirjanpito/ui/EntryTableActions.java)
- ✅ Custom [EntryTableNavigationHandler.kt](src/main/kotlin/kirjanpito/ui/javafx/EntryTableNavigationHandler.kt)
- ✅ Tab logic:
  - Account → Debet/Credit (smart toggle) → Amount → Description
  - Description → Account (next row)
  - Last row → Auto-create new entry or new document
- ✅ Shift+Tab reverses direction
- ✅ Up arrow from first row → Focus date field

**Files created/modified:**

- ✅ Created `EntryTableNavigationHandler.kt`
- ✅ Modified `MainController.java` (integration)
- ✅ Modified `EntryRowModel.kt` (toggleDebitCredit method)

### 1.2 Debet/Credit Toggle with Asterisk ✅ DONE

**Implementation:**

- ✅ Asterisk (*) key handler in navigation handler
- ✅ Toggle debet/credit for current entry
- ✅ Updates totals automatically
- ✅ Works in both debet and credit columns

**Files modified:**

- ✅ `EntryTableNavigationHandler.kt`
- ✅ `EntryRowModel.kt` (toggleDebitCredit, isDebit methods)
- ✅ `AmountTableCell.kt` (asterisk handling)

### 1.3 Description Auto-complete ✅ DONE

**Implementation:**

- ✅ Enhanced [DescriptionTableCell.kt](src/main/kotlin/kirjanpito/ui/javafx/cells/DescriptionTableCell.kt)
- ✅ Uses TreeMapAutoCompleteSupport from existing codebase
- ✅ Auto-completes as user types (2+ characters)
- ✅ F12 / Ctrl+Backspace removes suffix (last comma-separated part)

**Files modified:**

- ✅ `DescriptionTableCell.kt` (full rewrite with auto-complete)
- ✅ `MainController.java` (auto-complete support integration)

### 1.4 Additional Keyboard Shortcuts ✅ DONE

- ✅ Enter - Start editing / Add entry
- ✅ Ctrl+Enter - Create new document
- ✅ Ctrl+Backspace - Remove description suffix
- ✅ Up arrow from top row - Focus date picker

---

## Phase 2: Reports (HIGH PRIORITY - 20-30 hours)

**Goal:** Enable period-end reporting (required for accounting compliance)

### 2.1 Report Generation Dialogs (20-30 hours)

**Required dialogs:**

1. **GeneralJournalDialogFX** (3-5 hours)
   - Date range selection
   - Account filter
   - PDF/HTML export

2. **AccountStatementDialogFX** (3-5 hours)
   - Account selection
   - Date range
   - PDF/HTML export

3. **AccountSummaryDialogFX** (3-5 hours)
   - All accounts summary
   - Date range
   - PDF export

4. **VATReportDialogFX** (4-6 hours)
   - VAT calculation by period
   - Export options

5. **BalanceComparisonDialogFX** (4-6 hours)
   - Compare balances between periods
   - Chart generation

6. **ReportViewerDialogFX** (3-5 hours)
   - Generic report display
   - Print preview
   - Export options

**Technical notes:**

- Reuse existing report generation logic from Swing
- JavaFX WebView for HTML reports
- PDF export via existing libraries

---

## Phase 3: Additional Keyboard Shortcuts (MEDIUM - 4-6 hours)

**Missing shortcuts:**

- F8: Add new entry
- Ctrl+Backspace: Remove suffix from description
- Ctrl+M: Edit entry templates
- Ctrl+F: Search/Find
- Ctrl+G: General journal
- Ctrl+R: Account statement
- Ctrl+L: Account summary

**Implementation:**

- Add to FXML accelerators or global key event filter
- Link to existing handlers

---

## Phase 4: Context Menus (MEDIUM - 3-4 hours)

### 4.1 Entry Table Context Menu

**Actions:**

- Insert entry above
- Insert entry below
- Delete entry
- Copy entry
- Paste entry
- Toggle debet/credit

### 4.2 Document List Context Menu

**Actions:**

- Open document
- Delete document
- Copy document
- Document properties

---

## Phase 5: Dynamic Menus (MEDIUM - 4-6 hours)

### 5.1 Entry Template Quick-Insert Menu

**Implementation:**

- Populate "Data → Entry Templates" submenu from database
- Alt+1 through Alt+9 shortcuts
- Click template → insert pre-filled entries

### 5.2 Document Type Menu

**Implementation:**

- Populate "Document → Type" submenu
- Click type → change current document type
- Checkmark shows current type

### 5.3 Recent Files Menu

**Implementation:**

- Track last 10 opened files
- Persist across sessions
- Click to reopen

---

## Phase 6: Remaining Dialogs (LOWER PRIORITY - 30-50 hours)

### 6.1 Data Management (10-15 hours)

- BackupSettingsDialogFX (4-6 hours)
- RestoreBackupDialogFX (3-5 hours)
- DataExportDialogFX (2-3 hours)
- CSVImportDialogFX (4-6 hours)

### 6.2 Tools (8-12 hours)

- SearchDialogFX (4-6 hours)
- FilterDialogFX (3-5 hours)
- BalanceComparisonDialogFX (already in Phase 2)
- DocumentNumberShiftDialogFX (2-4 hours)
- VATChangeDialogFX (3-5 hours)

### 6.3 Utilities (5-8 hours)

- CalculatorDialogFX (2-3 hours)
- AboutDialogFX (1-2 hours)
- HelpDialogFX (2-3 hours)

---

## Phase 7: Advanced Features (OPTIONAL - 10-15 hours)

### 7.1 Drag & Drop (5-7 hours)

- Reorder entry rows
- Visual feedback during drag

### 7.2 UI Customization (5-8 hours)

- ColorSchemeDialogFX
- FontSettingsDialogFX
- Layout preferences

---

## Effort Summary

| Phase | Effort (hours) | Priority | Status |
|-------|----------------|----------|--------|
| **Phase 1: Entry Table UX** | 16-23 | CRITICAL | ⏳ Not started |
| **Phase 2: Reports** | 20-30 | HIGH | ⏳ Not started |
| **Phase 3: Keyboard Shortcuts** | 4-6 | MEDIUM | ⏳ Not started |
| **Phase 4: Context Menus** | 3-4 | MEDIUM | ⏳ Not started |
| **Phase 5: Dynamic Menus** | 4-6 | MEDIUM | ⏳ Not started |
| **Phase 6: Remaining Dialogs** | 30-50 | LOW | ⏳ Not started |
| **Phase 7: Advanced Features** | 10-15 | OPTIONAL | ⏳ Not started |
| **TOTAL** | **87-134 hours** | | **50-60% complete** |

---

## Recommended Approach

### Sprint 1: Make JavaFX Usable (16-23 hours)

**Focus:** Entry Table UX (Phase 1)

**Outcome:** JavaFX version becomes practical for daily data entry

### Sprint 2: Enable Reporting (20-30 hours)

**Focus:** Report Generation (Phase 2)

**Outcome:** Can complete accounting periods and generate required reports

### Sprint 3: Polish & Efficiency (11-16 hours)

**Focus:** Keyboard shortcuts + Context menus + Dynamic menus (Phases 3-5)

**Outcome:** Power users can work efficiently

### Sprint 4: Feature Completeness (30-50 hours)

**Focus:** Remaining dialogs (Phase 6)

**Outcome:** Full feature parity with Swing

### Sprint 5: Optional Enhancements (10-15 hours)

**Focus:** Advanced features (Phase 7)

**Outcome:** Modern UX improvements beyond Swing

---

## Success Criteria

### Minimal Viable JavaFX (After Sprint 1)

- ✅ Can create and edit documents
- ✅ Entry table navigation feels natural (Tab logic)
- ✅ Quick debet/credit toggle (asterisk)
- ✅ Basic reporting works (existing)

### Production Ready (After Sprint 2)

- ✅ All critical reports available
- ✅ Can complete accounting period
- ✅ VAT reporting works

### Feature Parity (After Sprint 4)

- ✅ All Swing dialogs ported
- ✅ All menu handlers implemented
- ✅ All keyboard shortcuts work

---

## Notes

- **Phase 1 is non-negotiable** - Without it, users will reject JavaFX version
- **Phase 2 is legally required** - Finnish accounting law requires certain reports
- Phases 3-5 improve productivity but aren't blockers
- Phase 6 can be done incrementally based on user requests
- Phase 7 is "nice to have" - can be skipped

---

## Next Steps

1. Start Phase 1.1: Smart Tab Navigation
2. Test thoroughly with real accounting data
3. Get user feedback before proceeding to Phase 2
