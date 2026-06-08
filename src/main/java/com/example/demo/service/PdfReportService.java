package com.example.demo.service;

import com.example.demo.dto.EventReportDTO;
import com.example.demo.model.Expense;
import com.example.demo.model.Transaction;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.Map;

@Service
public class PdfReportService {

    public byte[] generateEventReport(EventReportDTO report) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            // 1. Nagłówek
            document.add(new Paragraph("Raport Rozliczenia: " + report.getEventTitle()).setFontSize(24).setBold());
            document.add(new Paragraph("Całkowity koszt wydarzenia: " + String.format("%.2f", report.getTotalCost()) + " PLN"));
            document.add(new Paragraph(" "));

            // 2. Tabela Wydatków
            document.add(new Paragraph("Lista wydatków:").setBold());
            Table tableExpenses = new Table(new float[]{3, 2, 2});
            tableExpenses.addHeaderCell("Opis wydatku");
            tableExpenses.addHeaderCell("Kwota (PLN)");
            tableExpenses.addHeaderCell("Płacący");

            for (Expense exp : report.getExpenses()) {
                tableExpenses.addCell(exp.getDescription());
                tableExpenses.addCell(String.format("%.2f", exp.getAmount()));
                tableExpenses.addCell(exp.getPayer() != null ? exp.getPayer().getName() : "Nieznany");
            }
            document.add(tableExpenses);
            document.add(new Paragraph(" "));

            // 3. Bilans użytkowników
            document.add(new Paragraph("Bilans użytkowników (nadpłaty/niedopłaty):").setBold());
            for (Map.Entry<String, Double> entry : report.getBalancesPerUser().entrySet()) {
                document.add(new Paragraph(entry.getKey() + ": " + String.format("%.2f", entry.getValue()) + " PLN"));
            }
            document.add(new Paragraph(" "));

            // 4. Rozliczenie końcowe (kto komu wisi)
            document.add(new Paragraph("Jak wyrównać długi:").setBold());
            if (report.getSettlementTransactions() == null || report.getSettlementTransactions().isEmpty()) {
                document.add(new Paragraph("Brak długów - wszyscy są rozliczeni!"));
            } else {
                for (Transaction t : report.getSettlementTransactions()) {
                    // Poprawka dla recordu Transaction: używamy from(), to() oraz amount()
                    document.add(new Paragraph(t.from().getName() + " -> musi oddać -> " +
                            t.to().getName() + ": " +
                            String.format("%.2f", t.amount()) + " PLN"));
                }
            }

            document.close();
            return baos.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Błąd generowania pełnego PDF: " + e.getMessage());
        }
    }
}