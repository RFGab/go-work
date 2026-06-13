package ru.itis.raslgab.gowork.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.itis.raslgab.gowork.dto.RoomOptionDto;
import ru.itis.raslgab.gowork.models.Option;

import java.util.List;

public interface OptionRepo extends JpaRepository<Option, Long> {
    @Query("""
            select new ru.itis.raslgab.gowork.dto.RoomOptionDto(
                o.id,
                o.name,
                o.category
            )
            from Option o
            order by o.category, o.name
            """)
    List<RoomOptionDto> findAllOptions();
}
