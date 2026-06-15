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

import java.security.Principal;
import java.util.List;

/**
 * Kontroler odpowiedzialny za generowanie raportów finansowych z wydarzeń.
 * Umożliwia pobieranie danych rozliczeniowych w formacie JSON oraz eksport do pliku PDF.
 */
@RestController
@RequestMapping("/api/reports")
@CrossOrigin(origins = "*")
public class ReportController {

    private final PdfReportService pdfReportService;
    private final ExpenseRepository expenseRepository;
    private final EventService eventService;

    public ReportController(PdfReportService pdfReportService, ExpenseRepository expenseRepository, EventService eventService) {
        this.pdfReportService = pdfReportService;
        this.expenseRepository = expenseRepository;
        this.eventService = eventService;
    }

    /**
     * GET /api/reports/{eventId}
     * Zwraca surowe dane rozliczeniowe wydarzenia w formacie JSON.
     * Weryfikuje uprawnienia użytkownika (principal) do dostępu do danych danego wydarzenia.
     */
    @GetMapping("/{eventId}")
    public ResponseEntity<EventReportDTO> getReportData(@PathVariable Long eventId, Principal principal) {
        // Generowanie raportu z serwisu z weryfikacją e-maila użytkownika
        EventReportDTO report = eventService.generateReport(eventId, principal.getName());
        return ResponseEntity.ok(report);
    }

    /**
     * GET /api/reports/{eventId}/pdf
     * Generuje i zwraca plik PDF z pełnym raportem finansowym dla danego wydarzenia.
     * Ustawia odpowiednie nagłówki HTTP, aby przeglądarka rozpoznała plik jako PDF.
     */
    @GetMapping("/{eventId}/pdf")
    public ResponseEntity<byte[]> downloadPdfReport(@PathVariable Long eventId, Principal principal) {
        try {
            // Najpierw pobieramy dane raportu dla zweryfikowanego użytkownika
            EventReportDTO report = eventService.generateReport(eventId, principal.getName());

            // Konwersja danych raportu na dokument PDF
            byte[] pdfBytes = pdfReportService.generateEventReport(report);

            // Przygotowanie nagłówków odpowiedzi dla pobrania pliku
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("filename", "raport-wydarzenia-" + eventId + ".pdf");

            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);

        } catch (Exception e) {
            e.printStackTrace();
            // W razie wystąpienia błędu zwracamy status 500
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}