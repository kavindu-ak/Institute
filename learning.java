import java.sql.*;
import java.util.*;
import lib.DatabaseUtil;

public class learning {

    static class registration {
        int s_id;
        String s_name;
        int c_id;
        String c_name;
        String reg_date;

        registration(int s_id, String s_name, int c_id, String c_name, String reg_date) {
            this.s_id = s_id;
            this.s_name = s_name;
            this.c_id = c_id;
            this.c_name = c_name;
            this.reg_date = reg_date;
        }

        @Override
        public String toString() {
            return String.format("| %-8d | %-30s | %-8d | %-30s | %-12s |", 
                s_id, s_name, c_id, c_name, reg_date);
        }
    }

    static class modules {
        int m_id;
        String m_name;
        int cost;
        int c_id;
        String teacher_name;
        boolean alreadyPaid;

        modules(int m_id, String m_name, int cost, int c_id, String teacher_name, boolean alreadyPaid) {
            this.m_id = m_id;
            this.m_name = m_name;
            this.cost = cost;
            this.c_id = c_id;
            this.teacher_name = teacher_name;
            this.alreadyPaid = alreadyPaid;
        }

        @Override
        public String toString() {
            String status = alreadyPaid ? "✓ PAID" : "Not Paid";
            String teacher = teacher_name != null ? teacher_name : "No Teacher";
            return String.format("| %-8d | %-35s | Rs. %-10d | %-20s | %-10s |", 
                m_id, m_name, cost, teacher, status);
        }
    }

    static class learningDetails {
        int s_id;
        int c_id;
        int m_id;
        String paid;

        public learningDetails(int s_id, int c_id, int m_id, String paid) {
            this.s_id = s_id;
            this.c_id = c_id;
            this.m_id = m_id;
            this.paid = paid;
        }

        public int gets_id() { return s_id; }
        public int getc_id() { return c_id; }
        public int getm_id() { return m_id; }
        public String paid() { return paid; }
    }

    public static void learningAvailable(Scanner scanner) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DatabaseUtil.getInstance().getConnection();

            System.out.println("\n╔═══════════════════════════════════════════════════════════════════════════════╗");
            System.out.println("║                     STUDENT MODULE PAYMENT FORM                               ║");
            System.out.println("╚═══════════════════════════════════════════════════════════════════════════════╝");

            // ==================== STEP 1: FETCH & DISPLAY REGISTRATIONS ====================
            List<registration> registrationList = new ArrayList<>();
            
            String regQuery = 
                "SELECT r.s_id, s.s_name, r.c_id, c.c_name, r.date " +
                "FROM registration r " +
                "INNER JOIN student s ON r.s_id = s.s_id " +
                "INNER JOIN course c ON r.c_id = c.c_id " +
                "ORDER BY s.s_name, c.c_name";
            
            Statement stmt = conn.createStatement();
            ResultSet res = stmt.executeQuery(regQuery);

            System.out.println("\n╔════════════════════════════════════════════════════════════════════════════════════════════════════╗");
            System.out.println("║                              STUDENT REGISTRATIONS                                             ║");
            System.out.println("╠══════════╦════════════════════════════════════╦══════════╦════════════════════════════════╦══════════╣");
            System.out.println("║ S_ID     ║ Student Name                       ║ C_ID     ║ Course Name                    ║ Reg Date ║");
            System.out.println("╠══════════╬════════════════════════════════════╬══════════╬════════════════════════════════╬══════════╣");

            while (res.next()) {
                registration reg = EntityFactory.createregistration(
                    res.getInt("s_id"),
                    res.getString("s_name"),
                    res.getInt("c_id"),
                    res.getString("c_name"),
                    res.getString("date")
                );
                registrationList.add(reg);
                System.out.println(reg);
            }
            System.out.println("╚══════════╩════════════════════════════════════╩══════════╩════════════════════════════════╩══════════╝");

