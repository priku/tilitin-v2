# Tilitin vs Kitupiikki (Kitsas) - Tekninen Vertailu

**Päivitetty:** 2025-12-28
**Tekijä:** Projektin dokumentaatio
**Tarkoitus:** Kattava tekninen vertailu kahden suomalaisen kirjanpito-ohjelman välillä

---

## Tiivistelmä

Tämä dokumentti vertailee kahta avoimen lähdekoodin suomalaista kirjanpito-ohjelmaa:

- **Tilitin 2.1** - Java/Kotlin-pohjainen modernisoidun Swing UI:n kanssa
- **Kitupiikki/Kitsas** - C++/Qt-pohjainen natiivisovellus

---

## 1. Projektin Koko ja Laajuus

### Tilitin 2.1

| Metriikka | Määrä |
|-----------|-------|
| Java-tiedostot | 194 |
| Kotlin-tiedostot | 12 |
| Yhteensä kooditiedostoja | 206 |
| Päätekniikka | Java 25 + Kotlin 2.3.0 |
| Build-työkalu | Maven |
| UI-framework | Java Swing + FlatLaf |
| Lisenssi | GPL v3 |

**Kotlin-tiedostot (Modernisaatio käynnissä):**
- Account.kt, Document.kt, Entry.kt, Period.kt - Data classes
- DatabaseExtensions.kt, SQLAccountDAOKt.kt - DAO layer
- DialogUtils.kt, SwingExtensions.kt, ValidationUtils.kt - Utilities

**Paketin rakenne:**
```
kirjanpito/
├── db/         - Database layer (26 luokkaa)
├── models/     - Domain models
├── reports/    - Raportointijärjestelmä
├── ui/         - Käyttöliittymä
└── util/       - Apuluokat
```

### Kitupiikki (Kitsas)

| Metriikka | Määrä |
|-----------|-------|
| C++ source (.cpp) | 446 |
| Header (.h) | 448 |
| Yhteensä kooditiedostoja | 894 |
| Päätekniikka | C++ (C++14) |
| Build-työkalu | QMake |
| UI-framework | Qt 6.4+ (QtWidgets, QtPdf, QtWebEngine) |
| Lisenssi | GPL v3 + lisäehdot |

**Qt-moduulit:**
- QtWidgets - Käyttöliittymä
- QtPdf - PDF-käsittely
- QtWebEngine - Web-sisällön näyttö
- QtNetwork - Verkkoviestintä
- QtSql - Tietokanta
- QtPrintSupport - Tulostus
- QtSvg - Vektorigrafiikat

**Moduulirakenne:**
```
kitsas/
├── aloitussivu/     - Käynnistysnäyttö & kirjautuminen
├── alv/             - ALV-ilmoitukset & laskenta
├── apuri/           - Kirjausapurit
├── arkisto/         - Arkistointitoiminnot
├── arkistoija/      - Sähköinen arkistointi
├── db/              - Database layer (17 luokkaa)
├── kieli/           - Monikielisyys
├── kierto/          - Tositteen kierto
├── kirjaus/         - Kirjaussivu
├── laskutus/        - Laskutustoiminnot (laaja!)
├── liite/           - Liitteiden hallinta
├── lisaosat/        - Lisäosajärjestelmä
├── maaritys/        - Asetukset
├── maksatus/        - Maksatusjärjestelmä
├── model/           - Domain models
├── naytin/          - Näyttötoiminnot (PDF, raportti)
├── pilvi/           - Pilvipalvelu-integraatio
├── raportti/        - Raportointi
├── rekisteri/       - Asiakasrekisteri
├── saldodock/       - Saldo-näyttö
├── selaus/          - Tositteiden selaus
├── smtpclient/      - Sähköpostin lähetys
├── sqlite/          - SQLite-implementaatio
├── tilaus/          - Tilausjärjestelmä
├── tilinpaatoseditori/ - Tilinpäätöksen teko
├── toimisto/        - Toimistotoiminnot
├── tools/           - Työkalut & widgetit
├── tuonti/          - Tuontitoiminnot
└── uusikirjanpito/  - Uuden kirjanpidon luonti
```

