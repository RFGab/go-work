package ru.itis.raslgab.gowork.controllers.api;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.itis.raslgab.gowork.services.FileStorageService;

@RestController
@RequestMapping("/img")
@RequiredArgsConstructor
public class ImagesController {

    private final FileStorageService fileStorageService;

    @PostMapping
    public ResponseEntity<String> fileUpload(@RequestParam("file") MultipartFile file) {
        System.out.println(file.getName());
        String filePath = fileStorageService.saveFile(file);
        System.out.println(filePath);
        return ResponseEntity.ok()
                .body(filePath);
    }

    @GetMapping("/{file-name:.+}")
    public void getFile(@PathVariable("file-name") String fileName, HttpServletResponse response) {
        fileStorageService.writeFileToResponse(fileName, response);
    }
}
