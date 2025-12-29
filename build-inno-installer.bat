@echo off
setlocal enabledelayedexpansion

echo ============================================
echo   Tilitin - Modern Installer Builder
echo ============================================
echo.

REM Hae versio pom.xml:stä
for /f "tokens=3 delims=<>" %%v in ('findstr /r "<version>.*</version>" pom.xml ^| findstr /n . ^| findstr "^1:"') do set VERSION=%%v
echo Versio: %VERSION%

REM Check if jPackage output exists
if not exist "dist\windows\Tilitin %VERSION%\Tilitin %VERSION%.exe" (
    echo [ERROR] jPackage output not found!
    echo Expected: dist\windows\Tilitin %VERSION%\Tilitin %VERSION%.exe
    echo Please run build-windows.bat first.
    echo.
    if "%CI%"=="" pause
    exit /b 1
)

REM Find Inno Setup compiler
set "ISCC="
if exist "%ProgramFiles(x86)%\Inno Setup 6\ISCC.exe" (
    set "ISCC=%ProgramFiles(x86)%\Inno Setup 6\ISCC.exe"
) else if exist "%ProgramFiles%\Inno Setup 6\ISCC.exe" (
    set "ISCC=%ProgramFiles%\Inno Setup 6\ISCC.exe"
) else if exist "%LocalAppData%\Programs\Inno Setup 6\ISCC.exe" (
    set "ISCC=%LocalAppData%\Programs\Inno Setup 6\ISCC.exe"
)

if "!ISCC!"=="" (
    echo [ERROR] Inno Setup 6 not found!
    echo Please install Inno Setup from: https://jrsoftware.org/isinfo.php
    echo.
    pause
    exit /b 1
)

echo [INFO] Found Inno Setup: !ISCC!
echo.

REM Create output directory
if not exist "dist\installer" mkdir "dist\installer"

REM Compile the installer
echo [BUILD] Compiling installer...
echo.
"!ISCC!" /Q "installer\tilitin.iss"

if %errorlevel% neq 0 (
    echo.
    echo [ERROR] Installer compilation failed!
    pause
    exit /b 1
)

REM Hae versio pom.xml:stä
for /f "tokens=3 delims=<>" %%v in ('findstr /r "<version>.*</version>" pom.xml ^| findstr /n . ^| findstr "^1:"') do set VERSION=%%v

echo.
echo ============================================
echo   Build Complete
echo ============================================
echo.
echo Installer created:
dir /b "dist\installer\Tilitin-%VERSION%-setup.exe" 2>nul
for %%F in ("dist\installer\Tilitin-%VERSION%-setup.exe") do echo Size: %%~zF bytes
echo.
echo Location: dist\installer\Tilitin-%VERSION%-setup.exe
echo.

REM Skip pause in CI environment
if "%CI%"=="" pause
exit /b 0
