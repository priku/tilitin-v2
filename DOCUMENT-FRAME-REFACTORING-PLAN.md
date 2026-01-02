# DocumentFrame.java Refaktoroinnin Suunnitelma

**Päivämäärä:** 2026-01-02  
**Tiedosto:** `src/main/java/kirjanpito/ui/DocumentFrame.java`  
**Koko:** ~2200 riviä  
**Tila:** Swing UI (legacy), JavaFX UI on valmis

---

## 🎯 Tavoite

Refaktoroida DocumentFrame.java pienempiin, ylläpidettäviin osiin. Dokumentti analysoi nykytilan ja tarjoaa suunnitelman refaktoroinnille.

---

## 📊 Nykyinen Rakenne

### Päätoteutus
- **Luokka:** `DocumentFrame extends JFrame`
- **Koko:** ~2200 riviä
- **Vastuualueet:** 
  - UI-komponenttien luominen
  - Tapahtumakäsittely
  - Tietokannan hallinta
  - Dokumenttien hallinta
  - Navigointi
  - Tulostus
  - Vientimuodot

### Nykyiset Managerit (jo eroteltu)
Onneksi suurin osa toiminnallisuudesta on jo eroteltu eri manager-luokkiin:

1. **DocumentModel** - Dokumentin datan hallinta
2. **DocumentMenuHandler** - Menujen käsittely
3. **DocumentMenuBuilder** - Menubar-komponenttien luominen ✅ JO OLEMASSA
4. **DocumentNavigator** - Dokumenttien navigointi
5. **DocumentTableManager** - Taulukon hallinta
6. **DocumentEntryManager** - Vientien hallinta
7. **DocumentValidator** - Validointi
8. **DocumentPrinter** - Tulostus
9. **DocumentExporter** - Vientimuodot
10. **DocumentDataSourceManager** - Tietokannan hallinta
11. **DocumentBackupManager** - Varmuuskopiointi
12. **DocumentUIBuilder** - UI-komponenttien luominen ✅ JO OLEMASSA
13. **DocumentUIUpdater** - UI-komponenttien päivittäminen ✅ JO OLEMASSA
14. **DocumentToolbarBuilder** - Toolbar-komponenttien luominen ✅ JO OLEMASSA

### Jäljellä Olevat Vastuualueet DocumentFramessa

**HUOM:** Suurin osa UI-logiikasta on jo eroteltu manager-luokkiin! DocumentFrame toimii pääasiassa **orchestratorina** joka koordinoi managerit.

**Todennäköiset jäljellä olevat vastuualueet:**

1. **Orchestration & Coordination** (~800 riviä)
   - Manager-luokkien alustaminen
   - Manager-luokkien koordinoiminen
   - Callback-käsittely
   - Yleinen sovelluksen elinkaari

2. **Dialog-kutsut** (~400 riviä)
   - Kaikkien dialogien näyttäminen
   - Raporttien generointi
   - Asetukset
   - Dialog-coordinator puuttuu?

3. **Tapahtumakäsittely** (~300 riviä)
   - Menu-actions (osittain DocumentMenuHandler:ssa)
   - Button-actions
   - Keyboard shortcuts
   - Window events

4. **Helper-metodit** (~200 riviä)
   - Validointi
   - Konversiot
   - Apufunktiot

5. **State management** (~300 riviä)
   - Komponenttien tilan hallinta
   - Dokumenttien lataus/tallennus
   - Navigointi (osittain DocumentNavigator:ssa)

---

## 🏗️ Refaktoroinnin Strategia

### Vaihtoehto 1: Inkrementaalinen Refaktorointi (SUOSITELTU)

**Yleisperiaate:** Jatketaan samaa linjaa kuin managerit - erotellaan toiminnallisuutta pieniin osiin.

#### Vaihe 1: UI Builderit (2-3h)

**Luodaan erillisiä builder-luokkia UI-komponenttien luomiseen:**

