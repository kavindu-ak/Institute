/**
 * Student class - Pure OOP implementation
 * Extends Person to inherit common attributes
 * Used by StudentManagementGUI for all student operations
 */
public class Student extends Person {
    private int studentId;
    private String dateOfBirth;
    
    // Default constructor
    public Student() {
        super();
    }
    
    // Constructor without ID (for new students)
    public Student(String name, String address, String gender, int nic, int phoneNumber, String dateOfBirth) {
        super(name, address, gender, nic, phoneNumber);
        this.dateOfBirth = dateOfBirth;
    }
    
    // Constructor with ID (for existing students)
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
    
    // Override abstract method from Person
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
    
    // Override toString for easy display
    @Override
    public String toString() {
        return String.format("Student[ID=%d, Name=%s, NIC=%d, DOB=%s]", 
            studentId, name, nic, dateOfBirth);
    }
    
    // Validation method
    public boolean isValid() {
        return name != null && !name.trim().isEmpty() 
            && address != null && !address.trim().isEmpty()
            && gender != null && (gender.equals("M") || gender.equals("F"))
            && nic > 0
            && phoneNumber > 0
            && dateOfBirth != null && !dateOfBirth.trim().isEmpty();
    }
}