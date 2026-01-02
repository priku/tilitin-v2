# Manuaalinen testaus - Checklist

**Päivämäärä:** 2026-01-02  
**Tarkoitus:** Systemaattinen manuaalinen testaus ennen releasea

---

## 🚀 Aloitus

### 1. Sovelluksen käynnistys
- [ ] Käynnistä sovellus: `./gradlew run`
- [ ] Pääikkuna avautuu ilman virheitä
- [ ] Konsolissa ei virheitä tai varoituksia
- [ ] Muistinkäyttö on kohtuullinen

---

## 📁 Tiedosto-toiminnot

### 2. Uuden tietokannan luonti
- [ ] Tiedosto → Uusi tietokanta
- [ ] Tietokantatiedoston valinta toimii
- [ ] Tilikarttamallin valinta toimii
- [ ] Tietokanta luodaan onnistuneesti
- [ ] Pääikkuna päivittyy uudelle tietokannalle

### 3. Tietokannan avaus
- [ ] Tiedosto → Avaa tietokanta
- [ ] Tiedostonvalinta toimii
- [ ] Olemassa oleva tietokanta avautuu
- [ ] Data näkyy oikein

### 4. Tietokannan sulkeminen
- [ ] Tiedosto → Sulje tietokanta
- [ ] Tietokanta sulkeutuu
- [ ] Pääikkuna tyhjenee

---

## 📄 Tosite-toiminnot

### 5. Uuden tositteen luonti
- [ ] Uusi tosite -nappi toimii
- [ ] Tosite luodaan oikealla numerolla
- [ ] Päivämäärä on oikein (tänään)
- [ ] Tosite on tyhjä (ei vientejä)

### 6. Tosite-tietojen muokkaus
- [ ] Päivämäärän muokkaus toimii
- [ ] Tositetyypin valinta toimii
- [ ] Tositetyypit näkyvät oikein
- [ ] Tallenna-nappi toimii

### 7. Vientien lisääminen
- [ ] Lisää vienti -nappi toimii
- [ ] Uusi tyhjä rivi lisätään
- [ ] Tilin valinta toimii (AccountSelectionDialogFX)
- [ ] Tilin tiedot täyttyvät automaattisesti
- [ ] Kuvaus-kenttä toimii
- [ ] Summa-kenttä toimii
- [ ] Debet/Credit-vaihto toimii (*-näppäin)

### 8. Vientien poistaminen
- [ ] Poista vienti -nappi toimii
- [ ] Valittu rivi poistetaan
- [ ] Tosite tallentuu automaattisesti

### 9. Tosite-navigointi
- [ ] Edellinen tosite -nappi toimii
- [ ] Seuraava tosite -nappi toimii
- [ ] Ensimmäinen tosite -nappi toimii
- [ ] Viimeinen tosite -nappi toimii
- [ ] Siirry tositteeseen -toiminto toimii

### 10. Tosite-tulostus
- [ ] Tulosta -nappi toimii
- [ ] Tulostusdialogi avautuu
- [ ] Esikatselu toimii
- [ ] Tulostus toimii

---

## 🎨 Dialogit

### 11. Asetukset-dialogit
- [ ] Tiedosto → Asetukset → Asetukset
  - [ ] Dialogi avautuu
  - [ ] Kaikki välilehdet toimivat
  - [ ] Tallenna toimii
  - [ ] Peruuta toimii

- [ ] Tiedosto → Asetukset → Ulkoasu
  - [ ] Teeman vaihto toimii (tumma/vaalea)
  - [ ] Fonttikoon muutos toimii
  - [ ] Esikatselu päivittyy
  - [ ] Tallenna toimii

- [ ] Tiedosto → Asetukset → Pikanäppäimet
  - [ ] Dialogi avautuu
  - [ ] Kaikki pikanäppäimet näkyvät
  - [ ] Kategoriat toimivat

- [ ] Tiedosto → Asetukset → Tulostusasetukset
  - [ ] Dialogi avautuu
  - [ ] Asetusten muokkaus toimii
  - [ ] Tallenna toimii

### 12. Muokkaa-valikko dialogit
- [ ] Muokkaa → Tilikartta
  - [ ] COADialogFX avautuu
  - [ ] Tilit näkyvät
  - [ ] Tilin lisääminen toimii
  - [ ] Tilin muokkaus toimii
  - [ ] Tilin poistaminen toimii

- [ ] Muokkaa → Tositetyypit
  - [ ] DocumentTypeDialogFX avautuu
  - [ ] Tositetyypit näkyvät
  - [ ] Tositetyypin lisääminen toimii
  - [ ] Tositetyypin muokkaus toimii
  - [ ] Tositetyypin poistaminen toimii

- [ ] Muokkaa → Vientipohjat
  - [ ] EntryTemplateDialogFX avautuu
  - [ ] Vientipohjat näkyvät
  - [ ] Vientipohjan lisääminen toimii
  - [ ] Vientipohjan muokkaus toimii
  - [ ] Vientipohjan poistaminen toimii

- [ ] Muokkaa → Alkusaldot
  - [ ] StartingBalanceDialogFX avautuu
  - [ ] Alkusaldot näkyvät
  - [ ] Saldojen muokkaus toimii
  - [ ] Tallenna toimii

### 13. Raportti-dialogit
- [ ] Tulosteet → Tilin saldo
  - [ ] AccountSummaryOptionsDialogFX avautuu
  - [ ] Asetusten valinta toimii
  - [ ] Raportin generointi toimii

- [ ] Tulosteet → Tuloslaskelma
  - [ ] FinancialStatementOptionsDialogFX avautuu
  - [ ] Asetusten valinta toimii
  - [ ] Raportin generointi toimii

