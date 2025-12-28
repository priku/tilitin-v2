# Legacy Components Inventory

Tämä dokumentti listaa kaikki vanhat komponentit jotka tarvitsevat modernisointia Tilitin-projektissa.

**Analyysi suoritettu**: 2025-12-28
**Perustuu**: 186 Java-tiedoston kattavaan koodianalyysiin
**Versio**: feature/windows-modernization (v2.0.3-dev)

---

## 📁 Pakettirakenne ja tiedostomäärät

```
src/main/java/kirjanpito/
├── db/                  (62 tiedostoa) - Tietokanta-abstraktio
│   ├── mysql/           (13 tiedostoa)
│   ├── postgresql/      (13 tiedostoa)
│   ├── sqlite/          (13 tiedostoa)
│   └── sql/             (13 tiedostoa)
├── models/              (25 tiedostoa) - MVC mallit
├── reports/             (27 tiedostoa) - Raportit ja tulostus
├── ui/                  (43 tiedostoa) - Käyttöliittymä ⚠️
└── util/                (16 tiedostoa) - Apuohjelmat
```

**⚠️ Huomio**: `ui/` paketti sisältää eniten legacy-koodia.

---

## 🔴 KRIITTISET: Vaativat välitöntä modernisointia

### 1. DocumentFrame.java (MONOLITH)

**Tiedosto**: `src/main/java/kirjanpito/ui/DocumentFrame.java`
**Koko**: 37,313 tavua (37 KB)
**Rivit**: ~1200+
**Ongelmat**:
- ❌ Massiivinen God Object anti-pattern
- ❌ Yhdistää menu, toolbar, table, events, export, print, backup
- ❌ Yli tuhat riviä koodia
- ❌ Käyttää vanhoja anonymous inner classes
- ❌ Manuaalinen GridBagLayout
- ❌ Hardkoodattuja värejä
- ❌ Ei testattavissa (liian iso)

**Modernisointitarpeet**:
1. Jaa erillisiin manager-luokkiin:
   - `DocumentMenuBuilder.java`
   - `DocumentToolbarBuilder.java`
   - `DocumentTableManager.java`
   - `DocumentEventHandler.java`
   - `DocumentExporter.java`
   - `DocumentPrinter.java`
   - `DocumentBackupManager.java`

2. Lambda-lausekkeet
3. FlatLaf theming
4. Extract constants

**Prioriteetti**: 🔴 KRIITTINEN
**Estimaatti**: 3-5 päivää
**Riski**: KORKEA - core component, paljon riippuvuuksia

---

### 2. COADialog.java (Chart of Accounts)

**Tiedosto**: `src/main/java/kirjanpito/ui/COADialog.java`
**Koko**: ~1000 riviä
**Ongelmat**:
- ❌ Monimutkainen GridBagLayout
- ❌ Manuaalinen drag-and-drop toteutus
- ❌ Kymmeniä anonymous inner classes
- ❌ Popup menu management hankalaa
- ❌ Custom TransferHandler

**Modernisointitarpeet**:
1. Lambda-lausekkeet
2. MigLayout tai modernimpi layout
3. Erota drag-and-drop logiikka omaan luokkaan
4. FlatLaf colors
5. Testattava arkkitehtuuri

**Prioriteetti**: 🔴 KRIITTINEN
**Estimaatti**: 2-3 päivää
**Riski**: KESKISUURI - monimutkainen toiminnallisuus

---

## 🟠 TÄRKEÄT: GridBagLayout-dialogit (19 kpl)

Seuraavat dialogit käyttävät vanhaa GridBagLayout-patternia verbose constraints-määrittelyillä:

### UI Dialogs (tarvitsevat modernisointia)

