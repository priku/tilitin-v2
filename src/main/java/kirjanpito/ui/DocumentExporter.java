package kirjanpito.ui;

import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import kirjanpito.models.CSVExportWorker;
import kirjanpito.util.AppSettings;
import kirjanpito.util.Registry;

/**
 * Hallinnoi dokumenttien vientiä eri formaatteihin (CSV, jne.).
 *
 * Vastuualueet:
 * - CSV-vienti
 * - Tiedostojen valinta
 * - Vientitoimintojen koordinointi
 *
 * Osa DocumentFrame-refaktorointia (Phase 1b, v2.1.0)
 *
 * @author Claude Sonnet 4.5 (DocumentFrame refactoring)
 */
public class DocumentExporter {
	private final DocumentFrame parentFrame;
	private final Registry registry;

	/**
	 * Rajapinta CSV-viennin aloitukselle.
	 */
	public interface CSVExportStarter {
		/**
		 * Aloittaa CSV-viennin progress-dialogilla.
		 *
		 * @param worker CSV-vientiworker
		 */
		void startCSVExport(CSVExportWorker worker);
	}

	private final CSVExportStarter csvExportStarter;

	/**
	 * Luo uuden DocumentExporter-olion.
	 *
	 * @param parentFrame pääikkuna
	 * @param registry tietovarasto
	 * @param csvExportStarter CSV-viennin aloittaja
	 */
	public DocumentExporter(DocumentFrame parentFrame, Registry registry,
			CSVExportStarter csvExportStarter) {
		this.parentFrame = parentFrame;
		this.registry = registry;
		this.csvExportStarter = csvExportStarter;
	}

	/**
	 * Tallentaa viennit CSV-tiedostoon.
	 * Näyttää tiedostonvalintadialog ja käynnistää viennin.
	 */
	public void exportToCSV() {
		AppSettings settings = AppSettings.getInstance();
		String path = settings.getString("csv-directory", ".");
		JFileChooser fc = new JFileChooser(path);
		fc.setFileFilter(new FileFilter() {
			public boolean accept(File file) {
				return file.isDirectory() || file.getName().endsWith(".csv");
			}

			public String getDescription() {
				return "CSV-tiedostot";
			}
		});

		if (fc.showSaveDialog(parentFrame) == JFileChooser.APPROVE_OPTION) {
			File file = fc.getSelectedFile();

			// Lisää .csv-pääte jos sitä ei ole
			if (!file.getName().toLowerCase().endsWith(".csv")) {
				file = new File(file.getAbsolutePath() + ".csv");
			}

			settings.set("csv-directory",
					file.getParentFile().getAbsolutePath());

			CSVExportWorker worker = new CSVExportWorker(registry, file);
			csvExportStarter.startCSVExport(worker);
		}
	}
}
