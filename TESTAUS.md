# Tilitin 2.0.0 - Testausohje

## 🎯 Nopea Testaus (5 minuuttia)

### Vaihe 1: Buildaa projekti

```bash
cd c:\Github\Prod\tilitin
mvn clean package
```

**Odotettu tulos:**

- ✅ BUILD SUCCESS
- ✅ `target\tilitin-2.1.1.jar` luotu
- ✅ Kotlin 2.3.0 + Java 25 kääntyy onnistuneesti

### Vaihe 2: Käynnistä sovellus

```bash
java -jar target\tilitin-2.0.0.jar
```

**Mitä pitäisi tapahtua:**
1. Sovellus käynnistyy
2. Näet **modernin FlatLaf Light -teeman**
3. Ikkunan otsikossa lukee "Tilitin 2.0"

### Vaihe 3: Tarkista FlatLaf-teema

**Tarkista nämä:**
- [ ] **Pyöristetyt kulmat** painikkeissa ja tekstikentissä
- [ ] **Modernimpi värimaailma** (vaaleansininen/harmaa)
- [ ] **Selkeämmät taulukot** paremmilla viivoilla
- [ ] **Yhtenäinen ulkoasu** koko sovelluksessa

---

## 🔍 Yksityiskohtainen Testaus

### A. Perustoiminnot

#### 1. Tietokantayhteys
```
1. Käynnistä Tilitin
2. Valitse/Luo tietokanta (SQLite suositeltu testaukseen)
3. Varmista että yhteys toimii
```

#### 2. Pääikkuna
```
✓ Tarkista että tositelista näkyy
✓ Toolbar-painikkeet näkyvät ja toimivat
✓ Taulukko on selkeä ja luettava
```

#### 3. Tositteiden Hallinta
```
1. Klikkaa "Uusi tosite"
2. Täytä tiedot
3. Lisää kirjauksia
4. Tallenna
✓ Varmista että dialogi näyttää modernilta
✓ Kentät ovat pyöristettyjä
```

#### 4. Tilikartta
```
1. Avaa tilikartta (Chart of Accounts)
2. Tarkista ulkoasu
✓ Puurakenne näkyy selkeästi
✓ Värit toimivat
```

#### 5. Raportit
```
1. Luo testiraportti (esim. Pääkirja)
2. Tarkista esikatselu
3. Generoi PDF
✓ Raportti näyttää hyvältä
✓ PDF luodaan onnistuneesti
```

---

### B. Teeman Testaus

#### Vaalea Teema (Oletus)

Sovelluksen pitäisi käynnistyä vaalealla teemalla automaattisesti.

**Tarkista:**
- Tausta: Vaalea harmaa/valkoinen
- Teksti: Tumma, helppolukuinen
- Aksentit: Sininen
- Painikkeet: Selkeät, pyöristetyt

#### Tumma Teema

1. Sulje sovellus
2. Avaa asetustiedosto:
   ```
   Windows: %APPDATA%\Tilitin\asetukset.properties
   Mac: ~/Library/Application Support/Tilitin/asetukset.properties
   Linux: ~/.config/Tilitin/asetukset.properties
   ```
3. Lisää rivi:
   ```properties
   ui.theme=dark
   ```
4. Tallenna ja käynnistä Tilitin uudelleen

**Tarkista:**
- Tausta: Tumma (musta/tummanharmaa)
- Teksti: Vaalea, luettava
- Aksentit: Sininen/keltainen
- Ei silmille sattuvia kontrasteja

#### Takaisin Vaaleaan

Muuta `asetukset.properties`:
```properties
ui.theme=light
```

---

### C. Visuaalinen Vertailu

#### Vertaa Vanhaan Versioon

**Vanha versio (1.5.0):**
```bash
git checkout master
mvn clean package
java -jar target\tilitin-1.5.0-jkseppan.1.jar
```
→ Ota kuvakaappaus

