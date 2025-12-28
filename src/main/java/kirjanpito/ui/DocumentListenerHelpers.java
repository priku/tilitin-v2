package kirjanpito.ui;

import java.awt.Window;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.concurrent.CancellationException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.SwingWorker.StateValue;

import kirjanpito.db.DataAccessException;
import kirjanpito.models.DataSourceInitializationWorker;

/**
 * DocumentFrame-luokan apuluokat kuuntelijoille.
 * 
 * Sisältää sisäiset luokat jotka oli aiemmin DocumentFrame:ssa
 * mutta jotka voidaan erottaa selkeyden vuoksi.
 * 
 * @since 2.1.5
 */
public class DocumentListenerHelpers {
    
    private static final Logger logger = Logger.getLogger(DocumentListenerHelpers.class.getName());
    
    /**
     * Callback-rajapinta tietokannan alustuksen jälkeisille toimenpiteille.
     */
    public interface InitializationCallback {
        void initializeModel() throws DataAccessException;
        void updateAfterInitialization();
    }
    
    /**
     * Kuuntelija tietokannan alustusoperaatiolle.
     * Käsittelee SwingWorkerin valmistumisen ja virheiden näytön.
     */
    public static class InitializationWorkerListener implements PropertyChangeListener {
        private final Window owner;
        private final DataSourceInitializationWorker worker;
        private final InitializationCallback callback;

        public InitializationWorkerListener(Window owner,
                DataSourceInitializationWorker worker,
                InitializationCallback callback) {
            this.owner = owner;
            this.worker = worker;
            this.callback = callback;
        }

        @Override
        public void propertyChange(PropertyChangeEvent ev) {
            if (!ev.getPropertyName().equals("state") || worker.getState() != StateValue.DONE) {
                return;
            }

            try {
                worker.get();
            }
            catch (CancellationException e) {
                return;
            }
            catch (Exception e) {
                e.printStackTrace();
                logger.log(Level.SEVERE, "Tietokannan luonti epäonnistui", e.getCause());
                SwingUtils.showErrorMessage(owner, "Tietokannan luonti epäonnistui. " +
                        e.getCause().getMessage());
                return;
            }

            try {
                callback.initializeModel();
            }
            catch (DataAccessException e) {
                String message = "Tietokantayhteyden avaaminen epäonnistui";
                logger.log(Level.SEVERE, message, e);
                SwingUtils.showDataAccessErrorMessage(owner, e, message);
                return;
            }

            callback.updateAfterInitialization();
        }
    }
}
