package models;

public class ShippingAddress {
    private String fullName;
    private String phone;
    private String address;
    private String city;

    // Constructor rỗng cho Firebase
    public ShippingAddress() {
    }

    public ShippingAddress(String fullName, String phone, String address, String city) {
        this.fullName = fullName;
        this.phone = phone;
        this.address = address;
        this.city = city;
    }

    // Getters and setters
    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    @Override
    public String toString() {
        return fullName + "\n" + phone + "\n" + address + ", " + city;
    }
}