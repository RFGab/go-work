package ru.itis.raslgab.gowork.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.itis.raslgab.gowork.dto.organizations.OrganizationCatalogItemDto;
import ru.itis.raslgab.gowork.dto.organizations.OrganizationProfileItemDto;
import ru.itis.raslgab.gowork.models.Organization;
import ru.itis.raslgab.gowork.models.enums.OrganizationStatus;

import java.util.List;
import java.util.Optional;

public interface OrganizationRepo extends JpaRepository<Organization, Long> {
    @Query("""
            select new ru.itis.raslgab.gowork.dto.organizations.OrganizationProfileItemDto(
                o.id,
                o.name,
                coalesce(c.name, 'Город не указан'),
                count(r.id)
            )
            from Organization o
            left join o.city c
            left join o.rooms r
            where o.owner.id = :ownerId
            group by o.id, o.name, c.name
            order by o.name
            """)
    List<OrganizationProfileItemDto> findProfileItemsByOwnerId(@Param("ownerId") Long ownerId);

    @Query("""
            select o
            from Organization o
            left join fetch o.city
            left join fetch o.owner
            left join fetch o.logo
            where o.id = :organizationId
            """)
    Optional<Organization> findDetailsById(@Param("organizationId") Long organizationId);

    @Query("""
            select new ru.itis.raslgab.gowork.dto.organizations.OrganizationCatalogItemDto(
                o.id,
                o.name,
                o.description,
                coalesce(c.name, 'Город не указан'),
                o.contactEmail,
                o.contactPhone,
                logo.storageFileName,
                count(r.id)
            )
            from Organization o
            left join o.city c
            left join o.logo logo
            left join o.rooms r
            where o.status = :status
              and (:cityId is null or c.id = :cityId)
            group by o.id, o.name, o.description, c.name, o.contactEmail, o.contactPhone, logo.storageFileName
            order by o.name
            """)
    List<OrganizationCatalogItemDto> findCatalogItems(@Param("cityId") Long cityId,
                                                      @Param("status") OrganizationStatus status);

    @Query(value = """
            select new ru.itis.raslgab.gowork.dto.organizations.OrganizationCatalogItemDto(
                o.id,
                o.name,
                o.description,
                coalesce(c.name, 'Город не указан'),
                o.contactEmail,
                o.contactPhone,
                logo.storageFileName,
                count(r.id)
            )
            from Organization o
            left join o.city c
            left join o.logo logo
            left join o.rooms r
            where o.status = :status
              and (:cityId is null or c.id = :cityId)
            group by o.id, o.name, o.description, c.name, o.contactEmail, o.contactPhone, logo.storageFileName
            order by o.name
            """,
            countQuery = """
            select count(o)
            from Organization o
            left join o.city c
            where o.status = :status
              and (:cityId is null or c.id = :cityId)
            """)
    Page<OrganizationCatalogItemDto> findCatalogItemsPage(@Param("cityId") Long cityId,
                                                          @Param("status") OrganizationStatus status,
                                                          Pageable pageable);

    @Query(value = """
            select new ru.itis.raslgab.gowork.dto.organizations.OrganizationCatalogItemDto(
                o.id,
                o.name,
                o.description,
                coalesce(c.name, 'Город не указан'),
                o.contactEmail,
                o.contactPhone,
                logo.storageFileName,
                count(r.id)
            )
            from Organization o
            left join o.city c
            left join o.logo logo
            left join o.rooms r
            where o.status = :status
              and (:cityId is null or c.id = :cityId)
              and lower(o.name) like :namePattern
            group by o.id, o.name, o.description, c.name, o.contactEmail, o.contactPhone, logo.storageFileName
            order by o.name
            """,
            countQuery = """
            select count(o)
            from Organization o
            left join o.city c
            where o.status = :status
              and (:cityId is null or c.id = :cityId)
              and lower(o.name) like :namePattern
            """)
    Page<OrganizationCatalogItemDto> findCatalogItemsPageByName(@Param("cityId") Long cityId,
                                                                @Param("namePattern") String namePattern,
                                                                @Param("status") OrganizationStatus status,
                                                                Pageable pageable);
}
