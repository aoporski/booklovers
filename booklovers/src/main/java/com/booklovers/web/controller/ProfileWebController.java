package com.booklovers.web.controller;

import com.booklovers.dto.UserDto;
import com.booklovers.dto.UserStatsDto;
import com.booklovers.service.export.ExportService;
import com.booklovers.service.import_.ImportService;
import com.booklovers.service.stats.StatsService;
import com.booklovers.service.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    public String updateProfile(UserDto userDto, RedirectAttributes redirectAttributes) {
        try {
            userService.updateUser(userDto);
            redirectAttributes.addFlashAttribute("success", "Profil został zaktualizowany pomyślnie!");
            return "redirect:/profile";
        } catch (Exception e) {
            log.error("Error updating profile: ", e);
            redirectAttributes.addFlashAttribute("error", "Wystąpił błąd podczas aktualizacji profilu: " + e.getMessage());
            return "redirect:/profile";
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
                           @RequestParam("format") String format,
                           RedirectAttributes redirectAttributes) {
        try {
            if (file.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Plik jest pusty");
                return "redirect:/profile";
            }
            
            UserDto currentUser = userService.getCurrentUser();
            String content = new String(file.getBytes(), StandardCharsets.UTF_8);
            
            if ("json".equalsIgnoreCase(format)) {
                importService.importUserDataFromJson(currentUser.getId(), content);
            } else if ("csv".equalsIgnoreCase(format)) {
                importService.importUserDataFromCsv(currentUser.getId(), content);
            } else {
                redirectAttributes.addFlashAttribute("error", "Nieobsługiwany format pliku");
                return "redirect:/profile";
            }
            
            redirectAttributes.addFlashAttribute("success", "Dane zostały zaimportowane pomyślnie!");
        } catch (Exception e) {
            log.error("Error importing data: ", e);
            redirectAttributes.addFlashAttribute("error", "Błąd podczas importu danych: " + e.getMessage());
        }
        return "redirect:/profile";
    }
}
