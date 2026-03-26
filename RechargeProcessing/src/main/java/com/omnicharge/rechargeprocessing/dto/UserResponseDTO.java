package com.omnicharge.rechargeprocessing.dto;


import java.io.Serializable;

import com.omnicharge.usermanagement.enums.Roles;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class UserResponseDTO implements Serializable{
	private final long serialIzableID = 1L;
    private Long userId;
    private String name;
    private String email;
    private String contactNo;
    private Roles role;
}