---

## 2. Teknologiavertailu

### 2.1 Ohjelmointikieli ja Ympäristö

| Aspekti | Tilitin | Kitsas |
|---------|---------|--------|
| **Pääkieli** | Java 25 | C++ (C++14) |
| **Lisäkielet** | Kotlin 2.3.0 (migraatio käynnissä) | - |
| **Muistinhallinta** | Automaattinen (GC) | Manuaalinen (C++) + Qt Object Tree |
| **Tyypitys** | Staattinen, vahva | Staattinen, vahva |
| **Ajoympäristö** | JVM (Java Virtual Machine) | Natiivi (käännetty binääri) |
| **Kääntöaika** | Nopea (incremental) | Hidas (koko projekti) |
| **Käynnistysaika** | Hidas (JVM startup) | Nopea (natiivi) |
| **Muistinkäyttö (runtime)** | Suurempi (JVM overhead) | Pienempi (natiivi) |
| **Jakelukoko** | ~57 MB (sisältää JRE) | Riippuu Qt-kirjastoista |

### 2.2 UI-Framework

#### Tilitin - Java Swing + FlatLaf

**Edut:**
- Moderni ulkoasu FlatLaf-teeman ansiosta
- Vaalea ja tumma teema
- Helppo kehittää (Java API)
- Hyvä yhteensopivuus JVM-ekosysteemin kanssa
- Cross-platform luontaisesti

**Haitat:**
- Vanhempi teknologia (vs. JavaFX tai modernit web-pohjaiset)
- Rajoitetut UI-komponentit
- Suorituskyky ei yhtä nopea kuin natiivi
- Ei tukea modernia touch-käyttöliittymää

**Esimerkkejä Tilittimen UI-komponenteista:**
```java
// DocumentFrame - Pääikkuna
// AccountsView - Tilikartta
// EntriesView - Kirjausnäkymä
// ReportsView - Raportit
```

#### Kitsas - Qt Widgets

**Edut:**
- Natiivi suorituskyky ja ulkoasu
- Laaja valikoima valmiita widgettejä
- QtPdf - Integroitu PDF-tuki
- QtWebEngine - Web-sisällön näyttö
- Model-View arkkitehtuuri (Qt MVC)
- Signals & Slots -mekanismi
- Monikielisyystuki (Qt Linguist)

**Haitat:**
- Monimutkaisempi kehitysympäristö
- LGPL-lisenssi voi rajoittaa kaupallista käyttöä
- Qt-kirjastojen koko
- Käännösaikaisempi kehitys

**Esimerkkejä Kitsas UI-komponenteista:**
```cpp
// KitupiikkiIkkuna - Pääikkuna
// KirjausSivu - Kirjaussivu
// SelausSivu - Selaussivu
// RaporttiSivu - Raporttisivu
// LaskuSivu - Laskutussivu
```

---

## 3. Arkkitehtuuri

### 3.1 Tilitin - Java/Kotlin Arkkitehtuuri

**Kerrosarkkitehtuuri:**

```
┌─────────────────────────────────────┐
│   UI Layer (Swing + FlatLaf)        │
│   - DocumentFrame                   │
│   - AccountsView, EntriesView       │
│   - ReportsView                     │
└─────────────────────────────────────┘
           ↓ ↑
┌─────────────────────────────────────┐
│   Business Logic Layer              │
│   - kirjanpito.models.*             │
│   - Kotlin Data Classes (modernized)│
└─────────────────────────────────────┘
           ↓ ↑
┌─────────────────────────────────────┐
│   Data Access Layer (DAO)           │
│   - AccountDAO, DocumentDAO         │
│   - EntryDAO, PeriodDAO             │
│   - SQLAccountDAOKt (Kotlin)        │
└─────────────────────────────────────┘
           ↓ ↑
┌─────────────────────────────────────┐
│   Database Layer                    │
│   - SQLite (local)                  │
│   - MySQL (optional)                │
│   - PostgreSQL (optional)           │
└─────────────────────────────────────┘
```

