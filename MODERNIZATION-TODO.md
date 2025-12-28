# Tilitin Modernization TODO

Tämä dokumentti sisältää kattavan listan jäljellä olevista modernisointitehtävistä Windows-modernisaatioprojektissa.

**Projektin tila**: v2.1.1 kehitteillä (feature/2.1-documentframe-refactor)
**Viimeksi päivitetty**: 2025-12-28
**Analyysi perustuu**: 186 Java-tiedoston + Kotlin-modernisaation kattavaan analyysiin

---

## Yhteenveto

**Valmista modernisaatiota:**
- ✅ FlatLaf teemajärjestelmä (light/dark mode)
- ✅ Splash screen gradientilla ja dynaamisella versiolla
- ✅ Backup-järjestelmä pilvipalvelutunnistuksella
- ✅ AppearanceDialog live-esikatselulla
- ✅ Ikonit modernisoitu (256x256 asti)
- ✅ Kotlin 2.3.0 + Java 25 päivitys
- ✅ Kotlin data classes (Account, Document, Entry, Period, DocumentType, COAHeading)
- ✅ Kotlin utility classes (SwingExtensions, ValidationUtils, DialogUtils)

**Jäljellä olevia haasteita:**

- ❌ DAO-luokat käyttävät vielä vanhoja Java-malleja (Phase 3)
- ❌ 19+ dialogia käyttää vanhaa GridBagLayout-patternia
- ❌ DocumentFrame.java on 37KB monolittti
- ❌ Vanhat Swing-patternit (anonymous inner classes)
- ❌ Epäjohdonmukainen UI-komponenttisuunnittelu

---

## 🟢 VALMIS - Kotlin Modernisaatio (v2.1.1)

### ✅ Phase 1: Foundation (COMPLETED)

- **Kotlin 2.3.0** lisätty projektiin (tuki Java 25:lle)
- **Maven-konfiguraatio** päivitetty (jvmTarget=25)
- **Kotlin utility classes** luotu:
  - `SwingExtensions.kt` - GridBagConstraints helpers, dialog extensions
  - `ValidationUtils.kt` - Null-safe validation
  - `DialogUtils.kt` - File choosers, EDT utilities
- **Build pipeline** toimii (Java + Kotlin mixed compilation)

### ✅ Phase 2: Model Classes (COMPLETED)

- **6 Kotlin data classes** luotu:
  - `AccountData` - Tilin tiedot + helper methods
  - `DocumentData` - Tositteen tiedot
  - `EntryData` - Viennin tiedot + validation
  - `PeriodData` - Tilikauden tiedot
  - `DocumentTypeData` - Tositelajin tiedot
  - `COAHeadingData` - Tilikartan väliotsikko
- **Koodi vähennetty**: ~764 riviä Java → ~300 riviä Kotlin (60% vähemmän)
- **Helper methods** lisätty: `isBalanceSheetAccount()`, `hasVat()`, `displayName()`, etc.

### 🔄 Phase 3: DAO Migration (NEXT)

**Tavoite**: Migroi DAO-luokat käyttämään Kotlin data classeja

**Tehtävät**:

- [ ] Luo `DatabaseExtensions.kt` (ResultSet mapping helpers)
- [ ] Migroi `SQLAccountDAO.java` → `SQLAccountDAO.kt`
- [ ] Migroi `SQLEntryDAO.java` → `SQLEntryDAO.kt`
- [ ] Migroi `SQLDocumentDAO.java` → `SQLDocumentDAO.kt`
- [ ] Päivitä UI-komponentit käyttämään Kotlin-malleja
- [ ] Testaa yhteensopivuus

**Hyödyt**:

- Null-safety SQL-kyselyissä
- Vähemmän boilerplate-koodia (try-catch, resource management)
- Extension functions ResultSet-käsittelyyn
- Type-safe database operations

**Dokumentaatio**: Katso [KOTLIN_MIGRATION.md](KOTLIN_MIGRATION.md) täydellisestä suunnitelmasta

---

## 🔴 Korkea prioriteetti - Blokkaa Windows-modernisaation

### 1. Teematuki vanhoihin dialogeihin

**Ongelma**: Vain uudet komponentit (backup-dialogit, splash screen) tukevat FlatLaf dark/light modea. Vanhat dialogit käyttävät hardkoodattuja värejä.

