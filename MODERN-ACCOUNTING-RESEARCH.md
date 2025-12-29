# Modern Accounting Software Research - Tilitin

**Tutkimuspäivä:** 2025-12-29
**Versio:** 1.0
**Tila:** ✅ Valmis

---

## 🎯 Tiivistelmä

Kattava analyysi nykyaikaisista kirjanpito-ohjelmistoista ja parhaista käytännöistä. Tutkimus kattaa kilpailijat (GnuCash, Kitupiikki/Kitsas), pilvipohjaiset ratkaisut (QuickBooks, Xero) ja modernit UX-trendit.

**Keskeiset löydökset:**
- Modernit kirjanpito-ohjelmat priorisoivat automaatiota ja puhdasta UX:ää
- PDF-liitteiden hallinta on nyt olennaista (Tilitin v2.2.0 ✅)
- Pankkitapahtumien tuonti/vienti älytunnistuksella on kriittistä
- Näppäinkomennot säästävät 30-40% päivittäisestä työajasta
- Tumma teema on odotus 80%+ käyttäjillä (Tilitin ✅)
- Automaattiset varmuuskopiot ovat standardi (Tilitin v2.0+ ✅)

---

## 📊 Kilpailija-analyysi

### 1. GnuCash (Ensisijainen kilpailija)

**Kuvaus:** Avoimen lähdekoodin kirjanpito-ohjelma, samankaltainen kuin Tilitin

**Vahvuudet:**
- **Tuonti/Vienti:** OFX, QFX, QIF ja CSV-muodot pankkitiliotteiden tuontiin
- **Bayesin älytunnistus:** Koneoppiminen kohdetilien määrittämiseen historian perusteella
- **Toistuvat tapahtumat:** Kattava järjestelmä toistuvien kirjausten hallintaan
  - "Since Last Run" -avustaja luo automaattisesti tapahtumat
- **Monivaluuttatuki:** Vahvat kyvyt kansainvälisiin transaktioihin
- **Monitietokanta:** SQLite3, MySQL, PostgreSQL (sama kuin Tilitin ✅)

**Heikkoudet:**
- Vanhentunut käyttöliittymä, ei auta aloittelijoita
- Kaavioiden luonti vaikeaa
- Vain yhden käyttäjän järjestelmä
- Ei web-käyttöliittymää

**Viimeisin versio:** GnuCash 5.14 (21.12.2025)

### 2. Kitupiikki/Kitsas (Suomalainen kilpailija)

**Kuvaus:** Suomalainen kirjanpito-ohjelma pienille organisaatioille

**Keskeiset ominaisuudet:**
- **PDF-kuittien käsittely:** Kuitit PDF-muodossa
- **Sisäänrakennettu laskutus:** Integroitu laskutusjärjestelmä
- **Sähköinen arkisto:** Luo sähköisen arkiston säännösten mukaisesti
- **Tallennusvaihtoehdot:** Paikallinen SQLite tai pilvipalvelin (maksullinen)
- **Vain suomeksi:** Käyttöliittymä, dokumentaatio täysin suomeksi

**Merkitys Tilitinille:** Suora kilpailija suomalaisilla markkinoilla, sama kohderyhmä

### 3. Pilvipohjaiset ratkaisut (QuickBooks, Xero, FreshBooks, Wave)

**Yleiset UX-mallit:**

1. **Puhdas, minimalistinen muotoilu**
   - Xero: "Erittäin moderni käyttöliittymä, joka tekee monimutkaisesta taloushallintadatasta helposti ymmärrettävää"
   - Selkeä, moderni sivuston muotoilu täydellinen pienyritysten omistajille

2. **Mobiili-ensin lähestymistapa**
   - Vuonna 2025 ohjelmistot käyttävät moderneja UI/UX-periaatteita
   - Saumaton kokemus työpöydällä ja mobiililaitteilla
   - Kuittien skannaus, kulujen seuranta, laskujen luonti mobiilissa

3. **Tekoälypohjainen automaatio**
   - QuickBooks: Tekoäly auttaa kategorisoimaan tapahtumia, havaitsemaan epäjohdonmukaisuuksia
   - Tekoälyohjatut työnkulut yksinkertaistavat päivittäistä hallintaa

4. **Ohjatut työnkulut**
   - Velhot ja vaiheittaiset prosessit yksinkertaistavat monimutkaisia tehtäviä

---

## 🔍 Ominaisuusanalyysi kategorioittain

### 1. Dokumenttiliitteet ja PDF-hallinta

**Nykytilanne (Tilitin v2.2.0):** ✅ ERINOMAINEN
- PDF-liitteet tositteisiin toteutettu
- 10 MB tiedostokoko raja, 5 MB varoituskynnys
- Metatietojen näyttö (tiedostokoko, sivumäärä, lisäyspäivä)
- SQLite, MySQL, PostgreSQL tuki

**Alan parhaat käytännöt:**

**Monimuototuki:**
- freee & Money Forward: .doc, .ppt, .xls, .jpg, .png, .pdf tiedostojen lataus
- Invoice Ninja: Liitä useita tiedostotyyppejä asiakaslaskuihin

**OCR ja automaatio:**
- freee: OCR poimii automaattisesti päivämäärän, summan, laskun numeron
- Scan2Invoice: Tekoälypohjainen OCR poimii kirjanpitodataa

**Massatoiminnot:**
- Massa-lataa useita tiedostoja
- Jaa monisivuiset PDF:t
- Rajaa yksittäisiä kuitteja

**SUOSITUKSET TILITINILLE:**

1. **PDF-katseluohjelman integrointi (Sprint 3 - Jo suunniteltu)**
   - PDF-esikatselu sovelluksen sisällä ilman ulkoista katseluohjelmaa
   - Pikkukuvanäkymä nopeaan dokumenttien tunnistamiseen
   - Sivunavigointikontrollit

