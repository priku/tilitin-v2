package kirjanpito.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker.StateValue;
import javax.swing.UIManager;
import javax.swing.border.BevelBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.TableColumn;
import javax.swing.text.DefaultCaret;

import kirjanpito.util.BackupService;

import kirjanpito.db.Account;
import kirjanpito.db.DataAccessException;
import kirjanpito.db.Document;
import kirjanpito.db.DocumentType;
import kirjanpito.db.Entry;
import kirjanpito.db.EntryTemplate;
import kirjanpito.db.Period;
import kirjanpito.db.Settings;
import kirjanpito.models.COAModel;
import kirjanpito.models.CSVExportWorker;
import kirjanpito.models.DataSourceInitializationModel;
import kirjanpito.models.DataSourceInitializationWorker;
import kirjanpito.models.DocumentModel;
import kirjanpito.models.DocumentTypeModel;
import kirjanpito.models.EntryTableModel;
import kirjanpito.models.EntryTemplateModel;
import kirjanpito.models.PropertiesModel;
import kirjanpito.models.ReportEditorModel;
import kirjanpito.models.StartingBalanceModel;
import kirjanpito.models.StatisticsModel;
import kirjanpito.models.TextFieldWithLockIcon;
import kirjanpito.ui.resources.Resources;
import kirjanpito.util.AppSettings;
import kirjanpito.util.RecentDatabases;
import kirjanpito.util.Registry;
import kirjanpito.util.RegistryAdapter;

/**
 * Tositetietojen muokkausikkuna.
 *
 * @author Tommi Helineva
 */
