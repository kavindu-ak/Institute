import java.sql.*;
import java.util.Scanner;
import lib.DatabaseUtil;

public class Login {
    
    private static String loggedInUser = null;
    private static String loggedInRole = null;
    private static int loggedInUserId = -1;
    
    public static class User {
        int userId;
        String username;
        String password;
        String role;
        String fullName;
        
        public User(int userId, String username, String role, String fullName) {
            this.userId = userId;
            this.username = username;
            this.role = role;
            this.fullName = fullName;
        }
    }
    
    public static User authenticate(Scanner scanner) {
        System.out.println("\n╔═══════════════════════════════════════════════════════════════════════════════╗");
        System.out.println("║                    LEARNING INSTITUTE MANAGEMENT SYSTEM                       ║");
        System.out.println("║                           LOGIN PAGE                                          ║");
        System.out.println("╚═══════════════════════════════════════════════════════════════════════════════╝");
        
        int attempts = 0;
        int maxAttempts = 3;
        
        while (attempts < maxAttempts) {
            System.out.print("\n➤ Username: ");
            String username = scanner.nextLine().trim();
            
            System.out.print("➤ Password: ");
            String password = scanner.nextLine().trim();
            
            if (username.isEmpty() || password.isEmpty()) {
                System.out.println("\n✗ Username and password cannot be empty!");
                attempts++;
                continue;
            }
            
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                Connection conn = DatabaseUtil.getInstance().getConnection();
                
                String query = "SELECT user_id, username, password, role, full_name FROM users WHERE username = ?";
                PreparedStatement stmt = conn.prepareStatement(query);
                stmt.setString(1, username);
                
                ResultSet rs = stmt.executeQuery();
                
                if (rs.next()) {
                    String storedPassword = rs.getString("password");
                    
                    // Simple password verification (in production, use hashing)
                    if (password.equals(storedPassword)) {
                        User user = new User(
                            rs.getInt("user_id"),
                            rs.getString("username"),
                            rs.getString("role"),
                            rs.getString("full_name")
                        );
                        
                        loggedInUser = user.username;
                        loggedInRole = user.role;
                        loggedInUserId = user.userId;
                        
                        System.out.println("\n╔═══════════════════════════════════════════════════════════════════════════════╗");
                        System.out.println("║                         ✓ LOGIN SUCCESSFUL                                    ║");
                        System.out.println("╠═══════════════════════════════════════════════════════════════════════════════╣");
                        System.out.println("║ Welcome: " + String.format("%-67s", user.fullName) + "║");
                        System.out.println("║ Role   : " + String.format("%-67s", user.role.toUpperCase()) + "║");
                        System.out.println("╚═══════════════════════════════════════════════════════════════════════════════╝");
                        
                        rs.close();
                        stmt.close();
                        conn.close();
                        
                        return user;
                    } else {
                        System.out.println("\n✗ Incorrect password!");
                    }
                } else {
                    System.out.println("\n✗ User not found!");
                }
                
                rs.close();
                stmt.close();
                conn.close();
                
            } catch (Exception e) {
                System.out.println("\n✗ Login Error: " + e.getMessage());
                e.printStackTrace();
            }
            
            attempts++;
            int remaining = maxAttempts - attempts;
            if (remaining > 0) {
                System.out.println("⚠ Attempts remaining: " + remaining);
            }
        }
        
