package ru.itis.raslgab.gowork.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.itis.raslgab.gowork.dto.ReviewItemDto;
import ru.itis.raslgab.gowork.models.Review;

import java.util.List;

public interface ReviewRepo extends JpaRepository<Review, Long> {
    @Query("""
            select new ru.itis.raslgab.gowork.dto.ReviewItemDto(
                r.id,
                author.id,
                concat(author.firstName, ' ', author.lastName),
                r.rating,
                r.text,
                r.createdAt,
                case when author.id = :currentUserId then true else false end
            )
            from Review r
            join r.author author
            where r.organization.id = :organizationId
            order by r.createdAt desc
            """)
    List<ReviewItemDto> findItemsByOrganizationId(@Param("organizationId") Long organizationId,
                                                  @Param("currentUserId") Long currentUserId);
}
