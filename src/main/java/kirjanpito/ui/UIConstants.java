package kirjanpito.ui;

import java.awt.Dimension;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.border.Border;

/**
 * Yhtenäiset UI-vakiot Tilitin-sovellukselle.
 *
 * Tämä luokka keskittää kaikki käyttöliittymän spacing-, padding- ja
 * layout-vakiot yhteen paikkaan. Tarkoituksena on varmistaa johdonmukainen
 * ulkoasu kaikissa dialogeissa ja komponenteissa.
 *
 * Suunnitteluperiaatteet:
 * - Spacing perustuu 5px:n perusyksikköön (5, 10, 15, 20...)
 * - Modernit dialogit käyttävät suurempia marginaaleja (15px)
 * - Komponenttien välinen tila on tyypillisesti 5-10px
 * - Eri osioiden välinen tila on 15-20px
 *
 * @since 2.0.4
 */
public final class UIConstants {

    // Estä instantiointi
    private UIConstants() {
        throw new AssertionError("UIConstants ei ole instantioitavissa");
    }

    // ========== SPACING CONSTANTS ==========

    /**
     * Perusyksikkö spacing-laskennalle (5px).
     * Kaikki spacing-arvot ovat tämän monikertoja.
     */
    public static final int UNIT = 5;

    /**
     * Dialogin reunamarginaalit (15px).
     * Käytetään modernien dialogien pääreunoilla.
     */
    public static final int DIALOG_PADDING = 15;

    /**
     * Pieni padding (5px).
     * Käytetään paneelien sisällä ja tiiviissä layouteissa.
     */
    public static final int SMALL_PADDING = 5;

    /**
     * Keskisuuri padding (10px).
     * Käytetään komponenttien väleissä.
     */
    public static final int MEDIUM_PADDING = 10;

    /**
     * Suuri padding (20px).
     * Käytetään eri osioiden väleissä.
     */
    public static final int LARGE_PADDING = 20;

    /**
     * Komponenttien välinen tila (10px).
     * Standardi etäisyys kahden komponentin välillä.
     */
    public static final int COMPONENT_SPACING = 10;

    /**
     * Osioiden välinen tila (20px).
     * Suurempi tila erottamaan loogisia osioita toisistaan.
     */
    public static final int SECTION_SPACING = 20;

    /**
     * Pieni tila komponenttien välissä (3px).
     * Käytetään kun komponentit ovat loogisesti lähekkäin.
     */
    public static final int TIGHT_SPACING = 3;

    /**
     * Työkalupalkin padding (8px).
     * Käytetään toolbareissa ja tiiviissä kontrolleissa.
     */
    public static final int TOOLBAR_PADDING = 8;

    // ========== INSETS CONSTANTS ==========

    /**
     * Ei marginaaleja (0, 0, 0, 0).
     */
    public static final Insets NO_INSETS = new Insets(0, 0, 0, 0);

    /**
     * Oletusmarginaalit GridBagLayout:lle (5, 5, 5, 5).
     * Yhtenäinen 5px marginaali kaikilla reunoilla.
     */
    public static final Insets DEFAULT_INSETS = new Insets(
        UNIT, UNIT, UNIT, UNIT
    );

    /**
     * Tiiviit marginaalit (3, 5, 3, 5).
     * Käytetään kun komponentit ovat lähekkäin.
     */
    public static final Insets TIGHT_INSETS = new Insets(
        TIGHT_SPACING, UNIT, TIGHT_SPACING, UNIT
    );

    /**
     * Komponenttien väliset marginaalit (5, 5, 5, 5).
     * Standardi marginaalit dialogien sisällä.
     */
    public static final Insets COMPONENT_INSETS = new Insets(
        UNIT, UNIT, UNIT, UNIT
    );

    /**
     * Osioiden väliset marginaalit (10, 5, 5, 5).
     * Ylempänä suurempi tila erottamaan osioita.
     */
    public static final Insets SECTION_INSETS = new Insets(
        MEDIUM_PADDING, UNIT, UNIT, UNIT
    );

    /**
     * Työkalupalkin marginaalit (4, 8, 4, 8).
     * Tiivistä mutta luettavaa.
     */
    public static final Insets TOOLBAR_INSETS = new Insets(
        4, TOOLBAR_PADDING, 4, TOOLBAR_PADDING
    );

    /**
     * Painikkeen sisämarginaalit (3, 10, 3, 10).
     * Tekee painikkeista sopivan kokoiset.
     */
    public static final Insets BUTTON_INSETS = new Insets(
        TIGHT_SPACING, MEDIUM_PADDING, TIGHT_SPACING, MEDIUM_PADDING
    );