- [ ] Tulosteet → Tase
  - [ ] FinancialStatementOptionsDialogFX avautuu (Balance)
  - [ ] Asetusten valinta toimii
  - [ ] Raportin generointi toimii

- [ ] Tulosteet → ALV-raportti
  - [ ] VATReportDialogFX avautuu
  - [ ] Asetusten valinta toimii
  - [ ] Raportin generointi toimii

- [ ] Tulosteet → Muokkaa raportteja
  - [ ] ReportEditorDialogFX avautuu
  - [ ] Raporttien muokkaus toimii

### 14. Työkalut-dialogit
- [ ] Työkalut → CSV-tuonti
  - [ ] CSVImportDialog avautuu
  - [ ] Tiedoston valinta toimii
  - [ ] Tuonti toimii
  - [ ] Tuodut viennit näkyvät

- [ ] Työkalut → Tiedon vienti
  - [ ] DataExportDialogFX avautuu
  - [ ] Vientiasetukset toimivat
  - [ ] Vienti toimii

- [ ] Työkalut → Varmuuskopio
  - [ ] BackupSettingsDialogFX avautuu
  - [ ] Varmuuskopion luonti toimii

- [ ] Työkalut → Palauta varmuuskopiosta
  - [ ] RestoreBackupDialogFX avautuu
  - [ ] Varmuuskopion valinta toimii
  - [ ] Palautus toimii

### 15. Ohje-dialogit
- [ ] Ohje → Ohje
  - [ ] HelpDialogFX avautuu
  - [ ] Sisältö näkyy oikein

- [ ] Ohje → Tietoja
  - [ ] AboutDialogFX avautuu
  - [ ] Versiotiedot näkyvät

- [ ] Ohje → Debug-tiedot
  - [ ] DebugInfoDialogFX avautuu
  - [ ] Tiedot näkyvät
  - [ ] Kopioi leikepöydälle toimii

---

## ⌨️ Pikanäppäimet

### 16. Pikanäppäinten testaus
- [ ] Ctrl+N - Uusi tosite
- [ ] Ctrl+S - Tallenna
- [ ] Ctrl+P - Tulosta
- [ ] Ctrl+O - Avaa tietokanta
- [ ] Ctrl+W - Sulje tietokanta
- [ ] F1 - Ohje
- [ ] Tab - Siirry seuraavaan kenttään
- [ ] Enter - Siirry seuraavaan riviin
- [ ] * - Vaihda debet/credit

---

## 📊 Entry Table UX

### 17. Tab-navigointi
- [ ] Tab siirtyy oikeaan järjestykseen
- [ ] Shift+Tab siirtyy taaksepäin
- [ ] Navigointi toimii kaikissa kentissä

### 18. Automaattinen täydennys
- [ ] Tilin valinta täyttää automaattisesti tiedot
- [ ] Kuvauksen automaattinen täydennys toimii
- [ ] Aikaisemmat kuvaukset näkyvät

### 19. Debet/Credit-vaihto
- [ ] *-näppäin vaihtaa debet/credit
- [ ] Vaihto näkyy visuaalisesti
- [ ] Summa säilyy

---

## 📎 PDF-liitteet

### 20. Liitteiden hallinta
- [ ] Liitteet-nappi toimii
- [ ] AttachmentsDialogFX avautuu
- [ ] PDF-tiedoston lisääminen toimii
- [ ] Liitteen poistaminen toimii
- [ ] Liitteen avaaminen toimii

---

## 🔍 Haku

### 21. Tosite-haku
- [ ] Haku-kenttä toimii
- [ ] Haku löytää tositteet
- [ ] Haku päivittyy reaaliajassa

---

## 💾 Tietokantatoiminnot

### 22. Tietokannan päivitys
- [ ] Vanha tietokanta päivittyy automaattisesti
- [ ] Päivitys ilmoitetaan käyttäjälle
- [ ] Päivitys onnistuu ilman virheitä

### 23. Varmuuskopiointi
- [ ] Automaattinen varmuuskopiointi toimii
- [ ] Manuaalinen varmuuskopiointi toimii
- [ ] Varmuuskopiot löytyvät oikeasta paikasta

---

## 🐛 Tunnetut ongelmat

### Testaa että seuraavat ongelmat on korjattu:
- [ ] Ei NullPointerExceptioneja
- [ ] Ei muistivuotoja
- [ ] Dialogit avautuvat oikein
- [ ] Tietokanta tallentuu oikein
- [ ] Ei konsolivirheitä

---

## ✅ Testauksen valmistuminen

### 24. Lopullinen tarkistus
- [ ] Kaikki yllä olevat kohdat testattu
- [ ] Löydetyt bugit dokumentoitu
- [ ] Kriittiset bugit korjattu
- [ ] Sovellus on vakaa

---

## 📝 Testausraportti

**Testaaja:** _________________  
**Päivämäärä:** _________________  
**Versio:** 2.2.0  
**Alusta:** Windows / macOS / Linux  

### Yhteenveto
- **Testattuja toimintoja:** ___ / 24
- **Löydettyjä bugeja:** ___
- **Kriittisiä bugeja:** ___
- **Keskisuuria bugeja:** ___
- **Pieniä bugeja:** ___

### Löydetyt bugit
1. 
2. 
3. 

### Suositus
- [ ] ✅ Valmis releasea varten
- [ ] ⚠️ Tarvitsee korjauksia
- [ ] ❌ Ei valmis releasea varten

---

**Huom:** Tämä on kattava checklist. Testaa kaikki kohdat ennen releasea!
