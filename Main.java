import java.util.Scanner;

public class Main {
    
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        
        // Login Screen
        Login.User user = Login.authenticate(scanner);
        
        if (user == null) {
            System.out.println("\nLogin failed. Exiting system...");
            scanner.close();
            return;
        }
        
        System.out.println("\nPress Enter to continue to main menu...");
        scanner.nextLine();
        
        // Main Application Loop
        while (true) {
            displayMainMenu();
            System.out.print("Enter your choice: ");
            
            try {
                int choice = scanner.nextInt();
                scanner.nextLine(); // Clear buffer
                
                if (choice == 0) {
                    Login.logout();
                    scanner.close();
                    System.exit(0);
                }
                
                handleMainMenuChoice(choice, scanner);
                
                System.out.println("\nPress Enter to continue...");
                scanner.nextLine();
                
            } catch (Exception e) {
                System.out.println("\n✗ Invalid input. Please enter a number.");
                scanner.nextLine(); // Clear invalid input
            }
        }
    }
    
    private static void displayMainMenu() {
        clearScreen();
        String role = Login.getLoggedInRole();
        
        System.out.println("╔═════════════════════════════════════════════════════════════════════╗");
        System.out.println("║         LEARNING INSTITUTE MANAGEMENT SYSTEM                        ║");
        System.out.println("║              Salary Calculation System (93% Formula)                ║");
        System.out.println("╠═════════════════════════════════════════════════════════════════════╣");
        System.out.println("║ Logged in as: " + String.format("%-52s", Login.getLoggedInUser() + " (" + role.toUpperCase() + ")") + "║");
        System.out.println("╠═════════════════════════════════════════════════════════════════════╣");
        System.out.println("║                         MAIN MENU                                   ║");
        System.out.println("╠═════════════════════════════════════════════════════════════════════╣");
        
        // Role-based menu display
        if (role.equalsIgnoreCase("admin")) {
            // ADMIN - Full Access
            System.out.println("║  1. Student Management                                              ║");
            System.out.println("║  2. Course Management                                               ║");
            System.out.println("║  3. Module Management                                               ║");
            System.out.println("║  4. Teacher Management                                              ║");
            System.out.println("║  5. Registration Management                                         ║");
            System.out.println("║  6. Learning & Payment Management                                   ║");
            System.out.println("║  7. Salary Management (Teacher Payments)                            ║");
            System.out.println("║  8. Reports & Statistics                                            ║");
            System.out.println("║  9. User Management                                                 ║");
            System.out.println("║  10. Change Password                                                ║");
            
        } else if (role.equalsIgnoreCase("staff")) {
            // STAFF - Limited Access
            System.out.println("║  1. Student Management                                              ║");
            System.out.println("║  4. Teacher Management                                              ║");
            System.out.println("║  6. Learning & Payment Management                                   ║");
            System.out.println("║  8. Reports & Statistics                                            ║");
            System.out.println("║  10. Change Password                                                ║");
            System.out.println("║                                                                     ║");
            System.out.println("║  Note: Limited access - Staff role                                  ║");
            
        } else if (role.equalsIgnoreCase("teacher")) {
            // TEACHER - View Salary Only
            System.out.println("║  7. View My Salary Details                                          ║");
            System.out.println("║  10. Change Password                                                ║");
            System.out.println("║                                                                     ║");
            System.out.println("║  Note: Limited access - Teacher role                                ║");
            System.out.println("║        You can view your salary information only                    ║");
        }
        
        System.out.println("║  0. Logout & Exit                                                   ║");
        System.out.println("╚═════════════════════════════════════════════════════════════════════╝");
    }
    
    private static void handleMainMenuChoice(int choice, Scanner scanner) {
        String role = Login.getLoggedInRole();
        
        switch (choice) {
            case 1: // Student Management
                if (hasAccess(role, "admin", "staff")) {
                    studentMenu(scanner);
                } else {
                    showAccessDenied("Student Management", role);
                }
                break;
                
            case 2: // Course Management
                if (hasAccess(role, "admin")) {
                    courseMenu(scanner);
                } else {
                    showAccessDenied("Course Management", role);
                }
                break;
                
            case 3: // Module Management
                if (hasAccess(role, "admin")) {
                    moduleMenu(scanner);
                } else {
                    showAccessDenied("Module Management", role);
                }
                break;
                
            case 4: // Teacher Management
                if (hasAccess(role, "admin", "staff")) {
                    teacherMenu(scanner);
                } else {
                    showAccessDenied("Teacher Management", role);
                }
                break;
                
            case 5: // Registration Management
                if (hasAccess(role, "admin")) {
                    registrationMenu(scanner);
                } else {
                    showAccessDenied("Registration Management", role);
                }
                break;
                
            case 6: // Learning & Payment Management
                if (hasAccess(role, "admin", "staff")) {
                    learningMenu(scanner);
                } else {
                    showAccessDenied("Learning & Payment Management", role);
                }
                break;
                
            case 7: // Salary Management
                if (hasAccess(role, "admin")) {
                    salaryMenu(scanner);
                } else if (hasAccess(role, "teacher")) {
                    teacherSalaryMenu(scanner);
                } else {
                    showAccessDenied("Salary Management", role);
                }
                break;
                
            case 8: // Reports & Statistics
                if (hasAccess(role, "admin", "staff")) {
                    reportsMenu(scanner);
                } else {
                    showAccessDenied("Reports & Statistics", role);
                }
                break;
                
            case 9: // User Management
                if (hasAccess(role, "admin")) {
                    userManagementMenu(scanner);
                } else {
                    showAccessDenied("User Management", role);
                }
                break;
                
            case 10: // Change Password
                if (hasAccess(role, "admin")) {
                    changePasswordMenu(scanner);
                } else {
                    showAccessDenied("Change Password", role);
                }
                break;
                
            default:
                System.out.println("\n✗ Invalid choice. Please try again.");
        }
    }
    
    // Helper method to check if user has required role
    private static boolean hasAccess(String userRole, String... allowedRoles) {
        for (String allowedRole : allowedRoles) {
            if (userRole.equalsIgnoreCase(allowedRole)) {
                return true;
            }
        }
        return false;
    }
    
    // Show access denied message
    private static void showAccessDenied(String featureName, String userRole) {
        System.out.println("\n╔═══════════════════════════════════════════════════════════════════════════════╗");
        System.out.println("║                         ✗ ACCESS DENIED                                       ║");
        System.out.println("╠═══════════════════════════════════════════════════════════════════════════════╣");
        System.out.println("║ Feature    : " + String.format("%-64s", featureName) + "║");
        System.out.println("║ Your Role  : " + String.format("%-64s", userRole.toUpperCase()) + "║");
        System.out.println("║                                                                               ║");
        System.out.println("║ You do not have permission to access this feature.                            ║");
        System.out.println("║ Please contact your system administrator.                                     ║");
        System.out.println("╚═══════════════════════════════════════════════════════════════════════════════╝");
    }
    
    // User Management Menu (Admin Only)
    private static void userManagementMenu(Scanner scanner) {
        clearScreen();
        System.out.println("╔═════════════════════════════════════════════════════════╗");
        System.out.println("║           USER MANAGEMENT (Admin Only)                  ║");
        System.out.println("╠═════════════════════════════════════════════════════════╣");
        System.out.println("║  1. Register New User                                   ║");
        System.out.println("║  2. View All Users                                      ║");
        System.out.println("║  0. Back to Main Menu                                   ║");
        System.out.println("╚═════════════════════════════════════════════════════════╝");
        System.out.print("Enter your choice: ");
        
        int choice = scanner.nextInt();
        scanner.nextLine();
        
        switch (choice) {
            case 1:
                Login.registerUser(scanner);
                break;
            case 2:
                Login.viewAllUsers(scanner);
                break;
            case 0:
                return;
            default:
                System.out.println("\n✗ Invalid choice.");
        }
    }
    
    // Change Password Menu (Admin Only)
    private static void changePasswordMenu(Scanner scanner) {
        clearScreen();
        System.out.println("╔═════════════════════════════════════════════════════════╗");
        System.out.println("║           PASSWORD MANAGEMENT (Admin Only)              ║");
        System.out.println("╠═════════════════════════════════════════════════════════╣");
        System.out.println("║  1. Change My Password                                  ║");
        System.out.println("║  2. Reset User Password (Admin)                         ║");
        System.out.println("║  0. Back to Main Menu                                   ║");
        System.out.println("╚═════════════════════════════════════════════════════════╝");
        System.out.print("Enter your choice: ");
        
        int choice = scanner.nextInt();
        scanner.nextLine();
        
        switch (choice) {
            case 1:
                Login.changePassword(scanner);
                break;
            case 2:
                System.out.println("\n⚠ Feature coming soon: Reset any user's password");
                break;
            case 0:
                return;
            default:
                System.out.println("\n✗ Invalid choice.");
        }
    }
    
    // Teacher Salary Menu (Teachers - View Only)
    private static void teacherSalaryMenu(Scanner scanner) {
        clearScreen();
        System.out.println("╔═════════════════════════════════════════════════════════╗");
        System.out.println("║           MY SALARY INFORMATION (Teacher)               ║");
        System.out.println("╠═════════════════════════════════════════════════════════╣");
        System.out.println("║  1. View My Salary Details                              ║");
        System.out.println("║  2. View My Revenue Breakdown                           ║");
        System.out.println("║  0. Back to Main Menu                                   ║");
        System.out.println("╚═════════════════════════════════════════════════════════╝");
        System.out.print("Enter your choice: ");
        
        int choice = scanner.nextInt();
        scanner.nextLine();
        
        switch (choice) {
            case 1:
                viewMyTeacherSalary(scanner);
                break;
            case 2:
                viewMyTeacherRevenue(scanner);
                break;
            case 0:
                return;
            default:
                System.out.println("\n✗ Invalid choice.");
        }
    }
    
    // View salary for logged-in teacher
    private static void viewMyTeacherSalary(Scanner scanner) {
        String username = Login.getLoggedInUser();
        
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            java.sql.Connection conn = lib.DatabaseUtil.getInstance().getConnection();
            
            // Get teacher ID from username (assuming username matches teacher name)
            String query = 
                "SELECT s.t_id, t.t_name, m.m_name, s.student_count, s.total_revenue, " +
                "s.salary, s.paid, s.payment_date " +
                "FROM salary s " +
                "INNER JOIN teacher t ON s.t_id = t.t_id " +
                "INNER JOIN modules m ON t.m_id = m.m_id " +
                "WHERE t.t_name = ? " +
                "ORDER BY s.payment_date DESC";
            
            java.sql.PreparedStatement ps = conn.prepareStatement(query);
            ps.setString(1, username);
            java.sql.ResultSet rs = ps.executeQuery();
            
            System.out.println("\n╔════════════════════════════════════════════════════════════════════════════════════════════════╗");
            System.out.println("║                                  MY SALARY HISTORY                                             ║");
            System.out.println("╠════════════════════════════╦═══════════╦════════════╦══════════════╦════════════╦═════════════╣");
            System.out.println("║ Module Name                ║ Students  ║ Revenue    ║ Salary (93%) ║ Status     ║ Date        ║");
            System.out.println("╠════════════════════════════╬═══════════╬════════════╬══════════════╬════════════╬═════════════╣");
            
            boolean hasRecords = false;
            int totalSalary = 0;
            
            while (rs.next()) {
                hasRecords = true;
                int salary = rs.getInt("salary");
                totalSalary += salary;
                
                System.out.printf("║ %-26s ║ %-9d ║ %-10d ║ %-12d ║ %-10s ║ %-11s ║%n",
                    rs.getString("m_name"),
                    rs.getInt("student_count"),
                    rs.getInt("total_revenue"),
                    salary,
                    rs.getString("paid"),
                    rs.getString("payment_date") != null ? 
                        rs.getString("payment_date").substring(0, 10) : "N/A"
                );
            }
            
            if (!hasRecords) {
                System.out.println("║ No salary records found for your account.                                                     ║");
            } else {
                System.out.println("╠════════════════════════════╩═══════════╩════════════╬══════════════╩════════════╩═════════════╣");
                System.out.printf("║ TOTAL SALARY RECEIVED                                ║ Rs. %-37d ║%n", totalSalary);
                System.out.println("╚══════════════════════════════════════════════════════╩═══════════════════════════════════════╝");
            }
            
            rs.close();
            ps.close();
            conn.close();
            
        } catch (Exception e) {
            System.out.println("\n✗ Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // View revenue breakdown for logged-in teacher
    private static void viewMyTeacherRevenue(Scanner scanner) {
        String username = Login.getLoggedInUser();
        
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            java.sql.Connection conn = lib.DatabaseUtil.getInstance().getConnection();
            
            String query = 
                "SELECT t.t_id, t.t_name, m.m_name, m.cost " +
                "FROM teacher t " +
                "INNER JOIN modules m ON t.m_id = m.m_id " +
                "WHERE t.t_name = ?";
            
            java.sql.PreparedStatement ps = conn.prepareStatement(query);
            ps.setString(1, username);
            java.sql.ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                int teacherId = rs.getInt("t_id");
                String moduleName = rs.getString("m_name");
                int moduleCost = rs.getInt("cost");
                
                // Now get student payment details
                salary.viewTeacherRevenue(scanner);
                
            } else {
                System.out.println("\n✗ Teacher record not found for your account.");
            }
            
            rs.close();
            ps.close();
            conn.close();
            
        } catch (Exception e) {
            System.out.println("\n✗ Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // Student Menu (Admin + Staff)
    private static void studentMenu(Scanner scanner) {
        clearScreen();
        System.out.println("╔═════════════════════════════════════════════════════════╗");
        System.out.println("║           STUDENT MANAGEMENT                            ║");
        System.out.println("╠═════════════════════════════════════════════════════════╣");
        System.out.println("║  1. Register New Student                                ║");
        System.out.println("║  2. View All Students                                   ║");
        System.out.println("║  0. Back to Main Menu                                   ║");
        System.out.println("╚═════════════════════════════════════════════════════════╝");
        System.out.print("Enter your choice: ");
        
        int choice = scanner.nextInt();
        scanner.nextLine();
        
        switch (choice) {
            case 1:
                Student.new_s(scanner);
                break;
            case 2:
                Student.viewStudents(scanner);
                break;
            case 0:
                return;
            default:
                System.out.println("\n✗ Invalid choice.");
        }
    }
    
    // Course Menu (Admin Only)
    private static void courseMenu(Scanner scanner) {
        clearScreen();
        System.out.println("╔═════════════════════════════════════════════════════════╗");
        System.out.println("║           COURSE MANAGEMENT                             ║");
        System.out.println("╠═════════════════════════════════════════════════════════╣");
        System.out.println("║  1. Add New Course                                      ║");
        System.out.println("║  2. View All Courses                                    ║");
        System.out.println("║  0. Back to Main Menu                                   ║");
        System.out.println("╚═════════════════════════════════════════════════════════╝");
        System.out.print("Enter your choice: ");
        
        int choice = scanner.nextInt();
        scanner.nextLine();
        
        switch (choice) {
            case 1:
                course.new_c(scanner);
                break;
            case 2:
                course.viewCourses(scanner);
                break;
            case 0:
                return;
            default:
                System.out.println("\n✗ Invalid choice.");
        }
    }
    
    // Module Menu (Admin Only)
    private static void moduleMenu(Scanner scanner) {
        clearScreen();
        System.out.println("╔═════════════════════════════════════════════════════════╗");
        System.out.println("║           MODULE MANAGEMENT                             ║");
        System.out.println("╠═════════════════════════════════════════════════════════╣");
        System.out.println("║  1. Add New Module                                      ║");
        System.out.println("║  2. View All Modules                                    ║");
        System.out.println("║  0. Back to Main Menu                                   ║");
        System.out.println("╚═════════════════════════════════════════════════════════╝");
        System.out.print("Enter your choice: ");
        
        int choice = scanner.nextInt();
        scanner.nextLine();
        
        switch (choice) {
            case 1:
                Modules.new_m(scanner);
                break;
            case 2:
                Modules.viewModules(scanner);
                break;
            case 0:
                return;
            default:
                System.out.println("\n✗ Invalid choice.");
        }
    }
    
    // Teacher Menu (Admin + Staff)
    private static void teacherMenu(Scanner scanner) {
        clearScreen();
        System.out.println("╔═════════════════════════════════════════════════════════╗");
        System.out.println("║           TEACHER MANAGEMENT                            ║");
        System.out.println("╠═════════════════════════════════════════════════════════╣");
        System.out.println("║  1. Register New Teacher                                ║");
        System.out.println("║  2. View All Teachers                                   ║");
        System.out.println("║  0. Back to Main Menu                                   ║");
        System.out.println("╚═════════════════════════════════════════════════════════╝");
        System.out.print("Enter your choice: ");
        
        int choice = scanner.nextInt();
        scanner.nextLine();
        
        switch (choice) {
            case 1:
                Teacher.new_t(scanner);
                break;
            case 2:
                Teacher.viewTeachers(scanner);
                break;
            case 0:
                return;
            default:
                System.out.println("\n✗ Invalid choice.");
        }
    }
    
    // Registration Menu (Admin Only)
    private static void registrationMenu(Scanner scanner) {
        clearScreen();
        System.out.println("╔═════════════════════════════════════════════════════════╗");
        System.out.println("║      COURSE REGISTRATION MANAGEMENT                     ║");
        System.out.println("╠═════════════════════════════════════════════════════════╣");
        System.out.println("║  1. Register Student to Course                          ║");
        System.out.println("║  2. View All Registrations                              ║");
        System.out.println("║  0. Back to Main Menu                                   ║");
        System.out.println("╚═════════════════════════════════════════════════════════╝");
        System.out.print("Enter your choice: ");
        
        int choice = scanner.nextInt();
        scanner.nextLine();
        
        switch (choice) {
            case 1:
                registration.registrationcourse(scanner);
                break;
            case 2:
                registration.viewRegistrations(scanner);
                break;
            case 0:
                return;
            default:
                System.out.println("\n✗ Invalid choice.");
        }
    }
    
    // Learning Menu (Admin + Staff)
    private static void learningMenu(Scanner scanner) {
        clearScreen();
        System.out.println("╔═════════════════════════════════════════════════════════╗");
        System.out.println("║      LEARNING & PAYMENT MANAGEMENT                      ║");
        System.out.println("╠═════════════════════════════════════════════════════════╣");
        System.out.println("║  1. Process Student Payment for Module                  ║");
        System.out.println("║  2. View Payment History                                ║");
        System.out.println("║  0. Back to Main Menu                                   ║");
        System.out.println("╚═════════════════════════════════════════════════════════╝");
        System.out.print("Enter your choice: ");
        
        int choice = scanner.nextInt();
        scanner.nextLine();
        
        switch (choice) {
            case 1:
                learning.learningAvailable(scanner);
                break;
            case 2:
                learning.viewPaymentHistory(scanner);
                break;
            case 0:
                return;
            default:
                System.out.println("\n✗ Invalid choice.");
        }
    }
    
    // Salary Menu (Admin Only - Full Access)
    private static void salaryMenu(Scanner scanner) {
        clearScreen();
        System.out.println("╔═════════════════════════════════════════════════════════╗");
        System.out.println("║        SALARY MANAGEMENT (93% Formula)                  ║");
        System.out.println("╠═════════════════════════════════════════════════════════╣");
        System.out.println("║  1. Calculate & Process Teacher Salary                  ║");
        System.out.println("║  2. View Salary Payment History                         ║");
        System.out.println("║  3. View Teacher Revenue Breakdown                      ║");
        System.out.println("║  0. Back to Main Menu                                   ║");
        System.out.println("╚═════════════════════════════════════════════════════════╝");
        System.out.print("Enter your choice: ");
        
        int choice = scanner.nextInt();
        scanner.nextLine();
        
        switch (choice) {
            case 1:
                salary.salary_teacher(scanner);
                break;
            case 2:
                salary.viewSalaryHistory(scanner);
                break;
            case 3:
                salary.viewTeacherRevenue(scanner);
                break;
            case 0:
                return;
            default:
                System.out.println("\n✗ Invalid choice.");
        }
    }
    
    // Reports Menu (Admin + Staff)
    private static void reportsMenu(Scanner scanner) {
        clearScreen();
        System.out.println("╔═════════════════════════════════════════════════════════╗");
        System.out.println("║           REPORTS & STATISTICS                          ║");
        System.out.println("╠═════════════════════════════════════════════════════════╣");
        System.out.println("║  1. Course Statistics                                   ║");
        System.out.println("║  2. Module Statistics                                   ║");
        System.out.println("║  3. Financial Summary                                   ║");
        System.out.println("║  4. Student Registrations                               ║");
        System.out.println("║  0. Back to Main Menu                                   ║");
        System.out.println("╚═════════════════════════════════════════════════════════╝");
        System.out.print("Enter your choice: ");
        
        int choice = scanner.nextInt();
        scanner.nextLine();
        
        switch (choice) {
            case 1:
                course.viewCourses(scanner);
                break;
            case 2:
                Modules.viewModules(scanner);
                break;
            case 3:
                salary.viewSalaryHistory(scanner);
                break;
            case 4:
                registration.viewRegistrations(scanner);
                break;
            case 0:
                return;
            default:
                System.out.println("\n✗ Invalid choice.");
        }
    }
    
    private static void clearScreen() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }
}