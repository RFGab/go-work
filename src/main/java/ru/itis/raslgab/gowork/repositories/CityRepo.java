package ru.itis.raslgab.gowork.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.itis.raslgab.gowork.models.City;

public interface CityRepo extends JpaRepository<City, Long> {
}
