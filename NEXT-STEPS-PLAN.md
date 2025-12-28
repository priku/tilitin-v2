# Tilitin - Next Steps Plan
## Comprehensive Development Roadmap

**Current Version:** 2.2.1  
**Last Updated:** 2025-12-29  
**DocumentFrame Size:** ~2,654 lines (target: <500 lines)

---

## 📊 Current State Analysis

### ✅ Completed (v2.0.0 - v2.1.5)

1. **UI Modernization**
   - ✅ FlatLaf integration with light/dark themes
   - ✅ Modern icons (up to 256x256)
   - ✅ Splash screen with gradients
   - ✅ AppearanceDialog with live preview
   - ✅ UIConstants foundation
   - ✅ BaseDialog foundation

2. **Kotlin Migration (Phase 1-3)**
   - ✅ 6 data classes (Account, Document, Entry, Period, DocumentType, COAHeading)
   - ✅ 3 utility classes (SwingExtensions, ValidationUtils, DialogUtils)
   - ✅ DAO foundation (DatabaseExtensions, SQLAccountDAOKt)
   - ✅ SQLiteAccountDAOKt in production

3. **DocumentFrame Refactoring (Partial)**
   - ✅ Phase 1: DocumentBackupManager (193 lines)
   - ✅ Phase 1b: DocumentExporter (83 lines)
   - ✅ Phase 2: DocumentMenuBuilder (453 lines)
   - ✅ Phase 2: DocumentToolbarBuilder (112 lines)
   - ✅ Phase 3: DocumentListenerHelpers (76 lines)
   - ✅ Phase 3: EntryTableActions (280 lines)
   - ✅ Phase 3b: DocumentTableManager (400 lines)
   - ✅ Phase 4: Code cleanup (imports, wrappers)
   - **Progress:** 3,856 → ~2,930 lines (-24%)

4. **Backup System**
   - ✅ Per-database backup locations
   - ✅ Cloud storage detection
   - ✅ Auto-backup system

5. **Windows Packaging**
   - ✅ jPackage app-image
   - ✅ Inno Setup installer
   - ✅ MSI installer

### ❌ Remaining Issues

- **DocumentFrame:** Still 3,024 lines (God Object)
- **Legacy Dialogs:** 19 dialogs need theme support
- **Anonymous Inner Classes:** ~10+ in DocumentFrame, 40+ total
- **Hardcoded Colors:** Many instances across legacy dialogs
- **Deprecated APIs:** Some usage still present

---

## 🎯 Strategic Priorities

### Priority 1: Complete DocumentFrame Refactoring (CRITICAL)
**Why:** DocumentFrame is still a 3,024-line God Object, making maintenance difficult.

**Target:** Reduce to <500 lines by extracting remaining functionality.

### Priority 2: Theme Support for Legacy Dialogs (HIGH)
**Why:** Dark mode looks broken in 19 dialogs, affecting user experience.

**Target:** All dialogs support light/dark themes seamlessly.

### Priority 3: Code Modernization (MEDIUM)
**Why:** Anonymous inner classes and deprecated APIs reduce code quality.

**Target:** Modern Java patterns throughout codebase.

---

## 📋 Detailed Action Plan

## SPRINT 1: Complete DocumentFrame Refactoring (2-3 weeks)

### Phase 3b: Table Management Extraction

**Goal:** Extract table-related code from DocumentFrame.

**Tasks:**
1. **Create `DocumentTableManager.java`**
   - Extract table creation logic
   - Extract cell renderer/editor setup
   - Extract column configuration
   - Extract table event handlers
   - **Estimated:** 300-400 lines

2. **Extract Cell Renderers/Editors**
   - Move `AccountCellRenderer`, `AccountCellEditor` logic
   - Move `DateCellRenderer`, `DateCellEditor` logic
   - Move `CurrencyCellRenderer`, `CurrencyCellEditor` logic
   - Move `ComboBoxCellRenderer`, `ComboBoxCellEditor` logic
   - **Estimated:** 200-300 lines

3. **Extract Table Event Handlers**
   - Move table selection listeners
   - Move table model listeners
   - Move keyboard event handlers
   - **Estimated:** 150-200 lines

**Expected Result:** DocumentFrame reduced by ~650-900 lines → ~2,100-2,400 lines

---

