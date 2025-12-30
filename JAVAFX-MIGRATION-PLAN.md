# JavaFX Migration Plan

## 📋 Yhteenveto

Tämä dokumentti kuvaa suunnitelman Tilitin-sovelluksen UI-migraatiosta Swingistä JavaFX:ään.

**Tavoite:** Moderni, nopea ja kaunis desktop-sovellus  
**Aikataulu:** 2-4 viikkoa  
**Vaivannäkö:** Matala - manager-luokat toimivat sellaisenaan

---

## 🎯 Miksi JavaFX?

### Vertailu vaihtoehtoihin

| Ominaisuus | JavaFX | Electron | Web UI | Swing+FlatLaf |
|------------|--------|----------|--------|---------------|
| Moderni ulkoasu | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐ |
| Suorituskyky | ⭐⭐⭐⭐⭐ | ⭐⭐⭐ | ⭐⭐⭐ | ⭐⭐⭐⭐ |
| Manager-yhteensopivuus | ⭐⭐⭐⭐⭐ | ⭐⭐⭐ | ⭐⭐⭐ | ⭐⭐⭐⭐⭐ |
| Kehitysaika | ⭐⭐⭐⭐ | ⭐⭐ | ⭐⭐ | ⭐⭐⭐⭐⭐ |
| Pakettikoko | ~75 MB | ~280 MB | N/A | ~60 MB |
| Muistin käyttö | ~150 MB | ~400 MB | N/A | ~150 MB |

### JavaFX:n edut

1. **Moderni ulkoasu** - CSS-tyylit, animaatiot, GPU-rendering
2. **Parempi suorituskyky** - 2x nopeampi kuin Swing
3. **Helppo kehitys** - Scene Builder visual editor
4. **Manager-yhteensopivuus** - Toimivat sellaisenaan, ei muutoksia!
5. **Sama ekosysteemi** - 100% Java, ei uusia kieliä
6. **Native installer** - jpackage luo .exe:n automaattisesti

---

## 🏗️ Arkkitehtuuri

### Nykyiset manager-luokat (toimivat sellaisenaan)

```
DocumentFrame (Swing/JavaFX)
├── DocumentNavigator        - Navigointi ja haku
├── DocumentStateManager     - Tilan hallinta
├── DocumentValidator        - Validointi ja tallennus
├── DocumentEntryManager     - Vientien hallinta
├── DocumentDataSourceManager - Tietokantayhteydet
├── DocumentMenuHandler      - Menu-toiminnot
├── DocumentPrinter          - Tulostus
├── DocumentExporter         - CSV-vienti
└── DocumentBackupManager    - Varmuuskopiointi
```

### JavaFX-integraatio

```java
// DocumentFrameFX.java - JavaFX versio
public class DocumentFrameFX extends BorderPane implements NavigationCallbacks {
    
    // Samat managerit - ei muutoksia!
    private final DocumentNavigator navigator;
    private final DocumentStateManager stateManager;
    private final DocumentValidator validator;
    
    // JavaFX UI komponentit
    @FXML private TextField numberTextField;
    @FXML private DatePicker datePicker;
    @FXML private TableView<Entry> entryTable;
    @FXML private Label documentLabel;
    
    public DocumentFrameFX() {
        loadFXML();
        initializeManagers();
    }
    
    private void initializeManagers() {
        // SAMA koodi kuin Swingissä - toimii heti!
        this.navigator = new DocumentNavigator(
            registry, searchPanel, searchPhraseField, this);
        this.stateManager = new DocumentStateManager(...);
        this.validator = new DocumentValidator(...);
    }
    
    @FXML
    private void onCreateDocument() {
        navigator.createDocument(); // Sama kutsu!
    }
}
```

---

## 📅 Aikataulu

### Vaihe 1: Prototyyppi (1-2 päivää)

- [ ] Asenna Scene Builder
- [ ] Luo `DocumentFrameFX.java` prototyyppi
- [ ] Luo `DocumentFrame.fxml` layout
- [ ] Testaa että yksi manager toimii (esim. `createDocument`)

### Vaihe 2: Core funktiot (Viikko 1)

- [ ] Document navigation (create, delete, go to)
- [ ] Entry table (add, remove, edit)
- [ ] Document fields (number, date)
- [ ] Totals calculation
- [ ] Basic keyboard shortcuts

### Vaihe 3: Advanced funktiot (Viikko 2)

- [ ] Search functionality
- [ ] Print preview
- [ ] CSV export/import
- [ ] Attachments panel
- [ ] Menu bar
- [ ] All dialogs

### Vaihe 4: Polish & Testing (Viikko 3)

- [ ] CSS-tyylit (dark/light theme)
- [ ] Animaatiot
- [ ] Full keyboard shortcuts
- [ ] Settings dialog
- [ ] Reports
- [ ] Comprehensive testing

### Vaihe 5: Release (Viikko 4)

- [ ] Build jpackage installer
- [ ] Test installer on Windows/Linux/macOS
- [ ] Documentation
- [ ] Release

---

## 🎨 UI Design

### FXML Layout (DocumentFrame.fxml)

