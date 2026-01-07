import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import lib.DatabaseUtil;

public class SalaryManagementGUI extends JDialog {
    
    private JTable salaryTable;
    private DefaultTableModel tableModel;
    private JFrame parent;
    
    public SalaryManagementGUI(JFrame parent) {
        super(parent, "Salary Management", true);
        this.parent = parent;
        
        setSize(1200, 700);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));
        
        // Header
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(39, 60, 117));
        headerPanel.setPreferredSize(new Dimension(0, 60));
        JLabel titleLabel = new JLabel("💵 Teacher Salary Management (93% Formula)");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel);
        
        // Info panel
        JPanel infoPanel = new JPanel(new GridLayout(1, 4, 10, 0));
        infoPanel.setBackground(new Color(236, 240, 241));
        infoPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JLabel formula = createInfoLabel("💡 Formula", "93% to Teachers, 7% to Institute");
        JLabel eligible = createInfoLabel("✓ Eligible", "Teachers with paid students");
        JLabel status = createInfoLabel("📊 Status", "Real-time calculations");
        JLabel note = createInfoLabel("⚠ Note", "Based on actual payments");
        
        infoPanel.add(formula);
        infoPanel.add(eligible);
        infoPanel.add(status);
        infoPanel.add(note);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        buttonPanel.setBackground(Color.WHITE);
        
        JButton processButton = createStyledButton("💰 Process Salary", new Color(46, 204, 113));
        JButton historyButton = createStyledButton("📜 Salary History", new Color(52, 152, 219));
        JButton revenueButton = createStyledButton("📊 Revenue Details", new Color(155, 89, 182));
        JButton refreshButton = createStyledButton("🔄 Refresh", new Color(243, 156, 18));
        
        processButton.addActionListener(e -> processSalaryForSelected());
        historyButton.addActionListener(e -> showSalaryHistory());
        revenueButton.addActionListener(e -> showRevenueDetails());
        refreshButton.addActionListener(e -> loadTeacherSalaries());
        
        buttonPanel.add(processButton);
        buttonPanel.add(historyButton);
        buttonPanel.add(revenueButton);
        buttonPanel.add(refreshButton);
        
        // Table
        String[] columns = {"T_ID", "Teacher", "Module", "Students Paid", "Revenue (Rs.)", "Salary 93% (Rs.)", "Institute 7% (Rs.)", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        salaryTable = new JTable(tableModel);
        salaryTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        salaryTable.setRowHeight(30);
        salaryTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        salaryTable.getTableHeader().setBackground(new Color(52, 73, 94));
        salaryTable.getTableHeader().setForeground(Color.WHITE);
        salaryTable.setSelectionBackground(new Color(52, 152, 219));
        salaryTable.setSelectionForeground(Color.WHITE);
        
        JScrollPane scrollPane = new JScrollPane(salaryTable);
        
        // Main panel
        JPanel mainPanel = new JPanel(new BorderLayout(0, 10));
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        mainPanel.add(buttonPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Top container
        JPanel topContainer = new JPanel(new BorderLayout());
        topContainer.add(headerPanel, BorderLayout.NORTH);
        topContainer.add(infoPanel, BorderLayout.CENTER);
        
        add(topContainer, BorderLayout.NORTH);
        add(mainPanel, BorderLayout.CENTER);
        
        loadTeacherSalaries();
    }
    
    private JLabel createInfoLabel(String title, String text) {
        JLabel label = new JLabel(
            "<html><div style='text-align: center;'><b>" + title + "</b><br/>" +
            "<span style='font-size: 10px; color: #666;'>" + text + "</span></div></html>",
            SwingConstants.CENTER
        );
        label.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        label.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1));
        label.setOpaque(true);
        label.setBackground(Color.WHITE);
        return label;
    }
    
    private JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(160, 35));
        return button;
    }
    
    private void loadTeacherSalaries() {
        tableModel.setRowCount(0);
        
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DatabaseUtil.getInstance().getConnection();
            
            String query = 
                "SELECT t.t_id, t.t_name, m.m_name, " +
                "COUNT(CASE WHEN l.paid = 'yes' THEN 1 END) as student_count, " +
                "COALESCE(SUM(CASE WHEN l.paid = 'yes' THEN m.cost ELSE 0 END), 0) as total_revenue, " +
                "t.paid as salary_paid " +
                "FROM teacher t " +
                "INNER JOIN modules m ON t.m_id = m.m_id " +
                "LEFT JOIN learning l ON l.m_id = t.m_id " +
                "GROUP BY t.t_id, t.t_name, m.m_name, t.paid " +
                "ORDER BY t.t_id";
            
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            
            while (rs.next()) {
                int studentCount = rs.getInt("student_count");
                int totalRevenue = rs.getInt("total_revenue");
                int salary = (int)(totalRevenue * 0.93);
                int instituteShare = totalRevenue - salary;
                String status = "yes".equals(rs.getString("salary_paid")) ? "✓ Paid" : 
                               (totalRevenue > 0 ? "⏳ Pending" : "⊘ No Revenue");
                
                Object[] row = {
                    rs.getInt("t_id"),
                    rs.getString("t_name"),
                    rs.getString("m_name"),
                    studentCount,
                    totalRevenue,
                    salary,
                    instituteShare,
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
                "Error loading teacher salaries: " + e.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void processSalaryForSelected() {
        int selectedRow = salaryTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                "Please select a teacher to process salary!",
                "No Selection",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int teacherId = (int) tableModel.getValueAt(selectedRow, 0);
        String teacherName = (String) tableModel.getValueAt(selectedRow, 1);
        String moduleName = (String) tableModel.getValueAt(selectedRow, 2);
        int studentCount = (int) tableModel.getValueAt(selectedRow, 3);
        int totalRevenue = (int) tableModel.getValueAt(selectedRow, 4);
        int salary = (int) tableModel.getValueAt(selectedRow, 5);
        int instituteShare = (int) tableModel.getValueAt(selectedRow, 6);
        String status = (String) tableModel.getValueAt(selectedRow, 7);
        
        if (totalRevenue == 0) {
            JOptionPane.showMessageDialog(this,
                "No revenue to process! This teacher has no paid students yet.",
                "No Revenue",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        if ("✓ Paid".equals(status)) {
            int confirm = JOptionPane.showConfirmDialog(this,
                "Salary already processed for this teacher. Process again?",
                "Already Paid",
                JOptionPane.YES_NO_OPTION);
            
            if (confirm != JOptionPane.YES_OPTION) {
                return;
            }
        }
        
        // Show detailed confirmation
        String message = String.format(
            "<html><body style='width: 400px;'>" +
            "<h2 style='color: #2c3e50;'>Salary Payment Confirmation</h2>" +
            "<table style='width: 100%%;'>" +
            "<tr><td><b>Teacher:</b></td><td>%s</td></tr>" +
            "<tr><td><b>Module:</b></td><td>%s</td></tr>" +
            "<tr><td><b>Students Paid:</b></td><td>%d</td></tr>" +
            "<tr><td colspan='2'><hr></td></tr>" +
            "<tr><td><b>Total Revenue:</b></td><td>Rs. %,d</td></tr>" +
            "<tr><td><b>Teacher Salary (93%%):</b></td><td style='color: #27ae60;'><b>Rs. %,d</b></td></tr>" +
            "<tr><td><b>Institute Share (7%%):</b></td><td>Rs. %,d</td></tr>" +
            "</table>" +
            "<br><p style='color: #7f8c8d;'><i>Process this salary payment?</i></p>" +
            "</body></html>",
            teacherName, moduleName, studentCount, totalRevenue, salary, instituteShare
        );
        
        int confirm = JOptionPane.showConfirmDialog(this,
            message,
            "Confirm Salary Payment",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            processSalary(teacherId, salary, studentCount, totalRevenue);
        }
    }
    
    private void processSalary(int teacherId, int salary, int studentCount, int totalRevenue) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DatabaseUtil.getInstance().getConnection();
            conn.setAutoCommit(false);
            
            try {
                // Insert salary record
                String insertSql = "INSERT INTO salary (t_id, salary, paid, student_count, total_revenue) VALUES (?, ?, 'yes', ?, ?)";
                PreparedStatement insertStmt = conn.prepareStatement(insertSql);
                insertStmt.setInt(1, teacherId);
                insertStmt.setInt(2, salary);
                insertStmt.setInt(3, studentCount);
                insertStmt.setInt(4, totalRevenue);
                insertStmt.executeUpdate();
                insertStmt.close();
                
                // Update teacher paid status
                String updateSql = "UPDATE teacher SET paid = 'yes' WHERE t_id = ?";
                PreparedStatement updateStmt = conn.prepareStatement(updateSql);
                updateStmt.setInt(1, teacherId);
                updateStmt.executeUpdate();
                updateStmt.close();
                
                conn.commit();
                
                JOptionPane.showMessageDialog(this,
                    String.format("Salary processed successfully!\nAmount paid: Rs. %,d", salary),
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);
                
                loadTeacherSalaries();
                
            } catch (Exception e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
                conn.close();
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                "Error processing salary: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void showSalaryHistory() {
        JDialog dialog = new JDialog(this, "Salary Payment History", true);
        dialog.setSize(1100, 600);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());
        
        // Header
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(52, 73, 94));
        headerPanel.setPreferredSize(new Dimension(0, 50));
        JLabel titleLabel = new JLabel("📜 Complete Salary Payment History");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel);
        
        String[] columns = {"T_ID", "Teacher", "Module", "Students", "Revenue (Rs.)", "Salary Paid (Rs.)", "Date"};
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
                "SELECT s.t_id, t.t_name, m.m_name, s.student_count, " +
                "s.total_revenue, s.salary, s.payment_date " +
                "FROM salary s " +
                "INNER JOIN teacher t ON s.t_id = t.t_id " +
                "INNER JOIN modules m ON t.m_id = m.m_id " +
                "WHERE s.paid = 'yes' " +
                "ORDER BY s.payment_date DESC";
            
            ResultSet rs = conn.createStatement().executeQuery(query);
            
            int totalSalaries = 0;
            int totalRevenue = 0;
            
            while (rs.next()) {
                int salary = rs.getInt("salary");
                int revenue = rs.getInt("total_revenue");
                totalSalaries += salary;
                totalRevenue += revenue;
                
                historyModel.addRow(new Object[]{
                    rs.getInt("t_id"),
                    rs.getString("t_name"),
                    rs.getString("m_name"),
                    rs.getInt("student_count"),
                    revenue,
                    salary,
                    rs.getString("payment_date")
                });
            }
            
            rs.close();
            conn.close();
            
            // Summary panel
            JPanel summaryPanel = new JPanel(new GridLayout(1, 3, 10, 0));
            summaryPanel.setBackground(new Color(236, 240, 241));
            summaryPanel.setBorder(BorderFactory.createEmptyBorder(15, 10, 15, 10));
            
            JLabel paymentCount = createSummaryLabel("Total Payments", String.valueOf(historyModel.getRowCount()));
            JLabel totalRev = createSummaryLabel("Total Revenue", String.format("Rs. %,d", totalRevenue));
            JLabel totalSal = createSummaryLabel("Total Salaries Paid", String.format("Rs. %,d", totalSalaries));
            
            summaryPanel.add(paymentCount);
            summaryPanel.add(totalRev);
            summaryPanel.add(totalSal);
            
            JPanel mainPanel = new JPanel(new BorderLayout());
            mainPanel.add(new JScrollPane(historyTable), BorderLayout.CENTER);
            mainPanel.add(summaryPanel, BorderLayout.SOUTH);
            
            dialog.add(headerPanel, BorderLayout.NORTH);
            dialog.add(mainPanel, BorderLayout.CENTER);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        dialog.setVisible(true);
    }
    
    private JLabel createSummaryLabel(String title, String value) {
        JLabel label = new JLabel(
            "<html><div style='text-align: center;'><b>" + title + "</b><br/>" +
            "<span style='font-size: 16px; color: #27ae60;'>" + value + "</span></div></html>",
            SwingConstants.CENTER
        );
        label.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        label.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        label.setOpaque(true);
        label.setBackground(Color.WHITE);
        return label;
    }
    
    private void showRevenueDetails() {
        int selectedRow = salaryTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                "Please select a teacher to view revenue details!",
                "No Selection",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int teacherId = (int) tableModel.getValueAt(selectedRow, 0);
        String teacherName = (String) tableModel.getValueAt(selectedRow, 1);
        
        JDialog dialog = new JDialog(this, "Revenue Details - " + teacherName, true);
        dialog.setSize(900, 600);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());
        
        String[] columns = {"Student ID", "Student Name", "Payment Date", "Module Cost (Rs.)", "Status"};
        DefaultTableModel revenueModel = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int row, int column) { return false; }
        };
        
        JTable revenueTable = new JTable(revenueModel);
        revenueTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        revenueTable.setRowHeight(30);
        revenueTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        revenueTable.getTableHeader().setBackground(new Color(52, 73, 94));
        revenueTable.getTableHeader().setForeground(Color.WHITE);
        
        try {
            Connection conn = DatabaseUtil.getInstance().getConnection();
            
            String query = 
                "SELECT s.s_id, s.s_name, l.payment_date, m.cost, l.paid " +
                "FROM teacher t " +
                "INNER JOIN modules m ON t.m_id = m.m_id " +
                "LEFT JOIN learning l ON l.m_id = t.m_id " +
                "LEFT JOIN student s ON s.s_id = l.s_id " +
                "WHERE t.t_id = ? " +
                "ORDER BY l.payment_date DESC";
            
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, teacherId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                String status = "yes".equals(rs.getString("paid")) ? "✓ Paid" : "Unpaid";
                
                revenueModel.addRow(new Object[]{
                    rs.getInt("s_id"),
                    rs.getString("s_name"),
                    rs.getString("payment_date"),
                    rs.getInt("cost"),
                    status
                });
            }
            
            rs.close();
            stmt.close();
            conn.close();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        dialog.add(new JScrollPane(revenueTable));
        dialog.setVisible(true);
    }
}
