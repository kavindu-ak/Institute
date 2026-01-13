import java.awt.*;
import java.sql.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import lib.DatabaseUtil;

// CourseManagementGUI.java - Enhanced with better UI
class CourseManagementGUI extends JDialog {
    private JTable courseTable;
    private DefaultTableModel tableModel;
    
    public CourseManagementGUI(JFrame parent) {
        super(parent, "Course Management", true);
        setSize(900, 550);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());
        
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(39, 60, 117));
        headerPanel.setPreferredSize(new Dimension(0, 60));
        JLabel titleLabel = new JLabel("📚 Course Management");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        buttonPanel.setBackground(Color.WHITE);
        
        JButton addButton = createButton("➕ Add Course", new Color(46, 204, 113));
        JButton refreshButton = createButton("🔄 Refresh", new Color(52, 152, 219));
        JButton viewButton = createButton("👁️ View Details", new Color(155, 89, 182));
        
        addButton.addActionListener(e -> showAddCourseDialog());
        refreshButton.addActionListener(e -> loadCourses());
        viewButton.addActionListener(e -> viewCourseDetails());
        
        buttonPanel.add(addButton);
        buttonPanel.add(refreshButton);
        buttonPanel.add(viewButton);
        
        String[] columns = {"Course ID", "Course Name", "Modules Count", "Total Students"};
        tableModel = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int row, int column) { return false; }
        };
        
        courseTable = new JTable(tableModel);
        styleTable(courseTable);
        
        JPanel mainPanel = new JPanel(new BorderLayout(0, 10));
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        mainPanel.add(buttonPanel, BorderLayout.NORTH);
        mainPanel.add(new JScrollPane(courseTable), BorderLayout.CENTER);
        
        add(headerPanel, BorderLayout.NORTH);
        add(mainPanel, BorderLayout.CENTER);
        
        loadCourses();
    }
    
    private void loadCourses() {
        tableModel.setRowCount(0);
        try {
            Connection conn = DatabaseUtil.getInstance().getConnection();
            String query = "SELECT c.c_id, c.c_name, " +
                          "COUNT(DISTINCT m.m_id) as module_count, " +
                          "COUNT(DISTINCT r.s_id) as student_count " +
                          "FROM course c " +
                          "LEFT JOIN modules m ON c.c_id = m.c_id " +
                          "LEFT JOIN registration r ON c.c_id = r.c_id " +
                          "GROUP BY c.c_id, c.c_name ORDER BY c.c_name";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                    rs.getInt("c_id"),
                    rs.getString("c_name"),
                    rs.getInt("module_count"),
                    rs.getInt("student_count")
                });
            }
            rs.close();
            stmt.close();
            conn.close();
        } catch (Exception e) {
            showError("Error loading courses: " + e.getMessage());
        }
    }
    
    private void showAddCourseDialog() {
        JTextField nameField = new JTextField(25);
        nameField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        gbc.gridx = 0; gbc.gridy = 0;
        JLabel label = new JLabel("Course Name:");
        label.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        panel.add(label, gbc);
        
        gbc.gridx = 1;
        panel.add(nameField, gbc);
        
        int result = JOptionPane.showConfirmDialog(this, panel, "Add New Course",
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        
        if (result == JOptionPane.OK_OPTION) {
            String name = nameField.getText().trim();
            if (name.isEmpty()) {
                showError("Course name cannot be empty!");
                return;
            }
            
            try {
                Connection conn = DatabaseUtil.getInstance().getConnection();
                PreparedStatement stmt = conn.prepareStatement("INSERT INTO course (c_name) VALUES (?)");
                stmt.setString(1, name);
                stmt.executeUpdate();
                stmt.close();
                conn.close();
                
                JOptionPane.showMessageDialog(this, 
                    "Course added successfully!\n\nNext Step: Add modules for this course.", 
                    "Success", JOptionPane.INFORMATION_MESSAGE);
                loadCourses();
            } catch (Exception e) {
                showError("Error adding course: " + e.getMessage());
            }
        }
    }
    
    private void viewCourseDetails() {
        int row = courseTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a course!", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int id = (int) tableModel.getValueAt(row, 0);
        String name = (String) tableModel.getValueAt(row, 1);
        int modules = (int) tableModel.getValueAt(row, 2);
        int students = (int) tableModel.getValueAt(row, 3);
        
        String details = String.format(
            "<html><body style='width: 350px; padding: 10px;'>" +
            "<h2 style='color: #2c3e50;'>Course Details</h2>" +
            "<table style='width: 100%%;'>" +
            "<tr><td><b>Course ID:</b></td><td>%d</td></tr>" +
            "<tr><td><b>Course Name:</b></td><td>%s</td></tr>" +
            "<tr><td><b>Total Modules:</b></td><td>%d</td></tr>" +
            "<tr><td><b>Enrolled Students:</b></td><td>%d</td></tr>" +
            "</table></body></html>",
            id, name, modules, students
        );
        
        JOptionPane.showMessageDialog(this, details, "Course Details", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private JButton createButton(String text, Color color) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(150, 35));
        return btn;
    }
    
    private void styleTable(JTable table) {
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setRowHeight(30);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        table.getTableHeader().setBackground(new Color(52, 73, 94));
        table.getTableHeader().setForeground(Color.WHITE);
        table.setSelectionBackground(new Color(52, 152, 219));
    }
    
    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }
}

