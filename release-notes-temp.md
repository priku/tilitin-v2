### 🚀 Kotlin DAO Integration + Code Modernization

**Branch:** `feature/2.1-documentframe-refactor`

### Lisätty
- **Kotlin DAO tuotantokäytössä** - Ensimmäinen Kotlin DAO integroitu
  - `SQLiteAccountDAOKt` korvaa Java-toteutuksen
  - Täysi yhteensopivuus olemassa olevan koodin kanssa
- **UIConstants teemavärit** - Uudet värifunktiot
  - `getSuccessColor()` - Vihreä onnistumisille
  - `getInfoColor()` - Sininen informaatiolle
  - `getErrorColor()` - Punainen virheille
  - `getWarningColor()` - Oranssi varoituksille
  - `getMutedColor()` - Harmaa deaktivoiduille

### Muutettu
- **DocumentFrame.java** refaktoroitu (-698 riviä)
  - 26 ActionListener → lambda-lausekkeet
  - Tiedosto: 3856 → 3158 riviä (-18%)
- **Backup-indikaattori** käyttää teemavärejä
  - Mukautuu automaattisesti dark/light modeen

### Poistettu
- **KotlinDemo.java** - Kehitystyökalu poistettu

### Tekninen
- GitHub Actions: Automaattinen release notes CHANGELOG.md:stä
- Java-tiedostot: 191 → 190
- Kotlin-tiedostot: 12 (6% koodista)
