# Compose Desktop -siirtymäsuunnitelma

## Yleiskuvaus

Tämä dokumentti kuvaa vaiheittaisen siirtymän Swing-UI:sta Compose Desktopiin.

## Nykyinen arkkitehtuuri

### Pääkomponentit:
- **DocumentFrame.java** - Pääikkuna (JFrame)
  - MenuBar (DocumentMenuBuilder)
  - ToolBar (DocumentToolbarBuilder)
  - StatusBar
  - EntryTable (JTable)
  - TextFields (numero, päivämäärä, tagi)
  - AttachmentsPanel
  - SearchPanel

### Dialogit (~50 kpl):
- SettingsDialog, PropertiesDialog, COADialog, AccountDialog
- EntryTemplateDialog, StartingBalanceDialog, SearchDialog
- PrintOptionsDialog, FinancialStatementOptionsDialog
- Ja monia muita...

### Apuluokat:
- DocumentMenuBuilder, DocumentToolbarBuilder
- DocumentTableManager, EntryTableActions
- BaseDialog (abstrakti pohjaluokka)
- UIConstants (teematuki)

## Siirtymästrategia

### Vaihe 1: Perus Compose Desktop -sovellus ✅
- [x] Lisätty Compose Desktop -riippuvuudet
- [x] Luotu TilitinApp.kt (perusikkuna)
- [ ] Testata että sovellus käynnistyy

### Vaihe 2: Swing-Interoperability (vaiheittainen siirtymä)
Compose Desktop tukee Swing-komponenttien käyttöä Compose-UI:ssa, joten voimme siirtyä vaiheittain:

**Hybridi-arkkitehtuuri:**
- Compose Desktop -ikkuna
- Swing-komponentit Compose-UI:n sisällä (SwingPanel)
- Vaiheittainen korvaaminen Compose-komponenteilla

### Vaihe 3: Yksinkertaiset komponentit Compose Desktopiin
Aloitetaan yksinkertaisista komponenteista:

1. **Tekstikentät** (TextField → Compose TextField)
2. **Napit** (JButton → Compose Button)
3. **Labelit** (JLabel → Compose Text)
4. **Checkboxit** (JCheckBox → Compose Checkbox)

### Vaihe 4: Taulukot (JTable → Compose LazyColumn/Table)
- EntryTable on monimutkainen (solueditointi, valinta, renderöinti)
- Vaatii huolellisen suunnittelun
- Compose Desktop 1.7.1 tukee Table-komponenttia

### Vaihe 5: Dialoogit Compose Desktopiin
- Compose Desktop tukee Dialog-komponenttia
- Korvataan JDialog → Compose Dialog
- Yksinkertaiset dialoogit ensin (AboutDialog, SettingsDialog)
- Monimutkaisemmat myöhemmin (COADialog, EntryTemplateDialog)

### Vaihe 6: Pääikkuna (DocumentFrame → Compose)
- MenuBar → Compose MenuBar
- ToolBar → Compose TopAppBar/Toolbar
- StatusBar → Compose BottomBar
- Sisältö → Compose Column/Row -layoutit

## Toteutusjärjestys

### Sprint 1: Perusinfra ja testaus (1-2 päivää)
1. ✅ Lisätty Compose Desktop -riippuvuudet
2. ✅ Luotu TilitinApp.kt
3. [ ] Testata että sovellus käynnistyy
4. [ ] Korjata mahdolliset kääntymisvirheet
5. [ ] Luoda yksinkertainen testi-UI

### Sprint 2: Swing-Interoperability (2-3 päivää)
1. [ ] Integroida DocumentFrame SwingPanel:ina Compose-ikkunaan
2. [ ] Testata että nykyinen UI toimii Compose-ikkunassa
3. [ ] Varmistaa että kaikki toiminnot säilyvät

### Sprint 3: Yksinkertaiset komponentit (3-5 päivää)
1. [ ] Luoda Compose-versiot tekstikentistä
2. [ ] Luoda Compose-versiot napeista
3. [ ] Integroida DocumentFrameen vaiheittain
4. [ ] Testata toiminnallisuus

