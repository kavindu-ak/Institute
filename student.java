import java.sql.*;
import java.util.Scanner;
import lib.DatabaseUtil;

public class Student extends Person {
    private int studentId;
    private String dateOfBirth;
    
    // Constructors
    public Student() {
        super();
    }
    
    public Student(String name, String address, String gender, int nic, int phoneNumber, String dateOfBirth) {
        super(name, address, gender, nic, phoneNumber);
        this.dateOfBirth = dateOfBirth;
    }
    
    public Student(int studentId, String name, String address, String gender, int nic, int phoneNumber, String dateOfBirth) {
        super(name, address, gender, nic, phoneNumber);
        this.studentId = studentId;
        this.dateOfBirth = dateOfBirth;
    }
    
    // Getters and Setters
    public int getStudentId() {
        return studentId;
    }
    
    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }
    
    public String getDateOfBirth() {
        return dateOfBirth;
    }
    
    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }
    
    @Override
    public void displayInfo() {
        System.out.println("╔════════════════════════════════════════════════════════╗");
        System.out.println("║              STUDENT INFORMATION                       ║");
        System.out.println("╠════════════════════════════════════════════════════════╣");
        System.out.println("║ Student ID : " + String.format("%-38d", studentId) + "║");
        System.out.println("║ Name       : " + String.format("%-38s", name) + "║");
        System.out.println("║ NIC        : " + String.format("%-38d", nic) + "║");
        System.out.println("║ Phone      : " + String.format("%-38d", phoneNumber) + "║");
        System.out.println("║ Gender     : " + String.format("%-38s", gender) + "║");
        System.out.println("║ DOB        : " + String.format("%-38s", dateOfBirth) + "║");
        System.out.println("║ Address    : " + String.format("%-38s", address) + "║");
        System.out.println("╚════════════════════════════════════════════════════════╝");
    }

    public static void new_s(Scanner scanner) {
        Student student = new Student();
        
        System.out.print("Enter student name: ");
        student.setName(scanner.nextLine().trim());
        
        System.out.print("Enter address: ");
        student.setAddress(scanner.nextLine().trim());
        
        System.out.print("Enter Gender (M/F): ");
        student.setGender(scanner.nextLine().trim());
        
        System.out.print("Enter date of birth (YYYY-MM-DD): ");
        student.setDateOfBirth(scanner.nextLine().trim());
        
        System.out.print("Enter NIC number: ");
        student.setNic(scanner.nextInt());
        scanner.nextLine(); // Clear buffer after reading int
        
        System.out.print("Enter phone number: ");
        student.setPhoneNumber(scanner.nextInt());
        scanner.nextLine(); // Clear buffer after reading int

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DatabaseUtil.getInstance().getConnection();

            String sql = "INSERT INTO student (s_name, nic, address, tp, gen, dob) VALUES (?, ?, ?, ?, ?, ?)";
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, student.getName());
                stmt.setInt(2, student.getNic());
                stmt.setString(3, student.getAddress());
                stmt.setInt(4, student.getPhoneNumber());
                stmt.setString(5, student.getGender());
                stmt.setString(6, student.getDateOfBirth());

                int rows = stmt.executeUpdate();
                if (rows > 0) {
                    System.out.println("\n╔═══════════════════════════════════════════════════════════╗");
                    System.out.println("║       ✓ STUDENT REGISTERED SUCCESSFULLY                   ║");
                    System.out.println("╠═══════════════════════════════════════════════════════════╣");
                    System.out.println("║ Student: " + String.format("%-46s", student.getName()) + "║");
                    System.out.println("║ DOB    : " + String.format("%-46s", student.getDateOfBirth()) + "║");
                    System.out.println("╚═══════════════════════════════════════════════════════════╝");
                } else {
                    System.out.println("\n✗ Failed to register student.");
                }
            }

            conn.close();
            
        } catch (SQLException e) {
            if (e.getMessage().contains("unique_student_nic")) {
                System.out.println("\n✗ A student with this NIC already exists!");
            } else {
                System.out.println("\n✗ Database Error: " + e.getMessage());
                e.printStackTrace();
            }
        } catch (Exception e) {
            System.out.println("\n✗ Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public static void viewStudents(Scanner scanner) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DatabaseUtil.getInstance().getConnection();

            String query = "SELECT s_id, s_name, nic, tp, gen, dob, address FROM student ORDER BY s_name";

            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            System.out.println("\n╔══════════════════════════════════════════════════════════════════════════════════════════════════════════════╗");
            System.out.println("║                                         ALL STUDENTS                                                         ║");
            System.out.println("╠════════╦══════════════════════╦═════════════╦══════════════╦══════════════╦══════════════════════════════════╣");
            System.out.println("║ S_ID   ║ Student Name         ║ Phone       ║ Gender       ║ DOB          ║ Address                          ║");
            System.out.println("╠════════╬══════════════════════╬═════════════╬══════════════╬══════════════╬══════════════════════════════════╣");

            while (rs.next()) {
                System.out.printf("| %-6d | %-20s | %-11d | %-12s | %-12s | %-32s |%n",
                    rs.getInt("s_id"),
                    rs.getString("s_name"),
                    rs.getInt("tp"),
                    rs.getString("gen"),
                    rs.getString("dob"),
                    rs.getString("address")
                );
            }
            System.out.println("╚════════╩══════════════════════╩═════════════╩══════════════╩══════════════╩══════════════════════════════════╝");

            rs.close();
            stmt.close();
            conn.close();

        } catch (Exception e) {
            System.out.println("\n✗ Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}