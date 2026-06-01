package ru.itis.raslgab.gowork.services;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.itis.raslgab.gowork.models.FileInfo;
import ru.itis.raslgab.gowork.repositories.FileInfoRepo;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileStorageServiceImpl implements FileStorageService {

    private final FileInfoRepo fileInfoRepository;

    @Value("${storage.path}")
    private String storagePath;

    @Override
    public String saveFile(MultipartFile uploadFile) {
        String storageName = UUID.randomUUID() + "_" + uploadFile.getOriginalFilename();
        // + "." + FilenameUtils.getExtention(uploadFile.getOriginalFilename());

        FileInfo file = FileInfo.builder()
                .type(uploadFile.getContentType())
                .originalFileName(uploadFile.getOriginalFilename())
                .size(uploadFile.getSize())
                .storageFileName(storageName)
                .url(storagePath + "\\" + storageName)
                .build();

        try {
//            Files.copy(uploadFile.getInputStream(), Path.of(storagePath, storageName));
            Files.copy(uploadFile.getInputStream(), Paths.get(storagePath, storageName));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }

        fileInfoRepository.save(file);
        return file.getStorageFileName();
    }

    @Override
    public void writeFileToResponse(String fileName, HttpServletResponse response) {
        FileInfo fileInfo = fileInfoRepository.findByStorageFileName(fileName);
        response.setContentType(fileInfo.getType());

        try {
            IOUtils.copy(new FileInputStream(fileInfo.getUrl()), response.getOutputStream());
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}