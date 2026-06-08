package com.example.demo.controller;

import com.example.demo.dto.ExpenseRequest;
import com.example.demo.service.EventService;
import com.example.demo.service.ExpenseService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping("/{eventId}")
    public ResponseEntity<?> addExpense(@PathVariable Long eventId, @RequestBody ExpenseRequest request) {
        eventService.validateEventIsActive(eventId);

        return ResponseEntity.ok(expenseService.createExpense(eventId, request));
    }
}