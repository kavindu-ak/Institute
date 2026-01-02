import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import lib.DatabaseUtil;

public class Modules {

    static class course {
        int c_id;
        String c_name;

        course(int c_id, String c_name) {
            this.c_id = c_id;
            this.c_name = c_name;
        }

        @Override
        public String toString() {
            return String.format("| %-10d | %-50s |", c_id, c_name);
        }
    }

    public static void new_m(Scanner scanner) {
        String mname = promptInputS(scanner, "Enter module name: "); 
        int cost = promptInputI(scanner, "Enter module cost (Rs.): ");
        scanner.nextLine(); // Clear buffer after reading int

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DatabaseUtil.getInstance().getConnection();

            // Fetch courses
            List<course> courseList = new ArrayList<>();
            
            try (Statement courseStmt = conn.createStatement();
                 ResultSet courseRs = courseStmt.executeQuery("SELECT c_id, c_name FROM course")) {

                System.out.println("\n╔════════════════════════════════════════════════════════════╗");
                System.out.println("║                    AVAILABLE COURSES                       ║");
                System.out.println("╠════════════╦═══════════════════════════════════════════════╣");
                System.out.println("║ Course ID  ║ Course Name                                   ║");
                System.out.println("╠════════════╬═══════════════════════════════════════════════╣");
                
                while (courseRs.next()) {
                    course crs = new course(
                        courseRs.getInt("c_id"),
                        courseRs.getString("c_name")
                    );
                    courseList.add(crs);
                    System.out.println(crs);
                }
                System.out.println("╚════════════╩═══════════════════════════════════════════════╝");
                
            } catch (SQLException e) {
                System.out.println("\n✗ Error fetching courses: " + e.getMessage());
                e.printStackTrace();
                conn.close();
                return;
            }

            if (courseList.isEmpty()) {
                System.out.println("\n⚠ No courses available. Please add courses first.");
                conn.close();
                return;
            }

            int c_id = promptInputI(scanner, "\nEnter Course ID: ");
            scanner.nextLine(); // Clear buffer

            // Validate course exists
            boolean courseExists = courseList.stream()
                .anyMatch(c -> c.c_id == c_id);

            if (!courseExists) {
                System.out.println("\n✗ Invalid Course ID. Module registration failed.");
                conn.close();
                return;
            }

            // Insert module
            String sql = "INSERT INTO modules (m_name, cost, c_id) VALUES (?, ?, ?)";
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, mname);
                stmt.setInt(2, cost);
                stmt.setInt(3, c_id);

                int rows = stmt.executeUpdate();
                if (rows > 0) {
                    System.out.println("\n✓ Module registered successfully!");
                    System.out.println("  Module: " + mname);
                    System.out.println("  Cost: Rs. " + cost);
                } else {
                    System.out.println("\n✗ Failed to register module.");
                }
            } catch (SQLException e) {
                System.out.println("\n✗ Error inserting module: " + e.getMessage());
                e.printStackTrace();
            }

            conn.close();

        } catch (SQLException e) {
            System.out.println("\n✗ Database Error: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println("\n✗ Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void viewModules(Scanner scanner) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DatabaseUtil.getInstance().getConnection();

            String query = "SELECT m.m_id, m.m_name, m.cost, m.c_id, c.c_name " +
                          "FROM modules m " +
                          "INNER JOIN course c ON m.c_id = c.c_id " +
                          "ORDER BY c.c_name, m.m_name";

            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            System.out.println("\n╔════════════════════════════════════════════════════════════════════════════════════════╗");
            System.out.println("║                                  ALL MODULES                                           ║");
            System.out.println("╠══════════╦════════════════════════════╦════════════════════════════╦══════════════════╣");
            System.out.println("║ Module ID║ Module Name                ║ Course Name                ║ Cost (Rs.)       ║");
            System.out.println("╠══════════╬════════════════════════════╬════════════════════════════╬══════════════════╣");

            while (rs.next()) {
                System.out.printf("| %-8d | %-26s | %-26s | %-16d |%n",
                    rs.getInt("m_id"),
                    rs.getString("m_name"),
                    rs.getString("c_name"),
                    rs.getInt("cost")
                );
            }
            System.out.println("╚══════════╩════════════════════════════╩════════════════════════════╩══════════════════╝");

            rs.close();
            stmt.close();
            conn.close();

        } catch (Exception e) {
            System.out.println("\n✗ Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static String promptInputS(Scanner scanner, String message) {
        System.out.print(message);
        return scanner.nextLine().trim();
    }

    private static int promptInputI(Scanner scanner, String message) {
        System.out.print(message);
        return scanner.nextInt();
    }
}