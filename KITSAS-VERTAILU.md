# Tilitin vs Kitsas - Ominaisuusvertailu

**Päivitetty**: 2025-12-28

Tämä dokumentti vertaa **Tilitin 2.1** ja **Kitsas** -kirjanpito-ohjelmia. Molemmat ovat ilmaisia, avoimen lähdekoodin suomalaisia kirjanpito-ohjelmia.

---

## 📊 Pikavertailu

| Ominaisuus | Tilitin 2.1 | Kitsas |
|-----------|-------------|--------|
| **Hinta** | 💚 Täysin ilmainen | 🟡 Ilmainen perusversio + maksulliset lisäpalvelut |
| **Lähdekoodi** | ✅ Avoin (GPL v3) | ✅ Avoin (GPL v3) |
| **Alustat** | Windows, Mac, Linux (JAR) | Windows, Mac, Linux |
| **Tietokanta** | SQLite, MySQL, PostgreSQL | SQLite + pilvipalvelu |
| **Verkkolaskutus** | ❌ Ei | ✅ Kyllä (maksullinen) |
| **Laskutus** | ❌ Ei sisäänrakennettua | ✅ Kyllä |
| **Pilvipalvelu** | ❌ Ei | ✅ Kyllä (maksullinen) |
| **OCR-tositteentunnistus** | ❌ Ei | ✅ Kyllä |
| **Pankkiyhteys** | ❌ Ei | ✅ Kyllä (maksullinen) |

---

## 🟢 Tilitinin vahvuudet

### 1. ✅ Täysin ilmainen
- **Ei maksullisia lisäpalveluita**
- Ei kuukausimaksuja
- Kaikki ominaisuudet ilmaisia

### 2. ✅ Täysi paikallinen kontrolli
- Tiedot omalla koneellasi
- Ei pilvipakotetta
- Ei ulkoisia riippuvuuksia

### 3. ✅ Monipuoliset tietokantavaihtoehdot
- **SQLite** - Paikallinen, yksinkertainen
- **MySQL** - Verkko, jaettu käyttö
- **PostgreSQL** - Ammattikäyttö

### 4. ✅ Kattava varmuuskopiointijärjestelmä (v2.0+)
- **AutoBackup** - Automaattinen varmuuskopiointi (1-60 min)
- **Pilvipalveluintegraatiot** - OneDrive, Google Drive, Dropbox, iCloud
- **USB-tunnistus** - Automaattinen irrotettavien asemien tunnistus
- **Versiohistoria** - 1-100 versiota per tietokanta
- **Monikohtainen backup** - Useita sijainteja per tietokanta

### 5. ✅ Moderni käyttöliittymä
- **FlatLaf-teema** - Vaalea ja tumma teema
- **Fonttikoon säätö** - Saavutettavuus
- **Näppäinkomennot** - Tehokas työskentely
- **Automaattinen täydennys** - Tilit ja selitteet

### 6. ✅ Kotlin-modernisaatio (v2.1+)
- **Kotlin 2.3.0** - Moderni koodipohja
- **Java 25** - Uusin Java-versio
- **-50% vähemmän koodia** - Helpompi ylläpitää

### 7. ✅ Kattava dokumentaatio
- **USER-GUIDE.md** - 1,085 riviä käyttäjädokumentaatiota
- **30+ FAQ-kysymystä**
- Yksityiskohtaiset ohjeet jokaisesta ominaisuudesta

---

## 🟢 Kitsasin vahvuudet

### 1. ✅ Sisäänrakennettu laskutus
- Laskujen luonti
- Tuoterekisteri
- Vastikkeiden ja vuokrien laskutus

### 2. ✅ Verkkolaskutus
- Verkkolaskujen vastaanotto
- Verkkolaskujen lähettäminen
- *Vaatii maksullisen tilauksen*

### 3. ✅ Pankkiyhteys
- Tilitapahtumien automaattinen nouto pankista
- *Maksullinen lisäosa*

### 4. ✅ OCR-tositteentunnistus
- PDF-tositteista tietojen tunnistus
- Automatisoitu tositteiden käsittely

### 5. ✅ Pilvipalvelu
- Tiedot suomalaisella palvelimella
- **Webkitsas** - Selainversio
- Automaattiset varmuuskopiot
- *Maksullinen palvelu*

### 6. ✅ Laskujen kiertojärjestelmä
- Hyväksyntäkierto
- Seuranta

### 7. ✅ Sähköinen ALV-ilmoitus
- Suora yhteys Vero.fi:hin
- *Maksullinen ominaisuus*

