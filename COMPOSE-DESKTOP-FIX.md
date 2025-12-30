# Compose Desktop -riippuvuusongelmien korjaus

## Ongelma

Maven ei löydä Compose Desktop -riippuvuuksia. Virheet viittaavat siihen, että:
- `androidx.compose.*` -artifaktit eivät löydy
- Versiot ovat ristiriidassa (1.7.1 vs 1.7.5)

## Ratkaisu

### 1. Lisätty repositoryt
- JetBrains Compose repository
- Google Maven repository (androidx.compose.* -artifakteille)
- Maven Central

### 2. Compose Compiler Plugin
- Lisätty Compose Compiler plugin Kotlin pluginin yhteyteen
- Versio: 1.7.1 (sama kuin Compose Desktop)

### 3. Plugin Repositories
- Lisätty pluginRepositories JetBrains Compose repositorylle

## Seuraavat askeleet

1. **Päivitä Maven projektit**: 
   ```
   mvn clean
   mvn dependency:resolve
   ```

2. **Jos ongelmat jatkuvat**, kokeile:
   - Poista `.m2/repository/org/jetbrains/compose` -kansio
   - Aja `mvn clean install -U` (force update)

3. **Vaihtoehtoinen ratkaisu**: 
   Jos Compose Desktop 1.7.1 ei toimi, kokeile uudempaa versiota:
   ```xml
   <compose.version>1.7.5</compose.version>
   ```

## Tarkistus

Kun riippuvuudet on korjattu, tarkista että:
- `mvn compile` toimii ilman virheitä
- IDE:n Problems-paneeli on tyhjä
- `TilitinApp.kt` kääntyy onnistuneesti

