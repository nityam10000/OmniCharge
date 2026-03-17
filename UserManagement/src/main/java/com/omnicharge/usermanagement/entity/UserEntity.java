package com.omnicharge.usermanagement.entity;

import com.omnicharge.usermanagement.enums.Roles;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "userentity")
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Builder
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private  long userId;

    private String name;

    private String email;

    private String contactNo;

    private String password;

    @Enumerated(EnumType.STRING)
    private Roles role=Roles.USER;
}
