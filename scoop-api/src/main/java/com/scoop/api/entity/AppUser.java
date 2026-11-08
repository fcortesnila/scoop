package com.scoop.api.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class AppUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 160)
    private String name;
    @Column(nullable = false, unique = true, length = 254)
    private String email;
    @Column(name = "google_sub", nullable = false, unique = true)
    private String googleSub;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;
}
