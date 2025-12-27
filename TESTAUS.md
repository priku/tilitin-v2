# Tilitin 1.6.0 - Testausohje

## 🎯 Nopea Testaus (5 minuuttia)

### Vaihe 1: Buildaa projekti

```bash
cd c:\Github\Prod\tilitin
mvn clean package
```

**Odotettu tulos:**
- ✅ BUILD SUCCESS
- ✅ `target\tilitin-1.6.0.jar` luotu

### Vaihe 2: Käynnistä sovellus

```bash
java -jar target\tilitin-1.6.0.jar
```

**Mitä pitäisi tapahtua:**
1. Sovellus käynnistyy
2. Näet **modernin FlatLaf Light -teeman**
3. UI näyttää paljon paremmalta kuin vanha versio!

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

**Uusi versio (1.6.0):**
```bash
git checkout feature/windows-modernization
mvn clean package
java -jar target\tilitin-1.6.0.jar
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

## 📊 Testausraportti (Täytä)

```
================================
TILITIN 1.6.0 TESTAUSRAPORTTI
================================

Testaaja: _______________
Päivämäärä: _______________
Ympäristö: Windows ___ / Mac / Linux

BUILDAUS:
[ ] Maven build onnistui
[ ] JAR-tiedosto luotu (target\tilitin-1.6.0.jar)

KÄYNNISTYS:
[ ] Sovellus käynnistyy
[ ] FlatLaf Light-teema näkyy
[ ] Ei virheviestejä konsolissa

UI-KOMPONENTIT:
[ ] Pääikkuna (DocumentFrame) - Moderni ulkoasu
[ ] Painikkeet - Pyöristetyt kulmat
[ ] Tekstikentät - Pyöristetyt kulmat
[ ] Taulukot - Selkeät viivat
[ ] Dialogit - Modernit

TOIMINNALLISUUS:
[ ] Tietokantayhteys toimii
[ ] Tositteiden luonti/muokkaus toimii
[ ] Tilikartta avautuu
[ ] Raportit generoidaan
[ ] PDF-tulostus toimii

TEEMAN VAIHTO:
[ ] FlatLaf Light toimii (oletus)
[ ] FlatLaf Dark toimii (ui.theme=dark)
[ ] Teeman vaihto toimii lennossa (uudelleenkäynnistys)

VIRHEENKÄSITTELY:
[ ] Fallback toimii (väärä teema → perinteinen teema)
[ ] Sovellus ei kaadu

VISUAALINEN LAATU:
Asteikko: 1 (Huono) - 5 (Erinomainen)

Yleisilme: [ ] 1  [ ] 2  [ ] 3  [ ] 4  [ ] 5
Värit: [ ] 1  [ ] 2  [ ] 3  [ ] 4  [ ] 5
Luettavuus: [ ] 1  [ ] 2  [ ] 3  [ ] 4  [ ] 5
Moderniteetti: [ ] 1  [ ] 2  [ ] 3  [ ] 4  [ ] 5

ONGELMAT / BUGIT:
________________________________________________
________________________________________________
________________________________________________
________________________________________________

YHTEENVETO:
[ ] ✅ HYVÄKSYTTY - Valmis seuraavaan vaiheeseen
[ ] ⚠️ EHDOLLINEN - Pieniä korjauksia tarvitaan
[ ] ❌ HYLÄTTY - Merkittäviä ongelmia

LISÄKOMMENTIT:
________________________________________________
________________________________________________
________________________________________________
```

---

## 🐛 Yleiset Ongelmat ja Ratkaisut

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
