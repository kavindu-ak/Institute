import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import lib.DatabaseUtil;

public class Teacher extends Person {
    private int teacherId;
    private int moduleId;
    
    // Inner module class
    static class Module {
        int m_id;
        String m_name;
        int c_id;
        String c_name;
        boolean hasTeacher;
        String teacherName;

        Module(int m_id, String m_name, int c_id, String c_name, boolean hasTeacher, String teacherName) {
            this.m_id = m_id;
            this.m_name = m_name;
            this.c_id = c_id;
            this.c_name = c_name;
            this.hasTeacher = hasTeacher;
            this.teacherName = teacherName;
        }

        @Override
        public String toString() {
            String status = hasTeacher ? "✓ " + teacherName : "Available";
            return String.format("| %-8d | %-35s | %-30s | %-20s |", 
                m_id, m_name, c_name, status);
        }
    }
    
    // Constructors
    public Teacher() {
        super();
    }
    
    public Teacher(String name, String address, String gender, int nic, int phoneNumber, int moduleId) {
        super(name, address, gender, nic, phoneNumber);
        this.moduleId = moduleId;
    }
    
    public Teacher(int teacherId, String name, String address, String gender, int nic, int phoneNumber, int moduleId) {
        super(name, address, gender, nic, phoneNumber);
        this.teacherId = teacherId;
        this.moduleId = moduleId;
    }
    
    // Getters and Setters
    public int getTeacherId() {
        return teacherId;
    }
    
    public void setTeacherId(int teacherId) {
        this.teacherId = teacherId;
    }
    
    public int getModuleId() {
        return moduleId;
    }
    
    public void setModuleId(int moduleId) {
        this.moduleId = moduleId;
    }
    
    @Override
    public void displayInfo() {
        System.out.println("╔════════════════════════════════════════════════════════╗");
        System.out.println("║              TEACHER INFORMATION                       ║");
        System.out.println("╠════════════════════════════════════════════════════════╣");
        System.out.println("║ Teacher ID : " + String.format("%-38d", teacherId) + "║");
        System.out.println("║ Name       : " + String.format("%-38s", name) + "║");
        System.out.println("║ NIC        : " + String.format("%-38d", nic) + "║");
        System.out.println("║ Phone      : " + String.format("%-38d", phoneNumber) + "║");
        System.out.println("║ Gender     : " + String.format("%-38s", gender) + "║");
        System.out.println("║ Address    : " + String.format("%-38s", address) + "║");
        System.out.println("║ Module ID  : " + String.format("%-38d", moduleId) + "║");
        System.out.println("╚════════════════════════════════════════════════════════╝");
    }

    public static void new_t(Scanner scanner) {
        Teacher teacher = new Teacher();
        
        System.out.println("Enter teacher name: ");
        teacher.setName(scanner.nextLine().trim());
        
        System.out.println("Enter address: ");
        teacher.setAddress(scanner.nextLine().trim());
        
        System.out.println("Enter Gender (M/F): ");
        teacher.setGender(scanner.nextLine().trim());
        
        System.out.println("Enter NIC number: ");
        teacher.setNic(scanner.nextInt());
        scanner.nextLine(); // Clear buffer
        
        System.out.println("Enter phone number: ");
        teacher.setPhoneNumber(scanner.nextInt());
        scanner.nextLine(); // Clear buffer

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DatabaseUtil.getInstance().getConnection();

            // Fetch modules with teacher assignment status
            List<Module> moduleList = new ArrayList<>();
            
            String query = 
                "SELECT m.m_id, m.m_name, m.c_id, c.c_name, " +
                "t.t_id, t.t_name " +
                "FROM modules m " +
                "INNER JOIN course c ON m.c_id = c.c_id " +
                "LEFT JOIN teacher t ON m.m_id = t.m_id " +
                "ORDER BY c.c_name, m.m_name";
            
            Statement moduleStmt = conn.createStatement();
            ResultSet moduleRs = moduleStmt.executeQuery(query);

            System.out.println("\n╔══════════════════════════════════════════════════════════════════════════════════════════════════════╗");
            System.out.println("║                                    AVAILABLE MODULES                                                 ║");
            System.out.println("╠══════════╦═════════════════════════════════════╦════════════════════════════════╦══════════════════════╣");
            System.out.println("║ Module ID║ Module Name                         ║ Course Name                    ║ Teacher Status       ║");
            System.out.println("╠══════════╬═════════════════════════════════════╬════════════════════════════════╬══════════════════════╣");
            
            List<Module> availableModules = new ArrayList<>();
            
            while (moduleRs.next()) {
                boolean hasTeacher = moduleRs.getInt("t_id") > 0;
                String teacherName = moduleRs.getString("t_name");
                
                Module mod = new Module(
                    moduleRs.getInt("m_id"),
                    moduleRs.getString("m_name"),
                    moduleRs.getInt("c_id"),
                    moduleRs.getString("c_name"),
                    hasTeacher,
                    teacherName
                );
                moduleList.add(mod);
                System.out.println(mod);
                
                // Keep track of available modules
                if (!hasTeacher) {
                    availableModules.add(mod);
                }
            }
            System.out.println("╚══════════╩═════════════════════════════════════╩════════════════════════════════╩══════════════════════╝");

            moduleRs.close();
            moduleStmt.close();

            if (availableModules.isEmpty()) {
                System.out.println("\n⚠ All modules already have teachers assigned!");
                System.out.println("   Please add more modules before registering new teachers.");
                conn.close();
                return;
            }

            System.out.println("\n✓ Available modules (without teachers): " + availableModules.size());
            System.out.println("  Note: You can only assign teachers to modules marked as 'Available'");

        } catch (Exception e) {
            System.out.println("\n✗ Error fetching modules: " + e.getMessage());
            e.printStackTrace();
            return;
        }

