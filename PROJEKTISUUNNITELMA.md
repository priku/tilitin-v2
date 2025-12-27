# PROJEKTISUUNNITELMA: Tilitin Windows Modernisointiprojekti

## Projektin Tavoitteet

1. **Modernisoi Tilitin-sovelluksen käyttöliittymä** Windows-alustalla
2. **Luo natiivi Windows-asennusohjelma** jPackage-työkalulla
3. **Julkaise Microsoft Store -kaupassa**
4. **Paranna käyttäjäkokemusta** modernilla teemalla ja käytettävyydellä

---

## Projektin Yleiskatsaus

| Kenttä | Arvo |
|--------|------|
| **Kohdealusta** | Windows 10/11 (64-bit) |
| **Teknologia** | Java 21 + Swing + FlatLaf |
| **Kokonaisaika** | 6-8 viikkoa |
| **Resurssit** | 1 kehittäjä |
| **Budjetti** | ~10,000-15,000 € (kehitystyö + sertifikaatit) |
| **Julkaisutapa** | Microsoft Store + GitHub Releases |

---

## Projektin Vaiheet ja Aikataulu

### VAIHE 1: Perusta ja Modernisoi UI (2 viikkoa)

#### Sprint 1.1: Projektin Valmistelu (3 päivää)
- [x] Päivitä pom.xml FlatLaf-riippuvuudella ✅ (27.12.2025)
- [x] Luo development-haara Gitissä (`feature/windows-modernization`)
- [x] Dokumentoi nykyinen UI-toiminnallisuus
- [x] Luo testaussuunnitelma ✅ TESTAUS.md luotu

**Deliverables:**
- ✅ FlatLaf lisätty projektiin
- ✅ Git-haara luotu
- ✅ Testaussuunnitelma dokumentoitu

---

#### Sprint 1.2: FlatLaf-integraatio (4 päivää) ✅ VALMIS 27.12.2025

**Tehtävät:**

1. **Lisää FlatLaf-tuki pom.xml** ✅
   ```xml
   <dependency>
       <groupId>com.formdev</groupId>
       <artifactId>flatlaf</artifactId>
       <version>3.5.2</version>
   </dependency>
   <dependency>
       <groupId>com.formdev</groupId>
       <artifactId>flatlaf-extras</artifactId>
       <version>3.5.2</version>
   </dependency>
   <dependency>
       <groupId>com.formdev</groupId>
       <artifactId>flatlaf-intellij-themes</artifactId>
       <version>3.5.2</version>
   </dependency>
   ```

2. **Päivitä Kirjanpito.java**
   - Lisää FlatLaf Look and Feel
   - Lisää teeman vaihto-optio asetuksiin
   - Tuki Light/Dark-teemoille

3. **Testaa kaikki UI-komponentit**

**Deliverables:**
- ✅ FlatLaf käytössä kaikissa ikkunoissa
- ✅ Light/Dark-teema vaihdettavissa
- ✅ Ei visuaalisia bugeja

---

#### Sprint 1.3: UI-parannukset (4 päivää)

**Tehtävät:**
1. Paranna pääikkunaa (DocumentFrame)
2. Paranna dialogeja
3. Lisää Windows-integraatio
4. Paranna käytettävyyttä

**Deliverables:**
- ✅ Modernimpi käyttöliittymä
- ✅ Parempi käytettävyys
- ✅ Windows-integraatio toimii

---

#### Sprint 1.4: Testaus ja bugien korjaus (3 päivää)

**Tehtävät:**
- [x] Manuaalinen testaus kaikissa ikkunoissa ✅
- [x] Testaa SQLite -yhteyksiä ✅
- [ ] Testaa MySQL, PostgreSQL -yhteyksiä
- [x] Testaa raporttien generointi (PDF) ✅
- [ ] Testaa tietojen tuonti/vienti
- [x] Korjaa löydetyt bugit ✅ (build-windows.bat korjattu)

---

### VAIHE 2: jPackage ja Natiivi Paketointi (2 viikkoa)

#### Sprint 2.1: jPackage-konfiguraatio (4 päivää) ✅ VALMIS 27.12.2025

**Tehtävät:**

1. **Luo Windows-ikonit (.ico)** ✅
   - Resoluutiot: 16x16, 32x32, 48x48, 64x64, 128x128, 256x256
   - Tallenna: `src/main/resources/tilitin.ico`

2. **Luo jPackage-build script** ✅

   `build-windows.bat`:
   ```batch
   @echo off
   echo Building Tilitin for Windows...

   REM Build JAR
   call mvn clean package

   REM Create app image with jpackage
   jpackage ^
     --input target ^
     --name Tilitin ^
     --main-jar tilitin-1.6.0.jar ^
     --main-class kirjanpito.ui.Kirjanpito ^
     --type app-image ^
     --app-version 1.6.0 ^
     --vendor "Tilitin Project" ^
     --description "Ilmainen kirjanpito-ohjelma yrityksille ja yhdistyksille" ^
     --icon src/main/resources/tilitin.ico ^
     --win-console ^
     --win-menu ^
     --win-menu-group "Tilitin" ^
     --win-shortcut ^
     --win-dir-chooser ^
     --dest dist/windows

   echo Build complete! Check dist/windows folder.
   pause
   ```

