package com.example.demo.controller;

import com.example.demo.dto.EventReportDTO;
import com.example.demo.model.Expense;
import com.example.demo.repository.ExpenseRepository;
import com.example.demo.service.EventService;
import com.example.demo.service.PdfReportService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reports")
@CrossOrigin(origins = "*")
public class ReportController {

    private final PdfReportService pdfReportService;
    private final ExpenseRepository expenseRepository;
    private final EventService eventService; // DODANO: wstrzyknięcie serwisu

    // Zaktualizowany konstruktor
    public ReportController(PdfReportService pdfReportService, ExpenseRepository expenseRepository, EventService eventService) {
        this.pdfReportService = pdfReportService;
        this.expenseRepository = expenseRepository;
        this.eventService = eventService;
    }

    // 1. Zwraca pełne rozliczenie z serwisu (JSON)
    @GetMapping("/{eventId}")
    public ResponseEntity<EventReportDTO> getReportData(@PathVariable Long eventId) {
        EventReportDTO report = eventService.generateReport(eventId);
        return ResponseEntity.ok(report);
    }

    // 2. Zwraca PDF
    @GetMapping("/{eventId}/pdf")
    public ResponseEntity<byte[]> downloadPdfReport(@PathVariable Long eventId) {
        try {
            // Wykorzystujemy tę samą metodę z serwisu dla spójności danych
            EventReportDTO report = eventService.generateReport(eventId);
            byte[] pdfBytes = pdfReportService.generateEventReport(report);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("filename", "raport-wydarzenia-" + eventId + ".pdf");

            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}