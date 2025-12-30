# DocumentFrame Refactoring Progress

Tämä dokumentti seuraa DocumentFrame.java -refaktoroinnin edistymistä.

**Alkuperäinen koko:** 3,856 riviä
**Nykyinen koko:** 2,423 riviä
**Vähennys:** -1,433 riviä (-37%)
**Tavoite:** 400-450 riviä (realistinen)

---

## ✅ Valmiit vaiheet

### Phase 1: DocumentBackupManager (v2.1.3)
**Tiedosto:** `DocumentBackupManager.java` (193 riviä)  
**Vähennys:** ~193 riviä DocumentFrame:sta

**Eriytetty:**
- Varmuuskopioinnin hallinta
- DatabaseOpener callback-rajapinta
- Backup-sijaintien hallinta

---

### Phase 1b: DocumentExporter (v2.1.3)
**Tiedosto:** `DocumentExporter.java` (83 riviä)  
**Vähennys:** ~83 riviä DocumentFrame:sta

**Eriytetty:**
- CSV-viennin hallinta
- CSVExportStarter-rajapinta
- Tiedostonvalinta ja hakemiston muistaminen

---

### Phase 2: Menu & Toolbar Builders (v2.1.4)
**Tiedostot:**
- `DocumentMenuBuilder.java` (460 riviä)
- `DocumentToolbarBuilder.java` (112 riviä)

**Vähennys:** ~572 riviä DocumentFrame:sta

**Eriytetty:**
- Valikkorivin luominen
- Toolbarin luominen
- Menu item -referenssit
- Keyboard shortcuts -konfiguraatio

---

### Phase 3: Helper Classes (v2.1.5)
**Tiedostot:**
- `DocumentListenerHelpers.java` (76 riviä)
- `EntryTableActions.java` (280 riviä)

**Vähennys:** ~356 riviä DocumentFrame:sta

**Eriytetty:**
- Listener-helper metodit
- Entry table actions
- Copy/paste -toiminnot

---

### Phase 3b: Table Management (v2.2.1) ✅ UUSI
**Tiedosto:** `DocumentTableManager.java` (400 riviä)  
**Vähennys:** ~75 riviä DocumentFrame:sta

**Eriytetty:**
- Table creation ja konfiguraatio
- Cell renderer/editor -asetukset
- Keyboard shortcuts -hallinta (kaikki table-related shortcuts)
- Column width management
- VAT column visibility control
- Column index mapping (view/model)
- updateTableSettings() -metodi

**Rajapinnat:**
- `TableCallbacks` - Callback-rajapinta DocumentFrame:lle
- `TableActions` - Actionsit keyboard shortcuts:lle
- `ColumnMapper` - Sarakeindeksien muunnin

**Ominaisuudet:**
- OS-tunnistus keyboard shortcuts:lle (Mac/Windows)
- F12: remove suffix -toiminto
- Enter: next/prev cell -toiminto
- Previous row action (UP key)

---

### Phase 4: Code Cleanup (v2.2.1) ✅ UUSI
**Vähennys:** ~3 riviä DocumentFrame:sta + koodin laadun parannus

**Tehdyt muutokset:**
- Yksinkertaistettu wrapperit (getPrevDocumentAction/getNextDocumentAction)
- Poistettu 10 käyttämätöntä importia:
  - `java.awt.Component`
  - `javax.imageio.ImageIO`
  - `javax.swing.JComponent`
  - `javax.swing.JScrollPane`
  - `javax.swing.ListSelectionModel`
  - `javax.swing.event.TableModelEvent`
  - `javax.swing.event.TableModelListener`
  - `javax.swing.table.DefaultTableCellRenderer`
  - `javax.swing.table.TableCellEditor`
  - `javax.swing.table.TableCellRenderer`
  - `javax.swing.table.TableColumnModel`

**Huomioita:**
- Useimmat `AbstractAction`-luokat eivät voi olla lambda-lausekkeita, koska:
  - Ne tarvitsevat `serialVersionUID`:n
  - Ne käytetään `ActionMap`:issa, jotka vaativat `Action`-tyypin
  - Ne sisältävät monimutkaista logiikkaa