            if (registrationList.isEmpty()) {
                System.out.println("\n⚠ No registrations found. Please register students to courses first.");
                conn.close();
                return;
            }

            res.close();
            stmt.close();

            // ==================== STEP 2: SELECT STUDENT ====================
            System.out.print("\n➤ Enter Student ID: ");
            int s_id = scanner.nextInt();
            scanner.nextLine();

            // Filter registrations for this student
            List<registration> studentRegs = registrationList.stream()
                .filter(r -> r.s_id == s_id)
                .toList();

            if (studentRegs.isEmpty()) {
                System.out.println("\n✗ No registrations found for this student!");
                conn.close();
                return;
            }

            registration selectedReg = studentRegs.get(0);
            System.out.println("\n✓ Selected Student: " + selectedReg.s_name);

            // Show student's courses
            if (studentRegs.size() > 1) {
                System.out.println("\n📚 Student's Registered Courses:");
                for (registration r : studentRegs) {
                    System.out.println("   • Course ID " + r.c_id + ": " + r.c_name);
                }
            }

            // ==================== STEP 3: SELECT COURSE ====================
            System.out.print("\n➤ Enter Course ID: ");
            int c_id = scanner.nextInt();
            scanner.nextLine();

            // Validate course registration
            registration validReg = studentRegs.stream()
                .filter(r -> r.c_id == c_id)
                .findFirst()
                .orElse(null);

            if (validReg == null) {
                System.out.println("\n✗ Student is not registered for this course!");
                conn.close();
                return;
            }

            System.out.println("✓ Selected Course: " + validReg.c_name);

            // ==================== STEP 4: FETCH & DISPLAY MODULES ====================
            List<modules> moduleList = new ArrayList<>();
            
            String moduleQuery = 
                "SELECT m.m_id, m.m_name, m.cost, m.c_id, t.t_name, " +
                "CASE WHEN l.l_id IS NOT NULL THEN 1 ELSE 0 END as already_paid " +
                "FROM modules m " +
                "LEFT JOIN teacher t ON m.m_id = t.m_id " +
                "LEFT JOIN learning l ON l.m_id = m.m_id AND l.s_id = ? AND l.paid = 'yes' " +
                "WHERE m.c_id = ? " +
                "ORDER BY m.m_name";
            
            PreparedStatement moduleStmt = conn.prepareStatement(moduleQuery);
            moduleStmt.setInt(1, s_id);
            moduleStmt.setInt(2, c_id);
            ResultSet moduleRs = moduleStmt.executeQuery();

            System.out.println("\n╔════════════════════════════════════════════════════════════════════════════════════════════════════╗");
            System.out.println("║                              COURSE MODULES                                                        ║");
            System.out.println("╠══════════╦═════════════════════════════════════╦══════════════╦══════════════════════╦════════════╣");
            System.out.println("║ M_ID     ║ Module Name                         ║ Cost         ║ Teacher              ║ Status     ║");
            System.out.println("╠══════════╬═════════════════════════════════════╬══════════════╬══════════════════════╬════════════╣");

            List<modules> unpaidModules = new ArrayList<>();
            int totalCost = 0;
            int paidCount = 0;

            while (moduleRs.next()) {
                boolean isPaid = moduleRs.getInt("already_paid") == 1;
                modules mod = new modules(
                    moduleRs.getInt("m_id"),
                    moduleRs.getString("m_name"),
                    moduleRs.getInt("cost"),
                    moduleRs.getInt("c_id"),
                    moduleRs.getString("t_name"),
                    isPaid
                );
                moduleList.add(mod);
                System.out.println(mod);
                
                totalCost += mod.cost;
                if (isPaid) {
                    paidCount++;
                } else {
                    unpaidModules.add(mod);
                }
            }
            System.out.println("╚══════════╩═════════════════════════════════════╩══════════════╩══════════════════════╩════════════╝");

            moduleRs.close();
            moduleStmt.close();

            if (moduleList.isEmpty()) {
                System.out.println("\n⚠ No modules found for this course!");
                conn.close();
                return;
            }

