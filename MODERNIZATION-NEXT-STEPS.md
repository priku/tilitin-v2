# Modernisaation Seuraavat Askeleet

**Päivämäärä:** 2026-01-02  
**Nykyinen tila:** JavaFX UI 100% valmis, Kotlin 8.2%, Testit 58 kpl

---

## 🎯 Suosittu Järjestys

### 1. Kotlin BaseDialog-pohja (2-3h) ✅ VALMIS

**Status:** ✅ Tehty 2026-01-02

**Tiedosto:** `src/main/kotlin/kirjanpito/ui/javafx/dialogs/BaseDialogFX.kt`

**Tehty:**
- ✅ Yhtenäinen malli kaikille dialogeille
- ✅ OK/Cancel -nappien hallinta
- ✅ show() ja showAndWait() -metodit
- ✅ Yhtenäinen tyyli
- ✅ Abstrakti createContent() -metodi
- ✅ Helppo laajennettavuus

**Hyödyt:**
- Kaikki uudet dialogit käyttävät samaa mallia
- Helpompi ylläpito
- Vähemmän boilerplate-koodia

---

### 2. Migroi yksinkertaiset dialogit Kotliniin (4-6h)

**Ehdokkaat (helpoimmat ensin):**

#### 2.1 AccountSelectionDialogFX (1-2h) ✅ VALMIS
- **Status:** ✅ Tehty 2026-01-02
- **Tiedosto:** `src/main/kotlin/kirjanpito/ui/javafx/dialogs/AccountSelectionDialogFX.kt`
- **Koko:** ~256 riviä Java → ~262 riviä Kotlin
- **Monimutkaisuus:** Keskisuuri - lista + valinta + suodatus
- **Ominaisuudet:**
  - ✅ Käyttää BaseDialogFX-pohjaa
  - ✅ Suodatus tilinumeron ja nimen mukaan
  - ✅ Suosikki-suodatin
  - ✅ Kaksoisnapsautus valintaan
  - ✅ Enter-näppäin valintaan
  - ✅ Yhteensopiva Java-koodin kanssa (@JvmStatic)

#### 2.2 PrintSettingsDialogFX (1-2h) ✅ VALMIS
- **Status:** ✅ Tehty 2026-01-02
- **Tiedosto:** `src/main/kotlin/kirjanpito/ui/javafx/dialogs/PrintSettingsDialogFX.kt`
- **Koko:** ~163 riviä Java → ~177 riviä Kotlin
- **Monimutkaisuus:** Pieni - asetukset
- **Ominaisuudet:**
  - ✅ Käyttää BaseDialogFX-pohjaa
  - ✅ Paperin koko ja orientaatio
  - ✅ Marginaalit Spinner-komponenteilla
  - ✅ Lisäasetukset (ruudukko, sivunumerot)
  - ✅ MainController toimii ilman muutoksia

#### 2.3 AppearanceDialogFX (2-3h) ✅ VALMIS
- **Status:** ✅ Tehty 2026-01-02
- **Tiedosto:** `src/main/kotlin/kirjanpito/ui/javafx/dialogs/AppearanceDialogFX.kt`
- **Koko:** ~151 riviä Java → ~158 riviä Kotlin
- **Monimutkaisuus:** Keskisuuri - teema + fontti + esikatselu
- **Ominaisuudet:**
  - ✅ Käyttää BaseDialogFX-pohjaa
  - ✅ Teeman valinta (Vaalea, Tumma, Järjestelmä)
  - ✅ Fonttikoon valinta (8-24 pt)
  - ✅ Esikatselu fonttikoon muutoksista
  - ✅ Callback teeman muutokselle
  - ✅ MainController päivitetty

**Yhteensä:** 4-7 tuntia  
**Kotlin-prosentti:** 8.2% → ~10-11%

---

### 3. Laajenna testikattavuutta (3-4h)

**Prioriteetit:**

#### 3.1 COAHeadingDAOTest (1-2h)
- **Miksi:** Tilikartan väliotsikot
- **Arvo:** Keskisuuri
- **Vaikeus:** Helppo

#### 3.2 ReportStructureDAOTest (1-2h)
- **Miksi:** Raporttien rakenne
- **Arvo:** Keskisuuri
- **Vaikeus:** Helppo

**Yhteensä:** 2-4 tuntia  
**Testikattavuus:** ~10-15% → ~15-20%

---

### 4. Migroi keskisuuret dialogit (6-8h)

**Ehdokkaat:**

#### 4.1 SettingsDialogFX (3-4h)
- **Koko:** ~400-500 riviä
- **Monimutkaisuus:** Keskisuuri - useita välilehtiä
- **Riippuvuudet:** Keskisuuri
- **Arvio:** 3-4 tuntia

