package ru.itis.raslgab.gowork.models;


import jakarta.persistence.*;
import lombok.*;
import ru.itis.raslgab.gowork.models.enums.OrganizationStatus;

import java.util.List;
import java.util.Set;

// организация
@Entity
@Table(name = "organizations", indexes = {
        @Index(name = "idx_org_city_status", columnList = "city_id, status"),
        @Index(name = "idx_org_owner", columnList = "owner_id"),
        @Index(name = "idx_org_contact_email", columnList = "contact_email")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Organization {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(columnDefinition = "TEXT", length = 1000)
    private String description;

    @OneToOne()
    @JoinColumn(name = "logo_file_id")
    private FileInfo logo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "city_id")
    private City city;

    private String yandexMapLink;

    private String contactEmail;

    private String contactPhone;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    private User owner;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrganizationStatus status;

    @OneToMany(mappedBy = "organization")
    private Set<Room> rooms;

    @OneToMany(mappedBy = "organization")
    private List<Review> reviews;
}
