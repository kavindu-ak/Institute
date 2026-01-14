import java.awt.*;
import java.sql.*;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import lib.DatabaseUtil;

public class MySalaryGUI extends JDialog {
    
    private String teacherUsername;
    private JTable salaryHistoryTable;
    private DefaultTableModel historyTableModel;
    private JTable studentPaymentsTable;
    private DefaultTableModel paymentsTableModel;
    
    public MySalaryGUI(JFrame parent, String username) {
        super(parent, "My Salary Information", true);
        this.teacherUsername = username;
        
        setSize(1100, 700);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));
        
        // Header
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(39, 60, 117));
        headerPanel.setPreferredSize(new Dimension(0, 80));
        headerPanel.setLayout(new BorderLayout());
        
        JPanel titlePanel = new JPanel();
        titlePanel.setBackground(new Color(39, 60, 117));
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        
        JLabel titleLabel = new JLabel("💵 My Salary Information");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel userLabel = new JLabel("Teacher: " + username);
        userLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        userLabel.setForeground(new Color(200, 200, 200));
        userLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        titlePanel.add(Box.createVerticalGlue());
        titlePanel.add(titleLabel);
        titlePanel.add(Box.createVerticalStrut(5));
        titlePanel.add(userLabel);
        titlePanel.add(Box.createVerticalGlue());
        
        headerPanel.add(titlePanel, BorderLayout.CENTER);
        
        // Summary Panel
        JPanel summaryPanel = createSummaryPanel();
        
        // Create tabbed pane for different views
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        
        // Tab 1: Salary History
        JPanel salaryHistoryPanel = createSalaryHistoryPanel();
        tabbedPane.addTab("📜 Salary History", salaryHistoryPanel);
        
        // Tab 2: Student Payments
        JPanel studentPaymentsPanel = createStudentPaymentsPanel();
        tabbedPane.addTab("👥 Student Payments", studentPaymentsPanel);
        
        // Tab 3: Revenue Details
        JPanel revenuePanel = createRevenueDetailsPanel();
        tabbedPane.addTab("💰 Revenue Details", revenuePanel);
        
        // Main content
        JPanel mainPanel = new JPanel(new BorderLayout(0, 10));
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        mainPanel.add(summaryPanel, BorderLayout.NORTH);
        mainPanel.add(tabbedPane, BorderLayout.CENTER);
        
        add(headerPanel, BorderLayout.NORTH);
        add(mainPanel, BorderLayout.CENTER);
        
        loadData();
    }
    
    private JPanel createSummaryPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 4, 10, 0));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        
        // These will be updated with actual data
        JPanel totalEarnedCard = createSummaryCard("Total Earned", "Rs. 0", new Color(46, 204, 113));
        JPanel studentsPaidCard = createSummaryCard("Students Paid", "0", new Color(52, 152, 219));
        JPanel pendingCard = createSummaryCard("Pending Salary", "Rs. 0", new Color(241, 196, 15));
        JPanel moduleCard = createSummaryCard("Teaching Module", "Loading...", new Color(155, 89, 182));
        
        panel.add(totalEarnedCard);
        panel.add(studentsPaidCard);
        panel.add(pendingCard);
        panel.add(moduleCard);
        
        return panel;
    }
    
    private JPanel createSummaryCard(String title, String value, Color color) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(color, 2),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        titleLabel.setForeground(new Color(100, 100, 100));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        valueLabel.setForeground(color);
        valueLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        card.add(titleLabel);
        card.add(Box.createVerticalStrut(5));
        card.add(valueLabel);
        
        return card;
    }
    
    private JPanel createSalaryHistoryPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        String[] columns = {"Payment Date", "Module", "Students", "Total Revenue", "Salary (93%)", "Status"};
        historyTableModel = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int row, int column) { return false; }
        };
        
        salaryHistoryTable = new JTable(historyTableModel);
        styleTable(salaryHistoryTable);
        
        JScrollPane scrollPane = new JScrollPane(salaryHistoryTable);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createStudentPaymentsPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        String[] columns = {"Student ID", "Student Name", "Course", "Payment Date", "Amount", "Status"};
        paymentsTableModel = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int row, int column) { return false; }
        };
        
        studentPaymentsTable = new JTable(paymentsTableModel);
        styleTable(studentPaymentsTable);
        
        JScrollPane scrollPane = new JScrollPane(studentPaymentsTable);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createRevenueDetailsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JTextArea detailsArea = new JTextArea();
        detailsArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        detailsArea.setEditable(false);
        detailsArea.setLineWrap(true);
        detailsArea.setWrapStyleWord(true);
        
        JScrollPane scrollPane = new JScrollPane(detailsArea);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Load revenue details
        loadRevenueDetails(detailsArea);
        
        return panel;
    }
    
    private void loadData() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DatabaseUtil.getInstance().getConnection();
            
            // Get teacher information by username (matching t_name)
            String teacherQuery = "SELECT t.t_id, t.t_name, m.m_name, m.m_id, m.cost, c.c_name " +
                                 "FROM teacher t " +
                                 "INNER JOIN modules m ON t.m_id = m.m_id " +
                                 "INNER JOIN course c ON m.c_id = c.c_id " +
                                 "WHERE t.t_name = ?";
            
            PreparedStatement teacherStmt = conn.prepareStatement(teacherQuery);
            teacherStmt.setString(1, teacherUsername);
            ResultSet teacherRs = teacherStmt.executeQuery();
            
            if (!teacherRs.next()) {
                JLabel noDataLabel = new JLabel("No teacher record found for: " + teacherUsername, SwingConstants.CENTER);
                noDataLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
                JPanel panel = new JPanel(new BorderLayout());
                panel.add(noDataLabel, BorderLayout.CENTER);
                setContentPane(panel);
                teacherRs.close();
                teacherStmt.close();
                conn.close();
                return;
            }
            
            int teacherId = teacherRs.getInt("t_id");
            String moduleName = teacherRs.getString("m_name");
            int moduleId = teacherRs.getInt("m_id");
            int moduleCost = teacherRs.getInt("cost");
            String courseName = teacherRs.getString("c_name");
            
            teacherRs.close();
            teacherStmt.close();
            
            // Update summary cards
            updateSummaryCards(conn, teacherId, moduleId, moduleName, moduleCost);
            
            // Load salary history
            loadSalaryHistory(conn, teacherId);
            
            // Load student payments
            loadStudentPayments(conn, moduleId, moduleCost);
            
            conn.close();
            
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                "Error loading data: " + e.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void updateSummaryCards(Connection conn, int teacherId, int moduleId, String moduleName, int moduleCost) throws SQLException {
        // Get total earned (sum of all salary payments)
        String totalQuery = "SELECT COALESCE(SUM(salary), 0) as total_earned FROM salary WHERE t_id = ? AND paid = 'yes'";
        PreparedStatement totalStmt = conn.prepareStatement(totalQuery);
        totalStmt.setInt(1, teacherId);
        ResultSet totalRs = totalStmt.executeQuery();
        int totalEarned = 0;
        if (totalRs.next()) {
            totalEarned = totalRs.getInt("total_earned");
        }
        totalRs.close();
        totalStmt.close();
        
        // Get students paid count
        String countQuery = "SELECT COUNT(DISTINCT s_id) as student_count FROM learning WHERE m_id = ? AND paid = 'yes'";
        PreparedStatement countStmt = conn.prepareStatement(countQuery);
        countStmt.setInt(1, moduleId);
        ResultSet countRs = countStmt.executeQuery();
        int studentsPaid = 0;
        if (countRs.next()) {
            studentsPaid = countRs.getInt("student_count");
        }
        countRs.close();
        countStmt.close();
        
        // Calculate pending (revenue not yet paid as salary)
        int totalRevenue = studentsPaid * moduleCost;
        int pendingSalary = totalRevenue - totalEarned;
        
        // Update the summary panel
        Component[] cards = ((JPanel) ((JPanel) getContentPane().getComponent(1)).getComponent(0)).getComponents();
        
        // Update values in cards
        updateCardValue((JPanel) cards[0], String.format("Rs. %,d", totalEarned));
        updateCardValue((JPanel) cards[1], String.valueOf(studentsPaid));
        updateCardValue((JPanel) cards[2], String.format("Rs. %,d", pendingSalary));
        updateCardValue((JPanel) cards[3], moduleName);
    }
    
    private void updateCardValue(JPanel card, String value) {
        Component[] components = card.getComponents();
        for (Component comp : components) {
            if (comp instanceof JLabel) {
                JLabel label = (JLabel) comp;
                if (label.getFont().getSize() == 20) { // Value label
                    label.setText(value);
                }
            }
        }
    }
    
    private void loadSalaryHistory(Connection conn, int teacherId) throws SQLException {
        String query = "SELECT s.payment_date, m.m_name, s.student_count, s.total_revenue, s.salary, s.paid " +
                      "FROM salary s " +
                      "INNER JOIN teacher t ON s.t_id = t.t_id " +
                      "INNER JOIN modules m ON t.m_id = m.m_id " +
                      "WHERE s.t_id = ? " +
                      "ORDER BY s.payment_date DESC";
        
        PreparedStatement stmt = conn.prepareStatement(query);
        stmt.setInt(1, teacherId);
        ResultSet rs = stmt.executeQuery();
        
        while (rs.next()) {
            String status = "yes".equals(rs.getString("paid")) ? "✓ Paid" : "Pending";
            
            historyTableModel.addRow(new Object[]{
                rs.getString("payment_date"),
                rs.getString("m_name"),
                rs.getInt("student_count"),
                String.format("Rs. %,d", rs.getInt("total_revenue")),
                String.format("Rs. %,d", rs.getInt("salary")),
                status
            });
        }
        
        if (historyTableModel.getRowCount() == 0) {
            historyTableModel.addRow(new Object[]{
                "No salary payments yet", "", "", "", "", ""
            });
        }
        
        rs.close();
        stmt.close();
    }
    
    private void loadStudentPayments(Connection conn, int moduleId, int moduleCost) throws SQLException {
        String query = "SELECT l.s_id, s.s_name, c.c_name, l.payment_date, l.paid " +
                      "FROM learning l " +
                      "INNER JOIN student s ON l.s_id = s.s_id " +
                      "INNER JOIN course c ON l.c_id = c.c_id " +
                      "WHERE l.m_id = ? " +
                      "ORDER BY l.payment_date DESC";
        
        PreparedStatement stmt = conn.prepareStatement(query);
        stmt.setInt(1, moduleId);
        ResultSet rs = stmt.executeQuery();
        
        while (rs.next()) {
            String status = "yes".equals(rs.getString("paid")) ? "✓ Paid" : "Unpaid";
            
            paymentsTableModel.addRow(new Object[]{
                rs.getInt("s_id"),
                rs.getString("s_name"),
                rs.getString("c_name"),
                rs.getString("payment_date"),
                String.format("Rs. %,d", moduleCost),
                status
            });
        }
        
        if (paymentsTableModel.getRowCount() == 0) {
            paymentsTableModel.addRow(new Object[]{
                "", "No student payments yet", "", "", "", ""
            });
        }
        
        rs.close();
        stmt.close();
    }
    
    private void loadRevenueDetails(JTextArea textArea) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DatabaseUtil.getInstance().getConnection();
            
            // Get teacher info
            String teacherQuery = "SELECT t.t_id, t.t_name, m.m_name, m.m_id, m.cost, c.c_name " +
                                 "FROM teacher t " +
                                 "INNER JOIN modules m ON t.m_id = m.m_id " +
                                 "INNER JOIN course c ON m.c_id = c.c_id " +
                                 "WHERE t.t_name = ?";
            
            PreparedStatement stmt = conn.prepareStatement(teacherQuery);
            stmt.setString(1, teacherUsername);
            ResultSet rs = stmt.executeQuery();
            
            if (!rs.next()) {
                textArea.setText("No teacher record found.");
                rs.close();
                stmt.close();
                conn.close();
                return;
            }
            
            int teacherId = rs.getInt("t_id");
            String moduleName = rs.getString("m_name");
            int moduleId = rs.getInt("m_id");
            int moduleCost = rs.getInt("cost");
            String courseName = rs.getString("c_name");
            
            rs.close();
            stmt.close();
            
            // Get payment statistics
            String statsQuery = "SELECT " +
                               "COUNT(CASE WHEN paid = 'yes' THEN 1 END) as paid_count, " +
                               "COUNT(CASE WHEN paid != 'yes' OR paid IS NULL THEN 1 END) as unpaid_count " +
                               "FROM learning WHERE m_id = ?";
            
            PreparedStatement statsStmt = conn.prepareStatement(statsQuery);
            statsStmt.setInt(1, moduleId);
            ResultSet statsRs = statsStmt.executeQuery();
            
            int paidCount = 0;
            int unpaidCount = 0;
            
            if (statsRs.next()) {
                paidCount = statsRs.getInt("paid_count");
                unpaidCount = statsRs.getInt("unpaid_count");
            }
            
            statsRs.close();
            statsStmt.close();
            
            int totalRevenue = paidCount * moduleCost;
            int teacherShare = (int)(totalRevenue * 0.93);
            int instituteShare = totalRevenue - teacherShare;
            
            // Build detailed text
            StringBuilder details = new StringBuilder();
            details.append("═══════════════════════════════════════════════════════\n");
            details.append("                REVENUE DETAILS\n");
            details.append("═══════════════════════════════════════════════════════\n\n");
            
            details.append("Teacher: ").append(teacherUsername).append("\n");
            details.append("Module: ").append(moduleName).append("\n");
            details.append("Course: ").append(courseName).append("\n");
            details.append("Module Cost: Rs. ").append(String.format("%,d", moduleCost)).append(" per student\n\n");
            
            details.append("───────────────────────────────────────────────────────\n");
            details.append("STUDENT PAYMENT STATISTICS\n");
            details.append("───────────────────────────────────────────────────────\n\n");
            
            details.append("Students Who Paid: ").append(paidCount).append("\n");
            details.append("Students Unpaid: ").append(unpaidCount).append("\n");
            details.append("Total Students Enrolled: ").append(paidCount + unpaidCount).append("\n\n");
            
            details.append("───────────────────────────────────────────────────────\n");
            details.append("REVENUE BREAKDOWN (93% FORMULA)\n");
            details.append("───────────────────────────────────────────────────────\n\n");
            
            details.append("Total Revenue Generated: Rs. ").append(String.format("%,d", totalRevenue)).append("\n");
            details.append("  (").append(paidCount).append(" students × Rs. ").append(String.format("%,d", moduleCost)).append(")\n\n");
            
            details.append("Your Share (93%): Rs. ").append(String.format("%,d", teacherShare)).append("\n");
            details.append("Institute Share (7%): Rs. ").append(String.format("%,d", instituteShare)).append("\n\n");
            
            details.append("═══════════════════════════════════════════════════════\n\n");
            
            details.append("Note: Your salary is calculated as 93% of the total revenue\n");
            details.append("from students who have paid for your module. The remaining\n");
            details.append("7% goes to the institute for administrative costs.\n\n");
            
            details.append("Salary payments are processed by the administration and will\n");
            details.append("appear in your Salary History tab once completed.\n");
            
            textArea.setText(details.toString());
            
            conn.close();
            
        } catch (Exception e) {
            e.printStackTrace();
            textArea.setText("Error loading revenue details: " + e.getMessage());
        }
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