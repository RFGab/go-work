package ru.itis.raslgab.gowork.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.itis.raslgab.gowork.models.Organization;

public interface OrganizationRepo extends JpaRepository<Organization, Integer> {
}
