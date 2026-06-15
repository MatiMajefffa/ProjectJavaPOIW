package com.example.demo.service;

import com.example.demo.model.Event;
import com.example.demo.model.User;
import com.example.demo.repository.AttendeeRepository;
import com.example.demo.repository.EventRepository;
import com.example.demo.repository.UserRepository;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;

@Service
public class QrCodeService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final AttendeeRepository attendeeRepository;

    public QrCodeService(EventRepository eventRepository, UserRepository userRepository,
                         AttendeeRepository attendeeRepository) {
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
        this.attendeeRepository = attendeeRepository;
    }

    public byte[] generateQrCode(String joinCode, String email) {
        // 1. Znajdź wydarzenie po kodzie (joinCode)
        Event event = eventRepository.findByJoinCode(joinCode)
                .orElseThrow(() -> new RuntimeException("Wydarzenie o tym kodzie nie istnieje"));

        // 2. Znajdź użytkownika
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Użytkownik nie istnieje"));

        // 3. Weryfikacja: czy użytkownik ma dostęp?
        boolean isOrganizer = event.getOrganizer().equals(user);
        boolean isParticipant = attendeeRepository.existsByEventAndUser(event, user);

        if (!isOrganizer && !isParticipant) {
            throw new RuntimeException("Brak uprawnień do wygenerowania kodu QR dla tego wydarzenia");
        }

        // 4. Generowanie kodu QR
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(joinCode, BarcodeFormat.QR_CODE, 250, 250);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", baos);

            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Błąd podczas generowania kodu QR", e);
        }
    }
}