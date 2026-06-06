package ru.itis.raslgab.gowork.controllers.api;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.itis.raslgab.gowork.services.FileStorageService;

@RestController
@RequestMapping("/img")
@RequiredArgsConstructor
public class ImagesController {

    private final FileStorageService fileStorageService;

    @GetMapping("/{file-name:.+}")
    public void getFile(@PathVariable("file-name") String fileName, HttpServletResponse response) {
        fileStorageService.writeFileToResponse(fileName, response);
    }
}
