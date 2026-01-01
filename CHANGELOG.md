# Changelog

Kaikki merkittävät muutokset Tilitin-projektiin dokumentoidaan tähän tiedostoon.

Formaatti perustuu [Keep a Changelog](https://keepachangelog.com/en/1.0.0/) -standardiin.

---

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