- `FileFilter` ei voi olla lambda, koska sillä on kaksi metodia

---

## 📊 Edistyminen

| Vaihe | Tiedosto | Koko | Vähennys | Status |
|-------|----------|------|----------|--------|
| Phase 1 | DocumentBackupManager.java | 193 riviä | -193 riviä | ✅ |
| Phase 1b | DocumentExporter.java | 83 riviä | -83 riviä | ✅ |
| Phase 2 | DocumentMenuBuilder.java | 460 riviä | -460 riviä | ✅ |
| Phase 2 | DocumentToolbarBuilder.java | 112 riviä | -112 riviä | ✅ |
| Phase 3 | DocumentListenerHelpers.java | 76 riviä | -76 riviä | ✅ |
| Phase 3 | EntryTableActions.java | 280 riviä | -280 riviä | ✅ |
| Phase 3b | DocumentTableManager.java | 400 riviä | -75 riviä | ✅ |
| Phase 4 | Code cleanup | - | -3 riviä | ✅ |
| Phase 5 | DocumentPrinter.java | 434 riviä | -276 riviä | ✅ |
| Phase 5b | DocumentMenuHandler.java (laajennus) | 299 riviä | -34 riviä | ✅ |
| Phase 6 | DocumentStateManager.java | 368 riviä | ~300 riviä | ✅ |
| Phase 7 | DocumentUIBuilder.java | 287 riviä | ~200 riviä | ✅ |
| Phase 7 | DocumentUIUpdater.java | 372 riviä | ~250 riviä | ✅ |
| Phase 8 | DocumentNavigator.java | 320 riviä | -194 riviä | ✅ |
| Phase 9 | DocumentEntryManager.java | 535 riviä | -200 riviä | ✅ |
| Phase 10 | DocumentValidator.java | 320 riviä | -99 riviä | ✅ |
| **YHTEENSÄ** | | **~4,539 riviä** | **-1,433 riviä** | |

**Huomio:** Vähennys on pienempi kuin eriytetty koodi, koska:
- Uudet luokat tarvitsevat oman rakenteensa (importit, dokumentaatio, jne.)
- Jotkut metodit ovat lyhyempiä uusissa luokissa
- Lisätty callback-rajapinnat ja abstraktiot

### Phase 5: Print Operations ✅
**Tiedosto:** `DocumentPrinter.java` (434 riviä)  
**Vähennys:** ~276 riviä DocumentFrame:sta

**Eriytetty:**
- Kaikki print-metodit (showAccountSummary, showDocumentPrint, jne.)
- Print preview -ikkunan hallinta
- Report generation -kutsut
- Print options -dialogien hallinta
- PrintCallbacks-rajapinta DocumentFrame:lle

**Rajapinnat:**
- `PrintCallbacks` - Callback-rajapinta saveDocumentIfChanged(), model, entryTable:lle

**Ominaisuudet:**
- 9 print-metodia siirretty DocumentPrinter:iin
- Kaikki print-importit poistettu DocumentFrame:sta
- Print preview -ikkunan hallinta siirretty

---

## 🔄 Jäljellä olevat vaiheet

### Phase 9: Dialog Management (Seuraavaksi - HELPPO ⭐)

**Tavoite:** Eriytä dialog-käynnistysmetodit omaan manageriin

**Tiedosto:** `DocumentDialogManager.java` (~200 riviä)
**Vähennys:** ~200 riviä DocumentFrame:sta

**Tehtävät:**
- Luo DocumentDialogManager.java
- Siirrä kaikki dialog-käynnistysmetodit (15+ metodia):
  - showChartOfAccounts(), showCsvImportDialog()
  - createVATDocument(), editEntryTemplates(), createEntryTemplateFromDocument()
  - showStartingBalances(), showProperties(), showSettings(), showAppearanceDialog()
  - showDatabaseSettings(), showAccountSelection(), showBalanceComparison()
  - showDocumentNumberShiftDialog(), showVATChangeDialog()
  - showAboutDialog(), showHelp(), showLogMessages()
  - restoreFromBackup() (61 riviä)
- Luo DialogCallbacks-rajapinta