            // Payment summary
            System.out.println("\n📊 Payment Summary:");
            System.out.println("   Total Modules: " + moduleList.size());
            System.out.println("   Paid Modules: " + paidCount);
            System.out.println("   Unpaid Modules: " + unpaidModules.size());
            System.out.println("   Total Course Cost: Rs. " + totalCost);
            System.out.println("   Amount Paid: Rs. " + (totalCost - unpaidModules.stream().mapToInt(m -> m.cost).sum()));
            System.out.println("   Amount Due: Rs. " + unpaidModules.stream().mapToInt(m -> m.cost).sum());

            if (unpaidModules.isEmpty()) {
                System.out.println("\n✓ All modules have been paid for this course!");
                conn.close();
                return;
            }

            // ==================== STEP 5: SELECT MULTIPLE MODULES ====================
            System.out.println("\n╔═══════════════════════════════════════════════════════════════════════════════╗");
            System.out.println("║                    PAYMENT OPTIONS                                            ║");
            System.out.println("╠═══════════════════════════════════════════════════════════════════════════════╣");
            System.out.println("║  1. Pay for ALL unpaid modules                                                ║");
            System.out.println("║  2. Pay for SPECIFIC modules (select multiple)                                ║");
            System.out.println("║  3. Pay for ONE module only                                                   ║");
            System.out.println("║  0. Cancel                                                                    ║");
            System.out.println("╚═══════════════════════════════════════════════════════════════════════════════╝");
            System.out.print("\n➤ Enter your choice: ");
            int paymentOption = scanner.nextInt();
            scanner.nextLine();

            List<modules> selectedModules = new ArrayList<>();

            switch (paymentOption) {
                case 1:
                    // Pay for ALL unpaid modules
                    System.out.println("\n📋 You will pay for ALL these modules:");
                    System.out.println("╔══════════╦═════════════════════════════════════╦══════════════╗");
                    System.out.println("║ M_ID     ║ Module Name                         ║ Cost         ║");
                    System.out.println("╠══════════╬═════════════════════════════════════╬══════════════╣");
                    for (modules mod : unpaidModules) {
                        System.out.printf("║ %-8d ║ %-35s ║ Rs. %-8d ║%n", mod.m_id, mod.m_name, mod.cost);
                    }
                    System.out.println("╚══════════╩═════════════════════════════════════╩══════════════╝");
                    
                    selectedModules.addAll(unpaidModules);
                    break;

                case 2:
                    // Pay for SPECIFIC modules
                    System.out.println("\n📋 Available Modules to Pay:");
                    System.out.println("╔══════════╦═════════════════════════════════════╦══════════════╗");
                    System.out.println("║ M_ID     ║ Module Name                         ║ Cost         ║");
                    System.out.println("╠══════════╬═════════════════════════════════════╬══════════════╣");
                    for (modules mod : unpaidModules) {
                        System.out.printf("║ %-8d ║ %-35s ║ Rs. %-8d ║%n", mod.m_id, mod.m_name, mod.cost);
                    }
                    System.out.println("╚══════════╩═════════════════════════════════════╩══════════════╝");
                    
                    System.out.println("\n📝 Enter Module IDs to pay for (separate with commas):");
                    System.out.println("   Example: 11,12  or  11, 12");
                    System.out.print("➤ Module IDs: ");
                    String input = scanner.nextLine().trim();
                    
                    String[] moduleIds = input.split(",");
                    for (String idStr : moduleIds) {
                        try {
                            int mid = Integer.parseInt(idStr.trim());
                            modules mod = unpaidModules.stream()
                                .filter(m -> m.m_id == mid)
                                .findFirst()
                                .orElse(null);
                            
                            if (mod != null) {
                                selectedModules.add(mod);
                            } else {
                                System.out.println("⚠ Module ID " + mid + " not found or already paid - skipped");
                            }
                        } catch (NumberFormatException e) {
                            System.out.println("⚠ Invalid ID '" + idStr.trim() + "' - skipped");
                        }
                    }
                    break;

                case 3:
                    // Pay for ONE module
                    System.out.println("\n📋 Available Modules to Pay:");
                    System.out.println("╔══════════╦═════════════════════════════════════╦══════════════╗");
                    System.out.println("║ M_ID     ║ Module Name                         ║ Cost         ║");
                    System.out.println("╠══════════╬═════════════════════════════════════╬══════════════╣");
                    for (modules mod : unpaidModules) {
                        System.out.printf("║ %-8d ║ %-35s ║ Rs. %-8d ║%n", mod.m_id, mod.m_name, mod.cost);
                    }
                    System.out.println("╚══════════╩═════════════════════════════════════╩══════════════╝");
                    
                    System.out.print("\n➤ Enter Module ID to pay for: ");
                    int m_id = scanner.nextInt();
                    scanner.nextLine();
                    
                    modules singleMod = unpaidModules.stream()
                        .filter(m -> m.m_id == m_id)
                        .findFirst()
                        .orElse(null);
                    
                    if (singleMod != null) {
                        selectedModules.add(singleMod);
                    } else {
                        System.out.println("\n✗ Invalid Module ID or already paid!");
                        conn.close();
                        return;
                    }
                    break;

                case 0:
                    System.out.println("\n✗ Payment cancelled.");
                    conn.close();
                    return;

                default:
                    System.out.println("\n✗ Invalid option!");
                    conn.close();
                    return;
            }

