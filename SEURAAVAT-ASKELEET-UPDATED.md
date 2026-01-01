# Seuraavat Askeleet - Päivitetty Suunnitelma

**Päivämäärä:** 2026-01-02  
**Päätös:** Molemmat UI:t (Swing + JavaFX) pidetään  
**Tilanne:** Quick wins valmiit, projekti kääntyy onnistuneesti

---

## 🎯 Suosittelen seuraavia vaiheita (Swing UI säilytetty)

### 1. Migroi lisää dialogeja Kotliniin (2-4h) ⭐ SUOSITELTU

**Miksi nyt:**
- ✅ Parantaa koodin laatua
- ✅ Pieni riski (yksinkertaiset dialogit)
- ✅ Jatkaa modernisointia
- ✅ Ei vaikuta Swing-UI:hin (JavaFX-dialogit)

**Ehdokkaat (helpoimmat ensin):**

#### 1.1 DebugInfoDialogFX (1-2h)
- Yksinkertaisin - vähän logiikkaa
- Hyvä aloituspiste
- Nopea voitto

#### 1.2 KeyboardShortcutsDialogFX (1-2h)
- Yksinkertainen lista-näyttö
- Vähän logiikkaa
- Helppo migroida

#### 1.3 PropertiesDialogFX (2-3h)
- Keskisuuri dialogi
- Enemmän logiikkaa, mutta selkeä
- Hyvä harjoitus

**Arvio:**
- 1 dialogi: ~1-2 tuntia
- 3 dialogia: ~4-6 tuntia

**Hyödyt:**
- ✅ Kotlin-prosentti nousee (8.2% → ~9-10%)
- ✅ Parempi null-safety
- ✅ Yhtenäisempi koodikanta
- ✅ Ei vaikuta Swing-UI:hin

---

### 2. Laajenna testikattavuutta (3-4h) 🧪

**Miksi:**
- ✅ Testikattavuus on nyt hyvin alhainen
- ✅ Testit mahdollistavat turvallisen refaktoroinnin
- ✅ Parantaa koodin laatua
- ✅ Ei vaikuta UI:hin

**Prioriteetit:**

#### 2.1 EntryDAOTest (1-2h)
- Kriittinen business-logiikka
- Viennit ovat ydin-toiminnallisuus
- Korkea arvo

#### 2.2 DocumentDAOTest (1-2h)
- Tositteiden hallinta
- Keskeinen toiminnallisuus
- Korkea arvo

#### 2.3 PeriodDAOTest (1h)
- Tilikausien hallinta
- Yksinkertaisempi kuin muut
- Nopea voitto

**Arvio:**
- 1 DAO-testi: ~1-2 tuntia
- 3 DAO-testiä: ~4-6 tuntia

**Hyödyt:**
- ✅ Testikattavuus nousee (0% → 10-15%)
- ✅ Turvallisempi refaktorointi
- ✅ Regressioiden estäminen
- ✅ Ei vaikuta UI:hin

---

### 3. Testaa JavaFX-sovellus manuaalisesti (2-4h) 🧪

**Miksi:**
- ✅ Varmistaa että JavaFX-UI toimii käytännössä
- ✅ Löytää mahdollisia bugeja
- ✅ Valmistaa releasea varten
- ✅ Testaa migroidut dialogit

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
- ✅ AboutDialogFX toimii (Kotlin-versio)
- ✅ HelpDialogFX toimii (Kotlin-versio)
- ✅ Muutama muu dialogi

---

### 4. Paranna dokumentaatiota (1-2h) 📝

**Miksi:**
- ✅ Selkeyttää projektin tilaa
- ✅ Auttaa tulevia kehittäjiä
- ✅ Valmistaa releasea varten

**Tehtävät:**
1. Päivitä README.md -selvitys molemmista UI:sta
2. Päivitä BUILDING.md -ohjeet
3. Dokumentoi että molemmat UI:t ovat saatavilla
4. Selitä erot Swing vs JavaFX -UI:en välillä

