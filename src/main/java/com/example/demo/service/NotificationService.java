package com.example.demo.service;

import com.example.demo.model.Notification;
import com.example.demo.model.User;
import com.example.demo.repository.NotificationRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    // Pobiera powiadomienia po ID użytkownika
    public List<Notification> findAllByUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Użytkownik nie istnieje"));
        return notificationRepository.findByUserId(user.getId());
    }

    // Oznacza jako przeczytane z weryfikacją właściciela
    public void markAsRead(Long id, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Użytkownik nie istnieje"));

        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Powiadomienie nie istnieje"));

        if (!notification.getUserId().equals(user.getId())) {
            throw new RuntimeException("Brak uprawnień do tego powiadomienia");
        }

        notification.setRead(true);
        notificationRepository.save(notification);
    }

    // Oznacza wszystkie jako przeczytane dla danego użytkownika
    public void markAllAsRead(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Użytkownik nie istnieje"));

        List<Notification> notifications = notificationRepository.findByUserId(user.getId());
        for (Notification n : notifications) {
            n.setRead(true);
        }
        notificationRepository.saveAll(notifications);
    }

    // Usuwa powiadomienie z weryfikacją właściciela
    public void deleteNotification(Long id, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Użytkownik nie istnieje"));

        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Powiadomienie nie istnieje"));

        if (!notification.getUserId().equals(user.getId())) {
            throw new RuntimeException("Brak uprawnień do usunięcia tego powiadomienia");
        }

        notificationRepository.deleteById(id);
    }
}