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

    public String getMessage() {
        return message;
    }

    public String getEmail() {
        return email;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}