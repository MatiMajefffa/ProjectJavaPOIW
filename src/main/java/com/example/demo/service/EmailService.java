package com.example.demo.service;

import jakarta.mail.internet.MimeMessage;
import jakarta.mail.util.ByteArrayDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Async
    public void sendInvitationEmail(String recipientEmail, String invitationLink, String eventName) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(recipientEmail);
            message.setSubject("Zaproszenie do: " + eventName);
            message.setText(
                    "Cześć!\n\n" +
                            "Zostałeś zaproszony do wydarzenia: " + eventName + "\n" +
                            "Kliknij poniższy link, aby dołączyć:\n\n" +
                            invitationLink + "\n\n" +
                            "Pozdrawiamy,\n" +
                            "Zespół EventSplit"
            );

            mailSender.send(message);
            System.out.println("✅Mail wysłany do: " + recipientEmail);
        } catch (Exception e) {
            System.err.println(" Błąd wysyłki maila: " + e.getMessage());
        }
    }

    @Async
    public void sendDebtNotification(String recipientEmail, String debtorName, double amount) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(recipientEmail);
            message.setSubject("EventSplit - Nowy dług");
            message.setText(
                    "Cześć!\n\n" +
                            debtorName + " dodał nowy wydatek.\n" +
                            "Twój dług: " + amount + " PLN\n\n" +
                            "Sprawdź szczegóły w aplikacji."
            );

            mailSender.send(message);

        } catch (Exception e) {
            System.err.println(" Błąd wysyłki powiadomienia: " + e.getMessage());
        }
    }

    // Nowa metoda obsługująca wysyłkę PDF jako załącznika
    @Async
    public void sendReportEmail(String to, String eventName, byte[] pdfBytes) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true); // true = multipart (załączniki)
            helper.setTo(to);
            helper.setSubject("Raport finansowy z wydarzenia: " + eventName);
            helper.setText("Cześć,\n\nW załączniku przesyłamy pełne sprawozdanie finansowe dla wydarzenia: " + eventName);
            // Dodanie pliku PDF jako załącznika
            helper.addAttachment("Raport_" + eventName + ".pdf", new ByteArrayDataSource(pdfBytes, "application/pdf"));

            mailSender.send(message);
            System.out.println("Raport PDF wysłany do: " + to);

        } catch (Exception e) {
            System.err.println("Błąd wysyłki raportu PDF: " + e.getMessage());
        }
    }
}