import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.*;
import java.util.List;
import lib.DatabaseUtil;

public class PaymentManagementGUI extends JDialog {
    
    private JTable paymentTable;
    private DefaultTableModel tableModel;
    private JFrame parent;
    
    public PaymentManagementGUI(JFrame parent) {
        super(parent, "Payment Management", true);
        this.parent = parent;
        
        setSize(1200, 700);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));
        
        // Header
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(39, 60, 117));
        headerPanel.setPreferredSize(new Dimension(0, 60));
        JLabel titleLabel = new JLabel("💰 Student Payment Management");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        buttonPanel.setBackground(Color.WHITE);
        
        JButton processButton = createStyledButton("➕ Process Payment", new Color(46, 204, 113));
        JButton historyButton = createStyledButton("📜 Payment History", new Color(52, 152, 219));
        JButton refreshButton = createStyledButton("🔄 Refresh", new Color(155, 89, 182));
        
        processButton.addActionListener(e -> showProcessPaymentDialog());
        historyButton.addActionListener(e -> showPaymentHistory());
        refreshButton.addActionListener(e -> loadPaymentSummary());
        
        buttonPanel.add(processButton);
        buttonPanel.add(historyButton);
        buttonPanel.add(refreshButton);
        
        // Table - Summary of unpaid modules by student
        String[] columns = {"Student ID", "Student Name", "Course", "Unpaid Modules", "Total Due (Rs.)", "Paid Modules"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        paymentTable = new JTable(tableModel);
        paymentTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        paymentTable.setRowHeight(30);
        paymentTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        paymentTable.getTableHeader().setBackground(new Color(52, 73, 94));
        paymentTable.getTableHeader().setForeground(Color.WHITE);
        paymentTable.setSelectionBackground(new Color(52, 152, 219));
        paymentTable.setSelectionForeground(Color.WHITE);
        
        JScrollPane scrollPane = new JScrollPane(paymentTable);
        
        // Main panel
        JPanel mainPanel = new JPanel(new BorderLayout(0, 10));
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        mainPanel.add(buttonPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        
        add(headerPanel, BorderLayout.NORTH);
        add(mainPanel, BorderLayout.CENTER);
        
        loadPaymentSummary();
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
    
    private void loadPaymentSummary() {
        tableModel.setRowCount(0);
        
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DatabaseUtil.getInstance().getConnection();
            
            String query = 
                "SELECT r.s_id, s.s_name, c.c_name, " +
                "COUNT(DISTINCT m.m_id) as total_modules, " +
                "SUM(CASE WHEN l.paid IS NULL OR l.paid != 'yes' THEN m.cost ELSE 0 END) as total_due, " +
                "SUM(CASE WHEN l.paid = 'yes' THEN 1 ELSE 0 END) as paid_count " +
                "FROM registration r " +
                "INNER JOIN student s ON r.s_id = s.s_id " +
                "INNER JOIN course c ON r.c_id = c.c_id " +
                "INNER JOIN modules m ON c.c_id = m.c_id " +
                "LEFT JOIN learning l ON l.s_id = r.s_id AND l.m_id = m.m_id " +
                "GROUP BY r.s_id, s.s_name, c.c_name " +
                "HAVING total_due > 0 " +
                "ORDER BY s.s_name, c.c_name";
            
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            
            while (rs.next()) {
                int totalModules = rs.getInt("total_modules");
                int paidCount = rs.getInt("paid_count");
                int unpaidCount = totalModules - paidCount;
                
                Object[] row = {
                    rs.getInt("s_id"),
                    rs.getString("s_name"),
                    rs.getString("c_name"),
                    unpaidCount,
                    rs.getInt("total_due"),
                    paidCount
                };
                tableModel.addRow(row);
            }
            
            rs.close();
            stmt.close();
            conn.close();
            
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                "Error loading payment summary: " + e.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void showProcessPaymentDialog() {
        JDialog dialog = new JDialog(this, "Process Student Payment", true);
        dialog.setSize(900, 700);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(10, 10));
        
        // Step 1: Select Student and Course
        JPanel topPanel = new JPanel(new GridBagLayout());
        topPanel.setBackground(Color.WHITE);
        topPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(52, 152, 219), 2),
            "Step 1: Select Student & Course",
            0, 0,
            new Font("Segoe UI", Font.BOLD, 14),
            new Color(52, 152, 219)
        ));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 10, 5, 10);
        
        JComboBox<RegistrationItem> registrationBox = new JComboBox<>();
        loadRegistrations(registrationBox);
        registrationBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.3;
        topPanel.add(new JLabel("Student & Course:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        topPanel.add(registrationBox, gbc);
        
        // Step 2: Module selection area
        JPanel modulePanel = new JPanel(new BorderLayout(5, 5));
        modulePanel.setBackground(Color.WHITE);
        modulePanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(46, 204, 113), 2),
            "Step 2: Select Modules to Pay",
            0, 0,
            new Font("Segoe UI", Font.BOLD, 14),
            new Color(46, 204, 113)
        ));
        
        DefaultTableModel moduleTableModel = new DefaultTableModel(
            new String[]{"☑", "Module ID", "Module Name", "Cost (Rs.)", "Teacher", "Status"}, 0) {
            @Override
            public Class<?> getColumnClass(int column) {
                return column == 0 ? Boolean.class : Object.class;
            }
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 0 && "Unpaid".equals(getValueAt(row, 5));
            }
        };
        
        JTable moduleTable = new JTable(moduleTableModel);
        moduleTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        moduleTable.setRowHeight(30);
        moduleTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        moduleTable.getTableHeader().setBackground(new Color(52, 73, 94));
        moduleTable.getTableHeader().setForeground(Color.WHITE);
        
        JScrollPane moduleScroll = new JScrollPane(moduleTable);
        modulePanel.add(moduleScroll, BorderLayout.CENTER);
        
        // Payment summary panel
        JPanel summaryPanel = new JPanel(new GridLayout(4, 2, 10, 10));
        summaryPanel.setBackground(new Color(236, 240, 241));
        summaryPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(155, 89, 182), 2),
                "Payment Summary",
                0, 0,
                new Font("Segoe UI", Font.BOLD, 14),
                new Color(155, 89, 182)
            ),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        
        JLabel selectedCountLabel = new JLabel("Selected Modules: 0");
        JLabel totalAmountLabel = new JLabel("Total Amount: Rs. 0");
        JLabel teacherShareLabel = new JLabel("Teacher Share (93%): Rs. 0");
        JLabel instituteShareLabel = new JLabel("Institute Share (7%): Rs. 0");
        
        Font summaryFont = new Font("Segoe UI", Font.BOLD, 14);
        selectedCountLabel.setFont(summaryFont);
        totalAmountLabel.setFont(summaryFont);
        teacherShareLabel.setFont(summaryFont);
        instituteShareLabel.setFont(summaryFont);
        
        summaryPanel.add(new JLabel("  "));
        summaryPanel.add(selectedCountLabel);
        summaryPanel.add(new JLabel("  "));
        summaryPanel.add(totalAmountLabel);
        summaryPanel.add(new JLabel("  "));
        summaryPanel.add(teacherShareLabel);
        summaryPanel.add(new JLabel("  "));
        summaryPanel.add(instituteShareLabel);
        
        // Update summary when checkboxes change
        moduleTableModel.addTableModelListener(e -> {
            int selectedCount = 0;
            int totalAmount = 0;
            
            for (int i = 0; i < moduleTableModel.getRowCount(); i++) {
                Boolean checked = (Boolean) moduleTableModel.getValueAt(i, 0);
                if (checked != null && checked) {
                    selectedCount++;
                    totalAmount += (Integer) moduleTableModel.getValueAt(i, 3);
                }
            }
            
            int teacherShare = (int)(totalAmount * 0.93);
            int instituteShare = totalAmount - teacherShare;
            
            selectedCountLabel.setText("Selected Modules: " + selectedCount);
            totalAmountLabel.setText("Total Amount: Rs. " + totalAmount);
            teacherShareLabel.setText("Teacher Share (93%): Rs. " + teacherShare);
            instituteShareLabel.setText("Institute Share (7%): Rs. " + instituteShare);
        });
        
        // Load modules when registration selected
        registrationBox.addActionListener(e -> {
            RegistrationItem item = (RegistrationItem) registrationBox.getSelectedItem();
            if (item != null) {
                loadModulesForRegistration(moduleTableModel, item);
            }
        });
        
        // Initial load
        if (registrationBox.getItemCount() > 0) {
            loadModulesForRegistration(moduleTableModel, registrationBox.getItemAt(0));
        }
        
        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
        buttonPanel.setBackground(Color.WHITE);
        
        JButton payButton = createStyledButton("💰 Process Payment", new Color(46, 204, 113));
        JButton selectAllButton = createStyledButton("☑ Select All Unpaid", new Color(52, 152, 219));
        JButton cancelButton = createStyledButton("❌ Cancel", new Color(231, 76, 60));
        
        selectAllButton.addActionListener(e -> {
            for (int i = 0; i < moduleTableModel.getRowCount(); i++) {
                if ("Unpaid".equals(moduleTableModel.getValueAt(i, 5))) {
                    moduleTableModel.setValueAt(true, i, 0);
                }
            }
        });
        
        payButton.addActionListener(e -> {
            RegistrationItem item = (RegistrationItem) registrationBox.getSelectedItem();
            if (item == null) return;
            
            List<Integer> selectedModules = new ArrayList<>();
            int totalAmount = 0;
            
            for (int i = 0; i < moduleTableModel.getRowCount(); i++) {
                Boolean checked = (Boolean) moduleTableModel.getValueAt(i, 0);
                if (checked != null && checked) {
                    selectedModules.add((Integer) moduleTableModel.getValueAt(i, 1));
                    totalAmount += (Integer) moduleTableModel.getValueAt(i, 3);
                }
            }
            
            if (selectedModules.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Please select at least one module!", "No Selection", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            int confirm = JOptionPane.showConfirmDialog(dialog,
                String.format("Process payment of Rs. %d for %d module(s)?", totalAmount, selectedModules.size()),
                "Confirm Payment",
                JOptionPane.YES_NO_OPTION);
            
            if (confirm == JOptionPane.YES_OPTION) {
                processPayment(item, selectedModules, dialog);
            }
        });
        
        cancelButton.addActionListener(e -> dialog.dispose());
        
        buttonPanel.add(selectAllButton);
        buttonPanel.add(payButton);
        buttonPanel.add(cancelButton);
        
        // Layout
        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        centerPanel.setBackground(Color.WHITE);
        centerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        centerPanel.add(modulePanel, BorderLayout.CENTER);
        centerPanel.add(summaryPanel, BorderLayout.SOUTH);
        
        dialog.add(topPanel, BorderLayout.NORTH);
        dialog.add(centerPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.setVisible(true);
    }
    
    private void loadRegistrations(JComboBox<RegistrationItem> box) {
        try {
            Connection conn = DatabaseUtil.getInstance().getConnection();
            
            String query = 
                "SELECT r.s_id, r.c_id, s.s_name, c.c_name " +
                "FROM registration r " +
                "INNER JOIN student s ON r.s_id = s.s_id " +
                "INNER JOIN course c ON r.c_id = c.c_id " +
                "ORDER BY s.s_name, c.c_name";
            
            ResultSet rs = conn.createStatement().executeQuery(query);
            
            while (rs.next()) {
                box.addItem(new RegistrationItem(
                    rs.getInt("s_id"),
                    rs.getInt("c_id"),
                    rs.getString("s_name"),
                    rs.getString("c_name")
                ));
            }
            
            rs.close();
            conn.close();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void loadModulesForRegistration(DefaultTableModel model, RegistrationItem item) {
        model.setRowCount(0);
        
        try {
            Connection conn = DatabaseUtil.getInstance().getConnection();
            
            String query = 
                "SELECT m.m_id, m.m_name, m.cost, t.t_name, " +
                "CASE WHEN l.paid = 'yes' THEN 'Paid' ELSE 'Unpaid' END as status " +
                "FROM modules m " +
                "LEFT JOIN teacher t ON m.m_id = t.m_id " +
                "LEFT JOIN learning l ON l.m_id = m.m_id AND l.s_id = ? " +
                "WHERE m.c_id = ? " +
                "ORDER BY m.m_name";
            
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, item.getStudentId());
            stmt.setInt(2, item.getCourseId());
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                String status = rs.getString("status");
                boolean isPaid = "Paid".equals(status);
                
                model.addRow(new Object[]{
                    !isPaid,
                    rs.getInt("m_id"),
                    rs.getString("m_name"),
                    rs.getInt("cost"),
                    rs.getString("t_name") != null ? rs.getString("t_name") : "No Teacher",
                    status
                });
            }
            
            rs.close();
            stmt.close();
            conn.close();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void processPayment(RegistrationItem item, List<Integer> moduleIds, JDialog dialog) {
        try {
            Connection conn = DatabaseUtil.getInstance().getConnection();
            conn.setAutoCommit(false);
            
            String sql = "INSERT INTO learning (s_id, c_id, m_id, paid) VALUES (?, ?, ?, 'yes')";
            PreparedStatement stmt = conn.prepareStatement(sql);
            
            int successCount = 0;
            for (int moduleId : moduleIds) {
                stmt.setInt(1, item.getStudentId());
                stmt.setInt(2, item.getCourseId());
                stmt.setInt(3, moduleId);
                
                try {
                    stmt.executeUpdate();
                    successCount++;
                } catch (SQLException e) {
                    // Module already paid, skip
                }
            }
            
            conn.commit();
            stmt.close();
            conn.close();
            
            JOptionPane.showMessageDialog(dialog,
                String.format("Payment processed successfully!\n%d module(s) paid.", successCount),
                "Success",
                JOptionPane.INFORMATION_MESSAGE);
            
            dialog.dispose();
            loadPaymentSummary();
            
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(dialog,
                "Error processing payment: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void showPaymentHistory() {
        JDialog dialog = new JDialog(this, "Payment History", true);
        dialog.setSize(1000, 600);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());
        
        String[] columns = {"Date", "Student", "Course", "Module", "Amount (Rs.)", "Teacher"};
        DefaultTableModel historyModel = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int row, int column) { return false; }
        };
        
        JTable historyTable = new JTable(historyModel);
        historyTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        historyTable.setRowHeight(30);
        historyTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        historyTable.getTableHeader().setBackground(new Color(52, 73, 94));
        historyTable.getTableHeader().setForeground(Color.WHITE);
        
        try {
            Connection conn = DatabaseUtil.getInstance().getConnection();
            
            String query = 
                "SELECT l.payment_date, s.s_name, c.c_name, m.m_name, m.cost, t.t_name " +
                "FROM learning l " +
                "INNER JOIN student s ON l.s_id = s.s_id " +
                "INNER JOIN course c ON l.c_id = c.c_id " +
                "INNER JOIN modules m ON l.m_id = m.m_id " +
                "LEFT JOIN teacher t ON m.m_id = t.m_id " +
                "WHERE l.paid = 'yes' " +
                "ORDER BY l.payment_date DESC";
            
            ResultSet rs = conn.createStatement().executeQuery(query);
            
            while (rs.next()) {
                historyModel.addRow(new Object[]{
                    rs.getString("payment_date"),
                    rs.getString("s_name"),
                    rs.getString("c_name"),
                    rs.getString("m_name"),
                    rs.getInt("cost"),
                    rs.getString("t_name") != null ? rs.getString("t_name") : "No Teacher"
                });
            }
            
            rs.close();
            conn.close();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        dialog.add(new JScrollPane(historyTable));
        dialog.setVisible(true);
    }
    
    // Helper classes
    private static class RegistrationItem {
        private int studentId, courseId;
        private String studentName, courseName;
        
        public RegistrationItem(int studentId, int courseId, String studentName, String courseName) {
            this.studentId = studentId;
            this.courseId = courseId;
            this.studentName = studentName;
            this.courseName = courseName;
        }
        
        public int getStudentId() { return studentId; }
        public int getCourseId() { return courseId; }
        
        @Override
        public String toString() {
            return studentName + " → " + courseName;
        }
    }
}
