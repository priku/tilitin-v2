# Tilitin - Build-ohjeet

## Vaatimukset

- **Java Development Kit (JDK) 21** tai uudempi
  - Suositus: [Eclipse Temurin](https://adoptium.net/)
- **Gradle 8.11+** (wrapper mukana projektissa)
- **Windows 10/11** (Windows-buildia varten)
- **Inno Setup 6** (asennusohjelmaa varten)
  - Lataa: https://jrsoftware.org/isinfo.php

## Perus JAR-paketin Buildaaminen

```bash
# Windows
.\gradlew jar

# Linux/Mac
./gradlew jar
```

Tuottaa: `build/libs/tilitin-X.X.X.jar`

Suorita:
```bash
java -jar build/libs/tilitin-X.X.X.jar
```

## Kääntäminen

```bash
# Käännä kaikki (Java + Kotlin)
.\gradlew build

# Vain käännös ilman testejä
.\gradlew compileJava compileKotlin

# Puhdista ja käännä
.\gradlew clean build
```

## Windows Natiivi Sovellus

### Vaihtoehto 1: App Image (.exe)

Luo natiivi Windows-sovellus ilman asennusohjelmaa:

```bash
build-windows.bat
```

Tuottaa:
- `dist/windows/Tilitin/Tilitin.exe`
- `dist/windows/Tilitin/` (koko sovelluskansio)

Sovellus sisältää JRE:n, joten Java-asennusta ei tarvita.

### Vaihtoehto 2: Inno Setup Installer (Suositus)

Luo moderni Windows-asennusohjelma:

```bash
# 1. Luo ensin app-image
build-windows.bat

# 2. Luo Inno Setup -asennusohjelma
build-inno-installer.bat
```

Tuottaa:
- `dist/installer/Tilitin-<versio>-setup.exe` (~57 MB)

**Edut:**
- Moderni ulkoasu (WizardStyle=modern)
- Suomen- ja englanninkielinen
- Pienempi tiedostokoko (LZMA2-pakkaus)
- Pikakuvakkeet työpöydälle ja Käynnistä-valikkoon

**Huom:** Vaatii [Inno Setup 6](https://jrsoftware.org/isinfo.php) -asennuksen.

## Gradle-tehtävät

```bash
# Listaa kaikki tehtävät
.\gradlew tasks

# JAR-paketti
.\gradlew jar

# Suorita sovellus
.\gradlew run

# Puhdista build-hakemisto
.\gradlew clean

# Käännä Kotlin
.\gradlew compileKotlin

# Käännä Java
.\gradlew compileJava
```

## Code Signing (Allekirjoitus)

Ennen julkaisua, allekirjoita asennusohjelma:

```bash
signtool sign /fd SHA256 /a /f cert.pfx /p PASSWORD dist/installer/Tilitin-X.X.X-setup.exe
```

Tai jos sertifikaatti on Windows Certificate Storessa:

```bash
signtool sign /fd SHA256 /n "Tilitin Project" dist/installer/Tilitin-X.X.X-setup.exe
```

## Testaus

### Testaa app-image

```bash
"dist\windows\Tilitin 2.0\Tilitin 2.0.exe"
```

### Testaa JAR-paketti

```bash
java -jar build/libs/tilitin-2.2.0.jar
```

## Ongelmatilanteet

### "gradlew: command not found"

Varmista että olet projektin juurihakemistossa ja käytä oikeaa komentoa:

```bash
# Windows PowerShell
.\gradlew build

# Windows CMD
gradlew build

# Linux/Mac
./gradlew build
```

### "JAVA_HOME is not set"

Aseta JAVA_HOME osoittamaan JDK 21+ asennukseen:

```bash
# Windows PowerShell
$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-21.0.1.12-hotspot"

# Pysyvä asetus Windows:ssa - Järjestelmäasetukset > Ympäristömuuttujat
```

### Gradle-välimuistin tyhjennys

```bash
.\gradlew clean
.\gradlew --stop
Remove-Item -Recurse -Force .gradle -ErrorAction SilentlyContinue
.\gradlew build
```

## CI/CD

GitHub Actions -workflowit ovat `.github/workflows/` -hakemistossa:

- **gradle.yml** - Perus CI-buildi jokaiselle pushille
- **advanced-build.yml** - Täysi buildi + release-paketit (Windows, macOS, Linux)

Automaattinen buildi suoritetaan:
- Push master-haaraan
- Pull requestit
- Tag-julkaisut (v*) → luodaan release + asennuspaketit

## Projektin rakenne

```
Tilitin2.0/
├── build.gradle.kts      # Gradle build-konfiguraatio
├── settings.gradle.kts   # Gradle-asetukset
├── gradle.properties     # Gradle-ominaisuudet
├── gradlew              # Gradle wrapper (Linux/Mac)
├── gradlew.bat          # Gradle wrapper (Windows)
├── src/
│   └── main/
│       ├── java/        # Java-lähdekoodi
│       ├── kotlin/      # Kotlin-lähdekoodi
│       └── resources/   # Resurssit (ikonit, SQL, jne.)
├── build/               # Gradle build-output
│   └── libs/            # JAR-paketit
└── dist/                # Jakelutiedostot
    ├── windows/         # App-image
    └── installer/       # Asennusohjelmat
```

## Lisätietoa

- Gradle: https://gradle.org/
- jPackage: https://docs.oracle.com/en/java/javase/21/jpackage/
- FlatLaf: https://www.formdev.com/flatlaf/
- Inno Setup: https://jrsoftware.org/isinfo.php
