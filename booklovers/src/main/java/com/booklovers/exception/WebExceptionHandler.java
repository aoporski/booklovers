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
    public String handleConflictException(ConflictException ex, RedirectAttributes redirectAttributes) {
        log.error("Conflict: {}", ex.getMessage());
        redirectAttributes.addFlashAttribute("error", ex.getMessage());
        return "redirect:/books";
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
