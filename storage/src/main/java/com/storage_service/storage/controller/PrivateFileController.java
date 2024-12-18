package com.storage_service.storage.controller;

import com.storage_service.storage.FileUtils.FileUtils;
import com.storage_service.storage.dto.request.FileSearchRequest;
import com.storage_service.storage.dto.response.FilesResponse;
import com.storage_service.storage.entity.File;
import com.storage_service.storage.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/file/private")
@RequiredArgsConstructor
public class PrivateFileController {
    private final FileService fileService;
    private final FileUtils fileUtils;
    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file,
                                        @RequestParam("visibility") boolean visibility,
                                        @RequestParam("version") String version,
                                        @RequestParam(value = "owner", defaultValue = "Anonymous user") String owner) {
        try {
            return ResponseEntity.ok().body(fileService.uploadFile(file, visibility, version, owner));
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
    @PostMapping("/uploads")
    public ResponseEntity<?> uploadFiles(@RequestParam("files") List<MultipartFile> files,
                                         @RequestParam("visibility") boolean visibility,
                                         @RequestParam("version") String version,
                                         @RequestParam(value = "owner", defaultValue = "System") String owner) {
        try {
            return ResponseEntity.ok().body(fileService.uploadFiles(files, visibility, version, owner));
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
    @PostMapping("/files-search")
    public ResponseEntity<FilesResponse<File>> getListPagingFile(@RequestBody FileSearchRequest request){
        try {
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(fileService.pagingFile(request));
        } catch (RuntimeException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
    @DeleteMapping("/delete/{fileId}")
    public ResponseEntity<?> deleteFile(@PathVariable String fileId) {
        try {
//            fileUtils.checkPrivate(fileId);
            fileService.deleteFile(fileId);
            return ResponseEntity.ok().body("Delete successful");
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }
    @GetMapping("/get-content/{fileId}")
    public ResponseEntity<?> getFileContent(@PathVariable("fileId") String id) {
        try {
//            fileUtils.checkPrivate(id);
            Resource resource = fileService.getContent(id);
            String contentType = Files.probeContentType(Paths.get(resource.getURI()));
            if (contentType == null) {
                contentType = "application/octet-stream";
            }
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(resource);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }
    @GetMapping("/view-image/{filedId}")
    public ResponseEntity<?> viewImage(
            @PathVariable("filedId") String fileId,
            @RequestParam Optional<Integer> width,
            @RequestParam Optional<Integer> height,
            @RequestParam Optional<Double> ratio
    ) {
        try {
//            fileUtils.checkPrivate(fileId);
            Resource resource = fileService.getContent(fileId);
            byte[] fileContent = fileService.processImage(resource, width, height, ratio);
            String contentType = Files.probeContentType(Paths.get(resource.getURI()));
            if (contentType == null) {
                contentType = "application/octet-stream";
            }
            return ResponseEntity.ok().contentType(MediaType.parseMediaType(contentType)).body(fileContent);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Error: " + e.getMessage());
        }
    }
}
