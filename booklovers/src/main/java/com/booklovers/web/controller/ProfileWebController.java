package com.booklovers.web.controller;

import com.booklovers.dto.UserDto;
import com.booklovers.dto.UserStatsDto;
import com.booklovers.service.export.ExportService;
import com.booklovers.service.file.FileStorageService;
import com.booklovers.service.import_.ImportService;
import com.booklovers.service.stats.StatsService;
import com.booklovers.service.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Slf4j
@Controller
@RequestMapping("/profile")
@RequiredArgsConstructor
public class ProfileWebController {
    
    private final UserService userService;
    private final ExportService exportService;
    private final ImportService importService;
    private final StatsService statsService;
    private final FileStorageService fileStorageService;
    
    @GetMapping
    public String profilePage(Model model) {
        try {
            UserDto user = userService.getCurrentUser();
            UserStatsDto userStats = statsService.getUserStats(user.getId());
            model.addAttribute("user", user);
            model.addAttribute("userDto", user);
            model.addAttribute("userStats", userStats);
            return "profile";
        } catch (Exception e) {
            return "redirect:/login";
        }
    }
    
    @PostMapping
    public String updateProfile(
            @ModelAttribute UserDto userDto,
            @RequestParam(value = "avatarFile", required = false) MultipartFile avatarFile,
            RedirectAttributes redirectAttributes) {
        try {
            log.info("Aktualizacja profilu użytkownika");
            
            UserDto currentUser = userService.getCurrentUser();
            
            if (avatarFile != null && !avatarFile.isEmpty()) {
                log.info("Przesyłanie zdjęcia profilowego: originalFilename={}, size={}", 
                        avatarFile.getOriginalFilename(), avatarFile.getSize());
                
                if (currentUser.getAvatarUrl() != null && !currentUser.getAvatarUrl().isEmpty() 
                        && !currentUser.getAvatarUrl().startsWith("http")) {
                    try {
                        String oldPath = currentUser.getAvatarUrl();
                        if (oldPath.contains("/")) {
                            String[] parts = oldPath.split("/");
                            if (parts.length >= 2) {
                                fileStorageService.deleteFile(parts[parts.length - 1], parts[parts.length - 2]);
                                log.debug("Stare zdjęcie profilowe usunięte: path={}", oldPath);
                            }
                        }
                    } catch (Exception e) {
                        log.warn("Nie można usunąć starego zdjęcia profilowego", e);
                    }
                }
                
                String filePath = fileStorageService.storeFile(avatarFile, "avatars");
                userDto.setAvatarUrl(filePath);
                log.info("Zdjęcie profilowe zapisane: path={}", filePath);
            } else {
                userDto.setAvatarUrl(currentUser.getAvatarUrl());
            }
            
            userService.updateUser(userDto);
            redirectAttributes.addFlashAttribute("success", "Profil został zaktualizowany pomyślnie!");
            return "redirect:/profile";
        } catch (Exception e) {
            log.error("Błąd podczas aktualizacji profilu", e);
            redirectAttributes.addFlashAttribute("error", "Wystąpił błąd podczas aktualizacji profilu: " + e.getMessage());
            return "redirect:/profile";
        }
    }
    
