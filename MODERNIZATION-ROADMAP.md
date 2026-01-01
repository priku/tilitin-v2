# Tilitin v2 - Modernization Roadmap

**Date:** 2026-01-02  
**Current Status:** JavaFX UI 100% complete, Kotlin migration 7.8%  
**Next Phase:** Continue modernization

---

## 🎯 Modernization Priorities

### Priority 1: Testing Infrastructure (HIGH IMPACT) ⭐

**Current State:**
- Only 1 test file found: `AttachmentDAOTest.java`
- No unit tests for business logic
- No integration tests
- Manual testing only

**Why First:**
- Enables safe refactoring
- Prevents regressions
- Improves code quality
- Industry best practice

**Tasks:**
1. **Set up JUnit 5 + TestFX**
   ```kotlin
   // build.gradle.kts
   dependencies {
       testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")
       testImplementation("org.testfx:testfx-junit5:4.0.18")
   }
   ```

2. **Create test structure:**
   ```
   src/test/
   ├── kotlin/
   │   ├── db/          # DAO tests
   │   ├── models/      # Model tests
   │   └── ui/          # UI component tests
   └── java/
       └── integration/ # Integration tests
   ```

3. **Priority test areas:**
   - ✅ DAO layer (database operations)
   - ✅ DocumentModel (business logic)
   - ✅ Entry validation
   - ✅ Report generation
   - ✅ CSV import/export

**Estimated Time:** 2-3 weeks  
**Impact:** High - Enables safe modernization

---

### Priority 2: Kotlin Migration - UI Layer (MEDIUM IMPACT)

**Current State:**
- 29 JavaFX DialogFX files in Java
- MainController.java: ~3000 lines (Java)
- Only 7.8% Kotlin codebase

**Why Second:**
- Reduces code size (Kotlin is more concise)
- Improves null safety
- Better IDE support
- Modern language features

**Tasks:**

#### 2.1: Migrate Simple Dialogs to Kotlin
**Target:** Start with smaller, self-contained dialogs

**Candidates:**
- `AboutDialogFX.java` → `AboutDialogFX.kt`
- `HelpDialogFX.java` → `HelpDialogFX.kt`
- `DebugInfoDialogFX.java` → `DebugInfoDialogFX.kt`
- `PropertiesDialogFX.java` → `PropertiesDialogFX.kt`

**Benefits:**
- ~30-40% code reduction
- Null-safety improvements
- Extension functions for cleaner code

**Estimated Time:** 1-2 weeks (4 dialogs)

#### 2.2: Create Kotlin BaseDialog Pattern
**Task:** Create reusable dialog base class in Kotlin

```kotlin
abstract class BaseDialogFX(owner: Window?) {
    protected val dialog = Stage().apply {
        initModality(Modality.APPLICATION_MODAL)
        owner?.let { initOwner(it) }
    }
    
    abstract fun createContent(): Parent
    abstract fun onOK(): Boolean
    
    fun show() { /* ... */ }
    fun showAndWait(): Boolean { /* ... */ }
}
```

**Benefits:**
- Consistent dialog patterns
- Less boilerplate
- Easier to maintain

**Estimated Time:** 1 week

#### 2.3: Migrate MainController to Kotlin (LONG TERM)
**Current:** `MainController.java` ~3000 lines

**Strategy:**
1. Extract handlers to separate Kotlin classes
2. Migrate in phases:
   - Phase 1: Extract menu handlers → `MenuHandlers.kt`
   - Phase 2: Extract report handlers → `ReportHandlers.kt`
   - Phase 3: Extract document handlers → `DocumentHandlers.kt`
   - Phase 4: Migrate MainController itself

**Benefits:**
- Better code organization
- Easier to test
- More maintainable

**Estimated Time:** 4-6 weeks (phased approach)

---

### Priority 3: Legacy Code Cleanup (LOW RISK)

**Current State:**
- 21 Swing dialog classes (unused)
- 18+ DocumentFrame-related classes (unused)
- Old Swing entry point (`Kirjanpito.java`)

**Why Third:**
- Low risk (code is unused)
- Reduces codebase size
- Simplifies maintenance
- Removes confusion

**Tasks:**

#### 3.1: Remove Legacy Swing Dialogs
**Files to remove:**
- All 21 Swing dialog classes in `src/main/java/kirjanpito/ui/`
- `BaseDialog.java` (Swing base class)