3. **Lisää Maven-profile jPackagea varten**

4. **Testaa jPackage-buildi paikallisesti** ✅

**Deliverables:**
- ✅ tilitin.ico luotu
- ✅ build-windows.bat toimii (korjattu 27.12.2025)
- ✅ Maven-profile lisätty
- ✅ Sovellus käynnistyy .exe-tiedostosta (testattu 27.12.2025)

**Testaustulokset (27.12.2025):**
- Tilitin.exe: 0.58 MB
- Kokonaiskoko (JRE mukana): 148.6 MB
- Native access -varoitukset poistettu (--enable-native-access sisäänrakennettu)

---

#### Sprint 2.2: MSI ja MSIX Installer (4 päivää)

**Tehtävät:**
1. Luo MSI-asennusohjelma
2. Testaa MSI-asennus
3. Luo MSIX-paketti (Store-yhteensopiva)
4. Valmistele AppxManifest.xml

**Deliverables:**
- ⏳ MSI-asennusohjelma toimii
- ⏳ MSIX-paketti luotu
- ⏳ AppxManifest.xml valmis

---

#### Sprint 2.3: Code Signing -valmistelu (3 päivää)

**Tehtävät:**
1. Hanki Code Signing -sertifikaatti (Sectigo/DigiCert, ~$70-200/vuosi)
2. Asenna sertifikaatti
3. Allekirjoita MSIX-paketti
4. Testaa allekirjoitettu paketti

**Deliverables:**
- ✅ Code Signing -sertifikaatti hankittu
- ✅ MSIX allekirjoitettu
- ✅ Allekirjoitus validoitu

---

### VAIHE 3: Windows Store -julkaisu (2 viikkoa)

#### Sprint 3.1: Partner Center Setup (2 päivää)

**Tehtävät:**
1. Rekisteröidy Microsoft Partner Centeriin ($19 yksilö / $99 yritys)
2. Luo sovellus Partner Centerissä
3. Valmistele resurssit (ikonit, kuvakaappaukset)

**Deliverables:**
- ✅ Partner Center -tili luotu
- ✅ Sovellus varattu
- ✅ Kuvamateriaali valmis

---

#### Sprint 3.2: Store Listing -sisältö (3 päivää)

**Tehtävät:**
1. Kirjoita sovelluskuvaus (FI + EN)
2. Lisää kuvakaappaukset
3. Täytä metadata

**Deliverables:**
- ✅ Sovelluskuvaus kirjoitettu
- ✅ Kuvakaappaukset lisätty
- ✅ Metadata täytetty

---

#### Sprint 3.3: Submission ja Testing (4 päivää)

**Tehtävät:**
1. Lataa MSIX-paketti
2. Täytä age ratings ja compliance
3. Lähetä tarkastukseen
4. Korjaa mahdolliset ongelmat
5. Testaa Store-versiota

**Deliverables:**
- ✅ Sovellus lähetetty Storeen
- ✅ Sovellus hyväksytty
- ✅ Tilitin saatavilla Microsoft Storessa

---

### VAIHE 4: Dokumentaatio ja Julkaisu (1 viikko)

#### Sprint 4.1: Dokumentaatio (3 päivää)

**Tehtävät:**
1. Päivitä README.md
2. Luo käyttöohje
3. Luo kehittäjädokumentaatio (BUILDING.md, CONTRIBUTING.md, CHANGELOG.md)
4. Luo video (valinnainen)

**Deliverables:**
- ✅ README päivitetty
- ✅ Käyttöohje luotu
- ✅ Kehittäjädokumentaatio valmis

---

#### Sprint 4.2: GitHub Release (2 päivää)

**Tehtävät:**
1. Päivitä versio 1.6.0
2. Luo GitHub Release
3. Lataa artefaktit
4. Päivitä GitHub Actions

**Deliverables:**
- ✅ GitHub Release julkaistu
- ✅ Kaikki artefaktit ladattavissa
- ✅ CI/CD päivitetty

---

### VAIHE 5: Markkinointi ja Lanseeraus (1 viikko)

#### Sprint 5.1: Lanseeraus (5 päivää)

**Tehtävät:**
1. Tiedote
2. Sosiaalinen media
3. Käyttäjätuki
4. Monitorointi

**Deliverables:**
- ✅ Tiedote julkaistu
- ✅ Sosiaalinen media -postaukset
- ✅ Käyttäjätuki valmis

---

## Deliverables - Lopputuotteet

| # | Deliverable | Kuvaus |
|---|-------------|--------|
| 1 | **Tilitin 1.6.0 (modernized)** | FlatLaf-teemalla varustettu Windows-versio |
| 2 | **Tilitin.exe** | Natiivi Windows-sovellus (jPackage) |
| 3 | **Tilitin-1.6.0.msi** | Windows Installer |
| 4 | **Tilitin.msix** | Microsoft Store -paketti |
| 5 | **Microsoft Store -listaus** | Julkaistu ja hyväksytty sovellus |
| 6 | **Päivitetty dokumentaatio** | README, käyttöohje, build-ohjeet |
| 7 | **GitHub Release v1.6.0** | Julkinen release kaikilla artefakteilla |
| 8 | **Build scripts** | Automatisoidut build- ja paketointi-skriptit |