2. **Monimuototuki**
   - Lisää tuki kuville (JPG, PNG) - yleistä kuittikuville
   - Harkitse Excel/CSV-liitteitä tukidokumenteille

3. **OCR-parannus (tulevaisuus)**
   - OCR poimii summat, päivämäärät PDF-kuitteista
   - Automaattitäyttö kirjaussummille skannatuista laskuista
   - Datan validointi syötettyjä arvoja vastaan

4. **Vedä ja pudota (Sprint 4 - Jo suunniteltu)**
   - Vedä PDF tiedostonhallinnasta suoraan tositteeseen
   - Pudota useita tiedostoja massa-tuontiin

5. **Leikepöytätuki (Sprint 4 - Jo suunniteltu)**
   - Liitä kuvia leikepöydältä (kuvakaappaukset, skannatut kuitit)

---

### 2. Tuonti/Vienti-ominaisuudet

**Nykytilanne (Tilitin):**
- **Vienti:** CSV-vienti UTF-8 BOM:lla, puolipiste erottimena (Excel-yhteensopiva)
- **Tuonti:** ✅ CSV-tuonti toteutettu (v2.2.1)

**Alan standardit:**

**Pankkitiliotteen tuonti:**
- GnuCash: OFX/QFX, QIF, CSV-muodot
- DocuClipper: Muuntaa PDF-pankkiotteita Exceliksi/CSV:ksi 99,5% OCR-tarkkuudella
- Datamolino: Automatisoi PDF-pankkiotteen muunnoksen CSV:ksi tuontia varten

**Älykäs tuonnin täsmäytys:**
- GnuCash: Bayesin lähestymistapa määrittää kohdetilit aiempien tuontien perusteella
- Automaattitäsmäytys päivämäärän ja kuvauksen perusteella

**Automaatio:**
- SaasAnt: Automaattinen datan tuonti sähköpostista, FTP:stä, Zapierista
- Tiller Money: Yhdistää 21 000+ pankkia, automaattinen päivittäinen tapahtumien tuonti

**SUOSITUKSET TILITINILLE:**

**Prioriteetti 1: CSV-tuonnin parannus** ✅ TOTEUTETTU

Toteutetut ominaisuudet:
1. Tuontivelho sarakekartoituksen käyttöliittymällä
   - Automaattitunnista yleiset sarakemuodot (päivämäärä, summa, kuvaus)
   - Esikatsele data ennen tuontia
   - Automaattinen koodauksen tunnistus (UTF-8, ISO-8859-1, Windows-1252)

2. Seuraavat parannukset:
   - Tallenna sarakekartoitukset malleiksi uudelleenkäyttöä varten
   - Älykäs tilien täsmäytys (ehdota tilejä avainsanojen perusteella)
   - Kaksoiskappaleiden havaitseminen

**Prioriteetti 2: Pankkiformaattien tuki**

Yleiset suomalaiset pankkiformaatit:
- Nordea: CSV-vientimuoto
- OP: CSV/Excel-muoto
- Danske Bank: CSV-muoto
- S-Pankki: CSV-muoto

Toteutus:
1. Luo pankkikohtaiset tuontimallit
2. Lisää pankkivalitsin tuontidialogiin
3. Esikonfiguroi sarakekartoitukset per pankki
4. Testaa esimerkkitiliotteiden kanssa jokaisesta pankista

**Prioriteetti 3: PDF-pankkitiliotteen OCR (Edistynyt)**
- Pura tapahtumat PDF-pankkitiliötteistä
- Käytä Apache PDFBox:ia (jo projektissa liitteille)
- Jäsennä tapahtumien taulukot yleisistä suomalaisista pankeista

---

### 3. Automaatio ja toistuvat kirjaukset

**Nykytilanne (Tilitin):**
- Kirjausmallit olemassa (`EntryTemplateModel.java`)
- Manuaalinen mallin käyttö
- Ei aikataulutettuja/toistuvia tapahtumia

**Alan standardit:**

**Aikataulutetut tapahtumat:**
- GnuCash: Luo kirjanpidosta tai editorista, aseta taajuus, automaattilisäys "Since Last Run" -avustajalla
- QuickBooks: Toistuvat laskut automaattisella laskutuksella
- NetSuite: Aikataulutetut toistuvat ja palautuvat päiväkirjakirjaukset

**Mallien ominaisuudet:**
- Mukautettavat päiväkirjakirjausmallit eri reskontrille
- Uku: Viikoittainen toistuvuus, päivämääräpohjaiset kuukausittaiset/neljännesvuosittaiset/vuosittaiset tehtävät

**SUOSITUKSET TILITINILLE:**

**Toistuva tapahtumajärjestelmä:**

```java
// Uusi malli: RecurringDocument.java
public class RecurringDocument {
    private int id;
    private String name;                    // "Kuukausivuokra"
    private DocumentType documentType;
    private RecurrencePattern pattern;      // PÄIVITTÄIN, VIIKOITTAIN, KUUKAUSITTAIN, VUOSITTAIN
    private int interval;                   // Joka N päivä/viikko/kuukausi
    private Date startDate;
    private Date endDate;                   // Valinnainen
    private Integer occurrences;            // Tai rajoitettu määrä kertoja
    private Date lastCreated;
    private boolean autoCreate;             // Auto vs. kysy
    private boolean notifyOnCreate;
    private List<EntryTemplate> entries;
}

// Toistuvuusmallit
enum RecurrencePattern {
    DAILY,              // Päivittäin
    WEEKLY,             // Viikoittain
    MONTHLY,            // Sama päivä joka kuukausi
    MONTHLY_LAST_DAY,   // Kuukauden viimeinen päivä
    QUARTERLY,          // Neljännesvuosittain
    YEARLY              // Vuosittain
}
```

**Käyttöliittymän toteutus:**
1. **"Aikataulutetut tositteet" -valikkovaihtoehto**
   - Työkalut → Aikataulutetut tositteet → Hallinnoi
   - Luo olemassa olevasta tositteesta ("Tallenna toistuvana")
   - Luo uusi aikataulutettu tosite