#### 4.2 DocumentTypeDialogFX (2-3h)
- **Koko:** ~300-400 riviä
- **Monimutkaisuus:** Keskisuuri - taulukko + CRUD
- **Riippuvuudet:** Keskisuuri
- **Arvio:** 2-3 tuntia

**Yhteensä:** 5-7 tuntia  
**Kotlin-prosentti:** ~10-11% → ~12-13%

---

### 5. Legacy-koodin poisto (1-2h) 🧹

**Miksi nyt:**
- ✅ Ei vaikuta toiminnallisuuteen
- ✅ Pienentää koodikantaa
- ✅ Selkeyttää projektia
- ✅ Pieni riski

**Tehtävät:**

#### 5.1 Poista käyttämättömät Swing-dialogit (1h)
- 21 Swing-dialogia
- Ei käytössä JavaFX-UI:ssa
- Vahvistettu auditissa

#### 5.2 Poista käyttämättömät Swing-UI-luokat (1h)
- DocumentFrame ja liittyvät luokat
- Ei käytössä JavaFX-UI:ssa
- Vahvistettu auditissa

**Yhteensä:** 2 tuntia  
**Koodikanta:** ~40 tiedostoa vähemmän

---

## 📊 Suosittu Aikataulu

### Viikko 1 (8-10h)
1. ✅ **Kotlin BaseDialog-pohja** (2-3h) - VALMIS 2026-01-02
2. ✅ **AccountSelectionDialogFX migraatio** (1-2h) - VALMIS 2026-01-02
3. ⏳ **PrintSettingsDialogFX migraatio** (1-2h) - SEURAAVA
4. ⏳ **COAHeadingDAOTest** (1-2h)
5. ⏳ **ReportStructureDAOTest** (1-2h)

**Tulokset:**
- BaseDialog-pohja valmis
- 2 dialogia migroitu
- 2 DAO-testiä lisätty
- Kotlin-prosentti: 8.2% → ~10%

### Viikko 2 (8-10h)
1. ✅ **AppearanceDialogFX migraatio** (2-3h)
2. ✅ **SettingsDialogFX migraatio** (3-4h)
3. ✅ **DocumentTypeDialogFX migraatio** (2-3h)
4. ✅ **Legacy-koodin poisto** (1-2h)

**Tulokset:**
- 3 dialogia migroitu
- Legacy-koodi poistettu
- Kotlin-prosentti: ~10% → ~13%

---

## 🎯 Nopein Voitto (aloita tästä!)

**Vaihe 1: Kotlin BaseDialog-pohja (2-3h)** ✅ VALMIS
- ✅ Luotu `BaseDialogFX.kt`
- ✅ Yhtenäinen malli
- ✅ Helpottaa tulevia migraatioita

**Vaihe 2: AccountSelectionDialogFX migraatio (1-2h)** ✅ VALMIS
- ✅ Migroitu Kotliniin
- ✅ Käyttää BaseDialogFX-pohjaa
- ✅ Yhteensopiva Java-koodin kanssa

**Seuraava vaihe:**
- ⏳ SettingsDialogFX migraatio (3-4h) - Keskisuuri dialogi
- ⏳ DocumentUIUpdater migraatio (2h) - Manager-luokka
- ⏳ Laajenna testikattavuutta - COAHeadingDAOTest, ReportStructureDAOTest

---

## 📈 Modernisaation Mittarit

### Nykyinen tila (2026-01-02):
- **Kotlin:** ~10.5% (10 dialogia + BaseDialogFX-pohja + DocumentMenuBuilder)
- **Migroidut dialogit:** 10 / ~31 JavaFX-dialogia
- **Migroidut managerit:** 1 / ~14 manager-luokkaa
- **Testit:** 58 testiä
- **Legacy-koodi:** ~40 tiedostoa

### Tavoite (1-2 kuukautta):
- **Kotlin:** ~15-20% (15-20 dialogia)
- **Testit:** ~80-100 testiä
- **Legacy-koodi:** Poistettu

---

## 💡 Miksi tämä järjestys?

1. **BaseDialog ensin** - Helpottaa kaikkia tulevia migraatioita
2. **Yksinkertaiset dialogit** - Nopeat voitot, vähän riskiä
3. **Testit** - Turvallinen refaktorointi
4. **Keskisuuret dialogit** - Jatkaa vauhtia
5. **Legacy-poisto** - Puhdistaa koodikantaa

---

## 🚀 Aloita tästä

**Suosittelen aloittamaan:**
1. **Kotlin BaseDialog-pohja** - 2-3h, korkea arvo
2. **AccountSelectionDialogFX migraatio** - 1-2h, nopea voitto

Tämä antaa nopean tuloksen ja parantaa koodin laatua!

---

**Kysymys:** Haluatko että aloitan BaseDialog-pohjan luomisen?