**Tietokantaajurit:**
- SQLite JDBC 3.51.1.0 (paikallinen tietokanta)
- MySQL Connector/J 9.5.0 (valinnainen)
- PostgreSQL 42.7.8 (valinnainen)

**Kotlin Modernisaatio (v2.1.x):**
- Data classes: Account, Document, Entry, Period
- Extension functions: DatabaseExtensions.kt
- Null-safety parannettu
- DAO-kerros Kotlinissa (SQLAccountDAOKt)

### 3.2 Kitsas - Qt/C++ Arkkitehtuuri

**Model-View-Controller + Qt Signals/Slots:**

```
┌─────────────────────────────────────┐
│   View Layer (Qt Widgets)           │
│   - KitupiikkiSivu (abstract)       │
│   - KirjausSivu, SelausSivu         │
│   - RaporttiSivu, LaskuSivu         │
└─────────────────────────────────────┘
           ↓ ↑ (signals/slots)
┌─────────────────────────────────────┐
│   Model Layer (Qt Models)           │
│   - TiliModel, TositeModel          │
│   - LaskuModel, RaporttiModel       │
│   - QAbstractTableModel subclasses  │
└─────────────────────────────────────┘
           ↓ ↑
┌─────────────────────────────────────┐
│   Business Logic                    │
│   - Kirjanpito (singleton)          │
│   - Tili, Tilikausi, Tosite         │
│   - Lasku, Raportti                 │
└─────────────────────────────────────┘
           ↓ ↑
┌─────────────────────────────────────┐
│   Database Abstraction              │
│   - KpKysely (abstract query)       │
│   - SqliteKysely, PilviKysely       │
└─────────────────────────────────────┘
           ↓ ↑
┌─────────────────────────────────────┐
│   Database                          │
│   - SQLite (local)                  │
│   - Pilvipalvelu (Kitsas Oy)        │
└─────────────────────────────────────┘
```

**Keskeiset suunnittelumallit:**
- **Singleton:** Kirjanpito-luokka (globaali tila)
- **Model-View:** Qt:n MVC-arkkitehtuuri
- **Observer:** Signals & Slots
- **Strategy:** PilviKysely vs SqliteKysely
- **Factory:** Eri tositetyyppien luonti
- **Delegate:** Custom cell editors (Qt delegates)

---

## 4. Tietokannat ja Datan Hallinta

### 4.1 Tilitin - JDBC-pohjainen

**Tietokantatuki:**
- SQLite (pääasiallinen, paikallinen)
- MySQL (valinnainen, verkko)
- PostgreSQL (valinnainen, verkko)

**DAO-malli:**
```java
// Perinteinen Java DAO
public interface AccountDAO {
    Account getById(int id);
    List<Account> findAll();
    void save(Account account);
    void delete(int id);
}

// Kotlin DAO (modernisaatio)
class SQLAccountDAOKt(private val dataSource: DataSource) {
    fun findById(id: Int): Account? { ... }
    fun findAll(): List<Account> { ... }
}
```

**Transaktiot:**
- JDBC Transaction API
- `Connection.setAutoCommit(false)`
- Manual rollback on error

**Datamalli (Kotlin Data Classes):**
```kotlin
data class Account(
    val id: Int,
    val number: String,
    val name: String,
    val type: AccountType
)

data class Document(
    val id: Int,
    val date: LocalDate,
    val description: String
)

data class Entry(
    val id: Int,
    val documentId: Int,
    val accountId: Int,
    val debit: BigDecimal,
    val credit: BigDecimal
)
```

### 4.2 Kitsas - Qt SQL + Abstraktio

**Tietokantatuki:**
- SQLite (paikallinen)
- Pilvipalvelu (Kitsas Oy - maksullinen)