        System.out.println("\n✗ Maximum login attempts exceeded. System exiting...");
        return null;
    }
    
    public static void logout() {
        System.out.println("\n╔═══════════════════════════════════════════════════════════════════════════════╗");
        System.out.println("║                      ✓ LOGGED OUT SUCCESSFULLY                                ║");
        System.out.println("║                         Goodbye, " + String.format("%-45s", loggedInUser) + "║");
        System.out.println("╚═══════════════════════════════════════════════════════════════════════════════╝");
        
        loggedInUser = null;
        loggedInRole = null;
        loggedInUserId = -1;
    }
    
    public static String getLoggedInUser() {
        return loggedInUser;
    }
    
    public static String getLoggedInRole() {
        return loggedInRole;
    }
    
    public static int getLoggedInUserId() {
        return loggedInUserId;
    }
    
    public static boolean isLoggedIn() {
        return loggedInUser != null;
    }
    
    public static boolean hasRole(String role) {
        return loggedInRole != null && loggedInRole.equalsIgnoreCase(role);
    }
    
    public static void registerUser(Scanner scanner) {
        System.out.println("\n╔═══════════════════════════════════════════════════════════════════════════════╗");
        System.out.println("║                         NEW USER REGISTRATION                                 ║");
        System.out.println("╚═══════════════════════════════════════════════════════════════════════════════╝");
        
        System.out.print("\n➤ Full Name: ");
        String fullName = scanner.nextLine().trim();
        
        System.out.print("➤ Username: ");
        String username = scanner.nextLine().trim();
        
        System.out.print("➤ Password: ");
        String password = scanner.nextLine().trim();
        
        System.out.print("➤ Confirm Password: ");
        String confirmPassword = scanner.nextLine().trim();
        
        if (!password.equals(confirmPassword)) {
            System.out.println("\n✗ Passwords do not match!");
            return;
        }
        
        System.out.println("\n📋 Select Role:");
        System.out.println("   1. Admin");
        System.out.println("   2. Teacher");
        System.out.println("   3. Staff");
        System.out.print("➤ Choice: ");
        int roleChoice = scanner.nextInt();
        scanner.nextLine();
        
        String role;
        switch (roleChoice) {
            case 1:
                role = "admin";
                break;
            case 2:
                role = "teacher";
                break;
            case 3:
                role = "staff";
                break;
            default:
                System.out.println("\n✗ Invalid role!");
                return;
        }
        
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DatabaseUtil.getInstance().getConnection();
            
            // Check if username exists
            String checkQuery = "SELECT username FROM users WHERE username = ?";
            PreparedStatement checkStmt = conn.prepareStatement(checkQuery);
            checkStmt.setString(1, username);
            ResultSet checkRs = checkStmt.executeQuery();
            
            if (checkRs.next()) {
                System.out.println("\n✗ Username already exists!");
                checkRs.close();
                checkStmt.close();
                conn.close();
                return;
            }
            checkRs.close();
            checkStmt.close();
            
            // Insert new user
            String insertQuery = "INSERT INTO users (username, password, role, full_name) VALUES (?, ?, ?, ?)";
            PreparedStatement insertStmt = conn.prepareStatement(insertQuery);
            insertStmt.setString(1, username);
            insertStmt.setString(2, password); // In production, hash this!
            insertStmt.setString(3, role);
            insertStmt.setString(4, fullName);
            
            int rows = insertStmt.executeUpdate();
            
            if (rows > 0) {
                System.out.println("\n╔═══════════════════════════════════════════════════════════════════════════════╗");
                System.out.println("║                    ✓ USER REGISTERED SUCCESSFULLY                             ║");
                System.out.println("╠═══════════════════════════════════════════════════════════════════════════════╣");
                System.out.println("║ Name    : " + String.format("%-67s", fullName) + "║");
                System.out.println("║ Username: " + String.format("%-67s", username) + "║");
                System.out.println("║ Role    : " + String.format("%-67s", role.toUpperCase()) + "║");
                System.out.println("╚═══════════════════════════════════════════════════════════════════════════════╝");
            } else {
                System.out.println("\n✗ Registration failed!");
            }
            
            insertStmt.close();
            conn.close();
            
        } catch (Exception e) {
            System.out.println("\n✗ Registration Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public static void viewAllUsers(Scanner scanner) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DatabaseUtil.getInstance().getConnection();
            
            String query = "SELECT user_id, username, role, full_name, created_at FROM users ORDER BY role, full_name";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            
            System.out.println("\n╔═══════════════════════════════════════════════════════════════════════════════════════════════╗");
            System.out.println("║                                     ALL USERS                                                 ║");
            System.out.println("╠═══════╦══════════════════╦══════════════════════════╦═══════════════╦═══════════════════════╣");
            System.out.println("║ ID    ║ Username         ║ Full Name                ║ Role          ║ Created               ║");
            System.out.println("╠═══════╬══════════════════╬══════════════════════════╬═══════════════╬═══════════════════════╣");
            
            while (rs.next()) {
                System.out.printf("║ %-5d ║ %-16s ║ %-24s ║ %-13s ║ %-21s ║%n",
                    rs.getInt("user_id"),
                    rs.getString("username"),
                    rs.getString("full_name"),
                    rs.getString("role").toUpperCase(),
                    rs.getString("created_at").substring(0, 19)
                );
            }
            System.out.println("╚═══════╩══════════════════╩══════════════════════════╩═══════════════╩═══════════════════════╝");
            
            rs.close();
            stmt.close();
            conn.close();
            
        } catch (Exception e) {
            System.out.println("\n✗ Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public static void changePassword(Scanner scanner) {
        System.out.println("\n╔═══════════════════════════════════════════════════════════════════════════════╗");
        System.out.println("║                         CHANGE PASSWORD                                       ║");
        System.out.println("╚═══════════════════════════════════════════════════════════════════════════════╝");
        
        System.out.print("\n➤ Current Password: ");
        String currentPassword = scanner.nextLine().trim();
        
        System.out.print("➤ New Password: ");
        String newPassword = scanner.nextLine().trim();
        
        System.out.print("➤ Confirm New Password: ");
        String confirmPassword = scanner.nextLine().trim();
        
        if (!newPassword.equals(confirmPassword)) {
            System.out.println("\n✗ New passwords do not match!");
            return;
        }
        
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DatabaseUtil.getInstance().getConnection();
            
            // Verify current password
            String verifyQuery = "SELECT password FROM users WHERE username = ?";
            PreparedStatement verifyStmt = conn.prepareStatement(verifyQuery);
            verifyStmt.setString(1, loggedInUser);
            ResultSet rs = verifyStmt.executeQuery();
            
            if (rs.next()) {
                String storedPassword = rs.getString("password");
                
                if (!currentPassword.equals(storedPassword)) {
                    System.out.println("\n✗ Current password is incorrect!");
                    rs.close();
                    verifyStmt.close();
                    conn.close();
                    return;
                }
            }
            rs.close();
            verifyStmt.close();
            
            // Update password
            String updateQuery = "UPDATE users SET password = ? WHERE username = ?";
            PreparedStatement updateStmt = conn.prepareStatement(updateQuery);
            updateStmt.setString(1, newPassword);
            updateStmt.setString(2, loggedInUser);
            
            int rows = updateStmt.executeUpdate();
            
            if (rows > 0) {
                System.out.println("\n✓ Password changed successfully!");
            } else {
                System.out.println("\n✗ Failed to change password!");
            }
            
            updateStmt.close();
            conn.close();
            
        } catch (Exception e) {
            System.out.println("\n✗ Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
