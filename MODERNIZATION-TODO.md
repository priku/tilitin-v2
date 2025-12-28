# Tilitin Modernization TODO

Tämä dokumentti sisältää kattavan listan jäljellä olevista modernisointitehtävistä Windows-modernisaatioprojektissa.

**Projektin tila**: v2.1.6 kehitteillä (feature/code-modernization)
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
- ✅ UIConstants theme-aware värimetodit (7 uutta metodia v2.1.6)
- ✅ Deprecated API korjattu DocumentMenuBuilder:issa (v2.1.6)
- ✅ Lambda-migraatio aloitettu: 6 anonymous inner class → lambda (v2.1.6)

**Jäljellä olevia haasteita:**

- ⚠️ DAO-luokat käyttävät vielä vanhoja Java-malleja (Phase 4 - tulevaisuus)
- ❌ 19+ dialogia käyttää vanhaa GridBagLayout-patternia
- ⚠️ DocumentFrame.java on ~2,654 riviä (vähennetty -1,202 riviä, -31%)
- ⚠️ Vanhat Swing-patternit (anonymous inner classes) - 10/40+ korjattu (25%)
- ❌ Epäjohdonmukainen UI-komponenttisuunnittelu

---

## 🟢 VALMIS - Kotlin Modernisaatio

**Status**: Phase 1 ✅ | Phase 2 ✅ | Phase 2.5 ✅ | Phase 3 ✅ **COMPLETED**

**Tulokset**:

- ✅ Kotlin 2.3.0 + Java 25 toiminnassa
- ✅ 6 data classes (Account, Document, Entry, Period, DocumentType, COAHeading)
- ✅ 3 utility classes (SwingExtensions, ValidationUtils, DialogUtils)
- ✅ DAO Foundation (DatabaseExtensions, SQLAccountDAOKt, SQLiteAccountDAOKt)
- ✅ **Phase 3 AccountDAO integraatio** - SQLiteAccountDAOKt käytössä tuotannossa
- **Koodi vähennetty**: 1,081 → 538 riviä Kotlin (50% vähemmän)

**Phase 3 valmis (v2.1.3)**:
- ✅ SQLiteDataSource käyttää SQLiteAccountDAOKt
- ✅ Testattu ja toimii tuotannossa
- ⚠️ Vanhat Java DAO-tiedostot säilytetty fallbackina (poistetaan v2.2.0)

**Seuraavaksi (Phase 4 - Tulevaisuus)**:
- [ ] Entry DAO migraatio Kotliniin
- [ ] Document DAO migraatio Kotliniin
- [ ] Poista vanhat Java DAO fallbackit

📖 **Yksityiskohtainen dokumentaatio**: [KOTLIN_MIGRATION.md](KOTLIN_MIGRATION.md)

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

### 2. ✅ Yhtenäinen spacing-järjestelmä (COMPLETED v2.0.4, EXTENDED v2.1.6)

**Status**: ✅ **VALMIS** - UIConstants.java luotu ja laajennettu

**Toteutettu v2.0.4**:

- ✅ `UIConstants.java` luotu
- ✅ Standardoidut spacing-vakiot (5px perusyksikkö)
- ✅ Valmiit Insets ja Border objektit
- ✅ Käytössä uusissa dialogeissa (BackupSettingsDialog, RestoreBackupDialog, etc.)

**Toteutettu v2.1.6**:

- ✅ **7 uutta theme-aware värimetodia**:
  - `getBackgroundColor()` - Paneelien taustavärit
  - `getForegroundColor()` - Tekstivärit
  - `getBorderColor()` - Reunusvärit
  - `getTextFieldBackgroundColor()` - Tekstikenttien taustat
  - `getTextFieldForegroundColor()` - Tekstikenttien tekstit
  - `getTableBackgroundColor()` - Taulukoiden taustat
  - `getTableForegroundColor()` - Taulukoiden tekstit
- ✅ Kaikki metodit käyttävät `UIManager`-värejä fallbackeilla
- ✅ Valmiina legacy-dialogien teematukeen

