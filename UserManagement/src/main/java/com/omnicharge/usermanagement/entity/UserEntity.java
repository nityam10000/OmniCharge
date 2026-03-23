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
    private  Long userId;

    private String name;

    @Column(unique = true)
    private String email;

    private String contactNo;

    private String password;

    @Enumerated(EnumType.STRING)
    private Roles role=Roles.USER;
	public UserEntity(String name, String email, String contactNo, String password, Roles role) {
		super();
		this.name = name;
		this.email = email;
		this.contactNo = contactNo;
		this.password = password;
		this.role = role;
	}
    
    
}
