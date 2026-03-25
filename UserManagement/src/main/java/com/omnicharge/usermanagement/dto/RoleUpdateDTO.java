package com.omnicharge.usermanagement.dto;

import com.omnicharge.usermanagement.enums.Roles;
import lombok.Data;

@Data
public class RoleUpdateDTO {
    private Roles role;
}