| # | Tiedosto | Rivit | Monimutkaisuus | Prioriteetti |
|---|----------|-------|----------------|--------------|
| 1 | `SettingsDialog.java` | ~300 | Keskisuuri | 🟠 Tärkeä |
| 2 | `PropertiesDialog.java` | ~400 | Keskisuuri | 🟠 Tärkeä |
| 3 | `AccountSelectionDialog.java` | ~250 | Pieni | 🟡 Normaali |
| 4 | `EntryTemplateDialog.java` | ~500 | Suuri | 🟠 Tärkeä |
| 5 | `FinancialStatementOptionsDialog.java` | ~300 | Keskisuuri | 🟡 Normaali |
| 6 | `StartingBalanceDialog.java` | ~200 | Pieni | 🟡 Normaali |
| 7 | `SearchDialog.java` | ~350 | Keskisuuri | 🟡 Normaali |
| 8 | `PrintStyleEditorDialog.java` | ~400 | Suuri | 🟠 Tärkeä |
| 9 | `ChartOptionsDialog.java` | ~250 | Keskisuuri | 🟡 Normaali |
| 10 | `VoucherTemplateDialog.java` | ~300 | Keskisuuri | 🟡 Normaali |
| 11 | `ImportCSVDialog.java` | ~600 | Suuri | 🟠 Tärkeä |
| 12 | `AccountPeriodDialog.java` | ~200 | Pieni | 🟡 Normaali |
| 13 | `PeriodDialog.java` | ~150 | Pieni | 🟡 Normaali |
| 14 | `AccountDialog.java` | ~250 | Keskisuuri | 🟡 Normaali |
| 15 | `COATableDialog.java` | ~300 | Keskisuuri | 🟡 Normaali |
| 16 | `ReportStructureDialog.java` | ~450 | Suuri | 🟠 Tärkeä |
| 17 | `CompanyInformationDialog.java` | ~200 | Pieni | 🟡 Normaali |
| 18 | `DateCellEditor.java` | ~150 | Pieni | 🟢 Matala |
| 19 | `DescriptionCellEditor.java` | ~100 | Pieni | 🟢 Matala |

**Yhteenlinjat**: ~5,650
**Estimaatti per dialogi**: 1-4 tuntia
**Kokonaisestimaatti**: 4-6 päivää

### Yhteiset ongelmat kaikissa:

```java
// ONGELMA 1: Verbose GridBagLayout
GridBagConstraints c = new GridBagConstraints();
c.gridx = 0;
c.gridy = 0;
c.weightx = 0.0;
c.weighty = 0.0;
c.fill = GridBagConstraints.HORIZONTAL;
c.anchor = GridBagConstraints.WEST;
c.insets = new Insets(4, 4, 4, 4); // Epäjohdonmukainen!
panel.add(label, c);

// ONGELMA 2: Anonymous inner classes
button.addActionListener(new ActionListener() {
    public void actionPerformed(ActionEvent e) {
        handleAction();
    }
});

// ONGELMA 3: Hardcoded colors
panel.setBackground(new Color(240, 240, 240));

// ONGELMA 4: Manuaalinen component creation
JLabel label = new JLabel("Text:");
JTextField field = new JTextField(20);
JButton button = new JButton("OK");
// Toistetaan kymmeniä kertoja...
```

### Modernisointisuunnitelma per dialogi:

**Vaihe 1: Perusta** (2h per dialogi)
- [ ] Extend BaseDialog (kun se on luotu)
- [ ] Poista hardcoded colors -> UIManager colors
- [ ] Lambda-lausekkeet
- [ ] UIConstants spacing

**Vaihe 2: Layout** (2h per dialogi)
- [ ] MigLayout tai GroupLayout
- [ ] Reduce code by 30-50%

**Vaihe 3: Testaus** (1h per dialogi)
- [ ] Dark mode
- [ ] Light mode
- [ ] Functional testing

---

## 🟡 NORMAALIT: Cell Renderers ja Editors

### Custom Cell Renderers (10 kpl)

| Tiedosto | Käyttötarkoitus | Modernisointitarve |
|----------|-----------------|-------------------|
| `AccountCellRenderer.java` | Tilinumero-sarake | Generic factory |
| `DateCellRenderer.java` | Päivämäärä-sarake | Generic factory |
| `CurrencyCellRenderer.java` | Rahamäärä-sarake | Generic factory |
| `ComboBoxCellRenderer.java` | Dropdown-sarake | Generic factory |
| `COATableCellRenderer.java` | Tilikartta-taulu | Erityislogiikka, säilytä |

### Custom Cell Editors (5 kpl)

| Tiedosto | Käyttötarkoitus | Modernisointitarve |
|----------|-----------------|-------------------|
| `AccountCellEditor.java` | Tilinumero-muokkaus | Generic factory |
| `DateCellEditor.java` | Päivämäärä-picker | Modernisoi, käytä JDatePicker? |
| `CurrencyCellEditor.java` | Rahamäärä-syöttö | Generic factory |
| `ComboBoxCellEditor.java` | Dropdown-muokkaus | Generic factory |
| `DescriptionCellEditor.java` | Teksti-muokkaus | Generic factory |

**Ongelma**: Paljon copypaste-koodia. Esimerkki:

