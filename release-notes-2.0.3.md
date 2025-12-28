## 💾 Backup System Release

### Lataukset

| Tiedosto | Kuvaus |
|----------|--------|
| `Tilitin-2.0.3-setup.exe` | ⭐ Suositus! Moderni asennusohjelma |
| `tilitin-2.0.3.jar` | JAR (vaatii Java 25+) |

### Uutta versiossa 2.0.3

#### 💾 Moderni varmuuskopiointijärjestelmä
- **Per-tietokanta sijainnit:** Jokaiselle tietokannalle omat backup-sijainnit
- **Automaattinen pilvipalvelutunnistus:** Google Drive, OneDrive, Dropbox, iCloud
- **USB-asemien tunnistus:** Irrotettavat asemat tunnistetaan automaattisesti
- **AutoBackup:** Word-tyylinen automaattinen varmuuskopiointi (1-60 min välein)
- **Manuaalinen "Tee nyt":** Varmuuskopioi valitut tietokannat heti kaikkiin sijainteihin

#### 🎛️ Uusi käyttöliittymä
- **Varmuuskopiointiasetukset-dialogi:** Hallitse kaikkia tietokantoja yhdestä paikasta
- **Sijainnit-nappi:** Lisää pilvi-, USB- tai paikallisia sijainteja per tietokanta
- **Sijaintien määrä näkyvissä:** Näe heti montako sijaintia kullakin tietokannalla on

#### 🔧 Parannukset
- Poistettu globaali backup-kansio - nyt vain per-tietokanta sijainnit
- Varmuuskopiot sisältävät tunnisteen (hash) polusta - ei sekoitu vaikka sama tiedostonimi
- Siivotaan automaattisesti vanhat kopiot (säilytä 1-100 versiota)

### Käyttö

1. **Valikko:** Tiedosto → Varmuuskopiointiasetukset
2. **Valitse tietokannat:** Merkitse varmuuskopioitavat tietokannat
3. **Lisää sijainnit:** Klikkaa "Sijainnit..." ja lisää pilvi/USB/kansio
4. **AutoBackup:** Ota käyttöön automaattinen varmuuskopiointi haluamallasi välillä
5. **Tee nyt:** Varmuuskopioi heti kaikki valitut kaikkiin sijainteihin

### Vaatimukset
- Windows 10/11 (64-bit)
- JAR-versio vaatii Java 25+
