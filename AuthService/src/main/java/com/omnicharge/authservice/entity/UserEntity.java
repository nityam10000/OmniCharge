package com.omnicharge.authservice.entity;


import com.omnicharge.authservice.enums.Roles;
import jakarta.persistence.*;
import lombok.*;


@Entity
@Table(name = "userentity")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    private String name;

    @Column(unique = true)
    private String email;

    private String contactNo;

    private String password;

    @Enumerated(EnumType.STRING)
    private Roles role;

    @Column(length = 512)
    private String refreshToken;

    private java.time.LocalDateTime refreshTokenExpiry;
}