```java
// AccountCellRenderer.java (~50 riviä)
public class AccountCellRenderer extends DefaultTableCellRenderer {
    public Component getTableCellRendererComponent(...) {
        // Formatting logic
    }
}

// DateCellRenderer.java (~50 riviä)
public class DateCellRenderer extends DefaultTableCellRenderer {
    public Component getTableCellRendererComponent(...) {
        // Formatting logic (eri formaatti)
    }
}

// CurrencyCellRenderer.java (~50 riviä)
public class CurrencyCellRenderer extends DefaultTableCellRenderer {
    public Component getTableCellRendererComponent(...) {
        // Formatting logic (eri formaatti)
    }
}
```

**Ratkaisu**: Generic Factory Pattern

```java
public class CellRendererFactory {
    public static <T> TableCellRenderer create(
        Class<T> type,
        Function<T, String> formatter,
        int alignment) {

        return new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(...) {
                T value = (T) tableValue;
                String formatted = formatter.apply(value);
                setText(formatted);
                setHorizontalAlignment(alignment);
                applyTheme(); // FlatLaf theming
                return this;
            }
        };
    }

    // Factory methods
    public static TableCellRenderer forCurrency() {
        return create(BigDecimal.class,
            v -> String.format("%.2f €", v),
            SwingConstants.RIGHT);
    }

    public static TableCellRenderer forDate() {
        return create(Date.class,
            v -> new SimpleDateFormat("dd.MM.yyyy").format(v),
            SwingConstants.CENTER);
    }
}
```

**Estimaatti**: 1-2 päivää
**Riski**: MATALA - hyvin eristetty toiminnallisuus

---

## 🔵 UTILITY: Apuohjelmaluokat

### Modernisoidut (✅ v2.0.3)

- ✅ `BackupService.java` - Word AutoSave -tyylinen backup
- ✅ `BackupLocation.java` - Multi-location abstraktio
- ✅ `DatabaseBackupConfig.java` - Per-DB konfiguraatio
- ✅ `CloudStorageDetector.java` - OneDrive/Dropbox/iCloud/Google Drive

### Legacy-utilit (⚠️ tarkista)

| Tiedosto | Kuvaus | Modernisointitarve |
|----------|--------|-------------------|
| `AppSettings.java` | Asetusten hallinta | Tarkista deprecated APIs |
| `CSVReader.java` | CSV-tuonti | Harkitse OpenCSV-kirjastoa? |
| `AccountBalances.java` | Saldolaskenta | Testattavuus? |
| `Registry.java` | Observer pattern | Moderni EventBus? |

**Toimenpiteet**:
- [ ] Code review utilities
- [ ] Unit testit puuttuvat -> lisää
- [ ] Deprecated API check
- [ ] Consider modern alternatives

---

## 🎨 UI Component Patterns

### Pattern 1: Dialog Creation (Legacy)

**19 dialogia** noudattaa tätä patternia:

```java
public class SomeDialog extends JDialog {

    public SomeDialog(Frame owner, String title) {
        super(owner, title, true);
    }

    public void create() {
        // Manual setup
        setLayout(new BorderLayout());

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        // ... verbose constraints ...

        add(contentPanel, BorderLayout.CENTER);
        add(createButtonPanel(), BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(getOwner());
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel();
        JButton okButton = new JButton("OK");
        okButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // ...
            }
        });
        // ...
        return panel;
    }
}
```

**Ongelmat**:
- ❌ Copypaste joka dialogissa
- ❌ Ei yhtenäistä teemaa
- ❌ Manuaalinen button panel creation
- ❌ Ei standardoituja button actions (OK/Cancel/Apply)

### Pattern 2: BaseDialog (Moderni - ehdotettu)

```java
public abstract class BaseDialog extends JDialog {

    protected BaseDialog(Frame owner, String title) {
        super(owner, title, true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }

    public void initialize() {
        setLayout(new BorderLayout());

        JPanel content = createContentPanel();
        content.setBorder(UIConstants.DIALOG_BORDER);
        add(content, BorderLayout.CENTER);

        add(createStandardButtonPanel(), BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(getOwner());
        applyTheme();
    }

    protected abstract JPanel createContentPanel();

    protected JPanel createStandardButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panel.setBorder(UIConstants.BUTTON_PANEL_BORDER);

        JButton cancelButton = new JButton("Peruuta");
        cancelButton.addActionListener(e -> onCancel());

        JButton okButton = new JButton("OK");
        okButton.addActionListener(e -> onOK());
        getRootPane().setDefaultButton(okButton);

        panel.add(cancelButton);
        panel.add(okButton);
        return panel;
    }

    protected void applyTheme() {
        // Ensure all components use FlatLaf theme
    }

    protected void onOK() {
        dispose();
    }

    protected void onCancel() {
        dispose();
    }
}
```

