package com.example.demo.repository;

import com.example.demo.model.Attendee;
import com.example.demo.model.Event;
import com.example.demo.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AttendeeRepository extends JpaRepository<Attendee, Long> {
    List<Attendee> findByEvent(Event event);
    boolean existsByEventAndUser(Event event, User user);
}