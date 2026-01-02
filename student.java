import java.sql.*;
import java.util.Scanner;
import lib.DatabaseUtil;

public class student {

    public static void new_s(Scanner scanner) {
        String name = promptInputS(scanner, "Enter student name: "); 
        String address = promptInputS(scanner, "Enter address: ");
        String gender = promptInputS(scanner, "Enter Gender (M/F): ");
        String bd = promptInputS(scanner, "Enter date of birth (YYYY-MM-DD): ");
        int nic = promptInputI(scanner, "Enter NIC number: ");
        scanner.nextLine(); // Clear buffer after reading int
        int tp_no = promptInputI(scanner, "Enter phone number: ");
        scanner.nextLine(); // Clear buffer after reading int

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DatabaseUtil.getInstance().getConnection();

            String sql = "INSERT INTO student (s_name, nic, address, tp, gen, dob) VALUES (?, ?, ?, ?, ?, ?)";
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, name);
                stmt.setInt(2, nic);
                stmt.setString(3, address);
                stmt.setInt(4, tp_no);
                stmt.setString(5, gender);
                stmt.setString(6, bd);

                int rows = stmt.executeUpdate();
                if (rows > 0) {
                    System.out.println("\n✓ Student registered successfully.");
                } else {
                    System.out.println("\n✗ Failed to register student.");
                }
            }

            conn.close();
            
        } catch (SQLException e) {
            System.out.println("Database Error: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
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