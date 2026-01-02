import java.sql.*;
import java.util.*;
import lib.DatabaseUtil;

public class salary {
    
    static class teacher {
        int t_id;
        String t_name;
        int m_id;
        String m_name;
        int studentCount;
        int totalRevenue;
        int calculatedSalary;
        int institutionShare;

        teacher(int t_id, String t_name, int m_id, String m_name, int studentCount, int totalRevenue) {
            this.t_id = t_id;
            this.t_name = t_name;
            this.m_id = m_id;
            this.m_name = m_name;
            this.studentCount = studentCount;
            this.totalRevenue = totalRevenue;
            this.calculatedSalary = (int)(totalRevenue * 0.93); // 93% to teacher
            this.institutionShare = totalRevenue - calculatedSalary; // 7% to institution
        }

        @Override
        public String toString() {
            return String.format("| %-6d | %-25s | %-25s | %-8d | %-10d | %-12d | %-12d |", 
                t_id, t_name, m_name, studentCount, totalRevenue, calculatedSalary, institutionShare);
        }
    }

    static class salaryDetails {
        int t_id;
        int salary;
        String paid;
        int studentCount;
        int totalRevenue;

        public salaryDetails(int t_id, int salary, String paid, int studentCount, int totalRevenue) {
            this.t_id = t_id;
            this.salary = salary;
            this.paid = paid;
            this.studentCount = studentCount;
            this.totalRevenue = totalRevenue;
        }

        public int gett_id() { return t_id; }
        public int getsalary() { return salary; }
        public String getpaid() { return paid; }
        public int getstudentCount() { return studentCount; }
        public int gettotalRevenue() { return totalRevenue; }
    }

    public static void salary_teacher(Scanner scanner) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DatabaseUtil.getInstance().getConnection();

            // Fetch teachers with their module revenue (ONLY from paid students)
            List<teacher> teacherList = new ArrayList<>();
            
            String query = 
                "SELECT t.t_id, t.t_name, t.m_id, m.m_name, " +
                "COUNT(CASE WHEN l.paid = 'yes' THEN 1 END) as student_count, " +
                "COALESCE(SUM(CASE WHEN l.paid = 'yes' THEN m.cost ELSE 0 END), 0) as total_revenue " +
                "FROM teacher t " +
                "INNER JOIN modules m ON t.m_id = m.m_id " +
                "LEFT JOIN learning l ON l.m_id = t.m_id " +
                "GROUP BY t.t_id, t.t_name, t.m_id, m.m_name " +
                "ORDER BY t.t_id";

            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            System.out.println("\n╔══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════╗");
            System.out.println("║                                            TEACHER SALARY REPORT                                                     ║");
            System.out.println("╠════════╦═══════════════════════════╦═══════════════════════════╦══════════╦════════════╦══════════════╦══════════════╣");
            System.out.println("║ T_ID   ║ Teacher Name              ║ Module Name               ║ Students ║ Revenue    ║ Salary (93%) ║ Institute(7%)║");
            System.out.println("╠════════╬═══════════════════════════╬═══════════════════════════╬══════════╬════════════╬══════════════╬══════════════╣");

            while (rs.next()) {
                teacher t = new teacher(
                    rs.getInt("t_id"),
                    rs.getString("t_name"),
                    rs.getInt("m_id"),
                    rs.getString("m_name"),
                    rs.getInt("student_count"),
                    rs.getInt("total_revenue")
                );
                teacherList.add(t);
                System.out.println(t);
            }
            System.out.println("╚════════╩═══════════════════════════╩═══════════════════════════╩══════════╩════════════╩══════════════╩══════════════╝");

            if (teacherList.isEmpty()) {
                System.out.println("\n⚠ No teachers found.");
                conn.close();
                return;
            }

            // Filter teachers who have revenue
            List<teacher> eligibleTeachers = teacherList.stream()
                .filter(t -> t.totalRevenue > 0)
                .toList();

            if (eligibleTeachers.isEmpty()) {
                System.out.println("\n⚠ No teachers have received payments yet. No salaries to process.");
                conn.close();
                return;
            }

            // Prompt for teacher selection
            System.out.print("\nEnter teacher ID to process salary: ");
            int t_id = scanner.nextInt();
            scanner.nextLine(); // Clear buffer

            // Find the selected teacher
            teacher selectedTeacher = teacherList.stream()
                .filter(t -> t.t_id == t_id)
                .findFirst()
                .orElse(null);

            if (selectedTeacher == null) {
                System.out.println("\n✗ Teacher not found.");
                conn.close();
                return;
            }

