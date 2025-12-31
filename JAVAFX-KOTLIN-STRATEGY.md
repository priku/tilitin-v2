# JavaFX + Kotlin -strategia

**Päivitetty:** 2025-12-31  
**Status:** ✅ Päätös: Tehdään JavaFX suoraan Kotlinilla

---

## 🎯 Päätös

### Valittu teknologia: **Kotlin + JavaFX**

**Miksi Kotlin?**
- ✅ DAO:t ovat jo Kotliniin (100% SQLite DAO:t)
- ✅ Kotlin on modernimpi ja tiiviimpi
- ✅ JavaFX toimii täysin Kotlinilla
- ✅ Null-safety vähentää virheitä
- ✅ Extension functions helpottavat koodia
- ✅ Data classes vähentävät boilerplatea

**Miksi ei Java?**
- ❌ Ei teknistä syytä - JavaFX toimii molemmilla
- ❌ Java on verbosimpi
- ❌ Ei null-safetya
- ❌ Enemmän boilerplatea

---

## 📋 Strategia

### 1. **Uusi JavaFX-UI: Kotlin** ✅
```kotlin
// MainController.kt
class MainController : Initializable {
    @FXML private lateinit var entryTable: TableView<EntryRowModel>
    // ...
}
```

**Edut:**
- Tiiviimpi syntaksi
- Null-safety
- Extension functions
- Data classes
- Parempi tyyppipäättely

### 2. **Manager-luokat: Java (olemassa olevat)** ✅
```java
// DocumentMenuHandler.java - pysyy Javana
// DocumentNavigator.java - pysyy Javana
// DocumentEntryManager.java - pysyy Javana
```

**Miksi?**
- Ne ovat jo olemassa ja toimivat
- UI-agnostisia, ei tarvitse muuttaa
- Kotlin kutsuu Javaa sujuvasti

### 3. **Dialogit: Kotlin** ✅
```kotlin
// StartingBalanceDialogFX.kt
class StartingBalanceDialogFX(
    owner: Window,
    model: StartingBalanceModel
) {
    // ...
}
```

**Edut:**
- Tiiviimpi koodi
- Parempi null-handling
- Extension functions helpottavat

### 4. **DAO:t: Kotlin (jo valmiina)** ✅
```kotlin
// SQLiteAccountDAOKt.kt - jo Kotliniin
// SQLiteEntryDAOKt.kt - jo Kotliniin
// ...
```

---

## 🔄 Migraatiotilanne

### Nykyinen tila:
```
Java-tiedostoja:   202 (84.5%)
Kotlin-tiedostoja:  37 (15.5%)
```

### Tavoite (JavaFX-migraation jälkeen):
```
Java-tiedostoja:   ~150 (Manager-luokat, legacy Swing)
Kotlin-tiedostoja: ~100 (JavaFX-UI, DAO:t)
Kotlin-osuus:      ~40%
```

### Lopullinen tavoite (valinnainen):
```
Java-tiedostoja:   ~50 (vain legacy Swing, jos säilytetään)
Kotlin-tiedostoja: ~200 (JavaFX-UI, DAO:t, utilities)
Kotlin-osuus:      ~80%
```

---

## 📝 Toteutussuunnitelma

### Vaihe 1: Muunna nykyinen JavaFX-koodi Kotliniin

**Tehtävät:**
- [ ] `MainController.java` → `MainController.kt`
- [ ] `EntryRowModel.java` → `EntryRowModel.kt`
- [ ] `StartingBalanceDialogFX.java` → `StartingBalanceDialogFX.kt`
- [ ] `EntryTemplateDialogFX.java` → `EntryTemplateDialogFX.kt`
- [ ] `StartingBalanceRowModel.java` → `StartingBalanceRowModel.kt`
- [ ] Custom cells → Kotlin

**Arvio:** 2-3 tuntia (automaattinen konversio + korjaukset)

### Vaihe 2: Uudet dialogit Kotlinilla

**Tehtävät:**
- [ ] Kaikki uudet dialogit tehdään Kotlinilla
- [ ] Käytetään Kotlin-ominaisuuksia (data classes, extension functions)

**Arvio:** Sama kuin Javalla, mutta tiiviimpää koodia

### Vaihe 3: Manager-luokat (valinnainen)

**Tehtävät:**
- [ ] Voimme konvertoida manager-luokat Kotliniin myöhemmin
- [ ] Ei prioriteetti - ne toimivat Javana

---

## 💡 Esimerkkejä Kotlin vs Java

### 1. EntryRowModel

**Java:**
```java
public class EntryRowModel {
    private final IntegerProperty rowNumber;
    private final ObjectProperty<Entry> entry;
    // ... 50+ riviä boilerplatea
}
```

**Kotlin:**
```kotlin
class EntryRowModel(
    rowNumber: Int,
    entry: Entry?,
    account: Account?,
    format: DecimalFormat
) {
    val rowNumberProperty = SimpleIntegerProperty(rowNumber)
    val entryProperty = SimpleObjectProperty(entry)
    // ... 30 riviä, tiiviimpää
}
```

### 2. Dialog

**Java:**
```java
private void showError(String message) {
    Alert alert = new Alert(Alert.AlertType.ERROR);
    alert.setTitle("Virhe");
    alert.setHeaderText(null);
    alert.setContentText(message);
    alert.showAndWait();
}
```

**Kotlin:**
```kotlin
private fun showError(message: String) {
    Alert(Alert.AlertType.ERROR).apply {
        title = "Virhe"
        headerText = null
        contentText = message
    }.showAndWait()
}
```

### 3. Null-safety

**Java:**
```java
if (dataSource != null && registry != null) {
    // ...
}
```

**Kotlin:**
```kotlin
dataSource?.let { ds ->
    registry?.let { reg ->
        // ...
    }
}
```

---

## ✅ Yhteenveto

**Tehdään:**
1. ✅ JavaFX-UI Kotlinilla (nyt)
2. ✅ Manager-luokat Javaa (olemassa olevat)
3. ✅ DAO:t Kotliniin (jo valmiina)

**Etuja:**
- Tiiviimpi koodi
- Null-safety
- Modernimpi syntaksi
- Parempi tyyppipäättely
- Extension functions

**Ei haittoja:**
- JavaFX toimii täysin Kotlinilla
- Kotlin kutsuu Javaa sujuvasti
- Manager-luokat pysyvät Javana

---

**Dokumentin luoja:** AI Assistant  
**Hyväksyjä:** _odottaa_
