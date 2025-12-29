# CSV-tuonnin testitiedostot

Tässä kansiossa on esimerkkitiedostoja CSV-tuonnin testaamiseen.

## Tiedostot

### 1. nordea-tiliote.csv
- **Pankki:** Nordea
- **Erotin:** `,` (pilkku)
- **Desimaalimerkki:** `.` (piste)
- **Päivämäärä:** `dd.MM.yyyy` (01.12.2024)
- **Rivejä:** 10 tilitapahtumaa
- **Sarakkeet:**
  - Kirjauspäivä, Arvopäivä, Määrä, Valuutta
  - Saaja/Maksaja, Tilinumero, BIC
  - Viesti, Viite, Tapahtumatyyppi

### 2. op-tiliote.csv
- **Pankki:** OP
- **Erotin:** `;` (puolipiste)
- **Desimaalimerkki:** `,` (pilkku)
- **Päivämäärä:** `dd.MM.yyyy` (02.12.2024)
- **Rivejä:** 13 tilitapahtumaa
- **Sarakkeet:**
  - Pvm, Arkistointitunnus, Saaja/Maksaja
  - Selitys, Viite, Määrä EUR

### 3. procountor-tiliote.csv
- **Järjestelmä:** Procountor
- **Erotin:** `;` (puolipiste)
- **Desimaalimerkki:** `,` (pilkku)
- **Päivämäärä:** `yyyy-MM-dd` (2024-12-15)
- **Rivejä:** 5 tilitapahtumaa
- **Sarakkeet (15 kpl):**
  - Tyyppi, Numero, Päivämäärä, Arvopäivä, Maksuväline
  - Summa, Vastaanottaja, Nimi, Viesti
  - Viite, Arkistointitunnus, Tila, ALV
  - Tilinumero, IBAN
- **Erikoisuus:** Automaattinen Procountor-tunnistus aktivoituu!

### 4. tiliote-2024-12.csv
- **Geneerinen** testitiedosto
- **Erotin:** `;` (puolipiste)
- **Desimaalimerkki:** `,` (pilkku)
- **Rivejä:** 13 tilitapahtumaa

## Käyttö

1. **Avaa Tilitin** (versio 2.2.1+)
2. **Valitse:** Työkalut → Tuo CSV-tiedostosta… (Ctrl+I)
3. **Valitse** jokin yllä olevista tiedostoista
4. **Tarkista:**
   - Procountor-tiedostossa näkyy "Procountor-muoto tunnistettu"
   - Esikatselu näyttää 10 ensimmäistä riviä
   - Sarakkeiden mappaukset ehdotetaan automaattisesti
5. **Muokkaa** mappauksia tarvittaessa pudotusvalikoista
6. **Paina:** "Tuo kirjaukset"

## Automaattinen tunnistus

Tilitin tunnistaa automaattisesti:
- **Koodaus:** UTF-8, ISO-8859-1, Windows-1252
- **Erotin:** `,` (pilkku), `;` (puolipiste), `\t` (sarkain)
- **Saraketyypit:** Päivämäärä, Raha, Teksti, IBAN, Viite, Tilinumero
- **Procountor-formaatti:** Erikoislogiikat aktivoituvat automaattisesti

## Huomioita

- **Tilinumerot:** Varmista että CSV:ssä mainitut tilinumerot löytyvät tilikarrastasi
- **Päivämäärät:** Tuonti onnistuu vain jos päivämäärät ovat nykyisellä tilikaudella
- **Desimaalimerkki:** Tilitin tunnistaa sekä pilkun (`,`) että pisteen (`.`)
- **Negatiiviset summat:** Tulkitaan kredit-kirjauksiksi (tulot)
- **Positiiviset summat:** Tulkitaan debet-kirjauksiksi (menot)

## Testaus

Nämä tiedostot ovat tarkoitettu:
- CSV-tuonnin toiminnallisuuden testaamiseen
- Automaattisen tunnistuksen varmistamiseen
- Eri pankkiformaattien yhteensopivuuden tarkistukseen
- Kehittäjille: regressiotestaukseen
