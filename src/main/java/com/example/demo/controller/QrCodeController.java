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
        // Generujemy kod QR przekazując tekst i e-mail użytkownika w celu weryfikacji dostępu
        byte[] qrImage = qrCodeService.generateQrCode(text, principal.getName());

        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .body(qrImage);
    }
}