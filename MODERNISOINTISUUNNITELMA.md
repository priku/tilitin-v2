# Tilitin 2.0 - Modernisointisuunnitelma

**Luotu:** 27.12.2025
**Versio:** 2.0.0
**Tila:** Aktiivinen suunnittelu

---

## 📋 Sisällysluettelo

1. [Yhteenveto](#yhteenveto)
2. [TOP 5 Prioriteettiparannukset](#top-5-prioriteettiparannukset)
3. [UI/UX Parannukset](#uiux-parannukset)
4. [Koodin Laatu](#koodin-laatu)
5. [Dokumentaatio](#dokumentaatio)
6. [Testaus](#testaus)
7. [Build & Deploy](#build--deploy)
8. [Windows-Spesifit Parannukset](#windows-spesifit-parannukset)
9. [Turvallisuusriskit](#turvallisuusriskit)
10. [Performance-Optimoinnit](#performance-optimoinnit)
11. [Toteutusaikataulu](#toteutusaikataulu)

---

## Yhteenveto

Tilitin 2.0 on juuri saanut **FlatLaf UI-modernisaation** (valmis 27.12.2025). Projekti on hyvässä kunnossa, mutta analyysi (176 Java-tiedostoa) paljasti useita käytännöllisiä parannusmahdollisuuksia jotka parantavat merkittävästi:
- Käyttäjäkokemusta
- Koodin laatua
- Turvallisuutta
- Ylläpidettävyyttä

**Projektikoko:**
- Java-tiedostot: 176 kpl
- Suurin luokka: `DocumentFrame.java` (3544 riviä)
- Riippuvuudet: FlatLaf 3.5.2, iText PDF 5.5.13.4, JDBC (SQLite, MySQL, PostgreSQL)
- Java-versio: 21

---

## TOP 5 Prioriteettiparannukset

### 1. Teeman Vaihto UI:ssa ⭐⭐⭐⭐⭐

**Ongelma:**
Käyttäjän pitää manuaalisesti muokata `%APPDATA%\Tilitin\asetukset.properties`-tiedostoa vaihtaakseen teemaa (light/dark).

**Ratkaisu:**
Lisää Settings-dialogiin dropdown-valikko teeman valintaan.

**Toteutus:**
```java
// Lisää SettingsDialog.java:aan
private JComboBox<String> themeComboBox;

// Constructor
themeComboBox = new JComboBox<>(new String[]{"Vaalea (Light)", "Tumma (Dark)"});
String currentTheme = settings.getString("ui.theme", "light");
themeComboBox.setSelectedIndex("dark".equalsIgnoreCase(currentTheme) ? 1 : 0);

// Save-metodi
String selectedTheme = themeComboBox.getSelectedIndex() == 0 ? "light" : "dark";
settings.set("ui.theme", selectedTheme);

// Live-vaihto (valinnainen)
if (!selectedTheme.equals(oldTheme)) {
    if ("dark".equalsIgnoreCase(selectedTheme)) {
        FlatDarkLaf.setup();
    } else {
        FlatLightLaf.setup();
    }
    SwingUtilities.updateComponentTreeUI(frame);
}
```

**Tiedostot:**
- `src/main/java/kirjanpito/ui/SettingsDialog.java` (lisää teema-valinta)
- `src/main/java/kirjanpito/ui/Kirjanpito.java` (ei muutoksia, toimii jo)

**Aika:** ~30-45 minuuttia
**Vaikutus:** KORKEA - Käyttäjäystävällisyys paranee merkittävästi
**Prioriteetti:** 1

---

### 2. Keyboard Shortcuts ⭐⭐⭐⭐⭐

**Ongelma:**
Puuttuu standardit pikanäppäimet:
- **Ctrl+N** - Uusi tosite
- **Ctrl+S** - Tallenna tosite
- **Ctrl+F** - Hae tositetta
- **Ctrl+P** - Tulosta raportti
- **F5** - Päivitä näkymä

**Ratkaisu:**
Lisää KeyBindings `DocumentFrame.java`:aan.

**Toteutus:**
```java
// DocumentFrame.java - setupKeyboardShortcuts()
private void setupKeyboardShortcuts() {
    JRootPane rootPane = getRootPane();

    // Ctrl+N - Uusi tosite
    rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
        KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK),
        "newDocument");
    rootPane.getActionMap().put("newDocument", new AbstractAction() {
        public void actionPerformed(ActionEvent e) {
            createDocument();
        }
    });

    // Ctrl+S - Tallenna
    rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
        KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK),
        "saveDocument");
    rootPane.getActionMap().put("saveDocument", new AbstractAction() {
        public void actionPerformed(ActionEvent e) {
            saveCurrentDocument();
        }
    });

    // Ctrl+F - Haku
    rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
        KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_DOWN_MASK),
        "search");
    rootPane.getActionMap().put("search", new AbstractAction() {
        public void actionPerformed(ActionEvent e) {
            showSearchDialog();
        }
    });

    // F5 - Päivitä
    rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
        KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0),
        "refresh");
    rootPane.getActionMap().put("refresh", new AbstractAction() {
        public void actionPerformed(ActionEvent e) {
            refreshView();
        }
    });
}
```

**Tiedostot:**
- `src/main/java/kirjanpito/ui/DocumentFrame.java` (lisää setupKeyboardShortcuts())

**Aika:** ~1 tunti
**Vaikutus:** KORKEA - Tuottavuus paranee 10x
**Prioriteetti:** 1

---

### 3. File Associations (.tilitin-tiedostot) ⭐⭐⭐⭐

**Ongelma:**
`.tilitin`-tiedostoja ei voi tuplaklikkaa Explorerissa → ei avaudu Tilitinissä.

**Ratkaisu:**
Lisää jPackage `--file-associations` parametri.

**Toteutus:**

**1. Luo `tilitin-file-assoc.properties`:**
```properties
# Tilitin File Association
mime-type=application/x-tilitin
extension=tilitin
description=Tilitin Kirjanpitotietokanta
icon=src/main/resources/tilitin.ico
```

**2. Päivitä `build-windows.bat` ja `build-windows-installer.bat`:**
```batch
jpackage ^
  ... (muut parametrit) ...
  --file-associations tilitin-file-assoc.properties ^
  --dest dist\windows
```

**3. Päivitä sovelluksen käynnistys:**
```java
// Kirjanpito.java - main()
public static void main(String[] args) {
    // Jos käynnistetty tiedostolla, avaa se
    if (args.length > 0 && args[0].endsWith(".tilitin")) {
        // Aseta jdbcUrl tiedostopolusta
        jdbcUrl = "jdbc:sqlite:" + args[0];
    }
    // ...
}
```

**Tiedostot:**
- `tilitin-file-assoc.properties` (uusi)
- `build-windows.bat` (lisää --file-associations)
- `build-windows-installer.bat` (lisää --file-associations)
- `src/main/java/kirjanpito/ui/Kirjanpito.java` (tiedostoparametrin käsittely)

**Aika:** ~20-30 minuuttia
**Vaikutus:** KORKEA - Käytettävyys paranee merkittävästi
**Prioriteetti:** 2

---

### 4. Code Signing (Windows Defender) ⭐⭐⭐⭐⭐

**Ongelma:**
Allekirjoittamaton `.exe` ja `.msi` → Windows Defender SmartScreen varoittaa:
```
"Windows protected your PC"
"Unknown publisher"
```
→ Käyttäjät eivät uskalla asentaa.

**Ratkaisu:**
Osta Code Signing -sertifikaatti ja allekirjoita paketit.

**Toteutus:**

**1. Hanki sertifikaatti:**
- **Sectigo** (aiemmin Comodo): ~70-100 €/vuosi
- **DigiCert**: ~250-400 €/vuosi
- **SSL.com**: ~60-80 €/vuosi

**Suositus:** Sectigo PositiveSSL Code Signing (halvin)

**2. Asenna sertifikaatti Windows Certificate Storeen TAI tallenna `.pfx`-tiedostona**

**3. Allekirjoita MSI:**
```batch
REM Vaihtoehto A: Sertifikaatti Windows Storessa
signtool sign /fd SHA256 /n "Tilitin Project" /t http://timestamp.sectigo.com "dist\installer\Tilitin 2.0-2.0.0.msi"

REM Vaihtoehto B: .pfx-tiedosto
signtool sign /fd SHA256 /a /f "cert.pfx" /p "salasana" /t http://timestamp.sectigo.com "dist\installer\Tilitin 2.0-2.0.0.msi"
```

**4. Lisää build-skripteihin automaattinen allekirjoitus:**
```batch
REM build-windows-installer.bat lopussa
if defined CERT_FILE (
    echo Allekirjoitetaan MSI...
    signtool sign /fd SHA256 /f "%CERT_FILE%" /p "%CERT_PASSWORD%" /t http://timestamp.sectigo.com "dist\installer\Tilitin 2.0-2.0.0.msi"
)
```

**Tiedostot:**
- `build-windows-installer.bat` (lisää automaattinen allekirjoitus)
- `cert.pfx` (sertifikaatti, EI tallenneta GitHubiin!)

**Aika:** ~2 tuntia (setup), ~10 min/build
**Kustannus:** 70-100 €/vuosi
**Vaikutus:** KRIITTINEN - Ilman tätä käyttäjät eivät asenna
**Prioriteetti:** 1 (ENNEN julkaisua)

---

### 5. Käyttöohje (KAYTTOOHJE.md) ⭐⭐⭐⭐

**Ongelma:**
README.md sisältää vain asennusohjeet. Ei käyttöohjeita:
- Miten luodaan uusi kirjanpito?
- Miten kirjataan tositteita?
- Miten luodaan raportteja?

**Ratkaisu:**
Luo kattava `KAYTTOOHJE.md` kuvakaappauksineen.

**Sisältö:**

```markdown
# Tilitin 2.0 - Käyttöohje

## 1. Ensimmäinen Käynnistys

### 1.1 Uuden Kirjanpidon Luominen

1. Käynnistä Tilitin 2.0
2. Valitse **Tiedosto → Uusi tietokanta**
3. Valitse tietokantaformaatti:
   - **SQLite** (suositus): Helppo, ei vaadi palvelinta
   - MySQL/PostgreSQL: Edistyneille käyttäjille
4. Valitse tilikarttamalli:
   - Yksityistalous
   - Elinkeinotoiminta (ALV 25.5%)
   - Yhdistys
   - Asunto-osakeyhtiö
5. Klikkaa **Luo**

[Kuvakaappaus: Uusi tietokanta -dialogi]

### 1.2 Olemassa Olevan Kirjanpidon Avaaminen

1. **Vaihtoehto A:** Tuplaklikkaa `.tilitin`-tiedostoa Explorerissa
2. **Vaihtoehto B:** Tiedosto → Avaa → Valitse tietokanta

---

## 2. Tositteiden Kirjaus

### 2.1 Uuden Tositteen Luominen

1. Klikkaa **Uusi tosite** (tai paina **Ctrl+N**)
2. Täytä tositteen tiedot:
   - **Päivämäärä:** Tapahtumapäivä
   - **Tositelaji:** Osto, Myynti, Palkka, jne.
   - **Selite:** Kuvaus (esim. "Tietokoneostos")

[Kuvakaappaus: Tosite-dialogi]

### 2.2 Vientien Lisääminen

**Esimerkki: Tietokoneen osto 1000 € + ALV 240 €**

| Tili | Debet | Kredit | Selite |
|------|-------|--------|--------|
| 1011 Kone ja kalusto | 1000,00 | | Tietokone |
| 1901 Arvonlisävero | 240,00 | | ALV 24% |
| 1910 Ostovelat | | 1240,00 | Maksettava |

**Saldolaskenta:** Erotus = 0,00 (debet = kredit) ✅

3. Klikkaa **Tallenna** (tai paina **Ctrl+S**)

---

## 3. Raportit

### 3.1 Pääkirja (General Ledger)

1. Valitse **Raportit → Pääkirja**
2. Valitse aikaväli (esim. 1.1.2024 - 31.12.2024)
3. Klikkaa **Luo raportti**
4. Esikatselu avautuu
5. Klikkaa **Tulosta** → **Tallenna PDF:ksi**

[Kuvakaappaus: Raportti-esikatselu]

### 3.2 Tuloslaskelma ja Tase

- **Tuloslaskelma:** Raportit → Tuloslaskelma
- **Tase:** Raportit → Tase

---

## 4. Asetukset

### 4.1 Teeman Vaihto (Vaalea/Tumma)

1. Valitse **Työkalut → Asetukset**
2. Teema-välilehti
3. Valitse:
   - **Vaalea (Light)** - Oletusarvo
   - **Tumma (Dark)** - Silmille helpompi
4. Klikkaa **OK**
5. Sovellus vaihtaa teeman välittömästi

[Kuvakaappaus: Vaalea vs. Tumma teema]

---

## 5. Keyboard Shortcuts (Pikanäppäimet)

| Näppäin | Toiminto |
|---------|----------|
| **Ctrl+N** | Uusi tosite |
| **Ctrl+S** | Tallenna tosite |
| **Ctrl+F** | Hae tositetta |
| **Ctrl+P** | Tulosta raportti |
| **F5** | Päivitä näkymä |

---

## 6. Vianmääritys

### Tietokanta ei avaudu

1. Tarkista tiedostopolku
2. Tarkista tiedoston oikeudet (ei read-only)
3. SQLite: Tarkista että tiedosto ei ole vioittunut

### Raportti ei luodu

1. Tarkista että tositteet on kirjattu oikein
2. Tarkista aikaväli
3. Katso virheviestit

---

## 7. Lisätietoja

- **Tuki:** https://github.com/priku/tilitin-modernized/issues
- **Dokumentaatio:** https://helineva.net/tilitin/
- **Lisenssi:** GPL v3
```

**Tiedostot:**
- `KAYTTOOHJE.md` (uusi)
- `screenshots/` (uusi kansio kuvakaappauksia varten)

**Aika:** ~3-4 tuntia (sisältäen kuvakaappaukset)
**Vaikutus:** KORKEA - Käyttäjät osaavat käyttää sovellusta
**Prioriteetti:** 2

---

## UI/UX Parannukset

### Toteutettu ✅
- [x] FlatLaf-teema (Light/Dark) - **Valmis 27.12.2025**
- [x] Pyöristetyt kulmat (10px border-radius)
- [x] Modernit taulukot (viivat näkyvissä)

### Suunnitteilla 📋

#### 6. Hakutoiminto (Ctrl+F) ⭐⭐⭐⭐⭐
- Globaali haku tositteiden välillä
- Haku päivämäärän, selitteen, summan mukaan
- Real-time filtering (kirjoitettaessa)

**Aika:** ~2 tuntia
**Prioriteetti:** 2

#### 7. Undo/Redo ⭐⭐⭐
- UndoManager tositteiden muokkaukseen
- Ctrl+Z (Undo), Ctrl+Y (Redo)

**Aika:** ~1.5 tuntia
**Prioriteetti:** 3

#### 8. Recent Files Lista ⭐⭐⭐
- Viimeksi käytetyt 5 tietokantaa
- Tiedosto → Viimeaikaiset

**Aika:** ~30 minuuttia
**Prioriteetti:** 3

#### 9. Auto-save ⭐⭐
- Timer-pohjainen automaattinen tallennus (5 min välein)
- Asetettavissa päällä/pois

**Aika:** ~45 minuuttia
**Prioriteetti:** 4

#### 10. Status Bar ⭐⭐
- Tilapalkki pääikkunan alareunaan
- Näyttää: Tietokanta, Käyttäjä, Viimeisin tallennus

**Aika:** ~30 minuuttia
**Prioriteetti:** 4

#### 11. SVG-Ikonit ⭐⭐⭐
- Vaihda PNG-ikonit (16x16, 22x22) SVG-ikoneihin
- FlatLaf tukee SVG:tä natiivisti
- Skaalautuvat kaikille resoluutioille

**Aika:** ~2 tuntia
**Prioriteetti:** 3

#### 12. Accessibility (Saavutettavuus) ⭐⭐⭐
- `setAccessibleName()` ja `setAccessibleDescription()`
- Tuki näytönlukijoille (JAWS, NVDA)
- Keyboard navigation -parannukset

**Aika:** ~3 tuntia
**Prioriteetti:** 3

---

## Koodin Laatu

### Kriittiset Korjaukset ⚠️

#### 13. Try-with-resources (KRIITTINEN) ⭐⭐⭐⭐⭐

**Ongelma:**
Resource leakit - `PreparedStatement` ja `ResultSet` eivät sulkeudu poikkeusten yhteydessä.

**Esimerkki (VÄÄRIN):**
```java
// SQLiteSession.java:70-81
PreparedStatement stmt = prepareStatement("SELECT last_insert_rowid()");
ResultSet rs = stmt.executeQuery();
// ...
rs.close();  // EI SULJE jos poikkeus tapahtuu!
stmt.close();
```

**Korjaus (OIKEIN):**
```java
try (PreparedStatement stmt = prepareStatement("SELECT last_insert_rowid()");
     ResultSet rs = stmt.executeQuery()) {
    // ...
} // Sulkeutuu automaattisesti
```

**Tiedostot korjattava:**
- Kaikki DAO-luokat (67 kpl):
  - `src/main/java/kirjanpito/db/sql/*.java`

**Aika:** ~6-8 tuntia (67 luokkaa)
**Vaikutus:** KRIITTINEN - Estää resource leakit
**Prioriteetti:** 1

---

#### 14. JPasswordField Salasanoille (TURVALLISUUS) ⭐⭐⭐⭐⭐

**Ongelma:**
Salasanat näkyvät tekstikenttinä.

```java
// DatabaseSettingsDialog.java:36
private JTextField passwordTextField; // VÄÄRIN!
```

**Korjaus:**
```java
private JPasswordField passwordField; // OIKEIN!
```

**Tiedostot:**
- `src/main/java/kirjanpito/ui/DatabaseSettingsDialog.java`

**Aika:** ~15 minuuttia
**Vaikutus:** KORKEA - Turvallisuus
**Prioriteetti:** 1

---

### Java 21 Modernisaatio

#### 15. StringBuilder (StringBuffer → StringBuilder) ⭐⭐

**Ongelma:**
Vanhentunut `StringBuffer` (synkronoitu, hidas).

```java
// DatabaseUpgradeUtil.java:28
StringBuffer buf = new StringBuffer(); // VÄÄRIN
```

**Korjaus:**
```java
StringBuilder buf = new StringBuilder(); // OIKEIN (nopeampi)
```

**Tiedostot:**
- `src/main/java/kirjanpito/db/DatabaseUpgradeUtil.java`

**Aika:** ~30 minuuttia (etsi kaikki StringBuffer)
**Prioriteetti:** 3

---

#### 16. StandardCharsets.UTF_8 ⭐⭐

**Ongelma:**
```java
Charset.forName("UTF-8") // Vanhentunut
```

**Korjaus:**
```java
StandardCharsets.UTF_8 // Java 7+
```

**Aika:** ~20 minuuttia
**Prioriteetti:** 4

---

#### 17. Java Streams ⭐⭐⭐

**Ongelma:**
Ei modernia kokoelmien käsittelyä.

**Esimerkki (VANHA):**
```java
List<String> result = new ArrayList<>();
for (String key : keys) {
    if (key.startsWith("account_")) {
        result.add(key.substring(8));
    }
}
return result.toArray(new String[0]);
```

**Korjaus (MODERNI):**
```java
return keys.stream()
    .filter(key -> key.startsWith("account_"))
    .map(key -> key.substring(8))
    .toArray(String[]::new);
```

**Aika:** ~4 tuntia (refaktoroi kriittiset kohdat)
**Prioriteetti:** 3

---

#### 18. Null-annotaatiot ⭐⭐⭐

**Ongelma:**
Ei `@Nullable`, `@NotNull`, `Objects.requireNonNull()`.

**Ratkaisu:**
```java
import javax.annotation.Nullable;
import javax.annotation.NotNull;

public @NotNull String getAccountName(@Nullable Account account) {
    Objects.requireNonNull(account, "Account ei voi olla null");
    return account.getName();
}
```

**Riippuvuus:**
```xml
<dependency>
    <groupId>com.google.code.findbugs</groupId>
    <artifactId>jsr305</artifactId>
    <version>3.0.2</version>
</dependency>
```

**Aika:** ~3 tuntia
**Prioriteetti:** 3

---

#### 19. Logger.log() (ei printStackTrace) ⭐⭐⭐

**Ongelma:**
12 kpl `e.printStackTrace()` -kutsuja.

```java
// Kirjanpito.java:82, 136, 142
catch (Exception e) {
    e.printStackTrace(); // VÄÄRIN
}
```

**Korjaus:**
```java
catch (Exception e) {
    Logger.getLogger(LOGGER_NAME).log(Level.SEVERE, "Virhe", e);
}
```

**Aika:** ~1 tunti
**Prioriteetti:** 3

---

### Arkkitehtuuri

#### 20. DocumentFrame.java Refaktorointi ⭐⭐⭐⭐

**Ongelma:**
God Class - 3544 riviä! Tekee liikaa.

**Ratkaisu:**
Pilko pienempiin luokkiin:
- `DocumentFrameController.java` (logiikka)
- `DocumentFrameView.java` (UI-komponentit)
- `DocumentFrameModel.java` (data)

**Tavoite:** <500 riviä/luokka

**Aika:** ~12-16 tuntia (iso refaktorointi)
**Prioriteetti:** 4 (pitkällä tähtäimellä)

---

## Dokumentaatio

### Toteutettu ✅
- [x] README.md
- [x] BUILDING.md
- [x] TESTAUS.md
- [x] PROJEKTISUUNNITELMA.md
- [x] CHANGELOG.md

### Suunnitteilla 📋

#### 21. KAYTTOOHJE.md ⭐⭐⭐⭐⭐
→ Katso kohta 5 (TOP 5)

#### 22. API-dokumentaatio (Javadoc) ⭐⭐⭐

**Ratkaisu:**
```xml
<!-- pom.xml -->
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-javadoc-plugin</artifactId>
    <version>3.6.3</version>
    <configuration>
        <show>public</show>
        <nohelp>true</nohelp>
    </configuration>
</plugin>
```

**Generoi:**
```bash
mvn javadoc:javadoc
# Tulostuu: target/site/apidocs/
```

**Aika:** ~4 tuntia (Javadoc-kommentit)
**Prioriteetti:** 3

---

#### 23. ARCHITECTURE.md ⭐⭐⭐

**Sisältö:**
- Tietokantakaavio (ER-diagram)
- Luokkakaavio (UML)
- Arkkitehtuurikuvaus (MVC-pohja)

**Aika:** ~3 tuntia
**Prioriteetti:** 3

---

#### 24. CONTRIBUTING.md ⭐⭐

**Sisältö:**
- Pull request -ohjeet
- Koodityyliohjeet
- Testausvaatimukset

**Aika:** ~1 tunti
**Prioriteetti:** 4

---

## Testaus

### Nykyinen Tila: ❌ EI TESTEJÄ (0 kpl)

#### 25. JUnit 5 + Mockito (KRIITTINEN) ⭐⭐⭐⭐⭐

**Ongelma:**
Ei yksikkötestejä → regressioriskit.

**Ratkaisu:**
```xml
<!-- pom.xml -->
<dependency>
    <groupId>org.junit.jupiter</groupId>
    <artifactId>junit-jupiter</artifactId>
    <version>5.10.1</version>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.mockito</groupId>
    <artifactId>mockito-core</artifactId>
    <version>5.8.0</version>
    <scope>test</scope>
</dependency>
```

**Testausstrategia:**
1. **DAO-luokat** (data-kriittinen) - Prioriteetti 1
2. **Model-luokat** (logiikka) - Prioriteetti 2
3. **UI-testit** (AssertJ Swing) - Prioriteetti 3

**Esimerkkitesti:**
```java
// src/test/java/kirjanpito/db/sql/SQLiteAccountDAOTest.java
@Test
void testFindAccountById() throws DataAccessException {
    // Arrange
    Account account = new Account();
    account.setId(1);
    account.setName("Myyntitulot");

    // Act
    Account result = accountDAO.getById(1);

    // Assert
    assertEquals("Myyntitulot", result.getName());
}
```

**Tavoite:** 60%+ testikattavuus

**Aika:** ~20-30 tuntia (DAO + Model testit)
**Prioriteetti:** 1

---

#### 26. JaCoCo (Testikattavuus) ⭐⭐⭐

**Ratkaisu:**
```xml
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <version>0.8.11</version>
    <executions>
        <execution>
            <goals>
                <goal>prepare-agent</goal>
            </goals>
        </execution>
        <execution>
            <id>report</id>
            <phase>test</phase>
            <goals>
                <goal>report</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

**Generoi raportti:**
```bash
mvn test jacoco:report
# Tulostuu: target/site/jacoco/index.html
```

**Aika:** ~30 minuuttia (setup)
**Prioriteetti:** 2

---

## Build & Deploy

### Toteutettu ✅
- [x] GitHub Actions CI/CD
- [x] Maven build
- [x] jPackage Windows .exe
- [x] jPackage Windows .msi

### Suunnitteilla 📋

#### 27. Code Quality (SpotBugs, PMD, Checkstyle) ⭐⭐⭐

**Ratkaisu:**
```xml
<!-- SpotBugs -->
<plugin>
    <groupId>com.github.spotbugs</groupId>
    <artifactId>spotbugs-maven-plugin</artifactId>
    <version>4.8.2.0</version>
</plugin>

<!-- PMD -->
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-pmd-plugin</artifactId>
    <version>3.21.2</version>
</plugin>

<!-- Checkstyle -->
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-checkstyle-plugin</artifactId>
    <version>3.3.1</version>
</plugin>
```

**GitHub Actions:**
```yaml
- name: Code Quality
  run: mvn spotbugs:check pmd:check checkstyle:check
```

**Aika:** ~2 tuntia (setup + korjaukset)
**Prioriteetti:** 2

---

#### 28. Dependabot ⭐⭐⭐

**Ratkaisu:**
```yaml
# .github/dependabot.yml
version: 2
updates:
  - package-ecosystem: "maven"
    directory: "/"
    schedule:
      interval: "weekly"
    open-pull-requests-limit: 5
```

**Aika:** ~10 minuuttia
**Prioriteetti:** 2

---

#### 29. Automaattiset Releaaset ⭐⭐

**Nykyinen:** Draft release (vaatii manuaalisen julkaisun)

**Ratkaisu:**
```yaml
# .github/workflows/release.yml
on:
  push:
    tags:
      - 'v*'
jobs:
  release:
    runs-on: windows-latest
    steps:
      - uses: actions/checkout@v4
      - name: Build Windows Installer
        run: .\build-windows-installer.bat
      - name: Create Release
        uses: softprops/action-gh-release@v1
        with:
          files: dist/installer/*.msi
```

**Aika:** ~1 tunti
**Prioriteetti:** 3

---

## Windows-Spesifit Parannukset

### Toteutettu ✅
- [x] Windows .exe (standalone)
- [x] Windows .msi (installer)
- [x] Windows ikoni (.ico)

### Suunnitteilla 📋

#### 30. File Associations ⭐⭐⭐⭐⭐
→ Katso kohta 3 (TOP 5)

#### 31. Code Signing ⭐⭐⭐⭐⭐
→ Katso kohta 4 (TOP 5)

#### 32. Microsoft Store (MSIX) ⭐⭐⭐⭐

**Edellytykset:**
1. Code Signing -sertifikaatti
2. Microsoft Partner Center -tili (19 USD/vuosi)
3. MSIX-paketti

**MSIX vs. MSI:**
| Ominaisuus | MSI | MSIX |
|------------|-----|------|
| Microsoft Store | ❌ | ✅ |
| Automaattiset päivitykset | ❌ | ✅ |
| Sandboxed | ❌ | ✅ |
| Admin-oikeudet | ✅ Vaatii | ❌ Ei vaadi |

**Toteutus:**

**1. Luo MSIX AppxManifest.xml:**
```xml
<!-- msix/AppxManifest.xml -->
<?xml version="1.0" encoding="utf-8"?>
<Package xmlns="http://schemas.microsoft.com/appx/manifest/foundation/windows10"
         xmlns:uap="http://schemas.microsoft.com/appx/manifest/uap/windows10">
  <Identity Name="TilitinProject.Tilitin"
            Version="2.0.0.0"
            Publisher="CN=Tilitin Project"
            ProcessorArchitecture="x64"/>
  <Properties>
    <DisplayName>Tilitin 2.0</DisplayName>
    <PublisherDisplayName>Tilitin Project</PublisherDisplayName>
    <Logo>tilitin.png</Logo>
  </Properties>
  <Resources>
    <Resource Language="fi-FI"/>
  </Resources>
  <Applications>
    <Application Id="Tilitin" Executable="Tilitin 2.0.exe" EntryPoint="Windows.FullTrustApplication">
      <uap:VisualElements DisplayName="Tilitin 2.0"
                          Description="Ilmainen kirjanpito-ohjelma"
                          BackgroundColor="#0078D4"
                          Square150x150Logo="tilitin_150x150.png"
                          Square44x44Logo="tilitin_44x44.png"/>
    </Application>
  </Applications>
</Package>
```

**2. Paketointi:**
```powershell
# Tarvitsee Windows SDK:n makeappx.exe
makeappx pack /d "dist\windows\Tilitin 2.0" /p "dist\Tilitin-2.0.0.msix"
```

**3. Allekirjoitus:**
```powershell
signtool sign /fd SHA256 /f cert.pfx /p salasana "dist\Tilitin-2.0.0.msix"
```

**4. Julkaisu Microsoft Storeen:**
- Luo Partner Center -tili
- Lataa MSIX
- Täytä Store-listaus (kuvaukset, kuvakaappaukset)
- Lähetä hyväksyttäväksi (2-3 päivää)

**Aika:** ~8 tuntia (setup + Store-listaus)
**Kustannus:** 19 USD/vuosi (Partner Center)
**Prioriteetti:** 2 (ENNEN julkaisua)

---

#### 33. Jump List (Viimeaikaiset Tietokannat) ⭐⭐⭐

**Ratkaisu:**
```java
// JNA - Windows TaskBar API
import com.sun.jna.platform.win32.*;

public void addRecentDatabase(String path) {
    // Lisää Jump Listiin
    Shell32.INSTANCE.SHAddToRecentDocs(
        Shell32.SHARD_PATHA,
        path
    );
}
```

**Riippuvuus:**
```xml
<dependency>
    <groupId>net.java.dev.jna</groupId>
    <artifactId>jna-platform</artifactId>
    <version>5.14.0</version>
</dependency>
```

**Aika:** ~2 tuntia
**Prioriteetti:** 3

---

#### 34. Toast-notifikaatiot ⭐⭐

**Esim.:** "Tosite tallennettu", "Raportti valmis"

**Ratkaisu:**
```java
// Windows 10+ Toast Notifications
import org.codehaus.jettison.json.JSONObject;

public void showToast(String title, String message) {
    // Käytä Windows Runtime (WinRT) API:a
    // TAI kirjoita PowerShell-skripti
}
```

**Aika:** ~3 tuntia
**Prioriteetti:** 4

---

## Turvallisuusriskit

### Kriittiset ⚠️

#### 35. JPasswordField ⭐⭐⭐⭐⭐
→ Katso kohta 14

#### 36. Try-with-resources ⭐⭐⭐⭐⭐
→ Katso kohta 13

#### 37. Älä Logita Salasanoja ⭐⭐⭐⭐

**Ongelma:**
```java
Logger.log("Opening database: " + url + " user: " + username + " password: " + password);
// VÄÄRIN!
```

**Korjaus:**
```java
Logger.log("Opening database: " + url + " user: " + username);
// password EI logissa
```

**Aika:** ~30 minuuttia (etsi kaikki)
**Prioriteetti:** 1

---

### Keskisuuret

#### 38. Input-validointi ⭐⭐⭐

**Ongelma:**
Ei tarkisteta päivämääriä, numeroita.

**Ratkaisu:**
```java
// Bean Validation (JSR 303)
public class Account {
    @NotNull(message = "Nimi ei voi olla tyhjä")
    @Size(min = 1, max = 255)
    private String name;

    @Min(value = 0, message = "Saldo ei voi olla negatiivinen")
    private BigDecimal balance;
}
```

**Riippuvuus:**
```xml
<dependency>
    <groupId>org.hibernate.validator</groupId>
    <artifactId>hibernate-validator</artifactId>
    <version>8.0.1.Final</version>
</dependency>
```

**Aika:** ~4 tuntia
**Prioriteetti:** 3

---

## Performance-Optimoinnit

#### 39. In-memory Caching ⭐⭐⭐

**Ongelma:**
Account-tietoja haetaan joka kerta tietokannasta.

**Ratkaisu:**
```java
// Caffeine Cache
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

private Cache<Integer, Account> accountCache = Caffeine.newBuilder()
    .maximumSize(1000)
    .expireAfterWrite(10, TimeUnit.MINUTES)
    .build();

public Account getAccountById(int id) {
    return accountCache.get(id, k -> accountDAO.getById(id));
}
```

**Riippuvuus:**
```xml
<dependency>
    <groupId>com.github.ben-manes.caffeine</groupId>
    <artifactId>caffeine</artifactId>
    <version>3.1.8</version>
</dependency>
```

**Aika:** ~2 tuntia
**Prioriteetti:** 3

---

#### 40. Lazy Loading (Pagination) ⭐⭐⭐⭐

**Ongelma:**
Kaikki tositteet ladataan kerralla → hidas isolla tietokannalla (1000+ tositetta).

**Ratkaisu:**
```java
// JTable - LazyLoadingModel
public class LazyDocumentTableModel extends AbstractTableModel {
    private static final int PAGE_SIZE = 100;
    private List<Document> currentPage;
    private int totalRows;

    public void loadPage(int pageNumber) {
        int offset = pageNumber * PAGE_SIZE;
        currentPage = documentDAO.getDocuments(offset, PAGE_SIZE);
    }
}
```

**Aika:** ~4 tuntia
**Prioriteetti:** 2

---

#### 41. Background Threads (SwingWorker) ⭐⭐⭐

**Ongelma:**
Raporttien generointi jäädyttää UI:n.

**Nykyinen (OSA OK):**
```java
// Joissain kohdissa käytetty, mutta ei kaikkialla
SwingWorker<Report, Void> worker = new SwingWorker<>() {
    protected Report doInBackground() {
        return reportService.generateReport();
    }
    protected void done() {
        showReport(get());
    }
};
worker.execute();
```

**Korjaa:** Lisää SwingWorker kaikkialle missä tehdään raskasta laskentaa.

**Aika:** ~2 tuntia
**Prioriteetti:** 3

---

## Toteutusaikataulu

### Sprint 1: Kriittiset Korjaukset (2-3 viikkoa)

**Viikko 1:**
- [ ] 13. Try-with-resources (67 DAO-luokkaa) - 8h
- [ ] 14. JPasswordField - 0.5h
- [ ] 25. JUnit 5 + testit DAO-luokille - 12h
- [ ] 37. Älä logita salasanoja - 0.5h

**Viikko 2:**
- [ ] 4. Code Signing (hanki sertifikaatti) - 2h
- [ ] 5. Käyttöohje (KAYTTOOHJE.md) - 4h
- [ ] 27. Code Quality (SpotBugs) - 2h
- [ ] 28. Dependabot - 0.5h

**Viikko 3:**
- [ ] 19. Logger.log() (ei printStackTrace) - 1h
- [ ] 26. JaCoCo testikattavuus - 0.5h
- [ ] Testaa & dokumentoi

**Yhteensä:** ~31 tuntia

---

### Sprint 2: UI-Parannukset (2 viikkoa)

**Viikko 1:**
- [ ] 1. Teeman vaihto UI:ssa - 1h
- [ ] 2. Keyboard shortcuts - 1h
- [ ] 3. File associations - 0.5h
- [ ] 6. Hakutoiminto (Ctrl+F) - 2h

**Viikko 2:**
- [ ] 8. Recent files lista - 0.5h
- [ ] 11. SVG-ikonit - 2h
- [ ] 32. Microsoft Store (MSIX) - 8h

**Yhteensä:** ~15 tuntia

---

### Sprint 3: Arkkitehtuuri & Laatu (3 viikkoa)

**Viikko 1:**
- [ ] 15. StringBuilder - 0.5h
- [ ] 16. StandardCharsets.UTF_8 - 0.5h
- [ ] 17. Java Streams - 4h
- [ ] 18. Null-annotaatiot - 3h

**Viikko 2:**
- [ ] 22. Javadoc - 4h
- [ ] 23. ARCHITECTURE.md - 3h
- [ ] 29. Automaattiset releaaset - 1h

**Viikko 3:**
- [ ] 40. Lazy loading - 4h
- [ ] 39. Caching - 2h
- [ ] 41. Background threads - 2h

**Yhteensä:** ~24 tuntia

---

### Sprint 4: Polish & Accessibility (1 viikko)

- [ ] 7. Undo/Redo - 1.5h
- [ ] 9. Auto-save - 0.75h
- [ ] 10. Status bar - 0.5h
- [ ] 12. Accessibility - 3h
- [ ] 24. CONTRIBUTING.md - 1h
- [ ] 33. Jump List - 2h

**Yhteensä:** ~9 tuntia

---

### Sprint 5: Refaktorointi (2 viikkoa) [Valinnainen]

- [ ] 20. DocumentFrame.java refaktorointi - 16h
- [ ] 38. Input-validointi - 4h

**Yhteensä:** ~20 tuntia

---

## Yhteenveto

### Kokonaiskesto
- **Sprint 1-4:** 7-8 viikkoa (~79 tuntia)
- **Sprint 5 (valinnainen):** +2 viikkoa (~20 tuntia)

### Budjetti-arvio
- **Code Signing -sertifikaatti:** 70-100 €/vuosi
- **Microsoft Partner Center:** 19 USD/vuosi (~18 €)
- **Kehitystyö:** 79 tuntia × 50 €/h = **3950 €**

**Yhteensä:** ~4050 €

### Prioriteettijärjestys (TOP 10)

1. ⭐⭐⭐⭐⭐ **Try-with-resources** (Estää resource leakit)
2. ⭐⭐⭐⭐⭐ **JPasswordField** (Turvallisuus)
3. ⭐⭐⭐⭐⭐ **Testit (JUnit 5)** (Estää regressiot)
4. ⭐⭐⭐⭐⭐ **Käyttöohje** (Käyttäjät osaavat käyttää)
5. ⭐⭐⭐⭐⭐ **Code Signing** (Windows Defender ei blokkaa)
6. ⭐⭐⭐⭐⭐ **Keyboard shortcuts** (Tuottavuus)
7. ⭐⭐⭐⭐⭐ **Teeman vaihto UI:ssa** (Käytettävyys)
8. ⭐⭐⭐⭐ **File associations** (Tuplaklikkaus toimii)
9. ⭐⭐⭐⭐ **Hakutoiminto** (Löydä tositteet nopeasti)
10. ⭐⭐⭐⭐ **Lazy loading** (Toimii isoilla tietokannoilla)

---

**Projektin tila:** HYVÄ - FlatLaf modernisaatio onnistunut, paljon potentiaalia lisäparannuksille.

**Suositus:** Aloita Sprint 1 (kriittiset), sitten Sprint 2 (UI), lopuksi Sprint 3-4 (arkkitehtuuri).

---

**Laatija:** GitHub Copilot + Koodipohjan analyysi (176 Java-tiedostoa)
**Päivitetty:** 27.12.2025
