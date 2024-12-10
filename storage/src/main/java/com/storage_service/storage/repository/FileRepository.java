package com.storage_service.storage.repository;

import com.storage_service.storage.entity.File;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FileRepository extends JpaRepository<File,String> {
    Optional<File> getFileByFileId(String fileId);
}