            if (selectedModules.isEmpty()) {
                System.out.println("\n✗ No valid modules selected!");
                conn.close();
                return;
            }

            // ==================== STEP 6: PAYMENT CONFIRMATION ====================
            int totalPayment = selectedModules.stream().mapToInt(m -> m.cost).sum();
            int teacherShare = (int)(totalPayment * 0.93);

            System.out.println("\n╔═══════════════════════════════════════════════════════════════════════════════╗");
            System.out.println("║                          PAYMENT SUMMARY                                      ║");
            System.out.println("╠═══════════════════════════════════════════════════════════════════════════════╣");
            System.out.println("║ Student    : " + String.format("%-64s", selectedReg.s_name) + "║");
            System.out.println("║ Course     : " + String.format("%-64s", validReg.c_name) + "║");
            System.out.println("║ Modules    : " + String.format("%-64s", selectedModules.size() + " module(s)") + "║");
            System.out.println("╠═══════════════════════════════════════════════════════════════════════════════╣");

            for (modules mod : selectedModules) {
                String teacher = mod.teacher_name != null ? mod.teacher_name : "No Teacher";
                System.out.println("║ • " + String.format("%-30s", mod.m_name) + " Rs. " + String.format("%-10d", mod.cost) + " (" + teacher + ")");
            }

            System.out.println("╠═══════════════════════════════════════════════════════════════════════════════╣");
            System.out.println("║ Total Amount : Rs. " + String.format("%-58d", totalPayment) + "║");
            System.out.println("║ Teachers Get : Rs. " + String.format("%-58d", teacherShare) + "(93%)║");
            System.out.println("╚═══════════════════════════════════════════════════════════════════════════════╝");

            System.out.print("\n⚠ Confirm payment? (yes/no): ");
            String confirm = scanner.nextLine().trim().toLowerCase();

            if (!confirm.equals("yes")) {
                System.out.println("\n✗ Payment cancelled.");
                conn.close();
                return;
            }

            // ==================== STEP 7: PROCESS PAYMENTS ====================
            int successCount = 0;
            for (modules mod : selectedModules) {
                try {
                    learningDetails learningDetails = new learningDetails(s_id, c_id, mod.m_id, "yes");
                    paid(conn, learningDetails);
                    successCount++;
                } catch (SQLException e) {
                    System.out.println("⚠ Error processing payment for module " + mod.m_name + ": " + e.getMessage());
                }
            }

