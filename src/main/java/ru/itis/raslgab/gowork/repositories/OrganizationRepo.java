package ru.itis.raslgab.gowork.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.itis.raslgab.gowork.dto.OrganizationProfileItemDto;
import ru.itis.raslgab.gowork.models.Organization;

import java.util.List;

public interface OrganizationRepo extends JpaRepository<Organization, Long> {
    @Query("""
            select new ru.itis.raslgab.gowork.dto.OrganizationProfileItemDto(
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
}
