# Legacy Code Removal Plan

**Date:** 2026-01-02  
**Status:** Ready for review

---

## Overview

This document lists legacy Swing code that can be safely removed. All code listed here is **confirmed unused** by the JavaFX UI (verified by audit).

---

## ⚠️ Important Notes

1. **Backup first!** Create a git commit or branch before removing files
2. **Test after removal:** Run `./gradlew build` and `./gradlew run` to verify
3. **Keep if unsure:** If any file is questionable, keep it for now

---

## Swing Dialogs to Remove (21 files)

These dialogs are **only used by the legacy Swing UI** (`DocumentFrame.java`). The JavaFX UI uses DialogFX versions.

### Location: `src/main/java/kirjanpito/ui/`

1. ✅ `AboutDialog.java` → Replaced by `AboutDialogFX.kt` (Kotlin)
2. ✅ `AccountSelectionDialog.java` → Replaced by `AccountSelectionDialogFX.java`
3. ✅ `AppearanceDialog.java` → Replaced by `AppearanceDialogFX.java`
4. ✅ `BackupSettingsDialog.java` → Replaced by `BackupSettingsDialogFX.java`
5. ✅ `BalanceComparisonDialog.java` → Replaced by `BalanceComparisonDialogFX.java`
6. ✅ `COADialog.java` → Replaced by `COADialogFX.java`
7. ✅ `DatabaseBackupConfigDialog.java` → Functionality moved to `BackupSettingsDialogFX.java`
8. ✅ `DatabaseSettingsDialog.java` → Functionality moved to `PropertiesDialogFX.java`
9. ✅ `DataSourceInitializationDialog.java` → Replaced by `DataSourceInitializationDialogFX.java`
10. ✅ `DocumentNumberShiftDialog.java` → Replaced by `DocumentNumberShiftDialogFX.java`
11. ✅ `DocumentTypeDialog.java` → Replaced by `DocumentTypeDialogFX.java`
12. ✅ `EntryTemplateDialog.java` → Replaced by `EntryTemplateDialogFX.java`
13. ✅ `FinancialStatementOptionsDialog.java` → Replaced by `FinancialStatementOptionsDialogFX.java`
14. ✅ `PrintOptionsDialog.java` → Replaced by `PrintSettingsDialogFX.java`
15. ✅ `PropertiesDialog.java` → Replaced by `PropertiesDialogFX.java`
16. ✅ `ReportEditorDialog.java` → Replaced by `ReportEditorDialogFX.java`
17. ✅ `RestoreBackupDialog.java` → Replaced by `RestoreBackupDialogFX.java`
18. ✅ `SettingsDialog.java` → Replaced by `SettingsDialogFX.java`
19. ✅ `StartingBalanceDialog.java` → Replaced by `StartingBalanceDialogFX.java`
20. ✅ `VATChangeDialog.java` → Replaced by `VATChangeDialogFX.java`
21. ✅ `BaseDialog.java` → Base class for Swing dialogs (unused)

**Total:** 21 files, ~3000+ lines of code

---

## Swing UI Classes to Remove (18+ files)

These classes are **only used by the legacy Swing UI** (`DocumentFrame.java`). The JavaFX UI uses a different architecture.

### Location: `src/main/java/kirjanpito/ui/`

1. ✅ `DocumentFrame.java` (3262 lines) - Main Swing window
2. ✅ `DocumentFramePanel.java` - Wrapper for Compose Desktop (unused)
3. ✅ `DocumentUIBuilder.java` - UI builder for Swing
4. ✅ `DocumentUIUpdater.java` - UI updater for Swing
5. ✅ `DocumentMenuBuilder.java` - Menu builder for Swing
6. ✅ `DocumentToolbarBuilder.java` - Toolbar builder for Swing
7. ✅ `DocumentTableManager.java` - Table manager for Swing
8. ✅ `DocumentMenuHandler.java` - Menu handler for Swing
9. ✅ `DocumentNavigator.java` - Navigator (JavaFX uses different one)
10. ✅ `DocumentStateManager.java` - State manager for Swing
11. ✅ `DocumentValidator.java` - Validator (JavaFX uses different one)
12. ✅ `DocumentEntryManager.java` - Entry manager for Swing
13. ✅ `DocumentDataSourceManager.java` - DataSource manager for Swing
14. ✅ `DocumentBackupManager.java` - Backup manager for Swing
15. ✅ `DocumentExporter.java` - Exporter (JavaFX uses different one)
16. ✅ `DocumentPrinter.java` - Printer (JavaFX uses different one)
17. ✅ `DocumentListenerHelpers.java` - Listener helpers for Swing
18. ✅ `EntryTableActions.java` - Table actions for Swing

**Total:** 18+ files, ~15,000+ lines of code

---

## Legacy Entry Point (Optional)

### `Kirjanpito.java` - Legacy Swing entry point

**Status:** Used by `./gradlew runSwing` task

**Decision:**
- **Option 1:** Keep it (allows users to run old Swing UI if needed)
- **Option 2:** Remove it (if all users have migrated to JavaFX)

**Recommendation:** Keep for now, remove in future release

---

## Removal Script

