# Tilitin 2.0

Tilitin on ilmainen kirjanpito-ohjelma. Ohjelman teki alun perin Tommi Helineva, ja sen dokumentaatio on hänen
sivuillaan: <https://helineva.net/tilitin/>

Ohjelmaan on vuonna 2024 tehnyt pieniä ajanmukaistuksia ja korjauksia Jouni Seppänen.

## 🚀 Versio 2.0.4 - Foundation Sprint

**Uutta versiossa 2.0.4:**

- 🏗️ **UIConstants** - Keskitetyt UI-vakiot (värit, fontit, marginaalit)
- 📦 **BaseDialog** - Yhtenäinen pohjaluokka dialogeille

**Versiossa 2.0.3:**

- 💾 **Varmuuskopiointijärjestelmä** - Per-tietokanta sijainnit
- ☁️ **Pilvipalvelutunnistus** - Google Drive, OneDrive, Dropbox, iCloud
- ⛁ **USB-tunnistus** - Irrotettavat asemat havaitaan automaattisesti
- 🔄 **AutoBackup** - Word-tyylinen automaattinen tallennus (1-60 min)
- 🎛️ **Sijainnit-dialogi** - Hallitse backup-sijainteja per tietokanta

**Versiossa 2.0.2:**

- 🎨 **Modernisoidut ikonit** - Evolution-tyyli, gradientit, euro-symboli
- 🏷️ **Dynaaminen versio** - Ikkunaotsikko näyttää tarkan version

**Versiossa 2.0.1:**

- 🚀 **Splash screen** - Moderni käynnistysnäyttö
- 📂 **Viimeisimmät tietokannat** - Nopea pääsy viimeksi avattuihin tietokantoihin
- ⌨️ **Uudet näppäinoikotiet** - Ctrl+U, Ctrl+D, Ctrl+B, Ctrl+P, Ctrl+E

**Versiossa 2.0.0:**

- ✨ **Moderni FlatLaf-teema** - Aikaisempaa hienompi käyttöliittymä
- 🎨 **Vaalea ja tumma teema** - Valittavissa asetuksista
- 📦 **Windows natiivi-asennusohjelma** - .exe ja .msi -paketit
- 🏪 **Microsoft Store -tuki** (tulossa)
- ⚡ Parannettu käytettävyys ja modernimpi ulkoasu
- 🔄 **Rinnakkainen asennus** - Voit pitää vanhan Tilitin-version asennettuna

**Kehityshaara:** `feature/windows-modernization`
**Tila:** 🟢 Aktiivinen kehitys (Sprint 2.2)

**Dokumentaatio:**

- [PROJEKTISUUNNITELMA.md](PROJEKTISUUNNITELMA.md) - Kehityssuunnitelma ja sprintit
- [TESTAUS.md](TESTAUS.md) - Testausraportit ja ohjeet
- [BUILDING.md](BUILDING.md) - Build-ohjeet
- [CHANGELOG.md](CHANGELOG.md) - Muutosloki
- [CONTRIBUTING.md](CONTRIBUTING.md) - Kehittäjäohjeet
- [IKONI-MODERNISOINTI.md](IKONI-MODERNISOINTI.md) - Ikoni-suunnitelma (v2.1+)
- [GITHUB-RELEASE-PUUTTUU.md](GITHUB-RELEASE-PUUTTUU.md) - Release-julkaisuohjeet

## Asennus

### Windows (suositeltu)

**Moderni Inno Setup -asennusohjelma:**

1. Lataa **Tilitin-2.0.3-setup.exe** GitHubin [releases-osiosta][releases]
2. Tuplaklikkaa asennusohjelmaa ja seuraa ohjeita
3. Käynnistä sovellus Start-valikosta tai työpöydän pikakuvakkeesta

**Huom:** Asennusohjelma sisältää Java-ajoympäristön, joten erillistä Java-asennusta ei tarvita.

**Ominaisuudet:**

- ✨ Moderni FlatLaf-käyttöliittymä
- 🎨 Vaalea ja tumma teema (vaihda: Muokkaa → Ulkoasu...)
- 🌍 Suomen- ja englanninkielinen asennusohjelma
- 📦 Pieni tiedostokoko (~57 MB)
- 🚀 Nopea asennus

### Jar-tiedostolla (kaikki alustat)

1. Asenna Java 25 tai uudempi. Suosittelen OpenJDK-versiota, joka on saatavailla ilmaiseksi ja
   avoimella lisenssillä. Esimerkiksi [Azulin](https://www.azul.com/downloads/#zulu) tai
   [Adoptiumin](https://adoptium.net/) jakelut ovat hyviä. (Oraclen Javaa en suosittele, koska
   vaikka uusia versioita saakin ilmaiseksi, sillä on ollut kummallisia lisenssitemppuja
   menneisyydessä.)
2. Lataa jar-tiedosto GitHubin [releases-osiosta][releases] ja kopioi sopivaan hakemistoon.
3. Tuplaklikkaa jar-tiedostoa. Jos sinulla on Mac, tämä johtaa luultavasti valitukseen
   epäilyttävästä ohjelmistosta. Jos uskallat käyttää sitä, voit ohittaa valituksen klikkaamalla
   jar-tiedostoa hiiren oikealla painikkeella (ohjauslevyllä käytä kahta sormea, tai paina
   ctrl-näppäintä klikatessa) ja valitsemalla "Avaa".

## Buildaaminen

Katso [BUILDING.md](BUILDING.md) tarkemmat ohjeet.

```bash
# JAR-paketin buildaaminen
mvn clean package

# Windows .exe sovellus (app-image)
build-windows.bat

# Windows Inno Setup -asennusohjelma (suositeltu)
build-inno-installer.bat

# Windows MSI-asennusohjelma (vaihtoehtoinen)
build-windows-installer.bat
```

## Muutokset

- Tilittimen pitäisi nyt toimia uusillakin Maceilla, joissa on ARM-siru.
- Korjattu Macilla esiintynyt bugi, jossa tekstikentän ensimmäinen merkki katosi usein.
- Tilikartat ovat nyt jar-paketin sisällä, joten niitä ei tarvitse kopioida erikseen oikeaan
  hakemistoon.
- Uusi tilikartta alv-prosentilla 25,5%
- Pieniä teknisiä ajanmukaistuksia
- Käytetyt kirjastot on päivitetty ja niiden lisenssiehdot luetellaan ikkunassa
  "Tietoja ohjelmasta Tilitin".

## Lisenssi

Tämä on vapaa ohjelma: tätä ohjelmaa saa levittää edelleen ja muuttaa Free Software Foundationin
julkaiseman GNU General Public Licensen (GPL-lisenssi) version 3 ehtojen mukaisesti.

Tätä ohjelmaa levitetään siinä toivossa, että se olisi hyödyllinen mutta ilman mitään takuuta; edes
hiljaista takuuta kaupallisesti hyväksyttävästä laadusta tai soveltuvuudesta tiettyyn tarkoitukseen.
Katso GPL-lisenssistä lisää yksityiskohtia.

Tämän ohjelman mukana pitäisi tulla kopio GPL-lisenssistä tiedostossa COPYING. Jos näin ei ole,
katso <http://www.gnu.org/licenses/>.

Komponenttien lisenssit ja niihin sisältyvät ehdot ovat tiedostossa LISENSSIT.html.
Tilittimen lähdekoodin voit kloonata GitHubista tai ladata [releases-osiosta][releases],
jossa ovat myös kirjastojen lähdekoodit.

[releases]: https://github.com/priku/tilitin-modernized/releases
