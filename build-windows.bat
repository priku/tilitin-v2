@echo off
REM ====================================================================
REM Tilitin - Windows Build Script
REM Luo natiivin Windows-sovelluksen jPackage-työkalulla
REM ====================================================================

echo.
echo ========================================
echo  Tilitin Windows Build
echo ========================================
echo.

REM Tarkista että Java on asennettu
java -version >nul 2>&1
if errorlevel 1 (
    echo [VIRHE] Java ei löydy! Asenna Java 21 tai uudempi.
    pause
    exit /b 1
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
echo [2/3] Luodaan natiivi Windows-sovellus jPackage:lla...
echo.

REM Luo dist-hakemisto jos ei ole olemassa
if not exist "dist" mkdir dist
if not exist "dist\windows" mkdir dist\windows

REM Suorita jPackage
jpackage ^
  --input target ^
  --name Tilitin ^
  --main-jar tilitin-1.6.0.jar ^
  --main-class kirjanpito.ui.Kirjanpito ^
  --type app-image ^
  --app-version 1.6.0 ^
  --vendor "Tilitin Project" ^
  --description "Ilmainen kirjanpito-ohjelma yrityksille ja yhdistyksille" ^
  --icon src\main\resources\tilitin.ico ^
  --java-options "--enable-native-access=ALL-UNNAMED" ^
  --win-console ^
  --win-menu ^
  --win-menu-group "Tilitin" ^
  --win-shortcut ^
  --win-dir-chooser ^
  --dest dist\windows

if errorlevel 1 (
    echo [VIRHE] jPackage-paketointi epäonnistui!
    pause
    exit /b 1
)

echo.
echo [3/3] Build valmis!
echo.
echo ========================================
echo  BUILD ONNISTUI!
echo ========================================
echo.
echo Sovellus on kansiossa: dist\windows\Tilitin\
echo Suoritettava tiedosto: dist\windows\Tilitin\Tilitin.exe
echo.
echo Voit testata sovellusta komennolla:
echo   dist\windows\Tilitin\Tilitin.exe
echo.

pause
