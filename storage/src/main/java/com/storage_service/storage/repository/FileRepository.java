package com.storage_service.storage.repository;

import com.storage_service.storage.entity.File;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FileRepository extends JpaRepository<File,String> {
    Optional<File> getFileByFileId(String fileId);
}