**Käyttö**:

```java
public class ModernDialog extends BaseDialog {

    public ModernDialog(Frame owner) {
        super(owner, "Modern Dialog");
        initialize();
    }

    @Override
    protected JPanel createContentPanel() {
        JPanel panel = new JPanel(new MigLayout(
            "fill, insets dialog",
            "[right]rel[grow,fill]",
            "[]rel[]"
        ));

        panel.add(new JLabel("Name:"));
        panel.add(new JTextField(), "wrap");

        panel.add(new JLabel("Email:"));
        panel.add(new JTextField(), "wrap");

        return panel;
    }

    @Override
    protected void onOK() {
        // Validation
        if (isValid()) {
            saveData();
            super.onOK();
        }
    }
}
```

**Edut**:
- ✅ 50% vähemmän koodia
- ✅ Yhtenäinen rakenne
- ✅ Teematuki built-in
- ✅ Standard button actions
- ✅ Helppo laajentaa

---

## 📊 Deprecated API Usage

### Löydetyt deprecated API-kutsut:

#### 1. Toolkit.getMenuShortcutKeyMask() ⚠️

**Sijainti**: `DocumentFrame.java`

```java
// DEPRECATED (since Java 10)
int shortcutKeyMask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();

// KORJAUS:
int shortcutKeyMask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();
// TAI parempi (cross-platform):
int shortcutKeyMask = java.awt.event.InputEvent.CTRL_DOWN_MASK;
if (System.getProperty("os.name").toLowerCase().contains("mac")) {
    shortcutKeyMask = java.awt.event.InputEvent.META_DOWN_MASK;
}
```

**Prioriteetti**: 🟡 Normaali (toimii vielä, mutta poistuu tulevissa versioissa)

#### 2. Reflection for Linux WM_CLASS ⚠️⚠️

**Sijainti**: `Kirjanpito.java`

```java
// FRAGILE HACK
Class<?> toolkitClass = Toolkit.getDefaultToolkit().getClass();
Field awtAppClassNameField = toolkitClass.getDeclaredField("awtAppClassName");
awtAppClassNameField.setAccessible(true); // Rikkoo module system!
awtAppClassNameField.set(Toolkit.getDefaultToolkit(), "Tilitin");
```

**Ongelma**:
- Rikkoo Java 9+ module system
- Fragile (private field access)
- Voi hajota tulevissa Java-versioissa

**Korjaus**: Käytä JVM-argumenttia

```java
// Poista reflection hack kokonaan

// Sen sijaan: launch.json / IDE run configuration / shell script:
java -Dawt.appClassName=Tilitin -jar tilitin.jar
```

**Prioriteetti**: 🟠 Tärkeä (tulevaisuuden Java-yhteensopivuus)

#### 3. Mahdollisia muita (tarkistettava)

```bash
# Etsi deprecated API-käyttö:
grep -r "@Deprecated" src/main/java/
grep -r "getMenuShortcutKeyMask()" src/
grep -r "setAccessible(true)" src/
```

---

## 🧪 Testaus-status

### Unit testit

**Nykyinen tila**: ❌ EI UNIT TESTEJÄ

**Ongelmat**:
- Ei `src/test/java/` hakemistoa
- Ei JUnit dependencyjä pom.xml:ssä
- Legacy koodi ei ole testattavissa (liian sidottu UI:hin)

**Suositukset**:
1. Lisää JUnit 5 + Mockito pom.xml:ään
2. Luo testit uusille komponenteille (BackupService, etc.)
3. Refaktoroi legacy-koodia testattavammaksi (dependency injection)
4. Target: >70% code coverage core logic

### Integration testit

**Ei ole** - harkitse:
- AssertJ Swing (UI integration tests)
- TestFX (jos siirtyisit JavaFX:ään tulevaisuudessa)

---

## 📈 Modernisaation eteneminen

### Valmis (v2.0.2 - v2.0.3) ✅