    /**
     * Dialogin sisältöalueen marginaalit (8, 8, 8, 8).
     * Käytetään pääsisältöalueilla.
     */
    public static final Insets CONTENT_INSETS = new Insets(
        TOOLBAR_PADDING, TOOLBAR_PADDING, TOOLBAR_PADDING, TOOLBAR_PADDING
    );

    // ========== BORDER CONSTANTS ==========

    /**
     * Dialogin pääreunan tyhjä reunus (15, 15, 15, 15).
     * Käytetään modernien dialogien päätasolla.
     */
    public static final Border DIALOG_BORDER = BorderFactory.createEmptyBorder(
        DIALOG_PADDING, DIALOG_PADDING, DIALOG_PADDING, DIALOG_PADDING
    );

    /**
     * Dialogin yläreunan tyhjä reunus (15, 15, 5, 15).
     * Käytetään kun alareunassa on erillinen button panel.
     */
    public static final Border DIALOG_TOP_BORDER = BorderFactory.createEmptyBorder(
        DIALOG_PADDING, DIALOG_PADDING, UNIT, DIALOG_PADDING
    );

    /**
     * Paneelin reunus (5, 5, 5, 5).
     * Standardi reunus paneeleille.
     */
    public static final Border PANEL_BORDER = BorderFactory.createEmptyBorder(
        UNIT, UNIT, UNIT, UNIT
    );

    /**
     * Button panelin reunus (5, 15, 15, 15).
     * Käytetään dialogien alapuolella olevissa painikepaneeleissa.
     */
    public static final Border BUTTON_PANEL_BORDER = BorderFactory.createEmptyBorder(
        UNIT, DIALOG_PADDING, DIALOG_PADDING, DIALOG_PADDING
    );

    /**
     * Sisältöpaneelin reunus (5, 8, 5, 8).
     * Käytetään scroll-näkymissä ja sisältöalueilla.
     */
    public static final Border CONTENT_BORDER = BorderFactory.createEmptyBorder(
        UNIT, TOOLBAR_PADDING, UNIT, TOOLBAR_PADDING
    );

    /**
     * Pieni reunus (5, 10, 5, 10).
     * Käytetään kompakteissa komponenteissa.
     */
    public static final Border SMALL_BORDER = BorderFactory.createEmptyBorder(
        UNIT, MEDIUM_PADDING, UNIT, MEDIUM_PADDING
    );

    /**
     * Työkalupalkin reunus (4, 8, 4, 8).
     * Käytetään toolbareissa.
     */
    public static final Border TOOLBAR_BORDER = BorderFactory.createEmptyBorder(
        4, TOOLBAR_PADDING, 4, TOOLBAR_PADDING
    );

    /**
     * Splash screen reunus (30, 40, 20, 40).
     * Käytetään aloitusnäytössä.
     */
    public static final Border SPLASH_BORDER = BorderFactory.createEmptyBorder(
        30, 40, 20, 40
    );

    /**
     * Otsikon reunus (10, 10, 5, 10).
     * Käytetään dialogien otsikkokentissä.
     */
    public static final Border TITLE_BORDER = BorderFactory.createEmptyBorder(
        MEDIUM_PADDING, MEDIUM_PADDING, UNIT, MEDIUM_PADDING
    );

    // ========== COMPONENT SIZES ==========

    /**
     * Standardi painikkeen koko (100, 30).
     */
    public static final Dimension BUTTON_SIZE = new Dimension(100, 30);

    /**
     * Pieni painike (80, 25).
     */
    public static final Dimension SMALL_BUTTON_SIZE = new Dimension(80, 25);

    /**
     * Suuri painike (120, 35).
     */
    public static final Dimension LARGE_BUTTON_SIZE = new Dimension(120, 35);

    /**
     * Lyhyt tekstikenttä (100px leveys).
     */
    public static final int SHORT_TEXT_FIELD_WIDTH = 100;

    /**
     * Keskipitkä tekstikenttä (200px leveys).
     */
    public static final int MEDIUM_TEXT_FIELD_WIDTH = 200;

    /**
     * Pitkä tekstikenttä (300px leveys).
     */
    public static final int LONG_TEXT_FIELD_WIDTH = 300;

    /**
     * Spinerin leveys (80px).
     */
    public static final int SPINNER_WIDTH = 80;

