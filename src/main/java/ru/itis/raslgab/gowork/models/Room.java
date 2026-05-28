package ru.itis.raslgab.gowork.models;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import ru.itis.raslgab.gowork.models.enums.RoomStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

// Комната (переговорка или х его знает что еще)
@Entity
@Table(name = "rooms", indexes = {
        @Index(name = "idx_room_org_status", columnList = "organization_id, status"),
        @Index(name = "idx_room_city_price", columnList = "city, price_per_hour"),
        @Index(name = "idx_room_capacity", columnList = "capacity")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false, length = 30)
    private String name;

    @Column()
    private String description;

    @Column(nullable = false)
    private Integer peopleCapacity;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal pricePerHour;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private RoomStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @OneToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "room_images",
            joinColumns = @JoinColumn(name="room_id", nullable = false),
            inverseJoinColumns = @JoinColumn(name = "image_id", nullable = false)
    )
    private Set<FileInfo> images;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "room_options",
            joinColumns = @JoinColumn(name = "room_id", nullable = false),
            inverseJoinColumns = @JoinColumn(name = "option_id", nullable = false)
    )
    private Set<Option> options; // теги: projector, whiteboard, coffee и прочее что может быть в комнате

    @Column(nullable = false)
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Column(nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "room", fetch = FetchType.LAZY)
    private Set<Booking> bookings;

}

