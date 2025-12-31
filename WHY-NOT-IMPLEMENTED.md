# Miksi osa handlerista ei ole toteutettu?

## Tilanne

**Toteutettu:** 25 handleria (58%)  
**Ei toteutettu:** 18 handleria (42%)

## Syyt

### 1. **JavaFX-dialogit puuttuvat**

Swing-versiossa on **33 dialogia**, mutta JavaFX-versioita on toteutettu vain **7 dialogia**:

#### ✅ Toteutetut JavaFX-dialogit:
- SettingsDialogFX
- HelpDialogFX
- AttachmentsDialogFX
- ReportDialogFX (Journal, Ledger, Income Statement, Balance Sheet)
- DocumentTypeDialogFX
- COADialogFX
- AccountSelectionDialogFX

#### ❌ Puuttuvat JavaFX-dialogit (26 dialogia):
1. **EntryTemplateDialogFX** → `handleEditEntryTemplates()`, `handleCreateEntryTemplate()`
2. **StartingBalanceDialogFX** → `handleStartingBalances()`
3. **BackupSettingsDialogFX** → `handleBackupSettings()`
4. **RestoreBackupDialogFX** → `handleRestoreBackup()`
5. **BalanceComparisonDialogFX** → `handleBalanceComparison()`
6. **AccountSummaryOptionsDialogFX** → `handleAccountSummary()`
7. **AccountStatementOptionsDialogFX** → `handleAccountStatement()`
8. **FinancialStatementOptionsDialogFX** → `handleIncomeStatementDetailed()`, `handleBalanceSheetDetailed()`
9. **VATReportDialogFX** → `handleVatReport()`
10. **ReportEditorDialogFX** → `handleEditReports()`
11. **CSVImportDialogFX** → `handleCsvImport()`
12. **ExportDialogFX** → `handleExport()`
13. **VATDocumentDialogFX** → `handleVatDocument()`
14. **NumberShiftDialogFX** → `handleNumberShift()`
15. **VATChangeDialogFX** → `handleVatChange()`
16. **COAReportDialogFX** → `handleCoa0()`, `handleCoa1()`, `handleCoa2()`
17. **DebugDialogFX** → `handleDebug()`
18. **SetIgnoreFlagDialogFX** → `handleSetIgnoreFlag()` (tämä voi olla inline-toiminto)

## Miksi ne on jätetty placeholder-tilaan?

### 1. **Priorisointi**
- Fokus on ollut **kriittisillä perustoiminnoilla**:
  - Tietokannan avaus/tallennus
  - Tositteiden muokkaus
  - Vientien hallinta
  - Perusraportit
  - Asetukset

### 2. **Monimutkaisuus**
- Jokainen dialogi vaatii:
  - FXML-layoutin suunnittelun
  - Controllerin toteutuksen
  - Swing-logiikan porttaamisen JavaFX:ään
  - Testaamisen
  - Arvio: **2-8 tuntia per dialogi**

### 3. **Ei-kriittisyys**
- Nämä toiminnot eivät estä peruskäyttöä
- Ne voidaan toteuttaa myöhemmin
- Käyttäjä voi käyttää Swing-versiota tarvittaessa

## Toteutussuunnitelma

### Vaihe 1: Kriittiset dialogit (korkea prioriteetti)
1. ✅ SettingsDialogFX
2. ✅ COADialogFX
3. ✅ ReportDialogFX (perusraportit)
4. ⏳ **EntryTemplateDialogFX** - Vientimallit (hyödyllinen)
5. ⏳ **StartingBalanceDialogFX** - Alkusaldot (tärkeä)

### Vaihe 2: Varmuuskopiointi (keskiprioriteetti)
6. ⏳ **BackupSettingsDialogFX**
7. ⏳ **RestoreBackupDialogFX**

### Vaihe 3: Lisäraportit (matala prioriteetti)
8. ⏳ **AccountSummaryOptionsDialogFX**
9. ⏳ **AccountStatementOptionsDialogFX**
10. ⏳ **FinancialStatementOptionsDialogFX**
11. ⏳ **VATReportDialogFX**
12. ⏳ **COAReportDialogFX**

### Vaihe 4: Työkalut (matala prioriteetti)
13. ⏳ **BalanceComparisonDialogFX**
14. ⏳ **NumberShiftDialogFX**
15. ⏳ **VATChangeDialogFX**
16. ⏳ **CSVImportDialogFX**
17. ⏳ **ExportDialogFX**
18. ⏳ **VATDocumentDialogFX**

### Vaihe 5: Muut (hyvin matala prioriteetti)
19. ⏳ **ReportEditorDialogFX**
20. ⏳ **DebugDialogFX**

## Arvio

- **Kriittiset (Vaihe 1):** ~16 tuntia (2 dialogia)
- **Keskiprioriteetti (Vaihe 2):** ~16 tuntia (2 dialogia)
- **Matala prioriteetti (Vaihe 3-5):** ~80-120 tuntia (14 dialogia)
- **Yhteensä:** ~112-152 tuntia

## Suositus

**Aloitetaan Vaihe 1:sta** (EntryTemplateDialogFX ja StartingBalanceDialogFX), koska ne ovat:
- Hyödyllisiä käyttäjille
- Suhteellisen yksinkertaisia
- Olennaisia perustoiminnalle

## Seuraavat askeleet

Jos haluat että aloitetaan toteuttamaan näitä, voin:
1. Aloittaa **EntryTemplateDialogFX**:n toteutuksella
2. Seurata samaa mallia kuin muissa JavaFX-dialogeissa
3. Portata Swing-logiikan JavaFX:ään
4. Testata toimivuus

**Haluatko että aloitetaan?**
