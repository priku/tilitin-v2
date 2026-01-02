# Release Notes - Tilitin v2.2.0

**Julkaisupäivä:** 2026-01-02  
**Versio:** 2.2.0  
**Koodinimi:** "Testaus ja Laatu"

---

## 🎉 Yhteenveto

Tilitin v2.2.0 tuo merkittävän parannuksen testikattavuuteen ja koodin laatuun. Tämä versio keskittyy vakauteen ja luotettavuuteen ennen uusia ominaisuuksia.

---

## ✨ Uudet ominaisuudet

### Testaus-infrastruktuuri laajennettu
- **58 automaattista testiä** (aikaisemmin 26)
- **8 DAO-testiluokkaa** - Kattava tietokantakerroksen testaus
- **2 Model-testiluokkaa** - Business-logiikan testaus
- **1 Integraatiotesti** - End-to-end -työnkulku

### Kotlin-migraatio jatkuu
- **DebugInfoDialogFX** migroitu Java → Kotlin
- **KeyboardShortcutsDialogFX** migroitu Java → Kotlin
- **7 dialogia Kotlinissa** (aikaisemmin 5)
- **Kotlin-prosentti:** ~8.2%

---

## 🔧 Parannukset

### Testikattavuus
- **DAO-taso:** ~60-70% (aikaisemmin ~40-50%)
- **Model-taso:** ~20-30% (uusi)
- **Kokonaisuus:** ~10-15% (aikaisemmin ~5-10%)

### Koodin laatu
- Parannettu null-safety Kotlin-dialogeissa
- Yhtenäisempi testaus-infrastruktuuri
- Parempi dokumentaatio testauksesta

### Dokumentaatio
- Päivitetty TESTING-GUIDE.md
- Luotu MANUAL-TESTING-CHECKLIST.md
- Päivitetty CHANGELOG.md
- Korjattu dialogilaskelmat dokumentaatiossa

---

## 📊 Testit

### DAO-testit (42 testiä)
- ✅ AccountDAOTest (5 testiä)
- ✅ AttachmentDAOTest (6 testiä)
- ✅ DocumentDAOTest (5 testiä)
- ✅ DocumentTypeDAOTest (5 testiä)
- ✅ EntryDAOTest (6 testiä)
- ✅ EntryTemplateDAOTest (5 testiä)
- ✅ PeriodDAOTest (5 testiä)
- ✅ SettingsDAOTest (5 testiä)

### Model-testit (12 testiä)
- ✅ DocumentModelTest (5 testiä)
- ✅ PropertiesModelTest (7 testiä)

### Integraatiotestit (4 testiä)
- ✅ DocumentWorkflowTest

**Yhteensä: 58 testiä** ✅

---

## 🐛 Korjaukset

### Testien korjaukset
- ✅ AttachmentDAOTest korjattu - testit menevät läpi
- ✅ PropertiesModelTest korjattu - registry.fetchSettings() lisätty
- ✅ Kaikki testit menevät läpi ilman virheitä

### Dokumentaation korjaukset
- ✅ Dialogilaskelmat päivitetty (31 dialogia)
- ✅ Kotlin-versio päivitetty (2.2.0)
- ✅ Testikattavuus dokumentoitu

---

## 📝 Muutokset

### Tiedostot
- ✅ `build.gradle.kts` - Versio päivitetty 2.2.0
- ✅ `CHANGELOG.md` - Päivitetty v2.2.0
- ✅ `README.md` - Kotlin-versio päivitetty
- ✅ `TESTING-GUIDE.md` - Päivitetty testien määrä
- ✅ `MANUAL-TESTING-CHECKLIST.md` - Uusi tiedosto

### Koodi
- ✅ Testit korjattu ja laajennettu
- ✅ Dokumentaatio päivitetty
- ✅ Ei muutoksia käyttöliittymään

---

## 🚀 Seuraavat vaiheet

### Ennen seuraavaa releasea:
1. **Manuaalinen testaus** - Käytä MANUAL-TESTING-CHECKLIST.md
2. **Bugien korjaus** - Korjaa löydetyt ongelmat
3. **Release** - Luoda tag ja julkaista

### Tulevaisuudessa:
- Lisää Model-testejä
- Lisää Integraatiotestejä
- UI-testit (TestFX)

---

## 📦 Lataus

Lataa uusin versio:
- **Windows:** [Tilitin-2.2.0-setup.exe](https://github.com/priku/tilitin-v2/releases/tag/v2.2.0)
- **macOS:** [Tilitin-2.2.0.dmg](https://github.com/priku/tilitin-v2/releases/tag/v2.2.0)
- **Linux:** [tilitin_2.2.0_amd64.deb](https://github.com/priku/tilitin-v2/releases/tag/v2.2.0)
- **JAR:** [tilitin-2.2.0-all.jar](https://github.com/priku/tilitin-v2/releases/tag/v2.2.0)

---

## 🙏 Kiitokset

Kiitos kaikille testaajille ja kehittäjille, jotka ovat auttaneet parantamaan Tilitin-laatu!

---

## 📚 Lisätietoja

- **Käyttöohje:** [USER-GUIDE.md](USER-GUIDE.md)
- **Testausohje:** [TESTING-GUIDE.md](TESTING-GUIDE.md)
- **Manuaalinen testaus:** [MANUAL-TESTING-CHECKLIST.md](MANUAL-TESTING-CHECKLIST.md)
- **Versiohistoria:** [CHANGELOG.md](CHANGELOG.md)

---

**Tilitin v2.2.0** - Testaus ja Laatu