**Uusi versio (2.0.0):**
```bash
git checkout feature/windows-modernization
mvn clean package
java -jar target\tilitin-2.0.0.jar
```
→ Ota kuvakaappaus

**Vertaile:**
- Painikkeet: Litteät vs. pyöristetyt
- Värit: Vanhat vs. modernit
- Taulukot: Perus vs. FlatLaf
- Yleisilme: Perinteinen vs. moderni

---

### D. Virhetilanteet

#### Testi 1: FlatLaf Epäonnistuu

1. Avaa `asetukset.properties`
2. Aseta: `ui.theme=invalid`
3. Käynnistä Tilitin

**Odotettu tulos:**
- ⚠️ Konsolissa: "FlatLaf-teeman asetus epäonnistui, käytetään oletusta"
- ✅ Sovellus käynnistyy silti perinteisellä teemalla
- ✅ Ei kaatumista!

#### Testi 2: Puuttuvat Riippuvuudet

(Ei tarvitse testata, Maven hoitaa tämän)

---

## 📊 Testausraportti

```
================================
TILITIN 2.0.0 TESTAUSRAPORTTI
================================

Testaaja: _______________
Päivämäärä: _______________
Ympäristö: Windows 11 / JDK 25+ / Maven 3.9.12+

BUILDAUS:
[ ] Maven build onnistui (BUILD SUCCESS)
[ ] JAR-tiedosto luotu (target\tilitin-2.0.0.jar, ~24 MB)

KÄYNNISTYS:
[ ] Sovellus käynnistyy
[ ] FlatLaf Light-teema näkyy
[ ] Ikkunan otsikko: "Tilitin 2.0"

UI-KOMPONENTIT:
[ ] Pääikkuna (DocumentFrame) - Moderni ulkoasu
[ ] Painikkeet - Pyöristetyt kulmat
[ ] Tekstikentät - Pyöristetyt kulmat
[ ] Taulukot - Selkeät viivat
[ ] Dialogit - Modernit

TOIMINNALLISUUS:
[ ] Tietokantayhteys toimii (SQLite)
[ ] Tositteiden luonti/muokkaus toimii
[ ] Tilin valinta toimii
[ ] Vientien lisääminen toimii (Debet/Kredit)
[ ] Saldolaskenta toimii
[ ] Tilikartta avautuu
[ ] Raportit generoidaan
[ ] PDF-tulostus toimii

TEEMAN VAIHTO:
[ ] FlatLaf Light toimii (oletus)
[ ] FlatLaf Dark toimii (ui.theme=dark)

YHTEENSOPIVUUS:
[ ] Vanhat tietokannat toimivat
[ ] Asetukset säilyvät (%APPDATA%\Tilitin)

WINDOWS-ASENNUS:
[ ] Inno Setup -asennusohjelma toimii
[ ] MSI-asennusohjelma toimii
[ ] Pikakuvakkeet luodaan
[ ] Ohjelma käynnistyy asennuksen jälkeen

YHTEENVETO:
[ ] ✅ HYVÄKSYTTY
[ ] ❌ HYLÄTTY (syy: _______________)

KOMMENTIT:
_______________________________________________
```

---

## � Windows Natiivi Build -testaus (Sprint 1.2)

