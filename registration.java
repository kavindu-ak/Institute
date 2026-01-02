import java.sql.*;
import java.util.*;
import lib.DatabaseUtil;

public class registration {
    
    static class student {
        int s_id;
        String s_name;
        String address;
        int tp;

        student(int s_id, String s_name, String address, int tp) {
            this.s_id = s_id;
            this.s_name = s_name;
            this.address = address;
            this.tp = tp;
        }

        @Override
        public String toString() {
            return String.format("| %-8d | %-30s | %-30s | %-12d |", 
                s_id, s_name, address, tp);
        }
    }
    
    static class course {
        int c_id;
        String c_name;
        int moduleCount;

        course(int c_id, String c_name, int moduleCount) {
            this.c_id = c_id;
            this.c_name = c_name;
            this.moduleCount = moduleCount;
        }

        @Override
        public String toString() {
            return String.format("| %-8d | %-40s | %-29s |", 
                c_id, c_name, moduleCount + " modules");
        }
    }

    static class registrationDetails {
        int s_id;
        int c_id;
        String date;

        public registrationDetails(int s_id, int c_id, String date) {
            this.c_id = c_id;
            this.s_id = s_id;
            this.date = date;
        }

        public int getc_id() { return c_id; }
        public int gets_id() { return s_id; }
        public String getdate() { return date; }
    }

    public static void registrationcourse(Scanner scanner) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DatabaseUtil.getInstance().getConnection();

            System.out.println("\n╔═══════════════════════════════════════════════════════════════════════════════╗");
            System.out.println("║                     STUDENT COURSE REGISTRATION FORM                          ║");
            System.out.println("╚═══════════════════════════════════════════════════════════════════════════════╝");

            // ==================== STEP 1: FETCH & DISPLAY STUDENTS ====================
            List<student> studentList = new ArrayList<>();
            
            String studentQuery = "SELECT s_id, s_name, address, tp FROM student ORDER BY s_name";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(studentQuery);

            System.out.println("\n╔═════════════════════════════════════════════════════════════════════════════════════════╗");
            System.out.println("║                                  AVAILABLE STUDENTS                                     ║");
            System.out.println("╠══════════╦════════════════════════════════════╦════════════════════════════════╦══════════╣");
            System.out.println("║ S_ID     ║ Student Name                       ║ Address                        ║ Phone    ║");
            System.out.println("╠══════════╬════════════════════════════════════╬════════════════════════════════╬══════════╣");

            while (rs.next()) {
                student s = new student(
                    rs.getInt("s_id"),
                    rs.getString("s_name"),
                    rs.getString("address"),
                    rs.getInt("tp")
                );
                studentList.add(s);
                System.out.println(s);
            }
            System.out.println("╚══════════╩════════════════════════════════════╩════════════════════════════════╩══════════╝");

            if (studentList.isEmpty()) {
                System.out.println("\n⚠ No students found. Please register students first.");
                conn.close();
                return;
            }

            rs.close();
            stmt.close();

            // ==================== STEP 2: SELECT STUDENT ====================
            System.out.print("\n➤ Enter Student ID: ");
            int s_id = scanner.nextInt();
            scanner.nextLine();

            // Validate student
            student selectedStudent = studentList.stream()
                .filter(s -> s.s_id == s_id)
                .findFirst()
                .orElse(null);

            if (selectedStudent == null) {
                System.out.println("\n✗ Invalid Student ID!");
                conn.close();
                return;
            }

            System.out.println("\n✓ Selected Student: " + selectedStudent.s_name);

            // Check already registered courses
            String registeredQuery = 
                "SELECT c.c_id, c.c_name " +
                "FROM registration r " +
                "INNER JOIN course c ON r.c_id = c.c_id " +
                "WHERE r.s_id = ?";
            
            PreparedStatement regCheckStmt = conn.prepareStatement(registeredQuery);
            regCheckStmt.setInt(1, s_id);
            ResultSet regCheckRs = regCheckStmt.executeQuery();

