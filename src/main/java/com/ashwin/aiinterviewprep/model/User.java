package com.ashwin.aiinterviewprep.model;

import jakarta.persistence.*;
import lombok.*;


@Entity
@Table(name="users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true)

    private String email;

    private String name;

    @Column(unique = true, nullable = false)
    private String password;

    private String fullName;
}