```
================================
WINDOWS .EXE BUILD TESTAUSRAPORTTI
================================

Testaaja: Käyttäjä + GitHub Copilot
Päivämäärä: 27.12.2025
Ympäristö: Windows 11 / JDK 25 (Temurin) / Maven 3.9.12 / jPackage

BUILD-SKRIPTI:
[x] build-windows.bat suoritettu
[x] Maven build onnistui (BUILD SUCCESS)
[x] jPackage paketointi onnistui

KORJAUKSET BUILDIN AIKANA:
- Poistettu --win-menu, --win-shortcut, --win-console parametrit
  (Nämä eivät toimi app-image -tyypillä, vain MSI:llä)

LUODUT TIEDOSTOT:
[x] dist\windows\Tilitin\Tilitin.exe (0.58 MB)
[x] dist\windows\Tilitin\app\ (sovelluksen JAR)
[x] dist\windows\Tilitin\runtime\ (sisäänrakennettu JRE)
[x] Kokonaiskoko: 148.6 MB

TOIMINNALLISUUS:
[x] Tilitin.exe käynnistyy
[x] FlatLaf-teema näkyy oikein
[x] Aiemmin luotu tietokanta/tosite näkyy
[x] Ei native access -varoituksia (--enable-native-access sisäänrakennettu)
[x] Ei vaadi erillistä Java-asennusta

YHTEENVETO:
[x] ✅ HYVÄKSYTTY - Windows natiivi sovellus toimii

HUOMIOT:
- Sovellus sisältää oman JRE:n → käyttäjän ei tarvitse asentaa Javaa
- Koko 148.6 MB on normaali jPackage-sovellukselle
- Valmis MSI-asennusohjelman rakentamiseen
```

---

## �🐛 Yleiset Ongelmat ja Ratkaisut

### Ongelma: "Sovellus ei käynnisty"

**Ratkaisu:**
1. Tarkista Java-versio: `java -version` (pitäisi olla 21+)
2. Tarkista että build onnistui: `mvn clean package`
3. Katso virheviestit konsolista

### Ongelma: "Näyttää edelleen vanhalta"

**Syyt:**
1. Käytät väärää JAR-tiedostoa
   - Varmista: `target\tilitin-1.6.0.jar`
2. FlatLaf ei latautunut
   - Katso konsolista virheviestejä
3. Välimuistiongelma
   - Tyhjennä ja buildaa uudelleen: `mvn clean package`

**Ratkaisu:**
```bash
# Tarkista riippuvuudet
mvn dependency:tree | findstr flatlaf

# Pitäisi näkyä:
# [INFO] +- com.formdev:flatlaf:jar:3.5.2:compile
# [INFO] +- com.formdev:flatlaf-extras:jar:3.5.2:compile
# [INFO] +- com.formdev:flatlaf-intellij-themes:jar:3.5.2:compile
```

### Ongelma: "Jotkut dialogit näyttävät oudoilta"

**Ratkaisu:**
- Tietyt custom-komponentit saattavat tarvita lisäsäätöä
- Raportoi ongelma testausraportissa
- Jatkokehityksessä korjataan

### Ongelma: "Tumma teema ei toimi"

**Ratkaisu:**
1. Tarkista `asetukset.properties` sijainti oikein
2. Varmista että rivi on: `ui.theme=dark` (ei välilyöntejä)
3. Käynnistä sovellus uudelleen
4. Jos ei toimi, tarkista konsolista virheviestit

---

## ✅ Seuraavat Askeleet Testauksen Jälkeen

### Jos Testaus Onnistui:

1. **Täytä testausraportti** (yllä)
2. **Ota kuvakaappauksia** (ennen/jälkeen vertailuun)
3. **Ilmoita OK** → Jatketaan Sprint 1.3 (UI-parannukset)

### Jos Ongelmia:

1. **Dokumentoi ongelmat** tarkkaan
2. **Ota kuvakaappauksia** ongelmista
3. **Kerää virheviestit** konsolista
4. **Raportoi** → Korjataan ennen jatkamista

---

## 🚀 Mikä Seuraavaksi?

Kun FlatLaf-perusteema on testattu ja toimii:

**Sprint 1.3: UI-Parannukset**
- Lisää teeman valinta Settings-dialogiin
- Paranna ikoneita (FlatLaf SVG-ikonit)
- Hienosäädä värejä ja välejä
- Lisää keyboard shortcuts -parannus

**Sprint 2: jPackage Windows Build**
- Testaa `build-windows.bat`
- Luo natiivi .exe-sovellus
- Testaa MSI-asennusohjelma

---

**Onnea testaukseen!** 🎯

