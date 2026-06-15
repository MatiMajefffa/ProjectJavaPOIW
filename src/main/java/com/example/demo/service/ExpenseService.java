package com.example.demo.service;

import com.example.demo.dto.ExpenseRequest;
import com.example.demo.model.Expense;
import com.example.demo.model.Transaction;
import com.example.demo.model.User;
import com.example.demo.repository.ExpenseRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final SettlementEngine engine;
    private final EventService eventService;
    private final UserRepository userRepository;
    private final Map<User, Long> balances = new HashMap<>();

    public ExpenseService(ExpenseRepository expenseRepository, SettlementEngine engine,
                          EventService eventService, UserRepository userRepository) {
        this.expenseRepository = expenseRepository;
        this.engine = engine;
        this.eventService = eventService;
        this.userRepository = userRepository;
    }

    public void registerUser(User user) {
        balances.putIfAbsent(user, 0L);
    }

    @Transactional
    public Expense createExpense(Long eventId, ExpenseRequest request, String email) {
        // 1. Sprawdź czy event jest aktywny
        eventService.validateEventIsActive(eventId);

        // 2. Pobierz płatnika na podstawie e-maila (bezpieczne!)
        User payer = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Nie znaleziono użytkownika"));

        // 3. Pobierz uczestników
        List<User> participants = userRepository.findAllById(request.getParticipantIds());

        // 4. Dodaj wydatek przez metodę pomocniczą
        Expense expense = addExpense(eventId, request.getTitle(), payer, request.getAmount(), participants);

        return expense;
    }

    @Transactional
    public Expense addExpense(Long eventId, String description, User payer, double amount, List<User> participants) {
        if (amount <= 0) throw new IllegalArgumentException("Kwota musi być dodatnia");
        if (participants == null || participants.isEmpty())
            throw new IllegalArgumentException("Musi być co najmniej jeden uczestnik");

        Expense newExpense = Expense.builder()
                .eventId(eventId)
                .description(description)
                .payer(payer)
                .amount(amount)
                .participants(participants)
                .build();

        expenseRepository.save(newExpense);
        processExpense(payer, (long) (amount * 100), participants);

        return newExpense;
    }

    private void processExpense(User payer, long totalAmount, List<User> participants) {
        long perPerson = totalAmount / participants.size();
        long remainder = totalAmount % participants.size();

        balances.put(payer, balances.getOrDefault(payer, 0L) + totalAmount);

        for (int i = 0; i < participants.size(); i++) {
            User u = participants.get(i);
            long share = perPerson + (i == 0 ? remainder : 0);
            balances.put(u, balances.getOrDefault(u, 0L) - share);
        }
    }

    public Map<User, Long> getCurrentBalances() {
        return Collections.unmodifiableMap(balances);
    }

    public List<Transaction> calculateSettlements() {
        return engine.calculate(new HashMap<>(balances));
    }

    public List<Expense> getHistory(Long eventId) {
        return expenseRepository.findByEventId(eventId);
    }
}