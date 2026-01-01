package kirjanpito.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JWindow;
import javax.swing.SwingConstants;
import javax.swing.UIManager;

/**
 * Moderni käynnistysruutu (splash screen) Tilitin-sovellukselle.
 * Näyttää sovelluksen nimen, version ja latauspalkin käynnistyksen aikana.
 */
public class SplashScreen extends JWindow {
    private static final long serialVersionUID = 1L;
    
    private JProgressBar progressBar;
    private JLabel statusLabel;
    private static SplashScreen instance;
    
    /**
     * Luo uuden splash screen -ikkunan.
     */
    public SplashScreen() {
        instance = this;
        createUI();
    }
    
    /**
     * Palauttaa nykyisen splash screen -instanssin.
     */
    public static SplashScreen getInstance() {
        return instance;
    }
    
    /**
     * Luo käyttöliittymän komponentit.
     */
    private void createUI() {
        JPanel content = new GradientPanel();
        content.setLayout(new BorderLayout(10, 10));
        content.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(70, 130, 180), 2),
            BorderFactory.createEmptyBorder(30, 40, 20, 40)
        ));
        
        // Otsikko
        JLabel titleLabel = new JLabel(Kirjanpito.APP_NAME, SwingConstants.CENTER);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 32));
        titleLabel.setForeground(Color.WHITE);
        content.add(titleLabel, BorderLayout.NORTH);
        
        // Keskialue - versio ja kuvaus
        JPanel centerPanel = new JPanel();
        centerPanel.setOpaque(false);
        centerPanel.setLayout(new BorderLayout(5, 10));
        
        JLabel subtitleLabel = new JLabel("Ilmainen kirjanpito-ohjelma", SwingConstants.CENTER);
        subtitleLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        subtitleLabel.setForeground(new Color(220, 220, 220));
        centerPanel.add(subtitleLabel, BorderLayout.NORTH);
        
        String version = Kirjanpito.APP_VERSION;
        if (version == null) version = "3.0.0";
        JLabel versionLabel = new JLabel("Versio " + version, SwingConstants.CENTER);
        versionLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        versionLabel.setForeground(new Color(180, 180, 180));
        centerPanel.add(versionLabel, BorderLayout.CENTER);
        
        content.add(centerPanel, BorderLayout.CENTER);
        
        // Alaosa - edistymispalkki ja status
        JPanel bottomPanel = new JPanel();
        bottomPanel.setOpaque(false);
        bottomPanel.setLayout(new BorderLayout(5, 8));
        
        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(false);
        progressBar.setPreferredSize(new Dimension(300, 8));
        progressBar.setBorderPainted(false);
        bottomPanel.add(progressBar, BorderLayout.NORTH);
        
        statusLabel = new JLabel("Käynnistetään...", SwingConstants.CENTER);
        statusLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
        statusLabel.setForeground(new Color(200, 200, 200));
        bottomPanel.add(statusLabel, BorderLayout.SOUTH);
        
        content.add(bottomPanel, BorderLayout.SOUTH);
        
        setContentPane(content);
        setSize(400, 220);
        setLocationRelativeTo(null);
    }
    
    /**
     * Päivittää edistymispalkin arvon.
     * @param progress arvo 0-100
     */
    public void setProgress(int progress) {
        if (progressBar != null) {
            progressBar.setValue(progress);
        }
    }
    
    /**
     * Päivittää tilarivin tekstin.
     * @param status näytettävä teksti
     */
    public void setStatus(String status) {
        if (statusLabel != null) {
            statusLabel.setText(status);
        }
    }
    
    /**
     * Näyttää splash screenin.
     */
    public void showSplash() {
        setVisible(true);
    }
    
    /**
     * Piilottaa ja tuhoaa splash screenin.
     */
    public void hideSplash() {
        setVisible(false);
        dispose();
        instance = null;
    }
    
    /**
     * Paneeli, jossa on liukuvärjätty tausta.
     */
    private static class GradientPanel extends JPanel {
        private static final long serialVersionUID = 1L;
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            
            int w = getWidth();
            int h = getHeight();
            
            // Tumma sininen gradientti
            Color color1 = new Color(25, 55, 95);
            Color color2 = new Color(45, 85, 135);
            
            // Tarkista onko tumma teema käytössä
            Color bg = UIManager.getColor("Panel.background");
            if (bg != null && bg.getRed() < 100) {
                // Tumma teema - käytä tummempaa gradienttia
                color1 = new Color(30, 30, 40);
                color2 = new Color(50, 50, 70);
            }
            
            GradientPaint gp = new GradientPaint(0, 0, color1, 0, h, color2);
            g2d.setPaint(gp);
            g2d.fillRect(0, 0, w, h);
            g2d.dispose();
        }
    }
}
