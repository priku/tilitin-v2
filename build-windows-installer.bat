@echo off
REM ====================================================================
REM Tilitin - Windows Installer Build Script
REM Luo MSI-asennusohjelman jPackage-työkalulla
REM ====================================================================

echo.
echo ========================================
echo  Tilitin Windows Installer Build
echo ========================================
echo.

REM Tarkista että Java on asennettu
java -version >nul 2>&1
if errorlevel 1 (
    echo [VIRHE] Java ei löydy! Asenna Java 25 tai uudempi.
    pause
    exit /b 1
)

REM Tarkista että WiX Toolset on asennettu (MSI-paketointi vaatii sen)
where candle >nul 2>&1
if errorlevel 1 (
    echo [VAROITUS] WiX Toolset ei löydy!
    echo MSI-paketointi saattaa epäonnistua.
    echo Lataa WiX: https://wixtoolset.org/
    echo.
    echo Jatketaanko silti? (Y/N)
    set /p continue=
    if /i not "%continue%"=="Y" exit /b 1
)

echo [1/3] Rakennetaan JAR-paketti Mavenilla...
echo.
call mvn clean package
if errorlevel 1 (
    echo [VIRHE] Maven build epäonnistui!
    pause
    exit /b 1
)

echo.
echo [2/3] Luodaan MSI-asennusohjelma jPackage:lla...
echo.

REM Luo dist-hakemisto jos ei ole olemassa
if not exist "dist" mkdir dist
if not exist "dist\installer" mkdir dist\installer

REM Generoi uniikki GUID upgrade UUID:lle (käytä samaa kaikissa versioissa!)
REM HUOM: Tämä tulisi olla vakio kaikissa versioissa päivityksen toimivuuden vuoksi
set UPGRADE_UUID=12345678-90AB-CDEF-1234-567890ABCDEF

REM Suorita jPackage MSI-tyypillä
jpackage ^
  --input target ^
  --name "Tilitin 2.0" ^
  --main-jar tilitin-2.0.0.jar ^
  --main-class kirjanpito.ui.Kirjanpito ^
  --type msi ^
  --app-version 2.0.0 ^
  --vendor "Tilitin Project" ^
  --description "Ilmainen kirjanpito-ohjelma yrityksille ja yhdistyksille" ^
  --icon src\main\resources\tilitin.ico ^
  --java-options "--enable-native-access=ALL-UNNAMED" ^
  --win-menu ^
  --win-menu-group "Tilitin 2.0" ^
  --win-shortcut ^
  --win-dir-chooser ^
  --win-per-user-install ^
  --win-upgrade-uuid %UPGRADE_UUID% ^
  --license-file COPYING ^
  --dest dist\installer

if errorlevel 1 (
    echo [VIRHE] MSI-paketointi epäonnistui!
    pause
    exit /b 1
)

echo.
echo [3/3] Build valmis!
echo.
echo ========================================
echo  MSI INSTALLER LUOTU!
echo ========================================
echo.
echo Asennusohjelma: dist\installer\Tilitin 2.0-2.0.0.msi
echo.
echo Voit testata asennusta tuplaklikkaamalla MSI-tiedostoa.
echo.
echo HUOM: Allekirjoita MSI ennen julkaisua:
echo   signtool sign /fd SHA256 /a /f cert.pfx /p PASSWORD "dist\installer\Tilitin 2.0-2.0.0.msi"
echo.

pause