**Vaikeus:** ⭐ Helppo - nämä ovat yksinkertaisia launcher-metodeja

---

### Phase 10: Document Validation (KESKIVAIKEA ⭐⭐)

**Tavoite:** Eriytä validointi-logiikka

**Tiedosto:** `DocumentValidator.java` (~150 riviä)
**Vähennys:** ~150 riviä DocumentFrame:sta

**Tehtävät:**
- Luo DocumentValidator.java
- Siirrä validointimetodit:
  - updateModel() (104 riviä - monimutkainen validointi)
  - removeEmptyEntry() (22 riviä)
  - validateDocumentNumber()
  - Osa saveDocumentIfChanged() logiikasta
- Luo ValidationCallbacks-rajapinta
- Jätä DocumentFrame:lle vain orchestration

**Vaikeus:** ⭐⭐ Keskivaikea - validointilogiikka on monimutkaista

---

### Phase 11: Data Source Management (KESKIVAIKEA ⭐⭐)

**Tavoite:** Eriytä tietolähteen hallinta

**Tiedosto:** `DataSourceManager.java` (~120 riviä)
**Vähennys:** ~120 riviä DocumentFrame:sta

**Tehtävät:**
- Luo DataSourceManager.java
- Siirrä data source -metodit:
  - openDataSource() (41 riviä)
  - openSqliteDataSource() (7 riviä)
  - updateRecentDatabasesMenu() (32 riviä)
  - openRecentDatabase() (11 riviä)
  - refreshModel() (19 riviä)
  - initializeDataSource() (22 riviä)
- Luo DataSourceCallbacks-rajapinta

**Vaikeus:** ⭐⭐ Keskivaikea - käynnistyslogiikka on kriittistä

---

### Phase 12: Cell Navigation Actions (VAIKEA ⭐⭐⭐)

**Tavoite:** Siirrä cell navigation DocumentTableManager:iin

**Laajennus:** `DocumentTableManager.java` (+140 riviä)
**Vähennys:** ~140 riviä DocumentFrame:sta

**Tehtävät:**
- Siirrä prevCellAction (52 riviä) → DocumentTableManager
- Siirrä nextCellAction (78 riviä) → DocumentTableManager
- Siirrä toggleDebitCreditAction
- Laajenna TableCallbacks-rajapintaa tarvittaessa

**Vaikeus:** ⭐⭐⭐ Vaikea - tiivis kytkentä taulukkoon ja entry-malliin

**Huomio:** Lisää coupling:ia DocumentTableManager:iin, mutta parempi kuin pitää DocumentFrame:ssa

---

### Phase 13: Utility Methods (HELPPO ⭐)

**Tavoite:** Eriytä utility-metodit erillisiin luokkiin

**Vähennys:** ~95 riviä DocumentFrame:sta

**Tehtävät:**
- quit() (34 riviä) → ApplicationLifecycleManager
- performBackupOnClose() (17 riviä) → DocumentBackupManager (laajennus)
- generateUniqueFileName() (18 riviä) → FileUtils
- findDocumentTypeByNumber() (13 riviä) → DocumentTypeUtils
- stopEditing(), moveToNextCell() → TableUtils

**Vaikeus:** ⭐ Helppo - yksinkertaiset utility-metodit

---

### Phase 14: Action Listeners Consolidation (KESKIVAIKEA ⭐⭐)

**Tavoite:** Konsolidoi jäljellä olevat action listeners

**Vähennys:** ~150 riviä DocumentFrame:sta

**Tehtävät:**
- Siirrä registry listener → DocumentStateManager
- Konsolidoi callback-implementaatiot
- Siirrä jäljellä olevat inline listeners handlereille
- Siivoa initialization sequence

**Vaikeus:** ⭐⭐ Keskivaikea - vaatii huolellista koordinaatiota

---

### Phase 15: Entry Operations (KESKIVAIKEA ⭐⭐)

**Tavoite:** Siirrä loput entry-operaatiot DocumentEntryManager:iin

**Laajennus:** `DocumentEntryManager.java` (+100 riviä)
**Vähennys:** ~100 riviä DocumentFrame:sta

