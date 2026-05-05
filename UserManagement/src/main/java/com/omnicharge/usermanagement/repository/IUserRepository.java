package com.omnicharge.usermanagement.repository;


import com.omnicharge.usermanagement.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface IUserRepository extends JpaRepository<UserEntity, Long> {
    public boolean existsByEmail(String email);
    public Optional<UserEntity> findByEmail(String email);
}