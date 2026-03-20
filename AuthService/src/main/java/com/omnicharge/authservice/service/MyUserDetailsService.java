package com.omnicharge.authservice.service;

import com.omnicharge.authservice.entity.UserEntity;
import com.omnicharge.authservice.exception.UserNotFoundException;
import com.omnicharge.authservice.repository.IUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MyUserDetailsService implements UserDetailsService {

    private final IUserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        return new UserPrincipal(user);
    }
}