package ru.itis.raslgab.gowork.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.itis.raslgab.gowork.models.FileInfo;

public interface FileInfoRepo extends JpaRepository<FileInfo, Long> {
    FileInfo findByStorageFileName(String fileName);
}
