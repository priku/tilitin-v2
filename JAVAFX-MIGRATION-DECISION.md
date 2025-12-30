# JavaFX Migration - Päätösdokumentti

**Päivämäärä:** 2025-12-31  
**Status:** ✅ Päätös tehty

---

## 🎯 Päätös

### Valittu teknologia: **Java + JavaFX**

---

## 📋 Perustelut

### 1. Kirjanpitosovelluksen vaatimukset

| Vaatimus | JavaFX-ratkaisu |
|----------|-----------------|
| Taulukot (Entry, Tilikartta) | ✅ TableView - paras vaihtoehto |
| Päivämääräkentät | ✅ DatePicker - valmis |
| Tulostus/PDF | ✅ PrinterJob - toimii |
| Vakaus | ✅ 16 vuotta kehitystä |

### 2. Cross-platform tuki

| Alusta | Tuki | Paketointi |
|--------|------|------------|
| 🪟 Windows | ✅ | .exe (jpackage + Inno Setup) |
| 🍎 macOS | ✅ Paras | .dmg (jpackage) |
| 🐧 Linux | ✅ | .deb, .rpm (jpackage) |

### 3. macOS-optimointi

| Ominaisuus | JavaFX |
|------------|--------|
| Natiivi menu bar | ✅ `useSystemMenuBar` |
| Apple Silicon (M1/M2/M3) | ✅ Natiivi tuki |
| Dark Mode | ✅ CSS-teemat |
| Retina/HiDPI | ✅ Automaattinen |
| Cmd-pikanäppäimet | ✅ |

### 4. Projektin nykytila

```
Java-tiedostoja:   202 (84.5%)
Kotlin-tiedostoja:  37 (15.5%)
```

**JavaFX sopii luonnollisesti Java-pohjaiseen projektiin.**

---

## ❌ Hylätyt vaihtoehdot

### Compose Desktop

| Pro | Contra |
|-----|--------|
| 100% Kotlin | Ei natiivia TableView:tä |
| Moderni | Ei natiivia macOS menu baria |
| Prototyyppi olemassa | Nuorempi teknologia |

**Päätös:** Hylätty koska TableView on kriittinen kirjanpitosovellukselle.

### Swing (nykyinen)

| Pro | Contra |
|-----|--------|
| Toimii jo | Vanhentunut |
| FlatLaf moderni | Ei yhtä hyvä macOS-tuki |

**Päätös:** Korvataan JavaFX:llä asteittain.

### 100% Kotlin -tavoite

**Päätös:** Luovutaan tavoitteesta. Java + JavaFX on parempi kirjanpitosovellukselle.

---

## 📅 Migraatiosuunnitelma

### Vaihe 1: Infrastruktuuri (1-2 päivää)
- [ ] Lisää JavaFX Gradle-riippuvuudet
- [ ] Testaa perus JavaFX-ikkuna
- [ ] Varmista jpackage toimii JavaFX:llä

### Vaihe 2: Prototyyppi (3-5 päivää)
- [ ] Luo DocumentFrameFX.java
- [ ] Luo DocumentFrame.fxml (Scene Builder)
- [ ] Integroi olemassa olevat Manager-luokat
- [ ] Testaa Entry-taulukko TableView:llä

### Vaihe 3: Pääikkuna (1-2 viikkoa)
- [ ] Implementoi täysi DocumentFrame
- [ ] Implementoi menu bar (useSystemMenuBar)
- [ ] Implementoi toolbar
- [ ] Testaa kaikki toiminnot

### Vaihe 4: Dialogit (2-3 viikkoa)
- [ ] Migroi 26 dialogia
- [ ] Käytä FXML + Controller -patternia
- [ ] Testaa jokainen dialogi

### Vaihe 5: Viimeistely (1 viikko)
- [ ] Print Preview
- [ ] Teemat (Light/Dark)
- [ ] Testaus kaikilla alustoilla
- [ ] Dokumentaatio

**Arvioitu kokonaisaika:** 6-8 viikkoa

---

## ✅ Hyväksyntä

- [x] TableView kirjanpitotaulukolle
- [x] Cross-platform (Windows, macOS, Linux)
- [x] Paras macOS-tuki (natiivi menu bar)
- [x] Vakaa ja kypsä teknologia
- [x] Sopii Java-projektiin

---

**Dokumentin luoja:** AI Assistant  
**Hyväksyjä:** _odottaa_
