import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import javax.swing.*;
import lib.DatabaseUtil;

public class LearningInstituteGUI extends JFrame {
    
    private CardLayout cardLayout;
    private JPanel mainPanel;
    private String currentUser;
    private String currentUsername;
    private String currentRole;
    private int currentUserId;
    
    public LearningInstituteGUI() {
        setTitle("Learning Institute Management System");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        
        // Add login panel
        mainPanel.add(createLoginPanel(), "LOGIN");
        
        add(mainPanel);
        
        // Show login screen
        cardLayout.show(mainPanel, "LOGIN");
    }
    
    private JPanel createLoginPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(45, 52, 54));
        
        // Header
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(39, 60, 117));
        headerPanel.setPreferredSize(new Dimension(0, 100));
        JLabel titleLabel = new JLabel("Learning Institute Management System");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel);
        
        // Center login form
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(new Color(45, 52, 54));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Login container
        JPanel loginBox = new JPanel(new GridBagLayout());
        loginBox.setBackground(Color.WHITE);
        loginBox.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
            BorderFactory.createEmptyBorder(30, 40, 30, 40)
        ));
        
        GridBagConstraints boxGbc = new GridBagConstraints();
        boxGbc.insets = new Insets(8, 8, 8, 8);
        boxGbc.fill = GridBagConstraints.HORIZONTAL;
        boxGbc.gridx = 0;
        boxGbc.gridy = 0;
        boxGbc.gridwidth = 2;
        
        JLabel loginTitle = new JLabel("Login");
        loginTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        loginTitle.setHorizontalAlignment(SwingConstants.CENTER);
        loginBox.add(loginTitle, boxGbc);
        
        boxGbc.gridwidth = 1;
        boxGbc.gridy++;
        JLabel userLabel = new JLabel("Username:");
        userLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        loginBox.add(userLabel, boxGbc);
        
        boxGbc.gridx = 1;
        JTextField userField = new JTextField(20);
        userField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        userField.setPreferredSize(new Dimension(250, 35));
        loginBox.add(userField, boxGbc);
        
        boxGbc.gridx = 0;
        boxGbc.gridy++;
        JLabel passLabel = new JLabel("Password:");
        passLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        loginBox.add(passLabel, boxGbc);
        
        boxGbc.gridx = 1;
        JPasswordField passField = new JPasswordField(20);
        passField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        passField.setPreferredSize(new Dimension(250, 35));
        loginBox.add(passField, boxGbc);
        
        boxGbc.gridx = 0;
        boxGbc.gridy++;
        boxGbc.gridwidth = 2;
        
        // Create BLUE RIBBON login button
        JButton loginButton = createRibbonButton("Login", new Color(41, 128, 185), new Color(52, 152, 219));
        
        loginButton.addActionListener(e -> {
            String username = userField.getText().trim();
            String password = new String(passField.getPassword()).trim();
            
            if (username.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                    "Please enter both username and password",
                    "Login Error",
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            if (authenticateUser(username, password)) {
                // Create dashboard based on role
                createDashboard();
                cardLayout.show(mainPanel, "DASHBOARD");
            } else {
                JOptionPane.showMessageDialog(this,
                    "Invalid username or password",
                    "Login Failed",
                    JOptionPane.ERROR_MESSAGE);
                passField.setText("");
            }
        });
        
        // Enter key support
        passField.addActionListener(e -> loginButton.doClick());
        
        loginBox.add(loginButton, boxGbc);
        
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(loginBox, gbc);
        
        panel.add(headerPanel, BorderLayout.NORTH);
        panel.add(formPanel, BorderLayout.CENTER);
        
        // Footer
        JPanel footerPanel = new JPanel();
        footerPanel.setBackground(new Color(45, 52, 54));
        JLabel footerLabel = new JLabel("© 2026 Learning Institute - Salary Calculation System (93% Formula)");
        footerLabel.setForeground(new Color(150, 150, 150));
        footerLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        footerPanel.add(footerLabel);
        panel.add(footerPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private boolean authenticateUser(String username, String password) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DatabaseUtil.getInstance().getConnection();
            
            String query = "SELECT user_id, username, password, role, full_name FROM users WHERE username = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, username);
            
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                String storedPassword = rs.getString("password");
                
                if (password.equals(storedPassword)) {
                    currentUserId = rs.getInt("user_id");
                    currentUsername = rs.getString("username");
                    currentUser = rs.getString("full_name");
                    currentRole = rs.getString("role");
                    
                    rs.close();
                    stmt.close();
                    conn.close();
                    
                    return true;
                }
            }
            
            rs.close();
            stmt.close();
            conn.close();
            
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                "Database error: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
        
        return false;
    }
    
    private void createDashboard() {
        JPanel dashboard = new JPanel(new BorderLayout());
        
        // Top bar
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(new Color(39, 60, 117));
        topBar.setPreferredSize(new Dimension(0, 60));
        topBar.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        
        JLabel titleLabel = new JLabel("Learning Institute Management System");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);
        
        JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        userPanel.setOpaque(false);
        JLabel userLabel = new JLabel(currentUser + " (" + currentRole.toUpperCase() + ")");
        userLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        userLabel.setForeground(Color.WHITE);
        
        // Create RED RIBBON logout button
        JButton logoutBtn = createRibbonButton("Logout", new Color(192, 57, 43), new Color(231, 76, 60));
        logoutBtn.addActionListener(e -> logout());
        
        userPanel.add(userLabel);
        userPanel.add(Box.createHorizontalStrut(15));
        userPanel.add(logoutBtn);
        
        topBar.add(titleLabel, BorderLayout.WEST);
        topBar.add(userPanel, BorderLayout.EAST);
        
        // Main content area
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(250);
        splitPane.setDividerSize(1);
        
        // Sidebar navigation
        JPanel sidebar = createSidebar();
        splitPane.setLeftComponent(sidebar);
        
        // Content area - Welcome message
        JPanel contentArea = new JPanel(new BorderLayout());
        contentArea.setBackground(Color.WHITE);
        
        JLabel welcomeLabel = new JLabel("<html><div style='text-align: center;'>" +
            "<h1>Welcome, " + currentUser + "!</h1>" +
            "<p style='color: #666; font-size: 14px;'>Select an option from the menu to get started.</p>" +
            "</div></html>", SwingConstants.CENTER);
        contentArea.add(welcomeLabel, BorderLayout.CENTER);
        
        splitPane.setRightComponent(contentArea);
        
        dashboard.add(topBar, BorderLayout.NORTH);
        dashboard.add(splitPane, BorderLayout.CENTER);
        
        mainPanel.add(dashboard, "DASHBOARD");
    }
    
    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(new Color(52, 73, 94));
        sidebar.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        
        // Add menu items based on role
        if (currentRole.equalsIgnoreCase("admin")) {
            addMenuItem(sidebar, "👥 Student Management", e -> openStudentManagement());
            addMenuItem(sidebar, "📚 Course Management", e -> openCourseManagement());
            addMenuItem(sidebar, "📖 Module Management", e -> openModuleManagement());
            addMenuItem(sidebar, "👨‍🏫 Teacher Management", e -> openTeacherManagement());
            addMenuItem(sidebar, "📝 Registration Management", e -> openRegistrationManagement());
            addMenuItem(sidebar, "💰 Payment Management", e -> openPaymentManagement());
            addMenuItem(sidebar, "💵 Salary Management", e -> openSalaryManagement());
            addMenuItem(sidebar, "📊 Reports & Statistics", e -> openReports());
            addMenuItem(sidebar, "⚙️ User Management", e -> openUserManagement());
            addMenuItem(sidebar, "🔑 Change Password", e -> openChangePassword());
            
        } else if (currentRole.equalsIgnoreCase("staff")) {
            addMenuItem(sidebar, "👥 Student Management", e -> openStudentManagement());
            addMenuItem(sidebar, "👨‍🏫 Teacher Management", e -> openTeacherManagement());
            addMenuItem(sidebar, "💰 Payment Management", e -> openPaymentManagement());
            addMenuItem(sidebar, "📊 Reports & Statistics", e -> openReports());
            addMenuItem(sidebar, "🔑 Change Password", e -> openChangePassword());
            
        } else if (currentRole.equalsIgnoreCase("teacher")) {
            addMenuItem(sidebar, "💵 My Salary", e -> openMySalary());
            addMenuItem(sidebar, "🔑 Change Password", e -> openChangePassword());
        }
        
        sidebar.add(Box.createVerticalGlue());
        
        return sidebar;
    }
    
    private void addMenuItem(JPanel sidebar, String text, ActionListener action) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        button.setForeground(Color.WHITE);
        button.setBackground(new Color(52, 73, 94));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setMaximumSize(new Dimension(250, 45));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        
        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                button.setBackground(new Color(44, 62, 80));
            }
            public void mouseExited(MouseEvent e) {
                button.setBackground(new Color(52, 73, 94));
            }
        });
        
        button.addActionListener(action);
        
        sidebar.add(button);
        sidebar.add(Box.createVerticalStrut(5));
    }
    
    private void logout() {
        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to logout?",
            "Confirm Logout",
            JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            currentUser = null;
            currentUsername = null;
            currentRole = null;
            currentUserId = -1;
            
            // Remove dashboard and show login
            for (Component comp : mainPanel.getComponents()) {
                if (comp != mainPanel.getComponent(0)) {
                    mainPanel.remove(comp);
                }
            }
            
            cardLayout.show(mainPanel, "LOGIN");
        }
    }
    
    // Menu action methods
    private void openStudentManagement() {
        new StudentManagementGUI(this).setVisible(true);
    }
    
    private void openCourseManagement() {
        new CourseManagementGUI(this).setVisible(true);
    }
    
    private void openModuleManagement() {
        new ModuleManagementGUI(this).setVisible(true);
    }
    
    private void openTeacherManagement() {
        new TeacherManagementGUI(this).setVisible(true);
    }
    
    private void openRegistrationManagement() {
        new RegistrationManagementGUI(this).setVisible(true);
    }
    
    private void openPaymentManagement() {
        new PaymentManagementGUI(this).setVisible(true);
    }
    
    private void openSalaryManagement() {
        new SalaryManagementGUI(this).setVisible(true);
    }
    
    private void openReports() {
        new ReportsGUI(this).setVisible(true);
    }
    
    private void openUserManagement() {
        new UserManagementGUI(this).setVisible(true);
    }
    
    private void openChangePassword() {
        new ChangePasswordGUI(this, currentUsername).setVisible(true);
    }
    
    private void openMySalary() {
        new MySalaryGUI(this, currentUsername).setVisible(true);
    }
    
    /**
     * Creates a modern ribbon-style button with gradient and 3D effect
     * @param text Button text
     * @param darkColor Darker shade for gradient top
     * @param lightColor Lighter shade for gradient bottom
     * @return Styled ribbon button
     */
    private JButton createRibbonButton(String text, Color darkColor, Color lightColor) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                int width = getWidth();
                int height = getHeight();
                
                // Create gradient paint
                GradientPaint gradient;
                if (getModel().isPressed()) {
                    // Pressed state - reverse gradient
                    gradient = new GradientPaint(0, 0, lightColor.darker(), 0, height, darkColor);
                } else if (getModel().isRollover()) {
                    // Hover state - brighter
                    gradient = new GradientPaint(0, 0, darkColor.brighter(), 0, height, lightColor.brighter());
                } else {
                    // Normal state
                    gradient = new GradientPaint(0, 0, darkColor, 0, height, lightColor);
                }
                
                g2d.setPaint(gradient);
                g2d.fillRoundRect(0, 0, width, height, 8, 8);
                
                // Add glossy effect on top half
                GradientPaint gloss = new GradientPaint(
                    0, 0, new Color(255, 255, 255, 60),
                    0, height / 2, new Color(255, 255, 255, 10)
                );
                g2d.setPaint(gloss);
                g2d.fillRoundRect(0, 0, width, height / 2, 8, 8);
                
                // Draw border
                g2d.setColor(darkColor.darker());
                g2d.setStroke(new BasicStroke(2));
                g2d.drawRoundRect(1, 1, width - 2, height - 2, 8, 8);
                
                // Draw inner highlight
                g2d.setColor(new Color(255, 255, 255, 40));
                g2d.drawRoundRect(2, 2, width - 4, height - 4, 6, 6);
                
                g2d.dispose();
                
                // Paint text
                super.paintComponent(g);
            }
        };
        
        button.setFont(new Font("Segoe UI", Font.BOLD, 16));
        button.setForeground(Color.WHITE);
        button.setPreferredSize(new Dimension(200, 45));
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Add shadow effect with border
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(2, 2, 4, 2),
            BorderFactory.createEmptyBorder(8, 20, 8, 20)
        ));
        
        return button;
    }
    
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        SwingUtilities.invokeLater(() -> {
            new LearningInstituteGUI().setVisible(true);
        });
    }
}