**Qt SQL Framework:**
```cpp
// KpKysely - Abstrakti kyselyluokka
class KpKysely {
public:
    virtual QVariant query(QString polku) = 0;
    virtual bool post(QString polku, QVariant data) = 0;
    // ...
};

// SqliteKysely - SQLite-implementaatio
class SqliteKysely : public KpKysely {
    QSqlDatabase database_;
    // ...
};

// PilviKysely - Pilvipalvelu-implementaatio
class PilviKysely : public KpKysely {
    QNetworkAccessManager *network_;
    // ...
};
```

**Model-View arkkitehtuuri:**
```cpp
// Qt Models
class TiliModel : public QAbstractTableModel {
    // Abstrakti taulukkomalli
    // Automaattinen UI-päivitys
};

class TositeModel : public QObject {
    Q_OBJECT
signals:
    void muokattu();
public slots:
    void tallenna();
};
```

**Pilvipalveluintegraatio:**
- RESTful API Kitsas Oy:n palvelimelle
- JSON-pohjainen viestintä
- Lisätoiminnot: laskun lähetys, verkkolaskut, ALV-ilmoitukset

---

## 5. Ominaisuusvertailu

### 5.1 Peruskirjanpito

| Ominaisuus | Tilitin | Kitsas | Huomiot |
|------------|---------|--------|---------|
| **Tilit** | ✅ | ✅ | Molemmat tukevat täysin |
| **Tilikartat** | ✅ | ✅ | Useita valmiita tilikarttoja |
| **Tositteet** | ✅ | ✅ | - |
| **Kirjaukset** | ✅ | ✅ | - |
| **Tilikaudet** | ✅ | ✅ | - |
| **Kohdennukset** | ✅ | ✅ | Kitsas: laajempi tuki |
| **ALV-käsittely** | ✅ | ✅ | Kitsas: ALV-ilmoitukset automaattisesti |

### 5.2 Raportit

| Raportti | Tilitin | Kitsas | Huomiot |
|----------|---------|--------|---------|
| **Tase** | ✅ | ✅ | - |
| **Tuloslaskelma** | ✅ | ✅ | - |
| **Päiväkirja** | ✅ | ✅ | - |
| **Pääkirja** | ✅ | ✅ | - |
| **Tase-erittely** | ✅ | ✅ | - |
| **ALV-raportti** | ✅ | ✅ | Kitsas: suoraan ilmoitusvalmiina |
| **Tilikarttaraportti** | ✅ | ✅ | - |
| **Kustannuspaikkaraportti** | ❓ | ✅ | Kitsas: kohdennusraportit |
| **Budjettivertailu** | ✅ | ✅ | Molemmat tukevat |

### 5.3 Laskutus

| Ominaisuus | Tilitin | Kitsas | Huomiot |
|------------|---------|--------|---------|
| **Myyntilaskut** | ⚠️ Rajoitettu | ✅ Laaja | Kitsas: täysi laskutusjärjestelmä |
| **Ostolaskut** | ⚠️ | ✅ | - |
| **Verkkolaskut** | ❌ | ✅ | Kitsas: Finvoice, Maventa |
| **Laskupohja** | ❌ | ✅ | Kitsas: muokattavat pohjat |
| **Maksumuistutukset** | ❌ | ✅ | - |
| **Hyvityslaskut** | ❌ | ✅ | - |
| **Tuoterekisteri** | ❌ | ✅ | Kitsas: täysi tuoterekisteri |
| **Asiakasrekisteri** | ⚠️ Rajoitettu | ✅ Laaja | Kitsas: 240+ tiedostoa laskutukseen |

**Kitsas laskutusmoduulit:**
```
laskutus/
├── laskudlg/          - Laskudialogit (14 tiedostoa)
├── tulostus/          - Laskun tulostus (8 tiedostoa)
├── toimittaja/        - Laskun toimitus (6 tiedostoa)
├── ryhmalasku/        - Massalaskutus (5 tiedostoa)
├── huoneisto/         - Huoneistolaskutus (5 tiedostoa)
├── tuotetuonti/       - Tuotteiden tuonti
└── vakioviite/        - Vakioviitteet
```