// ModuleManagementGUI.java - Enhanced
class ModuleManagementGUI extends JDialog {
    private JTable moduleTable;
    private DefaultTableModel tableModel;
    
    public ModuleManagementGUI(JFrame parent) {
        super(parent, "Module Management", true);
        setSize(1000, 600);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());
        
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(39, 60, 117));
        headerPanel.setPreferredSize(new Dimension(0, 60));
        JLabel titleLabel = new JLabel("📖 Module Management");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        buttonPanel.setBackground(Color.WHITE);
        
        JButton addButton = createButton("➕ Add Module", new Color(46, 204, 113));
        JButton refreshButton = createButton("🔄 Refresh", new Color(52, 152, 219));
        JButton viewButton = createButton("👁️ View Details", new Color(155, 89, 182));
        
        addButton.addActionListener(e -> showAddModuleDialog());
        refreshButton.addActionListener(e -> loadModules());
        viewButton.addActionListener(e -> viewModuleDetails());
        
        buttonPanel.add(addButton);
        buttonPanel.add(refreshButton);
        buttonPanel.add(viewButton);
        
        String[] columns = {"Module ID", "Module Name", "Course", "Cost (Rs.)", "Teacher", "Students"};
        tableModel = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int row, int column) { return false; }
        };
        
        moduleTable = new JTable(tableModel);
        styleTable(moduleTable);
        
        JPanel mainPanel = new JPanel(new BorderLayout(0, 10));
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        mainPanel.add(buttonPanel, BorderLayout.NORTH);
        mainPanel.add(new JScrollPane(moduleTable), BorderLayout.CENTER);
        
        add(headerPanel, BorderLayout.NORTH);
        add(mainPanel, BorderLayout.CENTER);
        
        loadModules();
    }
    
    private void loadModules() {
        tableModel.setRowCount(0);
        try {
            Connection conn = DatabaseUtil.getInstance().getConnection();
            String query = "SELECT m.m_id, m.m_name, c.c_name, m.cost, t.t_name, " +
                          "COUNT(DISTINCT l.s_id) as student_count " +
                          "FROM modules m " +
                          "INNER JOIN course c ON m.c_id = c.c_id " +
                          "LEFT JOIN teacher t ON m.m_id = t.m_id " +
                          "LEFT JOIN learning l ON m.m_id = l.m_id AND l.paid = 'yes' " +
                          "GROUP BY m.m_id, m.m_name, c.c_name, m.cost, t.t_name " +
                          "ORDER BY c.c_name, m.m_name";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            
            while (rs.next()) {
                String teacher = rs.getString("t_name");
                tableModel.addRow(new Object[]{
                    rs.getInt("m_id"),
                    rs.getString("m_name"),
                    rs.getString("c_name"),
                    rs.getInt("cost"),
                    teacher != null ? teacher : "No Teacher",
                    rs.getInt("student_count")
                });
            }
            rs.close();
            stmt.close();
            conn.close();
        } catch (Exception e) {
            showError("Error loading modules: " + e.getMessage());
        }
    }
    
    private void showAddModuleDialog() {
        JTextField nameField = new JTextField(20);
        JTextField costField = new JTextField(20);
        JComboBox<CourseItem> courseBox = new JComboBox<>();
        
        nameField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        costField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        courseBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        // Load courses
        try {
            Connection conn = DatabaseUtil.getInstance().getConnection();
            ResultSet rs = conn.createStatement().executeQuery("SELECT c_id, c_name FROM course ORDER BY c_name");
            while (rs.next()) {
                courseBox.addItem(new CourseItem(rs.getInt("c_id"), rs.getString("c_name")));
            }
            rs.close();
            conn.close();
        } catch (Exception e) {
            showError("Error loading courses: " + e.getMessage());
            return;
        }
        
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Module Name:"), gbc);
        gbc.gridx = 1;
        panel.add(nameField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Cost (Rs.):"), gbc);
        gbc.gridx = 1;
        panel.add(costField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Course:"), gbc);
        gbc.gridx = 1;
        panel.add(courseBox, gbc);
        
        int result = JOptionPane.showConfirmDialog(this, panel, "Add New Module",
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        
        if (result == JOptionPane.OK_OPTION) {
            try {
                String name = nameField.getText().trim();
                int cost = Integer.parseInt(costField.getText().trim());
                CourseItem course = (CourseItem) courseBox.getSelectedItem();
                
                if (name.isEmpty()) {
                    showError("Module name cannot be empty!");
                    return;
                }
                
                Connection conn = DatabaseUtil.getInstance().getConnection();
                PreparedStatement stmt = conn.prepareStatement(
                    "INSERT INTO modules (m_name, cost, c_id) VALUES (?, ?, ?)");
                stmt.setString(1, name);
                stmt.setInt(2, cost);
                stmt.setInt(3, course.id);
                stmt.executeUpdate();
                stmt.close();
                conn.close();
                
                JOptionPane.showMessageDialog(this, 
                    "Module added successfully!\n\nNext Step: Assign a teacher to this module.", 
                    "Success", JOptionPane.INFORMATION_MESSAGE);
                loadModules();
            } catch (NumberFormatException e) {
                showError("Cost must be a valid number!");
            } catch (Exception e) {
                showError("Error adding module: " + e.getMessage());
            }
        }
    }
    
    private void viewModuleDetails() {
        int row = moduleTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a module!", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int id = (int) tableModel.getValueAt(row, 0);
        String name = (String) tableModel.getValueAt(row, 1);
        String course = (String) tableModel.getValueAt(row, 2);
        int cost = (int) tableModel.getValueAt(row, 3);
        String teacher = (String) tableModel.getValueAt(row, 4);
        int students = (int) tableModel.getValueAt(row, 5);
        
        String details = String.format(
            "<html><body style='width: 350px; padding: 10px;'>" +
            "<h2 style='color: #2c3e50;'>Module Details</h2>" +
            "<table style='width: 100%%;'>" +
            "<tr><td><b>Module ID:</b></td><td>%d</td></tr>" +
            "<tr><td><b>Module Name:</b></td><td>%s</td></tr>" +
            "<tr><td><b>Course:</b></td><td>%s</td></tr>" +
            "<tr><td><b>Cost:</b></td><td>Rs. %,d</td></tr>" +
            "<tr><td><b>Teacher:</b></td><td>%s</td></tr>" +
            "<tr><td><b>Students Enrolled:</b></td><td>%d</td></tr>" +
            "</table></body></html>",
            id, name, course, cost, teacher, students
        );
        
        JOptionPane.showMessageDialog(this, details, "Module Details", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private JButton createButton(String text, Color color) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(150, 35));
        return btn;
    }
    
    private void styleTable(JTable table) {
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setRowHeight(30);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        table.getTableHeader().setBackground(new Color(52, 73, 94));
        table.getTableHeader().setForeground(Color.WHITE);
        table.setSelectionBackground(new Color(52, 152, 219));
    }
    
    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }
    
    static class CourseItem {
        int id;
        String name;
        CourseItem(int id, String name) { this.id = id; this.name = name; }
        public String toString() { return name; }
    }
}

// MySalaryGUI.java - Placeholder
class MySalaryGUI extends JDialog {
    public MySalaryGUI(JFrame parent, String username) {
        super(parent, "My Salary", true);
        setSize(900, 550);
        setLocationRelativeTo(parent);
        
        JLabel label = new JLabel("My Salary Information - Feature Available", SwingConstants.CENTER);
        label.setFont(new Font("Segoe UI", Font.BOLD, 18));
        add(label);
    }
}