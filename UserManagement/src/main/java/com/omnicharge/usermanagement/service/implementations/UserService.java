package com.omnicharge.usermanagement.service.implementations;

import com.omnicharge.usermanagement.dto.RoleUpdateDTO;
import com.omnicharge.usermanagement.dto.UserRequestDTO;
import com.omnicharge.usermanagement.dto.UserResponseDTO;
import com.omnicharge.usermanagement.entity.UserEntity;
import com.omnicharge.usermanagement.exception.UserAlreadyExistsException;
import com.omnicharge.usermanagement.exception.UserNotFoundException;
import com.omnicharge.usermanagement.mapper.CustomMapper;
import com.omnicharge.usermanagement.repository.IUserRepository;
import com.omnicharge.usermanagement.service.IUserService;
import lombok.RequiredArgsConstructor;
import org.apache.catalina.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService implements IUserService {

    private final IUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CustomMapper mapper;

    @Override
    public UserResponseDTO addUser(UserRequestDTO userRequestDTO) {
        if (userRepository.existsByEmail(userRequestDTO.getEmail())) {
            throw new UserAlreadyExistsException("User already exists!");
        }
        UserEntity userEntity = mapper.toEntity(userRequestDTO);


        return mapper.toResponseDTO(userRepository.save(userEntity));

    }

    @Override
    public List<UserResponseDTO> getAllUsers() {
        List<UserEntity> list = userRepository.findAll();
        return list.stream().map(mapper::toResponseDTO).toList();
    }

    @Override
    public UserResponseDTO getUserById(Long id) {
        validateAccess(id);
        UserEntity userEntity = userRepository.findById(id).orElseThrow(()->new UserNotFoundException("User with "+id+" not found!"));
        return mapper.toResponseDTO(userEntity);
    }


    @Override
    public UserResponseDTO updateUser(UserRequestDTO userRequestDTO) {
        Long id = getCurrentUser().getUserId();
        validateAccess(id);

        UserEntity userEntity = userRepository.findById(id).orElseThrow(()-> new UserNotFoundException("User Not Exists"));
        userEntity.setName(userRequestDTO.getName());
        userEntity.setEmail(userRequestDTO.getEmail());
        userEntity.setPassword(passwordEncoder.encode((userRequestDTO.getPassword())));
        userEntity.setContactNo(userRequestDTO.getContactNo());

        return mapper.toResponseDTO(userRepository.save(userEntity));
    }

    public UserResponseDTO getUserByEmail(String email) {
        UserEntity user = userRepository.findByEmail(email).orElseThrow(()->new UserNotFoundException("User with "+email+" not found!"));
        validateAccess(user.getUserId());
        return mapper.toResponseDTO(user);
    }



    @Override
    public String deleteUser(Long id) {
        UserEntity userEntity = userRepository.findById(id).orElseThrow(()-> new UserNotFoundException("User Not Exists"));
        userRepository.delete(userEntity);
        return "User with Id: "+id+" has been deleted!";
    }

    @Override
    public String updateUserRole(Long userId, RoleUpdateDTO roleUpdateDTO) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        user.setRole(roleUpdateDTO.getRole());

        return user.getName()+" promoted to ADMIN!!";
    }

    private void validateAccess(Long userId) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        String email = auth.getName();
        String role = auth.getAuthorities().iterator().next().getAuthority();

        if (role.equals("ROLE_ADMIN")) return;

        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getUserId() != userId) {
            throw new RuntimeException("Access Denied");
        }
    }
    @Override
    public UserResponseDTO getCurrentUser() {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        String email = auth.getName(); // comes from header

        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        return mapper.toResponseDTO(user);
    }
}
