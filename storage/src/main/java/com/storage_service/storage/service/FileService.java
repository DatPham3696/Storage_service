package com.storage_service.storage.service;

import com.storage_service.storage.dto.request.FileSearchRequest;
import com.storage_service.storage.dto.response.FilesResponse;
import com.storage_service.storage.entity.File;
import com.storage_service.storage.repository.FileRepository;
import com.storage_service.storage.repository.FileRepositoryImpl;
import lombok.RequiredArgsConstructor;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileService {
    private final FileRepository fileRepository;
    private final FileRepositoryImpl fileRepositoryImpl;
    @Value("${file.upload-dir}")
    private String uploadDir;

    public File uploadFile(MultipartFile file, boolean visibility, String version, String owner) throws IOException {
        String uuidFileName = UUID.randomUUID().toString() + "." + getFileExtension(file);
        String currentDate = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        Path uploadPath = Paths.get(uploadDir, currentDate);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        Path filePath = uploadPath.resolve(uuidFileName);
        Files.copy(file.getInputStream(), filePath);

        return fileRepository.save(File.builder()
                .fileName(uuidFileName)
                .fileType(file.getContentType())
                .fileSize(String.valueOf(file.getSize()))
                .filePath(filePath.toString())
                .visibility(visibility)
                .version(version)
                        .owner(owner)
                .build());
    }
    private String getFileExtension(MultipartFile file) {
        String fileName = file.getOriginalFilename();
        return fileName != null && fileName.contains(".") ? fileName.substring(fileName.lastIndexOf(".") + 1) : "";
    }
    public String uploadFiles(List<MultipartFile> files, boolean visibility, String version, String owner) throws IOException {
        if (files.isEmpty()) {
            return "Files empty";
        }
        for (MultipartFile file : files) {
            uploadFile(file, visibility, version, owner);
        }
        return "Upload file success";
    }

    public Resource getContent(String fileId) throws IOException {
        File fileEntity = fileRepository.findById(fileId).orElseThrow(() -> new RuntimeException("File not found"));
        Path path = Paths.get(fileEntity.getFilePath());
        Resource resource = new FileSystemResource(path);
        if (!resource.exists()) {
            throw new IOException("File not found: " + fileEntity.getFileName());
        }
        return resource;
    }

    public byte[] processImage(Resource resource, Optional<Integer> width, Optional<Integer> height, Optional<Double> ratio) throws IOException {
        String contentType = Files.probeContentType(Paths.get(resource.getURI()));
        if (contentType != null && contentType.startsWith("image")) {
            InputStream inputStream = resource.getInputStream();
            Thumbnails.Builder<?> thumbnailBuilder = Thumbnails.of(inputStream);
            if (width.isPresent() && height.isPresent()) {
                thumbnailBuilder.forceSize(width.get(), height.get());
            } else if (ratio.isPresent()) {
                thumbnailBuilder.scale(ratio.get());
            }
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            thumbnailBuilder.toOutputStream(outputStream);

            return outputStream.toByteArray();
        }
        return Files.readAllBytes(Paths.get(resource.getURI()));
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

    public FilesResponse<File> pagingFile(FileSearchRequest request) {
        List<File> file = fileRepositoryImpl.searchFile(request);
        Long totalFile = fileRepositoryImpl.countFile(request);
        return new FilesResponse<>(file, (totalFile / request.getSize()));
    }

    public ResponseEntity<Resource> downloadFile(String fileId) throws IOException {
        Resource resource = getContent(fileId);
        File file = fileRepository.findById(fileId).orElseThrow();
        String mimeType = file.getFileType();
        if (mimeType == null || mimeType.isEmpty()) {
            mimeType = "application/octet-stream";
        }
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(mimeType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFileName() + "\"")
                .body(resource);
    }

}
