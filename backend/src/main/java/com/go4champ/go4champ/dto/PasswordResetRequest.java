// Speichern unter: src/main/java/com/go4champ/go4champ/dto/PasswordResetRequest.java

package com.go4champ.go4champ.dto;

public class PasswordResetRequest {
    private String email;

    public PasswordResetRequest() {
    }

    public PasswordResetRequest(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}