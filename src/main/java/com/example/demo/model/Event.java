package com.example.demo.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "events")
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private LocalDateTime date;
    private String location;
    private String type;

    @Column(name = "max_attendees")
    private Integer maxAttendees;

    @ManyToOne
    @JoinColumn(name = "organizer_id")
    private User organizer;

    @Column(length = 32, unique = true)
    private String joinCode;

    @Column(name = "is_closed")
    private boolean isClosed = false;

    // Lista uczestników
    @JsonIgnore // <-- DODAJ TO
    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL)
    private List<Attendee> attendees = new ArrayList<>();

    // Lista wydatków
    @JsonIgnore // <-- DODAJ TO
    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL)
    private List<Expense> expenses = new ArrayList<>();

    // Lista zadań
    @JsonIgnore // <-- DODAJ TO
    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL)
    private List<Task> tasks = new ArrayList<>();
}