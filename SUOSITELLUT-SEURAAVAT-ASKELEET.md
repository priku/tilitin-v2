# Suositellut Seuraavat Askeleet - Modernisaatio

**Päivämäärä:** 2026-01-02  
**Nykyinen tila:** ~21% Kotlin (60 .kt / 228 .java), 10 dialogia Kotlinissa, 1 manager Kotlinissa

---

## 🎯 Prioriteettijärjestys

### 1. 🚀 Ottaa coroutinit käyttöön käytännössä ⭐ **SUOSITELTU ENSIN!**

**Miksi nyt:**
- ✅ Coroutine-infrastruktuuri on jo valmis (`CoroutineUtils.kt`, `AsyncDAOExtensions.kt`)
- ✅ DAO-kerros on 100% Kotlinissa
- ✅ Voidaan parantaa UI-responsiivisuutta merkittävästi
- ✅ Moderni lähestymistapa taustatyöhön

**Hyödyt:**
- UI ei jäädy pitkiin tietokantaoperaatioihin
- Parempi käyttäjäkokemus
- Moderni asynkroninen koodi
- Vähemmän SwingWorker-boilerplatea

**Ehdokkaat:**

#### 1.1 AccountSelectionDialogFX - Async-haku (2-3h) ⭐ SUOSITELTU ENSIN

**Tiedosto:** `src/main/kotlin/kirjanpito/ui/javafx/dialogs/AccountSelectionDialogFX.kt`

**Nykyinen käyttö:**
- MainController kutsuu: `dialog.setAccounts(new ArrayList<>(accounts))`
- Synkroninen tietokantahaku

**Muutokset:**
- Lisää `loadAccountsAsync()` -metodi
- Käyttää `accountDAO.getAllAsync()` coroutinella
- Näyttää latausindikaattorin haun aikana
- Parantaa responsiivisuutta isolla tilikartalla

**Arvio:** 2-3 tuntia  
**Riskitaso:** Pieni (pienet muutokset, hyvä testattavuus)  
**Vaikutus:** Korkea (parantaa käyttäjäkokemusta merkittävästi)

#### 1.2 MainController - Async tietokannan lataus (3-4h)

**Tiedosto:** `src/main/java/kirjanpito/ui/javafx/MainController.java`

**Muutokset:**
- Korvaa synkroninen tietokannan lataus async-versiolla
- Käyttää `withDB {}` coroutinella
- Näyttää progress-indikaattorin
- Estää UI:n jäätymisen

**Arvio:** 3-4 tuntia  
**Riskitaso:** Keskisuuri (keskeinen toiminnallisuus)  
**Vaikutus:** Korkea (parantaa käynnistyksen kokemusta)

---

### 2. 📦 Migroida util-paketti Kotliniin (4-6h)

**Miksi nyt:**
- ✅ Suhteellisen itsenäisiä luokkia
- ✅ Helppo migroida
- ✅ Kotlin extension functionit tekisivät koodista siistimpää
- ✅ Nopea voitto

**Hyödyt:**
- Lyhyempi syntaksi (extension functions)
- Parempi null-safety
- Vähemmän boilerplatea

**Ehdokkaat (helpoimmat ensin):**

#### 2.1 StringUtils → Kotlin extension functions (1-2h)

**Muutokset:**
- Migroi StringUtils → StringExtensions.kt
- Extension functions: `String.isBlankOrEmpty()`, `String.trimOrNull()`, jne.
- Poista Java-versio
- Päivitä käyttöpaikat

**Arvio:** 1-2 tuntia  
**Riskitaso:** Pieni

#### 2.2 DateUtils → Kotlin extension functions (1-2h)

**Muutokset:**
- Migroi DateUtils → DateExtensions.kt
- Extension functions päivämääräkäsittelyyn
- Parempi null-safety
- Poista Java-versio

**Arvio:** 1-2 tuntia  
**Riskitaso:** Pieni

#### 2.3 NumberUtils → Kotlin extension functions (1-2h)

**Muutokset:**
- Migroi NumberUtils → NumberExtensions.kt
- Extension functions numeroiden muotoiluun
- Poista Java-versio

**Arvio:** 1-2 tuntia  
**Riskitaso:** Pieni

**Yhteensä:** 3-6 tuntia  
**Kotlin-prosentti:** ~21% → ~23-24%

---

### 3. 🗃️ Migroida models-paketti Kotliniin (8-12h)

**Miksi nyt:**
- ✅ DAO-kerros on jo 100% Kotlinissa
- ✅ Data classit vähentävät boilerplatea merkittävästi
- ✅ Parempi null-safety

**Huomio:** Tämä viittaa `models`-pakettiin, joka sisältää business-logiikkaa (ei data class -versioita).

**Ehdokkaat:**

#### 3.1 DocumentModel migraatio (3-4h)

**Muutokset:**
- Migroi DocumentModel Java → Kotlin
- Käyttää data class -versioita
- Vähentää boilerplatea
- Parantaa null-safetya

**Arvio:** 3-4 tuntia  
**Riskitaso:** Keskisuuri (keskeinen business-logiikka)

#### 3.2 AccountModel migraatio (2-3h)

**Muutokset:**
- Migroi AccountModel Java → Kotlin
- Yksinkertaisempi kuin DocumentModel
- Parantaa koodin laatua

**Arvio:** 2-3 tuntia  
**Riskitaso:** Keskisuuri

#### 3.3 Muut model-luokat (3-5h)

**Muutokset:**
- EntryTemplateModel
- DocumentTypeModel
- PropertiesModel
- jne.

**Arvio:** 3-5 tuntia  
**Riskitaso:** Keskisuuri

