# Kehittäjäohje (Contributing)

Kiitos kiinnostuksestasi Tilittimen kehittämiseen! 🎉

## Projektin rakenne

```
tilitin/
├── src/main/java/kirjanpito/
│   ├── db/           # Tietokantakerros (SQLite, MySQL, PostgreSQL)
│   ├── models/       # Tietomallit (Account, Entry, Document, Period)
│   ├── reports/      # Raporttien generointi (PDF, tulostus)
│   ├── ui/           # Käyttöliittymä (Swing + FlatLaf)
│   └── util/         # Apuluokat
├── src/main/kotlin/kirjanpito/
│   ├── db/           # Kotlin DAO-toteutukset
│   └── ui/           # Kotlin UI-komponentit
├── src/main/resources/
│   ├── reports/      # Raporttien header-tiedostot
│   ├── schema/       # Tietokantaskeemat (SQLite, MySQL, PostgreSQL)
│   └── tilikarttamallit/  # Tilikarttapohjat
├── installer/        # Inno Setup -skriptit
├── build.gradle.kts  # Gradle build-konfiguraatio
├── settings.gradle.kts # Gradle-asetukset
├── build-windows.bat          # Windows .exe -build
├── build-inno-installer.bat   # Inno Setup -build
└── build/            # Gradle output
```

## Kehitysympäristön asennus

### Vaatimukset

- **JDK 21+** (suositus: [Eclipse Temurin](https://adoptium.net/))
- **Git**
- **IDE** (IntelliJ IDEA, Eclipse tai VS Code + Java-lisäosa)

### Aloitus

```bash
# Kloonaa repositorio
git clone https://github.com/priku/tilitin-modernized.git
cd tilitin-modernized

# Buildaa projekti
./gradlew build

# Käynnistä sovellus
./gradlew run

# Tai JAR-paketilla
./gradlew jar
java -jar build/libs/tilitin-2.2.3.jar
```

## Kehityskäytännöt

### Git-workflow

1. **Fork** repositorio omalle tilillesi
2. **Luo feature-haara** omasta forkista:
   ```bash
   git checkout -b feature/oma-ominaisuus
   ```
3. **Tee muutokset** ja commitoi:
   ```bash
   git commit -m "feat: Lisätty uusi ominaisuus"
   ```
4. **Push** haarasi GitHubiin:
   ```bash
   git push origin feature/oma-ominaisuus
   ```
5. **Luo Pull Request** GitHub-sivulla

### Commit-viestit

Käytämme [Conventional Commits](https://www.conventionalcommits.org/) -käytäntöä:

| Tyyppi | Kuvaus |
|--------|--------|
| `feat:` | Uusi ominaisuus |
| `fix:` | Bugikorjaus |
| `docs:` | Dokumentaatiomuutos |
| `style:` | Muotoilu (ei vaikuta koodiin) |
| `refactor:` | Refaktorointi |
| `test:` | Testien lisäys/muutos |
| `chore:` | Ylläpitotehtävät |

**Esimerkkejä:**
```
feat: Lisätty tumman teeman tuki
fix: Korjattu PDF-raportin marginaalit
docs: Päivitetty BUILDING.md Windows-ohjeilla
```

### Koodityyli

- **Sisennys:** 4 välilyöntiä (ei tabulaattoreita)
- **Rivinvaihto:** Unix-tyylinen (LF)
- **Merkistö:** UTF-8
- **Nimeäminen:**
  - Luokat: `PascalCase` (esim. `DocumentFrame`)
  - Metodit: `camelCase` (esim. `createDocument`)
  - Vakiot: `SCREAMING_SNAKE_CASE` (esim. `APP_NAME`)
  - Paketit: `lowercase` (esim. `kirjanpito.ui`)

### Testaus

Ennen Pull Requestin luomista:

1. **Buildaa projekti:**
   ```bash
   ./gradlew build
   ```

2. **Testaa manuaalisesti:**
   - Käynnistä sovellus
   - Testaa muuttamasi toiminnallisuus
   - Varmista ettei regressioita

3. **Testaa Windows-build (jos muokkasit UI:ta):**
   ```bash
   build-windows.bat
   ```

## Gradle-komennot

```bash
# Käännä kaikki (Java + Kotlin)
./gradlew build

# Vain käännös
./gradlew compileJava compileKotlin

# JAR-paketti
./gradlew jar

# Suorita sovellus
./gradlew run

# Puhdista
./gradlew clean

# Listaa kaikki tehtävät
./gradlew tasks
```

## FlatLaf-teeman kehitys

### Teeman vaihto

Käyttäjä voi vaihtaa teemaa `asetukset.properties`-tiedostossa:

```properties
# Vaalea teema (oletus)
ui.theme=light

# Tumma teema
ui.theme=dark
```

### Uusien teemojen lisääminen

FlatLaf tukee monia teemoja. Katso:
- [FlatLaf Themes](https://www.formdev.com/flatlaf/themes/)
- [IntelliJ Themes](https://www.formdev.com/flatlaf/intellij-themes/)

Teeman lisäys `Kirjanpito.java`-tiedostoon:

```java
case "dracula":
    FlatDraculaLaf.setup();
    break;
```

## Raportointi

### Bugiraportit

Luo [GitHub Issue](https://github.com/priku/tilitin-modernized/issues) seuraavilla tiedoilla:

1. **Otsikko:** Lyhyt kuvaus ongelmasta
2. **Ympäristö:** Windows/Mac/Linux, Java-versio
3. **Toistettavuus:** Vaiheet ongelman toistamiseen
4. **Odotettu tulos:** Mitä pitäisi tapahtua
5. **Todellinen tulos:** Mitä tapahtuu
6. **Kuvakaappaukset:** Jos mahdollista

### Ominaisuuspyynnöt

Luo [GitHub Issue](https://github.com/priku/tilitin-modernized/issues) tyypillä "Feature request":

1. **Kuvaus:** Mitä ominaisuutta ehdotat
2. **Käyttötapaus:** Miksi tämä olisi hyödyllinen
3. **Ehdotus:** Miten toiminnallisuus voisi toimia

## Yhteystiedot

- **GitHub Issues:** https://github.com/priku/tilitin-modernized/issues
- **Alkuperäinen dokumentaatio:** https://helineva.net/tilitin/

## Lisenssi

Kaikki kontribuutiot julkaistaan [GPL v3](COPYING) -lisenssillä.
