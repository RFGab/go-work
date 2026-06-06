package ru.itis.raslgab.gowork.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.itis.raslgab.gowork.dto.RoomDetailsDto;
import ru.itis.raslgab.gowork.dto.RoomCatalogItemDto;
import ru.itis.raslgab.gowork.dto.RoomOptionDto;
import ru.itis.raslgab.gowork.dto.SimilarRoomDto;
import ru.itis.raslgab.gowork.models.Room;
import ru.itis.raslgab.gowork.models.enums.OrganizationStatus;

import java.util.List;
import java.util.Optional;

public interface RoomRepo extends JpaRepository<Room, Long> {
    @Query("""
            select new ru.itis.raslgab.gowork.dto.RoomCatalogItemDto(
                r.id,
                r.name,
                r.description,
                r.peopleCapacity,
                r.pricePerHour,
                r.status
            )
            from Room r
            where r.organization.id = :organizationId
            order by r.name
            """)
    List<RoomCatalogItemDto> findCatalogItemsByOrganizationId(@Param("organizationId") Long organizationId);

    @Query("""
            select new ru.itis.raslgab.gowork.dto.RoomCatalogItemDto(
                r.id,
                r.name,
                r.description,
                o.id,
                o.name,
                coalesce(c.name, 'Город не указан'),
                r.peopleCapacity,
                r.pricePerHour,
                r.status
            )
            from Room r
            join r.organization o
            left join o.city c
            where o.status = :organizationStatus
              and (:cityId is null or c.id = :cityId)
              and (:minCapacity is null or r.peopleCapacity >= :minCapacity)
            order by r.name
            """)
    List<RoomCatalogItemDto> findRoomCatalogBaseItems(@Param("cityId") Long cityId,
                                                      @Param("minCapacity") Integer minCapacity,
                                                      @Param("organizationStatus") OrganizationStatus organizationStatus);

    @Query("""
            select new ru.itis.raslgab.gowork.dto.RoomDetailsDto(
                r.id,
                r.name,
                r.description,
                o.id,
                o.name,
                c.id,
                coalesce(c.name, 'Город не указан'),
                o.contactEmail,
                o.contactPhone,
                r.peopleCapacity,
                r.pricePerHour,
                r.status
            )
            from Room r
            join r.organization o
            left join o.city c
            where r.id = :roomId
            """)
    Optional<RoomDetailsDto> findDetailsById(@Param("roomId") Long roomId);

    @Query("""
            select new ru.itis.raslgab.gowork.dto.RoomOptionDto(
                opt.id,
                opt.name,
                opt.category
            )
            from Room r
            join r.options opt
            where r.id = :roomId
            order by opt.category, opt.name
            """)
    List<RoomOptionDto> findOptionsByRoomId(@Param("roomId") Long roomId);

    @Query("""
            select new ru.itis.raslgab.gowork.dto.SimilarRoomDto(
                r.id,
                r.name,
                o.name,
                r.peopleCapacity,
                r.pricePerHour
            )
            from Room r
            join r.organization o
            left join o.city c
            where r.id <> :roomId
              and o.status = :organizationStatus
              and c.id = :cityId
              and abs(r.peopleCapacity - :peopleCapacity) <= 3
            order by abs(r.peopleCapacity - :peopleCapacity), r.pricePerHour, r.name
            """)
    List<SimilarRoomDto> findSimilarRooms(@Param("roomId") Long roomId,
                                          @Param("cityId") Long cityId,
                                          @Param("peopleCapacity") Integer peopleCapacity,
                                          @Param("organizationStatus") OrganizationStatus organizationStatus);
}
