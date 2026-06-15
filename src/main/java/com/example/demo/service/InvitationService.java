package com.example.demo.service;

import com.example.demo.model.Event;
import com.example.demo.model.Invitation;
import com.example.demo.model.User;
import com.example.demo.repository.EventRepository;
import com.example.demo.repository.InvitationRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class InvitationService {

    private static final String BASE_URL = "https://eventsplit.app/join/";

    @Autowired
    private InvitationRepository invitationRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private UserRepository userRepository;

    public Invitation generateInvitation(Long eventId, String email) {
        // 1. Sprawdź czy użytkownik ma dostęp do tego eventu
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Użytkownik nie istnieje"));

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Wydarzenie nie istnieje"));

        // Zakładamy, że tylko organizator może generować zaproszenia
        if (!event.getOrganizer().equals(user)) {
            throw new RuntimeException("Brak uprawnień do generowania zaproszeń");
        }

        // 2. Generuj token
        String token = UUID.randomUUID().toString();

        // 3. Stwórz zaproszenie
        Invitation invitation = new Invitation();
        invitation.setEventId(eventId);
        invitation.setToken(token);
        invitation.setExpiresAt(LocalDateTime.now().plusDays(7));

        // 4. Zapisz w bazie
        return invitationRepository.save(invitation);
    }

    public String getInvitationLink(Invitation invitation) {
        return BASE_URL + invitation.getToken();
    }

    public Invitation validateToken(String token) {
        Invitation invitation = invitationRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Nieprawidłowy token"));

        if (invitation.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Link wygasł");
        }

        if (invitation.isUsed()) {
            throw new RuntimeException("Link został już wykorzystany");
        }

        return invitation;
    }
}