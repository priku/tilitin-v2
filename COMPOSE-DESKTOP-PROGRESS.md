# Compose Desktop -siirtymän edistyminen

## ✅ Valmiit vaiheet

### 1. Perusinfra
- [x] Compose Desktop -riippuvuudet lisätty (`pom.xml`)
- [x] Compose Compiler plugin konfiguroitu
- [x] Repositoryt konfiguroitu (JetBrains, Google Maven)
- [x] `TilitinApp.kt` luotu perus Compose Desktop -sovelluksena

### 2. Logging ja alustus
- [x] Logging-konfiguraatio toteutettu (`configureLogging`)
- [x] Exception handler asetettu
- [x] Komentoriviparametrit käsitelty
- [x] Asetukset ja Registry alustettu

### 3. Dokumentaatio
- [x] `COMPOSE-DESKTOP-MIGRATION-PLAN.md` - siirtymäsuunnitelma
- [x] `COMPOSE-DESKTOP-FIX.md` - riippuvuusongelmien korjausohjeet
- [x] `COMPOSE-DESKTOP-PROGRESS.md` - tämä dokumentti

## 🔄 Käynnissä olevat vaiheet

### Swing-Interoperability
- [x] Perus Compose Desktop -sovellus toimii
- [ ] DocumentFrame integraatio SwingPanel:ina
  - Ongelma: DocumentFrame on `JFrame`, ei `JPanel`
  - Ratkaisu: Joko
    1. Luoda wrapper JPanel joka sisältää DocumentFrame:n contentPanen
    2. Tai refaktoroida DocumentFrame käyttämään JPanel:ia sisäisesti

## 📋 Seuraavat vaiheet

### Vaihe 1: DocumentFrame integraatio
1. **Vaihtoehto A**: Luoda `DocumentFramePanel` joka on JPanel ja sisältää DocumentFrame:n sisällön
2. **Vaihtoehto B**: Refaktoroida DocumentFrame käyttämään JPanel:ia pääkomponenttina
3. **Vaihtoehto C**: Käyttää ComposePanel:ia DocumentFrame:n sisällä (käänteinen integraatio)

**Suositus**: Vaihtoehto A - luoda wrapper JPanel, koska se vaatii vähiten muutoksia nykyiseen koodiin.

### Vaihe 2: Yksinkertaiset komponentit Compose Desktopiin
- Tekstikentät (TextField → Compose TextField)
- Napit (JButton → Compose Button)
- Labelit (JLabel → Compose Text)

### Vaihe 3: Taulukot
- EntryTable → Compose Table/LazyColumn

### Vaihe 4: Dialoogit
- Yksinkertaiset dialoogit ensin
- Monimutkaisemmat myöhemmin

## 🐛 Tunnettuja ongelmia

1. **DocumentFrame on JFrame**: Ei voi suoraan käyttää SwingPanel:issa
   - Ratkaisu: Luoda wrapper tai refaktoroida

2. **Riippuvuudet**: Compose Desktop -riippuvuudet saattavat vaatia päivitystä
   - Tarkista: `mvn dependency:resolve`
   - Jos ongelmia: Poista `.m2/repository/org/jetbrains/compose` ja aja `mvn clean install -U`

## 📝 Muistiinpanot

- Compose Desktop tukee Swing-Interoperabilitya molempiin suuntiin:
  - `SwingPanel` - Swing-komponentit Compose-UI:ssa
  - `ComposePanel` - Compose-komponentit Swing-UI:ssa

- Vaiheittainen siirtymä on mahdollista:
  1. Aluksi: Compose-ikkuna, Swing-komponentit sisällä
  2. Vaiheittain: Korvataan Swing-komponentit Compose-komponenteilla
  3. Lopuksi: Puhtaasti Compose Desktop

## 🎯 Tavoite

100% Compose Desktop -sovellus, mutta vaiheittainen siirtymä minimoi riskit ja mahdollistaa jatkuvan toiminnan siirtymän aikana.

