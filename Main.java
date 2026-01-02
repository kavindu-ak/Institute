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
        System.out.println("╔═════════════════════════════════════════════════════════════════════╗");
        System.out.println("║         LEARNING INSTITUTE MANAGEMENT SYSTEM                        ║");
        System.out.println("║              Salary Calculation System (93% Formula)                ║");
        System.out.println("╠═════════════════════════════════════════════════════════════════════╣");
        System.out.println("║ Logged in as: " + String.format("%-52s", Login.getLoggedInUser() + " (" + Login.getLoggedInRole().toUpperCase() + ")") + "║");
        System.out.println("╠═════════════════════════════════════════════════════════════════════╣");
        System.out.println("║                         MAIN MENU                                   ║");
        System.out.println("╠═════════════════════════════════════════════════════════════════════╣");
        System.out.println("║  1. Student Management                                  (Admin)     ║");
        System.out.println("║  2. Course Management                                   (Admin)     ║");
        System.out.println("║  3. Module Management                                   (Admin)     ║");
        System.out.println("║  4. Teacher Management                                  (Admin)     ║");
        System.out.println("║  5. Registration Management                             (Admin)     ║");
        System.out.println("║  6. Learning & Payment Management                       (Admin)     ║");
        System.out.println("║  7. Salary Management (Teacher Payments)                (Admin)     ║");
        System.out.println("║  8. Reports & Statistics                                (Admin)     ║");
        System.out.println("║  9. User Management                                     (Admin)     ║");
        System.out.println("║  10. Change Password                                                ║");
        System.out.println("║  0. Logout & Exit                                                   ║");
        System.out.println("╚═════════════════════════════════════════════════════════════════════╝");
        
        if (!Login.hasRole("admin")) {
            System.out.println("\n⚠ Note: You are logged in as " + Login.getLoggedInRole().toUpperCase());
            System.out.println("   Most features require ADMIN privileges.");
        }
    }
    
    private static void handleMainMenuChoice(int choice, Scanner scanner) {
        // Check if user is admin for restricted features
        boolean isAdmin = Login.hasRole("admin");
        
        switch (choice) {
            case 1:
                if (checkAdminAccess(isAdmin, "Student Management")) {
                    studentMenu(scanner);
                }
                break;
            case 2:
                if (checkAdminAccess(isAdmin, "Course Management")) {
                    courseMenu(scanner);
                }
                break;
            case 3:
                if (checkAdminAccess(isAdmin, "Module Management")) {
                    moduleMenu(scanner);
                }
                break;
            case 4:
                if (checkAdminAccess(isAdmin, "Teacher Management")) {
                    teacherMenu(scanner);
                }
                break;
            case 5:
                if (checkAdminAccess(isAdmin, "Registration Management")) {
                    registrationMenu(scanner);
                }
                break;
            case 6:
                if (checkAdminAccess(isAdmin, "Learning & Payment Management")) {
                    learningMenu(scanner);
                }
                break;
            case 7:
                if (checkAdminAccess(isAdmin, "Salary Management")) {
                    salaryMenu(scanner);
                }
                break;
            case 8:
                if (checkAdminAccess(isAdmin, "Reports & Statistics")) {
                    reportsMenu(scanner);
                }
                break;
            case 9:
                if (isAdmin) {
                    userManagementMenu(scanner);
                } else {
                    System.out.println("\n✗ Access Denied! Admin privileges required.");
                }
                break;
            case 10:
                Login.changePassword(scanner);
                break;
            default:
                System.out.println("\n✗ Invalid choice. Please try again.");
        }
    }
    
    private static boolean checkAdminAccess(boolean isAdmin, String featureName) {
        if (!isAdmin) {
            System.out.println("\n╔═══════════════════════════════════════════════════════════════════════════════╗");
            System.out.println("║                         ✗ ACCESS DENIED                                       ║");
            System.out.println("╠═══════════════════════════════════════════════════════════════════════════════╣");
            System.out.println("║ Feature: " + String.format("%-67s", featureName) + "║");
            System.out.println("║                                                                               ║");
            System.out.println("║ This feature requires ADMIN privileges.                                       ║");
            System.out.println("║ Please contact your system administrator.                                     ║");
            System.out.println("╚═══════════════════════════════════════════════════════════════════════════════╝");
            return false;
        }
        return true;
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
    
    // Student Menu
    private static void studentMenu(Scanner scanner) {
        clearScreen();
        System.out.println("╔═════════════════════════════════════════════════════════╗");
        System.out.println("║           STUDENT MANAGEMENT                            ║");
        System.out.println("╠═════════════════════════════════════════════════════════╣");
        System.out.println("║  1. Register New Student                                ║");
        System.out.println("║  0. Back to Main Menu                                   ║");
        System.out.println("╚═════════════════════════════════════════════════════════╝");
        System.out.print("Enter your choice: ");
        
        int choice = scanner.nextInt();
        scanner.nextLine();
        
        switch (choice) {
            case 1:
                student.new_s(scanner);
                break;
            case 0:
                return;
            default:
                System.out.println("\n✗ Invalid choice.");
        }
    }
    
    // Course Menu
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
    
    // Module Menu
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
    
    // Teacher Menu
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
    
    // Registration Menu
    private static void registrationMenu(Scanner scanner) {
        clearScreen();
        System.out.println("╔═════════════════════════════════════════════════════════╗");
        System.out.println("║      COURSE REGISTRATION MANAGEMENT                     ║");
        System.out.println("╠═════════════════════════════════════════════════════════╣");
        System.out.println("║  1. Register Student to Course                          ║");
        System.out.println("║  0. Back to Main Menu                                   ║");
        System.out.println("╚═════════════════════════════════════════════════════════╝");
        System.out.print("Enter your choice: ");
        
        int choice = scanner.nextInt();
        scanner.nextLine();
        
        switch (choice) {
            case 1:
                registration.registrationcourse(scanner);
                break;
            case 0:
                return;
            default:
                System.out.println("\n✗ Invalid choice.");
        }
    }
    
    // Learning Menu
    private static void learningMenu(Scanner scanner) {
        clearScreen();
        System.out.println("╔═════════════════════════════════════════════════════════╗");
        System.out.println("║      LEARNING & PAYMENT MANAGEMENT                      ║");
        System.out.println("╠═════════════════════════════════════════════════════════╣");
        System.out.println("║  1. Process Student Payment for Module                  ║");
        System.out.println("║  0. Back to Main Menu                                   ║");
        System.out.println("╚═════════════════════════════════════════════════════════╝");
        System.out.print("Enter your choice: ");
        
        int choice = scanner.nextInt();
        scanner.nextLine();
        
        switch (choice) {
            case 1:
                learning.learningAvailable(scanner);
                break;
            case 0:
                return;
            default:
                System.out.println("\n✗ Invalid choice.");
        }
    }
    
    // Salary Menu
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
    
    // Reports Menu
    private static void reportsMenu(Scanner scanner) {
        clearScreen();
        System.out.println("╔═════════════════════════════════════════════════════════╗");
        System.out.println("║           REPORTS & STATISTICS                          ║");
        System.out.println("╠═════════════════════════════════════════════════════════╣");
        System.out.println("║  1. Course Statistics                                   ║");
        System.out.println("║  2. Module Statistics                                   ║");
        System.out.println("║  3. Financial Summary                                   ║");
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