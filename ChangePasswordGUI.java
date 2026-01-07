import javax.swing.*;
import java.awt.*;
import java.sql.*;
import lib.DatabaseUtil;

public class ChangePasswordGUI extends JDialog {
    
    private String currentUsername;
    private JPasswordField currentPasswordField;
    private JPasswordField newPasswordField;
    private JPasswordField confirmPasswordField;
    
    public ChangePasswordGUI(JFrame parent, String username) {
        super(parent, "Change Password", true);
        this.currentUsername = username;
        
        setSize(500, 450);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());
        
        // Header
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(39, 60, 117));
        headerPanel.setPreferredSize(new Dimension(0, 80));
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        
        JLabel titleLabel = new JLabel("🔑 Change Password");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel userLabel = new JLabel("User: " + username);
        userLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        userLabel.setForeground(new Color(200, 200, 200));
        userLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        headerPanel.add(Box.createVerticalGlue());
        headerPanel.add(titleLabel);
        headerPanel.add(Box.createVerticalStrut(5));
        headerPanel.add(userLabel);
        headerPanel.add(Box.createVerticalGlue());
        
        // Form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 10, 10, 10);
        
        currentPasswordField = new JPasswordField(20);
        newPasswordField = new JPasswordField(20);
        confirmPasswordField = new JPasswordField(20);
        
        Font fieldFont = new Font("Segoe UI", Font.PLAIN, 14);
        currentPasswordField.setFont(fieldFont);
        newPasswordField.setFont(fieldFont);
        confirmPasswordField.setFont(fieldFont);
        
        // Make password fields taller
        Dimension fieldSize = new Dimension(250, 35);
        currentPasswordField.setPreferredSize(fieldSize);
        newPasswordField.setPreferredSize(fieldSize);
        confirmPasswordField.setPreferredSize(fieldSize);
        
        int row = 0;
        
        // Current password
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.4;
        JLabel currentLabel = new JLabel("Current Password:");
        currentLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        formPanel.add(currentLabel, gbc);
        
        gbc.gridx = 1; gbc.weightx = 0.6;
        formPanel.add(currentPasswordField, gbc);
        
        row++;
        
        // New password
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.4;
        JLabel newLabel = new JLabel("New Password:");
        newLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        formPanel.add(newLabel, gbc);
        
        gbc.gridx = 1; gbc.weightx = 0.6;
        formPanel.add(newPasswordField, gbc);
        
        row++;
        
        // Confirm password
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.4;
        JLabel confirmLabel = new JLabel("Confirm Password:");
        confirmLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        formPanel.add(confirmLabel, gbc);
        
        gbc.gridx = 1; gbc.weightx = 0.6;
        formPanel.add(confirmPasswordField, gbc);
        
        row++;
        
        // Password requirements
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        JPanel requirementsPanel = new JPanel();
        requirementsPanel.setLayout(new BoxLayout(requirementsPanel, BoxLayout.Y_AXIS));
        requirementsPanel.setBackground(new Color(236, 240, 241));
        requirementsPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(189, 195, 199), 1),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        
        JLabel reqTitle = new JLabel("Password Requirements:");
        reqTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        reqTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel req1 = new JLabel("• Minimum 6 characters");
        JLabel req2 = new JLabel("• Must not match current password");
        JLabel req3 = new JLabel("• Confirmation must match");
        
        req1.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        req2.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        req3.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        
        req1.setAlignmentX(Component.LEFT_ALIGNMENT);
        req2.setAlignmentX(Component.LEFT_ALIGNMENT);
        req3.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        requirementsPanel.add(reqTitle);
        requirementsPanel.add(Box.createVerticalStrut(5));
        requirementsPanel.add(req1);
        requirementsPanel.add(req2);
        requirementsPanel.add(req3);
        
        formPanel.add(requirementsPanel, gbc);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 20));
        buttonPanel.setBackground(Color.WHITE);
        
        JButton changeButton = createStyledButton("🔐 Change Password", new Color(46, 204, 113));
        JButton cancelButton = createStyledButton("❌ Cancel", new Color(231, 76, 60));
        
        changeButton.addActionListener(e -> changePassword());
        cancelButton.addActionListener(e -> dispose());
        
        buttonPanel.add(changeButton);
        buttonPanel.add(cancelButton);
        
        // Main content panel
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(Color.WHITE);
        contentPanel.add(formPanel, BorderLayout.CENTER);
        contentPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        add(headerPanel, BorderLayout.NORTH);
        add(contentPanel, BorderLayout.CENTER);
    }
    
    private JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(180, 40));
        return button;
    }
    
    private void changePassword() {
        String currentPassword = new String(currentPasswordField.getPassword()).trim();
        String newPassword = new String(newPasswordField.getPassword()).trim();
        String confirmPassword = new String(confirmPasswordField.getPassword()).trim();
        
        // Validation
        if (currentPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Please fill all fields!",
                "Validation Error",
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        if (newPassword.length() < 6) {
            JOptionPane.showMessageDialog(this,
                "New password must be at least 6 characters long!",
                "Validation Error",
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        if (!newPassword.equals(confirmPassword)) {
            JOptionPane.showMessageDialog(this,
                "New passwords do not match!",
                "Validation Error",
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        if (currentPassword.equals(newPassword)) {
            JOptionPane.showMessageDialog(this,
                "New password must be different from current password!",
                "Validation Error",
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DatabaseUtil.getInstance().getConnection();
            
            // Verify current password
            String verifyQuery = "SELECT password FROM users WHERE username = ?";
            PreparedStatement verifyStmt = conn.prepareStatement(verifyQuery);
            verifyStmt.setString(1, currentUsername);
            ResultSet rs = verifyStmt.executeQuery();
            
            if (rs.next()) {
                String storedPassword = rs.getString("password");
                
                if (!currentPassword.equals(storedPassword)) {
                    JOptionPane.showMessageDialog(this,
                        "Current password is incorrect!",
                        "Authentication Error",
                        JOptionPane.ERROR_MESSAGE);
                    rs.close();
                    verifyStmt.close();
                    conn.close();
                    
                    // Clear current password field
                    currentPasswordField.setText("");
                    currentPasswordField.requestFocus();
                    return;
                }
            } else {
                JOptionPane.showMessageDialog(this,
                    "User not found!",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
                rs.close();
                verifyStmt.close();
                conn.close();
                return;
            }
            
            rs.close();
            verifyStmt.close();
            
            // Update password
            String updateQuery = "UPDATE users SET password = ? WHERE username = ?";
            PreparedStatement updateStmt = conn.prepareStatement(updateQuery);
            updateStmt.setString(1, newPassword); // In production, hash this!
            updateStmt.setString(2, currentUsername);
            
            int rows = updateStmt.executeUpdate();
            
            updateStmt.close();
            conn.close();
            
            if (rows > 0) {
                JOptionPane.showMessageDialog(this,
                    "Password changed successfully!\n\nPlease remember your new password.",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);
                
                // Clear all fields
                currentPasswordField.setText("");
                newPasswordField.setText("");
                confirmPasswordField.setText("");
                
                dispose();
            } else {
                JOptionPane.showMessageDialog(this,
                    "Failed to change password. Please try again.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                "Error: " + e.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
}
