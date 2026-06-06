package ru.itis.raslgab.gowork.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.itis.raslgab.gowork.dto.CityOptionDto;
import ru.itis.raslgab.gowork.models.City;

import java.util.List;
import java.util.Optional;

public interface CityRepo extends JpaRepository<City, Long> {
    Optional<City> findByNameIgnoreCase(String name);

    @Query("""
            select new ru.itis.raslgab.gowork.dto.CityOptionDto(c.id, c.name)
            from City c
            order by c.name
            """)
    List<CityOptionDto> findOptions();
}