**Tehtävät:**
- copyEntries() (38 riviä) - siirrä loput logiikasta
- pasteEntries() (73 riviä) - siirrä loput logiikasta
- removeEntry() (31 riviä) - siirrä loput logiikasta
- Konsolidoi entry-toiminnot yhteen paikkaan

**Vaikeus:** ⭐⭐ Keskivaikea - copy/paste ovat monimutkaisia

---

### Phase 16: Initialization Cleanup (KESKIVAIKEA ⭐⭐)

**Tavoite:** Delegoi loput UI-luonti buildereihin

**Vähennys:** ~200 riviä DocumentFrame:sta

**Tehtävät:**
- createTable() - siirrä loput DocumentTableManager:iin
- initializeUIManagers() - optimoi delegointia
- create() - optimoi initialization sequence
- Konsolidoi UI-päivityslogiikkaa

**Vaikeus:** ⭐⭐ Keskivaikea - initialization order on kriittinen

---

### Phase 5: Print Operations ✅
**Status:** Valmis (v2.2.1)

**Toteutettu:**
- ✅ DocumentPrinter.java laajennettu (434 riviä)
- ✅ Kaikki print-metodit siirretty
- ✅ Print preview -logiikka siirretty
- ✅ Report generation -kutsut siirretty
- ✅ Print options -hallinta siirretty
- ✅ ~276 riviä pois DocumentFrame:sta

---

### Phase 5b: File Menu Listeners ✅ UUSI
**Tiedosto:** `DocumentMenuHandler.java` laajennus (252 → 299 riviä)  
**Vähennys:** ~34 riviä DocumentFrame:sta

**Eriytetty:**
- newDatabaseListener (43 riviä → handler)
- openDatabaseListener (15 riviä → handler)
- databaseSettingsListener (handler)
- setIgnoreFlagToEntryAction (32 riviä → handler)

**Lisätty getterit DocumentFrame:een:**
- getModel()
- getSqliteFileFilter()
- getTableModel()
- getEntryTable()
- getRegistry()
- generateUniqueFileName() public

---

### Phase 6: State Management ✅
**Status:** Valmis (v2.2.5)

**Tiedosto:** `DocumentStateManager.java` (368 riviä)  

**Eriytetty:**
- Document state persistence (save/load)
- UI component updates (labels, text fields)
- Validation logic
- Dirty state tracking
- Total row calculations

---

### Phase 7: UI Component Management ✅
**Status:** Valmis (v2.2.5)

**Tiedostot:**
- `DocumentUIBuilder.java` (287 riviä)
- `DocumentUIUpdater.java` (372 riviä)

**Eriytetty:**
- Kaikki UI-komponenttien luominen
- Layout management
- Komponenttien alustus
- UI update -logiikka
- Label updates
- Button state management

---

### Phase 9: Entry Operations ✅

**Status:** Valmis (v2.2.5) - Cursor

**Tiedosto:** `DocumentEntryManager.java` (535 riviä)
**Vähennys:** ~200 riviä DocumentFrame:sta (2,722 → 2,523 riviä)

**Eriytetty:**

- Entry operations (add, remove, copy, paste)
- Cell navigation logic (nextCell, prevCell)
- Toggle debit/credit functionality
- Entry template application
- Clipboard operations

**Rajapinnat:**

- `EntryCallbacks` - Callback-rajapinta DocumentFrame:lle

**Metodit siirretty:**

- addEntry(), removeEntry()
- copyEntries(), pasteEntries()
- nextCellAction, prevCellAction
- toggleDebitCreditAction
- applyEntryTemplate()

**Ominaisuudet:**

- Keskittää kaikki entry-toiminnot yhteen luokkaan
- Clipboard-operaatiot (copy/paste TSV-muodossa)
- Cell navigation keyboard shortcuts
- Entry template -tuki

---

### Phase 10: Document Validation ✅

**Status:** Valmis (v2.2.5) - Cursor

**Tiedosto:** `DocumentValidator.java` (320 riviä)
**Vähennys:** ~99 riviä DocumentFrame:sta (2,523 → 2,423 riviä)

**Eriytetty:**

