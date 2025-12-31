# Puuttuvat Toiminnallisuudet - JavaFX-versio

## 📊 Yleiskuvaus

**Toteutettu:** 26 handleria (60%)  
**Puuttuu:** 17 handleria (40%)  
**Puuttuvia dialogeja:** 25 dialogia

## ✅ Toteutetut Toiminnallisuudet

### Perustoiminnot
- ✅ Tietokannan avaus/tallennus
- ✅ Tositteiden luonti/poisto/muokkaus
- ✅ Vientien lisääminen/poisto/kopiointi/liittäminen
- ✅ Dokumenttien navigointi (edellinen/seuraava/ensimmäinen/viimeinen)
- ✅ Haku tositteista
- ✅ Tositelajien valinta
- ✅ Liitteiden hallinta
- ✅ Tulostaminen

### Dialogit (8/33)
- ✅ SettingsDialogFX - Asetukset
- ✅ HelpDialogFX - Ohje
- ✅ AttachmentsDialogFX - Liitteet
- ✅ ReportDialogFX - Perusraportit (Päiväkirja, Pääkirja, Tuloslaskelma, Tase)
- ✅ DocumentTypeDialogFX - Tositelajit
- ✅ COADialogFX - Tilikartta
- ✅ AccountSelectionDialogFX - Tilinvalinta
- ✅ EntryTemplateDialogFX - Vientimallit (juuri toteutettu)

## ❌ Puuttuvat Toiminnallisuudet

### 1. Muokkaa-valikko (3/7)

#### ✅ Toteutettu
- Kopioi / Liitä
- Uusi tosite / Poista tosite
- Lisää vienti / Poista vienti
- Tilikartta / Perustiedot / Asetukset / Ulkoasu

#### ❌ Puuttuu
1. **Vientimallin luominen tositteesta** (`handleCreateEntryTemplate`)
   - **Tila:** Placeholder
   - **Vaativuus:** Keski
   - **Arvio:** 2-4 tuntia
   - **Kuvaus:** Luo vientimallin nykyisestä tositteesta. Vaatii DocumentModel-integraation.

2. **Alkusaldot** (`handleStartingBalances`)
   - **Tila:** Placeholder
   - **Vaativuus:** Korkea
   - **Arvio:** 4-6 tuntia
   - **Kuvaus:** Alkusaldojen muokkausdialogi. Tärkeä perustoiminto.
   - **Puuttuva dialogi:** StartingBalanceDialogFX

3. **ALV-merkintä** (`handleVatDocument`)
   - **Tila:** Placeholder
   - **Vaativuus:** Matala
   - **Arvio:** 2-3 tuntia
   - **Kuvaus:** ALV-tilien päättäminen. Vaatii VAT-laskentalogiikan.

### 2. Tiedosto-valikko (2/6)

#### ❌ Puuttuu
1. **Varmuuskopiointi** (`handleBackupSettings`)
   - **Tila:** Placeholder
   - **Vaativuus:** Keski
   - **Arvio:** 4-6 tuntia
   - **Kuvaus:** Varmuuskopiointiasetusten muokkaus.
   - **Puuttuva dialogi:** BackupSettingsDialogFX
   - **Huomio:** BackupService on olemassa, tarvitaan vain UI.

2. **Palauta varmuuskopiosta** (`handleRestoreBackup`)
   - **Tila:** Placeholder
   - **Vaativuus:** Keski
   - **Arvio:** 3-5 tuntia
   - **Kuvaus:** Varmuuskopion palautusdialogi.
   - **Puuttuva dialogi:** RestoreBackupDialogFX
   - **Huomio:** RestoreBackupDialog on olemassa Swing-versiossa.

### 3. Tulosteet-valikko (8/12)

#### ✅ Toteutettu
- Päiväkirja
- Pääkirja
- Tuloslaskelma
- Tase
- Tosite (tulostus)

#### ❌ Puuttuu
1. **Tilien saldot** (`handleAccountSummary`)
   - **Tila:** Placeholder
   - **Vaativuus:** Matala
   - **Arvio:** 3-5 tuntia
   - **Puuttuva dialogi:** AccountSummaryOptionsDialogFX

2. **Tiliote** (`handleAccountStatement`)
   - **Tila:** Placeholder
   - **Vaativuus:** Matala
   - **Arvio:** 3-5 tuntia
   - **Puuttuva dialogi:** AccountStatementOptionsDialogFX

3. **Tuloslaskelma erittelyin** (`handleIncomeStatementDetailed`)
   - **Tila:** Placeholder
   - **Vaativuus:** Matala
   - **Arvio:** 2-4 tuntia
   - **Puuttuva dialogi:** FinancialStatementOptionsDialogFX

4. **Tase erittelyin** (`handleBalanceSheetDetailed`)
   - **Tila:** Placeholder
   - **Vaativuus:** Matala
   - **Arvio:** 2-4 tuntia
   - **Puuttuva dialogi:** FinancialStatementOptionsDialogFX (sama kuin yllä)

