# JavaFX Migration Research

## 📊 Nykyisen UI-arkkitehtuurin analyysi

### Swing-komponenttien inventaario

| Kategoria | Määrä | Tiedostot |
|-----------|-------|-----------|
| **Dialog-luokat** | 26 | AboutDialog, AccountSelectionDialog, AppearanceDialog, jne. |
| **Frame-luokat** | 3 | DocumentFrame, PrintPreviewFrame, DocumentFramePanel |
| **Panel-luokat** | 3 | AttachmentsPanel, PrintPreviewPanel, DocumentFramePanel |
| **Manager-luokat** | 5 | DocumentBackupManager, DocumentDataSourceManager, DocumentEntryManager, DocumentStateManager, DocumentTableManager |
| **Builder-luokat** | 3 | DocumentMenuBuilder, DocumentToolbarBuilder, DocumentUIBuilder |
| **Yhteensä** | **64** | UI-tiedostoa |

### Callback-rajapinnat (uudelleenkäytettävät)

Nämä rajapinnat ovat **UI-agnostisia** ja toimivat sellaisenaan JavaFX:ssä:

```
DocumentNavigator.NavigationCallbacks
DocumentValidator.ValidationCallbacks
DocumentEntryManager.EntryCallbacks
DocumentDataSourceManager.DataSourceCallbacks
DocumentStateManager.StateCallbacks
DocumentUIBuilder.UICallbacks
DocumentUIUpdater.UICallbacks
DocumentTableManager.TableCallbacks
DocumentPrinter.PrintCallbacks
AccountSelectionListener
```

### DocumentFrame-riippuvuudet

```java
public class DocumentFrame extends JFrame implements 
    AccountSelectionListener,
    DocumentBackupManager.DatabaseOpener, 
    DocumentExporter.CSVExportStarter,
    DocumentNavigator.NavigationCallbacks {
    
    // Manager-instanssit (UI-agnostiset)
    private DocumentNavigator documentNavigator;
    private DocumentValidator documentValidator;
    private DocumentEntryManager entryManager;
    private DocumentDataSourceManager dataSourceManager;
    private DocumentStateManager stateManager;
    private DocumentMenuHandler menuHandler;
    private DocumentPrinter documentPrinter;
    private DocumentExporter documentExporter;
    private DocumentBackupManager backupManager;
    
    // Swing-komponentit (vaativat konversion)
    private JTable entryTable;
    private JTextField numberTextField;
    private DateTextField dateTextField;
    // ... 68 Swing-viittausta
}
```

---

## 🔄 Migraatiostrategiat

### Strategia A: Big Bang (Koko UI kerralla)

**Kuvaus:** Korvaa kaikki 64 UI-tiedostoa JavaFX-versioilla kerralla.

| Pro | Contra |
|-----|--------|
| Puhdas lopputulos | Suuri riski |
| Ei Swing/JavaFX -sekoitusta | 4-8 viikon työ |
| Yksinkertainen arkkitehtuuri | Ei inkrementaalista testausta |

**Arvio:** ⛔ Ei suositella - liian riskialtis

---

### Strategia B: Vaiheittainen (Suositus 🏆)

**Kuvaus:** Migroi yksi näkymä kerrallaan, käytä SwingNode/JFXPanel bridgeä.

#### Vaihe 1: Infrastruktuuri (1-2 päivää)
- [ ] Lisää JavaFX-riippuvuudet build.gradle.kts
- [ ] Luo JavaFX Application bootstrap
- [ ] Testaa Swing ↔ JavaFX interop

#### Vaihe 2: Pääikkuna prototyyppi (3-5 päivää)
- [ ] Luo DocumentFrameFX.java (BorderPane)
- [ ] Luo DocumentFrame.fxml (FXML layout)
- [ ] Integroi olemassa olevat Manager-luokat
- [ ] Testaa perusnavigaatio

#### Vaihe 3: Entry Table (5-7 päivää)
- [ ] Konvertoi JTable → TableView
- [ ] Implementoi CellFactory:t (Account, Currency, Date)
- [ ] Testaa editointi ja navigaatio

#### Vaihe 4: Dialogit (2-3 viikkoa)
- [ ] Priorisoi dialogit käyttötiheyden mukaan
- [ ] Konvertoi 26 dialogia asteittain
- [ ] Käytä FXML + Controller -patternia

#### Vaihe 5: Print Preview (3-5 päivää)
- [ ] Konvertoi PrintPreviewFrame
- [ ] Testaa PDF-generointi

| Pro | Contra |
|-----|--------|
| Matala riski | Swing/JavaFX -sekoitus väliaikaisesti |
| Inkrementaalinen testaus | Monimutkaisempi arkkitehtuuri siirtymässä |
| Voidaan keskeyttää | Bridget lisäävät overheadia |

