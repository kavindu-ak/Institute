/**
 * Teacher class - Pure OOP implementation
 * Extends Person to inherit common attributes
 * Used by TeacherManagementGUI for all teacher operations
 */
public class Teacher extends Person {
    private int teacherId;
    private int moduleId;
    private boolean paid;
    
    // Default constructor
    public Teacher() {
        super();
        this.paid = false;
    }
    
    // Constructor without ID (for new teachers)
    public Teacher(String name, String address, String gender, int nic, int phoneNumber, int moduleId) {
        super(name, address, gender, nic, phoneNumber);
        this.moduleId = moduleId;
        this.paid = false;
    }
    
    // Constructor with ID (for existing teachers)
    public Teacher(int teacherId, String name, String address, String gender, int nic, int phoneNumber, int moduleId) {
        super(name, address, gender, nic, phoneNumber);
        this.teacherId = teacherId;
        this.moduleId = moduleId;
        this.paid = false;
    }
    
    // Full constructor with paid status
    public Teacher(int teacherId, String name, String address, String gender, int nic, int phoneNumber, int moduleId, boolean paid) {
        super(name, address, gender, nic, phoneNumber);
        this.teacherId = teacherId;
        this.moduleId = moduleId;
        this.paid = paid;
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
    
    public boolean isPaid() {
        return paid;
    }
    
    public void setPaid(boolean paid) {
        this.paid = paid;
    }
    
    // Override abstract method from Person
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
        System.out.println("║ Paid Status: " + String.format("%-38s", paid ? "Yes" : "No") + "║");
        System.out.println("╚════════════════════════════════════════════════════════╝");
    }
    
    // Override toString for easy display
    @Override
    public String toString() {
        return String.format("Teacher[ID=%d, Name=%s, Module=%d, Paid=%s]", 
            teacherId, name, moduleId, paid ? "Yes" : "No");
    }
    
    // Validation method
    public boolean isValid() {
        return name != null && !name.trim().isEmpty() 
            && address != null && !address.trim().isEmpty()
            && gender != null && (gender.equals("M") || gender.equals("F"))
            && nic > 0
            && phoneNumber > 0
            && moduleId > 0;
    }
}