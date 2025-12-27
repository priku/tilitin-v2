# Tilitin 2.0 - Ikoni-modernisointisuunnitelma

## 📊 Nykytilanteen Analyysi

### Nykyinen Ikoni

**Konsepti:** Paperilomake + kynä
**Värit:** Sininen lomake, oranssi/keltainen kynä
**Tyyli:** Litteä (flat design), yksinkertainen
**Resoluutiot:** 16x16, 32x32, 48x48 (PNG) + multi-resolution .ico

**Vahvuudet:**
- ✅ Selkeä viesti (kirjanpito = kirjoittaminen)
- ✅ Toimii pienissä kooissa
- ✅ Tunnistettava
- ✅ Sopii FlatLaf-teemaan

**Heikkoudet:**
- ⚠️ Yleinen metafora (ei erottuu)
- ⚠️ Ei kovin moderni
- ⚠️ Ei viittaa digitaaliseen kirjanpitoon
- ⚠️ Neutraali värimaailma

**Arvosana:** 7/10 - Toimiva mutta ei muistumiinarvoinen

---

## 🎨 Modernisointisuunnitelma

### Vaihtoehto 1: "Evolution" - Kevyt Päivitys ⭐ SUOSITUS

**Konsepti:** Säilytetään lomake + kynä, mutta modernisoidaan toteutus

