import java.sql.*;
import java.util.Scanner;
import lib.DatabaseUtil;

public class course {
    
    public static void new_c(Scanner scanner) {
        String cname = promptInputS(scanner, "Enter course name: "); 

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DatabaseUtil.getInstance().getConnection();

            String sql = "INSERT INTO course (c_name) VALUES (?)";
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, cname);

                int rows = stmt.executeUpdate();
                if (rows > 0) {
                    System.out.println("\n✓ Course registered successfully!");
                    System.out.println("  Course: " + cname);
                } else {
                    System.out.println("\n✗ Failed to register course.");
                }
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

    public static void viewCourses(Scanner scanner) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DatabaseUtil.getInstance().getConnection();

            String query = "SELECT c.c_id, c.c_name, COUNT(m.m_id) as module_count " +
                          "FROM course c " +
                          "LEFT JOIN modules m ON c.c_id = m.c_id " +
                          "GROUP BY c.c_id, c.c_name " +
                          "ORDER BY c.c_name";

            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            System.out.println("\n╔═══════════════════════════════════════════════════════════════════╗");
            System.out.println("║                        ALL COURSES                                ║");
            System.out.println("╠════════════╦════════════════════════════════════╦═════════════════╣");
            System.out.println("║ Course ID  ║ Course Name                        ║ Modules Count   ║");
            System.out.println("╠════════════╬════════════════════════════════════╬═════════════════╣");

            while (rs.next()) {
                System.out.printf("| %-10d | %-34s | %-15d |%n",
                    rs.getInt("c_id"),
                    rs.getString("c_name"),
                    rs.getInt("module_count")
                );
            }
            System.out.println("╚════════════╩════════════════════════════════════╩═════════════════╝");

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
}