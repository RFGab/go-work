package ru.itis.raslgab.gowork.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.itis.raslgab.gowork.dto.BookingIntervalDto;
import ru.itis.raslgab.gowork.dto.BookingListItemDto;
import ru.itis.raslgab.gowork.models.Booking;
import ru.itis.raslgab.gowork.models.enums.BookingStatus;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface BookingRepo extends JpaRepository<Booking, Long> {
    @Query("""
            select new ru.itis.raslgab.gowork.dto.BookingIntervalDto(
                b.room.id,
                b.timeStart,
                b.timeFinish
            )
            from Booking b
            where b.room.id in :roomIds
              and b.status in :statuses
              and b.timeStart < :dayEnd
              and b.timeFinish > :dayStart
            order by b.room.id, b.timeStart
            """)
    List<BookingIntervalDto> findBlockingIntervals(@Param("roomIds") Collection<Long> roomIds,
                                                   @Param("statuses") Collection<BookingStatus> statuses,
                                                   @Param("dayStart") LocalDateTime dayStart,
                                                   @Param("dayEnd") LocalDateTime dayEnd);

    boolean existsByRoomIdAndStatusInAndTimeStartLessThanAndTimeFinishGreaterThan(Long roomId,
                                                                                  Collection<BookingStatus> statuses,
                                                                                  LocalDateTime timeFinish,
                                                                                 LocalDateTime timeStart);

    @Query("""
            select b
            from Booking b
            join fetch b.renter renter
            join fetch b.room room
            join fetch room.organization organization
            join fetch organization.owner owner
            left join fetch organization.city city
            where b.id = :bookingId
            """)
    Optional<Booking> findDetailsById(@Param("bookingId") Long bookingId);

    @Query("""
            select new ru.itis.raslgab.gowork.dto.BookingListItemDto(
                b.id,
                room.id,
                room.name,
                organization.id,
                organization.name,
                coalesce(city.name, 'Город не указан'),
                b.timeStart,
                b.timeFinish,
                b.numOfPeople,
                b.comment,
                b.status,
                b.createdAt
            )
            from Booking b
            join b.room room
            join room.organization organization
            left join organization.city city
            where b.renter.id = :renterId
            order by b.timeStart desc, b.createdAt desc
            """)
    List<BookingListItemDto> findUserBookingItems(@Param("renterId") Long renterId);
}