**Verification:**
- ✅ Confirmed unused by audit
- ✅ No imports in JavaFX code
- ✅ Only used by legacy Swing UI

**Estimated Time:** 1 day  
**Risk:** Very Low

#### 3.2: Remove Legacy Swing UI Classes
**Files to remove:**
- `DocumentFrame.java` (3262 lines)
- `DocumentFramePanel.java`
- `DocumentUIBuilder.java`
- `DocumentUIUpdater.java`
- `DocumentMenuBuilder.java`
- `DocumentToolbarBuilder.java`
- `DocumentTableManager.java`
- `DocumentMenuHandler.java`
- `DocumentNavigator.java`
- `DocumentStateManager.java`
- `DocumentValidator.java`
- `DocumentEntryManager.java`
- `DocumentDataSourceManager.java`
- `DocumentBackupManager.java`
- `DocumentExporter.java`
- `DocumentPrinter.java`
- `DocumentListenerHelpers.java`
- `EntryTableActions.java`

**Verification:**
- ✅ Only used by `Kirjanpito.java` (legacy entry point)
- ✅ JavaFX uses different architecture

**Estimated Time:** 1 day  
**Risk:** Low (can keep `runSwing` task disabled)

#### 3.3: Remove Legacy Entry Point (Optional)
**File:** `Kirjanpito.java`

**Decision:**
- Keep if users still need Swing version
- Remove if all users migrated to JavaFX

**Estimated Time:** 1 hour  
**Risk:** Medium (if users still need it)

---

### Priority 4: Modern JavaFX Features (MEDIUM IMPACT)

**Current State:**
- Using JavaFX 21 (modern)
- Some patterns could be improved
- PrintPreviewFrame still uses Swing

**Tasks:**

#### 4.1: Migrate PrintPreviewFrame to JavaFX
**Current:** Uses Swing `PrintPreviewFrame` embedded via `SwingNode`

**Target:** Use JavaFX Print API directly

**Benefits:**
- Pure JavaFX (no Swing bridge)
- Better performance
- Modern print API

**Challenges:**
- JavaFX Print API limitations
- May need to keep Swing for some features

**Estimated Time:** 2-3 weeks  
**Risk:** Medium (may need to keep Swing)

#### 4.2: Use JavaFX Properties More
**Current:** Some manual property binding

**Target:** Leverage JavaFX properties and bindings

**Benefits:**
- Automatic UI updates
- Less boilerplate
- Better reactive patterns

**Estimated Time:** 1-2 weeks

#### 4.3: Improve ReportDialog with Modern Features
**Current:** Uses WebView for reports

**Enhancements:**
- Better CSS styling
- Interactive charts (if needed)
- Export improvements

**Estimated Time:** 1 week

---

### Priority 5: Advanced Kotlin Features (LOW PRIORITY)

**Tasks:**

#### 5.1: Kotlin Coroutines for Async Operations
**Current:** Uses JavaFX `Task` for background work

**Target:** Use Kotlin coroutines

```kotlin
// Current (JavaFX Task)
val task = Task<Void>() { /* ... */ }
new Thread(task).start()

// Future (Kotlin Coroutines)
lifecycleScope.launch {
    val result = withContext(Dispatchers.IO) { /* ... */ }
    // Update UI on main thread
}
```

**Benefits:**
- More readable async code
- Better error handling
- Cancellation support

**Estimated Time:** 2-3 weeks  
**Dependencies:** Requires coroutines library

#### 5.2: Sealed Classes for State Management
**Use Cases:**
- Document state (New, Editing, Saved, Error)
- Report generation state
- Import/export state

**Benefits:**
- Type-safe state management
- Exhaustive when expressions
- Better compiler checks

**Estimated Time:** 1-2 weeks

#### 5.3: Type-Safe Builders for Complex UIs
**Use Cases:**
- Dialog construction
- Report layout
- Form building

**Benefits:**
- More readable UI code
- Compile-time safety
- Less boilerplate

**Estimated Time:** 2-3 weeks

---

### Priority 6: Architecture Improvements (LONG TERM)

**Tasks:**

#### 6.1: MVVM Pattern
**Current:** MVC-like pattern

**Target:** Adopt MVVM for better separation