**Vaikuttaa tiedostoihin** (19 dialogia):
- `src/main/java/kirjanpito/ui/SettingsDialog.java`
- `src/main/java/kirjanpito/ui/PropertiesDialog.java`
- `src/main/java/kirjanpito/ui/COADialog.java`
- `src/main/java/kirjanpito/ui/AccountSelectionDialog.java`
- `src/main/java/kirjanpito/ui/EntryTemplateDialog.java`
- `src/main/java/kirjanpito/ui/FinancialStatementOptionsDialog.java`
- `src/main/java/kirjanpito/ui/StartingBalanceDialog.java`
- `src/main/java/kirjanpito/ui/SearchDialog.java`
- `src/main/java/kirjanpito/ui/PrintStyleEditorDialog.java`
- `src/main/java/kirjanpito/ui/ChartOptionsDialog.java`
- `src/main/java/kirjanpito/ui/VoucherTemplateDialog.java`
- `src/main/java/kirjanpito/ui/ImportCSVDialog.java`
- `src/main/java/kirjanpito/ui/AccountPeriodDialog.java`
- `src/main/java/kirjanpito/ui/PeriodDialog.java`
- `src/main/java/kirjanpito/ui/AccountDialog.java`
- `src/main/java/kirjanpito/ui/COATableDialog.java`
- `src/main/java/kirjanpito/ui/ReportStructureDialog.java`
- `src/main/java/kirjanpito/ui/CompanyInformationDialog.java`
- Ja muita...

**Tehtävät**:
- [ ] Poista kaikki hardkoodatut värit (esim. `new Color(...)`)
- [ ] Käytä UIManager-värejä: `UIManager.getColor("Panel.background")`
- [ ] Testaa jokainen dialogi dark ja light modessa
- [ ] Varmista että tekstikontrastit ovat riittävät

**Prioriteetti**: 🔴 KORKEA - Ilman tätä dark mode näyttää rikkinäiseltä

---

### 2. Yhtenäinen spacing-järjestelmä

**Ongelma**: Jokainen dialogi käyttää omia marginaaleja ja padding-arvoja. Ei yhtenäistä design systemiä.

**Esimerkkejä epäjohdonmukaisuudesta**:
```java
// SettingsDialog.java
BorderFactory.createEmptyBorder(4, 4, 4, 4)

// COADialog.java
BorderFactory.createEmptyBorder(8, 8, 8, 8)

// BackupSettingsDialog.java (uusi)
BorderFactory.createEmptyBorder(15, 15, 15, 15)
```

**Ratkaisu**: Luo `UIConstants.java`

**Tehtävät**:
- [ ] Luo `src/main/java/kirjanpito/ui/UIConstants.java`
- [ ] Määrittele standardit:
  ```java
  public class UIConstants {
      // Spacing
      public static final int DIALOG_PADDING = 15;
      public static final int COMPONENT_SPACING = 10;
      public static final int SECTION_SPACING = 20;

      // Borders
      public static final Border DIALOG_BORDER =
          BorderFactory.createEmptyBorder(DIALOG_PADDING, DIALOG_PADDING,
                                         DIALOG_PADDING, DIALOG_PADDING);

      // Common GridBagConstraints
      public static final Insets DEFAULT_INSETS = new Insets(5, 5, 5, 5);
      public static final Insets NO_INSETS = new Insets(0, 0, 0, 0);

      // Component sizes
      public static final Dimension BUTTON_SIZE = new Dimension(100, 30);
      public static final Dimension SMALL_BUTTON_SIZE = new Dimension(80, 25);
  }
  ```
- [ ] Päivitä kaikki dialogit käyttämään näitä vakioita
- [ ] Dokumentoi design-päätökset

**Prioriteetti**: 🔴 KORKEA - Yhtenäinen UX edellyttää tätä

---

### 3. DocumentFrame.java refaktorointi

**Ongelma**: `DocumentFrame.java` on 37,313 tavua (37KB), sisältää:
- Menu bar creation
- Toolbar creation
- Table management
- Event listeners (kymmeniä)
- Window state management
- Print logic
- Export logic
- Report generation
- Ja paljon muuta...

**Tämä rikkoo**:
- Single Responsibility Principle
- Maintainability
- Testability

**Tehtävät**:
- [ ] **Vaihe 1**: Erota menu/toolbar creation omiin luokkiinsa
  - [ ] Luo `DocumentMenuBuilder.java`
  - [ ] Luo `DocumentToolbarBuilder.java`

