#!/bin/bash
# ====================================================================
# Tilitin 3.0 - macOS Build Script (Gradle version)
# Luo natiivin macOS-sovelluksen jPackage-työkalulla
# ====================================================================

echo ""
echo "========================================"
echo " Tilitin 3.0 macOS Build"
echo "========================================"
echo ""

# Tarkista että Java on asennettu
if ! command -v java &> /dev/null; then
    echo "[VIRHE] Java ei löydy! Asenna Java 21 tai uudempi."
    exit 1
fi

echo "Java-versio:"
java -version

echo ""
echo "[1/4] Rakennetaan JAR-paketti Gradlella..."
echo ""
./gradlew clean fatJar
if [ $? -ne 0 ]; then
    echo "[VIRHE] Gradle build epäonnistui!"
    exit 1
fi

echo ""
echo "[2/4] Luodaan natiivi macOS-sovellus jPackage:lla..."
echo ""

# Luo dist-hakemisto jos ei ole olemassa
mkdir -p dist/macos

# Hae versio build.gradle.kts:stä
VERSION=$(grep -m 1 'version = ' build.gradle.kts | sed 's/.*version = "\(.*\)".*/\1/')
echo "Versio: $VERSION"

# Suorita jPackage
# Huom: --mac-sign ohitetaan kehitysaikana, tuotantoon allekirjoitetaan erikseen
jpackage \
  --input build/libs \
  --name "Tilitin" \
  --main-jar "tilitin-${VERSION}-all.jar" \
  --main-class kirjanpito.ui.javafx.JavaFXApp \
  --type app-image \
  --app-version "$VERSION" \
  --vendor "Tilitin Project" \
  --description "Ilmainen kirjanpito-ohjelma yrityksille ja yhdistyksille" \
  --icon src/main/resources/tilitin.icns \
  --java-options "--enable-native-access=ALL-UNNAMED" \
  --mac-package-identifier "fi.priku.tilitin" \
  --dest dist/macos

if [ $? -ne 0 ]; then
    echo "[VIRHE] jPackage-paketointi epäonnistui!"
    exit 1
fi

echo ""
echo "[3/4] Korjataan allekirjoitus..."
echo ""

# Poista ongelmalliset extended attributes jotka macOS lisää automaattisesti
xattr -d com.apple.FinderInfo dist/macos/Tilitin.app 2>/dev/null || true
xattr -d com.apple.fileprovider.fpfs#P dist/macos/Tilitin.app 2>/dev/null || true

# Ad-hoc allekirjoitus kehitysversiota varten
codesign --force --deep --sign - dist/macos/Tilitin.app

if [ $? -eq 0 ]; then
    echo "✓ Sovellus allekirjoitettu onnistuneesti (ad-hoc signature)"
else
    echo "⚠ Allekirjoitus epäonnistui, mutta sovellus saattaa silti toimia"
fi

echo ""
echo "[4/4] Build valmis!"
echo ""
echo "========================================"
echo " BUILD ONNISTUI!"
echo "========================================"
echo ""
echo "Sovellus on kansiossa: dist/macos/Tilitin.app/"
echo ""
echo "Voit testata sovellusta komennolla:"
echo "  open dist/macos/Tilitin.app"
echo ""
