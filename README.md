# Tilitin

[![Build Status](https://github.com/priku/tilitin-v2/workflows/Advanced%20Build%20&%20Release/badge.svg)](https://github.com/priku/tilitin-v2/actions)
[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://adoptium.net/)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.2.0-blue.svg)](https://kotlinlang.org/)
[![JavaFX](https://img.shields.io/badge/JavaFX-21-green.svg)](https://openjfx.io/)
[![License](https://img.shields.io/badge/License-GPL%20v3-green.svg)](COPYING)

**Tilitin** on ilmainen, avoimen lähdekoodin kirjanpito-ohjelma suomalaisille pk-yrityksille ja yhdistyksille.

Ohjelman teki alun perin Tommi Helineva ([helineva.net/tilitin](https://helineva.net/tilitin/)). Modernisointia on tehnyt Jouni Seppänen (2024) ja priku (2025-2026).

---

## ⚠️ Tietokantayhteensopivuus

### Tärkeää ennen käyttöönottoa

Tilitin 2.1 päivittää tietokannan automaattisesti **versiosta 13 → 14** ensimmäisellä avauksella. Tämä mahdollistaa:

- ✅ **Vapaamuotoiset ALV-prosentit** (esim. 25,5%, 14%, 10%)
- ✅ Tulevat ALV-muutokset ilman ohjelmapäivitystä

### ⚠️ Yhteensopivuusvaroitus

| Suunta | Toimii | Selitys |
|--------|--------|---------|
| Helineva 1.5.0 → Tilitin 2.1 | ✅ Kyllä | Päivittyy automaattisesti |
| Tilitin 2.1 → Helineva 1.5.0 | ❌ Ei | Vanha versio ei ymmärrä uutta skeemaa |

**💾 Ota varmuuskopio ennen ensimmäistä avausta!**

Katso tarkemmat tiedot: [USER-GUIDE.md - Tietokantayhteensopivuus](USER-GUIDE.md#tietokantayhteensopivuus)

---

## ✨ Uudet ominaisuudet v2.1

### 🎨 JavaFX-käyttöliittymä
- **Täysin uusi JavaFX UI** - Moderni, responsiivinen käyttöliittymä
- **31 dialogia** (29 JavaFX + 2 Kotlin) - Kaikki toiminnot siirretty JavaFX:ään
- **100% JavaFX** - Ei Swing-dialogeja jäljellä
- **Tumma ja vaalea teema** - Skaalautuva fonttikoko

### 📊 CSV-tuonti
- **Pankkitiliotteen tuonti** - Tuo kirjaukset CSV-tiedostosta
- **Automaattinen tiliöinti** - Tunnistaa toistuvat maksut

### 📎 PDF-liitteet
- **PDF-liitteet tositteisiin** - Liitä kuitteja ja laskuja
- **Apache PDFBox 3.0.3** - Moderni PDF-käsittely

### ⚙️ Asetukset-valikko
- **Ulkoasuasetukset** - Teema ja fonttikoko esikatselulla
- **Pikanäppäimet** - Kaikki pikanäppäimet kategorioittain
- **Asetusten vienti/tuonti** - Varmuuskopioi asetukset

📋 Katso täydellinen versiohistoria: [CHANGELOG.md](CHANGELOG.md)

---

## 📦 Asennus

### Windows (suositeltu)

1. Lataa **[Tilitin-setup.exe](https://github.com/priku/tilitin-v2/releases/latest)** (releases-sivulta)
2. Tuplaklikkaa ja seuraa ohjeita
3. Käynnistä Start-valikosta

> 💡 Asennusohjelma sisältää Javan - erillistä asennusta ei tarvita.

### macOS

1. Lataa **[Tilitin.dmg](https://github.com/priku/tilitin-v2/releases/latest)** (releases-sivulta)
   - **Apple Silicon (M1/M2/M3/M4):** `Tilitin-X.X.X-arm64.dmg`
   - **Intel:** `Tilitin-X.X.X.dmg`
2. Avaa DMG ja vedä Tilitin Applications-kansioon
3. Jos macOS estää: Järjestelmäasetukset → Tietosuoja ja turvallisuus → Avaa silti

### Linux

Lataa [releases-sivulta](https://github.com/priku/tilitin-v2/releases/latest):
- **Debian/Ubuntu:** `tilitin_X.X.X_amd64.deb`
- **Fedora/Red Hat:** `tilitin-X.X.X-1.x86_64.rpm`

```bash
# Debian/Ubuntu
sudo dpkg -i tilitin_*.deb

# Fedora/Red Hat  
sudo rpm -i tilitin-*.rpm
```

### JAR-tiedostolla (kaikki alustat)

1. Asenna [Java 21+](https://adoptium.net/)
2. Lataa `tilitin-X.X.X-all.jar` [releases-sivulta](https://github.com/priku/tilitin-v2/releases/latest)
3. Tuplaklikkaa JAR-tiedostoa

---

## �️ Kehitysympäristö ja Build-infrastruktuuri

**Kaikki build-työkalut ovat valmiina:**

### Build-skriptit
- `build-windows.bat` - Windows app-image (.exe)
- `build-windows-installer.bat` - Windows MSI-asennusohjelma (WiX)
- `build-inno-installer.bat` - Windows Inno Setup -asennusohjelma (suositus)
- `build-macos.sh` - macOS .app bundle ja DMG
- `create-macos-icon.sh` - macOS .icns-ikonin generointi

### GitHub Actions CI/CD
- `.github/workflows/advanced-build.yml` - Kattava multi-platform build
- `.github/workflows/release.yml` - Automaattinen release tag-julkaisuille

### Tuetut alustat
- **Windows:** EXE (jPackage), MSI (WiX), Inno Setup installer
- **macOS:** .app bundle, DMG-levykuva, ad-hoc code signing
- **Linux:** DEB (Debian/Ubuntu), RPM (Fedora/RHEL)
- **JAR:** Universaali Java 21+ -paketti

---

## �📖 Dokumentaatio

### Käyttäjille
- **[USER-GUIDE.md](USER-GUIDE.md)** - 📖 Kattava käyttäjän opas
- [CHANGELOG.md](CHANGELOG.md) - Versiohistoria

### Kehittäjille
- [BUILDING.md](BUILDING.md) - Build-ohjeet
- [CONTRIBUTING.md](CONTRIBUTING.md) - Kehittäjäohjeet
- [KOTLIN_MIGRATION.md](KOTLIN_MIGRATION.md) - Kotlin-migraatio

---

## 🛠️ Kehittäjille

```bash
# Buildaa JAR
./gradlew jar

# Käännä kaikki
./gradlew build

# Windows-asennusohjelma
build-windows.bat
build-inno-installer.bat
```

Katso [BUILDING.md](BUILDING.md) tarkemmat ohjeet.

---

## 📄 Lisenssi

[GNU General Public License v3](COPYING)

Tämä on vapaa ohjelma. Voit levittää ja muokata sitä GPL v3 -lisenssin ehtojen mukaisesti.

Komponenttien lisenssit: [LISENSSIT.html](LISENSSIT.html)