- [ ] **Vaihe 2**: Erota table management
  - [ ] Luo `DocumentTableManager.java`
  - [ ] Siirrä cell renderer/editor logiikka

- [ ] **Vaihe 3**: Erota event handling
  - [ ] Luo `DocumentEventHandler.java`
  - [ ] Käytä lambda-lausekkeita

- [ ] **Vaihe 4**: Erota export/print toiminnot
  - [ ] Luo `DocumentExporter.java`
  - [ ] Luo `DocumentPrinter.java`

- [ ] **Vaihe 5**: Erota backup-integraatio
  - [ ] Luo `DocumentBackupManager.java`
  - [ ] Tarkempi statusinäyttö (ei vain label)

**Tavoite**: DocumentFrame < 500 riviä, loput komponenteissa

**Prioriteetti**: 🔴 KORKEA - Code smell, vaikea ylläpitää

---

### 4. Lambda-lausekkeet anonymous inner class -rakenteiden tilalle

**Ongelma**: Koodissa 40+ kohtaa käytetään vanhoja anonymous inner classeja:

```java
// VANHA (pre-Java 8)
button.addActionListener(new ActionListener() {
    public void actionPerformed(ActionEvent e) {
        doSomething();
    }
});

// MODERNI (Java 8+)
button.addActionListener(e -> doSomething());
```

**Vaikuttaa tiedostoihin**:
- `DocumentFrame.java` - kymmeniä kohtia
- `COADialog.java` - useita kohtia
- `SettingsDialog.java`
- `PropertiesDialog.java`
- `EntryTemplateDialog.java`
- Ja monissa muissa...

**Tehtävät**:
- [ ] Tunnista kaikki ActionListener-käyttökohteet
- [ ] Refaktoroi lambda-lausekkeiksi
- [ ] Testaa että toiminnallisuus säilyy
- [ ] Sama WindowListener, FocusListener, etc.

**Prioriteetti**: 🔴 KORKEA - Modernin Java-koodin standardi

---

## 🟡 Keskisuuri prioriteetti - Parantaa laatua

### 5. Yhtenäinen BaseDialog-pohjaluokka

**Ongelma**: Jokainen dialogi toteuttaa omat `create()`, `createButtons()` jne. metodit. Paljon copypaste-koodia.

**Ratkaisu**: Luo abstrakti pohjaluokka

**Tehtävät**:
- [ ] Luo `src/main/java/kirjanpito/ui/BaseDialog.java`:
  ```java
  public abstract class BaseDialog extends JDialog {
      protected BaseDialog(Frame owner, String title) {
          super(owner, title, true);
          setDefaultCloseOperation(DISPOSE_ON_CLOSE);
      }

      protected void initialize() {
          setLayout(new BorderLayout());
          add(createContentPanel(), BorderLayout.CENTER);
          add(createButtonPanel(), BorderLayout.SOUTH);
          pack();
          setLocationRelativeTo(getOwner());
      }

      protected abstract JPanel createContentPanel();

      protected JPanel createButtonPanel() {
          // Standardit OK/Cancel/Apply napit
      }

      protected void applyTheme() {
          // FlatLaf theming
      }
  }
  ```
- [ ] Refaktoroi ainakin 5 dialogia käyttämään tätä
- [ ] Dokumentoi pattern muille kehittäjille

**Prioriteetti**: 🟡 KESKISUURI - Vähentää copypastea, helpottaa ylläpitoa

---

### 6. GridBagLayout migraatio

**Ongelma**: GridBagLayout on verbose ja vaikeasti luettava:

```java
GridBagConstraints c = new GridBagConstraints();
c.gridx = 0;
c.gridy = 0;
c.weightx = 0.0;
c.weighty = 0.0;
c.fill = GridBagConstraints.HORIZONTAL;
c.anchor = GridBagConstraints.WEST;
c.insets = new Insets(4, 4, 4, 4);
panel.add(label, c);
// Toistetaan joka komponentille...
```

**Vaihtoehdot**:
1. **MigLayout** (paras, mutta external dependency)
2. **GroupLayout** (Swing built-in, NetBeans käyttää)
3. **BorderLayout + Box combinations** (yksinkertainen, ei dependencyjä)

**Tehtävät**:
- [ ] Valitse migraatiostrategia (suositus: MigLayout)
- [ ] Jos MigLayout, lisää pom.xml:ään:
  ```xml
  <dependency>
      <groupId>com.miglayout</groupId>
      <artifactId>miglayout-swing</artifactId>
      <version>11.3</version>
  </dependency>
  ```
