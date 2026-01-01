# Tilitin - Käyttäjän opas

Ilmainen kirjanpito-ohjelma pk-yrityksille ja yhdistyksille

---

## 📚 Sisällysluettelo

1. [Mikä on Tilitin?](#mikä-on-tilitin)
2. [Pääominaisuudet](#pääominaisuudet)
3. [Pika-aloitus](#pika-aloitus)
4. [Käyttöliittymä](#käyttöliittymä)
5. [Kirjanpidon perustoiminnot](#kirjanpidon-perustoiminnot)
6. [Raportit ja tulosteet](#raportit-ja-tulosteet)
7. [ALV-hallinta](#alv-hallinta)
8. [PDF-liitteet](#pdf-liitteet)
9. [Varmuuskopiointi](#varmuuskopiointi)
10. [Asetukset ja konfiguraatio](#asetukset-ja-konfiguraatio)
11. [Näppäinkomennot](#näppäinkomennot)
12. [Tietokantayhteensopivuus](#tietokantayhteensopivuus) ⚠️ **Tärkeää**
13. [Usein kysytyt kysymykset](#usein-kysytyt-kysymykset)

---

## Mikä on Tilitin?

**Tilitin** on ilmainen, avoimen lähdekoodin kirjanpito-ohjelma, joka on suunniteltu erityisesti suomalaisille pk-yrityksille ja yhdistyksille. Ohjelma tukee täydellistä kaksinkertaista kirjanpitoa ja sisältää kaikki lakisääteisen kirjanpidon vaatimat toiminnot.

### Kenelle Tilitin sopii?

✅ Pienyrittäjät ja toiminimet
✅ Pienet osakeyhtiöt
✅ Yhdistykset ja säätiöt
✅ Kirjanpitäjät ja tilitoimistot
✅ Opiskelijat ja harjoittelijat

### Miksi valita Tilitin?

- 🆓 **Täysin ilmainen** - Ei lisenssi- tai kuukausimaksuja
- 🇫🇮 **Suomalainen** - Tukee suomalaista ALV-lainsäädäntöä
- 💾 **Paikallinen** - Tietosi pysyvät omalla koneellasi
- 🔒 **Turvallinen** - Ei pilvipalveluita, täysi kontrolli datasta
- 🎨 **Moderni** - FlatLaf-käyttöliittymä, vaalea ja tumma teema
- 📊 **Kattava** - Kaikki tarvittavat raportit ja työkalut

---

## Pääominaisuudet

### 📖 Kirjanpito

- **Kaksinkertainen kirjanpito** - Täysi tuki debet/kredit-kirjauksille
- **Tositelajit** - Määrittele omat tositelajit (esim. Myyntilasku, Ostolasku)
- **Vientimallit** - Tallenna usein toistuvat kirjaukset malleiksi
- **Automaattinen numerointi** - Tositenumerot automaattisesti per tositelaji
- **Suosikkitilit** - Merkitse usein käytetyt tilit suosikeiksi (F7)
- **Tilikartta** - Muokattava tilikartta, tuo valmiita tilikarttoja

### 📊 Raportit (11 kpl)

#### Päivittäinen seuranta
- **Tiliote** - Yksittäisen tilin tapahtumat ja saldo
- **Tilien saldot** - Kaikkien tilien saldot yhdellä silmäyksellä

#### Kirjanpitokirjat
- **Päiväkirja** - Kronologinen kirjaus kaikista tositteista
- **Pääkirja** - Kirjaukset tileittäin järjestettynä

#### Tilinpäätösraportit
- **Tuloslaskelma** - Perinteinen tai erittelyillä
- **Tase** - Perinteinen tai erittelyillä
- **ALV-laskelma** - ALV-erittely verottajalle

#### Muut raportit
- **Tilikarttaraportit** - Tulosta tilikartta (kaikki, käytössä olevat, suosikit)
- **Tosite** - Tulosta yksittäinen tosite

### 💰 ALV-hallinta

- **11 ALV-koodia** - Kaikki suomalaiset ALV-tilanteet
- **Automaattinen ALV-laskenta** - Ohjelma laskee ALV:n puolestasi
- **ALV-vastatilit** - Määrittele 1-2 vastatiliä per tili
- **ALV-tilien päättäminen** - Automaattinen ALV-kirjaus
- **ALV-kantojen muutos** - Massapäivitys ALV-prosentin muuttuessa

#### Tuetut ALV-koodit:
1. Veroton myynti/osto
2. ALV vähennyskelvoton
3. Verollinen myynti/osto (kotimaa)
4. Verollinen myynti/osto (EU)
5. Verollinen myynti/osto (EU-ulkopuolinen)
6. Rakentamispalvelun myynti
7. Rakentamispalvelun osto
8. Käännetty verovelvollisuus
9. Marginaaliverotus
10. ALV 0% (erikoistilanteet)
11. Vähennyskelpoinen osto ilman ALV:tä

### 💾 Varmuuskopiointi (v2.0+)

**AutoBackup** - Automaattinen varmuuskopiointi kuten Wordissa:
- ⏱️ Määritettävä aikaväli (1-60 min)
- 📂 Monikohtainen backup (useita sijainteja per tietokanta)
- ☁️ Pilvipalvelu-integraatiot (OneDrive, Google Drive, Dropbox, iCloud)
- 💿 USB-asemien tunnistus
- 📜 Versiohistoria (1-100 versiota per tietokanta)
- 🔙 Helppokäyttöinen palautus

**Manuaalinen backup**:
- "Tee nyt" -painike välitöntä varmuuskopiointia varten
- Valitse tietokannat ja sijainnit

### 🗃️ Tietokannat

**Tuetut tietokantamoottorit:**
- **SQLite** (oletus) - Paikallinen tiedostopohjainen, helppo käyttää
- **MySQL** - Verkko-tietokanta, jaettu käyttö
- **PostgreSQL** - Verkko-tietokanta, ammattikäyttö

**Ominaisuudet:**
- Viimeisimmät tietokannat -lista nopeaa avausta varten
- Useita tilikausia samassa tietokannassa
- Tietokannan varmuuskopiointi ja palautus

### 🎨 Ulkoasu ja käyttökokemus

- **FlatLaf-teema** - Moderni, nykyaikainen ulkoasu
- **Vaalea ja tumma teema** - Valitse mieltymyksesi mukaan
- **Fonttikoon säätö** - Mukauta tekstikokoa
- **Näppäinkomennot** - Nopea työskentely ilman hiirtä
- **Automaattinen täydennys** - Tilit ja selitteet täydentyvät historiasta
- **Drag & drop** - Järjestele tilikartta vetämällä

### 📎 PDF-liitteet (v2.2.0+)

**Liitä PDF-tiedostoja tositteisiin** - Tallenna kuitit, laskut ja muut dokumentit suoraan tositteisiin:

- ✅ **Lisää PDF-liitteitä** - Liitä PDF-tiedostoja tositteisiin
- 📋 **Katso liitetiedostoja** - Näe kaikki tosit teen liitteet yhdellä silmäyksellä
- 💾 **Vie liitetiedostoja** - Tallenna liitetiedostoja takaisin levylle
- 🗑️ **Poista liitteitä** - Hallitse liitteitä helposti
- 🔍 **Metatiedot** - Tiedostokoko, sivumäärä ja lisäyspäivä näkyvissä
- ⚡ **Automaattinen päivitys** - Liitteet päivittyvät tositteiden välillä navigoidessa

**Rajoitukset:**
- Maksimi tiedostokoko: 10 MB per PDF
- Varoitus: 5 MB+ tiedostoista
- Tuki: SQLite, MySQL, PostgreSQL

**Miten käytän:**
1. Avaa tosite
2. Vieritä alas "PDF-liitteet" -osioon
3. Klikkaa "Lisää PDF" ja valitse tiedosto
4. Liite tallentuu automaattisesti tietokantaan

### 🔧 Työkalut

- **Tositenumeroiden muutos** - Massapäivitys tositenumeroille
- **Saldojen vertailu** - Vertaa tilien saldoja eri ajanjaksoilla
- **CSV-vienti** - Vie kaikki kirjaukset CSV-muotoon (Excel-yhteensopiva)
- **CSV-tuonti** - Tuo tilitapahtumia CSV-tiedostosta (Ctrl+I)
- **PDF-tulosteet** - Tulosta raportit PDF-muotoon
- **Tilikarttojen tuonti** - Tuo valmiita tilikarttoja

---

## Pika-aloitus

### 1. Asenna Tilitin

**Windows:**
1. Lataa `Tilitin-<versio>-setup.exe` [releases-sivulta](https://github.com/priku/tilitin-modernized/releases/latest)
2. Tuplaklikkaa asennusohjelmaa
3. Seuraa ohjeita
4. Käynnistä Tilitin Start-valikosta

**Muut käyttöjärjestelmät (JAR):**
1. Asenna Java 25+ ([Adoptium](https://adoptium.net/))
2. Lataa `tilitin-<versio>.jar`
3. Tuplaklikkaa JAR-tiedostoa

### 2. Luo ensimmäinen tietokanta

1. Käynnistä Tilitin
2. Valitse **Tiedosto → Uusi tietokanta**
3. Valitse sijainti tiedostolle (esim. `Oma_yritys.db`)
4. Tietokanta luodaan automaattisesti

### 3. Syötä perustiedot

**Tiedosto → Perustiedot:**
- **Yrityksen nimi**: Esim. "Oma Yritys Oy"
- **Y-tunnus**: Esim. "1234567-8"
- **Tilikaudet**: Lisää ensimmäinen tilikausi
  - Alkamispäivä: Esim. 1.1.2025
  - Päättymispäivä: Esim. 31.12.2025

### 4. Tuo tilikartta

**Muokkaa → Tilikartta:**
1. Klikkaa **Tuo tiedostosta**
2. Valitse sopiva valmis tilikartta (esim. `osakeyhtiö_alv25.5.txt`)
3. Tilikartta tuodaan automaattisesti

Tai luo tilikartta manuaalisesti:
- Klikkaa **Lisää tili** tai **Lisää otsikko**
- Määrittele tilinumero, nimi, tyyppi

### 5. Tee ensimmäinen kirjaus

1. Klikkaa **Uusi tosite** (Ctrl+N)
2. Valitse **Päivämäärä**
3. Valitse **Tositelaji** (esim. "Myyntilasku")
4. Lisää viennit:
   - **Tili**: Aloita kirjoittamaan tilin numeroa tai nimeä
   - **Debet/Kredit**: Syötä summa
   - **Selite**: Vapaaehtoinen kuvaus
5. Tallenna **Ctrl+S**

---

## Käyttöliittymä

### Pääikkuna

```
┌─────────────────────────────────────────────────────┐
│ Tiedosto  Muokkaa  Siirry  Tositelaji  Tulosteet   │ ← Valikot
├─────────────────────────────────────────────────────┤
│ [◀] [▶] [Uusi] [Tallenna] [Kopioi] [Liitä] [Hae]  │ ← Työkalupalkki
├─────────────────────────────────────────────────────┤
│ Päivämäärä: [01.01.2025 ▼]  Numero: [1      ]     │
│ Tositelaji: [Myyntilasku    ▼]                     │
├─────────────────────────────────────────────────────┤
│ Tili    │ Debet   │ Kredit  │ ALV  │ Selite       │ ← Vientitaulukko
├─────────┼─────────┼─────────┼──────┼──────────────┤
│ 3000    │ 1000,00 │         │ 25,5%│ Myynti       │
│ 2939    │         │  255,00 │      │ Myynti ALV   │
│ 1700    │         │  745,00 │      │ Myyntisaamiset│
├─────────┼─────────┼─────────┼──────┼──────────────┤
│ Yhteensä│ 1000,00 │ 1000,00 │  0,00│              │ ← Summat
└─────────────────────────────────────────────────────┘
```

### Valikkorakenne

#### Tiedosto
- Uusi tietokanta
- Avaa tietokanta
- Viimeisimmät tietokannat
- Varmuuskopiointiasetukset
- Palauta varmuuskopiosta
- Tietokanta-asetukset
- Lopeta

#### Muokkaa
- Kopioi / Liitä
- Uusi tosite / Poista tosite
- Lisää vienti / Poista vienti
- **Vientimallit** (alavalikko)
- Muokkaa vientimalleja
- Luo malli tositteesta
- **Tilikartta**
- **Alkusaldot**
- **Perustiedot**
- **Kirjausasetukset**
- **Ulkoasu**

#### Siirry
- Edellinen / Seuraava tosite
- Ensimmäinen / Viimeinen tosite
- Hae numerolla
- Hae

#### Tositelaji (dynaaminen)
- Muokkaa tositelajeja
- [Tositelaji 1]
- [Tositelaji 2]
- ...

#### Tulosteet
- **Raportit:**
  - Tilien saldot
  - Tosite
  - Tiliote
  - Tuloslaskelma (+ erittelyin)
  - Tase (+ erittelyin)
  - Päiväkirja
  - Pääkirja
  - ALV-laskelma
- **Tilikartta:**
  - Vain käytössä olevat
  - Vain suosikkitilit
  - Kaikki tilit
- **Muokkaa** (raporttien rakenne)

#### Työkalut
- ALV-tilien päättäminen
- Ohita vienti ALV-laskelmassa
- ALV-kantojen muutokset
- Tilien saldojen vertailu
- Muuta tositenumeroita
- Vie tiedostoon (CSV)
- Tuo CSV-tiedostosta (Ctrl+I)

#### Ohje
- Sisältö
- Virheenjäljitystietoja
- Tietoja ohjelmasta

---

## Kirjanpidon perustoiminnot

### Tositteen luominen

**Perustyönkulku:**

1. **Luo uusi tosite** (Ctrl+N)
2. **Valitse päivämäärä** - Klikkaa kalenterikuvaketta
3. **Valitse tositelaji** - Pudotusvalikosta (esim. Myyntilasku)
4. **Lisää viennit:**
   - Klikkaa Tili-saraketta
   - Aloita kirjoittamaan tilinumeroa tai nimeä
   - Ohjelma ehdottaa tilejä automaattisesti
   - TAB-näppäimellä seuraavaan kenttään
   - Syötä summa Debet- tai Kredit-sarakkeeseen
   - ALV-koodi täyttyy automaattisesti (jos tilillle määritelty)
   - Syötä selite (valinnainen, täydentyy historiasta)
5. **Tarkista summat** - Debet ja kredit yhteensä pitää täsmätä
6. **Tallenna** (Ctrl+S)

**Vinkit:**
- **F3** avaa tilinvalintaikkunan
- **F7** merkitsee tilin suosikiksi
- **F9** näyttää vain suosikkitilit
- **Enter** siirtyy älykkäästi seuraavaan kenttään
- Punainen erotus tarkoittaa, että debet ja kredit eivät täsmää

### Vientimallien käyttö

**Luo vientimalli:**

1. Tee tosite normaalisti
2. Valitse **Muokkaa → Luo malli tositteesta**
3. Anna mallille nimi (esim. "Vuokran maksu")
4. Malli tallennetaan

**Käytä vientimallia:**

1. Luo uusi tosite
2. Valitse **Muokkaa → Vientimallit → [Mallin nimi]**
3. Viennit kopioituvat tositteelle
4. Muokkaa tarvittaessa summia ja päivämäärää
5. Tallenna

**Muokkaa vientimalleja:**

- **Muokkaa → Muokkaa vientimalleja**
- Lisää, muokkaa tai poista malleja
- Määrittele oletussummat ja selitteet

### Tositteen kopiointi

**Kopioi viennit:**

1. Avaa haluamasi tosite
2. Valitse viennit (Ctrl+Click useita)
3. **Kopioi** (Ctrl+C)
4. Luo uusi tosite tai avaa toinen
5. **Liitä** (Ctrl+V)
6. Tallenna

**Käyttötapaus:**
- Toistuva kuukausittainen kirjaus (esim. vuokra)
- Samantyyppiset ostolaskut

### Tositteen etsiminen

**Hae numerolla:**
- **Siirry → Hae numerolla**
- Syötä tositenumero
- Tosite avautuu

**Tekstihaku:**
- **Siirry → Hae** (Ctrl+F)
- Syötä hakusana (tilin nimi, selite, summa)
- Ohjelma näyttää hakutulokset
- Klikkaa tulosta avataksesi tositteen

**Navigointi:**
- **Edellinen tosite** (◀ -painike tai Ctrl+Left)
- **Seuraava tosite** (▶ -painike tai Ctrl+Right)
- **Ensimmäinen tosite** (Home)
- **Viimeinen tosite** (End)

---

## Raportit ja tulosteet

### Tiliote

**Käyttö:**
1. **Tulosteet → Tiliote**
2. Valitse tili pudotusvalikosta
3. Valitse aikaväli (koko tilikausi tai mukautettu)
4. Klikkaa **Esikatsele**

**Sisältö:**
- Tilin nimi ja numero
- Kaikki viennit aikavälillä (päivämäärä, tosite, selite, debet, kredit)
- Alkusaldo
- Loppusaldo

**Käyttötapaus:**
- Tarkista yksittäisen tilin tapahtumat
- Verifoi pankkitilitapahtumien täsmäys

### Päiväkirja

**Käyttö:**
1. **Tulosteet → Päiväkirja**
2. Valitse aikaväli
3. Valitse muoto:
   - **Lyhyt** - Vain tositteet
   - **Pitkä** - Tositteet + viennit
4. Klikkaa **Esikatsele**

**Sisältö:**
- Kronologinen lista kaikista tositteista
- Päivämäärä, tositenumero, selite, summa
- (Pitkä muoto: kaikki viennit per tosite)

**Käyttötapaus:**
- Lakisääteinen kirjanpitokirja
- Kronologinen seuranta

### Pääkirja

**Käyttö:**
1. **Tulosteet → Pääkirja**
2. Valitse aikaväli
3. Valitse muoto (lyhyt/pitkä)
4. Klikkaa **Esikatsele**

**Sisältö:**
- Kaikki tilit järjestyksessä
- Viennit tileittäin
- Tilin saldo

**Käyttötapaus:**
- Lakisääteinen kirjanpitokirja
- Tilikohtainen seuranta

### Tuloslaskelma

**Käyttö:**
1. **Tulosteet → Tuloslaskelma** (tai "Tuloslaskelma erittelyin")
2. Valitse tilikausi
3. Klikkaa **Esikatsele**

**Sisältö:**
- Liikevaihto
- Kulut (kategorisoituna)
- Liikevoitto/-tappio
- Rahoituskulut ja -tuotot
- Tilikauden voitto/tappio

**Erittelyversio:**
- Näyttää yksittäiset tilit kategorioittain

**Käyttötapaus:**
- Tilinpäätös
- Kuukausittainen kannattavuusseuranta

### Tase

**Käyttö:**
1. **Tulosteet → Tase** (tai "Tase erittelyin")
2. Valitse tilikausi
3. Klikkaa **Esikatsele**

**Sisältö:**
- **Vastaavaa:**
  - Pysyvät vastaavat
  - Vaihtuvat vastaavat
- **Vastattavaa:**
  - Oma pääoma
  - Vieras pääoma

**Erittelyversio:**
- Näyttää yksittäiset tilit kategorioittain

**Käyttötapaus:**
- Tilinpäätös
- Taloudellisen tilanteen seuranta

### ALV-laskelma

**Käyttö:**
1. **Tulosteet → ALV-laskelma tileittäin**
2. Valitse aikaväli (yleensä kuukausi tai kvartaali)
3. Klikkaa **Esikatsele**

**Sisältö:**
- Myynnit ALV-kannoittain
- Ostot ALV-kannoittain
- Maksettava/palautettava ALV

**Käyttötapaus:**
- ALV-ilmoituksen täyttö
- ALV:n oikeellisuuden tarkistus

### Tilien saldot

**Käyttö:**
1. **Tulosteet → Tilien saldot**
2. Valitse aikaväli
3. Klikkaa **Esikatsele**

**Sisältö:**
- Kaikki tilit numerojärjestyksessä
- Saldo per tili

**Käyttötapaus:**
- Nopea yleiskuva tilanteesta
- Tilien täsmäytys

### Tilikarttaraportit

**Tulosta tilikartta:**
- **Vain käytössä olevat tilit** - Tilit joilla on kirjauksia
- **Vain suosikkitilit** - Merkityt suosikit
- **Kaikki tilit** - Koko tilikartta

**Sisältö:**
- Tilinumero
- Tilin nimi
- Tilin tyyppi
- ALV-koodi

**Käyttötapaus:**
- Tilikartan dokumentointi
- Kirjanpidon suunnittelu

---

## ALV-hallinta

### ALV-koodit ja niiden käyttö

Tilitin tukee 11 erilaista ALV-koodia, jotka kattavat kaikki Suomen ALV-tilanteet:

#### 1. Veroton myynti/osto
**Käyttö:** Verottomat tuotteet ja palvelut
**Esimerkki:** Terveydenhuoltopalvelut, rahoituspalvelut

#### 2. ALV vähennyskelvoton
**Käyttö:** Ostot joista ei voi vähentää ALV:tä
**Esimerkki:** Henkilöstön virkistyskulut, edustustilaisuudet

#### 3. Verollinen myynti/osto (kotimaa)
**Käyttö:** Normaali kotimaankauppa
**ALV-prosentit:** 25,5%, 14%, 10%
**Esimerkki:** Tuotteiden myynti/osto Suomessa

#### 4. Verollinen myynti/osto (EU)
**Käyttö:** EU-maiden välinen kauppa
**Esimerkki:** Myynti saksalaiselle yritykselle

#### 5. Verollinen myynti/osto (EU-ulkopuolinen)
**Käyttö:** Kauppa EU:n ulkopuolelle
**Esimerkki:** Vienti Norjaan

#### 6. Rakentamispalvelun myynti
**Käyttö:** Rakennusalan palvelut
**Esimerkki:** Aliurakointi rakennustyömaalla

#### 7. Rakentamispalvelun osto
**Käyttö:** Rakennusalan palveluiden osto
**Esimerkki:** Aliurakoitsijan palkkaus

#### 8. Käännetty verovelvollisuus
**Käyttö:** Ostaja maksaa ALV:n
**Esimerkki:** Romukauppa, päästöoikeuksien osto

#### 9. Marginaaliverotus
**Käyttö:** Käytettyjen tavaroiden kauppa
**Esimerkki:** Antiikkiliike

#### 10. ALV 0%
**Käyttö:** Erikoistilanteet
**Esimerkki:** Sanomalehtien tilausmyynti

#### 11. Vähennyskelpoinen osto ilman ALV:tä
**Käyttö:** Ostot joista ei peritä ALV:tä mutta vähennys mahdollinen
**Esimerkki:** Koulutuspalvelut

### ALV-vastatilit

**Määrittely:**
1. **Muokkaa → Tilikartta**
2. Valitse tili
3. Määrittele:
   - **ALV-koodi** (1-11)
   - **ALV-prosentti** (esim. 25,5)
   - **Vastatili 1** (esim. 2939 Verovelka)
   - **Vastatili 2** (valinnainen, käytetään erikoistilanteissa)

**Esimerkki - Myyntitili:**
- Tili: 3000 Tavaroiden myynti
- ALV-koodi: 3 (Verollinen myynti kotimaa)
- ALV-%: 25,5
- Vastatili 1: 2939 Myynti ALV
- Vastatili 2: (tyhjä)

**Kun kirjaat:**
```
Tili 3000, Kredit 1000€, ALV 25,5%
→ Ohjelma luo automaattisesti:
   Tili 2939, Kredit 255€ (ALV)
```

### ALV-tilien päättäminen

**Käyttö:**
1. **Työkalut → ALV-tilien päättäminen**
2. Valitse aikaväli (esim. tammikuu 2025)
3. Ohjelma laskee:
   - Myynnin ALV yhteensä
   - Ostojen ALV yhteensä
   - Maksettava/palautettava ALV
4. Klikkaa **Luo kirjaus**
5. Tosite luodaan automaattisesti

**Tositteen sisältö:**
- Verovelka-tili (2939) → Verotili (1234)
- Tai Verotili (1234) → Verosaaminen (1432)

**Aikataulutus:**
- Kuukausittain (useimmat yritykset)
- Neljännesvuosittain (pienet yritykset)
- Vuosittain (yhdistykset)

### ALV-kantojen muutokset

**Käyttötapaus:** ALV-prosentti muuttuu (esim. 24% → 25,5%)

**Käyttö:**
1. **Työkalut → ALV-kantojen muutokset**
2. Klikkaa **Lisää rivi**
3. Määrittele:
   - **Tili:** Valitse muutettava tili (esim. 3000)
   - **Vanha ALV-%:** 24
   - **Uusi ALV-%:** 25,5
   - **Luo uusi tili:** Kyllä/Ei
4. Klikkaa **Toteuta muutokset**

**Vaihtoehdot:**
- **Päivitä olemassa oleva tili** - Muuttaa prosenttia tilillle
- **Luo uusi tili** - Luo uuden tilin uudella prosentilla (esim. 3000→3001)

**Hyöty:**
- Massapäivitys kaikille tileille kerralla
- Historia säilyy (jos luot uudet tilit)

---

## Varmuuskopiointi

### AutoBackup-toiminto

**Mikä se on?**
AutoBackup on automaattinen varmuuskopiointijärjestelmä, joka toimii kuten Microsoft Wordin AutoSave. Ohjelma luo varmuuskopioita säännöllisin väliajoin ilman käyttäjän toimenpiteitä.

**Käyttöönotto:**

1. **Tiedosto → Varmuuskopiointiasetukset**
2. **Yleiset asetukset:**
   - ✅ Ota AutoBackup käyttöön
   - Aikaväli: 5-60 min (suositus: 15 min)
3. **Valitse tietokannat:**
   - Merkitse varmuuskopioitavat tietokannat
4. **Määrittele sijainnit:**
   - Klikkaa **Sijainnit...**
   - Lisää backup-sijainnit (ks. alla)
5. **Klikkaa OK**

**Backup-sijainnit:**

Voit määritellä useita backup-sijainteja per tietokanta:

#### Paikallinen kansio
- Esim. `C:\Varmuuskopiot\`
- Nopea, paikallinen
- Ei suojaa kovalevyn rikkoutumiselta

#### Pilvipalvelut (automaattinen tunnistus)
- **OneDrive** - Ohjelma tunnistaa automaattisesti
- **Google Drive** - Ohjelma tunnistaa automaattisesti
- **Dropbox** - Ohjelma tunnistaa automaattisesti
- **iCloud** - Ohjelma tunnistaa automaattisesti

**Hyöty:** Pilvipalvelu synkronoi varmuuskopiot automaattisesti

#### USB-asemat
- Ohjelma tunnistaa irrotettavat asemat
- Valitse USB-aseman kansio
- **Huom:** Toimii vain kun USB-asema on kytkettynä

**Versiohistoria:**

- Määrittele kuinka monta versiota säilytetään (1-100)
- Vanhimmat versiot poistetaan automaattisesti
- Esim. 10 versiota = 10 viimeisintä varmuuskopiota

**Suositeltu konfiguraatio:**

```
AutoBackup: Käytössä
Aikaväli: 15 min
Versioita: 10

Sijainnit:
1. C:\Varmuuskopiot\ (paikallinen)
2. OneDrive\Tilitin_Backup\ (pilvi)
3. USB-asema:\Backup\ (ulkoinen)
```

### Manuaalinen varmuuskopiointi

**Tee nyt -toiminto:**

1. **Tiedosto → Varmuuskopiointiasetukset**
2. Klikkaa **Tee nyt**
3. Valitse tietokannat
4. Ohjelma kopioi kaikki valitut tietokannat kaikkiin sijainteihin
5. Näyttää edistymispalkin

**Käyttötapaus:**
- Ennen suurta muutosta (esim. tilikauden vaihto)
- Ennen ohjelman päivitystä
- Ennen ALV-kantojen massapäivitystä

### Varmuuskopion palautus

**Palauta tietokanta:**

1. **Tiedosto → Palauta varmuuskopiosta**
2. Valitse tietokanta pudotusvalikosta
3. Valitse versio listasta:
   - Näyttää päivämäärän ja kellonajan
   - Näyttää tiedostokoon
4. Valitse palautussijainti:
   - **Oletussijainti** (korvaa nykyisen tietokannan)
   - **Valitse sijainti** (palauta eri kansioon)
5. Klikkaa **Palauta**
6. Vahvista toiminto

**Turvallisuusvinkit:**
- ⚠️ Palautus **korvaa** nykyisen tietokannan
- 💡 Suositus: Palauta eri sijaintiin ensin, tarkista, kopioi sitten
- 🔒 Luo aina varmuuskopio ennen palautusta

---

## Asetukset ja konfiguraatio

### Perustiedot

**Muokkaa → Perustiedot**

**Yrityksen tiedot:**
- **Nimi** - Yrityksen virallinen nimi
- **Y-tunnus** - Muodossa 1234567-8

**Tilikaudet:**
- **Lisää tilikausi** - Luo uusi tilikausi
- **Alkamispäivä** - Tilikauden alku (esim. 1.1.2025)
- **Päättymispäivä** - Tilikauden loppu (esim. 31.12.2025)
- **Poista tilikausi** - Poistaa valitun (vain jos ei kirjauksia)

**Vinkit:**
- Ensimmäinen tilikausi voi olla lyhyempi (esim. 6 kk)
- Tilikauden päättymispäivä määrää tilinpäätöksen ajankohdan

### Kirjausasetukset

**Muokkaa → Kirjausasetukset**

**ALV-asetukset:**
- ☑ **Näytä ALV-sarake** - Näyttää/piilottaa ALV-sarakkeen vientitaulukossa
- ☑ **Lukitse ALV-sarake** - Estää ALV-koodin muokkauksen

**Automaattiset toiminnot:**
- ☑ **Automaattinen vientiselitteen täydennys** - Ehdottaa selitteitä historiasta
- ☑ **Debet/kredit-eroavaisuusvaroitus** - Varoittaa jos debet ≠ kredit

**Lukitukset:**
- **Lukitse kuukaudet** - Valitse kuukaudet jotka lukitaan
  - Estää muutokset lukittuihin kuukausiin
  - Hyödyllinen ALV-ilmoituksen jälkeen
- ☑ **Lukitse koko tilikausi** - Estää kaikki muutokset tilikaudella
  - Käytä tilinpäätöksen jälkeen

**Vinkit:**
- Lukitse kuukaudet ALV-ilmoituksen jälkeen (esim. tammikuu lukitaan 15.2.)
- Lukitse tilikausi vasta kun tilinpäätös vahvistettu

### Tietokanta-asetukset

**Tiedosto → Tietokanta-asetukset**

**SQLite (oletus):**
- Ei asetuksia tarvita
- Tiedostopohjainen (*.db)
- Helpoin vaihtoehto

**MySQL:**
- **Palvelin** - esim. `localhost` tai `mysql.example.com`
- **Portti** - Oletus 3306
- **Tietokanta** - Tietokannan nimi
- **Käyttäjänimi** - MySQL-käyttäjä
- **Salasana** - MySQL-salasana

**PostgreSQL:**
- **Palvelin** - esim. `localhost` tai `postgres.example.com`
- **Portti** - Oletus 5432
- **Tietokanta** - Tietokannan nimi
- **Käyttäjänimi** - PostgreSQL-käyttäjä
- **Salasana** - PostgreSQL-salasana

**Käyttötapaukset:**
- **SQLite** - Yksittäinen käyttäjä, paikallinen kone
- **MySQL/PostgreSQL** - Useita käyttäjiä, verkko, pilvi

### Ulkoasuasetukset

**Muokkaa → Ulkoasu**

**Teema:**
- **System** - Käyttöjärjestelmän oletusteema
- **FlatLaf Light** - Moderni vaalea teema (suositus)
- **FlatLaf Dark** - Moderni tumma teema
- **IntelliJ Light/Dark** - IntelliJ-tyylinen
- **Darcula** - Tumma Darcula-teema

**Fonttikoko:**
- Liukusäätimellä 8-24 pt
- Oletus: 12 pt
- Suositus: 10-14 pt (riippuen näytön koosta)

**Live-esikatselu:**
- Muutokset näkyvät heti
- Voit testata eri teemoja ja fonttikokoja

**Vinkit:**
- **Vaalea teema** - Parempi kirkkaan valon alla
- **Tumma teema** - Vähentää silmien rasitusta hämärässä
- **Isompi fontti** - Parantaa luettavuutta suurilla näytöillä

---

## Näppäinkomennot

### Yleiset

| Näppäin | Toiminto |
|---------|----------|
| **Ctrl+O** | Avaa tietokanta |
| **Ctrl+N** | Uusi tosite |
| **Ctrl+C** | Kopioi viennit |
| **Ctrl+V** | Liitä viennit |
| **Ctrl+F** | Hae tosite |
| **Ctrl+G** | Siirry tositteeseen |
| **Ctrl+I** | Tuo CSV-tiedostosta |
| **Ctrl+E** | Vie tiedostoon |
| **Ctrl+T** | Tilikartta |
| **Ctrl+Shift+P** | Perustiedot |
| **Ctrl+Shift+S** | Kirjausasetukset |
| **Ctrl+Shift+V** | ALV-tilien päättäminen |
| **Ctrl+Q** | Lopeta ohjelma |
| **F1** | Ohje |

### Navigointi

| Näppäin | Toiminto |
|---------|----------|
| **PgUp** | Edellinen tosite |
| **PgDown** | Seuraava tosite |
| **Ctrl+PgUp** | Ensimmäinen tosite |
| **Ctrl+PgDown** | Viimeinen tosite |

### Vientitaulukko

| Näppäin | Toiminto |
|---------|----------|
| **Enter** | Siirry seuraavaan soluun (älykäs) |
| **Tab** | Seuraava sarake |
| **Shift+Tab** | Edellinen sarake |
| **F3** | Avaa tilinvalintaikkuna |
| **F7** | Merkitse tili suosikiksi |
| **F9** | Näytä vain suosikkitilit |
| **Insert** | Lisää vienti |
| **Delete** | Poista vienti |
| **Ctrl+Up/Down** | Siirrä vientiä ylös/alas |

### Tilikartta

| Näppäin | Toiminto |
|---------|----------|
| **Insert** | Lisää tili/otsikko |
| **Delete** | Poista tili/otsikko |
| **Ctrl+C** | Kopioi tili |
| **Ctrl+V** | Liitä tili |
| **Ctrl+Up/Down** | Siirrä tiliä ylös/alas |

### Raportit

| Näppäin | Toiminto |
|---------|----------|
| **Ctrl+P** | Tulosta raportti |
| **Ctrl+E** | Vie raportti (CSV/PDF) |
| **+** / **-** | Zoomaa sisään/ulos |
| **Ctrl+0** | Palauta zoom |

---

## Tietokantayhteensopivuus

### Versiohistoria

Tilitin käyttää tietokantaskeemaa, joka päivittyy automaattisesti kun uusia ominaisuuksia lisätään:

| Tietokantaversio | Tilitin-versio | Muutokset |
|------------------|----------------|-----------|
| 13 | 1.6.1 (Helineva) | Alkuperäinen versio |
| 14 | 2.0+ | ALV-prosenttisarake (`vat_percentage`) |

### ⚠️ Tärkeää yhteensopivuudesta

**Tilitin 2.1** päivittää tietokannan automaattisesti versioon 14 kun avaat vanhan tietokannan ensimmäistä kertaa. Tämä mahdollistaa:

- ✅ **Vapaamuotoiset ALV-prosentit** (esim. 25,5%, 14%, 10%)
- ✅ **Tulevat ALV-muutokset** ilman ohjelmapäivitystä

**Yhteensopivuus:**

| Suunta | Toimii | Selitys |
|--------|--------|---------|
| Vanha → Tilitin 2.1 | ✅ Kyllä | Päivittyy automaattisesti, ei tiedonmenetystä |
| Tilitin 2.1 → Vanha | ❌ Ei | Vanha versio ei ymmärrä uutta skeemaa |

### Käytännön ohjeet

1. **Ennen päivitystä:** Ota varmuuskopio tietokannasta (Tiedosto → Varmuuskopioi)
2. **Päivityksen jälkeen:** Tietokanta toimii vain Tilitin 2.0+ versioissa
3. **Paluu vanhaan:** Ei mahdollista automaattisesti - käytä varmuuskopiota

### PDF-liitteet ja yhteensopivuus

PDF-liitteet tallennetaan **tiedostoina** tietokannan viereen (ei tietokantaan). Tämä säilyttää:
- ✅ Tiedoston koon pienempänä
- ✅ Helpon varmuuskopioinnin
- ✅ Yhteensopivuuden vanhempien versioiden kanssa (ne eivät näe liitteitä, mutta eivät häiriinny niistä)

---

## Usein kysytyt kysymykset

### Yleistä

**K: Onko Tilitin todella ilmainen?**
V: Kyllä, Tilitin on täysin ilmainen. Ei lisenssi-, kuukausi- tai muita maksuja. Ohjelma on avoimen lähdekoodin (GPL v3).

**K: Toimiiko Tilitin Macilla ja Linuxilla?**
V: Kyllä. Lataa JAR-versio ja asenna Java 25+. Ohjelman pitäisi toimia kaikilla alustoilla.

**K: Voiko tietokantaa käyttää usealla koneella?**
V: Kyllä:
- **SQLite** - Kopioi .db-tiedosto tai käytä pilvipalvelua (OneDrive, Dropbox)
- **MySQL/PostgreSQL** - Käytä verkko-tietokantaa, toimii samanaikaisesti

**K: Voinko käyttää Tilitiä tilitoimistossa?**
V: Kyllä. Tilitin tukee useita tietokantoja ja asiakkaita. Suositus: MySQL/PostgreSQL verkossa.

**K: Tukeeko Tilitin sähköistä kirjanpitoa?**
V: Kyllä. Tilitin tuottaa kaikki lakisääteiset raportit (päiväkirja, pääkirja, tase, tuloslaskelma). Raportit voi tulostaa PDF:ksi.

### Kirjanpito

**K: Miten lisään uuden tositteen?**
V: Ctrl+N tai klikkaa **Uusi tosite** -painiketta työkalupalkista.

**K: Miten poistan tositteen?**
V: Avaa tosite ja valitse **Muokkaa → Poista tosite**. Vahvista toiminto.

**K: Voiko tositteita muokata jälkikäteen?**
V: Kyllä, ellei kuukautta tai tilikautta ole lukittu. Avaa tosite, muokkaa, tallenna.

**K: Miten merkitään lasku maksetuksi?**
V: Tee uusi tosite:
- Debet: Ostovelka/Myyntisaaminen
- Kredit: Pankkitili
- Selite: "Lasku nro XXX maksettu"

**K: Miten korjaan virheen vanhassa tositteessa?**
V: Kaksi vaihtoehtoa:
1. **Muokkaa tositetta** (jos ei lukittu)
2. **Luo oikaisutosite** (suositus, säilyttää audit trail)

### ALV

**K: Miten ALV-prosentti määritellään?**
V: Muokkaa → Tilikartta → Valitse tili → Määrittele ALV-% (esim. 25,5)

**K: Voiko ALV:n laskea käsin?**
V: Kyllä, mutta ei suositella. Määrittele ALV-vastatilit, niin ohjelma laskee automaattisesti.

**K: Miten teen ALV-ilmoituksen?**
V:
1. **Tulosteet → ALV-laskelma tileittäin** (kuukausi/kvartaali)
2. Kopioi luvut Vero.fi:hin
3. **Työkalut → ALV-tilien päättäminen** (luo kirjaus)

**K: Miten muutan ALV-prosentin kaikkiin tileihin?**
V: **Työkalut → ALV-kantojen muutokset** - Massapäivitys kaikkiin tileihin kerralla.

**K: Mitä ALV-koodia käytän EU-myyntiin?**
V: ALV-koodi 4 (Verollinen myynti EU). Muista ALV-numero ja yhteenvetoilmoitus.

### CSV-tuonti

**K: Miten tuon pankkitilitapahtumia CSV-tiedostosta?**
V: **Työkalut → Tuo CSV-tiedostosta** (Ctrl+I). Valitse tiedosto, määritä sarakkeiden vastaavuudet, esikatsele ja tuo.

**K: Mitä CSV-muotoja tuetaan?**
V: Procountor-muoto tunnistetaan automaattisesti. Muille voit määrittää sarakkeet manuaalisesti (päivämäärä, summa, selite, tili).

**K: Voiko CSV-tuonnin esikatsella ennen tuontia?**
V: Kyllä, dialogi näyttää esikatselun riveistä ennen tuontia. Voit tarkistaa sarakkeiden vastaavuudet.

### Raportit

**K: Miten tulostan raportin?**
V: Valitse raportti Tulosteet-valikosta → Esikatsele → Tulosta (Ctrl+P)

**K: Voinko viedä raportin Exceliin?**
V: Kyllä. **Työkalut → Vie tiedostoon** - Vie CSV-muotoon, avaa Excelissä.

**K: Miten muokkaan raportin rakennetta?**
V: **Tulosteet → Muokkaa** - Voit muokata tuloslaskelman ja taseen rakennetta.

**K: Voiko raportteja lähettää sähköpostilla?**
V: Tulosta raportti PDF:ksi (Tulosta → Tallenna PDF:nä), liitä sähköpostiin.

### Varmuuskopiointi

**K: Kuinka usein AutoBackup varmuuskopioi?**
V: Määrität itse aikavälin (1-60 min). Suositus: 15-30 min.

**K: Minne varmuuskopiot tallennetaan?**
V: Määrittämääsi sijaintiin (paikallinen kansio, pilvi, USB). Voit valita useita.

**K: Vie varmuuskopiot levytilaa?**
V: SQLite-tietokanta on yleensä alle 10 MB. 10 versiota = noin 100 MB.

**K: Voiko vanhan varmuuskopion palauttaa?**
V: Kyllä. **Tiedosto → Palauta varmuuskopiosta** - Valitse versio ja palauta.

**K: Miten varmistan että backup toimii?**
V: Testaa: **Tiedosto → Varmuuskopiointiasetukset → Tee nyt** - Tarkista että tiedostot syntyvät.

### Tekniset

**K: Mikä Java-versio tarvitaan?**
V: Java 25 tai uudempi. Suositus: [Eclipse Adoptium](https://adoptium.net/)

**K: Windows-asennusohjelma ei käynnisty?**
V: Asennusohjelma sisältää Javan, ei erillistä asennusta tarvita. Tarkista virustorjunta.

**K: Ohjelma on hidas?**
V:
- Tarkista tietokannan koko (yli 100 000 tositetta → harkitse MySQL/PostgreSQL)
- Sulje muut ohjelmat
- Lisää RAM-muistia

**K: Voiko tietokantaa varmuuskopioida manuaalisesti?**
V: Kyllä (SQLite). Kopioi .db-tiedosto turvalliseen paikkaan.

**K: Miten päivitän Tilitinin uuteen versioon?**
V:
1. Varmuuskopioi tietokanta ensin
2. Asenna uusi versio (Windows: suoraan vanhan päälle)
3. Avaa tietokanta uudella versiolla

**K: Tuleeko uusia ominaisuuksia?**
V: Kyllä. Seuraa [GitHub-sivua](https://github.com/priku/tilitin-modernized) päivityksistä.

### Tuki ja kehitys

**K: Mistä saan apua?**
V:
- Tämä käyttäjän opas
- **Ohje → Sisältö** (ohjelman sisäinen ohje)
- [GitHub Issues](https://github.com/priku/tilitin-modernized/issues) - Raportoi ongelmia
- [Tilitinin kotisivu](https://helineva.net/tilitin/)

**K: Voinko ehdottaa uusia ominaisuuksia?**
V: Kyllä! Luo [GitHub Issue](https://github.com/priku/tilitin-modernized/issues) merkinnällä "Feature Request".

**K: Voinko osallistua kehitykseen?**
V: Kyllä! Projekti on avoimen lähdekoodin. Katso [CONTRIBUTING.md](CONTRIBUTING.md) ohjeet.

**K: Löysin bugin, mitä teen?**
V: Raportoi [GitHub Issues](https://github.com/priku/tilitin-modernized/issues):
1. Kerro mitä teit
2. Mitä tapahtui
3. Mitä odotit tapahtuvan
4. Ohjelmaversio (**Ohje → Tietoja ohjelmasta**)

---

## Liitteet

### Tilikartat

Tilitin sisältää useita valmiita tilikarttoja eri yritysmuodoille:

- **osakeyhtiö_alv25.5.txt** - Osakeyhtiö, ALV 25,5%
- **toiminimi_alv25.5.txt** - Toiminimi, ALV 25,5%
- **yhdistys_alv25.5.txt** - Yhdistys, ALV 25,5%

**Tuonti:**
1. **Muokkaa → Tilikartta**
2. **Tuo tiedostosta**
3. Valitse sopiva tilikartta
4. Tilikartta tuodaan automaattisesti

### Linkit

- **Projektin kotisivu:** https://helineva.net/tilitin/
- **GitHub-repositorio:** https://github.com/priku/tilitin-modernized
- **Lataukset (Releases):** https://github.com/priku/tilitin-modernized/releases
- **Bugiraportit ja ehdotukset:** https://github.com/priku/tilitin-modernized/issues
- **Dokumentaatio:**
  - [BUILDING.md](BUILDING.md) - Build-ohjeet kehittäjille
  - [CONTRIBUTING.md](CONTRIBUTING.md) - Kontribuutio-ohjeet
  - [CHANGELOG.md](CHANGELOG.md) - Muutosloki
  - [KOTLIN_MIGRATION.md](KOTLIN_MIGRATION.md) - Kotlin-migraation tekninen dokumentaatio

### Lisenssit

- **Tilitin:** GPL v3 - Vapaa ja avoimen lähdekoodin ohjelma
- **Komponenttien lisenssit:** Katso **Ohje → Tietoja ohjelmasta** tai [COPYING](COPYING)

---

**Kiitos että käytät Tilitiä!** 🎉

Jos tästä oppaasta oli apua, harkitse projektin tukemista:
- ⭐ Anna GitHub-tähti repositoriolle
- 📝 Jaa kokemuksesi muille
- 🐛 Raportoi bugit
- 💡 Ehdota parannuksia
- 💻 Osallistu kehitykseen

**Tilitin-yhteisö toivottaa sinut tervetulleeksi!**