### 5.4 Arkistointi ja Liitteet

| Ominaisuus | Tilitin | Kitsas | Huomiot |
|------------|---------|--------|---------|
| **PDF-liitteet** | ✅ | ✅ | Molemmat tukevat |
| **Sähköinen arkisto** | ⚠️ | ✅ | Kitsas: SAF-T, ZIP-arkisto |
| **TAR-arkisto** | ✅ | ✅ | - |
| **Tositteiden skannaus** | ❌ | ✅ | Kitsas: Tesseract OCR |
| **PDF-tuonti** | ⚠️ | ✅ | Kitsas: automaattinen tunnistus |

### 5.5 Lisätoiminnot

| Ominaisuus | Tilitin | Kitsas | Huomiot |
|------------|---------|--------|---------|
| **Pilvipalvelu** | ❌ | ✅ | Kitsas Oy:n maksullinen palvelu |
| **Monikäyttäjä** | ❌ | ✅ | Pilvipalvelussa |
| **Käyttöoikeudet** | ❌ | ✅ | Pilvipalvelussa |
| **Tositteen kierto** | ❌ | ✅ | Pilvipalvelussa |
| **Lisäosajärjestelmä** | ❌ | ✅ | Kitsas: lisäosat |
| **Maksujen tuonti** | ⚠️ | ✅ | Kitsas: tiliotteen tuonti |
| **Sähköpostilähetys** | ❌ | ✅ | Kitsas: SMTP-client |
| **Monikielisyys** | ⚠️ Rajoitettu | ✅ | Kitsas: FI, EN, SV |

---

## 6. Varmuuskopiointijärjestelmät

### 6.1 Tilitin - Edistynyt Backup (v2.0.3+)

**Ominaisuudet:**
- Per-tietokanta backup-sijainnit
- Pilvipalvelutunnistus (Google Drive, OneDrive, Dropbox, iCloud)
- USB-tunnistus (irrotettavat asemat)
- AutoBackup (Word-tyylinen, 1-60 min)
- Sijainnit-dialogi per tietokanta

**Toteutus:**
```java
// DocumentBackupManager.java
public class DocumentBackupManager {
    private final BackupLocationManager locationManager;
    private final AutoBackupScheduler scheduler;

    public void scheduleAutoBackup(int intervalMinutes) {
        // Word-tyylinen autosave
    }

    public void addBackupLocation(BackupLocation location) {
        // Lisää backup-sijainti
    }
}
```

**Backup-tiedostomuoto:**
- `.tilitin.backup` - Aikaleimalla
- Versionhallinta sisäänrakennettuna

### 6.2 Kitsas - Arkistointijärjestelmä

**Ominaisuudet:**
- Sähköinen arkisto (SAF-T XML)
- ZIP-arkisto tositteista ja liitteistä
- Tilinpäätösarkisto
- Arkiston vienti
- PDF-laatuasetukset

**Arkistoija-moduuli:**
```cpp
// arkistoija/arkistoija.h
class Arkistoija {
    void luoArkisto(QString polku);
    void vieArkisto(QString polku);
    // SAF-T XML -tuonti/vienti
};
```

---

## 7. Käyttöönotto ja Jakelu

### 7.1 Tilitin

**Windows:**
- Inno Setup -asennusohjelma (.exe)
- Sisältää Java Runtime Environment (JRE)
- Tiedostokoko: ~57 MB
- Start-valikko + työpöydän pikakuvake

**JAR-tiedosto (cross-platform):**
- Vaatii Java 25+ asennettuna
- Kaksoisklikkaus käynnistää (macOS: oikea klikkaus)
- Portable, ei asennusta

**Buildaus:**
```bash
mvn clean package                    # JAR
build-windows.bat                    # .exe app-image
build-inno-installer.bat             # Inno Setup installer
build-windows-installer.bat          # MSI installer
```

### 7.2 Kitsas

**Windows, Linux, macOS:**
- Natiivi binäärit kullekin alustalle
- Qt-kirjastot mukana tai dynaamisesti linkitetty
- Erillinen installer per alusta