Jos löydät ongelmia, raportoi ne ja korjataan yhdessä.

---

## 🚀 Tilitin 2.0 -päivitys (27.12.2025)

```
================================
TILITIN 2.0 PÄIVITYSRAPORTTI
================================

Päivämäärä: 27.12.2025
Testaaja: Käyttäjä + GitHub Copilot

MUUTOKSET:
- Versio: 1.6.0 → 2.0.0 (major version bump)
- APP_NAME: "Tilitin" → "Tilitin 2.0"
- Lisätty APP_DATA_NAME = "Tilitin" (yhteensopivuus vanhan version kanssa)

PÄIVITETYT TIEDOSTOT:
[x] pom.xml - versio 2.0.0
[x] Kirjanpito.java - APP_NAME, APP_DATA_NAME
[x] build-windows.bat - versiot ja nimi
[x] build-windows-installer.bat - versiot ja nimi
[x] README.md - otsikko ja versiotiedot
[x] PROJEKTISUUNNITELMA.md - sprint-tilat

UUDET TIEDOSTONIMET:
- JAR: tilitin-1.6.0.jar → tilitin-2.0.0.jar ✅
- Windows-kansio: dist\windows\Tilitin → dist\windows\Tilitin 2.0 ✅
- EXE: Tilitin.exe → Tilitin 2.0.exe ✅
- MSI (tuleva): Tilitin-1.6.0.msi → Tilitin 2.0-2.0.0.msi

YHTEENSOPIVUUS:
[x] Asetuskansio säilyy: %APPDATA%\Tilitin\ (APP_DATA_NAME)
[x] Vanhat tietokannat toimivat
[x] Rinnakkainen asennus mahdollinen (eri Start Menu -nimi)

TESTATTU:
[x] JAR-tiedosto luotu (23.81 MB)
[x] Windows .exe rakennettu (Tilitin 2.0.exe, 0.58 MB)
[x] Sovellus käynnistyy
[x] Ikkunan otsikko: "Tilitin 2.0" ✅
[x] Tietokanta latautuu automaattisesti (sama kuin vanhassa)

YHTEENVETO:
[x] ✅ HYVÄKSYTTY - Tilitin 2.0 toimii oikein

HUOMIOT:
- APP_DATA_NAME säilyttää yhteensopivuuden vanhan version kanssa
- Käyttäjät voivat pitää molemmat versiot asennettuna rinnakkain
- Sama tietokanta toimii molemmissa versioissa
```

---

## 📦 MSI-asennusohjelman testaus (27.12.2025)

