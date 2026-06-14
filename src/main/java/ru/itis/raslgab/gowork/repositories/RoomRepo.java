package ru.itis.raslgab.gowork.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.itis.raslgab.gowork.dto.RoomDetailsDto;
import ru.itis.raslgab.gowork.dto.PopularRoomDto;
import ru.itis.raslgab.gowork.dto.RoomCatalogItemDto;
import ru.itis.raslgab.gowork.dto.RoomOptionDto;
import ru.itis.raslgab.gowork.dto.SimilarRoomDto;
import ru.itis.raslgab.gowork.models.Room;
import ru.itis.raslgab.gowork.models.enums.OrganizationStatus;
import ru.itis.raslgab.gowork.models.enums.RoomStatus;

import java.util.List;
import java.util.Optional;

public interface RoomRepo extends JpaRepository<Room, Long> {
    @Query("""
            select new ru.itis.raslgab.gowork.dto.RoomCatalogItemDto(
                r.id,
                r.name,
                r.description,
                null,
                null,
                null,
                r.peopleCapacity,
                r.pricePerHour,
                r.status,
                null,
                r.dayStart,
                r.dayEnd,
                null,
                null
            )
            from Room r
            where r.organization.id = :organizationId
            order by r.name
            """)
    List<RoomCatalogItemDto> findCatalogItemsByOrganizationId(@Param("organizationId") Long organizationId);

    @Query(value = """
            select new ru.itis.raslgab.gowork.dto.RoomCatalogItemDto(
                r.id,
                r.name,
                r.description,
                o.id,
                o.name,
                coalesce(c.name, 'Город не указан'),
                r.peopleCapacity,
                r.pricePerHour,
                r.status,
                null,
                r.dayStart,
                r.dayEnd,
                c.utc,
                null
            )
            from Room r
            join r.organization o
            left join o.city c
            where o.status = :organizationStatus
              and (:cityId is null or c.id = :cityId)
              and (:minCapacity is null or r.peopleCapacity >= :minCapacity)
              and (:availableOnly = false or r.status = :availableStatus)
            """,
            countQuery = """
            select count(r)
            from Room r
            join r.organization o
            left join o.city c
            where o.status = :organizationStatus
              and (:cityId is null or c.id = :cityId)
              and (:minCapacity is null or r.peopleCapacity >= :minCapacity)
              and (:availableOnly = false or r.status = :availableStatus)
            """)
    Page<RoomCatalogItemDto> findRoomCatalogBaseItems(@Param("cityId") Long cityId,
                                                      @Param("minCapacity") Integer minCapacity,
                                                      @Param("organizationStatus") OrganizationStatus organizationStatus,
                                                      @Param("availableOnly") boolean availableOnly,
                                                      @Param("availableStatus") RoomStatus availableStatus,
                                                      Pageable pageable);

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
                r.status,
                null,
                null,
                false,
                r.dayStart,
                r.dayEnd,
                c.utc
            )
            from Room r
            join r.organization o
            left join o.city c
            where r.id = :roomId
            """)
    Optional<RoomDetailsDto> findDetailsById(@Param("roomId") Long roomId);

    @Query("""
            select distinct r
            from Room r
            left join fetch r.images images
            join fetch r.organization organization
            left join fetch organization.owner owner
            where r.id = :roomId
            """)
    Optional<Room> findByIdWithImages(@Param("roomId") Long roomId);

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
            select image.storageFileName
            from Room r
            join r.images image
            where r.id = :roomId
            order by image.id
            """)
    List<String> findImageFileNamesOrderById(@Param("roomId") Long roomId, Pageable pageable);

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

    @Query("""
            select new ru.itis.raslgab.gowork.dto.PopularRoomDto(
                r.id,
                r.name,
                o.name,
                coalesce(c.name, 'Город не указан'),
                r.peopleCapacity,
                r.pricePerHour,
                count(b.id)
            )
            from Room r
            join r.organization o
            left join o.city c
            left join r.bookings b
            where o.status = :organizationStatus
            group by r.id, r.name, o.name, c.name, r.peopleCapacity, r.pricePerHour
            order by count(b.id) desc, r.name
            """)
    List<PopularRoomDto> findPopularRooms(@Param("organizationStatus") OrganizationStatus organizationStatus,
                                          Pageable pageable);
}
