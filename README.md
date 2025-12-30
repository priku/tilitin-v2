# Tilitin 2.2

[![Build Status](https://github.com/priku/tilitin-modernized/workflows/Advanced%20Build%20&%20Release/badge.svg)](https://github.com/priku/tilitin-modernized/actions)
[![Java](https://img.shields.io/badge/Java-25-orange.svg)](https://adoptium.net/)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.3.0-blue.svg)](https://kotlinlang.org/)
[![License](https://img.shields.io/badge/License-GPL%20v3-green.svg)](COPYING)

**Tilitin** on ilmainen, avoimen lähdekoodin kirjanpito-ohjelma suomalaisille pk-yrityksille ja yhdistyksille.

Ohjelman teki alun perin Tommi Helineva ([helineva.net/tilitin](https://helineva.net/tilitin/)). Modernisointia on tehnyt Jouni Seppänen (2024) ja priku (2025).

---

## ✨ Uusimmat ominaisuudet

### v2.2.0 - PDF-liitteiden tuki
- 📎 **PDF-liitteet tositteisiin** - Liitä kuitteja ja laskuja suoraan tositteisiin
- 📄 **Apache PDFBox 3.0.3** - Moderni PDF-käsittelykirjasto
- 🗄️ **Automaattinen tietokantapäivitys** - Migraatio versioon 15

### Aiemmat versiot
- 🦜 **Kotlin 2.3.0** + Java 25 -tuki
- ✨ **FlatLaf-teema** - Vaalea ja tumma teema
- 💾 **Varmuuskopiointijärjestelmä** - AutoBackup, pilvipalvelutuki
- 📊 **CSV-vienti** - Excel-yhteensopiva (UTF-8 BOM)

📋 Katso täydellinen versiohistoria: [CHANGELOG.md](CHANGELOG.md)

---

## 📦 Asennus

### Windows (suositeltu)

1. Lataa **[Tilitin-2.2.0-setup.exe](https://github.com/priku/tilitin-modernized/releases/latest)**
2. Tuplaklikkaa ja seuraa ohjeita
3. Käynnistä Start-valikosta

> 💡 Asennusohjelma sisältää Javan - erillistä asennusta ei tarvita.

### JAR-tiedostolla (kaikki alustat)

1. Asenna [Java 25+](https://adoptium.net/)
2. Lataa `tilitin-2.2.0.jar` [releases-sivulta](https://github.com/priku/tilitin-modernized/releases/latest)
3. Tuplaklikkaa JAR-tiedostoa

---

## 📖 Dokumentaatio

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