```
================================
MSI INSTALLER TESTAUSRAPORTTI
================================

Päivämäärä: 27.12.2025
Testaaja: Käyttäjä + GitHub Copilot

BUILD:
[x] build-windows-installer.bat suoritettu
[x] Maven build onnistui (BUILD SUCCESS)
[x] jPackage MSI-paketointi onnistui
[x] WiX Toolset 3.14 käytössä

LUODUT TIEDOSTOT:
[x] dist\installer\Tilitin 2.0-2.0.0.msi (71.8 MB)
[x] MSI sisältää embedded JRE
[x] MSI sisältää GPL-lisenssin (COPYING)

MSI OMINAISUUDET:
[x] Asennuswizard näkyy (perinteinen Windows Installer)
[x] GPL-lisenssi näytetään asennuksen aikana
[x] Käyttäjä voi valita asennushakemiston (--win-dir-chooser)
[x] Luo pikakuvakkeen Start Menuun (--win-menu)
[x] Luo työpöydän pikakuvakkeen (--win-shortcut)
[x] Per-user asennus (--win-per-user-install)
[x] Upgrade UUID asetettu (päivitysten toimivuus)

TESTAUS:
[x] MSI-tiedosto tuplaklikkaus toimii
[x] Asennuswizard käynnistyy
[x] Asennus onnistuu ilman virheitä
[x] Start Menu -pikakuvake luotu: "Tilitin 2.0"
[x] Sovellus käynnistyy asennuksen jälkeen
[x] FlatLaf-teema näkyy oikein
[x] Ei native access -varoituksia
[x] Tietokanta toimii (sama kuin aiemmin)

KÄYTTÖLIITTYMÄ:
- Asennuswizard: Perinteinen Windows Installer -tyyli
- Värimaailma: Harmaa/valkoinen (vanha tyyli)
- Toiminnallisuus: Ammattimainen, toimiva
- Moderniteetti: ⭐⭐ (perinteinen, mutta tuttu käyttäjille)

MSI MODERNISOINTI:
Käyttäjä kysyi: "Onko mitään muuta tapaa modernisoida msi paketin asennus prosessia?"

ANALYYSI:
- jPackage luo toimivan MSI:n, mutta wizardi näyttää vanhalta
- jPackage ei tarjoa visuaalisen kustomoinnin parametreja
- Modernisointivaihtoehdot dokumentoitu: MSI-MODERNISOINTI.md

VAIHTOEHDOT:
1. Advanced Installer - Nopein tapa, maksullinen ($499/v)
2. WiX Custom UI - Ilmainen, vaatii XML-osaamista
3. WiX Bootstrapper + WPF - Täysi kontrolli, vaatii C#-osaamista
4. Pidä jPackage sellaisenaan - Wizardi tuttu käyttäjille

PÄÄTÖS:
[x] Dokumentoitu MSI-MODERNISOINTI.md
[x] Suositus: Pidä jPackage MSI (keskity sovelluksen UI:hin)
[ ] Vaihtoehtoisesti: Advanced Installer (jos halutaan moderni wizard)

YHTEENVETO:
[x] ✅ HYVÄKSYTTY - MSI-asennusohjelma toimii ammattimaisesti

HUOMIOT:
- MSI:n perinteinen ulkoasu on tuttu Windows-käyttäjille
- Wizardi näkyy vain asennuksen aikana (kerran)
- Sovelluksen FlatLaf-teema näkyy joka päivä (tärkeämpi)
- Modernisointivaihtoehdot saatavilla tarvittaessa
```

---

## 🎨 Inno Setup -asennusohjelman testaus (27.12.2025)