            List<Integer> registeredCourseIds = new ArrayList<>();
            if (regCheckRs.next()) {
                System.out.println("\n📚 Already Registered Courses:");
                do {
                    int cid = regCheckRs.getInt("c_id");
                    registeredCourseIds.add(cid);
                    System.out.println("   • " + regCheckRs.getString("c_name"));
                } while (regCheckRs.next());
            }
            regCheckRs.close();
            regCheckStmt.close();

            // ==================== STEP 3: FETCH & DISPLAY COURSES ====================
            List<course> courseList = new ArrayList<>();
            
            String courseQuery = 
                "SELECT c.c_id, c.c_name, COUNT(m.m_id) as module_count " +
                "FROM course c " +
                "LEFT JOIN modules m ON c.c_id = m.c_id " +
                "GROUP BY c.c_id, c.c_name " +
                "ORDER BY c.c_name";
            
            Statement courseStmt = conn.createStatement();
            ResultSet courseRs = courseStmt.executeQuery(courseQuery);

            System.out.println("\n╔═════════════════════════════════════════════════════════════════════════════════════════╗");
            System.out.println("║                          AVAILABLE COURSES                                              ║");
            System.out.println("╠══════════╦══════════════════════════════════════════════╦═══════════════════════════════╣");
            System.out.println("║ C_ID     ║ Course Name                                  ║ Modules                       ║");
            System.out.println("╠══════════╬══════════════════════════════════════════════╬═══════════════════════════════╣");

            List<course> availableCourses = new ArrayList<>();

            while (courseRs.next()) {
                int cid = courseRs.getInt("c_id");
                course c = new course(
                    cid,
                    courseRs.getString("c_name"),
                    courseRs.getInt("module_count")
                );
                courseList.add(c);
                
                // Only show courses not yet registered
                if (!registeredCourseIds.contains(cid)) {
                    System.out.println(c);
                    availableCourses.add(c);
                }
            }
            System.out.println("╚══════════╩══════════════════════════════════════════════╩═══════════════════════════════╝");

            if (availableCourses.isEmpty()) {
                System.out.println("\n⚠ This student is already registered for all available courses!");
                conn.close();
                return;
            }

            courseRs.close();
            courseStmt.close();

            // ==================== STEP 4: SELECT COURSE ====================
            System.out.print("\n➤ Enter Course ID: ");
            int c_id = scanner.nextInt();
            scanner.nextLine();

            // Validate course
            course selectedCourse = availableCourses.stream()
                .filter(c -> c.c_id == c_id)
                .findFirst()
                .orElse(null);

            if (selectedCourse == null) {
                System.out.println("\n✗ Invalid Course ID or already registered!");
                conn.close();
                return;
            }

            if (selectedCourse.moduleCount == 0) {
                System.out.println("\n⚠ Warning: This course has no modules yet!");
            }

            System.out.println("✓ Selected Course: " + selectedCourse.c_name);

            // ==================== STEP 5: ENTER DATE ====================
            System.out.print("\n➤ Enter Registration Date (YYYY-MM-DD) or press Enter for today: ");
            String date = scanner.nextLine().trim();
            
            if (date.isEmpty()) {
                date = java.time.LocalDate.now().toString();
            }

            // ==================== STEP 6: CONFIRMATION ====================
            System.out.println("\n╔═══════════════════════════════════════════════════════════════════════════════╗");
            System.out.println("║                         REGISTRATION SUMMARY                                  ║");
            System.out.println("╠═══════════════════════════════════════════════════════════════════════════════╣");
            System.out.println("║ Student    : " + String.format("%-64s", selectedStudent.s_name) + "║");
            System.out.println("║ Course     : " + String.format("%-64s", selectedCourse.c_name) + "║");
            System.out.println("║ Modules    : " + String.format("%-64s", selectedCourse.moduleCount + " modules available") + "║");
            System.out.println("║ Date       : " + String.format("%-64s", date) + "║");
            System.out.println("╚═══════════════════════════════════════════════════════════════════════════════╝");

