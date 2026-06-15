package com.example.demo.service;

import com.example.demo.model.*;
import com.example.demo.repository.*;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class TaskService {
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final AttendeeRepository attendeeRepository;

    public TaskService(TaskRepository taskRepository, UserRepository userRepository,
                       EventRepository eventRepository, AttendeeRepository attendeeRepository) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
        this.attendeeRepository = attendeeRepository;
    }


    private void validateAccess(Long eventId, String email) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("Użytkownik nie istnieje"));
        Event event = eventRepository.findById(eventId).orElseThrow(() -> new RuntimeException("Wydarzenie nie istnieje"));

        boolean isOrganizer = event.getOrganizer().equals(user);
        boolean isParticipant = attendeeRepository.existsByEventAndUser(event, user);

        if (!isOrganizer && !isParticipant) {
            throw new RuntimeException("Brak uprawnień do tego wydarzenia");
        }
    }

    public List<Task> getTasksByEvent(Long eventId, String email) {
        validateAccess(eventId, email);
        return taskRepository.findByEventId(eventId);
    }

    public Task addTask(Task task, String email) {
        validateAccess(task.getEventId(), email);
        return taskRepository.save(task);
    }

    public void completeTask(Long taskId, String email) {
        Task task = taskRepository.findById(taskId).orElseThrow();
        validateAccess(task.getEventId(), email);
        task.setCompleted(true);
        taskRepository.save(task);
    }

    public void assignTask(Long taskId, String targetEmail, String actorEmail) {
        Task task = taskRepository.findById(taskId).orElseThrow();
        validateAccess(task.getEventId(), actorEmail);

        User user = userRepository.findByEmail(targetEmail).orElseThrow();
        task.setAssignee(user);
        taskRepository.save(task);
    }
}