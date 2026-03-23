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

    private String email;

    private String contactNo;

    private String password;

    @Enumerated(EnumType.STRING)
    private Roles role=Roles.USER;

	public UserEntity(Long userId, String name, String email, String contactNo, String password, Roles role) {
		super();
		this.userId = userId;
		this.name = name;
		this.email = email;
		this.contactNo = contactNo;
		this.password = password;
		this.role = role;
	}

	public Long getUserId() {
		return userId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getContactNo() {
		return contactNo;
	}

	public void setContactNo(String contactNo) {
		this.contactNo = contactNo;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public Roles getRole() {
		return role;
	}

	public void setRole(Roles role) {
		this.role = role;
	}

	public UserEntity(String name, String email, String contactNo, String password, Roles role) {
		super();
		this.name = name;
		this.email = email;
		this.contactNo = contactNo;
		this.password = password;
		this.role = role;
	}

	public UserEntity() {
		super();
		// TODO Auto-generated constructor stub
	}
    
    
}
