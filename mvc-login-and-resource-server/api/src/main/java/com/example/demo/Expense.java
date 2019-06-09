package com.example.demo;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor @Getter
public class Expense {

    private String description;
    private Double amount;
    private Boolean approved;

}
