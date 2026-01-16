import java.awt.*;
import java.sql.*;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import lib.DatabaseUtil;

public class TeacherManagementGUI extends JDialog {
    
    private JTable teacherTable;
    private DefaultTableModel tableModel;
    private JFrame parent;
    
    public TeacherManagementGUI(JFrame parent) {
        super(parent, "Teacher Management", true);
        this.parent = parent;
        
        setSize(1100, 600);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));
        
        // Header
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(39, 60, 117));
        headerPanel.setPreferredSize(new Dimension(0, 60));
        JLabel titleLabel = new JLabel("👨‍🏫 Teacher Management");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        buttonPanel.setBackground(Color.WHITE);
        
        JButton addButton = createStyledButton("➕ Add Teacher", new Color(46, 204, 113));
        JButton refreshButton = createStyledButton("🔄 Refresh", new Color(52, 152, 219));
        JButton viewButton = createStyledButton("👁️ View Details", new Color(155, 89, 182));
        
        addButton.addActionListener(e -> showAddTeacherDialog());
        refreshButton.addActionListener(e -> loadTeachers());
        viewButton.addActionListener(e -> viewTeacherDetails());
        
        buttonPanel.add(addButton);
        buttonPanel.add(refreshButton);
        buttonPanel.add(viewButton);
        
        // Table
        String[] columns = {"T_ID", "Name", "NIC", "Phone", "Gender", "Module", "Course", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        teacherTable = new JTable(tableModel);
        styleTable(teacherTable);
        
        JScrollPane scrollPane = new JScrollPane(teacherTable);
        
        // Main panel
        JPanel mainPanel = new JPanel(new BorderLayout(0, 10));
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        mainPanel.add(buttonPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        
        add(headerPanel, BorderLayout.NORTH);
        add(mainPanel, BorderLayout.CENTER);
        
        loadTeachers();
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
    
    private void loadTeachers() {
        tableModel.setRowCount(0);
        
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DatabaseUtil.getInstance().getConnection();
            
            String query = 
                "SELECT t.t_id, t.t_name, t.nic, t.tp, t.gen, " +
                "m.m_name, c.c_name, t.paid " +
                "FROM teacher t " +
                "INNER JOIN modules m ON t.m_id = m.m_id " +
                "INNER JOIN course c ON m.c_id = c.c_id " +
                "ORDER BY t.t_name";
            
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            
            while (rs.next()) {
                String status = "yes".equals(rs.getString("paid")) ? "✓ Paid" : "Pending";
                
                Object[] row = {
                    rs.getInt("t_id"),
                    rs.getString("t_name"),
                    rs.getInt("nic"),
                    rs.getInt("tp"),
                    rs.getString("gen"),
                    rs.getString("m_name"),
                    rs.getString("c_name"),
                    status
                };
                tableModel.addRow(row);
            }
            
            rs.close();
            stmt.close();
            conn.close();
            
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                "Error loading teachers: " + e.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void showAddTeacherDialog() {
        JDialog dialog = new JDialog(this, "Add New Teacher", true);
        dialog.setSize(600, 550);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());
        
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 8, 8, 8);
        
        JTextField nameField = new JTextField(20);
        JTextField nicField = new JTextField(20);
        JTextField phoneField = new JTextField(20);
        JTextField addressField = new JTextField(20);
        JComboBox<String> genderBox = new JComboBox<>(new String[]{"M", "F"});
        
        // Fetch available modules
        JComboBox<ModuleItem> moduleBox = new JComboBox<>();
        loadAvailableModules(moduleBox);
        
        Font fieldFont = new Font("Segoe UI", Font.PLAIN, 14);
        nameField.setFont(fieldFont);
        nicField.setFont(fieldFont);
        phoneField.setFont(fieldFont);
        addressField.setFont(fieldFont);
        genderBox.setFont(fieldFont);
        moduleBox.setFont(fieldFont);
        
        int row = 0;
        addFormField(formPanel, gbc, row++, "Name:", nameField);
        addFormField(formPanel, gbc, row++, "NIC:", nicField);
        addFormField(formPanel, gbc, row++, "Phone:", phoneField);
        addFormField(formPanel, gbc, row++, "Gender:", genderBox);
        addFormField(formPanel, gbc, row++, "Address:", addressField);
        addFormField(formPanel, gbc, row++, "Module:", moduleBox);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 15));
        buttonPanel.setBackground(Color.WHITE);
        
        JButton saveButton = createStyledButton("💾 Save", new Color(46, 204, 113));
        JButton cancelButton = createStyledButton("❌ Cancel", new Color(231, 76, 60));
        
        saveButton.addActionListener(e -> {
            String name = nameField.getText().trim();
            String nicStr = nicField.getText().trim();
            String phoneStr = phoneField.getText().trim();
            String gender = (String) genderBox.getSelectedItem();
            String address = addressField.getText().trim();
            ModuleItem selectedModule = (ModuleItem) moduleBox.getSelectedItem();
            
            if (name.isEmpty() || nicStr.isEmpty() || phoneStr.isEmpty() || address.isEmpty() || selectedModule == null) {
                JOptionPane.showMessageDialog(dialog, "Please fill all fields!", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            try {
                int nic = Integer.parseInt(nicStr);
                int phone = Integer.parseInt(phoneStr);
                int moduleId = selectedModule.getId();
                
                Teacher teacher = new Teacher(name, address, gender, nic, phone, moduleId);
                
                Class.forName("com.mysql.cj.jdbc.Driver");
                Connection conn = DatabaseUtil.getInstance().getConnection();
                
                // Check if module already has a teacher
                String checkQuery = "SELECT t_id FROM teacher WHERE m_id = ?";
                PreparedStatement checkStmt = conn.prepareStatement(checkQuery);
                checkStmt.setInt(1, moduleId);
                ResultSet checkRs = checkStmt.executeQuery();
                
                if (checkRs.next()) {
                    JOptionPane.showMessageDialog(dialog, 
                        "This module already has a teacher assigned!", 
                        "Validation Error", 
                        JOptionPane.ERROR_MESSAGE);
                    checkRs.close();
                    checkStmt.close();
                    conn.close();
                    return;
                }
                checkRs.close();
                checkStmt.close();
                
                String sql = "INSERT INTO teacher (t_name, nic, address, tp, gen, m_id) VALUES (?, ?, ?, ?, ?, ?)";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setString(1, teacher.getName());
                stmt.setInt(2, teacher.getNic());
                stmt.setString(3, teacher.getAddress());
                stmt.setInt(4, teacher.getPhoneNumber());
                stmt.setString(5, teacher.getGender());
                stmt.setInt(6, teacher.getModuleId());
                
                int rows = stmt.executeUpdate();
                
                stmt.close();
                conn.close();
                
                if (rows > 0) {
                    JOptionPane.showMessageDialog(dialog, "Teacher added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    dialog.dispose();
                    loadTeachers();
                }
                
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "NIC and Phone must be numbers!", "Validation Error", JOptionPane.ERROR_MESSAGE);
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
    
    private void loadAvailableModules(JComboBox<ModuleItem> moduleBox) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DatabaseUtil.getInstance().getConnection();
            
            String query = 
                "SELECT m.m_id, m.m_name, c.c_name " +
                "FROM modules m " +
                "INNER JOIN course c ON m.c_id = c.c_id " +
                "LEFT JOIN teacher t ON m.m_id = t.m_id " +
                "WHERE t.t_id IS NULL " +
                "ORDER BY c.c_name, m.m_name";
            
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            
            while (rs.next()) {
                int id = rs.getInt("m_id");
                String moduleName = rs.getString("m_name");
                String courseName = rs.getString("c_name");
                moduleBox.addItem(new ModuleItem(id, moduleName, courseName));
            }
            
            rs.close();
            stmt.close();
            conn.close();
            
            if (moduleBox.getItemCount() == 0) {
                moduleBox.addItem(new ModuleItem(0, "No available modules", ""));
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void addFormField(JPanel panel, GridBagConstraints gbc, int row, String label, JComponent field) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.3;
        JLabel jLabel = new JLabel(label);
        jLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        panel.add(jLabel, gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        panel.add(field, gbc);
    }
    
    private void viewTeacherDetails() {
        int selectedRow = teacherTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a teacher to view!", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int id = (int) tableModel.getValueAt(selectedRow, 0);
        String name = (String) tableModel.getValueAt(selectedRow, 1);
        int nic = (int) tableModel.getValueAt(selectedRow, 2);
        int phone = (int) tableModel.getValueAt(selectedRow, 3);
        String gender = (String) tableModel.getValueAt(selectedRow, 4);
        String module = (String) tableModel.getValueAt(selectedRow, 5);
        String course = (String) tableModel.getValueAt(selectedRow, 6);
        String status = (String) tableModel.getValueAt(selectedRow, 7);
        
        String details = String.format(
            "<html><body style='width: 350px; padding: 10px;'>" +
            "<h2 style='color: #2c3e50;'>Teacher Details</h2>" +
            "<table style='width: 100%%;'>" +
            "<tr><td><b>ID:</b></td><td>%d</td></tr>" +
            "<tr><td><b>Name:</b></td><td>%s</td></tr>" +
            "<tr><td><b>NIC:</b></td><td>%d</td></tr>" +
            "<tr><td><b>Phone:</b></td><td>%d</td></tr>" +
            "<tr><td><b>Gender:</b></td><td>%s</td></tr>" +
            "<tr><td><b>Module:</b></td><td>%s</td></tr>" +
            "<tr><td><b>Course:</b></td><td>%s</td></tr>" +
            "<tr><td><b>Payment Status:</b></td><td>%s</td></tr>" +
            "</table></body></html>",
            id, name, nic, phone, gender, module, course, status
        );
        
        JOptionPane.showMessageDialog(this, details, "Teacher Details", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void styleTable(JTable table) {
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setRowHeight(30);
        table.setGridColor(new Color(220, 220, 220));
        table.setShowGrid(true);
        table.setIntercellSpacing(new Dimension(1, 1));
        
        // FORCE header colors with custom renderer
        table.getTableHeader().setOpaque(true);
        table.getTableHeader().setBackground(new Color(41, 128, 185));
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        table.getTableHeader().setPreferredSize(new Dimension(0, 40));
        table.getTableHeader().setBorder(BorderFactory.createLineBorder(new Color(31, 97, 141), 2));
        table.getTableHeader().setReorderingAllowed(false);
        
        // Custom renderer to FORCE colors
        table.getTableHeader().setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(
                    table, value, isSelected, hasFocus, row, column);
                label.setBackground(new Color(41, 128, 185));
                label.setForeground(Color.WHITE);
                label.setFont(new Font("Segoe UI", Font.BOLD, 14));
                label.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 2, 1, new Color(31, 97, 141)),
                    BorderFactory.createEmptyBorder(10, 5, 10, 5)
                ));
                label.setHorizontalAlignment(SwingConstants.CENTER);
                label.setOpaque(true);
                return label;
            }
        });
        
        table.setSelectionBackground(new Color(52, 152, 219));
        table.setSelectionForeground(Color.WHITE);
    }
    
    // Helper class for module items
    private static class ModuleItem {
        private int id;
        private String name;
        private String courseName;
        
        public ModuleItem(int id, String name, String courseName) {
            this.id = id;
            this.name = name;
            this.courseName = courseName;
        }
        
        public int getId() {
            return id;
        }
        
        @Override
        public String toString() {
            return name + " (" + courseName + ")";
        }
    }
}