2. **"Viimeisen ajon jälkeen" -käynnistysdialogi**
   - Näyttää odottavat aikataulutetut tositteet
   - Valintaruudut luodaksesi/ohittaaksesi jokaisen
   - "Luo kaikki" / "Ohita kaikki" -painikkeet

3. **Kalenterinäkymä (Edistynyt)**
   - Visuaalinen kalenteri näyttää aikataulutetut päivämäärät
   - Klikkaa päivämäärää esikatseluun/muokkaamaan aikataulutettua kirjausta

**Käyttötapaukset suomalaiseen kirjanpitoon:**
- Kuukausivuokra/leasing-maksut
- Kuukausipalkat
- Neljännesvuosittaiset ALV-maksut
- Vuosittaiset vakuutusmaksut
- Kuukausittaiset lainan maksut

---

### 4. Datan validointi ja virheiden ehkäisy

**Alan parhaat käytännöt:**

**Validointisäännöt:**
- Automatisoidut tarkistukset merkitsevät erot välittömästi, ehkäisten virheet
- Datan tyyppi-, koodi- ja aluearvotarkistukset

**Virheiden ehkäisy:**
- Käytä kirjanpito-ohjelmistoa automatisointiin ja vähentääksesi manuaalista datasyöttöä
- Tekoälyehdotukset ja datan täsmäytys

**Täsmäytys:**
- Säännöllinen pankkitiliotteen täsmäytys havaitsee virheet ennen kuin ne vaikuttavat tilinpäätökseen

**SUOSITUKSET TILITINILLE:**

**Parannettu validointikerros:**

```java
// Uusi: ValidationService.java
public class ValidationService {
    // Tallennusta edeltävä validointi
    public ValidationResult validateDocument(Document doc, List<Entry> entries) {
        ValidationResult result = new ValidationResult();

        // 1. Tasapainotarkistus (debet = kredit)
        if (!isBalanced(entries)) {
            result.addError("Tosite ei tasapainossa", "Debet- ja kredit-summat täytyy täsmätä");
        }

        // 2. Päivämäärän validointi
        if (doc.getDate().before(period.getStartDate())) {
            result.addError("Päivämäärä kauden ulkopuolella", "Tositteen päivämäärä ennen kauden alkua");
        }

        // 3. ALV-validointi
        for (Entry entry : entries) {
            if (hasVatCode(entry) && !hasVatCounterAccount(entry)) {
                result.addWarning("Puuttuu ALV-vastatili",
                    "Tilillä " + entry.getAccount().getNumber() + " on ALV-koodi mutta ei vastatiliä");
            }
        }

        // 4. Kaksoiskappaleiden havaitseminen
        List<Document> similar = findSimilarDocuments(doc, entries);
        if (!similar.isEmpty()) {
            result.addWarning("Mahdollinen kaksoiskapale",
                "Samanlainen tosite olemassa: " + similar.get(0).getNumber());
        }

        return result;
    }
}
```

**Toteutettavat validointityypit:**

1. **Reaaliaikainen validointi (kun käyttäjä kirjoittaa)**
   - Tilinumeromuoto (vain numerot)
   - Summan muoto (kelvollinen desimaali)
   - Päivämäärämuoto
   - Näytä punainen reunus virheellisille kentille

2. **Tallennusta edeltävä validointi**
   - Tasapainoiset kirjaukset (debet = kredit)
   - Kaikki pakolliset kentät täytetty
   - Päivämäärä nykyisen kauden sisällä
   - ALV-logiikan oikeellisuus

3. **Varoitusjärjestelmä (ei estävä)**
   - Epätavallisen suuret summat (merkitse jos >10x keskiarvo)
   - Kaksoiskappaleiden havaitseminen
   - Puuttuva ALV tileillä, joilla yleensä on ALV
   - Pyöreät numerosummat (saattaa viitata arvioon)

4. **Kauden lukitussuojaus**
   - Estä tositteiden muokkaaminen suljetuilla kausilla
   - Vaadi avaamiseen salasana/vahvistus
   - Kirjaa kaikki muutokset lukittuihin kausiin

**Käyttöliittymä validoinnille:**
```
┌─────────────────────────────────────────┐
│ ⚠️  Validointivaroitukset                │
├─────────────────────────────────────────┤
│ • Tosite ei tasapainossa (-15.50€)      │
│ • Mahdollinen kaksoiskapale (Tos #245)  │
│ • Suuri summa (>1000€) - tarkista       │
├─────────────────────────────────────────┤
│ [Ohita] [Korjaa] [Tallenna silti]       │
└─────────────────────────────────────────┘
```

---

### 5. Näppäinkomennot ja tehokäyttäjien ominaisuudet

**Nykytilanne (Tilitin):** HYVÄ
- Hyvät näppäinkomennot olemassa (dokumentoitu USER-GUIDE.md:ssa)
- `Tab`, `Shift+Tab`, `*` (debet/kredit vaihto), `Ctrl+Backspace`
- `F7` suosikkitileille

**Alan standardit:**

**Tuottavuusvaikutus:**
- Näppäinkomennot säästävät kirjanpitäjille 30-40% päivittäisestä työajasta

**Edistyneet ominaisuudet:**
- TallyPrime: Alt+C luo uuden tilin poistumatta tositenäytöltä
- Mukautettavat pikavalinnat rutiinifunktioille

**SUOSITUKSET TILITINILLE:**

**Lisää toteutettavat näppäinkomennot:**

