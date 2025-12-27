# MSI-asennusohjelman Modernisointi

## Ongelma

jPackage luo toimivan MSI-asennusohjelman, mutta wizardi näyttää vanhalta:
- Perinteinen Windows Installer -ulkoasu
- Harmaa värimaailma
- Vanhanaikaiset dialogit
- Ei tumman teeman tukea

## Vaihtoehdot Modernisointiin

### 🚀 Vaihtoehto 1: Advanced Installer (Suositeltu!)

**Mikä se on:**
- Ammattilaistyökalu Windows-asennusohjelmien tekemiseen
- Moderni visuaalinen editori
- Tukee tummia/vaaleita teemoja
- Automaattinen teeman tunnistus (Windows dark/light mode)

**Edut:**
- ✅ Napsuttelupohjainen käyttöliittymä (ei XML-koodausta)
- ✅ Valmiit modernit teemat
- ✅ MSI Embedded UI -tuki
- ✅ Ilmainen Freeware-versio (rajoitetut ominaisuudet)
- ✅ Professional-versio: $499/vuosi

**Haitat:**
- ❌ Maksullinen täysiin ominaisuuksiin
- ❌ Lisätyökalu asennettava
- ❌ Ei integroidu suoraan Maven-buildiin

**Käyttö:**
1. Lataa: https://www.advancedinstaller.com/
2. Tuo jPackage-luotu MSI projektiksi
3. Muokkaa teemaa visuaalisessa editorissa
4. Rakenna uudelleen