**Arvio:** ✅ Suositeltu - hallittu riski

---

### Strategia C: Parallel Development

**Kuvaus:** Kehitä JavaFX-versio rinnakkain, vaihda kun valmis.

| Pro | Contra |
|-----|--------|
| Nollariski tuotantoon | Kaksi koodipohjaa ylläpidettävänä |
| Täydellinen testaus ennen julkaisua | Duplikoitu työ |

**Arvio:** ⚠️ Mahdollinen jos resursseja riittää

---

## 🛠️ Tekninen toteutus

### Gradle-konfiguraatio

```kotlin
// build.gradle.kts - JavaFX lisäys
plugins {
    id("org.openjfx.javafxplugin") version "0.1.0"
}

javafx {
    version = "21"
    modules = listOf(
        "javafx.controls",
        "javafx.fxml",
        "javafx.swing"  // Swing interop
    )
}
```

### Swing ↔ JavaFX Interop

**Vaihtoehto 1: JFXPanel (Swing sisältää JavaFX:ää)**
```java
// Swing-ikkunassa JavaFX-sisältö
JFXPanel jfxPanel = new JFXPanel();
Platform.runLater(() -> {
    Scene scene = new Scene(new DocumentViewFX());
    jfxPanel.setScene(scene);
});
swingFrame.add(jfxPanel);
```

**Vaihtoehto 2: SwingNode (JavaFX sisältää Swingiä)**
```java
// JavaFX-ikkunassa Swing-sisältö (vanha dialogi)
SwingNode swingNode = new SwingNode();
SwingUtilities.invokeLater(() -> {
    swingNode.setContent(new OldSwingDialog());
});
javaFxPane.getChildren().add(swingNode);
```

### Manager-integraatio (ei muutoksia)

```java
// DocumentFrameFX.java
public class DocumentFrameFX extends BorderPane 
        implements NavigationCallbacks, ValidationCallbacks {
    
    // Samat managerit - EI MUUTOKSIA
    private final DocumentNavigator navigator;
    private final DocumentValidator validator;
    private final DocumentEntryManager entryManager;
    
    public DocumentFrameFX(Registry registry, DocumentModel model) {
        this.navigator = new DocumentNavigator(
            registry, searchPanel, searchField, this);
        this.validator = new DocumentValidator(
            model, tableModel, registry, this);
        this.entryManager = new DocumentEntryManager(
            model, tableModel, entryTable, registry, 
            dateTextField, formatter, this);
    }
    
    // NavigationCallbacks - sama toteutus
    @Override
    public void updateDocument() {
        // JavaFX UI päivitys
        Platform.runLater(() -> {
            numberTextField.setText(String.valueOf(doc.getNumber()));
            datePicker.setValue(doc.getDate().toLocalDate());
        });
    }
}
```

---

## 📋 Komponenttikohtainen analyysi

### JTable → TableView

| Swing (JTable) | JavaFX (TableView) | Huomiot |
|----------------|-------------------|---------|
| `TableModel` | `ObservableList` | Automaattinen päivitys |
| `TableCellRenderer` | `CellFactory` | Sama logiikka |
| `TableCellEditor` | `CellFactory` + `commitEdit()` | Hieman erilainen |
| `getSelectedRow()` | `getSelectionModel().getSelectedItem()` | Suora mapping |
| `changeSelection()` | `getSelectionModel().select()` | Suora mapping |

**Haaste:** Entry-taulukon monimutkaiset editorit (tili, valuutta, päivämäärä)

### JTextField → TextField

| Swing | JavaFX | Huomiot |
|-------|--------|---------|
| `getText()` | `getText()` | Sama |
| `setText()` | `setText()` | Sama |
| `addKeyListener()` | `setOnKeyPressed()` | Lambda-tuki |
| `requestFocusInWindow()` | `requestFocus()` | Sama |

**Haaste:** DateTextField custom-komponentti → DatePicker

### JMenu/JMenuBar → MenuBar/Menu

| Swing | JavaFX | Huomiot |
|-------|--------|---------|
| `JMenuBar` | `MenuBar` | Suora mapping |
| `JMenu` | `Menu` | Suora mapping |
| `JMenuItem` | `MenuItem` | Suora mapping |
| `addActionListener()` | `setOnAction()` | Lambda-tuki |
| `setAccelerator()` | `setAccelerator()` | Sama |

**Haaste:** Dokumenttilistener-viittaukset - helppo muuttaa

### JDialog → Dialog/Stage

