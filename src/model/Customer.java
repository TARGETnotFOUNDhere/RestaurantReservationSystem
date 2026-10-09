package model;

public class Customer extends BaseEntity {
    private String name;
    private String phone;

    public Customer(int id, String name, String phone) {
        super(id);
        this.name = validateName(name);
        this.phone = validatePhone(phone);
    }

    /** Returns the cleaned-up name or throws IllegalArgumentException if it is invalid. */
    public static String validateName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Customer name cannot be empty.");
        }
        String cleaned = name.trim().replaceAll("\\s+", " ");
        if (!cleaned.matches("\\p{L}[\\p{L} .'-]{1,49}")) {
            throw new IllegalArgumentException(
                    "Customer name must be 2-50 characters and contain only letters, spaces, dots, hyphens or apostrophes.");
        }
        return cleaned;
    }

    /** Returns the cleaned-up phone number or throws IllegalArgumentException if it is invalid. */
    public static String validatePhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            throw new IllegalArgumentException("Phone number cannot be empty.");
        }
        String cleaned = phone.trim();
        if (!cleaned.matches("\\d{10}")) {
            throw new IllegalArgumentException("Phone number must contain exactly 10 digits.");
        }
        return cleaned;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = validateName(name);
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = validatePhone(phone);
    }

    @Override
    public String getSummary() {
        return getId() + " - " + name;
    }
}
