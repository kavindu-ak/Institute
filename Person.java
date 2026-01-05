import java.util.Scanner;

public abstract class Person {
    protected String name;
    protected String address;
    protected String gender;
    protected int nic;
    protected int phoneNumber;
    
    // Constructor
    public Person(String name, String address, String gender, int nic, int phoneNumber) {
        this.name = name;
        this.address = address;
        this.gender = gender;
        this.nic = nic;
        this.phoneNumber = phoneNumber;
    }
    
    // Default constructor
    public Person() {
    }
    
    // Getters
    public String getName() {
        return name;
    }
    
    public String getAddress() {
        return address;
    }
    
    public String getGender() {
        return gender;
    }
    
    public int getNic() {
        return nic;
    }
    
    public int getPhoneNumber() {
        return phoneNumber;
    }
    
    // Setters
    public void setName(String name) {
        this.name = name;
    }
    
    public void setAddress(String address) {
        this.address = address;
    }
    
    public void setGender(String gender) {
        this.gender = gender;
    }
    
    public void setNic(int nic) {
        this.nic = nic;
    }
    
    public void setPhoneNumber(int phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
    
    // Common input methods
    protected static String promptInputS(Scanner scanner, String message) {
        System.out.print(message);
        return scanner.nextLine().trim();
    }
    
    protected static int promptInputI(Scanner scanner, String message) {
        System.out.print(message);
        return scanner.nextInt();
    }
    
    // Abstract method to be implemented by child classes
    public abstract void displayInfo();
    
    // Common method to read basic person info
    protected void readPersonInfo(Scanner scanner) {
        this.name = promptInputS(scanner, "Enter name: ");
        this.address = promptInputS(scanner, "Enter address: ");
        this.gender = promptInputS(scanner, "Enter Gender (M/F): ");
        this.nic = promptInputI(scanner, "Enter NIC number: ");
        scanner.nextLine(); // Clear buffer
        this.phoneNumber = promptInputI(scanner, "Enter phone number: ");
        scanner.nextLine(); // Clear buffer
    }
    
    @Override
    public String toString() {
        return "Name: " + name + ", Address: " + address + ", Gender: " + gender + 
               ", NIC: " + nic + ", Phone: " + phoneNumber;
    }
}
