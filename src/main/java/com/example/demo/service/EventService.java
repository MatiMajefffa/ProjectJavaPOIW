package com.example.demo.service;

import com.example.demo.dto.EventReportDTO;
import com.example.demo.model.*;
import com.example.demo.repository.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class EventService {
    private final ExpenseRepository expenseRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final AttendeeRepository attendeeRepository;

    public EventService(EventRepository eventRepository, UserRepository userRepository,
                        ExpenseRepository expenseRepository, AttendeeRepository attendeeRepository) {
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
        this.expenseRepository = expenseRepository;
        this.attendeeRepository = attendeeRepository;
    }

    // --- ZAKTUALIZOWANE METODY Z WERYFIKACJĄ ---

    public void closeEvent(Long eventId, String email) {
        Event event = findById(eventId);
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("Użytkownik nie istnieje"));

        if (!event.getOrganizer().equals(user)) {
            throw new RuntimeException("Tylko organizator może zamknąć wydarzenie.");
        }
        event.setClosed(true);
        eventRepository.save(event);
    }

    public void deleteEvent(Long eventId, String email) {
        Event event = findById(eventId);
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("Użytkownik nie istnieje"));

        if (!event.getOrganizer().equals(user)) {
            throw new RuntimeException("Tylko organizator może usunąć wydarzenie.");
        }
        eventRepository.deleteById(eventId);
    }

    public void joinEvent(String joinCode, String email) {
        Event event = eventRepository.findByJoinCode(joinCode)
                .orElseThrow(() -> new RuntimeException("Błędny kod wydarzenia"));
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("Użytkownik nie istnieje"));

        // Logika dołączania do wydarzenia (np. zapis do AttendeeRepository)
        // Jeśli użytkownik już jest uczestnikiem, nie dodawaj ponownie
        if (!attendeeRepository.existsByEventAndUser(event, user)) {
            Attendee attendee = new Attendee(event, user);
            attendeeRepository.save(attendee);
        }
    }

    // --- METODY POMOCNICZE I ISTNIEJĄCE ---

    public void validateEventIsActive(Long eventId) {
        Event event = findById(eventId);
        if (event.isClosed()) {
            throw new RuntimeException("Wydarzenie jest zamknięte. Nie można dodawać nowych wydatków.");
        }
    }

    public List<Event> findAllByUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Nie znaleziono użytkownika"));
        return eventRepository.findByOrganizer(user);
    }

    public Event createEvent(Event event, String email) {
        User organizer = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Nie znaleziono użytkownika"));
        event.setOrganizer(organizer);
        return eventRepository.save(event);
    }

    public Event findById(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Wydarzenie nie istnieje"));
    }

    public Event getEventWithPermission(Long eventId, String email) {
        Event event = findById(eventId);
        User user = userRepository.findByEmail(email).orElseThrow();

        boolean isOrganizer = event.getOrganizer().equals(user);
        boolean isParticipant = attendeeRepository.existsByEventAndUser(event, user);

        if (!isOrganizer && !isParticipant) {
            throw new RuntimeException("Brak dostępu do tego wydarzenia");
        }
        return event;
    }

    public EventReportDTO generateReport(Long eventId, String email) {
        // ... (Twoja logika z poprzedniej wiadomości pozostaje bez zmian)
        // Pamiętaj, aby jej tutaj nie usuwać!
        return null; // (Tu wstaw swój oryginalny kod metody)
    }
}