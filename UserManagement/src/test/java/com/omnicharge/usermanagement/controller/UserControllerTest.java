package com.omnicharge.usermanagement.controller;

import com.omnicharge.usermanagement.dto.RoleUpdateDTO;
import com.omnicharge.usermanagement.dto.UserRequestDTO;
import com.omnicharge.usermanagement.dto.UserResponseDTO;
import com.omnicharge.usermanagement.enums.Roles;
import com.omnicharge.usermanagement.service.IUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private IUserService userService;

    private UserController controller;
    private UserRequestDTO requestDTO;
    private UserResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        controller = new UserController(userService);
        requestDTO = UserRequestDTO.builder()
                .name("Rahul Sharma")
                .email("rahul@example.com")
                .contactNo("9876543210")
                .password("Test@1234")
                .build();
        responseDTO = UserResponseDTO.builder()
                .userId(1L)
                .name("Rahul Sharma")
                .email("rahul@example.com")
                .contactNo("9876543210")
                .role(Roles.USER)
                .build();
    }

    @Test
    void addUser_ShouldReturnOkWithBody() {
        when(userService.addUser(requestDTO)).thenReturn(responseDTO);

        ResponseEntity<UserResponseDTO> response = controller.addUser(requestDTO);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(responseDTO, response.getBody());
    }

    @Test
    void getAllUsers_ShouldReturnOkWithList() {
        List<UserResponseDTO> users = List.of(responseDTO);
        when(userService.getAllUsers()).thenReturn(users);

        ResponseEntity<List<UserResponseDTO>> response = controller.getAllUsers();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(users, response.getBody());
    }

    @Test
    void updateUserProfile_ShouldReturnUpdatedUser() {
        when(userService.updateUser(requestDTO)).thenReturn(responseDTO);

        ResponseEntity<UserResponseDTO> response = controller.updateUserProfile(requestDTO);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(responseDTO, response.getBody());
    }

    @Test
    void getUserById_ShouldDelegateToService() {
        when(userService.getUserById(1L)).thenReturn(responseDTO);

        ResponseEntity<UserResponseDTO> response = controller.getUserById(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(responseDTO, response.getBody());
    }

    @Test
    void getUserProfile_ShouldReturnCurrentUser() {
        when(userService.getCurrentUser()).thenReturn(responseDTO);

        ResponseEntity<UserResponseDTO> response = controller.getUserProfile();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(responseDTO, response.getBody());
    }

    @Test
    void deleteUser_ShouldReturnServiceMessage() {
        when(userService.deleteUser(1L)).thenReturn("deleted");

        ResponseEntity<String> response = controller.deleteUser(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("deleted", response.getBody());
    }

    @Test
    void getUserByEmail_ShouldReturnUser() {
        when(userService.getUserByEmail("rahul@example.com")).thenReturn(responseDTO);

        ResponseEntity<UserResponseDTO> response = controller.getUserByEmail("rahul@example.com");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(responseDTO, response.getBody());
    }

    @Test
    void updateUserRole_ShouldReturnStatusMessage() {
        RoleUpdateDTO roleUpdateDTO = new RoleUpdateDTO();
        roleUpdateDTO.setRole(Roles.ADMIN);
        when(userService.updateUserRole(1L, roleUpdateDTO)).thenReturn("promoted");

        ResponseEntity<String> response = controller.updateUserRole(1L, roleUpdateDTO);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("promoted", response.getBody());
        verify(userService).updateUserRole(1L, roleUpdateDTO);
    }
}