### Phase 4: Event Handling Extraction

**Goal:** Extract all event handling logic.

**Tasks:**
1. **Create `DocumentEventHandler.java`**
   - Extract all ActionListener implementations
   - Extract WindowListener implementations
   - Extract PropertyChangeListener implementations
   - Extract TableModelListener implementations
   - **Estimated:** 400-500 lines

2. **Convert Anonymous Inner Classes to Lambdas**
   - Replace remaining `new ActionListener()` with lambdas
   - Replace `new WindowListener()` with method references
   - Replace other anonymous classes
   - **Estimated:** 50-100 lines saved

**Expected Result:** DocumentFrame reduced by ~450-600 lines → ~1,650-1,950 lines

---

### Phase 5: Print Operations Extraction ✅ (v2.2.1)

**Goal:** Extract all print-related functionality.

**Status:** ✅ COMPLETED

**Tasks:**
1. ✅ **Laajennettu `DocumentPrinter.java`** (434 riviä)
   - Extract print preview logic
   - Extract print dialog handling
   - Extract report generation calls
   - Extract print options management
   - **Estimated:** 200-300 lines

**Expected Result:** ✅ DocumentFrame reduced by ~276 lines → ~2,654 lines (COMPLETED v2.2.1)

---

### Phase 6: Document Navigation & State Management

**Goal:** Extract document navigation and state logic.

**Tasks:**
1. **Create `DocumentNavigator.java`**
   - Extract prev/next/first/last document logic
   - Extract document search logic
   - Extract document filtering
   - **Estimated:** 200-250 lines

2. **Create `DocumentStateManager.java`**
   - Extract document loading logic
   - Extract document saving logic
   - Extract dirty state tracking
   - Extract validation logic
   - **Estimated:** 300-400 lines

**Expected Result:** DocumentFrame reduced by ~500-650 lines → ~900-1,250 lines

---

### Phase 7: UI Component Management

**Goal:** Extract UI component creation and management.

**Tasks:**
1. **Create `DocumentUIBuilder.java`**
   - Extract all component creation
   - Extract layout management
   - Extract component initialization
   - **Estimated:** 400-500 lines

2. **Create `DocumentUIUpdater.java`**
   - Extract UI update logic
   - Extract label updates
   - Extract button state management
   - **Estimated:** 200-300 lines

**Expected Result:** DocumentFrame reduced by ~600-800 lines → **~300-650 lines** ✅

---

## SPRINT 2: Theme Support for Legacy Dialogs (1-2 weeks)

### Task 1: Audit All Dialogs

**Goal:** Identify all hardcoded colors and theme issues.

**Tasks:**
1. List all 19 legacy dialogs
2. Search for `new Color(...)` in each dialog
3. Document current color usage
4. Create migration checklist

**Dialogs to Update:**
- SettingsDialog.java
- PropertiesDialog.java
- COADialog.java
- AccountSelectionDialog.java
- EntryTemplateDialog.java
- FinancialStatementOptionsDialog.java
- StartingBalanceDialog.java
- SearchDialog.java
- PrintStyleEditorDialog.java
- ChartOptionsDialog.java
- VoucherTemplateDialog.java
- ImportCSVDialog.java
- AccountPeriodDialog.java
- PeriodDialog.java
- AccountDialog.java
- COATableDialog.java
- ReportStructureDialog.java
- CompanyInformationDialog.java
- (and others...)

---

### Task 2: Create Theme Helper Utilities

**Goal:** Simplify theme color access.

**Tasks:**
1. **Extend UIConstants.java**
   ```java
   // Add theme-aware color getters
   public static Color getBackgroundColor() {
       return UIManager.getColor("Panel.background");
   }
   
   public static Color getForegroundColor() {
       return UIManager.getColor("Label.foreground");
   }
   
   public static Color getBorderColor() {
       return UIManager.getColor("Border.color");
   }
   ```

2. **Create ThemeUtils.java** (optional)
   - Helper methods for common color operations
   - Contrast checking utilities
   - Theme change listeners

---

### Task 3: Migrate Dialogs (Batch 1: High Priority - 5 dialogs)

**Priority Dialogs:**
1. SettingsDialog.java
2. PropertiesDialog.java
3. COADialog.java
4. AccountDialog.java
5. EntryTemplateDialog.java