---

## Budjetti

| Kulu | Kustannus (€) | Huomiot |
|------|---------------|---------|
| **Kehitystyö (6-8 viikkoa)** | 10,000-15,000 | 1 kehittäjä, ~250-300€/päivä |
| **Microsoft Partner Center** | 18 | $19 yksilötili |
| **Code Signing -sertifikaatti** | 70-200 | Vuosimaksu (Sectigo/DigiCert) |
| **Apple Developer (tulevaisuus)** | 0 | Ei vielä |
| **Domain/hosting (valinnainen)** | 0-50 | Jos luot erillisen sivuston |
| **Graafinen suunnittelu** | 0-500 | Jos palkkaat designerin logoihin |
| **YHTEENSÄ** | **10,100-15,800 €** | |

---

## Success Metrics - Menestyksen Mittarit

| Mittari | Tavoite | Miten mitata |
|---------|---------|--------------|
| **Store-lataukset** | 500+ ensimmäisen kuukauden aikana | Partner Center Analytics |
| **GitHub-lataukset** | 200+ ensimmäisen kuukauden aikana | GitHub Insights |
| **Käyttäjäarviot (Store)** | 4+ tähteä keskimäärin | Microsoft Store reviews |
| **Bug-raportit** | <10 kriittistä bugia | GitHub Issues |
| **Dokumentaation kattavuus** | 100% päätoiminnoista | Manual review |

---

## Riskit ja Lieventäminen

| Riski | Todennäköisyys | Vaikutus | Lieventäminen |
|-------|----------------|----------|---------------|
| **FlatLaf yhteensopivuusongelmat** | Keskisuuri | Korkea | Testaa laajasti, pidä vanha teema varalla |
| **jPackage-ongelmat** | Matala | Korkea | Testaa aikaisin, käytä vakaata Java-versiota |
| **Microsoft Store hylkää sovelluksen** | Keskisuuri | Keskisuuri | Lue ohjeet tarkkaan, testaa ennen lähetystä |
| **Code Signing -sertifikaatin viive** | Korkea | Matala | Tilaa ajoissa (1-2 viikkoa ennen tarvetta) |
| **Käyttäjät eivät löydä Storea** | Matala | Matala | Markkinoi GitHub-sivulla ja dokumentaatiossa |
| **Regressioita vanhoissa ominaisuuksissa** | Keskisuuri | Korkea | Laaja testaus, pidä release notes ajan tasalla |

---

## Checklist - Tarkistuslista ennen julkaisua

### Ennen Microsoft Store -lähetystä:
- [ ] Kaikki UI-testit läpäisty
- [ ] MSI-asennusohjelma testattu puhtaalla koneella
- [ ] MSIX-paketti allekirjoitettu
- [ ] Kaikki Store-kuvat ja tekstit valmiina
- [ ] Privacy policy luotu (jos tarvitaan)
- [ ] Code Signing -sertifikaatti voimassa
- [ ] Version numero päivitetty kaikkialle (1.6.0)
- [ ] README päivitetty
- [ ] CHANGELOG luotu

### Ennen GitHub Release:
- [ ] Git tag luotu (v1.6.0)
- [ ] Release notes kirjoitettu
- [ ] Kaikki artefaktit buildettu
- [ ] Lähdekoodit pakattu
- [ ] CI/CD-testit läpäisty
- [ ] Dokumentaatio valmis

---

## Ylläpito-suunnitelma (Release jälkeen)

### Viikko 1-2 jälkeen:
- Seuraa käyttäjäpalautetta
- Korjaa kriittiset bugit nopeasti (hotfix 1.6.1)
- Vastaa Store-arviointihin

### Kuukausi 1-3 jälkeen:
- Analysoi käyttäjädataa
- Suunnittele seuraavat ominaisuudet
- Päivitä dokumentaatio palautteen perusteella

### Vuosittain:
- Uusii Code Signing -sertifikaatti
- Päivitä Java-versio (LTS)
- Päivitä riippuvuudet (FlatLaf, iText, JDBC-ajurit)
- Luo major release (2.0.0) jos isoja muutoksia

---

## Tuki ja Resurssit

### Dokumentaatio:
- [jPackage Guide](https://docs.oracle.com/en/java/javase/21/jpackage/)
- [Microsoft Store Documentation](https://docs.microsoft.com/en-us/windows/apps/publish/)
- [FlatLaf Documentation](https://www.formdev.com/flatlaf/)

### Työkalut:
- **IDE:** IntelliJ IDEA / Eclipse
- **Build:** Maven 3.x
- **Java:** JDK 21 (Temurin)
- **MSIX Packaging Tool:** [Download](https://www.microsoft.com/store/productId/9N5LW3JBCXKF)
- **ImageMagick:** [Download](https://imagemagick.org/script/download.php)

---

**Projektin Status:** Hyväksytty
**Versio:** 1.0
**Viimeisin päivitys:** 2025-12-27
