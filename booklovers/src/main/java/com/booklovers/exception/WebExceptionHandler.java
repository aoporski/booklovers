package com.booklovers.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Slf4j
@ControllerAdvice(basePackages = "com.booklovers.web.controller")
public class WebExceptionHandler {
    
    @ExceptionHandler(ResourceNotFoundException.class)
    public String handleResourceNotFoundException(ResourceNotFoundException ex, RedirectAttributes redirectAttributes) {
        log.error("Resource not found: {}", ex.getMessage());
        redirectAttributes.addFlashAttribute("error", ex.getMessage());
        return "redirect:/books";
    }
    
    @ExceptionHandler(BadRequestException.class)
    public String handleBadRequestException(BadRequestException ex, RedirectAttributes redirectAttributes) {
        log.error("Bad request: {}", ex.getMessage());
        redirectAttributes.addFlashAttribute("error", ex.getMessage());
        return "redirect:/books";
    }
    
    @ExceptionHandler(ConflictException.class)
    public String handleConflictException(ConflictException ex, RedirectAttributes redirectAttributes, org.springframework.web.context.request.WebRequest request) {
        log.error("Conflict: {}", ex.getMessage());
        String requestUri = request.getDescription(false).replace("uri=", "");
        // Jeśli błąd wystąpił na stronie szczegółów książki, przekieruj tam
        if (requestUri.contains("/books/") && requestUri.contains("/reviews")) {
            String bookId = extractBookIdFromUri(requestUri);
            if (bookId != null) {
                redirectAttributes.addFlashAttribute("error", "Już dodałeś recenzję do tej książki. Możesz edytować istniejącą recenzję.");
                return "redirect:/books/" + bookId;
            }
        }
        redirectAttributes.addFlashAttribute("error", ex.getMessage());
        return "redirect:/books";
    }
    
    private String extractBookIdFromUri(String uri) {
        try {
            // Format: /books/{id}/reviews
            String[] parts = uri.split("/");
            for (int i = 0; i < parts.length; i++) {
                if (parts[i].equals("books") && i + 1 < parts.length) {
                    return parts[i + 1];
                }
            }
        } catch (Exception e) {
            log.warn("Could not extract book ID from URI: {}", uri);
        }
        return null;
    }
    
    @ExceptionHandler(ForbiddenException.class)
    public String handleForbiddenException(ForbiddenException ex, RedirectAttributes redirectAttributes) {
        log.error("Forbidden: {}", ex.getMessage());
        redirectAttributes.addFlashAttribute("error", ex.getMessage());
        return "redirect:/books";
    }
    
    @ExceptionHandler(UnauthorizedException.class)
    public String handleUnauthorizedException(UnauthorizedException ex) {
        log.error("Unauthorized: {}", ex.getMessage());
        return "redirect:/login";
    }
    
    @ExceptionHandler(IllegalArgumentException.class)
    public String handleIllegalArgumentException(IllegalArgumentException ex, RedirectAttributes redirectAttributes) {
        log.error("Illegal argument: {}", ex.getMessage());
        redirectAttributes.addFlashAttribute("error", ex.getMessage());
        return "redirect:/books";
    }
    
    @ExceptionHandler(Exception.class)
    public String handleGenericException(Exception ex, RedirectAttributes redirectAttributes) {
        log.error("Unexpected error: ", ex);
        redirectAttributes.addFlashAttribute("error", "Wystąpił nieoczekiwany błąd. Spróbuj ponownie później.");
        return "redirect:/books";
    }
}
