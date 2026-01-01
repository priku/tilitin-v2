package kirjanpito.ui.javafx.dialogs;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import kirjanpito.db.Account;
import kirjanpito.db.Period;
import kirjanpito.util.AppSettings;
import kirjanpito.util.ChartOfAccounts;
import kirjanpito.util.Registry;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

/**
 * JavaFX dialog for account statement (tiliote) report options.
 * Allows selection of account, date range, and order options.
 * Ports functionality from AccountStatementOptionsDialog (Swing).
 */
public class AccountStatementOptionsDialogFX {

    private Stage dialog;
    private Registry registry;
    private ChartOfAccounts coa;
    
    // UI Components
    private TextField searchField;
    private TableView<AccountRow> accountTable;
    private ObservableList<AccountRow> allAccounts;
    private FilteredList<AccountRow> filteredAccounts;
    private CheckBox hideNonFavAccountsCheckBox;
    private RadioButton orderByNumberRadio;
    private RadioButton orderByDateRadio;
    
    // Date selection
    private DatePicker startDatePicker;
    private DatePicker endDatePicker;
    private RadioButton periodRadio;
    private RadioButton customRadio;
    
    // Result
    private Account selectedAccount;
    private Period period;
    private Date startDate;
    private Date endDate;
    private boolean okPressed = false;

    public AccountStatementOptionsDialogFX(Window owner, Registry registry) {
        this.registry = registry;
        this.coa = registry.getChartOfAccounts();
        
        dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(owner);
        dialog.setTitle("Tiliote");
        dialog.setMinWidth(550);
        dialog.setMinHeight(550);

        createContent();
        loadSettings();
    }