| Swing | JavaFX | Huomiot |
|-------|--------|---------|
| `JDialog` | `Dialog<R>` tai `Stage` | Valinta käyttötapauksen mukaan |
| `setModal(true)` | `initModality(Modality.APPLICATION_MODAL)` | Sama toiminto |
| `setVisible(true)` | `showAndWait()` | Blocking-käyttäytyminen |
| `dispose()` | `close()` | Sama |

---

## ⏱️ Aikataulu-arvio

### Vaiheittainen migraatio (Strategia B)

| Vaihe | Kesto | Kumulatiivinen |
|-------|-------|----------------|
| 1. Infrastruktuuri | 1-2 pv | 2 pv |
| 2. Pääikkuna prototyyppi | 3-5 pv | 1 vko |
| 3. Entry Table | 5-7 pv | 2 vko |
| 4. Dialogit (26 kpl) | 2-3 vko | 4-5 vko |
| 5. Print Preview | 3-5 pv | 5-6 vko |
| 6. Testaus & Polish | 1 vko | 6-7 vko |
| **Yhteensä** | | **6-7 viikkoa** |

### Resurssit per dialogi

| Dialogin kompleksisuus | Arvio |
|------------------------|-------|
| Yksinkertainen (About, Settings) | 2-4 h |
| Keskitaso (AccountSelection, Properties) | 4-8 h |
| Monimutkainen (COA, ReportEditor) | 1-2 pv |

---

## 🎯 Prioriteettijärjestys

### Kriittinen polku (tee ensin)

1. **DocumentFrameFX** - Pääikkuna
2. **EntryTableViewFX** - Vientitaulukko
3. **AccountSelectionDialogFX** - Käytetään jatkuvasti
4. **PrintPreviewFrameFX** - Tulostus

### Sekundäärinen (myöhemmin)

5. SettingsDialogFX
6. PropertiesDialogFX
7. COADialogFX
8. Muut dialogit...

---

## ❓ Avoimet kysymykset

1. **Compose Desktop vs. JavaFX?**
   - Projektissa on jo Compose Desktop -riippuvuudet
   - Pitäisikö siirtyä Compose Desktopiin JavaFX:n sijaan?
   - Compose Desktop on modernimpi mutta Kotlin-pohjainen

2. **FlatLaf JavaFX:ssä?**
   - FlatLaf on Swing-spesifinen
   - JavaFX käyttää CSS-teemoja
   - Tarvitaan uusi teema (esim. JMetro, BootstrapFX)

3. **Testausstrategia?**
   - Miten varmistetaan toiminnallisuus migraation aikana?
   - Automatisoidut UI-testit (TestFX)?

4. **Scene Builder vai käsin?**
   - Scene Builder nopeuttaa layout-työtä
   - Käsin kirjoitettu FXML antaa paremman kontrollin

---

## 📚 Resurssit