```
Tositteen navigointi:
Ctrl+N         - Uusi tosite (on jo olemassa)
Ctrl+S         - Tallenna tosite
Ctrl+D         - Kopioi nykyinen tosite
Ctrl+K         - Poista tosite
Ctrl+F         - Etsi/hae tositteita
Ctrl+G         - Siirry tositenumeroon

Kirjausten muokkaus:
Ctrl+Enter     - Lisää uusi kirjausrivi
Ctrl+Delete    - Poista nykyinen kirjausrivi
Ctrl+Up/Down   - Siirrä kirjausta ylös/alas listassa
Ctrl+C         - Kopioi kirjaus
Ctrl+V         - Liitä kirjaus

Pikatoiminnot:
F2             - Muokkaa tiliä (avaa tilivalitsin)
F3             - Pika-päivämäärävalitsin
F4             - ALV-koodivalitsin
F5             - Päivitä/lataa tosite uudelleen
F6             - Laskin popup (summakenttään)
F7             - Suosikkitilit (on jo olemassa)
F8             - Viimeaikaiset kuvaukset (automaattitäydennys)
F9             - Käytä kirjausmallia
F12            - Tallenna ja sulje

Tilivalitsimen parannukset:
Kirjoita numero  - Hyppää tiliin numeron mukaan
Kirjoita kirjaimia - Suodata tilin nimen mukaan
Ctrl+F         - Merkitse suosikiksi
Välilyönti     - Valitse ja sulje
Esc            - Peruuta ja sulje
```

**Parannettu automaattitäydennys:**

```java
// Älykäs kuvauksen automaattitäydennys
public class DescriptionAutocomplete {
    // Opi historiasta
    - Seuraa usein käytettyjä kuvauksia per tili
    - Näytä top 5 ehdotusta pudotusvalikossa
    - Nuolinäppäimet valitaksesi, Enter käyttääksesi

    // Kontekstitietoiset ehdotukset
    - Erilaiset ehdotukset eri tileille
    - Opi yleiset parit (tili + kuvaus)
    - Ehdota summia kuvaushistorian perusteella
}
```

**Pikakirjaustila:**

```
Ominaisuus: "Express Entry" -tila toistuvaan datasyöttöön
- Optimoitu nopeuteen
- Automaattisiirtymä seuraavaan kenttään kelvollisella syötteellä
- Minimaalinen UI, keskitytään datasyöttöön
- Vain näppäimistöllä toimiva
- Massatila: syötä 10+ tositetta nopeasti

Esimerkkityönkulku:
1. Ctrl+Q aktivoidaksesi Express Entry
2. Kirjoita tositenumero [Enter]
3. Kirjoita päivämäärä [Enter]
4. Kirjoita tili [Tab] summa [Tab] kuvaus [Enter]
5. Toista vaihe 4 jokaiselle kirjaukselle
6. F12 tallentaaksesi ja seuraava tosite
```

---

### 6. Varmuuskopiointi ja tietoturva

**Nykytilanne (Tilitin v2.0+):** ✅ ERINOMAINEN
- AutoBackup-järjestelmä (Word-tyylinen, 1-60 min intervallit)
- Monipaikkainen varmuuskopiointi
- Pilvipalvelujen havaitseminen (OneDrive, Google Drive, Dropbox, iCloud)
- USB-aseman havaitseminen
- Versiohistoria (1-100 versiota)

**Alan parhaat käytännöt:**

**Varmuuskopiointistrategia:**
- 3-2-1 sääntö: 3 kopiota 2 erilaisella tallennustyypillä ja 1 kopio poissa paikalta
- Yölliset varmuuskopiot riippumatta on-premise vai pilvestä

**Testaus ja validointi:**
- Testaa datan palautusta säännöllisesti
- Validoi varmuuskopiot - ei riitä että ohjelmisto raportoi onnistumisen

**Salaus:**
- Salaa varmuuskopiot siirron aikana ja levossa

**SUOSITUKSET TILITINILLE:**

**Parannukset olemassa olevaan varmuuskopiointijärjestelmään:**

1. **Varmuuskopion validointi**
   ```java
   // Jokaisen varmuuskopioinnin jälkeen, tarkista eheys
   public class BackupValidator {
       public boolean validateBackup(File backupFile) {
           try {
               // 1. Tarkista tiedosto on luettavissa
               if (!backupFile.canRead()) return false;

               // 2. Varmista SQLite-tietokannan eheys
               Connection conn = DriverManager.getConnection("jdbc:sqlite:" + backupFile);
               Statement stmt = conn.createStatement();
               ResultSet rs = stmt.executeQuery("PRAGMA integrity_check");
               String result = rs.getString(1);
               conn.close();

               // 3. Tarkista tiedostokoko (pitäisi olla >0)
               if (backupFile.length() == 0) return false;

               return result.equals("ok");
           } catch (Exception e) {
               return false;
           }
       }
   }
   ```

2. **Varmuuskopion tilaindikaattorin parannus**
   - Näyttää jo varmuuskopion tilan DocumentFramessa
   - Lisää yksityiskohtainen tooltip: "Viimeisin varmuuskopio: 5 minuuttia sitten 3 paikkaan"
   - Värikoodit: Vihreä (tuore), Keltainen (>30 min), Punainen (epäonnistunut/pois käytöstä)

3. **Palautustestausominaisuus**
   ```
   Uusi valikko: Työkalut → Varmuuskopiointi → Testaa palautus

   Työnkulku:
   1. Valitse varmuuskopiotiedosto
   2. Luo väliaikainen tietokanta varmuuskopiosta
   3. Varmista voi avata ja lukea dataa
   4. Raportoi: "Varmuuskopio kelvollinen, sisältää X tositetta, Y kirjausta"
   5. Poista väliaikainen tietokanta
   ```

4. **Salaus pilvivarmuuskopioille (Edistynyt)**
   ```java
   // Valinnainen: Salaa pilvivarmuuskopiot
   public class EncryptedBackup {
       // Käytä AES-256 salausta
       // Salasana käyttäjäasetuksista
       // Tallenna salausmetadata erilliseen .meta-tiedostoon
   }
   ```