    private void createContent() {
        VBox root = new VBox(12);
        root.setPadding(new Insets(16));
        root.setStyle("-fx-background-color: #ffffff;");

        // Date selection section
        VBox dateSection = createDateSection();
        
        // Account selection section
        VBox accountSection = createAccountSection();
        VBox.setVgrow(accountSection, Priority.ALWAYS);
        
        // Order options
        HBox orderSection = createOrderSection();

        // Buttons
        HBox buttonBox = createButtonBox();

        root.getChildren().addAll(dateSection, accountSection, orderSection, buttonBox);

        Scene scene = new Scene(root, 600, 600);
        
        // F9 shortcut for favorites toggle
        scene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.F9) {
                hideNonFavAccountsCheckBox.setSelected(!hideNonFavAccountsCheckBox.isSelected());
            }
        });
        
        dialog.setScene(scene);
    }
    
    private VBox createDateSection() {
        Label dateLabel = new Label("Aikaväli");
        dateLabel.setStyle("-fx-font-weight: bold;");

        VBox dateSection = new VBox(8);
        dateSection.setPadding(new Insets(10));
        dateSection.setStyle("-fx-border-color: #e0e0e0; -fx-border-radius: 4; -fx-background-color: #f9fafb; -fx-background-radius: 4;");

        // Period radio
        periodRadio = new RadioButton("Koko tilikausi");
        periodRadio.setSelected(true);

        // Custom date range radio
        customRadio = new RadioButton("Aikaväli:");

        ToggleGroup dateGroup = new ToggleGroup();
        periodRadio.setToggleGroup(dateGroup);
        customRadio.setToggleGroup(dateGroup);

        // Date pickers
        HBox datePickerBox = new HBox(10);
        datePickerBox.setAlignment(Pos.CENTER_LEFT);
        datePickerBox.setPadding(new Insets(0, 0, 0, 30));

        startDatePicker = new DatePicker();
        startDatePicker.setPromptText("Alkupvm");
        startDatePicker.setPrefWidth(140);
        startDatePicker.setDisable(true);

        Label toLabel = new Label("—");

        endDatePicker = new DatePicker();
        endDatePicker.setPromptText("Loppupvm");
        endDatePicker.setPrefWidth(140);
        endDatePicker.setDisable(true);

        datePickerBox.getChildren().addAll(startDatePicker, toLabel, endDatePicker);

        // Enable/disable date pickers
        periodRadio.selectedProperty().addListener((obs, oldVal, newVal) -> {
            startDatePicker.setDisable(newVal);
            endDatePicker.setDisable(newVal);
        });

        dateSection.getChildren().addAll(periodRadio, customRadio, datePickerBox);
        
        VBox wrapper = new VBox(4);
        wrapper.getChildren().addAll(dateLabel, dateSection);
        return wrapper;
    }
    
    private VBox createAccountSection() {
        Label accountLabel = new Label("Tili");
        accountLabel.setStyle("-fx-font-weight: bold;");

        VBox accountSection = new VBox(8);
        accountSection.setPadding(new Insets(10));
        accountSection.setStyle("-fx-border-color: #e0e0e0; -fx-border-radius: 4; -fx-background-color: #f9fafb; -fx-background-radius: 4;");

        // Search field
        HBox searchBox = new HBox(10);
        searchBox.setAlignment(Pos.CENTER_LEFT);

        Label searchLabel = new Label("Haku:");
        searchField = new TextField();
        searchField.setPromptText("Tilinumero tai nimi...");
        HBox.setHgrow(searchField, Priority.ALWAYS);

        hideNonFavAccountsCheckBox = new CheckBox("Vain suosikkitilit (F9)");

        searchBox.getChildren().addAll(searchLabel, searchField, hideNonFavAccountsCheckBox);

        // Account table
        accountTable = new TableView<>();
        accountTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(accountTable, Priority.ALWAYS);

        TableColumn<AccountRow, String> numberCol = new TableColumn<>("Numero");
        numberCol.setCellValueFactory(new PropertyValueFactory<>("number"));
        numberCol.setPrefWidth(80);
        numberCol.setMinWidth(60);

        TableColumn<AccountRow, String> nameCol = new TableColumn<>("Nimi");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setPrefWidth(350);

        accountTable.getColumns().addAll(numberCol, nameCol);

        // Set up filtering
        setupAccountFiltering();

        // Event handlers
        setupAccountEventHandlers();

        accountSection.getChildren().addAll(searchBox, accountTable);
        
        VBox wrapper = new VBox(4);
        wrapper.getChildren().addAll(accountLabel, accountSection);
        VBox.setVgrow(wrapper, Priority.ALWAYS);
        VBox.setVgrow(accountSection, Priority.ALWAYS);
        return wrapper;
    }
    
    private void setupAccountFiltering() {
        allAccounts = FXCollections.observableArrayList();
        refreshAccountList();
        
        filteredAccounts = new FilteredList<>(allAccounts, p -> true);
        accountTable.setItems(filteredAccounts);
        
        // Search filter
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            filterAccounts();
        });
        
        // Favorites filter
        hideNonFavAccountsCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            refreshAccountList();
            filterAccounts();
            saveSettings();
        });
    }
    
    private void refreshAccountList() {
        allAccounts.clear();
        
        boolean favoritesOnly = hideNonFavAccountsCheckBox.isSelected();
        ChartOfAccounts source;
        
        if (favoritesOnly) {
            source = new ChartOfAccounts();
            source.set(registry.getAccounts(), registry.getCOAHeadings());
            source.filterNonFavouriteAccounts();
        } else {
            source = registry.getChartOfAccounts();
        }
        
        this.coa = source;
        
        for (int i = 0; i < source.getSize(); i++) {
            if (source.getType(i) == ChartOfAccounts.TYPE_ACCOUNT) {
                Account account = source.getAccount(i);
                allAccounts.add(new AccountRow(account));
            }
        }
    }
    
    private void filterAccounts() {
        String searchText = searchField.getText().toLowerCase().trim();
        
        filteredAccounts.setPredicate(row -> {
            if (searchText.isEmpty()) {
                return true;
            }
            return row.getNumber().toLowerCase().contains(searchText) ||
                   row.getName().toLowerCase().contains(searchText);
        });
        
        // Select first match
        if (!filteredAccounts.isEmpty()) {
            accountTable.getSelectionModel().selectFirst();
        }
    }
    
    private void setupAccountEventHandlers() {
        // Double-click to select
        accountTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                handleOK();
            }
        });

        // Enter to select, Up/Down from search field
        searchField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.DOWN) {
                accountTable.requestFocus();
                if (!filteredAccounts.isEmpty()) {
                    accountTable.getSelectionModel().selectFirst();
                }
            } else if (event.getCode() == KeyCode.UP) {
                accountTable.requestFocus();
                if (!filteredAccounts.isEmpty()) {
                    accountTable.getSelectionModel().selectLast();
                }
            } else if (event.getCode() == KeyCode.ENTER) {
                handleOK();
            }
        });

        accountTable.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                handleOK();
            }
        });
    }
    
    private HBox createOrderSection() {
        HBox orderSection = new HBox(20);
        orderSection.setPadding(new Insets(10));
        orderSection.setAlignment(Pos.CENTER_LEFT);

        Label orderLabel = new Label("Järjestys:");
        orderLabel.setStyle("-fx-font-weight: bold;");

        orderByNumberRadio = new RadioButton("Tositenumerojärjestys");
        orderByDateRadio = new RadioButton("Aikajärjestys");

        ToggleGroup orderGroup = new ToggleGroup();
        orderByNumberRadio.setToggleGroup(orderGroup);
        orderByDateRadio.setToggleGroup(orderGroup);
        orderByNumberRadio.setSelected(true);

        orderSection.getChildren().addAll(orderLabel, orderByNumberRadio, orderByDateRadio);
        return orderSection;
    }
    
    private HBox createButtonBox() {
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.setPadding(new Insets(10, 0, 0, 0));

        Button okButton = new Button("OK");
        okButton.setDefaultButton(true);
        okButton.setStyle("-fx-background-color: #2563eb; -fx-text-fill: white; -fx-min-width: 80;");
        okButton.setOnAction(e -> handleOK());

        Button cancelButton = new Button("Peruuta");
        cancelButton.setCancelButton(true);
        cancelButton.setStyle("-fx-min-width: 80;");
        cancelButton.setOnAction(e -> dialog.close());

        buttonBox.getChildren().addAll(cancelButton, okButton);
        return buttonBox;
    }

    private void handleOK() {
        // Get selected account
        AccountRow selected = accountTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Virhe", "Valitse tili listasta");
            return;
        }
        selectedAccount = selected.getAccount();

        // Validate dates if custom range selected
        if (customRadio.isSelected()) {
            if (startDatePicker.getValue() == null || endDatePicker.getValue() == null) {
                showError("Virhe", "Valitse sekä alku- että loppupäivämäärä");
                return;
            }

            LocalDate start = startDatePicker.getValue();
            LocalDate end = endDatePicker.getValue();

            if (start.isAfter(end)) {
                showError("Virhe", "Alkupäivämäärä ei voi olla loppupäivämäärän jälkeen");
                return;
            }

            startDate = Date.from(start.atStartOfDay(ZoneId.systemDefault()).toInstant());
            endDate = Date.from(end.atStartOfDay(ZoneId.systemDefault()).toInstant());
        } else if (period != null) {
            startDate = period.getStartDate();
            endDate = period.getEndDate();
        } else {
            showError("Virhe", "Tilikautta ei ole asetettu");
            return;
        }

        okPressed = true;
        dialog.close();
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.initOwner(dialog);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void loadSettings() {
        AppSettings settings = AppSettings.getInstance();
        hideNonFavAccountsCheckBox.setSelected(
            settings.getBoolean("account-selection.hide-non-favourite-accounts", false));
    }
    
    private void saveSettings() {
        AppSettings settings = AppSettings.getInstance();
        settings.set("account-selection.hide-non-favourite-accounts", 
            hideNonFavAccountsCheckBox.isSelected());
    }

    // Public API

    public void setPeriod(Period period) {
        this.period = period;
        if (period != null) {
            LocalDate start = period.getStartDate().toInstant()
                .atZone(ZoneId.systemDefault()).toLocalDate();
            LocalDate end = period.getEndDate().toInstant()
                .atZone(ZoneId.systemDefault()).toLocalDate();

            startDatePicker.setValue(start);
            endDatePicker.setValue(end);
        }
    }

    public Account getSelectedAccount() {
        return selectedAccount;
    }

    public void selectAccount(Account account) {
        if (account == null) return;
        
        for (AccountRow row : allAccounts) {
            if (row.getAccount().getId() == account.getId()) {
                accountTable.getSelectionModel().select(row);
                accountTable.scrollTo(row);
                return;
            }
        }
        
        // If not found and favorites filter is on, try disabling it
        if (hideNonFavAccountsCheckBox.isSelected()) {
            hideNonFavAccountsCheckBox.setSelected(false);
            selectAccount(account);
        }
    }

    public Date getStartDate() {
        return startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public boolean isOrderByDate() {
        return orderByDateRadio.isSelected();
    }

    public void setOrderByDate(boolean orderByDate) {
        orderByDateRadio.setSelected(orderByDate);
        orderByNumberRadio.setSelected(!orderByDate);
    }

    public boolean showAndWait() {
        Platform.runLater(() -> {
            searchField.requestFocus();
        });
        dialog.showAndWait();
        return okPressed;
    }

    /**
     * Static factory method.
     */
    public static AccountStatementOptionsDialogFX create(Window owner, Registry registry, Period period) {
        AccountStatementOptionsDialogFX dialog = new AccountStatementOptionsDialogFX(owner, registry);
        dialog.setPeriod(period);
        return dialog;
    }

    /**
     * Row model for the account table.
     */
    public static class AccountRow {
        private final Account account;

        public AccountRow(Account account) {
            this.account = account;
        }

        public Account getAccount() {
            return account;
        }

        public String getNumber() {
            return account.getNumber();
        }

        public String getName() {
            return account.getName();
        }
    }
}