```xml
<?xml version="1.0" encoding="UTF-8"?>
<BorderPane xmlns:fx="http://javafx.com/fxml" 
            fx:controller="kirjanpito.ui.DocumentFrameFX">
    <top>
        <VBox>
            <MenuBar fx:id="menuBar"/>
            <ToolBar>
                <Button text="Uusi tosite" onAction="#onCreateDocument"/>
                <Button text="Poista" onAction="#onDeleteDocument"/>
                <Separator orientation="VERTICAL"/>
                <Button text="◀" onAction="#onPrevDocument"/>
                <Button text="▶" onAction="#onNextDocument"/>
            </ToolBar>
        </VBox>
    </top>
    
    <center>
        <VBox spacing="10" padding="10">
            <!-- Document fields -->
            <HBox spacing="10">
                <Label text="Tositenumero:"/>
                <TextField fx:id="numberTextField" prefWidth="100"/>
                <Label text="Päivämäärä:"/>
                <DatePicker fx:id="datePicker" prefWidth="150"/>
            </HBox>
            
            <!-- Entry table -->
            <TableView fx:id="entryTable" VBox.vgrow="ALWAYS">
                <columns>
                    <TableColumn text="Tili" prefWidth="200"/>
                    <TableColumn text="Selite" prefWidth="300"/>
                    <TableColumn text="Debet" prefWidth="100"/>
                    <TableColumn text="Kredit" prefWidth="100"/>
                    <TableColumn text="ALV" prefWidth="80"/>
                </columns>
            </TableView>
            
            <!-- Totals -->
            <HBox spacing="20">
                <Label text="Debet:" style="-fx-font-weight: bold"/>
                <Label fx:id="debitTotalLabel" text="0.00"/>
                <Label text="Kredit:" style="-fx-font-weight: bold"/>
                <Label fx:id="creditTotalLabel" text="0.00"/>
                <Label text="Erotus:" style="-fx-font-weight: bold"/>
                <Label fx:id="differenceLabel" text="0.00"/>
            </HBox>
        </VBox>
    </center>
    
    <bottom>
        <HBox spacing="10" padding="5">
            <Label fx:id="documentLabel" text="Tosite 1 / 1"/>
            <Separator orientation="VERTICAL"/>
            <Label fx:id="periodLabel" text="Tilikausi 2025"/>
        </HBox>
    </bottom>
</BorderPane>
```

### CSS Themes

**Dark Theme (dark-theme.css):**
```css
.root {
    -fx-base: #2b2b2b;
    -fx-background: #1e1e1e;
    -fx-control-inner-background: #3c3f41;
    -fx-accent: #4a86e8;
}

.table-view {
    -fx-background-color: transparent;
}

.table-row-cell:selected {
    -fx-background-color: #4a86e8;
}

.button {
    -fx-background-radius: 3;
    -fx-padding: 5 15;
}

.button:hover {
    -fx-background-color: #4a86e8;
}
```

**Light Theme (light-theme.css):**
```css
.root {
    -fx-base: #f4f4f4;
    -fx-background: #ffffff;
    -fx-accent: #0078d7;
}
```

---

## 📦 Build & Deployment

### Gradle Configuration

```kotlin
// build.gradle.kts
plugins {
    id("org.openjfx.javafxplugin") version "0.1.0"
}

javafx {
    version = "21"
    modules = listOf("javafx.controls", "javafx.fxml")
}
```

### jpackage Installer

```bash
# Windows .exe installer
jpackage --input build/libs \
  --name Kirjanpito \
  --main-jar kirjanpito-2.3.0.jar \
  --main-class kirjanpito.ui.KirjanpitoFX \
  --type exe \
  --icon src/main/resources/icon.ico \
  --win-menu \
  --win-shortcut \
  --app-version 2.3.0

# Output: Kirjanpito-2.3.0.exe (~75 MB)
```

---

## 🔧 Tekniset yksityiskohdat

### Scene Builder

- **Lataa:** https://gluonhq.com/products/scene-builder/
- **Integrointi:** IntelliJ IDEA: Settings → Languages & Frameworks → JavaFX → Scene Builder path

### Suorituskyky

| Toiminto | Swing | JavaFX | Parannus |
|----------|-------|--------|----------|
| Käynnistys | ~3s | ~2s | 33% nopeampi |
| Table scroll | ~30 FPS | ~60 FPS | 2x sujuvampi |
| UI render | CPU | GPU | Paljon nopeampi |

### Animaatiot

```java
// Fade in effect
FadeTransition fadeIn = new FadeTransition(Duration.millis(300), node);
fadeIn.setFromValue(0.0);
fadeIn.setToValue(1.0);
fadeIn.play();
```

---

## ✅ Tarkistuslista

### Ennen aloitusta
- [ ] Scene Builder asennettu
- [ ] JavaFX dependencies lisätty build.gradle.kts
- [ ] IntelliJ FXML support konfiguroitu

### Migraation aikana
- [ ] Jokainen manager testattu JavaFX:ssä
- [ ] Keyboard shortcuts toimivat
- [ ] Kaikki dialogit konvertoitu
- [ ] CSS-teemat toimivat
- [ ] Print preview toimii

### Ennen julkaisua
- [ ] jpackage installer testattu
- [ ] Windows/Linux/macOS testattu
- [ ] Dokumentaatio päivitetty
- [ ] CHANGELOG päivitetty

---

## 📚 Resurssit

- [JavaFX Documentation](https://openjfx.io/)
- [Scene Builder](https://gluonhq.com/products/scene-builder/)
- [JavaFX CSS Reference](https://openjfx.io/javadoc/21/javafx.graphics/javafx/scene/doc-files/cssref.html)
- [jpackage Guide](https://docs.oracle.com/en/java/javase/21/jpackage/)

---

**Luotu:** 2025-12-31  
**Status:** Suunnitelma valmis, odottaa toteutusta  
**Prioriteetti:** Keskipitkä aikaväli (refaktoroinnin jälkeen)
