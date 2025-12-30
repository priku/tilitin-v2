# Kotlin Modernization Project

## Overview
This document describes the Kotlin migration strategy for Tilitin 2.1, focusing on modernizing the codebase while maintaining full Java interoperability.

## Status
**Phase 1: Foundation - COMPLETED ✓**
**Phase 2: Model Classes - COMPLETED ✓**
**Phase 2.5: DAO Foundation - COMPLETED ✓**
**Phase 3: AccountDAO Migration - COMPLETED ✓**
**Phase 4: All SQLite DAO Migration - COMPLETED ✓**
**Phase 5: Cleanup & Session Interface - COMPLETED ✓**

## What Has Been Done

### 1. Gradle Configuration

- Upgraded to Kotlin 2.1.0 with Java 21 toolchain
- Configured Kotlin plugin with Java 21 target (jvmTarget=21)
- Added kotlin-stdlib dependency
- Configured mixed Java/Kotlin compilation
- Compose Desktop support enabled

### 2. Directory Structure

Created Kotlin source directories:

```
src/main/kotlin/kirjanpito/
├── db/       # Data model classes (Phase 2)
└── ui/       # UI utilities (Phase 1)
```

### 3. Model Classes (Phase 2 - COMPLETED ✓)

Converted core data models to Kotlin data classes:

#### [Account.kt](src/main/kotlin/kirjanpito/db/Account.kt)
- `AccountData` - Tilin tiedot
- Companion object with type constants (TYPE_ASSET, TYPE_LIABILITY, etc.)
- Helper methods: `isBalanceSheetAccount()`, `hasVat()`, `displayName()`

#### [Document.kt](src/main/kotlin/kirjanpito/db/Document.kt)
- `DocumentData` - Tositteen tiedot
- Simplified from 93 lines Java to ~35 lines Kotlin

#### [Entry.kt](src/main/kotlin/kirjanpito/db/Entry.kt)
- `EntryData` - Viennin tiedot
- Flag handling with `getFlag()`, `setFlag()`
- Helper methods: `signedAmount()`, `isEmpty()`, `isValid()`

#### [Period.kt](src/main/kotlin/kirjanpito/db/Period.kt)
- `PeriodData` - Tilikauden tiedot
- Helper methods: `containsDate()`, `durationInDays()`

#### [DocumentType.kt](src/main/kotlin/kirjanpito/db/DocumentType.kt)
- `DocumentTypeData` - Tositelajin tiedot
- Helper methods: `isInRange()`, `nextNumber()`

#### [COAHeading.kt](src/main/kotlin/kirjanpito/db/COAHeading.kt)
- `COAHeadingData` - Tilikartan väliotsikon tiedot
- Helper method: `indentedText()`

### 4. Kotlin Utility Classes

Created three modern utility modules in Kotlin:

#### [SwingExtensions.kt](src/main/kotlin/kirjanpito/ui/SwingExtensions.kt)
Modern Swing helper functions:
- `gridBagConstraints()` - Simplified GridBagConstraints creation
- `Container.addWithConstraints()` - One-line component adding
- `insets()` - Easy margin creation
- `Component.showError/Info/Confirmation()` - Dialog helpers
- `panel()` and `button()` - Type-safe builders
- Extension properties for safer text access

#### [ValidationUtils.kt](src/main/kotlin/kirjanpito/ui/ValidationUtils.kt)
Null-safe validation utilities:
- String validation with null-safety
- BigDecimal validation helpers
- Date parsing utilities
- Extension functions with descriptive error messages

#### [DialogUtils.kt](src/main/kotlin/kirjanpito/ui/DialogUtils.kt)
Dialog and threading utilities:
- File chooser helpers
- EDT (Event Dispatch Thread) utilities
- Window management
- File filter creation

## Build Configuration

### Prerequisites

- Java 21+ (Eclipse Adoptium JDK)
- Gradle 8.11+ (wrapper included)
- Kotlin 2.1.0 (auto-downloaded by Gradle)

### Build Commands
```bash
# Clean compile
./gradlew clean compileJava compileKotlin

# Build JAR with dependencies
./gradlew jar

# Run application
./gradlew run
# or
java -jar build/libs/tilitin-2.2.3.jar
```

## Migration Strategy

### Phase 1: Foundation (COMPLETED ✓)
- ✓ Configure Maven for Kotlin
- ✓ Create utility classes
- ✓ Verify build pipeline
- ✓ Test Java-Kotlin interoperability

### Phase 2: Model Classes (COMPLETED ✓)

- ✓ Create Kotlin data classes for db package
- ✓ AccountData, DocumentData, EntryData, PeriodData
- ✓ DocumentTypeData, COAHeadingData
- ✓ All model classes converted to Kotlin
- ✓ Helper methods and validation added

