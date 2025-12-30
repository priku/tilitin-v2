@echo off
REM ====================================================================
REM Tilitin - Kotlin Build Script
REM Kaantaa Java- ja Kotlin-koodin
REM ====================================================================

echo Kaannetaan Kotlin ja Java...
call gradlew compileJava compileKotlin
echo.
echo Valmis!
pause
