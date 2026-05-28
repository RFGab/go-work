package ru.itis.raslgab.gowork.models;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import ru.itis.raslgab.gowork.models.enums.BookingStatus;

import java.time.LocalDateTime;

// Бронь
@Entity
@Table(name = "bookings", indexes = {
        @Index(name = "idx_booking_room_time", columnList = "room_id, time_start, time_finish"),
        @Index(name = "idx_booking_user_status", columnList = "renter_id, status"),
        @Index(name = "idx_booking_status_created", columnList = "status, created_at"),
        @Index(name = "idx_booking_qr", columnList = "qr_code_path", unique = true)
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne()
    @JoinColumn(name = "room_id", nullable = false)
    private Room room; // комната

    @ManyToOne()
    @JoinColumn(name = "user_id",
            nullable = false)
    private User renter; // арендатор

    @Column(nullable = false)
    private LocalDateTime timeStart; // начало брони

    @Column(nullable = false)
    private LocalDateTime timeFinish; // конец

    @Column()
    private Integer numOfPeople; // сколько людей

    @Column()
    private String comment; // коммент пожелание (опционально)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BookingStatus status; // статус заявления

    @Column(nullable = false)
    @UpdateTimestamp
    private LocalDateTime editedAt;

    @Column(nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

}

