/**
 * Person class - Abstract parent class
 * Contains common attributes and methods for Student and Teacher
 * Pure OOP implementation for GUI system
 */
public abstract class Person {
    protected String name;
    protected String address;
    protected String gender;
    protected int nic;
    protected int phoneNumber;
    
    // Default constructor
    public Person() {
    }
    
    // Parameterized constructor
    public Person(String name, String address, String gender, int nic, int phoneNumber) {
        this.name = name;
        this.address = address;
        this.gender = gender;
        this.nic = nic;
        this.phoneNumber = phoneNumber;
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
    
    // Abstract method - must be implemented by child classes
    public abstract void displayInfo();
    
    // Common validation
    public boolean hasValidBasicInfo() {
        return name != null && !name.trim().isEmpty()
            && address != null && !address.trim().isEmpty()
            && gender != null && !gender.trim().isEmpty()
            && nic > 0
            && phoneNumber > 0;
    }
    
    // toString for debugging/logging
    @Override
    public String toString() {
        return String.format("Person[Name=%s, NIC=%d, Phone=%d, Gender=%s]", 
            name, nic, phoneNumber, gender);
    }
    
    // equals and hashCode based on NIC (unique identifier)
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Person person = (Person) obj;
        return nic == person.nic;
    }
    
    @Override
    public int hashCode() {
        return Integer.hashCode(nic);
    }
}