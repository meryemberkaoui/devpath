package com.devPath.user.domain;

import com.devPath.user.ressources.Level;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor
public class User {
    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false, unique = true)
    private String keycloakId;

    @Column(nullable = false, unique = true)
    private  String email;

    @Column(nullable = false, unique = true)
    private String username;


    private Level level;
    private String githubUrl;
    private String linkedinUrl;

    public User(String keycloakId, String email, String fullName) {
        this.keycloakId = keycloakId;
        this.email = email;
        this.username = fullName;
    }


}