    @GetMapping("/avatar")
    public ResponseEntity<Resource> getAvatar() {
        try {
            UserDto currentUser = userService.getCurrentUser();
            String avatarUrl = currentUser.getAvatarUrl();
            
            if (avatarUrl == null || avatarUrl.isEmpty()) {
                log.debug("Użytkownik nie ma zdjęcia profilowego");
                return ResponseEntity.notFound().build();
            }
            
            if (avatarUrl.startsWith("http://") || avatarUrl.startsWith("https://")) {
                return ResponseEntity.status(302)
                        .header(HttpHeaders.LOCATION, avatarUrl)
                        .build();
            }
            
            String[] parts = avatarUrl.split("/");
            if (parts.length < 2) {
                log.warn("Nieprawidłowa ścieżka do zdjęcia profilowego: {}", avatarUrl);
                return ResponseEntity.notFound().build();
            }
            
            String filename = parts[parts.length - 1];
            String subdirectory = parts[parts.length - 2];
            
            Resource resource = fileStorageService.loadFileAsResource(filename, subdirectory);
            String contentType = "image/jpeg"; // Domyślnie JPEG, można rozszerzyć
            
            if (filename.toLowerCase().endsWith(".png")) {
                contentType = "image/png";
            } else if (filename.toLowerCase().endsWith(".gif")) {
                contentType = "image/gif";
            }
            
            log.debug("Pobieranie zdjęcia profilowego: filename={}, subdirectory={}", filename, subdirectory);
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                    .body(resource);
        } catch (Exception e) {
            log.error("Błąd podczas pobierania zdjęcia profilowego", e);
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/avatar/{userId}")
    public ResponseEntity<Resource> getUserAvatar(@PathVariable Long userId) {
        try {
            UserDto user = userService.findByIdDto(userId)
                    .orElseThrow(() -> new com.booklovers.exception.ResourceNotFoundException("User", userId));
            
            String avatarUrl = user.getAvatarUrl();
            if (avatarUrl == null || avatarUrl.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            if (avatarUrl.startsWith("http://") || avatarUrl.startsWith("https://")) {
                return ResponseEntity.status(302)
                        .header(HttpHeaders.LOCATION, avatarUrl)
                        .build();
            }
            
            String[] parts = avatarUrl.split("/");
            if (parts.length < 2) {
                return ResponseEntity.notFound().build();
            }
            
            String filename = parts[parts.length - 1];
            String subdirectory = parts[parts.length - 2];
            
            Resource resource = fileStorageService.loadFileAsResource(filename, subdirectory);
            String contentType = "image/jpeg";
            
            if (filename.toLowerCase().endsWith(".png")) {
                contentType = "image/png";
            } else if (filename.toLowerCase().endsWith(".gif")) {
                contentType = "image/gif";
            }
            
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                    .body(resource);
        } catch (Exception e) {
            log.error("Błąd podczas pobierania zdjęcia profilowego użytkownika: userId={}", userId, e);
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/export/json")
    public ResponseEntity<byte[]> exportJson() {
        try {
            UserDto currentUser = userService.getCurrentUser();
            String json = exportService.exportUserDataAsJson(currentUser.getId());
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setContentDispositionFormData("attachment", "user-data.json");
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(json.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            log.error("Error exporting JSON: ", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/export/csv")
    public ResponseEntity<byte[]> exportCsv() {
        try {
            UserDto currentUser = userService.getCurrentUser();
            String csv = exportService.exportUserDataAsCsv(currentUser.getId());
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.TEXT_PLAIN);
            headers.setContentDispositionFormData("attachment", "user-data.csv");
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(csv.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            log.error("Error exporting CSV: ", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @PostMapping("/import")
    public String importData(@RequestParam("file") MultipartFile file,
                           RedirectAttributes redirectAttributes) {
        try {
            if (file.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Plik jest pusty");
                return "redirect:/profile";
            }
            
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || originalFilename.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Nie można określić nazwy pliku");
                return "redirect:/profile";
            }
            
            String extension = getFileExtension(originalFilename);
            if (extension == null) {
                redirectAttributes.addFlashAttribute("error", "Nie można określić formatu pliku. Użyj pliku .json lub .csv");
                return "redirect:/profile";
            }
            
            UserDto currentUser = userService.getCurrentUser();
            String content = new String(file.getBytes(), StandardCharsets.UTF_8);
            
            log.info("Import danych użytkownika: userId={}, format={}, filename={}", 
                    currentUser.getId(), extension, originalFilename);
            
            if ("json".equalsIgnoreCase(extension)) {
                importService.importUserDataFromJson(currentUser.getId(), content);
            } else if ("csv".equalsIgnoreCase(extension)) {
                importService.importUserDataFromCsv(currentUser.getId(), content);
            } else {
                redirectAttributes.addFlashAttribute("error", "Nieobsługiwany format pliku. Obsługiwane formaty: .json, .csv");
                return "redirect:/profile";
            }
            
            redirectAttributes.addFlashAttribute("success", "Dane zostały zaimportowane pomyślnie!");
        } catch (Exception e) {
            log.error("Błąd podczas importu danych", e);
            redirectAttributes.addFlashAttribute("error", "Błąd podczas importu danych: " + e.getMessage());
        }
        return "redirect:/profile";
    }
    
    @PostMapping("/delete")
    public String deleteAccount(RedirectAttributes redirectAttributes) {
        try {
            log.info("Usuwanie konta użytkownika");
            UserDto currentUser = userService.getCurrentUser();
            
            if (currentUser.getAvatarUrl() != null && !currentUser.getAvatarUrl().isEmpty() 
                    && !currentUser.getAvatarUrl().startsWith("http")) {
                try {
                    String oldPath = currentUser.getAvatarUrl();
                    if (oldPath.contains("/")) {
                        String[] parts = oldPath.split("/");
                        if (parts.length >= 2) {
                            fileStorageService.deleteFile(parts[parts.length - 1], parts[parts.length - 2]);
                            log.debug("Zdjęcie profilowe usunięte podczas usuwania konta: path={}", oldPath);
                        }
                    }
                } catch (Exception e) {
                    log.warn("Nie można usunąć zdjęcia profilowego podczas usuwania konta", e);
                }
            }
            
            userService.deleteCurrentUser();
            redirectAttributes.addFlashAttribute("success", "Twoje konto zostało usunięte pomyślnie!");
            log.info("Konto użytkownika usunięte pomyślnie");
            return "redirect:/login?accountDeleted=true";
        } catch (Exception e) {
            log.error("Błąd podczas usuwania konta", e);
            redirectAttributes.addFlashAttribute("error", "Wystąpił błąd podczas usuwania konta: " + e.getMessage());
            return "redirect:/profile";
        }
    }
    
    private String getFileExtension(String filename) {
        if (filename == null || filename.isEmpty()) {
            return null;
        }
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1 || lastDotIndex == filename.length() - 1) {
            return null;
        }
        return filename.substring(lastDotIndex + 1).toLowerCase();
    }
}