### Phase 3: AccountDAO Migration (COMPLETED ✓)

- [x] Create DatabaseExtensions.kt for ResultSet mapping
- [x] Create SQLAccountDAOKt abstract base class
- [x] Create SQLiteAccountDAOKt implementation
- [x] Integrate SQLiteAccountDAOKt into SQLiteDataSource

### Phase 4: All SQLite DAO Migration (COMPLETED ✓)

**Migrated DAOs:**
- [x] SQLAccountDAOKt / SQLiteAccountDAOKt
- [x] SQLEntryDAOKt / SQLiteEntryDAOKt
- [x] SQLDocumentDAOKt / SQLiteDocumentDAOKt
- [x] SQLPeriodDAOKt / SQLitePeriodDAOKt
- [x] SQLDocumentTypeDAOKt / SQLiteDocumentTypeDAOKt
- [x] SQLCOAHeadingDAOKt / SQLiteCOAHeadingDAOKt
- [x] SQLSettingsDAOKt / SQLiteSettingsDAOKt
- [x] SQLReportStructureDAOKt / SQLiteReportStructureDAOKt
- [x] SQLEntryTemplateDAOKt / SQLiteEntryTemplateDAOKt
- [x] SQLiteAttachmentDAO (already in Kotlin)

**Total: 10 DAO implementations migrated to Kotlin**

### Phase 5: Cleanup & Session Interface (COMPLETED ✓)

- [x] Remove all old Java DAO fallback classes (9 files deleted)
- [x] Update all DAOs to use Session interface instead of SQLiteSession type
- [x] Create Session extension properties (insertId, prepareStatement)
- [x] Migrate SQLiteDataSource to Kotlin (SQLiteDataSourceKt)
- [x] Migrate SQLiteSession to Kotlin (SQLiteSessionKt)
- [x] Migrate DataSourceFactory to Kotlin (DataSourceFactoryKt)
- [x] Update SQLiteDataSource.java to use Kotlin DAOs without casting

### Phase 2.5: DAO Foundation (COMPLETED ✓)

Created database extension utilities and Kotlin DAO base classes:

#### [DatabaseExtensions.kt](src/main/kotlin/kirjanpito/db/DatabaseExtensions.kt)
ResultSet and PreparedStatement extension functions:
- `ResultSet.getIntOrNull()`, `getIntOrMinusOne()` - Null-safe int getters
- `ResultSet.getStringOrEmpty()`, `getBigDecimalOrZero()` - Safe defaults
- `ResultSet.toAccountData()`, `toAccountDataSQLite()` - Row mapping
- `PreparedStatement.setIntOrNull()`, `setAccountValues()` - Parameter setting
- `ResultSet.mapToList()` - Iterate and map results
- `withDataAccess()` - SQLException to DataAccessException wrapper
- Java ↔ Kotlin conversion: `Account.toAccountData()`, `AccountData.toAccount()`
- **Session extensions**: `Session.insertId`, `Session.prepareStatement()` - Works with both Java and Kotlin Session implementations

#### [SQLAccountDAOKt.kt](src/main/kotlin/kirjanpito/db/sql/SQLAccountDAOKt.kt)
Abstract Kotlin base class for Account DAO:
- Implements `AccountDAO` interface
- Uses `withDataAccess {}` for exception handling
- Uses Kotlin `use {}` for auto-closing resources
- 166 lines vs 225 Java lines (26% reduction)

#### [SQLiteAccountDAOKt.kt](src/main/kotlin/kirjanpito/db/sqlite/SQLiteAccountDAOKt.kt)
SQLite-specific implementation:
- Multi-line SQL queries as companion constants
- Override for SQLite-specific vatRate handling (TEXT vs DECIMAL)
- Uses Session interface with extension properties
- 83 lines vs 91 Java lines (9% reduction)

### Phase 4: Complete DAO Migration (COMPLETED ✓)

All SQLite DAO implementations have been migrated to Kotlin:

#### Abstract Base Classes (in `src/main/kotlin/kirjanpito/db/sql/`)
- `SQLAccountDAOKt.kt`
- `SQLEntryDAOKt.kt`
- `SQLDocumentDAOKt.kt`
- `SQLPeriodDAOKt.kt`
- `SQLDocumentTypeDAOKt.kt`
- `SQLCOAHeadingDAOKt.kt`
- `SQLSettingsDAOKt.kt`
- `SQLReportStructureDAOKt.kt`
- `SQLEntryTemplateDAOKt.kt`