1. **DocumentMenuBarBuilder.kt**
   - `buildMenuBar()` - Menubar
   - `buildFileMenu()`, `buildEditMenu()`, `buildDocumentMenu()`, jne.
   - Vastuu: Menu-komponenttien luominen
   - Koko: ~200-300 riviä

2. **DocumentToolBarBuilder.kt**
   - `buildToolBar()` - Toolbar
   - Vastuu: Toolbar-komponenttien luominen
   - Koko: ~100-150 riviä

3. **DocumentFormBuilder.kt**
   - `buildTextFieldPanel()` - Kentät
   - `buildTotalRow()` - Summarivi
   - `buildSearchBar()` - Hakupalkki
   - `buildStatusBar()` - Statusbar
   - Koko: ~200-300 riviä

**Hyödyt:**
- UI-logiikka erillään business-logiikasta
- Testattavuus parantuu
- Koodin ymmärrettävyys parantuu
- Kotlinissa helpompi koodata

#### Vaihe 2: UI Updaterit (2-3h)

**Luodaan erillinen luokka UI-komponenttien päivittämiseen:**

4. **DocumentUIUpdater.kt**
   - `updateTitle()`, `updatePeriod()`, `updatePosition()`
   - `updateDocument()`, `updateTotalRow()`
   - `updateEntryTemplates()`, `updateDocumentTypes()`
   - `updateRecentDatabasesMenu()`, `updateBackupStatusLabel()`
   - `setComponentsEnabled()`
   - Koko: ~300-400 riviä

**Hyödyt:**
- Update-logiikka keskitetty
- Helpompi debugata
- Yksinkertaisempi testata

#### Vaihe 3: Action Handlers (2-3h)

**Luodaan erillinen luokka tapahtumien käsittelyyn:**

5. **DocumentActionHandler.kt**
   - Menu-actions
   - Button-actions
   - Keyboard shortcuts
   - Koko: ~300-400 riviä

**Hyödyt:**
- Tapahtumakäsittely keskitetty
- Helpompi laajentaa
- Testattavuus parantuu

#### Vaihe 4: Dialog Coordinator (1-2h)

**Luodaan erillinen luokka dialogien koordinoimiseen:**

6. **DocumentDialogCoordinator.kt**
   - Kaikkien dialogien näyttäminen
   - Raporttien generointi
   - Koko: ~200-300 riviä

**Hyödyt:**
- Dialog-logiikka keskitetty
- Helpompi ylläpitää

#### Vaihe 5: DocumentFrame Simplification (3-4h)

**Kun kaikki on eroteltu, DocumentFrame yksinkertaistuu:**

- **Koko:** ~2200 riviä → ~300-500 riviä
- **Vastuu:** Koordinoi managerit ja builderit
- **Rooli:** Orchestrator

---

### Vaihtoehto 2: Big Bang Refaktorointi (EI SUOSITELTU)

**Yleisperiaate:** Migroida koko DocumentFrame kerralla Kotliniin.

**Ongelmia:**
- Liian suuri riski
- Vaikea testata
- Keskeyttää kehityksen
- Vaikea debugata

**Suositus:** ❌ Älä tee tätä

---

## 📋 Suositeltu Toteutusjärjestys

### Sprint 1: UI Builderit (1 viikko, ~10h)

1. **DocumentMenuBarBuilder.kt** (3h)
   - Migroi `createMenuBar()` ja menu-metodit
   - Testaa että menut toimivat

2. **DocumentToolBarBuilder.kt** (2h)
   - Migroi `createToolBar()`
   - Testaa että toolbar toimii

3. **DocumentFormBuilder.kt** (5h)
   - Migroi `createTextFieldPanel()`, `createTotalRow()`, `createSearchBar()`, `createStatusBar()`
   - Testaa että komponentit toimivat

**Tulokset:**
- ~500-750 riviä migroitu Kotliniin
- DocumentFrame.java:sta poistettu ~500 riviä
- Testattavuus parantunut

### Sprint 2: UI Updaterit (1 viikko, ~5h)

