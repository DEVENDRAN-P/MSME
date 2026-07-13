package com.idbi.msme.security;

public class FirebaseUserPrincipal {
    private final String uid;
    private final String email;
    private final String role;

    public FirebaseUserPrincipal(String uid, String email, String role) {
        this.uid = uid;
        this.email = email;
        this.role = role;
    }

    public String getUid() { return uid; }
    public String getEmail() { return email; }
    public String getRole() { return role; }
}
