package kirjanpito.ui;

import java.awt.Window;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.SwingWorker.StateValue;

import kirjanpito.db.DataAccessException;
import kirjanpito.db.DataSource;
import kirjanpito.models.DataSourceInitializationModel;
import kirjanpito.models.DataSourceInitializationWorker;
import kirjanpito.models.DocumentModel;
import kirjanpito.util.AppSettings;
import kirjanpito.util.BackupService;
import kirjanpito.util.RecentDatabases;
import kirjanpito.util.Registry;

/**
 * Manages data source operations for DocumentFrame.
 * Handles opening, closing, and initializing database connections.
 * 
 * Extracted from DocumentFrame as part of Phase 11 refactoring.
 */
public class DocumentDataSourceManager {

	private final DocumentModel model;
	private final Registry registry;
	private final DataSourceCallbacks callbacks;
	
	private static final Logger logger = Logger.getLogger(Kirjanpito.LOGGER_NAME);

	/**
	 * Callback interface for data source operations.
	 */
	public interface DataSourceCallbacks {
		/** Gets the parent window */
		Window getParentWindow();
		
		/** Gets the parent frame (for dialogs that require Frame) */
		java.awt.Frame getParentFrame();
		
		/** Sets components enabled/disabled */
		void setComponentsEnabled(boolean read, boolean create, boolean edit);
		
		/** Updates title bar */
		void updateTitle();
		
		/** Updates period display */
		void updatePeriod();
		
		/** Updates position display */
		void updatePosition();
		
		/** Updates document fields */
		void updateDocument();
		
		/** Updates total row */
		void updateTotalRow();
		
		/** Updates entry templates */
		void updateEntryTemplates();
		
		/** Updates document types */
		void updateDocumentTypes();
		
		/** Updates table settings */
		void updateTableSettings();
		
		/** Sets data source for attachments panel */
		void setAttachmentsPanelDataSource(DataSource dataSource);
		
		/** Updates recent databases menu */
		void updateRecentDatabasesMenu();
		
		/** Updates backup status label */
		void updateBackupStatusLabel();
		
		/** Shows error message */
		void showErrorMessage(String message);
		
		/** Shows data access error */
		void showDataAccessError(DataAccessException e, String message);
	}

	/**
	 * Constructor.
	 */
	public DocumentDataSourceManager(DocumentModel model, Registry registry, 
			DataSourceCallbacks callbacks) {
		this.model = model;
		this.registry = registry;
		this.callbacks = callbacks;
	}

	/**
	 * Opens database connection and fetches required data.
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
			callbacks.setComponentsEnabled(false, false, false);
			callbacks.showDataAccessError(e, message);
		}

		callbacks.updateTitle();
		callbacks.updatePeriod();
		callbacks.updatePosition();
		callbacks.updateDocument();
		callbacks.updateTotalRow();
		callbacks.updateEntryTemplates();
		callbacks.updateDocumentTypes();
		callbacks.updateTableSettings();
		
		// Update attachments panel with data source
		callbacks.setAttachmentsPanelDataSource(registry.getDataSource());
		
		// Tallenna viimeisimpien tietokantojen listaan
		String dbUrl = AppSettings.getInstance().getString("database.url", "");
		if (!dbUrl.isEmpty()) {
			RecentDatabases.getInstance().addDatabase(dbUrl);
			callbacks.updateRecentDatabasesMenu();
			
			// Käynnistä AutoBackup jos käytössä
			BackupService.getInstance().setCurrentDatabase(dbUrl);
			callbacks.updateBackupStatusLabel();
		}
	}

	/**
	 * Opens SQLite database file.
	 */
	public void openSqliteDataSource(File file) {
		AppSettings settings = AppSettings.getInstance();
		settings.set("database.url", String.format("jdbc:sqlite:%s",
				file.getAbsolutePath().replace(File.pathSeparatorChar, '/')));
		settings.set("database.username", "");
		settings.set("database.password", "");
		openDataSource();
	}

	/**
	 * Initializes data source with default data.
	 */
	public void initializeDataSource() {
		callbacks.setComponentsEnabled(false, false, false);

		DataSourceInitializationModel initModel =
			new DataSourceInitializationModel();

		DataSourceInitializationDialog dialog =
			new DataSourceInitializationDialog(callbacks.getParentFrame(),
					registry, initModel);

		dialog.create();
		dialog.setVisible(true);

		DataSourceInitializationWorker worker = dialog.getWorker();

		if (worker == null) {
			model.closeDataSource();
		}
		else {
			worker.addPropertyChangeListener(
					new InitializationWorkerListener(callbacks.getParentWindow(), worker));
		}
	}

	/**
	 * Updates recent databases menu.
	 */
	public void updateRecentDatabasesMenu(JMenu recentMenu) {
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
				updateRecentDatabasesMenu(recentMenu);
			});
			recentMenu.add(clearItem);
		}
	}

	/**
	 * Opens recent database.
	 */
	public void openRecentDatabase(String dbUrl) {
		AppSettings settings = AppSettings.getInstance();
		settings.set("database.url", dbUrl);
		
		// Jos SQLite, tyhjennä käyttäjätunnus/salasana
		if (dbUrl.startsWith("jdbc:sqlite:")) {
			settings.set("database.username", "");
			settings.set("database.password", "");
		}
		
		openDataSource();
	}

	/**
	 * Listener for data source initialization completion.
	 */
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

				callbacks.updatePeriod();
				callbacks.updatePosition();
				callbacks.updateDocument();
				callbacks.updateTotalRow();
				callbacks.updateEntryTemplates();
				callbacks.updateDocumentTypes();
				callbacks.updateTableSettings();
			}
		}
	}
}