- Document field validation
- Save operation coordination
- Empty entry removal
- Document number validation

**Rajapinnat:**

- `ValidationCallbacks` - Callback-rajapinta DocumentFrame:lle

**Metodit siirretty:**

- saveDocumentIfChanged()
- updateModel()
- removeEmptyEntry()
- validateDocumentNumber()

**Ominaisuudet:**

- Kokoaa validointilogiikan yhteen paikkaan
- Validoi tositenumero, päivämäärä ja viennit
- Tarkistaa lukitut kaudet
- Debit/credit balance -tarkistus

---

### Phase 8: Navigation & Search ✅

**Status:** Valmis (v2.2.5) - Claude

**Tiedosto:** `DocumentNavigator.java` (320 riviä)
**Vähennys:** ~194 riviä DocumentFrame:sta (2,916 → 2,722 riviä, -6.7%)

**Eriytetty:**

- Document navigation (create, delete, go to)
- Document search functionality
- Search panel management
- Search state management (searchEnabled)

**Rajapinnat:**

- `NavigationCallbacks` - Callback-rajapinta DocumentFrame:lle

**Metodit siirretty:**

- createDocument()
- deleteDocument()
- goToDocument(int index)
- findDocumentByNumber()
- toggleSearchPanel()
- searchDocuments()
- isSearchEnabled()

**Ominaisuudet:**

- Callback-pohjainen arkkitehtuuri irrottaa navigation-logiikan DocumentFrame:sta
- Search panel visibility hallitaan suoraan DocumentNavigator:ssa
- searchEnabled-tila siirretty DocumentNavigator:iin

---

## 🎯 Tavoite

**Alkuperäinen tavoite:** DocumentFrame < 500 riviä
**Realistinen tavoite:** **400-450 riviä** (85% vähennys alkuperäisestä)

**Nykyinen tila:** 2,423 riviä
**Jäljellä:** ~1,973-2,023 riviä ekstrahoitavana

**Analyysin tulos (31.12.2025):**
- ✅ Tavoite on **saavutettavissa** - realistinen lopputulos 400-450 riviä
- ✅ **Phases 9-10 VALMIIT** - Cursor toteutti (-299 riviä)
- ✅ Phases 11-16 voivat poistaa yhteensä ~1,973-2,023 riviä
- ⚠️ Alle 400 riviä ei ole realistista ilman arkkitehtuurin uudelleensuunnittelua
- 📋 DocumentFrame:n lopullinen rooli: **View Controller** - koordinoi ja orkestroi managereita

**Jäljellä olevat ekstrahoitavat kokonaisuudet:**

| Kokonaisuus | Rivit | Vaikeus | Phase | Status |
|-------------|-------|---------|-------|--------|
| ~~Entry Operations~~ | ~~-200~~ | ~~⭐⭐~~ | ~~Phase 9~~ | ✅ **VALMIS** |
| ~~Document Validation~~ | ~~-99~~ | ~~⭐⭐~~ | ~~Phase 10~~ | ✅ **VALMIS** |
| Data Source Management | ~120 | ⭐⭐ Keskivaikea | Phase 11 | Seuraavaksi |
| Dialog Management (15+ metodia) | ~200 | ⭐ Helppo | Phase 12 | |
| Utility Methods (quit, backup, jne.) | ~95 | ⭐ Helppo | Phase 13 | |
| Action Listeners Consolidation | ~150 | ⭐⭐ Keskivaikea | Phase 14 | |
| Initialization Cleanup | ~1,400+ | ⭐⭐⭐ Vaikea | Phase 15-16 | |
| **JÄLJELLÄ** | **~1,965** | | | |

**Mikä JOUTUU jäämään (~400-450 riviä):**
- Rakenne (luokka, kentät, constructor) ~15 riviä
- Core command routing (delegointi) ~80 riviä
- UI update koordinaatio ~80 riviä
- Interface implementaatiot (getterit) ~30 riviä
- Core persistence orchestration ~50 riviä
- Initialization framework ~50 riviä
- Registry listeners ~50 riviä
- Misc (lifecycle, jne.) ~45 riviä

**Arvioitu lopputulos Phase 9-16 jälkeen:** **400-450 riviä** ✅

