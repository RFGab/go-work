package ru.itis.raslgab.gowork.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.itis.raslgab.gowork.models.Booking;

public interface BookingRepo extends JpaRepository<Booking, Integer> {
}
