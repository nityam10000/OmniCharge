package com.omnicharge.usermanagement.service.implementations;

import com.omnicharge.usermanagement.dto.RoleUpdateDTO;
import com.omnicharge.usermanagement.dto.UserRequestDTO;
import com.omnicharge.usermanagement.dto.UserResponseDTO;
import com.omnicharge.usermanagement.entity.UserEntity;
import com.omnicharge.usermanagement.enums.Roles;
import com.omnicharge.usermanagement.exception.UserAlreadyExistsException;
import com.omnicharge.usermanagement.exception.UserNotFoundException;
import com.omnicharge.usermanagement.mapper.CustomMapper;
import com.omnicharge.usermanagement.repository.IUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import static org.mockito.Mockito.lenient;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for UserService using Mockito.
 *
 * Key annotations explained:
 *  @ExtendWith(MockitoExtension.class) — tells JUnit 5 to use Mockito
 *  @Mock         — creates a fake (mock) object; no real DB or Spring context needed
 *  @InjectMocks  — creates the real UserService and injects all @Mock objects into it
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    // ── Mocks (fakes) ──────────────────────────────────────────────────────────
    @Mock
    private IUserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private CustomMapper mapper;

    // ── System Under Test ──────────────────────────────────────────────────────
    @InjectMocks
    private UserService userService;

    // ── Shared test data ───────────────────────────────────────────────────────
    private UserEntity userEntity;
    private UserRequestDTO userRequestDTO;
    private UserResponseDTO userResponseDTO;

    /**
     * Runs before EVERY test method.
     * Build reusable test objects here so each test starts with the same data.
     */
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

        userRequestDTO = new UserRequestDTO(
                "Rahul Sharma",
                "rahul@example.com",
                "9876543210",
                "Test@1234"
        );

        userResponseDTO = UserResponseDTO.builder()
                .userId(1L)
                .name("Rahul Sharma")
                .email("rahul@example.com")
                .contactNo("9876543210")
                .role(Roles.USER)
                .build();
    }

    // ══════════════════════════════════════════════════════════════════════════
    // addUser()
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("addUser: should save and return UserResponseDTO when email is new")
    void addUser_ShouldReturnUserResponseDTO_WhenEmailDoesNotExist() {
        // ARRANGE — tell the mocks what to return when called
        when(userRepository.existsByEmail("rahul@example.com")).thenReturn(false);
        when(mapper.toEntity(userRequestDTO)).thenReturn(userEntity);
        when(userRepository.save(userEntity)).thenReturn(userEntity);
        when(mapper.toResponseDTO(userEntity)).thenReturn(userResponseDTO);

        // ACT — call the real method
        UserResponseDTO result = userService.addUser(userRequestDTO);

        // ASSERT — verify the output and that the right methods were called
        assertNotNull(result);
        assertEquals("Rahul Sharma", result.getName());
        assertEquals("rahul@example.com", result.getEmail());

        verify(userRepository).existsByEmail("rahul@example.com"); // was called once
        verify(userRepository).save(userEntity);                    // save was called
    }

    @Test
    @DisplayName("addUser: should throw UserAlreadyExistsException when email already exists")
    void addUser_ShouldThrowException_WhenEmailAlreadyExists() {
        // ARRANGE — simulate a duplicate email scenario
        when(userRepository.existsByEmail("rahul@example.com")).thenReturn(true);

        // ACT & ASSERT — expect the exception to be thrown
        assertThrows(UserAlreadyExistsException.class,
                () -> userService.addUser(userRequestDTO));

        // Make sure save() was never called — we don't want to save duplicates
        verify(userRepository, never()).save(any());
    }

    // ══════════════════════════════════════════════════════════════════════════
    // getAllUsers()
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("getAllUsers: should return a list of all users")
    void getAllUsers_ShouldReturnListOfUsers() {
        // ARRANGE
        UserEntity user2 = UserEntity.builder()
                .userId(2L).name("Priya Patel").email("priya@example.com")
                .contactNo("9123456780").password("encoded").role(Roles.USER).build();

        UserResponseDTO responseDTO2 = UserResponseDTO.builder()
                .userId(2L).name("Priya Patel").email("priya@example.com")
                .contactNo("9123456780").role(Roles.USER).build();

        when(userRepository.findAll()).thenReturn(List.of(userEntity, user2));
        when(mapper.toResponseDTO(userEntity)).thenReturn(userResponseDTO);
        when(mapper.toResponseDTO(user2)).thenReturn(responseDTO2);

        // ACT
        List<UserResponseDTO> result = userService.getAllUsers();

        // ASSERT
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Rahul Sharma", result.get(0).getName());
        assertEquals("Priya Patel", result.get(1).getName());
    }

    @Test
    @DisplayName("getAllUsers: should return empty list when no users exist")
    void getAllUsers_ShouldReturnEmptyList_WhenNoUsers() {
        when(userRepository.findAll()).thenReturn(List.of());

        List<UserResponseDTO> result = userService.getAllUsers();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ══════════════════════════════════════════════════════════════════════════
    // getUserById()
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("getUserById: should return user when accessed by ADMIN")
    void getUserById_ShouldReturnUser_WhenCallerIsAdmin() {
        // ARRANGE — mock SecurityContext to simulate an ADMIN caller
        mockSecurityContext("admin@example.com", "ROLE_ADMIN");

        when(userRepository.findById(1L)).thenReturn(Optional.of(userEntity));
        when(mapper.toResponseDTO(userEntity)).thenReturn(userResponseDTO);

        // ACT
        UserResponseDTO result = userService.getUserById(1L);

        // ASSERT
        assertNotNull(result);
        assertEquals(1L, result.getUserId());
        verify(userRepository).findById(1L);
    }

    @Test
    @DisplayName("getUserById: should throw UserNotFoundException when user does not exist")
    void getUserById_ShouldThrowException_WhenUserNotFound() {
        // ARRANGE — admin so access check passes, but DB returns empty
        mockSecurityContext("admin@example.com", "ROLE_ADMIN");
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        // ACT & ASSERT
        assertThrows(UserNotFoundException.class,
                () -> userService.getUserById(99L));
    }

    // ══════════════════════════════════════════════════════════════════════════
    // deleteUser()
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("deleteUser: should delete user and return success message")
    void deleteUser_ShouldReturnSuccessMessage_WhenUserExists() {
        // ARRANGE
        when(userRepository.findById(1L)).thenReturn(Optional.of(userEntity));
        doNothing().when(userRepository).delete(userEntity); // void method stub

        // ACT
        String result = userService.deleteUser(1L);

        // ASSERT
        assertEquals("User with Id: 1 has been deleted!", result);
        verify(userRepository).delete(userEntity); // delete was actually called
    }

    @Test
    @DisplayName("deleteUser: should throw UserNotFoundException when user does not exist")
    void deleteUser_ShouldThrowException_WhenUserNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class,
                () -> userService.deleteUser(99L));

        verify(userRepository, never()).delete(any()); // delete must NOT be called
    }

    // ══════════════════════════════════════════════════════════════════════════
    // updateUserRole()
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("updateUserRole: should update role and return promotion message")
    void updateUserRole_ShouldPromoteUser_WhenUserExists() {
        // ARRANGE
        RoleUpdateDTO roleUpdateDTO = new RoleUpdateDTO();
        roleUpdateDTO.setRole(Roles.ADMIN);

        when(userRepository.findById(1L)).thenReturn(Optional.of(userEntity));

        // ACT
        String result = userService.updateUserRole(1L, roleUpdateDTO);

        // ASSERT
        assertEquals(Roles.ADMIN, userEntity.getRole()); // role was changed on the entity
        assertEquals("Rahul Sharma promoted to ADMIN!!", result);
    }

    @Test
    @DisplayName("updateUserRole: should throw UserNotFoundException when user not found")
    void updateUserRole_ShouldThrowException_WhenUserNotFound() {
        RoleUpdateDTO roleUpdateDTO = new RoleUpdateDTO();
        roleUpdateDTO.setRole(Roles.ADMIN);

        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class,
                () -> userService.updateUserRole(99L, roleUpdateDTO));
    }

    // ══════════════════════════════════════════════════════════════════════════
    // getCurrentUser()
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("getCurrentUser: should return current authenticated user")
    void getCurrentUser_ShouldReturnCurrentUser_WhenAuthenticated() {
        // ARRANGE — mock Security context with the user's own email
        mockSecurityContext("rahul@example.com", "ROLE_USER");

        when(userRepository.findByEmail("rahul@example.com"))
                .thenReturn(Optional.of(userEntity));
        when(mapper.toResponseDTO(userEntity)).thenReturn(userResponseDTO);

        // ACT
        UserResponseDTO result = userService.getCurrentUser();

        // ASSERT
        assertNotNull(result);
        assertEquals("rahul@example.com", result.getEmail());
    }

    @Test
    @DisplayName("getCurrentUser: should throw UserNotFoundException when email not in DB")
    void getCurrentUser_ShouldThrowException_WhenUserNotFound() {
        mockSecurityContext("ghost@example.com", "ROLE_USER");
        when(userRepository.findByEmail("ghost@example.com")).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class,
                () -> userService.getCurrentUser());
    }

    // ══════════════════════════════════════════════════════════════════════════
    // getUserByEmail()
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("getUserByEmail: should return user when accessed by ADMIN")
    void getUserByEmail_ShouldReturnUser_WhenCallerIsAdmin() {
        mockSecurityContext("admin@example.com", "ROLE_ADMIN");

        when(userRepository.findByEmail("rahul@example.com"))
                .thenReturn(Optional.of(userEntity));
        when(mapper.toResponseDTO(userEntity)).thenReturn(userResponseDTO);

        UserResponseDTO result = userService.getUserByEmail("rahul@example.com");

        assertNotNull(result);
        assertEquals("rahul@example.com", result.getEmail());
    }

    @Test
    @DisplayName("getUserByEmail: should throw UserNotFoundException when email not found")
    void getUserByEmail_ShouldThrowException_WhenEmailNotFound() {
        mockSecurityContext("admin@example.com", "ROLE_ADMIN");
        when(userRepository.findByEmail("notfound@example.com")).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class,
                () -> userService.getUserByEmail("notfound@example.com"));
    }

    // ══════════════════════════════════════════════════════════════════════════
    // Helper — mock Spring Security context
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Sets up a fake Spring Security context.
     * This is required because UserService reads the authenticated user from
     * SecurityContextHolder in validateAccess() and getCurrentUser().
     *
     * @param email  the username returned by authentication.getName()
     * @param role   e.g. "ROLE_ADMIN" or "ROLE_USER"
     */
    private void mockSecurityContext(String email, String role) {
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);

        lenient().when(authentication.getName()).thenReturn(email);

        Collection<GrantedAuthority> authorities =
                List.of(new SimpleGrantedAuthority(role));
        lenient().doReturn(authorities).when(authentication).getAuthorities();

        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }
}