**Tasks per Dialog:**
1. Replace `new Color(...)` with `UIConstants.getBackgroundColor()` etc.
2. Test in light mode
3. Test in dark mode
4. Verify contrast ratios
5. Fix any visibility issues

**Estimated:** 2-3 days for 5 dialogs

---

### Task 4: Migrate Dialogs (Batch 2: Medium Priority - 7 dialogs)

**Dialogs:**
6. FinancialStatementOptionsDialog.java
7. StartingBalanceDialog.java
8. SearchDialog.java
9. PrintStyleEditorDialog.java
10. ChartOptionsDialog.java
11. ImportCSVDialog.java
12. AccountPeriodDialog.java

**Estimated:** 3-4 days for 7 dialogs

---

### Task 5: Migrate Dialogs (Batch 3: Remaining - 7 dialogs)

**Dialogs:**
13. PeriodDialog.java
14. COATableDialog.java
15. ReportStructureDialog.java
16. CompanyInformationDialog.java
17. VoucherTemplateDialog.java
18. (and others...)

**Estimated:** 2-3 days for remaining dialogs

---

## SPRINT 3: Code Modernization (1-2 weeks)

### Task 1: Replace Anonymous Inner Classes

**Goal:** Convert all anonymous inner classes to lambdas/method references.

**Tasks:**
1. **DocumentFrame.java** (10 remaining)
   - Find all `new ActionListener()`
   - Find all `new WindowListener()`
   - Find all `new FocusListener()`
   - Convert to lambdas
   - Test functionality

2. **Other UI Files** (~30 remaining)
   - COADialog.java
   - SettingsDialog.java
   - PropertiesDialog.java
   - EntryTemplateDialog.java
   - (and others...)

**Estimated:** 3-5 days

---

### Task 2: Remove Deprecated APIs

**Goal:** Replace deprecated methods with modern alternatives.

**Tasks:**
1. **DocumentMenuBuilder.java**
   ```java
   // OLD (deprecated)
   int shortcutKeyMask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
   
   // NEW
   int shortcutKeyMask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();
   // OR
   int shortcutKeyMask = System.getProperty("os.name").toLowerCase().contains("mac") 
       ? InputEvent.META_DOWN_MASK 
       : InputEvent.CTRL_DOWN_MASK;
   ```

2. **Kirjanpito.java**
   ```java
   // OLD (reflection hack)
   Field awtAppClassNameField = toolkitClass.getDeclaredField("awtAppClassName");
   
   // NEW (JVM argument)
   // Use -Dawt.appClassName=Tilitin in launch script
   ```

**Estimated:** 1-2 days

---

### Task 3: Migrate to UIConstants

**Goal:** Use UIConstants throughout codebase.

**Tasks:**
1. Replace hardcoded `Insets` with `UIConstants.INSETS_*`
2. Replace hardcoded `Border` with `UIConstants.BORDER_*`
3. Replace hardcoded spacing values
4. Update all 19 legacy dialogs

**Estimated:** 2-3 days

---

## SPRINT 4: BaseDialog Migration (1-2 weeks)

### Task 1: Migrate Simple Dialogs

**Goal:** Convert 5-7 simple dialogs to use BaseDialog.

**Candidates:**
- AccountPeriodDialog.java
- PeriodDialog.java
- (other simple dialogs)

**Tasks per Dialog:**
1. Extend BaseDialog instead of JDialog
2. Use `createContentPanel()` method
3. Use `createButtonPanel()` method
4. Remove duplicate OK/Cancel logic
5. Test functionality

**Estimated:** 3-4 days for 5-7 dialogs

---

### Task 2: Migrate Complex Dialogs

**Goal:** Convert remaining dialogs to BaseDialog.

**Candidates:**
- SettingsDialog.java
- PropertiesDialog.java
- COADialog.java
- (and others...)

**Estimated:** 4-5 days for remaining dialogs

---

## SPRINT 5: Kotlin Migration (Phase 4) - Future

### Task 1: Entry DAO Migration

**Goal:** Migrate Entry DAO to Kotlin.

**Tasks:**
1. Create `SQLEntryDAOKt.kt` (abstract base)
2. Create `SQLiteEntryDAOKt.kt` (implementation)
3. Update SQLiteDataSource to use Kotlin DAO
4. Test thoroughly
5. Remove Java fallback

