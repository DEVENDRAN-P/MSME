package com.idbi.msme.dto;

public class UserProfileResponse {

    private String id;
    private String email;
    private String fullName;
    private String role;
    private String phone;
    private String status;

    public UserProfileResponse() {}

    public UserProfileResponse(String id, String email, String fullName, String role, String phone, String status) {
        this.id = id;
        this.email = email;
        this.fullName = fullName;
        this.role = role;
        this.phone = phone;
        this.status = status;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
