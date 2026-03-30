import javax.swing.*;
import java.awt.*;

/**
 * Bark Bites - A simple Swing application
 * Displays a centered label with professional styling
 */
public class BarkBitesApp extends JFrame {
    
    public BarkBitesApp() {
        // Set window properties
        setTitle("Bark Bites");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 300);
        setLocationRelativeTo(null); // Center on screen
        
        // Create main panel with GridBagLayout
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(new Color(211, 211, 211)); // Light Gray
        
        // Create label
        JLabel label = new JLabel("Bark Bites: In Construction");
        label.setFont(new Font("Arial", Font.BOLD, 18));
        label.setForeground(Color.BLACK);
        
        // Add label to panel (GridBagLayout automatically centers it)
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        mainPanel.add(label, constraints);
        
        // Add panel to frame
        add(mainPanel);
        
        // Make frame visible
        setVisible(true);
    }
    
    public static void main(String[] args) {
        // Run on Event Dispatch Thread for thread safety
        SwingUtilities.invokeLater(() -> new BarkBitesApp());
    }
}