```
================================
INNO SETUP INSTALLER TESTAUSRAPORTTI
================================

Päivämäärä: 27.12.2025
Testaaja: Käyttäjä + GitHub Copilot

PÄÄTÖS:
Valittiin Inno Setup modernina asennusohjelmana jPackage MSI:n sijaan.

TOTEUTUS:
[x] Luotu installer/tilitin.iss (Inno Setup -skripti)
[x] Luotu build-inno-installer.bat (Build-skripti)
[x] Inno Setup 6.6.1 asennettu

BUILD:
[x] build-windows.bat suoritettu (luo app-image)
[x] build-inno-installer.bat suoritettu
[x] Kompiloi onnistuneesti ISCC.exe:llä

LUODUT TIEDOSTOT:
[x] dist\installer\Tilitin-2.0.0-setup.exe (~57 MB)
[x] Pienempi kuin MSI (57 MB vs. 71.8 MB)
[x] LZMA2 ultra64 -pakkaus

INNO SETUP OMINAISUUDET:
[x] WizardStyle=modern (moderni ulkoasu)
[x] Suomen- ja englanninkielinen käyttöliittymä
[x] GPL-lisenssi näytetään asennuksen aikana
[x] Käyttäjä voi valita asennushakemiston
[x] Pikakuvake Start Menuun (oletuksena)
[x] Pikakuvake työpöydälle (vapaaehtoinen)
[x] "Käynnistä Tilitin 2.0" -valinta asennuksen jälkeen
[x] Per-user asennus (ei vaadi admin-oikeuksia)

TESTAUS:
[x] Setup.exe tuplaklikkaus toimii
[x] Moderni wizard käynnistyy
[x] Kielivalinta toimii (suomi/englanti)
[x] Asennus onnistuu ilman virheitä
[x] Start Menu -pikakuvake luotu: "Tilitin 2.0"
[x] Työpöydän pikakuvake (jos valittu)
[x] Sovellus käynnistyy asennuksen jälkeen
[x] FlatLaf-teema näkyy oikein
[x] Ei native access -varoituksia
[x] Tietokanta toimii (sama kuin aiemmin)
[x] Uninstaller toimii (ohjelmat-listasta)

KÄYTTÖLIITTYMÄ:
- Asennuswizard: Moderni Inno Setup -tyyli ⭐⭐⭐⭐⭐
- Värimaailma: Vaalea, moderni (parempi kuin MSI)
- Moderniteetti: ⭐⭐⭐⭐ (paljon parempi kuin jPackage MSI)
- Toiminnallisuus: Ammattimainen, toimiva
- Käyttäjäkokemus: Sujuva, tuttu Windows-käyttäjille

VERTAILU MSI vs. Inno Setup:
| Ominaisuus | jPackage MSI | Inno Setup |
|------------|--------------|------------|
| Tiedostokoko | 71.8 MB | 57 MB |
| Ulkoasu | ⭐⭐ Vanha | ⭐⭐⭐⭐ Moderni |
| Kustomointi | ⭐ Ei mahdollista | ⭐⭐⭐⭐ Pascal-skripti |
| Monikielisyys | - Englanti | ⭐ Suomi + Englanti |
| Build-aika | ~30 sek | ~10 sek |

MIKSI INNO SETUP VALITTIIN:
1. Modernimpi ulkoasu (WizardStyle=modern)
2. Helpompi kustomoida kuin WiX
3. Ilmainen (ei kustannuksia)
4. Pienempi tiedostokoko (parempi pakkaus)
5. Suomen kielen tuki
6. Pascal-skriptaus helposti luettavaa

SKRIPTIN RAKENNE (installer/tilitin.iss):
- [Setup] - Perusasetukset (nimi, versio, kuvake)
- [Languages] - Suomi + Englanti
- [CustomMessages] - Käännetyt viestit
- [Tasks] - Pikakuvake-valinnat
- [Files] - Kopioitavat tiedostot (jPackage app-image)
- [Icons] - Start Menu ja työpöydän pikakuvakkeet
- [Run] - "Käynnistä sovellus" -valinta
- [Code] - Pascal-koodi (tulevaa laajennusta varten)

BUILD-PROSESSI:
1. build-windows.bat → dist\windows\Tilitin 2.0\
2. build-inno-installer.bat → dist\installer\Tilitin-2.0.0-setup.exe
3. ISCC.exe kompiloi .iss-skriptin
4. Valmis installer muutamassa sekunnissa

YHTEENVETO:
[x] ✅ HYVÄKSYTTY - Inno Setup on parempi vaihtoehto kuin jPackage MSI

HUOMIOT:
- Inno Setup tarjoaa modernimman käyttökokemuksen
- Pienempi tiedostokoko (20% säästö)
- Helpompi ylläpitää kuin WiX XML
- Suomen kieli arvokasta kotimaiselle ohjelmistolle
- Jatkossa voidaan lisätä lisäominaisuuksia Pascal-skriptauksella
```

---

## 📎 PDF-liitteet -ominaisuuden testaus (v2.2.0+)

### Pika-testaus PDF-liitteille

#### Valmistelu

```bash
# Buildaa ja käynnistä
mvn clean package -DskipTests
mvn exec:java
```

#### Perustestitapaukset

**1. PDF-liitteen lisääminen**

```
✓ Avaa/luo tietokanta
✓ Avaa tai luo tosite (tositeella oltava vähintään yksi vienti)
✓ Vieritä alas "PDF-liitteet" -osioon (ikkunan alaosa)
✓ Klikkaa "Lisää PDF"
✓ Valitse PDF-tiedosto
✓ Tarkista että PDF näkyy listassa
✓ Tarkista että tiedoston koko, sivumäärä ja päivämäärä näkyvät
```

