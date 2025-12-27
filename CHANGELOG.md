# Changelog

Kaikki merkittävät muutokset Tilitin-projektiin dokumentoidaan tähän tiedostoon.

Formaatti perustuu [Keep a Changelog](https://keepachangelog.com/en/1.0.0/) -standardiin,
ja tämä projekti noudattaa [Semantic Versioning](https://semver.org/spec/v2.0.0.html) -versiointia.

## [Unreleased] - Kehityksessä

### Sprint 1.2 Valmis (2024-12-27)

#### Lisätty
- FlatLaf 3.5.2 modernin Look and Feel -teeman tuki
- Vaalea ja tumma teema (`ui.theme` asetus)
- Windows-ikonit (.ico) kaikilla resoluutioilla (16-256px)
- Windows build-skriptit (build-windows.bat, build-windows-installer.bat)
- PowerShell-skripti ikoni-tiedoston luomiseen (create-icon.ps1)
- Kattava build-dokumentaatio (BUILDING.md)
- Testausohjeet (TESTAUS.md)
- Projektisuunnitelma (PROJEKTISUUNNITELMA.md)
- Uusi privaatti GitHub-repositorio (tilitin-modernized)

#### Muutettu
- Versio päivitetty 1.5.0 → 1.6.0
- Kirjanpito.java: setupLookAndFeel() -metodi FlatLaf-tuelle
- UIManager-asetukset: pyöristetyt kulmat (10px), modernit taulukot
- .gitignore: lisätty Windows build-tiedostot, IDE-asetukset

#### Tekninen
- Maven riippuvuudet:
  - flatlaf 3.5.2
  - flatlaf-extras 3.5.2
  - flatlaf-intellij-themes 3.5.2
- Java 21 vaaditaan (ennallaan)
- Fallback-tuki jos FlatLaf epäonnistuu

### Sprint 1.1 Suunnittelu (2024-12-27)

#### Suunniteltu
- Windows modernisointiprojektin roadmap (6-8 viikkoa)
- 5 päävaihetta: UI modernisoit, jPackage, Store-julkaisu, dokumentaatio, lanseeraus
- Budjetti: 10,000-15,000 €
- Kohdealusta: Windows 10/11 (64-bit)

---

## [1.5.0-jkseppan.1] - 2024

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
- Shade plugin 3.6.0

---

## [1.5.0] - Tommi Helineva (Alkuperäinen)

### Alkuperäinen versio
- Tositteiden hallinta
- Tilikartan hallinta
- Kirjausten hallinta (debet/kredit)
- Raportit:
  - Pääkirja (General Journal)
  - Pääkirja detalji (General Ledger)
  - Tuloslaskelma (Income Statement)
  - Tase (Balance Sheet)
  - Tilin tiliote (Account Statement)
  - ALV-raportti (VAT Report)
- PDF-tulostus (iText)
- Monipuolinen tietokantatuki:
  - SQLite (oletus)
  - MySQL
  - PostgreSQL
- Tilikarttamallit:
  - Asunto-osakeyhtiö
  - Elinkeinotoiminta (22%, 23%, 24%)
  - Tiekunta
  - Yhdistys
  - Yhteisen vesialueen osakaskunta
  - Yksityistalous
- Swing-pohjainen käyttöliittymä
- Cross-platform (Windows, Mac, Linux)
- GPL v3 -lisenssi

---

## Tulevat Versiot

### [1.7.0] - Suunniteltu (Q1 2025)
- UI-parannukset (Sprint 1.3-1.4)
- Teeman vaihto Settings-dialogissa
- Parannetut ikonit (SVG-tuki)
- Keyboard shortcuts -parannukset
- Kattavammat testit

### [2.0.0] - Suunniteltu (Q2 2025)
- Microsoft Store -julkaisu
- MSI-asennusohjelma
- Code-signed sovellus
- Automaattiset päivitykset (Store)
- macOS notarisointi (ei App Store)
- Kotlin-migraatio (mahdollinen)

---

## Versiohistorian Merkinnät

- **Lisätty**: Uudet ominaisuudet
- **Muutettu**: Muutokset olemassa oleviin toimintoihin
- **Vanhentunut**: Pian poistettavat ominaisuudet
- **Poistettu**: Nyt poistetut ominaisuudet
- **Korjattu**: Bugien korjaukset
- **Turvallisuus**: Haavoittuvuuksien korjaukset
- **Tekninen**: Infrastruktuuri ja riippuvuudet

---

[Unreleased]: https://github.com/priku/tilitin-modernized/compare/v1.5.0...HEAD
[1.5.0-jkseppan.1]: https://github.com/jkseppan/tilitin/releases/tag/v1.5.0-jkseppan.1