public class DocumentFrame extends JFrame implements AccountSelectionListener,
		DocumentBackupManager.DatabaseOpener, DocumentExporter.CSVExportStarter,
		DocumentNavigator.NavigationCallbacks {
	protected Registry registry;
	protected DocumentModel model;
	private DocumentExporter documentExporter;
	private DocumentMenuHandler menuHandler = new DocumentMenuHandler(this);
	private DocumentNavigator documentNavigator;
	protected JMenu entryTemplateMenu;
	protected JMenu docTypeMenu;
	protected JMenu gotoMenu;
	protected JMenu reportsMenu;
	protected JMenu toolsMenu;
	protected JMenu recentMenu;
	private JMenuItem newDatabaseMenuItem;
	private JMenuItem openDatabaseMenuItem;
	private JMenuItem newDocMenuItem;
	private JMenuItem deleteDocMenuItem;
	private JMenuItem addEntryMenuItem;
	private JMenuItem removeEntryMenuItem;
	private JMenuItem pasteMenuItem;
	private JMenuItem coaMenuItem;
	private JMenuItem vatDocumentMenuItem;
	private JMenuItem editEntryTemplatesMenuItem;
	private JMenuItem createEntryTemplateMenuItem;
	private JMenuItem startingBalancesMenuItem;
	private JMenuItem propertiesMenuItem;
	private JMenuItem settingsMenuItem;
	private JCheckBoxMenuItem searchMenuItem;
	private JCheckBoxMenuItem[] docTypeMenuItems;
	private JMenuItem editDocTypesMenuItem;
	private JMenuItem setIgnoreFlagMenuItem;
	private JButton prevButton;
	private JButton nextButton;
	private JButton searchButton;
	private JButton findByNumberButton;
	private JButton newDocButton;
	private JButton addEntryButton;
	private JButton removeEntryButton;
	private TextFieldWithLockIcon numberTextField;
	private DateTextField dateTextField;
	private JLabel debitTotalLabel;
	private JLabel creditTotalLabel;
	private JLabel differenceLabel;
	private JLabel documentLabel;
	private JLabel periodLabel;
	private JLabel documentTypeLabel;
	private JLabel backupStatusLabel;
	private JTable entryTable;
	private AttachmentsPanel attachmentsPanel;
	private TableColumn vatColumn;
	private JPanel searchPanel;
	private JTextField searchPhraseTextField;
	private EntryTableModel tableModel;
	private AccountCellRenderer accountCellRenderer;
	private AccountCellEditor accountCellEditor;
	private DescriptionCellEditor descriptionCellEditor;
	private DecimalFormat formatter;
	private AccountSelectionDialog accountSelectionDialog;
	private boolean searchEnabled;
	private BigDecimal debitTotal;
	private BigDecimal creditTotal;
	
	// Builder instances for menu and toolbar
	private DocumentMenuBuilder menuBuilder;
	private DocumentToolbarBuilder toolbarBuilder;
	
	// Helper for entry table actions
	private EntryTableActions entryTableActions;
	
	// Table manager for entry table
	private DocumentTableManager tableManager;
	
	// Print manager
	private DocumentPrinter documentPrinter;
	
	// UI managers for Phase 7 refactoring
	private DocumentUIBuilder uiBuilder;
	private DocumentUIUpdater uiUpdater;
	private DocumentUIBuilder.UIComponents uiComponents;
	private DocumentUIUpdater.UIComponents uiUpdaterComponents;

	// File filter for SQLite databases
	FileFilter sqliteFileFilter = new FileFilter() {
		@Override
		public boolean accept(File file) {
			return file.isDirectory() || file.getName().toLowerCase().endsWith(".sqlite");
		}

		@Override
		public String getDescription() {
			return "Tilitin-tietokannat (.sqlite)";
		}
	}; // Note: FileFilter has two methods, cannot be converted to lambda

	// Action listeners - delegated to DocumentMenuHandler
	/* Uusi tietokanta */
	private ActionListener newDatabaseListener = menuHandler.getNewDatabaseListener();

	/* Avaa tietokanta */
	private ActionListener openDatabaseListener = menuHandler.getOpenDatabaseListener();

	/* Tietokanta-asetukset */
	private ActionListener databaseSettingsListener = menuHandler.getDatabaseSettingsListener();
	
	// Column mapper for DocumentTableManager
	// This will be initialized after tableManager is created
	private DocumentTableManager.ColumnMapper columnMapper = new DocumentTableManager.ColumnMapper() {
		@Override
		public int mapColumnIndexToModel(int viewIndex) {
			return DocumentFrame.this.mapColumnIndexToModel(viewIndex);
		}
		
		@Override
		public int mapColumnIndexToView(int modelIndex) {
			return DocumentFrame.this.mapColumnIndexToView(modelIndex);
		}
	};
	
	// Table callbacks for DocumentTableManager
	private DocumentTableManager.TableCallbacks tableCallbacks = new DocumentTableManager.TableCallbacks() {
		@Override
		public void onTableModelChanged() {
			updateTotalRow();
		}
		
		@Override
		public void showAccountSelection(String query) {
			DocumentFrame.this.showAccountSelection(query);
		}
		
		@Override
		public Action getToggleDebitCreditAction() {
			return toggleDebitCreditAction;
		}
	};

	private static Logger logger = Logger.getLogger(Kirjanpito.LOGGER_NAME);
	private static final long serialVersionUID = 1L;

	public DocumentFrame(Registry registry, DocumentModel model) {
		super(Kirjanpito.APP_NAME);
		this.registry = registry;
		this.model = model;
		this.debitTotal = BigDecimal.ZERO;
		this.creditTotal = BigDecimal.ZERO;
		registry.addListener(registryListener);
	}

	/**
	 * Luo ikkunan komponentit.
	 */
	public void create() {
		// Initialize managers
		documentExporter = new DocumentExporter(this, registry, this);
		documentPrinter = new DocumentPrinter(this, registry, new DocumentPrinter.PrintCallbacks() {
			@Override
			public boolean saveDocumentIfChanged() {
				return DocumentFrame.this.saveDocumentIfChanged();
			}
			
			@Override
			public DocumentModel getDocumentModel() {
				return model;
			}
			
			@Override
			public JTable getEntryTable() {
				return entryTable;
			}
		});

		setLayout(new BorderLayout());
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			public void windowOpened(WindowEvent e) {
				if (tableManager != null) {
					tableManager.setRowHeight(getFontMetrics(entryTable.getFont()));
				}
			}

			public void windowClosing(WindowEvent e) {
				quit();
			}
		});

		List<Image> images = new ArrayList<Image>(3);
		images.add(Resources.loadAsImage("tilitin-24x24.png"));
		images.add(Resources.loadAsImage("tilitin-32x32.png"));
		images.add(Resources.loadAsImage("tilitin-48x48.png"));
		setIconImages(images);

		setAboutAction();
		createMenuBar();
		createToolBar();
		
		// Initialize formatter before UI builders
		formatter = new DecimalFormat();
		formatter.setMinimumFractionDigits(2);
		formatter.setMaximumFractionDigits(2);
		formatter.setParseBigDecimal(true);
		
		// Initialize UI builders and updaters
		initializeUIManagers();
		
		createStatusBar();

		JPanel contentPanel = new JPanel();
		contentPanel.setLayout(new BorderLayout());
		add(contentPanel, BorderLayout.CENTER);

		JPanel bottomPanel = new JPanel();
		bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.PAGE_AXIS));
		contentPanel.add(bottomPanel, BorderLayout.PAGE_END);

		createTextFieldPanel(contentPanel);
		createTable(contentPanel);
		createTotalRow(bottomPanel);
		createSearchBar(bottomPanel);
		createAttachmentsPanel(bottomPanel);

		AppSettings settings = AppSettings.getInstance();
		int width = settings.getInt("window.width", 0);
		int height = settings.getInt("window.height", 0);
		setMinimumSize(new Dimension(500, 300));

		if (width > 0 && height > 0) {
			setSize(width, height);
		}
		else {
			pack();
		}

		setLocationRelativeTo(null);
	}

	/**
	 * "Tietoja ohjelmasta" -toiminto Macilla.
	 */
	protected void setAboutAction() {
		if (System.getProperty("os.name").toLowerCase().startsWith("mac os x")) {
			Desktop.getDesktop().setAboutHandler(e -> showAboutDialog());
		}
	}

	/**
	 * Luo ikkunan valikot käyttäen DocumentMenuBuilder-luokkaa.
	 */
	protected void createMenuBar() {
		menuBuilder = new DocumentMenuBuilder();
		
		// Konfiguroi kuuntelijat
		DocumentMenuBuilder.MenuListeners listeners = new DocumentMenuBuilder.MenuListeners();
		listeners.newDatabaseListener = newDatabaseListener;
		listeners.openDatabaseListener = openDatabaseListener;
		listeners.databaseSettingsListener = databaseSettingsListener;
		listeners.backupSettingsListener = backupSettingsListener;
		listeners.restoreBackupListener = restoreBackupListener;
		listeners.quitListener = quitListener;
		listeners.copyEntriesAction = copyEntriesAction;
		listeners.pasteEntriesAction = pasteEntriesAction;
		listeners.newDocListener = newDocListener;
		listeners.deleteDocListener = deleteDocListener;
		listeners.addEntryListener = addEntryListener;
		listeners.removeEntryListener = removeEntryListener;
		listeners.editEntryTemplatesListener = editEntryTemplatesListener;
		listeners.createEntryTemplateListener = createEntryTemplateListener;
		listeners.chartOfAccountsListener = chartOfAccountsListener;
		listeners.startingBalancesListener = startingBalancesListener;
		listeners.propertiesListener = propertiesListener;
		listeners.settingsListener = settingsListener;
		listeners.appearanceListener = appearanceListener;
		listeners.prevDocListener = prevDocListener;
		listeners.nextDocListener = nextDocListener;
		listeners.firstDocListener = firstDocListener;
		listeners.lastDocListener = lastDocListener;
		listeners.findDocumentByNumberListener = findDocumentByNumberListener;
		listeners.searchListener = searchListener;
		listeners.editDocTypesListener = editDocTypesListener;
		listeners.printListener = printListener;
		listeners.editReportsListener = editReportsListener;
		listeners.vatDocumentListener = vatDocumentListener;
		listeners.setIgnoreFlagToEntryAction = setIgnoreFlagToEntryAction;
		listeners.balanceComparisonListener = balanceComparisonListener;
		listeners.numberShiftListener = numberShiftListener;
		listeners.vatChangeListener = vatChangeListener;
		listeners.exportListener = exportListener;
		listeners.csvImportListener = csvImportListener;
		listeners.helpListener = helpListener;
		listeners.debugListener = debugListener;
		listeners.aboutListener = aboutListener;
		
		// Rakenna valikkorivi
		JMenuBar menuBar = menuBuilder.build(listeners);
		
		// Hae viitteet menu-objekteihin
		entryTemplateMenu = menuBuilder.getEntryTemplateMenu();
		docTypeMenu = menuBuilder.getDocTypeMenu();
		gotoMenu = menuBuilder.getGotoMenu();
		reportsMenu = menuBuilder.getReportsMenu();
		toolsMenu = menuBuilder.getToolsMenu();
		recentMenu = menuBuilder.getRecentMenu();
		
		// Hae viitteet menu item -objekteihin
		newDatabaseMenuItem = menuBuilder.getNewDatabaseMenuItem();
		openDatabaseMenuItem = menuBuilder.getOpenDatabaseMenuItem();
		newDocMenuItem = menuBuilder.getNewDocMenuItem();
		deleteDocMenuItem = menuBuilder.getDeleteDocMenuItem();
		addEntryMenuItem = menuBuilder.getAddEntryMenuItem();
		removeEntryMenuItem = menuBuilder.getRemoveEntryMenuItem();
		pasteMenuItem = menuBuilder.getPasteMenuItem();
		coaMenuItem = menuBuilder.getCoaMenuItem();
		vatDocumentMenuItem = menuBuilder.getVatDocumentMenuItem();
		editEntryTemplatesMenuItem = menuBuilder.getEditEntryTemplatesMenuItem();
		createEntryTemplateMenuItem = menuBuilder.getCreateEntryTemplateMenuItem();
		startingBalancesMenuItem = menuBuilder.getStartingBalancesMenuItem();
		propertiesMenuItem = menuBuilder.getPropertiesMenuItem();
		settingsMenuItem = menuBuilder.getSettingsMenuItem();
		searchMenuItem = menuBuilder.getSearchMenuItem();
		editDocTypesMenuItem = menuBuilder.getEditDocTypesMenuItem();
		setIgnoreFlagMenuItem = menuBuilder.getSetIgnoreFlagMenuItem();
		
		// Päivitä viimeisimmät tietokannat -valikko
		updateRecentDatabasesMenu();
		
		setJMenuBar(menuBar);
	}

	/**
	 * Luo ikkunan työkalurivin käyttäen DocumentToolbarBuilder-luokkaa.
	 */
	protected void createToolBar() {
		toolbarBuilder = new DocumentToolbarBuilder();
		
		// Konfiguroi kuuntelijat
		DocumentToolbarBuilder.ToolbarListeners listeners = new DocumentToolbarBuilder.ToolbarListeners();
		listeners.prevDocListener = prevDocListener;
		listeners.nextDocListener = nextDocListener;
		listeners.newDocListener = newDocListener;
		listeners.addEntryListener = addEntryListener;
		listeners.removeEntryListener = removeEntryListener;
		listeners.findDocumentByNumberListener = findDocumentByNumberListener;
		listeners.searchListener = searchListener;
		
		// Rakenna työkalurivi ja lisää ikkunaan
		JToolBar toolBar = toolbarBuilder.build(listeners);
		add(toolBar, BorderLayout.PAGE_START);
		
		// Hae viitteet painike-objekteihin
		prevButton = toolbarBuilder.getPrevButton();
		nextButton = toolbarBuilder.getNextButton();
		newDocButton = toolbarBuilder.getNewDocButton();
		addEntryButton = toolbarBuilder.getAddEntryButton();
		removeEntryButton = toolbarBuilder.getRemoveEntryButton();
		findByNumberButton = toolbarBuilder.getFindByNumberButton();
		searchButton = toolbarBuilder.getSearchButton();
	}

	/**
	 * Alustaa UI-managerit Phase 7 -refaktoroinnin osana.
	 */
	private void initializeUIManagers() {
		// Initialize UI builder
		uiBuilder = new DocumentUIBuilder(new DocumentUIBuilder.UICallbacks() {
			@Override
			public void onNumberFieldChanged() {
				model.setDocumentChanged();
			}
			
			@Override
			public void onDateFieldChanged() {
				model.setDocumentChanged();
			}
			
			@Override
			public void onSearchButtonClicked() {
				searchDocuments();
			}
			
			@Override
			public void onSearchPanelClosed() {
				// Handled by searchListener
			}
			
			@Override
			public Action getAddEntryAction() {
				return addEntryListener;
			}
			
			@Override
			public ActionListener getSearchListener() {
				return searchListener;
			}
		});
		
		// Get UI components from builder
		uiComponents = uiBuilder.getComponents();
		
		// Initialize UI updater components wrapper
		uiUpdaterComponents = new DocumentUIUpdater.UIComponents();
		
		// Initialize UI updater
		uiUpdater = new DocumentUIUpdater(model, registry, uiUpdaterComponents, 
			new DocumentUIUpdater.UICallbacks() {
				@Override
				public JTable getEntryTable() {
					return entryTable;
				}
				
				@Override
				public EntryTableModel getTableModel() {
					return tableModel;
				}
				
				@Override
				public Object getAttachmentsPanel() {
					return attachmentsPanel;
				}
				
				@Override
				public ActionListener getEntryTemplateListener() {
					return entryTemplateListener;
				}
				
				@Override
				public ActionListener getDocTypeListener() {
					return docTypeListener;
				}
				
				@Override
				public JMenuItem getEditDocTypesMenuItem() {
					return editDocTypesMenuItem;
				}
				
				@Override
				public JMenuItem getCreateEntryTemplateMenuItem() {
					return createEntryTemplateMenuItem;
				}
				
				@Override
				public JMenuItem getEditEntryTemplatesMenuItem() {
					return editEntryTemplatesMenuItem;
				}
				
				@Override
				public int findDocumentTypeByNumber(int number) {
					return DocumentFrame.this.findDocumentTypeByNumber(number);
				}
				
				@Override
				public void setComponentsEnabled(boolean read, boolean create, boolean edit) {
					DocumentFrame.this.setComponentsEnabled(read, create, edit);
				}
				
				@Override
				public void setTitle(String title) {
					DocumentFrame.this.setTitle(title);
				}
				
				@Override
				public void updateBackupStatusLabel() {
					DocumentFrame.this.updateBackupStatusLabel();
				}
				
				@Override
				public void updateTableSettings() {
					DocumentFrame.this.updateTableSettings();
				}
			}, formatter);
	}
	
	/**
	 * Lisää <code>container</code>-paneeliin tositenumerokentän,
	 * päivämääräkentän ja tagikentän.
	 *
	 * @param container paneeli, johon komponentit lisätään
	 */
	protected void createTextFieldPanel(JPanel container) {
		// Use UI builder for Phase 7 refactoring
		uiBuilder.createTextFieldPanel(container);

		// Get component references
		numberTextField = uiComponents.numberTextField;
		dateTextField = uiComponents.dateTextField;

		// Update updater components
		uiUpdaterComponents.numberTextField = numberTextField;
		uiUpdaterComponents.dateTextField = dateTextField;

		// Configure firstEntry action to select first row in table
		// Note: entryTable may not be initialized yet, so we'll set this up after table creation
		// For now, the action is already configured in DocumentUIBuilder
	}

	/**
	 * Luo taulukon, joka näyttää tositteen viennit.
	 *
	 * @param container paneeli, johon taulukko lisätään
	 */
	protected void createTable(JPanel container) {
		// Create table manager
		tableManager = new DocumentTableManager(registry, model, container, 
				tableCallbacks, columnMapper);
		
		// Get references to table components
		entryTable = tableManager.getTable();
		tableModel = tableManager.getTableModel();
		accountCellRenderer = tableManager.getAccountCellRenderer();
		accountCellEditor = tableManager.getAccountCellEditor();
		descriptionCellEditor = tableManager.getDescriptionCellEditor();
		vatColumn = tableManager.getVatColumn();
		
		// Set table actions (this will configure keyboard shortcuts)
		tableManager.setTableActions(new DocumentTableManager.TableActions() {
			@Override
			public Action getPrevCellAction() { return prevCellAction; }
			
			@Override
			public Action getNextCellAction() { return nextCellAction; }
			
			@Override
			public Action getAddEntryAction() { return addEntryListener; }
			
			@Override
			public Action getRemoveEntryAction() { return removeEntryListener; }
			
			@Override
			public Action getCopyEntriesAction() { return copyEntriesAction; }
			
			@Override
			public Action getPasteEntriesAction() { return pasteEntriesAction; }
			
			@Override
			public Action getPrevDocumentAction() { 
				return new AbstractAction() {
					private static final long serialVersionUID = 1L;
					@Override
					public void actionPerformed(ActionEvent e) {
						goToDocument(DocumentModel.FETCH_PREVIOUS);
					}
				};
			}
			
			@Override
			public Action getNextDocumentAction() { 
				return new AbstractAction() {
					private static final long serialVersionUID = 1L;
					@Override
					public void actionPerformed(ActionEvent e) {
						goToDocument(DocumentModel.FETCH_NEXT);
					}
				};
			}
			
			@Override
			public Action getSetIgnoreFlagAction() { return setIgnoreFlagToEntryAction; }
		});
	}

	/**
	 * Luo summarivin.
	 *
	 * @param container paneeli, johon rivi lisätään
	 */
	protected void createTotalRow(JPanel container) {
		// Use UI builder for Phase 7 refactoring
		uiBuilder.createTotalRow(container);
		
		// Get component references
		debitTotalLabel = uiComponents.debitTotalLabel;
		creditTotalLabel = uiComponents.creditTotalLabel;
		differenceLabel = uiComponents.differenceLabel;
		
		// Update updater components
		uiUpdaterComponents.debitTotalLabel = debitTotalLabel;
		uiUpdaterComponents.creditTotalLabel = creditTotalLabel;
		uiUpdaterComponents.differenceLabel = differenceLabel;
	}

	/**
	 * Luo hakupalkin.
	 *
	 * @param container paneeli, johon hakupalkki lisätään
	 */
	protected void createSearchBar(JPanel container) {
		// Use UI builder for Phase 7 refactoring
		uiBuilder.createSearchBar(container);

		// Get component references
		searchPanel = uiComponents.searchPanel;
		searchPhraseTextField = uiComponents.searchPhraseTextField;

		// Update updater components
		uiUpdaterComponents.searchPanel = searchPanel;
		uiUpdaterComponents.searchPhraseTextField = searchPhraseTextField;

		// Initialize document navigator (Phase 8)
		documentNavigator = new DocumentNavigator(registry, searchPanel,
				searchPhraseTextField, this);
	}

	/**
	 * Creates the attachments panel for displaying PDF attachments.
	 * 
	 * @param container parent container
	 */
	protected void createAttachmentsPanel(JPanel container) {
		attachmentsPanel = new AttachmentsPanel(null, 0);
		attachmentsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("PDF-liitteet"));
		container.add(attachmentsPanel);
	}

	/**
	 * Luo tilarivin, jossa näytetään valitun tositteen järjestysnumero
	 * ja tilikausi.
	 */
	protected void createStatusBar() {
		// Use UI builder for Phase 7 refactoring
		uiBuilder.createStatusBar(this);

		// Get component references
		documentLabel = uiComponents.documentLabel;
		periodLabel = uiComponents.periodLabel;
		documentTypeLabel = uiComponents.documentTypeLabel;
		backupStatusLabel = uiComponents.backupStatusLabel;

		// Update updater components FIRST before calling any update methods
		uiUpdaterComponents.documentLabel = documentLabel;
		uiUpdaterComponents.periodLabel = periodLabel;
		uiUpdaterComponents.documentTypeLabel = documentTypeLabel;
		uiUpdaterComponents.backupStatusLabel = backupStatusLabel;
		uiUpdaterComponents.entryTemplateMenu = entryTemplateMenu;
		uiUpdaterComponents.docTypeMenu = docTypeMenu;
		uiUpdaterComponents.searchMenuItem = searchMenuItem;

		// Now safe to update backup status label
		updateBackupStatusLabel();

		// Aseta listener backup-statuksen päivitykseen
		BackupService.getInstance().setStatusListener(status -> updateBackupStatusLabel());

		// Backup-indikaattoria klikkaamalla avautuu asetukset
		backupStatusLabel.addMouseListener(new java.awt.event.MouseAdapter() {
			@Override
			public void mouseClicked(java.awt.event.MouseEvent e) {
				BackupSettingsDialog.show(DocumentFrame.this);
				updateBackupStatusLabel();
			}
		});
	}
	
	/**
	 * Päivittää backup-indikaattorin tilariville.
	 */
	private void updateBackupStatusLabel() {
		// Use UI updater for Phase 7 refactoring
		uiUpdater.updateBackupStatusLabel();
	}

	/**
	 * Lopettaa ohjelman suorituksen.
	 */
	public void quit() {
		if (registry.getDataSource() != null) {
			if (!saveDocumentIfChanged()) {
				return;
			}

			saveDocumentTypeIfChanged();
			
			// Suorita varmuuskopiointi ennen sulkemista
			performBackupOnClose();
			
			// Pysäytä AutoBackup
			BackupService.getInstance().stopAutoBackup();
			
			model.closeDataSource();
		}

		if (documentPrinter != null) {
			documentPrinter.disposePrintPreview();
		}

		AppSettings settings = AppSettings.getInstance();

		/* Tallennetaan ikkunan koko. */
		settings.set("window.width", getWidth());
		settings.set("window.height", getHeight());

		/* Tallennetaan taulukon sarakkeiden leveydet. */
		if (tableManager != null) {
			tableManager.saveColumnWidths();
		}

		settings.save();
		System.exit(0);
	}

	/**
	 * Luo uuden tositteen. Ennen tositteen luontia
	 * käyttäjän tekemät muutokset tallennetaan.
	 */
	public void createDocument() {
		documentNavigator.createDocument();
	}

	/**
	 * Poistaa valitun tositteen.
	 */
	public void deleteDocument() {
		documentNavigator.deleteDocument();
	}

	/**
	 * Siirtyy toiseen tositteeseen. Ennen tietojen
	 * hakemista käyttäjän tekemät muutokset tallennetaan.
	 *
	 * @param index tositteen järjestysnumero
	 */
	public void goToDocument(int index) {
		documentNavigator.goToDocument(index);
	}

	/**
	 * Kysyy käyttäjältä tositenumeroa, ja siirtyy tähän tositteeseen.
	 */
	public void findDocumentByNumber() {
		documentNavigator.findDocumentByNumber();
	}

	/**
	 * Kytkee tositteiden haun päälle tai pois päältä.
	 */
	public void toggleSearchPanel() {
		documentNavigator.toggleSearchPanel();
	}

	/**
	 * Etsii tositteita käyttäjän antamalla hakusanalla.
	 */
	public void searchDocuments() {
		documentNavigator.searchDocuments();
	}

	/**
	 * Avaa tilikarttaikkunan.
	 */
	public void showChartOfAccounts() {
		if (!saveDocumentIfChanged()) {
			return;
		}

		if (documentPrinter != null) {
			documentPrinter.closePrintPreview();
		}
		COAModel coaModel = new COAModel(registry);
		COADialog dialog = new COADialog(this, registry, coaModel);
		dialog.create();
		dialog.setVisible(true);
	}

	/**
	 * Tallentaa viennit CSV-tiedostoon.
	 *
	 * @deprecated Käytä documentExporter.exportToCSV() suoraan
	 */
	@Deprecated
	public void export() {
		documentExporter.exportToCSV();
	}

	/**
	 * Näyttää CSV-tuontidialogin.
	 */
	public void showCsvImportDialog() {
		if (!model.isDocumentEditable()) {
			SwingUtils.showErrorMessage(this, "Tilikausi on lukittu. CSV-tuonti ei ole mahdollista.");
			return;
		}
		
		// Tallenna nykyinen tosite ennen tuontia
		if (!saveDocumentIfChanged()) {
			return;
		}
		
		kirjanpito.ui.dialogs.CsvImportDialog dialog = 
			new kirjanpito.ui.dialogs.CsvImportDialog(this, registry);
		dialog.setVisible(true);
		
		// Päivitä näkymä tuonnin jälkeen
		if (dialog.getImportResult() != null && dialog.getImportResult().getSuccess()) {
			try {
				// Siirry viimeiseen tositteeseen (äskön tuotu)
				model.goToDocument(model.getDocumentCount() - 1);
				updatePosition();
				updateDocument();
				updateTotalRow();
			} catch (Exception e) {
				// Ignore, just update current view
				updatePosition();
				updateDocument();
			}
		}
	}

	/**
	 * Päättää ALV-tilit.
	 */
	public void createVATDocument() {
		if (!saveDocumentIfChanged()) {
			return;
		}

		boolean result;

		try {
			result = model.createVATDocument();
		}
		catch (DataAccessException e) {
			String message = "Uuden tositteen luominen epäonnistui";
			logger.log(Level.SEVERE, message, e);
			SwingUtils.showDataAccessErrorMessage(this, e, message);
			return;
		}

		updatePosition();
		updateDocument();
		updateTotalRow();

		if (!result) {
			SwingUtils.showErrorMessage(this, "Arvonlisäverovelkatiliä ei ole määritetty.");
		}
	}

	/**
	 * Näyttää vientimallien muokkausikkunan.
	 */
	public void editEntryTemplates() {
		EntryTemplateModel templateModel = new EntryTemplateModel(registry);

		EntryTemplateDialog dialog = new EntryTemplateDialog(
				this, registry, templateModel);

		dialog.create();
		dialog.setVisible(true);
	}

	/**
	 * Luo vientimallin valitusta tositteesta.
	 */
	public void createEntryTemplateFromDocument() {
		int number;

		try {
			number = model.createEntryTemplateFromDocument();
		}
		catch (DataAccessException e) {
			String message = "Vientimallin luominen epäonnistui";
			logger.log(Level.SEVERE, message, e);
			SwingUtils.showDataAccessErrorMessage(this, e, message);
			return;
		}

		if (number < 0) {
			return;
		}

		String message = String.format("Vientimalli on luotu numerolle %d", number);

		if (number >= 1 && number < 10) {
			message += String.format(" (Alt+%s)", number % 10);
		}

		updateEntryTemplates();
		SwingUtils.showInformationMessage(this, message);
	}

	/**
	 * Lisää viennit mallin perusteella.
	 *
	 * @param number vientimallin numero
	 */
	public void addEntriesFromTemplate(int number) {
		int result = updateModel();

		if (result < 0) {
			return;
		}

		model.addEntriesFromTemplate(number);
		tableModel.fireTableDataChanged();
	}

	/**
	 * Näyttää alkusaldojen muokkausikkunan.
	 */
	public void showStartingBalances() {
		StartingBalanceModel balanceModel = new StartingBalanceModel(registry);

		try {
			balanceModel.initialize();
		}
		catch (DataAccessException e) {
			String message = "Alkusaldojen hakeminen epäonnistui";
			logger.log(Level.SEVERE, message, e);
			SwingUtils.showDataAccessErrorMessage(this, e, message);
			return;
		}

		StartingBalanceDialog dialog = new StartingBalanceDialog(
				this, balanceModel);

		dialog.create();
		dialog.setVisible(true);
	}

	/**
	 * Näyttää perustiedot.
	 */
	public void showProperties() {
		if (!saveDocumentIfChanged()) {
			return;
		}

		if (documentPrinter != null) {
			documentPrinter.closePrintPreview();
		}
		final PropertiesModel settingsModel = new PropertiesModel(registry);

		try {
			settingsModel.initialize();
		}
		catch (DataAccessException e) {
			String message = "Asetuksien hakeminen epäonnistui";
			logger.log(Level.SEVERE, message, e);
			SwingUtils.showDataAccessErrorMessage(this, e, message);
			return;
		}

		PropertiesDialog dialog = new PropertiesDialog(
				this, settingsModel);

		dialog.create();
		dialog.setVisible(true);
	}

	/**
	 * Näyttää kirjausasetukset.
	 */
	public void showSettings() {
		if (!saveDocumentIfChanged()) {
			return;
		}

		SettingsDialog dialog = new SettingsDialog(this, registry);
		dialog.create();
		dialog.setVisible(true);
	}

	/**
	 * Näyttää ulkoasun asetukset.
	 */
	public void showAppearanceDialog() {
		AppearanceDialog dialog = new AppearanceDialog(this);
		dialog.create();
		dialog.setVisible(true);
	}

	/**
	 * Näyttää tietokanta-asetukset.
	 */
	public void showDatabaseSettings() {
		DatabaseSettingsDialog dialog = new DatabaseSettingsDialog(
				this);

		AppSettings settings = AppSettings.getInstance();
		String url = settings.getString("database.url", null);
		String defaultUrl = model.buildDefaultJDBCURL();

		if (url == null)
			url = defaultUrl;

		dialog.create();
		dialog.setURL(url);
		dialog.setUsername(settings.getString("database.username", ""));
		dialog.setPassword(settings.getString("database.password", ""));
		dialog.setDefaultUrl(defaultUrl);
		dialog.setVisible(true);

		if (dialog.getResult() == JOptionPane.OK_OPTION) {
			settings.set("database.url", dialog.getURL());
			settings.set("database.username", dialog.getUsername());
			settings.set("database.password", dialog.getPassword());

			if (registry.getDataSource() != null) {
				model.closeDataSource();
				updatePeriod();
				updatePosition();
				updateDocument();
				updateTotalRow();
				updateEntryTemplates();
				updateDocumentTypes();
			}

			openDataSource();
		}

		dialog.dispose();
	}

	/**
	 * Lisää viennin tositteeseen.
	 */
	public void addEntry() {
		if (!model.isDocumentEditable()) {
			return;
		}

		stopEditing();
		int index = model.addEntry();
		tableModel.fireTableRowsInserted(index, index);
		updateTotalRow();
		entryTable.changeSelection(index, 0, false, false);
		entryTable.requestFocusInWindow();
	}

	/**
	 * Poistaa käyttäjän valitseman viennin.
	 */
	public void removeEntry() {
		if (!model.isDocumentEditable()) {
			return;
		}

		int[] rows = entryTable.getSelectedRows();

		if (rows.length == 0) {
			return;
		}

		stopEditing();
		Arrays.sort(rows);
		int index = -1;

		for (int i = rows.length - 1; i >= 0; i--) {
			index = rows[i];
			model.removeEntry(index);
			tableModel.fireTableRowsDeleted(index, index);
		}

		updateTotalRow();
		index = Math.min(index, tableModel.getRowCount() - 1);

		if (tableModel.getRowCount() > 0) {
			entryTable.setRowSelectionInterval(index, index);
			entryTable.requestFocusInWindow();
		}
		else if (entryTable.isFocusOwner()) {
			dateTextField.requestFocusInWindow();
		}
	}

	/**
	 * Kopioi valitut viennit leikepöydälle.
	 */
	public void copyEntries() {
		stopEditing();
		StringBuilder sb = new StringBuilder();
		int[] rows = entryTable.getSelectedRows();

		for (int i = 0; i < rows.length; i++) {
			Entry entry = model.getEntry(rows[i]);
			Account account = registry.getAccountById(entry.getAccountId());

			if (account == null) {
				sb.append('\t');
			}
			else {
				sb.append(account.getNumber());
				sb.append('\t');
				sb.append(account.getName());
			}

			sb.append('\t');

			if (entry.isDebit()) {
				sb.append(formatter.format(model.getVatIncludedAmount(rows[i])));
				sb.append('\t');
			}
			else {
				sb.append('\t');
				sb.append(formatter.format(model.getVatIncludedAmount(rows[i])));
			}

			sb.append('\t');
			sb.append(formatter.format(model.getVatAmount(i)));
			sb.append('\t');
			sb.append(entry.getDescription());
			sb.append(System.getProperty("line.separator"));
		}

		Toolkit.getDefaultToolkit().getSystemClipboard(
				).setContents(new StringSelection(sb.toString()), null);
	}

	/**
	 * Liittää leikepöydällä olevat viennit.
	 */
	public void pasteEntries() {
		Transferable t = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
		String text = null;

		try {
			if (t != null && t.isDataFlavorSupported(DataFlavor.stringFlavor)) {
				text = t.getTransferData(DataFlavor.stringFlavor).toString();
			}
		}
		catch (UnsupportedFlavorException e) {
		}
		catch (IOException e) {
		}

		if (text == null) {
			return;
		}

		String[] lines = text.split("\n");
		stopEditing();

		for (String line : lines) {
			String[] cols = line.split("\t", 6);

			if (cols.length != 6) {
				continue;
			}

			int index = model.addEntry();
			Entry entry = model.getEntry(index);
			Account account = registry.getAccountByNumber(cols[0]);
			boolean vatEntries = true;

			if (account != null) {
				model.updateAccountId(index, account.getId());
			}

			if (!cols[4].isEmpty()) {
				/* Lasketaan ALV, jos ALV-sarakkeen rahamäärä on erisuuri kuin 0,00. */
				try {
					vatEntries = ((BigDecimal)formatter.parse(cols[4])).compareTo(BigDecimal.ZERO) != 0;
				}
				catch (ParseException e) {
				}
			}

			if (!cols[2].isEmpty()) {
				entry.setDebit(true);

				try {
					model.updateAmount(index, (BigDecimal)formatter.parse(cols[2]), vatEntries);
				}
				catch (ParseException e) {
				}
			}

			if (!cols[3].isEmpty()) {
				entry.setDebit(false);

				try {
					model.updateAmount(index, (BigDecimal)formatter.parse(cols[3]), vatEntries);
				}
				catch (ParseException e) {
				}
			}

			entry.setDescription(cols[5]);
			tableModel.fireTableRowsInserted(index, index);
			entryTable.changeSelection(index, 0, false, false);
		}

		updateTotalRow();
	}

	/**
	 * Valitsee tositelajin.
	 *
	 * @param index tositelajin järjestysnumero
	 */
	public void setDocumentType(int index) {
		selectDocumentTypeMenuItem(index);
		model.setDocumentTypeIndex(index);

		try {
			model.fetchDocuments(-1);
		}
		catch (DataAccessException e) {
			String message = "Tositetietojen hakeminen epäonnistui";
			logger.log(Level.SEVERE, message, e);
			SwingUtils.showDataAccessErrorMessage(this, e, message);
		}

		updatePosition();
		updateDocument();
		updateTotalRow();
	}

	/**
	 * Näyttää tositelajien muokkausikkunan.
	 */
	public void editDocumentTypes() {
		if (!saveDocumentIfChanged()) {
			return;
		}

		DocumentTypeModel documentTypeModel = new DocumentTypeModel(registry);

		DocumentTypeDialog dialog = new DocumentTypeDialog(
				this, documentTypeModel);

		dialog.create();
		dialog.setVisible(true);
	}

	/**
	 * Näyttää tilien saldot esikatseluikkunassa.
	 */
	public void showAccountSummary() {
		documentPrinter.showAccountSummary();
	}

	/**
	 * Näyttää tositteen esikatseluikkunassa.
	 */
	public void showDocumentPrint() {
		documentPrinter.showDocumentPrint();
	}

	/**
	 * Näyttää tiliotteen esikatseluikkunassa.
	 */
	public void showAccountStatement() {
		documentPrinter.showAccountStatement();
	}

	/**
	 * Näyttää tuloslaskelman esikatseluikkunassa.
	 */
	public void showIncomeStatement(boolean detailed) {
		documentPrinter.showIncomeStatement(detailed);
	}

	/**
	 * Näyttää taseen esikatseluikkunassa.
	 */
	public void showBalanceSheet(boolean detailed) {
		documentPrinter.showBalanceSheet(detailed);
	}

	/**
	 * Näyttää päiväkirjan esikatseluikkunassa.
	 */
	public void showGeneralJournal() {
		documentPrinter.showGeneralJournal();
	}

	/**
	 * Näyttää pääkirjan esikatseluikkunassa.
	 */
	public void showGeneralLedger() {
		documentPrinter.showGeneralLedger();
	}

	/**
	 * Näyttää ALV-laskelman esikatseluikkunassa.
	 */
	public void showVATReport() {
		documentPrinter.showVATReport();
	}

	public void showChartOfAccountsPrint(int mode) {
		documentPrinter.showChartOfAccountsPrint(mode);
	}

	/**
	 * Näyttää tulosteiden muokkausikkunan.
	 */
	public void editReports() {
		ReportEditorModel editorModel = new ReportEditorModel(registry);
		ReportEditorDialog dialog = new ReportEditorDialog(
				this, editorModel);

		try {
			editorModel.load();
		}
		catch (DataAccessException e) {
			String message = "Tulostetietojen hakeminen epäonnistui";
			logger.log(Level.SEVERE, message, e);
			SwingUtils.showDataAccessErrorMessage(this, e, message);
		}

		dialog.create();
		dialog.setVisible(true);
	}

	/**
	 * Näyttää tilinvalintaikkunan ja hakee tilikartasta tilin
	 * hakusanalla <code>q</code>.
	 *
	 * @param q hakusana
	 */
	public void showAccountSelection(String q) {
		if (accountSelectionDialog == null) {
			accountSelectionDialog = new AccountSelectionDialog(
					this, registry);

			accountSelectionDialog.setListener(this);
			accountSelectionDialog.create();
		}

		if (entryTable.isEditing())
			entryTable.getCellEditor().cancelCellEditing();

		accountSelectionDialog.setSearchPhrase(q);
		accountSelectionDialog.setVisible(true);
	}

	/**
	 * Päivittää valittuun vientiin käyttäjän valitseman tilin.
	 */
	public void accountSelected() {
		Account account = accountSelectionDialog.getSelectedAccount();
		int index = entryTable.getSelectedRow();
		model.updateAccountId(index, account.getId());
		moveToNextCell();
		accountSelectionDialog.setVisible(false);
	}

	/**
	 * Avaa tilien saldovertailuikkunan.
	 */
	public void showBalanceComparison() {
		StatisticsModel statsModel = new StatisticsModel(registry);

		BalanceComparisonDialog dialog = new BalanceComparisonDialog(this,
				registry, statsModel);

		dialog.create();
		dialog.setVisible(true);
	}

	public void showDocumentNumberShiftDialog() {
		DocumentType documentType = model.getDocumentType();
		int endNumber = Integer.MAX_VALUE;

		if (documentType != null) {
			endNumber = documentType.getNumberEnd();
		}

		DocumentNumberShiftDialog dialog = new DocumentNumberShiftDialog(this, registry);
		dialog.create();

		try {
			dialog.fetchDocuments(model.getDocument().getNumber(), endNumber);
		}
		catch (DataAccessException e) {
			String message = "Tositetietojen hakeminen epäonnistui";
			logger.log(Level.SEVERE, message, e);
			SwingUtils.showDataAccessErrorMessage(this, e, message);
			return;
		}

		dialog.setVisible(true);

		if (dialog.getResult() == JOptionPane.OK_OPTION) {
			refreshModel(false);
		}
	}

	/**
	 * Avaa ALV-kantojen muutosikkunan.
	 */
	public void showVATChangeDialog() {
		VATChangeDialog dialog = new VATChangeDialog(this, registry);
		dialog.create();
		dialog.setVisible(true);
	}

	/**
	 * Avaa ohjeet Web-selaimessa.
	 */
	public void showHelp() {
		try {
			Desktop.getDesktop().browse(
				new URI("http://helineva.net/tilitin/ohjeet/?v=" + Kirjanpito.APP_VERSION));
		}
		catch (Exception e) {
			SwingUtils.showErrorMessage(this, "Web-selaimen avaaminen epäonnistui. " +
					"Ohjeet löytyvät osoitteesta\n" +
					"http://helineva.net/tilitin/ohjeet/");
		}
	}

	/**
	 * Avaa lokitiedoston tekstieditorissa.
	 */
	public void showLogMessages() {
		File file = Kirjanpito.logFile;

		if (file.exists()) {
			try {
				Desktop.getDesktop().browse(new URI("file://" + file.getAbsolutePath().replace('\\', '/')));
			}
			catch (Exception e) {
				SwingUtils.showErrorMessage(this, String.format(
						"Lokitiedoston %s avaaminen epäonnistui.", file.getAbsolutePath()));
			}
		}
		else {
			SwingUtils.showInformationMessage(this, "Virheenjäljitystietoja ei löytynyt.");
		}
	}

	/**
	 * Näyttää tietoja ohjelmasta.
	 */
	public void showAboutDialog() {
		AboutDialog dialog = new AboutDialog(this);
		dialog.create();
		dialog.setVisible(true);
	}

	/**
	 * Avaa tietokantayhteyden ja hakee tarvittavat tiedot tietokannasta.
	 */
	public void openDataSource() {
		try {
			model.openDataSource();
			boolean initialized = model.initialize();

			/* Lisätään tietokantaan perustiedot, jos niitä ei vielä ole. */
			if (!initialized) {
				initializeDataSource();
			}
		}
		catch (DataAccessException e) {
			String message = "Tietokantayhteyden avaaminen epäonnistui";
			logger.log(Level.SEVERE, message, e);
			setComponentsEnabled(false, false, false);
			SwingUtils.showDataAccessErrorMessage(this, e, message);
		}

		updateTitle();
		updatePeriod();
		updatePosition();
		updateDocument();
		updateTotalRow();
		updateEntryTemplates();
		updateDocumentTypes();
		updateTableSettings();
		
		// Update attachments panel with data source
		if (attachmentsPanel != null) {
			attachmentsPanel.setDataSource(registry.getDataSource());
		}
		
		// Tallenna viimeisimpien tietokantojen listaan
		String dbUrl = AppSettings.getInstance().getString("database.url", "");
		if (!dbUrl.isEmpty()) {
			RecentDatabases.getInstance().addDatabase(dbUrl);
			updateRecentDatabasesMenu();
			
			// Käynnistä AutoBackup jos käytössä
			BackupService.getInstance().setCurrentDatabase(dbUrl);
			updateBackupStatusLabel();
		}
	}

	public void openSqliteDataSource(File file) {
		AppSettings settings = AppSettings.getInstance();
		settings.set("database.url", String.format("jdbc:sqlite:%s",
				file.getAbsolutePath().replace(File.pathSeparatorChar, '/')));
		settings.set("database.username", "");
		settings.set("database.password", "");
		openDataSource();
	}
	
	/**
	 * Päivittää viimeisimpien tietokantojen valikon.
	 */
	protected void updateRecentDatabasesMenu() {
		if (recentMenu == null) return;
		
		recentMenu.removeAll();
		RecentDatabases recent = RecentDatabases.getInstance();
		List<String> databases = recent.getRecentDatabases();
		
		if (databases.isEmpty()) {
			JMenuItem emptyItem = new JMenuItem("(ei viimeisimpiä)");
			emptyItem.setEnabled(false);
			recentMenu.add(emptyItem);
		} else {
			int index = 1;
			for (final String dbUrl : databases) {
				String displayName = RecentDatabases.getDisplayName(dbUrl);
				JMenuItem item = new JMenuItem(index + ". " + displayName);
				if (index <= 9) {
					item.setMnemonic(Character.forDigit(index, 10));
				}
				item.addActionListener(e -> openRecentDatabase(dbUrl));
				recentMenu.add(item);
				index++;
			}
			
			recentMenu.addSeparator();
			JMenuItem clearItem = new JMenuItem("Tyhjennä lista");
			clearItem.addActionListener(e -> {
				RecentDatabases.getInstance().clearAll();
				updateRecentDatabasesMenu();
			});
			recentMenu.add(clearItem);
		}
	}
	
	/**
	 * Avaa viimeisimmän tietokannan.
	 */
	private void openRecentDatabase(String dbUrl) {
		AppSettings settings = AppSettings.getInstance();
		settings.set("database.url", dbUrl);
		
		// Jos SQLite, tyhjennä käyttäjätunnus/salasana
		if (dbUrl.startsWith("jdbc:sqlite:")) {
			settings.set("database.username", "");
			settings.set("database.password", "");
		}
		
		openDataSource();
	}

	@Override
	public void startCSVExport(CSVExportWorker worker) {
		TaskProgressDialog dialog = new TaskProgressDialog(
				this, "CSV-tiedostoon vienti", worker);
		dialog.create();
		dialog.setVisible(true);
		worker.execute();
	}

	protected void refreshModel(boolean positionChanged) {
		try {
			model.fetchDocuments(positionChanged ? -1 : model.getDocumentPosition());
		}
		catch (DataAccessException e) {
			String message = "Tositetietojen hakeminen epäonnistui";
			logger.log(Level.SEVERE, message, e);
			SwingUtils.showDataAccessErrorMessage(this, e, message);
		}

		updatePeriod();
		updatePosition();
		updateDocument();
		updateTotalRow();

		if (searchEnabled) {
			searchEnabled = false;
			updateSearchPanel();
		}
	}

	/**
	 * Päivittää ikkunan otsikkorivin.
	 */
	protected void updateTitle() {
		// Use UI updater for Phase 7 refactoring
		uiUpdater.updateTitle();
	}

	/**
	 * Päivittää tilikauden tiedot tilariville.
	 */
	protected void updatePeriod() {
		// Use UI updater for Phase 7 refactoring
		uiUpdater.updatePeriod();
	}

	/**
	 * Päivittää tositteen järjestysnumeron, tositteiden
	 * lukumäärän ja tositelajin tilariville.
	 */
	public void updatePosition() {
		// Use UI updater for Phase 7 refactoring
		uiUpdater.updatePosition();
		
		// Handle searchEnabled case
		if (searchEnabled) {
			DocumentType type;
			int index = findDocumentTypeByNumber(model.getDocument().getNumber());
			type = (index < 0) ? null : registry.getDocumentTypes().get(index);
			
			if (type == null) {
				documentTypeLabel.setText("");
			}
			else {
				documentTypeLabel.setText(type.getName());
			}
		}
	}

	/**
	 * Päivittää tositteen tiedot.
	 */
	public void updateDocument() {
		// Use UI updater for Phase 7 refactoring
		uiUpdater.updateDocument();
		
		// Update dateTextField focus and selection for new documents
		Document document = model.getDocument();
		if (document != null && document.getId() <= 0) {
			dateTextField.select(0, dateTextField.getText().length());
			dateTextField.requestFocus();
		}
	}

	/**
	 * Päivittää summarivin tiedot.
	 */
	public void updateTotalRow() {
		debitTotal = BigDecimal.ZERO;
		creditTotal = BigDecimal.ZERO;
		int count = model.getEntryCount();
		Entry entry;

		for (int i = 0; i < count; i++) {
			entry = model.getEntry(i);

			if (entry.isDebit()) {
				debitTotal = debitTotal.add(model.getVatIncludedAmount(i));
			}
			else {
				creditTotal = creditTotal.add(model.getVatIncludedAmount(i));
			}
		}

		// Use UI updater for Phase 7 refactoring
		uiUpdater.updateTotalRow(debitTotal, creditTotal);
	}

	/**
	 * Päivittää vientimallivalikon.
	 */
	protected void updateEntryTemplates() {
		// Update updater components
		uiUpdaterComponents.entryTemplateMenu = entryTemplateMenu;
		
		// Use UI updater for Phase 7 refactoring
		uiUpdater.updateEntryTemplates();
	}

	/**
	 * Päivittää tositelajivalikon.
	 */
	protected void updateDocumentTypes() {
		// Update updater components
		uiUpdaterComponents.docTypeMenu = docTypeMenu;
		
		// Use UI updater for Phase 7 refactoring
		uiUpdater.updateDocumentTypes();
		
		// Get docTypeMenuItems reference back
		docTypeMenuItems = uiUpdaterComponents.docTypeMenuItems;
	}

	/**
	 * Päivittää tositelajivalinnan.
	 *
	 * @param index uuden tositelajin järjestysnumero
	 */
	public void selectDocumentTypeMenuItem(int index) {
		int oldIndex = model.getDocumentTypeIndex();

		if (!saveDocumentIfChanged()) {
			docTypeMenuItems[index].setSelected(index == oldIndex);
			return;
		}

		if (oldIndex >= 0)
			docTypeMenuItems[oldIndex].setSelected(false);

		docTypeMenuItems[index].setSelected(true);
	}

	/**
	 * Näyttää tai piilottaa hakupaneelin.
	 */
	public void updateSearchPanel() {
		// Use UI updater for Phase 7 refactoring
		uiUpdater.updateSearchPanel(searchEnabled);
	}

	/**
	 * Päivittää vientitaulukon asetukset.
	 */
	protected void updateTableSettings() {
		Settings settings = registry.getSettings();
		if (tableManager != null) {
			tableManager.updateTableSettings(settings);
			vatColumn = tableManager.getVatColumn();
		}
	}

	protected int mapColumnIndexToView(int col) {
		if (tableManager != null) {
			return tableManager.mapColumnIndexToView(col);
		}
		// Fallback if tableManager not initialized yet
		return col;
	}

	protected int mapColumnIndexToModel(int col) {
		if (tableManager != null) {
			return tableManager.mapColumnIndexToModel(col);
		}
		// Fallback if tableManager not initialized yet
		return col;
	}

	protected void initializeDataSource() {
		setComponentsEnabled(false, false, false);

		DataSourceInitializationModel initModel =
			new DataSourceInitializationModel();

		DataSourceInitializationDialog dialog =
			new DataSourceInitializationDialog(this,
					registry, initModel);

		dialog.create();
		dialog.setVisible(true);

		DataSourceInitializationWorker worker = dialog.getWorker();

		if (worker == null) {
			model.closeDataSource();
		}
		else {
			worker.addPropertyChangeListener(
					new InitializationWorkerListener(this, worker));
		}
	}

	protected void setComponentsEnabled(boolean read, boolean create, boolean edit) {
		coaMenuItem.setEnabled(read);
		startingBalancesMenuItem.setEnabled(read);
		propertiesMenuItem.setEnabled(read);
		settingsMenuItem.setEnabled(read);
		gotoMenu.setEnabled(read);
		docTypeMenu.setEnabled(read);
		reportsMenu.setEnabled(read);
		toolsMenu.setEnabled(read);
		prevButton.setEnabled(read);
		nextButton.setEnabled(read);
		findByNumberButton.setEnabled(read);
		searchButton.setEnabled(read);

		newDocMenuItem.setEnabled(create);
		newDocButton.setEnabled(create);
		vatDocumentMenuItem.setEnabled(create);

		deleteDocMenuItem.setEnabled(edit);
		addEntryMenuItem.setEnabled(edit);
		addEntryButton.setEnabled(edit);
		removeEntryMenuItem.setEnabled(edit);
		setIgnoreFlagMenuItem.setEnabled(edit);
		removeEntryButton.setEnabled(edit);
		entryTemplateMenu.setEnabled(edit);
		pasteMenuItem.setEnabled(edit);
		numberTextField.setEditable(edit);
		dateTextField.setEditable(edit);
	}

	/**
	 * Tallentaa tositteen tiedot, jos käyttäjä on tehnyt
	 * muutoksia niihin.
	 *
	 * @return <code>false</code>, jos tallentaminen epäonnistuu
	 */
	public boolean saveDocumentIfChanged() {
		stopEditing();

		if (!model.isDocumentChanged() || !model.isDocumentEditable()) {
			return true;
		}

		if (logger.isLoggable(Level.FINE)) {
			Document document = model.getDocument();
			logger.fine(String.format("Tallennetaan tosite %d (ID %d)",
					document.getNumber(), document.getId()));
		}

		boolean numberChanged = false;

		try {
			int result = updateModel();

			if (result < 0) {
				return false;
			}

			if (registry.getSettings().getProperty("debitCreditRemark", "false").equals("true")) {
				if (debitTotal.compareTo(creditTotal) != 0) {
					SwingUtils.showInformationMessage(this, "Debet- ja kredit-vientien summat eroavat toisistaan.");
				}
			}

			model.saveDocument();
			numberChanged = (result == 1);
		}
		catch (DataAccessException e) {
			String message = "Tositetietojen tallentaminen epäonnistui";
			logger.log(Level.SEVERE, message, e);
			SwingUtils.showDataAccessErrorMessage(this, e, message);
			return false;
		}

		if (numberChanged) {
			refreshModel(false);
		}
		else {
			updatePosition();
		}

		return true;
	}

	/**
	 * Päivittää käyttäjän syöttämät tiedot <code>DocumentModel</code>ille.
	 *
	 * @return -1, jos tiedot ovat virheellisiä; 0, jos tietojen päivittäminen onnistui;
	 * 1, jos päivittäminen onnistui ja tositenumero on muuttunut
	 */
	protected int updateModel() {
		Document document = model.getDocument();
		int result = 0;
		stopEditing();

		try {
			int number = Integer.parseInt(numberTextField.getText());

			if (number != document.getNumber() && JOptionPane.showConfirmDialog(
					this, "Haluatko varmasti muuttaa tositenumeroa?",
					Kirjanpito.APP_NAME, JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE)
					!= JOptionPane.YES_OPTION) {
				numberTextField.setText(Integer.toString(document.getNumber()));
				number = document.getNumber();
			}

			/* Tarkistetaan tositenumeron oikeellisuus, jos käyttäjä on muuttanut sitä. */
			if (number != document.getNumber()) {
				int r;

				try {
					r = model.validateDocumentNumber(number);
				}
				catch (DataAccessException e) {
					String message = "Tositetietojen hakeminen epäonnistui";
					logger.log(Level.SEVERE, message, e);
					SwingUtils.showDataAccessErrorMessage(this, e, message);
					return -2;
				}

				if (r == -1) {
					SwingUtils.showErrorMessage(this, String.format("Tositenumero %d on jo käytössä.", number));
					return -1;
				}
				else if (r == -2) {
					DocumentType documentType = model.getDocumentType();
					SwingUtils.showErrorMessage(this, String.format("Tositenumero %d ei kuulu tositelajin \"%s\" numerovälille (%d-%d).",
							number, documentType.getName(), documentType.getNumberStart(), documentType.getNumberEnd()));
					return -1;
				}

				document.setNumber(number);
				result = 1;
			}
		}
		catch (NumberFormatException e) {
			SwingUtils.showErrorMessage(this, "Virheellinen tositenumero.");
			numberTextField.requestFocusInWindow();
			return -1;
		}

		try {
			document.setDate(dateTextField.getDate());
		}
		catch (ParseException e) {
			SwingUtils.showErrorMessage(this, "Virheellinen päivämäärä.");
			dateTextField.requestFocusInWindow();
			return -1;
		}

		if (document.getDate() == null) {
			SwingUtils.showErrorMessage(this, "Syötä tositteen päivämäärä ennen tallentamista.");
			dateTextField.requestFocusInWindow();
			return -1;
		}

		if (!model.isMonthEditable(document.getDate())) {
			SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM yyyy");
			SwingUtils.showErrorMessage(this, String.format("Kuukausi %s on lukittu.",
					dateFormat.format(document.getDate())));
			dateTextField.requestFocusInWindow();
			return -1;
		}

		Period period = registry.getPeriod();

		if (document.getDate().before(period.getStartDate()) ||
				document.getDate().after(period.getEndDate())) {
			SimpleDateFormat dateFormat = new SimpleDateFormat("d.M.yyyy");
			SwingUtils.showErrorMessage(this, String.format("Päivämäärä ei kuulu nykyiselle tilikaudelle\n%s - %s.",
					dateFormat.format(period.getStartDate()),
					dateFormat.format(period.getEndDate())));
			dateTextField.requestFocusInWindow();
			return -1;
		}

		removeEmptyEntry();
		int count = model.getEntryCount();

		for (int i = 0; i < count; i++) {
			if (model.getEntry(i).getAccountId() < 1) {
				SwingUtils.showErrorMessage(this, "Valitse tili ennen tallentamista.");
				entryTable.changeSelection(i, 0, false, false);
				entryTable.requestFocusInWindow();
				return -1;
			}

			if (model.getEntry(i).getAmount() == null) {
				SwingUtils.showErrorMessage(this, "Syötä viennin rahamäärä ennen tallentamista.");
				return -1;
			}
		}

		return result;
	}

	/**
	 * Poistaa viimeisen viennin, jos selite on sama kuin edellisessä viennissä
	 * ja lisäksi rahamäärä on nolla tai tiliä ei ole valittu.
	 */
	protected void removeEmptyEntry() {
		int count = model.getEntryCount();

		if (count > 0) {
			String prevDescription = "";

			if (count - 2 >= 0) {
				prevDescription = model.getEntry(
						count - 2).getDescription();
			}

			Entry lastEntry = model.getEntry(count - 1);

			if ((lastEntry.getAccountId() <= 0 ||
					BigDecimal.ZERO.compareTo(lastEntry.getAmount()) == 0) &&
					lastEntry.getDescription().equals(prevDescription)) {

				model.removeEntry(count - 1);
				tableModel.fireTableRowsDeleted(count - 1, count - 1);
				count--;
			}
		}
	}

	protected void saveDocumentTypeIfChanged() {
		try {
			model.saveDocumentType();
		}
		catch (DataAccessException e) {
			String message = "Asetuksien tallentaminen epäonnistui";
			logger.log(Level.SEVERE, message, e);
		}
	}

	/**
	 * Suorittaa varmuuskopioinnin sulkemisen yhteydessä.
	 */
	protected void performBackupOnClose() {
		BackupService backupService = BackupService.getInstance();
		
		if (!backupService.isEnabled()) {
			return;
		}
		
		String uri = backupService.getCurrentDatabase();
		if (uri == null || !uri.contains("sqlite")) {
			return;
		}
		
		boolean success = backupService.performBackup(uri);
		
		if (!success) {
			// Näytä varoitus mutta älä estä sulkemista
			logger.warning("Varmuuskopiointi epäonnistui sulkemisen yhteydessä");
		}
	}

	/**
	 * Näyttää dialogin varmuuskopion palauttamiseksi.
	 */
	public void restoreFromBackup() {
		BackupService backupService = BackupService.getInstance();
		File backupDir = backupService.getBackupDirectory();
		
		if (backupDir == null || !backupDir.exists()) {
			// Tarjoa mahdollisuus määrittää asetukset suoraan
			String[] options = {"Määritä asetukset", "Peruuta"};
			int choice = JOptionPane.showOptionDialog(this,
				"Varmuuskopiokansiota ei ole määritetty tai se ei ole olemassa.\n\n" +
				"Haluatko määrittää varmuuskopiointiasetukset nyt?",
				"Ei varmuuskopioita",
				JOptionPane.DEFAULT_OPTION,
				JOptionPane.INFORMATION_MESSAGE,
				null,
				options,
				options[0]);
			
			if (choice == 0) {
				// Avaa asetukset
				BackupSettingsDialog.show(this);
				// Tarkista uudelleen
				backupDir = backupService.getBackupDirectory();
				if (backupDir == null || !backupDir.exists()) {
					return;
				}
			} else {
				return;
			}
		}
		
		// Listaa varmuuskopiot
		File[] backups = backupService.listAllBackups();
		
		if (backups.length == 0) {
			JOptionPane.showMessageDialog(this,
				"Varmuuskopiokansiossa ei ole varmuuskopioita.\n\n" +
				"Kansio: " + backupDir.getAbsolutePath(),
				"Ei varmuuskopioita",
				JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		
		// Näytä valintadialogi
		RestoreBackupDialog dialog = new RestoreBackupDialog(this, backups, backupService);
		dialog.setVisible(true);
		
		File restoredFile = dialog.getRestoredFile();
		if (restoredFile != null) {
			// Avaa palautettu tietokanta
			int result = JOptionPane.showConfirmDialog(this,
				"Tietokanta palautettu:\n" + restoredFile.getAbsolutePath() + "\n\n" +
				"Haluatko avata sen nyt?",
				"Palautus onnistui",
				JOptionPane.YES_NO_OPTION,
				JOptionPane.QUESTION_MESSAGE);
			
			if (result == JOptionPane.YES_OPTION) {
				openSqliteDataSource(restoredFile);
			}
		}
	}

	/**
	 * Etsii tositelajin, johon tositenumero <code>number</code> kuuluu.
	 *
	 * @param number tositenumero
	 * @return tositelajin järjestysnumero tai -1, jos tositelajia ei löytynyt
	 */
	public int findDocumentTypeByNumber(int number) {
		int index = 0;

		for (DocumentType type : registry.getDocumentTypes()) {
			if (number >= type.getNumberStart() && number <= type.getNumberEnd()) {
				return index;
			}

			index++;
		}

		return -1;
	}


	/**
	 * Lopettaa vientien muokkaamisen.
	 */
	protected void stopEditing() {
		if (entryTable.isEditing())
			entryTable.getCellEditor().stopCellEditing();
	}

	/**
	 * Valitsee taulukon seuraavan solun.
	 */
	protected void moveToNextCell() {
		nextCellAction.actionPerformed(null);
	}

	private RegistryAdapter registryListener = new RegistryAdapter() {
		public void settingsChanged() {
			model.loadLockedMonths();
			updateTableSettings();
			updateDocument();
			updateTitle();
		}

		public void entryTemplatesChanged() {
			updateEntryTemplates();
		}

		public void documentTypesChanged() {
			updateDocumentTypes();
			refreshModel(true);
		}

		public void periodChanged() {
			updatePeriod();
			refreshModel(true);
		}
	};


	/**
	 * Generoi vapaan tiedostonimen tietokannalle.
	 * Jos "kirjanpito.sqlite" on olemassa, ehdottaa "kirjanpito_2.sqlite" jne.
	 */
	public File generateUniqueFileName(File directory, String baseName) {
		File file = new File(directory, baseName + ".sqlite");
		if (!file.exists()) {
			return file;
		}
		
		int counter = 2;
		while (true) {
			file = new File(directory, baseName + "_" + counter + ".sqlite");
			if (!file.exists()) {
				return file;
			}
			counter++;
			if (counter > 1000) { // Turvarajoitus
				break;
			}
		}
		return new File(directory, baseName + "_" + System.currentTimeMillis() + ".sqlite");
	}

	// ========================================
	// ACCESSORS FOR DocumentMenuHandler
	// ========================================

	/**
	 * Palauttaa DocumentModel-instanssin.
	 * @return model
	 */
	public DocumentModel getModel() {
		return model;
	}

	/**
	 * Palauttaa DocumentModel-instanssin.
	 * Implementoi NavigationCallbacks.getDocumentModel().
	 * @return model
	 */
	public DocumentModel getDocumentModel() {
		return model;
	}

	/**
	 * Palauttaa SQLite-tiedostosuodattimen.
	 * @return sqliteFileFilter
	 */
	public FileFilter getSqliteFileFilter() {
		return sqliteFileFilter;
	}

	/**
	 * Palauttaa EntryTableModel-instanssin.
	 * @return tableModel
	 */
	public EntryTableModel getTableModel() {
		return tableModel;
	}

	/**
	 * Palauttaa entryTable-instanssin.
	 * @return entryTable
	 */
	public JTable getEntryTable() {
		return entryTable;
	}

	/**
	 * Palauttaa Registry-instanssin.
	 * @return registry
	 */
	public Registry getRegistry() {
		return registry;
	}

	/**
	 * Palauttaa pääikkunan dialogeja varten.
	 * Implementoi NavigationCallbacks.getParentWindow().
	 * @return this frame
	 */
	public java.awt.Window getParentWindow() {
		return this;
	}

	// ========================================
	// MENU ACTION LISTENERS
	// ========================================
	// Organized by menu category as defined in DocumentMenuBuilder
	// See DOCUMENTFRAME-MENU-REFACTORING.md for refactoring plan
	//
	// Note: Some listeners (newDatabaseListener, openDatabaseListener, databaseSettingsListener)
	// are defined earlier in the file (lines 193-254) because they are needed before
	// this section during object initialization.

	// --- File Menu ---
	// Also includes: newDatabaseListener, openDatabaseListener, databaseSettingsListener (defined at line 193)

	/* Lopeta */
	private ActionListener quitListener = menuHandler.getQuitListener();

	// --- Go Menu ---

	/* Edellinen tosite */
	private ActionListener prevDocListener = menuHandler.getPrevDocListener();

	/* Seuraava tosite */
	private ActionListener nextDocListener = menuHandler.getNextDocListener();

	/* Ensimmäinen tosite */
	private ActionListener firstDocListener = menuHandler.getFirstDocListener();

	/* Viimeinen tosite */
	private ActionListener lastDocListener = menuHandler.getLastDocListener();

	/* Hae numerolla */
	private ActionListener findDocumentByNumberListener = menuHandler.getFindDocumentByNumberListener();

	/* Etsi */
	private ActionListener searchListener = menuHandler.getSearchListener();

	// --- Edit Menu ---

	/* Uusi tosite */
	private ActionListener newDocListener = menuHandler.getNewDocListener();

	/* Poista tosite */
	private ActionListener deleteDocListener = menuHandler.getDeleteDocListener();

	/* Muokkaa vientimalleja */
	private ActionListener editEntryTemplatesListener = menuHandler.getEditEntryTemplatesListener();

	/* Luo vientimalli tositteesta */
	private ActionListener createEntryTemplateListener = menuHandler.getCreateEntryTemplateListener();

	/* Vientimalli */
	private ActionListener entryTemplateListener = menuHandler.getEntryTemplateListener();

	// --- Settings Menu ---

	/* Vie */
	private ActionListener exportListener = menuHandler.getExportListener();

	/* Tuo CSV */
	private ActionListener csvImportListener = menuHandler.getCsvImportListener();

	/* Tilikartta */
	private ActionListener chartOfAccountsListener = menuHandler.getChartOfAccountsListener();

	/* Alkusaldot */
	private ActionListener startingBalancesListener = menuHandler.getStartingBalancesListener();

	/* Perustiedot */
	private ActionListener propertiesListener = menuHandler.getPropertiesListener();

	/* Kirjausasetukset */
	private ActionListener settingsListener = menuHandler.getSettingsListener();

	/* Ulkoasu */
	private ActionListener appearanceListener = menuHandler.getAppearanceListener();

	/* Varmuuskopiointiasetukset */
	private ActionListener backupSettingsListener = menuHandler.getBackupSettingsListener();

	/* Palauta varmuuskopiosta */
	private ActionListener restoreBackupListener = menuHandler.getRestoreBackupListener();

	// --- Edit Menu Entry Actions (Ctrl+N, Ctrl+D) ---

	/* Lisää vienti */
	private AbstractAction addEntryListener = new AbstractAction() {
		private static final long serialVersionUID = 1L;
		@Override
		public void actionPerformed(ActionEvent e) {
			if (!entryTable.isEditing()) {
				addEntry();
			}
		}
	};

	/* Poista vienti */
	private AbstractAction removeEntryListener = new AbstractAction() {
		private static final long serialVersionUID = 1L;
		@Override
		public void actionPerformed(ActionEvent e) {
			if (!entryTable.isEditing()) {
				removeEntry();
				if (entryTable.getRowCount() == 0) {
					dateTextField.requestFocusInWindow();
				}
			}
		}
	};

	// --- Document Type Menu ---

	/* Muokkaa tositelajeja */
	private ActionListener editDocTypesListener = menuHandler.getEditDocTypesListener();

	// --- Entry Table Actions (Ctrl+C, Ctrl+V) ---

	/* Kopioi */
	private Action copyEntriesAction = new AbstractAction() {
		private static final long serialVersionUID = 1L;
		@Override
		public void actionPerformed(ActionEvent e) {
			copyEntries();
		}
	}; // Note: AbstractAction needed for Action interface, cannot use simple lambda

	/* Liitä */
	private Action pasteEntriesAction = new AbstractAction() {
		private static final long serialVersionUID = 1L;
		@Override
		public void actionPerformed(ActionEvent e) {
			pasteEntries();
		}
	}; // Note: AbstractAction needed for Action interface, cannot use simple lambda

	/* Tositelaji */
	private ActionListener docTypeListener = menuHandler.getDocTypeListener();

	// --- Reports Menu ---

	/* Tilien saldot */
	private ActionListener printListener = menuHandler.getPrintListener();

	/* Muokkaa tulosteita */
	private ActionListener editReportsListener = menuHandler.getEditReportsListener();

	// --- Tools Menu ---

	/* Tilien saldojen vertailu */
	private ActionListener balanceComparisonListener = menuHandler.getBalanceComparisonListener();

	/* ALV-tilien päättäminen */
	private ActionListener vatDocumentListener = menuHandler.getVatDocumentListener();

	/* Ohita vienti ALV-laskelmassa */
	private Action setIgnoreFlagToEntryAction = menuHandler.getSetIgnoreFlagToEntryAction();

	/* Muuta tositenumeroita */
	private ActionListener numberShiftListener = menuHandler.getNumberShiftListener();

	/* ALV-kantojen muutokset */
	private ActionListener vatChangeListener = menuHandler.getVatChangeListener();

	// --- Help Menu ---

	/* Ohje */
	private ActionListener helpListener = menuHandler.getHelpListener();

	/* Virheenjäljitystietoja */
	private ActionListener debugListener = menuHandler.getDebugListener();

	/* Tietoja ohjelmasta */
	private ActionListener aboutListener = menuHandler.getAboutListener();

	// --- Entry Table Navigation Actions (Shift+Enter, Enter, Tab) ---

	private AbstractAction prevCellAction = new AbstractAction() {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			int column = mapColumnIndexToModel(entryTable.getSelectedColumn());
			int row = entryTable.getSelectedRow();
			boolean changed = false;

			if (entryTable.isEditing())
				entryTable.getCellEditor().stopCellEditing();

			if (row < 0) {
				addEntry();
				return;
			}

			/* Tilisarakkeesta siirrytään edellisen viennin selitteeseen. */
			if (column == 0) {
				if (row > 0) {
					row--;
					column = 4;
					changed = true;
				}
				else {
					dateTextField.requestFocusInWindow();
					return;
				}
			}
			/* Jos kreditsarakkeessa on 0,00, siirrytään debetsarakkeeseen.
			 * Muussa tapauksessa kredit- ja debetsarakkeesta siirrytään
			 * selitesarakkeeseen. */
			else if (column == 1 || column == 2) {
				BigDecimal amount = model.getEntry(row).getAmount();

				if (amount.compareTo(BigDecimal.ZERO) == 0 && column == 2) {
					column = 1;
				}
				else {
					column = 0;
				}

				changed = true;
			}
			else {
				Entry entry = model.getEntry(row);
				column = entry.isDebit() ? 1 : 2;
				changed = true;
			}

			if (changed) {
				column = mapColumnIndexToView(column);
				entryTable.changeSelection(row, column, false, false);
				entryTable.editCellAt(row, column);
			}
		}
	};

	private AbstractAction nextCellAction = new AbstractAction() {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			int column = mapColumnIndexToModel(entryTable.getSelectedColumn());
			int row = entryTable.getSelectedRow();
			boolean changed = false;

			if (entryTable.isEditing())
				entryTable.getCellEditor().stopCellEditing();

			if (row < 0) {
				addEntry();
				return;
			}

			/* Tilisarakkeesta siirrytään debet- tai kreditsarakkeeseen. */
			if (column == 0) {
				column = model.getEntry(row).isDebit() ? 1 : 2;
				changed = true;
			}
			/* Jos debetsarakkeessa on 0,00, siirrytään kreditsarakkeeseen.
			 * Muussa tapauksessa kredit- ja debetsarakkeesta siirrytään
			 * selitesarakkeeseen. */
			else if (column == 1 || column == 2) {
				BigDecimal amount = model.getEntry(row).getAmount();

				if (amount.compareTo(BigDecimal.ZERO) == 0 && column == 1) {
					column = 2;
				}
				else {
					column = 4;
				}

				changed = true;
			}
			else if (column == 3) {
				column = 4;
				changed = true;
			}
			else {
				int lastRow = entryTable.getRowCount() - 1;

				/* Selitesarakkeesta siirrytään seuraavan rivin
				 * tilisarakkeeseen. */
				if (row < lastRow) {
					column = 0;
					row++;
					changed = true;
				}
				else if (row >= 0) {
					String prevDescription = "";

					if (row > 0) {
						prevDescription = model.getEntry(
								row - 1).getDescription();
					}

					Entry entry = model.getEntry(row);

					/* Siirrytään uuteen tositteeseen, jos vientejä on jo vähintään kaksi
					 * ja selite on sama kuin edellisessä viennissä ja lisäksi
					 * tiliä ei ole valittu tai rahamäärä on nolla. */
					if (row == lastRow && row >= 1 && entry.getDescription().equals(prevDescription) &&
							(entry.getAccountId() < 0 || BigDecimal.ZERO.compareTo(entry.getAmount()) == 0)) {
						createDocument();
					}
					else {
						addEntry();
					}
				}
			}

			if (changed) {
				column = mapColumnIndexToView(column);
				entryTable.changeSelection(row, column, false, false);
				entryTable.editCellAt(row, column);
			}
		}
	};

	private AbstractAction toggleDebitCreditAction = new AbstractAction() {
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			if (entryTable.getSelectedRowCount() != 1 || entryTable.getSelectedColumnCount() != 1) {
				return;
			}

			int column = entryTable.getSelectedColumn();

			if (column != 1 && column != 2) {
				return;
			}

			boolean editing = entryTable.isEditing();
			int index = entryTable.getSelectedRow();

			if (editing) {
				entryTable.getCellEditor().stopCellEditing();
			}

			boolean addVatEntries = model.getVatAmount(index).compareTo(BigDecimal.ZERO) != 0;
			Entry entry = model.getEntry(index);
			entry.setDebit(!entry.isDebit());
			model.updateAmount(index, model.getVatIncludedAmount(index), addVatEntries);
			model.setDocumentChanged();
			tableModel.fireTableRowsUpdated(index, index);

			if (editing) {
				entryTable.editCellAt(index, entry.isDebit() ? 1 : 2);
			}
		}
	};



	private class InitializationWorkerListener implements PropertyChangeListener {
		private Window owner;
		private DataSourceInitializationWorker worker;

		public InitializationWorkerListener(Window owner,
				DataSourceInitializationWorker worker) {

			this.owner = owner;
			this.worker = worker;
		}

		public void propertyChange(PropertyChangeEvent ev) {
			if (ev.getPropertyName().equals("state") &&
					worker.getState() == StateValue.DONE) {

				try {
					worker.get();
				}
				catch (CancellationException e) {
					return;
				}
				catch (Exception e) {
					e.printStackTrace();
					logger.log(Level.SEVERE, "Tietokannan luonti epäonnistui",
							e.getCause());

					SwingUtils.showErrorMessage(owner, "Tietokannan luonti epäonnistui. " +
							e.getCause().getMessage());
					return;
				}

				try {
					model.initialize();
				}
				catch (DataAccessException e) {
					String message = "Tietokantayhteyden avaaminen epäonnistui";
					logger.log(Level.SEVERE, message, e);
					SwingUtils.showDataAccessErrorMessage(owner, e, message);
					return;
				}

				updatePeriod();
				updatePosition();
				updateDocument();
				updateTotalRow();
				updateEntryTemplates();
				updateDocumentTypes();
				updateTableSettings();
			}
		}
	}
}