            System.out.println("\n╔═══════════════════════════════════════════════════════════════════════════════╗");
            System.out.println("║                      ✓ PAYMENT SUCCESSFUL                                     ║");
            System.out.println("╠═══════════════════════════════════════════════════════════════════════════════╣");
            System.out.println("║ Modules Paid     : " + String.format("%-60d", successCount) + "║");
            System.out.println("║ Total Amount     : Rs. " + String.format("%-56d", totalPayment) + "║");
            System.out.println("║ Teachers Receive : Rs. " + String.format("%-56d", teacherShare) + "║");
            System.out.println("║                                                                               ║");
            System.out.println("║ Remaining unpaid : " + String.format("%-60d", (unpaidModules.size() - successCount)) + " module(s)║");
            System.out.println("╚═══════════════════════════════════════════════════════════════════════════════╝");

            conn.close();

        } catch (SQLIntegrityConstraintViolationException e) {
            System.out.println("\n⚠ One or more payment records already exist!");
        } catch (SQLException e) {
            System.out.println("\n✗ Database Error: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println("\n✗ Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void paid(Connection conn, learningDetails learningDetails) throws SQLException {
        String sql = "INSERT INTO learning (s_id, c_id, m_id, paid) VALUES (?, ?, ?, ?)";
        
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, learningDetails.gets_id());
            ps.setInt(2, learningDetails.getc_id());
            ps.setInt(3, learningDetails.getm_id());
            ps.setString(4, learningDetails.paid());
            ps.executeUpdate();
        }
    }

    public static void viewPaymentHistory(Scanner scanner) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DatabaseUtil.getInstance().getConnection();

            String query = 
                "SELECT s.s_name, c.c_name, m.m_name, m.cost, l.paid, l.payment_date " +
                "FROM learning l " +
                "INNER JOIN student s ON l.s_id = s.s_id " +
                "INNER JOIN course c ON l.c_id = c.c_id " +
                "INNER JOIN modules m ON l.m_id = m.m_id " +
                "ORDER BY l.payment_date DESC, s.s_name";

            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            System.out.println("\n╔═════════════════════════════════════════════════════════════════════════════════════════════════════════════╗");
            System.out.println("║                                       PAYMENT HISTORY                                                       ║");
            System.out.println("╠══════════════════════╦══════════════════════╦═══════════════════════╦══════════════╦════════╦═════════════════╣");
            System.out.println("║ Student Name         ║ Course Name          ║ Module Name           ║ Amount       ║ Status ║ Date            ║");
            System.out.println("╠══════════════════════╬══════════════════════╬═══════════════════════╬══════════════╬════════╬═════════════════╣");

            int totalPaid = 0;
            int count = 0;

            while (rs.next()) {
                if (rs.getString("paid").equals("yes")) {
                    totalPaid += rs.getInt("cost");
                    count++;
                }
                
                System.out.printf("| %-20s | %-20s | %-21s | Rs. %-8d | %-6s | %-15s |%n",
                    rs.getString("s_name"),
                    rs.getString("c_name"),
                    rs.getString("m_name"),
                    rs.getInt("cost"),
                    rs.getString("paid"),
                    rs.getString("payment_date").substring(0, 10)
                );
            }
            System.out.println("╠══════════════════════╩══════════════════════╩═══════════════════════╬══════════════╩════════╩═════════════════╣");
            System.out.printf("║ TOTAL PAYMENTS                                                       ║ Rs. %-31d ║%n", totalPaid);
            System.out.printf("║ Payment Count: %-56d ║                                   ║%n", count);
            System.out.println("╚══════════════════════════════════════════════════════════════════════╩═══════════════════════════════════════╝");

            rs.close();
            stmt.close();
            conn.close();

        } catch (Exception e) {
            System.out.println("\n✗ Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static class EntityFactory {
        public static learning.registration createregistration(int s_id, String s_name, int c_id, String c_name, String reg_date) {
            return new learning.registration(s_id, s_name, c_id, c_name, reg_date);
        }
    } 
}