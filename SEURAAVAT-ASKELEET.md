# Seuraavat Askeleet - Suositukset

**Päivämäärä:** 2026-01-02  
**Tilanne:** Quick wins valmiit, projekti kääntyy onnistuneesti

---

## 🎯 Suosittelen seuraavia vaiheita (prioriteetti-järjestyksessä)

### 1. Testaa luodut testit (5 min) ⚡ VÄLITÖN

**Miksi ensin:**
- Varmistaa että test-infrastruktuuri toimii
- Näyttää että testit todella ajetaan
- Antaa luottamusta jatkokehitykseen

**Tehtävä:**
```bash
./gradlew test
```

**Odotettu tulos:**
- Testit ajetaan onnistuneesti
- AccountDAOTest:n 5 testiä läpäisevät

**Jos testit epäonnistuvat:**
- Korjaa testit
- Tarkista tietokanta-initialisointi

---

### 2. Poista legacy Swing-dialogit (1-2h) ⭐ SUURI ARVO

**Miksi nyt:**
- ✅ **Vähäinen riski** - koodi on varmistettu käyttämättömäksi
- ✅ **Suuri vaikutus** - ~3000 riviä koodia pois
- ✅ **Nopea** - 1-2 tuntia
- ✅ **Selkeyttää koodikantaa** - vähemmän sekaannusta

**Tehtävä:**
Käytä `LEGACY-CODE-REMOVAL.md` -dokumenttia:
1. Luo git-haara: `git checkout -b remove-legacy-swing-dialogs`
2. Poista 21 Swing-dialogia (Phase 1)
3. Testaa: `./gradlew build && ./gradlew run`
4. Jos toimii → commit

**Tiedostot poistettavaksi:**
- `src/main/java/kirjanpito/ui/AboutDialog.java`
- `src/main/java/kirjanpito/ui/AccountSelectionDialog.java`
- `src/main/java/kirjanpito/ui/AppearanceDialog.java`
- ... (katso `LEGACY-CODE-REMOVAL.md` täydellinen lista)

**Hyödyt:**
- ✅ ~3000 riviä koodia pois
- ✅ Selkeämpi koodikanta
- ✅ Nopeampi build
- ✅ Vähemmän ylläpidettävää koodia

---

### 3. Migroi lisää yksinkertaisia dialogeja Kotliniin (2-3h) 📝

**Miksi:**
- Jatkaa Kotlin-migraatiota
- Pienempi riski kuin isommat refaktorointit
- Parantaa koodin laatua

**Ehdokkaat (helpoimmat ensin):**
1. **DebugInfoDialogFX** - Yksinkertainen, vähän logiikkaa
2. **PropertiesDialogFX** - Keskisuuri, mutta selkeä
3. **KeyboardShortcutsDialogFX** - Yksinkertainen lista

**Arvio:**
- 1 dialogi: ~1-2 tuntia
- 3 dialogia: ~4-6 tuntia

**Hyödyt:**
- ✅ Kotlin-prosentti nousee (8.2% → ~9-10%)
- ✅ Parempi null-safety
- ✅ Yhtenäisempi koodikanta

---

### 4. Laajenna testikattavuutta (2-3h) 🧪

**Miksi:**
- Kattavuus on nyt hyvin alhainen
- Testit mahdollistavat turvallisen refaktoroinnin
- Parantaa koodin laatua

**Prioriteetit:**
1. **EntryDAOTest** - Kriittinen business-logiikka
2. **DocumentDAOTest** - Tositteiden hallinta
3. **PeriodDAOTest** - Tilikausien hallinta

**Arvio:**
- 1 DAO-testi: ~1-2 tuntia
- 3 DAO-testiä: ~4-6 tuntia

**Hyödyt:**
- ✅ Testikattavuus nousee (n. 0% → 10-15%)
- ✅ Turvallisempi refaktorointi
- ✅ Regressioiden estäminen

---

### 5. Testaa sovellus manuaalisesti (2-4h) 🧪

**Miksi:**
- Varmistaa että kaikki toimii käytännössä
- Löytää mahdollisia bugeja
- Valmistaa releasea varten

**Tehtävä:**
Käytä `TESTING-GUIDE.md` -dokumenttia:
1. Käynnistä: `./gradlew run`
2. Testaa kriittiset toiminnot
3. Testaa migroidut dialogit (About, Help)
4. Dokumentoi löydetyt bugit

**Kriittiset testit:**
- ✅ Sovellus käynnistyy
- ✅ Tietokannan luonti/avaus
- ✅ Tositteen luonti/tallennus
- ✅ AboutDialogFX toimii
- ✅ HelpDialogFX toimii
- ✅ Muutama muu dialogi

---

## 📊 Suosittu järjestys

### Tänään (1-2h):
1. ✅ **Aja testit** - Varmista että toimivat
2. ✅ **Poista legacy Swing-dialogit** - Suuri vaikutus, pieni riski

### Tällä viikolla (4-6h):
3. ✅ **Migroi 2-3 dialogia** Kotliniin
4. ✅ **Lisää 1-2 DAO-testiä**

### Ennen releasea:
5. ✅ **Manuaalinen testaus** - Varmista että kaikki toimii

---

## 🎯 Nopein voitto (suosittelen aloittamaan tästä)

**Vaihe 1: Testaa testit (5 min)**
```bash
./gradlew test
```

**Vaihe 2: Poista legacy Swing-dialogit (1-2h)**
- Käytä `LEGACY-CODE-REMOVAL.md` -dokumenttia
- Poista Phase 1 (21 dialogia)
- Testaa että build toimii

**Tämä antaa:**
- ✅ Suuren vaikutuksen (3000 riviä pois)
- ✅ Pienen riskin (koodi on varmistettu käyttämättömäksi)
- ✅ Nopean tuloksen (1-2h)

---

## 💡 Vaihtoehtoiset suunnat

### Jos haluat keskittyä testaamiseen:
1. Aja testit
2. Laajenna testikattavuutta
3. Testaa sovellus manuaalisesti

### Jos haluat keskittyä Kotlin-migraatioon:
1. Migroi lisää dialogeja
2. Luo BaseDialog-pohja Kotlinissa
3. Aloita MainController-handlereiden erottelu

### Jos haluat valmistautua releaseen:
1. Testaa sovellus manuaalisesti
2. Korjaa löydetyt bugit
3. Päivitä dokumentaatio
4. Valmista release

---

## 🚀 Aloita tästä

**Suosittelen aloittamaan:**
1. **Aja testit** (`./gradlew test`) - 5 min
2. **Poista legacy Swing-dialogit** - 1-2h, suuri vaikutus

Tämä antaa nopean tuloksen ja selkeyttää koodikantaa merkittävästi!

---

**Kysymys:** Haluatko että aloitan jonkin näistä vaiheista?
