# JavaFX Migration Plan

## ✅ Päätös

| Valinta | Perustelu |
|---------|-----------|
| **Kieli:** Java | Projekti 84.5% Java |
| **UI:** JavaFX | Paras TableView, paras macOS-tuki |
| **Cross-platform:** ✅ | Windows, macOS, Linux |

---

## 📋 Valmius aloittamiseen

### ✅ Jo valmiina:

| Komponentti | Status |
|-------------|--------|
| Java 21 | ✅ Asennettu |
| JavaFX plugin | ✅ `org.openjfx.javafxplugin` v0.1.0 |
| JavaFX moduulit | ✅ `controls`, `fxml`, `swing` |
| jpackage | ✅ GitHub Actions valmiina |
| Manager-luokat | ✅ Refaktoroitu, UI-agnostiset |

### Gradle-konfiguraatio (jo olemassa):

```kotlin
// build.gradle.kts
plugins {
    id("org.openjfx.javafxplugin") version "0.1.0"
}

javafx {
    version = "21"
    modules = listOf("javafx.controls", "javafx.fxml", "javafx.swing")
}
```

---

## 🚀 Migraatiosuunnitelma

### Vaihe 1: Prototyyppi (1-2 päivää)

**Tavoite:** Testaa JavaFX toimii ja manager-luokat integroituvat.

```
src/main/java/kirjanpito/ui/javafx/
├── JavaFXApp.java          # Application bootstrap
├── MainController.java     # FXML controller
└── MainView.fxml           # FXML layout
```

**Tehtävät:**
- [ ] Luo `JavaFXApp.java` - Application-luokka
- [ ] Luo yksinkertainen `MainView.fxml` - BorderPane + MenuBar
- [ ] Testaa macOS menu bar (`useSystemMenuBar`)
- [ ] Testaa manager-integraatio (DocumentNavigator)

### Vaihe 2: Entry-taulukko (3-5 päivää)

**Tavoite:** Toteuta kirjanpidon ydin - Entry TableView.

```java
TableView<Entry> entryTable = new TableView<>();
entryTable.getColumns().addAll(
    new TableColumn<>("Nro"),
    new TableColumn<>("Tili"),
    new TableColumn<>("Debet"),
    new TableColumn<>("Kredit")
);
```

**Tehtävät:**
- [ ] Luo `EntryTableView.java`
- [ ] Implementoi solueditorit (Account, Currency, Date)
- [ ] Testaa editointi ja navigaatio
- [ ] Integroi `DocumentEntryManager`

### Vaihe 3: Pääikkuna (1 viikko)

**Tavoite:** Korvaa DocumentFrame JavaFX-versiolla.

```
DocumentFrameFX.java
├── MenuBar (useSystemMenuBar)
├── ToolBar
├── Document fields (number, date)
├── EntryTableView
├── TotalRow
├── SearchBar
└── StatusBar
```

### Vaihe 4: Dialogit (2-3 viikkoa)

**Prioriteettijärjestys:**
1. AboutDialog
2. AccountSelectionDialog
3. SettingsDialog
4. PropertiesDialog
5. COADialog
6. Muut (21 kpl)

### Vaihe 5: Tulostus & Raportit (1 viikko)

- [ ] PrintPreview JavaFX-versio
- [ ] PDF-generointi

---

## 📁 Tiedostorakenne

```
src/main/java/kirjanpito/ui/javafx/
├── JavaFXApp.java              # Main application
├── controllers/
│   ├── MainController.java
│   ├── EntryTableController.java
│   └── dialogs/
│       ├── AboutController.java
│       ├── SettingsController.java
│       └── ...
├── views/
│   ├── MainView.fxml
│   ├── EntryTable.fxml
│   └── dialogs/
│       ├── About.fxml
│       └── ...
└── css/
    ├── light-theme.css
    └── dark-theme.css
```

---

## ⏱️ Aikataulu

| Vaihe | Kesto | Kumulatiivinen |
|-------|-------|----------------|
| 1. Prototyyppi | 1-2 pv | 2 pv |
| 2. Entry-taulukko | 3-5 pv | 1 vko |
| 3. Pääikkuna | 5-7 pv | 2 vko |
| 4. Dialogit | 2-3 vko | 4-5 vko |
| 5. Tulostus | 5-7 pv | 5-6 vko |
| 6. Testaus | 1 vko | 6-7 vko |
| **Yhteensä** | | **6-7 viikkoa** |

---

## 🎯 Seuraava askel

**Aloita Vaihe 1:** Luo JavaFX-prototyyppi

```bash
# Testaa JavaFX toimii
./gradlew runJavaFXTest
```

---

**Luotu:** 2025-12-31
**Status:** ✅ VALMIS ALOITETTAVAKSI
