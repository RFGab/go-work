package ru.itis.raslgab.gowork.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.itis.raslgab.gowork.models.User;

import java.util.Optional;

public interface UserRepo extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    boolean existsByEmailAndIdNot(String email, Long id);
}
