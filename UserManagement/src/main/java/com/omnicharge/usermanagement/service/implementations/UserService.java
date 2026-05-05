package com.omnicharge.usermanagement.service.implementations;

import com.omnicharge.usermanagement.configuration.RabbitMQConfig;
import com.omnicharge.usermanagement.dto.NotificationEvent;
import com.omnicharge.usermanagement.dto.RoleUpdateDTO;
import com.omnicharge.usermanagement.dto.UserRequestDTO;
import com.omnicharge.usermanagement.dto.UserResponseDTO;
import com.omnicharge.usermanagement.entity.UserEntity;
import com.omnicharge.usermanagement.exception.AccessDeniedException;
import com.omnicharge.usermanagement.exception.UserAlreadyExistsException;
import com.omnicharge.usermanagement.exception.UserNotFoundException;
import com.omnicharge.usermanagement.mapper.CustomMapper;
import com.omnicharge.usermanagement.repository.IUserRepository;
import com.omnicharge.usermanagement.service.IUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService implements IUserService {

    private final IUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CustomMapper mapper;
    private final RabbitTemplate rabbitTemplate;


    //  addUser — sends welcome email after registration


    @Override
    @CachePut(value = "user", key = "#userRequestDTO.email")
    @CacheEvict(value = "userList", allEntries = true)
    public UserResponseDTO addUser(UserRequestDTO userRequestDTO) {
        log.info("Attempting to add new user with email: {}", userRequestDTO.getEmail());

        if (userRepository.existsByEmail(userRequestDTO.getEmail())) {
            log.warn("User with email {} already exists", userRequestDTO.getEmail());
            throw new UserAlreadyExistsException("User already exists!");
        }

        UserEntity userEntity = mapper.toEntity(userRequestDTO);
        UserResponseDTO savedUser = mapper.toResponseDTO(userRepository.save(userEntity));

        log.info("User with email {} has been created successfully", userRequestDTO.getEmail());

        sendWelcomeNotification(savedUser);

        return savedUser;
    }


    //  getAllUsers


    @Override
    @Cacheable(value = "userList")
    public List<UserResponseDTO> getAllUsers() {
        log.info("Fetching all users from database");
        List<UserEntity> list = userRepository.findAll();
        List<UserResponseDTO> users = list.stream().map(mapper::toResponseDTO).toList();
        log.info("Successfully retrieved {} users", users.size());
        return users;
    }

    @Override
    public Page<UserResponseDTO> getAllUsers(Pageable pageable) {
        log.info("Fetching users with pagination: page={}, size={}", pageable.getPageNumber(), pageable.getPageSize());
        Page<UserEntity> userPage = userRepository.findAll(pageable);
        Page<UserResponseDTO> responseDTO = userPage.map(mapper::toResponseDTO);
        log.info("Successfully retrieved {} users", responseDTO.getNumberOfElements());
        return responseDTO;
    }


    //  getUserById


    @Override
    @Cacheable(value = "user", key = "#id")
    public UserResponseDTO getUserById(Long id) {
        log.info("Fetching user with ID: {}", id);
        validateAccess(id);
        UserEntity userEntity = userRepository.findById(id).orElseThrow(() -> {
            log.error("User not found with id: {}", id);
            return new UserNotFoundException("User with " + id + " not found!");
        });
        log.info("User retrieved successfully with ID: {}", id);
        return mapper.toResponseDTO(userEntity);
    }


    //  updateUser


    @Override
    @Caching(
            put   = { @CachePut(value = "user", key = "#userRequestDTO.email") },
            evict = { @CacheEvict(value = "userList", allEntries = true) }
    )
    public UserResponseDTO updateUser(UserRequestDTO userRequestDTO) {
        Long id = getCurrentUser().getUserId();
        log.info("Updating user with ID: {} - Email: {}", id, userRequestDTO.getEmail());
        validateAccess(id);
        UserEntity userEntity = userRepository.findById(id).orElseThrow(() -> {
            log.error("User not found with id: {} during update", id);
            return new UserNotFoundException("User Not Exists");
        });
        userEntity.setName(userRequestDTO.getName());
        userEntity.setEmail(userRequestDTO.getEmail());
        userEntity.setPassword(passwordEncoder.encode(userRequestDTO.getPassword()));
        userEntity.setContactNo(userRequestDTO.getContactNo());

        UserResponseDTO updated = mapper.toResponseDTO(userRepository.save(userEntity));
        log.info("User with ID: {} updated successfully", id);
        return updated;
    }


    //  getUserByEmail


    @Cacheable(value = "user", key = "#email")
    public UserResponseDTO getUserByEmail(String email) {
        log.info("Fetching user with email: {}", email);
        UserEntity user = userRepository.findByEmail(email).orElseThrow(() -> {
            log.error("User not found with email: {}", email);
            return new UserNotFoundException("User with " + email + " not found!");
        });
        validateAccess(user.getUserId());
        log.info("User retrieved successfully with email: {}", email);
        return mapper.toResponseDTO(user);
    }


    //  deleteUser


    @Override
    @Caching(evict = {
            @CacheEvict(value = "user", key = "#id"),
            @CacheEvict(value = "userList", allEntries = true)
    })
    public String deleteUser(Long id) {
        log.info("Deleting user with ID: {}", id);
        UserEntity userEntity = userRepository.findById(id).orElseThrow(() -> {
            log.error("User not found with id: {} during deletion", id);
            return new UserNotFoundException("User Not Exists");
        });
        userRepository.delete(userEntity);
        log.info("User with ID: {} deleted successfully", id);
        return "User with Id: " + id + " has been deleted!";
    }


    //  updateUserRole


    @Override
    @Caching(
            put   = { @CachePut(value = "user", key = "#userId") },
            evict = { @CacheEvict(value = "userList", allEntries = true) }
    )
    public String updateUserRole(Long userId, RoleUpdateDTO roleUpdateDTO) {
        log.info("Updating user role for user ID: {} to role: {}", userId, roleUpdateDTO.getRole());
        UserEntity user = userRepository.findById(userId).orElseThrow(() -> {
            log.error("User not found with id: {} during role update", userId);
            return new UserNotFoundException("User not found");
        });
        user.setRole(roleUpdateDTO.getRole());
        userRepository.save(user);
        log.info("User ID: {} promoted to role: {}", userId, roleUpdateDTO.getRole());
        return user.getName() + " promoted to " + roleUpdateDTO.getRole() + "!!";
    }


    @Override
    @Cacheable(value = "user", key = "#root.target.getAuthenticatedEmail()")
    public UserResponseDTO getCurrentUser() {
        String email = getAuthenticatedEmail();
        log.info("Fetching current logged-in user with email: {}", email);
        UserEntity user = userRepository.findByEmail(email).orElseThrow(() -> {
            log.error("Current logged-in user not found with email: {}", email);
            return new UserNotFoundException("User not found");
        });
        log.info("Current user retrieved successfully with email: {}", email);
        return mapper.toResponseDTO(user);
    }


    public String getAuthenticatedEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            log.error("No authentication found in SecurityContext");
            throw new UserNotFoundException("Authentication required: No user authenticated");
        }
        String email = auth.getName();
        if (email == null || email.isEmpty()) {
            log.error("Authentication principal name is null or empty");
            throw new UserNotFoundException("Authentication required: Invalid authentication principal");
        }
        log.debug("Retrieved authenticated email: {}", email);
        return email;
    }


    private void sendWelcomeNotification(UserResponseDTO user) {
        try {
            String message = "Hi " + user.getName() + ", welcome to OmniCharge! " +
                    "Your account has been created successfully. " +
                    "You can now recharge your mobile plans instantly. " +
                    "If you didn't create this account, please contact our support team immediately.";

            NotificationEvent event = NotificationEvent.builder()
                    .email(user.getEmail())
                    .phoneNumber(user.getContactNo())
                    .message(message)
                    .type("WELCOME")
                    .subject("Welcome to OmniCharge!")
                    .build();

            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EXCHANGE,
                    RabbitMQConfig.ROUTING_KEY,
                    event
            );

            log.info("Welcome notification published for new user: email={}", user.getEmail());

        } catch (Exception e) {
            log.error("Failed to publish welcome notification for email={}: {}",
                    user.getEmail(), e.getMessage());
        }
    }

    private void validateAccess(Long userId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        String role  = auth.getAuthorities().iterator().next().getAuthority();
        if (role.equals("ROLE_ADMIN")) return;
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        if (!user.getUserId().equals(userId)) {
            throw new AccessDeniedException("Access Denied: you can only access your own data");
        }
    }
}