**Yhteensä:** 8-12 tuntia  
**Kotlin-prosentti:** ~23-24% → ~26-28%

---

### 4. 📊 Migroida reports-paketti (10-15h)

**Miksi nyt:**
- ✅ Hyötyisi Kotlinin string templatesista
- ✅ Raporttien generointi olisi selkeämpää
- ✅ Vähentäisi boilerplatea

**Ehdokkaat:**

#### 4.1 Yksinkertaiset raportit ensin (5-8h)

- AccountSummary
- GeneralJournal
- GeneralLedger

**Arvio:** 5-8 tuntia  
**Riskitaso:** Keskisuuri

#### 4.2 Monimutkaisemmat raportit (5-7h)

- BalanceSheet
- IncomeStatement
- VATReport

**Arvio:** 5-7 tuntia  
**Riskitaso:** Keskisuuri

**Yhteensä:** 10-15 tuntia  
**Kotlin-prosentti:** ~26-28% → ~30-35%

---

### 5. 🧪 Lisätä UI-testit JavaFX-komponenteille (4-6h)

**Miksi nyt:**
- ✅ TestFX-kirjasto jo käytössä
- ✅ Parantaisi refaktoroinnin turvallisuutta
- ✅ Varmistaa UI-toiminnallisuuden

**Ehdokkaat:**

#### 5.1 Dialog-testit (2-3h)

- AboutDialogFX test
- HelpDialogFX test
- PrintSettingsDialogFX test
- AppearanceDialogFX test

**Arvio:** 2-3 tuntia  
**Riskitaso:** Pieni

#### 5.2 Table-testit (2-3h)

- EntryTable testit
- Navigation testit
- Cell editing testit

**Arvio:** 2-3 tuntia  
**Riskitaso:** Pieni

**Yhteensä:** 4-6 tuntia  
**Testikattavuus:** ~15% → ~20-25%

---

## 📋 Suositeltu Aikataulu

### Viikko 1-2 (8-10h) ⭐ SUOSITELTU ALOITTAA TÄSTÄ

1. ✅ **AccountSelectionDialogFX async-haku** (2-3h) ⭐ SUOSITELTU ENSIN
2. ✅ **StringUtils → Kotlin** (1-2h)
3. ✅ **DateUtils → Kotlin** (1-2h)
4. ✅ **UI-testit (dialogit)** (2-3h)

**Tulokset:**
- ✅ Coroutinit käytössä käytännössä
- ✅ Util-paketti osittain Kotlinissa
- ✅ Parannettu testikattavuus
- ✅ Kotlin-prosentti: ~21% → ~24%

### Viikko 3-4 (10-12h)

1. ✅ **MainController async-lataus** (3-4h)
2. ✅ **DocumentModel migraatio** (3-4h)
3. ✅ **NumberUtils → Kotlin** (1-2h)
4. ✅ **UI-testit (taulukot)** (2-3h)

**Tulokset:**
- ✅ Coroutinit laajemmin käytössä
- ✅ Models-paketti osittain Kotlinissa
- ✅ Parannettu testikattavuus
- ✅ Kotlin-prosentti: ~24% → ~27%

### Viikko 5-6 (10-15h)

1. ✅ **Raportit migraatio (yksinkertaiset)** (5-8h)
2. ✅ **Muut model-luokat** (3-5h)

**Tulokset:**
- ✅ Reports-paketti osittain Kotlinissa
- ✅ Models-paketti lähes valmis
- ✅ Kotlin-prosentti: ~27% → ~32%

---

## 🎯 Nopein Voitto (aloita tästä!)

**Vaihe 1: AccountSelectionDialogFX async-haku (2-3h)** ⭐ SUOSITELTU ENSIN

**Miksi:**
- ✅ Coroutine-infrastruktuuri on valmis
- ✅ Pieni muutos, iso vaikutus
- ✅ Parantaa käyttäjäkokemusta
- ✅ Helppo testata

**Tämä antaa:**
- ✅ Nopean tuloksen (2-3h)
- ✅ Modernin asynkronisen koodin
- ✅ Parannetun UI-responsiivisuuden
- ✅ Hyvän esimerkin muille coroutine-käytöille

---

## 📊 Modernisaation Mittarit

### Nykyinen tila (2026-01-02):
- **Kotlin:** ~21% (60 .kt / 228 .java)
- **Migroidut dialogit:** 10 / ~31 JavaFX-dialogia
- **Migroidut managerit:** 1 / ~14 manager-luokkaa
- **Testit:** 58 testiä
- **Coroutinit:** Infrastruktuuri valmis, käyttö odottaa

### Tavoite (1-2 kuukautta):
- **Kotlin:** ~30-35% (70-80 .kt)
- **Coroutinit:** Käytössä 2-3 kohdassa
- **Testit:** ~80-100 testiä
- **UI-testit:** 5-10 testiä

---

## 💡 Miksi tämä järjestys?

1. **Coroutinit ensin** - Infrastruktuuri on valmis, hyvä hyödyntää se nyt
2. **Util-paketti** - Helppo voitto, parantaa koodin laatua
3. **Models-paketti** - Keskeinen business-logiikka, iso vaikutus
4. **Reports-paketti** - Hyötyy Kotlinin string templatesista
5. **UI-testit** - Parantaa refaktoroinnin turvallisuutta

---

## 🚀 Aloita Tästä

**Suosittelen aloittamaan:**

1. **AccountSelectionDialogFX async-haku** - 2-3h, korkea arvo, pieni riski
2. **StringUtils → Kotlin** - 1-2h, nopea voitto

Tämä antaa nopean tuloksen ja parantaa koodin laatua!

---

**Kysymys:** Haluatko että aloitan AccountSelectionDialogFX:n async-haun toteutuksen?