**2. PDF-liitteen vienti**

```
✓ Valitse liite listasta
✓ Klikkaa "Vie tiedostoksi"
✓ Valitse tallennussijainti
✓ Avaa viety tiedosto ja varmista että se on oikea PDF
```

**3. PDF-liitteen poisto**

```
✓ Valitse liite listasta
✓ Klikkaa "Poista"
✓ Vahvista poisto
✓ Tarkista että liite poistui listasta
```

**4. Dokumenttien välillä navigointi**

```
✓ Lisää PDF-liite tositteeseen A
✓ Vaihda tositteeseen B
✓ Tarkista että liitteet-paneeli päivittyy (tyhjä tai B:n liitteet)
✓ Palaa tositteeseen A
✓ Tarkista että A:n liitteet näkyvät taas
```

### Virhetilanteiden testaus

**Testi 5: Iso tiedosto (5-10 MB)**

```
1. Yritä lisätä 5-10 MB:n PDF
2. Odotettu: Varoitusviesti (jatka/peruuta)
3. Tarkista että liite tallentuu kun jatketaan
```

**Testi 6: Liian iso tiedosto (>10 MB)**

```
1. Yritä lisätä >10 MB PDF
2. Odotettu: Virheviesti, liitettä EI tallenneta
```

**Testi 7: Virheellinen PDF**

```
1. Yritä lisätä korruptoitunut/virheellinen PDF
2. Odotettu: Virheviesti "ei kelvollinen PDF"
```

**Testi 8: Tallentamaton tosite**

```
1. Luo uusi tosite (älä tallenna/lisää vientejä)
2. Yritä lisätä PDF
3. Odotettu: Painike ei ole aktiivinen tai varoitus
```

### Automaattiset testit

**Tietokantakerroksen testit:**

```bash
# Windows
run-attachment-test.bat

# Linux/Mac
./run-attachment-test.sh
```

**Odotettu tulos:** ✅ 15/15 testiä läpi

**Testikattavuus:**

- Tietokantamigraatio 14 → 15
- CRUD-operaatiot (SQLite, MySQL, PostgreSQL)
- PDF-validointi
- Sivumäärän laskenta
- Tiedostokoon rajoitukset

### PDF-testausraportti

```
================================
PDF-LIITTEET TESTAUSRAPORTTI
================================

Testaaja: _______________
Päivämäärä: _______________
Versio: 2.2.0+

PERUSTOIMINNOT:
[ ] Liitteet-paneeli näkyy ikkunan alaosassa
[ ] PDF-liitteen lisäys toimii
[ ] Liitetiedot näkyvät oikein (nimi, koko, sivut, pvm)
[ ] PDF-liitteen vienti toimii
[ ] PDF-liitteen poisto toimii
[ ] Poistovahvistus toimii

NAVIGOINTI:
[ ] Liitteet päivittyvät dokumentin vaihtuessa
[ ] Liitteet pysyvät tallessa tietokantaan
[ ] Liitteet latautuvat oikein tietokannan avautuessa

VIRHEENKÄSITTELY:
[ ] Iso tiedosto (5-10 MB) → Varoitus
[ ] Liian iso (>10 MB) → Virhe
[ ] Virheellinen PDF → Virhe
[ ] Tallentamaton tosite → Varoitus/painike ei aktiivinen

AUTOMAATTISET TESTIT:
[ ] AttachmentDAOTest: ___/15 testiä läpi

YHTEENVETO:
[ ] ✅ HYVÄKSYTTY
[ ] ❌ HYLÄTTY (syy: _______________)

KOMMENTIT:
_______________________________________________
```

---

**Onnea testaukseen!** 🎯

Jos löydät ongelmia, raportoi ne ja korjataan yhdessä.
