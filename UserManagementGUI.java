import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import lib.DatabaseUtil;

public class UserManagementGUI extends JDialog {
    
    private JTable userTable;
    private DefaultTableModel tableModel;
    private JFrame parent;
    
    public UserManagementGUI(JFrame parent) {
        super(parent, "User Management", true);
        this.parent = parent;
        
        setSize(1000, 600);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));
        
        // Header
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(39, 60, 117));
        headerPanel.setPreferredSize(new Dimension(0, 60));
        JLabel titleLabel = new JLabel("⚙️ User Management (Admin Only)");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        buttonPanel.setBackground(Color.WHITE);
        
        JButton addButton = createStyledButton("➕ Add User", new Color(46, 204, 113));
        JButton refreshButton = createStyledButton("🔄 Refresh", new Color(52, 152, 219));
        JButton viewButton = createStyledButton("👁️ View Details", new Color(155, 89, 182));
        JButton deleteButton = createStyledButton("🗑️ Delete User", new Color(231, 76, 60));
        
        addButton.addActionListener(e -> showAddUserDialog());
        refreshButton.addActionListener(e -> loadUsers());
        viewButton.addActionListener(e -> viewUserDetails());
        deleteButton.addActionListener(e -> deleteUser());
        
        buttonPanel.add(addButton);
        buttonPanel.add(refreshButton);
        buttonPanel.add(viewButton);
        buttonPanel.add(deleteButton);
        
        // Table
        String[] columns = {"User ID", "Username", "Full Name", "Role", "Created At"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        userTable = new JTable(tableModel);
        userTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        userTable.setRowHeight(30);
        userTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        userTable.getTableHeader().setBackground(new Color(52, 73, 94));
        userTable.getTableHeader().setForeground(Color.WHITE);
        userTable.setSelectionBackground(new Color(52, 152, 219));
        userTable.setSelectionForeground(Color.WHITE);
        
        JScrollPane scrollPane = new JScrollPane(userTable);
        
        // Main panel
        JPanel mainPanel = new JPanel(new BorderLayout(0, 10));
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        mainPanel.add(buttonPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        
        add(headerPanel, BorderLayout.NORTH);
        add(mainPanel, BorderLayout.CENTER);
        
        loadUsers();
    }
    
    private JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(150, 35));
        return button;
    }
    
    private void loadUsers() {
        tableModel.setRowCount(0);
        
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DatabaseUtil.getInstance().getConnection();
            
            String query = "SELECT user_id, username, full_name, role, created_at FROM users ORDER BY role, full_name";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            
            while (rs.next()) {
                Object[] row = {
                    rs.getInt("user_id"),
                    rs.getString("username"),
                    rs.getString("full_name"),
                    rs.getString("role").toUpperCase(),
                    rs.getString("created_at")
                };
                tableModel.addRow(row);
            }
            
            rs.close();
            stmt.close();
            conn.close();
            
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                "Error loading users: " + e.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void showAddUserDialog() {
        JDialog dialog = new JDialog(this, "Add New User", true);
        dialog.setSize(550, 450);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());
        
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 8, 8, 8);
        
        JTextField fullNameField = new JTextField(20);
        JTextField usernameField = new JTextField(20);
        JPasswordField passwordField = new JPasswordField(20);
        JPasswordField confirmPasswordField = new JPasswordField(20);
        JComboBox<String> roleBox = new JComboBox<>(new String[]{"Admin", "Staff", "Teacher"});
        
        Font fieldFont = new Font("Segoe UI", Font.PLAIN, 14);
        fullNameField.setFont(fieldFont);
        usernameField.setFont(fieldFont);
        passwordField.setFont(fieldFont);
        confirmPasswordField.setFont(fieldFont);
        roleBox.setFont(fieldFont);
        
        int row = 0;
        addFormField(formPanel, gbc, row++, "Full Name:", fullNameField);
        addFormField(formPanel, gbc, row++, "Username:", usernameField);
        addFormField(formPanel, gbc, row++, "Password:", passwordField);
        addFormField(formPanel, gbc, row++, "Confirm Password:", confirmPasswordField);
        addFormField(formPanel, gbc, row++, "Role:", roleBox);
        
        // Info label
        gbc.gridx = 0;
        gbc.gridy = row++;
        gbc.gridwidth = 2;
        JLabel infoLabel = new JLabel("<html><i>Note: Password should be at least 6 characters</i></html>");
        infoLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        infoLabel.setForeground(new Color(100, 100, 100));
        formPanel.add(infoLabel, gbc);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 15));
        buttonPanel.setBackground(Color.WHITE);
        
        JButton saveButton = createStyledButton("💾 Save", new Color(46, 204, 113));
        JButton cancelButton = createStyledButton("❌ Cancel", new Color(231, 76, 60));
        
        saveButton.addActionListener(e -> {
            String fullName = fullNameField.getText().trim();
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword()).trim();
            String confirmPassword = new String(confirmPasswordField.getPassword()).trim();
            String role = ((String) roleBox.getSelectedItem()).toLowerCase();
            
            // Validation
            if (fullName.isEmpty() || username.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Please fill all fields!", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            if (!password.equals(confirmPassword)) {
                JOptionPane.showMessageDialog(dialog, "Passwords do not match!", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            if (password.length() < 6) {
                JOptionPane.showMessageDialog(dialog, "Password must be at least 6 characters!", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                Connection conn = DatabaseUtil.getInstance().getConnection();
                
                // Check if username exists
                String checkQuery = "SELECT username FROM users WHERE username = ?";
                PreparedStatement checkStmt = conn.prepareStatement(checkQuery);
                checkStmt.setString(1, username);
                ResultSet checkRs = checkStmt.executeQuery();
                
                if (checkRs.next()) {
                    JOptionPane.showMessageDialog(dialog, 
                        "Username already exists! Please choose a different username.", 
                        "Duplicate Username", 
                        JOptionPane.ERROR_MESSAGE);
                    checkRs.close();
                    checkStmt.close();
                    conn.close();
                    return;
                }
                checkRs.close();
                checkStmt.close();
                
                // Insert new user
                String sql = "INSERT INTO users (username, password, role, full_name) VALUES (?, ?, ?, ?)";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setString(1, username);
                stmt.setString(2, password); // In production, hash this!
                stmt.setString(3, role);
                stmt.setString(4, fullName);
                
                int rows = stmt.executeUpdate();
                
                stmt.close();
                conn.close();
                
                if (rows > 0) {
                    String message = String.format(
                        "User created successfully!\n\n" +
                        "Username: %s\n" +
                        "Full Name: %s\n" +
                        "Role: %s\n\n" +
                        "The user can now login with these credentials.",
                        username, fullName, role.toUpperCase()
                    );
                    
                    JOptionPane.showMessageDialog(dialog, message, "Success", JOptionPane.INFORMATION_MESSAGE);
                    dialog.dispose();
                    loadUsers();
                }
                
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(dialog, "Error: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        cancelButton.addActionListener(e -> dialog.dispose());
        
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        
        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.setVisible(true);
    }
    
    private void addFormField(JPanel panel, GridBagConstraints gbc, int row, String label, JComponent field) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.3;
        gbc.gridwidth = 1;
        JLabel jLabel = new JLabel(label);
        jLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        panel.add(jLabel, gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        panel.add(field, gbc);
    }
    
    private void viewUserDetails() {
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a user to view!", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int userId = (int) tableModel.getValueAt(selectedRow, 0);
        String username = (String) tableModel.getValueAt(selectedRow, 1);
        String fullName = (String) tableModel.getValueAt(selectedRow, 2);
        String role = (String) tableModel.getValueAt(selectedRow, 3);
        String createdAt = (String) tableModel.getValueAt(selectedRow, 4);
        
        String details = String.format(
            "<html><body style='width: 350px; padding: 10px;'>" +
            "<h2 style='color: #2c3e50;'>User Details</h2>" +
            "<table style='width: 100%%;'>" +
            "<tr><td><b>User ID:</b></td><td>%d</td></tr>" +
            "<tr><td><b>Username:</b></td><td>%s</td></tr>" +
            "<tr><td><b>Full Name:</b></td><td>%s</td></tr>" +
            "<tr><td><b>Role:</b></td><td>%s</td></tr>" +
            "<tr><td><b>Created:</b></td><td>%s</td></tr>" +
            "</table>" +
            "<br><p style='color: #7f8c8d;'><i>Tip: Users can change their own passwords from the main menu</i></p>" +
            "</body></html>",
            userId, username, fullName, role, createdAt
        );
        
        JOptionPane.showMessageDialog(this, details, "User Details", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void deleteUser() {
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a user to delete!", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int userId = (int) tableModel.getValueAt(selectedRow, 0);
        String username = (String) tableModel.getValueAt(selectedRow, 1);
        String fullName = (String) tableModel.getValueAt(selectedRow, 2);
        
        // Prevent deleting the last admin
        String role = (String) tableModel.getValueAt(selectedRow, 3);
        if (role.equalsIgnoreCase("ADMIN")) {
            try {
                Connection conn = DatabaseUtil.getInstance().getConnection();
                String countQuery = "SELECT COUNT(*) as admin_count FROM users WHERE role = 'admin'";
                ResultSet rs = conn.createStatement().executeQuery(countQuery);
                
                if (rs.next() && rs.getInt("admin_count") <= 1) {
                    JOptionPane.showMessageDialog(this,
                        "Cannot delete the last admin user!\n\nAt least one admin must exist in the system.",
                        "Cannot Delete",
                        JOptionPane.ERROR_MESSAGE);
                    rs.close();
                    conn.close();
                    return;
                }
                rs.close();
                conn.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        int confirm = JOptionPane.showConfirmDialog(this,
            String.format("Are you sure you want to delete this user?\n\nUsername: %s\nFull Name: %s\n\nThis action cannot be undone!",
                username, fullName),
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                Connection conn = DatabaseUtil.getInstance().getConnection();
                
                String sql = "DELETE FROM users WHERE user_id = ?";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setInt(1, userId);
                
                int rows = stmt.executeUpdate();
                
                stmt.close();
                conn.close();
                
                if (rows > 0) {
                    JOptionPane.showMessageDialog(this, 
                        "User deleted successfully!", 
                        "Success", 
                        JOptionPane.INFORMATION_MESSAGE);
                    loadUsers();
                }
                
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, 
                    "Error deleting user: " + e.getMessage(), 
                    "Database Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}