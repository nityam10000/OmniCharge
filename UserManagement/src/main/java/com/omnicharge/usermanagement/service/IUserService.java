package com.omnicharge.usermanagement.service;

import com.omnicharge.usermanagement.dto.RoleUpdateDTO;
import com.omnicharge.usermanagement.dto.UserRequestDTO;
import com.omnicharge.usermanagement.dto.UserResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface IUserService {

    public UserResponseDTO addUser(UserRequestDTO userRequestDTO);
    public List<UserResponseDTO> getAllUsers();
    public Page<UserResponseDTO> getAllUsers(Pageable pageable);
    public UserResponseDTO getUserById(Long id);
    public UserResponseDTO updateUser(UserRequestDTO userRequestDTO);
    public String deleteUser(Long id);
    String updateUserRole(Long userId, RoleUpdateDTO roleUpdateDTO);
    public UserResponseDTO getCurrentUser();
    public UserResponseDTO getUserByEmail(String email);
}