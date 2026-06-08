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


    public void closeEvent(Long eventId) {
        Event event = findById(eventId);
        event.setClosed(true);
        eventRepository.save(event);
    }


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

    public void deleteEvent(Long eventId) {
        eventRepository.deleteById(eventId);
    }

    public void joinEvent(String joinCode, String email) {
        eventRepository.findByJoinCode(joinCode)
                .orElseThrow(() -> new RuntimeException("Błędny kod wydarzenia"));
    }

    public EventReportDTO generateReport(Long eventId) {
        Event event = findById(eventId);
        List<Expense> expenses = expenseRepository.findByEventId(eventId);
        List<Attendee> attendees = attendeeRepository.findByEvent(event);
        int participantsCount = attendees.size();

        double totalCost = expenses.stream().mapToDouble(Expense::getAmount).sum();
        double averagePerPerson = (participantsCount > 0) ? (totalCost / participantsCount) : 0;

        Map<User, Double> balances = new HashMap<>();

        for (Attendee a : attendees) {
            balances.put(a.getUser(), 0.0);
        }

        for (Expense e : expenses) {
            User payer = e.getPayer();
            balances.put(payer, balances.getOrDefault(payer, 0.0) + e.getAmount());
        }

        balances.replaceAll((user, totalSpent) -> totalSpent - averagePerPerson);

        List<Transaction> transactions = new ArrayList<>();
        List<Map.Entry<User, Double>> debtors = new ArrayList<>();
        List<Map.Entry<User, Double>> creditors = new ArrayList<>();

        for (Map.Entry<User, Double> entry : balances.entrySet()) {
            if (entry.getValue() < -0.01) debtors.add(entry);
            else if (entry.getValue() > 0.01) creditors.add(entry);
        }

        int d = 0, c = 0;
        while (d < debtors.size() && c < creditors.size()) {
            Map.Entry<User, Double> debtor = debtors.get(d);
            Map.Entry<User, Double> creditor = creditors.get(c);

            double amount = Math.min(-debtor.getValue(), creditor.getValue());

            transactions.add(new Transaction(debtor.getKey(), creditor.getKey(), amount));

            debtor.setValue(debtor.getValue() + amount);
            creditor.setValue(creditor.getValue() - amount);

            if (Math.abs(debtor.getValue()) < 0.01) d++;
            if (Math.abs(creditor.getValue()) < 0.01) c++;
        }

        return EventReportDTO.builder()
                .eventTitle(event.getTitle())
                .expenses(expenses)
                .totalCost(totalCost)
                .balancesPerUser(balances.entrySet().stream()
                        .collect(Collectors.toMap(e -> e.getKey().getName(), Map.Entry::getValue)))
                .settlementTransactions(transactions)
                .build();
    }
}