package com.omnicharge.usermanagement.service;

import com.omnicharge.usermanagement.dto.UserRequestDTO;
import com.omnicharge.usermanagement.dto.UserResponseDTO;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface IUserService {

    public UserResponseDTO addUser(UserRequestDTO userRequestDTO);
    public List<UserResponseDTO> getAllUsers();
    public UserResponseDTO getUserById(Long id);
    public UserResponseDTO updateUser(Long id, UserRequestDTO userRequestDTO);
    public String deleteUser(Long id);
}
