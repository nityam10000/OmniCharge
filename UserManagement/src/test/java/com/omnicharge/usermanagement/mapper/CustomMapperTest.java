package com.omnicharge.usermanagement.mapper;

import com.omnicharge.usermanagement.dto.UserRequestDTO;
import com.omnicharge.usermanagement.dto.UserResponseDTO;
import com.omnicharge.usermanagement.entity.UserEntity;
import com.omnicharge.usermanagement.enums.Roles;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomMapperTest {

    @Mock
    private PasswordEncoder passwordEncoder;

    private CustomMapper customMapper;

    @BeforeEach
    void setUp() {
        customMapper = new CustomMapper(passwordEncoder);
    }

    @Test
    void toResponseDTO_ShouldMapEntityToDto() {
        UserEntity entity = new UserEntity();
        entity.setUserId(1L);
        entity.setName("Test User");
        entity.setEmail("test@example.com");
        entity.setContactNo("1234567890");
        entity.setRole(Roles.USER);

        UserResponseDTO dto = customMapper.toResponseDTO(entity);

        assertEquals(entity.getUserId(), dto.getUserId());
        assertEquals(entity.getName(), dto.getName());
        assertEquals(entity.getEmail(), dto.getEmail());
        assertEquals(entity.getContactNo(), dto.getContactNo());
        assertEquals(entity.getRole(), dto.getRole());
    }

    @Test
    void toEntity_ShouldMapDtoToEntity() {
        UserRequestDTO dto = new UserRequestDTO();
        dto.setName("Test User");
        dto.setEmail("test@example.com");
        dto.setContactNo("1234567890");
        dto.setPassword("password123");

        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");

        UserEntity entity = customMapper.toEntity(dto);

        assertEquals(dto.getName(), entity.getName());
        assertEquals(dto.getEmail(), entity.getEmail());
        assertEquals(dto.getContactNo(), entity.getContactNo());
        assertEquals("encodedPassword", entity.getPassword());
    }
}