**Jäljellä**:

- [ ] Päivitä 19 vanhaa dialogia käyttämään UIConstants-vakioita
- [ ] Korvaa hardkoodatut marginaalit UIConstants-viittauksilla
- [ ] Käytä uusia theme-aware värimetodeja legacy-dialogeissa

**Prioriteetti**: 🟡 KESKISUURI - Pohja tehty, jäljellä migraatio

---

### 3. 🔄 DocumentFrame.java refaktorointi (IN PROGRESS v2.1.2)

**Status**: 🔄 **ALOITETTU** - Phase 1, 1b, 2, 3 (partial) valmiit

**Toteutettu v2.1.2-v2.1.6**:

- ✅ **Phase 1**: DocumentBackupManager.java (193 riviä)
  - Varmuuskopioinnin hallinta eriytetty
  - DatabaseOpener callback-rajapinta
  - Testattava arkkitehtuuri
- ✅ **Phase 1b**: DocumentExporter.java (83 riviä)
  - CSV-viennin hallinta eriytetty
  - CSVExportStarter-rajapinta
  - Tiedostonvalinta ja hakemiston muistaminen
- ✅ **Phase 2**: Menu/Toolbar creation (v2.1.4)
  - ✅ DocumentMenuBuilder.java (453 riviä)
  - ✅ DocumentToolbarBuilder.java (112 riviä)
- ✅ **Phase 3**: Helper classes (v2.1.5)
  - ✅ DocumentListenerHelpers.java (76 riviä)
  - ✅ EntryTableActions.java (280 riviä)
- ✅ **Phase 3b**: Table management (v2.2.1)
  - ✅ DocumentTableManager.java (400 riviä)
  - ✅ Cell renderer/editor logiikka siirretty
  - ✅ Keyboard shortcuts -hallinta siirretty
  - ✅ Column mapping -logiikka siirretty
  - ✅ updateTableSettings() -metodi siirretty
- ✅ **Phase 4 (partial)**: Lambda-migraatio ja koodin siistiminen (v2.2.1)
  - ✅ 6 anonymous inner class → lambda-lausekkeet (v2.1.6)
  - ✅ Field initialization order korjattu (v2.1.6)
  - ✅ Wrapperit yksinkertaistettu (getPrevDocumentAction/getNextDocumentAction)
  - ✅ 10 käyttämätöntä importia poistettu
- ✅ DocumentFrame.java vähennetty: 3,856 → ~2,930 riviä (-926 riviä, -24%)

**Jäljellä**:

- [ ] **Phase 4**: Event handling (jatkuu)
  - [ ] Jäljellä olevat anonymous inner classes → lambdas (jos mahdollista)
  - [ ] Luo `DocumentEventHandler.java` (valinnainen)
- [x] **Phase 5**: Print toiminnot ✅ (v2.2.1)
  - [x] Laajennettu `DocumentPrinter.java` (434 riviä)
  - [x] Kaikki print-metodit siirretty
  - [x] Print preview -ikkunan hallinta
  - [x] ~276 riviä pois DocumentFrame:sta
- [ ] **Phase 6**: Navigation & State
  - [ ] Luo `DocumentNavigator.java`
  - [ ] Luo `DocumentStateManager.java`
- [ ] **Phase 7**: UI Components
  - [ ] Luo `DocumentUIBuilder.java`
  - [ ] Luo `DocumentUIUpdater.java`

**Tavoite**: DocumentFrame < 500 riviä, loput komponenteissa

**Prioriteetti**: 🔴 KORKEA - Code smell, vaikea ylläpitää

---

### 4. 🔄 Lambda-lausekkeet anonymous inner class -rakenteiden tilalle (IN PROGRESS v2.1.6)

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