            if (selectedTeacher.totalRevenue == 0) {
                System.out.println("\n⚠ No students have paid for this teacher's module yet.");
                System.out.println("   Salary can only be processed after students pay.");
                conn.close();
                return;
            }

            // Check if salary already paid
            if (checkSalaryAlreadyPaid(conn, t_id)) {
                System.out.println("\n⚠ Salary already processed for this teacher.");
                System.out.print("Process again? (yes/no): ");
                String reprocess = scanner.nextLine().trim().toLowerCase();
                if (!reprocess.equals("yes")) {
                    conn.close();
                    return;
                }
            }

            // Display detailed breakdown
            System.out.println("\n╔══════════════════════════════════════════════════════════╗");
            System.out.println("║              SALARY CALCULATION DETAILS                  ║");
            System.out.println("╠══════════════════════════════════════════════════════════╣");
            System.out.println("║ Teacher      : " + String.format("%-39s", selectedTeacher.t_name) + "║");
            System.out.println("║ Module       : " + String.format("%-39s", selectedTeacher.m_name) + "║");
            System.out.println("║ Students Paid: " + String.format("%-39d", selectedTeacher.studentCount) + "║");
            System.out.println("╠══════════════════════════════════════════════════════════╣");
            System.out.println("║ Total Revenue           : Rs. " + String.format("%-24d", selectedTeacher.totalRevenue) + "║");
            System.out.println("║ Teacher Salary (93%)    : Rs. " + String.format("%-24d", selectedTeacher.calculatedSalary) + "║");
            System.out.println("║ Institution Share (7%)  : Rs. " + String.format("%-24d", selectedTeacher.institutionShare) + "║");
            System.out.println("╚══════════════════════════════════════════════════════════╝");

            // Confirm payment
            System.out.print("\n⚠ Process this salary payment? (yes/no): ");
            String confirm = scanner.nextLine().trim().toLowerCase();

            if (!confirm.equals("yes")) {
                System.out.println("\n✗ Salary payment cancelled.");
                conn.close();
                return;
            }

            // Create salary details and process
            salaryDetails salaryDetails = new salaryDetails(
                selectedTeacher.t_id,
                selectedTeacher.calculatedSalary,
                "yes",
                selectedTeacher.studentCount,
                selectedTeacher.totalRevenue
            );

            processSalary(conn, salaryDetails);
            updateTeacherPaidStatus(conn, t_id, "yes");

            System.out.println("\n╔═══════════════════════════════════════════════╗");
            System.out.println("║          ✓ SALARY PROCESSED SUCCESSFULLY      ║");
            System.out.println("╠═══════════════════════════════════════════════╣");
            System.out.println("║ Amount Paid : Rs. " + String.format("%-25d", selectedTeacher.calculatedSalary) + "║");
            System.out.println("║ Teacher     : " + String.format("%-28s", selectedTeacher.t_name) + "║");
            System.out.println("╚═══════════════════════════════════════════════╝");

