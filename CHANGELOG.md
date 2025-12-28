# Changelog

Kaikki merkittävät muutokset Tilitin-projektiin dokumentoidaan tähän tiedostoon.

Formaatti perustuu [Keep a Changelog](https://keepachangelog.com/en/1.0.0/) -standardiin,
ja tämä projekti noudattaa [Semantic Versioning](https://semver.org/spec/v2.0.0.html) -versiointia.

---

## [2.1.2] - 2025-12-28

### 🚀 Kotlin Modernization + DocumentFrame Refactoring

**Branch:** `feature/2.1-documentframe-refactor`

### Lisätty
- **DocumentBackupManager** (193 riviä) - Varmuuskopioinnin hallinta
  - Eriytetty backup-logiikka omaksi komponentiksi
  - Testattava arkkitehtuuri callback-rajapintojen kautta
  - DatabaseOpener-rajapinta tietokannan avaukselle
- **DocumentExporter** (83 riviä) - CSV-viennin hallinta
  - Eriytetty export-logiikka omaksi komponentiksi
  - CSVExportStarter-rajapinta viennin käynnistykselle
  - Tiedostonvalinta ja hakemiston muistaminen

### Korjattu
- **CSV-vienti** - Täydellinen Excel-yhteensopivuus
  - ✅ Desimaalierottaja: piste (.) kansainvälisen standardin mukaan
  - ✅ Kenttäerottaja: puolipiste (;) suomalaisen Excelin mukaan
  - ✅ Merkistökoodaus: UTF-8 BOM ääkkösten (ä, ö, å) tueksi
  - ✅ Tiedostopääte: automaattinen .csv-lisäys
  - ✅ Ei tuhanserottimia numeroissa
- **Varmuuskopiointi** (GitHub Copilot)
  - SQLite VACUUM INTO -komento turvalliseen varmuuskopiointiin
  - Ei tiedostolukituksia kopioinnin aikana
  - Fallback perinteiseen file copy -metodiin
  - PRAGMA busy_timeout (30s) SQLITE_BUSY -virheiden estoon

### Muutettu
- **DocumentFrame.java** - Yksinkertaistettu (-88 riviä)
  - Delegoi backup-operaatiot DocumentBackupManager:lle
  - Delegoi CSV-viennin DocumentExporter:lle
  - @Deprecated export() - säilyy yhteensopivuuden vuoksi
  - Implements DatabaseOpener, CSVExportStarter

### Tekninen velka
- DocumentFrame edelleen 3,849 riviä (God Object)
- Jatkotyö: Phase 2-7 (Menu, Toolbar, Table, Print managers)

### Dokumentaatio
- `REFACTORING-NOTES.md` - Yksityiskohtainen refaktorointidokumentaatio
- Inline-kommentit suomeksi ja englanniksi
- Kattavat commit-viestit

### Git-kommitit
```
20a9b46 fix: CSV export UTF-8 with BOM encoding
cb892b5 fix: Auto-append .csv extension
b7d2427 fix: Semicolon delimiter for Finnish Excel
0cd28c4 fix: Dot decimal separator
83fbe1a refactor: Phase 1b DocumentExporter
0a8b447 refactor: Phase 1 DocumentBackupManager
```

---

## [2.0.4] - 2025-12-28

### 🏗️ Foundation Sprint

**Lataukset:** https://github.com/priku/tilitin-modernized/releases/tag/v2.0.4

| Tiedosto | Koko | Kuvaus |
|----------|------|--------|
| `Tilitin-2.0.4-setup.exe` | ~57 MB | ⭐ Suositus! Moderni asennusohjelma |
| `tilitin-2.0.4.jar` | ~25 MB | JAR (vaatii Java 25+) |

### Lisätty
- **UIConstants** - Keskitetyt UI-vakiot (värit, fontit, marginaalit)
- **BaseDialog** - Yhtenäinen pohjaluokka kaikille dialogeille

### Muutettu
- Installer-skripti päivitetty versiolle 2.0.4

---

## [2.0.3] - 2025-12-28

### 💾 Backup System Release

**Lataukset:** https://github.com/priku/tilitin-modernized/releases/tag/v2.0.3

| Tiedosto | Koko | Kuvaus |
|----------|------|--------|
| `Tilitin-2.0.3-setup.exe` | ~57 MB | ⭐ Suositus! Moderni asennusohjelma |
| `tilitin-2.0.3.jar` | ~25 MB | JAR (vaatii Java 25+) |

### Lisätty
- **Moderni varmuuskopiointijärjestelmä** - BackupSettingsDialog
  - Per-tietokanta backup-sijainnit (ei enää globaalia kansiota)
  - Automaattinen pilvipalvelutunnistus (Google Drive, OneDrive, Dropbox, iCloud)
  - USB-asemien tunnistus irrotettaville tallennusvälineille
  - AutoBackup - Word-tyylinen automaattinen varmuuskopiointi (1-60 min välein)
  - Manuaalinen "Tee nyt" - varmuuskopioi heti kaikkiin sijainteihin
- **DatabaseBackupConfigDialog** - Yksittäisen tietokannan sijaintien hallinta
- **RestoreBackupDialog** - Varmuuskopion palautus
- **BackupService** - Taustalla toimiva varmuuskopiointipalvelu
- **CloudStorageDetector** - Pilvipalveluiden ja USB-asemien tunnistus
- **BackupLocation & DatabaseBackupConfig** - Uudet malliluokat

### Muutettu
- Valikko: Tiedosto → Varmuuskopiointiasetukset
- Varmuuskopiot sisältävät hash-tunnisteen polusta (ei sekoitu)
- Siivotaan automaattisesti vanhat kopiot (säilytä 1-100 versiota)

---

## [2.0.2] - 2025-12-28

### 🎨 Icon Modernization Release

**Lataukset:** https://github.com/priku/tilitin-modernized/releases/tag/v2.0.2

| Tiedosto | Koko | Kuvaus |
|----------|------|--------|
| `Tilitin-2.0.2-setup.exe` | ~57 MB | ⭐ Suositus! Moderni asennusohjelma |
| `tilitin-2.0.2.jar` | ~25 MB | JAR (vaatii Java 25+) |

### Lisätty
- **Modernisoidut sovellusikonit** - "Evolution" -tyyli IKONI-MODERNISOINTI.md:n mukaan
  - Tummansininen → sininen gradientti
  - Pyöristetyt kulmat
  - Hienovarainen varjo
  - Euro-symboli (€) oikeassa yläkulmassa
  - Turkoosi kynä glow-efektillä
  - Grid-viivat (viittaa kirjanpitotaulukkoon)
- **PowerShell-skripti** - `create-modern-icons.ps1` ikonien generointiin
- **Multi-resolution .ico** - Kaikki koot: 16x16, 24x24, 32x32, 48x48, 64x64, 128x128, 256x256
- **Dynaaminen versionumero** - Ikkunaotsikko näyttää nyt tarkan version (esim. "Tilitin 2.0.2")
- **Automaattinen vanhan version poisto** - Asennusohjelma poistaa aiemman version automaattisesti

### Muutettu
- Väripaletti päivitetty:
  - Primääri: #1E3A8A (tummansininen) → #3B82F6 (sininen)
  - Aksentti: #14B8A6 (teal/turkoosi)
  - Euro: #F59E0B (kulta)

---

## [2.0.1] - 2025-12-28

### 🎨 UX Improvements Release

**Lataukset:** https://github.com/priku/tilitin-modernized/releases/tag/v2.0.1

| Tiedosto | Koko | Kuvaus |
|----------|------|--------|
| `Tilitin-2.0.1-setup.exe` | 57 MB | ⭐ Suositus! Moderni asennusohjelma |
| `tilitin-2.0.1.jar` | 25 MB | JAR (vaatii Java 25+) |

### Lisätty
- **Splash screen** - Moderni käynnistysnäyttö progress-palkilla
- **Viimeisimmät tietokannat** - Tiedosto-valikossa lista viimeksi avatuista tietokannoista (max 10)
- **Uudet näppäinoikotiet:**
  - `Ctrl+U` - Uusi tietokanta
  - `Ctrl+D` - Tietokanta-asetukset
  - `Ctrl+B` - Alkusaldot
  - `Ctrl+P` - Perustiedot
  - `Ctrl+E` - Vie tiedostoon
  - `Ctrl+Shift+S` - Kirjausasetukset
  - `Ctrl+Shift+A` - Ulkoasu
  - `Shift+Delete` - Poista vienti

### Muutettu
- **Toolbar** - Paremmat välistykset ja näppäinoikotievihjeet tooltip-teksteissä

---

## [2.0.0] - 2025-12-28

### 🚀 Windows Modernization Release

**Lataukset:** https://github.com/priku/tilitin-modernized/releases/tag/v2.0.0

| Tiedosto | Koko | Kuvaus |
|----------|------|--------|
| `Tilitin-2.0.0-setup.exe` | 57 MB | ⭐ Suositus! Moderni asennusohjelma |
| `tilitin-2.0.0.jar` | 24 MB | JAR (vaatii Java 25+) |

### Lisätty
- **FlatLaf-teema** - Moderni käyttöliittymä FlatLaf 3.5.2 -kirjastolla
- **Teemavaihtodialogi** - Vaihda teemaa kätevästi Muokkaa → Ulkoasu... -valikosta
- **Vaalea ja tumma teema** - Vaihdettavissa lennossa ilman uudelleenkäynnistystä
- **Windows natiivi-sovellus** - jPackage-paketointi (.exe)
- **Windows MSI-asennusohjelma** - WiX Toolset 3.14
- **Inno Setup -asennusohjelma** - Moderni ulkoasu, LZMA2-pakkaus (21% pienempi)
- **Build-skriptit**:
  - `build-windows.bat` - Luo natiivi .exe-sovelluksen
  - `build-windows-installer.bat` - Luo MSI-asennusohjelman
  - `build-inno-installer.bat` - Luo modernin Inno Setup -asennusohjelman
- **Dokumentaatio**:
  - `PROJEKTISUUNNITELMA.md` - Kehityssuunnitelma ja sprintit
  - `TESTAUS.md` - Testausohjeet ja raportit
  - `BUILDING.md` - Build-ohjeet
  - `CONTRIBUTING.md` - Kehittäjäohjeet

### Muutettu
- **Versio** - 1.6.0 → 2.0.0 (major version bump)
- **Sovelluksen nimi** - "Tilitin" → "Tilitin 2.0"
- **Käyttöliittymä** - Vanha Swing-teema → FlatLaf Light/Dark
- **Java-versio** - Java 21 → Java 25 (LTS)
- **Kirjastopäivitykset:**
  - FlatLaf 3.5.2 → 3.7
  - SQLite JDBC 3.46.0.1 → 3.51.1.0
  - MySQL Connector 9.0.0 → 9.5.0
  - PostgreSQL 42.7.3 → 42.7.8

### Korjattu
- Native access -varoitukset poistettu jPackage-buildista
- Build-skriptien polut korjattu

### Yhteensopivuus
- ✅ Asetukset säilyvät samassa kansiossa (`%APPDATA%\Tilitin`)
- ✅ Vanhat tietokannat toimivat sellaisenaan
- ✅ Rinnakkainen asennus vanhan version kanssa mahdollinen

---

## [1.6.0] - 2024

Jouni Seppäsen ylläpitämä versio (jkseppan/tilitin).

### Lisätty
- ARM Mac -tuki (M1/M2/M3 -sirut)
- Tilikarttamallit jar-paketin sisällä
- Uusi tilikartta ALV 25.5% -prosentilla
- LISENSSIT.html kaikkien kirjastojen lisensseille

### Korjattu
- Mac-bugi: tekstikentän ensimmäinen merkki katosi
- Tilikarttamallien sijainti-ongelma

### Muutettu
- Päivitetty kirjastot:
  - iText PDF 5.5.13.4
  - SQLite JDBC 3.46.0.1
  - MySQL Connector 9.0.0
  - PostgreSQL 42.7.3
  - SLF4J 2.0.13

### Tekninen
- Java 25 vaaditaan (aiemmin Java 8)
- Maven 3.9.12+

---

## [1.5.0] - Tommi Helineva (Alkuperäinen)

Alkuperäinen Tilitin - ilmainen kirjanpito-ohjelma.

Dokumentaatio: https://helineva.net/tilitin/

### Ominaisuudet
- Tositteiden hallinta
- Tilikartan hallinta
- Kirjausten hallinta (debet/kredit)
- Raportit: Pääkirja, Tuloslaskelma, Tase, ALV-raportti
- PDF-tulostus (iText)
- Tietokantatuki: SQLite, MySQL, PostgreSQL
- Tilikarttamallit suomalaisille organisaatioille
- Cross-platform (Windows, Mac, Linux)
- GPL v3 -lisenssi

---

## Linkit

- **Tilitin 2.0 (Modernized):** https://github.com/priku/tilitin-modernized
- **Jouni Seppäsen fork:** https://github.com/jkseppan/tilitin
- **Alkuperäinen dokumentaatio:** https://helineva.net/tilitin/

[2.0.0]: https://github.com/priku/tilitin-modernized/releases/tag/v2.0.0
[1.6.0]: https://github.com/jkseppan/tilitin/releases
[1.5.0]: https://helineva.net/tilitin/
