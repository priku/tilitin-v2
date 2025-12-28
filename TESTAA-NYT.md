# 🚀 TESTAA TILITIN 1.6.0 FLATLAF - VAIHEITTAISET OHJEET

## ⚡ NOPEA TESTAUS (5 minuuttia)

### Vaihe 1: Avaa Komentorivi

**Vaihtoehto A: PowerShell (Suositeltu)**
```
1. Paina Windows + X
2. Valitse "Windows PowerShell" tai "Terminal"
```

**Vaihtoehto B: CMD**
```
1. Paina Windows + R
2. Kirjoita: cmd
3. Paina Enter
```

---

### Vaihe 2: Siirry Projektikansioon

```powershell
cd c:\Github\Prod\tilitin
```

Tarkista että olet oikeassa paikassa:
```powershell
dir
```

Pitäisi näkyä: `pom.xml`, `src`, `target`, jne.

---

### Vaihe 3: Buildaa Projekti

```powershell
mvn clean package
```

**Mitä tapahtuu:**
- Maven lataa riippuvuudet (FlatLaf, iText, jne.)
- Kääntää Java-koodin
- Luo JAR-tiedoston: `target\tilitin-1.6.0.jar`

**Kesto:** ~30-60 sekuntia ensimmäisellä kerralla

**Odotettu lopputulos:**
```
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  XX.XXX s
```

**Jos näet virheen:**
- Tarkista että Maven on asennettu: `mvn --version`
- Tarkista että Java 25+ on asennettu: `java -version`
- Jos Maven puuttuu, asenna: https://maven.apache.org/download.cgi

---

### Vaihe 4: Tarkista että JAR Luotiin

```powershell
dir target\*.jar
```

**Pitäisi näkyä:**
```
tilitin-1.6.0.jar          (~ 10-15 MB, kaikki riippuvuudet mukana)
```

---

### Vaihe 5: Käynnistä Sovellus

```powershell
java -jar target\tilitin-1.6.0.jar
```

**Mitä pitäisi tapahtua:**

✅ Sovellus käynnistyy (ikkuna aukeaa)
✅ Näet MODERNIN ulkoasun (ei vanha Windows-tyyli!)
✅ Ei virheviestejä konsolissa

---

## 🎨 TARKISTA FLATLAF-TEEMA

Kun sovellus on auki, tarkista:

### ✅ Modernit Ominaisuudet (FlatLaf Light)

1. **Värimaailma:**
   - [ ] Vaalean sinertävä/harmaa pohja (ei litteä valkoinen)
   - [ ] Sininen aksenttiväri (valitut rivit, fokus)

2. **Painikkeet:**
   - [ ] **PYÖRISTETYT KULMAT** (10px border-radius)
   - [ ] Modernit varjot hoveroitaessa
   - [ ] Selkeät värit

3. **Tekstikentät:**
   - [ ] Pyöristetyt kulmat
   - [ ] Moderni reunus (ei 90-luvun tyyli)

4. **Taulukot:**
   - [ ] Selkeät viivat (horizontal + vertical)
   - [ ] Vaihtelevat rivin värit (stripe)
   - [ ] Moderni header

5. **Scroll barit:**
   - [ ] Näkyvät nuolipainikkeet (ylös/alas)
   - [ ] Modernit

---

## 🌑 TESTAA TUMMA TEEMA (Valinnainen)

### Vaihe 1: Sulje Sovellus

Sulje Tilitin.

### Vaihe 2: Muokkaa Asetustiedostoa

```powershell
notepad %APPDATA%\Tilitin\asetukset.properties
```

**Jos tiedostoa ei ole:**
Luo se ja lisää:
```properties
ui.theme=dark
```

**Jos tiedosto on olemassa:**
Lisää rivi:
```properties
ui.theme=dark
```

Tallenna (Ctrl+S) ja sulje Notepad.

### Vaihe 3: Käynnistä Uudelleen

```powershell
java -jar target\tilitin-1.6.0.jar
```

