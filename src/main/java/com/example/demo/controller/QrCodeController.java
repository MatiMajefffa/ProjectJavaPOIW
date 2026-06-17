package com.example.demo.controller;

import com.example.demo.service.QrCodeService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;

/**
 * Kontroler odpowiedzialny za generowanie kodów QR.
 * Umożliwia łatwe udostępnianie linków (np. do dołączenia do wydarzenia) w postaci graficznej.
 */
@RestController
@RequestMapping("/api/qr")
@CrossOrigin(origins = "*")
public class QrCodeController {

    private final QrCodeService qrCodeService;

    public QrCodeController(QrCodeService qrCodeService) {
        this.qrCodeService = qrCodeService;
    }

    /**
     * GET /api/qr/{text}
     * Generuje obraz kodu QR na podstawie podanego tekstu (np. URL wydarzenia).
     */
    @GetMapping("/{text}")
    public ResponseEntity<byte[]> getQrCode(@PathVariable String text, Principal principal) {
        // POPRAWKA: Przekazujemy zmienną 'text' jako joinCode oraz jako textToEncode,
        // a także login użytkownika w celu weryfikacji uprawnień.
        byte[] qrImage = qrCodeService.generateQrCode(text, text, principal.getName());

        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .body(qrImage);
    }
}