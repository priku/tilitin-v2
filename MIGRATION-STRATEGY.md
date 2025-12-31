# Migraatiostrategia - Vältetään päällekkäinen työ

**Päivitetty:** 2025-12-31  
**Tavoite:** Selkeä strategia, jotta ei tehdä samaa työtä useita kertoja

---

## 🎯 Nykyinen tilanne

### 1. **Swing-versio (Legacy)**
- ✅ `DocumentFrame.java` - refaktoroitu osittain (2,423 riviä)
- ✅ Manager-luokat: `DocumentMenuHandler`, `DocumentNavigator`, `DocumentEntryManager`, jne.
- ✅ **Status:** Legacy-versio, jätetään rauhaan
- ❌ **Ei enää refaktoroida** - tämä on vanha UI

### 2. **JavaFX-versio (Uusi)**
- ✅ `MainController.java` - uusi JavaFX-controller
- ✅ `JavaFXApp.java` - uusi entry point
- ✅ Dialogit: `StartingBalanceDialogFX`, `EntryTemplateDialogFX`, jne.
- 🔄 **Status:** Keskeneräinen, nyt käynnissä
- ✅ **Tavoite:** Korvata Swing-versio kokonaan

### 3. **Kotlin-migraatio**
- ✅ DAO:t: 100% Kotlin (SQLite DAO:t)
- ❌ UI: Java (JavaFX)
- ⚠️ **Status:** Valinnainen, ei prioriteetti

---

## 📋 Strategia: Yksi kerta, oikein

### ✅ Mitä tehdään:

1. **JavaFX-migraatio ensin** (nyt käynnissä)
   - Luodaan uusi JavaFX-UI alusta asti
   - Käytetään olemassa olevia manager-luokkia (ne ovat UI-agnostisia)
   - **Ei refaktoroida Swing-versiota enää**

2. **Kun JavaFX on valmis:**
   - Swing-versio poistetaan
   - JavaFX-versio on ainoa UI
   - Manager-luokat pysyvät (ne toimivat molemmilla)

3. **Kotlin-konversio (valinnainen, myöhemmin):**
   - Jos halutaan, voidaan konvertoida JavaFX-koodia Kotliniin
   - **Ei prioriteetti** - JavaFX toimii hyvin Javalla
   - DAO:t ovat jo Kotliniin, se riittää

### ❌ Mitä EI tehdä:

1. ❌ **Ei refaktoroida Swing-DocumentFrame:ia enempää**
   - Se on legacy, jätetään rauhaan
   - Manager-luokat ovat jo eriytetty, se riittää

2. ❌ **Ei refaktoroida JavaFX-versiota ennen kuin se on valmis**
   - Tehdään ensin koko JavaFX-UI
   - Sitten voidaan optimoida/refaktoroida jos tarvetta

3. ❌ **Ei konvertoida Kotliniin ennen kuin JavaFX on valmis**
   - JavaFX toimii hyvin Javalla
   - Kotlin on valinnainen optimointi, ei pakollinen

---

## 🗺️ Toteutussuunnitelma

### Vaihe 1: JavaFX-migraatio (nyt käynnissä) ✅
- [x] Perus UI (`MainController`, `MainView.fxml`)
- [x] Entry-taulukko (`EntryRowModel`, custom cells)
- [x] Dialogit (`StartingBalanceDialogFX`, `EntryTemplateDialogFX`)
- [ ] Loput dialogit (26 dialogia)
- [ ] Raportit
- [ ] Print-toiminnot
- [ ] Testaus

### Vaihe 2: Swing-poisto (kun JavaFX on valmis)
- [ ] Poista `DocumentFrame.java` (Swing)
- [ ] Poista Swing-dialogit
- [ ] Päivitä `Kirjanpito.java` käyttämään `JavaFXApp`
- [ ] Testaus

### Vaihe 3: Kotlin-konversio (valinnainen)
- [ ] Konvertoi JavaFX-koodia Kotliniin (jos halutaan)
- [ ] Tämä on valinnainen optimointi, ei pakollinen

---

## 💡 Miksi tämä strategia?

### 1. **Manager-luokat ovat UI-agnostisia**
```java
// Nämä toimivat molemmilla UI:illa
DocumentMenuHandler.java      // UI-agnostinen
DocumentNavigator.java         // UI-agnostinen
DocumentEntryManager.java      // UI-agnostinen
DocumentValidator.java         // UI-agnostinen
```

### 2. **JavaFX on uusi UI, ei refaktorointia**
- Luomme uuden UI:n alusta asti
- Käytämme olemassa olevia manager-luokkia
- Swing-versio jää legacyksi, ei tarvitse refaktoroida

### 3. **Kotlin on valinnainen**
- DAO:t ovat jo Kotliniin ✅
- UI voi olla Javaa, se toimii hyvin
- Kotlin-konversio on optimointi, ei pakollinen

---

## ✅ Yhteenveto

**Tehdään kerran, oikein:**
1. ✅ JavaFX-migraatio (nyt käynnissä)
2. ⏳ Swing-poisto (kun JavaFX on valmis)
3. ⏸️ Kotlin-konversio (valinnainen, myöhemmin)

**Ei tehdä:**
- ❌ Swing-refaktorointia enempää
- ❌ JavaFX-refaktorointia ennen valmista
- ❌ Kotlin-konversiota ennen JavaFX-valmista

---

**Dokumentin luoja:** AI Assistant  
**Hyväksyjä:** _odottaa_