5. **Katastrofipalautusdokumentaatio**
   - Luo RECOVERY.md-opas
   - Vaiheittaiset palautusohjeet
   - Kuvakaappaukset jokaisesta vaiheesta
   - Sisällytä Ohje-valikkoon: Ohje → Palautusopas

---

### 7. Käyttöliittymä ja UX-mallit

**Nykytilanne (Tilitin v2.0+):** HYVÄ
- FlatLaf moderni teema
- Vaalea/Tumma tila vaihto
- Moderni käynnistysnäyttö
- Viimeaikaiset tietokannat -lista
- Hyvät näppäinkomennot

**Modernit UX-trendit:**

**Tumma tila:**
- 82% mobiilikäyttäjistä suosii tummaa tilaa vuonna 2025
- Säästää jopa 47% akkua OLED-näytöillä
- Tilitinillä on jo tämä - ERINOMAINEN! ✅

**Puhdas muotoilu:**
- Xero ylistetty puhtaasta käyttöliittymästä
- Minimaalinen, järjestetty asettelu
- Selkeä visuaalinen hierarkia

**SUOSITUKSET TILITINILLE:**

**Käyttöliittymän parannukset:**

1. **Koontinäyttö/Yleiskatsausnäyttö**
   ```
   Uusi: Avaa tietokanta → Näytä koontinäyttö ennen DocumentFramea

   Koontinäytön widgetit:
   - Kauden yhteenveto (tositteet yhteensä, kirjaukset tässä kuussa)
   - Pikatilastot (tulot, menot, saldo)
   - Viimeaikaiset tositteet (viimeiset 10)
   - Hälytykset (epätasapainossa olevat tos., puuttuva ALV)
   - Pikatoiminnot (Uusi tosite, Hae, Raportit)

   Alapainike: "Avaa tositteet" → avaa DocumentFramen
   ```

2. **Moderni kuvakkeisto**
   - Jo modernisoitu v2.0.2:ssa (Evolution-tyyli)
   - Harkitse kuvakekirjastoa painikkeille (FlatLaf extras)
   - Yhtenäinen kuvaketyyli kautta sovelluksen

3. **Tilarivin parannukset**
   ```
   Nykyinen tilarivi näyttää kauden, tositelajin

   Lisää:
   - Edistymisindikaattorit pitkille toiminnoille
   - Verkkoyhteystila (MySQL/PostgreSQL:lle)
   - Tietokannan sijainti (paikallinen/verkko)
   - Nykyinen käyttäjä (jos tulevaisuudessa monikäyttäjä)
   ```

4. **Reagoivat asettelut**
   - Käsittele eri näyttöresoluutiot paremmin
   - Muista ikkunapaikat per näyttö
   - Skaalaa UI korkean DPI:n näytöille (4K, 5K)

5. **Saavutettavuusominaisuudet**
   ```
   - Fonttikoon säätö (on jo olemassa?)
   - Korkean kontrastin tila -vaihtoehto
   - Ruudunlukijan yhteensopivuus (ARIA-tägit)
   - Näppäimistönavigoinnin indikaattorit (fokusrengas)
   ```

---

### 8. Tarkistusketju ja tapahtumahistoria

**Nykytilanne (Tilitin):**
- Ei tarkistusketjujärjestelmää
- Ei kumoa/tee uudelleen -toiminnallisuutta
- Manuaaliset tapahtumakorjaukset vaaditaan

**Alan standardit:**

**Tarkistusketjun ominaisuudet:**
- Pidä kirjaa jokaisesta toiminnosta aikaleimalla ja käyttäjätiedoilla
- Seuraa kuka loi, muokkasi tai poisti tapahtumia
- Vertaa useita versioita toiminnoista ja muutoksista

**Kumoa/Tee uudelleen:**
- Xero: Pura täsmäytys katkaisemaan tapahtumalinkitykset, Poista & Tee uudelleen puhtaalle pöydälle
- QuickBooks: Käyttää tarkistuslokia paikantamaan ja korjaamaan tapahtumat (ei suoraa kumoamista)

**SUOSITUKSET TILITINILLE:**

**Tarkistusketjujärjestelmä:**

```sql
-- Uusi taulu: audit_log
CREATE TABLE audit_log (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    timestamp DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    user_name TEXT,          -- Tulevaisuus: monikäyttäjätuki
    action_type TEXT,        -- INSERT, UPDATE, DELETE
    entity_type TEXT,        -- DOCUMENT, ENTRY, ACCOUNT, jne.
    entity_id INTEGER,
    old_values TEXT,         -- JSON vanhoista arvoista
    new_values TEXT,         -- JSON uusista arvoista
    description TEXT         -- Ihmisluettava kuvaus
);
```

**Toteutus:**
```java
public class AuditLogger {
    public void logAction(String actionType, String entityType,
                         int entityId, Object oldValue, Object newValue) {
        String description = buildDescription(actionType, entityType, entityId);
        // Kirjaa tietokantaan
        // Esimerkki: "Päivitetty tosite #123: muutettu pvm 2025-01-01 → 2025-01-02"
    }
}

// Käytä DocumentDAO:ssa
public void save(Document document) {
    Document oldDoc = getById(document.getId());
    // ... tallennus logiikka ...
    auditLogger.logAction("UPDATE", "DOCUMENT", document.getId(), oldDoc, document);
}
```

**Tarkistusketjun katseluohjelman käyttöliittymä:**
```
Työkalut → Tarkistusketju

┌───────────────────────────────────────────────────────┐
│ Tarkistusketju                                         │
├────────────┬────────┬──────────────────────────────────┤
│ Aikaleima  │ Toiminto│ Kuvaus                          │
├────────────┼────────┼──────────────────────────────────┤
│ 12:34:56   │ UPDATE │ Tosite #123: Summa 100→150€     │
│ 12:30:12   │ DELETE │ Kirjaus poistettu tositteesta #122│
│ 12:25:45   │ INSERT │ Uusi tosite #124 luotu          │
└────────────┴────────┴──────────────────────────────────┘

Suodattimet: [Aikaväli] [Toimintotyyppi] [Kohdetyyppi] [Hae]
```