### Dokumentaatio
- [OpenJFX Documentation](https://openjfx.io/openjfx-docs/)
- [JavaFX CSS Reference](https://openjfx.io/javadoc/21/javafx.graphics/javafx/scene/doc-files/cssref.html)
- [FXML Tutorial](https://openjfx.io/openjfx-docs/#FXML)

### Työkalut
- [Scene Builder](https://gluonhq.com/products/scene-builder/) - Visuaalinen FXML-editori
- [JMetro Theme](https://github.com/JFXtras/jfxtras-styles) - Moderni JavaFX-teema
- [TestFX](https://github.com/TestFX/TestFX) - JavaFX UI-testaus

### Esimerkkiprojektit
- [JFoenix](https://github.com/sshahine/JFoenix) - Material Design JavaFX
- [ControlsFX](https://github.com/controlsfx/controlsfx) - Lisäkomponentteja

---

## 🏁 Johtopäätökset

### Suositus

**Strategia B (Vaiheittainen migraatio)** on suositeltavin lähestymistapa:

1. ✅ Matala riski - voidaan keskeyttää milloin tahansa
2. ✅ Manager-luokat toimivat sellaisenaan
3. ✅ Inkrementaalinen testaus
4. ✅ Realistinen aikataulu (6-7 viikkoa)

### Seuraavat askeleet

1. **Päätä:** JavaFX vai Compose Desktop?
2. **Asenna:** Scene Builder
3. **Prototyyppi:** Luo yksinkertainen DocumentFrameFX
4. **Testaa:** Varmista manager-integraatio toimii
5. **Iteroi:** Migroi dialogit prioriteettijärjestyksessä

---

## 🆚 JavaFX vs Compose Desktop - Vertailu

### Projektissa on jo Compose Desktop!

Löysin olemassa olevan prototyypin: `src/main/kotlin/kirjanpito/ui/compose/TilitinApp.kt`

```kotlin
// Käyttää SwingPanel-bridgeä upottaakseen Swing-UI:n
SwingPanel(
    modifier = Modifier.fillMaxSize(),
    factory = {
        val panel = DocumentFramePanel(registry, documentModel)
        panel
    }
)
```

### Vertailutaulukko

| Ominaisuus | JavaFX | Compose Desktop |
|------------|--------|-----------------|
| **Kieli** | Java | Kotlin |
| **Ekosysteemi** | Oracle/OpenJFX | JetBrains |
| **Kypsyys** | ⭐⭐⭐⭐⭐ (2008-) | ⭐⭐⭐ (2021-) |
| **Dokumentaatio** | ⭐⭐⭐⭐⭐ | ⭐⭐⭐ |
| **IDE-tuki** | ⭐⭐⭐⭐ (Scene Builder) | ⭐⭐⭐⭐⭐ (IntelliJ) |
| **Swing Interop** | SwingNode | SwingPanel ✅ (käytössä) |
| **Deklaratiivinen** | FXML + Controller | ⭐⭐⭐⭐⭐ 100% Kotlin |
| **Hot Reload** | Ei | Kyllä (Preview) |
| **Android-koodi** | Ei | Jaettavissa |
| **Riippuvuudet** | Erillinen moduli | Gradle plugin (jo käytössä) |

### Compose Desktop - Edut

1. ✅ **Jo konfiguroitu** - `build.gradle.kts` sisältää Compose-pluginin
2. ✅ **Prototyyppi valmiina** - `TilitinApp.kt` toimii
3. ✅ **Kotlin-natiivi** - Projekti jo käyttää Kotlinia (37 tiedostoa)
4. ✅ **SwingPanel toimii** - Asteittainen migraatio mahdollinen
5. ✅ **Modernin reaktiivinen** - State management sisäänrakennettu

### Compose Desktop - Haitat

1. ⚠️ **Nuori teknologia** - Vähemmän resursseja/esimerkkejä
2. ⚠️ **Swing-UI upotettuna** - Ei vielä natiivi Compose-UI
3. ⚠️ **Ei Scene Builderia** - Kaikki koodina
4. ⚠️ **Taulukkotuki** - LazyColumn, ei TableView-vastinetta

### JavaFX - Edut

1. ✅ **Kypsä teknologia** - 15+ vuotta kehitystä
2. ✅ **Scene Builder** - Visuaalinen FXML-editointi
3. ✅ **TableView** - Valmis taulukkokomponentti
4. ✅ **CSS-teemat** - Laaja teematuki
5. ✅ **Dokumentaatio** - Kattava

### JavaFX - Haitat

1. ⚠️ **Ei projektissa** - Vaatii uuden konfiguraation
2. ⚠️ **Java-pohjainen** - Projekti siirtyy Kotliniin
3. ⚠️ **FXML + Controller** - Kaksi tiedostoa per näkymä
4. ⚠️ **Ei Hot Reload** - Käännös joka muutokseen

---

## 🎯 Suositus

### Vaihtoehto A: Compose Desktop (Suositeltu 🏆)

**Miksi:**
- Projekti käyttää jo Kotlinia
- Compose Desktop jo konfiguroitu
- Prototyyppi olemassa
- Moderni, reaktiivinen arkkitehtuuri
- SwingPanel mahdollistaa asteittaisen migraation

**Migraatiostrategia:**
1. Käytä `TilitinApp.kt` lähtökohtana
2. Korvaa yksi Swing-dialogi kerrallaan Compose-versiolla
3. Lopulta korvaa koko DocumentFrame Compose-toteutuksella

### Vaihtoehto B: JavaFX

**Milloin:**
- Jos tarvitset Scene Builder -visuaalieditoria
- Jos TableView on kriittinen (Composessa LazyColumn)
- Jos haluat pysyä Java-ekosysteemissä

---

## 📋 Seuraavat askeleet

### Jos valitset Compose Desktop:

1. **Korjaa Compose-build** (nyt Kotlin Compose -virhe)
2. **Luo yksinkertainen dialogi** Composella (esim. AboutDialog)
3. **Testaa SwingPanel-integraatio** olemassa olevalla koodilla
4. **Iteroi** - korvaa dialogit yksi kerrallaan

### Jos valitset JavaFX:

1. **Lisää JavaFX Gradle-riippuvuudet**
2. **Luo DocumentFrameFX prototyyppi**
3. **Testaa manager-integraatio**
4. **Käytä Scene Builderia layoutiin**

---

**Luotu:** 2025-12-31  
**Päivitetty:** 2025-12-31  
**Status:** Tutkimus valmis - **Compose Desktop suositeltu** koska jo käytössä