**Linkit:**
- [Themes Page](https://www.advancedinstaller.com/user-guide/ui-themes.html)
- [MSI Embedded UI](https://www.advancedinstaller.com/user-guide/msi-embedded-ui.html)

---

### 🔧 Vaihtoehto 2: WiX Toolset Custom UI

**Mikä se on:**
- jPackage käyttää WiX Toolsetiä sisäisesti MSI:n luomiseen
- WiX:llä voi tehdä custom UI:ta XML-tiedostoilla
- Täysi kontrolli asennusohjelman ulkoasusta

**Edut:**
- ✅ Ilmainen ja avoimen lähdekoodin
- ✅ Täysi kontrolli ulkoasusta
- ✅ Integroitavissa Maven-buildiin
- ✅ Valmiit dialog-kirjastot (WixUI)

**Haitat:**
- ❌ Vaatii WiX XML -ohjelmointia
- ❌ Jyrkkä oppimiskäyrä
- ❌ Ei voi käyttää suoraan jPackage-outputin kanssa
- ❌ Pitää luopua jPackagesta ja kirjoittaa koko MSI WiX:llä

**Miten toimii:**

1. **Luo WiX-projekti** (.wxs-tiedostot)
2. **Määrittele custom dialogs** XML:nä
3. **Käytä teemoja** (kopioi SDK/themes-kansiosta)
4. **Buildaa**: `candle.exe` ja `light.exe`

**Esimerkki - WiX dialogi-määritys:**
```xml
<UI>
  <UIRef Id="WixUI_InstallDir" />
  <Publish Dialog="ExitDialog" Control="Finish" Event="DoAction" Value="LaunchApplication">
    WIXUI_EXITDIALOGOPTIONALCHECKBOX = 1 and NOT Installed
  </Publish>
</UI>
```

**Valmiit WixUI-setit:**
- WixUI_Mondo
- WixUI_FeatureTree
- WixUI_InstallDir (suositeltu)
- WixUI_Minimal
- WixUI_Advanced

**Linkit:**
- [Using Built-in WixUI Dialog Sets](https://wixtoolset.org/docs/v3/wixui/wixui_dialog_library/)
- [Custom UIs](https://deepwiki.com/wixtoolset/wix/7.2-custom-uis)
- [How to add custom dialogs](https://www.add-in-express.com/creating-addins-blog/add-custom-dialogs-wix-installer/)

---

### 🎨 Vaihtoehto 3: WiX Bootstrapper (Burn)

**Mikä se on:**
- Luo .exe-asennusohjelman MSI:n sijaan
- Tukee WPF/WinForms-pohjaista modernia UI:ta
- Voi sisältää useita MSI-paketteja

**Edut:**
- ✅ Täysin mukautettu moderni UI (WPF)
- ✅ Tumma/vaalea teema helposti
- ✅ Voi näyttää "app store" -tyyliseltä

**Haitat:**
- ❌ Monimutkaisempi kuin pelkkä MSI
- ❌ Vaatii WiX XML + WPF/C#-osaamista
- ❌ Suurempi työmäärä

**Linkit:**
- [Customizing WiX bootstrapper layout](https://michielsioen.be/2017-12-02-wix-bootstrapper-customization/)

---

### 📦 Vaihtoehto 4: Electron-tyylinen Installer (NSIS)

**Mikä se on:**
- NSIS (Nullsoft Scriptable Install System)
- Käytetään mm. Electronin asennusohjelmissa
- Moderni, yksinkertainen ulkoasu

**Edut:**
- ✅ Moderni ulkoasu
- ✅ Kevyt ja nopea
- ✅ Ilmainen

**Haitat:**
- ❌ Ei MSI-muoto (ei Windows Installer -ominaisuuksia)
- ❌ Ei integroidu jPackage-työnkulkuun
- ❌ Vaatii NSIS-skriptauksen oppimista

---

### ⚙️ Vaihtoehto 5: jPackage-parametrit (rajoitettu)

**Mikä se on:**
- jPackage tarjoaa muutamia parametreja MSI:n kustomointiin

**Saatavilla olevat parametrit:**
```batch
--icon <path>                   # Kuvake (toimii)
--license-file <path>           # Lisenssitiedosto (toimii)
--win-menu-group <name>         # Start Menu -ryhmä (toimii)
--win-dir-chooser               # Asennushakemiston valinta (toimii)
--win-upgrade-uuid <uuid>       # Päivitys-UUID (toimii)
```

**Ei saatavilla:**
- ❌ Teeman värien muuttaminen
- ❌ Fonttien muuttaminen
- ❌ Dialog-layoutin muokkaus
- ❌ Tumman teeman tuki

**Yhteenveto:**
jPackage tarjoaa vain perusparametrit, ei visuaalista kustomointia.

---

## 🎯 Suositus: Mikä valita?

### Jos haluat "riittävän hyvän" ratkaisun (5 min):

**➡️ Käytä jPackagea sellaisenaan**
- MSI toimii hyvin, vaikka näyttää perinteiseltä
- Käyttäjät ovat tottuneet Windows Installer -wizardiin
- Keskity sovelluksen sisäisen UI:n modernisointiin (FlatLaf)

---

### Jos haluat modernin asennusohjelman (1-2 päivää):

**➡️ Vaihtoehto A: Advanced Installer**

1. Lataa Advanced Installer (ilmainen tai trial)
2. Tuo jPackage MSI projektiksi
3. Valitse moderni teema
4. Rakenna uudelleen

**Aikaa:** ~2-4 tuntia
**Hinta:** Ilmainen (Freeware) tai $499/vuosi (Professional)

---

**➡️ Vaihtoehto B: WiX Custom UI**

1. Luo WiX-projekti (.wxs)
2. Käytä `WixUI_InstallDir` dialog settiä
3. Kopioi ja muokkaa teema SDK/themes -kansiosta
4. Buildaa `candle.exe` ja `light.exe` -työkaluilla

**Aikaa:** ~1-2 päivää (jos osaat XML:ää)
**Hinta:** Ilmainen

---

### Jos haluat "waun" modernin installer (1-2 viikkoa):

**➡️ WiX Bootstrapper + WPF Custom UI**

1. Luo WiX Burn bootstrapper -projekti
2. Tee moderni WPF-käyttöliittymä (C#)
3. Integroituu jPackage-luotuun MSI:hen
4. Täysi kontrolli ulkoasusta

**Aikaa:** ~1-2 viikkoa
**Hinta:** Ilmainen (vaatii osaamista)

---

## 💡 Käytännön Esimerkit

### Esimerkki 1: Advanced Installer Modernin Teeman Käyttö

**Vaiheet:**

1. **Lataa Advanced Installer**
   - https://www.advancedinstaller.com/download.html

2. **Tuo MSI-projekti**
   - File → Import → MSI
   - Valitse: `dist\installer\Tilitin 2.0-2.0.0.msi`

3. **Valitse Teema**
   - Dialogs → Themes
   - Valitse "Modern Blue" tai "Dark Metro"

4. **Muokkaa Värejä**
   - Customize Theme Colors
   - Aseta omat värit (esim. sininen aksentti)

5. **Rakenna**
   - Build → Build

**Tulos:** Moderni MSI samalla toiminnallisuudella, uudella ulkoasulla.

---

### Esimerkki 2: WiX Custom UI (Minimaalinen)

**Luo tiedosto: installer.wxs**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<Wix xmlns="http://schemas.microsoft.com/wix/2006/wi">
  <Product Id="*" Name="Tilitin 2.0" Language="1033" Version="2.0.0"
           Manufacturer="Tilitin Project" UpgradeCode="12345678-90AB-CDEF-1234-567890ABCDEF">

    <Package InstallerVersion="200" Compressed="yes" InstallScope="perUser" />

    <MajorUpgrade DowngradeErrorMessage="A newer version is already installed." />

    <MediaTemplate EmbedCab="yes" />

    <!-- Käytä modernia WixUI_InstallDir -dialogia -->
    <UIRef Id="WixUI_InstallDir" />
    <Property Id="WIXUI_INSTALLDIR" Value="INSTALLFOLDER" />

    <!-- Asennettavat tiedostot -->
    <Directory Id="TARGETDIR" Name="SourceDir">
      <Directory Id="ProgramFilesFolder">
        <Directory Id="INSTALLFOLDER" Name="Tilitin 2.0">
          <Component Id="MainExecutable" Guid="*">
            <File Id="TilitinEXE" Source="dist\windows\Tilitin 2.0\Tilitin 2.0.exe" KeyPath="yes" />
          </Component>
        </Directory>
      </Directory>
    </Directory>

    <Feature Id="ProductFeature" Title="Tilitin 2.0" Level="1">
      <ComponentRef Id="MainExecutable" />
    </Feature>
  </Product>
</Wix>
```

**Buildaa:**
```batch
candle.exe installer.wxs
light.exe -ext WixUIExtension installer.wixobj -o Tilitin-2.0.msi
```

**Huom:** Tämä on yksinkertaistettu esimerkki. Todellinen WiX-projekti vaatii enemmän komponentteja.

---

## 🔍 Vertailu: jPackage vs. Advanced Installer vs. WiX

| Ominaisuus | jPackage | Advanced Installer | WiX Custom UI |
|------------|----------|-------------------|---------------|
| **Helppous** | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐ | ⭐⭐ |
| **Moderni ulkoasu** | ⭐⭐ | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐ |
| **Kustomointi** | ⭐ | ⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ |
| **Hinta** | Ilmainen | $499/vuosi | Ilmainen |
| **Oppimiskäyrä** | Helppo | Keskiverto | Vaikea |
| **Integrointi Maven** | ✅ Suora | ❌ Manuaalinen | ⚠️ Mahdollinen |
| **Tumma teema** | ❌ | ✅ | ✅ |
| **Windows Store** | ❌ | ✅ (MSIX) | ✅ (MSIX) |

---

## 📋 Päätöksenteko

### Jos budjetti on rajallinen ja aika lyhyt:

**➡️ Pidä jPackage MSI sellaisenaan**
- Toimii hyvin
- Käyttäjät tunnistavat perinteisen wizardin
- Keskity sovelluksen sisäiseen modernisointiin

---

### Jos haluat panostaa ammattimaisen vaikutelman:

**➡️ Advanced Installer (Freeware tai trial)**
- Nopea toteuttaa (2-4h)
- Moderni ulkoasu
- Helppo käyttää

---

### Jos haluat täyden kontrollin ja ilmaisen ratkaisun:

**➡️ WiX Custom UI**
- Vaatii oppimista
- Täysi kustomointi
- Ilmainen

---

## 🚀 Seuraavat Askeleet

### Vaihtoehto A: Advanced Installer

1. Lataa Advanced Installer
2. Tuo `Tilitin 2.0-2.0.0.msi`
3. Valitse moderni teema
4. Rakenna uudelleen
5. Testaa

### Vaihtoehto B: WiX Custom UI

1. Luo WiX-projekti
2. Määrittele komponentit (.wxs)
3. Käytä `WixUI_InstallDir`
4. Buildaa `candle` + `light`
5. Testaa

### Vaihtoehto C: Jatka jPackagella

1. Hyväksy perinteinen ulkoasu
2. Keskity sovelluksen FlatLaf-teeman viimeistelyyn
3. Julkaise MSI sellaisenaan

---

## 📚 Lisäresurssit

**Advanced Installer:**
- [Themes Page](https://www.advancedinstaller.com/user-guide/ui-themes.html)
- [Customize Suite Installer Theme](https://www.advancedinstaller.com/customize-suite-installer-theme.html)
- [MSI Embedded UI](https://www.advancedinstaller.com/user-guide/msi-embedded-ui.html)

**WiX Toolset:**
- [Using Built-in WixUI Dialog Sets](https://wixtoolset.org/docs/v3/wixui/wixui_dialog_library/)
- [Custom UIs](https://deepwiki.com/wixtoolset/wix/7.2-custom-uis)
- [How to add custom dialogs in WiX installers](https://www.add-in-express.com/creating-addins-blog/add-custom-dialogs-wix-installer/)
- [Customizing WiX bootstrapper layout](https://michielsioen.be/2017-12-02-wix-bootstrapper-customization/)

**jPackage:**
- [Creating Installers for Java Applications with jpackage](https://dev.to/ozkanpakdil/creating-installers-for-java-applications-with-jpackage-5016)
- [Use jpackage to Create Native Java App Installers](https://www.devdungeon.com/content/use-jpackage-create-native-java-app-installers)
- [JPackage : Create MSI/EXE Installer for Java App](https://howtodoinjava.com/devops/jpackage-plugin-example/)

**FlatLaf (sovelluksen UI):**
- [Swing on Steroids: Modernizing Java Desktop Apps with FlatLaf and JReleaser](https://www.javacodegeeks.com/2025/05/swing-on-steroids-modernizing-java-desktop-apps-with-flatlaf-and-jreleaser.html)

---

## ✅ Yhteenveto

**Nopea vastaus kysymykseesi:**

> "Onko mitään muuta tapaa modernisoida msi paketin asennus prosessia?"

**Kyllä on! 3 päävaihtoehtoa:**

1. **Advanced Installer** - Helpoin, maksullinen, moderni
2. **WiX Custom UI** - Ilmainen, vaatii XML-osaamista, täysi kontrolli
3. **WiX Bootstrapper** - Ilmainen, vaatii WPF-osaamista, "waun" moderni

**Suositus Tilitin 2.0 -projektille:**

- **Pieni budjetti:** Pidä jPackage MSI (riittävän hyvä)
- **Keskikokoinen panostus:** Advanced Installer Freeware (moderni, helppo)
- **Täysi kontrolli:** WiX Custom UI (ilmainen, vaatii oppimista)

**Mielestäni:** Koska Tilitin 2.0:n pääfokus on sovelluksen sisäisen UI:n modernisointi FlatLaf:lla, MSI:n perinteinen ulkoasu ei ole kriittinen ongelma. Käyttäjät näkevät wizardin vain kerran (asennuksessa), mutta FlatLaf-teemaa joka päivä.

Jos kuitenkin haluat modernin asennusohjelman, **Advanced Installer Freeware** on nopein tapa (2-4h työtä).
