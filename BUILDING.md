# Tilitin 2.1 - Build-ohjeet

## Vaatimukset

- **Java Development Kit (JDK) 25** tai uudempi
  - Suositus: [Eclipse Temurin](https://adoptium.net/)
- **Apache Maven 3.x**
- **Windows 10/11** (Windows-buildia varten)
- **WiX Toolset 3.14+** (MSI-asennusohjelmaa varten)
  - Lataa: https://wixtoolset.org/

## Perus JAR-paketin Buildaaminen

```bash
mvn clean package
```

Tuottaa: `target/tilitin-2.1.2.jar`

Suorita:
```bash
java -jar target/tilitin-2.1.2.jar
```

## Windows Natiivi Sovellus

### Vaihtoehto 1: App Image (.exe)

Luo natiivi Windows-sovellus ilman asennusohjelmaa:

```bash
build-windows.bat
```

Tuottaa:
- `dist/windows/Tilitin 2.0/Tilitin 2.0.exe`
- `dist/windows/Tilitin 2.0/` (koko sovelluskansio)

Sovellus sisältää JRE:n, joten Java-asennusta ei tarvita.

### Vaihtoehto 2: MSI Installer

Luo MSI-asennusohjelma:

```bash
build-windows-installer.bat
```

Tuottaa:
- `dist/installer/Tilitin 2.0-2.1.2.msi`

**Huom:** Vaatii WiX Toolset 3.14+ -asennuksen.

### Vaihtoehto 3: Inno Setup Installer (Suositus)

Luo moderni Windows-asennusohjelma:

```bash
# 1. Luo ensin app-image
build-windows.bat

# 2. Luo Inno Setup -asennusohjelma
build-inno-installer.bat
```

Tuottaa:
- `dist/installer/Tilitin-2.1.2-setup.exe` (~57 MB)

**Edut:**
- Moderni ulkoasu (WizardStyle=modern)
- Suomen- ja englanninkielinen
- Pienempi tiedostokoko (LZMA2-pakkaus)
- Pikakuvakkeet työpöydälle ja Käynnistä-valikkoon

**Huom:** Vaatii [Inno Setup 6](https://jrsoftware.org/isinfo.php) -asennuksen.

## Code Signing (Allekirjoitus)

Ennen julkaisua, allekirjoita MSI-paketti:

```bash
signtool sign /fd SHA256 /a /f cert.pfx /p PASSWORD dist/installer/Tilitin-1.6.0.msi
```

Tai jos sertifikaatti on Windows Certificate Storessa:

```bash
signtool sign /fd SHA256 /n "Tilitin Project" dist/installer/Tilitin-1.6.0.msi
```

## MSIX (Microsoft Store)

MSIX-paketin luominen:

```bash
# 1. Luo app-image ensin
build-windows.bat

# 2. Paketointi MSIX:ksi (vaatii MSIX Packaging Tool tai makeappx.exe)
makeappx pack /d dist\windows\Tilitin /p dist\Tilitin.msix

# 3. Allekirjoita
signtool sign /fd SHA256 /a /f cert.pfx /p PASSWORD dist\Tilitin.msix
```

## Testaus

### Testaa app-image

```bash
"dist\windows\Tilitin 2.0\Tilitin 2.0.exe"
```

### Testaa MSI-asennusohjelma

1. Tuplaklikkaa `dist/installer/Tilitin 2.0-2.1.2.msi`
2. Seuraa asennusohjeita
3. Käynnistä sovellus Start-valikosta tai työpöydän pikakuvakkeesta

## Ongelmatilanteet

### "jpackage: command not found"

Varmista että JDK 14+ on asennettu ja PATH-ympäristömuuttuja on asetettu.

```bash
java -version
```

### "WiX Toolset not found"

MSI-buildaus vaatii WiX Toolset -asennuksen:
1. Lataa: https://github.com/wixtoolset/wix3/releases
2. Asenna
3. Lisää `C:\Program Files (x86)\WiX Toolset v3.11\bin` PATH:iin

### Maven-riippuvuusongelmat

```bash
mvn clean install -U
```

## CI/CD

GitHub Actions -workflow on määritelty `.github/workflows/maven.yml` -tiedostossa.

Automaattinen buildi suoritetaan:
- Push master-haaraan
- Pull requestit
- Tag-julkaisut (v*)

## Lisätietoa

- jPackage: https://docs.oracle.com/en/java/javase/21/jpackage/
- FlatLaf: https://www.formdev.com/flatlaf/
- WiX Toolset: https://wixtoolset.org/documentation/