- [ ] Migroi 1-2 yksinkertaista dialogia testiksi
- [ ] Vertaile koodin määrää ja luettavuutta
- [ ] Päätä jatketaanko migraatiota

**Prioriteetti**: 🟡 KESKISUURI - Parantaa koodin laatua, ei välttämätön

---

### 7. Cell Renderer/Editor konsolidointi

**Ongelma**: 10+ erillistä cell renderer/editor luokkaa:
- `AccountCellRenderer.java` + `AccountCellEditor.java`
- `DateCellRenderer.java` + `DateCellEditor.java`
- `CurrencyCellRenderer.java` + `CurrencyCellEditor.java`
- `ComboBoxCellRenderer.java` + `ComboBoxCellEditor.java`
- `DescriptionCellEditor.java`
- `COATableCellRenderer.java`

**Ratkaisu**: Generic renderer/editor factory

**Tehtävät**:
- [ ] Luo `CellRendererFactory.java`:
  ```java
  public class CellRendererFactory {
      public static <T> TableCellRenderer createRenderer(
          Class<T> type, Function<T, String> formatter) {
          // Generic implementation
      }
  }
  ```
- [ ] Käytä Java generics vähentämään copypaste-koodia
- [ ] Testaa backward compatibility

**Prioriteetti**: 🟡 KESKISUURI - Vähentää koodin määrää

---

### 8. Deprecated API:n poisto

**Ongelma**: Käytössä deprecated metodeja

**Esimerkkejä**:
```java
// DocumentFrame.java
int shortcutKeyMask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
// Deprecated Java 10+, käytä: InputEvent.CTRL_DOWN_MASK tai META_DOWN_MASK

// Kirjanpito.java - Reflection hack
Field awtAppClassNameField = toolkitClass.getDeclaredField("awtAppClassName");
awtAppClassNameField.setAccessible(true);
// Fragile, rikkouu module system (Java 9+)
```

**Tehtävät**:
- [ ] Etsi kaikki `@Deprecated` API-kutsut
- [ ] Korvaa modernilla vaihtoehdolla
- [ ] Linux WM_CLASS: Harkitse `-Dawt.appClassName=Tilitin` JVM-argumenttia

**Prioriteetti**: 🟡 KESKISUURI - Tulevaisuuden Java-versioissa voi rikkoutua

---

## 🟢 Matala prioriteetti - Nice-to-have

### 9. Accessibility-ominaisuudet

**Puuttuu**:
- Keyboard navigation (tab order)
- Screen reader support (ARIA labels)
- Focus indicators
- Keyboard shortcuts dokumentaatio

**Tehtävät**:
- [ ] Lisää `setMnemonic()` kaikille menu-itemeille
- [ ] Lisää `setAccessibleDescription()` komponeteille
- [ ] Testaa keyboard-only navigation
- [ ] Luo accessibility guide

**Prioriteetti**: 🟢 MATALA - Lakisääteinen joissain maissa, mutta ei blokkeri

---

### 10. Responsiivinen suunnittelu

**Ongelma**: Kiinteät koot dialogeissa, ei skaalaudu eri resoluutioille

**Tehtävät**:
- [ ] Käytä `pack()` kiinteiden kokojen sijaan
- [ ] Käytä relative sizing (proportions)
- [ ] Testaa 4K ja pieni 1366x768 näytöillä
- [ ] HiDPI-tuki (Retina, 4K)

**Prioriteetti**: 🟢 MATALA - Toimii nyt, parantaisi UX:ää

---

### 11. Asset management -järjestelmä

**Ongelma**: Ikoneita ladataan manuaalisesti try-catch blokeissa:

```java
try {
    ImageIcon icon = new ImageIcon(
        ImageIO.read(getClass().getResourceAsStream("/icons/save.png")));
} catch (IOException e) {
    logger.warning("Icon not found");
}
```

**Ratkaisu**: Keskitetty asset manager

**Tehtävät**:
- [ ] Luo `AssetManager.java`:
  ```java
  public class AssetManager {
      private static final Map<String, ImageIcon> iconCache = new HashMap<>();

      public static ImageIcon getIcon(String name) {
          return iconCache.computeIfAbsent(name, AssetManager::loadIcon);
      }
  }
  ```
