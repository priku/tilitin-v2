# DocumentFrame Refactoring Progress

Tämä dokumentti seuraa DocumentFrame.java -refaktoroinnin edistymistä.

**Alkuperäinen koko:** 3,856 riviä  
**Nykyinen koko:** ~2,654 riviä  
**Vähennys:** -1,202 riviä (-31%)  
**Tavoite:** <500 riviä

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
| **YHTEENSÄ** | | **2,038 riviä** | **-1,202 riviä** | |

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

### Phase 4: Event Handling (jatkuu)
**Tavoite:** Jäljellä olevat anonymous inner classes → lambdas (jos mahdollista)

**Jäljellä:**
- Monimutkaiset AbstractAction-luokat (prevCellAction, nextCellAction, toggleDebitCreditAction)
- FileFilter (ei voi olla lambda, koska 2 metodia)

**Valinnainen:**
- `DocumentEventHandler.java` - Kaikki event handling -logiikka yhteen paikkaan

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

### Phase 6: Navigation & State Management
**Tavoite:** Eriytä navigointi ja state management

**Tehtävät:**
- Luo `DocumentNavigator.java`
  - prev/next/first/last document -logiikka
  - document search -logiikka
  - document filtering
- Luo `DocumentStateManager.java`
  - document loading -logiikka
  - document saving -logiikka
  - dirty state tracking
  - validation -logiikka

**Arvio:** ~500-650 riviä pois DocumentFrame:sta

---

### Phase 7: UI Component Management
**Tavoite:** Eriytä UI-komponenttien luominen ja päivitys

**Tehtävät:**
- Luo `DocumentUIBuilder.java`
  - Kaikki komponenttien luominen
  - Layout management
  - Komponenttien alustus
- Luo `DocumentUIUpdater.java`
  - UI update -logiikka
  - Label updates
  - Button state management

**Arvio:** ~600-800 riviä pois DocumentFrame:sta

---

## 🎯 Tavoite

**Lopullinen tavoite:** DocumentFrame < 500 riviä

**Nykyinen tila:** ~2,930 riviä  
**Jäljellä:** ~2,430 riviä

**Arvioitu vähennys Phase 5-7:**
- Phase 5: ~200-300 riviä
- Phase 6: ~500-650 riviä
- Phase 7: ~600-800 riviä
- **Yhteensä:** ~1,300-1,750 riviä

**Arvioitu lopputulos:** ~1,180-1,630 riviä

**Huomio:** Tavoite <500 riviä saavutetaan todennäköisesti vasta Phase 7:n jälkeen, ja saattaa vaatia lisää eriytyksiä.

---

## 📝 Muistiinpanot

- **Koodin laatu:** Eriytetty koodi on selkeämpää ja helpommin testattavaa
- **Ylläpidettävyys:** Pienempi DocumentFrame on helpompi ylläpitää
- **Testattavuus:** Eriytetyt luokat voidaan testata erikseen
- **Modulaarisuus:** Uudet luokat ovat riippumattomia ja uudelleenkäytettäviä

---

**Viimeksi päivitetty:** 2025-12-29  
**Versio:** 2.2.1  
**Testaus:** ✅ Testattu ja toimii (2025-12-29)

