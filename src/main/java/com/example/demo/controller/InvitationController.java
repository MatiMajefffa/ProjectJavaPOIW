package com.example.demo.controller;

import com.example.demo.service.EmailService;
import com.example.demo.service.InvitationService;
import com.example.demo.service.QrCodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.util.Map;

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

    // Generuje link na podstawie stałego kodu wydarzenia
    @GetMapping("/link/{eventId}")
    public ResponseEntity<Map<String, String>> getLink(@PathVariable Long eventId, Principal principal) {
        String code = invitationService.getJoinCode(eventId, principal.getName());
        String link = invitationService.getInvitationLink(code);
        return ResponseEntity.ok(Map.of("link", link));
    }

    // Wysyłka kodu e-mailem
    @PostMapping("/send/{eventId}")
    public ResponseEntity<Map<String, String>> sendInvitation(
            @PathVariable Long eventId,
            @RequestParam String email,
            @RequestParam String eventName,
            Principal principal
    ) {
        String code = invitationService.getJoinCode(eventId, principal.getName());
        String link = invitationService.getInvitationLink(code);

        emailService.sendInvitationEmail(email, link, eventName);
        return ResponseEntity.ok(Map.of("message", "Wysłano zaproszenie do: " + email));
    }

    // QR kod oparty na stałym joinCode
    @GetMapping("/qr/{eventId}")
    public ResponseEntity<byte[]> getQrCode(@PathVariable Long eventId, Principal principal) {
        // code = czysty kod do bazy (np. MAZURY2026)
        String code = invitationService.getJoinCode(eventId, principal.getName());
        // link = pełny URL do zakodowania w obrazku QR
        String link = invitationService.getInvitationLink(code);

        // POPRAWKA: Przekazujemy zarówno czysty kod do walidacji dostępu, jak i pełny link jako treść kodu QR
        byte[] qrBytes = qrCodeService.generateQrCode(code, link, principal.getName());

        return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(qrBytes);
    }
}