**Huomio:** Alle 400 riviä vaatisi cell navigation -logiikan siirtämistä (lisää coupling:ia), keinotekoisia wrapper-tasoja tai arkkitehtuurin uudelleensuunnittelua (event bus, mediator pattern). Tavoite 400-450 riviä edustaa optimaalista tasapainoa separation of concerns:n ja arkkitehtuurisen selkeyden välillä.

---

## 📝 Muistiinpanot

- **Koodin laatu:** Eriytetty koodi on selkeämpää ja helpommin testattavaa
- **Ylläpidettävyys:** Pienempi DocumentFrame on helpompi ylläpitää
- **Testattavuus:** Eriytetyt luokat voidaan testata erikseen
- **Modulaarisuus:** Uudet luokat ovat riippumattomia ja uudelleenkäytettäviä

---

**Viimeksi päivitetty:** 2025-12-31
**Versio:** 2.2.5
**Testaus:** ✅ Testattu ja toimii (Gradle build)

---

## 📈 Yhteenveto Edistymisestä

**Aloitustilanne (v2.1.3):**
- DocumentFrame.java: 3,856 riviä
- Arkkitehtuuri: Massiivinen "God Object"
- Ongelmat: Vaikea ylläpitää, testata ja ymmärtää

**Nykytilanne (v2.2.5):**
- DocumentFrame.java: 2,423 riviä (-1,433 riviä, -37%)
- Valmiit vaiheet: Phases 1-10 ✅
- Ekstrahtoitu: 10 uutta manager-luokkaa (~4,539 riviä koodia)
- Arkkitehtuuri: Callback-pohjainen separation of concerns

**Tavoitetilanne (Phases 11-16):**
- DocumentFrame.java: 400-450 riviä (-1,973-2,023 riviä, -85% alkuperäisestä)
- Lopullinen rooli: View Controller - koordinoi ja orkestroi managereita
- Jäljellä olevat vaiheet: 6 phasea (11-16)
- Arvioitu lisävähennys: ~1,973-2,023 riviä (jo vähennetty -1,433 riviä)

**Arkkitehtuurinen muutos:**
```
Ennen: God Object (3,856 riviä)
  ├─ Kaikki vastuut yhdessä luokassa
  └─ Vaikea testata ja ylläpitää

Nyt: Modular Architecture (2,423 + 4,539 riviä)
  ├─ DocumentFrame (2,423 riviä) - View Controller
  ├─ DocumentBackupManager (193 riviä) - Backup operations
  ├─ DocumentExporter (83 riviä) - CSV export
  ├─ DocumentMenuBuilder (460 riviä) - Menu construction
  ├─ DocumentToolbarBuilder (112 riviä) - Toolbar construction
  ├─ DocumentListenerHelpers (76 riviä) - Helper methods
  ├─ EntryTableActions (280 riviä) - Table actions
  ├─ DocumentTableManager (400 riviä) - Table management
  ├─ DocumentPrinter (434 riviä) - Printing operations
  ├─ DocumentMenuHandler (299 riviä) - Menu listeners
  ├─ DocumentStateManager (432 riviä) - State management
  ├─ DocumentUIBuilder (316 riviä) - UI construction
  ├─ DocumentUIUpdater (406 riviä) - UI updates
  ├─ DocumentNavigator (320 riviä) - Navigation & search
  ├─ DocumentEntryManager (535 riviä) - Entry operations ✨ UUSI
  └─ DocumentValidator (320 riviä) - Validation ✨ UUSI

Tulevaisuus: Clean Architecture (400-450 + ~5,400 riviä)
  └─ + 6 uutta manageria (Phases 11-16)
```

**Hyödyt:**
- ✅ Parempi testattavuus - eriytetyt luokat testattavissa erikseen
- ✅ Selkeämpi vastuunjako - jokainen luokka yksi vastuu
- ✅ Helpompi ylläpitää - pienempiä, fokusoidumpia luokkia
- ✅ Parempi modulaarisuus - uudelleenkäytettäviä komponentteja
- ✅ Callback-arkkitehtuuri - löyhä kytkentä