4. **DocumentUIUpdater.kt** (5h)
   - Migroi kaikki update-metodit
   - Testaa että päivitykset toimivat

**Tulokset:**
- ~300 riviä migroitu Kotliniin
- DocumentFrame.java:sta poistettu ~300 riviä
- Update-logiikka keskitetty

### Sprint 3: Action Handlers (1 viikko, ~5h)

5. **DocumentActionHandler.kt** (5h)
   - Migroi tapahtumakäsittely
   - Testaa että toiminnot toimivat

**Tulokset:**
- ~300 riviä migroitu Kotliniin
- DocumentFrame.java:sta poistettu ~300 riviä
- Tapahtumakäsittely keskitetty

### Sprint 4: Dialog Coordinator (1 viikko, ~3h)

6. **DocumentDialogCoordinator.kt** (3h)
   - Migroi dialog-kutsut
   - Testaa että dialogit toimivat

**Tulokset:**
- ~200 riviä migroitu Kotliniin
- DocumentFrame.java:sta poistettu ~200 riviä

### Sprint 5: Final Cleanup (1 viikko, ~5h)

7. **DocumentFrame Simplification** (5h)
   - Poista migroidut metodit
   - Yksinkertaista DocumentFrame
   - Testaa että kaikki toimii

**Tulokset:**
- DocumentFrame.java: ~2200 riviä → ~300-500 riviä
- Kaikki UI-logiikka Kotlinissa
- Selkeämpi rakenne

---

## 💡 Tekniset Näkökohdat

### Kotlin Edut

1. **Extension Functions**
   - UI-komponenttien laajennus
   - Apufunktiot

2. **Data Classes**
   - Konfiguraatio-objektit
   - State-objektit

3. **Sealed Classes**
   - Tapahtumatyypit
   - Dialog-tyypit

4. **Null Safety**
   - Turvallisempi koodi
   - Vähemmän NullPointerExceptioneja

5. **Properties**
   - Lyhyempi syntaksi
   - Parempi luettavuus

### Java Yhteensopivuus

- Kotlin-luokat ovat 100% yhteensopivia Java-luokkien kanssa
- DocumentFrame voi kutsua Kotlin-luokkia suoraan
- Ei tarvita muutoksia olemassa oleviin Java-luokkiin

---

## ⚠️ Riskiarvio

### Pienet Riskit
- UI Builderit: Pieni riski, helposti testattavissa
- UI Updaterit: Pieni riski, helposti testattavissa

### Keskisuuret Riskit
- Action Handlers: Keskisuuri riski, monimutkaisempi tapahtumakäsittely
- Dialog Coordinator: Pieni riski, yksinkertaisempi

### Suuret Riskit
- Final Cleanup: Keskisuuri riski, vaatii huolellista testausta

**Yleinen Arvio:** ⚠️ Keskisuuri riski - inkrementaalinen lähestymistapa minimoi riskin

---

## ✅ Success Criteria

1. **Koodin Laatu**
   - DocumentFrame.java < 500 riviä
   - Yksittäiset luokat < 500 riviä
   - Koodin ymmärrettävyys parantunut

2. **Testattavuus**
   - UI Builderit testattavissa
   - UI Updaterit testattavissa
   - Action Handlers testattavissa

3. **Toiminnallisuus**
   - Kaikki toiminnot toimivat kuten ennen
   - Ei regressiovikoja
   - Parempi ylläpidettävyys

4. **Kotlin-osuus**
   - ~500-1000 riviä lisää Kotliniin
   - Kotlin-prosentti: ~20% → ~22-23%

---

## 🚀 Aloita Tästä

**Suosittelen aloittamaan:**

1. **DocumentMenuBarBuilder.kt** - Helpoin, erillinen toiminnallisuus
2. **DocumentToolBarBuilder.kt** - Yksinkertainen
3. **DocumentFormBuilder.kt** - Pienempiä osia

Tämä antaa nopean tuloksen ja parantaa koodin laatua!

---

**Kysymys:** Haluatko että aloitan DocumentMenuBarBuilder.kt:n luomisen?
