# Kotlin Modernization Project

## Overview
This document describes the Kotlin migration strategy for Tilitin 2.1, focusing on modernizing the codebase while maintaining full Java interoperability.

## Status
**Phase 1: Foundation - COMPLETED ✓**
**Phase 2: Model Classes - COMPLETED ✓**
**Phase 2.5: DAO Foundation - IN PROGRESS 🔄**
**Phase 3: DAO Migration - PLANNED 📋**

## What Has Been Done

### 1. Maven Configuration

- Upgraded to Kotlin 2.3.0 (from 2.1.10) to support Java 25
- Configured kotlin-maven-plugin with Java 25 target (jvmTarget=25)
- Added kotlin-stdlib dependency (version 2.3.0)
- Configured mixed Java/Kotlin compilation
- Maven compiler release set to 25

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

- Java 25 (Eclipse Adoptium JDK)
- Maven 3.9+
- Kotlin 2.3.0 (auto-downloaded by Maven)

### Build Commands
```bash
# Clean compile
mvn clean compile

# Build JAR with dependencies
mvn package

# Run application
java -jar target/tilitin-2.1.1.jar
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

### Phase 3: DAO Migration (PLANNED 📋)

- [ ] Migrate SQLAccountDAO to Kotlin
- [ ] Migrate SQLEntryDAO to Kotlin
- [ ] Migrate SQLDocumentDAO to Kotlin
- [x] Create DatabaseExtensions.kt for ResultSet mapping
- [x] Create SQLAccountDAOKt abstract base class
- [x] Create SQLiteAccountDAOKt implementation
- [ ] Update UI to use Kotlin models

### Phase 2.5: DAO Foundation (IN PROGRESS 🔄)

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
- 83 lines vs 91 Java lines (9% reduction)

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
- [pom.xml](pom.xml) - Added Kotlin configuration
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