**Buildaus:**
```bash
qmake kitupiikki.pro && make qmake_all
make
# tai
kaanna-Windows.sh      # Windows MXE cross-compile
kaanna-Linux.sh        # Linux
kaanna-qt6.sh          # Qt6-versio
```

**Riippuvuudet:**
- Qt 6.4+ (6.8 kaikki ominaisuudet)
- libzip (Linux)
- poppler-qt (Linux)

---

## 8. Ylläpito ja Kehitys

### 8.1 Tilitin

**Tekijät:**
- Alkuperäinen: Tommi Helineva
- Modernisaatio 2024: Jouni Seppänen
- Kotlin-migraatio: Käynnissä (Sprint 2.2)

**Versiohistoria:**
- 2.1.5 (tulossa) - DocumentFrame refaktorointi
- 2.1.4 - MenuBuilder, ToolbarBuilder
- 2.1.2 - Kotlin 2.3.0, Data classes
- 2.0.3 - Varmuuskopiointijärjestelmä
- 2.0.0 - FlatLaf, modernit teemat

**Aktiivisuus:**
- Aktiivinen kehitys (feature/2.2-listener-extraction)
- Säännölliset commitit
- Modernisaatio jatkuu

**Dokumentaatio:**
- USER-GUIDE.md (1085+ riviä)
- BUILDING.md, CONTRIBUTING.md
- KOTLIN_MIGRATION.md

### 8.2 Kitsas

**Tekijä:**
- Arto Hyvättinen <arto@kitsas.fi>
- Kitsas Oy (kaupallinen yritys)

**Versiohistoria:**
- Ykkösversio julkaistu nimellä "Kitupiikki"
- Nykyinen nimi: "Kitsas"
- Aktiivinen kehitys ja päivitykset