| Komponentti | Tila | Rivit | Testattu |
|-------------|------|-------|----------|
| FlatLaf Integration | ✅ | - | ✅ |
| SplashScreen.java | ✅ | 150 | ✅ |
| AppearanceDialog.java | ✅ | 200 | ✅ |
| BackupService.java | ✅ | 900 | ⚠️ Ei testejä |
| BackupLocation.java | ✅ | 150 | ⚠️ Ei testejä |
| DatabaseBackupConfig.java | ✅ | 200 | ⚠️ Ei testejä |
| CloudStorageDetector.java | ✅ | 300 | ⚠️ Ei testejä |
| BackupSettingsDialog.java | ✅ | 500 | ✅ |
| DatabaseBackupConfigDialog.java | ✅ | 400 | ✅ |
| RestoreBackupDialog.java | ✅ | 350 | ✅ |

**Yhteensä**: ~3,150 riviä modernia koodia ✅

### Jäljellä ⚠️

| Komponentti | Estimaatti | Prioriteetti | Riski |
|-------------|-----------|--------------|-------|
| DocumentFrame.java | 3-5 päivää | 🔴 Kriittinen | KORKEA |
| COADialog.java | 2-3 päivää | 🔴 Kriittinen | KESKISUURI |
| 19x GridBag dialogs | 4-6 päivää | 🟠 Tärkeä | MATALA |
| Cell renderers/editors | 1-2 päivää | 🟡 Normaali | MATALA |
| Deprecated API cleanup | 0.5 päivää | 🟡 Normaali | MATALA |
| BaseDialog creation | 1 päivä | 🟠 Tärkeä | MATALA |
| UIConstants.java | 0.5 päivää | 🔴 Kriittinen | MATALA |
| Unit tests | 3-5 päivää | 🟡 Normaali | MATALA |

**Kokonaisestimaatti jäljellä**: 15-25 työpäivää (3-5 viikkoa)

---

## 🎯 Suositeltu toteutusjärjestys

### Sprint 1: Foundation (Viikko 1)
1. Luo UIConstants.java
2. Luo BaseDialog.java
3. Testaa 2-3 yksinkertaisella dialogilla
4. Dokumentoi patterns

### Sprint 2: Critical Refactors (Viikko 2-3)
1. DocumentFrame.java pilkkominen
2. Lambda-lausekkeet DocumentFrame
3. COADialog.java modernisaatio
4. FlatLaf theming critical dialogs

### Sprint 3: Dialogs (Viikko 3-4)
1. Migroi 19 dialogia BaseDialog:iin
2. MigLayout käyttöönotto
3. Theme testing (dark/light)
4. Accessibility basics

### Sprint 4: Polish (Viikko 4-5)
1. Cell renderer factory
2. Deprecated API cleanup
3. Unit tests core functionality
4. Documentation
5. Final QA

---

## 📝 Muistilista koodikatselmukseen

Kun käyt läpi legacy-koodia, tarkista:

- [ ] ❌ Hardcoded colors -> UIManager colors
- [ ] ❌ Anonymous inner classes -> Lambda
- [ ] ❌ GridBagLayout verbose -> MigLayout/Modern
- [ ] ❌ Manual component creation -> Factories/Builders
- [ ] ❌ Copypaste code -> Abstraktiot
- [ ] ❌ Magic numbers -> Constants
- [ ] ❌ Deprecated APIs -> Modern replacements
- [ ] ❌ No tests -> Add unit tests
- [ ] ❌ Poor separation of concerns -> Refactor
- [ ] ❌ God objects -> Split responsibilities

---

## 🔗 Riippuvuudet

### Legacy-koodin riippuvuudet:

```
DocumentFrame.java (MONOLITH)
  ├── 43 UI dialogia (suora tai epäsuora riippuvuus)
  ├── Registry.java (observer pattern)
  ├── AppSettings.java
  ├── BackupService.java (uusi)
  └── 10+ Table models

COADialog.java
  ├── AccountCellRenderer/Editor
  ├── Registry.java
  └── Custom TransferHandler

19x GridBag Dialogs
  ├── Ei yhtenäistä base classia
  ├── Kopioitu button panel logic
  └── Manuaalinen theming
```

**Riski**: Korjaus yhdessä komponentissa voi vaikuttaa moneen muuhun.

**Mitigaatio**:
- Tee pienet, inkrementaaliset muutokset
- Testaa jokainen muutos
- Käytä feature flageja isommille muutoksille
- Versionhallinta: yksi muutos = yksi commit

---

**Päivitetty**: 2025-12-28
**Seuraava katselmus**: Sprint 1 jälkeen
**Vastuuhenkilö**: [Määrittämättä]