#### SQLite Implementations (in `src/main/kotlin/kirjanpito/db/sqlite/`)
- `SQLiteAccountDAOKt.kt`
- `SQLiteEntryDAOKt.kt`
- `SQLiteDocumentDAOKt.kt`
- `SQLitePeriodDAOKt.kt`
- `SQLiteDocumentTypeDAOKt.kt`
- `SQLiteCOAHeadingDAOKt.kt`
- `SQLiteSettingsDAOKt.kt`
- `SQLiteReportStructureDAOKt.kt`
- `SQLiteEntryTemplateDAOKt.kt`
- `SQLiteAttachmentDAO.kt` (already in Kotlin)

**Key improvements:**
- All DAOs use Session interface (not SQLiteSession type) - enables both Java and Kotlin Session implementations
- Extension properties for Session (`insertId`, `prepareStatement`)
- Null-safe operations throughout
- Resource management with Kotlin `use {}`
- Exception handling with `withDataAccess {}`

### Phase 5: DataSource & Factory Migration (COMPLETED ✓)

#### [SQLiteDataSourceKt.kt](src/main/kotlin/kirjanpito/db/sqlite/SQLiteDataSourceKt.kt)
- Complete Kotlin implementation of SQLiteDataSource
- Uses SQLiteSessionKt for new sessions
- All DAO methods return Kotlin implementations
- Cleaner code with Kotlin idioms

#### [SQLiteSessionKt.kt](src/main/kotlin/kirjanpito/db/sqlite/SQLiteSessionKt.kt)
- Kotlin implementation of SQLiteSession
- Compatible with Session interface
- Works with extension properties

#### [DataSourceFactoryKt.kt](src/main/kotlin/kirjanpito/db/DataSourceFactoryKt.kt)
- Kotlin object for DataSource creation
- Uses `@JvmStatic` for Java compatibility
- Delegates from Java DataSourceFactory

**Cleanup:**
- Removed 9 old Java DAO fallback files
- Updated SQLiteDataSource.java to use Kotlin DAOs directly
- Removed all "Legacy Java implementation" comments

### Phase 4: Dialog Refactoring (FUTURE)

- [ ] Create BaseDialog abstract class in Kotlin
- [ ] Convert simple dialogs to Kotlin
- [ ] Implement common dialog patterns

### Phase 5: DocumentFrame Refactoring (FUTURE)

1. **DocumentFrame** (3262 lines) - Break down into smaller Kotlin classes
   - [ ] Extract menu builders
   - [ ] Extract toolbar builders
   - [ ] Extract event handlers
   - [ ] Extract table management

### Phase 6: Advanced Features (FUTURE)

- Use Kotlin coroutines for background tasks
- Implement type-safe builders for complex UIs
- Leverage sealed classes for state management

## Benefits of Kotlin

### Code Reduction
- Data classes eliminate boilerplate
- Extension functions reduce utility class clutter
- Smart casts eliminate redundant casts

### Safety
- Null-safety prevents NullPointerExceptions
- Type inference reduces verbosity
- Immutability by default

### Interoperability
- 100% compatible with existing Java code
- Gradual migration possible
- Java code can call Kotlin seamlessly

## Example Usage

### Before (Java)
```java
GridBagConstraints gbc = new GridBagConstraints();
gbc.gridx = 0;
gbc.gridy = 0;
gbc.weightx = 1.0;
gbc.fill = GridBagConstraints.HORIZONTAL;
gbc.insets = new Insets(5, 5, 5, 5);
panel.add(component, gbc);
```

### After (Kotlin)
```kotlin
panel.addWithConstraints(
    component,
    gridBagConstraints(
        gridx = 0,
        gridy = 0,
        weightx = 1.0,
        fill = GridBagConstraints.HORIZONTAL,
        insets = insets(5)
    )
)
```

## Files Modified
- [build.gradle.kts](build.gradle.kts) - Gradle/Kotlin configuration
- Created [src/main/kotlin/kirjanpito/ui/SwingExtensions.kt](src/main/kotlin/kirjanpito/ui/SwingExtensions.kt)
- Created [src/main/kotlin/kirjanpito/ui/ValidationUtils.kt](src/main/kotlin/kirjanpito/ui/ValidationUtils.kt)
- Created [src/main/kotlin/kirjanpito/ui/DialogUtils.kt](src/main/kotlin/kirjanpito/ui/DialogUtils.kt)

## Next Steps
1. Identify first Java class to refactor to Kotlin
2. Create Kotlin equivalent maintaining API compatibility
3. Update references gradually
4. Delete old Java code when safe

## Notes
- Kotlin code compiles to Java bytecode - no runtime performance difference
- All existing Java code continues to work
- Build time slightly increased due to Kotlin compilation
- IDE support: IntelliJ IDEA recommended, VS Code with Kotlin extension also works