**Kotisivut:**
- [kitsas.fi](https://kitsas.fi)
- [kitsas.fi/docs](https://kitsas.fi/docs) - Käyttöohjeet

**Lisenssi:**
- GPL v3 + lisäehdot:
  - Muokattu ohjelma merkittävä selkeästi
  - Ei Kitsas Oy:n tukea muokatulle versiolle
  - Kitsas Oy:n nimen välttäminen muokatussa versiossa

---

## 9. Testaus ja Laatu

### 9.1 Tilitin

**Testikattavuus:**
- Ei virallisia yksikkötestejä (vielä)
- Manuaalinen testaus
- TESTAUS.md -dokumentti

**Laadunvalvonta:**
- Kotlin null-safety
- Strong typing
- Maven dependency management

### 9.2 Kitsas

**Testikattavuus:**
- Yksikkötestit (unittest/-hakemisto):
  - AlvLaskelmaTesti
  - KieliTesti
  - ValidatorTest
  - eurotest
  - tositerivitesti
  - viitetesti
- Integraatiotestit (testit/-hakemisto)
- Qt Test Framework

**Laadunvalvonta:**
- C++ tyyppiturvallisuus
- Qt:n signaalimekanismi (compile-time tarkistukset)
- MOC (Meta-Object Compiler) -generointi

---

## 10. Vahvuudet ja Heikkoudet

### 10.1 Tilitin

**Vahvuudet:**
✅ Moderni ulkoasu (FlatLaf)
✅ Cross-platform (JVM)
✅ Helppo asentaa (sisältää JRE)
✅ Kotlin-modernisaatio käynnissä
✅ Yksinkertaisempi koodikanta
✅ Edistynyt varmuuskopiointijärjestelmä
✅ Aktiivinen modernisaatio
✅ Hyvä dokumentaatio

**Heikkoudet:**
❌ Rajoitettu laskutusjärjestelmä
❌ Ei pilvipalvelua
❌ Ei monikäyttäjätukea
❌ Ei verkkolaskuja
❌ Hitaampi käynnistys (JVM)
❌ Suurempi muistinkäyttö
❌ Ei OCR-tunnistusta
❌ Ei lisäosajärjestelmää

### 10.2 Kitsas

**Vahvuudet:**
✅ Laaja ominaisuuskirjo
✅ Täysi laskutusjärjestelmä
✅ Verkkolaskutuki
✅ Pilvipalvelu (Kitsas Oy)
✅ Monikäyttäjätuki
✅ Natiivi suorituskyky
✅ OCR-tuki (Tesseract)
✅ Lisäosajärjestelmä
✅ Monikielisyys (FI, EN, SV)
✅ ALV-ilmoitukset automaattisesti
✅ Laaja testikattavuus

**Heikkoudet:**
❌ Monimutkaisempi koodikanta (894 tiedostoa)
❌ Hitaampi käännösaika
❌ Qt-riippuvuudet
❌ Kaupallinen lisenssi pilvipalvelulle
❌ Kitsas Oy -riippuvuus pilvipalvelulle
❌ Vaikeampi kehitysympäristö (Qt, C++)

---

## 11. Yhteenveto ja Suositukset

### Käyttötapaus 1: Pienyrittäjä (yksinkertainen kirjanpito)

**Suositus: Tilitin**

- Yksinkertainen asentaa ja käyttää
- Riittävät ominaisuudet peruskirjanpitoon
- Ilmainen, ei pilvipalvelumaksuja
- Hyvä varmuuskopiointijärjestelmä

### Käyttötapaus 2: Pienyrittäjä (laskutuksen kanssa)

**Suositus: Kitsas**

- Täysi laskutusjärjestelmä
- Verkkolaskut
- Asiakasrekisteri
- Pilvipalvelu (valinnainen)

### Käyttötapaus 3: Tilitoimisto (monikäyttäjä)

**Suositus: Kitsas (pilvipalvelu)**

- Monikäyttäjätuki
- Käyttöoikeudet
- Tositteen kierto
- Toimistotoiminnot

### Käyttötapaus 4: Kehittäjä (oppiminen)

**Suositus: Tilitin**

- Yksinkertaisempi koodikanta
- Moderni Kotlin-kieli
- Hyvä dokumentaatio
- Aktiivinen modernisaatio

### Käyttötapaus 5: Avoimen lähdekoodin projekti

**Suositus: Molemmat**

- Tilitin: Helpompi aloittaa (Java/Kotlin)
- Kitsas: Laajemmat ominaisuudet (C++/Qt)

---

## 12. Tulevaisuuden Näkymät

### Tilitin

**Kotlin-migraatio (käynnissä):**
- Sprint 2.2: Listener-ekstraktion
- Sprint 2.3: DAO-kerros kokonaan Kotlinissa
- Sprint 3.0: Business logic Kotlinissa
- Tavoite: Täysi Kotlin-pohja vuoden 2026 loppuun mennessä

**Suunnitellut ominaisuudet:**
- Parempi laskutustuki
- REST API?
- Web-UI (tulevaisuudessa)?

### Kitsas

**Aktiivinen kehitys:**
- Qt 6.8 -tuki
- Uusia pilvipalveluominaisuuksia
- Lisäosien kehitys
- Monikielisyyden parantaminen

---

## 13. Lopuksi

Molemmat projektit ovat **erinomaisia esimerkkejä suomalaisesta avoimen lähdekoodin kehityksestä**.

- **Tilitin** on kevyempi, modernimpi ja helpompi aloittaa.
- **Kitsas** on laajempi, ammattimaisempi ja tuotantovalmis.

Valinta riippuu käyttötarpeesta: yksinkertainen kirjanpito vs. täysi liiketoiminnan hallinta.

---

**Dokumentin päivitys:** Tämä dokumentti päivitetään säännöllisesti molempien projektien kehityksen mukana.

**Yhteistyömahdollisuudet:** Olisiko mahdollista jakaa koodia tai ideoita projektien välillä?

---

**Tekijä:** Claude Sonnet 4.5 (AI-avusteinen analyysi)
**Lisenssi:** Sama kuin Tilitin-projekti (GPL v3)
