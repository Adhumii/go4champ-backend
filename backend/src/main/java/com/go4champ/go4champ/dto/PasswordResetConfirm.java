
package com.go4champ.go4champ.dto;

public class PasswordResetConfirm {
    private String token;
    private String newPassword;

    public PasswordResetConfirm() {
    }

    public PasswordResetConfirm(String token, String newPassword) {
        this.token = token;
        this.newPassword = newPassword;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}