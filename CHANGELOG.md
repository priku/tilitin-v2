# Changelog

Kaikki merkittävät muutokset Tilitin-projektiin dokumentoidaan tähän tiedostoon.

Formaatti perustuu [Keep a Changelog](https://keepachangelog.com/en/1.0.0/) -standardiin.

---

## [2.2.1] - 2026-01-02 (work in progress)

### Tekniset parannukset

- **BaseDialogFX-pohja luotu** ⭐ UUSI
  - Yhtenäinen malli kaikille JavaFX-dialogeille
  - Vähentää toistoa ja parantaa ylläpidettävyyttä
  - OK/Cancel -nappien hallinta
  - Yhtenäinen layout ja tyyli

- **Kotlin-migraatio jatkuu**
  - AccountSelectionDialogFX migroitu Java → Kotlin
    - Käyttää BaseDialogFX-pohjaa
    - Tilinvalintadialogi (F9) nyt Kotlinissa
    - Yhteensopiva Java-koodin kanssa (@JvmStatic)
  - PrintSettingsDialogFX migroitu Java → Kotlin
    - Käyttää BaseDialogFX-pohjaa
    - Tulostusasetukset-dialogi nyt Kotlinissa
    - MainController toimii ilman muutoksia
  - DocumentMenuBuilder migroitu Java → Kotlin ⭐ UUSI
    - Valikkorivin rakentaja nyt Kotlinissa
    - Ensimmäinen DocumentFrame-refaktoroinnin osa
    - DocumentFrame toimii ilman muutoksia
  - Kotlin-prosentti: ~8.2% → ~10.5% (9 dialogia + manager-luokka Kotlinissa)

---

## [2.2.0] - 2026-01-02

### Uudet ominaisuudet

- **Täysin uusi JavaFX-käyttöliittymä**
  - Moderni, responsiivinen design
  - Tumma ja vaalea teema
  - Skaalautuva fonttikoko

- **31 JavaFX-dialogia** (24 Java DialogFX + 7 Kotlin)
  - Kaikki toiminnot siirretty JavaFX:ään
  - Parannettu käytettävyys ja ulkoasu
  - 100% JavaFX - ei Swing-dialogeja jäljellä

### Tekniset parannukset

- **Kotlin-migraatio jatkuu**
  - AboutDialogFX migroitu Java → Kotlin
  - HelpDialogFX migroitu Java → Kotlin
  - PropertiesDialogFX migroitu Java → Kotlin
  - DebugInfoDialogFX migroitu Java → Kotlin
  - KeyboardShortcutsDialogFX migroitu Java → Kotlin
  - CSVImportDialog Kotlinissa
  - ReportDialog Kotlinissa
  - Kotlin-prosentti: ~8.2% (7 dialogia Kotlinissa)

- **Testaus-infrastruktuuri laajennettu**
  - JUnit 5 integroitu
  - TestFX lisätty JavaFX-testaukseen
  - Kattavat DAO-testit:
    - AccountDAOTest (5 testiä)
    - AttachmentDAOTest (6 testiä)
    - DocumentDAOTest (5 testiä)
    - DocumentTypeDAOTest (5 testiä)
    - EntryDAOTest (6 testiä)
    - EntryTemplateDAOTest (5 testiä)
    - PeriodDAOTest (5 testiä)
    - SettingsDAOTest (5 testiä)
  - Model-testit:
    - PropertiesModelTest (7 testiä)
    - DocumentModelTest (5 testiä)
  - Integration-testit:
    - DocumentWorkflowTest
  - Testikattavuus merkittävästi parantunut

## [2.1.1] - 2026-01-02

### Uudet ominaisuudet

- **Täysin uusi JavaFX-käyttöliittymä**
  - Moderni, responsiivinen design
  - Tumma ja vaalea teema
  - Skaalautuva fonttikoko

- **31 JavaFX-dialogia** (29 DialogFX + 2 Kotlin)
  - Kaikki toiminnot siirretty JavaFX:ään
  - Parannettu käytettävyys ja ulkoasu
  - 100% JavaFX - ei Swing-dialogeja jäljellä

### Tekniset parannukset

- **Kotlin-migraatio jatkuu**
  - AboutDialogFX migroitu Java → Kotlin
  - HelpDialogFX migroitu Java → Kotlin
  - PropertiesDialogFX migroitu Java → Kotlin
  - Kotlin-prosentti: ~8.2% (7 dialogia Kotlinissa)

- **Testaus-infrastruktuuri**
  - JUnit 5 integroitu
  - TestFX lisätty JavaFX-testaukseen
  - AccountDAOTest luotu (5 testiä)
  - Testikattavuus aloitettu

- **Koodin laatu**
  - Parannettu null-safety Kotlin-dialogeissa
  - Yhtenäisempi koodikanta
  - Vähemmän boilerplate-koodia

- **Asetukset-valikko**
  - Eriytetty omaksi valikokseen
  - Pikanäppäimet-näkymä
  - Tulostusasetukset
  - Asetusten vienti/tuonti

- **PDF-liitteet** - Liitä tositteisiin PDF-tiedostoja

- **CSV-tuonti** - Tuo tilitapahtumia pankkitiliotteesta (Procountor-yhteensopiva)

- **Tulosteiden esikatselu** - JavaFX PrintPreviewStageFX

### Tekniset parannukset

- **Java 21** vaaditaan
- **Gradle Kotlin DSL** - Moderni buildijärjestelmä
- **Apple Silicon (M1/M2/M3/M4)** natiiivituki
- **Windows installer** - Helppokäyttöinen asennusohjelma (Inno Setup)
- **macOS DMG** - Apple Silicon ja Intel -versiot
- **Linux** - DEB ja RPM -paketit
- **Pikanäppäimet** yhteensopiviksi Windows-standardien kanssa
- Parannettu suorituskyky

### Tuetut alustat

| Käyttöjärjestelmä | Tiedosto |
|-------------------|----------|
| Windows | Tilitin-X.X.X-setup.exe |
| macOS (Apple Silicon) | Tilitin-X.X.X-arm64.dmg |
| macOS (Intel) | Tilitin-X.X.X.dmg |
| Linux (Debian/Ubuntu) | tilitin_X.X.X_amd64.deb |
| Linux (Fedora/Red Hat) | tilitin-X.X.X-1.x86_64.rpm |
| Cross-platform | tilitin-X.X.X-all.jar |

---

## Aikaisemmat versiot

Alkuperäinen Tilitin-ohjelma (versiot 0.x - 1.x) kehitetty Tommi Helinevan toimesta.
Katso [helineva.net/tilitin](https://helineva.net/tilitin/)
