package ru.itis.raslgab.gowork.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.itis.raslgab.gowork.models.Room;

public interface RoomRepo extends JpaRepository<Room, Long> {
}
