package com.example.demo.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Entity
@Table(name = "users", indexes = {@Index(name = "loginIndex", columnList = "username,email")})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;
    @NotBlank
    @Size(min = 3, max = 20)
    @Column(name = "username", unique = true)
    private String username;
    @NotBlank
    @Email(message = "Not valid Email")
    @Column(name = "email", unique = true)
    private String email;
    @NotBlank
    @Column(name = "password", unique = true)
    private String password;
    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    private Role role;

    public User(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
    }
}
