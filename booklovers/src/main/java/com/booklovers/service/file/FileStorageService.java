package com.booklovers.service.file;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;

public interface FileStorageService {
    String storeFile(MultipartFile file, String subdirectory);
    Resource loadFileAsResource(String filename, String subdirectory);
    void deleteFile(String filename, String subdirectory);
    Path getFileLocation(String filename, String subdirectory);
}