5. **ALV-laskelma tileittäin** (`handleVatReport`)
   - **Tila:** Placeholder
   - **Vaativuus:** Matala
   - **Arvio:** 3-5 tuntia
   - **Puuttuva dialogi:** VATReportDialogFX

6. **Tilikartta - Kaikki tilit** (`handleCoa0`)
   - **Tila:** Placeholder
   - **Vaativuus:** Matala
   - **Arvio:** 1-2 tuntia
   - **Puuttuva dialogi:** COAReportDialogFX

7. **Tilikartta - Vain käytössä olevat tilit** (`handleCoa1`)
   - **Tila:** Placeholder
   - **Vaativuus:** Matala
   - **Arvio:** 1-2 tuntia
   - **Puuttuva dialogi:** COAReportDialogFX

8. **Tilikartta - Vain suosikkitilit** (`handleCoa2`)
   - **Tila:** Placeholder
   - **Vaativuus:** Matala
   - **Arvio:** 1-2 tuntia
   - **Puuttuva dialogi:** COAReportDialogFX

9. **Raporttien muokkaus** (`handleEditReports`)
   - **Tila:** Placeholder
   - **Vaativuus:** Matala
   - **Arvio:** 4-6 tuntia
   - **Puuttuva dialogi:** ReportEditorDialogFX

### 4. Työkalut-valikko (6/7)

#### ❌ Puuttuu
1. **Ohita vienti ALV-laskelmassa** (`handleSetIgnoreFlag`)
   - **Tila:** Placeholder
   - **Vaativuus:** Matala
   - **Arvio:** 1-2 tuntia
   - **Kuvaus:** Inline-toiminto, ei tarvitse dialogia. Vaatii TableView-valinnan käsittelyn.

2. **Tilien saldojen vertailu** (`handleBalanceComparison`)
   - **Tila:** Placeholder
   - **Vaativuus:** Matala
   - **Arvio:** 4-6 tuntia
   - **Puuttuva dialogi:** BalanceComparisonDialogFX

3. **Muuta tositenumeroita** (`handleNumberShift`)
   - **Tila:** Placeholder
   - **Vaativuus:** Matala
   - **Arvio:** 2-4 tuntia
   - **Puuttuva dialogi:** DocumentNumberShiftDialogFX

4. **ALV-kantojen muutokset** (`handleVatChange`)
   - **Tila:** Placeholder
   - **Vaativuus:** Matala
   - **Arvio:** 3-5 tuntia
   - **Puuttuva dialogi:** VATChangeDialogFX

5. **Vie tiedostoon** (`handleExport`)
   - **Tila:** Placeholder
   - **Vaativuus:** Matala
   - **Arvio:** 2-3 tuntia
   - **Kuvaus:** CSV-vienti. DocumentExporter on olemassa, tarvitaan vain UI.

6. **Tuo CSV-tiedostosta** (`handleCsvImport`)
   - **Tila:** Placeholder
   - **Vaativuus:** Matala
   - **Arvio:** 4-6 tuntia
   - **Puuttuva dialogi:** CSVImportDialogFX

### 5. Ohje-valikko (1/3)

#### ❌ Puuttuu
1. **Virheenjäljitystietoja** (`handleDebug`)
   - **Tila:** Placeholder
   - **Vaativuus:** Matala
   - **Arvio:** 1-2 tuntia
   - **Kuvaus:** Näyttää lokitiedoston. Yksinkertainen toiminto.

### 6. Tositelaji-valikko

#### ⚠️ Osittain toteutettu
- **Tositelajit dynaamisesti:** Valikko on olemassa, mutta tarvitsee täydentämisen
- **Muokkaa:** `handleDocumentTypes()` on toteutettu, mutta valikon täyttö puuttuu

### 7. Muut UI-komponentit

#### ⚠️ Osittain toteutettu
1. **Vientimallit-alivalikko**
   - **Tila:** Valikko on olemassa, mutta vientimallien listaaminen puuttuu
   - **Vaativuus:** Keski
   - **Arvio:** 2-3 tuntia
   - **Kuvaus:** Vientimallien listaaminen alivalikossa Alt+1-9 -pikanäppäimillä.

2. **Tositelajit-alivalikko**
   - **Tila:** Valikko on olemassa, mutta tositelajien listaaminen puuttuu
   - **Vaativuus:** Keski
   - **Arvio:** 2-3 tuntia
   - **Kuvaus:** Tositelajien listaaminen alivalikossa.

## 📋 Priorisointi

### 🔴 Korkea prioriteetti (Kriittiset perustoiminnot)
1. **StartingBalanceDialogFX** - Alkusaldot
   - **Syy:** Tärkeä perustoiminto
   - **Arvio:** 4-6 tuntia
   - **Status:** ⏳ Seuraavaksi