            System.out.print("\n⚠ Confirm registration? (yes/no): ");
            String confirm = scanner.nextLine().trim().toLowerCase();

            if (!confirm.equals("yes")) {
                System.out.println("\n✗ Registration cancelled.");
                conn.close();
                return;
            }

            // ==================== STEP 7: PROCESS REGISTRATION ====================
            registrationDetails regDetails = new registrationDetails(s_id, c_id, date);
            processregistration(conn, regDetails);

            System.out.println("\n╔═══════════════════════════════════════════════════════════════════════════════╗");
            System.out.println("║                  ✓ REGISTRATION SUCCESSFUL                                    ║");
            System.out.println("╠═══════════════════════════════════════════════════════════════════════════════╣");
            System.out.println("║ " + selectedStudent.s_name + " has been enrolled in " + selectedCourse.c_name);
            System.out.println("║                                                                               ║");
            System.out.println("║ Next Steps:                                                                   ║");
            System.out.println("║ 1. Go to Learning & Payment menu                                              ║");
            System.out.println("║ 2. Process payments for individual modules                                    ║");
            System.out.println("╚═══════════════════════════════════════════════════════════════════════════════╝");

            conn.close();

        } catch (SQLIntegrityConstraintViolationException e) {
            System.out.println("\n✗ This student is already registered for this course!");
        } catch (SQLException e) {
            System.out.println("\n✗ Database Error: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println("\n✗ Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void processregistration(Connection conn, registrationDetails registrationDetails) throws SQLException {
        String sql = "INSERT INTO registration (s_id, c_id, date) VALUES (?, ?, ?)";
        
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, registrationDetails.gets_id());
            ps.setInt(2, registrationDetails.getc_id());
            ps.setString(3, registrationDetails.getdate());
            ps.executeUpdate();
        }
    }

    public static void viewRegistrations(Scanner scanner) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DatabaseUtil.getInstance().getConnection();

            String query = 
                "SELECT r.reg_id, s.s_name, c.c_name, r.date, " +
                "COUNT(m.m_id) as total_modules, " +
                "SUM(CASE WHEN l.paid = 'yes' THEN 1 ELSE 0 END) as paid_modules " +
                "FROM registration r " +
                "INNER JOIN student s ON r.s_id = s.s_id " +
                "INNER JOIN course c ON r.c_id = c.c_id " +
                "LEFT JOIN modules m ON c.c_id = m.c_id " +
                "LEFT JOIN learning l ON l.s_id = r.s_id AND l.m_id = m.m_id " +
                "GROUP BY r.reg_id, s.s_name, c.c_name, r.date " +
                "ORDER BY r.date DESC, s.s_name";

            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            System.out.println("\n╔════════════════════════════════════════════════════════════════════════════════════════════════════════╗");
            System.out.println("║                                    ALL REGISTRATIONS                                                   ║");
            System.out.println("╠═══════╦═══════════════════════════╦══════════════════════════════╦═════════════╦══════════════════════╣");
            System.out.println("║ Reg ID║ Student Name              ║ Course Name                  ║ Date        ║ Modules (Paid/Total) ║");
            System.out.println("╠═══════╬═══════════════════════════╬══════════════════════════════╬═════════════╬══════════════════════╣");

            while (rs.next()) {
                System.out.printf("| %-5d | %-25s | %-28s | %-11s | %-20s |%n",
                    rs.getInt("reg_id"),
                    rs.getString("s_name"),
                    rs.getString("c_name"),
                    rs.getString("date"),
                    rs.getInt("paid_modules") + "/" + rs.getInt("total_modules")
                );
            }
            System.out.println("╚═══════╩═══════════════════════════╩══════════════════════════════╩═════════════╩══════════════════════╝");

            rs.close();
            stmt.close();
            conn.close();

        } catch (Exception e) {
            System.out.println("\n✗ Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static class EntityFactory {
        public static registration.student createStudent(int s_id, String s_name, String address, int tp) {
            return new registration.student(s_id, s_name, address, tp);
        }
    }
}