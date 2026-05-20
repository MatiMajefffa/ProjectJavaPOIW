package com.example.demo.controller;

import com.example.demo.dto.ChangePasswordRequest;
import com.example.demo.dto.LoginRequest;
import com.example.demo.dto.ProfileUpdateRequest;
import com.example.demo.model.User;
import com.example.demo.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/auth") // Główny adres dla operacji autoryzacji i konta
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * To wywoła Flutter/React, gdy użytkownik wpisze dane w oknie logowania
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            // AuthService sprawdzi bazę w Dockerze i wygeneruje JWT
            String token = authService.login(loginRequest);
            return ResponseEntity.ok(token);
        } catch (Exception e) {
            // Jeśli hasło złe lub brak usera - wysyłamy błąd 401 (Unauthorized)
            return ResponseEntity.status(401).body("Błąd logowania: " + e.getMessage());
        }
    }

    /**
     * To wywoła frontend, gdy użytkownik wypełni formularz "Zarejestruj się"
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        try {
            User registeredUser = authService.register(user);
            return ResponseEntity.ok("Użytkownik zarejestrowany pomyślnie: " + registeredUser.getEmail());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Błąd rejestracji: " + e.getMessage());
        }
    }

    /**
     * 1. DO ZAPISYWANIA NAZWY, KTÓRĄ UŻYTKOWNIK NADA
     * To wywoła Flutter, przesyłając nową nazwę w body JSON-a
     */
    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@RequestBody ProfileUpdateRequest request, Principal principal) {
        try {
            // principal.getName() wyciąga adres e-mail użytkownika z tokenu JWT automatycznie
            authService.updateProfile(principal.getName(), request);
            return ResponseEntity.ok("Nazwa profilu została zaktualizowana pomyślnie.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Błąd aktualizacji profilu: " + e.getMessage());
        }
    }

    /**
     * 2. ZMIANA HASŁA
     * To wywoła Flutter, podając stare i nowe hasło
     */
    @PutMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody ChangePasswordRequest request, Principal principal) {
        try {
            authService.changePassword(principal.getName(), request);
            return ResponseEntity.ok("Hasło zostało pomyślnie zmienione.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Błąd zmiany hasła: " + e.getMessage());
        }
    }

    /**
     * 3. USUNIĘCIE KONTA
     * To wywoła Flutter, gdy użytkownik zechce całkowicie skasować konto
     */
    @DeleteMapping("/user")
    public ResponseEntity<?> deleteUser(Principal principal) {
        try {
            authService.deleteUser(principal.getName());
            return ResponseEntity.ok("Konto użytkownika zostało usunięte.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Błąd podczas usuwania konta: " + e.getMessage());
        }
    }
}