**Estimated:** 3-4 days

---

### Task 2: Document DAO Migration

**Goal:** Migrate Document DAO to Kotlin.

**Tasks:**
1. Create `SQLDocumentDAOKt.kt` (abstract base)
2. Create `SQLiteDocumentDAOKt.kt` (implementation)
3. Update SQLiteDataSource
4. Test thoroughly
5. Remove Java fallback

**Estimated:** 3-4 days

---

## 📅 Recommended Timeline

### Q1 2025 (Weeks 1-12)

**Month 1 (Weeks 1-4): DocumentFrame Refactoring**
- Week 1-2: Phase 3b (Table Management)
- Week 3: Phase 4 (Event Handling)
- Week 4: Phase 5 (Print Operations)

**Month 2 (Weeks 5-8): DocumentFrame Completion + Theme Support**
- Week 5: Phase 6 (Navigation & State)
- Week 6: Phase 7 (UI Components)
- Week 7-8: Theme Support (Batch 1-2)

**Month 3 (Weeks 9-12): Theme Support + Modernization**
- Week 9: Theme Support (Batch 3)
- Week 10: Code Modernization (Lambdas, Deprecated APIs)
- Week 11: UIConstants Migration
- Week 12: BaseDialog Migration (Batch 1)

### Q2 2025 (Weeks 13-24)

**Month 4 (Weeks 13-16): BaseDialog + Polish**
- Week 13-14: BaseDialog Migration (Batch 2)
- Week 15: Testing & Bug Fixes
- Week 16: Documentation Updates

**Month 5-6 (Weeks 17-24): Kotlin Migration + Advanced Features**
- Week 17-18: Entry DAO Migration
- Week 19-20: Document DAO Migration
- Week 21-22: Advanced Features (optional)
- Week 23-24: Final Testing & Release Prep

---

## 🎯 Success Metrics

### Code Quality Metrics

**Current:**
- DocumentFrame: 3,024 lines
- Legacy dialogs: 19
- Anonymous inner classes: ~40+
- Hardcoded colors: Many

**Target (v2.2.0):**
- DocumentFrame: <500 lines ✅
- Legacy dialogs: 0 ✅
- Anonymous inner classes: 0 ✅
- Hardcoded colors: 0 ✅

### User Experience Metrics

**Current:**
- Dark mode: Broken in 19 dialogs
- Theme switching: Works but inconsistent

**Target:**
- Dark mode: Perfect in all dialogs ✅
- Theme switching: Seamless everywhere ✅

---

## 🚀 Quick Wins (Can Start Immediately)

### 1. Replace Deprecated API in DocumentMenuBuilder (30 minutes)
```java
// In DocumentMenuBuilder.java line 54
// OLD:
this.shortcutKeyMask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();

// NEW:
this.shortcutKeyMask = System.getProperty("os.name").toLowerCase().contains("mac")
    ? InputEvent.META_DOWN_MASK
    : InputEvent.CTRL_DOWN_MASK;
```

### 2. Add Theme Color Getters to UIConstants (1 hour)
Add helper methods to UIConstants.java for common colors.

### 3. Convert 5 Anonymous Inner Classes in DocumentFrame (2 hours)
Find and convert 5 simple ActionListener instances to lambdas.

### 4. Migrate One Simple Dialog to BaseDialog (3-4 hours)
Pick the simplest dialog (e.g., AccountPeriodDialog) and migrate it.

---

## 📝 Notes

- **Incremental Approach:** Each phase should be tested before moving to the next
- **Backward Compatibility:** Maintain API compatibility during refactoring
- **Documentation:** Update inline comments and documentation as you go
- **Testing:** Test each change in both light and dark themes
- **Git Strategy:** Use feature branches for each phase

---

## 🔄 Continuous Improvement

After completing the main plan:

1. **GridBagLayout Migration** (optional)
   - Consider MigLayout for complex dialogs
   - Evaluate if worth the dependency

2. **Cell Renderer Consolidation**
   - Create generic renderer factory
   - Reduce code duplication

3. **Accessibility Features**
   - Add keyboard navigation
   - Add screen reader support

4. **Performance Optimization**
   - Profile application
   - Optimize slow operations
   - Add caching where appropriate

---

**Last Updated:** 2025-12-28  
**Next Review:** After Sprint 1 completion

