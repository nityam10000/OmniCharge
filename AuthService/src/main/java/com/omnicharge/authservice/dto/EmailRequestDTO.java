package com.omnicharge.authservice.dto;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class EmailRequestDTO {

    @NotBlank
    @Email
    private String email;
}