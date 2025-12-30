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
    echo [VIRHE] Java ei loydy! Asenna Java 21 tai uudempi.
    pause
    exit /b 1
)

echo [1/3] Rakennetaan JAR-paketti Gradlella...
echo.
call gradlew clean jar
if errorlevel 1 (
    echo [VIRHE] Gradle build epäonnistui!
    pause
    exit /b 1
)

echo.
echo [2/3] Luodaan natiivi Windows-sovellus jPackage:lla...
echo.

REM Luo dist-hakemisto jos ei ole olemassa
if not exist "dist" mkdir dist
if not exist "dist\windows" mkdir dist\windows

REM Hae versio build.gradle.kts:sta
for /f "tokens=2 delims==" %%v in ('findstr /r "version.*=" build.gradle.kts ^| findstr /n . ^| findstr "^1:"') do set VERSION=%%v
REM Poista välilyönnit ja lainausmerkit
set VERSION=%VERSION: =%
set VERSION=%VERSION:"=%
echo Versio: %VERSION%

REM Fallback jos versiota ei löydy
if "%VERSION%"=="" set VERSION=2.2.0

REM Suorita jPackage
jpackage ^
  --input build\libs ^
  --name "Tilitin %VERSION%" ^
  --main-jar tilitin-%VERSION%.jar ^
  --main-class kirjanpito.ui.Kirjanpito ^
  --type app-image ^
  --app-version %VERSION% ^
  --vendor "Tilitin Project" ^
  --description "Ilmainen kirjanpito-ohjelma yrityksille ja yhdistyksille" ^
  --icon src\main\resources\tilitin.ico ^
  --java-options "--enable-native-access=ALL-UNNAMED" ^
  --dest dist\windows

if errorlevel 1 (
    echo [VIRHE] jPackage-paketointi epäonnistui!
    if "%CI%"=="" pause
    exit /b 1
)

echo.
echo [3/3] Build valmis!
echo.
echo ========================================
echo  BUILD ONNISTUI!
echo ========================================
echo.
echo Sovellus on kansiossa: dist\windows\Tilitin %VERSION%\
echo Suoritettava tiedosto: dist\windows\Tilitin %VERSION%\Tilitin %VERSION%.exe
echo.
echo Voit testata sovellusta komennolla:
echo   "dist\windows\Tilitin %VERSION%\Tilitin %VERSION%.exe"
echo.

pause
