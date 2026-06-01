package ru.itis.raslgab.gowork.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.itis.raslgab.gowork.models.Review;

public interface ReviewRepo extends JpaRepository<Review, Long> {
}