**Tositteen versiohistoria:**
```
Oikea klikkaa tositetta → Näytä historia

Näyttää:
- Kaikki muutokset tähän tositteeseen
- Kuka teki muutokset (tulevaisuus monikäyttäjä)
- Milloin muutokset tehtiin
- Mitä muutettiin (diff-näkymä)
- Vaihtoehto palauttaa edelliseen versioon
```

**Yksinkertainen Kumoa/Tee uudelleen (Istuntopohjainen):**
```java
// Muistissa oleva kumoa-pino (tyhjennetään suljettaessa)
public class DocumentUndoManager {
    private Stack<DocumentSnapshot> undoStack = new Stack<>();
    private Stack<DocumentSnapshot> redoStack = new Stack<>();

    public void takeSnapshot(Document doc, List<Entry> entries) {
        undoStack.push(new DocumentSnapshot(doc, entries));
        redoStack.clear(); // Tyhjennä tee uudelleen uudella toiminnolla
    }

    public void undo() {
        if (!undoStack.isEmpty()) {
            DocumentSnapshot current = getCurrentState();
            redoStack.push(current);
            DocumentSnapshot previous = undoStack.pop();
            restoreSnapshot(previous);
        }
    }
}

// Näppäinkomennot
Ctrl+Z - Kumoa viimeisin muutos (nykyisen tositteen sisällä)
Ctrl+Y - Tee uudelleen
```

---

## 🎯 Prioriteettiominaisuudet Tilitinille

Kilpailija-analyysin ja käyttäjätarpeiden perusteella, tässä priorioidut ominaisuussuositukset:

### Korkea prioriteetti (Toteutus 2025)

**1. CSV/Pankkitapahtumien tuonti älytäsmäytyksellä** ✅ TOTEUTETTU
- **Vaikutus:** Korkea - eliminoi manuaalisen syötön, suuri aikasäästö
- **Vaiva:** Keskitaso - rakennettu olemassa olevan CSV-infrastruktuurin päälle
- **Kilpailijat:** GnuCash, kaikilla suurilla alustoilla on tämä
- **ROI:** Välitön tuottavuushyöty
- **Tila:** ✅ Perusversio valmis v2.2.1:ssä

**2. Aikataulutetut/Toistuvat tositteet**
- **Vaikutus:** Korkea - käsittelee vuokrat, palkat, toistuvat menot
- **Vaiva:** Keskitaso - uusi malli + käyttöliittymä
- **Kilpailijat:** GnuCashilla on kattava järjestelmä
- **ROI:** Säästää tunteja kuukaudessa tyypillisille käyttäjille

**3. Parannettu datan validointi ja virheiden ehkäisy**
- **Vaikutus:** Korkea - ehkäisee kalliita virheitä
- **Vaiva:** Matala-Keskitaso - inkrementaaliset parannukset
- **Kilpailijat:** Standardi modernissa ohjelmistossa
- **ROI:** Vähentää virheitä, lisää luottamusta

**4. Tarkistusketjujärjestelmä**
- **Vaikutus:** Keskitaso-Korkea - vaaditaan vaatimustenmukaisuuteen, vastuullisuuteen
- **Vaiva:** Keskitaso - uusi taulu + kirjaamisinfrastruktuuri
- **Kilpailijat:** Standardi ammattimaisessa ohjelmistossa
- **ROI:** Lakivaatimustenmukaisuus, vianmääritys

### Keskitason prioriteetti (2026)

**5. Koontinäyttö/Yleiskatsausnäyttö**
- **Vaikutus:** Keskitaso - parantaa alkukäyttäjäkokemusta
- **Vaiva:** Keskitaso - uusi ikkuna + widgetit
- **Kilpailijat:** Useimmilla moderneilla sovelluksilla on koontinäytöt
- **ROI:** Parempi UX, nopeammat oivallukset

**6. Pikakirjaus/Express-tila**
- **Vaikutus:** Keskitaso - nopeuttaa toistuvaa datasyöttöä
- **Vaiva:** Matala-Keskitaso - optimoitu käyttöliittymän kulku
- **Kilpailijat:** TallyPrime ja muilla on tämä
- **ROI:** Tuottavuus tehokäyttäjille

**7. Parannetut näppäinkomennot**
- **Vaikutus:** Keskitaso - tehokäyttäjän tuottavuus
- **Vaiva:** Matala - pääosin näppäinsidontien lisäykset
- **Kilpailijat:** Kaikki ammattimaiset ohjelmistot
- **ROI:** 30-40% aikasäästö (tutkimuksen mukaan)

**8. OCR PDF-kuitteille**
- **Vaikutus:** Keskitaso-Korkea - automatisoi datasyötön kuitteista
- **Vaiva:** Korkea - OCR-integraatio, tekoälyn koulutus
- **Kilpailijat:** Moderneilla pilvialustoilla on tämä
- **ROI:** Merkittävä aikasäästö, mutta monimutkainen

### Matala prioriteetti (Tulevaisuus/Hyvä olla)

**9. Mobiilisovellus (Android/iOS)**
- **Vaikutus:** Matala-Keskitaso - mukavuusominaisuus
- **Vaiva:** Erittäin korkea - erillinen koodipohja
- **Kilpailijat:** Useimmilla pilvialustoilla on mobiili
- **ROI:** Vaatii erilaisen arkkitehtuurin (pilvi backend)

**10. Monikäyttäjätuki**
- **Vaikutus:** Matala-Keskitaso - laajentaa kohdemarkkinoita
- **Vaiva:** Korkea - vaatii käyttäjän autentikoinnin, oikeudet
- **Kilpailijat:** GnuCashilla ei ole, isommilla alustoilla on
- **ROI:** Avaa liiketoimintaa suuremmille organisaatioille

