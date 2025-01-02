package com.storage_service.storage.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileSearchRequest extends SearchRequest{
    private String fileName;
    private String fileType;
    private Instant createdDate;
    private String lastModifiedDate;
    private String createdBy;
}
