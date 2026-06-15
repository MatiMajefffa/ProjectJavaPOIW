package com.example.demo.controller;

import com.example.demo.dto.InvitationResponse;
import com.example.demo.model.Invitation;
import com.example.demo.service.EmailService;
import com.example.demo.service.InvitationService;
import com.example.demo.service.QrCodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.util.Map;

/**
 * Kontroler zarządzający procesem zapraszania użytkowników do wydarzeń.
 * Odpowiada za generowanie linków zaproszeń, wysyłkę e-mailową oraz kody QR.
 */
@RestController
@RequestMapping("/api/invitations")
@CrossOrigin(origins = "*")
public class InvitationController {

    @Autowired
    private InvitationService invitationService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private QrCodeService qrCodeService;

    /**
     * POST /api/invitations/generate/{eventId}
     * Generuje unikalny token zaproszenia dla wydarzenia.
     * Weryfikuje, czy osoba wywołująca jest organizatorem wydarzenia.
     */
    @PostMapping("/generate/{eventId}")
    public ResponseEntity<InvitationResponse> generateInvitation(@PathVariable Long eventId, Principal principal) {
        Invitation invitation = invitationService.generateInvitation(eventId, principal.getName());
        String link = invitationService.getInvitationLink(invitation);

        InvitationResponse response = new InvitationResponse(link, invitation.getExpiresAt());
        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/invitations/send/{eventId}
     * Generuje zaproszenie i natychmiast wysyła je e-mailem do wskazanej osoby.
     */
    @PostMapping("/send/{eventId}")
    public ResponseEntity<Map<String, String>> sendInvitation(
            @PathVariable Long eventId,
            @RequestParam String email,
            @RequestParam String eventName,
            Principal principal
    ) {
        // Generujemy zaproszenie i pobieramy link do wysyłki
        Invitation invitation = invitationService.generateInvitation(eventId, principal.getName());
        String link = invitationService.getInvitationLink(invitation);

        // Wysyłka asynchroniczna e-maila
        emailService.sendInvitationEmail(email, link, eventName);

        return ResponseEntity.ok(Map.of("message", "Wysłano zaproszenie do: " + email));
    }

    /**
     * GET /api/invitations/validate/{token}
     * Sprawdza poprawność tokenu zaproszenia (np. czy nie wygasł).
     * Dostęp publiczny – użytkownik może jeszcze nie być zalogowany.
     */
    @GetMapping("/validate/{token}")
    public ResponseEntity<Map<String, String>> validateInvitation(@PathVariable String token) {
        try {
            Invitation invitation = invitationService.validateToken(token);
            return ResponseEntity.ok(Map.of(
                    "status", "valid",
                    "eventId", String.valueOf(invitation.getEventId())
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * GET /api/invitations/qr/{token}
     * Generuje obraz kodu QR (PNG), który po zeskanowaniu przenosi do wydarzenia.
     */
    @GetMapping("/qr/{token}")
    public ResponseEntity<byte[]> getQrCode(@PathVariable String token, Principal principal) {
        String link = "https://apka.pl/join/" + token;
        // Generowanie QR powiązane z użytkownikiem (np. do celów logowania lub śledzenia)
        byte[] qrBytes = qrCodeService.generateQrCode(link, principal.getName());

        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .body(qrBytes);
    }
}