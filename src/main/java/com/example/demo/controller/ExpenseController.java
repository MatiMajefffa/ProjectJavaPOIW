package com.example.demo.controller;

import com.example.demo.dto.ExpenseRequest;
import com.example.demo.service.EventService;
import com.example.demo.service.ExpenseService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.security.Principal; // <-- Konieczny import

@RestController
@RequestMapping("/api/expenses")
@CrossOrigin(origins = "*")
public class ExpenseController {

    private final ExpenseService expenseService;
    private final EventService eventService;

    public ExpenseController(ExpenseService expenseService, EventService eventService) {
        this.expenseService = expenseService;
        this.eventService = eventService;
    }

    /**
     * Endpoint służący do dodawania nowego wydatku do konkretnego wydarzenia.
     * Wymaga podania ID wydarzenia w ścieżce oraz danych wydatku w ciele zapytania.
     */
    @PostMapping("/{eventId}")
    public ResponseEntity<?> addExpense(@PathVariable Long eventId,
                                        @RequestBody ExpenseRequest request,
                                        Principal principal) { // <-- Dodane
        // Walidacja czy event jest aktywny – jeśli wydarzenie jest zamknięte, rzuci wyjątek
        eventService.validateEventIsActive(eventId);

        // Przekazujemy email zalogowanego użytkownika (z tokenu JWT) do serwisu,
        // aby przypisać go jako płatnika wydatku
        return ResponseEntity.ok(expenseService.createExpense(eventId, request, principal.getName()));
    }
}