package kirjanpito.ui;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;

import kirjanpito.ui.SwingExtensionsKt;
import kirjanpito.ui.ValidationUtils;

/**
 * Demo class showing Kotlin utilities working from Java code
 */
public class KotlinDemo {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Kotlin Integration Demo - Tilitin 2.1");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(500, 400);

            // Create panel using Kotlin extension functions
            JPanel panel = new JPanel(new GridBagLayout());
            panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

            // Add title using Kotlin's gridBagConstraints helper
            JLabel title = new JLabel("Kotlin Modernization Demo");
            title.setFont(new Font("Arial", Font.BOLD, 18));
            panel.add(title, SwingExtensionsKt.gridBagConstraints(
                0, 0, 2, 1,
                1.0, 0.0,
                GridBagConstraints.CENTER,
                GridBagConstraints.HORIZONTAL,
                SwingExtensionsKt.insets(0, 0, 20, 0),
                0, 0
            ));

            // Add description
            JTextArea description = new JTextArea(
                "Tämä demo näyttää että Kotlin toimii täydellisesti Java-koodin kanssa!\n\n" +
                "Käytössä on:\n" +
                "• SwingExtensions.kt - GridBagConstraints helpottimet\n" +
                "• ValidationUtils.kt - Null-safe validointi\n" +
                "• DialogUtils.kt - Dialog-apuvälineet\n\n" +
                "Kokeile validointia syöttämällä numero alle:"
            );
            description.setEditable(false);
            description.setLineWrap(true);
            description.setWrapStyleWord(true);
            description.setBackground(panel.getBackground());
            panel.add(description, SwingExtensionsKt.gridBagConstraints(
                0, 1, 2, 1,
                1.0, 0.0,
                GridBagConstraints.WEST,
                GridBagConstraints.BOTH,
                SwingExtensionsKt.insets(10),
                0, 0
            ));

            // Input field
            JLabel inputLabel = new JLabel("Syötä numero:");
            panel.add(inputLabel, SwingExtensionsKt.gridBagConstraints(
                0, 2, 1, 1,
                0.0, 0.0,
                GridBagConstraints.WEST,
                GridBagConstraints.NONE,
                SwingExtensionsKt.insets(10, 0, 5, 10),
                0, 0
            ));

            JTextField inputField = new JTextField();
            panel.add(inputField, SwingExtensionsKt.gridBagConstraints(
                1, 2, 1, 1,
                1.0, 0.0,
                GridBagConstraints.WEST,
                GridBagConstraints.HORIZONTAL,
                SwingExtensionsKt.insets(10, 0, 5, 0),
                0, 0
            ));

            // Result label
            JLabel resultLabel = new JLabel(" ");
            resultLabel.setFont(new Font("Arial", Font.PLAIN, 12));
            panel.add(resultLabel, SwingExtensionsKt.gridBagConstraints(
                0, 3, 2, 1,
                1.0, 0.0,
                GridBagConstraints.WEST,
                GridBagConstraints.HORIZONTAL,
                SwingExtensionsKt.insets(5, 0, 10, 0),
                0, 0
            ));

            // Create validate button
            JButton validateButton = new JButton("Validoi numero");
            validateButton.addActionListener(e -> {
                String input = inputField.getText();

                // Use Kotlin validation utilities
                if (ValidationUtils.INSTANCE.isValidNumber(input)) {
                    BigDecimal number = ValidationUtils.INSTANCE.toBigDecimalOrNull(input);

                    if (ValidationUtils.INSTANCE.isPositive(number)) {
                        resultLabel.setText("✓ Numero " + number + " on positiivinen!");
                        resultLabel.setForeground(new Color(0, 128, 0));
                    } else if (ValidationUtils.INSTANCE.isNonNegative(number)) {
                        resultLabel.setText("✓ Numero " + number + " on nolla tai positiivinen");
                        resultLabel.setForeground(new Color(0, 100, 200));
                    } else {
                        resultLabel.setText("⚠ Numero " + number + " on negatiivinen");
                        resultLabel.setForeground(new Color(200, 100, 0));
                    }
                } else {
                    resultLabel.setText("✗ Virheellinen numero!");
                    resultLabel.setForeground(Color.RED);
                }
            });

            panel.add(validateButton, SwingExtensionsKt.gridBagConstraints(
                0, 4, 1, 1,
                0.5, 0.0,
                GridBagConstraints.CENTER,
                GridBagConstraints.HORIZONTAL,
                SwingExtensionsKt.insets(10, 0, 0, 5),
                0, 0
            ));

            // Dialog button using Kotlin's showInfo extension
            JButton dialogButton = new JButton("Näytä info-dialogi");
            dialogButton.addActionListener(e -> {
                SwingExtensionsKt.showInfo(
                    panel,
                    "Tämä dialogi näytetään Kotlin-laajennusfunktion avulla!\n\n" +
                    "Kotlin-koodi toimii täydellisesti Java-koodin kanssa.",
                    "Kotlin Works!"
                );
            });

            panel.add(dialogButton, SwingExtensionsKt.gridBagConstraints(
                1, 4, 1, 1,
                0.5, 0.0,
                GridBagConstraints.CENTER,
                GridBagConstraints.HORIZONTAL,
                SwingExtensionsKt.insets(10, 5, 0, 0),
                0, 0
            ));

            // Spacer
            panel.add(Box.createVerticalGlue(), SwingExtensionsKt.gridBagConstraints(
                0, 5, 2, 1,
                1.0, 1.0,
                GridBagConstraints.CENTER,
                GridBagConstraints.BOTH,
                SwingExtensionsKt.insets(0),
                0, 0
            ));

            // Status label
            JLabel statusLabel = new JLabel(
                "✓ Kotlin 2.1.10 käytössä | Java 25 | Tilitin 2.1.1"
            );
            statusLabel.setFont(new Font("Arial", Font.ITALIC, 10));
            statusLabel.setForeground(Color.GRAY);
            panel.add(statusLabel, SwingExtensionsKt.gridBagConstraints(
                0, 6, 2, 1,
                1.0, 0.0,
                GridBagConstraints.CENTER,
                GridBagConstraints.HORIZONTAL,
                SwingExtensionsKt.insets(10, 0, 0, 0),
                0, 0
            ));

            frame.add(panel);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);

            System.out.println("=================================================");
            System.out.println("Kotlin Integration Demo käynnissä!");
            System.out.println("=================================================");
            System.out.println("Kotlin utilities toiminnassa:");
            System.out.println("  ✓ SwingExtensions.kt - gridBagConstraints()");
            System.out.println("  ✓ SwingExtensions.kt - button()");
            System.out.println("  ✓ SwingExtensions.kt - insets()");
            System.out.println("  ✓ SwingExtensions.kt - showInfo()");
            System.out.println("  ✓ ValidationUtils.kt - isValidNumber()");
            System.out.println("  ✓ ValidationUtils.kt - isPositive()");
            System.out.println("=================================================");
        });
    }
}
