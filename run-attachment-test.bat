@echo off
REM Test runner for AttachmentDAO tests
REM This script compiles and runs the AttachmentDAO test suite

echo ========================================
echo AttachmentDAO Test Runner
echo ========================================
echo.

echo Building project...
call mvn clean compile -q
if %ERRORLEVEL% NEQ 0 (
    echo Build failed!
    pause
    exit /b 1
)

echo.
echo Running tests...
echo.

REM Find the JAR file
for %%f in (target\tilitin-*.jar) do set JAR_FILE=%%f

if not exist "%JAR_FILE%" (
    echo JAR file not found. Building package...
    call mvn package -DskipTests -q
    for %%f in (target\tilitin-*.jar) do set JAR_FILE=%%f
)

REM Build classpath
set CP=%JAR_FILE%
for %%f in (target\lib\*.jar) do set CP=%CP%;%%f

REM Run the test
java -cp "%CP%" kirjanpito.test.AttachmentDAOTest

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ========================================
    echo All tests passed!
    echo ========================================
) else (
    echo.
    echo ========================================
    echo Some tests failed!
    echo ========================================
)

pause

