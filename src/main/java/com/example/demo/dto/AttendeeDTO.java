package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendeeDTO {
    private Long id;
    private Long userId;
    private String name;
    private String email;
    private LocalDateTime joinedAt;
}