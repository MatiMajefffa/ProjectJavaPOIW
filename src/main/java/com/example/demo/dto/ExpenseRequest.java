package com.example.demo.dto;

import lombok.Data;
import java.util.List;

@Data
public class ExpenseRequest {
    private String title;
    private double amount;
    private String splitType; // np. "EQUAL"
    private Long payerId;
    private List<Long> participantIds;
}