    /**
     * Pieni ikoni koko (16x16).
     */
    public static final Dimension SMALL_ICON_SIZE = new Dimension(16, 16);

    /**
     * Keskikokoinen ikoni (24x24).
     */
    public static final Dimension MEDIUM_ICON_SIZE = new Dimension(24, 24);

    /**
     * Suuri ikoni (32x32).
     */
    public static final Dimension LARGE_ICON_SIZE = new Dimension(32, 32);

    // ========== DIALOG SIZES ==========

    /**
     * Pieni dialogi (400x300).
     */
    public static final Dimension SMALL_DIALOG_SIZE = new Dimension(400, 300);

    /**
     * Keskikokoinen dialogi (600x400).
     */
    public static final Dimension MEDIUM_DIALOG_SIZE = new Dimension(600, 400);

    /**
     * Suuri dialogi (800x600).
     */
    public static final Dimension LARGE_DIALOG_SIZE = new Dimension(800, 600);

    // ========== UTILITY METHODS ==========

    /**
     * Luo custom insets-objektin.
     *
     * @param top yläreunan marginaali
     * @param left vasemman reunan marginaali
     * @param bottom alareunan marginaali
     * @param right oikean reunan marginaali
     * @return uusi Insets-objekti
     */
    public static Insets createInsets(int top, int left, int bottom, int right) {
        return new Insets(top, left, bottom, right);
    }

    /**
     * Luo yhtenäiset insets kaikille reunoille.
     *
     * @param all marginaali kaikille reunoille
     * @return uusi Insets-objekti
     */
    public static Insets createInsets(int all) {
        return new Insets(all, all, all, all);
    }

    /**
     * Luo tyhjä reunus annetuilla arvoilla.
     *
     * @param top yläreunan padding
     * @param left vasemman reunan padding
     * @param bottom alareunan padding
     * @param right oikean reunan padding
     * @return uusi Border-objekti
     */
    public static Border createBorder(int top, int left, int bottom, int right) {
        return BorderFactory.createEmptyBorder(top, left, bottom, right);
    }

    /**
     * Luo yhtenäinen tyhjä reunus kaikille reunoille.
     *
     * @param all padding kaikille reunoille
     * @return uusi Border-objekti
     */
    public static Border createBorder(int all) {
        return BorderFactory.createEmptyBorder(all, all, all, all);
    }

    /**
     * Palauttaa spacing-arvon perustuen kerrannaiseen.
     * Esim. getSpacing(2) = 10px (2 * UNIT)
     *
     * @param multiplier kerroin perusyksikölle
     * @return spacing pixels
     */
    public static int getSpacing(int multiplier) {
        return UNIT * multiplier;
    }

    // ========== THEME-AWARE COLORS ==========
    
    /**
     * Palauttaa teeman mukaisen "success" värin (vihreä).
     * Käytetään onnistuneissa operaatioissa ja autobackup-indikaattorissa.
     * 
     * @return success-väri teemasta tai fallback
     */
    public static java.awt.Color getSuccessColor() {
        java.awt.Color color = javax.swing.UIManager.getColor("Actions.Green");
        if (color == null) {
            color = javax.swing.UIManager.getColor("Component.accentColor");
        }
        if (color == null) {
            color = new java.awt.Color(0, 128, 0); // Dark green fallback
        }
        return color;
    }
    
    /**
     * Palauttaa teeman mukaisen "info" värin (sininen).
     * Käytetään informatiivisissa indikaattoreissa.
     * 
     * @return info-väri teemasta tai fallback
     */
    public static java.awt.Color getInfoColor() {
        java.awt.Color color = javax.swing.UIManager.getColor("Actions.Blue");
        if (color == null) {
            color = javax.swing.UIManager.getColor("Component.linkColor");
        }
        if (color == null) {
            color = new java.awt.Color(0, 100, 200); // Blue fallback
        }
        return color;
    }
    
    /**
     * Palauttaa teeman mukaisen "error" värin (punainen).
     * Käytetään virheiden ja epätasapainon korostamiseen.
     * 
     * @return error-väri teemasta tai fallback
     */
    public static java.awt.Color getErrorColor() {
        java.awt.Color color = javax.swing.UIManager.getColor("Actions.Red");
        if (color == null) {
            color = javax.swing.UIManager.getColor("Component.errorFocusColor");
        }
        if (color == null) {
            color = java.awt.Color.RED; // Red fallback
        }
        return color;
    }
    
