package ru.itis.raslgab.gowork.models;

import jakarta.persistence.*;
import lombok.*;
import ru.itis.raslgab.gowork.models.enums.RoleEnum;

import java.util.Set;

// Пользователь (арендатор)
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false)
    private String phone;

    @Column(nullable = false)
    @Builder.Default
    private boolean isBlocked = false;

    @Column
    @Builder.Default
    private boolean isConfirmed = false;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private RoleEnum role;

    @OneToMany(mappedBy = "renter")
    private Set<Booking> bookings;

}