---

## 📚 Yhteiset perusominaisuudet

Molemmat ohjelmat tukevat:

### Kirjanpito
- ✅ Kaksinkertainen kirjanpito
- ✅ Tositteiden hallinta
- ✅ Tilikartta (muokattavissa)
- ✅ ALV-hallinta (11 suomalaista koodia)
- ✅ Tilikauden hallinta

### Raportit
- ✅ Päiväkirja
- ✅ Pääkirja
- ✅ Tase
- ✅ Tuloslaskelma
- ✅ ALV-laskelma/tilitys
- ✅ Tiliote

### Tilinpäätös
- ✅ Poistot
- ✅ Jaksotukset
- ✅ Tilinpäätösasiakirjat

### Yleiset
- ✅ Suomen kieli
- ✅ Suomalainen ALV-lainsäädäntö
- ✅ Avoin lähdekoodi
- ✅ Monikäyttöjärjestelmätuki (Windows, Mac, Linux)

---

## ⚖️ Keskinäiset erot ominaisuuksissa

| Ominaisuus | Tilitin | Kitsas |
|-----------|---------|--------|
| **Laskutus** | ❌ Ei sisäänrakennettua | ✅ Sisäänrakennettu |
| **Verkkolaskutus** | ❌ Ei | ✅ Kyllä (€) |
| **Pankkiyhteys** | ❌ Ei | ✅ Kyllä (€) |
| **OCR-tunnistus** | ❌ Ei | ✅ Kyllä |
| **Pilvipalvelu** | ❌ Ei | ✅ Kyllä (€) |
| **Webversio** | ❌ Ei | ✅ Webkitsas (€) |
| **Sähköinen veroilmoitus** | ❌ Ei | ✅ Kyllä (€) |
| **AutoBackup** | ✅ Kyllä (ilmainen) | ✅ Pilvipalvelussa (€) |
| **Pilvi-integraatiot** | ✅ OneDrive, Google Drive, Dropbox, iCloud | ❌ Vain oma pilvi |
| **Versiohistoria** | ✅ 1-100 versiota | ⚠️ Pilvipalvelussa |
| **MySQL/PostgreSQL** | ✅ Kyllä | ❌ Vain SQLite |
| **Tumma teema** | ✅ Kyllä (FlatLaf) | ❓ Ei tietoa |
| **Vientimallit** | ✅ Kyllä | ❓ Ei tietoa |
| **Suosikkitilit** | ✅ Kyllä (F7/F9) | ❓ Ei tietoa |
| **CSV-vienti** | ✅ Kyllä (Excel-yhteensopiva) | ❓ Ei tietoa |
| **Kohdennukset** | ❌ Ei | ✅ Kustannuspaikat, projektit |
| **Harjoituskirjanpito** | ❌ Ei | ✅ Kyllä |

---

## 💰 Kustannusvertailu

### Tilitin
- **Asennus**: Ilmainen
- **Käyttö**: Ilmainen
- **Varmuuskopiointi**: Ilmainen (oma pilvi)
- **Tuki**: Yhteisö (GitHub Issues)
- **Päivitykset**: Ilmaiset
- **Yhteensä**: **0 €/kk, 0 €/vuosi**

### Kitsas (arvio)

#### Ilmainen versio:
- ✅ Paikallinen kirjanpito
- ✅ Perusraportit
- ✅ Laskutus (ei verkkolaskuja)
- ❌ Ei pilvipalvelua
- ❌ Ei pankkiyhteyttä
- ❌ Ei sähköistä veroilmoitusta

#### Kerho (4,17 €/kk = 50 €/vuosi):
- ✅ Pilvipalvelu
- ✅ Webkitsas
- ⚠️ Rajoitetut ominaisuudet

#### Firma (10,42 €/kk = 125 €/vuosi):
- ✅ Pilvipalvelu
- ✅ Verkkolaskutus
- ✅ Sähköinen veroilmoitus
- ✅ Käyttäjätuki

#### Pro (50+ €/kk = 600+ €/vuosi):
- ✅ Kaikki Firma-ominaisuudet
- ✅ Pankkiyhteys
- ✅ Laajennettu tuki

---

## 🎯 Kenelle mikä sopii?

### Valitse Tilitin, jos:

✅ **Haluat täysin ilmaisen ratkaisun**
- Ei kuukausimaksuja
- Ei lisämaksuja mistään ominaisuuksista

✅ **Arvostaa paikallista tietojen hallintaa**
- Tiedot omalla koneella
- Ei pilvipakotetta