    /**
     * Palauttaa teeman mukaisen "warning" värin (oranssi/keltainen).
     * Käytetään varoituksissa.
     * 
     * @return warning-väri teemasta tai fallback
     */
    public static java.awt.Color getWarningColor() {
        java.awt.Color color = javax.swing.UIManager.getColor("Actions.Yellow");
        if (color == null) {
            color = javax.swing.UIManager.getColor("Component.warningFocusColor");
        }
        if (color == null) {
            color = new java.awt.Color(200, 150, 0); // Orange fallback
        }
        return color;
    }
    
    /**
     * Palauttaa teeman mukaisen "muted/disabled" värin (harmaa).
     * Käytetään deaktivoiduissa elementeissä.
     * 
     * @return muted-väri teemasta tai fallback
     */
    public static java.awt.Color getMutedColor() {
        java.awt.Color color = javax.swing.UIManager.getColor("Label.disabledForeground");
        if (color == null) {
            color = java.awt.Color.GRAY;
        }
        return color;
    }
    
    /**
     * Palauttaa teeman mukaisen taustavärin.
     * Käytetään paneelien ja dialogien taustoissa.
     * 
     * @return taustaväri teemasta tai fallback
     */
    public static java.awt.Color getBackgroundColor() {
        java.awt.Color color = javax.swing.UIManager.getColor("Panel.background");
        if (color == null) {
            color = javax.swing.UIManager.getColor("control");
        }
        if (color == null) {
            color = java.awt.Color.WHITE; // Light mode fallback
        }
        return color;
    }
    
    /**
     * Palauttaa teeman mukaisen tekstivärin.
     * Käytetään labelien ja tekstikenttien tekstissä.
     * 
     * @return tekstiväri teemasta tai fallback
     */
    public static java.awt.Color getForegroundColor() {
        java.awt.Color color = javax.swing.UIManager.getColor("Label.foreground");
        if (color == null) {
            color = javax.swing.UIManager.getColor("textText");
        }
        if (color == null) {
            color = java.awt.Color.BLACK; // Light mode fallback
        }
        return color;
    }
    
    /**
     * Palauttaa teeman mukaisen reunusvärin.
     * Käytetään komponenttien reunoissa.
     * 
     * @return reunusväri teemasta tai fallback
     */
    public static java.awt.Color getBorderColor() {
        java.awt.Color color = javax.swing.UIManager.getColor("Border.color");
        if (color == null) {
            color = javax.swing.UIManager.getColor("controlShadow");
        }
        if (color == null) {
            color = new java.awt.Color(128, 128, 128); // Gray fallback
        }
        return color;
    }
    
    /**
     * Palauttaa teeman mukaisen tekstikentän taustavärin.
     * Käytetään JTextField, JTextArea jne. taustoissa.
     * 
     * @return tekstikentän taustaväri teemasta tai fallback
     */
    public static java.awt.Color getTextFieldBackgroundColor() {
        java.awt.Color color = javax.swing.UIManager.getColor("TextField.background");
        if (color == null) {
            color = javax.swing.UIManager.getColor("text");
        }
        if (color == null) {
            color = java.awt.Color.WHITE; // Light mode fallback
        }
        return color;
    }
    
    /**
     * Palauttaa teeman mukaisen tekstikentän tekstivärin.
     * Käytetään tekstikenttien tekstissä.
     * 
     * @return tekstikentän tekstiväri teemasta tai fallback
     */
    public static java.awt.Color getTextFieldForegroundColor() {
        java.awt.Color color = javax.swing.UIManager.getColor("TextField.foreground");
        if (color == null) {
            color = javax.swing.UIManager.getColor("textText");
        }
        if (color == null) {
            color = java.awt.Color.BLACK; // Light mode fallback
        }
        return color;
    }
    
    /**
     * Palauttaa teeman mukaisen taulukon taustavärin.
     * Käytetään JTable taustoissa.
     * 
     * @return taulukon taustaväri teemasta tai fallback
     */
    public static java.awt.Color getTableBackgroundColor() {
        java.awt.Color color = javax.swing.UIManager.getColor("Table.background");
        if (color == null) {
            color = getBackgroundColor();
        }
        return color;
    }
    
    /**
     * Palauttaa teeman mukaisen taulukon tekstivärin.
     * Käytetään taulukon tekstissä.
     * 
     * @return taulukon tekstiväri teemasta tai fallback
     */
    public static java.awt.Color getTableForegroundColor() {
        java.awt.Color color = javax.swing.UIManager.getColor("Table.foreground");
        if (color == null) {
            color = getForegroundColor();
        }
        return color;
    }
}