**11. Web-käyttöliittymä**
- **Vaikutus:** Keskitaso - monialustainen saavutettavuus
- **Vaiva:** Erittäin korkea - täydellinen uudelleenkirjoitus
- **Kilpailijat:** Pilvialustat ovat web-pohjaisia
- **ROI:** Perustavanlaatuinen arkkitehtuurimuutos

---

## 📅 Toimiva toteutussuunnitelma

### Vaihe 1: Datasyötön automaatio (Q1-Q2 2025)

**Sprint 1: CSV-tuonnin parannus** ✅ TOTEUTETTU
```
✅ Viikko 1-2: Käyttöliittymä ja sarakekartoitus
- CsvImportDialog.kt visuaalisella sarakekartoittimella
- Esikatselutaulukko näyttää kartoitettu data
- Automaattinen koodauksen ja erottimen tunnistus

✅ Viikko 3: Perus CSV-tuonti
- CsvParser.kt ja CsvColumnAnalyzer.kt
- CsvImporter.kt tietokantaan tallentamiseen
- Integroitu Työkalut-valikkoon (Ctrl+I)

Seuraavat parannukset:
- Tallenna/lataa kartoitusmallit
- Älykäs tilien täsmäytys
- Kaksoiskappaleiden havaitseminen
```

**Sprint 2: Toistuvat tositteet (4-5 viikkoa)**
```
Viikko 1: Tietokanta ja malli
- Luo recurring_documents-taulu
- Toteuta RecurringDocument.java-malli
- Lisää RecurringDocumentDAO

Viikko 2-3: Käyttöliittymä
- Aikataulutetut tositteet -dialogi
- Toistuvuusmallin valitsin (päivittäin/viikoittain/kuukausittain)
- Mallikisjauseditori

Viikko 4: "Since Last Run" -ominaisuus
- Käynnistysdialogi näyttää odottavat tositteet
- Automaattiluo tai kysy -vaihtoehdot
- Ilmoitusjärjestelmä

Viikko 5: Testaus
- Testaa kaikki toistuvuusmallit
- Erikoistapaukset (kuukauden loppu, karkausvuosi)
- Käyttäjien hyväksyntätestaus
```

**Sprint 3: Parannettu validointi (2-3 viikkoa)**
```
Viikko 1: Validointipalvelu
- Rakenna ValidationService.java
- Toteuta sääntömoottori
- Reaaliaikainen ja tallennusta edeltävä validointi

Viikko 2: Käyttöliittymäintegraatio
- Validointivaroitusdialogit
- Kentän tason virheellisindikaattorit
- Massavalidointi tuonneille

Viikko 3: Testaus
- Testaa kaikki validointisäännöt
- Suorituskykytestaus
- Käyttäjäpalaute
```

### Vaihe 2: Tehokäyttäjien ominaisuudet (Q3 2025)

**Sprint 4: Näppäinkomennot ja pikakirjaus (2 viikkoa)**
```
Viikko 1: Lisänäppäinkomennot
- Toteuta uudet näppäinkomennot
- Parannettu automaattitäydennys kuvauksille
- Laskin popup summakenttään

Viikko 2: Express Entry -tila
- Virtaviivaistettu käyttöliittymä nopeaan datasyöttöön
- Vain näppäimistöllä toimiva työnkulku
- Testaus tehokäyttäjien kanssa
```

**Sprint 5: Tarkistusketju (3 viikkoa)**
```
Viikko 1: Tietokanta ja kirjaaminen
- Luo audit_log-taulu
- Toteuta AuditLogger-palvelu
- Integroi kaikkiin DAO-toimintoihin

Viikko 2: Tarkistusketjun katseluohjelman käyttöliittymä
- Rakenna tarkistusketjun selain
- Suodatus ja haku
- Tositteen historianäkymä

Viikko 3: Testaus ja dokumentaatio
- Testaa kirjaamisen tarkkuus
- Suorituskykytestaus
- Käyttäjädokumentaatio
```

### Vaihe 3: Edistyneet ominaisuudet (Q4 2025 / 2026)

**Sprint 6: Koontinäyttö (3-4 viikkoa)**
- Yleiskatsausnäyttö widgeteillä
- Pikatilastot ja viimeaikainen toiminta
- Mukautettava asettelu

**Sprint 7: OCR-integraatio (6-8 viikkoa)**
- PDF-tekstin poiminta PDFBoxilla
- Hahmontunnistus suomalaisille laskuille
- Automaattitäyttö skannatuista dokumenteista

---

## 💡 Tilitinin kilpailuasemointi

### Tilitinin ainutlaatuiset vahvuudet

1. **Suomi-ensin suunnittelu**
   - Rakennettu erityisesti suomalaisille kirjanpitosäännöille
   - Native ALV-tuki (11 ALV-koodia)
   - Suomalainen terminologia ja työnkulut
   - Tilikartta suomalaisille organisaatioille

2. **Yksityisyys ja paikallinen kontrolli**
   - Työpöytäsovellus, pilvi ei vaadita
   - Data pysyy käyttäjän koneella
   - Ei kuukausimaksuja
   - Ei toimittajalukkoa

3. **Avoimen lähdekoodin ja ilmainen**
   - GPL v3 -lisenssi
   - Ei lisenssikustannuksia
   - Yhteisövetoinen kehitys
   - Läpinäkyvä koodipohja

4. **Ammattimaiset ominaisuudet**
   - Kahdenkertainen kirjanpito
   - Täysi taloudellinen raportointi
   - Monikausituki
   - Ammattimaiset PDF-raportit

### Alueet, joilla kilpailijat ovat parempia

1. **GnuCash:**
   - Kypsempi tuonti/vienti
   - Parempi monivaluuttatuki
   - Aikataulutetut tapahtumat
   - Suurempi yhteisö

