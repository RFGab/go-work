package ru.itis.raslgab.gowork.mappers;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import ru.itis.raslgab.gowork.models.FileInfo;

import java.nio.file.Path;

@Component
public class FileInfoDataMapper {

    public FileInfo mapUploadToModel(MultipartFile uploadFile, String storageName, Path storageFilePath) {
        return FileInfo.builder()
                .type(uploadFile.getContentType())
                .originalFileName(uploadFile.getOriginalFilename())
                .size(uploadFile.getSize())
                .storageFileName(storageName)
                .url(storageFilePath.toString())
                .build();
    }
}
