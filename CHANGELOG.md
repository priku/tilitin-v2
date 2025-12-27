# Changelog

Kaikki merkittävät muutokset Tilitin-projektiin dokumentoidaan tähän tiedostoon.

Formaatti perustuu [Keep a Changelog](https://keepachangelog.com/en/1.0.0/) -standardiin,
ja tämä projekti noudattaa [Semantic Versioning](https://semver.org/spec/v2.0.0.html) -versiointia.

---

## [2.0.0] - 2025-12-28

### 🚀 Windows Modernization Release

**Lataukset:** https://github.com/priku/tilitin-modernized/releases/tag/v2.0.0

| Tiedosto | Koko | Kuvaus |
|----------|------|--------|
| `Tilitin-2.0.0-setup.exe` | 57 MB | ⭐ Suositus! Moderni asennusohjelma |
| `tilitin-2.0.0.jar` | 24 MB | JAR (vaatii Java 21+) |

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
- Java 21 vaaditaan (aiemmin Java 8)
- Maven 3.13.0

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
