package com.example.demo.repository;

import com.example.demo.model.Attendee;
import com.example.demo.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AttendeeRepository extends JpaRepository<Attendee, Long> {
    List<Attendee> findByEvent(Event event); // To jest metoda, której używamy w serwisie!
}