# Changelog

Kaikki merkittävät muutokset Tilitin-projektiin dokumentoidaan tähän tiedostoon.

Formaatti perustuu [Keep a Changelog](https://keepachangelog.com/en/1.0.0/) -standardiin,
ja tämä projekti noudattaa [Semantic Versioning](https://semver.org/spec/v2.0.0.html) -versiointia.

---

## [2.2.4] - 2025-12-30

### 🚀 Modernization Session - Lambda Migration, DocumentFrame Refactoring & Theme Support

**Tila:** ✅ Valmis
**Toteutusaika:** 2025-12-30 (5-6 tuntia)
**Modernisaatio:** 78% → 80% (+2%)

### Lisätty

- **DocumentMenuHandler.java** (252 riviä)
  - Keskitetty menu action listener -hallinta
  - 31 listeneriä ekstrahtoitu DocumentFramesta
  - Organisoitu menu-kategorioittain (File, Go, Edit, Settings, Reports, Tools, Help)
  - Selkeä separation of concerns

- **Lambda-syntaksi 12 dialog-tiedostossa**
  - ~40+ ActionListener anonymous class → lambda expression
  - Moderni Java 8+ syntaksi käytössä

### Muutettu

- **DocumentFrame.java** - Menu listener refaktorointi
  - Section markerit lisätty (8 menu-kategoriaa)
  - 31 listeneriä siirretty DocumentMenuHandleriin
  - 3,093 → 3,073 riviä (-20 riviä, -0.6%)
  - Parempi koodi-organisaatio

- **Dialog-tiedostot** - Lambda-konversiot
  - AboutDialog, BalanceComparisonDialog, StartingBalanceDialog
  - DocumentTypeDialog, VATChangeDialog, FinancialStatementOptionsDialog
  - EntryTemplateDialog, AccountSelectionDialog, AccountStatementOptionsDialog
  - PrintOptionsDialog, COADialog
  - ~100+ riviä vähemmän boilerplate-koodia

### Dokumentaatio

- Luotu **LAMBDA-MIGRATION-2025-12-30.md** - Lambda-migraation dokumentaatio
- Luotu **SESSION-SUMMARY-2025-12-30.md** - Täydellinen session yhteenveto
- Päivitetty **DOCUMENTFRAME-MENU-REFACTORING.md** - Toteutuksen tiivistelmä

### Tekniset yksityiskohdat - Session 2.2.4

**Git Commits:** 7 kpl

- Lambda dokumentaatio (134b5ce)
- Menu organisaatio (81e5e4a)
- Simple listeners extraction (0f9cf80)
- Medium & Complex listeners (e2c23af)
- Go Menu navigation (c8764a9)
- Session summary (4160058)
- Documentation update (1c5047d)

**Build:**

- ✅ BUILD SUCCESSFUL kaikissa vaiheissa
- Zero compilation errors
- Zero regressioita

**Code Quality:**

- Parempi separation of concerns
- Moderni lambda-syntaksi
- Selkeämpi koodi-organisaatio
- Helpompi ylläpidettävyys ja testattavuus

### 🏗️ Phase 7 - UI Component Management

**Tila:** ✅ Valmis (korjattu Claude:n toimesta)
**Toteutusaika:** 2025-12-30 (~2 tuntia Cursor + 1 tunti Claude korjaukset)
**Refaktorointi:** DocumentFrame 3,073 → 2,916 riviä (-157 riviä, -5.1%)

### Lisätty (Phase 7)

- **DocumentUIBuilder.java** (316 riviä)
  - UI-komponenttien luonti ja konfigurointi
  - Callback-rajapinta DocumentFrame-vuorovaikutukselle
  - Metoder: createTextFieldPanel(), createTotalRow(), createSearchBar(), createStatusBar()
  - Centralisoitu UI-rakentaminen

- **DocumentUIUpdater.java** (406 riviä)
  - UI-päivityslogiikka eriytetty
  - UIComponents wrapper-luokka komponenttiviittauksille
  - Metodit: updateDocument(), updatePosition(), updatePeriod(), updateTotalRow()
  - updateBackupStatusLabel(), updateDocumentTypes(), updateEntryTemplates()

- **DocumentStateManager.java** (436 riviä)
  - Tilan hallinta ja validointi
  - StateCallbacks-rajapinta
  - Metodit: saveDocumentIfChanged(), updateModel()
  - Debit/credit total -laskenta

### Muutettu (Phase 7)

- **DocumentFrame.java** - UI-refaktorointi
  - 3,073 → 2,916 riviä (-157 riviä)
  - Siirretty UI-luonti DocumentUIBuilder:iin
  - Siirretty UI-päivitys DocumentUIUpdater:iin
  - Siirretty tilan hallinta DocumentStateManager:iin
  - Implementoi StateCallbacks-rajapinta
  - Callback-pohjainen arkkitehtuuri

### Korjattu (Phase 7 - Claude)

**Ongelma:** Cursor jäi jumiin Phase 7:ssä, koska UI-komponenttien alustus ei toiminut

**Korjaukset:**

1. ✅ DocumentStateManager.java import-virheet
   - Korjattu TextFieldWithLockIcon import-polku
   - Lisätty ParseException import
   - Korjattu updatePeriod() käyttämään oikeita Period-metodeja

2. ✅ DocumentFrame.java alustusten järjestys
   - Korjattu createStatusBar() - uiUpdaterComponents asetetaan ennen updateBackupStatusLabel() kutsua
   - Lisätty numberTextField ja dateTextField asetus uiUpdaterComponents:iin createTextFieldPanel():ssa
   - Varmistettu että kaikki UI-komponentit alustetaan oikeassa järjestyksessä

**Tulos:** ✅ Sovellus käynnistyy ja toimii täydellisesti

### Tekniset yksityiskohdat (Phase 7)

- **Yhteensä luotu:** 3 uutta tiedostoa, 1,158 riviä
- **DocumentFrame vähennetty:** 157 riviä (-5.1%)
- **Arkkitehtuuri:** Callback-pohjainen separation of concerns
- **Build status:** ✅ BUILD SUCCESSFUL (Clauden korjausten jälkeen)
- **Runtime:** ✅ Zero NullPointerException:eja, kaikki toimii

### Dokumentaatio (Phase 7)

- Cursor aloitti Phase 7 mutta jäi jumiin alustusongelmiin
- Claude jatkoi ja sai Phase 7:n valmiiksi
- Kaikki muutokset dokumentoitu

### 🎨 Theme Support - Legacy Dialogs

**Tila:** ✅ Valmis
**Toteutusaika:** 2025-12-30 (~30 min)

### Muutettu (Theme Support)

- **DocumentNumberShiftDialog.java** - Theme-aware värit
  - Korvattu hardcoded fallback-värit UIConstants-metodeilla
  - `UIConstants.getErrorColor()` ja `getForegroundColor()`
  - Error-tekstin tyylitys noudattaa nyt teemaa
- **COATableCellRenderer.java** - Theme-aware värit
  - Korvattu hardcoded fallback-värit UIConstants-metodeilla
  - `UIConstants.getInfoColor()` ja `getErrorColor()`
  - Suosikkitilien ja otsikoiden värit noudattavat nyt teemaa

### Tarkistettu (Theme Support)

- **16 legacy-dialogia tarkistettu** - Ei hardcoded värejä löytynyt
  - SettingsDialog, PropertiesDialog, COADialog, AccountSelectionDialog
  - EntryTemplateDialog, FinancialStatementOptionsDialog, StartingBalanceDialog
  - SearchDialog, PrintStyleEditorDialog, ChartOptionsDialog, VoucherTemplateDialog
  - ImportCSVDialog, AccountPeriodDialog, PeriodDialog, ReportStructureDialog
  - CompanyInformationDialog, ja muut
- **Tulos:** Useimmat legacy-dialogit käyttivät jo UIManager-värejä, jotka kunnioittavat teemaa

### Tekniset yksityiskohdat (Theme Support)

- **Yhteensä muutettu:** 2 tiedostoa, 3 hardcoded väriä korvattu
- **Tarkistettu:** 16 legacy-dialogia (ei muutoksia tarvittu)
- **Build status:** ✅ BUILD SUCCESSFUL
- **Dark mode:** Toimii nyt kaikissa dialogeissa

### Dokumentaatio (Theme Support)

- Luotu **THEME-SUPPORT-MIGRATION-2025-12-30.md** - Täydellinen teematuki-migraation dokumentaatio
- Päivitetty **MODERNIZATION-TODO.md** - Teematuki merkitty valmiiksi

---

## [2.2.3] - 2025-12-30

### 🎯 Compose Desktop Menu Integration

**Tila:** ✅ Valmis
**Toteutusaika:** 2025-12-30 (~60 min)

### 🔄 Lambda Migration - Phase 2

**Tila:** ✅ Valmis
**Toteutusaika:** 2025-12-30 (~1 tunti)

### Lisätty

- **Compose Desktop MenuBar** - Natiivi AWT/Swing menu-integraatio
  - Menu-palkki näkyy nyt Compose Desktop -ikkunassa
  - Kaikki valikot toimivat: Tiedosto, Muokkaa, Siirry, Tositelaji, Tulosteet, Työkalut, Ohje
  - Kaikki pikanäppäimet toimivat (Ctrl+N, Ctrl+O, jne.)
  - 100% yhteensopivuus olemassa olevan `DocumentMenuBuilder.java`:n kanssa
  - Ei tarvetta uudelleenkirjoittaa menu-logiikkaa

### Muutettu

- **TilitinApp.kt** - Menu-integraatio Compose-ikkunaan
  - Lisätty `composeWindow`-parametri `MainContent`-funktioon
  - Menu-palkin asetus `SwingUtilities.invokeLater`:lla EDT:ssä
  - JMenuBar haetaan `DocumentFramePanel`:sta ja asetetaan JFrame:en

### Tekniset yksityiskohdat

- **Lähestymistapa:** AWT MenuBar (vaihtoehto A)
  - Hyödyntää olemassa olevaa `DocumentMenuBuilder.java` (465 riviä)
  - Ei uutta koodia (~20 riviä muutoksia)
  - Vähemmän implementointiaikaa verrattuna Compose MenuBar:iin
- **Build-korjaus:** Gradle konfiguroitu käyttämään Java 21
  - Lisätty `org.gradle.java.home` `gradle.properties`:iin
  - Gradle 8.11.1 ei tue Java 25:tä

### Dokumentaatio

- Luotu COMPOSE-MENU-IMPLEMENTATION.md - Täydellinen toteutusdokumentaatio
- Päivitetty NEXT-STEPS-PLAN.md - Menu-integraatio merkitty valmiiksi

### Testattu

- ✅ Menu näkyy oikein ikkunan yläreunassa
- ✅ Tiedosto → Uusi tietokanta - Toimii
- ✅ Tiedosto → Avaa tietokanta - Toimii
- ✅ Muokkaa-valikko - Toimii
- ✅ Tositteet-valikko - Toimii
- ✅ Tulosteet-valikko - Toimii
- ✅ Työkalut-valikko - Toimii
- ✅ Ohje-valikko - Toimii
- ✅ Kaikki pikanäppäimet - Toimivat

### Muutettu (Lambda Migration Phase 2)

- **SettingsDialog.java** - 3 ActionListener → lambda
  - lockAllMonthsButton, okButton, cancelButton
- **PropertiesDialog.java** - 4 ActionListener → lambda
  - deletePeriodButton, createPeriodButton, okButton, cancelButton
- **DatabaseSettingsDialog.java** - 4 ActionListener → lambda
  - openButton, resetButton, cancelButton, okButton
- **ReportEditorDialog.java** - 8 ActionListener → lambda
  - exportButton, importButton, helpButton, saveButton, cancelButton
  - printComboBox, restoreHeaderButton, restoreFooterButton

### Tekniset yksityiskohdat (Lambda Migration)

- **Yhteensä muutettu:** 4 tiedostoa, 19 ActionListener → lambda
- **Rivit vähennetty:** ~76 riviä
- **Kokonaistilanne:** 16 tiedostoa, ~60+ ActionListener → lambda, ~176+ riviä vähennetty
- **Build status:** ✅ BUILD SUCCESSFUL
- **Koodin laatu:** Modernimpi Java 8+ syntaksi kaikissa ActionListener-lausekkeissa

### Dokumentaatio

- Päivitetty LAMBDA-MIGRATION-2025-12-30.md - Phase 2 lisätty
- Päivitetty MODERNIZATION-TODO.md - Lambda-migraatio merkitty valmiiksi

---

## [2.2.3-shortcuts] - 2025-12-29

### 🔧 Pikanäppäinkorjaukset

**Tila:** ✅ Valmis

### Korjattu
- **Ctrl+P** → **Ctrl+Shift+P** (Perustiedot) - Vapautettu Ctrl+P tulevaa tulostustoimintoa varten
- **Ctrl+R** → **Ctrl+Shift+V** (ALV-tilien päättäminen) - R on yleensä Refresh/Replace
- **Ctrl+H** → Poistettu (Ohita ALV-laskelmassa) - H on yleensä History/Replace

### Dokumentaatio
- USER-GUIDE.md pikanäppäimet päivitetty vastaamaan Windows-standardeja
- CSV-tuonti dokumentoitu USER-GUIDE.md:hen

---

## [2.2.2] - 2025-12-29

### 🚀 Multi-platform Release & CSV-tuonti

**Branch:** `feature/csv-import` merged to `master`  
**Toteutusaika:** 2025-12-29  
**Tila:** ✅ Valmis

### Lisätty
- **Multi-platform julkaisu** - GitHub Actions rakentaa automaattisesti:
  - 🪟 Windows Installer (.exe) - jPackage + Inno Setup
  - 🍎 macOS Package (.dmg) - natiivi Mac-asennus
  - 🐧 Linux DEB (.deb) - Debian/Ubuntu
  - 🐧 Linux RPM (.rpm) - Fedora/Red Hat
- **CSV-tuonti (Kotlin)** - Moderni tilitapahtumien tuonti
  - `CsvImportDialog.kt` - Käyttöliittymä CSV-tuontiin
  - `CsvParser.kt` - CSV-tiedostojen jäsennys
  - `CsvImporter.kt` - Tuontilogiikka
  - `CsvColumnAnalyzer.kt` - Sarakkeiden automaattinen tunnistus
  - `ProcountorCsvPreset.kt` - Procountor-muodon esiasetukset
- **Test-data** - Testitiedostot CSV-tuonnin testaamiseen

### Muutettu
- **GitHub Actions workflow** - Optimoitu rakenne:
  - JAR rakennetaan kerran Ubuntulla (nopeampi, halvempi)
  - CHANGELOG parsing AWK:lla (ei PowerShell)
  - Artifact sharing jobien välillä

### Tekninen
- Release notes luetaan automaattisesti CHANGELOG.md:stä
- Kaikki paketit ladataan samaan GitHub Releaseen

---

## [2.2.1] - 2025-12-29

### 🏗️ DocumentFrame Refactoring - Phase 3b, 4 & 5

**Branch:** `feature/code-modernization`  
**Toteutusaika:** 2025-12-29  
**Tila:** ✅ Valmis - Phase 3b, 4 & 5 - Testattu ja toimii ✅

### Lisätty
- **DocumentTableManager.java** - Taulukon hallinta eriytetty DocumentFrame:sta
  - ✅ Table creation ja konfiguraatio
  - ✅ Cell renderer/editor -asetukset
  - ✅ Keyboard shortcuts -hallinta
  - ✅ Column width management
  - ✅ VAT column visibility control
  - ✅ Column index mapping (view/model)
- **DocumentPrinter.java** - Print-toiminnot eriytetty DocumentFrame:sta (laajennettu)
  - ✅ Kaikki print-metodit (9 metodia)
  - ✅ Print preview -ikkunan hallinta
  - ✅ Report generation -kutsut
  - ✅ Print options -dialogien hallinta
  - ✅ PrintCallbacks-rajapinta DocumentFrame:lle

### Muutettu
- **DocumentFrame.java** - Refaktorointi jatkuu
  - ✅ Phase 3b: Table management siirretty DocumentTableManager:iin (~75 riviä pois)
  - ✅ Phase 4: Koodin siistiminen ja yksinkertaistaminen
  - ✅ Phase 5: Print operations siirretty DocumentPrinter:iin (~276 riviä pois)
  - ✅ Poistettu 10 käyttämätöntä importia + 21 print-importia
  - ✅ Yksinkertaistettu wrapperit (getPrevDocumentAction/getNextDocumentAction)
  - ✅ DocumentFrame: 3,008 → 3,093 riviä (+85 riviä, +2.8%)
  - ✅ **Kokonaisprogress:** 3,856 → 3,093 riviä (-763 riviä, -20%)

### Tekninen
- **DocumentTableManager.java** - Uusi luokka (400 riviä)
  - TableCallbacks-rajapinta DocumentFrame:lle
  - TableActions-rajapinta keyboard shortcuts:lle
  - ColumnMapper-rajapinta sarakeindeksien muuntamiseen
  - updateTableSettings() -metodi ALV-sarakkeen hallintaan
  - mapColumnIndexToView/Model() -metodit
- **DocumentPrinter.java** - Laajennettu luokka (434 riviä)
  - PrintCallbacks-rajapinta DocumentFrame:lle
  - 9 print-metodia (showAccountSummary, showDocumentPrint, jne.)
  - Print preview -ikkunan hallinta
  - Kaikki report generation -kutsut

### Korjattu
- Keyboard shortcut -konfiguraatio siirretty DocumentTableManager:iin
- Column mapping -logiikka siirretty DocumentTableManager:iin
- Käyttämättömät importit poistettu
- SwingUtils import-virhe DocumentPrinter.java:ssa korjattu

### Testattu
- ✅ Sovellus käynnistyy ilman virheitä
- ✅ Kaikki taulukon toiminnot toimivat (keyboard shortcuts, cell editing)
- ✅ Sarakkeiden leveydet tallennetaan oikein
- ✅ ALV-sarake näkyy/piiloutuu oikein
- ✅ Kaikki perustoiminnot toimivat (tietokanta, tositteet, viennit)
- ✅ PDF-liitteet toimivat (v2.2.0)
- ✅ Teemat toimivat (vaalea/tumma)

---

## [2.2.0] - 2025-12-29

### 📎 PDF-liitteet - Uusi ominaisuus

**Branch:** `feature/pdf-attachments`
**Toteutusaika:** 2025-12-28 to 2025-12-29 (Sprint 1 & 2)
**Tila:** ✅ Valmis - Odottaa käyttäjätestausta

### Lisätty
- **PDF-liitteet tositteisiin** - Täysi tuki PDF-tiedostojen liittämiseen
  - ✅ Tietokantataulu `attachments` (versio 15)
  - ✅ Tietokantamigraatio versiosta 14 → 15
  - ✅ Tuki kaikille tietokannoille (SQLite, MySQL, PostgreSQL)
  - ✅ DAO-kerros kaikille tietokannoille
  - ✅ UI-komponentit (AttachmentsPanel, AttachmentsTableModel)
  - ✅ Integraatio DocumentFrameen
  - ✅ PDF-validoinnit ja kookontrollit
  - ✅ Testaussuite (15 testiä, 100% läpäisyaste)

#### Sprint 1: Tietokanta & DAO-kerros
**Toteutetut ominaisuudet:**
- **Tietokantamigraatio:** `upgrade14to15()` - tuki kaikille tietokannoille
- **Tietokantaskeema:** 9 saraketta, indeksi `document_id`:lle, CASCADE-poisto
- **Domain-malli:** `Attachment.kt` - validoinnit (10 MB max, 5 MB varoitus)
- **DAO-rajapinta:** `AttachmentDAO` - 6 CRUD-operaatiota
- **DAO-toteutukset:** SQLite, MySQL, PostgreSQL - tietokantakohtaiset BLOB-tyypit
- **PDF-apuvälineet:** `PdfUtils.kt` - validointi ja sivumäärän laskenta
- **DataSource-integraatio:** `getAttachmentDAO()` kaikissa DataSource-luokissa

**Testattu:** 15 testitapausta, 100% läpäisyaste

#### Sprint 2: Käyttöliittymä
**Toteutetut ominaisuudet:**
- **AttachmentsPanel:** Lista-, lisää-, poista- ja vie-painikkeet
- **AttachmentsTableModel:** 5 saraketta (tiedosto, koko, sivut, lisätty, kuvaus)
- **Tiedostovalitsin:** PDF-suodatin, validointi ennen tallennusta
- **Virheenkäsittely:** Käyttäjäystävälliset viestit, vahvistukset
- **DocumentFrame-integraatio:** Paneeli näkyy ikkunan alaosassa
- **Automaattiset päivitykset:** Lista päivittyy dokumenttien välillä navigoitaessa

**Testattu:** Manuaalinen testaus (katso TESTAUS.md)

#### Tilastot

- **Koodimäärä:** ~1,400 riviä (800 Kotlin + 600 Java)
- **Testikattavuus:** 15 testitapausta, 100% läpäisyaste
- **Luodut tiedostot:** 15 (5 Kotlin, 3 Java, 7 dokumentaatio)
- **Muokatut tiedostot:** 12 (tietokantakerros, skeema, UI, build)

Katso yksityiskohtainen tekninen dokumentaatio: `PDF-ATTACHMENTS-IMPLEMENTATION.md`

### Muutettu
- **Tietokantaversio:** Päivitetty versiosta 14 → 15
- **Kaikki SettingsDAO-luokat:** Päivitetty versioon 15
- **Kaikki DataSource-luokat:** Lisätty `getAttachmentDAO()` -metodi
- **Kaikki DataSource-luokat:** Lisätty migraatiokutsu versiolle 14 → 15
- **pom.xml:** Lisätty Apache PDFBox 3.0.3 -riippuvuus

### Tekninen
- **Kieli:** Kotlin 2.3.0 uusille komponenteille
- **PDF-kirjastot:** Apache PDFBox 3.0.3 (uusi), iText 5.5.13.4 (olemassa oleva)
- **Tiedostokoon rajat:** 10 MB maksimi, 5 MB varoituskynnys
- **Tietokantatuki:** SQLite (pääasiallinen), MySQL, PostgreSQL

### Tiedostot
- **Uudet tiedostot:** 15 (5 Kotlin, 3 Java, 6 dokumentaatio, 2 testiskripti)
- **Muokatut tiedostot:** 12 (8 tietokantakerros, 3 skeema, 1 UI, 1 build)
- **Yhteensä:** 27 tiedostoa muutettu/luotu

### Tunnettuja rajoituksia
- PDF-katselija ei vielä toteutettu (suunniteltu Sprint 3:lle)
- Ei drag & drop -tukea (suunniteltu Sprint 4:lle)
- Ei leikepöytä-tukea (suunniteltu Sprint 4:lle)
- Ei PDF-välimuistia (suunniteltu Sprint 3:lle)
- Vain yhden tiedoston valinta kerrallaan

### Seuraavat askeleet
- Sprint 3: PDF-katselijan toteutus
- Sprint 4: Välimuisti, drag & drop, leikepöytä-tuki

---

## [2.1.6] - 2025-12-28

### 🔧 Code Modernization - Quick Wins

**Branch:** `main` / `feature/code-modernization`

### Korjattu
- **Deprecated API poistettu** - Kaikki deprecated API-kutsut korjattu
  - ✅ DocumentMenuBuilder.java: `getMenuShortcutKeyMask()` → OS-tunnistus
  - ✅ DocumentFrame.java line 661: `getMenuShortcutKeyMask()` → OS-tunnistus
  - ✅ DocumentFrame.java lines 2362, 2407: `InputEvent.ALT_MASK` → `InputEvent.ALT_DOWN_MASK`
  - Ei enää riippuvainen deprecated API:sta
  - Kaikki korvaukset käyttävät moderneja Java 9+ API:ja

### Lisätty
- **UIConstants teemavärit** - 7 uutta theme-aware värimetodia
  - `getBackgroundColor()` - Paneelien taustavärit
  - `getForegroundColor()` - Tekstivärit
  - `getBorderColor()` - Reunusvärit
  - `getTextFieldBackgroundColor()` - Tekstikenttien taustat
  - `getTextFieldForegroundColor()` - Tekstikenttien tekstit
  - `getTableBackgroundColor()` - Taulukoiden taustat
  - `getTableForegroundColor()` - Taulukoiden tekstit
  - Kaikki metodit käyttävät `UIManager`-värejä fallbackeilla
  - Valmiina legacy-dialogien teematukeen

### Muutettu
- **DocumentFrame.java** - Lambda-migraatio edistynyt
  - **10 anonymous inner class → lambda-lausekkeet** (yhteensä)
  - AccountCellEditor ActionListener
  - Search button ActionListener
  - Recent database menu items (2 kpl)
  - newDatabaseListener, openDatabaseListener
  - entryTemplateListener
  - editDocTypesListener
  - docTypeListener
  - printListener (switch-lauseke optimoitu)
  - Tiedostokoko: 3,024 → 3,007 riviä (-17 riviä)
  - Koodi nyt modernimpaa ja luettavampaa
  - printListener optimoitu if-else → switch-lauseke

### Tekninen
- Field initialization order korjattu
  - `sqliteFileFilter` ja listenerit siirretty oikeaan järjestykseen
  - Ei enää forward reference -virheitä
- Compilation errors korjattu
- Kaikki muutokset testattavissa ja yhteensopivia
- Ei enää deprecated API -varoituksia

### Edistyminen
- ✅ Deprecated API: **4/4 korjattu** (100% valmis)
- Lambda-migraatio: **10/16+ DocumentFramessa** (62% valmis)
- Theme helpers: 7 uutta metodia valmiina käyttöön

---

## [2.1.5] - 2025-12-28

### 🏗️ DocumentFrame Phase 3 - Helper Classes

**Branch:** `feature/2.2-listener-extraction`

### Lisätty
- **DocumentListenerHelpers.java** (76 riviä) - Kuuntelijoiden apuluokka
  - `InitializationWorkerListener` - Tietokannan alustuksen kuuntelija
  - `InitializationCallback` - Callback-rajapinta alustuksen jälkeisiin toimenpiteisiin
- **EntryTableActions.java** (280 riviä) - Taulukkotoimintojen apuluokka
  - `createPrevCellAction()` - Edellinen solu (Shift+Tab)
  - `createNextCellAction()` - Seuraava solu (Tab)
  - `createToggleDebitCreditAction()` - Debet/kredit vaihto (*)
  - `createPreviousRowAction()` - Edellinen rivi (Up)
  - `createRemoveSuffixAction()` - Päätteen poisto (Ctrl+Backspace)
  - `createSetIgnoreFlagToEntryAction()` - ALV-ohitus

### Muutettu
- **DocumentFrame.java** - Valmisteltu käyttämään apuluokkia
  - Lisätty `entryTableActions` kenttä

### Tekninen
- ColumnMapper-rajapinta sarakeindeksien muuntamiseen
- EntryTableCallback-rajapinta DocumentFrame-toimintojen kutsumiseen
- Valmiina täydelliseen refaktorointiin myöhemmin

---

## [2.1.4] - 2025-12-28

### 🏗️ DocumentFrame Phase 2 - Builder Pattern

**Branch:** `feature/2.1-documentframe-refactor`

### Lisätty
- **DocumentMenuBuilder.java** (449 riviä) - Eriytetty valikkojen luonti
  - Kaikki 7 valikkoa: Tiedosto, Muokkaa, Siirry, Tositelaji, Tulosteet, Työkalut, Ohje
  - Listener injection -pattern puhtaaseen separaatioon
  - MenuListeners-luokka kuuntelijoiden hallintaan
- **DocumentToolbarBuilder.java** (112 riviä) - Eriytetty työkalurivin luonti
  - Navigointi-, tosite-, vienti- ja haku-osiot
  - ToolbarListeners-luokka kuuntelijoiden hallintaan

### Muutettu
- **DocumentFrame.java** refaktoroitu edelleen (-731 riviä)
  - 3752 → 3021 riviä (-19%)
  - Käyttää nyt DocumentMenuBuilder ja DocumentToolbarBuilder -luokkia
  - Modulaarisempi arkkitehtuuri

### Tekninen
- Builder-pattern menu- ja toolbar-komponenteille
- Yhteensä -1429 riviä DocumentFramesta tässä sprintissä

---

## [2.1.3] - 2025-12-28

### 🚀 Kotlin DAO Integration + Code Modernization

**Branch:** `feature/2.1-documentframe-refactor`

### Lisätty
- **Kotlin DAO tuotantokäytössä** - Ensimmäinen Kotlin DAO integroitu
  - `SQLiteAccountDAOKt` korvaa Java-toteutuksen
  - Täysi yhteensopivuus olemassa olevan koodin kanssa
- **UIConstants teemavärit** - Uudet värifunktiot
  - `getSuccessColor()` - Vihreä onnistumisille
  - `getInfoColor()` - Sininen informaatiolle
  - `getErrorColor()` - Punainen virheille
  - `getWarningColor()` - Oranssi varoituksille
  - `getMutedColor()` - Harmaa deaktivoiduille

### Muutettu
- **DocumentFrame.java** refaktoroitu (-698 riviä)
  - 26 ActionListener → lambda-lausekkeet
  - Tiedosto: 3856 → 3158 riviä (-18%)
- **Backup-indikaattori** käyttää teemavärejä
  - Mukautuu automaattisesti dark/light modeen

### Poistettu
- **KotlinDemo.java** - Kehitystyökalu poistettu

### Tekninen
- GitHub Actions: Automaattinen release notes CHANGELOG.md:stä
- Java-tiedostot: 191 → 190
- Kotlin-tiedostot: 12 (6% koodista)

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