### Sprint 4: Taulukot (5-7 päivää)
1. [ ] Tutkia Compose Desktop Table-komponenttia
2. [ ] Luoda EntryTable Compose-versio
3. [ ] Migroida taulukon toiminnallisuus
4. [ ] Testata kaikki taulukon toiminnot

### Sprint 5: Dialoogit - Batch 1 (5-7 päivää)
Yksinkertaiset dialoogit ensin:
1. [ ] AboutDialog → Compose Dialog
2. [ ] SettingsDialog → Compose Dialog
3. [ ] PropertiesDialog → Compose Dialog
4. [ ] Testata toiminnallisuus

### Sprint 6: Dialoogit - Batch 2 (7-10 päivää)
Monimutkaisemmat dialoogit:
1. [ ] COADialog → Compose Dialog
2. [ ] AccountDialog → Compose Dialog
3. [ ] EntryTemplateDialog → Compose Dialog
4. [ ] Testata toiminnallisuus

### Sprint 7: Pääikkuna (7-10 päivää)
1. [ ] MenuBar → Compose MenuBar
2. [ ] ToolBar → Compose TopAppBar
3. [ ] StatusBar → Compose BottomBar
4. [ ] Layoutit → Compose Column/Row
5. [ ] Testata kaikki toiminnot

### Sprint 8: Siivous ja optimointi (3-5 päivää)
1. [ ] Poistaa Swing-riippuvuudet
2. [ ] Poistaa vanhat Swing-komponentit
3. [ ] Optimoida Compose-koodia
4. [ ] Lopullinen testaus

## Tekniset huomiot

### Compose Desktop -versio
- Käytetään versiota 1.7.1 (stabilimpi kuin uudemmat)
- Material Design (ei Material3, koska se vaatii erillisen riippuvuuden)

### Swing-Interoperability
```kotlin
// Swing-komponentti Compose-UI:ssa
SwingPanel(factory = { 
    JButton("Click me") 
})
```

### Compose Desktop -komponentit
- `Window` - ikkuna
- `Dialog` - dialogi
- `MenuBar` - valikkopalkki
- `TextField`, `Button`, `Text` - peruskomponentit
- `LazyColumn`, `LazyRow` - listat
- `Table` - taulukot (1.7.1+)

### Tietokanta- ja modeli-integraatio
- Nykyiset modelit (DocumentModel, Registry) toimivat suoraan
- DAO-kerros on jo Kotlinissa
- Ei tarvita muutoksia datakerrokseen

## Testausstrategia

1. **Yksikkötestit**: Jokaiselle uudelle Compose-komponentille
2. **Integraatiotestit**: Komponenttien yhteistoiminta
3. **Manuaalinen testaus**: Kaikki toiminnot käsin
4. **Regressiotestaus**: Varmistaa että vanhat toiminnot säilyvät

## Riskit ja haasteet

1. **JTable → Compose Table**: Monimutkainen migraatio
2. **Swing-riippuvuudet**: Voi olla piilotettuja riippuvuuksia
3. **Suorituskyky**: Compose Desktop voi olla hitaampi kuin Swing
4. **Look & Feel**: FlatLaf → Material Design -muutos
5. **Aikataulu**: Siirtymä voi kestää pidempään kuin arvioitu

## Seuraavat askeleet

1. **Testaa TilitinApp.kt**: Varmista että sovellus käynnistyy
2. **Luoda yksinkertainen testi-UI**: Napit, tekstikentät
3. **Integroida Swing-ikkuna**: DocumentFrame SwingPanel:ina
4. **Aloittaa komponenttien migraatio**: Yksinkertaiset ensin

## Dokumentaatio

- [Compose Desktop dokumentaatio](https://www.jetbrains.com/lp/compose-multiplatform/)
- [Compose Desktop samples](https://github.com/JetBrains/compose-multiplatform/tree/master/tutorials/Desktop_Application)
- [Swing interoperability](https://www.jetbrains.com/help/kotlin-multiplatform-dev/compose-multiplatform-swing-interop.html)

