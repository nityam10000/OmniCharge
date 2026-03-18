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

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class NotificationEvent {

    private String message;
    private String email;
    private String phone;
    private String type; // SUCCESS / FAILED

}