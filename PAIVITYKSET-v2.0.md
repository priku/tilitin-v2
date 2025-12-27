# Tilitin 2.0 - Versiopäivitykset

## 📅 Päivitetty: 28.12.2025

### Yhteenveto

Päivitetty kaikki dokumentaatio ja build-skriptit vastaamaan Tilitin 2.0:n todellisia versiovaatimuksia.

---

## ✅ Päivitetyt Versiot

### Java-versio: 21 → 25

**Muutetut tiedostot:**
- ✅ `.github/workflows/maven.yml` - GitHub Actions CI/CD
- ✅ `CHANGELOG.md` - Muutosloki
- ✅ `CONTRIBUTING.md` - Kehittäjäohjeet
- ✅ `PROJEKTISUUNNITELMA.md` - Projektisuunnitelma
- ✅ `TESTAUS.md` - Testausohje
- ✅ `TESTAA-NYT.md` - Pikatestausohje
- ✅ `build-windows.bat` - Windows build-skripti
- ✅ `build-windows-installer.bat` - MSI build-skripti
- ✅ `README.md` - Käyttäjäohjeet (jo päivitetty aiemmin)
- ✅ `pom.xml` - Maven (oli jo 25, ei muutettu)

**Perustelut:**
- Projekti käyttää Java 25.0.1 (Temurin LTS)
- pom.xml määrittelee `maven.compiler.release>25`
- GitHub Actions tarvitsee JDK 25:n buildiin

---

### Maven-versio: 3.x → 3.9.12+

**Muutetut tiedostot:**
- ✅ `CHANGELOG.md` - Tarkka versio 3.9.12+
- ✅ `TESTAUS.md` - Maven 3.9.12+

**Perustelut:**
- Projekti käyttää Maven 3.9.12+ -versiota
- Maven 3.x oli liian epämääräinen

---

### GitHub Releases URL: jkseppan/tilitin → priku/tilitin-modernized

**Muutetut tiedostot:**
- ✅ `README.md` - [releases]-linkki

**Muutos:**
```markdown
# Ennen:
[releases]: https://github.com/jkseppan/tilitin/releases

# Jälkeen:
[releases]: https://github.com/priku/tilitin-modernized/releases
```

**Perustelut:**
- Tilitin 2.0 julkaistaan priku/tilitin-modernized -repositoryssa
- Käyttäjät ohjataan oikeaan paikkaan lataamaan v2.0.0

---

## 📋 Kaikki Muutetut Tiedostot

| Tiedosto | Muutos | Syy |
|----------|--------|-----|
| `.github/workflows/maven.yml` | JDK 21 → 25 | GitHub Actions CI/CD |
| `CHANGELOG.md` | Java 21 → 25, Maven 3.13.0 → 3.9.12+ | Dokumentaatio |
| `CONTRIBUTING.md` | JDK 21+ → 25+ | Kehittäjäohjeet |
| `PROJEKTISUUNNITELMA.md` | Java 21 → 25 | Projektisuunnitelma |
| `README.md` | Java 21 → 25, releases-URL | Käyttäjäohjeet |
| `TESTAUS.md` | JDK 21+ → 25+, Maven 3.x → 3.9.12+ | Testausohje |
| `TESTAA-NYT.md` | Java 21+ → 25+ | Pikatestaus |
| `build-windows.bat` | Java 21 → 25 | Build-skripti |
| `build-windows-installer.bat` | Java 21 → 25 | MSI build-skripti |

**Yhteensä:** 9 tiedostoa päivitetty

---

## 🔍 Tarkistus

### Java-versio

```bash
# Tarkista pom.xml
grep "maven.compiler.release" pom.xml
# Pitäisi näyttää: <maven.compiler.release>25</maven.compiler.release>

# Tarkista käytössä oleva Java
java -version
# Pitäisi näyttää: openjdk version "25.0.1"
```

### Maven-versio

```bash
mvn --version
# Pitäisi näyttää: Apache Maven 3.9.12 tai uudempi
```

### GitHub Releases URL

```bash
# Tarkista README.md
grep "\[releases\]:" README.md
# Pitäisi näyttää: https://github.com/priku/tilitin-modernized/releases
```

---

## 📚 Muita Huomioita

### Ei Muutettu (jo oikein):

- ✅ `pom.xml` - oli jo Java 25
- ✅ FlatLaf 3.5.2 - kirjaston versio on oikein
- ✅ iText PDF 5.5.13.4 - kirjaston versio on oikein
- ✅ SQLite JDBC 3.46.0.1 - kirjaston versio on oikein
- ✅ MySQL Connector 9.0.0 - kirjaston versio on oikein
- ✅ PostgreSQL 42.7.3 - kirjaston versio on oikein
- ✅ SLF4J 2.0.13 - kirjaston versio on oikein

---

## 🚀 Seuraavat Askeleet

1. **Tarkista muutokset:**
   ```bash
   git diff
   ```

2. **Commitoi päivitykset:**
   ```bash
   git add .
   git commit -m "docs: Päivitä Java 25 ja Maven 3.9.12+ versiotiedot"
   ```

3. **Pushaa muutokset:**
   ```bash
   git push
   ```

4. **Jatka GitHub Release -julkaisuun:**
   - Seuraa ohjeita: [GITHUB-RELEASE-PUUTTUU.md](GITHUB-RELEASE-PUUTTUU.md)

---

## 🎯 Yhteenveto

**Syy päivityksille:**
- Käyttäjä päivitti Java 25:een
- Dokumentaatio oli epäjohdonmukainen (Java 21 vs. 25)
- GitHub Releases -URL osoitti väärään repositoryyn

**Tulos:**
- ✅ Kaikki dokumentaatio johdonmukaista
- ✅ Oikeat versiovaatimukset (Java 25+, Maven 3.9.12+)
- ✅ Oikea releases-URL (priku/tilitin-modernized)
- ✅ GitHub Actions käyttää JDK 25:ta

**Vaikutus:**
- Käyttäjät saavat oikeat asennusohjeet
- Kehittäjät tietävät oikeat versiovaatimukset
- CI/CD buildaa oikealla Java-versiolla
- README.md ohjaa oikeaan release-sivulle

---

**Dokumentti luotu:** 28.12.2025
**Status:** ✅ Valmis
**Commitoitu:** Odottaa (git commit)
