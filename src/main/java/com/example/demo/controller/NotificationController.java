package com.example.demo.controller;

import com.example.demo.service.EmailService;
import com.example.demo.service.NotificationService;
import com.example.demo.model.Notification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

/**
 * Kontroler obsługujący powiadomienia systemowe i e-mailowe.
 * Zapewnia użytkownikom wgląd w historię powiadomień oraz możliwość zarządzania ich stanem.
 */
@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins = "*")
public class NotificationController {

    @Autowired
    private EmailService emailService;

    @Autowired
    private NotificationService notificationService;

    /**
     * POST /api/notifications/send-debt
     * Wysyła powiadomienie e-mailowe o nowym długu do wskazanego użytkownika.
     */
    @PostMapping("/send-debt")
    public ResponseEntity<Map<String, String>> notifyDebt(
            @RequestParam String email,
            @RequestParam String debtorName,
            @RequestParam double amount,
            Principal principal
    ) {
        // Wysyłka notyfikacji e-mailowej o długu
        emailService.sendDebtNotification(email, debtorName, amount);
        return ResponseEntity.ok(Map.of("message", "Powiadomienie o długu (" + amount + " PLN) zostało zakolejkowane do wysyłki."));
    }

    /**
     * GET /api/notifications
     * Pobiera listę wszystkich powiadomień należących do aktualnie zalogowanego użytkownika.
     */
    @GetMapping
    public ResponseEntity<List<Notification>> getAllNotifications(Principal principal) {
        return ResponseEntity.ok(notificationService.findAllByUser(principal.getName()));
    }

    /**
     * PUT /api/notifications/mark-read/{id}
     * Oznacza konkretne powiadomienie jako przeczytane.
     * Wewnątrz serwisu następuje weryfikacja, czy użytkownik jest właścicielem tego powiadomienia.
     */
    @PutMapping("/mark-read/{id}")
    public ResponseEntity<Map<String, String>> markAsRead(@PathVariable Long id, Principal principal) {
        notificationService.markAsRead(id, principal.getName());
        return ResponseEntity.ok(Map.of("message", "Powiadomienie oznaczone jako przeczytane"));
    }

    /**
     * PUT /api/notifications/mark-all-read
     * Oznacza wszystkie powiadomienia danego użytkownika jako przeczytane.
     */
    @PutMapping("/mark-all-read")
    public ResponseEntity<Map<String, String>> markAllAsRead(Principal principal) {
        notificationService.markAllAsRead(principal.getName());
        return ResponseEntity.ok(Map.of("message", "Wszystkie powiadomienia oznaczone jako przeczytane"));
    }

    /**
     * DELETE /api/notifications/{id}
     * Usuwa wybrane powiadomienie z bazy danych po uprzedniej weryfikacji uprawnień.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteNotification(@PathVariable Long id, Principal principal) {
        notificationService.deleteNotification(id, principal.getName());
        return ResponseEntity.ok(Map.of("message", "Powiadomienie usunięte."));
    }
}