**Toteutettu v2.1.6**:
- ✅ DocumentFrame.java: **10 anonymous inner class → lambda** (yhteensä)
  - AccountCellEditor ActionListener
  - Search button ActionListener
  - Recent database menu items (2 kpl)
  - newDatabaseListener, openDatabaseListener
  - entryTemplateListener
  - editDocTypesListener
  - docTypeListener
  - printListener (switch-lauseke optimoitu)
- ✅ Field initialization order korjattu
- ✅ DocumentFrame: 3,024 → 3,007 riviä (-17 riviä)
- ✅ printListener optimoitu: if-else → switch-lauseke (modernimpi)

**Jäljellä**:
- [ ] DocumentFrame.java: ~6+ anonymous inner classes vielä jäljellä (AbstractAction -instanssit ActionMap:issa)
- [ ] COADialog.java - useita kohtia
- [ ] SettingsDialog.java
- [ ] PropertiesDialog.java
- [ ] EntryTemplateDialog.java
- [ ] Ja monissa muissa... (~30+ jäljellä)

**Tehtävät**:
- [ ] Tunnista kaikki ActionListener-käyttökohteet
- [ ] Refaktoroi lambda-lausekkeiksi
- [ ] Testaa että toiminnallisuus säilyy
- [ ] Sama WindowListener, FocusListener, etc.

**Prioriteetti**: 🔴 KORKEA - Modernin Java-koodin standardi

---

## 🟡 Keskisuuri prioriteetti - Parantaa laatua

### 5. ✅ Yhtenäinen BaseDialog-pohjaluokka (COMPLETED v2.0.4)

**Status**: ✅ **VALMIS** - BaseDialog.java luotu ja käytössä

**Toteutettu v2.0.4**:

- ✅ `BaseDialog.java` luotu abstraktina pohjaluokkana
- ✅ Standardirakenne: BorderLayout (content + button panel)
- ✅ Standardipainikkeet: OK, Cancel, Apply (valinnainen)
- ✅ Keyboard shortcuts: ESC = Cancel, Enter = OK
- ✅ UIConstants-integraatio
- ✅ RestoreBackupDialog konvertoitu käyttämään BaseDialog:ia

**Jäljellä**:

- [ ] Migroi 19 vanhaa dialogia käyttämään BaseDialog-pohjaluokkaa
- [ ] Dokumentoi pattern kehittäjille

**Prioriteetti**: 🟡 KESKISUURI - Pohja tehty, jäljellä migraatio

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

### 8. ✅ Deprecated API:n poisto (COMPLETED v2.1.6)

**Ongelma**: Käytössä deprecated metodeja

**Toteutettu v2.1.6**:
- ✅ DocumentMenuBuilder.java: `getMenuShortcutKeyMask()` korvattu
  - Käyttää nyt OS-tunnistusta: `InputEvent.META_DOWN_MASK` (Mac) / `InputEvent.CTRL_DOWN_MASK` (Windows/Linux)
- ✅ DocumentFrame.java line 661: `getMenuShortcutKeyMask()` korvattu
  - Sama OS-tunnistus kuin DocumentMenuBuilder:issa
- ✅ DocumentFrame.java lines 2362, 2407: `InputEvent.ALT_MASK` → `InputEvent.ALT_DOWN_MASK`
  - Korvattu deprecated Java 9+ API modernilla vaihtoehdolla

**Jäljellä** (ei kriittisiä):
```java
// Kirjanpito.java - Reflection hack (ei deprecated, mutta fragile)
Field awtAppClassNameField = toolkitClass.getDeclaredField("awtAppClassName");
awtAppClassNameField.setAccessible(true);
// Fragile, rikkouu module system (Java 9+)
// Vaihtoehto: Harkitse `-Dawt.appClassName=Tilitin` JVM-argumenttia
```

**Tehtävät**:
- [ ] Linux WM_CLASS: Harkitse `-Dawt.appClassName=Tilitin` JVM-argumenttia Kirjanpito.java:lle (valinnainen)

**Prioriteetti**: 🟢 MATALA - Kaikki deprecated API-kutsut korjattu, jäljellä vain reflection-hack joka ei ole deprecated

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