### ✅ Tarkista Tumma Teema

- [ ] **Tumma tausta** (musta/tummanharmaa)
- [ ] **Vaalea teksti** (helposti luettava)
- [ ] **Hyvä kontrasti** (ei silmille sattuva)
- [ ] **Modernit värit**

### Takaisin Vaaleaan

Muokkaa `asetukset.properties`:
```properties
ui.theme=light
```

---

## 📸 VERTAA VANHAAN VERSIOON

### Vanha Versio (1.5.0)

```powershell
# Vaihda master-haaraan
git checkout master

# Buildaa
mvn clean package

# Käynnistä
java -jar target\tilitin-1.5.0-jkseppan.1.jar
```

**Ota kuvakaappaus** (Win + Shift + S)

### Uusi Versio (1.6.0)

```powershell
# Vaihda takaisin feature-haaraan
git checkout feature/windows-modernization

# Käynnistä
java -jar target\tilitin-1.6.0.jar
```

**Ota kuvakaappaus**

### Vertaa

Laita kuvat vierekkäin:
- **Vanha:** Litteät painikkeet, perinteinen Windows-tyyli
- **Uusi:** Pyöristetyt kulmat, moderni värimaailma

---

## ✅ TESTAA TOIMINNOT

### Perustoiminnot (5 min)

1. **Tietokantayhteys**
   ```
   [ ] Valitse/Luo SQLite-tietokanta
   [ ] Yhteys toimii
   ```

2. **Pääikkuna**
   ```
   [ ] Tositelista näkyy
   [ ] Toolbar toimii
   [ ] Taulukko on selkeä
   ```

3. **Uusi Tosite**
   ```
   [ ] Klikkaa "Uusi tosite"
   [ ] Dialogi aukeaa
   [ ] Kentät näyttävät moderneilta
   [ ] Tallenna toimii
   ```

4. **Tilikartta**
   ```
   [ ] Avaa tilikartta
   [ ] Puurakenne näkyy
   [ ] Värit toimivat
   ```

5. **Raportti**
   ```
   [ ] Luo testiraportti (esim. Pääkirja)
   [ ] Esikatselu toimii
   [ ] PDF-generointi onnistuu
   ```

---

## 🐛 JOS ONGELMIA

### Ongelma 1: "Maven ei löydy"

**Ratkaisu - Vaihtoehto A: Chocolatey (Nopein)** ⭐
```powershell
# Jos sinulla on Chocolatey
choco install maven -y

# Jos ei ole Chocolateyta, asenna ensin:
Set-ExecutionPolicy Bypass -Scope Process -Force; [System.Net.ServicePointManager]::SecurityProtocol = [System.Net.ServicePointManager]::SecurityProtocol -bor 3072; iex ((New-Object System.Net.WebClient).DownloadString('https://community.chocolatey.org/install.ps1'))

# Sitten:
choco install maven -y
```

**Ratkaisu - Vaihtoehto B: Scoop (Kevyempi)** ⭐
```powershell
# Asenna Scoop
Set-ExecutionPolicy RemoteSigned -Scope CurrentUser
irm get.scoop.sh | iex

# Asenna Maven
scoop install maven
```

**Ratkaisu - Vaihtoehto C: WinGet (Microsoftin virallinen)**
```powershell
winget install Maven.Maven
```

**Ratkaisu - Vaihtoehto D: Manuaalinen**
1. Lataa: https://maven.apache.org/download.cgi
2. Pura zip: `C:\Program Files\Apache\maven`
3. Lisää PATH:iin (Win + X → System → Advanced → Environment Variables)
4. Avaa uusi terminal: `mvn --version`

### Ongelma 2: "Java ei löydy"

**Ratkaisu:**
1. Tarkista versio: `java -version`
2. Pitäisi olla 21 tai uudempi
3. Jos ei ole, lataa: https://adoptium.net/

### Ongelma 3: "Build epäonnistui"

**Ratkaisu:**
```powershell
# Tyhjennä cache ja yritä uudelleen
mvn clean install -U
```

