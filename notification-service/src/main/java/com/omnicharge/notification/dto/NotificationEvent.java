//package com.omnicharge.notification.dto;
//
//import lombok.AllArgsConstructor;
//import lombok.Data;
//import lombok.NoArgsConstructor;
//
//@Data
//@AllArgsConstructor
//@NoArgsConstructor
//public class NotificationEvent {
//
//    private String message;
//    private String email;
//}
package com.omnicharge.notification.dto;

public class NotificationEvent {

    private String message;
    private String email;
    private String phone;
    private String type; // SUCCESS / FAILED

    // Default Constructor
    public NotificationEvent() {
    }

    // Parameterized Constructor
    public NotificationEvent(String message, String email, String phone, String type) {
        this.message = message;
        this.email = email;
        this.phone = phone;
        this.type = type;
    }

    // Getter & Setter for message
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    // Getter & Setter for email
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    // Getter & Setter for phone
    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    // Getter & Setter for type
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}