2. **Pilvialustat (QuickBooks, Xero):**
   - Mobiilisovellukset
   - Monikäyttäjäyhteistyö
   - Pankkiintegraatiot (automaattinen synkronointi)
   - Tekoälyvetoinen automaatio

3. **Kitupiikki/Kitsas:**
   - Sisäänrakennettu laskutus
   - Sähköinen arkiston luonti
   - Pilvipalvelinvaihtoehto

### Suositeltu asemointi

**Tilitin: "Yksityinen, ammattimainen suomalainen kirjanpitoratkaisu"**

Kohdeuskäyttäjät:
- Yksityisyystietoiset yritykset
- Käyttäjät haluavat paikallisen datan kontrollin
- Organisaatiot, jotka tarvitsevat suomalaista vaatimustenmukaisuutta
- Budjettitietoiset pienyritykset
- Käyttäjät, jotka suosivat työpöytäohjelmistoja

Arvolupausta:
- "Ammattimainen kirjanpito ilman pilveä tai tilausmaksuja"
- "Sinun datasi, sinun tietokoneesi, sinun hallinnassasi"
- "Suomalainen kirjanpito, oikein tehty"

---

## 📊 Yhteenveto ja seuraavat askeleet

### Keskeiset suositukset tiivistettynä

**Välittömät toimenpiteet (Seuraavat 3-6 kuukautta):**
1. ✅ CSV/pankkituonti älytäsmäytyksellä - TOTEUTETTU
2. Lisää aikataulutetut/toistuvat tositteet
3. Paranna datan validointia ja virheiden ehkäisyä
4. Toteuta tarkistusketjujärjestelmä

**Keskipitkän aikavälin (6-12 kuukautta):**
5. Rakenna koontinäyttö/yleiskatsausnäyttö
6. Lisää pikakirjaustila tehokäyttäjille
7. Laajenna näppäinkomentoja
8. Paranna varmuuskopion validointia ja testausta

**Pitkän aikavälin harkittavaa:**
9. OCR kuittien käsittelyyn
10. Mobiilisovellus (vain luku -tila katselu)
11. Monikäyttäjätuen arviointi

---

## 📚 Lähteet ja viitteet

### Kilpailija-tutkimus
- GnuCash Features: https://www.gnucash.org/features.phtml
- GnuCash Review 2025: https://crm.org/news/gnucash-review
- GnuCash Import Documentation: https://gnucash.org/docs/v5/C/gnucash-manual/trans-import.html
- GnuCash Scheduled Transactions: https://wiki.gnucash.org/wiki/Scheduled_Transactions
- Kitupiikki GitHub: https://github.com/artoh/kitupiikki

### Modernit kirjanpito-ohjelmistotrendit
- Best Accounting Software 2025: https://catalyst-cpa.com/best-accounting-software-2025-top-4-solutions-ranked/
- Top Accounting Software 2025: https://apakus.co/top-20-accounting-software-tools-for-small-businesses-in-2025/
- Accounting Website Design: https://hostadvice.com/blog/website-design/accounting-website-design/
- Accounting Software Development: https://www.scnsoft.com/financial-management/accounting-software-development

### PDF ja dokumenttihallinta
- Cloud Accounting Document Attachment: https://cpasayu.com/en/2025/07/08/cloud-accounting-review-document-attachment-and-receipt-management-in-freee-and-money-forward/
- Invoice Ninja: https://invoiceninja.com/
- DocuClipper: https://www.docuclipper.com/
- Scan2Invoice: https://www.scan2invoice.com/

### Tuonti/Vienti ja automaatio
- SaasAnt Transactions: https://www.saasant.com/app-saasant-transactions-quickbooks-automation
- Tiller Money Feeds: https://tiller.com/how-to-automatically-download-bank-transactions-to-excel/
- Journal Entry Automation - NetSuite: https://www.netsuite.com/portal/resource/articles/accounting/journal-entry-automation.shtml

### Näppäinkomennot ja tuottavuus
- Windows Shortcuts for Accountants: https://www.finoptimal.com/resources/windows-keyboard-shortcuts-for-accountants-boost-your-productivity-and-accuracy
- TallyPrime Shortcut Keys Guide 2025: https://www.tallyatcloud.com/article/ultimate-tally-prime-shortcut-keys-guide-2025-save-time-work-faster-stay-productive/603/0/1

### Datan validointi ja virheiden ehkäisy
- Data Validation Best Practices: https://www.cubesoftware.com/blog/data-validation-best-practices
- Data Validation Techniques: https://numerous.ai/blog/data-validation-techniques
- Accounting Errors - QuickBooks: https://quickbooks.intuit.com/r/bookkeeping/accounting-errors/

### Varmuuskopiointi ja katastrofipalautus
- Database Backup Best Practices: https://www.isaca.org/resources/isaca-journal/past-issues/2012/database-backup-and-recovery-best-practices
- Data Backup Best Practices: https://blog.quest.com/8-data-backup-best-practices/
- Accounting Software Backup: https://sbsweb.com/accounting-software-backup/

### Tarkistusketju ja tapahtumahistoria
- Audit Trails - Zoho Books: https://www.zoho.com/books/academy/accounting-principles/what-is-an-audit-trail.html
- Audit Trail in Accounting Software: https://www.zoho.com/in/books/audit-trail/
- Xero Unreconcile: https://www.saasant.com/blog/xero-unreconcile-remove-redo/

### UX ja tumma tila
- Dark Mode Design Trends 2025: https://altersquare.medium.com/dark-mode-design-trends-for-2025-should-your-startup-adopt-it-a7e7c8c961ab
- QuickBooks Dark Mode: https://royalwise.com/qbo-dark-mode-cash-flow-dashboard-widget-community-reports/

---

**Raportti valmisteltu:** 29.12.2025
**Analysoitu Tilitin-versio:** 2.2.1
**Lähteitä yhteensä:** 60+
**Tutkimuskesto:** Kattava analyysi nykyisestä kirjanpito-ohjelmistojen maisemasta
