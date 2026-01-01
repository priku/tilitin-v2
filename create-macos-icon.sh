#!/bin/bash
# ====================================================================
# Tilitin - macOS Icon Generator
# Luo .icns-tiedosto macOS-sovellukselle
# ====================================================================

echo ""
echo "========================================"
echo " Tilitin macOS Icon Generator"
echo "========================================"
echo ""

# Tarkista että sips on saatavilla (macOS built-in)
if ! command -v sips &> /dev/null; then
    echo "[VIRHE] sips-komento ei löydy! Tämä skripti toimii vain macOS:ssä."
    exit 1
fi

# Tarkista että iconutil on saatavilla (macOS built-in)
if ! command -v iconutil &> /dev/null; then
    echo "[VIRHE] iconutil-komento ei löydy! Tämä skripti toimii vain macOS:ssä."
    exit 1
fi

RESOURCES_DIR="src/main/resources"
SOURCE_ICON="$RESOURCES_DIR/tilitin-48x48.png"
ICONSET_DIR="tilitin.iconset"
OUTPUT_ICON="$RESOURCES_DIR/tilitin.icns"

# Tarkista että lähdetiedosto on olemassa
if [ ! -f "$SOURCE_ICON" ]; then
    echo "[VIRHE] Lähdetiedostoa $SOURCE_ICON ei löydy!"
    exit 1
fi

echo "[1/3] Luodaan iconset-hakemisto..."
rm -rf "$ICONSET_DIR"
mkdir -p "$ICONSET_DIR"

# Luo tarvittavat koot .icns-tiedostoa varten
# macOS vaatii seuraavat koot: 16x16, 32x32, 128x128, 256x256, 512x512 ja retina-versiot (@2x)
echo "[2/3] Generoidaan eri kokoiset ikonit..."

# 16x16 ja 32x32@2x
sips -z 16 16 "$SOURCE_ICON" --out "$ICONSET_DIR/icon_16x16.png" > /dev/null 2>&1
sips -z 32 32 "$SOURCE_ICON" --out "$ICONSET_DIR/icon_16x16@2x.png" > /dev/null 2>&1

# 32x32 ja 64x64@2x
sips -z 32 32 "$SOURCE_ICON" --out "$ICONSET_DIR/icon_32x32.png" > /dev/null 2>&1
sips -z 64 64 "$SOURCE_ICON" --out "$ICONSET_DIR/icon_32x32@2x.png" > /dev/null 2>&1

# 128x128 ja 256x256@2x
sips -z 128 128 "$SOURCE_ICON" --out "$ICONSET_DIR/icon_128x128.png" > /dev/null 2>&1
sips -z 256 256 "$SOURCE_ICON" --out "$ICONSET_DIR/icon_128x128@2x.png" > /dev/null 2>&1

# 256x256 ja 512x512@2x
sips -z 256 256 "$SOURCE_ICON" --out "$ICONSET_DIR/icon_256x256.png" > /dev/null 2>&1
sips -z 512 512 "$SOURCE_ICON" --out "$ICONSET_DIR/icon_256x256@2x.png" > /dev/null 2>&1

# 512x512 ja 1024x1024@2x
sips -z 512 512 "$SOURCE_ICON" --out "$ICONSET_DIR/icon_512x512.png" > /dev/null 2>&1
sips -z 1024 1024 "$SOURCE_ICON" --out "$ICONSET_DIR/icon_512x512@2x.png" > /dev/null 2>&1

echo "[3/3] Luodaan .icns-tiedosto..."
iconutil -c icns "$ICONSET_DIR" -o "$OUTPUT_ICON"

# Siivoa
rm -rf "$ICONSET_DIR"

if [ -f "$OUTPUT_ICON" ]; then
    echo ""
    echo "========================================"
    echo " ONNISTUI!"
    echo "========================================"
    echo ""
    echo "macOS-ikoni luotu: $OUTPUT_ICON"
    echo "Koko: $(du -h "$OUTPUT_ICON" | cut -f1)"
    echo ""
else
    echo ""
    echo "[VIRHE] .icns-tiedoston luonti epäonnistui!"
    exit 1
fi
