package com.omnicharge.usermanagement.mapper;

import com.omnicharge.usermanagement.dto.UserRequestDTO;
import com.omnicharge.usermanagement.dto.UserResponseDTO;
import com.omnicharge.usermanagement.entity.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class CustomMapper {

    private final PasswordEncoder passwordEncoder;

	public UserResponseDTO toResponseDTO(UserEntity userEntity) {
        UserResponseDTO dto = new UserResponseDTO();
        dto.setName(userEntity.getName());
        dto.setEmail(userEntity.getEmail());
        dto.setContactNo(userEntity.getContactNo());

        return dto;
    }

    public UserEntity toEntity(UserRequestDTO dto) {
        UserEntity entity = new UserEntity();
        entity.setName(dto.getName());
        entity.setEmail(dto.getEmail());
        entity.setContactNo(dto.getContactNo());
        entity.setPassword(passwordEncoder.encode(dto.getPassword()));

        return entity;
    }
}
