package com.omnicharge.usermanagement.service.implementations;

import com.omnicharge.usermanagement.dto.UserRequestDTO;
import com.omnicharge.usermanagement.dto.UserResponseDTO;
import com.omnicharge.usermanagement.entity.UserEntity;
import com.omnicharge.usermanagement.exception.UserAlreadyExistsException;
import com.omnicharge.usermanagement.exception.UserNotFoundException;
import com.omnicharge.usermanagement.mapper.CustomMapper;
import com.omnicharge.usermanagement.repository.IUserRepository;
import com.omnicharge.usermanagement.service.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService implements IUserService {

    private final IUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CustomMapper mapper;
    
    

   

	public UserService(IUserRepository userRepository, PasswordEncoder passwordEncoder, CustomMapper mapper) {
		super();
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.mapper = mapper;
	}

	@Override
    public UserResponseDTO addUser(UserRequestDTO userRequestDTO) {
        if(userRepository.existsByEmail(userRequestDTO.getEmail())){
            throw new UserAlreadyExistsException("User with "+userRequestDTO.getEmail()+" already exixts!");
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
        UserEntity userEntity = userRepository.findById(id).orElseThrow(()->new UserNotFoundException("User with "+id+" not found!"));
        return mapper.toResponseDTO(userEntity);
    }


    @Override
    public UserResponseDTO updateUser(Long id,UserRequestDTO userRequestDTO) {

        UserEntity userEntity = userRepository.findById(id).orElseThrow(()-> new UserNotFoundException("User Not Exists"));
        userEntity.setName(userRequestDTO.getName());
        userEntity.setEmail(userRequestDTO.getEmail());
        userEntity.setPassword(passwordEncoder.encode((userRequestDTO.getPassword())));
        userEntity.setContactNo(userRequestDTO.getContactNo());

        return mapper.toResponseDTO(userEntity);
    }

    @Override
    public String deleteUser(Long id) {
        UserEntity userEntity = userRepository.findById(id).orElseThrow(()-> new UserNotFoundException("User Not Exists"));
        userRepository.delete(userEntity);
        return "User with Id: "+id+" has been deleted!";
    }
}