**Benefits:**
- Better testability
- Clearer separation of concerns
- Easier to maintain

**Estimated Time:** 6-8 weeks  
**Impact:** High (architectural change)

#### 6.2: Dependency Injection
**Current:** Manual dependency management

**Target:** Use Koin or similar DI framework

**Benefits:**
- Easier testing
- Better modularity
- Cleaner code

**Estimated Time:** 2-3 weeks

#### 6.3: Compose Desktop (FUTURE EXPLORATION)
**Current:** JavaFX FXML + code

**Target:** Explore Jetpack Compose Desktop

**Considerations:**
- Major rewrite required
- Learning curve
- Ecosystem maturity

**Estimated Time:** 3-6 months (if pursued)  
**Priority:** Very Low (exploratory)

---

## 📊 Modernization Metrics

### Current State:
- **Java Code:** ~241 files, ~50,000+ lines
- **Kotlin Code:** ~48 files, ~7.8% of codebase
- **Test Coverage:** <1% (1 test file)
- **Legacy Code:** ~40 unused files

### Target State (6 months):
- **Kotlin Code:** ~40-50% of codebase
- **Test Coverage:** ~30-40% (critical paths)
- **Legacy Code:** Removed
- **Modern Patterns:** Coroutines, sealed classes, DI

---

## 🎯 Recommended Next Steps

### Immediate (Next 2-4 weeks):
1. ✅ **Set up testing infrastructure** (Priority 1)
2. ✅ **Write tests for DAO layer** (Priority 1)
3. ✅ **Remove legacy Swing dialogs** (Priority 3.1)

### Short Term (1-3 months):
1. ✅ **Migrate 4-6 simple dialogs to Kotlin** (Priority 2.1)
2. ✅ **Create Kotlin BaseDialog pattern** (Priority 2.2)
3. ✅ **Remove legacy Swing UI classes** (Priority 3.2)
4. ✅ **Add tests for business logic** (Priority 1)

### Medium Term (3-6 months):
1. ✅ **Migrate more dialogs to Kotlin** (Priority 2.1)
2. ✅ **Extract MainController handlers** (Priority 2.3)
3. ✅ **Migrate PrintPreviewFrame** (Priority 4.1)
4. ✅ **Add coroutines for async** (Priority 5.1)

### Long Term (6+ months):
1. ✅ **MVVM refactoring** (Priority 6.1)
2. ✅ **Dependency injection** (Priority 6.2)
3. ✅ **Explore Compose Desktop** (Priority 6.3)

---

## 💡 Quick Wins (Can Do Now)

These are low-risk, high-value improvements:

1. **Remove unused Swing dialogs** (1 day)
   - ✅ Confirmed unused
   - ✅ Low risk
   - ✅ Reduces codebase size

2. **Add JUnit 5 setup** (1 day)
   - ✅ Foundation for testing
   - ✅ Enables safe refactoring

3. **Migrate 1-2 simple dialogs** (1 week)
   - ✅ AboutDialogFX
   - ✅ HelpDialogFX
   - ✅ Low complexity
   - ✅ Good learning

4. **Add DAO tests** (1 week)
   - ✅ Critical business logic
   - ✅ High value
   - ✅ Relatively easy

---

## 📈 Success Metrics

Track modernization progress:

- **Code Quality:**
  - Test coverage: 0% → 30%+
  - Kotlin percentage: 7.8% → 40%+
  - Code duplication: Reduce by 20%+

- **Maintainability:**
  - Average class size: Reduce by 30%
  - Cyclomatic complexity: Reduce by 25%
  - Legacy code: Remove 100%

- **Developer Experience:**
  - Build time: Keep <10s
  - Test execution: <30s
  - Code review time: Reduce by 20%

---

## 🚀 Getting Started

### Week 1: Foundation
1. Set up JUnit 5 + TestFX
2. Create test structure
3. Write 5-10 DAO tests

### Week 2: Quick Wins
1. Remove unused Swing dialogs
2. Migrate AboutDialogFX to Kotlin
3. Add more tests

### Week 3-4: Build Momentum
1. Migrate 2-3 more dialogs
2. Create BaseDialog pattern
3. Expand test coverage

---

**Remember:** Modernization is a journey, not a destination. Focus on incremental improvements that add value.