**Hyödyt:**
- ✅ Selkeämpi dokumentaatio
- ✅ Käyttäjät ymmärtävät vaihtoehdot
- ✅ Kehittäjät ymmärtävät arkkitehtuurin

---

### 5. Luo Kotlin BaseDialog-pohja (2-3h) 🏗️

**Miksi:**
- ✅ Yhtenäinen dialogi-pohja
- ✅ Vähentää toistoa
- ✅ Helpottaa tulevia migraatioita
- ✅ Parantaa koodin laatua

**Tehtävä:**
Luo `BaseDialogFX.kt`:
```kotlin
abstract class BaseDialogFX(owner: Window?) {
    protected val dialog: Stage
    abstract fun createContent(): Parent
    abstract fun onOK(): Boolean
    fun show() { ... }
    fun showAndWait(): Boolean { ... }
}
```

**Hyödyt:**
- ✅ Yhtenäinen malli kaikille dialogeille
- ✅ Vähemmän boilerplate-koodia
- ✅ Helpompi ylläpito

---

## 📊 Suosittu järjestys (Swing UI säilytetty)

### Tällä viikolla (4-6h):
1. ✅ **Migroi 2-3 dialogia** Kotliniin
   - DebugInfoDialogFX
   - KeyboardShortcutsDialogFX
   - (Valinnainen: PropertiesDialogFX)

2. ✅ **Lisää 1-2 DAO-testiä**
   - EntryDAOTest
   - DocumentDAOTest

### Ennen releasea:
3. ✅ **Manuaalinen testaus** - Varmista että JavaFX-UI toimii
4. ✅ **Dokumentaation päivitys** - Selitä molemmat UI:t

### Tulevaisuudessa:
5. ✅ **Kotlin BaseDialog-pohja** - Yhtenäinen malli
6. ✅ **Jatka migraatiota** - Lisää dialogeja Kotliniin

---

## 🎯 Nopein voitto (suosittelen aloittamaan tästä)

**Vaihe 1: Migroi DebugInfoDialogFX (1-2h)**
- Yksinkertaisin dialogi
- Nopea voitto
- Hyvä harjoitus

**Vaihe 2: Lisää EntryDAOTest (1-2h)**
- Kriittinen business-logiikka
- Korkea arvo
- Laajentaa testikattavuutta

**Tämä antaa:**
- ✅ Nopean tuloksen (2-4h)
- ✅ Parantaa koodin laatua
- ✅ Ei vaikuta Swing-UI:hin
- ✅ Jatkaa modernisointia

---

## 💡 Miksi ei poisteta legacy-koodia nyt?

**Hyvät syyt pitää molemmat UI:t:**
- ✅ Käyttäjät voivat valita mieluisan UI:n
- ✅ Swing-UI voi olla hyödyllinen tietyissä tilanteissa
- ✅ Ei tarvitse pakottaa migraatiota
- ✅ Voi poistaa myöhemmin jos halutaan

**Kun poistaa (tulevaisuudessa):**
- Kun kaikki käyttäjät ovat migroituneet JavaFX:ään
- Kun Swing-UI:n ylläpito on liian raskasta
- Kun haluat yksinkertaistaa koodikantaa

**Nyt:**
- Keskittyä JavaFX-UI:n parantamiseen
- Migroida koodia Kotliniin
- Laajentaa testikattavuutta
- Ei poistaa legacy-koodia

---

## 🚀 Aloita tästä

**Suosittelen aloittamaan:**
1. **Migroi DebugInfoDialogFX Kotliniin** - 1-2h, nopea voitto
2. **Lisää EntryDAOTest** - 1-2h, korkea arvo

Tämä antaa nopean tuloksen ja parantaa koodin laatua ilman että vaikuttaa Swing-UI:hin!

---

**Kysymys:** Haluatko että aloitan DebugInfoDialogFX:n migraation Kotliniin?
