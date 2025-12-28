# Kotlin Modernization Project

## Overview
This document describes the Kotlin migration strategy for Tilitin 2.1, focusing on modernizing the codebase while maintaining full Java interoperability.

## Status
**Phase 1: Foundation - COMPLETED ✓**

## What Has Been Done

### 1. Maven Configuration
- Added Kotlin 2.1.10 support to [pom.xml](pom.xml)
- Configured kotlin-maven-plugin with Java 21 target
- Added kotlin-stdlib dependency
- Configured mixed Java/Kotlin compilation

### 2. Directory Structure
Created new Kotlin source directory:
```
src/main/kotlin/kirjanpito/ui/
```

### 3. Kotlin Utility Classes
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
- Kotlin 2.1.10 (auto-downloaded by Maven)

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

### Phase 1: Foundation (COMPLETED)
- ✓ Configure Maven for Kotlin
- ✓ Create utility classes
- ✓ Verify build pipeline
- ✓ Test Java-Kotlin interoperability

### Phase 2: Incremental Refactoring (NEXT)
Target for modernization:
1. **DocumentFrame** (3262 lines) - Break down into smaller Kotlin classes
   - Extract UI builders
   - Extract business logic
   - Extract event handlers

2. **Dialog classes** - Convert to Kotlin data classes where appropriate

3. **Model classes** - Leverage Kotlin's data classes and null-safety

### Phase 3: Advanced Features
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