2. **Vientimallin luominen tositteesta** (`handleCreateEntryTemplate`)
   - **Syy:** Hyödyllinen toiminto
   - **Arvio:** 2-4 tuntia
   - **Status:** ⏳ Vaatii DocumentModel-integraation

### 🟡 Keskiprioriteetti (Hyödylliset toiminnot)
3. **BackupSettingsDialogFX** - Varmuuskopiointiasetukset
   - **Arvio:** 4-6 tuntia
   - **Status:** ⏳

4. **RestoreBackupDialogFX** - Palauta varmuuskopiosta
   - **Arvio:** 3-5 tuntia
   - **Status:** ⏳

5. **Vientimallit-alivalikko** - Vientimallien listaaminen
   - **Arvio:** 2-3 tuntia
   - **Status:** ⏳

6. **Tositelajit-alivalikko** - Tositelajien listaaminen
   - **Arvio:** 2-3 tuntia
   - **Status:** ⏳

### 🟢 Matala prioriteetti (Lisätoiminnot)
7. **CSVImportDialogFX** - CSV-tuonti
   - **Arvio:** 4-6 tuntia
   - **Status:** ⏳

8. **ExportDialogFX** - CSV-vienti
   - **Arvio:** 2-3 tuntia
   - **Status:** ⏳

9. **AccountSummaryOptionsDialogFX** - Tilien saldot
   - **Arvio:** 3-5 tuntia
   - **Status:** ⏳

10. **AccountStatementOptionsDialogFX** - Tiliote
    - **Arvio:** 3-5 tuntia
    - **Status:** ⏳

11. **FinancialStatementOptionsDialogFX** - Tuloslaskelma/Tase erittelyin
    - **Arvio:** 2-4 tuntia
    - **Status:** ⏳

12. **VATReportDialogFX** - ALV-laskelma
    - **Arvio:** 3-5 tuntia
    - **Status:** ⏳

13. **BalanceComparisonDialogFX** - Tilien saldojen vertailu
    - **Arvio:** 4-6 tuntia
    - **Status:** ⏳

14. **DocumentNumberShiftDialogFX** - Muuta tositenumeroita
    - **Arvio:** 2-4 tuntia
    - **Status:** ⏳

15. **VATChangeDialogFX** - ALV-kantojen muutokset
    - **Arvio:** 3-5 tuntia
    - **Status:** ⏳

16. **VATDocumentDialogFX** - ALV-tilien päättäminen
    - **Arvio:** 2-3 tuntia
    - **Status:** ⏳

17. **COAReportDialogFX** - Tilikartta-raportit
    - **Arvio:** 1-2 tuntia
    - **Status:** ⏳

18. **ReportEditorDialogFX** - Raporttien muokkaus
    - **Arvio:** 4-6 tuntia
    - **Status:** ⏳

19. **SetIgnoreFlag** - Ohita vienti ALV-laskelmassa
    - **Arvio:** 1-2 tuntia
    - **Status:** ⏳

20. **DebugDialogFX** - Virheenjäljitystietoja
    - **Arvio:** 1-2 tuntia
    - **Status:** ⏳

## 📊 Yhteenveto

### Toteutustilanne
- **Toteutettu:** 26 handleria (60%)
- **Puuttuu:** 17 handleria (40%)
- **Puuttuvia dialogeja:** 25 dialogia
- **Toteutettu EntryTemplateDialogFX:** ✅ (juuri valmis)

### Työmääräarvio
- **Korkea prioriteetti:** ~6-10 tuntia (2 dialogia + 1 toiminto)
- **Keskiprioriteetti:** ~15-22 tuntia (4 dialogia + 2 toimintoa)
- **Matala prioriteetti:** ~50-80 tuntia (19 dialogia/toimintoa)
- **Yhteensä:** ~71-112 tuntia

### Edistyminen
- **Vaihe 1 (Kriittiset):** 1/3 valmis (EntryTemplateDialogFX ✅)
- **Vaihe 2 (Keskiprioriteetti):** 0/6 valmis
- **Vaihe 3 (Matala prioriteetti):** 0/19 valmis

## 🎯 Suositus

**Jatketaan korkean prioriteetin toteutuksella:**
1. ✅ EntryTemplateDialogFX - **VALMIS**
2. ⏳ StartingBalanceDialogFX - **SEURAAVAKSI**
3. ⏳ Vientimallin luominen tositteesta

Tämän jälkeen siirrytään keskiprioriteettiin (varmuuskopiointi).

## 📝 Huomioita

- Kaikki handlerit on määritelty (ei puuttuvia FXML-handlereita)
- Osa toiminnoista voi olla inline-toimintoja (ei tarvitse dialogia)
- Jotkut dialogit voivat jakaa saman FXML-layoutin (esim. FinancialStatementOptionsDialogFX)
- CSV-vienti/import käyttää olemassa olevaa logiikkaa (DocumentExporter, CsvImportDialog)
