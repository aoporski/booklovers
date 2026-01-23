package com.booklovers.api.controller;

import com.booklovers.dto.LoginRequest;
import com.booklovers.dto.RegisterRequest;
import com.booklovers.dto.UserDto;
import com.booklovers.service.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "API do rejestracji i logowania użytkowników")
public class AuthController {
    
    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    
    @Operation(summary = "Rejestracja nowego użytkownika", description = "Tworzy nowe konto użytkownika w systemie. Endpoint dostępny publicznie - nie wymaga autoryzacji. Po rejestracji użytkownik może się zalogować używając endpointu /api/auth/login.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Użytkownik został pomyślnie zarejestrowany"),
            @ApiResponse(responseCode = "400", description = "Nieprawidłowe dane wejściowe (np. użytkownik już istnieje, nieprawidłowy format email)")
    })
    @PostMapping("/register")
    public ResponseEntity<UserDto> register(@Valid @RequestBody RegisterRequest request) {
        log.info("Próba rejestracji użytkownika: username={}, email={}", request.getUsername(), request.getEmail());
        try {
            UserDto user = userService.register(request);
            log.info("Rejestracja zakończona sukcesem: userId={}, username={}", user.getId(), user.getUsername());
            return ResponseEntity.status(HttpStatus.CREATED).body(user);
        } catch (Exception e) {
            log.error("Błąd podczas rejestracji użytkownika: username={}, error={}", request.getUsername(), e.getMessage(), e);
            throw e;
        }
    }
    
    @Operation(summary = "Logowanie użytkownika", description = "Uwierzytelnia użytkownika w systemie. Po pomyślnym logowaniu tworzona jest sesja użytkownika, która jest używana do autoryzacji w kolejnych żądaniach. Endpoint dostępny publicznie - nie wymaga autoryzacji.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Logowanie zakończone sukcesem - użytkownik jest zalogowany"),
            @ApiResponse(responseCode = "401", description = "Nieprawidłowe dane logowania (błędna nazwa użytkownika lub hasło)")
    })
    @PostMapping("/login")
    public ResponseEntity<String> login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        log.info("Próba logowania użytkownika: username={}", request.getUsername());
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
            
            SecurityContext securityContext = SecurityContextHolder.getContext();
            securityContext.setAuthentication(authentication);
            
            // Zapisz SecurityContext do sesji HTTP, żeby była dostępna w kolejnych żądaniach
            HttpSession session = httpRequest.getSession(true);
            session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, securityContext);
            
            log.info("Logowanie zakończone sukcesem: username={}, sessionId={}", request.getUsername(), session.getId());
            return ResponseEntity.ok("Login successful");
        } catch (BadCredentialsException e) {
            log.warn("Nieprawidłowe dane logowania: username={}", request.getUsername());
            throw e;
        } catch (Exception e) {
            log.error("Błąd podczas logowania: username={}, error={}", request.getUsername(), e.getMessage(), e);
            throw e;
        }
    }
}
