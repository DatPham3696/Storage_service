package com.storage_service.storage.FileUtils;

import com.storage_service.storage.entity.File;
import com.storage_service.storage.repository.FileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class FileUtils {
    private final FileRepository fileRepository;
    public void checkPrivate(String fileId){
        File file = fileRepository.getFileByFileId(fileId).orElseThrow(() -> new RuntimeException("Not found file"));
        if(file.isVisibility()){
            throw new RuntimeException("Not allow");
        }
    }
    public void checkPublic(String fileId){
        File file = fileRepository.getFileByFileId(fileId).orElseThrow(() -> new RuntimeException("Not found file"));
        if(!file.isVisibility()){
            throw new RuntimeException("Not allow");
        }
    }
}
