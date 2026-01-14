import java.awt.*;
import java.sql.*;
import java.time.LocalDate;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import lib.DatabaseUtil;

public class RegistrationManagementGUI extends JDialog {
    
    private JTable registrationTable;
    private DefaultTableModel tableModel;
    private JFrame parent;
    
    public RegistrationManagementGUI(JFrame parent) {
        super(parent, "Registration Management", true);
        this.parent = parent;
        
        setSize(1100, 650);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));
        
        // Header
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(39, 60, 117));
        headerPanel.setPreferredSize(new Dimension(0, 60));
        JLabel titleLabel = new JLabel("📝 Course Registration Management");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        buttonPanel.setBackground(Color.WHITE);
        
        JButton registerButton = createStyledButton("➕ Register Student", new Color(46, 204, 113));
        JButton refreshButton = createStyledButton("🔄 Refresh", new Color(52, 152, 219));
        JButton viewButton = createStyledButton("👁️ View Details", new Color(155, 89, 182));
        
        registerButton.addActionListener(e -> showRegisterStudentDialog());
        refreshButton.addActionListener(e -> loadRegistrations());
        viewButton.addActionListener(e -> viewRegistrationDetails());
        
        buttonPanel.add(registerButton);
        buttonPanel.add(refreshButton);
        buttonPanel.add(viewButton);
        
        // Table
        String[] columns = {"Reg ID", "Student ID", "Student Name", "Course ID", "Course Name", "Modules", "Paid/Total", "Date"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        registrationTable = new JTable(tableModel);
        styleTable(registrationTable);
        
        JScrollPane scrollPane = new JScrollPane(registrationTable);
        
        // Main panel
        JPanel mainPanel = new JPanel(new BorderLayout(0, 10));
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        mainPanel.add(buttonPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        
        add(headerPanel, BorderLayout.NORTH);
        add(mainPanel, BorderLayout.CENTER);
        
        loadRegistrations();
    }
    
    private JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(170, 35));
        return button;
    }
    
    private void loadRegistrations() {
        tableModel.setRowCount(0);
        
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DatabaseUtil.getInstance().getConnection();
            
            String query = 
                "SELECT r.reg_id, r.s_id, s.s_name, r.c_id, c.c_name, r.date, " +
                "COUNT(m.m_id) as total_modules, " +
                "SUM(CASE WHEN l.paid = 'yes' THEN 1 ELSE 0 END) as paid_modules " +
                "FROM registration r " +
                "INNER JOIN student s ON r.s_id = s.s_id " +
                "INNER JOIN course c ON r.c_id = c.c_id " +
                "LEFT JOIN modules m ON c.c_id = m.c_id " +
                "LEFT JOIN learning l ON l.s_id = r.s_id AND l.m_id = m.m_id " +
                "GROUP BY r.reg_id, r.s_id, s.s_name, r.c_id, c.c_name, r.date " +
                "ORDER BY r.date DESC, s.s_name";
            
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            
            while (rs.next()) {
                int paidModules = rs.getInt("paid_modules");
                int totalModules = rs.getInt("total_modules");
                String modulesStatus = paidModules + "/" + totalModules;
                
                Object[] row = {
                    rs.getInt("reg_id"),
                    rs.getInt("s_id"),
                    rs.getString("s_name"),
                    rs.getInt("c_id"),
                    rs.getString("c_name"),
                    totalModules,
                    modulesStatus,
                    rs.getString("date")
                };
                tableModel.addRow(row);
            }
            
            rs.close();
            stmt.close();
            conn.close();
            
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                "Error loading registrations: " + e.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void showRegisterStudentDialog() {
        JDialog dialog = new JDialog(this, "Register Student to Course", true);
        dialog.setSize(600, 550);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());
        
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 8, 8, 8);
        
        // Student selection
        JComboBox<StudentItem> studentBox = new JComboBox<>();
        loadStudents(studentBox);
        
        // Course selection
        JComboBox<CourseItem> courseBox = new JComboBox<>();
        
        // Date field
        JTextField dateField = new JTextField(LocalDate.now().toString());
        
        Font fieldFont = new Font("Segoe UI", Font.PLAIN, 14);
        studentBox.setFont(fieldFont);
        courseBox.setFont(fieldFont);
        dateField.setFont(fieldFont);
        
        // Student selection change listener - load available courses
        studentBox.addActionListener(e -> {
            StudentItem selected = (StudentItem) studentBox.getSelectedItem();
            if (selected != null) {
                loadAvailableCoursesForStudent(courseBox, selected.getId());
            }
        });
        
        int row = 0;
        addFormField(formPanel, gbc, row++, "Student:", studentBox);
        addFormField(formPanel, gbc, row++, "Course:", courseBox);
        addFormField(formPanel, gbc, row++, "Registration Date:", dateField);
        
        // Info label
        gbc.gridx = 0;
        gbc.gridy = row++;
        gbc.gridwidth = 2;
        JLabel infoLabel = new JLabel("<html><i>Note: Only courses not yet registered for this student will appear</i></html>");
        infoLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        infoLabel.setForeground(new Color(100, 100, 100));
        formPanel.add(infoLabel, gbc);
        
        // Trigger initial course load
        if (studentBox.getItemCount() > 0) {
            StudentItem first = studentBox.getItemAt(0);
            loadAvailableCoursesForStudent(courseBox, first.getId());
        }
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 15));
        buttonPanel.setBackground(Color.WHITE);
        
        JButton saveButton = createStyledButton("💾 Register", new Color(46, 204, 113));
        JButton cancelButton = createStyledButton("❌ Cancel", new Color(231, 76, 60));
        
        saveButton.addActionListener(e -> {
            StudentItem selectedStudent = (StudentItem) studentBox.getSelectedItem();
            CourseItem selectedCourse = (CourseItem) courseBox.getSelectedItem();
            String date = dateField.getText().trim();
            
            if (selectedStudent == null || selectedCourse == null || date.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Please fill all fields!", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                Connection conn = DatabaseUtil.getInstance().getConnection();
                
                String sql = "INSERT INTO registration (s_id, c_id, date) VALUES (?, ?, ?)";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setInt(1, selectedStudent.getId());
                stmt.setInt(2, selectedCourse.getId());
                stmt.setString(3, date);
                
                int rows = stmt.executeUpdate();
                
                stmt.close();
                conn.close();
                
                if (rows > 0) {
                    String message = String.format(
                        "Registration Successful!\n\n" +
                        "Student: %s\n" +
                        "Course: %s\n" +
                        "Modules Available: %d\n\n" +
                        "Next Step: Go to Payment Management to process module payments",
                        selectedStudent.getName(),
                        selectedCourse.getName(),
                        selectedCourse.getModuleCount()
                    );
                    
                    JOptionPane.showMessageDialog(dialog, message, "Success", JOptionPane.INFORMATION_MESSAGE);
                    dialog.dispose();
                    loadRegistrations();
                }
                
            } catch (SQLIntegrityConstraintViolationException ex) {
                JOptionPane.showMessageDialog(dialog, 
                    "This student is already registered for this course!", 
                    "Duplicate Registration", 
                    JOptionPane.ERROR_MESSAGE);
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
    
    private void loadStudents(JComboBox<StudentItem> studentBox) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DatabaseUtil.getInstance().getConnection();
            
            String query = "SELECT s_id, s_name FROM student ORDER BY s_name";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            
            while (rs.next()) {
                studentBox.addItem(new StudentItem(
                    rs.getInt("s_id"),
                    rs.getString("s_name")
                ));
            }
            
            rs.close();
            stmt.close();
            conn.close();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void loadAvailableCoursesForStudent(JComboBox<CourseItem> courseBox, int studentId) {
        courseBox.removeAllItems();
        
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DatabaseUtil.getInstance().getConnection();
            
            String query = 
                "SELECT c.c_id, c.c_name, COUNT(m.m_id) as module_count " +
                "FROM course c " +
                "LEFT JOIN modules m ON c.c_id = m.c_id " +
                "WHERE c.c_id NOT IN ( " +
                "    SELECT c_id FROM registration WHERE s_id = ? " +
                ") " +
                "GROUP BY c.c_id, c.c_name " +
                "ORDER BY c.c_name";
            
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, studentId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                courseBox.addItem(new CourseItem(
                    rs.getInt("c_id"),
                    rs.getString("c_name"),
                    rs.getInt("module_count")
                ));
            }
            
            rs.close();
            stmt.close();
            conn.close();
            
            if (courseBox.getItemCount() == 0) {
                courseBox.addItem(new CourseItem(0, "No available courses", 0));
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
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
    
    private void viewRegistrationDetails() {
        int selectedRow = registrationTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a registration to view!", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int regId = (int) tableModel.getValueAt(selectedRow, 0);
        int studentId = (int) tableModel.getValueAt(selectedRow, 1);
        String studentName = (String) tableModel.getValueAt(selectedRow, 2);
        int courseId = (int) tableModel.getValueAt(selectedRow, 3);
        String courseName = (String) tableModel.getValueAt(selectedRow, 4);
        int totalModules = (int) tableModel.getValueAt(selectedRow, 5);
        String paidStatus = (String) tableModel.getValueAt(selectedRow, 6);
        String date = (String) tableModel.getValueAt(selectedRow, 7);
        
        String details = String.format(
            "<html><body style='width: 400px; padding: 10px;'>" +
            "<h2 style='color: #2c3e50;'>Registration Details</h2>" +
            "<table style='width: 100%%;'>" +
            "<tr><td><b>Registration ID:</b></td><td>%d</td></tr>" +
            "<tr><td><b>Student ID:</b></td><td>%d</td></tr>" +
            "<tr><td><b>Student Name:</b></td><td>%s</td></tr>" +
            "<tr><td><b>Course ID:</b></td><td>%d</td></tr>" +
            "<tr><td><b>Course Name:</b></td><td>%s</td></tr>" +
            "<tr><td><b>Total Modules:</b></td><td>%d</td></tr>" +
            "<tr><td><b>Payment Status:</b></td><td>%s modules</td></tr>" +
            "<tr><td><b>Registration Date:</b></td><td>%s</td></tr>" +
            "</table>" +
            "<br><p style='color: #7f8c8d;'><i>Tip: Use Payment Management to process module payments</i></p>" +
            "</body></html>",
            regId, studentId, studentName, courseId, courseName, totalModules, paidStatus, date
        );
        
        JOptionPane.showMessageDialog(this, details, "Registration Details", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void styleTable(JTable table) {
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setRowHeight(30);
        table.setGridColor(new Color(220, 220, 220));
        table.setShowGrid(true);
        table.setIntercellSpacing(new Dimension(1, 1));
        
        // Enhanced header styling - more visible and flat
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        table.getTableHeader().setBackground(new Color(41, 128, 185));
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setPreferredSize(new Dimension(0, 40));
        table.getTableHeader().setBorder(BorderFactory.createLineBorder(new Color(31, 97, 141), 2));
        table.getTableHeader().setReorderingAllowed(false);
        
        table.setSelectionBackground(new Color(52, 152, 219));
        table.setSelectionForeground(Color.WHITE);
    }
    
    // Helper classes
    private static class StudentItem {
        private int id;
        private String name;
        
        public StudentItem(int id, String name) {
            this.id = id;
            this.name = name;
        }
        
        public int getId() { return id; }
        public String getName() { return name; }
        
        @Override
        public String toString() {
            return name + " (ID: " + id + ")";
        }
    }
    
    private static class CourseItem {
        private int id;
        private String name;
        private int moduleCount;
        
        public CourseItem(int id, String name, int moduleCount) {
            this.id = id;
            this.name = name;
            this.moduleCount = moduleCount;
        }
        
        public int getId() { return id; }
        public String getName() { return name; }
        public int getModuleCount() { return moduleCount; }
        
        @Override
        public String toString() {
            return name + " (" + moduleCount + " modules)";
        }
    }
}