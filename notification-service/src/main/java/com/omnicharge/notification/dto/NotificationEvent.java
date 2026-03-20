package com.omnicharge.notification.dto;

public class NotificationEvent {

    private String message;
    private String email;
    private String phoneNumber; // ✅ fixed (important)
    private String type;

    // Constructors
    public NotificationEvent() {}

    public NotificationEvent(String message, String email, String phoneNumber, String type) {
        this.message = message;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.type = type;
    }

    // Getters
    public String getMessage() {
        return message;
    }

    public String getEmail() {
        return email;
    }

    public String getPhoneNumber() {   // ✅ fixed
        return phoneNumber;
    }

    public String getType() {
        return type;
    }

    // Setters
    public void setMessage(String message) {
        this.message = message;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPhoneNumber(String phoneNumber) {  // ✅ fixed
        this.phoneNumber = phoneNumber;
    }

    public void setType(String type) {
        this.type = type;
    }
}