### Safe Removal (Recommended)

Remove files in phases:

#### Phase 1: Swing Dialogs (Low Risk)
```bash
# Backup first!
git checkout -b remove-legacy-swing-dialogs

# Remove Swing dialogs
rm src/main/java/kirjanpito/ui/AboutDialog.java
rm src/main/java/kirjanpito/ui/AccountSelectionDialog.java
rm src/main/java/kirjanpito/ui/AppearanceDialog.java
rm src/main/java/kirjanpito/ui/BackupSettingsDialog.java
rm src/main/java/kirjanpito/ui/BalanceComparisonDialog.java
rm src/main/java/kirjanpito/ui/COADialog.java
rm src/main/java/kirjanpito/ui/DatabaseBackupConfigDialog.java
rm src/main/java/kirjanpito/ui/DatabaseSettingsDialog.java
rm src/main/java/kirjanpito/ui/DataSourceInitializationDialog.java
rm src/main/java/kirjanpito/ui/DocumentNumberShiftDialog.java
rm src/main/java/kirjanpito/ui/DocumentTypeDialog.java
rm src/main/java/kirjanpito/ui/EntryTemplateDialog.java
rm src/main/java/kirjanpito/ui/FinancialStatementOptionsDialog.java
rm src/main/java/kirjanpito/ui/PrintOptionsDialog.java
rm src/main/java/kirjanpito/ui/PropertiesDialog.java
rm src/main/java/kirjanpito/ui/ReportEditorDialog.java
rm src/main/java/kirjanpito/ui/RestoreBackupDialog.java
rm src/main/java/kirjanpito/ui/SettingsDialog.java
rm src/main/java/kirjanpito/ui/StartingBalanceDialog.java
rm src/main/java/kirjanpito/ui/VATChangeDialog.java
rm src/main/java/kirjanpito/ui/BaseDialog.java

# Test
./gradlew build
./gradlew run

# If successful, commit
git commit -m "refactor: Remove unused Swing dialogs (replaced by JavaFX versions)"
```

#### Phase 2: Swing UI Classes (Medium Risk)
```bash
# Remove Swing UI classes
rm src/main/java/kirjanpito/ui/DocumentFrame.java
rm src/main/java/kirjanpito/ui/DocumentFramePanel.java
rm src/main/java/kirjanpito/ui/DocumentUIBuilder.java
rm src/main/java/kirjanpito/ui/DocumentUIUpdater.java
rm src/main/java/kirjanpito/ui/DocumentMenuBuilder.java
rm src/main/java/kirjanpito/ui/DocumentToolbarBuilder.java
rm src/main/java/kirjanpito/ui/DocumentTableManager.java
rm src/main/java/kirjanpito/ui/DocumentMenuHandler.java
rm src/main/java/kirjanpito/ui/DocumentNavigator.java
rm src/main/java/kirjanpito/ui/DocumentStateManager.java
rm src/main/java/kirjanpito/ui/DocumentValidator.java
rm src/main/java/kirjanpito/ui/DocumentEntryManager.java
rm src/main/java/kirjanpito/ui/DocumentDataSourceManager.java
rm src/main/java/kirjanpito/ui/DocumentBackupManager.java
rm src/main/java/kirjanpito/ui/DocumentExporter.java
rm src/main/java/kirjanpito/ui/DocumentPrinter.java
rm src/main/java/kirjanpito/ui/DocumentListenerHelpers.java
rm src/main/java/kirjanpito/ui/EntryTableActions.java

# Test
./gradlew build
./gradlew run

# If successful, commit
git commit -m "refactor: Remove unused Swing UI classes (replaced by JavaFX architecture)"
```

#### Phase 3: Legacy Entry Point (Optional)
```bash
# Only if you're sure no one needs the Swing UI
rm src/main/java/kirjanpito/ui/Kirjanpito.java

# Update build.gradle.kts - remove runSwing task
# Test
./gradlew build
./gradlew run

# If successful, commit
git commit -m "refactor: Remove legacy Swing entry point"
```

---

## Verification Checklist

After removal, verify:

- [ ] `./gradlew build` succeeds
- [ ] `./gradlew run` launches JavaFX app successfully
- [ ] All dialogs open correctly
- [ ] No compilation errors
- [ ] No runtime errors
- [ ] Application functions normally

---

## Impact

### Code Reduction:
- **Swing Dialogs:** ~3000 lines removed
- **Swing UI Classes:** ~15,000 lines removed
- **Total:** ~18,000 lines of legacy code removed

### Benefits:
- ✅ Smaller codebase (easier to maintain)
- ✅ Less confusion (no duplicate implementations)
- ✅ Faster builds (less code to compile)
- ✅ Clearer architecture (only JavaFX code)

### Risks:
- ⚠️ If someone still needs Swing UI, they'll need to use an older version
- ⚠️ `runSwing` task will break (if not removed)

---

## Recommendation

**Start with Phase 1** (Swing dialogs) - lowest risk, highest value.

Then proceed to Phase 2 after thorough testing.

Phase 3 (entry point) can wait until you're certain no one needs it.

---

**Created:** 2026-01-02  
**Last Updated:** 2026-01-02
