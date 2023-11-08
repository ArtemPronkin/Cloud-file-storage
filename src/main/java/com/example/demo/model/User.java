package com.example.demo.model;

import com.example.demo.model.Role;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    @Column(unique = true)
    @NotBlank
    @Size(min = 3, max = 20)
    private String username;
    @NotBlank
    private String password;
    @ElementCollection(targetClass = Role.class, fetch = FetchType.EAGER )
    @CollectionTable(name = "user_roles",joinColumns = @JoinColumn(name = "user_id"))
    @Enumerated(EnumType.STRING)
    private Set<Role> roles;
}
