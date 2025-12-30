# JavaFX Migration Progress

## Status: VALMIS ✅ - 95% Complete!

**Aloitettu:** 2025-12-31
**Valmis:** 2025-12-31

### ✅ Käyttövalmis!
Kaikki perustoiminnot on toteutettu. Sovellus on käyttövalmis.

---

## ✅ Phase 1: Perustoiminnot (VALMIS)

| Toiminto | Status | Tiedosto |
|----------|--------|----------|
| Application bootstrap | ✅ | `JavaFXApp.java` |
| FXML layout | ✅ | `MainView.fxml` |
| Modern CSS theme | ✅ | `styles.css` |
| Database open (SQLite) | ✅ | `MainController.java` |
| Period loading | ✅ | `MainController.java` |
| Document loading | ✅ | `MainController.java` |
| Entry loading | ✅ | `MainController.java` |
| Navigation (◀▶⏮⏭) | ✅ | `MainController.java` |
| Go to document number | ✅ | `MainController.java` |
| Search documents | ✅ | `MainController.java` |
| New document | ✅ | `MainController.java` |
| Editable entry table | ✅ | `EntryRowModel.java` |
| Account autocomplete | ✅ | `AccountTableCell.java` |
| Amount editing | ✅ | `AmountTableCell.java` |
| Description editing | ✅ | `DescriptionTableCell.java` |
| Save (Ctrl+S) | ✅ | `MainController.java` |
| Auto-save on navigation | ✅ | `MainController.java` |
| About dialog | ✅ | `MainController.java` |

---

## ✅ Phase 2: Kriittiset toiminnot (VALMIS)

| Toiminto | Status |
|----------|--------|
| F9 account quick search | ✅ |
| VAT handling | ✅ |
| Delete document | ✅ |
| Copy/Paste entries | ✅ |
| Date picker validation | ✅ |

---

## ✅ Phase 3: Dialogit (VALMIS)

| Dialogi | Status |
|---------|--------|
| AccountSelectionDialogFX | ✅ F9-haku |
| COADialogFX | ✅ Tilikartta |
| DocumentTypeDialogFX | ✅ Tositelajit |
| SettingsDialogFX | ✅ Asetukset |
| Period info | ✅ Info-dialogi |
| Database info | ✅ Info-dialogi |

---

## ✅ Phase 4: Raportit (VALMIS)

| Raportti | Status |
|----------|--------|
| Päiväkirja | ✅ |
| Pääkirja | ✅ |
| Tuloslaskelma | ✅ |
| Tase | ✅ |
| Export to .txt | ✅ |
| Print | ✅ |

---

## ✅ Phase 5: Keyboard Shortcuts (VALMIS)

| Shortcut | Toiminto |
|----------|----------|
| Ctrl+N | Uusi tosite |
| Ctrl+S | Tallenna |
| Ctrl+P | Tulosta |
| Ctrl+O | Avaa tietokanta |
| Ctrl+←/→ | Navigoi tositteissa |
| F9 | Tilikartan pikahaku |
| PageUp/Down | Navigoi tositteissa |
| Delete | Poista vienti |

---

## ⏳ Myöhemmin (ei kriittisiä)

| Toiminto | Prioriteetti |
|----------|--------------|
| Liitteet (attachments) | 🟡 |
| CSV-tuonti | 🟡 |
| Varmuuskopiointi | 🟢 |
| ALV-laskelma | 🟢 |
| Print preview | 🟢 |
| Tiliote-raportti | 🟢 |

---

## 📁 Tiedostorakenne

```
src/main/java/kirjanpito/ui/javafx/
├── JavaFXApp.java              # Application entry point
├── MainController.java         # Main window controller (~1100 lines)
├── EntryRowModel.java          # Entry table model
├── cells/
│   ├── AccountTableCell.java   # Account autocomplete cell
│   ├── AmountTableCell.java    # Currency amount cell
│   └── DescriptionTableCell.java # Text cell
└── dialogs/
    ├── AccountSelectionDialogFX.java  # F9 quick search
    ├── COADialogFX.java               # Chart of accounts
    ├── DocumentTypeDialogFX.java      # Document types
    ├── ReportDialogFX.java            # Reports
    └── SettingsDialogFX.java          # Settings

src/main/resources/fxml/
├── MainView.fxml               # Main window layout
└── styles.css                  # CSS theme
```

---

## 📊 Edistyminen

| Kategoria | Valmis | Yhteensä | % |
|-----------|--------|----------|---|
| Perustoiminnot | 18 | 18 | 100% |
| Kriittiset | 5 | 5 | 100% |
| Dialogit | 6 | 6 | 100% |
| Raportit | 6 | 6 | 100% |
| Shortcuts | 8 | 8 | 100% |
| **Yhteensä** | **43** | **43** | **100%** |

> Myöhemmin-tehtävät ovat lisäominaisuuksia, eivät kriittisiä.

---

## Käynnistys

```bash
# JavaFX-sovellus
./gradlew runJavaFX

# Vanha Swing-versio (varmuuskopio)
./gradlew run
```
