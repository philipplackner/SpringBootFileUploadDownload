package com.plcoding.springbootfileuploaddownload.service;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.util.UUID;

@Service
public class LocalImageStorageService {

    private final ImageStorageProperties properties;
    private final Path rootPath;

    public LocalImageStorageService(ImageStorageProperties properties) {
        this.properties = properties;
        this.rootPath = Paths.get(properties.basePath());
    }

    public String storeFile(InputStream inputStream, String originalName) throws IOException {
        LocalDate today = LocalDate.now();
        Path dateDirectory = rootPath.resolve(
            today.getYear() + File.separator +
                String.format("%02d", today.getMonthValue()) + File.separator +
                String.format("%02d", today.getDayOfMonth())
        );

        Files.createDirectories(dateDirectory);

        String ext = getFileExtension(originalName);
        String storedName = UUID.randomUUID() + (ext.isEmpty() ? "" : "." + ext);
        Path filePath = dateDirectory.resolve(storedName);

        try(OutputStream outputStream = Files.newOutputStream(filePath, StandardOpenOption.CREATE_NEW)) {
            StreamUtils.copy(inputStream, outputStream);
        }

        return rootPath.relativize(filePath).toString();
    }

    public Resource getFileResource(String storedPath) throws IOException {
        Path filePath = rootPath.resolve(storedPath).normalize().toAbsolutePath();
        Path normalizedRoot = rootPath.normalize().toAbsolutePath();

        if(!filePath.startsWith(normalizedRoot)) {
            throw new SecurityException("Access denied");
        }

        if(!Files.exists(filePath)) {
            throw new FileNotFoundException("File not found");
        }

        return new UrlResource(filePath.toUri());
    }

    private String getFileExtension(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        return lastDot == -1 ? "" : fileName.substring(lastDot + 1);
    }
}
