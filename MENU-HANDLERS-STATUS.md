# Valikoiden Handlerit - Tila

## ✅ Toteutetut handlerit (toimivat)

### Tiedosto-valikko
- ✅ `handleNewDatabase()` - Uusi tietokanta
- ✅ `handleOpenDatabase()` - Avaa tietokanta
- ✅ `handleDatabaseSettings()` - Tietokanta-asetukset
- ✅ `handleQuit()` - Lopeta

### Muokkaa-valikko
- ✅ `handleCopy()` - Kopioi
- ✅ `handlePaste()` - Liitä
- ✅ `handleNewDocument()` - Uusi tosite
- ✅ `handleDeleteDocument()` - Poista tosite
- ✅ `handleAddEntry()` - Lisää vienti
- ✅ `handleRemoveEntry()` - Poista vienti
- ✅ `handleChartOfAccounts()` - Tilikartta
- ✅ `handlePeriodSettings()` - Perustiedot
- ✅ `handleSettings()` - Kirjausasetukset
- ✅ `handleAppearance()` - Ulkoasu

### Siirry-valikko
- ✅ `handlePrevDocument()` - Edellinen
- ✅ `handleNextDocument()` - Seuraava
- ✅ `handleFirstDocument()` - Ensimmäinen
- ✅ `handleLastDocument()` - Viimeinen
- ✅ `handleGotoDocument()` - Hae numerolla
- ✅ `handleToggleSearch()` - Etsi (korjattu: päivittää checkboxin)

### Tulosteet-valikko
- ✅ `handleJournalReport()` - Päiväkirja
- ✅ `handleLedgerReport()` - Pääkirja
- ✅ `handleIncomeStatement()` - Tuloslaskelma
- ✅ `handleBalanceSheet()` - Tase
- ✅ `handlePrintDocument()` - Tosite (kutsuu handlePrint())

### Ohje-valikko
- ✅ `handleHelp()` - Sisältö
- ✅ `handleAbout()` - Tietoja ohjelmasta

### Muut
- ✅ `handleSave()` - Tallenna
- ✅ `handlePrint()` - Tulosta
- ✅ `handleSearch()` - Hae
- ✅ `handleDocumentTypes()` - Tositelajit
- ✅ `handleAttachment()` - Liite

## ⚠️ Osittain toteutetut (näyttävät "ei vielä toteutettu")

### Muokkaa-valikko
- ✅ `handleEditEntryTemplates()` - Vientimallien muokkaus (**VALMIS** - EntryTemplateDialogFX)
- ⚠️ `handleCreateEntryTemplate()` - Vientimallin luominen tositteesta
- ⚠️ `handleStartingBalances()` - Alkusaldot
- ⚠️ `handleVatDocument()` - ALV-merkintä

### Tiedosto-valikko
- ⚠️ `handleBackupSettings()` - Varmuuskopiointi
- ⚠️ `handleRestoreBackup()` - Palauta varmuuskopiosta

### Tulosteet-valikko
- ⚠️ `handleAccountSummary()` - Tilien saldot
- ⚠️ `handleAccountStatement()` - Tiliote
- ⚠️ `handleIncomeStatementDetailed()` - Tuloslaskelma erittelyin
- ⚠️ `handleBalanceSheetDetailed()` - Tase erittelyin
- ⚠️ `handleVatReport()` - ALV-laskelma tileittäin
- ⚠️ `handleCoa0()` - Tilikartta - Kaikki tilit
- ⚠️ `handleCoa1()` - Tilikartta - Vain käytössä olevat tilit
- ⚠️ `handleCoa2()` - Tilikartta - Vain suosikkitilit
- ⚠️ `handleEditReports()` - Raporttien muokkaus

### Työkalut-valikko
- ⚠️ `handleSetIgnoreFlag()` - Ohita vienti ALV-laskelmassa
- ⚠️ `handleBalanceComparison()` - Tilien saldojen vertailu
- ⚠️ `handleNumberShift()` - Muuta tositenumeroita
- ⚠️ `handleVatChange()` - ALV-kantojen muutokset
- ⚠️ `handleExport()` - Vie tiedostoon
- ⚠️ `handleCsvImport()` - Tuo CSV-tiedostosta

### Ohje-valikko
- ⚠️ `handleDebug()` - Virheenjäljitystietoja

## 📊 Yhteenveto

- **Toteutettu ja toimii:** 26 handleria (60%)
- **Osittain toteutettu (placeholder):** 17 handleria (40%)
- **Yhteensä:** 43 handleria

### Viimeisin päivitys
- ✅ EntryTemplateDialogFX toteutettu (2025-01-XX)
- ✅ `handleEditEntryTemplates()` nyt toimii

## ✅ Kaikki FXML-handlerit on määritelty

Kaikki FXML-tiedostossa määritellyt `onAction`-handlerit on toteutettu MainControllerissa. Ei puuttuvia handlerita.

## 🔧 Korjaukset

1. ✅ `handleToggleSearch()` - Päivittää nyt checkboxin tilan ja search buttonin näkyvyyden
2. ✅ `handleQuit()` - Käyttää nyt `Platform.exit()` oikein
