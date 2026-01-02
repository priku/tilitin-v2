# UI-testikattavuus

**Päivämäärä:** 2026-01-02  
**TestFX-kirjasto:** 4.0.18

---

## Dialog-testit

### Luodut testit

1. ✅ **AboutDialogFXTest**
   - Testaa dialogin luomisen
   - Sijainti: `src/test/kotlin/kirjanpito/ui/javafx/dialogs/AboutDialogFXTest.kt`

2. ✅ **HelpDialogFXTest**
   - Testaa dialogin luomisen
   - Sijainti: `src/test/kotlin/kirjanpito/ui/javafx/dialogs/HelpDialogFXTest.kt`

3. ✅ **AppearanceDialogFXTest**
   - Testaa dialogin luomisen
   - Sijainti: `src/test/kotlin/kirjanpito/ui/javafx/dialogs/AppearanceDialogFXTest.kt`

4. ✅ **PrintSettingsDialogFXTest**
   - Testaa dialogin luomisen
   - Sijainti: `src/test/kotlin/kirjanpito/ui/javafx/dialogs/PrintSettingsDialogFXTest.kt`

**Yhteensä:** 4 dialog-testiä

---

## Table-testit

### Luodut testit

1. ✅ **EntryRowModelTest**
   - Testaa EntryRowModel-luokan toiminnallisuutta
   - Testaa debet/kredit-logiikkaa
   - Testaa description-päivityksiä
   - Sijainti: `src/test/kotlin/kirjanpito/ui/javafx/EntryRowModelTest.kt`
   - **Testejä:** 6

2. ✅ **EntryTableNavigationTest**
   - Testaa TableView-komponentin perustoiminnallisuutta
   - Testaa itemien lisäämistä
   - Testaa valintaa
   - Testaa sarakkeiden lisäämistä
   - Sijainti: `src/test/kotlin/kirjanpito/ui/javafx/EntryTableNavigationTest.kt`
   - **Testejä:** 3

**Yhteensä:** 2 table-testiluokkaa, 9 testiä

---

## Testikattavuus

### Dialog-testit

**Testattavat dialogit:**
- AboutDialogFX ✅
- HelpDialogFX ✅
- AppearanceDialogFX ✅
- PrintSettingsDialogFX ✅

**Ei vielä testattuja dialogeja:**
- AccountSelectionDialogFX
- PropertiesDialogFX
- DebugInfoDialogFX
- SettingsDialogFX
- DocumentTypeDialogFX
- jne. (~21 dialogia)

### Table-testit

**Testattavat komponentit:**
- EntryRowModel ✅ (6 testiä)
- TableView perustoiminnallisuus ✅ (3 testiä)

**Ei vielä testattuja komponentteja:**
- EntryTableNavigationHandler (vaatii monimutkaisempaa setupia)
- Cell editing
- Keyboard navigation
- Real-time validation

---

## Testausrakenne

### TestFX-käyttö

- Käytetään `@ExtendWith(ApplicationExtension::class)`
- Käytetään `FxRobot` -perintöä
- Käytetään `interact {}` -blokkeja JavaFX-threadissa suoritukseen

### Esimerkkitesti

```kotlin
@ExtendWith(ApplicationExtension::class)
class AppearanceDialogFXTest : FxRobot() {

    private lateinit var testStage: Stage

    @Start
    fun start(stage: Stage) {
        testStage = stage
        testStage.show()
    }

    @Test
    fun `test dialog can be created`() {
        interact {
            val dialog = AppearanceDialogFX(testStage)
            assertNotNull(dialog)
        }
    }
}
```

---

## Seuraavat askeleet

### Dialog-testit (suositeltu)

1. **AccountSelectionDialogFX testit** (1-2h)
   - Testaa valintatoiminnallisuutta
   - Testaa suodatusta

2. **PropertiesDialogFX testit** (1-2h)
   - Testaa asetusten tallennusta

### Table-testit (suositeltu)

1. **EntryTableNavigationHandler testit** (2-3h)
   - Vaati mock-olioiden luomisen
   - Testaa keyboard navigationia
   - Testaa Tab/Shift+Tab -navigointia

2. **Cell editing testit** (2-3h)
   - Testaa solujen muokkausta
   - Testaa validaatiota

---

## Mittarit

**UI-testit yhteensä:** 13 testiä (4 dialog-testiä + 9 table-testiä)

**Testikattavuus:**
- Dialog-testit: 4 / ~25 JavaFX-dialogia (~16%)
- Table-testit: 2 komponenttia testattuna

**Tavoite:**
- Dialog-testit: 10-15 testiä (40-60% kattavuus)
- Table-testit: 5-10 testiä
- Yhteensä: 20-30 UI-testiä

---

**Viimeksi päivitetty:** 2026-01-02