**Muutokset:**
1. **Värimaailma:**
   - Vanha: Vaalea sininen (#4A90E2) + oranssi (#FFA500)
   - Uusi: Tummansininen (#1E3A8A) + teal/turkoosi (#14B8A6)
   - Lisää hienovarainen gradientti (2-3 sävyä)

2. **Muoto:**
   - Pyöristä lomakkeen kulmat (border-radius: 4px efekti)
   - Modernimpi kynä (gradientti metallisesta terävämpään)
   - Lisää pieni varjo lomakkeen alle (depth)

3. **Detaljit:**
   - Lisää lomakkeeseen hienovarainen "grid" tai viivat (viittaa taulukkoon)
   - Kynän pää hehkuu (glow-efekti, viittaa digitaaliseen)
   - Pieni euro-symboli (€) lomakkeen yläkulmassa

4. **3D-efektit:**
   - Hienovarainen kohokuvio (emboss)
   - Pehmeä varjo (soft shadow)
   - Lomake "kelluu" taustalla

**Toteutus:**
- 🛠️ Työkalu: Figma tai Inkscape (ilmainen vektorigrafiikka)
- ⏱️ Aika: 2-4 tuntia
- 💰 Kustannus: 0€ (itse) tai 50-150€ (freelancer)

**Tiedostot:**
- `tilitin-modern.ico` (16x16, 32x32, 48x48, 64x64, 128x128, 256x256)
- `app-icon-16x16.png`
- `app-icon-32x32.png`
- `app-icon-48x48.png`
- `app-icon-256x256.png` (Microsoft Store)
- `app-icon-512x512.png` (macOS, tulevaisuus)

---

### Vaihtoehto 2: "Digital Ledger" - Keskitason Uudistus

**Konsepti:** Yhdistetään kirjanpito + digitaalinen analytiikka

**Kuvaus:**
```
+---------------------------+
|  📊                    €  |  <- Pieni kaavio yläreunassa + euro-symboli
|  ___________________      |
|  |   |   |   |   |  |     |  <- Taulukko/grid
|  |---|---|---|---|--|     |
|  |   |   |   |   |  |     |
|  |___|___|___|___|__|     |
|                           |
|  [✓] ____________  💰    |  <- Checkbox + rahasäkki
+---------------------------+
```

**Elementit:**
1. **Taulukko/Spreadsheet** - Viittaa taulukkolaskentaan
2. **Mini-kaavio** - Pieni nouseva trendi-viiva (📈)
3. **Euro-symboli (€)** - Selkeä viittaus rahaan
4. **Checkbox** - Viittaa "kuitattuun" kirjanpitoon
5. **Rahasäkki-ikoni** - Pieni 3D-efekti

**Värimaailma:**
- Tummansininen pohja (#0F172A)
- Teal/turkoosi aksentit (#14B8A6)
- Kulta euro-symbolille (#F59E0B)
- Vihreä checkmarkille (#10B981)

**Toteutus:**
- 🛠️ Työkalu: Figma + Adobe Illustrator (jos saatavilla)
- ⏱️ Aika: 4-8 tuntia
- 💰 Kustannus: 0€ (itse) tai 150-300€ (freelancer)

---

### Vaihtoehto 3: "Brand Identity" - Täysi Uudistus

**Konsepti:** Luodaan uniikki Tilitin-brändi visuaalinen identiteetti

**Elementit:**
1. **Logo-monogrammi:**
   - "T" ja "2.0" yhdistetty graafiseksi elementiksi
   - Moderni, geometrinen muotoilu
   - Toimii standalone-logona

2. **Ikoniperhe:**
   - Pääikoni (sovellus)
   - Dokumentti-ikoni (.tilitin -tiedostotyyppi)
   - Splash screen -logo
   - Favicon (web-dokumentaatioon)

3. **Väripaletti:**
   ```
   Primääri:   #0F172A (Tummansininen)
   Sekundääri: #14B8A6 (Teal)
   Aksentti:   #F59E0B (Kulta)
   Neutraali:  #94A3B8 (Harmaa)
   ```

4. **Typografia:**
   - Logo: Inter Extra Bold / Poppins Bold
   - Tagline: "Moderni Kirjanpito"

**Visuaalinen Konsepti:**
```
     ████████
    ██      ██
   ██  ████  ██     <- "T" muoto
  ██   ████   ██       + 2.0 integroitu
 ██    ████    ██      + euro-symboli sisään
████████████████         rakennettu
```

**Toteutus:**
- 🛠️ Työkalu: Adobe Illustrator + Photoshop (tai Affinity Designer)
- ⏱️ Aika: 16-24 tuntia (ammattilainen)
- 💰 Kustannus: 300-800€ (graafinen suunnittelija)
- 📦 Deliverables:
  - Brand guidelines (PDF)
  - Ikoniperhe (kaikki koot)
  - Vektoritiedostot (.svg, .ai)
  - Rasteri (.png, .ico, .icns)

---

## 🎯 Suositeltu Lähestymistapa

### Vaiheistettu Toteutus

#### Vaihe 1: Tilitin 2.0 Release (Nyt, Tammikuu 2025)
**Toimenpide:** Säilytetään nykyinen ikoni
**Perustelu:**
- ✅ Nykyinen ikoni on riittävän hyvä
- ✅ Fokus GitHub Releaseen (GITHUB-RELEASE-PUUTTUU.md)
- ✅ Käyttäjäpalaute ensin, sitten visuaalinen uudistus

**Kustannus:** 0€
**Aika:** 0 tuntia

---

#### Vaihe 2: Tilitin 2.1 (Kevät 2025)
**Toimenpide:** Vaihtoehto 1 "Evolution" - Kevyt päivitys
**Perustelu:**
- ✅ Pieni parannus visuaaliseen ilmeeseen
- ✅ Ei radikaaleja muutoksia (käyttäjät tunnistavat)
- ✅ Edullinen ja nopea toteuttaa
- ✅ Testaa värimaailmaa ja modernia tyyliä

**Kustannus:** 0-150€
**Aika:** 2-4 tuntia (itse) tai 1-2 viikkoa (freelancer)

**Aikataulu:**
1. Suunnittelu (Maaliskuu 2025)
2. Toteutus (Huhtikuu 2025)
3. Testaus eri resoluutioilla
4. Julkaisu v2.1 mukana

---

#### Vaihe 3: Tilitin 3.0 (Syksy 2025-2026)
**Toimenpide:** Vaihtoehto 2 tai 3 (riippuen budjetista ja tarpeesta)
**Perustelu:**
- ✅ Major version bump → sopiva hetki brändi-uudistukselle
- ✅ Enemmän aikaa suunnitteluun
- ✅ Käyttäjäpalautetta kerätty
- ✅ Mahdollinen Microsoft Store -julkaisu (vaatii 256x256 ikonia)

**Kustannus:** 150-800€
**Aika:** 4-24 tuntia

---

## 📐 Tekniset Vaatimukset

### Windows (.ico)

**Vaaditut Resoluutiot:**
```
16x16   - Taskbar (pienimmät)
24x24   - Small toolbar icons
32x32   - Standard desktop icon
48x48   - Large icons view
64x64   - Extra large icons
128x128 - Jumbo icons (Windows 7+)
256x256 - Windows Vista+ high-DPI
```

**Tiedostomuoto:** .ico (multi-resolution, 32-bit RGBA)

**Luonti:**
```bash
# ImageMagick (ilmainen)
magick convert app-icon-*.png tilitin.ico

# Tai IcoFX (Windows, ilmainen versio)
# Tai GIMP (Export as .ico)
```

---

### macOS (.icns)

**Vaaditut Resoluutiot:**
```
16x16    @1x, 32x32   @2x
32x32    @1x, 64x64   @2x
128x128  @1x, 256x256 @2x
256x256  @1x, 512x512 @2x
512x512  @1x, 1024x1024 @2x
```

**Luonti:**
```bash
mkdir tilitin.iconset
# Kopioi kaikki koot iconset-kansioon
iconutil -c icns tilitin.iconset
```

---

### Microsoft Store (.png)

**Vaaditut Koot:**
```
50x50    - Store listing tile (small)
150x150  - Store listing tile (medium)
300x300  - App tile (large)
```

**Tiedostomuoto:** PNG, 32-bit RGBA, läpinäkyvä tausta

---

### Linux (.svg + .png)

**Suositus:** Vektoritiedosto (.svg) + rasterit

**Koot:**
```
scalable/apps/tilitin.svg
16x16/apps/tilitin.png
22x22/apps/tilitin.png
24x24/apps/tilitin.png
32x32/apps/tilitin.png
48x48/apps/tilitin.png
64x64/apps/tilitin.png
128x128/apps/tilitin.png
256x256/apps/tilitin.png
```

---

## 🎨 Suunnitteluohjeet (Vaihtoehto 1)

### Väripaletti

**Päävärit:**
```css
--primary-dark:  #1E3A8A;  /* Tummansininen */
--primary-light: #3B82F6;  /* Sininen */
--accent-teal:   #14B8A6;  /* Turkoosi */
--accent-gold:   #F59E0B;  /* Kulta */
--neutral-100:   #F3F4F6;  /* Vaalea harmaa */
--neutral-800:   #1F2937;  /* Tumma harmaa */
```

**Gradientit:**
```css
/* Lomake */
background: linear-gradient(135deg, #3B82F6 0%, #1E3A8A 100%);

/* Kynä */
background: linear-gradient(90deg, #14B8A6 0%, #0D9488 100%);

/* Varjo */
box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
```

---

### Mittasuhteet (256x256 pohja)

```
Lomake:
- Leveys: 180px (70%)
- Korkeus: 220px (86%)
- Border-radius: 8px
- Varjon blur: 12px

Kynä:
- Pituus: 120px
- Leveys: 20px
- Kulma: 45°
- Sijainti: Oikea alareuna

Euro-symboli:
- Koko: 24px
- Sijainti: Oikea yläkulma
- Väri: --accent-gold

Grid/viivat:
- 3-4 vaakaviivaa
- Väri: rgba(255,255,255,0.2)
- Line-width: 2px
```

---

## 🛠️ Toteutustyökalut

### Ilmaiset Työkalut

**1. Inkscape** (Vektorigrafiikka)
- URL: https://inkscape.org/
- Hyvä: SVG-tuki, ilmainen, cross-platform
- Huono: Hieman hankala oppia

**2. GIMP** (Kuvanmuokkaus)
- URL: https://www.gimp.org/
- Hyvä: Tehokas, ilmainen, .ico-tuki
- Huono: Ei vektorituki

**3. Figma** (UI-design, Web-pohjainen)
- URL: https://figma.com/
- Hyvä: Moderni, helppo, ilmainen peruskäyttö
- Huono: Vaatii internet-yhteyden

**4. Photopea** (Photoshop-klooni, Web)
- URL: https://www.photopea.com/
- Hyvä: Photoshop-yhteensopiva, selaimessa
- Huono: Mainoksia ilmaisversiossa

---

### Kaupalliset Työkalut

**1. Adobe Illustrator** (Vektorigrafiikka)
- Hinta: 24.39€/kk (Creative Cloud)
- Hyvä: Alan standardi, tehokas
- Käyttö: Jos jo tilaus olemassa

**2. Affinity Designer** (Vektorigrafiikka)
- Hinta: 74.99€ (kertamaksu)
- Hyvä: Edullinen, tehokas
- Suositus: Jos ei Adobe-tilausta

**3. Sketch** (macOS, UI-design)
- Hinta: 9$/kk
- Hyvä: Hyvä ikonisuunnitteluun
- Huono: Vain macOS

---

## 📋 Toimenpidelista (Vaihtoehto 1)

### Valmistelu
- [ ] Tutustu nykyiseen ikoniin (app-*.png)
- [ ] Lataa Figma tai Inkscape
- [ ] Luo uusi projekti (256x256 canvas)
- [ ] Määrittele väripaletti

### Suunnittelu
- [ ] Piirrä lomakkeen pohja (pyöristetyt kulmat)
- [ ] Lisää gradientti (tummansininen → sininen)
- [ ] Piirrä grid-viivat lomakkeeseen
- [ ] Lisää varjo lomakkeen alle
- [ ] Piirrä kynä (turkoosi gradientti)
- [ ] Lisää euro-symboli (€) oikeaan yläkulmaan
- [ ] Lisää hienovarainen "glow" kynän kärkeen

### Optimointi
- [ ] Exporttaa 256x256 PNG (32-bit RGBA)
- [ ] Skalaa 128x128, 64x64, 48x48, 32x32, 16x16
- [ ] Tarkista että 16x16 on vielä tunnistettava
- [ ] Luo multi-resolution .ico
- [ ] Testaa Windows Explorerissa

### Testaus
- [ ] Korvaa `src/main/resources/tilitin.ico`
- [ ] Korvaa `src/main/resources/app-*.png`
- [ ] Buildaa: `mvn clean package`
- [ ] Buildaa: `build-windows.bat`
- [ ] Testaa .exe taskbaarissa
- [ ] Testaa MSI/Inno Setup installer

### Dokumentaatio
- [ ] Ota kuvakaappaukset (ennen/jälkeen)
- [ ] Päivitä CHANGELOG.md
- [ ] Lisää ikoni-tiedostot git-repoon
- [ ] Commitoi: "feat: Modernisoidut sovellusikonit v2.1"

---

## 🖼️ Vertailukuva (Konsepti)

### Ennen (Nykyinen)
```
  ┌─────────┐
  │ ≡≡≡≡≡≡≡ │
  │ ≡≡≡≡≡≡≡ │   ✏️ <- Yksinkertainen kynä
  │ ≡≡≡≡≡≡≡ │
  └─────────┘
```
- Litteä, perus-sininen
- Ei deptia
- Yleinen

### Jälkeen (Vaihtoehto 1)
```
    ┌──────────┐ €
    │ ▓▓▓▓▓▓▓▓ │
    │ ░░░░░░░░ │   ✨🖊️ <- Modernimpi kynä + glow
    │ ░░░░░░░░ │
    └──────────┘
      ▒▒▒▒▒▒▒    <- Varjo
```
- Gradientti (tummansininen → sininen)
- Pyöristetyt kulmat
- Depth (varjo)
- Euro-symboli (€)
- Glow-efekti

---

## 💰 Kustannusarvio

### Vaihtoehto 1: Evolution (DIY)
| Työvaihe | Aika | Kustannus |
|----------|------|-----------|
| Suunnittelu | 1h | 0€ |
| Toteutus Figmalla | 2h | 0€ |
| Optimointi & Export | 1h | 0€ |
| Testaus | 0.5h | 0€ |
| **YHTEENSÄ** | **4.5h** | **0€** |

### Vaihtoehto 1: Evolution (Freelancer)
| Työvaihe | Aika | Kustannus |
|----------|------|-----------|
| Briefi | 0.5h | - |
| Suunnittelu | 2h | 50-100€ |
| Revisiot (2 kierrosta) | 1h | 25-50€ |
| Finalisiointi | 1h | 25-50€ |
| **YHTEENSÄ** | **4.5h** | **100-200€** |

**Freelancer-alustat:**
- Fiverr: 50-150€
- Upwork: 100-300€
- 99designs: 200-500€ (kilpailu)

---

### Vaihtoehto 3: Brand Identity (Ammattilainen)
| Työvaihe | Aika | Kustannus |
|----------|------|-----------|
| Brändistrategia | 4h | 200-400€ |
| Logo-konseptit (3 vaihtoehtoa) | 8h | 400-800€ |
| Ikoniperhe | 4h | 200-400€ |
| Brand guidelines | 2h | 100-200€ |
| Revisiot | 2h | 100-200€ |
| **YHTEENSÄ** | **20h** | **1000-2000€** |

**Suomalaisia graafisia suunnittelijoita:**
- Tuntihinta: 50-100€/h
- Projekti: 500-2000€ (riippuen laajuudesta)

---

## 📅 Aikataulu (Suositeltu)

### Q1 2025 (Nyt)
- ✅ Tilitin 2.0.0 Release (nykyisellä ikonilla)
- ✅ Käyttäjäpalautteen kerääminen

### Q2 2025 (Huhtikuu-Kesäkuu)
- 🎨 Vaihtoehto 1 "Evolution" suunnittelu
- 🧪 Betatestaus uudella ikonilla
- 🚀 Tilitin 2.1 Release (modernisoitu ikoni)

### Q3-Q4 2025 (Syys-Joulukuu)
- 📊 Analysointi: Toimiiko uusi ikoni?
- 🤔 Päätös: Jatketaanko Vaihtoehto 2/3:een?

### 2026 (Jos tarve)
- 🎨 Vaihtoehto 2 tai 3 toteutus
- 🚀 Tilitin 3.0 Release (täysi brändi-uudistus)

---

## 🎯 Suositukset

### Lyhyellä Aikavälillä (2025 Q1)
**Toimenpide:** Säilytetään nykyinen ikoni
**Perustelu:**
1. Fokus GitHub Release -julkaisuun
2. Nykyinen ikoni on riittävän hyvä
3. Käyttäjäpalaute ensin

**Kustannus:** 0€
**Riski:** Matala

---

### Keskipitkällä Aikavälillä (2025 Q2)
**Toimenpide:** Vaihtoehto 1 "Evolution"
**Perustelu:**
1. Pieni mutta näkyvä parannus
2. Edullinen (DIY tai 100-200€)
3. Käyttäjät tunnistavat sovelluksen

**Kustannus:** 0-200€
**Riski:** Matala

---

### Pitkällä Aikavälillä (2026)
**Toimenpide:** Harkitse Vaihtoehto 2 tai 3
**Perustelu:**
1. Microsoft Store -julkaisu (vaatii parempia kuvia)
2. Kilpailu kasvaa → brändi tärkeämpi
3. Budjetti kasvanut

**Kustannus:** 300-2000€
**Riski:** Keskisuuri (vaatii huolellista suunnittelua)

---

## 📚 Lisäresurssit

### Ikonisuunnittelun Periaatteet
- **The Icon Handbook** (gratis PDF): https://iconhandbook.co.uk/
- **Material Design Icons**: https://material.io/design/iconography
- **Human Interface Guidelines** (Apple): https://developer.apple.com/design/human-interface-guidelines/app-icons

### Inspiraatio
- **Dribbble** (ikonisuunnittelu): https://dribbble.com/tags/app-icon
- **Behance** (portfolio): https://www.behance.net/search/projects?search=app+icon
- **IconJar** (ikonikirjasto): https://geticonjar.com/

### Työkalut
- **RealFaviconGenerator** (testaa ikoneita): https://realfavicongenerator.net/
- **App Icon Generator** (iOS/Android): https://appicon.co/
- **ICO Convert** (.ico-luonti): https://icoconvert.com/

---

## ✅ Yhteenveto

**Nykyinen tilanne:**
- Ikoni on toimiva mutta ei muistumiinarvoinen (7/10)
- Sopii nykyiseen FlatLaf-teemaan
- Riittävän hyvä v2.0 -releaseen

**Suositus:**
1. **Nyt (2025 Q1):** Säilytä nykyinen ikoni, keskity releaseen
2. **Kevät 2025 (v2.1):** Vaihtoehto 1 "Evolution" (0-200€)
3. **Myöhemmin (v3.0):** Harkitse täyttä brändi-uudistusta

**Seuraavat askeleet:**
1. Julkaise Tilitin 2.0 (nykyisellä ikonilla)
2. Kerää käyttäjäpalautetta
3. Suunnittele Vaihtoehto 1 keväällä 2025
4. Testaa betatestauksen kautta
5. Julkaise v2.1 modernisoidulla ikonilla

---

**Dokumentti luotu:** 28.12.2025
**Versio:** 1.0
**Tekijä:** Tilitin Project
**Status:** Suunnitelma (ei vielä toteutettu)