            rs.close();
            stmt.close();
            conn.close();

        } catch (SQLException e) {
            System.out.println("\n✗ Database Error: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println("\n✗ Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static boolean checkSalaryAlreadyPaid(Connection conn, int t_id) throws SQLException {
        String query = "SELECT COUNT(*) as count FROM salary WHERE t_id = ? AND paid = 'yes'";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, t_id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("count") > 0;
            }
        }
        return false;
    }

    private static void processSalary(Connection conn, salaryDetails details) throws SQLException {
        String sql = "INSERT INTO salary (t_id, salary, paid, student_count, total_revenue) VALUES (?, ?, ?, ?, ?)";
        
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, details.gett_id());
            ps.setInt(2, details.getsalary());
            ps.setString(3, details.getpaid());
            ps.setInt(4, details.getstudentCount());
            ps.setInt(5, details.gettotalRevenue());
            ps.executeUpdate();
        }
    }

    private static void updateTeacherPaidStatus(Connection conn, int t_id, String paid) throws SQLException {
        String sql = "UPDATE teacher SET paid = ? WHERE t_id = ?";
        
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, paid);
            ps.setInt(2, t_id);
            ps.executeUpdate();
        }
    }

    public static void viewSalaryHistory(Scanner scanner) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DatabaseUtil.getInstance().getConnection();

            String query = 
                "SELECT s.t_id, t.t_name, m.m_name, s.student_count, s.total_revenue, s.salary, s.paid " +
                "FROM salary s " +
                "INNER JOIN teacher t ON s.t_id = t.t_id " +
                "INNER JOIN modules m ON t.m_id = m.m_id " +
                "ORDER BY s.t_id";

            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            System.out.println("\n╔════════════════════════════════════════════════════════════════════════════════════════════════════════╗");
            System.out.println("║                                      SALARY PAYMENT HISTORY                                            ║");
            System.out.println("╠════════╦═══════════════════════════╦═══════════════════════════╦══════════╦════════════╦══════════════╣");
            System.out.println("║ T_ID   ║ Teacher Name              ║ Module Name               ║ Students ║ Revenue    ║ Salary Paid  ║");
            System.out.println("╠════════╬═══════════════════════════╬═══════════════════════════╬══════════╬════════════╬══════════════╣");

            int totalSalaries = 0;
            int totalRevenue = 0;

            while (rs.next()) {
                int salary = rs.getInt("salary");
                int revenue = rs.getInt("total_revenue");
                totalSalaries += salary;
                totalRevenue += revenue;

                System.out.printf("| %-6d | %-25s | %-25s | %-8d | %-10d | %-12d |%n",
                    rs.getInt("t_id"),
                    rs.getString("t_name"),
                    rs.getString("m_name"),
                    rs.getInt("student_count"),
                    revenue,
                    salary
                );
            }
            System.out.println("╠════════╩═══════════════════════════╩═══════════════════════════╩══════════╬════════════╬══════════════╣");
            System.out.printf("║ TOTALS                                                                    ║ %-10d ║ %-12d ║%n", totalRevenue, totalSalaries);
            System.out.println("╚═══════════════════════════════════════════════════════════════════════════╩════════════╩══════════════╝");

            rs.close();
            stmt.close();
            conn.close();

        } catch (Exception e) {
            System.out.println("\n✗ Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void viewTeacherRevenue(Scanner scanner) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DatabaseUtil.getInstance().getConnection();

            System.out.print("Enter Teacher ID: ");
            int t_id = scanner.nextInt();
            scanner.nextLine();

            String query = 
                "SELECT t.t_name, m.m_name, m.cost, " +
                "l.s_id, s.s_name, l.paid " +
                "FROM teacher t " +
                "INNER JOIN modules m ON t.m_id = m.m_id " +
                "LEFT JOIN learning l ON l.m_id = t.m_id " +
                "LEFT JOIN student s ON s.s_id = l.s_id " +
                "WHERE t.t_id = ?";

            PreparedStatement ps = conn.prepareStatement(query);
            ps.setInt(1, t_id);
            ResultSet rs = ps.executeQuery();

            System.out.println("\n╔═════════════════════════════════════════════════════════════════════════════╗");
            System.out.println("║                         TEACHER REVENUE BREAKDOWN                           ║");
            System.out.println("╠═════════════════════════════════════════════════════════════════════════════╣");

            String teacherName = "";
            String moduleName = "";
            int moduleCost = 0;
            int paidCount = 0;
            int unpaidCount = 0;

            while (rs.next()) {
                if (teacherName.isEmpty()) {
                    teacherName = rs.getString("t_name");
                    moduleName = rs.getString("m_name");
                    moduleCost = rs.getInt("cost");
                }

                String paid = rs.getString("paid");
                if ("yes".equals(paid)) {
                    paidCount++;
                } else {
                    unpaidCount++;
                }
            }

            if (teacherName.isEmpty()) {
                System.out.println("║ Teacher not found.                                                          ║");
            } else {
                int totalRevenue = paidCount * moduleCost;
                int teacherSalary = (int)(totalRevenue * 0.93);

                System.out.println("║ Teacher       : " + String.format("%-56s", teacherName) + "║");
                System.out.println("║ Module        : " + String.format("%-56s", moduleName) + "║");
                System.out.println("║ Cost per Student : Rs. " + String.format("%-49d", moduleCost) + "║");
                System.out.println("╠═════════════════════════════════════════════════════════════════════════════╣");
                System.out.println("║ Students Paid    : " + String.format("%-56d", paidCount) + "║");
                System.out.println("║ Students Unpaid  : " + String.format("%-56d", unpaidCount) + "║");
                System.out.println("║ Total Revenue    : Rs. " + String.format("%-51d", totalRevenue) + "║");
                System.out.println("║ Teacher Salary   : Rs. " + String.format("%-51d", teacherSalary) + "║");
            }

            System.out.println("╚═════════════════════════════════════════════════════════════════════════════╝");

            rs.close();
            ps.close();
            conn.close();

        } catch (Exception e) {
            System.out.println("\n✗ Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
