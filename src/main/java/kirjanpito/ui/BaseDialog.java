package kirjanpito.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.KeyEvent;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

import static kirjanpito.ui.UIConstants.*;

/**
 * Abstrakti pohjaluokka kaikille Tilitin-dialogeille.
 *
 * Tarjoaa yhtenäisen rakenteen ja standarditoiminnallisuuden:
 * - Yhtenäinen layout (BorderLayout: content + button panel)
 * - Standardipainikkeet (OK, Cancel, Apply)
 * - Teematuki (FlatLaf dark/light mode)
 * - Keyboard shortcuts (ESC = cancel, Enter = OK)
 * - Yhtenäiset marginaalit ja spacing
 *
 * Aliluokkien tarvitsee vain toteuttaa:
 * - createContentPanel() - dialogin sisältö
 * - onOK() - mitä tehdään kun painetaan OK (valinnainen)
 * - onCancel() - mitä tehdään kun painetaan Cancel (valinnainen)
 *
 * Esimerkki:
 * <pre>
 * public class MyDialog extends BaseDialog {
 *     public MyDialog(Frame owner) {
 *         super(owner, "Otsikko");
 *         initialize();
 *     }
 *
 *     protected JPanel createContentPanel() {
 *         JPanel panel = new JPanel();
 *         // Rakenna sisältö...
 *         return panel;
 *     }
 *
 *     protected void onOK() {
 *         if (validateInput()) {
 *             saveData();
 *             super.onOK(); // Sulkee dialogin
 *         }
 *     }
 * }
 * </pre>
 *
 * @since 2.0.4
 */
public abstract class BaseDialog extends JDialog {

    private static final long serialVersionUID = 1L;

    /**
     * Dialogin tulos: true jos OK painettiin, false jos Cancel.
     */
    protected boolean result = false;

    // Standardipainikkeet
    protected JButton okButton;
    protected JButton cancelButton;
    protected JButton applyButton; // Valinnainen

    /**
     * Luo uusi dialogi.
     *
     * @param owner parent frame
     * @param title dialogin otsikko
     */
    protected BaseDialog(Frame owner, String title) {
        super(owner, title, true); // Modal
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }

    /**
     * Alustaa dialogin. Kutsu tätä konstruktorin lopussa.
     */
    protected void initialize() {
        setLayout(new BorderLayout());

        // Sisältöpaneeli
        JPanel contentPanel = createContentPanel();
        if (contentPanel != null) {
            contentPanel.setBorder(DIALOG_BORDER);
            add(contentPanel, BorderLayout.CENTER);
        }

        // Painikepaneeli
        JPanel buttonPanel = createButtonPanel();
        if (buttonPanel != null) {
            add(buttonPanel, BorderLayout.SOUTH);
        }

        // Keyboard shortcuts
        setupKeyboardShortcuts();

        // Teema
        applyTheme();

        // Koko ja sijainti
        pack();
        setLocationRelativeTo(getOwner());
    }

    /**
     * Luo dialogin sisältöpaneeli.
     * Aliluokan TÄYTYY toteuttaa tämä.
     *
     * @return sisältöpaneeli
     */
    protected abstract JPanel createContentPanel();

    /**
     * Luo painikepaneeli. Oletuksena OK ja Cancel.
     * Ylikirjoita jos haluat erilaisen painikejärjestelyn.
     *
     * @return painikepaneeli
     */
    protected JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, COMPONENT_SPACING, 0));
        panel.setBorder(BUTTON_PANEL_BORDER);

        // Cancel-painike
        cancelButton = new JButton("Peruuta");
        cancelButton.addActionListener(e -> onCancel());

        // OK-painike
        okButton = new JButton("OK");
        okButton.addActionListener(e -> onOK());

        panel.add(cancelButton);
        panel.add(okButton);

        // OK on oletuspainike (Enter-näppäin)
        getRootPane().setDefaultButton(okButton);

        return panel;
    }

    /**
     * Luo painikepaneeli Apply-painikkeella.
     * Käytä tätä jos haluat OK, Apply ja Cancel -painikkeet.
     *
     * @return painikepaneeli Apply-painikkeella
     */
    protected JPanel createButtonPanelWithApply() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, COMPONENT_SPACING, 0));
        panel.setBorder(BUTTON_PANEL_BORDER);

        // Cancel-painike
        cancelButton = new JButton("Peruuta");
        cancelButton.addActionListener(e -> onCancel());

        // Apply-painike
        applyButton = new JButton("Käytä");
        applyButton.addActionListener(e -> onApply());

        // OK-painike
        okButton = new JButton("OK");
        okButton.addActionListener(e -> onOK());

        panel.add(cancelButton);
        panel.add(applyButton);
        panel.add(okButton);

        getRootPane().setDefaultButton(okButton);

        return panel;
    }

    /**
     * Asettaa näppäinoikotiet.
     * ESC = Cancel
     * Enter = OK (asetetaan defaultButtonilla)
     */
    protected void setupKeyboardShortcuts() {
        // ESC sulkee dialogin
        KeyStroke escapeKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
        getRootPane().registerKeyboardAction(
            e -> onCancel(),
            escapeKeyStroke,
            JComponent.WHEN_IN_FOCUSED_WINDOW
        );
    }

    /**
     * Käyttää FlatLaf-teemaa dialogiin.
     * Ylikirjoita jos tarvitset custom-teemoitusta.
     */
    protected void applyTheme() {
        // FlatLaf päivittää automaattisesti kun teemaa vaihdetaan,
        // mutta voit lisätä custom-logiikkaa tähän tarvittaessa
    }

    /**
     * Kutsutaan kun OK painetaan.
     * Oletuksena asettaa result = true ja sulkee dialogin.
     * Ylikirjoita jos tarvitset validointia tai muuta logiikkaa.
     */
    protected void onOK() {
        result = true;
        dispose();
    }

    /**
     * Kutsutaan kun Cancel painetaan.
     * Oletuksena asettaa result = false ja sulkee dialogin.
     * Ylikirjoita jos tarvitset custom-logiikkaa.
     */
    protected void onCancel() {
        result = false;
        dispose();
    }

    /**
     * Kutsutaan kun Apply painetaan.
     * Oletuksena ei tee mitään.
     * Ylikirjoita jos käytät Apply-painiketta.
     */
    protected void onApply() {
        // Aliluokka voi ylikirjoittaa
    }

    /**
     * Palauttaa dialogin tuloksen.
     *
     * @return true jos OK painettiin, false jos Cancel
     */
    public boolean getResult() {
        return result;
    }

    /**
     * Näyttää dialogin ja odottaa kunnes se suljetaan.
     *
     * @return true jos OK painettiin, false jos Cancel
     */
    public boolean showDialog() {
        setVisible(true);
        return result;
    }

    /**
     * Helper-metodi painikkeiden käyttöönoton hallintaan.
     *
     * @param enabled true = käytössä, false = pois käytöstä
     */
    protected void setButtonsEnabled(boolean enabled) {
        if (okButton != null) okButton.setEnabled(enabled);
        if (applyButton != null) applyButton.setEnabled(enabled);
    }

    /**
     * Helper-metodi OK-painikkeen tekstin asettamiseen.
     *
     * @param text painikkeen teksti
     */
    protected void setOKButtonText(String text) {
        if (okButton != null) {
            okButton.setText(text);
        }
    }

    /**
     * Helper-metodi Cancel-painikkeen tekstin asettamiseen.
     *
     * @param text painikkeen teksti
     */
    protected void setCancelButtonText(String text) {
        if (cancelButton != null) {
            cancelButton.setText(text);
        }
    }
}
