package com.booklovers.service.file;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Slf4j
@Service
public class FileStorageServiceImp implements FileStorageService {
    
    private final Path fileStorageLocation;
    
    public FileStorageServiceImp(@Value("${file.upload-dir:uploads}") String uploadDir) {
        this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        
        try {
            Files.createDirectories(this.fileStorageLocation);
            log.info("Katalog do przechowywania plików utworzony: {}", this.fileStorageLocation);
        } catch (Exception ex) {
            log.error("Nie można utworzyć katalogu do przechowywania plików: {}", this.fileStorageLocation, ex);
            throw new RuntimeException("Nie można utworzyć katalogu do przechowywania plików", ex);
        }
    }
    
    @Override
    public String storeFile(MultipartFile file, String subdirectory) {
        log.info("Zapisywanie pliku: originalFilename={}, subdirectory={}, size={}", 
                file.getOriginalFilename(), subdirectory, file.getSize());
        
        if (file.isEmpty()) {
            log.warn("Próba zapisania pustego pliku");
            throw new RuntimeException("Plik jest pusty");
        }
        
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            log.warn("Nieprawidłowy typ pliku: {}", contentType);
            throw new RuntimeException("Tylko pliki graficzne są dozwolone");
        }
        
        try {
            Path subdirectoryPath = this.fileStorageLocation.resolve(subdirectory);
            Files.createDirectories(subdirectoryPath);
            
            String originalFilename = file.getOriginalFilename();
            String fileExtension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String fileName = UUID.randomUUID().toString() + fileExtension;
            
            Path targetLocation = subdirectoryPath.resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            
            String relativePath = subdirectory + "/" + fileName;
            log.info("Plik zapisany pomyślnie: path={}, size={}", relativePath, file.getSize());
            return relativePath;
        } catch (IOException ex) {
            log.error("Błąd podczas zapisywania pliku: originalFilename={}", file.getOriginalFilename(), ex);
            throw new RuntimeException("Nie można zapisać pliku: " + file.getOriginalFilename(), ex);
        }
    }
    
    @Override
    public Resource loadFileAsResource(String filename, String subdirectory) {
        log.debug("Pobieranie pliku: filename={}, subdirectory={}", filename, subdirectory);
        try {
            Path filePath = getFileLocation(filename, subdirectory);
            Resource resource = new UrlResource(filePath.toUri());
            
            if (resource.exists()) {
                log.debug("Plik znaleziony: path={}", filePath);
                return resource;
            } else {
                log.warn("Plik nie istnieje: path={}", filePath);
                throw new RuntimeException("Plik nie został znaleziony: " + filename);
            }
        } catch (Exception ex) {
            log.error("Błąd podczas pobierania pliku: filename={}, subdirectory={}", filename, subdirectory, ex);
            throw new RuntimeException("Błąd podczas pobierania pliku: " + filename, ex);
        }
    }
    
    @Override
    public void deleteFile(String filename, String subdirectory) {
        log.info("Usuwanie pliku: filename={}, subdirectory={}", filename, subdirectory);
        try {
            Path filePath = getFileLocation(filename, subdirectory);
            Files.deleteIfExists(filePath);
            log.info("Plik usunięty pomyślnie: path={}", filePath);
        } catch (IOException ex) {
            log.error("Błąd podczas usuwania pliku: filename={}, subdirectory={}", filename, subdirectory, ex);
            throw new RuntimeException("Nie można usunąć pliku: " + filename, ex);
        }
    }
    
    @Override
    public Path getFileLocation(String filename, String subdirectory) {
        if (filename.contains("/")) {
            return this.fileStorageLocation.resolve(filename).normalize();
        }
        return this.fileStorageLocation.resolve(subdirectory).resolve(filename).normalize();
    }
}
