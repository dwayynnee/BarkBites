package models;

import java.time.Instant;

/**
 * Represents a user in the BarkBites system (Student or Staff)
 * Maps to the 'users' Firestore collection
 */
public class User {
    private String student_id;      // Primary identifier (e.g., "S12345")
    private String name;            // Full name
    private String email;           // Email address (optional)
    private String role;            // "student" or "staff"
    private Instant created_at;     // Account creation timestamp
    private Instant last_login;     // Last login timestamp

    // Constructor
    public User() {
    }

    public User(String student_id, String name, String email, String role) {
        this.student_id = student_id;
        this.name = name;
        this.email = email;
        this.role = role;
        this.created_at = Instant.now();
        this.last_login = Instant.now();
    }

    // Getters and Setters
    public String getStudent_id() {
        return student_id;
    }

    public void setStudent_id(String student_id) {
        this.student_id = student_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Instant getCreated_at() {
        return created_at;
    }

    public void setCreated_at(Instant created_at) {
        this.created_at = created_at;
    }

    public Instant getLast_login() {
        return last_login;
    }

    public void setLast_login(Instant last_login) {
        this.last_login = last_login;
    }

    @Override
    public String toString() {
        return "User{" +
                "student_id='" + student_id + '\'' +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", role='" + role + '\'' +
                ", created_at=" + created_at +
                ", last_login=" + last_login +
                '}';
    }
}