- [ ] Cachetetaan kaikki ikonit
- [ ] Lazy loading
- [ ] Theme-aware icons (dark/light variants)

**Prioriteetti**: 🟢 MATALA - Toimii nyt, optimoisi muistinkäyttöä

---

### 12. Table Model tyypitys

**Ongelma**: Käytetään `DefaultTableModel` ilman type safety:

```java
DefaultTableModel model = new DefaultTableModel();
model.addRow(new Object[] {"foo", 123, true}); // Ei type safety
```

**Ratkaisu**: Typed table models

**Tehtävät**:
- [ ] Luo generic `TypedTableModel<T>`:
  ```java
  public class TypedTableModel<T> extends AbstractTableModel {
      private List<T> data = new ArrayList<>();
      private List<Column<T, ?>> columns = new ArrayList<>();

      public void addColumn(String name, Function<T, ?> getter) {
          columns.add(new Column<>(name, getter));
      }
  }
  ```
- [ ] Migroi ainakin backup-dialogi käyttämään
- [ ] Testaa compile-time type safety

**Prioriteetti**: 🟢 MATALA - Parantaisi koodin laatua, ei välttämätön

---

### 13. Async UI updates

**Ongelma**: Jotkut operaatiot saattavat blokeerata Event Dispatch Thread (EDT)

**Tehtävät**:
- [ ] Auditoi kaikki long-running operations
- [ ] Käytä `SwingWorker` raskaissa operaatioissa:
  ```java
  new SwingWorker<Void, Void>() {
      @Override
      protected Void doInBackground() {
          // Heavy work here
      }

      @Override
      protected void done() {
          // Update UI
      }
  }.execute();
  ```
- [ ] Lisää progress indicators

**Prioriteetti**: 🟢 MATALA - UI responsive nyt, parantaisi UX:ää

---

## 📋 Yhteenveto prioriteeteista

### Suositeltu toteutusjärjestys (Sprint-ehdotukset)

**Sprint 1: Teematuki (1-2 viikkoa)**
1. Teematuki vanhoihin dialogeihin
2. UIConstants.java luonti
3. Testaus dark/light modessa

**Sprint 2: Arkkitehtuuri (2-3 viikkoa)**
1. DocumentFrame refaktorointi
2. BaseDialog pohjaluokka
3. Lambda-lausekkeet

**Sprint 3: Code quality (1-2 viikkoa)**
1. Deprecated API:n poisto
2. Cell renderer konsolidointi
3. GridBagLayout migraatio (valinnainen)

**Sprint 4: Polish (1 viikko)**
1. Accessibility perusteet
2. Asset management
3. Dokumentaatio

---

## 🔍 Testaussuunnitelma

Jokaisen muutoksen jälkeen:
- [ ] Testaa dark mode
- [ ] Testaa light mode
- [ ] Testaa Windows 10
- [ ] Testaa Windows 11
- [ ] Testaa theme switching runtime
- [ ] Testaa keyboard navigation
- [ ] Regressiotestaus core-toiminnallisuuksille

---

## 📊 Metriikat

**Nykyinen tila**:
- Legacy dialogs: 19
- Anonymous inner classes: ~40+
- Hardcoded colors: Useita kymmeniä
- Monolittic files: 1 (DocumentFrame.java 37KB)

**Tavoite v2.1.0**:
- Legacy dialogs: 0
- Anonymous inner classes: 0
- Hardcoded colors: 0
- Largest file: <10KB

---

## 📚 Resurssit

**FlatLaf dokumentaatio**:
- https://www.formdev.com/flatlaf/
- https://www.formdev.com/flatlaf/themes/

**Swing modernization guides**:
- Oracle Swing Tutorial (updated for Java 21)
- MigLayout: https://www.miglayout.com/

**Design systems**:
- Material Design for reference
- Windows 11 Design Guidelines

---

## ✅ Valmis modernisaatio (vertailua varten)

Jo tehty v2.0.2:
- ✅ FlatLaf integration
- ✅ Dynamic theme switching
- ✅ Splash screen with gradients
- ✅ Backup system (7 new files)
- ✅ Cloud storage detection
- ✅ Icon modernization (256x256)
- ✅ Dynamic version display
- ✅ AppearanceDialog with live preview

---

**Huom**: Tämä dokumentti päivitetään projektin edetessä. Merkitse tehdyt tehtävät ✅ ja lisää uusia löydöksiä.