        int m_id = promptInputI(scanner, "\nEnter module ID to assign: ");
        scanner.nextLine(); // Clear buffer
        teacher.setModuleId(m_id);

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DatabaseUtil.getInstance().getConnection();

            // Check if module already has a teacher
            String checkQuery = "SELECT t_id, t_name FROM teacher WHERE m_id = ?";
            PreparedStatement checkStmt = conn.prepareStatement(checkQuery);
            checkStmt.setInt(1, m_id);
            ResultSet checkRs = checkStmt.executeQuery();

            if (checkRs.next()) {
                String existingTeacher = checkRs.getString("t_name");
                System.out.println("\n✗ Module already has a teacher assigned!");
                System.out.println("   Current teacher: " + existingTeacher);
                System.out.println("   Each module can have only ONE teacher.");
                checkRs.close();
                checkStmt.close();
                conn.close();
                return;
            }
            checkRs.close();
            checkStmt.close();

            // Check if module exists
            String moduleCheckQuery = "SELECT m_name FROM modules WHERE m_id = ?";
            PreparedStatement moduleCheckStmt = conn.prepareStatement(moduleCheckQuery);
            moduleCheckStmt.setInt(1, m_id);
            ResultSet moduleCheckRs = moduleCheckStmt.executeQuery();

            if (!moduleCheckRs.next()) {
                System.out.println("\n✗ Invalid module ID. Module not found.");
                moduleCheckRs.close();
                moduleCheckStmt.close();
                conn.close();
                return;
            }
            
            String moduleName = moduleCheckRs.getString("m_name");
            moduleCheckRs.close();
            moduleCheckStmt.close();

            // Insert teacher
            String sql = "INSERT INTO teacher (t_name, nic, address, tp, gen, m_id) VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, teacher.getName());
            stmt.setInt(2, teacher.getNic());
            stmt.setString(3, teacher.getAddress());
            stmt.setInt(4, teacher.getPhoneNumber());
            stmt.setString(5, teacher.getGender());
            stmt.setInt(6, teacher.getModuleId());

            int rows = stmt.executeUpdate();
            if (rows > 0) {
                System.out.println("\n╔═══════════════════════════════════════════════════════════╗");
                System.out.println("║       ✓ TEACHER REGISTERED SUCCESSFULLY                   ║");
                System.out.println("╠═══════════════════════════════════════════════════════════╣");
                System.out.println("║ Teacher: " + String.format("%-46s", teacher.getName()) + "║");
                System.out.println("║ Module : " + String.format("%-46s", moduleName) + "║");
                System.out.println("╚═══════════════════════════════════════════════════════════╝");
            } else {
                System.out.println("\n✗ Failed to register teacher.");
            }

            stmt.close();
            conn.close();
            
        } catch (SQLIntegrityConstraintViolationException e) {
            if (e.getMessage().contains("unique_module_teacher")) {
                System.out.println("\n✗ This module already has a teacher assigned!");
                System.out.println("   Each module can have only ONE teacher.");
            } else if (e.getMessage().contains("unique_teacher_nic")) {
                System.out.println("\n✗ A teacher with this NIC already exists!");
            } else {
                System.out.println("\n✗ Database constraint error: " + e.getMessage());
            }
        } catch (SQLException e) {
            System.out.println("\n✗ Database Error: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println("\n✗ Error registering teacher: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void viewTeachers(Scanner scanner) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DatabaseUtil.getInstance().getConnection();

            String query = 
                "SELECT t.t_id, t.t_name, t.nic, t.tp, t.gen, " +
                "m.m_name, c.c_name, t.paid " +
                "FROM teacher t " +
                "INNER JOIN modules m ON t.m_id = m.m_id " +
                "INNER JOIN course c ON m.c_id = c.c_id " +
                "ORDER BY t.t_name";

            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            System.out.println("\n╔══════════════════════════════════════════════════════════════════════════════════════════════════════════════╗");
            System.out.println("║                                         ALL TEACHERS                                                         ║");
            System.out.println("╠════════╦══════════════════════╦═════════════╦══════════════╦══════════════════════╦═══════════════════════╣");
            System.out.println("║ T_ID   ║ Teacher Name         ║ Phone       ║ Gender       ║ Module               ║ Course                ║");
            System.out.println("╠════════╬══════════════════════╬═════════════╬══════════════╬══════════════════════╬═══════════════════════╣");

            while (rs.next()) {
                System.out.printf("| %-6d | %-20s | %-11d | %-12s | %-20s | %-21s |%n",
                    rs.getInt("t_id"),
                    rs.getString("t_name"),
                    rs.getInt("tp"),
                    rs.getString("gen"),
                    rs.getString("m_name"),
                    rs.getString("c_name")
                );
            }
            System.out.println("╚════════╩══════════════════════╩═════════════╩══════════════╩══════════════════════╩═══════════════════════╝");

            rs.close();
            stmt.close();
            conn.close();

        } catch (Exception e) {
            System.out.println("\n✗ Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}