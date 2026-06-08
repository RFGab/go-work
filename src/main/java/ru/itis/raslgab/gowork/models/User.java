package ru.itis.raslgab.gowork.models;

import jakarta.persistence.*;
import lombok.*;
import ru.itis.raslgab.gowork.models.enums.RoleEnum;

import java.util.HashSet;
import java.util.Set;

// Пользователь (арендатор)
@Entity
@Table(name = "users", indexes = {
        @Index(name = "idx_users_email", columnList = "email", unique = true),
        @Index(name = "idx_users_role_confirmed", columnList = "role, is_confirmed")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
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

    @Column(name = "avatar_file_name")
    private String avatarFileName;

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
    @Builder.Default
    private Set<Booking> bookings =  new HashSet<>();

}