✅ **Tarvitset monipuolisen tietokantaratkaisun**
- SQLite pieneen käyttöön
- MySQL/PostgreSQL suurempaan tai jaettuun käyttöön

✅ **Haluat modernin käyttöliittymän**
- Tumma teema
- Fonttikoon säätö
- Tehokkaat näppäinkomennot

✅ **Haluat kontrolloida varmuuskopioinnin**
- Valitse itse pilvipalvelu (OneDrive, Google Drive, etc.)
- Määrittele itse versiohistorian pituus
- Useita backup-sijainteja

✅ **Yrityksellä ei ole laskutustarvetta**
- Pieni yritys
- Yhdistys
- Harrastustoiminta

✅ **Kirjanpitäjä tai tilitoimisto**
- Useita asiakkaita
- MySQL/PostgreSQL jaettu käyttö
- Täysi kontrolli datasta

---

### Valitse Kitsas, jos:

✅ **Tarvitset sisäänrakennettua laskutusta**
- Laskujen luonti ohjelmassa
- Tuoterekisteri

✅ **Tarvitset verkkolaskutusta**
- Verkkolaskujen vastaanotto ja lähetys
- Automatisoitu laskujen käsittely

✅ **Haluat pankkiyhteyden**
- Tilitapahtumien automaattinen nouto
- Automaattinen täsmäytys

✅ **Haluat OCR-tositteentunnistuksen**
- PDF-laskujen automaattinen käsittely
- Vähemmän manuaalista työtä

✅ **Haluat pilvipalvelun**
- Webversio (käytä selaimella)
- Automaattiset varmuuskopiot palvelimella

✅ **Haluat sähköisen veroilmoituksen**
- Suora yhteys Vero.fi:hin
- Automaattinen tietojen lähetys

✅ **Olet valmis maksamaan lisäpalveluista**
- 50-600+ €/vuosi riippuen tarpeesta

✅ **Tarvitset kohdennuksia**
- Kustannuspaikat
- Projektiseuranta

---

## 🔮 Tulevaisuuden kehitys

### Tilitin roadmap (MODERNIZATION-TODO.md):

**Korkea prioriteetti:**
1. ✅ Kotlin DAO integraatio (valmis v2.1.3)
2. 🔄 Teematuki dialogeihin (19 dialogia)
3. 🔄 DocumentFrame refaktorointi
4. 🔄 Lambda-lausekkeet

**Keskisuuri prioriteetti:**
- GridBagLayout migraatio
- Cell Renderer/Editor konsolidointi
- Deprecated API:n poisto

**Matala prioriteetti:**
- Accessibility-ominaisuudet
- Responsiivinen suunnittelu
- Asset management
- Async UI updates

### Kitsas roadmap:
- Ei julkisesti saatavilla

---

## 📈 Yhteenveto

### Tilitin on parempi, jos:
- 💰 Haluat **täysin ilmaisen** ratkaisun
- 🔒 Haluat **täyden kontrollin** tiedoistasi
- 🗄️ Tarvitset **monipuolisen tietokantaratkaisun**
- 🎨 Arvostaa **modernia käyttöliittymää**
- 💾 Haluat **joustavan varmuuskopioinnin**

### Kitsas on parempi, jos:
- 📄 Tarvitset **laskutusta**
- 📧 Tarvitset **verkkolaskutusta**
- 🏦 Tarvitset **pankkiyhteyttä**
- 🤖 Haluat **automaatiota** (OCR, veroilmoitus)
- ☁️ Haluat **pilvipalvelun**
- 💶 Olet valmis **maksamaan** lisäpalveluista

---

## 🔗 Lisätietoa

**Tilitin:**
- Kotisivu: https://helineva.net/tilitin/
- GitHub: https://github.com/priku/tilitin-modernized
- Dokumentaatio: [USER-GUIDE.md](USER-GUIDE.md)

**Kitsas:**
- Kotisivu: https://kitsas.fi
- Hinnasto: https://kitsas.fi/hinnasto
- Lataus: https://kitsas.fi/lataa

---

**Huom:** Tämä vertailu perustuu julkisesti saatavilla olevaan tietoon (28.12.2025). Kitsasin ominaisuudet ja hinnat voivat muuttua. Tarkista aina ajantasaiset tiedot viralliselta sivulta.

**Molemmat ohjelmat ovat laadukkaita suomalaisia kirjanpito-ohjelmia.** Valinta riippuu käyttötarpeista ja budjetista.
