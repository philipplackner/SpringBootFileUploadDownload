package com.plcoding.springbootfileuploaddownload.controller;

import com.plcoding.springbootfileuploaddownload.repository.ImageMetadata;
import com.plcoding.springbootfileuploaddownload.service.ImageService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/images")
public class ImageController {

    private final ImageService imageService;

    public ImageController(ImageService imageService) {
        this.imageService = imageService;
    }

    @PostMapping("/upload")
    public ResponseEntity<?> uploadImage(
        @RequestParam("file") MultipartFile file
    ) {
        String ownerId = "123";

        try {
            ImageMetadata metadata = imageService.uploadImage(file, ownerId);

            return ResponseEntity
                .ok(
                    Map.ofEntries(
                        Map.entry("imageId", metadata.id().toHexString()),
                        Map.entry("originalName", metadata.originalName()),
                        Map.entry("size", metadata.size())
                    )
                );
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @GetMapping("/{imageId}")
    public ResponseEntity<Resource> downloadImage(
        @PathVariable String imageId
    ) {
        try {
            ImageMetadata metadata = imageService.getImageMetadata(imageId);
            Resource resource = imageService.getImageResource(imageId);

            return ResponseEntity
                .ok()
                .header(
                    HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=\"" + metadata.originalName() + "\""
                )
                .contentType(MediaType.parseMediaType(metadata.mimeType()))
                .contentLength(metadata.size())
                .body(resource);
        } catch(IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
