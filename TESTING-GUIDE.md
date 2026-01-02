# Testaus-opas

**Päivämäärä:** 2026-01-02  
**Tyyppi:** Testien käyttöohje

---

## 🎯 Miksi testit?

### 1. Automaattinen validointi
Testit varmistavat että koodi toimii oikein. Ajetaan ennen jokaista commitia.

### 2. Turvallinen refaktorointi
Voit muuttaa koodia ilman pelkoa - testit kertovat jos jotain hajosi.

### 3. Dokumentaatio
Testit näyttävät miten API:a käytetään - ne ovat eläviä esimerkkejä.

### 4. Bugien esto
Testit löytävät ongelmat ennen releasea ja estävät regressioita.

---

## 🚀 Testien ajaminen

### Kaikki testit
```bash
./gradlew test
```

### Yksittäinen testiluokka
```bash
./gradlew test --tests "kirjanpito.db.EntryDAOTest"
```

### Yksittäinen testi
```bash
./gradlew test --tests "kirjanpito.db.EntryDAOTest.test create and retrieve entry"
```

### Testien ajaminen ilman daemonia
```bash
./gradlew test --no-daemon
```

---

## 📊 Testiraportit

Testien jälkeen raportit löytyvät:
- **HTML-raportti:** `build/reports/tests/test/index.html`
- **XML-raportti:** `build/test-results/test/`

Avaa HTML-raportti selaimessa nähdäksesi:
- Testien tulokset
- Epäonnistuneet testit
- Suoritusajat
- Stack traces

---

## ✅ Nykyiset testit

### DAO-testit (42 testiä)
- **AccountDAOTest** - Tilit (5 testiä)
- **AttachmentDAOTest** - PDF-liitteet (6 testiä)
- **DocumentDAOTest** - Tositteet (5 testiä)
- **DocumentTypeDAOTest** - Tositetyypit (5 testiä)
- **EntryDAOTest** - Viennit (6 testiä)
- **EntryTemplateDAOTest** - Vientipohjat (5 testiä)
- **PeriodDAOTest** - Tilikaudet (5 testiä)
- **SettingsDAOTest** - Asetukset (5 testiä)

### Model-testit (12 testiä)
- **DocumentModelTest** - Dokumenttimalli (5 testiä)
- **PropertiesModelTest** - Ominaisuudet-malli (7 testiä)

### Integraatiotestit (4 testiä)
- **DocumentWorkflowTest** - Dokumenttityönkulku

**Yhteensä: 58 testiä**

---

## 🔧 Testien rakenne

Jokainen testi:
1. **@BeforeAll** - Luo väliaikaisen tietokannan
2. **@Test** - Suorittaa testin
3. **@AfterAll** - Siivoaa väliaikaisen tietokannan

### Esimerkki:
```kotlin
@Test
fun `test create and retrieve entry`() {
    val session = dataSource.openSession()
    try {
        val entryDAO = dataSource.getEntryDAO(session)
        
        // Create entry
        val entry = Entry().apply {
            setDocumentId(testDocument.id)
            setAccountId(testAccount.id)
            setDescription("Test entry")
            setDebit(true)
            setAmount(BigDecimal("100.00"))
        }
        
        entryDAO.save(entry)
        session.commit()
        
        // Verify
        assertTrue(entry.id > 0)
        // ... more assertions
    } finally {
        session.close()
    }
}
```

---

## 📈 Testikattavuus

### Nykyinen kattavuus
- **DAO-taso:** ~60-70% (8/10 DAO:ta testattu)
- **Model-taso:** ~20-30% (2/10+ modelia testattu)
- **Kokonaisuus:** ~10-15%

### Tavoite
- **DAO-taso:** 100% (kaikki DAO:t testattu)
- **Model-taso:** 60%+
- **Kokonaisuus:** 30%+

---

## 🎯 Seuraavat vaiheet

### 1. Lisää DAO-testejä (2 jäljellä)
- COAHeadingDAOTest
- ReportStructureDAOTest

### 2. Model-testit (8+ jäljellä)
- StartingBalanceModelTest
- EntryTemplateModelTest
- ReportModelTest
- CSVImportModelTest
- BackupModelTest
- jne.

### 3. Integraatiotestit
- CSV Import workflow
- Report generation workflow
- Backup/Restore workflow

### 3. Integraatiotestit
- CSV Import workflow
- Report generation workflow
- Backup/Restore workflow

### 4. UI-testit (TestFX)
- Dialog-testit
- Entry Table -testit
- Navigation-testit

---

## 💡 Best Practices

### 1. Testinimet
Käytä selkeitä nimiä:
```kotlin
fun `test create and retrieve entry`() // ✅ Hyvä
fun test1() // ❌ Huono
```

### 2. Assertions
Käytä selkeitä assertioneja:
```kotlin
assertNotNull(retrieved) // ✅
assertTrue(retrieved != null) // ❌
```

### 3. Siivous
Aina siivoa resursseja:
```kotlin
try {
    // Test code
} finally {
    session.close()
}
```

### 4. Itsenäisyys
Jokainen testi on itsenäinen - ei riippuvuuksia muihin testeihin.

---

## 🐛 Ongelmatilanteet

### Testit epäonnistuvat
1. Tarkista virheviestit
2. Aja yksittäinen testi debug-moodissa
3. Tarkista testiraportti

### Muisti loppuu
- Kasvata heap-kokoa: `-Xmx2048m`
- Tarkista että testit siivoavat resursseja

### Tietokanta-ongelmat
- Tarkista että testit käyttävät väliaikaista tietokantaa
- Varmista että `@AfterAll` siivoaa

---

## 📚 Lisätietoja

- **JUnit 5:** https://junit.org/junit5/
- **TestFX:** https://github.com/TestFX/TestFX
- **Kotlin Testing:** https://kotlinlang.org/docs/jvm-test-using-junit.html

---

**Päivitetty:** 2026-01-02
