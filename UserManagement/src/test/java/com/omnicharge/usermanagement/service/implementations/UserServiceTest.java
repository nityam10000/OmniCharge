package com.omnicharge.usermanagement.service.implementations;

import com.omnicharge.usermanagement.dto.NotificationEvent;
import com.omnicharge.usermanagement.dto.RoleUpdateDTO;
import com.omnicharge.usermanagement.dto.UserRequestDTO;
import com.omnicharge.usermanagement.dto.UserResponseDTO;
import com.omnicharge.usermanagement.entity.UserEntity;
import com.omnicharge.usermanagement.enums.Roles;
import com.omnicharge.usermanagement.exception.UserAlreadyExistsException;
import com.omnicharge.usermanagement.exception.UserNotFoundException;
import com.omnicharge.usermanagement.mapper.CustomMapper;
import com.omnicharge.usermanagement.repository.IUserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private IUserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private CustomMapper mapper;
    @Mock private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private UserService userService;

    private UserEntity userEntity;
    private UserRequestDTO userRequestDTO;
    private UserResponseDTO userResponseDTO;

    @BeforeEach
    void setUp() {
        userEntity = UserEntity.builder()
                .userId(1L)
                .name("Rahul Sharma")
                .email("rahul@example.com")
                .contactNo("9876543210")
                .password("encoded_password")
                .role(Roles.USER)
                .build();

        userRequestDTO = UserRequestDTO.builder()
                .name("Rahul Sharma")
                .email("rahul@example.com")
                .contactNo("9876543210")
                .password("Test@1234")
                .build();

        userResponseDTO = UserResponseDTO.builder()
                .userId(1L)
                .name("Rahul Sharma")
                .email("rahul@example.com")
                .contactNo("9876543210")
                .role(Roles.USER)
                .build();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("addUser: should save user and publish welcome notification")
    void addUser_ShouldReturnUserResponseDTO_WhenEmailDoesNotExist() {
        when(userRepository.existsByEmail("rahul@example.com")).thenReturn(false);
        when(mapper.toEntity(userRequestDTO)).thenReturn(userEntity);
        when(userRepository.save(userEntity)).thenReturn(userEntity);
        when(mapper.toResponseDTO(userEntity)).thenReturn(userResponseDTO);

        UserResponseDTO result = userService.addUser(userRequestDTO);

        assertNotNull(result);
        assertEquals("rahul@example.com", result.getEmail());
        verify(userRepository).save(userEntity);

        ArgumentCaptor<NotificationEvent> captor = ArgumentCaptor.forClass(NotificationEvent.class);
        verify(rabbitTemplate).convertAndSend(eq("notification_exchange"), eq("notification_routing"), captor.capture());
        assertEquals("WELCOME", captor.getValue().getType());
    }

    @Test
    @DisplayName("addUser: should ignore notification failure and still return user")
    void addUser_ShouldIgnoreNotificationFailure() {
        when(userRepository.existsByEmail("rahul@example.com")).thenReturn(false);
        when(mapper.toEntity(userRequestDTO)).thenReturn(userEntity);
        when(userRepository.save(userEntity)).thenReturn(userEntity);
        when(mapper.toResponseDTO(userEntity)).thenReturn(userResponseDTO);
        doThrow(new RuntimeException("amqp down"))
                .when(rabbitTemplate).convertAndSend(any(String.class), any(String.class), any(NotificationEvent.class));

        UserResponseDTO result = userService.addUser(userRequestDTO);

        assertNotNull(result);
        verify(userRepository).save(userEntity);
    }

    @Test
    @DisplayName("addUser: should throw UserAlreadyExistsException when email already exists")
    void addUser_ShouldThrowException_WhenEmailAlreadyExists() {
        when(userRepository.existsByEmail("rahul@example.com")).thenReturn(true);

        assertThrows(UserAlreadyExistsException.class, () -> userService.addUser(userRequestDTO));
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("getAllUsers: should return all mapped users")
    void getAllUsers_ShouldReturnListOfUsers() {
        UserEntity user2 = UserEntity.builder()
                .userId(2L)
                .name("Priya Patel")
                .email("priya@example.com")
                .contactNo("9123456780")
                .password("encoded")
                .role(Roles.USER)
                .build();

        UserResponseDTO responseDTO2 = UserResponseDTO.builder()
                .userId(2L)
                .name("Priya Patel")
                .email("priya@example.com")
                .contactNo("9123456780")
                .role(Roles.USER)
                .build();

        when(userRepository.findAll()).thenReturn(List.of(userEntity, user2));
        when(mapper.toResponseDTO(userEntity)).thenReturn(userResponseDTO);
        when(mapper.toResponseDTO(user2)).thenReturn(responseDTO2);

        List<UserResponseDTO> result = userService.getAllUsers();

        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("getUserById: should return user when caller is admin")
    void getUserById_ShouldReturnUser_WhenCallerIsAdmin() {
        mockSecurityContext("admin@example.com", "ROLE_ADMIN");
        when(userRepository.findById(1L)).thenReturn(Optional.of(userEntity));
        when(mapper.toResponseDTO(userEntity)).thenReturn(userResponseDTO);

        UserResponseDTO result = userService.getUserById(1L);

        assertEquals(1L, result.getUserId());
    }

    @Test
    @DisplayName("getUserById: should deny access to another normal user")
    void getUserById_ShouldThrow_WhenCallerAccessesAnotherUser() {
        UserEntity loggedInUser = UserEntity.builder()
                .userId(2L)
                .email("rahul@example.com")
                .role(Roles.USER)
                .build();
        mockSecurityContext("rahul@example.com", "ROLE_USER");
        when(userRepository.findByEmail("rahul@example.com")).thenReturn(Optional.of(loggedInUser));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> userService.getUserById(1L));

        assertEquals("Access Denied: you can only access your own data", ex.getMessage());
    }

    @Test
    @DisplayName("updateUser: should update current user")
    void updateUser_ShouldUpdateCurrentUser() {
        mockSecurityContext("rahul@example.com", "ROLE_USER");
        when(userRepository.findByEmail("rahul@example.com")).thenReturn(Optional.of(userEntity));
        when(mapper.toResponseDTO(userEntity)).thenReturn(userResponseDTO);
        when(userRepository.findById(1L)).thenReturn(Optional.of(userEntity));
        when(passwordEncoder.encode("Test@1234")).thenReturn("encoded_new");
        when(userRepository.save(userEntity)).thenReturn(userEntity);

        UserResponseDTO result = userService.updateUser(userRequestDTO);

        assertNotNull(result);
        assertEquals("encoded_new", userEntity.getPassword());
        assertEquals("9876543210", userEntity.getContactNo());
    }

    @Test
    @DisplayName("updateUser: should throw when current user is missing")
    void updateUser_ShouldThrow_WhenCurrentUserMissing() {
        mockSecurityContext("ghost@example.com", "ROLE_USER");
        when(userRepository.findByEmail("ghost@example.com")).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.updateUser(userRequestDTO));
    }

    @Test
    @DisplayName("deleteUser: should delete user and return success message")
    void deleteUser_ShouldReturnSuccessMessage_WhenUserExists() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(userEntity));

        String result = userService.deleteUser(1L);

        assertEquals("User with Id: 1 has been deleted!", result);
        verify(userRepository).delete(userEntity);
    }

    @Test
    @DisplayName("deleteUser: should throw UserNotFoundException when user does not exist")
    void deleteUser_ShouldThrowException_WhenUserNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.deleteUser(99L));
        verify(userRepository, never()).delete(any());
    }

    @Test
    @DisplayName("updateUserRole: should update role and return message")
    void updateUserRole_ShouldPromoteUser_WhenUserExists() {
        RoleUpdateDTO roleUpdateDTO = new RoleUpdateDTO();
        roleUpdateDTO.setRole(Roles.ADMIN);
        when(userRepository.findById(1L)).thenReturn(Optional.of(userEntity));

        String result = userService.updateUserRole(1L, roleUpdateDTO);

        assertEquals(Roles.ADMIN, userEntity.getRole());
        assertEquals("Rahul Sharma promoted to ADMIN!!", result);
        verify(userRepository).save(userEntity);
    }

    @Test
    @DisplayName("getCurrentUser: should return current authenticated user")
    void getCurrentUser_ShouldReturnCurrentUser_WhenAuthenticated() {
        mockSecurityContext("rahul@example.com", "ROLE_USER");
        when(userRepository.findByEmail("rahul@example.com")).thenReturn(Optional.of(userEntity));
        when(mapper.toResponseDTO(userEntity)).thenReturn(userResponseDTO);

        UserResponseDTO result = userService.getCurrentUser();

        assertEquals("rahul@example.com", result.getEmail());
    }

    @Test
    @DisplayName("getCurrentUser: should throw when email is not found")
    void getCurrentUser_ShouldThrowException_WhenUserNotFound() {
        mockSecurityContext("ghost@example.com", "ROLE_USER");
        when(userRepository.findByEmail("ghost@example.com")).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.getCurrentUser());
    }

    @Test
    @DisplayName("getUserByEmail: should return same user for self lookup")
    void getUserByEmail_ShouldReturnUser_WhenSelfLookup() {
        mockSecurityContext("rahul@example.com", "ROLE_USER");
        when(userRepository.findByEmail("rahul@example.com")).thenReturn(Optional.of(userEntity));
        when(mapper.toResponseDTO(userEntity)).thenReturn(userResponseDTO);

        UserResponseDTO result = userService.getUserByEmail("rahul@example.com");

        assertEquals("rahul@example.com", result.getEmail());
    }

    @Test
    @DisplayName("getUserByEmail: should deny access to another user")
    void getUserByEmail_ShouldThrow_WhenLookingUpAnotherUser() {
        UserEntity otherUser = UserEntity.builder()
                .userId(1L)
                .email("other@example.com")
                .role(Roles.USER)
                .build();
        UserEntity loggedInUser = UserEntity.builder()
                .userId(2L)
                .email("rahul@example.com")
                .role(Roles.USER)
                .build();
        mockSecurityContext("rahul@example.com", "ROLE_USER");
        when(userRepository.findByEmail("other@example.com")).thenReturn(Optional.of(otherUser));
        when(userRepository.findByEmail("rahul@example.com")).thenReturn(Optional.of(loggedInUser));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> userService.getUserByEmail("other@example.com"));

        assertEquals("Access Denied: you can only access your own data", ex.getMessage());
    }

    @Test
    @DisplayName("getUserByEmail: should throw UserNotFoundException when email not found")
    void getUserByEmail_ShouldThrowException_WhenEmailNotFound() {
        mockSecurityContext("admin@example.com", "ROLE_ADMIN");
        when(userRepository.findByEmail("notfound@example.com")).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class,
                () -> userService.getUserByEmail("notfound@example.com"));
    }

    private void mockSecurityContext(String email, String role) {
        Collection<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(role));
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(email, null, authorities);
        SecurityContextHolder.setContext(new SecurityContextImpl(authentication));
    }
}
