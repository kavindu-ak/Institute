import java.awt.*;
import java.sql.*;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import lib.DatabaseUtil;

public class ReportsGUI extends JDialog {
    
    private JFrame parent;
    
    public ReportsGUI(JFrame parent) {
        super(parent, "Reports & Statistics", true);
        this.parent = parent;
        
        setSize(1100, 700);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());
        
        // Header
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(39, 60, 117));
        headerPanel.setPreferredSize(new Dimension(0, 60));
        JLabel titleLabel = new JLabel("📊 Reports & Statistics");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel);
        
        // Center panel with report cards
        JPanel centerPanel = new JPanel(new GridLayout(2, 2, 15, 15));
        centerPanel.setBackground(Color.WHITE);
        centerPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        centerPanel.add(createReportCard("👥 Students", "View student statistics", e -> showStudentReport()));
        centerPanel.add(createReportCard("👨‍🏫 Teachers", "View teacher statistics", e -> showTeacherReport()));
        centerPanel.add(createReportCard("💰 Payments", "View payment reports", e -> showPaymentReport()));
        centerPanel.add(createReportCard("💵 Salaries", "View salary reports", e -> showSalaryReport()));
        
        add(headerPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
    }
    
    private JPanel createReportCard(String title, String desc, java.awt.event.ActionListener action) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        
        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        
        JLabel descLabel = new JLabel(desc, SwingConstants.CENTER);
        descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        descLabel.setForeground(new Color(100, 100, 100));
        
        JButton viewButton = new JButton("View Report");
        viewButton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        viewButton.setBackground(new Color(52, 152, 219));
        viewButton.setForeground(Color.WHITE);
        viewButton.setFocusPainted(false);
        viewButton.setBorderPainted(false);
        viewButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        viewButton.addActionListener(action);
        
        JPanel textPanel = new JPanel(new GridLayout(2, 1, 0, 10));
        textPanel.setBackground(Color.WHITE);
        textPanel.add(titleLabel);
        textPanel.add(descLabel);
        
        card.add(textPanel, BorderLayout.CENTER);
        card.add(viewButton, BorderLayout.SOUTH);
        
        return card;
    }
    
    // ========================================================================
    // STUDENT REPORT
    // ========================================================================
    private void showStudentReport() {
        JDialog reportDialog = new JDialog(this, "Student Statistics Report", true);
        reportDialog.setSize(1100, 650);
        reportDialog.setLocationRelativeTo(this);
        reportDialog.setLayout(new BorderLayout(10, 10));
        
        // Header
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(41, 128, 185));
        headerPanel.setPreferredSize(new Dimension(0, 50));
        JLabel titleLabel = new JLabel("👥 Student Statistics Report");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel);
        
        // Summary Panel
        JPanel summaryPanel = new JPanel(new GridLayout(1, 4, 10, 0));
        summaryPanel.setBackground(new Color(236, 240, 241));
        summaryPanel.setBorder(BorderFactory.createEmptyBorder(15, 10, 15, 10));
        
        try {
            Connection conn = DatabaseUtil.getInstance().getConnection();
            
            // Total Students
            ResultSet rs1 = conn.createStatement().executeQuery("SELECT COUNT(*) as total FROM student");
            int totalStudents = rs1.next() ? rs1.getInt("total") : 0;
            rs1.close();
            
            // Students with registrations
            ResultSet rs2 = conn.createStatement().executeQuery(
                "SELECT COUNT(DISTINCT s_id) as total FROM registration");
            int registeredStudents = rs2.next() ? rs2.getInt("total") : 0;
            rs2.close();
            
            // Total payments
            ResultSet rs3 = conn.createStatement().executeQuery(
                "SELECT COUNT(*) as total FROM learning WHERE paid = 'yes'");
            int totalPayments = rs3.next() ? rs3.getInt("total") : 0;
            rs3.close();
            
            // Total revenue from students
            ResultSet rs4 = conn.createStatement().executeQuery(
                "SELECT COALESCE(SUM(m.cost), 0) as total FROM learning l " +
                "INNER JOIN modules m ON l.m_id = m.m_id WHERE l.paid = 'yes'");
            int totalRevenue = rs4.next() ? rs4.getInt("total") : 0;
            rs4.close();
            
            conn.close();
            
            summaryPanel.add(createSummaryLabel("Total Students", String.valueOf(totalStudents)));
            summaryPanel.add(createSummaryLabel("Registered Students", String.valueOf(registeredStudents)));
            summaryPanel.add(createSummaryLabel("Total Payments", String.valueOf(totalPayments)));
            summaryPanel.add(createSummaryLabel("Total Revenue", String.format("Rs. %,d", totalRevenue)));
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // Table
        String[] columns = {"Student ID", "Name", "NIC", "Phone", "Courses", "Modules Paid", "Total Paid (Rs.)"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int row, int column) { return false; }
        };
        
        JTable table = new JTable(model);
        styleTable(table);
        
        // Load data
        try {
            Connection conn = DatabaseUtil.getInstance().getConnection();
            
            String query = 
                "SELECT s.s_id, s.s_name, s.nic, s.tp, " +
                "COUNT(DISTINCT r.c_id) as course_count, " +
                "COUNT(DISTINCT l.m_id) as module_count, " +
                "COALESCE(SUM(m.cost), 0) as total_paid " +
                "FROM student s " +
                "LEFT JOIN registration r ON s.s_id = r.s_id " +
                "LEFT JOIN learning l ON s.s_id = l.s_id AND l.paid = 'yes' " +
                "LEFT JOIN modules m ON l.m_id = m.m_id " +
                "GROUP BY s.s_id, s.s_name, s.nic, s.tp " +
                "ORDER BY s.s_name";
            
            ResultSet rs = conn.createStatement().executeQuery(query);
            
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("s_id"),
                    rs.getString("s_name"),
                    rs.getInt("nic"),
                    rs.getInt("tp"),
                    rs.getInt("course_count"),
                    rs.getInt("module_count"),
                    rs.getInt("total_paid")
                });
            }
            
            rs.close();
            conn.close();
            
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(reportDialog, "Error loading data: " + e.getMessage());
        }
        
        JPanel mainPanel = new JPanel(new BorderLayout(0, 10));
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        mainPanel.add(summaryPanel, BorderLayout.NORTH);
        mainPanel.add(new JScrollPane(table), BorderLayout.CENTER);
        
        reportDialog.add(headerPanel, BorderLayout.NORTH);
        reportDialog.add(mainPanel, BorderLayout.CENTER);
        reportDialog.setVisible(true);
    }
    
    // ========================================================================
    // TEACHER REPORT
    // ========================================================================
    private void showTeacherReport() {
        JDialog reportDialog = new JDialog(this, "Teacher Statistics Report", true);
        reportDialog.setSize(1100, 650);
        reportDialog.setLocationRelativeTo(this);
        reportDialog.setLayout(new BorderLayout(10, 10));
        
        // Header
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(41, 128, 185));
        headerPanel.setPreferredSize(new Dimension(0, 50));
        JLabel titleLabel = new JLabel("👨‍🏫 Teacher Statistics Report");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel);
        
        // Summary Panel
        JPanel summaryPanel = new JPanel(new GridLayout(1, 4, 10, 0));
        summaryPanel.setBackground(new Color(236, 240, 241));
        summaryPanel.setBorder(BorderFactory.createEmptyBorder(15, 10, 15, 10));
        
        try {
            Connection conn = DatabaseUtil.getInstance().getConnection();
            
            // Total Teachers
            ResultSet rs1 = conn.createStatement().executeQuery("SELECT COUNT(*) as total FROM teacher");
            int totalTeachers = rs1.next() ? rs1.getInt("total") : 0;
            rs1.close();
            
            // Teachers paid
            ResultSet rs2 = conn.createStatement().executeQuery(
                "SELECT COUNT(*) as total FROM teacher WHERE paid = 'yes'");
            int teachersPaid = rs2.next() ? rs2.getInt("total") : 0;
            rs2.close();
            
            // Total salary paid
            ResultSet rs3 = conn.createStatement().executeQuery(
                "SELECT COALESCE(SUM(salary), 0) as total FROM salary WHERE paid = 'yes'");
            int totalSalary = rs3.next() ? rs3.getInt("total") : 0;
            rs3.close();
            
            // Total students taught
            ResultSet rs4 = conn.createStatement().executeQuery(
                "SELECT COUNT(DISTINCT l.s_id) as total FROM learning l " +
                "INNER JOIN teacher t ON l.m_id = t.m_id WHERE l.paid = 'yes'");
            int totalStudents = rs4.next() ? rs4.getInt("total") : 0;
            rs4.close();
            
            conn.close();
            
            summaryPanel.add(createSummaryLabel("Total Teachers", String.valueOf(totalTeachers)));
            summaryPanel.add(createSummaryLabel("Teachers Paid", String.valueOf(teachersPaid)));
            summaryPanel.add(createSummaryLabel("Total Salary Paid", String.format("Rs. %,d", totalSalary)));
            summaryPanel.add(createSummaryLabel("Students Taught", String.valueOf(totalStudents)));
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // Table
        String[] columns = {"Teacher ID", "Name", "Module", "Course", "Students", "Revenue (Rs.)", "Salary (Rs.)", "Status"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int row, int column) { return false; }
        };
        
        JTable table = new JTable(model);
        styleTable(table);
        
        // Load data
        try {
            Connection conn = DatabaseUtil.getInstance().getConnection();
            
            String query = 
                "SELECT t.t_id, t.t_name, m.m_name, c.c_name, " +
                "COUNT(DISTINCT l.s_id) as student_count, " +
                "COALESCE(SUM(CASE WHEN l.paid = 'yes' THEN m.cost ELSE 0 END), 0) as revenue, " +
                "CAST(COALESCE(SUM(CASE WHEN l.paid = 'yes' THEN m.cost ELSE 0 END), 0) * 0.93 AS SIGNED) as salary, " +
                "t.paid " +
                "FROM teacher t " +
                "INNER JOIN modules m ON t.m_id = m.m_id " +
                "INNER JOIN course c ON m.c_id = c.c_id " +
                "LEFT JOIN learning l ON l.m_id = t.m_id " +
                "GROUP BY t.t_id, t.t_name, m.m_name, c.c_name, t.paid " +
                "ORDER BY t.t_name";
            
            ResultSet rs = conn.createStatement().executeQuery(query);
            
            while (rs.next()) {
                String status = "yes".equals(rs.getString("paid")) ? "✓ Paid" : "Pending";
                
                model.addRow(new Object[]{
                    rs.getInt("t_id"),
                    rs.getString("t_name"),
                    rs.getString("m_name"),
                    rs.getString("c_name"),
                    rs.getInt("student_count"),
                    rs.getInt("revenue"),
                    rs.getInt("salary"),
                    status
                });
            }
            
            rs.close();
            conn.close();
            
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(reportDialog, "Error loading data: " + e.getMessage());
        }
        
        JPanel mainPanel = new JPanel(new BorderLayout(0, 10));
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        mainPanel.add(summaryPanel, BorderLayout.NORTH);
        mainPanel.add(new JScrollPane(table), BorderLayout.CENTER);
        
        reportDialog.add(headerPanel, BorderLayout.NORTH);
        reportDialog.add(mainPanel, BorderLayout.CENTER);
        reportDialog.setVisible(true);
    }
    
    // ========================================================================
    // PAYMENT REPORT
    // ========================================================================
    private void showPaymentReport() {
        JDialog reportDialog = new JDialog(this, "Payment Statistics Report", true);
        reportDialog.setSize(1100, 650);
        reportDialog.setLocationRelativeTo(this);
        reportDialog.setLayout(new BorderLayout(10, 10));
        
        // Header
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(41, 128, 185));
        headerPanel.setPreferredSize(new Dimension(0, 50));
        JLabel titleLabel = new JLabel("💰 Payment Statistics Report");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel);
        
        // Summary Panel
        JPanel summaryPanel = new JPanel(new GridLayout(1, 4, 10, 0));
        summaryPanel.setBackground(new Color(236, 240, 241));
        summaryPanel.setBorder(BorderFactory.createEmptyBorder(15, 10, 15, 10));
        
        try {
            Connection conn = DatabaseUtil.getInstance().getConnection();
            
            // Total payments
            ResultSet rs1 = conn.createStatement().executeQuery(
                "SELECT COUNT(*) as total FROM learning WHERE paid = 'yes'");
            int totalPayments = rs1.next() ? rs1.getInt("total") : 0;
            rs1.close();
            
            // Total revenue
            ResultSet rs2 = conn.createStatement().executeQuery(
                "SELECT COALESCE(SUM(m.cost), 0) as total FROM learning l " +
                "INNER JOIN modules m ON l.m_id = m.m_id WHERE l.paid = 'yes'");
            int totalRevenue = rs2.next() ? rs2.getInt("total") : 0;
            rs2.close();
            
            // Pending payments
            ResultSet rs3 = conn.createStatement().executeQuery(
                "SELECT COUNT(*) as total FROM learning WHERE paid != 'yes' OR paid IS NULL");
            int pendingPayments = rs3.next() ? rs3.getInt("total") : 0;
            rs3.close();
            
            // Unique paying students
            ResultSet rs4 = conn.createStatement().executeQuery(
                "SELECT COUNT(DISTINCT s_id) as total FROM learning WHERE paid = 'yes'");
            int payingStudents = rs4.next() ? rs4.getInt("total") : 0;
            rs4.close();
            
            conn.close();
            
            summaryPanel.add(createSummaryLabel("Total Payments", String.valueOf(totalPayments)));
            summaryPanel.add(createSummaryLabel("Total Revenue", String.format("Rs. %,d", totalRevenue)));
            summaryPanel.add(createSummaryLabel("Pending Payments", String.valueOf(pendingPayments)));
            summaryPanel.add(createSummaryLabel("Paying Students", String.valueOf(payingStudents)));
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // Table
        String[] columns = {"Date", "Student", "Course", "Module", "Amount (Rs.)", "Teacher", "Status"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int row, int column) { return false; }
        };
        
        JTable table = new JTable(model);
        styleTable(table);
        
        // Load data
        try {
            Connection conn = DatabaseUtil.getInstance().getConnection();
            
            String query = 
                "SELECT l.payment_date, s.s_name, c.c_name, m.m_name, m.cost, t.t_name, l.paid " +
                "FROM learning l " +
                "INNER JOIN student s ON l.s_id = s.s_id " +
                "INNER JOIN course c ON l.c_id = c.c_id " +
                "INNER JOIN modules m ON l.m_id = m.m_id " +
                "LEFT JOIN teacher t ON m.m_id = t.m_id " +
                "ORDER BY l.payment_date DESC";
            
            ResultSet rs = conn.createStatement().executeQuery(query);
            
            while (rs.next()) {
                String status = "yes".equals(rs.getString("paid")) ? "✓ Paid" : "Unpaid";
                String teacher = rs.getString("t_name");
                
                model.addRow(new Object[]{
                    rs.getString("payment_date"),
                    rs.getString("s_name"),
                    rs.getString("c_name"),
                    rs.getString("m_name"),
                    rs.getInt("cost"),
                    teacher != null ? teacher : "No Teacher",
                    status
                });
            }
            
            rs.close();
            conn.close();
            
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(reportDialog, "Error loading data: " + e.getMessage());
        }
        
        JPanel mainPanel = new JPanel(new BorderLayout(0, 10));
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        mainPanel.add(summaryPanel, BorderLayout.NORTH);
        mainPanel.add(new JScrollPane(table), BorderLayout.CENTER);
        
        reportDialog.add(headerPanel, BorderLayout.NORTH);
        reportDialog.add(mainPanel, BorderLayout.CENTER);
        reportDialog.setVisible(true);
    }
    
    // ========================================================================
    // SALARY REPORT
    // ========================================================================
    private void showSalaryReport() {
        JDialog reportDialog = new JDialog(this, "Salary Statistics Report", true);
        reportDialog.setSize(1100, 650);
        reportDialog.setLocationRelativeTo(this);
        reportDialog.setLayout(new BorderLayout(10, 10));
        
        // Header
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(41, 128, 185));
        headerPanel.setPreferredSize(new Dimension(0, 50));
        JLabel titleLabel = new JLabel("💵 Salary Statistics Report (93% Formula)");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel);
        
        // Summary Panel
        JPanel summaryPanel = new JPanel(new GridLayout(1, 4, 10, 0));
        summaryPanel.setBackground(new Color(236, 240, 241));
        summaryPanel.setBorder(BorderFactory.createEmptyBorder(15, 10, 15, 10));
        
        try {
            Connection conn = DatabaseUtil.getInstance().getConnection();
            
            // Total salary payments
            ResultSet rs1 = conn.createStatement().executeQuery(
                "SELECT COUNT(*) as total FROM salary WHERE paid = 'yes'");
            int totalPayments = rs1.next() ? rs1.getInt("total") : 0;
            rs1.close();
            
            // Total salary paid
            ResultSet rs2 = conn.createStatement().executeQuery(
                "SELECT COALESCE(SUM(salary), 0) as total FROM salary WHERE paid = 'yes'");
            int totalSalary = rs2.next() ? rs2.getInt("total") : 0;
            rs2.close();
            
            // Total revenue from which salary calculated
            ResultSet rs3 = conn.createStatement().executeQuery(
                "SELECT COALESCE(SUM(total_revenue), 0) as total FROM salary WHERE paid = 'yes'");
            int totalRevenue = rs3.next() ? rs3.getInt("total") : 0;
            rs3.close();
            
            // Institute share (7%)
            int instituteShare = totalRevenue - totalSalary;
            
            conn.close();
            
            summaryPanel.add(createSummaryLabel("Total Payments", String.valueOf(totalPayments)));
            summaryPanel.add(createSummaryLabel("Total Salary Paid (93%)", String.format("Rs. %,d", totalSalary)));
            summaryPanel.add(createSummaryLabel("Total Revenue", String.format("Rs. %,d", totalRevenue)));
            summaryPanel.add(createSummaryLabel("Institute Share (7%)", String.format("Rs. %,d", instituteShare)));
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // Table
        String[] columns = {"Date", "Teacher", "Module", "Students", "Revenue (Rs.)", "Salary 93% (Rs.)", "Institute 7% (Rs.)"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int row, int column) { return false; }
        };
        
        JTable table = new JTable(model);
        styleTable(table);
        
        // Load data
        try {
            Connection conn = DatabaseUtil.getInstance().getConnection();
            
            String query = 
                "SELECT s.payment_date, t.t_name, m.m_name, s.student_count, " +
                "s.total_revenue, s.salary " +
                "FROM salary s " +
                "INNER JOIN teacher t ON s.t_id = t.t_id " +
                "INNER JOIN modules m ON t.m_id = m.m_id " +
                "WHERE s.paid = 'yes' " +
                "ORDER BY s.payment_date DESC";
            
            ResultSet rs = conn.createStatement().executeQuery(query);
            
            while (rs.next()) {
                int revenue = rs.getInt("total_revenue");
                int salary = rs.getInt("salary");
                int instituteShare = revenue - salary;
                
                model.addRow(new Object[]{
                    rs.getString("payment_date"),
                    rs.getString("t_name"),
                    rs.getString("m_name"),
                    rs.getInt("student_count"),
                    revenue,
                    salary,
                    instituteShare
                });
            }
            
            rs.close();
            conn.close();
            
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(reportDialog, "Error loading data: " + e.getMessage());
        }
        
        JPanel mainPanel = new JPanel(new BorderLayout(0, 10));
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        mainPanel.add(summaryPanel, BorderLayout.NORTH);
        mainPanel.add(new JScrollPane(table), BorderLayout.CENTER);
        
        reportDialog.add(headerPanel, BorderLayout.NORTH);
        reportDialog.add(mainPanel, BorderLayout.CENTER);
        reportDialog.setVisible(true);
    }
    
    // ========================================================================
    // HELPER METHODS
    // ========================================================================
    
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
}