### Ongelma 4: "Sovellus näyttää vanhalta"

**Syyt:**
1. FlatLaf ei latautunut
   - Katso konsolista virheviestejä
   - Tarkista riippuvuudet: `mvn dependency:tree | findstr flatlaf`

2. Käytät väärää JAR:ia
   - Varmista: `target\tilitin-1.6.0.jar` (EI 1.5.0!)

**Ratkaisu:**
```powershell
# Buildaa puhtaalta pöydältä
mvn clean package -U

# Varmista versio
java -jar target\tilitin-1.6.0.jar
```

### Ongelma 5: "Tumma teema ei toimi"

**Ratkaisu:**
1. Tarkista tiedoston sijainti: `%APPDATA%\Tilitin\asetukset.properties`
2. Avaa ja varmista rivi: `ui.theme=dark` (ei välilyöntejä!)
3. Tallenna ja käynnistä uudelleen

---

## 📝 TESTAUSRAPORTTI

### Täytä Tämä (Kopioi ja Lähetä Minulle)

```
=================================
TILITIN 1.6.0 TESTAUSRAPORTTI
=================================

Testaaja: [Sinun nimi]
Päivämäärä: [Päivämäärä]
Windows-versio: [10/11]

BUILDAUS:
[ ] mvn clean package onnistui
[ ] tilitin-1.6.0.jar luotu
[ ] Kesto: ___ sekuntia

KÄYNNISTYS:
[ ] Sovellus käynnistyy
[ ] FlatLaf-teema näkyy (ei vanha tyyli)
[ ] Ei virheviestejä

VISUAALISET TARKISTUKSET:
[ ] Painikkeet pyöristettyjä (10px)
[ ] Tekstikentät pyöristettyjä
[ ] Taulukot modernit (viivat näkyvissä)
[ ] Värimaailma moderni (vaaleansininen/harmaa)
[ ] Scroll barit modernit

TOIMINNALLISUUS:
[ ] Tietokantayhteys toimii
[ ] Tositteiden luonti toimii
[ ] Tilikartta toimii
[ ] Raportit toimivat
[ ] PDF-tulostus toimii

TUMMA TEEMA:
[ ] ui.theme=dark toimii
[ ] Tumma tausta näkyy
[ ] Vaalea teksti näkyy
[ ] Hyvä kontrasti

ONGELMAT/BUGIT:
[Kirjoita tähän jos löysit ongelmia]

YHTEENVETO:
[ ] ✅ TOIMII HYVIN - FlatLaf näkyy, kaikki toimii
[ ] ⚠️ PIENIÄ ONGELMIA - Listaa ylhäällä
[ ] ❌ EI TOIMI - Suuria ongelmia

LISÄKOMMENTIT:
[Vapaa sana]
```

---

## 🎯 SEURAAVAT ASKELEET

### Jos Testaus Onnistui (✅):

**Kerro minulle:**
```
✅ FlatLaf toimii!
✅ Painikkeet pyöristettyjä
✅ Moderni ulkoasu näkyy
✅ Kaikki toiminnot toimivat
```

**→ Jatketaan Sprint 1.3: UI-Parannukset**
- Lisätään teeman vaihto Settings-dialogiin
- Parannetaan ikoneita
- Hienosäädetään ulkoasua

### Jos Ongelmia (❌):

**Kerro minulle:**
```
❌ Ongelma: [kuvaus]
📸 Kuvakaappaus: [liitä]
🔴 Virheviesti: [konsolista]
```

**→ Korjataan yhdessä!**

---

## 📞 APUA TARVITTAESSA

**Kysymyksiä?** → Kysy minulta!

**Ei näy modernia?** → Lähetä kuvakaappaus + konsolituloste

**Build epäonnistuu?** → Lähetä virheviesti

---

**ALOITA TÄSTÄ:**

```powershell
cd c:\Github\Prod\tilitin
mvn clean package
java -jar target\tilitin-1.6.0.jar
```

**Onnea testaukseen! 🚀**
