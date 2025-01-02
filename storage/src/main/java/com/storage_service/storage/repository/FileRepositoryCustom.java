package com.storage_service.storage.repository;

import com.storage_service.storage.dto.request.FileSearchRequest;
import com.storage_service.storage.entity.File;

import java.util.List;

public interface FileRepositoryCustom {
    List<File> searchFile(FileSearchRequest request);
    Long countFile(FileSearchRequest request);
}
