package ru.itis.raslgab.gowork.services;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {

    String saveFile(MultipartFile file);

    void writeFileToResponse(String fileName, HttpServletResponse response);
}
