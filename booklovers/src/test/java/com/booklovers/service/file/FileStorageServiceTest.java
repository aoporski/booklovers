package com.booklovers.service.file;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class FileStorageServiceTest {

    @TempDir
    Path tempDir;

    private FileStorageServiceImp fileStorageService;

    @BeforeEach
    void setUp() {
        fileStorageService = new FileStorageServiceImp(tempDir.toString());
    }

    @Test
    void testStoreFile_Success() throws IOException {
        MultipartFile file = new MockMultipartFile(
                "file", "test.jpg", "image/jpeg", "test image content".getBytes());

        String result = fileStorageService.storeFile(file, "avatars");

        assertNotNull(result);
        assertTrue(result.startsWith("avatars/"));
        assertTrue(result.endsWith(".jpg"));
        
        Path storedFile = tempDir.resolve(result);
        assertTrue(Files.exists(storedFile));
        assertEquals("test image content", new String(Files.readAllBytes(storedFile)));
    }

    @Test
    void testStoreFile_CreatesSubdirectory() throws IOException {
        MultipartFile file = new MockMultipartFile(
                "file", "test.png", "image/png", "test content".getBytes());

        String result = fileStorageService.storeFile(file, "test-subdir");

        assertNotNull(result);
        assertTrue(result.startsWith("test-subdir/"));
        
        Path subdirectory = tempDir.resolve("test-subdir");
        assertTrue(Files.exists(subdirectory));
        assertTrue(Files.isDirectory(subdirectory));
    }

    @Test
    void testStoreFile_EmptyFile() {
        MultipartFile file = new MockMultipartFile(
                "file", "empty.jpg", "image/jpeg", new byte[0]);

        assertThrows(RuntimeException.class, () -> {
            fileStorageService.storeFile(file, "avatars");
        });
    }

    @Test
    void testStoreFile_InvalidContentType() {
        MultipartFile file = new MockMultipartFile(
                "file", "test.txt", "text/plain", "some text".getBytes());

        assertThrows(RuntimeException.class, () -> {
            fileStorageService.storeFile(file, "avatars");
        });
    }

    @Test
    void testStoreFile_NullContentType() {
        MultipartFile file = new MockMultipartFile(
                "file", "test.jpg", null, "test content".getBytes());

        assertThrows(RuntimeException.class, () -> {
            fileStorageService.storeFile(file, "avatars");
        });
    }

    @Test
    void testStoreFile_GeneratesUniqueFilename() throws IOException {
        MultipartFile file1 = new MockMultipartFile(
                "file", "test.jpg", "image/jpeg", "content1".getBytes());
        MultipartFile file2 = new MockMultipartFile(
                "file", "test.jpg", "image/jpeg", "content2".getBytes());

        String result1 = fileStorageService.storeFile(file1, "avatars");
        String result2 = fileStorageService.storeFile(file2, "avatars");

        assertNotEquals(result1, result2);
        assertTrue(result1.startsWith("avatars/"));
        assertTrue(result2.startsWith("avatars/"));
    }

    @Test
    void testStoreFile_PreservesFileExtension() throws IOException {
        MultipartFile jpgFile = new MockMultipartFile(
                "file", "image.jpg", "image/jpeg", "content".getBytes());
        MultipartFile pngFile = new MockMultipartFile(
                "file", "image.png", "image/png", "content".getBytes());
        MultipartFile gifFile = new MockMultipartFile(
                "file", "image.gif", "image/gif", "content".getBytes());

        String jpgResult = fileStorageService.storeFile(jpgFile, "avatars");
        String pngResult = fileStorageService.storeFile(pngFile, "avatars");
        String gifResult = fileStorageService.storeFile(gifFile, "avatars");

        assertTrue(jpgResult.endsWith(".jpg"));
        assertTrue(pngResult.endsWith(".png"));
        assertTrue(gifResult.endsWith(".gif"));
    }

    @Test
    void testLoadFileAsResource_Success() throws IOException {
        MultipartFile file = new MockMultipartFile(
                "file", "test.jpg", "image/jpeg", "test content".getBytes());
        
        String storedPath = fileStorageService.storeFile(file, "avatars");
        String[] parts = storedPath.split("/");
        String filename = parts[parts.length - 1];
        String subdirectory = parts[parts.length - 2];

        Resource resource = fileStorageService.loadFileAsResource(filename, subdirectory);

        assertNotNull(resource);
        assertTrue(resource.exists());
        assertEquals("test content", new String(resource.getInputStream().readAllBytes()));
    }

    @Test
    void testLoadFileAsResource_NotFound() {
        assertThrows(RuntimeException.class, () -> {
            fileStorageService.loadFileAsResource("nonexistent.jpg", "avatars");
        });
    }

    @Test
    void testDeleteFile_Success() throws IOException {
        MultipartFile file = new MockMultipartFile(
                "file", "test.jpg", "image/jpeg", "test content".getBytes());
        
        String storedPath = fileStorageService.storeFile(file, "avatars");
        String[] parts = storedPath.split("/");
        String filename = parts[parts.length - 1];
        String subdirectory = parts[parts.length - 2];

        Path filePath = tempDir.resolve(storedPath);
        assertTrue(Files.exists(filePath));

        fileStorageService.deleteFile(filename, subdirectory);

        assertFalse(Files.exists(filePath));
    }

    @Test
    void testDeleteFile_NotFound() {
        assertDoesNotThrow(() -> {
            fileStorageService.deleteFile("nonexistent.jpg", "avatars");
        });
    }

    @Test
    void testGetFileLocation_WithSubdirectory() {
        Path location = fileStorageService.getFileLocation("test.jpg", "avatars");
        
        assertNotNull(location);
        assertTrue(location.toString().contains("avatars"));
        assertTrue(location.toString().contains("test.jpg"));
    }

    @Test
    void testGetFileLocation_WithPathInFilename() {
        Path location = fileStorageService.getFileLocation("avatars/test.jpg", "avatars");
        
        assertNotNull(location);
        assertTrue(location.toString().contains("avatars"));
        assertTrue(location.toString().contains("test.jpg"));
    }

    @Test
    void testStoreFile_ReplacesExistingFile() throws IOException {
        MultipartFile file1 = new MockMultipartFile(
                "file", "test.jpg", "image/jpeg", "content1".getBytes());
        MultipartFile file2 = new MockMultipartFile(
                "file", "test.jpg", "image/jpeg", "content2".getBytes());

        String path1 = fileStorageService.storeFile(file1, "avatars");
        String path2 = fileStorageService.storeFile(file2, "avatars");

        Path filePath1 = tempDir.resolve(path1);
        Path filePath2 = tempDir.resolve(path2);

        if (path1.equals(path2)) {
            assertEquals("content2", new String(Files.readAllBytes(filePath2)));
        } else {
            assertTrue(Files.exists(filePath1));
            assertTrue(Files.exists(filePath2));
        }
    }
}
