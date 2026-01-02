# Coroutine-toteutukset

**Päivämäärä:** 2026-01-02

---

## Toteutetut async-operaatiot

### 1. ✅ loadDocument() - Yksittäisen tositteen lataus

**Tiedosto:** `src/main/kotlin/kirjanpito/ui/javafx/DocumentAsyncOperations.kt`

**Metodi:** `loadDocumentAsync()`

**Käyttö:**
```java
DocumentAsyncOperations.INSTANCE.loadDocumentAsync(
    dataSource,
    doc,
    (List<Entry> entries) -> {
        // UI-päivitykset JavaFX-threadissa
        currentEntries = entries;
        updateUI();
    },
    (String errorMsg) -> {
        // Virheenkäsittely JavaFX-threadissa
        showError("Virhe", errorMsg);
    }
);
```

**Hyödyt:**
- UI ei jäädy tositteen latauksessa
- Näyttää status-viestin latauksen aikana
- Parantaa responsiivisuutta isolla tietokannalla

---

### 2. ✅ saveDocument() - Tositteen tallennus

**Tiedosto:** `src/main/kotlin/kirjanpito/ui/javafx/DocumentAsyncOperations.kt`

**Metodi:** `saveDocumentAsync()`

**Käyttö:**
```java
DocumentAsyncOperations.INSTANCE.saveDocumentAsync(
    dataSource,
    currentDocument,
    entriesToSave,
    entriesToDelete,
    () -> {
        // Onnistumisen jälkeen JavaFX-threadissa
        setStatus("Tallennettu");
        loadDocument(currentDocument); // Reload entries
    },
    (String errorMsg) -> {
        // Virheenkäsittely JavaFX-threadissa
        showError("Tallennusvirhe", errorMsg);
    }
);
```

**Hyödyt:**
- UI ei jäädy tallennuksessa
- Näyttää status-viestin "Tallennetaan..."
- Parantaa responsiivisuutta monien viennitien kanssa

---

### 3. ✅ refreshAccounts() - Tilien päivitys

**Tiedosto:** `src/main/kotlin/kirjanpito/ui/javafx/DocumentAsyncOperations.kt`

**Metodi:** `refreshAccountsAsync()`

**Käyttö:**
```java
DocumentAsyncOperations.INSTANCE.refreshAccountsAsync(
    dataSource,
    (List<Account> refreshedAccounts) -> {
        // UI-päivitykset JavaFX-threadissa
        accounts = refreshedAccounts;
        refreshAccountCells();
        setStatus("Tilit päivitetty");
    },
    (String errorMsg) -> {
        // Virheenkäsittely JavaFX-threadissa
        showError("Virhe", errorMsg);
    }
);
```

**Hyödyt:**
- UI ei jäädy tilien päivityksessä
- Näyttää status-viestin "Päivitetään tilejä..."
- Parantaa responsiivisuutta isolla tilikartalla

---

## Tekninen toteutus

### CoroutineUtils käyttö

- `launchDB {}` - Käynnistää coroutinen tietokanta-thread poolissa
- `withUI {}` - Vaihtaa JavaFX-threadiin UI-päivityksiä varten
- `withDB {}` - Vaihtaa tietokanta-thread pooliin (käytetään AsyncDAOExtensionsissä)

### Thread-safety

- Kaikki tietokantaoperaatiot ajetaan taustalla
- Kaikki UI-päivitykset ajetaan JavaFX-threadissa (`withUI {}`)
- Virheenkäsittely myös JavaFX-threadissa

### MainController-integraatio

**Muutetut metodit:**
1. ✅ `loadDocument()` - Nyt käyttää `loadDocumentAsync()`
2. ✅ `handleSave()` - Nyt käyttää `saveDocumentAsync()`
3. ✅ `refreshAccountsAsync()` - Uusi metodi käyttämään `refreshAccountsAsync()`

---

## Seuraavat askeleet

### Mahdollisia laajennuksia

1. **loadAllData() async-versio** (3-4h)
   - Koko tietokannan lataus taustalla
   - Näyttää progress-indikaattorin

2. **Document search async-versio** (2-3h)
   - Hakujen suorittaminen taustalla
   - Parantaa responsiivisuutta isossa tietokannassa

3. **Report generation async-versio** (3-4h)
   - Raporttien generointi taustalla
   - Näyttää progress-indikaattorin

---

## Mittarit

**Toteutetut async-operaatiot:** 3
- loadDocument ✅
- saveDocument ✅
- refreshAccounts ✅

**Parannettu responsiivisuus:**
- Tositteen lataus: Nyt async
- Tositteen tallennus: Nyt async
- Tilien päivitys: Nyt async

---

**Viimeksi päivitetty:** 2026-01-02
