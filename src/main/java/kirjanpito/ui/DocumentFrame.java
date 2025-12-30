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
	
	// Entry manager for add/remove/copy/paste and cell navigation
	private DocumentEntryManager entryManager;
	
	// Document validator for save/validation operations
	private DocumentValidator documentValidator;
	
	// Data source manager for database operations
	private DocumentDataSourceManager dataSourceManager;
	
	// Lazy wrapper actions for menu/toolbar (entryManager is created later)
	private Action addEntryListener = new AbstractAction() {
		private static final long serialVersionUID = 1L;
		@Override
		public void actionPerformed(ActionEvent e) {
			if (entryManager != null) entryManager.getAddEntryAction().actionPerformed(e);
		}
	};
	
	private Action removeEntryListener = new AbstractAction() {
		private static final long serialVersionUID = 1L;
		@Override
		public void actionPerformed(ActionEvent e) {
			if (entryManager != null) entryManager.getRemoveEntryAction().actionPerformed(e);
		}
	};
	
	private Action copyEntriesAction = new AbstractAction() {
		private static final long serialVersionUID = 1L;
		@Override
		public void actionPerformed(ActionEvent e) {
			if (entryManager != null) entryManager.getCopyEntriesAction().actionPerformed(e);
		}
	};
	
	private Action pasteEntriesAction = new AbstractAction() {
		private static final long serialVersionUID = 1L;
		@Override
		public void actionPerformed(ActionEvent e) {
			if (entryManager != null) entryManager.getPasteEntriesAction().actionPerformed(e);
		}
	};
	
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
			return entryManager != null ? entryManager.getToggleDebitCreditAction() : null;
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
		
		// Create entry manager for add/remove/copy/paste and cell navigation
		entryManager = new DocumentEntryManager(model, tableModel, entryTable, registry, 
				dateTextField, formatter, new DocumentEntryManager.EntryCallbacks() {
			@Override
			public void updateTotalRow() { DocumentFrame.this.updateTotalRow(); }
			
			@Override
			public void createDocument() { DocumentFrame.this.createDocument(); }
			
			@Override
			public int mapColumnIndexToModel(int viewIndex) { 
				return DocumentFrame.this.mapColumnIndexToModel(viewIndex); 
			}
			
			@Override
			public int mapColumnIndexToView(int modelIndex) { 
				return DocumentFrame.this.mapColumnIndexToView(modelIndex); 
			}
			
			@Override
			public boolean isDocumentEditable() { return model.isDocumentEditable(); }
			
			@Override
			public BigDecimal getVatIncludedAmount(int index) { return model.getVatIncludedAmount(index); }
			
			@Override
			public BigDecimal getVatAmount(int index) { return model.getVatAmount(index); }
		});
		
		// Create document validator for save/validation operations
		documentValidator = new DocumentValidator(model, tableModel, registry, 
				new DocumentValidator.ValidationCallbacks() {
			@Override
			public String getNumberText() { return numberTextField.getText(); }
			
			@Override
			public void setNumberText(String text) { numberTextField.setText(text); }
			
			@Override
			public java.util.Date getDate() throws java.text.ParseException { 
				return dateTextField.getDate(); 
			}
			
			@Override
			public void focusNumberField() { numberTextField.requestFocusInWindow(); }
			
			@Override
			public void focusDateField() { dateTextField.requestFocusInWindow(); }
			
			@Override
			public JTable getEntryTable() { return entryTable; }
			
			@Override
			public void stopEditing() { DocumentFrame.this.stopEditing(); }
			
			@Override
			public BigDecimal getDebitTotal() { return debitTotal; }
			
			@Override
			public BigDecimal getCreditTotal() { return creditTotal; }
			
			@Override
			public int showConfirmDialog(String message, String title) {
				return JOptionPane.showConfirmDialog(DocumentFrame.this, message, title, 
						JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
			}
			
			@Override
			public void showErrorMessage(String message) { 
				SwingUtils.showErrorMessage(DocumentFrame.this, message); 
			}
			
			@Override
			public void showInformationMessage(String message) { 
				SwingUtils.showInformationMessage(DocumentFrame.this, message); 
			}
			
			@Override
			public void showDataAccessError(DataAccessException e, String message) { 
				SwingUtils.showDataAccessErrorMessage(DocumentFrame.this, e, message); 
			}
			
			@Override
			public void updatePosition() { DocumentFrame.this.updatePosition(); }
			
			@Override
			public void refreshModel(boolean fullRefresh) { 
				DocumentFrame.this.refreshModel(fullRefresh); 
			}
			
			@Override
			public java.awt.Window getParentWindow() { return DocumentFrame.this; }
		});
		
		// Create data source manager for database operations
		dataSourceManager = new DocumentDataSourceManager(model, registry, 
				new DocumentDataSourceManager.DataSourceCallbacks() {
			@Override
			public java.awt.Window getParentWindow() { return DocumentFrame.this; }
			
			@Override
			public java.awt.Frame getParentFrame() { return DocumentFrame.this; }
			
			@Override
			public void setComponentsEnabled(boolean read, boolean create, boolean edit) {
				DocumentFrame.this.setComponentsEnabled(read, create, edit);
			}
			
			@Override
			public void updateTitle() { DocumentFrame.this.updateTitle(); }
			
			@Override
			public void updatePeriod() { DocumentFrame.this.updatePeriod(); }
			
			@Override
			public void updatePosition() { DocumentFrame.this.updatePosition(); }
			
			@Override
			public void updateDocument() { DocumentFrame.this.updateDocument(); }
			
			@Override
			public void updateTotalRow() { DocumentFrame.this.updateTotalRow(); }
			
			@Override
			public void updateEntryTemplates() { DocumentFrame.this.updateEntryTemplates(); }
			
			@Override
			public void updateDocumentTypes() { DocumentFrame.this.updateDocumentTypes(); }
			
			@Override
			public void updateTableSettings() { DocumentFrame.this.updateTableSettings(); }
			
			@Override
			public void setAttachmentsPanelDataSource(kirjanpito.db.DataSource dataSource) {
				if (attachmentsPanel != null) {
					attachmentsPanel.setDataSource(dataSource);
				}
			}
			
			@Override
			public void updateRecentDatabasesMenu() { 
				DocumentFrame.this.updateRecentDatabasesMenu(); 
			}
			
			@Override
			public void updateBackupStatusLabel() { 
				DocumentFrame.this.updateBackupStatusLabel(); 
			}
			
			@Override
			public void showErrorMessage(String message) { 
				SwingUtils.showErrorMessage(DocumentFrame.this, message); 
			}
			
			@Override
			public void showDataAccessError(DataAccessException e, String message) { 
				SwingUtils.showDataAccessErrorMessage(DocumentFrame.this, e, message); 
			}
		});
		
		// Set table actions (this will configure keyboard shortcuts)
		tableManager.setTableActions(new DocumentTableManager.TableActions() {
			@Override
			public Action getPrevCellAction() { return entryManager.getPrevCellAction(); }
			
			@Override
			public Action getNextCellAction() { return entryManager.getNextCellAction(); }
			
			@Override
			public Action getAddEntryAction() { return entryManager.getAddEntryAction(); }
			
			@Override
			public Action getRemoveEntryAction() { return entryManager.getRemoveEntryAction(); }
			
			@Override
			public Action getCopyEntriesAction() { return entryManager.getCopyEntriesAction(); }
			
			@Override
			public Action getPasteEntriesAction() { return entryManager.getPasteEntriesAction(); }
			
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
		entryManager.addEntry();
	}

	/**
	 * Poistaa käyttäjän valitseman viennin.
	 */
	public void removeEntry() {
		entryManager.removeEntry();
	}

	/**
	 * Kopioi valitut viennit leikepöydälle.
	 */
	public void copyEntries() {
		entryManager.copyEntries();
	}

	/**
	 * Liittää leikepöydällä olevat viennit.
	 */
	public void pasteEntries() {
		entryManager.pasteEntries();
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
		dataSourceManager.openDataSource();
	}

	public void openSqliteDataSource(File file) {
		dataSourceManager.openSqliteDataSource(file);
	}
	
	/**
	 * Päivittää viimeisimpien tietokantojen valikon.
	 */
	protected void updateRecentDatabasesMenu() {
		dataSourceManager.updateRecentDatabasesMenu(recentMenu);
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
		dataSourceManager.initializeDataSource();
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
		return documentValidator.saveDocumentIfChanged();
	}

	/**
	 * Päivittää käyttäjän syöttämät tiedot <code>DocumentModel</code>ille.
	 *
	 * @return -1, jos tiedot ovat virheellisiä; 0, jos tietojen päivittäminen onnistui;
	 * 1, jos päivittäminen onnistui ja tositenumero on muuttunut
	 */
	protected int updateModel() {
		return documentValidator.updateModel();
	}

	/**
	 * Poistaa viimeisen viennin, jos selite on sama kuin edellisessä viennissä
	 * ja lisäksi rahamäärä on nolla tai tiliä ei ole valittu.
	 */
	protected void removeEmptyEntry() {
		documentValidator.removeEmptyEntry();
	}

	protected void saveDocumentTypeIfChanged() {
		documentValidator.saveDocumentTypeIfChanged();
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
		entryManager.getNextCellAction().actionPerformed(null);
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

	// --- Document Type Menu ---

	/* Muokkaa tositelajeja */
	private ActionListener editDocTypesListener = menuHandler.getEditDocTypesListener();

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
}
