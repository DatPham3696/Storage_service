package com.storage_service.storage.service;

import com.storage_service.storage.entity.File;
import com.storage_service.storage.repository.FileRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.awt.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FileService {
    private final FileRepository fileRepository;
    @Value("${file.upload-dir}")
    private String uploadDir;

    public File uploadFile(MultipartFile file, boolean visibility, String version) throws IOException {
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath); // Tạo thư mục
        }
        String fileName = file.getOriginalFilename();
        Path filePath = uploadPath.resolve(fileName);
        Files.copy(file.getInputStream(), filePath);

        return fileRepository.save(File.builder()
                .fileName(fileName)
                .fileType(file.getContentType())
                .fileSize(String.valueOf(file.getSize()))
                .filePath(filePath.toString())
                .visibility(visibility)
                .version(version)
                .build());
    }

    public String uploadFiles(List<MultipartFile> files, boolean visibility, String version) throws IOException {
        if (files.isEmpty()) {
            return "Files empty";
        }
        for (MultipartFile file : files) {
            uploadFile(file, visibility, version);
        }
        return "Upload file success";
    }

    public Resource getFile(String fileId) throws IOException {
        File fileEntity = fileRepository.findById(fileId).orElseThrow(() -> new RuntimeException("File not found"));
        Path path = Paths.get(fileEntity.getFilePath());
        Resource resource = new FileSystemResource(path);
        if (!resource.exists()) {
            throw new IOException("File not found: " + fileEntity.getFileName());
        }
        return resource;
    }

    public File updateFile(String fileId, MultipartFile file, boolean visibility, String version) throws IOException {
        File existingFile = fileRepository.findById(fileId).orElseThrow(() -> new RuntimeException("File not exist"));
        Path uploadPath = Paths.get(uploadDir);
        String fileName = file.getOriginalFilename();
        Path filePath = uploadPath.resolve(fileName);
        if (Files.exists(Paths.get(existingFile.getFilePath()))) {
            Files.delete(Paths.get(existingFile.getFilePath()));
        }
        Files.copy(file.getInputStream(), filePath);

        existingFile.setFileName(fileName);
        existingFile.setFileType(file.getContentType());
        existingFile.setFileSize(String.valueOf(file.getSize()));
        existingFile.setFilePath(filePath.toString());
        existingFile.setVisibility(visibility);
        existingFile.setVersion(version);

        return fileRepository.save(existingFile);
    }

    public void deleteFile(String fileId) throws IOException {
        File file = fileRepository.findById(fileId).orElseThrow(() -> new RuntimeException("File not found"));
        Path filePath = Paths.get(file.getFilePath());
        if (Files.exists(filePath)) {
            Files.delete(filePath);
        } else {
            throw new IOException("File not found");
        }
        fileRepository.delete(file);
    }
}
