package com.lms.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {

    private final Path fileStorageLocation;

    public FileStorageService(@Value("${file.upload-dir}") String uploadDir) {
        this.fileStorageLocation = Paths.get(uploadDir)
                .toAbsolutePath().normalize();

        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new RuntimeException("Could not create the directory where the uploaded files will be stored.", ex);
        }
    }

    public String storeFile(MultipartFile file) throws IOException {
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());

        // Check if the file's name contains invalid characters
        if (fileName.contains("..")) {
            throw new RuntimeException("Sorry! Filename contains invalid path sequence " + fileName);
        }

        // Generate a unique file name
        String uniqueFileName = UUID.randomUUID().toString() + "_" + fileName;

        // Copy file to the target location
        Path targetLocation = this.fileStorageLocation.resolve(uniqueFileName);
        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

        return uniqueFileName;
    }

    public byte[] getFile(String fileName) throws IOException {
        Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
        
        if (!Files.exists(filePath)) {
            throw new RuntimeException("File not found " + fileName);
        }
        
        return Files.readAllBytes(filePath);
    }

    public void deleteFile(String fileName) throws IOException {
        Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
        
        if (!Files.exists(filePath)) {
            throw new RuntimeException("File not found " + fileName);
        }
        
        Files.delete(filePath);
    }
}
