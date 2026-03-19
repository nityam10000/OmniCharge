package com.omnicharge.usermanagement.dto;

import com.omnicharge.usermanagement.enums.Roles;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class UserResponseDTO {
    private Long userId;
    private String name;
    private String email;
    private String contactNo;
    private Roles role;

}
