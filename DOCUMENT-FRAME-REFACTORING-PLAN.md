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

#### Vaihe 1: Analysoi Nykyinen Tila (1h)

**Tarkista mitä on jo tehty:**

1. ✅ **DocumentMenuBuilder.java** - JO OLEMASSA
   - Tarkista käytetäänkö oikein
   - Voi olla jo refaktoroitu osittain

2. ✅ **DocumentUIUpdater.java** - JO OLEMASSA
   - Tarkista käytetäänkö oikein
   - Voi olla jo refaktoroitu osittain

3. ✅ **DocumentUIBuilder.java** - JO OLEMASSA
   - Tarkista käytetäänkö oikein
   - Voi olla jo refaktoroitu osittain

4. ✅ **DocumentToolbarBuilder.java** - JO OLEMASSA
   - Tarkista käytetäänkö oikein
   - Voi olla jo refaktoroitu osittain

**Tehtävä:** Analysoi mitä vielä puuttuu ja mitä voidaan parantaa.

#### Vaihe 2: Migroi Managerit Kotliniin (6-8h)

**Migroidaan olemassa olevat manager-luokat Kotliniin:**

1. **DocumentMenuBuilder.kt** (2h)
   - Migroi Java → Kotlin
   - Paranna koodin laatua
   - Käytä Kotlin-ominaisuuksia

2. **DocumentUIUpdater.kt** (2h)
   - Migroi Java → Kotlin
   - Paranna koodin laatua
   - Käytä Kotlin-ominaisuuksia

3. **DocumentUIBuilder.kt** (2h)
   - Migroi Java → Kotlin
   - Paranna koodin laatua
   - Käytä Kotlin-ominaisuuksia

4. **DocumentToolbarBuilder.kt** (1-2h)
   - Migroi Java → Kotlin
   - Paranna koodin laatua
   - Käytä Kotlin-ominaisuuksia

**Hyödyt:**
- Kotlin-ominaisuuksien käyttö
- Parempi null-safety
- Lyhyempi syntaksi
- Parempi ylläpidettävyys

#### Vaihe 3: Dialog Coordinator (2-3h)

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

#### Vaihe 4: Final Simplification (3-4h)

**Luodaan erillinen luokka dialogien koordinoimiseen:**

6. **DocumentDialogCoordinator.kt**
   - Kaikkien dialogien näyttäminen
   - Raporttien generointi
   - Koko: ~200-300 riviä

**Hyödyt:**
- Dialog-logiikka keskitetty
- Helpompi ylläpitää

#### Vaihe 5: DocumentFrame Migraatio (OPTIONAL, 8-10h)

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

### Sprint 1: Nykyisen Tilanteen Analyysi (1 viikko, ~5h)

1. **Analysoi DocumentMenuBuilder.java** (1h)
   - Miten se käytetään?
   - Mitä voidaan parantaa?
   - Voidaanko migroida Kotliniin?

2. **Analysoi DocumentUIUpdater.java** (1h)
   - Miten se käytetään?
   - Mitä voidaan parantaa?
   - Voidaanko migroida Kotliniin?

3. **Analysoi DocumentUIBuilder.java** (1h)
   - Miten se käytetään?
   - Mitä voidaan parantaa?
   - Voidaanko migroida Kotliniin?

4. **Analysoi DocumentToolbarBuilder.java** (1h)
   - Miten se käytetään?
   - Mitä voidaan parantaa?
   - Voidaanko migroida Kotliniin?

5. **Dokumentoi Nykyinen Tila** (1h)
   - Mitä on jo tehty?
   - Mitä vielä puuttuu?
   - Mikä on seuraava askel?

**Tulokset:**
- Selkeä kuva nykyisestä tilasta
- Priorisoidut tehtävät
- Refaktoroinnin suunnitelma

### Sprint 2: Managerien Migraatio Kotliniin (2 viikkoa, ~10h)

1. ✅ **DocumentMenuBuilder.kt** (2h) - VALMIS 2026-01-02
   - ✅ Migroitu Java → Kotlin
   - ✅ Parannettu koodin laatua
   - ✅ DocumentFrame toimii ilman muutoksia

2. **DocumentUIUpdater.kt** (2h)
   - Migroi Java → Kotlin
   - Paranna koodin laatua
   - Testaa että päivitykset toimivat

3. **DocumentUIBuilder.kt** (3h)
   - Migroi Java → Kotlin
   - Paranna koodin laatua
   - Testaa että komponentit toimivat

4. **DocumentToolbarBuilder.kt** (2h)
   - Migroi Java → Kotlin
   - Paranna koodin laatua
   - Testaa että toolbar toimii

**Tulokset:**
- ~800-1000 riviä migroitu Kotliniin
- Managerit nyt Kotlinissa
- Parempi koodin laatu

### Sprint 3: Dialog Coordinator (1 viikko, ~3h)

5. **DocumentDialogCoordinator.kt** (3h)
   - Migroi dialog-kutsut
   - Testaa että dialogit toimivat

**Tulokset:**
- ~200 riviä migroitu Kotliniin
- Dialog-logiikka keskitetty

### Sprint 4: Final Simplification (1 viikko, ~5h)

6. **DocumentFrame Simplification** (5h)
   - Poista migroidut metodit
   - Yksinkertaista DocumentFrame
   - Testaa että kaikki toimii

**Tulokset:**
- DocumentFrame.java: ~2000 riviä → ~800-1000 riviä (arvio)
- Suurin osa UI-logiikasta Kotlinissa
- Selkeämpi rakenne

### Sprint 5: DocumentFrame Migraatio (OPTIONAL, 2 viikkoa, ~10h)

7. **DocumentFrame.kt** (10h) - OPTIONAL
   - Migroi koko DocumentFrame Kotliniin
   - Suuri projekti
   - Vaatii huolellista testausta

**Tulokset:**
- DocumentFrame.kt: ~800-1000 riviä Kotlinissa
- 100% Kotlin UI-kerros
- Suurin osa koodikannasta Kotlinissa

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

**Kysymys:** Haluatko että aloitan nykyisen tilan analysoinnin ja managerien migraation Kotliniin?
