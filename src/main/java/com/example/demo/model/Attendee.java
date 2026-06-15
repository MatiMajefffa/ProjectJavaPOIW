package com.example.demo.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor; // Ten import jest kluczowy przy adnotacji @NoArgsConstructor
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "attendees")
@NoArgsConstructor
public class Attendee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "event_id")
    private Event event;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "invitation_token")
    private String invitationToken;

    @Column(name = "joined_at")
    private LocalDateTime joinedAt = LocalDateTime.now();


    public Attendee(Event event, User user) {
        this.event = event;
        this.user = user;
    }
}